package com.example.AppSaumerios.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

    // Duraciones en milisegundos
    private static final long EXPIRATION_USER_MS = 1000 * 60 * 60;          // 1 hora
    private static final long EXPIRATION_ADMIN_MS = 1000 * 60 * 30;         // 30 minutos
    private static final long REFRESH_TOKEN_EXP_MS = 1000 * 60 * 60 * 24 * 7; // 7 días

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // ================== ACCESS TOKEN ==================
    public String generarToken(Long id, String rol) {
        if (rol == null) rol = "ROLE_USER";
        if (!rol.startsWith("ROLE_")) rol = "ROLE_" + rol.toUpperCase();
        else rol = rol.toUpperCase();

        long expirationTime = rol.equals("ROLE_ADMIN") ? EXPIRATION_ADMIN_MS : EXPIRATION_USER_MS;

        return Jwts.builder()
                .setSubject(id.toString())
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ================== REFRESH TOKEN ==================
    public String generarRefreshToken(Long id) {
        return Jwts.builder()
                .setSubject(id.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ================== EXTRACCIÓN ==================
    public Long obtenerIdDesdeToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public String obtenerRolDesdeToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("rol", String.class);
    }

    // ================== VALIDACIÓN ==================
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

    public boolean estaExpirado(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}


