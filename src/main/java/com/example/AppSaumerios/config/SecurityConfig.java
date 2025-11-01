package com.example.AppSaumerios.config;

import com.example.AppSaumerios.jwrFilter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] PUBLIC_URLS = {
            "/", "/favicon.ico",
            "/usuarios/registrar", "/usuarios/login", "/usuarios/perfil", "/usuarios/logout", "/usuarios/refresh",
            "/api/productos/**", "/api/productos/destacados", "/api/productos/listado", "/productos/*", "/productos/resumen",
            "/api/ofertas/listar", "/api/ofertas/con-precio", "/api/ofertas/carrusel",
            "/api/posts/**", "/api/fragancias/**", "/api/categorias/**", "/api/atributos/**", "/api/productos/top5"
    };

    private static final String[] ADMIN_URLS = {
            "/admin/**",
            "/usuarios", "/usuarios/listaDeUser", "/usuarios/{id}", "/usuarios/agregarUser",
            "/usuarios/editarUser/{id}", "/usuarios/eliminarUser/{id}",
            "/productos/agregar", "/productos/editar/{id}", "/productos/eliminar/{id}",
            "/api/ofertas", "/api/ofertas/editar/{id}", "/api/ofertas/crearOferta", "/api/ofertas/eliminar/{id}",
            "/pedidos/admin", "/pedidos/{id}/estado",
            "/atributos", "/atributos/agregar", "/atributos/editar/{id}", "/atributos/eliminar/{id}",
            "/detallePedidos/admin/{id}"
    };

    private static final String[] USER_URLS = {
            "/pedidos/realizarPedido",
            "/pedidos/realizarPedidoConPago",
            "/api/pagos/**"
    };

    private final JwtFilter jwtFilter;
    private final CorsConfigurationSource corsConfigSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureBasicSecurity(http);
        configureExceptionHandling(http);
        configureAuthorization(http);
        configurejwtFilter(http);
        return http.build();
    }

    private void configureBasicSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Acceso denegado\", \"message\": \"No tienes permisos para acceder a este recurso\"}");
                }));
    }

    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(ADMIN_URLS).hasAuthority("ROLE_ADMIN")
                .requestMatchers(USER_URLS).hasAuthority("ROLE_USER")
                .anyRequest().authenticated()
        );
    }

    private void configurejwtFilter(HttpSecurity http) {
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}