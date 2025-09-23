package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.UsuarioService;
import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ============================================
 * Controlador REST para usuarios.
 * - CRUD para admin
 * - Registro y login para usuarios comunes
 * - Login devuelve URL de redirección según rol
 * ============================================
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Value("${frontend.url.${spring.profiles.active}}")
    private String frontendUrl;

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // ===================== MÉTODO AUXILIAR =====================
    private boolean isDev() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        return profile.equals("dev");
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
        Optional<Usuarios> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
        return usuarioOpt
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
            nuevoUsuario.setRol("ROLE_USER"); // rol por defecto
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
        Optional<Usuarios> usuario = usuarioService.login(
                credenciales.getEmail(),
                credenciales.getPassword()
        );

        if (usuario.isPresent()) {
            Usuarios u = usuario.get();
            String token = jwtUtil.generarToken(u.getId(), u.getRol());

            // Crear cookie HttpOnly
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(2 * 60 * 60); // 2 horas

            if (isDev()) {
                cookie.setSecure(false);
                cookie.setAttribute("SameSite", "Lax");
            } else {
                cookie.setSecure(true);
                cookie.setAttribute("SameSite", "None");
            }

            response.addCookie(cookie);

            // Determinar URL de redirección según rol
            String redirectUrl;
            if ("ROLE_ADMIN".equalsIgnoreCase(u.getRol())) {
                redirectUrl = "/admin/dashboard";
            } else {
                redirectUrl = "/productos/listado";
            }

            // Retornar info del usuario + URL de redirección
            Map<String, Object> responseBody = Map.of(
                    "usuario", Map.of(
                            "id", u.getId(),
                            "nombre", u.getNombre(),
                            "email", u.getEmail(),
                            "rol", u.getRol()
                    ),
                    "redirect", redirectUrl
            );

            return ResponseEntity.ok(responseBody);

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(!isDev());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", isDev() ? "Strict" : "None");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

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
}
