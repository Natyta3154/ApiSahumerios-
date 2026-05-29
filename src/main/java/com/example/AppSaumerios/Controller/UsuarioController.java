package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.UsuarioService;
import com.example.AppSaumerios.dto.ForgotPasswordRequest; // 💡 Importar DTO
import com.example.AppSaumerios.dto.ResetPasswordRequest;   // 💡 Importar DTO
import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
        // ... (código existente de register) ...
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Datos inválidos"));
        }

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
        // ... (código existente de login) ...
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
                ),
                "mensaje", "Inicio de sesión exitoso"
        );

        return ResponseEntity.ok(responseBody);
    }

    // ===================== REFRESH TOKEN =====================
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                          HttpServletResponse response) {
        // ... (código existente de refresh) ...
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
        // ... (código existente de logout) ...
        boolean isProd = !isDev();
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
    public ResponseEntity<?> actualizarPerfil(
            Authentication authentication,
            @RequestBody Map<String, Object> datosActualizados) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No hay sesión o token expirado"));
        }

        try {
            Long userId = Long.parseLong((String) authentication.getPrincipal());
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

    // ===============================================================
    // 🔑 ENDPOINTS DE RESTABLECIMIENTO DE CONTRASEÑA
    // ===============================================================

    /**
     * POST /usuarios/forgot-password/olvido contraseña
     * Solicita el envío de un enlace de restablecimiento al email proporcionado.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        // Llamamos al servicio. El servicio maneja la lógica de buscar, generar token, y enviar email.
        usuarioService.createPasswordResetToken(request.getEmail());

        // Respuesta genérica de éxito por motivos de seguridad
        return ResponseEntity.ok(
                Map.of("message", "Si la si el correo existe, se ha enviado un enlace de restablecimiento al correo electrónico.")
        );
    }

    /**
     * POST /usuarios/reset-password
     * Restablece la contraseña usando el token y el email de los query parameters.
     * La nueva contraseña va en el cuerpo de la solicitud.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("email") String email,
            @Valid @RequestBody ResetPasswordRequest request) {

        if (token == null || token.isBlank() || email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token o email faltante."));
        }

        try {
            // Usamos la nueva contraseña del body del request
            boolean success = usuarioService.resetPassword(token, email, request.getNewPassword());

            if (success) {
                return ResponseEntity.ok(Map.of("message", "Contraseña restablecida con éxito."));
            } else {
                // Token no válido, expirado o email incorrecto.
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("error", "El enlace de restablecimiento es inválido o ha expirado. Por favor, solicita uno nuevo.")
                );
            }
        } catch (Exception e) {
            logger.error("Error al procesar restablecimiento de contraseña", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al intentar restablecer la contraseña."));
        }
    }


    // ===================== UTIL =====================
    private boolean isDev() {
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        if (env == null) env = System.getProperty("spring.profiles.active", "dev");
        return env != null && env.equalsIgnoreCase("dev");
    }
}