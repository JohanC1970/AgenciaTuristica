package co.edu.uniquindio.agenciaturistica.exception;

public class IncorrectPasswordException extends AuthenticationException {
    public IncorrectPasswordException() {
        super("La contraseña es incorrecta");
    }
}
