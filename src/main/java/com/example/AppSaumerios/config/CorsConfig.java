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
                // Permitir requests a todos los endpoints
                registry.addMapping("/**")
                        // Orígenes permitidos (frontend local)
                        .allowedOrigins("http://localhost:9002", "http://127.0.0.1:5500",
                                "http://127.0.0.1:5500",
                                "https://apisahumerios.onrender.com",
                                "https://6000-firebase-studio-1756885120718.cluster-f73ibkkuije66wssuontdtbx6q.cloudworkstations.dev")
                        // Métodos HTTP permitidos
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Permitir todos los headers
                        .allowedHeaders("*")
                        // Permitir envío de credenciales (cookies, auth)
                        .allowCredentials(true);
            }
        };
    }
}
