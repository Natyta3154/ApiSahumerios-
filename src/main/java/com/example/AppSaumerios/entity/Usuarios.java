package com.example.AppSaumerios.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

// ============================================
// Usuarios.java
// Entidad JPA que representa un usuario en la base de datos.
// ============================================

@Entity
@Table(name = "usuarios")
public class Usuarios {
    @Id
    // Clave primaria auto incrementable
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = " El nombre no puede estar vacío")
    private String nombre;

    // Nota: La validación @Email suele ir aquí si usas Spring Validation,
    // pero mantenemos tu @NotBlank para email por ahora.
    @NotBlank(message = "El formato del email no es válido")
    @Column(unique = true) // opcional: evita correos duplicados en DB
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    private String rol;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fecha_creacion;

    // ============================================
    // 💡 CAMPOS AÑADIDOS PARA EL RESTABLECIMIENTO
    // ============================================
    /**
     * Token único para el restablecimiento de contraseña.
     * Solo tiene valor mientras el usuario está en el proceso de restablecer.
     */
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    /**
     * Fecha y hora en que expira el token de restablecimiento.
     */
    @Column(name = "reset_password_expiry_date")
    private LocalDateTime resetPasswordExpiryDate;


    // ============================================
    // CONSTRUCTORES
    // ============================================

    // Constructor completo
    public Usuarios( Long id, String nombre, String email, String password, String rol, LocalDateTime fecha_creacion) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.fecha_creacion = fecha_creacion;
        // Los campos de restablecimiento se inicializan como null por defecto
    }

    // Constructor por defecto (necesario para JPA)
    public Usuarios() {

    }

    // ============================================
    // GETTER Y SETTERS (existentes y nuevos)
    // ============================================

    // Getters y setters existentes
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... (resto de getters/setters para nombre, email, password, rol, fecha_creacion)

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public LocalDateTime getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(LocalDateTime fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    // 💡 Nuevos Getters y Setters para el restablecimiento

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordExpiryDate() {
        return resetPasswordExpiryDate;
    }

    public void setResetPasswordExpiryDate(LocalDateTime resetPasswordExpiryDate) {
        this.resetPasswordExpiryDate = resetPasswordExpiryDate;
    }

    // Establecer fecha de creación y rol por defecto antes de persistir
    @PrePersist
    public void prePersist() {
        fecha_creacion = LocalDateTime.now();
        if (this.rol == null) {
            this.rol = "USER";
        }
    }
}