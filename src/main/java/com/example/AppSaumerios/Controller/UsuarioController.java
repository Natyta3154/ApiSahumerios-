package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.UsuarioService;
import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.util.JwtUtil;
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
                "http://localhost:9002",
                "https://front-sahumerios-2.vercel.app",
                "https://app-sahumerio3.vercel.app" // tu dominio de producción
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

        // Validar que no exista otro usuario con el mismo email
        boolean emailExistente = usuarioService.obtenerUsuarios().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(nuevoUsuario.getEmail()));
        if (emailExistente) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        // Guardar usuario (el service ya encripta password y setea rol USER por defecto)
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
        String token = jwtUtil.generarToken(usuario.getId(), usuario.getRol());

        // 🔹 Cookie para producción cross-site
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)              // obligatorio en producción HTTPS
                .sameSite("None")          // necesario para cross-site
                .path("/")
                .maxAge(Duration.ofHours(2))
                .build();

        logger.info("SET-COOKIE HEADER: {}", cookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 🔹 Redirección según rol
       /* String redirectUrl;
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            redirectUrl = "/admin";
        } else {
            redirectUrl = "/productos";
        }*/


        Map<String, Object> responseBody = Map.of(
                "usuario", Map.of(
                        "id", usuario.getId(),
                        "nombre", usuario.getNombre(),
                        "email", usuario.getEmail(),
                        "rol", usuario.getRol()
                )
                //"redirect", redirectUrl
        );

        return ResponseEntity.ok(responseBody);
    }

    // ===================== LOGOUT =====================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        boolean isProd = "prod".equals(System.getProperty("spring.profiles.active", "dev"));

        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(isProd)             // solo HTTPS en prod
                .sameSite(isProd ? "None" : "Lax") // Lax funciona en dev
                .path("/")
                .maxAge(0)
                .build();

        logger.info("LOGOUT - DELETE COOKIE: {}", cookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

    // ===================== PERFIL =====================
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(@CookieValue(value = "token", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay sesión activa");
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

            return ResponseEntity.ok(Map.of("usuario", userSafe));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
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
        String profile = System.getProperty("spring.profiles.active", "dev");
        return profile.equals("dev");
    }
}
