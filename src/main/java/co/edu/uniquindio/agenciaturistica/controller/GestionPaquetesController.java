package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.PaqueteTuristico;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionPaquetesController implements Initializable {

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGestionarActividades;

    @FXML
    private Button btnGestionarHospedajes;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    @FXML
    private TableColumn<PaqueteTuristico, String> colAcciones;

    @FXML
    private TableColumn<PaqueteTuristico, Integer> colCupos;

    @FXML
    private TableColumn<PaqueteTuristico, Integer> colDuracion;

    @FXML
    private TableColumn<PaqueteTuristico, String> colFechaFin;

    @FXML
    private TableColumn<PaqueteTuristico, String> colFechaInicio;

    @FXML
    private TableColumn<PaqueteTuristico, Integer> colId;

    @FXML
    private TableColumn<PaqueteTuristico, String> colNombre;

    @FXML
    private TableColumn<PaqueteTuristico, Double> colPrecio;

    @FXML
    private DatePicker dateFechaFin;

    @FXML
    private DatePicker dateFechaInicio;

    @FXML
    private Label lblError;

    @FXML
    private Label lblTotalPaquetes;

    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<PaqueteTuristico> tablaPaquetes;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtCupoMaximo;

    @FXML
    private TextField txtCuposDisponibles;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private TextField txtDuracionDias;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecioBase;

    private Aplicacion aplicacion;
    private ObservableList<PaqueteTuristico> listaPaquetes = FXCollections.observableArrayList();
    private PaqueteTuristico paqueteSeleccionado;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tabla
        configurarTabla();

        // Ocultar panel de formulario
        panelFormulario.setVisible(false);

        // Configurar fechas mínimas
        dateFechaInicio.setValue(LocalDate.now());
        dateFechaFin.setValue(LocalDate.now().plusDays(1));

        // Desactivar botones de gestión hasta que se seleccione un paquete
        btnGestionarActividades.setDisable(true);
        btnGestionarHospedajes.setDisable(true);
    }

    /**
     * Método para configurar la tabla de paquetes
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecioBase()).asObject());
        colDuracion.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDuracionDias()).asObject());

        // Formatear fechas
        colFechaInicio.setCellValueFactory(cellData -> {
            PaqueteTuristico paquete = cellData.getValue();
            String fechaStr = paquete.getFechaInicio() != null ?
                    paquete.getFechaInicio().toString() : "No disponible";
            return new SimpleStringProperty(fechaStr);
        });

        colFechaFin.setCellValueFactory(cellData -> {
            PaqueteTuristico paquete = cellData.getValue();
            String fechaStr = paquete.getFechaFin() != null ?
                    paquete.getFechaFin().toString() : "No disponible";
            return new SimpleStringProperty(fechaStr);
        });

        colCupos.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCuposDisponibles()).asObject());

        // Botones de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());

        tablaPaquetes.setItems(listaPaquetes);

        // Listener para selección de paquete
        tablaPaquetes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                paqueteSeleccionado = newSelection;
                btnGestionarActividades.setDisable(false);
                btnGestionarHospedajes.setDisable(false);
            } else {
                paqueteSeleccionado = null;
                btnGestionarActividades.setDisable(true);
                btnGestionarHospedajes.setDisable(true);
            }
        });
    }

    /**
     * Método para crear los botones de acciones (editar y eliminar)
     * @return
     */
    private Callback<TableColumn<PaqueteTuristico, String>, TableCell<PaqueteTuristico, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<PaqueteTuristico, String>, TableCell<PaqueteTuristico, String>>() {
            @Override
            public TableCell<PaqueteTuristico, String> call(final TableColumn<PaqueteTuristico, String> param) {
                return new TableCell<PaqueteTuristico, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox pane = new HBox(5, btnEditar, btnEliminar);

                    {
                        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnEditar.setOnAction(event -> {
                            PaqueteTuristico paquete = getTableView().getItems().get(getIndex());
                            editarPaquete(paquete);
                        });

                        btnEliminar.setOnAction(event -> {
                            PaqueteTuristico paquete = getTableView().getItems().get(getIndex());
                            eliminarPaquete(paquete);
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

        // Cargar paquetes
        cargarPaquetes();
    }

    /**
     * Método para cargar los paquetes desde la base de datos
     */
    private void cargarPaquetes() {
        try {
            // Obtener la lista de paquetes turísticos desde el controlador
            List<PaqueteTuristico> paquetes = ModelFactoryController.getInstance()
                    .getSistema().obtenerPaquetesTuristicos();

            // Actualizar lista observable
            listaPaquetes.clear();
            listaPaquetes.addAll(paquetes);

            // Actualizar contador
            actualizarContador();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar los paquetes turísticos: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el contador de paquetes
     */
    private void actualizarContador() {
        lblTotalPaquetes.setText("Total de paquetes: " + listaPaquetes.size());
    }

    /**
     * Método para buscar un paquete
     * @param event
     */
    @FXML
    void buscarPaquete(ActionEvent event) {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos los paquetes
            tablaPaquetes.setItems(listaPaquetes);
            return;
        }

        // Filtrar paquetes por nombre o descripción
        ObservableList<PaqueteTuristico> paquetesFiltrados = FXCollections.observableArrayList();

        for (PaqueteTuristico paquete : listaPaquetes) {
            if (paquete.getNombre().toLowerCase().contains(textoBusqueda) ||
                    paquete.getDescripcion().toLowerCase().contains(textoBusqueda)) {
                paquetesFiltrados.add(paquete);
            }
        }

        // Actualizar tabla con resultados filtrados
        tablaPaquetes.setItems(paquetesFiltrados);
        lblTotalPaquetes.setText("Resultados: " + paquetesFiltrados.size());
    }

    /**
     * Método para gestionar las actividades de un paquete
     * @param event
     */
    @FXML
    void gestionarActividades(ActionEvent event) {

    }

    /**
     * Método para gestionar los hospedajes de un paquete
     * @param event
     */
    @FXML
    void gestionarHospedajes(ActionEvent event) {

    }

    /**
     * Método para guardar un paquete (crear o actualizar)
     * @param event
     */
    @FXML
    void guardarPaquete(ActionEvent event) {
        // Validar los datos del formulario
        if (!validarDatosFormulario()) {
            return;
        }

        try {
            // Crear objeto PaqueteTuristico con los datos del formulario
            PaqueteTuristico paquete = new PaqueteTuristico();
            if (modoEdicion) {
                paquete.setId(paqueteSeleccionado.getId());
            }

            paquete.setNombre(txtNombre.getText().trim());
            paquete.setDescripcion(txtDescripcion.getText().trim());
            paquete.setPrecioBase(Double.parseDouble(txtPrecioBase.getText().trim()));
            paquete.setDuracionDias(Integer.parseInt(txtDuracionDias.getText().trim()));
            paquete.setFechaInicio(dateFechaInicio.getValue());
            paquete.setFechaFin(dateFechaFin.getValue());
            paquete.setCupoMaximo(Integer.parseInt(txtCupoMaximo.getText().trim()));
            paquete.setCuposDisponibles(Integer.parseInt(txtCuposDisponibles.getText().trim()));

            Respuesta<PaqueteTuristico> respuesta;

            if (modoEdicion) {
                // Actualizar paquete existente
                respuesta = ModelFactoryController.getInstance().getSistema().actualizarPaqueteTuristico(paquete);
            } else {
                // Crear nuevo paquete
                respuesta = ModelFactoryController.getInstance().getSistema().crearPaqueteTuristico(paquete);
            }

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                String mensaje = modoEdicion ? "Paquete actualizado correctamente" : "Paquete creado correctamente";
                mostrarAlerta("Éxito", mensaje, AlertType.INFORMATION);

                // Recargar lista de paquetes
                cargarPaquetes();

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
            lblError.setText("Error al guardar el paquete: " + e.getMessage());
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

        // Validar precio base
        try {
            double precioBase = Double.parseDouble(txtPrecioBase.getText().trim());
            if (precioBase <= 0) {
                lblError.setText("El precio base debe ser mayor que cero");
                txtPrecioBase.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("El precio base debe ser un número válido");
            txtPrecioBase.requestFocus();
            return false;
        }

        // Validar duración
        try {
            int duracion = Integer.parseInt(txtDuracionDias.getText().trim());
            if (duracion <= 0) {
                lblError.setText("La duración debe ser mayor que cero");
                txtDuracionDias.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            lblError.setText("La duración debe ser un número entero válido");
            txtDuracionDias.requestFocus();
            return false;
        }

        // Validar fechas
        if (dateFechaInicio.getValue() == null) {
            lblError.setText("La fecha de inicio es obligatoria");
            dateFechaInicio.requestFocus();
            return false;
        }

        if (dateFechaFin.getValue() == null) {
            lblError.setText("La fecha de fin es obligatoria");
            dateFechaFin.requestFocus();
            return false;
        }

        // Validar que la fecha de inicio sea anterior a la fecha de fin
        if (dateFechaInicio.getValue().isAfter(dateFechaFin.getValue())) {
            lblError.setText("La fecha de inicio debe ser anterior a la fecha de fin");
            dateFechaInicio.requestFocus();
            return false;
        }

        // Validar que la fecha de inicio no sea anterior a la fecha actual si es un nuevo paquete
        if (!modoEdicion && dateFechaInicio.getValue().isBefore(LocalDate.now())) {
            lblError.setText("La fecha de inicio no puede ser anterior a la fecha actual");
            dateFechaInicio.requestFocus();
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
     * Método para mostrar el formulario de creación de paquetes
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

        // Establecer fechas por defecto
        dateFechaInicio.setValue(LocalDate.now());
        dateFechaFin.setValue(LocalDate.now().plusDays(7));

        // Deshabilitar botones de gestión
        btnGestionarActividades.setDisable(true);
        btnGestionarHospedajes.setDisable(true);

        // Enfocar en el nombre
        txtNombre.requestFocus();
    }

    /**
     * Método para editar un paquete existente
     * @param paquete
     */
    private void editarPaquete(PaqueteTuristico paquete) {
        // Guardar referencia al paquete seleccionado
        paqueteSeleccionado = paquete;

        // Cargar datos en el formulario
        txtNombre.setText(paquete.getNombre());
        txtDescripcion.setText(paquete.getDescripcion());
        txtPrecioBase.setText(String.valueOf(paquete.getPrecioBase()));
        txtDuracionDias.setText(String.valueOf(paquete.getDuracionDias()));
        dateFechaInicio.setValue(paquete.getFechaInicio());
        dateFechaFin.setValue(paquete.getFechaFin());
        txtCupoMaximo.setText(String.valueOf(paquete.getCupoMaximo()));
        txtCuposDisponibles.setText(String.valueOf(paquete.getCuposDisponibles()));

        // Establecer modo edición
        modoEdicion = true;

        // Habilitar botones de gestión
        btnGestionarActividades.setDisable(false);
        btnGestionarHospedajes.setDisable(false);

        // Mostrar panel de formulario
        panelFormulario.setVisible(true);
    }

    /**
     * Método para eliminar un paquete
     * @param paquete
     */
    private void eliminarPaquete(PaqueteTuristico paquete) {
        // Confirmar eliminación
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este paquete?");
        alert.setContentText("Esta acción no se puede deshacer.");

        if (alert.showAndWait().get().getButtonData().isCancelButton()) {
            return; // El usuario canceló la operación
        }

        try {
            // Eliminar paquete
            Respuesta<PaqueteTuristico> respuesta = ModelFactoryController.getInstance()
                    .getSistema().eliminarPaqueteTuristico(paquete.getId());

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "Paquete eliminado correctamente", AlertType.INFORMATION);

                // Recargar lista de paquetes
                cargarPaquetes();

                // Ocultar formulario si estaba visible
                if (panelFormulario.isVisible() && paqueteSeleccionado != null &&
                        paqueteSeleccionado.getId() == paquete.getId()) {
                    panelFormulario.setVisible(false);
                }
            } else {
                // Mostrar mensaje de error
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar el paquete: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cancelar la edición o creación de un paquete
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
        txtPrecioBase.clear();
        txtDuracionDias.clear();
        dateFechaInicio.setValue(LocalDate.now());
        dateFechaFin.setValue(LocalDate.now().plusDays(7));
        txtCupoMaximo.clear();
        txtCuposDisponibles.clear();
        lblError.setText("");

        paqueteSeleccionado = null;
        modoEdicion = false;
    }

    /**
     * Método para volver a la pantalla anterior
     * @param event
     */
    @FXML
    void volver(ActionEvent event) {
        try {

            if(aplicacion.getEmpleadoActual().getRol() == Rol.EMPLEADO){
                // Cargar la pantalla anterior (en este caso el panel de empleado)
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/co/edu/uniquindio/agenciaturistica/Empleado.fxml")
                );
                Parent root = loader.load();

                EmpleadoController controller = loader.getController();
                controller.setAplicacion(aplicacion);
                controller.inicializarInformacion();
                Scene scene = new Scene(root);
                Stage stage = (Stage) btnVolver.getScene().getWindow();
                stage.setScene(scene);
                stage.centerOnScreen();

            }else{
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
            }
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