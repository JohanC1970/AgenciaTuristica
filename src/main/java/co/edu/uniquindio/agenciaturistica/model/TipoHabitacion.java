package co.edu.uniquindio.agenciaturistica.model;

import java.util.ArrayList;
import java.util.List;

public class TipoHabitacion {

    private int id;
    private String nombre;
    private String descripcion;
    private List<String>caracteristicas;
    private double precio;

    public TipoHabitacion() {
        this.caracteristicas = new ArrayList<>();
    }
    public TipoHabitacion(int id, String nombre, String descripcion, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.caracteristicas = new ArrayList<>();
        this.precio = precio;
    }

    public List<String> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<String> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    @Override
    public String toString() {
        return "TipoHabitacion{" +
                "caracteristicas=" + caracteristicas +
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                '}';
    }


}
