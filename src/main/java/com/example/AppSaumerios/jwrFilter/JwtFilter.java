package com.example.AppSaumerios.jwrFilter;

import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

        final String requestId = UUID.randomUUID().toString();
        logger.debug("[{}] Procesando request: {}", requestId, request.getRequestURI());

        try {
            String token = extraerToken(request);

            if (token != null) {
                if (!validarYConfigurarAutenticacion(token, response, requestId)) {
                    return; // Error ya manejado
                }
            } else {
                logger.debug("[{}] No se encontró token JWT", requestId);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("[{}] Error crítico en JwtFilter: {}", requestId, e.getMessage());
            enviarErrorCritico(response, "Error interno de seguridad");
        }
    }

    private String extraerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null) {
            return null;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            logger.warn("Formato de Authorization header inválido");
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // Validaciones básicas del token
        if (token.isEmpty()) {
            logger.warn("Token vacío después de Bearer prefix");
            return null;
        }

        if (token.length() > MAX_TOKEN_LENGTH) {
            logger.warn("Token demasiado largo: {} caracteres", token.length());
            return null;
        }

        // Validar formato básico del token JWT (debe tener 3 partes separadas por puntos)
        if (!token.matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")) {
            logger.warn("Formato de token JWT inválido");
            return null;
        }

        return token;
    }

    private boolean validarYConfigurarAutenticacion(String token, HttpServletResponse response, String requestId) {
        try {
            // 1. Validaciones iniciales
            if (token.isBlank()) {
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token vacío");
                return false;
            }

            // 2. Validar token intentando extraer información
            // En tu JwtUtil actual, la validación ocurre al extraer los claims
            Long userId = jwtUtil.obtenerIdDesdeToken(token);
            String rol = jwtUtil.obtenerRolDesdeToken(token);

            // 3. Validar información esencial
            if (userId == null || rol == null || rol.isBlank()) {
                logger.warn("[{}] Token con información incompleta", requestId);
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token con información incompleta");
                return false;
            }

            // 4. Validar formato del rol
            if (!isRolValido(rol)) {
                logger.warn("[{}] Rol inválido en token: {}", requestId, rol);
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Rol inválido");
                return false;
            }

            // 5. Validar que el ID sea positivo
            if (userId <= 0) {
                logger.warn("[{}] ID de usuario inválido: {}", requestId, userId);
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "ID de usuario inválido");
                return false;
            }

            // 6. Crear autenticación segura
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            List.of(new SimpleGrantedAuthority(rol))
                    );

            // 7. Establecer en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("[{}] Usuario autenticado: ID={}, Rol={}", requestId, userId, rol);
            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("[{}] Token expirado: {}", requestId, e.getMessage());
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.warn("[{}] Token malformado: {}", requestId, e.getMessage());
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token malformado");
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.warn("[{}] Firma de token inválida: {}", requestId, e.getMessage());
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Firma de token inválida");
            return false;
        } catch (NumberFormatException e) {
            logger.warn("[{}] Formato de ID inválido en token: {}", requestId, e.getMessage());
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Formato de token inválido");
            return false;
        } catch (Exception e) {
            logger.error("[{}] Error validando token: {}", requestId, e.getMessage());
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return false;
        }
    }

    private boolean isRolValido(String rol) {
        // Validar que el rol esté en la lista de roles permitidos
        return List.of("ROLE_ADMIN", "ROLE_USER", "ROLE_EMPLEADO", "ROLE_CLIENT").contains(rol.toUpperCase());
    }

    private void enviarError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");

            String jsonResponse = String.format(
                    "{\"error\": \"unauthorized\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                    message, new java.util.Date().toString()
            );

            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

        } catch (IOException e) {
            logger.error("Error enviando respuesta de error: {}", e.getMessage());
        }
    }

    private void enviarErrorCritico(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"internal_server_error\", \"message\": \"" + message + "\"}");
        } catch (IOException e) {
            // Fallback silencioso
        }
    }

    // No aplicar filtro a endpoints públicos
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/usuarios/registrar") ||
                path.startsWith("/usuarios/login") ||
                path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/productos/listado") ||
                path.startsWith("/productos/detalles/") ||
                path.equals("/error") ||
                path.equals("/favicon.ico");
    }
}