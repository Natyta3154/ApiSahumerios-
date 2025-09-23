package com.example.AppSaumerios.jwrFilter;

import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long MAX_TOKEN_LENGTH = 2000;

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ✅ 1. PERMITIR PETICIONES OPTIONS (PREFLIGHT CORS) - ESTO ES CLAVE
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String requestId = UUID.randomUUID().toString();
        logger.debug("[{}] Procesando request: {}", requestId, request.getRequestURI());

        try {
            // ✅ 2. Si es un endpoint público (definido en shouldNotFilter), continuar sin autenticación
            if (shouldNotFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extraerToken(request);

            if (token != null) {
                if (!validarYConfigurarAutenticacion(token, response, requestId)) {
                    return; // Error ya manejado
                }
            } else {
                logger.debug("[{}] No se encontró token JWT para endpoint protegido", requestId);
                // ✅ 3. Para endpoints protegidos sin token, enviar error 401
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token requerido");
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("[{}] Error crítico en JwtFilter: {}", requestId, e.getMessage(), e);
            enviarErrorCritico(response, "Error interno de seguridad");
        }
    }

    private String extraerToken(HttpServletRequest request) {
        // ✅ 1. Primero verificar el header Authorization (estándar)
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty() && token.length() <= MAX_TOKEN_LENGTH && !token.equalsIgnoreCase("null")) {
                logger.debug("Token encontrado en Authorization header");
                return token;
            }
        }

        // ✅ 2. Fallback: verificar cookies (si usas cookies)
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.isBlank() && token.length() <= MAX_TOKEN_LENGTH && !token.equalsIgnoreCase("null")) {
                        logger.debug("Token encontrado en cookie");
                        return token.trim();
                    }
                }
            }
        }

        logger.debug("No se encontró token ni en header ni en cookie");
        return null;
    }

    private boolean validarYConfigurarAutenticacion(String token, HttpServletResponse response, String requestId) {
        try {
            // ✅ Validar formato básico del token
            if (token == null || token.trim().isEmpty()) {
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token vacío");
                return false;
            }

            // ✅ Obtenemos el ID y el rol del usuario desde el token JWT
            Long userId = jwtUtil.obtenerIdDesdeToken(token);
            String rol = jwtUtil.obtenerRolDesdeToken(token);

            // ✅ Validar que el token tenga la info necesaria
            if (userId == null || rol == null || rol.isBlank()) {
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token con información incompleta");
                return false;
            }

            // ✅ Aseguramos que el rol tenga el formato correcto para Spring Security
            String authority = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol.toUpperCase();

            // ✅ Creamos la autenticación
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(), // principal
                            null, // credentials
                            List.of(new SimpleGrantedAuthority(authority)) // authorities
                    );

            // ✅ Establecemos la autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("[{}] Usuario autenticado: ID={}, Rol={}", requestId, userId, authority);
            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("[{}] Token expirado", requestId);
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");
            return false;
        } catch (Exception e) {
            logger.warn("[{}] Token inválido: {}", requestId, e.getMessage());
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return false;
        }
    }

    private void enviarError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(String.format("{\"error\":\"unauthorized\",\"message\":\"%s\"}", message));
        } catch (IOException ignored) {
            logger.error("Error al enviar respuesta de error");
        }
    }

    private void enviarErrorCritico(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"internal_server_error\",\"message\":\"" + message + "\"}");
        } catch (IOException ignored) {
            logger.error("Error al enviar respuesta de error crítico");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // ✅ Permitir OPTIONS para todas las rutas (esto ya se maneja al inicio)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // ✅ Endpoints públicos que no requieren autenticación
        return path.equals("/usuarios/registrar") ||
                path.equals("/usuarios/login") ||
                path.equals("/productos/listado") ||
                path.startsWith("/productos/") || // Permite /productos/1, /productos/2, etc.
                path.equals("/api/ofertas/listar") ||
                path.startsWith("/api/ofertas/con-precio") ||
                path.equals("/atributos/listado");
    }
}