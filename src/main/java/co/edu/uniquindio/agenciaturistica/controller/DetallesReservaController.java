package co.edu.uniquindio.agenciaturistica.controller;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.*;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Enums.FormaPago;
import co.edu.uniquindio.agenciaturistica.util.EmailSender;
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
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DetallesReservaController implements Initializable {

    @FXML private Button btnAgregarHabitacion;
    @FXML private Button btnBuscarCliente;
    @FXML private Button btnCancelar;
    @FXML private Button btnCerrar;
    @FXML private Button btnConfirmar;
    @FXML private Button btnGenerarVoucher;
    @FXML private Button btnGuardar;
    @FXML private Button btnQuitarHabitacion;
    @FXML private Button btnVerPaquete;
    @FXML private TableColumn<Habitacion, Integer> colHabCapacidad;
    @FXML private TableColumn<Habitacion, Integer> colHabId;
    @FXML private TableColumn<Habitacion, Double> colHabPrecio;
    @FXML private TableColumn<Habitacion, String> colHabTipo;
    @FXML private TableColumn<Habitacion, Double> colHabTotal;
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<EstadoReserva> comboEstado;
    @FXML private ComboBox<FormaPago> comboFormaPago;
    @FXML private ComboBox<PaqueteTuristico> comboPaquete;
    @FXML private DatePicker dateFechaFin;
    @FXML private DatePicker dateFechaInicio;
    @FXML private GridPane gridDatosCliente;
    @FXML private Label lblError;
    @FXML private Label lblTitulo;
    @FXML private TableView<Habitacion> tablaHabitaciones;
    @FXML private TextField txtContactoCliente;
    @FXML private TextField txtIdReserva;
    @FXML private TextField txtIdentificacionCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtPrecioTotal;

    private Aplicacion aplicacion;
    private ObservableList<Habitacion> listaHabitaciones = FXCollections.observableArrayList();
    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private ObservableList<PaqueteTuristico> listaPaquetes = FXCollections.observableArrayList();
    private Reserva reservaActual;
    private boolean modoCreacion = true;
    private boolean modoVisualizacion = false;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Empleado empleadoActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar la tabla de habitaciones
        configurarTablaHabitaciones();

        // Configurar combos y estados iniciales
        configurarCombos();

        // Configurar listeners para recalcular precios automáticamente
        configurarListeners();

        // Inicializar con valores por defecto
        dateFechaInicio.setValue(LocalDate.now());
        dateFechaFin.setValue(LocalDate.now().plusDays(1));

        // Ocultar mensajes de error inicialmente
        lblError.setText("");
    }

    /**
     * Método para configurar la tabla de habitaciones
     */
    private void configurarTablaHabitaciones() {
        colHabId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colHabTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipoHabitacion().getNombre()));

        colHabCapacidad.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCapacidad()).asObject());

        colHabPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecioPorNoche()).asObject());
        colHabPrecio.setCellFactory(column -> new TableCell<Habitacion, Double>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", precio));
                }
            }
        });

        colHabTotal.setCellValueFactory(cellData -> {
            double precioPorNoche = cellData.getValue().getPrecioPorNoche();
            long diasEstancia = 1; // Valor por defecto

            if (dateFechaInicio.getValue() != null && dateFechaFin.getValue() != null) {
                diasEstancia = ChronoUnit.DAYS.between(dateFechaInicio.getValue(), dateFechaFin.getValue());
                if (diasEstancia < 1) diasEstancia = 1; // Mínimo 1 día
            }

            return new SimpleDoubleProperty(precioPorNoche * diasEstancia).asObject();
        });
        colHabTotal.setCellFactory(column -> new TableCell<Habitacion, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", total));
                }
            }
        });

        tablaHabitaciones.setItems(listaHabitaciones);
    }

    /**
     * Método para configurar los combo box
     */
    private void configurarCombos() {
        // Configurar combo de estados
        comboEstado.setItems(FXCollections.observableArrayList(EstadoReserva.values()));
        comboEstado.setValue(EstadoReserva.PENDIENTE);

        // Configurar combo de formas de pago
        comboFormaPago.setItems(FXCollections.observableArrayList(FormaPago.values()));
        comboFormaPago.setValue(FormaPago.TARJETA_CREDITO);
    }

    /**
     * Método para configurar los listeners de la interfaz
     */
    private void configurarListeners() {
        // Listener para fechas que recalculan el precio total
        dateFechaInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Actualizar los precios totales de las habitaciones
            tablaHabitaciones.refresh();
            // Recalcular precio total
            calcularPrecioTotal();
        });

        dateFechaFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Actualizar los precios totales de las habitaciones
            tablaHabitaciones.refresh();
            // Recalcular precio total
            calcularPrecioTotal();
        });

        // Listener para paquete turístico que actualiza el precio total
        comboPaquete.valueProperty().addListener((obs, oldVal, newVal) -> calcularPrecioTotal());

        // Listener para tabla de habitaciones que actualiza el precio total
        listaHabitaciones.addListener((javafx.collections.ListChangeListener.Change<? extends Habitacion> c) -> calcularPrecioTotal());

        // Listener para cliente seleccionado
        comboCliente.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarDatosCliente(newVal);
            } else {
                limpiarDatosCliente();
            }
        });
    }

    /**
     * Método para establecer la aplicación
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
        // Si hay un usuario actual, obtener el empleado
        if (aplicacion.getUsuarioActual() instanceof Empleado) {
            this.empleadoActual = (Empleado) aplicacion.getUsuarioActual();
        }
    }

    /**
     * Método para establecer el empleado actual
     */
    public void setEmpleado(Empleado empleado) {
        this.empleadoActual = empleado;
    }

    /**
     * Método para inicializar una nueva reserva
     */
    public void inicializarNuevaReserva() {
        try {
            // Establecer modo creación
            modoCreacion = true;
            modoVisualizacion = false;

            // Establecer título
            lblTitulo.setText("Crear Nueva Reserva");

            // Generar ID única para la nueva reserva
            txtIdReserva.setText(UUID.randomUUID().toString());

            // Inicializar valores por defecto
            txtPrecioTotal.setText("0.00");
            comboEstado.setValue(EstadoReserva.PENDIENTE);
            comboFormaPago.setValue(FormaPago.EFECTIVO);

            // Configurar visibilidad de botones
            btnConfirmar.setVisible(false);
            btnCancelar.setVisible(false);
            btnGenerarVoucher.setVisible(false);
            btnGuardar.setVisible(true);

            // Cargar datos necesarios
            cargarClientes();
            cargarPaquetes();

            // Habilitar campos editables
            habilitarCampos(true);

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al inicializar nueva reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cargar una reserva existente
     * @param reserva La reserva a cargar
     * @param soloLectura Si es true, los campos estarán deshabilitados
     */
    public void cargarReserva(Reserva reserva, boolean soloLectura) {
        try {
            // Establecer modos
            modoCreacion = false;
            modoVisualizacion = soloLectura;

            // Guardar referencia a la reserva
            this.reservaActual = reserva;

            // Establecer título según modo
            lblTitulo.setText(soloLectura ? "Detalles de Reserva" : "Editar Reserva");

            // Cargar datos necesarios
            cargarClientes();
            cargarPaquetes();

            // Cargar datos de la reserva
            txtIdReserva.setText(reserva.getId());
            comboEstado.setValue(reserva.getEstadoReserva());
            dateFechaInicio.setValue(reserva.getFechaInicio());
            dateFechaFin.setValue(reserva.getFechaFin());
            txtPrecioTotal.setText(String.format("%.2f", reserva.getPrecioTotal()));
            comboFormaPago.setValue(reserva.getFormaPago());

            // Seleccionar cliente
            seleccionarCliente(reserva.getCliente());

            // Seleccionar paquete si existe
            if (reserva.getPaqueteTuristico() != null) {
                seleccionarPaquete(reserva.getPaqueteTuristico());
            } else {
                // Seleccionar "Sin paquete"
                comboPaquete.getSelectionModel().selectFirst();
            }

            // Cargar habitaciones
            listaHabitaciones.clear();
            if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
                listaHabitaciones.addAll(reserva.getHabitaciones());
            }

            // Configurar controles según modo
            habilitarCampos(!soloLectura);
            configurarBotonesSegunEstado(reserva.getEstadoReserva());

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar la reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para habilitar o deshabilitar campos según el modo
     */
    private void habilitarCampos(boolean editable) {
        // Campos de texto siempre son de solo lectura
        txtIdReserva.setEditable(false);
        txtPrecioTotal.setEditable(false);
        txtNombreCliente.setEditable(false);
        txtIdentificacionCliente.setEditable(false);
        txtContactoCliente.setEditable(false);

        // Campos que cambian según el modo
        comboEstado.setDisable(!editable);
        dateFechaInicio.setDisable(!editable);
        dateFechaFin.setDisable(!editable);
        comboFormaPago.setDisable(!editable);
        comboCliente.setDisable(!editable);
        btnBuscarCliente.setDisable(!editable);
        comboPaquete.setDisable(!editable);
        btnVerPaquete.setDisable(false); // Siempre disponible
        btnAgregarHabitacion.setDisable(!editable);
        btnQuitarHabitacion.setDisable(!editable);
    }

    /**
     * Método para configurar los botones según el estado de la reserva
     */
    private void configurarBotonesSegunEstado(EstadoReserva estado) {
        // Por defecto, ocultar todos los botones de acción
        btnGuardar.setVisible(false);
        btnConfirmar.setVisible(false);
        btnCancelar.setVisible(false);
        btnGenerarVoucher.setVisible(false);

        // En modo visualización, solo mostrar botones relevantes
        if (modoVisualizacion) {
            btnGenerarVoucher.setVisible(estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.COMPLETADA);
            return;
        }

        // En modo creación, solo mostrar botón guardar
        if (modoCreacion) {
            btnGuardar.setVisible(true);
            return;
        }

        // En modo edición, configurar según estado
        switch (estado) {
            case PENDIENTE:
                btnGuardar.setVisible(true);
                btnConfirmar.setVisible(true);
                btnCancelar.setVisible(true);
                break;
            case CONFIRMADA:
                btnGuardar.setVisible(true);
                btnCancelar.setVisible(true);
                btnGenerarVoucher.setVisible(true);
                break;
            case COMPLETADA:
                btnGenerarVoucher.setVisible(true);
                break;
            case CANCELADA:
                // No mostrar ningún botón de acción
                break;
        }
    }

    /**
     * Método para cargar la lista de clientes desde la base de datos
     */
    private void cargarClientes() throws SQLException {
        listaClientes.clear();
        List<Cliente> clientes = ModelFactoryController.getInstance().getSistema().obtenerClientes();
        listaClientes.addAll(clientes);
        comboCliente.setItems(listaClientes);

        // Configurar cómo se muestran los clientes en el combo
        comboCliente.setCellFactory(param -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                if (empty || cliente == null) {
                    setText(null);
                } else {
                    setText(cliente.getNombre() + " " + cliente.getApellido() + " (" + cliente.getIdentificacion() + ")");
                }
            }
        });

        comboCliente.setButtonCell(new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                if (empty || cliente == null) {
                    setText(null);
                } else {
                    setText(cliente.getNombre() + " " + cliente.getApellido() + " (" + cliente.getIdentificacion() + ")");
                }
            }
        });
    }

    /**
     * Método para cargar la lista de paquetes turísticos desde la base de datos
     */
    private void cargarPaquetes() throws SQLException {
        listaPaquetes.clear();
        List<PaqueteTuristico> paquetes = ModelFactoryController.getInstance().getSistema().obtenerPaquetesTuristicos();

        // Filtrar solo paquetes disponibles con cupos
        List<PaqueteTuristico> paquetesDisponibles = new ArrayList<>();
        for (PaqueteTuristico p : paquetes) {
            if (p.getCuposDisponibles() > 0 && p.getFechaFin().isAfter(LocalDate.now())) {
                paquetesDisponibles.add(p);
            }
        }

        // Agregar una opción "Sin paquete"
        PaqueteTuristico sinPaquete = new PaqueteTuristico();
        sinPaquete.setId(-1);
        sinPaquete.setNombre("Sin paquete");
        sinPaquete.setPrecioBase(0);

        listaPaquetes.add(sinPaquete);
        listaPaquetes.addAll(paquetesDisponibles);

        comboPaquete.setItems(listaPaquetes);

        // Configurar cómo se muestran los paquetes en el combo
        comboPaquete.setCellFactory(param -> new ListCell<PaqueteTuristico>() {
            @Override
            protected void updateItem(PaqueteTuristico paquete, boolean empty) {
                super.updateItem(paquete, empty);
                if (empty || paquete == null) {
                    setText(null);
                } else {
                    setText(paquete.getNombre() + (paquete.getId() > 0 ? " ($" + String.format("%.2f", paquete.getPrecioBase()) + ")" : ""));
                }
            }
        });

        comboPaquete.setButtonCell(new ListCell<PaqueteTuristico>() {
            @Override
            protected void updateItem(PaqueteTuristico paquete, boolean empty) {
                super.updateItem(paquete, empty);
                if (empty || paquete == null) {
                    setText(null);
                } else {
                    setText(paquete.getNombre() + (paquete.getId() > 0 ? " ($" + String.format("%.2f", paquete.getPrecioBase()) + ")" : ""));
                }
            }
        });

        // Seleccionar el primer elemento (Sin paquete) por defecto
        comboPaquete.getSelectionModel().selectFirst();
    }

    /**
     * Método para seleccionar un cliente en el combobox
     */
    private void seleccionarCliente(Cliente cliente) {
        if (cliente == null) return;

        for (Cliente c : listaClientes) {
            if (c.getIdentificacion().equals(cliente.getIdentificacion())) {
                comboCliente.setValue(c);
                actualizarDatosCliente(c);
                break;
            }
        }
    }

    /**
     * Método para seleccionar un paquete en el combobox
     */
    private void seleccionarPaquete(PaqueteTuristico paquete) {
        if (paquete == null) return;

        for (PaqueteTuristico p : listaPaquetes) {
            if (p.getId() == paquete.getId()) {
                comboPaquete.setValue(p);
                break;
            }
        }
    }

    /**
     * Método para actualizar los campos de información del cliente
     */
    private void actualizarDatosCliente(Cliente cliente) {
        if (cliente != null) {
            txtNombreCliente.setText(cliente.getNombre() + " " + cliente.getApellido());
            txtIdentificacionCliente.setText(cliente.getIdentificacion());
            txtContactoCliente.setText(cliente.getCorreo() + " / " + cliente.getTelefono());
            gridDatosCliente.setVisible(true);
        }
    }

    /**
     * Método para limpiar los campos de información del cliente
     */
    private void limpiarDatosCliente() {
        txtNombreCliente.setText("");
        txtIdentificacionCliente.setText("");
        txtContactoCliente.setText("");
        gridDatosCliente.setVisible(false);
    }

    /**
     * Método para calcular el precio total de la reserva
     */
    private void calcularPrecioTotal() {
        double precioTotal = 0;

        // Precio del paquete
        PaqueteTuristico paquete = comboPaquete.getValue();
        if (paquete != null && paquete.getId() > 0) {
            precioTotal += paquete.getPrecioBase();
        }

        // Precio de las habitaciones
        if (dateFechaInicio.getValue() != null && dateFechaFin.getValue() != null) {
            long diasEstancia = ChronoUnit.DAYS.between(dateFechaInicio.getValue(), dateFechaFin.getValue());
            if (diasEstancia < 1) diasEstancia = 1; // Mínimo 1 día

            for (Habitacion habitacion : listaHabitaciones) {
                precioTotal += habitacion.getPrecioPorNoche() * diasEstancia;
            }
        }

        txtPrecioTotal.setText(String.format("%.2f", precioTotal));
    }

    /**
     * Método para buscar un cliente
     */
    @FXML
    void buscarCliente(ActionEvent event) {
        try {
            // Abrir diálogo para buscar cliente
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Buscar Cliente");
            dialog.setHeaderText("Ingrese la identificación o nombre del cliente");

            ButtonType buscarButtonType = new ButtonType("Buscar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buscarButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField textField = new TextField();
            textField.setPromptText("Identificación o nombre");

            grid.add(new Label("Criterio de búsqueda:"), 0, 0);
            grid.add(textField, 1, 0);

            dialog.getDialogPane().setContent(grid);

            // Convertir resultado del diálogo
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buscarButtonType) {
                    return textField.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(criterio -> {
                if (criterio.trim().isEmpty()) {
                    mostrarAlerta("Advertencia", "Debe ingresar un criterio de búsqueda", AlertType.WARNING);
                    return;
                }

                // Buscar cliente por identificación o nombre
                Cliente clienteEncontrado = null;

                for (Cliente cliente : listaClientes) {
                    if (cliente.getIdentificacion().equalsIgnoreCase(criterio) ||
                            cliente.getNombre().toLowerCase().contains(criterio.toLowerCase()) ||
                            cliente.getApellido().toLowerCase().contains(criterio.toLowerCase())) {
                        clienteEncontrado = cliente;
                        break;
                    }
                }

                if (clienteEncontrado != null) {
                    comboCliente.setValue(clienteEncontrado);
                    actualizarDatosCliente(clienteEncontrado);
                } else {
                    mostrarAlerta("Información", "No se encontró ningún cliente con ese criterio", AlertType.INFORMATION);
                }
            });

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al buscar cliente: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para agregar una habitación a la reserva
     */
    @FXML
    void agregarHabitacion(ActionEvent event) {
        try {
            // Verificar que se hayan seleccionado fechas
            if (dateFechaInicio.getValue() == null || dateFechaFin.getValue() == null) {
                mostrarAlerta("Advertencia", "Debe seleccionar las fechas de la reserva antes de agregar habitaciones", AlertType.WARNING);
                return;
            }

            if (dateFechaInicio.getValue().isAfter(dateFechaFin.getValue())) {
                mostrarAlerta("Advertencia", "La fecha de inicio debe ser anterior a la fecha de fin", AlertType.WARNING);
                return;
            }

            // Abrir ventana de selección de habitaciones
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/SeleccionHabitacion.fxml"));
            Parent root = loader.load();

            SeleccionHabitacionController controller = loader.getController();
            controller.setFechasReserva(dateFechaInicio.getValue(), dateFechaFin.getValue());
            controller.setHabitacionesSeleccionadas(new ArrayList<>(listaHabitaciones));
            controller.inicializarDatos();

            Stage stage = new Stage();
            stage.setTitle("Seleccionar Habitación");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Obtener habitaciones seleccionadas
            List<Habitacion> habitacionesSeleccionadas = controller.getHabitacionesSeleccionadas();
            if (habitacionesSeleccionadas != null) {
                listaHabitaciones.clear();
                listaHabitaciones.addAll(habitacionesSeleccionadas);
            }

        } catch (IOException e) {
            mostrarAlerta("Error", "Error al abrir la ventana de selección de habitaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para quitar una habitación seleccionada
     */
    @FXML
    void quitarHabitacion(ActionEvent event) {
        Habitacion habitacionSeleccionada = tablaHabitaciones.getSelectionModel().getSelectedItem();

        if (habitacionSeleccionada != null) {
            listaHabitaciones.remove(habitacionSeleccionada);
        } else {
            mostrarAlerta("Advertencia", "Debe seleccionar una habitación para quitar", AlertType.WARNING);
        }
    }

    /**
     * Método para ver detalles del paquete seleccionado
     */
    @FXML
    void verDetallesPaquete(ActionEvent event) {
        PaqueteTuristico paqueteSeleccionado = comboPaquete.getValue();

        if (paqueteSeleccionado == null || paqueteSeleccionado.getId() < 0) {
            mostrarAlerta("Advertencia", "No hay un paquete seleccionado para mostrar", AlertType.WARNING);
            return;
        }

        try {
            // Cargar información completa del paquete
            Respuesta<PaqueteTuristico> respuesta = ModelFactoryController.getInstance().getSistema().buscarPaquetePorId(paqueteSeleccionado.getId());

            if (respuesta.isExito()) {
                PaqueteTuristico paquete = respuesta.getData();

                // Crear ventana con detalles del paquete
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Detalles del Paquete");

                VBox vbox = new VBox(10);
                vbox.setPadding(new Insets(20));

                Label titulo = new Label(paquete.getNombre());
                titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                Label descripcion = new Label(paquete.getDescripcion());
                descripcion.setWrapText(true);

                Label precio = new Label("Precio: $" + String.format("%.2f", paquete.getPrecioBase()));
                Label duracion = new Label("Duración: " + paquete.getDuracionDias() + " días");
                Label fechas = new Label("Fechas: " + paquete.getFechaInicio().format(dateFormatter) +
                        " al " + paquete.getFechaFin().format(dateFormatter));
                Label cupos = new Label("Cupos disponibles: " + paquete.getCuposDisponibles() +
                        " de " + paquete.getCupoMaximo());

                // Agregar secciones para actividades y hospedajes si existen
                if (!paquete.getActividades().isEmpty()) {
                    Label tituloActividades = new Label("Actividades incluidas:");
                    tituloActividades.setStyle("-fx-font-weight: bold;");

                    VBox actividadesBox = new VBox(5);
                    for (Actividad actividad : paquete.getActividades()) {
                        Label actLabel = new Label("• " + actividad.getNombre() + " - " +
                                actividad.getUbicacion());
                        actividadesBox.getChildren().add(actLabel);
                    }

                    vbox.getChildren().addAll(tituloActividades, actividadesBox);
                }

                if (!paquete.getHospedajes().isEmpty()) {
                    Label tituloHospedajes = new Label("Hospedajes incluidos:");
                    tituloHospedajes.setStyle("-fx-font-weight: bold;");

                    VBox hospedajesBox = new VBox(5);
                    for (Hospedaje hospedaje : paquete.getHospedajes()) {
                        Label hospLabel = new Label("• " + hospedaje.getNombre() + " - " +
                                hospedaje.getCiudad() + " (" + hospedaje.getEstrellas() + " estrellas)");
                        hospedajesBox.getChildren().add(hospLabel);
                    }

                    vbox.getChildren().addAll(tituloHospedajes, hospedajesBox);
                }

                Button cerrarBtn = new Button("Cerrar");
                cerrarBtn.setOnAction(e -> stage.close());

                vbox.getChildren().addAll(titulo, descripcion, precio, duracion, fechas, cupos, cerrarBtn);

                Scene scene = new Scene(vbox, 400, 500);
                stage.setScene(scene);
                stage.showAndWait();

            } else {
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar detalles del paquete: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para guardar la reserva
     */
    @FXML
    void guardarReserva(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        try {
            // Crear objeto Reserva con los datos del formulario
            Reserva reserva = modoCreacion ? new Reserva() : reservaActual;

            // En modo creación, usamos el ID generado
            if (modoCreacion) {
                reserva.setId(txtIdReserva.getText().trim());
            }

            reserva.setFechaInicio(dateFechaInicio.getValue());
            reserva.setFechaFin(dateFechaFin.getValue());
            reserva.setPrecioTotal(Double.parseDouble(txtPrecioTotal.getText().trim()));
            reserva.setEstadoReserva(comboEstado.getValue());
            reserva.setFormaPago(comboFormaPago.getValue());
            reserva.setCliente(comboCliente.getValue());

            // Asignar empleado
            if (empleadoActual != null) {
                reserva.setEmpleado(empleadoActual);
            } else if (aplicacion.getUsuarioActual() instanceof Empleado) {
                reserva.setEmpleado((Empleado) aplicacion.getUsuarioActual());
            }

            // Asignar paquete turístico si no es "Sin paquete"
            PaqueteTuristico paqueteSeleccionado = comboPaquete.getValue();
            if (paqueteSeleccionado != null && paqueteSeleccionado.getId() > 0) {
                reserva.setPaqueteTuristico(paqueteSeleccionado);
            } else {
                reserva.setPaqueteTuristico(null);
            }

            // Asignar habitaciones
            reserva.setHabitaciones(new ArrayList<>(listaHabitaciones));

            // Crear o modificar según el modo
            Respuesta<Reserva> respuesta;
            if (modoCreacion) {
                respuesta = ModelFactoryController.getInstance().getSistema().crearReserva(reserva);
            } else {
                respuesta = ModelFactoryController.getInstance().getSistema().modificarReserva(reserva);
            }

            if (respuesta.isExito()) {
                mostrarAlerta("Éxito", modoCreacion ? "Reserva creada exitosamente" : "Reserva actualizada exitosamente", AlertType.INFORMATION);

                // Enviar notificación por correo
                enviarNotificacionReserva(respuesta.getData(), modoCreacion);

                // Cerrar ventana
                cerrarVentana(event);
            } else {
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }

        } catch (SQLException | NumberFormatException e) {
            mostrarAlerta("Error", "Error al guardar la reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para validar los campos del formulario
     */
    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        // Validar cliente
        if (comboCliente.getValue() == null) {
            errores.append("- Debe seleccionar un cliente\n");
        }

        // Validar fechas
        if (dateFechaInicio.getValue() == null) {
            errores.append("- Debe seleccionar una fecha de inicio\n");
        }

        if (dateFechaFin.getValue() == null) {
            errores.append("- Debe seleccionar una fecha de fin\n");
        }

        if (dateFechaInicio.getValue() != null && dateFechaFin.getValue() != null) {
            if (dateFechaInicio.getValue().isAfter(dateFechaFin.getValue())) {
                errores.append("- La fecha de inicio debe ser anterior a la fecha de fin\n");
            }
        }

        // Validar que haya al menos un paquete o una habitación
        PaqueteTuristico paquete = comboPaquete.getValue();
        boolean tienePaquete = paquete != null && paquete.getId() > 0;

        if (!tienePaquete && listaHabitaciones.isEmpty()) {
            errores.append("- Debe seleccionar al menos un paquete o una habitación\n");
        }

        // Validar forma de pago
        if (comboFormaPago.getValue() == null) {
            errores.append("- Debe seleccionar una forma de pago\n");
        }

        // Mostrar errores si los hay
        if (errores.length() > 0) {
            lblError.setText(errores.toString());
            return false;
        }

        // Limpiar mensaje de error
        lblError.setText("");
        return true;
    }

    /**
     * Método para confirmar una reserva
     */
    @FXML
    void confirmarReserva(ActionEvent event) {
        try {
            // Verificar que la reserva no sea nula y esté en estado PENDIENTE
            if (reservaActual == null) {
                mostrarAlerta("Error", "No hay una reserva para confirmar", AlertType.ERROR);
                return;
            }

            if (reservaActual.getEstadoReserva() != EstadoReserva.PENDIENTE) {
                mostrarAlerta("Error", "Solo se pueden confirmar reservas en estado PENDIENTE", AlertType.ERROR);
                return;
            }

            // Confirmar con el usuario
            Alert confirmacion = new Alert(AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Reserva");
            confirmacion.setHeaderText("¿Está seguro de confirmar esta reserva?");
            confirmacion.setContentText("La reserva pasará al estado CONFIRMADA.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                // Confirmar la reserva
                Respuesta<Reserva> respuesta = ModelFactoryController.getInstance()
                        .getSistema().confirmarReserva(reservaActual.getId());

                if (respuesta.isExito()) {
                    mostrarAlerta("Éxito", "Reserva confirmada exitosamente", AlertType.INFORMATION);

                    // Actualizar reserva actual
                    reservaActual = respuesta.getData();

                    // Actualizar estado en la interfaz
                    comboEstado.setValue(reservaActual.getEstadoReserva());

                    // Configurar botones según nuevo estado
                    configurarBotonesSegunEstado(reservaActual.getEstadoReserva());

                    // Enviar notificación por correo
                    enviarNotificacionConfirmacion(reservaActual);
                } else {
                    mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al confirmar la reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cancelar una reserva
     */
    @FXML
    void cancelarReserva(ActionEvent event) {
        try {
            // Verificar que la reserva no sea nula y no esté ya cancelada o completada
            if (reservaActual == null) {
                mostrarAlerta("Error", "No hay una reserva para cancelar", AlertType.ERROR);
                return;
            }

            if (reservaActual.getEstadoReserva() == EstadoReserva.CANCELADA) {
                mostrarAlerta("Error", "La reserva ya está cancelada", AlertType.ERROR);
                return;
            }

            if (reservaActual.getEstadoReserva() == EstadoReserva.COMPLETADA) {
                mostrarAlerta("Error", "No se puede cancelar una reserva completada", AlertType.ERROR);
                return;
            }

            // Confirmar con el usuario
            Alert confirmacion = new Alert(AlertType.CONFIRMATION);
            confirmacion.setTitle("Cancelar Reserva");
            confirmacion.setHeaderText("¿Está seguro de cancelar esta reserva?");
            confirmacion.setContentText("Esta acción liberará las habitaciones y cupos de paquete reservados.\nEsta acción no se puede deshacer.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                // Cancelar la reserva
                Respuesta<Reserva> respuesta = ModelFactoryController.getInstance()
                        .getSistema().cancelarReserva(reservaActual.getId());

                if (respuesta.isExito()) {
                    mostrarAlerta("Éxito", "Reserva cancelada exitosamente", AlertType.INFORMATION);

                    // Actualizar reserva actual
                    reservaActual = respuesta.getData();

                    // Actualizar estado en la interfaz
                    comboEstado.setValue(reservaActual.getEstadoReserva());

                    // Configurar botones según nuevo estado
                    configurarBotonesSegunEstado(reservaActual.getEstadoReserva());

                    // Enviar notificación por correo
                    enviarNotificacionCancelacion(reservaActual);
                } else {
                    mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cancelar la reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para generar un voucher de la reserva
     */
    @FXML
    void generarVoucher(ActionEvent event) {
        try {
            if (reservaActual == null) {
                mostrarAlerta("Error", "No hay una reserva para generar el voucher", AlertType.ERROR);
                return;
            }

            // Verificar que la reserva esté confirmada o completada
            if (reservaActual.getEstadoReserva() != EstadoReserva.CONFIRMADA &&
                    reservaActual.getEstadoReserva() != EstadoReserva.COMPLETADA) {
                mostrarAlerta("Advertencia", "Solo se pueden generar vouchers para reservas confirmadas o completadas", AlertType.WARNING);
                return;
            }

            // Generar el contenido del voucher
            String voucher = generarContenidoVoucher();

            // Mostrar diálogo para guardar archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Voucher");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
            fileChooser.setInitialFileName("voucher_" + reservaActual.getId() + ".txt");

            File archivo = fileChooser.showSaveDialog(btnGenerarVoucher.getScene().getWindow());
            if (archivo != null) {
                // Guardar el voucher en el archivo
                try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
                    writer.print(voucher);
                }

                mostrarAlerta("Éxito", "Voucher generado exitosamente", AlertType.INFORMATION);

                // Preguntar si quiere enviar por correo
                Alert confirmacion = new Alert(AlertType.CONFIRMATION);
                confirmacion.setTitle("Enviar Voucher");
                confirmacion.setHeaderText("¿Desea enviar el voucher por correo al cliente?");

                Optional<ButtonType> resultado = confirmacion.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                    enviarVoucherPorCorreo(voucher);
                }
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al generar el voucher: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para generar el contenido del voucher
     */
    private String generarContenidoVoucher() {
        StringBuilder sb = new StringBuilder();

        // Encabezado
        sb.append("=========================================\n");
        sb.append("              VOUCHER DE RESERVA         \n");
        sb.append("            AGENCIA TURÍSTICA            \n");
        sb.append("=========================================\n\n");

        // Datos de la reserva
        sb.append("CÓDIGO DE RESERVA: ").append(reservaActual.getId()).append("\n");
        sb.append("ESTADO: ").append(reservaActual.getEstadoReserva()).append("\n");
        sb.append("FECHA DE EMISIÓN: ").append(LocalDate.now().format(dateFormatter)).append("\n\n");

        // Datos del cliente
        Cliente cliente = reservaActual.getCliente();
        sb.append("DATOS DEL CLIENTE:\n");
        sb.append("Nombre: ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append("\n");
        sb.append("Identificación: ").append(cliente.getIdentificacion()).append("\n");
        sb.append("Contacto: ").append(cliente.getCorreo()).append(" / ").append(cliente.getTelefono()).append("\n\n");

        // Datos de la estancia
        sb.append("DETALLES DE LA ESTANCIA:\n");
        sb.append("Fecha de ingreso: ").append(reservaActual.getFechaInicio().format(dateFormatter)).append("\n");
        sb.append("Fecha de salida: ").append(reservaActual.getFechaFin().format(dateFormatter)).append("\n");
        sb.append("Duración: ").append(ChronoUnit.DAYS.between(reservaActual.getFechaInicio(), reservaActual.getFechaFin()))
                .append(" noches\n\n");

        // Paquete turístico
        if (reservaActual.getPaqueteTuristico() != null) {
            PaqueteTuristico paquete = reservaActual.getPaqueteTuristico();
            sb.append("PAQUETE TURÍSTICO:\n");
            sb.append("Nombre: ").append(paquete.getNombre()).append("\n");
            sb.append("Descripción: ").append(paquete.getDescripcion()).append("\n");
            sb.append("Precio: $").append(String.format("%.2f", paquete.getPrecioBase())).append("\n\n");

            // Actividades del paquete
            if (!paquete.getActividades().isEmpty()) {
                sb.append("ACTIVIDADES INCLUIDAS:\n");
                for (Actividad actividad : paquete.getActividades()) {
                    sb.append("- ").append(actividad.getNombre())
                            .append(" (").append(actividad.getUbicacion()).append(")\n");
                }
                sb.append("\n");
            }
        }

        // Habitaciones
        if (!reservaActual.getHabitaciones().isEmpty()) {
            sb.append("HABITACIONES:\n");
            long diasEstancia = ChronoUnit.DAYS.between(reservaActual.getFechaInicio(), reservaActual.getFechaFin());
            if (diasEstancia < 1) diasEstancia = 1;

            for (Habitacion habitacion : reservaActual.getHabitaciones()) {
                sb.append("- ").append(habitacion.getTipoHabitacion().getNombre())
                        .append(" (Capacidad: ").append(habitacion.getCapacidad()).append(" personas)")
                        .append(" - $").append(String.format("%.2f", habitacion.getPrecioPorNoche())).append(" por noche")
                        .append(" - Total: $").append(String.format("%.2f", habitacion.getPrecioPorNoche() * diasEstancia))
                        .append("\n");
            }
            sb.append("\n");
        }

        // Resumen de pago
        sb.append("RESUMEN DE PAGO:\n");
        sb.append("Precio total: $").append(String.format("%.2f", reservaActual.getPrecioTotal())).append("\n");
        sb.append("Forma de pago: ").append(reservaActual.getFormaPago()).append("\n\n");

        // Políticas y notas
        sb.append("POLÍTICAS Y NOTAS:\n");
        sb.append("- El check-in se realiza a partir de las 15:00 horas.\n");
        sb.append("- El check-out debe realizarse antes de las 12:00 horas.\n");
        sb.append("- Es necesario presentar un documento de identidad al momento del check-in.\n");
        sb.append("- Para cancelaciones, comuníquese con nosotros con al menos 48 horas de anticipación.\n\n");

        // Pie de página
        sb.append("=========================================\n");
        sb.append("Gracias por elegir nuestros servicios.\n");
        sb.append("Para cualquier consulta, comuníquese al:\n");
        sb.append("Teléfono: (57) 310 555-5555\n");
        sb.append("Email: atencion@agenciaturistica.com\n");
        sb.append("=========================================\n");

        return sb.toString();
    }

    /**
     * Método para enviar el voucher por correo
     */
    private void enviarVoucherPorCorreo(String voucher) {
        try {
            Cliente cliente = reservaActual.getCliente();
            String correoDestino = cliente.getCorreo();

            // Verificar que el cliente tenga un correo válido
            if (correoDestino == null || correoDestino.trim().isEmpty()) {
                mostrarAlerta("Error", "El cliente no tiene un correo electrónico registrado", AlertType.ERROR);
                return;
            }

            // Crear asunto y cuerpo del correo
            String asunto = "Voucher de Reserva - Agencia Turística";
            String contenidoHtml = "<h2>Voucher de Reserva</h2>" +
                    "<p>Estimado/a " + cliente.getNombre() + " " + cliente.getApellido() + ",</p>" +
                    "<p>Adjunto encontrará el voucher de su reserva.</p>" +
                    "<pre>" + voucher + "</pre>" +
                    "<p>Gracias por elegir nuestros servicios.</p>" +
                    "<p>Atentamente,<br>Agencia Turística</p>";

            // Enviar correo
            EmailSender.enviarEmailReserva(correoDestino, asunto, contenidoHtml);

            mostrarAlerta("Éxito", "Voucher enviado exitosamente al correo: " + correoDestino, AlertType.INFORMATION);

        } catch (MessagingException e) {
            mostrarAlerta("Error", "Error al enviar el voucher por correo: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para enviar notificación de creación/modificación de reserva
     */
    private void enviarNotificacionReserva(Reserva reserva, boolean esCreacion) {
        try {
            Cliente cliente = reserva.getCliente();
            String correoDestino = cliente.getCorreo();

            // Verificar que el cliente tenga un correo válido
            if (correoDestino == null || correoDestino.trim().isEmpty()) {
                return; // No enviar correo si no hay dirección
            }

            // Crear asunto y cuerpo del correo
            String accion = esCreacion ? "Creación" : "Modificación";
            String asunto = accion + " de Reserva - Agencia Turística";

            StringBuilder contenido = new StringBuilder();
            contenido.append("<h2>").append(accion).append(" de Reserva</h2>");
            contenido.append("<p>Estimado/a ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append(",</p>");

            if (esCreacion) {
                contenido.append("<p>Queremos informarle que su reserva ha sido creada exitosamente.</p>");
            } else {
                contenido.append("<p>Queremos informarle que su reserva ha sido modificada exitosamente.</p>");
            }

            contenido.append("<p>Detalles de la reserva:</p>");
            contenido.append("<ul>");
            contenido.append("<li>Código de reserva: ").append(reserva.getId()).append("</li>");
            contenido.append("<li>Estado: ").append(reserva.getEstadoReserva()).append("</li>");
            contenido.append("<li>Fecha de ingreso: ").append(reserva.getFechaInicio().format(dateFormatter)).append("</li>");
            contenido.append("<li>Fecha de salida: ").append(reserva.getFechaFin().format(dateFormatter)).append("</li>");

            if (reserva.getPaqueteTuristico() != null) {
                contenido.append("<li>Paquete: ").append(reserva.getPaqueteTuristico().getNombre()).append("</li>");
            }

            contenido.append("<li>Habitaciones: ").append(reserva.getHabitaciones().size()).append("</li>");
            contenido.append("<li>Precio total: $").append(String.format("%.2f", reserva.getPrecioTotal())).append("</li>");
            contenido.append("<li>Forma de pago: ").append(reserva.getFormaPago()).append("</li>");
            contenido.append("</ul>");

            if (reserva.getEstadoReserva() == EstadoReserva.PENDIENTE) {
                contenido.append("<p>Su reserva está actualmente en estado <strong>PENDIENTE</strong>. ");
                contenido.append("Pronto uno de nuestros agentes se comunicará con usted para confirmarla.</p>");
            }

            contenido.append("<p>Si tiene alguna duda o necesita realizar cambios, por favor contáctenos.</p>");
            contenido.append("<p>Atentamente,<br>Agencia Turística</p>");

            // Enviar correo
            EmailSender.enviarEmailReserva(correoDestino, asunto, contenido.toString());

        } catch (MessagingException e) {
            // Solo registrar el error, no mostrar alerta para no interrumpir el flujo principal
            System.err.println("Error al enviar notificación por correo: " + e.getMessage());
        }
    }

    /**
     * Método para enviar notificación de confirmación de reserva
     */
    private void enviarNotificacionConfirmacion(Reserva reserva) {
        try {
            Cliente cliente = reserva.getCliente();
            String correoDestino = cliente.getCorreo();

            // Verificar que el cliente tenga un correo válido
            if (correoDestino == null || correoDestino.trim().isEmpty()) {
                return; // No enviar correo si no hay dirección
            }

            // Crear asunto y cuerpo del correo
            String asunto = "Confirmación de Reserva - Agencia Turística";

            StringBuilder contenido = new StringBuilder();
            contenido.append("<h2>Confirmación de Reserva</h2>");
            contenido.append("<p>Estimado/a ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append(",</p>");
            contenido.append("<p>Nos complace informarle que su reserva ha sido <strong>CONFIRMADA</strong>.</p>");

            contenido.append("<p>Detalles de la reserva:</p>");
            contenido.append("<ul>");
            contenido.append("<li>Código de reserva: ").append(reserva.getId()).append("</li>");
            contenido.append("<li>Fecha de ingreso: ").append(reserva.getFechaInicio().format(dateFormatter)).append("</li>");
            contenido.append("<li>Fecha de salida: ").append(reserva.getFechaFin().format(dateFormatter)).append("</li>");

            if (reserva.getPaqueteTuristico() != null) {
                contenido.append("<li>Paquete: ").append(reserva.getPaqueteTuristico().getNombre()).append("</li>");
            }

            contenido.append("<li>Habitaciones: ").append(reserva.getHabitaciones().size()).append("</li>");
            contenido.append("<li>Precio total: $").append(String.format("%.2f", reserva.getPrecioTotal())).append("</li>");
            contenido.append("<li>Forma de pago: ").append(reserva.getFormaPago()).append("</li>");
            contenido.append("</ul>");

            contenido.append("<p>Recuerde que:</p>");
            contenido.append("<ul>");
            contenido.append("<li>El check-in se realiza a partir de las 15:00 horas.</li>");
            contenido.append("<li>El check-out debe realizarse antes de las 12:00 horas.</li>");
            contenido.append("<li>Es necesario presentar un documento de identidad al momento del check-in.</li>");
            contenido.append("</ul>");

            contenido.append("<p>Puede solicitar el voucher de su reserva en cualquier momento.</p>");
            contenido.append("<p>¡Esperamos que disfrute su estancia con nosotros!</p>");
            contenido.append("<p>Atentamente,<br>Agencia Turística</p>");

            // Enviar correo
            EmailSender.enviarEmailReserva(correoDestino, asunto, contenido.toString());

        } catch (MessagingException e) {
            // Solo registrar el error, no mostrar alerta para no interrumpir el flujo principal
            System.err.println("Error al enviar notificación de confirmación por correo: " + e.getMessage());
        }
    }

    /**
     * Método para enviar notificación de cancelación de reserva
     */
    private void enviarNotificacionCancelacion(Reserva reserva) {
        try {
            Cliente cliente = reserva.getCliente();
            String correoDestino = cliente.getCorreo();

            // Verificar que el cliente tenga un correo válido
            if (correoDestino == null || correoDestino.trim().isEmpty()) {
                return; // No enviar correo si no hay dirección
            }

            // Crear asunto y cuerpo del correo
            String asunto = "Cancelación de Reserva - Agencia Turística";

            StringBuilder contenido = new StringBuilder();
            contenido.append("<h2>Cancelación de Reserva</h2>");
            contenido.append("<p>Estimado/a ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append(",</p>");
            contenido.append("<p>Le informamos que su reserva con código <strong>").append(reserva.getId()).append("</strong> ha sido cancelada.</p>");

            contenido.append("<p>Detalles de la reserva cancelada:</p>");
            contenido.append("<ul>");
            contenido.append("<li>Fecha de ingreso: ").append(reserva.getFechaInicio().format(dateFormatter)).append("</li>");
            contenido.append("<li>Fecha de salida: ").append(reserva.getFechaFin().format(dateFormatter)).append("</li>");

            if (reserva.getPaqueteTuristico() != null) {
                contenido.append("<li>Paquete: ").append(reserva.getPaqueteTuristico().getNombre()).append("</li>");
            }

            contenido.append("<li>Precio total: $").append(String.format("%.2f", reserva.getPrecioTotal())).append("</li>");
            contenido.append("</ul>");

            contenido.append("<p>Si tiene alguna duda o desea realizar una nueva reserva, no dude en contactarnos.</p>");
            contenido.append("<p>Atentamente,<br>Agencia Turística</p>");

            // Enviar correo
            EmailSender.enviarEmailReserva(correoDestino, asunto, contenido.toString());

        } catch (MessagingException e) {
            // Solo registrar el error, no mostrar alerta para no interrumpir el flujo principal
            System.err.println("Error al enviar notificación de cancelación por correo: " + e.getMessage());
        }
    }

    /**
     * Método para cerrar la ventana
     */
    @FXML
    void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    /**
     * Método para mostrar alertas
     */
    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}