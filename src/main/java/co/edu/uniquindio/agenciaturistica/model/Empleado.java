package co.edu.uniquindio.agenciaturistica.model;

import java.time.LocalDate;
import java.util.List;

public class Empleado extends Usuario{

    private int id;
    private LocalDate fechaContratacion;

    public Empleado() {
    }


    public Reserva crearReserva(){

        return null;
    }

    public List<Reserva> consultarReservas() {
        return null;
    }

    public boolean modificarReserva(Reserva reserva) {
        return false;
    }

    public boolean cancelarReserva(Reserva reserva) {
        return false;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }
}
