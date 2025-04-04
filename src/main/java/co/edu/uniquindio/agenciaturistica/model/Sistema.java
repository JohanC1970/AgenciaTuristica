package co.edu.uniquindio.agenciaturistica.model;

import co.edu.uniquindio.agenciaturistica.dao.UsuarioDAO;
import co.edu.uniquindio.agenciaturistica.exception.AuthenticationException;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Sistema {

    private String nombre;

    //private UsuarioDAO usuarioDAO;

    public Sistema(String nombre) throws SQLException {

        this.nombre = nombre;
        //usuarioDAO = new UsuarioDAO();

    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Metodo para iniciar sesion en el sistema
     * @param email
     * @param password
     * @return
     * @throws AuthenticationException
     * @throws SQLException
     */
    public Usuario iniciarSesion(String email, String password) throws AuthenticationException, SQLException {

        if(email.isEmpty() || password.isEmpty()){
            throw new AuthenticationException("El email o la contraseña no pueden estar vacíos");
        }

       // Usuario usuario = usuarioDAO.iniciarSesion(email,password);
        return null;
    }



    public void cerrarSesion(Usuario usuario) {
        // Lógica para cerrar sesión
    }

    public boolean enviarCodigoVerificacion(String destinatario, String codigo) {
        final String usuario = "agenciaturisticasoftware2025@gmail.com";
        final String password = "ziac htmc lyhk vjpi";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, password);
            }
        });

        try {
            // Ruta relativa desde resources (omite 'src/main/resources')
            String rutaPlantilla = "/co/edu/uniquindio/agenciaturistica/emails/plantillaCodigoRecuperacionPassword.html";

            // Cargar plantilla usando ClassLoader
            InputStream is = getClass().getResourceAsStream(rutaPlantilla);
            if (is == null) {
                throw new RuntimeException("No se encontró la plantilla en: " + rutaPlantilla);
            }

            String htmlTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String htmlBody = htmlTemplate.replace("{CODIGO}", codigo);

            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(usuario));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject("🔐 Código de Recuperación");
            mensaje.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(mensaje);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarCodigoRecuperacionPassword(String email) {
        // Lógica para enviar código de recuperación de contraseña
        return true;
    }

    public List<Reporte>generarReportes() {
        // Lógica para generar reportes
        return null;
    }

    /**
     * Metodo para generar un codigo de 6 digitos
     * @return
     */
    private String generarCodigo(){
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Genera un número de 6 dígitos
        return String.valueOf(code);
    }

}
