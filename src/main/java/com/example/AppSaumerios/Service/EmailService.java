package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.ContactoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void enviarCorreoContacto(ContactoRequest contacto) {

        try {
            // HTML del correo
            String cuerpoHtml = """
                <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                        <h2>📩 Nuevo mensaje desde AppSaumerios</h2>
                        <p><strong>👤 Nombre:</strong> %s</p>
                        <p><strong>📧 Correo:</strong> <a href="mailto:%s">%s</a></p>
                        <hr>
                        <p><strong>📝 Mensaje:</strong></p>
                        <p style="background:#f4f4f4; padding:10px; border-radius:5px;">%s</p>
                        <br>
                        <small>📬 Este mensaje fue enviado automáticamente desde la web AppSaumerios.</small>
                    </body>
                </html>
            """.formatted(
                    contacto.getNombre(),
                    contacto.getEmail(),
                    contacto.getEmail(),
                    contacto.getMensaje()
            );

            // IMPORTANTE: Este correo debe ser el mismo que configures en application.properties
            String correoRemitente = "tu_correo_de_gmail@gmail.com";
            String correoDestinatario = "herny3154@gmail.com";

            MimeMessage mensaje = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(correoDestinatario);
            helper.setFrom(correoRemitente);
            helper.setReplyTo(contacto.getEmail());
            helper.setSubject("📩 Nuevo mensaje de contacto de " + contacto.getNombre());
            helper.setText(cuerpoHtml, true); // 'true' indica que el contenido es HTML

            emailSender.send(mensaje);
            System.out.println("📬 Correo enviado exitosamente a " + correoDestinatario + " a través de GMAIL SMTP.");

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo con GMAIL SMTP: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}