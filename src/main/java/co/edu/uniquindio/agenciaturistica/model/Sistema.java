package co.edu.uniquindio.agenciaturistica.model;

import java.util.List;

public class Sistema {

    private String nombre;

    public Sistema(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Usuario iniciarSesion(String email, String password) {
        // Lógica para iniciar sesión
        return null;
    }

    public void cerrarSesion(Usuario usuario) {
        // Lógica para cerrar sesión
    }

    public boolean enviarCodigoVerificacion(String email) {
        // Lógica para enviar código de verificación
        return true;
    }

    public boolean enviarCodigoRecuperacionPassword(String email) {
        // Lógica para enviar código de recuperación de contraseña
        return true;
    }

    public List<Reporte>generarReportes() {
        // Lógica para generar reportes
        return null;
    }

}
