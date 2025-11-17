package com.example.AppSaumerios.Controller;

import com.example.AppSaumerios.dto.ContactoRequest;
import com.example.AppSaumerios.Service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacto")
@CrossOrigin(origins = "http://localhost:9002", allowCredentials = "true") // Ajustá al puerto de tu frontend React
public class ContactoController {

    private final EmailService emailService;

    @Autowired
    public ContactoController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@Valid @RequestBody ContactoRequest contacto) {
        try {
            emailService.enviarCorreoContacto(contacto);
            return ResponseEntity.ok().body("{\"mensaje\": \"Correo enviado correctamente\"}");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar el correo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Hubo un problema al enviar el mensaje. Intenta nuevamente más tarde.\"}");
        }
    }}

