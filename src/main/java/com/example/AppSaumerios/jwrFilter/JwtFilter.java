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

        final String requestId = UUID.randomUUID().toString();
        logger.debug("[{}] Procesando request: {} {}", requestId, request.getMethod(), request.getRequestURI());

        try {
            String token = extraerToken(request);

            if (token != null) {
                if (!validarYConfigurarAutenticacion(token, response, requestId)) {
                    return; // error ya enviado al cliente
                }
            } else {
                logger.debug("[{}] No se encontró token JWT para endpoint protegido", requestId);
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
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty() && token.length() <= MAX_TOKEN_LENGTH && !"null".equalsIgnoreCase(token)) {
                logger.debug("Token encontrado en Authorization header");
                return token;
            }
        }

        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.isBlank() && token.length() <= MAX_TOKEN_LENGTH && !"null".equalsIgnoreCase(token)) {
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
            if (token == null || token.trim().isEmpty()) {
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token vacío");
                return false;
            }

            Long userId = jwtUtil.obtenerIdDesdeToken(token);
            String rol = jwtUtil.obtenerRolDesdeToken(token);

            if (userId == null || rol == null || rol.isBlank()) {
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token con información incompleta");
                return false;
            }

            String authority = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol.toUpperCase();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );

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
            response.flushBuffer(); // evita loops infinitos
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
            response.flushBuffer(); // evita loops infinitos
        } catch (IOException ignored) {
            logger.error("Error al enviar respuesta de error crítico");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Evitar loop en endpoints de error
        if (path.startsWith("/error")) return true;

        // Siempre permitir preflight
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // Endpoints públicos de usuarios
        if (path.equals("/usuarios/registrar") || path.equals("/usuarios/login")) return true;

        // Endpoints públicos de productos
        if ("GET".equalsIgnoreCase(method) && (
                path.equals("/api/productos") ||
                        path.equals("/api/productos/resumen") ||
                        path.equals("/api/productos/listado") ||
                        path.equals("/api/productos/destacados") ||
                        path.matches("/api/productos/\\d+")
        )) return true;

        // Blog
        if (path.startsWith("/api/posts")) return true;
        if (path.startsWith("/api/categoria-blog/listarCategoriaBlog")) return true;

        // Fragancias
        if (path.startsWith("/api/fragancias/listadoFragancias")) return true;

        //categoria public
        if (path.equals("/atributos/listado")) {
            return true; // No filtrar JWT
        }


        // Ofertas / Atributos
        if (path.equals("/api/ofertas/listar") ||
                path.startsWith("/api/ofertas/con-precio") ||
                path.equals("/api/ofertas/carrusel") ||
                path.equals("/api/atributos/listadoAtributos") ||
                path.equals("/api/categorias/listado")
        ) return true;

        // Todo lo demás requiere JWT
        return false;
    }
}

