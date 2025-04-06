package co.edu.uniquindio.agenciaturistica.model;

import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario {

    private String nombre;
    private String apellido;
    private String identificacion;
    private String email;
    private String password;
    private Rol rol;
    private String codigoVerificacion;
    private LocalDateTime expiracionCodigoVerificacion;
    private String codigoRecuperacionPassword;
    private LocalDateTime expiracionCodigoRecuperacion;
    private boolean cuentaVerificada;
    private LocalDate fechaRegistro;

    public Usuario(){

    }
    public Usuario(String nombre, String apellido, String identificacion, String email, String password, Rol rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.identificacion = identificacion;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.codigoVerificacion = null;
        this.cuentaVerificada = false;
        this.codigoRecuperacionPassword= null;
        this.expiracionCodigoRecuperacion= null;
        this.expiracionCodigoVerificacion = null;
        this.fechaRegistro = LocalDate.now();
    }



    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public boolean isCuentaVerificada() {
        return cuentaVerificada;
    }

    public void setCuentaVerificada(boolean cuentaVerificada) {
        this.cuentaVerificada = cuentaVerificada;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getCodigoRecuperacionPassword() {
        return codigoRecuperacionPassword;
    }

    public void setCodigoRecuperacionPassword(String codigoRecuperacionPassword) {
        this.codigoRecuperacionPassword = codigoRecuperacionPassword;
    }

    public LocalDateTime getExpiracionCodigoRecuperacion() {
        return expiracionCodigoRecuperacion;
    }

    public void setExpiracionCodigoRecuperacion(LocalDateTime expiracionCodigoRecuperacion) {
        this.expiracionCodigoRecuperacion = expiracionCodigoRecuperacion;
    }

    public LocalDateTime getExpiracionCodigoVerificacion() {
        return expiracionCodigoVerificacion;
    }

    public void setExpiracionCodigoVerificacion(LocalDateTime expiracionCodigoVerificacion) {
        this.expiracionCodigoVerificacion = expiracionCodigoVerificacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(identificacion, usuario.identificacion);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "apellido='" + apellido + '\'' +
                ", nombre='" + nombre + '\'' +
                ", identificacion='" + identificacion + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", rol=" + rol +
                ", codigoVerificacion='" + codigoVerificacion + '\'' +
                ", cuentaVerificada=" + cuentaVerificada +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identificacion);
    }


    public boolean login(String email, String password) {
        return false;
    }

    public boolean verificarCuenta(String codigo){
        if (codigo.equals(codigoVerificacion)) {
            cuentaVerificada = true;
            return true;
        }
        return false;
    }

    public boolean recuperarPassword(String email){
        return false;
    }

    public void cambiarPassword(String newPassword){

    }



}
