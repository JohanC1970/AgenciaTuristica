package co.edu.uniquindio.agenciaturistica.util;

public class Respuesta <T>{

    private boolean exito;
    private String mensaje;
    private T data;

    public Respuesta(boolean exito, String mensaje, T data) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.data = data;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
