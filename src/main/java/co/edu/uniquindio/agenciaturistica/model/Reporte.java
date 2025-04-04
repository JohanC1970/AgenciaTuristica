package co.edu.uniquindio.agenciaturistica.model;

import co.edu.uniquindio.agenciaturistica.model.Enums.TipoReporte;

import java.io.File;
import java.time.LocalDateTime;

public class Reporte {

    private int id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaGeneracion;
    private TipoReporte tipoReporte;
    private String contenido;

    public Reporte() {
    }

    public Reporte(int id, String titulo, String descripcion, LocalDateTime fechaGeneracion, TipoReporte tipoReporte, String contenido) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaGeneracion = fechaGeneracion;
        this.tipoReporte = tipoReporte;
        this.contenido = contenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TipoReporte getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(TipoReporte tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public String toString() {
        return "Reporte{" +
                "contenido='" + contenido + '\'' +
                ", id=" + id +
                ", titulo='" + titulo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaGeneracion=" + fechaGeneracion +
                ", tipoReporte=" + tipoReporte +
                '}';
    }

    public File exportarPDF() {
        // Implementación para exportar el reporte a un archivo PDF
        return null;
    }

    public File exportarExcel() {
        // Implementación para exportar el reporte a un archivo Excel
        return null;
    }

    public boolean enviarPorEmail(String email) {
        // Implementación para enviar el reporte por correo electrónico
        return false;
    }

}
