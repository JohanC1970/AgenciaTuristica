package co.edu.uniquindio.agenciaturistica.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Hospedaje {

    private int id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String telefono;
    private int estrellas;
    private String descripcion;
    private List<Habitacion> habitaciones;

    public Hospedaje() {
        this.habitaciones = new ArrayList<Habitacion>();
    }
    public Hospedaje(int id, String nombre, String direccion, String ciudad, String telefono, int estrellas,
                     String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.telefono = telefono;
        this.estrellas = estrellas;
        this.descripcion = descripcion;
        this.habitaciones = new ArrayList<Habitacion>();
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(int estrellas) {
        this.estrellas = estrellas;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Hospedaje{" +
                "ciudad='" + ciudad + '\'' +
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", direccion='" + direccion + '\'' +
                ", telefono='" + telefono + '\'' +
                ", estrellas=" + estrellas +
                ", descripcion='" + descripcion + '\'' +
                ", habitaciones=" + habitaciones +
                '}';
    }

    public List<Habitacion> getHabitacionesDisponibles(LocalDate fechaInicio, LocalDate fechaFin) {
        return null;
    }

}
