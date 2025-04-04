package co.edu.uniquindio.agenciaturistica.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para manejar la conexión a la base de datos
 */
public class DataBaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/empresa_turistica";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection connection;

    /**
     * Método para obtener la conexión a la base de datos
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if(connection == null) {
            try{
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión exitosa a la base de datos");
            }catch (SQLException e){
                throw new SQLException("Error al conectar a la base de datos", e);
            }
        }
        return connection;
    }

    /**
     * Método para cerrar la conexión a la base de datos
     */
    public static void closeConnection() {
        try{
            if(connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión cerrada");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cerrar la conexión a la base de datos", e);
        }
    }

}
