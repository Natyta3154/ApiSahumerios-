package com.example.AppSaumerios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea todas las solicitudes a /imagenes/** a la carpeta local ./imagenes/
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("file:./imagenes/") // ruta física de tus imágenes
                .setCachePeriod(3600) // opcional, cache de 1 hora
                .resourceChain(true);
    }
}