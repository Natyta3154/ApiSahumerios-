package com.example.AppSaumerios.jwrFilter;

import com.example.AppSaumerios.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
        // 1️⃣ Intentar leer token desde cookie
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.isBlank() && token.length() <= MAX_TOKEN_LENGTH) {
                        return token.trim();
                    }
                }
            }
        }

        // 2️⃣ Fallback: leer Authorization header si no había cookie
        /*String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty() && token.length() <= MAX_TOKEN_LENGTH) {
                return token;
            }
        }*/

        // 3️⃣ No se encontró token
        return null;
    }




    private boolean validarYConfigurarAutenticacion(String token, HttpServletResponse response, String requestId) {
        try {
            // Obtenemos el ID y el rol del usuario desde el token JWT
            Long userId = jwtUtil.obtenerIdDesdeToken(token);
            String rol = jwtUtil.obtenerRolDesdeToken(token);

            // Validamos que el token tenga la info necesaria
            if (userId == null || rol == null || rol.isBlank()) {
                enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token con información incompleta");
                return false;
            }

            // 🔹 Aseguramos que el rol tenga el prefijo ROLE_ que Spring Security necesita
            String authority = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;

            // Creamos la autenticación con el rol convertido a GrantedAuthority
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId.toString(),
                            null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );

            // Colocamos la autenticación en el contexto de Spring Security
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("[{}] Usuario autenticado: ID={}, Rol={}", requestId, userId, authority);
            return true;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");
            return false;
        } catch (Exception e) {
            enviarError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return false;
        }
    }


    private void enviarError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.getWriter().write(String.format("{\"error\":\"unauthorized\",\"message\":\"%s\"}", message));
            response.getWriter().flush();
        } catch (IOException ignored) {}
    }

    private void enviarErrorCritico(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"internal_server_error\",\"message\":\"" + message + "\"}");
        } catch (IOException ignored) {}
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/productos/listado") ||
                path.equals("/api/ofertas/listar") ||
                path.startsWith("/api/ofertas/con-precio") ||
                path.equals("/usuarios/registrar") ||
                path.equals("/usuarios/login") ||
                path.equals("/atributos/listado") ||
                path.startsWith("/detallePedidos");
    }
}
