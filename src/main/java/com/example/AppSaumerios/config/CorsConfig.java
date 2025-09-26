

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
            // URL real de Vercel
            configuration.setAllowedOriginPatterns(List.of(
                    "https://front-sahumerios-2.vercel.app",
                    "https://6000-firebase-studio-1758861612535.cluster-l2bgochoazbomqgfmlhuvdvgiy.cloudworkstations.dev\n" +
                            "\n"
            ));
        } else {
            // URLs de desarrollo
            configuration.setAllowedOrigins(Arrays.asList(
                    "http://localhost:9002"
            ));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); // clave para cookies HttpOnly
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
