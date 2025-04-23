package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;

public class EmpleadoController implements Initializable {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Button btnCerrarSesion;

    private Aplicacion aplicacion;
    private Usuario usuarioActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Este método se llama cuando se carga el FXML
    }

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
        this.usuarioActual = aplicacion.getUsuarioActual();
    }

    /**
     * Método para inicializar la información del panel
     */
    public void inicializarInformacion() {
        if (usuarioActual != null) {
            lblNombreUsuario.setText(usuarioActual.getNombre() + " " + usuarioActual.getApellido());
        }
    }

    /**
     * Método para cerrar sesión
     * @param event
     */
    @FXML
    void cerrarSesion(ActionEvent event) {
        aplicacion.cerrarSesion();
    }

    /**
     * Método para mostrar alertas
     * @param titulo
     * @param mensaje
     * @param tipo
     */
    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}