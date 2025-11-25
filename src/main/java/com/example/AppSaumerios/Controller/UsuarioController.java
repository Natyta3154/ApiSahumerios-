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

        // CORRECCIÓN: Usamos existsPorEmail (más rápido que traer toda la lista)
        if (usuarioService.existePorEmail(nuevoUsuario.getEmail())) {
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

        String accessToken = jwtUtil.generarToken(usuario.getId(), usuario.getRol());
        String refreshToken = jwtUtil.generarRefreshToken(usuario.getId());

        ResponseCookie cookieAccess = ResponseCookie.from("token", accessToken)
                .httpOnly(true)
                .secure(!isDev())
                .sameSite(isDev() ? "Lax" : "None")
                .path("/")
                .build();

        ResponseCookie cookieRefresh = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(!isDev())
                .sameSite(isDev() ? "Lax" : "None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookieAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieRefresh.toString());

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

        if (!jwtUtil.validarToken(refreshToken) || jwtUtil.estaExpirado(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token inválido o expirado"));
        }

        Long userId = jwtUtil.obtenerIdDesdeToken(refreshToken);
        Usuarios usuario = usuarioService.buscarPorId(userId);

        String nuevoAccessToken = jwtUtil.generarToken(usuario.getId(), usuario.getRol());

        ResponseCookie cookie = ResponseCookie.from("token", nuevoAccessToken)
                .httpOnly(true)
                .secure(!isDev())
                .sameSite(isDev() ? "Lax" : "None")
                .path("/")
                .maxAge(2 * 60 * 60)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("message", "Access token renovado"));
    }

    // ===================== LOGOUT =====================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        boolean isProd = !isDev();

        // CORRECCIÓN: Dominio dinámico. Null para localhost, url real para prod.
        String domain = isProd ? "apisahumerios-i8pd.onrender.com" : null;

        ResponseCookie cookieAccess = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(isProd)
                .sameSite(isProd ? "None" : "Lax")
                .domain(domain)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie cookieRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isProd)
                .sameSite(isProd ? "None" : "Lax")
                .domain(domain)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookieAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieRefresh.toString());

        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

    // ===================== PERFIL =====================
    @PutMapping("/perfil")
    @PermitAll
    public ResponseEntity<?> actualizarPerfil(
            @CookieValue(value = "token", required = false) String token,
            @RequestBody Map<String, Object> datosActualizados) {

        if (token == null || jwtUtil.estaExpirado(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No hay sesión o token expirado"));
        }

        try {
            Long userId = jwtUtil.obtenerIdDesdeToken(token);

            // CORRECCIÓN: Usamos el método seguro que NO re-encripta la contraseña
            Usuarios usuario = usuarioService.actualizarDatosPersonales(userId, datosActualizados);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Perfil actualizado correctamente",
                    "usuario", Map.of(
                            "id", usuario.getId(),
                            "nombre", usuario.getNombre(),
                            "email", usuario.getEmail(),
                            "rol", usuario.getRol()
                    )
            ));

        } catch (Exception e) {
            logger.error("Error actualizando perfil", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el perfil"));
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
        return env != null && env.equalsIgnoreCase("dev");
    }
}
