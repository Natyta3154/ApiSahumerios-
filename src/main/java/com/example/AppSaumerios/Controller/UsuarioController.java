package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.Service.UsuarioService;
import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@CrossOrigin(origins = "http://localhost:9002")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    // ===================== ADMIN =====================

    // Obtener todos los usuarios (solo admin)
    @GetMapping
    public ResponseEntity<List<Usuarios>> obtenerUsuarios(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            return ResponseEntity.ok(usuarioService.obtenerUsuarios());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Obtener usuario por ID (solo admin)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(Authentication authentication,
                                                 @PathVariable Long id) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Usuarios> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(usuarioOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }
    }

    // Crear nuevo usuario (solo admin)
    @PostMapping("/agregarUsuario")
    public ResponseEntity<?> agregarUsuario(Authentication authentication,
                                            @RequestBody Usuarios nuevoUsuario) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Usuarios usuarioCreado = usuarioService.guardar(nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Actualizar usuario (solo admin)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(Authentication authentication,
                                               @PathVariable Long id,
                                               @RequestBody Usuarios usuarioActualizado) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para actualizar este usuario");
        }
        try {
            return ResponseEntity.ok(usuarioService.actualizar(id, usuarioActualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Eliminar usuario (solo admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(Authentication authentication,
                                             @PathVariable Long id) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        usuarioService.eliminar(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    // ===================== USUARIO COMÚN =====================

    // Registro de usuario
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuarios nuevoUsuario, BindingResult result) {
        // Validaciones automáticas
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
            nuevoUsuario.setRol("user"); // rol por defecto
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

    // Login de usuario
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuarios credenciales) {
        Optional<Usuarios> usuario = usuarioService.login(
                credenciales.getEmail(),
                credenciales.getPassword()
        );

        if (usuario.isPresent()) {
            Usuarios u = usuario.get();

            // Generar token JWT
            String token = jwtUtil.generarToken(u.getId(), u.getRol());

            // Construir respuesta sin enviar password
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("usuario", Map.of(
                    "id", u.getId(),
                    "nombre", u.getNombre(),
                    "email", u.getEmail(),
                    "rol", u.getRol()
            ));

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales inválidas");
        }
    }
}
