package com.example.AppSaumerios.Service;

import com.example.AppSaumerios.dto.ContactoRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// ===== GMAIL (comentado) =====
// import jakarta.mail.internet.MimeMessage;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    // @Autowired
    // private JavaMailSender emailSender;

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

            // Construir JSON manualmente
            String json = """
                {
                  "from": "AppSaumerios <onboarding@resend.dev>",
                  "to": ["herny3154@gmail.com"],
                  "reply_to": "%s",
                  "subject": "Nuevo mensaje de contacto de %s",
                  "html": "%s"
                }
            """.formatted(
                    contacto.getEmail(),
                    contacto.getNombre(),
                    cuerpoHtml.replace("\"", "\\\"")
            );

            // Conexión a Resend
            URL url = new URL("https://api.resend.com/emails");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Enviar payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            System.out.println("📬 Resend respondió status: " + status);

            conn.disconnect();


            // ==================================================================
            // 📨 SMTP GMAIL (comentado, INTTACTO como lo tenías)
            // ==================================================================
            /*
            MimeMessage mensaje = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo("herny3154@gmail.com");
            helper.setFrom("herny3154@gmail.com");
            helper.setReplyTo(contacto.getEmail());
            helper.setSubject("📩 Nuevo mensaje de contacto de " + contacto.getNombre());
            helper.setText(cuerpoHtml, true);

            emailSender.send(mensaje);
            */

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo con Resend: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}
