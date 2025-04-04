package co.edu.uniquindio.agenciaturistica.exception;

public class AccountNotVerifiedException extends AuthenticationException {

    public AccountNotVerifiedException() {
        super("La cuenta no ha sido verificada");
    }
}

