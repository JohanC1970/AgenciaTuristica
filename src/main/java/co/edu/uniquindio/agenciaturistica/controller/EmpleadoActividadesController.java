package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Actividad;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class EmpleadoActividadesController implements Initializable {

    @FXML
    private TextField txtBusqueda;

    @FXML
    private Label lblTotalActividades;

    @FXML
    private TableView<Actividad> tablaActividades;

    @FXML
    private TableColumn<Actividad, String> colNombre;

    @FXML
    private TableColumn<Actividad, String> colDescripcion;

    @FXML
    private TableColumn<Actividad, String> colUbicacion;

    @FXML
    private TableColumn<Actividad, Integer> colDuracion;

    @FXML
    private TableColumn<Actividad, LocalDateTime> colFechaInicio;

    @FXML
    private TableColumn<Actividad, String> colPrecio;

    @FXML
    private TableColumn<Actividad, Integer> colCuposDisponibles;

    @FXML
    private TableColumn<Actividad, String> colAcciones;

    @FXML
    private Label lblMensaje;

    private Aplicacion aplicacion;
    private List<Actividad> listaActividades;
    private ObservableList<Actividad> actividadesObservable = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
            // Cargar lista de actividades
            listaActividades = ModelFactoryController.getInstance().getSistema().obtenerActividades();
            actividadesObservable.clear();

            if (listaActividades != null) {
                actividadesObservable.addAll(listaActividades);
            }

            // Actualizar tabla
            tablaActividades.setItems(actividadesObservable);

            // Actualizar contador
            lblTotalActividades.setText(String.valueOf(actividadesObservable.size()));

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos de actividades: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para configurar la tabla de actividades
     */
    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colDescripcion.setCellValueFactory(cellData -> {
            String descripcion = cellData.getValue().getDescripcion();
            if (descripcion != null && descripcion.length() > 40) {
                descripcion = descripcion.substring(0, 37) + "...";
            }
            return new SimpleStringProperty(descripcion);
        });

        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        colDuracion.setCellValueFactory(new PropertyValueFactory<>("duracion"));

        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaInicio.setCellFactory(column -> new TableCell<Actividad, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        colPrecio.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("$%,.0f", cellData.getValue().getPrecio())));

        colCuposDisponibles.setCellValueFactory(new PropertyValueFactory<>("cuposDisponibles"));
        colCuposDisponibles.setCellFactory(column -> new TableCell<Actividad, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    // Colorear los cupos según disponibilidad
                    Actividad actividad = getTableView().getItems().get(getIndex());
                    double porcentajeDisponible = (double) item / actividad.getCupoMaximo();

                    if (porcentajeDisponible <= 0.1) {
                        setStyle("-fx-text-fill: #e74c3c;"); // Rojo - pocos cupos
                    } else if (porcentajeDisponible <= 0.3) {
                        setStyle("-fx-text-fill: #e67e22;"); // Naranja - cupos limitados
                    } else {
                        setStyle("-fx-text-fill: #2ecc71;"); // Verde - cupos disponibles
                    }
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
    private Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>>() {
            @Override
            public TableCell<Actividad, String> call(TableColumn<Actividad, String> param) {
                return new TableCell<Actividad, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnVer = new Button("Ver");
                    private final HBox hbox = new HBox(5, btnVer, btnEditar);

                    {
                        // Configurar botón Ver
                        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnVer.setCursor(javafx.scene.Cursor.HAND);
                        btnVer.setOnAction(event -> {
                            Actividad actividad = getTableView().getItems().get(getIndex());
                            verDetallesActividad(actividad);
                        });

                        // Configurar botón Editar
                        btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                        btnEditar.setCursor(javafx.scene.Cursor.HAND);
                        btnEditar.setOnAction(event -> {
                            Actividad actividad = getTableView().getItems().get(getIndex());
                            editarActividad(actividad);
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
     * Método para buscar actividades por nombre o ubicación
     * @param event
     */
    @FXML
    void buscarActividad(ActionEvent event) {
        String textoBusqueda = txtBusqueda.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todas las actividades
            tablaActividades.setItems(actividadesObservable);
            lblMensaje.setText("");
            return;
        }

        // Filtrar actividades por nombre, descripción o ubicación
        ObservableList<Actividad> actividadesFiltradas = FXCollections.observableArrayList();

        for (Actividad actividad : actividadesObservable) {
            if (actividad.getNombre().toLowerCase().contains(textoBusqueda) ||
                    (actividad.getDescripcion() != null && actividad.getDescripcion().toLowerCase().contains(textoBusqueda)) ||
                    (actividad.getUbicacion() != null && actividad.getUbicacion().toLowerCase().contains(textoBusqueda))) {
                actividadesFiltradas.add(actividad);
            }
        }

        tablaActividades.setItems(actividadesFiltradas);

        // Mostrar mensaje si no hay resultados
        if (actividadesFiltradas.isEmpty()) {
            lblMensaje.setText("No se encontraron actividades que coincidan con la búsqueda");
        } else {
            lblMensaje.setText("");
        }

        // Actualizar contador
        lblTotalActividades.setText(String.valueOf(actividadesFiltradas.size()));
    }

    /**
     * Método para crear una nueva actividad
     * @param event
     */
    @FXML
    void nuevaActividad(ActionEvent event) {
        // Aquí se podría abrir un formulario para crear una nueva actividad
        mostrarAlerta("Información", "Funcionalidad no implementada: Nueva Actividad", AlertType.INFORMATION);
    }

    /**
     * Método para ver los detalles de una actividad
     * @param actividad Actividad a visualizar
     */
    private void verDetallesActividad(Actividad actividad) {
        mostrarAlerta("Detalles de la Actividad",
                "Nombre: " + actividad.getNombre() + "\n" +
                        "Descripción: " + actividad.getDescripcion() + "\n" +
                        "Ubicación: " + actividad.getUbicacion() + "\n" +
                        "Precio: $" + String.format("%,.0f", actividad.getPrecio()) + "\n" +
                        "Duración: " + actividad.getDuracion() + " horas\n" +
                        "Fecha Inicio: " + actividad.getFechaInicio().format(formatter) + "\n" +
                        "Fecha Fin: " + (actividad.getFechaFin() != null ? actividad.getFechaFin().format(formatter) : "No disponible") + "\n" +
                        "Cupos Disponibles: " + actividad.getCuposDisponibles() + " de " + actividad.getCupoMaximo(),
                AlertType.INFORMATION);
    }

    /**
     * Método para editar una actividad
     * @param actividad Actividad a editar
     */
    private void editarActividad(Actividad actividad) {
        // Aquí se podría abrir un formulario para editar la actividad
        mostrarAlerta("Información", "Funcionalidad no implementada: Editar Actividad", AlertType.INFORMATION);
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