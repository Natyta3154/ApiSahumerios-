package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.entity.Usuarios;
import com.example.AppSaumerios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value; // Importar para la URL del frontend
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Para manejar la fecha de expiración
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID; // Para generar tokens únicos

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService; // 💡 Inyectar el servicio de correo

    // 💡 Propiedad para la URL del frontend (definida en application.properties)
    @Value("${app.frontend.url}")
    private String frontendUrl;

    // 💡 Constructor actualizado para inyectar EmailService
    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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

        // ENCRIPTAR PASSWORD
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

    // ===============================================================
    // 🔑 MÉTODOS AÑADIDOS PARA RESTABLECIMIENTO DE CONTRASEÑA
    // ===============================================================

    /**
     * 1. Genera un token y lo guarda en el usuario.
     * 2. Envía un correo con el enlace de restablecimiento.
     * @param email Email del usuario que solicita el restablecimiento.
     */
    public void createPasswordResetToken(String email) {
        Optional<Usuarios> userOpt = usuarioRepository.findByEmail(email);

        // Seguridad: Si el usuario no existe, salimos silenciosamente para no revelar usuarios válidos.
        if (userOpt.isEmpty()) {
            System.out.println("Intento de restablecimiento para email no registrado: " + email);
            return;
        }

        Usuarios usuario = userOpt.get();

        // Generar token seguro y fecha de expiración
        String token = UUID.randomUUID().toString();
        // El token expira en 60 minutos
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(60);

        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordExpiryDate(expiryDate);
        usuarioRepository.save(usuario); // Guardar el token en la DB

        // Construir el enlace completo para el frontend
        // Ej: http://localhost:5173/reset-password?token=XYZ&email=ABC
        String resetLink = String.format("%s/reset-password?token=%s&email=%s",
                frontendUrl, token, usuario.getEmail());

        // Enviar el correo usando el EmailService
        emailService.enviarCorreoRestablecimiento(usuario.getEmail(), resetLink);
        System.out.println("Enlace generado: " + resetLink);
    }

    /**
     * Valida el token, actualiza la contraseña y limpia los campos de restablecimiento.
     * @param token El token recibido de la URL.
     * @param email El email recibido de la URL.
     * @param newPassword La nueva contraseña sin hashear.
     * @return true si la contraseña fue restablecida con éxito, false si el token es inválido/expirado.
     */
    public boolean resetPassword(String token, String email, String newPassword) {
        // 1. Buscar usuario por token y email
        Optional<Usuarios> userOpt = usuarioRepository.findByResetPasswordTokenAndEmail(token, email);

        if (userOpt.isEmpty()) {
            // Token no encontrado o email incorrecto
            return false;
        }

        Usuarios usuario = userOpt.get();

        // 2. Chequear expiración
        if (usuario.getResetPasswordExpiryDate() == null || usuario.getResetPasswordExpiryDate().isBefore(LocalDateTime.now())) {
            // Limpiar token expirado y retornar error
            usuario.setResetPasswordToken(null);
            usuario.setResetPasswordExpiryDate(null);
            usuarioRepository.save(usuario);
            return false;
        }

        // 3. Hashear la nueva contraseña y actualizar
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        usuario.setPassword(hashedNewPassword);

        // 4. Limpiar token después de usarlo
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordExpiryDate(null);

        usuarioRepository.save(usuario);
        return true;
    }
}