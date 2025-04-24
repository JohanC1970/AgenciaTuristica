package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Hospedaje;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class EmpleadoHospedajesController implements Initializable {

    @FXML
    private TextField txtBusqueda;

    @FXML
    private Label lblTotalHospedajes;

    @FXML
    private TableView<Hospedaje> tablaHospedajes;

    @FXML
    private TableColumn<Hospedaje, String> colNombre;

    @FXML
    private TableColumn<Hospedaje, String> colCiudad;

    @FXML
    private TableColumn<Hospedaje, String> colDireccion;

    @FXML
    private TableColumn<Hospedaje, String> colTelefono;

    @FXML
    private TableColumn<Hospedaje, Integer> colEstrellas;

    @FXML
    private TableColumn<Hospedaje, Integer> colHabitaciones;

    @FXML
    private TableColumn<Hospedaje, String> colAcciones;

    @FXML
    private Label lblMensaje;

    private Aplicacion aplicacion;
    private List<Hospedaje> listaHospedajes;
    private ObservableList<Hospedaje> hospedajesObservable = FXCollections.observableArrayList();

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
     * Método para inicializar los datos
     */
    public void inicializarDatos() {
        try {
            // Cargar lista de hospedajes
            listaHospedajes = ModelFactoryController.getInstance().getSistema().obtenerHospedajes();
            hospedajesObservable.clear();

            if (listaHospedajes != null) {
                hospedajesObservable.addAll(listaHospedajes);
            }

            // Actualizar tabla
            tablaHospedajes.setItems(hospedajesObservable);

            // Actualizar contador
            lblTotalHospedajes.setText(String.valueOf(hospedajesObservable.size()));

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos de hospedajes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para configurar la tabla de hospedajes
     */
    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colEstrellas.setCellValueFactory(new PropertyValueFactory<>("estrellas"));
        colEstrellas.setCellFactory(column -> new TableCell<Hospedaje, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Mostrar estrellas como ★★★★★
                    StringBuilder estrellas = new StringBuilder();
                    for (int i = 0; i < item; i++) {
                        estrellas.append("★");
                    }
                    setText(estrellas.toString());
                }
            }
        });

        colHabitaciones.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHabitaciones() != null) {
                return new SimpleIntegerProperty(cellData.getValue().getHabitaciones().size()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject();
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());
    }

    /**
     * Método para crear la columna de botones de acciones
     * @return Factory para crear celdas con botones
     */
    private Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>>() {
            @Override
            public TableCell<Hospedaje, String> call(TableColumn<Hospedaje, String> param) {
                return new TableCell<Hospedaje, String>() {
                    private final Button btnVerHabs = new Button("Habitaciones");
                    private final Button btnVer = new Button("Ver");
                    private final HBox hbox = new HBox(5, btnVer, btnVerHabs);

                    {
                        // Configurar botón Ver
                        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnVer.setCursor(javafx.scene.Cursor.HAND);
                        btnVer.setOnAction(event -> {
                            Hospedaje hospedaje = getTableView().getItems().get(getIndex());
                            verDetallesHospedaje(hospedaje);
                        });

                        // Configurar botón Ver Habitaciones
                        btnVerHabs.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
                        btnVerHabs.setCursor(javafx.scene.Cursor.HAND);
                        btnVerHabs.setOnAction(event -> {
                            Hospedaje hospedaje = getTableView().getItems().get(getIndex());
                            verHabitacionesHospedaje(hospedaje);
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
     * Método para buscar hospedajes
     * @param event
     */
    @FXML
    void buscarHospedaje(ActionEvent event) {
        String textoBusqueda = txtBusqueda.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos los hospedajes
            tablaHospedajes.setItems(hospedajesObservable);
            lblMensaje.setText("");
            return;
        }

        // Filtrar hospedajes por nombre, ciudad o dirección
        ObservableList<Hospedaje> hospedajesFiltrados = FXCollections.observableArrayList();

        for (Hospedaje hospedaje : hospedajesObservable) {
            if (hospedaje.getNombre().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getCiudad().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getDireccion().toLowerCase().contains(textoBusqueda)) {
                hospedajesFiltrados.add(hospedaje);
            }
        }

        tablaHospedajes.setItems(hospedajesFiltrados);

        // Mostrar mensaje si no hay resultados
        if (hospedajesFiltrados.isEmpty()) {
            lblMensaje.setText("No se encontraron hospedajes que coincidan con la búsqueda");
        } else {
            lblMensaje.setText("");
        }

        // Actualizar contador
        lblTotalHospedajes.setText(String.valueOf(hospedajesFiltrados.size()));
    }

    /**
     * Método para crear un nuevo hospedaje
     * @param event
     */
    @FXML
    void nuevoHospedaje(ActionEvent event) {
        // Aquí se podría abrir un formulario para crear un nuevo hospedaje
        mostrarAlerta("Información", "Funcionalidad no implementada: Nuevo Hospedaje", AlertType.INFORMATION);
    }

    /**
     * Método para ver los detalles de un hospedaje
     * @param hospedaje Hospedaje a visualizar
     */
    private void verDetallesHospedaje(Hospedaje hospedaje) {
        StringBuilder estrellas = new StringBuilder();
        for (int i = 0; i < hospedaje.getEstrellas(); i++) {
            estrellas.append("★");
        }

        mostrarAlerta("Detalles del Hospedaje",
                "Nombre: " + hospedaje.getNombre() + "\n" +
                        "Ciudad: " + hospedaje.getCiudad() + "\n" +
                        "Dirección: " + hospedaje.getDireccion() + "\n" +
                        "Teléfono: " + hospedaje.getTelefono() + "\n" +
                        "Estrellas: " + estrellas.toString() + "\n" +
                        "Descripción: " + hospedaje.getDescripcion() + "\n" +
                        "Habitaciones: " + (hospedaje.getHabitaciones() != null ? hospedaje.getHabitaciones().size() : 0),
                AlertType.INFORMATION);
    }

    /**
     * Método para ver las habitaciones de un hospedaje
     * @param hospedaje Hospedaje seleccionado
     */
    private void verHabitacionesHospedaje(Hospedaje hospedaje) {
        try {
            // Cargar la vista de habitaciones
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/EmpleadoHabitaciones.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            EmpleadoHabitacionesController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setHospedaje(hospedaje);
            controller.inicializarDatos();

            // Mostrar la ventana
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Habitaciones - " + hospedaje.getNombre());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir ventana de habitaciones: " + e.getMessage(), AlertType.ERROR);
        }
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