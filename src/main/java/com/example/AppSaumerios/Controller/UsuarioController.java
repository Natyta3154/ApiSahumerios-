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
@CrossOrigin(origins = "https://front-sahumerios-2.vercel.app")
public class UsuarioController {

    @Value("${frontend.url.${spring.profiles.active}}")
    private String frontendUrl;

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


    // ===================== MÉTODO AUXILIAR =====================
    private boolean isDev() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        return profile.equals("dev");
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
