package co.edu.uniquindio.agenciaturistica.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase para encriptar y verificar contraseñas
 */
public class PasswordEncoder {

    /**
     * Encripta una contraseña usando BCrypt
     * @param password
     * @return
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifica si una contraseña coincide con su hash
     * @param password
     * @param hashedPassword
     * @return
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

}
