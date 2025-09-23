package com.example.AppSaumerios.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60 * 2; // 2 horas

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera token JWT con id de usuario y rol
     */
    public String generarToken(Long id, String rol) {
        // Normalizar rol al formato correcto
        if (rol == null) rol = "ROLE_USER";
        if (!rol.startsWith("ROLE_")) rol = "ROLE_" + rol.toUpperCase();
        else rol = rol.toUpperCase();

        return Jwts.builder()
                .setSubject(id.toString())
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae id del token
     */
    public Long obtenerIdDesdeToken(String token) {
        String id = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(id);
    }

    /**
     * Extrae rol del token
     */
    public String obtenerRolDesdeToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("rol", String.class);
    }

    /**
     * Valida si el token sigue siendo válido (no expirado)
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
