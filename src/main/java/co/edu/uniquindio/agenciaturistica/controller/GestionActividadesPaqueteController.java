package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Actividad;
import co.edu.uniquindio.agenciaturistica.model.PaqueteTuristico;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionActividadesPaqueteController implements Initializable {

    @FXML
    private Button btnBuscarDisponibles;

    @FXML
    private Button btnBuscarPaquete;

    @FXML
    private Button btnVolver;

    @FXML
    private TableColumn<Actividad, String> colDisponiblesAcciones;

    @FXML
    private TableColumn<Actividad, Integer> colDisponiblesDuracion;

    @FXML
    private TableColumn<Actividad, String> colDisponiblesFecha;

    @FXML
    private TableColumn<Actividad, Integer> colDisponiblesId;

    @FXML
    private TableColumn<Actividad, String> colDisponiblesNombre;

    @FXML
    private TableColumn<Actividad, Double> colDisponiblesPrecio;

    @FXML
    private TableColumn<Actividad, String> colDisponiblesUbicacion;

    @FXML
    private TableColumn<Actividad, String> colPaqueteAcciones;

    @FXML
    private TableColumn<Actividad, Integer> colPaqueteDuracion;

    @FXML
    private TableColumn<Actividad, String> colPaqueteFecha;

    @FXML
    private TableColumn<Actividad, Integer> colPaqueteId;

    @FXML
    private TableColumn<Actividad, String> colPaqueteNombre;

    @FXML
    private TableColumn<Actividad, Double> colPaquetePrecio;

    @FXML
    private TableColumn<Actividad, String> colPaqueteUbicacion;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblTotalDisponibles;

    @FXML
    private Label lblTotalPaquete;

    @FXML
    private TableView<Actividad> tablaActividadesDisponibles;

    @FXML
    private TableView<Actividad> tablaActividadesPaquete;

    @FXML
    private TextField txtBuscarDisponibles;

    @FXML
    private TextField txtBuscarPaquete;

    private Aplicacion aplicacion;
    private PaqueteTuristico paquete;
    private ObservableList<Actividad> listaActividadesDisponibles = FXCollections.observableArrayList();
    private ObservableList<Actividad> listaActividadesPaquete = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tablas
        configurarTablaDisponibles();
        configurarTablaPaquete();
    }

    /**
     * Método para configurar la tabla de actividades disponibles
     */
    private void configurarTablaDisponibles() {
        colDisponiblesId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDisponiblesNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDisponiblesUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colDisponiblesPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());
        colDisponiblesDuracion.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDuracion()).asObject());

        // Formatear fecha
        colDisponiblesFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFechaInicio();
            return new SimpleStringProperty(fecha != null ? fecha.format(formatter) : "No disponible");
        });

        // Configurar botón de agregar
        colDisponiblesAcciones.setCellFactory(crearBotonAgregar());

        tablaActividadesDisponibles.setItems(listaActividadesDisponibles);
    }

    /**
     * Método para configurar la tabla de actividades del paquete
     */
    private void configurarTablaPaquete() {
        colPaqueteId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPaqueteNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPaqueteUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colPaquetePrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());
        colPaqueteDuracion.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDuracion()).asObject());

        // Formatear fecha
        colPaqueteFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFechaInicio();
            return new SimpleStringProperty(fecha != null ? fecha.format(formatter) : "No disponible");
        });

        // Configurar botón de quitar
        colPaqueteAcciones.setCellFactory(crearBotonQuitar());

        tablaActividadesPaquete.setItems(listaActividadesPaquete);
    }

    /**
     * Método para crear el botón de agregar actividad
     */
    private Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>> crearBotonAgregar() {
        return new Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>>() {
            @Override
            public TableCell<Actividad, String> call(final TableColumn<Actividad, String> param) {
                return new TableCell<Actividad, String>() {
                    private final Button btnAgregar = new Button("Agregar");

                    {
                        btnAgregar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

                        btnAgregar.setOnAction(event -> {
                            Actividad actividad = getTableView().getItems().get(getIndex());
                            agregarActividadAlPaquete(actividad);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnAgregar);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para crear el botón de quitar actividad
     */
    private Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>> crearBotonQuitar() {
        return new Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>>() {
            @Override
            public TableCell<Actividad, String> call(final TableColumn<Actividad, String> param) {
                return new TableCell<Actividad, String>() {
                    private final Button btnQuitar = new Button("Quitar");

                    {
                        btnQuitar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnQuitar.setOnAction(event -> {
                            Actividad actividad = getTableView().getItems().get(getIndex());
                            quitarActividadDelPaquete(actividad);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnQuitar);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para asignar la aplicación y paquete
     * @param aplicacion La aplicación principal
     * @param paquete El paquete a editar
     */
    public void setDatos(Aplicacion aplicacion, PaqueteTuristico paquete) {
        this.aplicacion = aplicacion;
        this.paquete = paquete;

        // Actualizar título
        lblTitulo.setText("Gestión de Actividades - " + paquete.getNombre());

        // Cargar datos
        cargarActividades();
    }

    /**
     * Método para cargar las actividades disponibles y las del paquete
     */
    private void cargarActividades() {
        try {
            // Obtener todas las actividades
            List<Actividad> todasLasActividades = ModelFactoryController.getInstance()
                    .getSistema().obtenerActividades();

            // Obtener actividades del paquete
            listaActividadesPaquete.clear();
            listaActividadesPaquete.addAll(paquete.getActividades());

            // Filtrar actividades disponibles (no incluidas en el paquete)
            listaActividadesDisponibles.clear();
            for (Actividad actividad : todasLasActividades) {
                boolean yaIncluida = false;
                for (Actividad actPaquete : listaActividadesPaquete) {
                    if (actividad.getId() == actPaquete.getId()) {
                        yaIncluida = true;
                        break;
                    }
                }
                if (!yaIncluida) {
                    listaActividadesDisponibles.add(actividad);
                }
            }

            // Actualizar contadores
            actualizarContadores();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar las actividades: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar los contadores de actividades
     */
    private void actualizarContadores() {
        lblTotalDisponibles.setText("Total disponibles: " + listaActividadesDisponibles.size());
        lblTotalPaquete.setText("Total en paquete: " + listaActividadesPaquete.size());
    }

    /**
     * Método para buscar actividades disponibles
     * @param event
     */
    @FXML
    void buscarActividadesDisponibles(ActionEvent event) {
        String textoBusqueda = txtBuscarDisponibles.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todas las actividades
            tablaActividadesDisponibles.setItems(listaActividadesDisponibles);
            return;
        }

        // Filtrar actividades por nombre, ubicación o descripción
        ObservableList<Actividad> actividadesFiltradas = FXCollections.observableArrayList();

        for (Actividad actividad : listaActividadesDisponibles) {
            if (actividad.getNombre().toLowerCase().contains(textoBusqueda) ||
                    actividad.getUbicacion().toLowerCase().contains(textoBusqueda) ||
                    actividad.getDescripcion().toLowerCase().contains(textoBusqueda)) {
                actividadesFiltradas.add(actividad);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaActividadesDisponibles.setItems(actividadesFiltradas);
        lblTotalDisponibles.setText("Resultados: " + actividadesFiltradas.size());
    }

    /**
     * Método para buscar actividades del paquete
     * @param event
     */
    @FXML
    void buscarActividadesPaquete(ActionEvent event) {
        String textoBusqueda = txtBuscarPaquete.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todas las actividades
            tablaActividadesPaquete.setItems(listaActividadesPaquete);
            return;
        }

        // Filtrar actividades por nombre, ubicación o descripción
        ObservableList<Actividad> actividadesFiltradas = FXCollections.observableArrayList();

        for (Actividad actividad : listaActividadesPaquete) {
            if (actividad.getNombre().toLowerCase().contains(textoBusqueda) ||
                    actividad.getUbicacion().toLowerCase().contains(textoBusqueda) ||
                    actividad.getDescripcion().toLowerCase().contains(textoBusqueda)) {
                actividadesFiltradas.add(actividad);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaActividadesPaquete.setItems(actividadesFiltradas);
        lblTotalPaquete.setText("Resultados: " + actividadesFiltradas.size());
    }

    /**
     * Método para agregar una actividad al paquete
     * @param actividad Actividad a agregar
     */
    private void agregarActividadAlPaquete(Actividad actividad) {
        try {
            // Llamar al método del sistema para agregar la actividad al paquete
            Respuesta<Boolean> respuesta = ModelFactoryController.getInstance()
                    .getSistema().agregarActividadAPaquete(paquete.getId(), actividad.getId());

            if (respuesta.isExito()) {
                // Mover la actividad de la lista disponible a la lista del paquete
                listaActividadesDisponibles.remove(actividad);
                listaActividadesPaquete.add(actividad);
                paquete.agregarActividad(actividad);

                // Actualizar contadores
                actualizarContadores();

                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Actividad agregada al paquete correctamente", AlertType.INFORMATION);
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar la actividad al paquete: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para quitar una actividad del paquete
     * @param actividad Actividad a quitar
     */
    private void quitarActividadDelPaquete(Actividad actividad) {
        try {
            // Llamar al método del sistema para quitar la actividad del paquete
            Respuesta<Boolean> respuesta = ModelFactoryController.getInstance()
                    .getSistema().eliminarActividadDePaquete(paquete.getId(), actividad.getId());

            if (respuesta.isExito()) {
                // Mover la actividad de la lista del paquete a la lista disponible
                listaActividadesPaquete.remove(actividad);
                listaActividadesDisponibles.add(actividad);

                // Eliminar la actividad del paquete
                for (int i = 0; i < paquete.getActividades().size(); i++) {
                    if (paquete.getActividades().get(i).getId() == actividad.getId()) {
                        paquete.getActividades().remove(i);
                        break;
                    }
                }

                // Actualizar contadores
                actualizarContadores();

                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Actividad quitada del paquete correctamente", AlertType.INFORMATION);
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al quitar la actividad del paquete: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para volver a la pantalla anterior
     * @param event
     */
    @FXML
    void volver(ActionEvent event) {
        // Cerrar la ventana actual
        Stage stage = (Stage) btnVolver.getScene().getWindow();
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