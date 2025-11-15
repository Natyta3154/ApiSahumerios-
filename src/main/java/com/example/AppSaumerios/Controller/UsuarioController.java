package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.UsuarioService;
import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(
        origins = {
                "http://localhost:8080",
                "http://localhost:9002",
                "https://app-aroman.vercel.app"
        },
        allowCredentials = "true"
)
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // ===================== REGISTRO =====================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Usuarios nuevoUsuario, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Datos inválidos"));
        }

        boolean emailExistente = usuarioService.obtenerUsuarios().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(nuevoUsuario.getEmail()));
        if (emailExistente) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        Usuarios guardado = usuarioService.guardar(nuevoUsuario);

        Map<String, Object> response = Map.of(
                "id", guardado.getId(),
                "nombre", guardado.getNombre(),
                "email", guardado.getEmail(),
                "rol", guardado.getRol(),
                "message", "Usuario registrado correctamente. Ahora puede iniciar sesión."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===================== LOGIN =====================
    // ===================== LOGIN =====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuarios credenciales, HttpServletResponse response) {
        Optional<Usuarios> usuarioOpt = usuarioService.login(
                credenciales.getEmail(),
                credenciales.getPassword()
        );

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        Usuarios usuario = usuarioOpt.get();

        // Generar access token y refresh token
        String accessToken = jwtUtil.generarToken(usuario.getId(), usuario.getRol());
        String refreshToken = jwtUtil.generarRefreshToken(usuario.getId());

        // Cookies configuradas según entorno
        ResponseCookie cookieAccess = ResponseCookie.from("token", accessToken)
                .httpOnly(true)
                .secure(!isDev()) // HTTPS solo en producción
                .sameSite(isDev() ? "Lax" : "None")
                .path("/")
                //.maxAge(Duration.ofHours(1))
                //.maxAge(usuario.getRol().equalsIgnoreCase("ADMIN") ? Duration.ofMinutes(30) : Duration.ofHours(1))
                .build();

        ResponseCookie cookieRefresh = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(!isDev())
                .sameSite(isDev() ? "Lax" : "None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        // Añadir cookies al response
        response.addHeader(HttpHeaders.SET_COOKIE, cookieAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieRefresh.toString());

        // Respuesta al frontend
        Map<String, Object> responseBody = Map.of(
                "usuario", Map.of(
                        "id", usuario.getId(),
                        "nombre", usuario.getNombre(),
                        "email", usuario.getEmail(),
                        "rol", usuario.getRol()
                )
        );

        return ResponseEntity.ok(responseBody);
    }

    // ===================== REFRESH TOKEN =====================

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                          HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No hay refresh token"));
        }

        if (!jwtUtil.validarToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token inválido"));
        }

        if (jwtUtil.estaExpirado(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token expirado"));
        }

        Long userId = jwtUtil.obtenerIdDesdeToken(refreshToken);
        Usuarios usuario = usuarioService.buscarPorId(userId);

        // Generar nuevo access token
        String nuevoAccessToken = jwtUtil.generarToken(usuario.getId(), usuario.getRol());

        ResponseCookie cookie = ResponseCookie.from("token", nuevoAccessToken)
                .httpOnly(true)
                .secure(!isDev())
                .sameSite(isDev() ? "Lax" : "None")
                .path("/")
                .maxAge(2 * 60 * 60) // 2 horas en segundos
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("message", "Access token renovado"));
    }


    // ===================== LOGOUT =====================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        boolean isProd = "prod".equals(System.getProperty("spring.profiles.active", "dev"));
        String domain = "apisahumerios-i8pd.onrender.com"; // dominio de la cookie original

        // Borrar cookies Access y Refresh
        ResponseCookie cookieAccess = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(isProd)
                .sameSite(isProd ? "None" : "Lax")
                .domain(domain)  // <--- aquí
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie cookieRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isProd)
                .sameSite(isProd ? "None" : "Lax")
                .domain(domain)  // <--- aquí
                .path("/")
                .maxAge(0)
                .build();

        logger.info("LOGOUT - DELETE COOKIES: {}, {}", cookieAccess, cookieRefresh);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieRefresh.toString());

        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }


    // ===================== PERFIL =====================

    @GetMapping("/perfil")
    @PermitAll
    public ResponseEntity<?> perfil(
            @CookieValue(value = "token", required = false) String token) {

        // Si no existe el token → no hay sesión
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No hay sesión activa"));
        }

        // Si el token existe pero está expirado
        if (jwtUtil.estaExpirado(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token expirado"));
        }

        try {
            Long userId = jwtUtil.obtenerIdDesdeToken(token);
            Usuarios usuario = usuarioService.buscarPorId(userId);

            Map<String, Object> userSafe = Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "email", usuario.getEmail(),
                    "rol", usuario.getRol()
            );

            return ResponseEntity.ok(
                    Map.of("usuario", userSafe)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido"));
        }
    }


    // ===================== ADMIN =====================
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/listaDeUser")
    public ResponseEntity<List<Usuarios>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/eliminarUser/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    // ===================== UTIL =====================
    private boolean isDev() {
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        if (env == null) env = System.getProperty("spring.profiles.active", "dev");
        return env.equalsIgnoreCase("dev");
    }
}

