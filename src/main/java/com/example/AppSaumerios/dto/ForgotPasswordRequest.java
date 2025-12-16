// src/main/java/com/example/AppSaumerios/dto/ForgotPasswordRequest.java
package com.example.AppSaumerios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ============================================
// DTO de Solicitud de Olvido de Contraseña
// ============================================
@Getter // Genera todos los métodos getter
@Setter // Genera todos los métodos setter
@NoArgsConstructor // Genera el constructor sin argumentos (necesario para Spring/Jackson)
public class ForgotPasswordRequest {

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    private String email;
}