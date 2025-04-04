package co.edu.uniquindio.agenciaturistica.dao;

import co.edu.uniquindio.agenciaturistica.exception.AccountNotVerifiedException;
import co.edu.uniquindio.agenciaturistica.exception.AuthenticationException;
import co.edu.uniquindio.agenciaturistica.exception.EmailFoundException;
import co.edu.uniquindio.agenciaturistica.exception.IncorrectPasswordException;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.DataBaseConnection;
import co.edu.uniquindio.agenciaturistica.util.PasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


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

}
