package co.edu.uniquindio.agenciaturistica.model;

import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Enums.FormaPago;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reserva {

    private String id;
    private Cliente cliente;
    private Empleado empleado;
    private PaqueteTuristico paqueteTuristico;
    private List<Habitacion>habitaciones;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private double precioTotal;
    private EstadoReserva estadoReserva;
    private FormaPago formaPago;

    public Reserva() {
        this.habitaciones = new ArrayList<Habitacion>();
    }

    public Reserva(String id, Cliente cliente, Empleado empleado, PaqueteTuristico paqueteTuristico, LocalDate fechaInicio, LocalDate fechaFin, double precioTotal, EstadoReserva estadoReserva, FormaPago formaPago) {
        this.id = id;
        this.cliente = cliente;
        this.empleado = empleado;
        this.paqueteTuristico = paqueteTuristico;
        this.habitaciones = new ArrayList<Habitacion>();
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.precioTotal = precioTotal;
        this.estadoReserva = estadoReserva;
        this.formaPago = formaPago;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoReserva getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(EstadoReserva estadoReserva) {
        this.estadoReserva = estadoReserva;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PaqueteTuristico getPaqueteTuristico() {
        return paqueteTuristico;
    }

    public void setPaqueteTuristico(PaqueteTuristico paqueteTuristico) {
        this.paqueteTuristico = paqueteTuristico;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "cliente=" + cliente +
                ", id='" + id + '\'' +
                ", empleado=" + empleado +
                ", paqueteTuristico=" + paqueteTuristico +
                ", habitaciones=" + habitaciones +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", precioTotal=" + precioTotal +
                ", estadoReserva=" + estadoReserva +
                ", formaPago=" + formaPago +
                '}';
    }

    private double calcularPrecioTotal() {
        return 0;
    }

    private boolean confirmarReserva() {
        // Lógica para confirmar la reserva
        return false;
    }

    private boolean cancelarReserva() {
        // Lógica para cancelar la reserva
        return false;
    }

    private boolean modificarReserva() {
        // Lógica para modificar la reserva
        return false;
    }

    private String generarVoucher() {
        // Lógica para generar el voucher de la reserva
        return null;
    }



}
