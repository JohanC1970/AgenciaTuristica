package co.edu.uniquindio.agenciaturistica.model;

import co.edu.uniquindio.agenciaturistica.dao.UsuarioDAO;
import co.edu.uniquindio.agenciaturistica.exception.AuthenticationException;
import co.edu.uniquindio.agenciaturistica.exception.EmailFoundException;
import co.edu.uniquindio.agenciaturistica.util.EmailSender;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Sistema {

    private String nombre;

    private UsuarioDAO usuarioDAO;
    private Random random = new Random();

    public Sistema(String nombre) throws SQLException {

        this.nombre = nombre;
        usuarioDAO = new UsuarioDAO();

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
        return usuarioDAO.iniciarSesion(email,password);
    }



    public void cerrarSesion(Usuario usuario) {
        // Lógica para cerrar sesión
    }

    public boolean enviarCodigoVerificacion(String destinatario, String codigo) {
        return false;
    }

    /**
     * Este método envía un código para poder recuperar la contraseña.
     *
     * @param destinatario Email de la persona que está solicitando recuperar la contraseña.
     * @return true en caso de que se envió el código exitosamente, false en caso de algún error.
     * @throws SQLException en caso de error al actualizar el código de recuperación en la base de datos.
     * @throws MessagingException en caso de error al enviar el correo electrónico.
     * @throws IOException en caso de error al leer la plantilla del correo.
     */
    public boolean enviarCodigoRecuperacionPassword(String destinatario) throws MessagingException, IOException {

        // Generar el código de recuperación
        String codigo = generarCodigo();

        // Iniciar la transacción
        try {
            // Iniciar la actualización del código de recuperación en la base de datos
            boolean actualizarCodigoRecuperacion = usuarioDAO.actualizarCodigoRecuperacion(destinatario, codigo);

            if (actualizarCodigoRecuperacion) {
                // Si la actualización fue exitosa, intentar enviar el correo
                String rutaPlantilla = "/co/edu/uniquindio/agenciaturistica/emails/plantillaCodigoRecuperacionPassword.html";

                // Intentar enviar el correo
                boolean correoEnviado = EmailSender.enviarEmail(destinatario, "Código de recuperación de contraseña", rutaPlantilla, codigo);

                // Si el correo se envió correctamente, retornar true
                if (correoEnviado) {
                    return true;
                } else {
                    // Si el correo no se envió, revertir la actualización del código
                    usuarioDAO.revertirCodigoRecuperacion(destinatario);
                    return false;
                }
            } else {
                // Si no se pudo actualizar el código, retornar false
                return false;
            }

        } catch (MessagingException | IOException e) {
            // En caso de cualquier error, revertir la actualización del código
            usuarioDAO.revertirCodigoRecuperacion(destinatario);
            throw e;
        }
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
        int code = 100000 + random.nextInt(900000); // Genera un número de 6 dígitos
        return String.valueOf(code);
    }

}
