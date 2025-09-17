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
 * ============================================
 * Controlador REST para usuarios.
 * - CRUD para admin
 * - Registro y login para usuarios comunes
 * ============================================
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
        Optional<Usuarios> usuario = usuarioService.login(
                credenciales.getEmail(),
                credenciales.getPassword()
        );

        if (usuario.isPresent()) {
            Usuarios u = usuario.get();
            String token = jwtUtil.generarToken(u.getId(), u.getRol());

            // Crear cookie HttpOnly segura
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(!isDev()); // HTTPS solo en producción
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 1 hora
            cookie.setAttribute("SameSite", "Strict"); // evita CSRF en la mayoría de los casos
            response.addCookie(cookie);

            // Retornar solo info del usuario (no el token)
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("usuario", Map.of(
                    "id", u.getId(),
                    "nombre", u.getNombre(),
                    "email", u.getEmail(),
                    "rol", u.getRol()
            ));

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
        cookie.setMaxAge(0); // expira de inmediato
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }




    // ====== AQUÍ VA EL MÉTODO isDev ======
    private boolean isDev() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        return profile.equals("dev");
    }
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(@CookieValue(value = "token", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay sesión activa");
        }

        try {
            Long userId = jwtUtil.obtenerIdDesdeToken(token); // <-- cambio aquí
            Usuarios usuario = usuarioService.buscarPorId(userId); // asegúrate de tener este método
            return ResponseEntity.ok(Map.of("usuario", usuario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
    }

}

