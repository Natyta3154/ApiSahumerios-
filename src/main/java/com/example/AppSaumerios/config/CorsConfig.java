package com.example.AppSaumerios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    private static final String PROD_FRONTEND = "https://app-aroman.vercel.app"; // tu frontend en Vercel
    private static final String PROD_BACKEND = "https://apisahumerios-i8pd.onrender.com"; // tu backend en Render

    private static final List<String> DEV_URLS = List.of(
            "http://localhost:9002",
            "http://localhost:8080"
    );

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔹 Siempre permitimos producción + local
        configuration.setAllowedOriginPatterns(List.of(
                PROD_FRONTEND,
                PROD_BACKEND,
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
