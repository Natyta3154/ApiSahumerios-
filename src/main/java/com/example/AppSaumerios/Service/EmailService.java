package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.ContactoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 💡 Necesario para inyectar propiedades
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    // 💡 Inyectar el correo remitente configurado en application.properties
    @Value("${spring.mail.username}")
    private String correoRemitente;


    // ============================================
    // 1. Método Existente (Contacto)
    // ============================================
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

            // IMPORTANTE: Este debe ser el correo que recibe los mensajes de contacto
            String correoDestinatario = "herny3154@gmail.com";

            MimeMessage mensaje = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(correoDestinatario);
            helper.setFrom(correoRemitente); // Usamos el correo inyectado como remitente
            helper.setReplyTo(contacto.getEmail());
            helper.setSubject("📩 Nuevo mensaje de contacto de " + contacto.getNombre());
            helper.setText(cuerpoHtml, true); // 'true' indica que el contenido es HTML

            emailSender.send(mensaje);
            System.out.println("📬 Correo de contacto enviado exitosamente a " + correoDestinatario + ".");

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de contacto: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo", e);
        }
    }

    // ============================================
    // 2. 💡 MÉTODO NUEVO: Enviar correo de Restablecimiento
    // ============================================
    /**
     * Envía un correo con el enlace para restablecer la contraseña.
     * @param destinatario Correo del usuario que solicita el cambio.
     * @param resetLink URL completa con el token para el frontend.
     */
    public void enviarCorreoRestablecimiento(String destinatario, String resetLink) {

        try {
            // HTML del correo de restablecimiento
            String cuerpoHtml = """
                <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                        <h2>🔑 Solicitud de Restablecimiento de Contraseña</h2>
                        <p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.</p>
                        <p>Si no solicitaste esto, puedes ignorar este correo.</p>
                        <hr>
                        <p>Haz click en el siguiente botón para establecer una nueva contraseña:</p>
                        <a href="%s" style="background-color: #4f46e5; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 15px;">
                            Restablecer Contraseña
                        </a>
                        <p style="margin-top: 25px;">O copia y pega el siguiente enlace en tu navegador:</p>
                        <p style="word-break: break-all;"><small>%s</small></p>
                        <br>
                        <small>El enlace es válido por una hora.</small>
                    </body>
                </html>
            """.formatted(resetLink, resetLink);

            MimeMessage mensaje = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setFrom(correoRemitente);
            helper.setSubject("🔒 Restablecimiento de Contraseña - AppSaumerios");
            helper.setText(cuerpoHtml, true);

            emailSender.send(mensaje);
            System.out.println("📬 Correo de restablecimiento enviado exitosamente a " + destinatario);

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de restablecimiento: " + e.getMessage());
            // Lanzamos RuntimeException para que Spring pueda manejar el fallo si es crítico.
            throw new RuntimeException("Error al enviar correo de restablecimiento", e);
        }
    }
}