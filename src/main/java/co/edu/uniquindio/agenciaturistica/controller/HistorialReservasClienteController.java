package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Cliente;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Reserva;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HistorialReservasClienteController implements Initializable {

    @FXML
    private Label lblNombreCliente;

    @FXML
    private Label lblTotalReservas;

    @FXML
    private TableView<Reserva> tablaReservas;

    @FXML
    private TableColumn<Reserva, String> colId;

    @FXML
    private TableColumn<Reserva, LocalDate> colFechaInicio;

    @FXML
    private TableColumn<Reserva, LocalDate> colFechaFin;

    @FXML
    private TableColumn<Reserva, String> colPrecioTotal;

    @FXML
    private TableColumn<Reserva, EstadoReserva> colEstado;

    @FXML
    private TableColumn<Reserva, Button> colAcciones;

    private Aplicacion aplicacion;
    private Cliente cliente;
    private List<Reserva> listaReservas;
    private ObservableList<Reserva> reservasObservable = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas de la tabla
        configurarTabla();
    }

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    /**
     * Método para establecer el cliente
     * @param cliente
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Método para inicializar los datos
     */
    public void inicializarDatos() {
        try {
            // Actualizar label con nombre del cliente
            lblNombreCliente.setText(cliente.getNombre() + " " + cliente.getApellido());

            // Cargar reservas del cliente
            cargarReservasCliente();

            // Actualizar total de reservas
            lblTotalReservas.setText(String.valueOf(reservasObservable.size()));

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cargar las reservas del cliente
     */
    private void cargarReservasCliente() throws SQLException {
        // Obtener reservas del cliente
        listaReservas = cliente.getHistorialReservas();

        // Si el historial de reservas está vacío, buscamos desde la base de datos
        if (listaReservas == null || listaReservas.isEmpty()) {
            listaReservas = ModelFactoryController.getInstance().getSistema()
                    .buscarReservasPorCliente(cliente.getIdentificacion());
        }

        // Actualizar tabla
        reservasObservable.clear();
        reservasObservable.addAll(listaReservas);
        tablaReservas.setItems(reservasObservable);
    }

    /**
     * Método para configurar la tabla de reservas
     */
    private void configurarTabla() {
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        colFechaInicio.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFechaInicio()));
        colFechaInicio.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        colFechaFin.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFechaFin()));
        colFechaFin.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        colPrecioTotal.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("$%,.0f", cellData.getValue().getPrecioTotal())));

        colEstado.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getEstadoReserva()));
        colEstado.setCellFactory(column -> new TableCell<Reserva, EstadoReserva>() {
            @Override
            protected void updateItem(EstadoReserva item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case PENDIENTE:
                            setStyle("-fx-text-fill: #e67e22;");
                            break;
                        case CONFIRMADA:
                            setStyle("-fx-text-fill: #2ecc71;");
                            break;
                        case CANCELADA:
                            setStyle("-fx-text-fill: #e74c3c;");
                            break;
                        case COMPLETADA:
                            setStyle("-fx-text-fill: #3498db;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        colAcciones.setCellFactory(param -> new TableCell<Reserva, Button>() {
            private final Button btnVerDetalles = new Button("Ver detalles");

            {
                btnVerDetalles.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnVerDetalles.setOnAction(event -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    verDetallesReserva(reserva);
                });
            }

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVerDetalles);
                }
            }
        });
    }

    /**
     * Método para ver los detalles de una reserva
     * @param reserva
     */
    private void verDetallesReserva(Reserva reserva) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/DetallesReserva.fxml"));
            Parent root = loader.load();

            DetallesReservaController controller = loader.getController();
            controller.setAplicacion(aplicacion);


            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Detalles de Reserva");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos por si se actualizó el estado de la reserva
            cargarReservasCliente();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir detalles de reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cerrar la ventana
     * @param event
     */
    @FXML
    void cerrar(ActionEvent event) {
        Stage stage = (Stage) lblNombreCliente.getScene().getWindow();
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