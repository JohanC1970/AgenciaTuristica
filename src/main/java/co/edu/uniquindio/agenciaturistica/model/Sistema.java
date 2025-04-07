package co.edu.uniquindio.agenciaturistica.model;

import co.edu.uniquindio.agenciaturistica.dao.UsuarioDAO;
import co.edu.uniquindio.agenciaturistica.util.EmailSender;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
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

    //----------METODOS DE INICIO DE SESION-------------------

    /**
     * Este método permite iniciar sesión en el sistema.
     * @param email Email de la persona que desea iniciar sesión
     * @param password Contraseña de la persona que desea iniciar sesión
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Usuario> iniciarSesion(String email, String password) throws SQLException {
        if (password.isEmpty()) {
            return new Respuesta<>(false, "La contraseña no puede estar vacía", null);
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return new Respuesta<>(false, "El email no es válido", null);
        }
        return usuarioDAO.iniciarSesion(email, password);
    }

    /**
     * Este método permite enviar un codigo para verificar la cuenta del usuario
     * @param destinatario Email del destinatario
     * @return Respuesta<String> Respuesta con el resultado de la operación
     * @throws MessagingException
     * @throws IOException
     * @throws SQLException
     */
    public Respuesta<String> enviarCodigoVerificacion(String destinatario) throws MessagingException, IOException, SQLException {
        String codigo = generarCodigo();
        try {
            Respuesta<String> actualizarCodigoVerificacion = usuarioDAO.actualizarCodigoVerificacion(destinatario, codigo);
            if (actualizarCodigoVerificacion.isExito()) {
                String rutaPlantilla = "/co/edu/uniquindio/agenciaturistica/emails/plantillaCodigoVerificacion.html";
                Respuesta<String> correoEnviado = EmailSender.enviarEmail(destinatario, "Código de verificación", rutaPlantilla, codigo);
                if (correoEnviado.isExito()) {
                    return new Respuesta<>(true, "Código de verificación enviado correctamente", codigo);
                } else {
                    usuarioDAO.revertirCodigoVerificacion(destinatario);
                    return correoEnviado;
                }
            } else {
                return actualizarCodigoVerificacion;
            }
        } catch (MessagingException | IOException | SQLException e) {
            usuarioDAO.revertirCodigoVerificacion(destinatario);
            return new Respuesta<>(false, "Error al enviar el código de verificación: " + e.getMessage(), null);
        }
    }

    /**
     * Este método permite enviar un código para recuperar la contraseña del usuario
     * @param destinatario Email del destinatario
     * @return Respuesta<String> Respuesta con el resultado de la operación
     * @throws MessagingException
     * @throws IOException
     * @throws SQLException
     */
    public Respuesta<String> enviarCodigoRecuperacion(String destinatario) throws MessagingException, IOException, SQLException {
        String codigo = generarCodigo();
        try {
            Respuesta<String> actualizarCodigoRecuperacion = usuarioDAO.actualizarCodigoRecuperacion(destinatario, codigo);
            if (actualizarCodigoRecuperacion.isExito()) {
                String rutaPlantilla = "/co/edu/uniquindio/agenciaturistica/emails/plantillaCodigoRecuperacionPassword.html";
                Respuesta<String> correoEnviado = EmailSender.enviarEmail(destinatario, "Código de recuperación de contraseña", rutaPlantilla, codigo);
                if (correoEnviado.isExito()) {
                    return new Respuesta<>(true, "Código de recuperación enviado correctamente", codigo);
                } else {
                    usuarioDAO.revertirCodigoRecuperacion(destinatario);
                    return correoEnviado;
                }
            } else {
                return actualizarCodigoRecuperacion;
            }
        } catch (MessagingException | IOException | SQLException e) {
            usuarioDAO.revertirCodigoRecuperacion(destinatario);
            return new Respuesta<>(false, "Error al enviar el código de recuperación: " + e.getMessage(), null);
        }
    }

    /**
     * Este metodo permite generar un código aleatorio de 6 dígitos
     * @return String Código generado
     */
    public String generarCodigo() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public List<Reporte> generarReportes() {
        // Lógica para generar reportes
        return null;
    }

    //----------METODOS CRUD USUARIO-------------------

    /**
     * Este método permite registrar un nuevo usuario en el sistema.
     * @param usuario Usuario a registrar
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Usuario>registrarUsuario(Usuario usuario) throws SQLException {

        Respuesta<Usuario> respuestaDatos = validarDatosUsuario(usuario);
        if(!respuestaDatos.isExito()){
            return respuestaDatos;
        }
        return usuarioDAO.registrarUsuario(usuario);
    }

    /**
     * Este método permite actualizar un usuario del sistema.
     * @param usuario Usuario a actualizar
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Usuario> actualizarUsuario(Usuario usuario) throws SQLException {
        Respuesta<Usuario> respuestaDatos = validarDatosUsuario(usuario);
        if(!respuestaDatos.isExito()){
            return respuestaDatos;
        }

        //Válida que el usuario no cambio su email
        if(usuarioDAO.existeEmail(usuario.getEmail())){
            return usuarioDAO.actualizarUsuario(usuario);
        }

        //En caso de que el usuario haya cambiado su email
        String codigoVerificacion = generarCodigo();
        return usuarioDAO.actualizarUsuario(usuario,codigoVerificacion);
    }

    public Respuesta<Usuario> eliminarUsuario(String identificacion) throws SQLException {

        //Válido que la identificación no esté vacía o sea nula
        if(identificacion==null || identificacion.isEmpty()){
            return new Respuesta<>(false, "La identificación no puede estar vacía", null);
        }
        //Válido que la identificación pertenezca a un usuario registrado
        if(!usuarioDAO.existeIdentifiacion(identificacion)){
            return new Respuesta<>(false, "El usuario no existe", null);
        }

        return usuarioDAO.eliminarUsuario(identificacion);
    }

    //----------METODOS DE VALIDACION-------------------

    /**
     * Este método permite validar que los valores de los atributos del usuario no estén vacíos ni sean nulos.
     * @param usuario Usuario a validar
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     */
    private Respuesta<Usuario> validarDatosUsuario(Usuario usuario) {
        StringBuilder mensaje = new StringBuilder();

        if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
            mensaje.append("El nombre no puede estar vacío\n");
        }
        if (usuario.getApellido() == null || usuario.getApellido().isEmpty()) {
            mensaje.append("El apellido no puede estar vacío\n");
        }
        if (usuario.getIdentificacion() == null || usuario.getIdentificacion().isEmpty()) {
            mensaje.append("La identificación no puede estar vacía\n");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
            mensaje.append("El email no puede estar vacío\n");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            mensaje.append("La contraseña no puede estar vacía\n");
        }
        if (usuario.getRol() == null) {
            mensaje.append("El tipo de usuario no puede estar vacío\n");
        }
        if (mensaje.length() == 0) {
            return new Respuesta<>(true, "Los datos son válidos", usuario);
        }
        return new Respuesta<>(false, mensaje.toString(), usuario);
    }


}