package co.edu.uniquindio.agenciaturistica.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Habitacion {

    private int id;
    private TipoHabitacion tipoHabitacion;
    private int capacidad;
    private double precioPorNoche;
    private boolean disponible;
    private List<Reserva> reservas;

    public Habitacion() {
        this.reservas = new ArrayList<>();
    }
    public Habitacion(int id, TipoHabitacion tipoHabitacion, int capacidad, double precioPorNoche, boolean disponible) {
        this.id = id;
        this.tipoHabitacion = tipoHabitacion;
        this.capacidad = capacidad;
        this.precioPorNoche = precioPorNoche;
        this.disponible = disponible;
        this.reservas = new ArrayList<>();
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(double precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public TipoHabitacion getTipoHabitacion() {
        return tipoHabitacion;
    }

    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }

    @Override
    public String toString() {
        return "Habitacion{" +
                "capacidad=" + capacidad +
                ", id=" + id +
                ", tipoHabitacion=" + tipoHabitacion +
                ", precioPorNoche=" + precioPorNoche +
                ", disponible=" + disponible +
                ", reservas=" + reservas +
                '}';
    }

    public boolean verificarDisponibilidad(LocalDate fechaInicio, LocalDate fechaFin) {
        return false;
    }

    public boolean reservar(LocalDate fechaInicio, LocalDate fechaFin) {
        return false;
    }

    public void cancelarReserva(Reserva reserva) {

    }
}
