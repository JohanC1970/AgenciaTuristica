package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Hospedaje;
import co.edu.uniquindio.agenciaturistica.model.PaqueteTuristico;
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
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionHospedajesPaqueteController implements Initializable {

    @FXML
    private Button btnBuscarDisponibles;

    @FXML
    private Button btnBuscarPaquete;

    @FXML
    private Button btnVolver;

    @FXML
    private TableColumn<Hospedaje, String> colDisponiblesAcciones;

    @FXML
    private TableColumn<Hospedaje, String> colDisponiblesCiudad;

    @FXML
    private TableColumn<Hospedaje, String> colDisponiblesDireccion;

    @FXML
    private TableColumn<Hospedaje, Integer> colDisponiblesEstrellas;

    @FXML
    private TableColumn<Hospedaje, Integer> colDisponiblesId;

    @FXML
    private TableColumn<Hospedaje, String> colDisponiblesNombre;

    @FXML
    private TableColumn<Hospedaje, String> colPaqueteAcciones;

    @FXML
    private TableColumn<Hospedaje, String> colPaqueteCiudad;

    @FXML
    private TableColumn<Hospedaje, String> colPaqueteDireccion;

    @FXML
    private TableColumn<Hospedaje, Integer> colPaqueteEstrellas;

    @FXML
    private TableColumn<Hospedaje, Integer> colPaqueteId;

    @FXML
    private TableColumn<Hospedaje, String> colPaqueteNombre;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblTotalDisponibles;

    @FXML
    private Label lblTotalPaquete;

    @FXML
    private TableView<Hospedaje> tablaHospedajesDisponibles;

    @FXML
    private TableView<Hospedaje> tablaHospedajesPaquete;

    @FXML
    private TextField txtBuscarDisponibles;

    @FXML
    private TextField txtBuscarPaquete;

    private Aplicacion aplicacion;
    private PaqueteTuristico paquete;
    private ObservableList<Hospedaje> listaHospedajesDisponibles = FXCollections.observableArrayList();
    private ObservableList<Hospedaje> listaHospedajesPaquete = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tablas
        configurarTablaDisponibles();
        configurarTablaPaquete();
    }

    /**
     * Método para configurar la tabla de hospedajes disponibles
     */
    private void configurarTablaDisponibles() {
        colDisponiblesId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDisponiblesNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDisponiblesCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colDisponiblesDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colDisponiblesEstrellas.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getEstrellas()).asObject());

        // Configurar botón de agregar
        colDisponiblesAcciones.setCellFactory(crearBotonAgregar());

        tablaHospedajesDisponibles.setItems(listaHospedajesDisponibles);
    }

    /**
     * Método para configurar la tabla de hospedajes del paquete
     */
    private void configurarTablaPaquete() {
        colPaqueteId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPaqueteNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPaqueteCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colPaqueteDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colPaqueteEstrellas.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getEstrellas()).asObject());

        // Configurar botón de quitar
        colPaqueteAcciones.setCellFactory(crearBotonQuitar());

        tablaHospedajesPaquete.setItems(listaHospedajesPaquete);
    }

    /**
     * Método para crear el botón de agregar hospedaje
     */
    private Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>> crearBotonAgregar() {
        return new Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>>() {
            @Override
            public TableCell<Hospedaje, String> call(final TableColumn<Hospedaje, String> param) {
                return new TableCell<Hospedaje, String>() {
                    private final Button btnAgregar = new Button("Agregar");

                    {
                        btnAgregar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

                        btnAgregar.setOnAction(event -> {
                            Hospedaje hospedaje = getTableView().getItems().get(getIndex());
                            agregarHospedajeAlPaquete(hospedaje);
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
     * Método para crear el botón de quitar hospedaje
     */
    private Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>> crearBotonQuitar() {
        return new Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>>() {
            @Override
            public TableCell<Hospedaje, String> call(final TableColumn<Hospedaje, String> param) {
                return new TableCell<Hospedaje, String>() {
                    private final Button btnQuitar = new Button("Quitar");

                    {
                        btnQuitar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnQuitar.setOnAction(event -> {
                            Hospedaje hospedaje = getTableView().getItems().get(getIndex());
                            quitarHospedajeDelPaquete(hospedaje);
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
        lblTitulo.setText("Gestión de Hospedajes - " + paquete.getNombre());

        // Cargar datos
        cargarHospedajes();
    }

    /**
     * Método para cargar los hospedajes disponibles y los del paquete
     */
    private void cargarHospedajes() {
        try {
            // Obtener todos los hospedajes
            List<Hospedaje> todosLosHospedajes = ModelFactoryController.getInstance()
                    .getSistema().obtenerHospedajes();

            // Obtener hospedajes del paquete
            listaHospedajesPaquete.clear();
            listaHospedajesPaquete.addAll(paquete.getHospedajes());

            // Filtrar hospedajes disponibles (no incluidos en el paquete)
            listaHospedajesDisponibles.clear();
            for (Hospedaje hospedaje : todosLosHospedajes) {
                boolean yaIncluido = false;
                for (Hospedaje hospPaquete : listaHospedajesPaquete) {
                    if (hospedaje.getId() == hospPaquete.getId()) {
                        yaIncluido = true;
                        break;
                    }
                }
                if (!yaIncluido) {
                    listaHospedajesDisponibles.add(hospedaje);
                }
            }

            // Actualizar contadores
            actualizarContadores();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar los hospedajes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar los contadores de hospedajes
     */
    private void actualizarContadores() {
        lblTotalDisponibles.setText("Total disponibles: " + listaHospedajesDisponibles.size());
        lblTotalPaquete.setText("Total en paquete: " + listaHospedajesPaquete.size());
    }

    /**
     * Método para buscar hospedajes disponibles
     * @param event
     */
    @FXML
    void buscarHospedajesDisponibles(ActionEvent event) {
        String textoBusqueda = txtBuscarDisponibles.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos los hospedajes
            tablaHospedajesDisponibles.setItems(listaHospedajesDisponibles);
            return;
        }

        // Filtrar hospedajes por nombre, ciudad o dirección
        ObservableList<Hospedaje> hospedajesFiltrados = FXCollections.observableArrayList();

        for (Hospedaje hospedaje : listaHospedajesDisponibles) {
            if (hospedaje.getNombre().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getCiudad().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getDireccion().toLowerCase().contains(textoBusqueda)) {
                hospedajesFiltrados.add(hospedaje);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaHospedajesDisponibles.setItems(hospedajesFiltrados);
        lblTotalDisponibles.setText("Resultados: " + hospedajesFiltrados.size());
    }

    /**
     * Método para buscar hospedajes del paquete
     * @param event
     */
    @FXML
    void buscarHospedajesPaquete(ActionEvent event) {
        String textoBusqueda = txtBuscarPaquete.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos los hospedajes
            tablaHospedajesPaquete.setItems(listaHospedajesPaquete);
            return;
        }

        // Filtrar hospedajes por nombre, ciudad o dirección
        ObservableList<Hospedaje> hospedajesFiltrados = FXCollections.observableArrayList();

        for (Hospedaje hospedaje : listaHospedajesPaquete) {
            if (hospedaje.getNombre().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getCiudad().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getDireccion().toLowerCase().contains(textoBusqueda)) {
                hospedajesFiltrados.add(hospedaje);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaHospedajesPaquete.setItems(hospedajesFiltrados);
        lblTotalPaquete.setText("Resultados: " + hospedajesFiltrados.size());
    }

    /**
     * Método para agregar un hospedaje al paquete
     * @param hospedaje Hospedaje a agregar
     */
    private void agregarHospedajeAlPaquete(Hospedaje hospedaje) {
        try {
            // Llamar al método del sistema para agregar el hospedaje al paquete
            Respuesta<Boolean> respuesta = ModelFactoryController.getInstance()
                    .getSistema().agregarHospedajeAPaquete(paquete.getId(), hospedaje.getId());

            if (respuesta.isExito()) {
                // Mover el hospedaje de la lista disponible a la lista del paquete
                listaHospedajesDisponibles.remove(hospedaje);
                listaHospedajesPaquete.add(hospedaje);
                paquete.agregarHospedaje(hospedaje);

                // Actualizar contadores
                actualizarContadores();

                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Hospedaje agregado al paquete correctamente", AlertType.INFORMATION);
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al agregar el hospedaje al paquete: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para quitar un hospedaje del paquete
     * @param hospedaje Hospedaje a quitar
     */
    private void quitarHospedajeDelPaquete(Hospedaje hospedaje) {
        try {
            // Llamar al método del sistema para quitar el hospedaje del paquete
            Respuesta<Boolean> respuesta = ModelFactoryController.getInstance()
                    .getSistema().eliminarHospedajeDePaquete(paquete.getId(), hospedaje.getId());

            if (respuesta.isExito()) {
                // Mover el hospedaje de la lista del paquete a la lista disponible
                listaHospedajesPaquete.remove(hospedaje);
                listaHospedajesDisponibles.add(hospedaje);

                // Eliminar el hospedaje del paquete
                for (int i = 0; i < paquete.getHospedajes().size(); i++) {
                    if (paquete.getHospedajes().get(i).getId() == hospedaje.getId()) {
                        paquete.getHospedajes().remove(i);
                        break;
                    }
                }

                // Actualizar contadores
                actualizarContadores();

                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Hospedaje quitado del paquete correctamente", AlertType.INFORMATION);
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al quitar el hospedaje del paquete: " + e.getMessage(), AlertType.ERROR);
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