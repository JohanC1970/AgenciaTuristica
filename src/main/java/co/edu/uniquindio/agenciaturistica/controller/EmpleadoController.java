package co.edu.uniquindio.agenciaturistica.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class EmpleadoController implements Initializable {

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnClientes;

    @FXML
    private Button btnPaquetes;

    @FXML
    private Button btnActividades;

    @FXML
    private Button btnHospedajes;

    @FXML
    private Button btnHabitaciones;

    @FXML
    private Button btnReservas;

    @FXML
    private Button btnReportes;

    private Aplicacion aplicacion;
    private Usuario usuarioActual;
    private BorderPane rootPane;

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
            Platform.runLater(() -> {
                lblNombreUsuario.setText(usuarioActual.getNombre() + " " + usuarioActual.getApellido());

                // Obtener referencia al BorderPane una vez la escena está disponible
                rootPane = (BorderPane) lblNombreUsuario.getScene().getRoot();

                // Mostrar dashboard por defecto
                mostrarDashboard(null);
            });
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
     * Método para mostrar el dashboard
     * @param event
     */
    @FXML
    void mostrarDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/EmpleadoDashboard.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            EmpleadoDashboardController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();
            aplicacion.setUsuarioActual(usuarioActual);
            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnDashboard);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de dashboard: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar la gestión de clientes
     * @param event
     */
    @FXML
    void mostrarClientes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionClientes.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            GestionClientesController controller = loader.getController();
            controller.setAplicacion(aplicacion);

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnClientes);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de clientes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar la gestión de paquetes turísticos
     * @param event
     */
    @FXML
    void mostrarPaquetes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionPaquetes.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            GestionPaquetesController controller = loader.getController();
            controller.setAplicacion(aplicacion);

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnPaquetes);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de paquetes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar la gestión de actividades
     * @param event
     */
    @FXML
    void mostrarActividades(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionActividades.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            GestionActividadesController controller = loader.getController();
            controller.setAplicacion(aplicacion);

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnActividades);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de actividades: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar la gestión de hospedajes
     * @param event
     */
    @FXML
    void mostrarHospedajes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionHospedajes.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            GestionHospedajesController controller = loader.getController();
            controller.setAplicacion(aplicacion);


            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnHospedajes);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de hospedajes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar la gestión de habitaciones
     * @param event
     */
    @FXML
    void mostrarHabitaciones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionHabitaciones.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            GestionHabitacionesController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnHabitaciones);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de habitaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar la gestión de reservas
     * @param event
     */
    @FXML
    void mostrarReservas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionReservas.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            GestionReservasController controller = loader.getController();
            controller.setAplicacion(aplicacion);


            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnReservas);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar los reportes
     * @param event
     */
    @FXML
    void mostrarReportes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/EmpleadoReportes.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            EmpleadoReportesController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

            // Resaltar botón seleccionado
            destacarBotonSeleccionado(btnReportes);

        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar el panel de reportes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para destacar el botón seleccionado y restaurar los demás
     * @param botonSeleccionado
     */
    private void destacarBotonSeleccionado(Button botonSeleccionado) {
        // Lista de todos los botones del menú
        Button[] botones = {btnDashboard, btnClientes, btnPaquetes, btnActividades,
                btnHospedajes, btnHabitaciones, btnReservas, btnReportes};

        // Restaurar estilo por defecto en todos los botones
        for (Button boton : botones) {
            boton.setStyle("-fx-background-color: transparent; -fx-border-color: #2980b9; -fx-border-width: 0 0 1 0;");
        }

        // Destacar el botón seleccionado
        botonSeleccionado.setStyle("-fx-background-color: #2471A3; -fx-border-color: #2980b9; -fx-border-width: 0 0 1 0;");
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