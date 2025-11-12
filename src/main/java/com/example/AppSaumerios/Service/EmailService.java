package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.ContactoRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    /**
     * Envía un correo HTML con los datos del formulario de contacto.
     */
    public void enviarCorreoContacto(ContactoRequest contacto) {
        try {
            MimeMessage mensaje = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo("herny3154@gmail.com"); // tu correo receptor
            helper.setFrom("herny3154@gmail.com"); // el correo autorizado (tu cuenta)
            helper.setReplyTo(contacto.getEmail()); // ✅ el correo del cliente (para responderle)
            helper.setSubject("📩 Nuevo mensaje de contacto de " + contacto.getNombre());

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
                    <small>📬 Este mensaje fue enviado automáticamente desde tu web AppSaumerios.</small>
                </body>
            </html>
        """.formatted(contacto.getNombre(), contacto.getEmail(), contacto.getEmail(), contacto.getMensaje());

            helper.setText(cuerpoHtml, true);
            emailSender.send(mensaje);

            System.out.println("✅ Correo con Reply-To enviado correctamente.");
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar el correo: " + e.getMessage());
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }}

