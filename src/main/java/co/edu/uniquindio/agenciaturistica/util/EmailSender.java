package co.edu.uniquindio.agenciaturistica.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailSender {

    private EmailSender(){}
    private static final String EMAIL = ConfigLoader.get("email.emisor");
    private static final String PASSWORD = ConfigLoader.get("email.password");
    private static final Properties props = new Properties();

    static {
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
    }


    /**
     * Método para enviar un correo electrónico
     *
     * @param destinatario  El destinatario del correo
     * @param asunto        El asunto del correo
     * @param rutaPlantilla La ruta de la plantilla HTML
     * @param codigo        El código a incluir en el correo
     * @return true si el correo se envió correctamente, false en caso contrario
     * @throws MessagingException en caso de error al enviar el correo
     * @throws IOException en caso de error al leer la plantilla
     */

    public static Respuesta<String> enviarEmail(String destinatario, String asunto, String rutaPlantilla, String codigo) throws MessagingException, IOException {
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL, PASSWORD);
                }
            });

            InputStream inputStream = EmailSender.class.getResourceAsStream(rutaPlantilla);
            if (inputStream == null) {
                throw new IOException("No se pudo encontrar la plantilla: " + rutaPlantilla);
            }

            String htmlTemplate = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String htmlContent = htmlTemplate.replace("{{CODIGO}}", codigo);

            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(EMAIL));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(mensaje);
            return new Respuesta<>(true, "El correo se envió correctamente", null);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return new Respuesta<>(false, "Error al enviar el correo: " + e.getMessage(), null);
        }
    }
    
}
