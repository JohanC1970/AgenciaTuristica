package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Habitacion;
import co.edu.uniquindio.agenciaturistica.model.Hospedaje;
import co.edu.uniquindio.agenciaturistica.model.TipoHabitacion;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class EmpleadoHabitacionesController implements Initializable {

    @FXML
    private Label lblTituloHospedaje;

    @FXML
    private Label lblTotalHabitaciones;

    @FXML
    private TableView<Habitacion> tablaHabitaciones;

    @FXML
    private TableColumn<Habitacion, Integer> colId;

    @FXML
    private TableColumn<Habitacion, String> colTipo;

    @FXML
    private TableColumn<Habitacion, Integer> colCapacidad;

    @FXML
    private TableColumn<Habitacion, Double> colPrecio;

    @FXML
    private TableColumn<Habitacion, Boolean> colDisponibilidad;

    @FXML
    private TableColumn<Habitacion, String> colAcciones;

    @FXML
    private Label lblMensaje;

    private Aplicacion aplicacion;
    private Hospedaje hospedaje;
    private List<Habitacion> listaHabitaciones;
    private ObservableList<Habitacion> habitacionesObservable = FXCollections.observableArrayList();

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
     * Método para establecer el hospedaje
     * @param hospedaje
     */
    public void setHospedaje(Hospedaje hospedaje) {
        this.hospedaje = hospedaje;
        lblTituloHospedaje.setText("Habitaciones de " + hospedaje.getNombre());
    }

    /**
     * Método para inicializar los datos
     */
    public void inicializarDatos() {
        try {
            // Cargar lista de habitaciones del hospedaje
            if (hospedaje != null) {
                listaHabitaciones = ModelFactoryController.getInstance().getSistema()
                        .obtenerHabitacionesPorHospedaje(hospedaje.getId());

                habitacionesObservable.clear();

                if (listaHabitaciones != null) {
                    // Guardar las habitaciones en el objeto hospedaje
                    hospedaje.setHabitaciones(listaHabitaciones);
                    // Añadir a la lista observable
                    habitacionesObservable.addAll(listaHabitaciones);
                }

                // Actualizar tabla
                tablaHabitaciones.setItems(habitacionesObservable);

                // Actualizar contador
                lblTotalHabitaciones.setText(String.valueOf(habitacionesObservable.size()));
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos de habitaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para configurar la tabla de habitaciones
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colTipo.setCellValueFactory(cellData -> {
            TipoHabitacion tipoHabitacion = cellData.getValue().getTipoHabitacion();
            return new SimpleStringProperty(tipoHabitacion != null ? tipoHabitacion.getNombre() : "No disponible");
        });

        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioPorNoche"));
        colPrecio.setCellFactory(column -> new TableCell<Habitacion, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.0f", item));
                }
            }
        });

        colDisponibilidad.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        colDisponibilidad.setCellFactory(column -> new TableCell<Habitacion, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Disponible" : "No disponible");
                    setStyle(item ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
                }
            }
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());
    }

    /**
     * Método para crear la columna de botones de acciones
     * @return Factory para crear celdas con botones
     */
    private Callback<TableColumn<Habitacion, String>, TableCell<Habitacion, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Habitacion, String>, TableCell<Habitacion, String>>() {
            @Override
            public TableCell<Habitacion, String> call(TableColumn<Habitacion, String> param) {
                return new TableCell<Habitacion, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnVer = new Button("Ver");
                    private final HBox hbox = new HBox(5, btnVer, btnEditar);

                    {
                        // Configurar botón Ver
                        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnVer.setCursor(javafx.scene.Cursor.HAND);
                        btnVer.setOnAction(event -> {
                            Habitacion habitacion = getTableView().getItems().get(getIndex());
                            verDetallesHabitacion(habitacion);
                        });

                        // Configurar botón Editar
                        btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                        btnEditar.setCursor(javafx.scene.Cursor.HAND);
                        btnEditar.setOnAction(event -> {
                            Habitacion habitacion = getTableView().getItems().get(getIndex());
                            editarHabitacion(habitacion);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(hbox);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para crear una nueva habitación
     * @param event
     */
    @FXML
    void nuevaHabitacion(ActionEvent event) {
        // Aquí se podría abrir un formulario para crear una nueva habitación
        mostrarAlerta("Información", "Funcionalidad no implementada: Nueva Habitación", AlertType.INFORMATION);
    }

    /**
     * Método para ver los detalles de una habitación
     * @param habitacion Habitación a visualizar
     */
    private void verDetallesHabitacion(Habitacion habitacion) {
        TipoHabitacion tipo = habitacion.getTipoHabitacion();

        StringBuilder caracteristicas = new StringBuilder();
        if (tipo != null && tipo.getCaracteristicas() != null) {
            for (String caracteristica : tipo.getCaracteristicas()) {
                caracteristicas.append("- ").append(caracteristica).append("\n");
            }
        }

        mostrarAlerta("Detalles de la Habitación",
                "ID: " + habitacion.getId() + "\n" +
                        "Tipo: " + (tipo != null ? tipo.getNombre() : "No disponible") + "\n" +
                        "Capacidad: " + habitacion.getCapacidad() + " personas\n" +
                        "Precio por noche: $" + String.format("%,.0f", habitacion.getPrecioPorNoche()) + "\n" +
                        "Disponible: " + (habitacion.isDisponible() ? "Sí" : "No") + "\n\n" +
                        "Características:\n" + (caracteristicas.length() > 0 ? caracteristicas.toString() : "No se han registrado características"),
                AlertType.INFORMATION);
    }

    /**
     * Método para editar una habitación
     * @param habitacion Habitación a editar
     */
    private void editarHabitacion(Habitacion habitacion) {
        // Aquí se podría abrir un formulario para editar la habitación
        mostrarAlerta("Información", "Funcionalidad no implementada: Editar Habitación", AlertType.INFORMATION);
    }

    /**
     * Método para cerrar la ventana
     * @param event
     */
    @FXML
    void cerrar(ActionEvent event) {
        tablaHabitaciones.getScene().getWindow().hide();
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