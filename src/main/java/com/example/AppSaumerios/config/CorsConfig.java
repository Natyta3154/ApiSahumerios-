package com.example.AppSaumerios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${frontend.url.prod}")
    private String prodFrontend;

    @Value("${frontend.url.dev}")
    private String devFrontend;

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔹 Siempre permitimos producción + local configurado en properties
        configuration.setAllowedOriginPatterns(List.of(
                prodFrontend,
                devFrontend,
                "https://apisahumerios-i8pd.onrender.com",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        // 🔍 Log útil para verificar en Render qué CORS se aplicó
        System.out.println("✅ CORS configurado para orígenes: " + configuration.getAllowedOriginPatterns());

        return source;
    }
}
