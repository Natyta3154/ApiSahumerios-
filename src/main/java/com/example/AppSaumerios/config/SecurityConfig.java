package com.example.AppSaumerios.config;

import com.example.AppSaumerios.jwrFilter.JwtFilter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Data
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilterUtil;
    private final CorsConfigurationSource corsConfigSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Acceso denegado\", \"message\": \"No tienes permisos para acceder a este recurso\"}");
                        })).authorizeHttpRequests(auth -> auth
                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Rutas públicas
                        .requestMatchers("/", "/favicon.ico", "/usuarios/registrar", "/usuarios/login", "/usuarios/perfil", "/usuarios/logout", "/usuarios/refresh").permitAll()
                        .requestMatchers("/api/productos/**","/api/productos/destacados","/api/productos/listado","/productos/*","/productos/resumen","/api/ofertas/listar","/api/ofertas/con-precio","/api/ofertas/carrusel").permitAll()

                        .requestMatchers("/api/productos/**").permitAll()
                        .requestMatchers("/api/posts/**").permitAll()
                        .requestMatchers("/api/fragancias/**").permitAll()
                        .requestMatchers("/api/categorias/**").permitAll()
                        .requestMatchers("/api/atributos/**").permitAll()
                        .requestMatchers("/api/productos/top5").permitAll()



                        // Admin
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(
                                "/usuarios", "/usuarios/listaDeUser", "/usuarios/{id}", "/usuarios/agregarUser",
                                "/usuarios/editarUser/{id}", "/usuarios/eliminarUser/{id}",
                                "/productos/agregar", "/productos/editar/{id}", "/productos/eliminar/{id}",
                                "/api/ofertas", "/api/ofertas/editar/{id}", "/api/ofertas/crearOferta", "/api/ofertas/eliminar/{id}",
                                "/pedidos/admin", "/pedidos/{id}/estado",
                                "/atributos", "/atributos/agregar", "/atributos/editar/{id}", "/atributos/eliminar/{id}",
                                "/detallePedidos/admin/{id}"
                        ).hasAuthority("ROLE_ADMIN")
                        // Usuario normal
                        .requestMatchers("/pedidos/realizarPedido", "/pedidos/realizarPedidoConPago", "/api/pagos/**").hasAuthority("ROLE_USER")
                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                // JWT Filter
                .addFilterBefore(jwtFilterUtil, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
