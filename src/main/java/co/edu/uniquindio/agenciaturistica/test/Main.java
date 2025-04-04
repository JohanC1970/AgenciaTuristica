package co.edu.uniquindio.agenciaturistica.test;

import co.edu.uniquindio.agenciaturistica.model.Sistema;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {

        Sistema sistema = new Sistema("Agencia Turistica");

        sistema.enviarCodigoVerificacion("difeso2895@movfull.com", "Prueba123");
    }
}
