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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionHabitacionesController implements Initializable {

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    @FXML
    private TableColumn<Habitacion, String> colAcciones;

    @FXML
    private TableColumn<Habitacion, Integer> colCapacidad;

    @FXML
    private TableColumn<Habitacion, Boolean> colDisponible;

    @FXML
    private TableColumn<Habitacion, Integer> colId;

    @FXML
    private TableColumn<Habitacion, Double> colPrecio;

    @FXML
    private TableColumn<Habitacion, String> colTipo;

    @FXML
    private CheckBox chkDisponible;

    @FXML
    private ComboBox<TipoHabitacion> comboTipoHabitacion;

    @FXML
    private Label lblError;

    @FXML
    private Label lblHospedaje;

    @FXML
    private Label lblTotalHabitaciones;

    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<Habitacion> tablaHabitaciones;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtCapacidad;

    @FXML
    private TextField txtPrecio;

    private Aplicacion aplicacion;
    private Hospedaje hospedaje;
    private ObservableList<Habitacion> listaHabitaciones = FXCollections.observableArrayList();
    private ObservableList<TipoHabitacion> listaTiposHabitacion = FXCollections.observableArrayList();
    private Habitacion habitacionSeleccionada;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tabla
        configurarTabla();

        // Ocultar panel de formulario
        panelFormulario.setVisible(false);

        // Configurar ComboBox de tipos de habitación
        comboTipoHabitacion.setItems(listaTiposHabitacion);

        // Configurar disponible como checked por defecto
        chkDisponible.setSelected(true);
    }

    /**
     * Método para configurar la tabla de habitaciones
     */
    private void configurarTabla() {
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipoHabitacion() != null ?
                        cellData.getValue().getTipoHabitacion().getNombre() : "No asignado"));
        colCapacidad.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCapacidad()).asObject());
        colPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecioPorNoche()).asObject());
        colDisponible.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().isDisponible()));

        // Formatear columna de disponibilidad
        colDisponible.setCellFactory(col -> new TableCell<Habitacion, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Disponible" : "No disponible");
                }
            }
        });

        // Botones de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());

        tablaHabitaciones.setItems(listaHabitaciones);
    }

    /**
     * Método para crear los botones de acciones (editar y eliminar)
     * @return
     */
    private Callback<TableColumn<Habitacion, String>, TableCell<Habitacion, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Habitacion, String>, TableCell<Habitacion, String>>() {
            @Override
            public TableCell<Habitacion, String> call(final TableColumn<Habitacion, String> param) {
                return new TableCell<Habitacion, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox pane = new HBox(5, btnEditar, btnEliminar);

                    {
                        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnEditar.setOnAction(event -> {
                            Habitacion habitacion = getTableView().getItems().get(getIndex());
                            editarHabitacion(habitacion);
                        });

                        btnEliminar.setOnAction(event -> {
                            Habitacion habitacion = getTableView().getItems().get(getIndex());
                            eliminarHabitacion(habitacion);
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
    }

    /**
     * Método para establecer el hospedaje asociado a estas habitaciones
     * @param hospedaje
     */
    public void setHospedaje(Hospedaje hospedaje) {
        this.hospedaje = hospedaje;

        // Actualizar título con el nombre del hospedaje
        if (hospedaje != null) {
            lblHospedaje.setText("Habitaciones de: " + hospedaje.getNombre());
        }
    }

    /**
     * Método para inicializar datos después de establecer el hospedaje
     */
    public void inicializarDatos() {
        // Cargar tipos de habitación
        cargarTiposHabitacion();

        // Cargar habitaciones del hospedaje
        cargarHabitaciones();
    }

    /**
     * Método para cargar los tipos de habitación disponibles
     */
    private void cargarTiposHabitacion() {
        try {
            // Obtener lista de tipos de habitación desde el controlador
            List<TipoHabitacion> tiposHabitacion = ModelFactoryController.getInstance()
                    .getSistema().obtenerTiposHabitacion();

            // Actualizar lista observable
            listaTiposHabitacion.clear();
            listaTiposHabitacion.addAll(tiposHabitacion);

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar los tipos de habitación: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cargar las habitaciones del hospedaje seleccionado
     */
    private void cargarHabitaciones() {
        if (hospedaje == null) {
            return;
        }

        try {
            // Obtener lista de habitaciones del hospedaje desde el controlador
            List<Habitacion> habitaciones = ModelFactoryController.getInstance()
                    .getSistema().obtenerHabitacionesPorHospedaje(hospedaje.getId());

            // Actualizar lista observable
            listaHabitaciones.clear();
            listaHabitaciones.addAll(habitaciones);

            // Actualizar contador
            actualizarContador();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar las habitaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el contador de habitaciones
     */
    private void actualizarContador() {
        lblTotalHabitaciones.setText("Total de habitaciones: " + listaHabitaciones.size());
    }

    /**
     * Método para buscar una habitación
     * @param event
     */
    @FXML
    void buscarHabitacion(ActionEvent event) {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todas las habitaciones
            tablaHabitaciones.setItems(listaHabitaciones);
            return;
        }

        // Filtrar habitaciones por tipo o capacidad
        ObservableList<Habitacion> habitacionesFiltradas = FXCollections.observableArrayList();

        for (Habitacion habitacion : listaHabitaciones) {
            boolean coincideTipo = habitacion.getTipoHabitacion() != null &&
                    habitacion.getTipoHabitacion().getNombre().toLowerCase().contains(textoBusqueda);
            boolean coincideCapacidad = String.valueOf(habitacion.getCapacidad()).contains(textoBusqueda);
            boolean coincidePrecio = String.valueOf(habitacion.getPrecioPorNoche()).contains(textoBusqueda);

            if (coincideTipo || coincideCapacidad || coincidePrecio) {
                habitacionesFiltradas.add(habitacion);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaHabitaciones.setItems(habitacionesFiltradas);
        lblTotalHabitaciones.setText("Resultados: " + habitacionesFiltradas.size());
    }

    /**
     * Método para guardar una habitación (crear o actualizar)
     * @param event
     */
    @FXML
    void guardarHabitacion(ActionEvent event) {
        // Validar los datos del formulario
        if (!validarDatosFormulario()) {
            return;
        }

        try {
            // Crear objeto Habitacion con los datos del formulario
            Habitacion habitacion = new Habitacion();
            if (modoEdicion) {
                habitacion.setId(habitacionSeleccionada.getId());
            }

            habitacion.setTipoHabitacion(comboTipoHabitacion.getValue());
            habitacion.setCapacidad(Integer.parseInt(txtCapacidad.getText().trim()));
            habitacion.setPrecioPorNoche(Double.parseDouble(txtPrecio.getText().trim()));
            habitacion.setDisponible(chkDisponible.isSelected());

            Respuesta<Habitacion> respuesta;

            if (modoEdicion) {
                // Actualizar habitación existente
                respuesta = ModelFactoryController.getInstance().getSistema().actualizarHabitacion(hospedaje.getId(), habitacion);
            } else {
                // Crear nueva habitación
                respuesta = ModelFactoryController.getInstance().getSistema().crearHabitacion(hospedaje.getId(), habitacion);
            }

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                String mensaje = modoEdicion ? "Habitación actualizada correctamente" : "Habitación creada correctamente";
                mostrarAlerta("Éxito", mensaje, AlertType.INFORMATION);

                // Recargar lista de habitaciones
                cargarHabitaciones();

                // Ocultar formulario
                panelFormulario.setVisible(false);
                limpiarFormulario();
            } else {
                // Mostrar mensaje de error
                lblError.setText(respuesta.getMensaje());
            }
        } catch (NumberFormatException e) {
            lblError.setText("Por favor, ingrese valores numéricos válidos para los campos de capacidad y precio");
        } catch (SQLException e) {
            lblError.setText("Error al guardar la habitación: " + e.getMessage());
        }
    }

    /**
     * Método para validar los datos del formulario
     * @return true si los datos son válidos, false en caso contrario
     */
    private boolean validarDatosFormulario() {
        lblError.setText("");

        // Validar tipo de habitación
        if (comboTipoHabitacion.getValue() == null) {
            lblError.setText("Debe seleccionar un tipo de habitación");
            comboTipoHabitacion.requestFocus();
            return false;
        }

        // Validar capacidad
        try {
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());
            if (capacidad <= 0) {
                lblError.setText("La capacidad debe ser mayor que cero");
                txtCapacidad.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("La capacidad debe ser un número entero válido");
            txtCapacidad.requestFocus();
            return false;
        }

        // Validar precio
        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                lblError.setText("El precio por noche debe ser mayor que cero");
                txtPrecio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("El precio por noche debe ser un número válido");
            txtPrecio.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Método para mostrar el formulario de creación de habitaciones
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

        // Si hay tipos de habitación disponibles, seleccionar el primero por defecto
        if (!listaTiposHabitacion.isEmpty()) {
            comboTipoHabitacion.setValue(listaTiposHabitacion.get(0));
        }

        // Enfocar en la capacidad
        txtCapacidad.requestFocus();
    }

    /**
     * Método para editar una habitación existente
     * @param habitacion
     */
    private void editarHabitacion(Habitacion habitacion) {
        // Guardar referencia a la habitación seleccionada
        habitacionSeleccionada = habitacion;

        // Cargar datos en el formulario
        comboTipoHabitacion.setValue(habitacion.getTipoHabitacion());
        txtCapacidad.setText(String.valueOf(habitacion.getCapacidad()));
        txtPrecio.setText(String.valueOf(habitacion.getPrecioPorNoche()));
        chkDisponible.setSelected(habitacion.isDisponible());

        // Establecer modo edición
        modoEdicion = true;

        // Mostrar panel de formulario
        panelFormulario.setVisible(true);
    }

    /**
     * Método para eliminar una habitación
     * @param habitacion
     */
    private void eliminarHabitacion(Habitacion habitacion) {
        // Confirmar eliminación
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta habitación?");
        alert.setContentText("Esta acción no se puede deshacer.");

        if (alert.showAndWait().get().getButtonData().isCancelButton()) {
            return; // El usuario canceló la operación
        }

        try {
            // Eliminar habitación
            Respuesta<Habitacion> respuesta = ModelFactoryController.getInstance()
                    .getSistema().eliminarHabitacion(hospedaje.getId(), habitacion.getId());

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Habitación eliminada correctamente", AlertType.INFORMATION);

                // Recargar lista de habitaciones
                cargarHabitaciones();

                // Ocultar formulario si estaba visible
                if (panelFormulario.isVisible() && habitacionSeleccionada != null &&
                        habitacionSeleccionada.getId() == habitacion.getId()) {
                    panelFormulario.setVisible(false);
                }
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar la habitación: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cancelar la edición o creación de una habitación
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
        comboTipoHabitacion.setValue(null);
        txtCapacidad.clear();
        txtPrecio.clear();
        chkDisponible.setSelected(true);
        lblError.setText("");

        habitacionSeleccionada = null;
        modoEdicion = false;
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
