package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

// ============================================
// UsuarioService.java
// Lógica de negocio de usuarios:
// - CRUD
// - Validación de admin
// - Login con encriptación de contraseñas
// ============================================



@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===================== ADMIN ====================

    // Validar si usuario es admin
    public void validarAdmin(Usuarios usuario) {
        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            throw new SecurityException("Acceso denegado. Solo administradores.");
        }
    }

    public List<Usuarios> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuarios> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuarios guardar(Usuarios usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        // Normalizar el rol: quitar "ROLE_" si está presente y convertir a mayúsculas
        String rol = usuario.getRol();
        if (rol != null && !rol.isBlank()) {
            if (rol.startsWith("ROLE_")) {
                rol = rol.substring(5); // quita "ROLE_"
            }
            rol = rol.toUpperCase();
        } else {
            rol = "USER"; // valor por defecto
        }
        usuario.setRol(rol);

        // Codificar la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    public Usuarios actualizar(Long id, Usuarios usuarioActualizado) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(usuarioActualizado.getNombre());
                    usuario.setEmail(usuarioActualizado.getEmail());
                    if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
                        usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
                    }
                    // Normalizar el rol
                    String rol = usuarioActualizado.getRol();
                    if (rol != null && !rol.isBlank()) {
                        if (rol.startsWith("ROLE_")) {
                            rol = rol.substring(5);
                        }
                        rol = rol.toUpperCase();
                    } else {
                        rol = "USER";
                    }
                    usuario.setRol(rol);
                    return usuarioRepository.save(usuario);
                })
                .orElseThrow(() -> new IllegalArgumentException("El usuario con id " + id + " no existe"));
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    // ===================== USUARIO COMÚN =====================
    public Optional<Usuarios> login(String email, String password) {
        Optional<Usuarios> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuarios usuario = usuarioOpt.get();
            // Comparar contraseña ingresada con la codificada
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }
}
