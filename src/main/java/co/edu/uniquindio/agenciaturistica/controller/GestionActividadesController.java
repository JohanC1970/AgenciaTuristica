package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Actividad;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionActividadesController implements Initializable {

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
    private TableColumn<Actividad, String> colAcciones;

    @FXML
    private TableColumn<Actividad, Integer> colCupos;

    @FXML
    private TableColumn<Actividad, Integer> colDuracion;

    @FXML
    private TableColumn<Actividad, String> colFechaInicio;

    @FXML
    private TableColumn<Actividad, Integer> colId;

    @FXML
    private TableColumn<Actividad, String> colNombre;

    @FXML
    private TableColumn<Actividad, Double> colPrecio;

    @FXML
    private TableColumn<Actividad, String> colUbicacion;

    @FXML
    private ComboBox<String> comboHora;

    @FXML
    private DatePicker dateFecha;

    @FXML
    private Label lblError;

    @FXML
    private Label lblTotalActividades;

    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<Actividad> tablaActividades;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtCupoMaximo;

    @FXML
    private TextField txtCuposDisponibles;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private TextField txtDuracion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtUbicacion;

    private Aplicacion aplicacion;
    private ObservableList<Actividad> listaActividades = FXCollections.observableArrayList();
    private Actividad actividadSeleccionada;
    private boolean modoEdicion = false;
    private DateTimeFormatter formatterFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tabla
        configurarTabla();

        // Ocultar panel de formulario
        panelFormulario.setVisible(false);

        // Cargar horas en combo
        cargarHorasEnCombo();

        // Inicializar fecha
        dateFecha.setValue(LocalDate.now());
    }

    /**
     * Método para cargar las horas en el combo (de 00:00 a 23:00)
     */
    private void cargarHorasEnCombo() {
        List<String> horas = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            horas.add(String.format("%02d:00", i));
        }
        comboHora.setItems(FXCollections.observableArrayList(horas));
        comboHora.setValue("08:00"); // Valor por defecto
    }

    /**
     * Método para configurar la tabla de actividades
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());
        colDuracion.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDuracion()).asObject());
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));

        // Formatear fecha y hora
        colFechaInicio.setCellValueFactory(cellData -> {
            Actividad actividad = cellData.getValue();
            String fechaStr = actividad.getFechaInicio() != null ?
                    actividad.getFechaInicio().format(formatterFechaHora) : "No disponible";
            return new SimpleStringProperty(fechaStr);
        });

        colCupos.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCuposDisponibles()).asObject());

        // Botones de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());

        tablaActividades.setItems(listaActividades);
    }

    /**
     * Método para crear los botones de acciones (editar y eliminar)
     * @return
     */
    private Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Actividad, String>, TableCell<Actividad, String>>() {
            @Override
            public TableCell<Actividad, String> call(final TableColumn<Actividad, String> param) {
                return new TableCell<Actividad, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox pane = new HBox(5, btnEditar, btnEliminar);

                    {
                        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnEditar.setOnAction(event -> {
                            Actividad actividad = getTableView().getItems().get(getIndex());
                            editarActividad(actividad);
                        });

                        btnEliminar.setOnAction(event -> {
                            Actividad actividad = getTableView().getItems().get(getIndex());
                            eliminarActividad(actividad);
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

        // Cargar actividades
        cargarActividades();
    }

    /**
     * Método para cargar las actividades desde la base de datos
     */
    private void cargarActividades() {
        try {
            // Obtener la lista de actividades desde el controlador
            List<Actividad> actividades = ModelFactoryController.getInstance()
                    .getSistema().obtenerActividades();

            // Actualizar lista observable
            listaActividades.clear();
            listaActividades.addAll(actividades);

            // Actualizar contador
            actualizarContador();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar las actividades: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el contador de actividades
     */
    private void actualizarContador() {
        lblTotalActividades.setText("Total de actividades: " + listaActividades.size());
    }

    /**
     * Método para buscar una actividad
     * @param event
     */
    @FXML
    void buscarActividad(ActionEvent event) {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todas las actividades
            tablaActividades.setItems(listaActividades);
            return;
        }

        // Filtrar actividades por nombre, descripción o ubicación
        ObservableList<Actividad> actividadesFiltradas = FXCollections.observableArrayList();

        for (Actividad actividad : listaActividades) {
            if (actividad.getNombre().toLowerCase().contains(textoBusqueda) ||
                    actividad.getDescripcion().toLowerCase().contains(textoBusqueda) ||
                    actividad.getUbicacion().toLowerCase().contains(textoBusqueda)) {
                actividadesFiltradas.add(actividad);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaActividades.setItems(actividadesFiltradas);
        lblTotalActividades.setText("Resultados: " + actividadesFiltradas.size());
    }

    /**
     * Método para guardar una actividad (crear o actualizar)
     * @param event
     */
    @FXML
    void guardarActividad(ActionEvent event) {
        // Validar los datos del formulario
        if (!validarDatosFormulario()) {
            return;
        }

        try {
            // Crear objeto Actividad con los datos del formulario
            Actividad actividad = new Actividad();
            if (modoEdicion) {
                actividad.setId(actividadSeleccionada.getId());
            }

            actividad.setNombre(txtNombre.getText().trim());
            actividad.setDescripcion(txtDescripcion.getText().trim());
            actividad.setUbicacion(txtUbicacion.getText().trim());
            actividad.setPrecio(Double.parseDouble(txtPrecio.getText().trim()));
            actividad.setDuracion(Integer.parseInt(txtDuracion.getText().trim()));

            // Crear LocalDateTime a partir de la fecha y hora seleccionadas
            LocalDate fecha = dateFecha.getValue();
            LocalTime hora = LocalTime.parse(comboHora.getValue() + ":00");
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            actividad.setFechaInicio(fechaHora);

            actividad.setCupoMaximo(Integer.parseInt(txtCupoMaximo.getText().trim()));
            actividad.setCuposDisponibles(Integer.parseInt(txtCuposDisponibles.getText().trim()));

            Respuesta<Actividad> respuesta;

            if (modoEdicion) {
                // Actualizar actividad existente
                respuesta = ModelFactoryController.getInstance().getSistema().actualizarActividad(actividad);
            } else {
                // Crear nueva actividad
                respuesta = ModelFactoryController.getInstance().getSistema().crearActividad(actividad);
            }

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                String mensaje = modoEdicion ? "Actividad actualizada correctamente" : "Actividad creada correctamente";
                mostrarAlerta("Éxito", mensaje, AlertType.INFORMATION);

                // Recargar lista de actividades
                cargarActividades();

                // Ocultar formulario
                panelFormulario.setVisible(false);
                limpiarFormulario();
            } else {
                // Mostrar mensaje de error
                lblError.setText(respuesta.getMensaje());
            }
        } catch (NumberFormatException e) {
            lblError.setText("Por favor, ingrese valores numéricos válidos para los campos de precio, duración y cupos");
        } catch (SQLException e) {
            lblError.setText("Error al guardar la actividad: " + e.getMessage());
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

        // Validar descripción
        if (txtDescripcion.getText().trim().isEmpty()) {
            lblError.setText("La descripción es obligatoria");
            txtDescripcion.requestFocus();
            return false;
        }

        // Validar ubicación
        if (txtUbicacion.getText().trim().isEmpty()) {
            lblError.setText("La ubicación es obligatoria");
            txtUbicacion.requestFocus();
            return false;
        }

        // Validar precio
        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                lblError.setText("El precio debe ser mayor que cero");
                txtPrecio.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("El precio debe ser un número válido");
            txtPrecio.requestFocus();
            return false;
        }

        // Validar duración
        try {
            int duracion = Integer.parseInt(txtDuracion.getText().trim());
            if (duracion <= 0) {
                lblError.setText("La duración debe ser mayor que cero");
                txtDuracion.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("La duración debe ser un número entero válido");
            txtDuracion.requestFocus();
            return false;
        }

        // Validar fecha
        if (dateFecha.getValue() == null) {
            lblError.setText("La fecha es obligatoria");
            dateFecha.requestFocus();
            return false;
        }

        // Validar hora
        if (comboHora.getValue() == null) {
            lblError.setText("La hora es obligatoria");
            comboHora.requestFocus();
            return false;
        }

        // Validar que la fecha y hora no sean anteriores a la fecha y hora actual si es un nuevo registro
        LocalDate fecha = dateFecha.getValue();
        LocalTime hora = LocalTime.parse(comboHora.getValue() + ":00");
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        if (!modoEdicion && fechaHora.isBefore(LocalDateTime.now())) {
            lblError.setText("La fecha y hora de inicio no pueden ser anteriores a la fecha y hora actual");
            dateFecha.requestFocus();
            return false;
        }

        // Validar cupo máximo
        try {
            int cupoMaximo = Integer.parseInt(txtCupoMaximo.getText().trim());
            if (cupoMaximo <= 0) {
                lblError.setText("El cupo máximo debe ser mayor que cero");
                txtCupoMaximo.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("El cupo máximo debe ser un número entero válido");
            txtCupoMaximo.requestFocus();
            return false;
        }

        // Validar cupos disponibles
        try {
            int cuposDisponibles = Integer.parseInt(txtCuposDisponibles.getText().trim());
            int cupoMaximo = Integer.parseInt(txtCupoMaximo.getText().trim());

            if (cuposDisponibles < 0) {
                lblError.setText("Los cupos disponibles no pueden ser negativos");
                txtCuposDisponibles.requestFocus();
                return false;
            }

            if (cuposDisponibles > cupoMaximo) {
                lblError.setText("Los cupos disponibles no pueden ser mayores que el cupo máximo");
                txtCuposDisponibles.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("Los cupos disponibles deben ser un número entero válido");
            txtCuposDisponibles.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Método para mostrar el formulario de creación de actividades
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

        // Establecer valores por defecto
        dateFecha.setValue(LocalDate.now());
        comboHora.setValue("08:00");

        // Enfocar en el nombre
        txtNombre.requestFocus();
    }

    /**
     * Método para editar una actividad existente
     * @param actividad
     */
    private void editarActividad(Actividad actividad) {
        // Guardar referencia a la actividad seleccionada
        actividadSeleccionada = actividad;

        // Cargar datos en el formulario
        txtNombre.setText(actividad.getNombre());
        txtDescripcion.setText(actividad.getDescripcion());
        txtUbicacion.setText(actividad.getUbicacion());
        txtPrecio.setText(String.valueOf(actividad.getPrecio()));
        txtDuracion.setText(String.valueOf(actividad.getDuracion()));

        // Establecer fecha y hora
        if (actividad.getFechaInicio() != null) {
            dateFecha.setValue(actividad.getFechaInicio().toLocalDate());
            String hora = String.format("%02d:00", actividad.getFechaInicio().getHour());
            comboHora.setValue(hora);
        }

        txtCupoMaximo.setText(String.valueOf(actividad.getCupoMaximo()));
        txtCuposDisponibles.setText(String.valueOf(actividad.getCuposDisponibles()));

        // Establecer modo edición
        modoEdicion = true;

        // Mostrar panel de formulario
        panelFormulario.setVisible(true);
    }

    /**
     * Método para eliminar una actividad
     * @param actividad
     */
    private void eliminarActividad(Actividad actividad) {
        // Confirmar eliminación
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta actividad?");
        alert.setContentText("Esta acción no se puede deshacer.");

        if (alert.showAndWait().get().getButtonData().isCancelButton()) {
            return; // El usuario canceló la operación
        }

        try {
            // Eliminar actividad
            Respuesta<Actividad> respuesta = ModelFactoryController.getInstance()
                    .getSistema().eliminarActividad(actividad.getId());

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Actividad eliminada correctamente", AlertType.INFORMATION);

                // Recargar lista de actividades
                cargarActividades();

                // Ocultar formulario si estaba visible
                if (panelFormulario.isVisible() && actividadSeleccionada != null &&
                        actividadSeleccionada.getId() == actividad.getId()) {
                    panelFormulario.setVisible(false);
                }
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar la actividad: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cancelar la edición o creación de una actividad
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
        txtDescripcion.clear();
        txtUbicacion.clear();
        txtPrecio.clear();
        txtDuracion.clear();
        dateFecha.setValue(LocalDate.now());
        comboHora.setValue("08:00");
        txtCupoMaximo.clear();
        txtCuposDisponibles.clear();
        lblError.setText("");

        actividadSeleccionada = null;
        modoEdicion = false;
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
