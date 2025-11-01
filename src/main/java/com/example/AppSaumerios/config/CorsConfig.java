package com.example.AppSaumerios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    private static final String PROD_URL = "https://app-aroman.vercel.app";
    private static final List<String> DEV_URLS = List.of(
            "http://localhost:9002",
            "http://localhost:8080"
    );
    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    );
    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
    );
    private static final List<String> EXPOSED_HEADERS = List.of(
            "Authorization",
            "Content-Type"
    );
    private static final long MAX_AGE = 3600L;
    private static final String ALL_PATHS_PATTERN = "/**";

    private enum Environment {
        DEV, PROD;

        static Environment getCurrent() {
            String profile = System.getProperty("spring.profiles.active", "dev");
            return "prod".equals(profile) ? PROD : DEV;
        }
    }

    @Bean("corsConfigSource")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = createCorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(ALL_PATHS_PATTERN, configuration);
        return source;
    }

    //
    private CorsConfiguration createCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configureOrigins(configuration);
        configureMethods(configuration);
        configureHeaders(configuration);
        configureGeneral(configuration);
        return configuration;
    }

    private void configureOrigins(CorsConfiguration configuration) {
        if (Environment.getCurrent() == Environment.PROD) {
            configuration.setAllowedOrigins(List.of(PROD_URL)); // origen exacto
        } else {
            configuration.setAllowedOrigins(DEV_URLS);
        }
    }

    private void configureMethods(CorsConfiguration configuration) {
        configuration.setAllowedMethods(ALLOWED_METHODS);
    }

    private void configureHeaders(CorsConfiguration configuration) {
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setExposedHeaders(EXPOSED_HEADERS);
    }

    private void configureGeneral(CorsConfiguration configuration) {
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(MAX_AGE);
    }
}