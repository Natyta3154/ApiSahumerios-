package com.example.AppSaumerios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ============================================
 * Configuración CORS para permitir requests
 * desde frontend externo (React/HTML).
 * ============================================
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                // Detectar el entorno
                String profile = System.getProperty("spring.profiles.active", "dev");
                String frontendUrl;

                if ("prod".equals(profile)) {
                    // Producción: tu frontend alojado en Render
                    frontendUrl = "https://apisahumerios.onrender.com";
                } else {
                    // Desarrollo: localhost
                    frontendUrl = "http://localhost:9002";
                }

                registry.addMapping("/**")
                        .allowedOrigins(frontendUrl)   // Solo un origen permitido cuando allowCredentials=true
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);      // Permitir envío de cookies HttpOnly
            }
        };
    }
}
