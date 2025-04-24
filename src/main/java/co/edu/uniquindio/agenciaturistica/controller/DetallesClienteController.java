package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Cliente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DetallesClienteController implements Initializable {

    @FXML
    private Label lblIdentificacion;

    @FXML
    private Label lblNombre;

    @FXML
    private Label lblApellido;

    @FXML
    private Label lblCorreo;

    @FXML
    private Label lblTelefono;

    @FXML
    private Label lblFechaNacimiento;

    @FXML
    private Label lblCantidadReservas;

    @FXML
    private Button btnVerReservas;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnCerrar;

    private Aplicacion aplicacion;
    private Cliente cliente;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
    }

    /**
     * Método para establecer el cliente a mostrar
     * @param cliente
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Método para inicializar los datos del cliente en la vista
     */
    public void inicializarDatosCliente() {
        if (cliente != null) {
            lblIdentificacion.setText(cliente.getIdentificacion());
            lblNombre.setText(cliente.getNombre());
            lblApellido.setText(cliente.getApellido());
            lblCorreo.setText(cliente.getCorreo());
            lblTelefono.setText(cliente.getTelefono());

            if (cliente.getFechaNacimiento() != null) {
                lblFechaNacimiento.setText(cliente.getFechaNacimiento().format(formatter));
            } else {
                lblFechaNacimiento.setText("No disponible");
            }

            int cantidadReservas = (cliente.getHistorialReservas() != null) ?
                    cliente.getHistorialReservas().size() : 0;
            lblCantidadReservas.setText(String.valueOf(cantidadReservas));
        }
    }

    /**
     * Método para editar el cliente
     * @param event
     */
    @FXML
    void editarCliente(ActionEvent event) {
        try {
            // Cargar la vista de formulario de cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/FormularioCliente.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            FormularioClienteController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setModoEdicion(true);
            controller.setCliente(cliente);
            controller.inicializarDatosCliente();

            // Mostrar la ventana
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Editar Cliente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Cerrar esta ventana
            cerrarVentana();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir formulario de edición: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para ver el historial de reservas del cliente
     * @param event
     */
    @FXML
    void verReservas(ActionEvent event) {
        try {
            // Cargar la vista de historial de reservas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/HistorialReservasCliente.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            HistorialReservasClienteController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setCliente(cliente);
            controller.inicializarDatos();

            // Mostrar la ventana
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Historial de Reservas - " + cliente.getNombre() + " " + cliente.getApellido());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir historial de reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cerrar la ventana
     * @param event
     */
    @FXML
    void cerrarVentana(ActionEvent event) {
        cerrarVentana();
    }

    /**
     * Método para cerrar la ventana
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
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