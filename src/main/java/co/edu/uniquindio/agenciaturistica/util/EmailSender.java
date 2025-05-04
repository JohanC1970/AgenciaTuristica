package co.edu.uniquindio.agenciaturistica.util;

import javax.mail.*;
import javax.mail.internet.AddressException;
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

    public static Respuesta<String> enviarEmailCodigo(String destinatario, String asunto, String rutaPlantilla, String codigo) throws IOException, MessagingException {
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
    }

    public static Respuesta<String> enviarCorreoVerificacion(String destinatario, String codigo) throws MessagingException, IOException {
        String asunto = "Código de verificación";
        String rutaPlantilla = "/co/edu/uniquindio/agenciaturistica/emails/plantillaCodigoVerificacion.html";
        return enviarEmailCodigo(destinatario, asunto, rutaPlantilla, codigo);
    }


    public static Respuesta<String> enviarCorreoRecuperacion(String destinatario, String codigo) throws MessagingException, IOException {
        String asunto = "Código de recuperación de contraseña";
        String rutaPlantilla = "/co/edu/uniquindio/agenciaturistica/emails/plantillaCodigoRecuperacionPassword.html";
        return enviarEmailCodigo(destinatario, asunto, rutaPlantilla, codigo);
    }

    public static void enviarEmailReserva(String destino, String asunto, String contenido) throws MessagingException {

        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");

        Session sesion = Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(EMAIL));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino));
            mensaje.setSubject(asunto);
            mensaje.setText(contenido);

            Transport.send(mensaje);
    }



}
