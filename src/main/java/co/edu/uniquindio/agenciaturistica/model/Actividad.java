package co.edu.uniquindio.agenciaturistica.model;

import java.time.LocalDateTime;

public class Actividad {

    private int id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private double precio;
    private int duracion; // en horas
    private int cupoMaximo;
    private int cuposDisponibles;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public Actividad() {}
    public Actividad(int id, String nombre, String descripcion, String ubicacion, double precio, int duracion,
                     int cupoMaximo, int cuposDisponibles, LocalDateTime fechaInicio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.precio = precio;
        this.duracion = duracion;
        this.cupoMaximo = cupoMaximo;
        this.cuposDisponibles = cuposDisponibles;
        this.fechaInicio = fechaInicio;
        this.fechaFin = calcularFechaFin(fechaInicio, duracion);
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public int getCuposDisponibles() {
        return cuposDisponibles;
    }

    public void setCuposDisponibles(int cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public String toString() {
        return "Actividad{" +
                "cupoMaximo=" + cupoMaximo +
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", precio=" + precio +
                ", duracion=" + duracion +
                ", cuposDisponibles=" + cuposDisponibles +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                '}';
    }

    private LocalDateTime calcularFechaFin(LocalDateTime fechaInicio, int duracion) {
        return fechaInicio.plusHours(duracion);
    }

    public boolean verificarDisponibilidad(int cantidad) {
        return cuposDisponibles >= cantidad;
    }

    public boolean reservarCupo(){
        return false;
    }

    public boolean liberarCupo(){
        return false;
    }



}
