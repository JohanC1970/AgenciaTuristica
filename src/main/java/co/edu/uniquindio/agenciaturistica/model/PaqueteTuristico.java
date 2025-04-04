package co.edu.uniquindio.agenciaturistica.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaqueteTuristico {

    private int id;
    private String nombre;
    private String descripcion;
    private double precioBase;
    private int duracionDias;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int cupoMaximo;
    private int cuposDisponibles;
    private List<Actividad>actividades;
    private List<Hospedaje>hospedajes;

    public PaqueteTuristico() {
        this.actividades = new ArrayList<Actividad>();
        this.hospedajes = new ArrayList<Hospedaje>();
    }

    public PaqueteTuristico(String nombre, String descripcion, double precioBase, int duracionDias,
                            LocalDate fechaInicio, LocalDate fechaFin, int cupoMaximo, int cuposDisponibles){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.duracionDias = duracionDias;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cupoMaximo = cupoMaximo;
        this.cuposDisponibles = cuposDisponibles;
        this.actividades = new ArrayList<Actividad>();
        this.hospedajes = new ArrayList<Hospedaje>();

    }

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
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

    public int getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(int duracionDias) {
        this.duracionDias = duracionDias;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public List<Hospedaje> getHospedajes() {
        return hospedajes;
    }

    public void setHospedajes(List<Hospedaje> hospedajes) {
        this.hospedajes = hospedajes;
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

    public double getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    @Override
    public String toString() {
        return "PaqueteTuristico{" +
                "actividades=" + actividades +
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precioBase=" + precioBase +
                ", duracionDias=" + duracionDias +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", cupoMaximo=" + cupoMaximo +
                ", cuposDisponibles=" + cuposDisponibles +
                ", hospedajes=" + hospedajes +
                '}';
    }

    public double calcularPrecioTotal(){
        return 0;
    }

    public void agregarActividad(Actividad actividad){
        this.actividades.add(actividad);
    }

    public void agregarHospedaje(Hospedaje hospedaje){
        this.hospedajes.add(hospedaje);
    }

    public boolean verificarDisponibilidad(){
        return false;
    }

    public boolean reservarCupo(){
        return false;
    }

    public void liberarCupo(){

    }

}
