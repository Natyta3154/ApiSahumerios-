package com.example.AppSaumerios.config;

import com.example.AppSaumerios.jwrFilter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
            // Usuarios
            "/usuarios/registrar", "/usuarios/login", "/usuarios/perfil", "/usuarios/logout", "/usuarios/refresh",
            // Productos
            "/api/productos/**", "/api/productos/destacados", "/api/productos/listado", "/api/productos/top5",
            "/productos/*", "/productos/resumen",
            // Ofertas
            "/api/ofertas/listar", "/api/ofertas/con-precio", "/api/ofertas/carrusel",
            // Blog y otros
            "/api/posts/listarPost", "/api/fragancias/listadoFragancias", "/api/categoria/listadoCat", "/api/atributos/listadoAtributos"
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

    // 🔹 Especificamos el bean correcto con @Qualifier
    private final @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigSource;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Seguridad básica
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Manejo de excepciones
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Acceso denegado\", \"message\": \"No tienes permisos para acceder a este recurso\"}");
                })
        );

        // Autorizaciones
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(ADMIN_URLS).hasAuthority("ROLE_ADMIN")
                .requestMatchers(USER_URLS).hasAuthority("ROLE_USER")
                .anyRequest().authenticated()
        );

        // JWT Filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
