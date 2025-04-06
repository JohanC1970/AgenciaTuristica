package co.edu.uniquindio.agenciaturistica.dao;

import co.edu.uniquindio.agenciaturistica.exception.AccountNotVerifiedException;
import co.edu.uniquindio.agenciaturistica.exception.AuthenticationException;
import co.edu.uniquindio.agenciaturistica.exception.EmailFoundException;
import co.edu.uniquindio.agenciaturistica.exception.IncorrectPasswordException;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.PasswordEncoder;

import java.sql.*;
import java.time.LocalDateTime;


public class UsuarioDAO {

    private Connection connection;
    private PasswordEncoder passwordEncoder;

    public UsuarioDAO() throws SQLException {
        this.passwordEncoder = new PasswordEncoder();
        try{
            this.connection = DataBaseConnection.getConnection();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Autenticar a un usuario por email y contraseña
     * @param email Email del usuario
     * @param password Contraseña del usuario sin encriptar
     * @return Usuario autenticado o null si la autenticacion falla
     */
    public Usuario iniciarSesion(String email, String password) throws AuthenticationException,SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Usuario usuario = null;

        try{
            String query = "SELECT * FROM usuario WHERE email = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                //Obtener la contraseña encriptada de la base de datos
                String passwordEncriptada = resultSet.getString("password");
                //Verificamos si la contraseña ingresada es correcta
                if(passwordEncoder.verifyPassword(password, passwordEncriptada)) {

                    boolean cuentaVerificada = resultSet.getBoolean("cuenta_verificada");
                    if(!cuentaVerificada){
                        throw new AccountNotVerifiedException();
                    }

                    usuario = new Usuario();
                    usuario.setNombre(resultSet.getString("nombre"));
                    usuario.setApellido(resultSet.getString("apellido"));
                    usuario.setEmail(resultSet.getString("email"));
                    usuario.setPassword("********");
                    usuario.setRol(Rol.valueOf(resultSet.getString("rol")));
                    usuario.setFechaRegistro(resultSet.getDate("fecha_registro").toLocalDate());

                }else{
                    throw new IncorrectPasswordException();
                }
            }else {
                throw new EmailFoundException(email);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al iniciar sesión: " + e.getMessage());
        } finally {
            try{
                if(resultSet != null) resultSet.close();
                if(preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                throw new SQLException("Error al cerrar la conexión: " + e.getMessage());
            }
        }

        return usuario;
    }

    /**
     * Verifica si el email existe en la base de datos
     * @param destinatario Email a verificar
     * @return true si el email existe, false en caso contrario
     */
    public boolean existeEmail(String destinatario) {
        String query = "SELECT 1 FROM usuario WHERE email = ? LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, destinatario);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Este metodo actualiza el codigo de recuperacion y la fecha de expiracion, en la base de datos
     * @param destinatario email de la persona que solicita el codigo
     * @param codigo codigo para restablecer la contraseña
     * @return true en caso de que si se actualizo el codigo, false en caso contrario
     */
    public boolean actualizarCodigoRecuperacion(String destinatario, String codigo) {

        PreparedStatement preparedStatement = null;
        try{
            if(!existeEmail(destinatario)){
                return false;
            }

            String query = "UPDATE usuario SET codigo_recuperacion_password = ?, expiracion_codigo_recuperacion_password = ? WHERE email = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, codigo);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusHours(1)));
            preparedStatement.setString(3, destinatario);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try{
                if(preparedStatement != null){
                    preparedStatement.close();
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * Este metodo revierte los cambios en la base de datos (codigo y fecha de expiracion)
     * @param destinatario
     * @return
     */
    public boolean revertirCodigoRecuperacion(String destinatario) {
        String query = "UPDATE usuario SET codigo_recuperacion_password = NULL, expiracion_codigo_recuperacion_password = NULL WHERE email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, destinatario);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Si se actualizó al menos una fila, se devuelve true
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
