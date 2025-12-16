// src/main/java/com/example/AppSaumerios/dto/ResetPasswordRequest.java
package com.example.AppSaumerios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================
// DTO de Solicitud de Restablecimiento de Contraseña
// ============================================
@Getter // Genera todos los métodos getter
@Setter // Genera todos los métodos setter
@NoArgsConstructor // Genera el constructor sin argumentos (necesario para Spring/Jackson)
public class ResetPasswordRequest {

    // El token y el email vienen por la URL, solo necesitamos la nueva contraseña del cuerpo (JSON)
    @NotBlank(message = "La nueva contraseña no puede estar vacía.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String newPassword;
}
