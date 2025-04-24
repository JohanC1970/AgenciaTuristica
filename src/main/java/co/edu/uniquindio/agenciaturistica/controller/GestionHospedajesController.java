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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionHospedajesController implements Initializable {

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGestionarHabitaciones;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    @FXML
    private TableColumn<Hospedaje, String> colAcciones;

    @FXML
    private TableColumn<Hospedaje, String> colCiudad;

    @FXML
    private TableColumn<Hospedaje, String> colDireccion;

    @FXML
    private TableColumn<Hospedaje, Integer> colEstrellas;

    @FXML
    private TableColumn<Hospedaje, Integer> colHabitaciones;

    @FXML
    private TableColumn<Hospedaje, Integer> colId;

    @FXML
    private TableColumn<Hospedaje, String> colNombre;

    @FXML
    private TableColumn<Hospedaje, String> colTelefono;

    @FXML
    private ComboBox<Integer> comboEstrellas;

    @FXML
    private Label lblError;

    @FXML
    private Label lblTotalHospedajes;

    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<Hospedaje> tablaHospedajes;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtCiudad;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    private Aplicacion aplicacion;
    private ObservableList<Hospedaje> listaHospedajes = FXCollections.observableArrayList();
    private Hospedaje hospedajeSeleccionado;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tabla
        configurarTabla();

        // Ocultar panel de formulario
        panelFormulario.setVisible(false);

        // Configurar combo de estrellas (de 1 a 5)
        comboEstrellas.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));

        // Desactivar botón de gestionar habitaciones hasta seleccionar un hospedaje
        btnGestionarHabitaciones.setDisable(true);
    }

    /**
     * Método para configurar la tabla de hospedajes
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstrellas.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getEstrellas()).asObject());

        // Contar habitaciones asociadas
        colHabitaciones.setCellValueFactory(cellData -> {
            int cantidadHabitaciones = cellData.getValue().getHabitaciones().size();
            return new SimpleIntegerProperty(cantidadHabitaciones).asObject();
        });

        // Botones de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());

        tablaHospedajes.setItems(listaHospedajes);

        // Listener para selección de hospedaje
        tablaHospedajes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                hospedajeSeleccionado = newSelection;
                btnGestionarHabitaciones.setDisable(false);
            } else {
                hospedajeSeleccionado = null;
                btnGestionarHabitaciones.setDisable(true);
            }
        });
    }

    /**
     * Método para crear los botones de acciones (editar y eliminar)
     * @return
     */
    private Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Hospedaje, String>, TableCell<Hospedaje, String>>() {
            @Override
            public TableCell<Hospedaje, String> call(final TableColumn<Hospedaje, String> param) {
                return new TableCell<Hospedaje, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox pane = new HBox(5, btnEditar, btnEliminar);

                    {
                        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnEditar.setOnAction(event -> {
                            Hospedaje hospedaje = getTableView().getItems().get(getIndex());
                            editarHospedaje(hospedaje);
                        });

                        btnEliminar.setOnAction(event -> {
                            Hospedaje hospedaje = getTableView().getItems().get(getIndex());
                            eliminarHospedaje(hospedaje);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;

        // Cargar hospedajes
        cargarHospedajes();
    }

    /**
     * Método para cargar los hospedajes desde la base de datos
     */
    private void cargarHospedajes() {
        try {
            // Obtener la lista de hospedajes desde el controlador
            List<Hospedaje> hospedajes = ModelFactoryController.getInstance()
                    .getSistema().obtenerHospedajes();

            // Actualizar lista observable
            listaHospedajes.clear();
            listaHospedajes.addAll(hospedajes);

            // Actualizar contador
            actualizarContador();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar los hospedajes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el contador de hospedajes
     */
    private void actualizarContador() {
        lblTotalHospedajes.setText("Total de hospedajes: " + listaHospedajes.size());
    }

    /**
     * Método para buscar un hospedaje
     * @param event
     */
    @FXML
    void buscarHospedaje(ActionEvent event) {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos los hospedajes
            tablaHospedajes.setItems(listaHospedajes);
            return;
        }

        // Filtrar hospedajes por nombre, ciudad o dirección
        ObservableList<Hospedaje> hospedajesFiltrados = FXCollections.observableArrayList();

        for (Hospedaje hospedaje : listaHospedajes) {
            if (hospedaje.getNombre().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getCiudad().toLowerCase().contains(textoBusqueda) ||
                    hospedaje.getDireccion().toLowerCase().contains(textoBusqueda)) {
                hospedajesFiltrados.add(hospedaje);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaHospedajes.setItems(hospedajesFiltrados);
        lblTotalHospedajes.setText("Resultados: " + hospedajesFiltrados.size());
    }

    /**
     * Método para gestionar las habitaciones de un hospedaje
     * @param event
     */
    @FXML
    void gestionarHabitaciones(ActionEvent event) {
        if (hospedajeSeleccionado == null) {
            mostrarAlerta("Aviso", "Por favor, seleccione un hospedaje", AlertType.WARNING);
            return;
        }

        try {
            // Cargar la pantalla de gestión de habitaciones
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionHabitaciones.fxml")
            );
            Parent root = loader.load();

            // Obtener el controlador y establecer el hospedaje seleccionado
            GestionHabitacionesController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setHospedaje(hospedajeSeleccionado);
            controller.inicializarDatos();

            // Mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Gestión de Habitaciones - " + hospedajeSeleccionado.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar hospedajes al cerrar la ventana
            cargarHospedajes();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir la ventana de gestión de habitaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para guardar un hospedaje (crear o actualizar)
     * @param event
     */
    @FXML
    void guardarHospedaje(ActionEvent event) {
        // Validar los datos del formulario
        if (!validarDatosFormulario()) {
            return;
        }

        try {
            // Crear objeto Hospedaje con los datos del formulario
            Hospedaje hospedaje = new Hospedaje();
            if (modoEdicion) {
                hospedaje.setId(hospedajeSeleccionado.getId());
                // Mantener las habitaciones existentes
                hospedaje.setHabitaciones(hospedajeSeleccionado.getHabitaciones());
            }

            hospedaje.setNombre(txtNombre.getText().trim());
            hospedaje.setCiudad(txtCiudad.getText().trim());
            hospedaje.setDireccion(txtDireccion.getText().trim());
            hospedaje.setTelefono(txtTelefono.getText().trim());
            hospedaje.setEstrellas(comboEstrellas.getValue());
            hospedaje.setDescripcion(txtDescripcion.getText().trim());

            Respuesta<Hospedaje> respuesta;

            if (modoEdicion) {
                // Actualizar hospedaje existente
                respuesta = ModelFactoryController.getInstance().getSistema().actualizarHospedaje(hospedaje);
            } else {
                // Crear nuevo hospedaje
                respuesta = ModelFactoryController.getInstance().getSistema().crearHospedaje(hospedaje);
            }

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                String mensaje = modoEdicion ? "Hospedaje actualizado correctamente" : "Hospedaje creado correctamente";
                mostrarAlerta("Éxito", mensaje, AlertType.INFORMATION);

                // Recargar lista de hospedajes
                cargarHospedajes();

                // Ocultar formulario
                panelFormulario.setVisible(false);
                limpiarFormulario();
            } else {
                // Mostrar mensaje de error
                lblError.setText(respuesta.getMensaje());
            }
        } catch (SQLException e) {
            lblError.setText("Error al guardar el hospedaje: " + e.getMessage());
        }
    }

    /**
     * Método para validar los datos del formulario
     * @return true si los datos son válidos, false en caso contrario
     */
    private boolean validarDatosFormulario() {
        lblError.setText("");

        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            lblError.setText("El nombre es obligatorio");
            txtNombre.requestFocus();
            return false;
        }

        // Validar ciudad
        if (txtCiudad.getText().trim().isEmpty()) {
            lblError.setText("La ciudad es obligatoria");
            txtCiudad.requestFocus();
            return false;
        }

        // Validar dirección
        if (txtDireccion.getText().trim().isEmpty()) {
            lblError.setText("La dirección es obligatoria");
            txtDireccion.requestFocus();
            return false;
        }

        // Validar teléfono
        if (txtTelefono.getText().trim().isEmpty()) {
            lblError.setText("El teléfono es obligatorio");
            txtTelefono.requestFocus();
            return false;
        }

        // Validar estrellas
        if (comboEstrellas.getValue() == null) {
            lblError.setText("Debe seleccionar la clasificación por estrellas");
            comboEstrellas.requestFocus();
            return false;
        }

        // Validar descripción
        if (txtDescripcion.getText().trim().isEmpty()) {
            lblError.setText("La descripción es obligatoria");
            txtDescripcion.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Método para mostrar el formulario de creación de hospedajes
     * @param event
     */
    @FXML
    void mostrarFormularioAgregar(ActionEvent event) {
        // Limpiar formulario
        limpiarFormulario();

        // Establecer modo creación
        modoEdicion = false;

        // Mostrar panel de formulario
        panelFormulario.setVisible(true);

        // Desactivar botón de gestionar habitaciones
        btnGestionarHabitaciones.setDisable(true);

        // Enfocar en el nombre
        txtNombre.requestFocus();
    }

    /**
     * Método para editar un hospedaje existente
     * @param hospedaje
     */
    private void editarHospedaje(Hospedaje hospedaje) {
        // Guardar referencia al hospedaje seleccionado
        hospedajeSeleccionado = hospedaje;

        // Cargar datos en el formulario
        txtNombre.setText(hospedaje.getNombre());
        txtCiudad.setText(hospedaje.getCiudad());
        txtDireccion.setText(hospedaje.getDireccion());
        txtTelefono.setText(hospedaje.getTelefono());
        comboEstrellas.setValue(hospedaje.getEstrellas());
        txtDescripcion.setText(hospedaje.getDescripcion());

        // Establecer modo edición
        modoEdicion = true;

        // Habilitar botón de gestionar habitaciones
        btnGestionarHabitaciones.setDisable(false);

        // Mostrar panel de formulario
        panelFormulario.setVisible(true);
    }

    /**
     * Método para eliminar un hospedaje
     * @param hospedaje
     */
    private void eliminarHospedaje(Hospedaje hospedaje) {
        // Confirmar eliminación
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este hospedaje?");
        alert.setContentText("Esta acción no se puede deshacer y eliminará también todas las habitaciones asociadas.");

        if (alert.showAndWait().get().getButtonData().isCancelButton()) {
            return; // El usuario canceló la operación
        }

        try {
            // Eliminar hospedaje
            Respuesta<Hospedaje> respuesta = ModelFactoryController.getInstance()
                    .getSistema().eliminarHospedaje(hospedaje.getId());

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Hospedaje eliminado correctamente", AlertType.INFORMATION);

                // Recargar lista de hospedajes
                cargarHospedajes();

                // Ocultar formulario si estaba visible
                if (panelFormulario.isVisible() && hospedajeSeleccionado != null &&
                        hospedajeSeleccionado.getId() == hospedaje.getId()) {
                    panelFormulario.setVisible(false);
                }
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar el hospedaje: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cancelar la edición o creación de un hospedaje
     * @param event
     */
    @FXML
    void cancelarFormulario(ActionEvent event) {
        // Limpiar formulario
        limpiarFormulario();

        // Ocultar panel de formulario
        panelFormulario.setVisible(false);
    }

    /**
     * Método para limpiar el formulario
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtCiudad.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        comboEstrellas.setValue(null);
        txtDescripcion.clear();
        lblError.setText("");

        hospedajeSeleccionado = null;
        modoEdicion = false;
    }

    /**
     * Método para volver a la pantalla anterior
     * @param event
     */
    @FXML
    void volver(ActionEvent event) {
        try {
            // Cargar la pantalla anterior (en este caso el panel de administrador)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Administrador.fxml")
            );
            Parent root = loader.load();

            AdministradorController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarInformacion();

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al volver a la pantalla anterior: " + e.getMessage(), AlertType.ERROR);
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
