package co.edu.uniquindio.agenciaturistica.model;

import co.edu.uniquindio.agenciaturistica.dao.*;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.util.EmailSender;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class Sistema {

    private String nombre;
    private UsuarioDAO usuarioDAO;
    private ClienteDAO clienteDAO;
    private PaqueteDAO paqueteDAO;
    private ActividadDAO actividadDAO;
    private HospedajeDAO hospedajeDAO;
    private HabitacionDAO habitacionDAO;
    private ReporteDAO reporteDAO;
    private ReservaDAO reservaDAO;

    private Random random = new Random();

    public Sistema(String nombre) throws SQLException {
        this.nombre = nombre;
        usuarioDAO = new UsuarioDAO();
        clienteDAO = new ClienteDAO();
        paqueteDAO = new PaqueteDAO();
        actividadDAO = new ActividadDAO();
        hospedajeDAO = new HospedajeDAO();
        habitacionDAO = new HabitacionDAO();
        reporteDAO = new ReporteDAO();
        reservaDAO = new ReservaDAO();
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
                Respuesta<String> correoEnviado = EmailSender.enviarCorreoVerificacion(destinatario, codigo);
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
                Respuesta<String> correoEnviado = EmailSender.enviarCorreoRecuperacion(destinatario, codigo);
                if (correoEnviado.isExito()) {
                    return new Respuesta<>(true, "Código de recuperación enviado correctamente", codigo);
                } else {
                    usuarioDAO.revertirCodigoRecuperacion(destinatario);
                    return correoEnviado;
                }
            } else {
                return actualizarCodigoRecuperacion;
            }
        } catch (SQLException e) {
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

    /**
     * Método para verificar si un email existe en el sistema
     * @param email Email a verificar
     * @return true si el email existe, false en caso contrario
     */
    public boolean verificarExistenciaEmail(String email) {
        try {
            return usuarioDAO.existeEmail(email);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método para actualizar la contraseña de un usuario
     * @param email Email del usuario
     * @param nuevaPassword Nueva contraseña
     * @return Respuesta con el resultado de la operación
     */
    public Respuesta<String> actualizarPassword(String email, String nuevaPassword) {
        try {
            if (nuevaPassword == null || nuevaPassword.isEmpty()) {
                return new Respuesta<>(false, "La contraseña no puede estar vacía", null);
            }

            if (nuevaPassword.length() < 6) {
                return new Respuesta<>(false, "La contraseña debe tener al menos 6 caracteres", null);
            }

            return usuarioDAO.actualizarPassword(email, nuevaPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return new Respuesta<>(false, "Error al actualizar la contraseña: " + e.getMessage(), null);
        }
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

    /**
     * Este método permite obtener la lista de todos los empleados registrados en el sistema
     * @return Lista de usuarios con rol de empleado
     * @throws SQLException
     */
    public List<Usuario> obtenerEmpleados() throws SQLException {
        try {
            return usuarioDAO.obtenerUsuariosPorRol(Rol.EMPLEADO);
        } catch (SQLException e) {
            throw new SQLException("Error al obtener la lista de empleados", e);
        }
    }

    /**
     * Este método permite verificar la cuenta de un usuario utilizando el código enviado por correo
     * @param email Email del usuario
     * @param codigo Código de verificación
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Usuario> verificarCuenta(String email, String codigo) throws SQLException {
        try {
            // Validar que los datos no sean null o vacíos
            if (email == null || email.isEmpty() || codigo == null || codigo.isEmpty()) {
                return new Respuesta<>(false, "El email y el código son obligatorios", null);
            }

            // Validar formato de email
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return new Respuesta<>(false, "El formato del email no es válido", null);
            }

            // Llamar al método del DAO para verificar la cuenta
            return usuarioDAO.verificarCuenta(email, codigo);
        } catch (SQLException e) {
            throw new SQLException("Error al verificar la cuenta: " + e.getMessage(), e);
        }
    }

    /**
     * Este método permite obtener la lista de todos los clientes registrados en el sistema
     * @return Lista de clientes
     * @throws SQLException
     */
    public List<Cliente> obtenerClientes() throws SQLException {
        try {
            return clienteDAO.obtenerTodosLosClientes();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener la lista de clientes", e);
        }
    }

    /**
     * Este método permite registrar un nuevo cliente en el sistema
     * @param cliente Cliente a registrar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> registrarCliente(Cliente cliente) throws SQLException {
        try {
            // Validar datos del cliente
            Respuesta<Cliente> respuestaDatos = validarDatosCliente(cliente);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return clienteDAO.guardarCliente(cliente);
        } catch (SQLException e) {
            throw new SQLException("Error al registrar el cliente", e);
        }
    }

    /**
     * Este método permite actualizar los datos de un cliente
     * @param cliente Cliente con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> actualizarCliente(Cliente cliente) throws SQLException {
        try {
            // Validar datos del cliente
            Respuesta<Cliente> respuestaDatos = validarDatosCliente(cliente);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return clienteDAO.actualizarCliente(cliente);
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el cliente", e);
        }
    }

    /**
     * Este método permite eliminar un cliente del sistema
     * @param identificacion Identificación del cliente a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> eliminarCliente(String identificacion) throws SQLException {
        try {
            return clienteDAO.eliminarCliente(identificacion);
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el cliente", e);
        }
    }

    /**
     * Este método permite buscar un cliente por su identificación
     * @param identificacion Identificación del cliente a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Cliente> buscarClientePorIdentificacion(String identificacion) throws SQLException {
        try {
            return clienteDAO.buscarClientePorIdentificacion(identificacion);
        } catch (SQLException e) {
            throw new SQLException("Error al buscar el cliente", e);
        }
    }

    /**
     * Este método permite validar los datos de un cliente
     * @param cliente Cliente a validar
     * @return Respuesta con el resultado de la validación
     */
    private Respuesta<Cliente> validarDatosCliente(Cliente cliente) {
        StringBuilder mensaje = new StringBuilder();

        if (cliente.getIdentificacion() == null || cliente.getIdentificacion().isEmpty()) {
            mensaje.append("La identificación es obligatoria\n");
        }

        if (cliente.getNombre() == null || cliente.getNombre().isEmpty()) {
            mensaje.append("El nombre es obligatorio\n");
        }

        if (cliente.getApellido() == null || cliente.getApellido().isEmpty()) {
            mensaje.append("El apellido es obligatorio\n");
        }

        if (cliente.getCorreo() == null || cliente.getCorreo().isEmpty()) {
            mensaje.append("El correo electrónico es obligatorio\n");
        } else if (!cliente.getCorreo().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            mensaje.append("El formato del correo electrónico no es válido\n");
        }

        if (cliente.getTelefono() == null || cliente.getTelefono().isEmpty()) {
            mensaje.append("El teléfono es obligatorio\n");
        }

        if (cliente.getFechaNacimiento() == null) {
            mensaje.append("La fecha de nacimiento es obligatoria\n");
        } else if (cliente.getFechaNacimiento().isAfter(LocalDate.now())) {
            mensaje.append("La fecha de nacimiento no puede ser en el futuro\n");
        }

        if (mensaje.length() == 0) {
            return new Respuesta<>(true, "Los datos son válidos", cliente);
        }

        return new Respuesta<>(false, mensaje.toString(), null);
    }

    /**
     * Este método permite obtener todos los paquetes turísticos disponibles
     * @return Lista de paquetes turísticos
     * @throws SQLException
     */
    public List<PaqueteTuristico> obtenerPaquetesTuristicos() throws SQLException {
        try {
            return paqueteDAO.obtenerTodosLosPaquetes();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener los paquetes turísticos", e);
        }
    }

    /**
     * Este método permite crear un nuevo paquete turístico
     * @param paquete Paquete turístico a crear
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> crearPaqueteTuristico(PaqueteTuristico paquete) throws SQLException {
        try {
            // Validar datos del paquete
            Respuesta<PaqueteTuristico> respuestaDatos = validarDatosPaquete(paquete);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return paqueteDAO.guardarPaquete(paquete);
        } catch (SQLException e) {
            throw new SQLException("Error al crear el paquete turístico", e);
        }
    }

    /**
     * Este método permite actualizar un paquete turístico existente
     * @param paquete Paquete turístico con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> actualizarPaqueteTuristico(PaqueteTuristico paquete) throws SQLException {
        try {
            // Validar datos del paquete
            Respuesta<PaqueteTuristico> respuestaDatos = validarDatosPaquete(paquete);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return paqueteDAO.actualizarPaquete(paquete);
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el paquete turístico", e);
        }
    }

    /**
     * Este método permite eliminar un paquete turístico
     * @param id ID del paquete a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> eliminarPaqueteTuristico(int id) throws SQLException {
        try {
            return paqueteDAO.eliminarPaquete(id);
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el paquete turístico", e);
        }
    }

    /**
     * Este método permite buscar un paquete turístico por su ID
     * @param id ID del paquete a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<PaqueteTuristico> buscarPaqueteTuristicoPorId(int id) throws SQLException {
        try {
            return paqueteDAO.buscarPaquetePorId(id);
        } catch (SQLException e) {
            throw new SQLException("Error al buscar el paquete turístico", e);
        }
    }

    /**
     * Este método permite agregar una actividad a un paquete turístico
     * @param paqueteId ID del paquete
     * @param actividadId ID de la actividad
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> agregarActividadAPaquete(int paqueteId, int actividadId) throws SQLException {
        try {
            return paqueteDAO.agregarActividadAPaquete(paqueteId, actividadId);
        } catch (SQLException e) {
            throw new SQLException("Error al agregar la actividad al paquete", e);
        }
    }

    /**
     * Este método permite eliminar una actividad de un paquete turístico
     * @param paqueteId ID del paquete
     * @param actividadId ID de la actividad
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> eliminarActividadDePaquete(int paqueteId, int actividadId) throws SQLException {
        try {
            return paqueteDAO.eliminarActividadDePaquete(paqueteId, actividadId);
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar la actividad del paquete", e);
        }
    }

    /**
     * Este método permite agregar un hospedaje a un paquete turístico
     * @param paqueteId ID del paquete
     * @param hospedajeId ID del hospedaje
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> agregarHospedajeAPaquete(int paqueteId, int hospedajeId) throws SQLException {
        try {
            return paqueteDAO.agregarHospedajeAPaquete(paqueteId, hospedajeId);
        } catch (SQLException e) {
            throw new SQLException("Error al agregar el hospedaje al paquete", e);
        }
    }

    /**
     * Este método permite eliminar un hospedaje de un paquete turístico
     * @param paqueteId ID del paquete
     * @param hospedajeId ID del hospedaje
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Boolean> eliminarHospedajeDePaquete(int paqueteId, int hospedajeId) throws SQLException {
        try {
            return paqueteDAO.eliminarHospedajeDePaquete(paqueteId, hospedajeId);
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el hospedaje del paquete", e);
        }
    }

    /**
     * Este método permite validar los datos de un paquete turístico
     * @param paquete Paquete a validar
     * @return Respuesta con el resultado de la validación
     */
    private Respuesta<PaqueteTuristico> validarDatosPaquete(PaqueteTuristico paquete) {
        StringBuilder mensaje = new StringBuilder();

        if (paquete.getNombre() == null || paquete.getNombre().isEmpty()) {
            mensaje.append("El nombre es obligatorio\n");
        }

        if (paquete.getDescripcion() == null || paquete.getDescripcion().isEmpty()) {
            mensaje.append("La descripción es obligatoria\n");
        }

        if (paquete.getPrecioBase() <= 0) {
            mensaje.append("El precio base debe ser mayor que cero\n");
        }

        if (paquete.getDuracionDias() <= 0) {
            mensaje.append("La duración debe ser mayor que cero\n");
        }

        if (paquete.getFechaInicio() == null) {
            mensaje.append("La fecha de inicio es obligatoria\n");
        }

        if (paquete.getFechaFin() == null) {
            mensaje.append("La fecha de fin es obligatoria\n");
        }

        if (paquete.getFechaInicio() != null && paquete.getFechaFin() != null &&
                paquete.getFechaInicio().isAfter(paquete.getFechaFin())) {
            mensaje.append("La fecha de inicio debe ser anterior a la fecha de fin\n");
        }

        if (paquete.getCupoMaximo() <= 0) {
            mensaje.append("El cupo máximo debe ser mayor que cero\n");
        }

        if (paquete.getCuposDisponibles() < 0) {
            mensaje.append("Los cupos disponibles no pueden ser negativos\n");
        }

        if (paquete.getCuposDisponibles() > paquete.getCupoMaximo()) {
            mensaje.append("Los cupos disponibles no pueden ser mayores que el cupo máximo\n");
        }

        if (mensaje.length() == 0) {
            return new Respuesta<>(true, "Los datos son válidos", paquete);
        }

        return new Respuesta<>(false, mensaje.toString(), null);
    }

    /**
     * Este método permite obtener todas las actividades disponibles
     * @return Lista de actividades
     * @throws SQLException
     */
    public List<Actividad> obtenerActividades() throws SQLException {
        try {
            return actividadDAO.obtenerTodasLasActividades();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener las actividades", e);
        }
    }

    /**
     * Este método permite crear una nueva actividad
     * @param actividad Actividad a crear
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> crearActividad(Actividad actividad) throws SQLException {
        try {
            // Validar datos de la actividad
            Respuesta<Actividad> respuestaDatos = validarDatosActividad(actividad);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return actividadDAO.guardarActividad(actividad);
        } catch (SQLException e) {
            throw new SQLException("Error al crear la actividad", e);
        }
    }

    /**
     * Este método permite actualizar una actividad existente
     * @param actividad Actividad con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> actualizarActividad(Actividad actividad) throws SQLException {
        try {
            // Validar datos de la actividad
            Respuesta<Actividad> respuestaDatos = validarDatosActividad(actividad);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return actividadDAO.actualizarActividad(actividad);
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar la actividad", e);
        }
    }

    /**
     * Este método permite eliminar una actividad
     * @param id ID de la actividad a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> eliminarActividad(int id) throws SQLException {
        try {
            return actividadDAO.eliminarActividad(id);
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar la actividad", e);
        }
    }

    /**
     * Este método permite buscar una actividad por su ID
     * @param id ID de la actividad a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Actividad> buscarActividadPorId(int id) throws SQLException {
        try {
            return actividadDAO.buscarActividadPorId(id);
        } catch (SQLException e) {
            throw new SQLException("Error al buscar la actividad", e);
        }
    }

    /**
     * Este método permite validar los datos de una actividad
     * @param actividad Actividad a validar
     * @return Respuesta con el resultado de la validación
     */
    private Respuesta<Actividad> validarDatosActividad(Actividad actividad) {
        StringBuilder mensaje = new StringBuilder();

        if (actividad.getNombre() == null || actividad.getNombre().isEmpty()) {
            mensaje.append("El nombre es obligatorio\n");
        }

        if (actividad.getDescripcion() == null || actividad.getDescripcion().isEmpty()) {
            mensaje.append("La descripción es obligatoria\n");
        }

        if (actividad.getUbicacion() == null || actividad.getUbicacion().isEmpty()) {
            mensaje.append("La ubicación es obligatoria\n");
        }

        if (actividad.getPrecio() <= 0) {
            mensaje.append("El precio debe ser mayor que cero\n");
        }

        if (actividad.getDuracion() <= 0) {
            mensaje.append("La duración debe ser mayor que cero\n");
        }

        if (actividad.getFechaInicio() == null) {
            mensaje.append("La fecha de inicio es obligatoria\n");
        }

        if (actividad.getCupoMaximo() <= 0) {
            mensaje.append("El cupo máximo debe ser mayor que cero\n");
        }

        if (actividad.getCuposDisponibles() < 0) {
            mensaje.append("Los cupos disponibles no pueden ser negativos\n");
        }

        if (actividad.getCuposDisponibles() > actividad.getCupoMaximo()) {
            mensaje.append("Los cupos disponibles no pueden ser mayores que el cupo máximo\n");
        }

        if (mensaje.length() == 0) {
            return new Respuesta<>(true, "Los datos son válidos", actividad);
        }

        return new Respuesta<>(false, mensaje.toString(), null);
    }


    /**
     * Este método permite obtener todos los hospedajes disponibles
     * @return Lista de hospedajes
     * @throws SQLException
     */
    public List<Hospedaje> obtenerHospedajes() throws SQLException {
        try {
            return hospedajeDAO.obtenerTodosLosHospedajes();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener los hospedajes", e);
        }
    }

    /**
     * Este método permite crear un nuevo hospedaje
     * @param hospedaje Hospedaje a crear
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> crearHospedaje(Hospedaje hospedaje) throws SQLException {
        try {
            // Validar datos del hospedaje
            Respuesta<Hospedaje> respuestaDatos = validarDatosHospedaje(hospedaje);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return hospedajeDAO.guardarHospedaje(hospedaje);
        } catch (SQLException e) {
            throw new SQLException("Error al crear el hospedaje", e);
        }
    }

    /**
     * Este método permite actualizar un hospedaje existente
     * @param hospedaje Hospedaje con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> actualizarHospedaje(Hospedaje hospedaje) throws SQLException {
        try {
            // Validar datos del hospedaje
            Respuesta<Hospedaje> respuestaDatos = validarDatosHospedaje(hospedaje);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return hospedajeDAO.actualizarHospedaje(hospedaje);
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el hospedaje", e);
        }
    }

    /**
     * Este método permite eliminar un hospedaje
     * @param id ID del hospedaje a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> eliminarHospedaje(int id) throws SQLException {
        try {
            return hospedajeDAO.eliminarHospedaje(id);
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el hospedaje", e);
        }
    }

    /**
     * Este método permite buscar un hospedaje por su ID
     * @param id ID del hospedaje a buscar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Hospedaje> buscarHospedajePorId(int id) throws SQLException {
        try {
            return hospedajeDAO.buscarHospedajePorId(id);
        } catch (SQLException e) {
            throw new SQLException("Error al buscar el hospedaje", e);
        }
    }

    /**
     * Este método permite validar los datos de un hospedaje
     * @param hospedaje Hospedaje a validar
     * @return Respuesta con el resultado de la validación
     */
    private Respuesta<Hospedaje> validarDatosHospedaje(Hospedaje hospedaje) {
        StringBuilder mensaje = new StringBuilder();

        if (hospedaje.getNombre() == null || hospedaje.getNombre().isEmpty()) {
            mensaje.append("El nombre es obligatorio\n");
        }

        if (hospedaje.getCiudad() == null || hospedaje.getCiudad().isEmpty()) {
            mensaje.append("La ciudad es obligatoria\n");
        }

        if (hospedaje.getDireccion() == null || hospedaje.getDireccion().isEmpty()) {
            mensaje.append("La dirección es obligatoria\n");
        }

        if (hospedaje.getTelefono() == null || hospedaje.getTelefono().isEmpty()) {
            mensaje.append("El teléfono es obligatorio\n");
        }

        if (hospedaje.getEstrellas() < 1 || hospedaje.getEstrellas() > 5) {
            mensaje.append("Las estrellas deben estar entre 1 y 5\n");
        }

        if (hospedaje.getDescripcion() == null || hospedaje.getDescripcion().isEmpty()) {
            mensaje.append("La descripción es obligatoria\n");
        }

        if (mensaje.length() == 0) {
            return new Respuesta<>(true, "Los datos son válidos", hospedaje);
        }

        return new Respuesta<>(false, mensaje.toString(), null);
    }



    // Añadir estos métodos a la clase Sistema.java

    /**
     * Este método permite obtener todos los tipos de habitación disponibles
     * @return Lista de tipos de habitación
     * @throws SQLException
     */
    public List<TipoHabitacion> obtenerTiposHabitacion() throws SQLException {
        try {
            return habitacionDAO.obtenerTiposHabitacion();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener los tipos de habitación: " + e.getMessage(), e);
        }
    }

    /**
     * Este método permite obtener todas las habitaciones de un hospedaje
     * @param hospedajeId ID del hospedaje
     * @return Lista de habitaciones
     * @throws SQLException
     */
    public List<Habitacion> obtenerHabitacionesPorHospedaje(int hospedajeId) throws SQLException {
        try {
            return habitacionDAO.obtenerHabitacionesPorHospedaje(hospedajeId);
        } catch (SQLException e) {
            throw new SQLException("Error al obtener las habitaciones del hospedaje: " + e.getMessage(), e);
        }
    }

    /**
     * Este método permite crear una nueva habitación en un hospedaje
     * @param hospedajeId ID del hospedaje
     * @param habitacion Habitación a crear
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Habitacion> crearHabitacion(int hospedajeId, Habitacion habitacion) throws SQLException {
        try {
            // Validar datos de la habitación
            Respuesta<Habitacion> respuestaDatos = validarDatosHabitacion(habitacion);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return habitacionDAO.guardarHabitacion(hospedajeId, habitacion);
        } catch (SQLException e) {
            throw new SQLException("Error al crear la habitación: " + e.getMessage(), e);
        }
    }

    /**
     * Este método permite actualizar una habitación existente
     * @param hospedajeId ID del hospedaje
     * @param habitacion Habitación con los datos actualizados
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Habitacion> actualizarHabitacion(int hospedajeId, Habitacion habitacion) throws SQLException {
        try {
            // Validar datos de la habitación
            Respuesta<Habitacion> respuestaDatos = validarDatosHabitacion(habitacion);
            if (!respuestaDatos.isExito()) {
                return respuestaDatos;
            }

            return habitacionDAO.actualizarHabitacion(hospedajeId, habitacion);
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar la habitación: " + e.getMessage(), e);
        }
    }

    /**
     * Este método permite eliminar una habitación
     * @param hospedajeId ID del hospedaje
     * @param habitacionId ID de la habitación a eliminar
     * @return Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Habitacion> eliminarHabitacion(int hospedajeId, int habitacionId) throws SQLException {
        try {
            return habitacionDAO.eliminarHabitacion(hospedajeId, habitacionId);
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar la habitación: " + e.getMessage(), e);
        }
    }

    /**
     * Este método permite validar los datos de una habitación
     * @param habitacion Habitación a validar
     * @return Respuesta con el resultado de la validación
     */
    private Respuesta<Habitacion> validarDatosHabitacion(Habitacion habitacion) {
        StringBuilder mensaje = new StringBuilder();

        if (habitacion.getTipoHabitacion() == null) {
            mensaje.append("El tipo de habitación es obligatorio\n");
        }

        if (habitacion.getCapacidad() <= 0) {
            mensaje.append("La capacidad debe ser mayor que cero\n");
        }

        if (habitacion.getPrecioPorNoche() <= 0) {
            mensaje.append("El precio por noche debe ser mayor que cero\n");
        }

        if (mensaje.length() == 0) {
            return new Respuesta<>(true, "Los datos son válidos", habitacion);
        }

        return new Respuesta<>(false, mensaje.toString(), null);
    }


    public List<Reserva> obtenerReservas() throws SQLException {
        return reservaDAO.obtenerTodasLasReservas();
    }

    public List<Reserva> buscarReservasPorFechas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        return reservaDAO.buscarReservasPorFechas(fechaInicio, fechaFin);
    }

    public Respuesta<Reserva> buscarReservaPorId(String textoBusqueda) {
        return null;
    }

    public List<Reserva> buscarReservasPorCliente(String textoBusqueda) throws SQLException {
        return reservaDAO.buscarReservasPorCliente(textoBusqueda);
    }

    public List<Reserva> buscarReservasPorEstado(EstadoReserva estado) throws SQLException {
        return reservaDAO.buscarReservasPorEstado(estado);
    }

    public Respuesta<Reserva> confirmarReserva(String id) throws SQLException {
        return reservaDAO.confirmarReserva(id);
    }

    public Respuesta<Reserva> completarReserva(String id) throws SQLException {
        return reservaDAO.completarReserva(id);
    }

    public Respuesta<Reserva> cancelarReserva(String id) throws SQLException {
        return reservaDAO.cancelarReserva(id);
    }

    public List<Reserva> generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        return reservaDAO.generarReporteVentas(fechaInicio, fechaFin);
    }

    public double calcularTotalVentas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        return reservaDAO.calcularTotalVentas(fechaInicio, fechaFin);
    }

    public List<Habitacion> generarReporteOcupacion(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        return reservaDAO.generarReporteOcupacion(fechaInicio, fechaFin);
    }

    public Respuesta<Reserva> crearReserva(Reserva reserva) throws SQLException {
        return reservaDAO.crearReserva(reserva);
    }

    public Respuesta<Reserva> modificarReserva(Reserva reserva) throws SQLException {
        return reservaDAO.modificarReserva(reserva);
    }

    public Respuesta<PaqueteTuristico> buscarPaquetePorId(int id) throws SQLException {
        return paqueteDAO.buscarPaquetePorId(id);
    }

    /**
     * Este método permite verificar si una habitación está disponible para un rango de fechas
     * @param habitacionId ID de la habitación
     * @param fechaInicio Fecha de inicio de la reserva
     * @param fechaFin Fecha de fin de la reserva
     * @return true si la habitación está disponible, false en caso contrario
     * @throws SQLException
     */
    public boolean verificarDisponibilidadHabitacion(int habitacionId, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        try {
            return habitacionDAO.verificarDisponibilidadHabitacion(habitacionId, fechaInicio, fechaFin);
        } catch (SQLException e) {
            throw new SQLException("Error al verificar disponibilidad de habitación: " + e.getMessage(), e);
        }
    }

}