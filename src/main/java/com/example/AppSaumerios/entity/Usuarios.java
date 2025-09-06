package com.example.AppSaumerios.entity;

import jakarta.persistence.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.time.LocalDateTime;

// ============================================
// Usuarios.java
// Entidad JPA que representa un usuario en la base de datos.
// Incluye validaciones y fecha de creación automática.
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

 @NotBlank(message = "El formato del email no es válid")
 @Column(unique = true) // opcional: evita correos duplicados en DB
 private String email;
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
    private String rol;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fecha_creacion;


    // contructor para los usuarios
    public Usuarios( Long id, String nombre, String email, String password, String rol, LocalDateTime fecha_creacion) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.fecha_creacion = fecha_creacion;

    }

    public Usuarios() {

    }

    //getter y setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
    // Establecer fecha de creación y rol por defecto antes de persistir
    @PrePersist
    public void prePersist() {
        fecha_creacion = LocalDateTime.now();
        if (this.rol == null) {
            this.rol = "USER";
        }
    }
}
