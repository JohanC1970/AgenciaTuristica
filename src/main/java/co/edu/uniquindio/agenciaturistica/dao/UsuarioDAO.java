package co.edu.uniquindio.agenciaturistica.dao;

import co.edu.uniquindio.agenciaturistica.model.Empleado;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.PasswordEncoder;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UsuarioDAO {

    private Connection connection;
    private Random random = new Random();
    public UsuarioDAO() throws SQLException {
        try {
            this.connection = DataBaseConnection.getConnection();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    public UsuarioDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    //---------------AUTENTICACION------------------//

    /**
     * Este método permite iniciar sesión en el sistema.
     * @param email Email de la persona que desea iniciar sesión
     * @param password Contraseña de la persona que desea iniciar sesión
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Usuario> iniciarSesion(String email, String password) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Usuario usuario = null;

        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM usuario WHERE email = ?");
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String passwordEncriptada = resultSet.getString("password");
                if (PasswordEncoder.verifyPassword(password, passwordEncriptada)) {
                    boolean cuentaVerificada = resultSet.getBoolean("cuenta_verificada");
                    if (!cuentaVerificada) {
                        return new Respuesta<>(false, "La cuenta no está verificada", null);
                    }

                    usuario = new Usuario();
                    usuario.setNombre(resultSet.getString("nombre"));
                    usuario.setApellido(resultSet.getString("apellido"));
                    usuario.setEmail(resultSet.getString("email"));
                    usuario.setPassword("********");
                    usuario.setRol(Rol.valueOf(resultSet.getString("rol")));
                    usuario.setFechaRegistro(resultSet.getDate("fecha_registro").toLocalDate());

                    return new Respuesta<>(true, "Inicio de sesión exitoso", usuario);
                } else {
                    return new Respuesta<>(false, "La contraseña es incorrecta", null);
                }
            } else {
                return new Respuesta<>(false, "El email no está registrado en nuestro sistema", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al iniciar sesión: " + e.getMessage(), null);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método verifica si el email existe en la base de datos.
     * @param destinatario Email a verificar
     * @return true si el email existe, false en caso contrario
     * @throws SQLException
     */
    public boolean existeEmail(String destinatario) throws SQLException {
        String query = "SELECT 1 FROM usuario WHERE email = ? LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, destinatario);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Este método permite enviar un código para recuperar la contraseña del usuario
     * @param destinatario Email del destinatario
     * @return Respuesta<String> Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<String> actualizarCodigoRecuperacion(String destinatario, String codigo) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            if (!existeEmail(destinatario)) {
                return new Respuesta<>(false, "El email no está registrado en nuestro sistema", destinatario);
            }

            String query = "UPDATE usuario SET codigo_recuperacion_password = ?, expiracion_codigo_recuperacion_password = ? WHERE email = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, codigo);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusHours(1)));
            preparedStatement.setString(3, destinatario);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Código de recuperación actualizado", codigo);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar el código de recuperación", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al actualizar el código de recuperación: " + e.getMessage(), null);
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    /**
     * Este método permite revertir el código de recuperación de contraseña
     * @param destinatario Email de la persona que solicito el cambio de contraseña
     * @return
     */
    public Respuesta<String> revertirCodigoRecuperacion(String destinatario) {
        String query = "UPDATE usuario SET codigo_recuperacion_password = NULL, expiracion_codigo_recuperacion_password = NULL WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, destinatario);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Se revirtieron los cambios exitosamente", null);
            } else {
                return new Respuesta<>(false, "No se pudo revertir los cambios", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al revertir los cambios: " + e.getMessage(), null);
        }
    }

    /**
     * Este método permite actualizar el código de verificación de la cuenta del usuario
     * @param destinatario Email del destinatario
     * @param codigo Código de verificación
     * @return Respuesta<String> Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<String> actualizarCodigoVerificacion(String destinatario, String codigo) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            if (!existeEmail(destinatario)) {
                return new Respuesta<>(false, "El email no está registrado en nuestro sistema", destinatario);
            }

            String query = "UPDATE usuario SET codigo_verificacion = ?, expiracion_codigo_verificacion = ? WHERE email = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, codigo);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusHours(1)));
            preparedStatement.setString(3, destinatario);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Código de verificación actualizado", codigo);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar el código de verificación", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al actualizar el código de verificación: " + e.getMessage(), null);
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    /**
     * Este método permite revertir el código de verificación de la cuenta del usuario
     * @param destinatario Email de la persona que solicito el cambio de contraseña
     * @return
     */
    public Respuesta<String> revertirCodigoVerificacion(String destinatario) {
        String query = "UPDATE usuario SET codigo_verificacion = NULL, expiracion_codigo_verificacion = NULL WHERE email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, destinatario);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Se revirtieron los cambios exitosamente", null);
            } else {
                return new Respuesta<>(false, "No se pudo revertir los cambios", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al revertir los cambios: " + e.getMessage(), null);
        }
    }

    //METODOS DE CRUD

    /**
     * Este método permite registrar un nuevo usuario en la base de datos.
     * @param usuario Usuario a registrar
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     * @throws SQLException
     */
    public Respuesta<Usuario> registrarUsuario(Usuario usuario) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {

            if(existeEmail(usuario.getEmail())) {
                return new Respuesta<>(false, "El email ya está registrado en nuestro sistema", null);
            }
            if(existeIdentifiacion(usuario.getIdentificacion())){
                return new Respuesta<>(false, "La identificación ya está registrada en nuestro sistema", null);
            }
            String query = "INSERT INTO usuario (nombre, apellido, identificacion, email, password, rol, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getApellido());
            preparedStatement.setString(3, usuario.getIdentificacion());
            preparedStatement.setString(4, usuario.getEmail());
            preparedStatement.setString(5, PasswordEncoder.hashPassword(usuario.getPassword()));
            preparedStatement.setString(6, usuario.getRol().toString());
            preparedStatement.setDate(7, new java.sql.Date(System.currentTimeMillis())); // Aquí le mandas la fecha actual


            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Usuario registrado exitosamente", usuario);
            } else {
                return new Respuesta<>(false, "Error al registrar el usuario", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al registrar el usuario: " + e.getMessage(), null);
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    /**
     * Este método permite verificar si la identificación existe en la base de datos.
     * @param identificacion Identificación a verificar
     * @return true si la identificación existe, false en caso contrario
     * @throws SQLException
     */
    public boolean existeIdentifiacion(String identificacion) throws SQLException {
        String query = "SELECT 1 FROM usuario WHERE identificacion = ? LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, identificacion);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Este método permite actualizar los datos de un usuario
     * @param usuario Usuario con los datos actualizados
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     */
    public Respuesta<Usuario> actualizarUsuario(Usuario usuario) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Verificamos si el usuario existe
            String verificarUsuarioQuery = "SELECT * FROM usuario WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(verificarUsuarioQuery);
            preparedStatement.setString(1, usuario.getIdentificacion());
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new Respuesta<>(false, "El usuario no existe en el sistema", null);
            }

            // Si el email ha cambiado, verificamos que no exista otro usuario con ese email
            String emailActual = resultSet.getString("email");
            if (!emailActual.equals(usuario.getEmail())) {
                if (existeEmail(usuario.getEmail())) {
                    return new Respuesta<>(false, "Ya existe otro usuario con el email proporcionado", null);
                }
            }

            // Actualizamos los datos del usuario
            String actualizarUsuarioQuery = "UPDATE usuario SET nombre = ?, apellido = ?, email = ?, rol = ? WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(actualizarUsuarioQuery);
            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getApellido());
            preparedStatement.setString(3, usuario.getEmail());
            preparedStatement.setString(4, usuario.getRol().toString());
            preparedStatement.setString(5, usuario.getIdentificacion());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Si el usuario es un empleado o administrador, actualizamos sus datos específicos
                if (usuario.getRol() == Rol.EMPLEADO || usuario.getRol() == Rol.ADMINISTRADOR) {
                    // Obtenemos el ID del usuario
                    String obtenerIdQuery = "SELECT id FROM usuario WHERE identificacion = ?";
                    preparedStatement = connection.prepareStatement(obtenerIdQuery);
                    preparedStatement.setString(1, usuario.getIdentificacion());
                    resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        int usuarioId = resultSet.getInt("id");

                        if (usuario.getRol() == Rol.EMPLEADO) {
                            // Verificamos si ya existe como empleado
                            String verificarEmpleadoQuery = "SELECT * FROM empleado WHERE id = ?";
                            preparedStatement = connection.prepareStatement(verificarEmpleadoQuery);
                            preparedStatement.setInt(1, usuarioId);
                            resultSet = preparedStatement.executeQuery();

                            if (resultSet.next()) {
                                // Actualizamos el empleado
                                String actualizarEmpleadoQuery = "UPDATE empleado SET fecha_contratacion = ? WHERE id = ?";
                                preparedStatement = connection.prepareStatement(actualizarEmpleadoQuery);
                                preparedStatement.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                                preparedStatement.setInt(2, usuarioId);
                                preparedStatement.executeUpdate();
                            } else {
                                // Insertamos el empleado
                                String insertarEmpleadoQuery = "INSERT INTO empleado (id, fecha_contratacion) VALUES (?, ?)";
                                preparedStatement = connection.prepareStatement(insertarEmpleadoQuery);
                                preparedStatement.setInt(1, usuarioId);
                                preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                                preparedStatement.executeUpdate();
                            }
                        } else if (usuario.getRol() == Rol.ADMINISTRADOR) {
                            // Verificamos si ya existe como administrador
                            String verificarAdminQuery = "SELECT * FROM administrador WHERE id = ?";
                            preparedStatement = connection.prepareStatement(verificarAdminQuery);
                            preparedStatement.setInt(1, usuarioId);
                            resultSet = preparedStatement.executeQuery();

                            if (resultSet.next()) {
                                // Actualizamos el administrador
                                String actualizarAdminQuery = "UPDATE administrador SET fecha_contratacion = ? WHERE id = ?";
                                preparedStatement = connection.prepareStatement(actualizarAdminQuery);
                                preparedStatement.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                                preparedStatement.setInt(2, usuarioId);
                                preparedStatement.executeUpdate();
                            } else {
                                // Insertamos el administrador
                                String insertarAdminQuery = "INSERT INTO administrador (id, fecha_contratacion) VALUES (?, ?)";
                                preparedStatement = connection.prepareStatement(insertarAdminQuery);
                                preparedStatement.setInt(1, usuarioId);
                                preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                                preparedStatement.executeUpdate();
                            }
                        }
                    }
                }

                return new Respuesta<>(true, "Usuario actualizado correctamente", usuario);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar el usuario", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al actualizar el usuario: " + e.getMessage(), null);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar un usuario en la base de datos.
     * Ya que el usuario ha cambiado su email, se requiere verificar el nuevo correo.
     * @param usuario Usuario a actualizar
     * @param codigoVerificacion Código de verificación del nuevo correo
     * @return
     */
    public Respuesta<Usuario> actualizarUsuario(Usuario usuario, String codigoVerificacion) {
        // Primero actualizamos los datos básicos como nombre y apellido
        String sql = "UPDATE usuario SET nombre = ?, apellido = ?, email = ? WHERE identificacion = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getIdentificacion());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                // Luego actualizamos el código de verificación y estado

                if (actualizarCodigoVerificacion(usuario.getEmail(), codigoVerificacion).isExito()) {
                    usuario.setCodigoVerificacion(codigoVerificacion);
                    usuario.setCuentaVerificada(false);

                    return new Respuesta<>(true, "Usuario actualizado y se requiere verificar el nuevo correo", usuario);
                } else {
                    return new Respuesta<>(false, "Se actualizó el usuario pero no se pudo generar el código de verificación", null);
                }

            } else {
                return new Respuesta<>(false, "No se encontró el usuario a actualizar", null);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new Respuesta<>(false, "Error al actualizar el usuario: " + e.getMessage(), null);
        }
    }


    /**
     * Este método permite eliminar un usuario del sistema por su identificación
     * @param identificacion Identificación del usuario a eliminar
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     */
    public Respuesta<Usuario> eliminarUsuario(String identificacion) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Usuario usuario = null;

        try {
            // Primero obtenemos el usuario para retornarlo en la respuesta
            String selectQuery = "SELECT * FROM usuario WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, identificacion);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                usuario = new Usuario();
                usuario.setNombre(resultSet.getString("nombre"));
                usuario.setApellido(resultSet.getString("apellido"));
                usuario.setIdentificacion(resultSet.getString("identificacion"));
                usuario.setEmail(resultSet.getString("email"));
                usuario.setRol(Rol.valueOf(resultSet.getString("rol")));
                if (resultSet.getDate("fecha_registro") != null) {
                    usuario.setFechaRegistro(resultSet.getDate("fecha_registro").toLocalDate());
                }
            } else {
                return new Respuesta<>(false, "No se encontró el usuario con la identificación proporcionada", null);
            }

            // Verificar si el usuario tiene reservas asociadas antes de eliminarlo
            String checkReservasQuery = "SELECT COUNT(*) FROM reserva WHERE cliente_id = (SELECT id FROM cliente WHERE identificacion = ?)";
            preparedStatement = connection.prepareStatement(checkReservasQuery);
            preparedStatement.setString(1, identificacion);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return new Respuesta<>(false, "No se puede eliminar el usuario porque tiene reservas asociadas", null);
            }

            // Si es un empleado, verificar si tiene reservas asociadas como empleado
            if (usuario.getRol() == Rol.EMPLEADO) {
                String checkReservasEmpleadoQuery = "SELECT COUNT(*) FROM reserva WHERE empleado_id = (SELECT id FROM empleado WHERE id = (SELECT id FROM usuario WHERE identificacion = ?))";
                preparedStatement = connection.prepareStatement(checkReservasEmpleadoQuery);
                preparedStatement.setString(1, identificacion);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return new Respuesta<>(false, "No se puede eliminar el empleado porque tiene reservas asociadas", null);
                }
            }

            // Eliminar registros relacionados según el rol
            if (usuario.getRol() == Rol.EMPLEADO) {
                String deleteEmpleadoQuery = "DELETE FROM empleado WHERE id = (SELECT id FROM usuario WHERE identificacion = ?)";
                preparedStatement = connection.prepareStatement(deleteEmpleadoQuery);
                preparedStatement.setString(1, identificacion);
                preparedStatement.executeUpdate();
            } else if (usuario.getRol() == Rol.ADMINISTRADOR) {
                String deleteAdminQuery = "DELETE FROM administrador WHERE id = (SELECT id FROM usuario WHERE identificacion = ?)";
                preparedStatement = connection.prepareStatement(deleteAdminQuery);
                preparedStatement.setString(1, identificacion);
                preparedStatement.executeUpdate();
            }

            // Finalmente, eliminar el usuario
            String deleteUsuarioQuery = "DELETE FROM usuario WHERE identificacion = ?";
            preparedStatement = connection.prepareStatement(deleteUsuarioQuery);
            preparedStatement.setString(1, identificacion);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Usuario eliminado exitosamente", usuario);
            } else {
                return new Respuesta<>(false, "No se pudo eliminar el usuario", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al eliminar el usuario: " + e.getMessage(), null);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite actualizar la contraseña de un usuario
     * @param email Email del usuario
     * @param nuevaPassword Nueva contraseña
     * @return Respuesta<String> Respuesta con el resultado de la operación
     */
    public Respuesta<String> actualizarPassword(String email, String nuevaPassword) {
        PreparedStatement preparedStatement = null;
        try {
            if (!existeEmail(email)) {
                return new Respuesta<>(false, "El email no está registrado en nuestro sistema", null);
            }

            String query = "UPDATE usuario SET password = ?, codigo_recuperacion_password = NULL, expiracion_codigo_recuperacion_password = NULL WHERE email = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, PasswordEncoder.hashPassword(nuevaPassword));
            preparedStatement.setString(2, email);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                return new Respuesta<>(true, "Contraseña actualizada correctamente", null);
            } else {
                return new Respuesta<>(false, "No se pudo actualizar la contraseña", null);
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al actualizar la contraseña: " + e.getMessage(), null);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                // Manejo de la excepción al cerrar el PreparedStatement
                return new Respuesta<>(false, "Error al cerrar la conexión: " + e.getMessage(), null);
            }
        }
    }

    /**
     * Este método permite verificar la cuenta de un usuario utilizando el código enviado por correo
     * @param email Email del usuario
     * @param codigo Código de verificación
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     */
    public Respuesta<Usuario> verificarCuenta(String email, String codigo) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Primero verificamos si el email existe
            if (!existeEmail(email)) {
                return new Respuesta<>(false, "El email no está registrado en nuestro sistema", null);
            }

            // Verificamos si el código es válido y no ha expirado
            String verificarCodigoQuery = "SELECT * FROM usuario WHERE email = ? AND codigo_verificacion = ? AND expiracion_codigo_verificacion > NOW()";
            preparedStatement = connection.prepareStatement(verificarCodigoQuery);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, codigo);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // El código es válido, actualizamos la cuenta como verificada
                String actualizarCuentaQuery = "UPDATE usuario SET cuenta_verificada = true, codigo_verificacion = NULL, expiracion_codigo_verificacion = NULL WHERE email = ?";
                preparedStatement = connection.prepareStatement(actualizarCuentaQuery);
                preparedStatement.setString(1, email);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    // Obtenemos los datos del usuario para devolverlos en la respuesta
                    String obtenerUsuarioQuery = "SELECT * FROM usuario WHERE email = ?";
                    preparedStatement = connection.prepareStatement(obtenerUsuarioQuery);
                    preparedStatement.setString(1, email);
                    resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        Usuario usuario = new Usuario();
                        usuario.setNombre(resultSet.getString("nombre"));
                        usuario.setApellido(resultSet.getString("apellido"));
                        usuario.setIdentificacion(resultSet.getString("identificacion"));
                        usuario.setEmail(resultSet.getString("email"));
                        usuario.setRol(Rol.valueOf(resultSet.getString("rol")));
                        usuario.setCuentaVerificada(true);
                        if (resultSet.getDate("fecha_registro") != null) {
                            usuario.setFechaRegistro(resultSet.getDate("fecha_registro").toLocalDate());
                        }

                        return new Respuesta<>(true, "Cuenta verificada correctamente", usuario);
                    }
                }
                return new Respuesta<>(false, "No se pudo verificar la cuenta", null);
            } else {
                // Verificamos si el código ha expirado
                String verificarExpiracionQuery = "SELECT * FROM usuario WHERE email = ? AND codigo_verificacion = ? AND expiracion_codigo_verificacion <= NOW()";
                preparedStatement = connection.prepareStatement(verificarExpiracionQuery);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, codigo);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return new Respuesta<>(false, "El código de verificación ha expirado. Solicite uno nuevo.", null);
                } else {
                    return new Respuesta<>(false, "El código de verificación es incorrecto", null);
                }
            }
        } catch (SQLException e) {
            return new Respuesta<>(false, "Error al verificar la cuenta: " + e.getMessage(), null);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Este método permite obtener todos los usuarios con un rol específico
     * @param rol Rol de los usuarios a buscar
     * @return Lista de usuarios con el rol especificado
     * @throws SQLException
     */
    public List<Usuario> obtenerUsuariosPorRol(Rol rol) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Usuario> listaUsuarios = new ArrayList<>();

        try {
            // Query base para obtener usuarios por rol
            String query = "SELECT u.*, ";

            // Si el rol es EMPLEADO o ADMINISTRADOR, obtenemos también la fecha de contratación
            if (rol == Rol.EMPLEADO) {
                query += "e.fecha_contratacion ";
                query += "FROM usuario u LEFT JOIN empleado e ON u.id = e.id ";
            } else if (rol == Rol.ADMINISTRADOR) {
                query += "a.fecha_contratacion ";
                query += "FROM usuario u LEFT JOIN administrador a ON u.id = a.id ";
            } else {
                query += "NULL as fecha_contratacion ";
                query += "FROM usuario u ";
            }

            query += "WHERE u.rol = ? ORDER BY u.nombre, u.apellido";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, rol.toString());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                // Creamos el usuario base
                Usuario usuario = crearUsuarioDesdeResultSet(resultSet);

                // Si es un empleado o administrador, convertimos a la subclase correspondiente
                if (rol == Rol.EMPLEADO) {
                    Empleado empleado = new Empleado();
                    // Copiamos todos los atributos del usuario
                    empleado.setNombre(usuario.getNombre());
                    empleado.setApellido(usuario.getApellido());
                    empleado.setIdentificacion(usuario.getIdentificacion());
                    empleado.setEmail(usuario.getEmail());
                    empleado.setPassword(usuario.getPassword());
                    empleado.setRol(usuario.getRol());
                    empleado.setCuentaVerificada(usuario.isCuentaVerificada());
                    empleado.setFechaRegistro(usuario.getFechaRegistro());

                    // Agregamos los atributos específicos del empleado
                    if (resultSet.getDate("fecha_contratacion") != null) {
                        empleado.setFechaContratacion(resultSet.getDate("fecha_contratacion").toLocalDate());
                    }

                    listaUsuarios.add(empleado);
                } else {
                    listaUsuarios.add(usuario);
                }
            }

            return listaUsuarios;
        } catch (SQLException e) {
            throw new SQLException("Error al obtener usuarios por rol: " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
        }
    }

    /**
     * Método utilitario para crear un objeto Usuario a partir de un ResultSet
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private Usuario crearUsuarioDesdeResultSet(ResultSet resultSet) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setNombre(resultSet.getString("nombre"));
        usuario.setApellido(resultSet.getString("apellido"));
        usuario.setIdentificacion(resultSet.getString("identificacion"));
        usuario.setEmail(resultSet.getString("email"));
        usuario.setPassword(resultSet.getString("password"));
        usuario.setRol(Rol.valueOf(resultSet.getString("rol")));
        usuario.setCuentaVerificada(resultSet.getBoolean("cuenta_verificada"));

        if (resultSet.getDate("fecha_registro") != null) {
            usuario.setFechaRegistro(resultSet.getDate("fecha_registro").toLocalDate());
        }

        return usuario;
    }


}