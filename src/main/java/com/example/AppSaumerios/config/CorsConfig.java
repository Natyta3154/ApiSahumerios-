

package com.example.AppSaumerios.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
@Configuration
public class CorsConfig {

    @Bean("corsConfigSource")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String profile = System.getProperty("spring.profiles.active", "dev");

        if ("prod".equals(profile)) {
            // URLs reales de producción
            configuration.setAllowedOriginPatterns(List.of(

                    "https://app-aroman.vercel.app"
            ));
        } else {
            // URLs de desarrollo
            configuration.setAllowedOrigins(List.of(
                    "http://localhost:9002",
                    "http://localhost:8080"
            ));
        }

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers permitidos en la petición
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Permite enviar cookies
        configuration.setAllowCredentials(true);

        // Headers que el frontend puede leer
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // Duración del preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
