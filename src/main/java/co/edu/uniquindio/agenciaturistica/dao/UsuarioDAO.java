package co.edu.uniquindio.agenciaturistica.dao;

import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.PasswordEncoder;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;

import java.sql.*;
import java.time.LocalDateTime;
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
            String query = "INSERT INTO usuario (nombre, apellido, identificacion, email, password, rol) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getApellido());
            preparedStatement.setString(3, usuario.getIdentificacion());
            preparedStatement.setString(4, usuario.getEmail());
            preparedStatement.setString(5, PasswordEncoder.hashPassword(usuario.getPassword()));
            preparedStatement.setString(6, usuario.getRol().toString());

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
        String query = "SELECT 1 FROM usuario WHERE identicacion = ? LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, identificacion);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Este método permite actualizar un usuario en la base de datos.
     * Los unicos datos que no se cambian son la identificacion, email, password y fecha contratacion.
     * @param usuario Usuario a actualizar
     * @return Respuesta<Usuario> Respuesta con el resultado de la operación
     */
    public Respuesta<Usuario> actualizarUsuario(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, apellido = ?, cuenta_verificada = ? WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setBoolean(3, usuario.isCuentaVerificada());
            ps.setString(4, usuario.getEmail());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                return new Respuesta<>(true, "Usuario actualizado correctamente", usuario);
            } else {
                return new Respuesta<>(false, "No se encontró el usuario para actualizar", null);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new Respuesta<>(false, "Error al actualizar el usuario: " + e.getMessage(), null);
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


    public Respuesta<Usuario> eliminarUsuario(String identificacion){

        return null;
    }

}