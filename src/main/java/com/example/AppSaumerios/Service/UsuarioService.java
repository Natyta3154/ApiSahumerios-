package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===================== VALIDACIONES RÁPIDAS ====================

    // Optimización: Verifica si existe sin traer todos los usuarios
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Usuarios buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id " + id));
    }

    // ===================== CREACIÓN (REGISTRO) ====================

    public Usuarios guardar(Usuarios usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        // Normalización de ROL
        String rol = usuario.getRol();
        if (rol != null && !rol.isBlank()) {
            if (rol.startsWith("ROLE_")) {
                rol = rol.substring(5);
            }
            rol = rol.toUpperCase();
        } else {
            rol = "USER";
        }
        usuario.setRol(rol);

        // ENCRIPTAR PASSWORD (Solo para usuarios nuevos o cambios de pass explícitos)
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    // ===================== ACTUALIZACIÓN DE PERFIL (SAFE) ====================

    // Este método actualiza SOLO nombre y email sin romper la contraseña
    public Usuarios actualizarDatosPersonales(Long id, Map<String, Object> datos) {
        Usuarios usuario = buscarPorId(id);

        if (datos.containsKey("nombre")) {
            usuario.setNombre((String) datos.get("nombre"));
        }
        if (datos.containsKey("email")) {
            usuario.setEmail((String) datos.get("email"));
        }

        // NOTA: No tocamos el password aquí para evitar el "doble hash"

        return usuarioRepository.save(usuario);
    }

    // ===================== ADMIN / CRUD ====================

    public List<Usuarios> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    // ===================== LOGIN =====================
    public Optional<Usuarios> login(String email, String password) {
        Optional<Usuarios> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuarios usuario = usuarioOpt.get();
            if (passwordEncoder.matches(password, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }
}