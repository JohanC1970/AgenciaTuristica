package co.edu.uniquindio.agenciaturistica.exception;

public class EmailFoundException extends AuthenticationException {
    public EmailFoundException(String email) {
        super("Email no encontrado: " + email);
    }
}
