package com.example.AppSaumerios.util;

import com.example.AppSaumerios.entity.Usuarios;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import static javax.crypto.Cipher.SECRET_KEY;

// ============================================
// JwtUtil.java
// Utilidades para generar y validar tokens JWT.
// Incluye generación de token y extracción de ID y rol.
// ============================================

@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    private String SECRET_KEY;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    // Genera token JWT con id y rol
    public String generarToken(Long id, String rol) {
        return Jwts.builder()
                .setSubject(id.toString())
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(getSigningKey())
                .compact();
    }

    // Extrae id del token
    public Long obtenerIdDesdeToken(String token) {
        String id = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(id);
    }

    // Extrae rol del token
    public String obtenerRolDesdeToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("rol", String.class);
    }
}
