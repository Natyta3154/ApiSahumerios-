package com.example.AppSaumerios.config;

import com.example.AppSaumerios.jwrFilter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilterUtil;

    // Constructor correctamente inicializado
    public SecurityConfig(JwtFilter jwtFilterUtil) {
        this.jwtFilterUtil = jwtFilterUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Acceso denegado\", \"message\": \"No tienes permisos para acceder a este recurso\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/usuarios/registrar", "/usuarios/login").permitAll()
                        .requestMatchers(
                                "/productos/listado",
                                "/productos/*",
                                "/api/ofertas/listar",
                                "/api/ofertas/con-precio",
                                "/atributos/listado",
                                "/detallePedidos/{pedidoId}",
                                "/detallePedidos",
                                "/api/webhook/**"

                        ).permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(
                                "/usuarios",
                                "/usuarios/listaDeUser",
                                "/usuarios/{id}",
                                "/usuarios/agregarUser",
                                "/usuarios/editarUser/{id}",
                                "/usuarios/eliminarUser/{id}",
                                "/productos/agregar",
                                "/productos/editar/{id}",
                                "/productos/eliminar/{id}",
                                "/api/ofertas",
                                "/api/ofertas/editar/{id}",
                                "/api/ofertas/crearOferta",
                                "/api/ofertas/eliminar/{id}",
                                "/pedidos/admin",
                                "/pedidos/{id}/estado",
                                "/atributos",
                                "/atributos/agregar",
                                "/atributos/editar/{id}",
                                "/atributos/eliminar/{id}",
                                "/detallePedidos/admin/{id}"
                        ).hasAuthority("ROLE_ADMIN")
                        .requestMatchers(
                                "/pedidos/realizarPedido",
                                "/pedidos/realizarPedidoConPago",
                                "/pedidos/**"
                        ).hasAuthority("ROLE_USER")  // Cambiado de hasRole("USER") a hasAuthority("ROLE_USER")
                        .requestMatchers("/api/pagos/**").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilterUtil, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:9002",
                "https://api-sahumerios.vercel.app",
                "https://hernan.alwaysdata.net",
                "https://apisahumerios.onrender.com",
                "https://6000-firebase-studio-1756885120718.cluster-f73ibkkuije66wssuontdtbx6q.cloudworkstations.dev"
        ));
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
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
