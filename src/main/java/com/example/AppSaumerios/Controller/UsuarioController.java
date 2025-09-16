package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.UsuarioService;
import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador REST para usuarios:
 * - CRUD para admin
 * - Registro y login para usuarios comunes
 * - Login con cookie HttpOnly
 */
@RestController
@RequestMapping("/usuarios")
@CrossOrigin(
        origins = {
                "http://localhost:9002",
                "http://127.0.0.1:5500",
                "https://tu-frontend.com"
        },
        allowCredentials = "true"
)
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // ===================== ADMIN =====================
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/listaDeUser")
    public ResponseEntity<List<Usuarios>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado"));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/agregarUser")
    public ResponseEntity<?> agregarUsuario(@RequestBody Usuarios nuevoUsuario) {
        try {
            Usuarios usuarioCreado = usuarioService.guardar(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/editarUser/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id,
                                               @RequestBody Usuarios usuarioActualizado) {
        try {
            return ResponseEntity.ok(usuarioService.actualizar(id, usuarioActualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/eliminarUser/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    // ===================== USUARIO COMÚN =====================
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuarios nuevoUsuario, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "mensaje", "Error de validación",
                            "errores", result.getAllErrors()
                                    .stream()
                                    .map(err -> err.getDefaultMessage())
                                    .toList()
                    )
            );
        }

        try {
            nuevoUsuario.setRol("USER"); // rol por defecto
            Usuarios usuarioCreado = usuarioService.guardar(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Usuario registrado correctamente.",
                    "usuario", usuarioCreado
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensaje", "Error al registrar el usuario",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuarios credenciales, HttpServletResponse response) {
        Optional<Usuarios> usuarioOpt = usuarioService.login(credenciales.getEmail(), credenciales.getPassword());

        if (usuarioOpt.isPresent()) {
            Usuarios u = usuarioOpt.get();
            String token = jwtUtil.generarToken(u.getId(), u.getRol());

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(!isDev()); // HTTPS en producción
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 1 hora
            response.addCookie(cookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("usuario", Map.of(
                    "id", u.getId(),
                    "nombre", u.getNombre(),
                    "email", u.getEmail(),
                    "rol", u.getRol()
            ));
            return ResponseEntity.ok(responseBody);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    // ===================== PERFIL =====================
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(@CookieValue(value = "token", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay sesión activa");
        }

        try {
            Long userId = jwtUtil.obtenerIdDesdeToken(token);
            String rol = jwtUtil.obtenerRolDesdeToken(token);

            Optional<Usuarios> usuarioOpt = usuarioService.obtenerUsuarioPorId(userId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
            }

            Usuarios usuario = usuarioOpt.get();
            return ResponseEntity.ok(Map.of(
                    "usuario", Map.of(
                            "id", usuario.getId(),
                            "nombre", usuario.getNombre(),
                            "email", usuario.getEmail(),
                            "rol", rol
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
    }

    private boolean isDev() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        return profile.equals("dev");
    }
}
