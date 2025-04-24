package co.edu.uniquindio.agenciaturistica.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.*;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Enums.TipoReporte;
import co.edu.uniquindio.agenciaturistica.util.EmailSender;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;

public class GestionReservasController implements Initializable {

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnCrearReserva;

    @FXML
    private Button btnGenerarReporteOcupacion;

    @FXML
    private Button btnGenerarReporteVentas;

    @FXML
    private Button btnVolver;

    @FXML
    private TableColumn<Reserva, String> colAcciones;

    @FXML
    private TableColumn<Reserva, String> colAccionesCancelada;

    @FXML
    private TableColumn<Reserva, String> colAccionesCompletada;

    @FXML
    private TableColumn<Reserva, String> colAccionesConfirmada;

    @FXML
    private TableColumn<Reserva, String> colAccionesPendiente;

    @FXML
    private TableColumn<Reserva, String> colCliente;

    @FXML
    private TableColumn<Reserva, String> colClienteCancelada;

    @FXML
    private TableColumn<Reserva, String> colClienteCompletada;

    @FXML
    private TableColumn<Reserva, String> colClienteConfirmada;

    @FXML
    private TableColumn<Reserva, String> colClientePendiente;

    @FXML
    private TableColumn<Reserva, String> colEstado;

    @FXML
    private TableColumn<Reserva, String> colFechaFin;

    @FXML
    private TableColumn<Reserva, String> colFechaFinCancelada;

    @FXML
    private TableColumn<Reserva, String> colFechaFinCompletada;

    @FXML
    private TableColumn<Reserva, String> colFechaFinConfirmada;

    @FXML
    private TableColumn<Reserva, String> colFechaFinPendiente;

    @FXML
    private TableColumn<Reserva, String> colFechaInicio;

    @FXML
    private TableColumn<Reserva, String> colFechaInicioCancelada;

    @FXML
    private TableColumn<Reserva, String> colFechaInicioCompletada;

    @FXML
    private TableColumn<Reserva, String> colFechaInicioConfirmada;

    @FXML
    private TableColumn<Reserva, String> colFechaInicioPendiente;

    @FXML
    private TableColumn<Reserva, String> colFormaPago;

    @FXML
    private TableColumn<Reserva, String> colFormaPagoCancelada;

    @FXML
    private TableColumn<Reserva, String> colFormaPagoCompletada;

    @FXML
    private TableColumn<Reserva, String> colFormaPagoConfirmada;

    @FXML
    private TableColumn<Reserva, String> colFormaPagoPendiente;

    @FXML
    private TableColumn<Reserva, String> colId;

    @FXML
    private TableColumn<Reserva, String> colIdCancelada;

    @FXML
    private TableColumn<Reserva, String> colIdCompletada;

    @FXML
    private TableColumn<Reserva, String> colIdConfirmada;

    @FXML
    private TableColumn<Reserva, String> colIdPendiente;

    @FXML
    private TableColumn<Reserva, String> colPaquete;

    @FXML
    private TableColumn<Reserva, String> colPaqueteCancelada;

    @FXML
    private TableColumn<Reserva, String> colPaqueteCompletada;

    @FXML
    private TableColumn<Reserva, String> colPaqueteConfirmada;

    @FXML
    private TableColumn<Reserva, String> colPaquetePendiente;

    @FXML
    private TableColumn<Reserva, Double> colPrecio;

    @FXML
    private TableColumn<Reserva, Double> colPrecioCancelada;

    @FXML
    private TableColumn<Reserva, Double> colPrecioCompletada;

    @FXML
    private TableColumn<Reserva, Double> colPrecioConfirmada;

    @FXML
    private TableColumn<Reserva, Double> colPrecioPendiente;

    @FXML
    private ComboBox<String> comboFiltro;

    @FXML
    private DatePicker dateBusquedaFin;

    @FXML
    private DatePicker dateBusquedaInicio;

    @FXML
    private Label lblTotalReservas;

    @FXML
    private TabPane tabPaneReservas;

    @FXML
    private TableView<Reserva> tablaReservas;

    @FXML
    private TableView<Reserva> tablaReservasCanceladas;

    @FXML
    private TableView<Reserva> tablaReservasCompletadas;

    @FXML
    private TableView<Reserva> tablaReservasConfirmadas;

    @FXML
    private TableView<Reserva> tablaReservasPendientes;

    @FXML
    private TextField txtBuscar;

    private Aplicacion aplicacion;
    private ObservableList<Reserva> listaReservas = FXCollections.observableArrayList();
    private ObservableList<Reserva> listaReservasPendientes = FXCollections.observableArrayList();
    private ObservableList<Reserva> listaReservasConfirmadas = FXCollections.observableArrayList();
    private ObservableList<Reserva> listaReservasCompletadas = FXCollections.observableArrayList();
    private ObservableList<Reserva> listaReservasCanceladas = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar filtros de búsqueda
        comboFiltro.setItems(FXCollections.observableArrayList(
                "ID Reserva", "Cliente", "Paquete", "Estado", "Fechas"
        ));
        comboFiltro.setValue("ID Reserva");

        // Configurar listener para cambio de filtro
        comboFiltro.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals("Fechas")) {
                dateBusquedaInicio.setDisable(false);
                dateBusquedaFin.setDisable(false);
                txtBuscar.setDisable(true);
                txtBuscar.clear();
            } else {
                dateBusquedaInicio.setDisable(true);
                dateBusquedaFin.setDisable(true);
                txtBuscar.setDisable(false);
            }
        });

        // Inicialmente, deshabilitar los datepickers
        dateBusquedaInicio.setDisable(true);
        dateBusquedaFin.setDisable(true);

        // Configurar tablas
        configurarTablas();
    }

    /**
     * Método para configurar todas las tablas de reservas
     */
    private void configurarTablas() {
        // Configurar tabla principal
        configurarTablaReservas(tablaReservas, colId, colCliente, colPaquete, colFechaInicio,
                colFechaFin, colPrecio, colEstado, colFormaPago, colAcciones);

        // Configurar tabla de pendientes
        configurarTablaReservas(tablaReservasPendientes, colIdPendiente, colClientePendiente,
                colPaquetePendiente, colFechaInicioPendiente, colFechaFinPendiente,
                colPrecioPendiente, null, colFormaPagoPendiente, colAccionesPendiente);

        // Configurar tabla de confirmadas
        configurarTablaReservas(tablaReservasConfirmadas, colIdConfirmada, colClienteConfirmada,
                colPaqueteConfirmada, colFechaInicioConfirmada, colFechaFinConfirmada,
                colPrecioConfirmada, null, colFormaPagoConfirmada, colAccionesConfirmada);

        // Configurar tabla de completadas
        configurarTablaReservas(tablaReservasCompletadas, colIdCompletada, colClienteCompletada,
                colPaqueteCompletada, colFechaInicioCompletada, colFechaFinCompletada,
                colPrecioCompletada, null, colFormaPagoCompletada, colAccionesCompletada);

        // Configurar tabla de canceladas
        configurarTablaReservas(tablaReservasCanceladas, colIdCancelada, colClienteCancelada,
                colPaqueteCancelada, colFechaInicioCancelada, colFechaFinCancelada,
                colPrecioCancelada, null, colFormaPagoCancelada, colAccionesCancelada);

        // Asignar listas a las tablas
        tablaReservas.setItems(listaReservas);
        tablaReservasPendientes.setItems(listaReservasPendientes);
        tablaReservasConfirmadas.setItems(listaReservasConfirmadas);
        tablaReservasCompletadas.setItems(listaReservasCompletadas);
        tablaReservasCanceladas.setItems(listaReservasCanceladas);
    }

    /**
     * Método para configurar la visibilidad de los campos de búsqueda según el filtro seleccionado
     */
    private void configurarVisibilidadBusqueda() {
        String filtroSeleccionado = comboFiltro.getValue();
        boolean mostrarFechas = "Fecha".equals(filtroSeleccionado);
        boolean mostrarTexto = !mostrarFechas;

        txtBuscar.setVisible(mostrarTexto);
        txtBuscar.setManaged(mostrarTexto);
        dateBusquedaInicio.setVisible(mostrarFechas);
        dateBusquedaInicio.setManaged(mostrarFechas);
        dateBusquedaFin.setVisible(mostrarFechas);
        dateBusquedaFin.setManaged(mostrarFechas);

        // Ajustar el placeholder según el filtro
        if (mostrarTexto) {
            txtBuscar.setPromptText("Buscar por " + filtroSeleccionado.toLowerCase() + "...");
        }
    }


    /**
     * Método para configurar una tabla de reservas específica
     */
    private void configurarTablaReservas(TableView<Reserva> tabla, TableColumn<Reserva, String> colId,
                                         TableColumn<Reserva, String> colCliente, TableColumn<Reserva, String> colPaquete,
                                         TableColumn<Reserva, String> colFechaInicio, TableColumn<Reserva, String> colFechaFin,
                                         TableColumn<Reserva, Double> colPrecio, TableColumn<Reserva, String> colEstado,
                                         TableColumn<Reserva, String> colFormaPago, TableColumn<Reserva, String> colAcciones) {

        // Configurar columnas de datos
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        colCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue().getCliente();
            return new SimpleStringProperty(cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : "");
        });

        colPaquete.setCellValueFactory(cellData -> {
            PaqueteTuristico paquete = cellData.getValue().getPaqueteTuristico();
            return new SimpleStringProperty(paquete != null ? paquete.getNombre() : "Sin paquete");
        });

        colFechaInicio.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaInicio();
            return new SimpleStringProperty(fecha != null ? fecha.format(dateFormatter) : "");
        });

        colFechaFin.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaFin();
            return new SimpleStringProperty(fecha != null ? fecha.format(dateFormatter) : "");
        });

        colPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecioTotal()).asObject());

        // Formatear precio como moneda
        colPrecio.setCellFactory(column -> {
            return new TableCell<Reserva, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%,.0f", item));
                    }
                }
            };
        });

        // Columna de estado (sólo para la tabla principal)
        if (colEstado != null) {
            colEstado.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEstadoReserva().toString()));

            // Formatear estado con colores
            colEstado.setCellFactory(column -> {
                return new TableCell<Reserva, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            switch (item) {
                                case "PENDIENTE":
                                    setStyle("-fx-text-fill: #f39c12;");
                                    break;
                                case "CONFIRMADA":
                                    setStyle("-fx-text-fill: #3498db;");
                                    break;
                                case "COMPLETADA":
                                    setStyle("-fx-text-fill: #2ecc71;");
                                    break;
                                case "CANCELADA":
                                    setStyle("-fx-text-fill: #e74c3c;");
                                    break;
                                default:
                                    setStyle("");
                            }
                        }
                    }
                };
            });
        }

        colFormaPago.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormaPago().toString()));

        // Botones de acciones
        colAcciones.setCellFactory(crearBotonesAcciones(tabla));
    }

    /**
     * Método para configurar las columnas de la tabla principal con columna de estado
     */
    private void configurarColumnasPrincipales(TableColumn<Reserva, String> colId, TableColumn<Reserva, String> colCliente,
                                               TableColumn<Reserva, String> colPaquete, TableColumn<Reserva, LocalDate> colFechaInicio,
                                               TableColumn<Reserva, LocalDate> colFechaFin, TableColumn<Reserva, Double> colPrecio,
                                               TableColumn<Reserva, String> colEstado, TableColumn<Reserva, String> colFormaPago,
                                               TableColumn<Reserva, String> colAcciones) {

        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        colCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue().getCliente();
            return new SimpleStringProperty(cliente.getNombre() + " " + cliente.getApellido());
        });

        colPaquete.setCellValueFactory(cellData -> {
            PaqueteTuristico paquete = cellData.getValue().getPaqueteTuristico();
            return new SimpleStringProperty(paquete != null ? paquete.getNombre() : "Sin paquete");
        });

        colFechaInicio.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFechaInicio()));
        colFechaInicio.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });

        colFechaFin.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFechaFin()));
        colFechaFin.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });

        colPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecioTotal()).asObject());
        colPrecio.setCellFactory(column -> new TableCell<Reserva, Double>() {
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

        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstadoReserva().toString()));
        colEstado.setCellFactory(column -> new TableCell<Reserva, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    switch (estado) {
                        case "PENDIENTE":
                            setStyle("-fx-text-fill: #f39c12;");
                            break;
                        case "CONFIRMADA":
                            setStyle("-fx-text-fill: #2ecc71;");
                            break;
                        case "COMPLETADA":
                            setStyle("-fx-text-fill: #3498db;");
                            break;
                        case "CANCELADA":
                            setStyle("-fx-text-fill: #e74c3c;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        colFormaPago.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormaPago().toString()));

        colAcciones.setCellFactory(crearBotonesAcciones(false));
    }

    /**
     * Método para configurar las columnas de las tablas secundarias
     */
    private void configurarColumnas(TableColumn<Reserva, String> colId, TableColumn<Reserva, String> colCliente,
                                    TableColumn<Reserva, String> colPaquete, TableColumn<Reserva, LocalDate> colFechaInicio,
                                    TableColumn<Reserva, LocalDate> colFechaFin, TableColumn<Reserva, Double> colPrecio,
                                    TableColumn<Reserva, String> colFormaPago, TableColumn<Reserva, String> colAcciones) {

        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        colCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue().getCliente();
            return new SimpleStringProperty(cliente.getNombre() + " " + cliente.getApellido());
        });

        colPaquete.setCellValueFactory(cellData -> {
            PaqueteTuristico paquete = cellData.getValue().getPaqueteTuristico();
            return new SimpleStringProperty(paquete != null ? paquete.getNombre() : "Sin paquete");
        });

        colFechaInicio.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFechaInicio()));
        colFechaInicio.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });

        colFechaFin.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFechaFin()));
        colFechaFin.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });

        colPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecioTotal()).asObject());
        colPrecio.setCellFactory(column -> new TableCell<Reserva, Double>() {
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

        colFormaPago.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormaPago().toString()));

        // Determinar si se deben mostrar botones limitados según el estado
        boolean botonesLimitados =
                (colAcciones == colAccionesCompletada || colAcciones == colAccionesCancelada);

        colAcciones.setCellFactory(crearBotonesAcciones(botonesLimitados));
    }


    /**
     * Método para crear los botones de acciones (ver, editar, cancelar, etc.)
     */
    private Callback<TableColumn<Reserva, String>, TableCell<Reserva, String>> crearBotonesAcciones(TableView<Reserva> tabla) {
        return new Callback<TableColumn<Reserva, String>, TableCell<Reserva, String>>() {
            @Override
            public TableCell<Reserva, String> call(final TableColumn<Reserva, String> param) {
                return new TableCell<Reserva, String>() {
                    private final Button btnVer = new Button("Ver");
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnConfirmar = new Button("Confirmar");
                    private final Button btnCompletar = new Button("Completar");
                    private final Button btnCancelar = new Button("Cancelar");
                    private final HBox pane = new HBox(5);

                    {
                        // Estilo de botones
                        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                        btnConfirmar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                        btnCompletar.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
                        btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        // Configurar acciones de botones
                        btnVer.setOnAction(event -> {
                            Reserva reserva = getTableView().getItems().get(getIndex());
                            verDetallesReserva(reserva);
                        });

                        btnEditar.setOnAction(event -> {
                            Reserva reserva = getTableView().getItems().get(getIndex());
                            editarReserva(reserva);
                        });

                        btnConfirmar.setOnAction(event -> {
                            Reserva reserva = getTableView().getItems().get(getIndex());
                            confirmarReserva(reserva);
                        });

                        btnCompletar.setOnAction(event -> {
                            Reserva reserva = getTableView().getItems().get(getIndex());
                            completarReserva(reserva);
                        });

                        btnCancelar.setOnAction(event -> {
                            Reserva reserva = getTableView().getItems().get(getIndex());
                            cancelarReserva(reserva);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            pane.getChildren().clear();

                            // Siempre mostrar botón de ver detalles
                            pane.getChildren().add(btnVer);

                            Reserva reserva = getTableView().getItems().get(getIndex());

                            // Mostrar botones según el estado de la reserva
                            if (reserva.getEstadoReserva() == EstadoReserva.PENDIENTE) {
                                pane.getChildren().addAll(btnEditar, btnConfirmar, btnCancelar);
                            } else if (reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA) {
                                pane.getChildren().addAll(btnEditar, btnCompletar, btnCancelar);
                            } else if (reserva.getEstadoReserva() == EstadoReserva.COMPLETADA ||
                                    reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
                                // No mostrar acciones adicionales para reservas completadas o canceladas
                            }

                            setGraphic(pane);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para establecer la aplicación principal
     *
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;

        // Cargar reservas
        cargarReservas();
    }

    /**
     * Método para cargar las reservas desde la base de datos
     */
    private void cargarReservas() {
        try {
            // Obtener todas las reservas
            List<Reserva> reservas = ModelFactoryController.getInstance().getSistema().obtenerReservas();

            // Limpiar listas observables
            listaReservas.clear();
            listaReservasPendientes.clear();
            listaReservasConfirmadas.clear();
            listaReservasCompletadas.clear();
            listaReservasCanceladas.clear();

            // Clasificar reservas según su estado
            for (Reserva reserva : reservas) {
                listaReservas.add(reserva);

                switch (reserva.getEstadoReserva()) {
                    case PENDIENTE:
                        listaReservasPendientes.add(reserva);
                        break;
                    case CONFIRMADA:
                        listaReservasConfirmadas.add(reserva);
                        break;
                    case COMPLETADA:
                        listaReservasCompletadas.add(reserva);
                        break;
                    case CANCELADA:
                        listaReservasCanceladas.add(reserva);
                        break;
                }
            }

            // Actualizar contador
            actualizarContador();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar las reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el contador de reservas
     */
    private void actualizarContador() {
        lblTotalReservas.setText("Total de reservas: " + listaReservas.size());
    }

    /**
     * Método para buscar reservas según filtros
     * @param event
     */
    @FXML
    void buscarReservas(ActionEvent event) {
        try {
            String filtro = comboFiltro.getValue();
            String textoBusqueda = txtBuscar.getText().trim();
            List<Reserva> reservasFiltradas = null;

            switch (filtro) {
                case "ID Reserva":
                    if (!textoBusqueda.isEmpty()) {
                        Respuesta<Reserva> respuesta = ModelFactoryController.getInstance()
                                .getSistema().buscarReservaPorId(textoBusqueda);
                        if (respuesta.isExito()) {
                            reservasFiltradas = List.of(respuesta.getData());
                        } else {
                            mostrarAlerta("Información", respuesta.getMensaje(), AlertType.INFORMATION);
                            return;
                        }
                    }
                    break;

                case "Cliente":
                    if (!textoBusqueda.isEmpty()) {
                        reservasFiltradas = ModelFactoryController.getInstance()
                                .getSistema().buscarReservasPorCliente(textoBusqueda);
                    }
                    break;

                case "Paquete":
                    // Buscar por nombre del paquete (se realiza manualmente)
                    if (!textoBusqueda.isEmpty()) {
                        ObservableList<Reserva> reservasFiltro = FXCollections.observableArrayList();
                        for (Reserva reserva : listaReservas) {
                            if (reserva.getPaqueteTuristico() != null &&
                                    reserva.getPaqueteTuristico().getNombre().toLowerCase()
                                            .contains(textoBusqueda.toLowerCase())) {
                                reservasFiltro.add(reserva);
                            }
                        }
                        reservasFiltradas = reservasFiltro;
                    }
                    break;

                case "Estado":
                    if (!textoBusqueda.isEmpty()) {
                        try {
                            EstadoReserva estado = EstadoReserva.valueOf(textoBusqueda.toUpperCase());
                            reservasFiltradas = ModelFactoryController.getInstance()
                                    .getSistema().buscarReservasPorEstado(estado);
                        } catch (IllegalArgumentException e) {
                            mostrarAlerta("Error", "Estado inválido. Valores permitidos: PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA",
                                    AlertType.ERROR);
                            return;
                        }
                    }
                    break;

                case "Fechas":
                    LocalDate fechaInicio = dateBusquedaInicio.getValue();
                    LocalDate fechaFin = dateBusquedaFin.getValue();

                    if (fechaInicio == null || fechaFin == null) {
                        mostrarAlerta("Error", "Debe seleccionar ambas fechas para realizar la búsqueda", AlertType.ERROR);
                        return;
                    }

                    if (fechaInicio.isAfter(fechaFin)) {
                        mostrarAlerta("Error", "La fecha de inicio debe ser anterior a la fecha de fin", AlertType.ERROR);
                        return;
                    }

                    reservasFiltradas = ModelFactoryController.getInstance()
                            .getSistema().buscarReservasPorFechas(fechaInicio, fechaFin);
                    break;
            }

            // Si no se encontraron resultados o no se aplicó ningún filtro, recargar todas las reservas
            if (reservasFiltradas == null ||
                    (textoBusqueda.isEmpty() && filtro != "Fechas") ||
                    (filtro == "Fechas" && dateBusquedaInicio.getValue() == null && dateBusquedaFin.getValue() == null)) {
                cargarReservas();
                mostrarAlerta("Información", "Se muestran todas las reservas", AlertType.INFORMATION);
                return;
            }

            // Actualizar las listas con los resultados filtrados
            actualizarListasConFiltro(reservasFiltradas);

            // Actualizar contador
            lblTotalReservas.setText("Resultados: " + reservasFiltradas.size());

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar las listas con los resultados filtrados
     *
     * @param reservasFiltradas Lista de reservas filtradas
     */
    private void actualizarListasConFiltro(List<Reserva> reservasFiltradas) {
        // Limpiar listas observables
        listaReservas.clear();
        listaReservasPendientes.clear();
        listaReservasConfirmadas.clear();
        listaReservasCompletadas.clear();
        listaReservasCanceladas.clear();

        // Clasificar reservas según su estado
        for (Reserva reserva : reservasFiltradas) {
            listaReservas.add(reserva);

            switch (reserva.getEstadoReserva()) {
                case PENDIENTE:
                    listaReservasPendientes.add(reserva);
                    break;
                case CONFIRMADA:
                    listaReservasConfirmadas.add(reserva);
                    break;
                case COMPLETADA:
                    listaReservasCompletadas.add(reserva);
                    break;
                case CANCELADA:
                    listaReservasCanceladas.add(reserva);
                    break;
            }
        }
    }

    /**
     * Método para mostrar el formulario de creación de reservas
     *
     * @param event
     */
    @FXML
    void mostrarFormularioCrear(ActionEvent event) {
        try {
            // Obtener el usuario actual (empleado)
            Empleado empleadoActual = obtenerEmpleadoActual();

            // Cargar la pantalla de detalles de reserva
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/DetallesReserva.fxml")
            );
            Parent root = loader.load();

            // Configurar el controlador
            DetallesReservaController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setEmpleado(empleadoActual);
            controller.inicializarNuevaReserva();

            // Mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Nueva Reserva");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar reservas al cerrar la ventana
            cargarReservas();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir el formulario de creación de reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private Empleado obtenerEmpleadoActual() {
        return aplicacion.getEmpleadoActual();
    }

    /**
     * Método para ver los detalles de una reserva
     *
     * @param reserva Reserva a ver
     */
    private void verDetallesReserva(Reserva reserva) {
        try {
            // Obtener el usuario actual (empleado)
            Empleado empleadoActual = obtenerEmpleadoActual();

            // Cargar la pantalla de detalles de reserva
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/DetallesReserva.fxml")
            );
            Parent root = loader.load();

            // Configurar el controlador
            DetallesReservaController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setEmpleado(empleadoActual);
            controller.cargarReserva(reserva, true); // modo lectura

            // Mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Detalles de Reserva - " + reserva.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir los detalles de la reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para editar una reserva existente
     *
     * @param reserva Reserva a editar
     */
    private void editarReserva(Reserva reserva) {
        try {
            // Verificar si la reserva se puede editar
            if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA ||
                    reserva.getEstadoReserva() == EstadoReserva.COMPLETADA) {
                mostrarAlerta("Aviso", "No se puede editar una reserva " +
                        reserva.getEstadoReserva().toString().toLowerCase(), AlertType.WARNING);
                return;
            }

            // Obtener el usuario actual (empleado)
            Empleado empleadoActual = obtenerEmpleadoActual();

            // Cargar la pantalla de detalles de reserva
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/DetallesReserva.fxml")
            );
            Parent root = loader.load();

            // Configurar el controlador
            DetallesReservaController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setEmpleado(empleadoActual);
            controller.cargarReserva(reserva, false); // modo edición

            // Mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Editar Reserva - " + reserva.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar reservas al cerrar la ventana
            cargarReservas();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir el formulario de edición de reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para confirmar una reserva
     *
     * @param reserva Reserva a confirmar
     */
    private void confirmarReserva(Reserva reserva) {
        // Verificar si la reserva está en estado PENDIENTE
        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE) {
            mostrarAlerta("Aviso", "Solo se pueden confirmar reservas en estado PENDIENTE", AlertType.WARNING);
            return;
        }

        // Confirmar la acción
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Reserva");
        alert.setHeaderText("¿Está seguro de confirmar esta reserva?");
        alert.setContentText("ID Reserva: " + reserva.getId() + "\n" +
                "Cliente: " + reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido());

        Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            // Confirmar la reserva
            Respuesta<Reserva> respuesta = ModelFactoryController.getInstance()
                    .getSistema().confirmarReserva(reserva.getId());

            if (respuesta.isExito()) {
                mostrarAlerta("Éxito", respuesta.getMensaje(), AlertType.INFORMATION);

                // Recargar reservas
                cargarReservas();
            } else {
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        }

    }

    /**
     * Método para completar una reserva
     *
     * @param reserva Reserva a completar
     */
    private void completarReserva(Reserva reserva) {
        try {
            // Verificar si la reserva está en estado CONFIRMADA
            if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
                mostrarAlerta("Aviso", "Solo se pueden completar reservas en estado CONFIRMADA", AlertType.WARNING);
                return;
            }

            // Confirmar la acción
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Completar Reserva");
            alert.setHeaderText("¿Está seguro de marcar esta reserva como completada?");
            alert.setContentText("ID Reserva: " + reserva.getId() + "\n" +
                    "Cliente: " + reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido());

            Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                // Completar la reserva
                Respuesta<Reserva> respuesta = ModelFactoryController.getInstance()
                        .getSistema().completarReserva(reserva.getId());

                if (respuesta.isExito()) {
                    mostrarAlerta("Éxito", respuesta.getMensaje(), AlertType.INFORMATION);

                    // Recargar reservas
                    cargarReservas();
                } else {
                    mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
                }
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al completar la reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cancelar una reserva
     *
     * @param reserva Reserva a cancelar
     */
    private void cancelarReserva(Reserva reserva) {
        try {
            // Verificar si la reserva no está ya cancelada o completada
            if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
                mostrarAlerta("Aviso", "La reserva ya está cancelada", AlertType.WARNING);
                return;
            }

            if (reserva.getEstadoReserva() == EstadoReserva.COMPLETADA) {
                mostrarAlerta("Aviso", "No se puede cancelar una reserva completada", AlertType.WARNING);
                return;
            }

            // Confirmar la acción
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Cancelar Reserva");
            alert.setHeaderText("¿Está seguro de cancelar esta reserva?");
            alert.setContentText("ID Reserva: " + reserva.getId() + "\n" +
                    "Cliente: " + reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido() + "\n\n" +
                    "Esta acción liberará los recursos reservados (habitaciones, cupos de paquete).");

            Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                // Cancelar la reserva
                Respuesta<Reserva> respuesta = ModelFactoryController.getInstance()
                        .getSistema().cancelarReserva(reserva.getId());

                if (respuesta.isExito()) {
                    mostrarAlerta("Éxito", respuesta.getMensaje(), AlertType.INFORMATION);

                    // Recargar reservas
                    cargarReservas();
                } else {
                    mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
                }
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cancelar la reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para notificar al cliente sobre la cancelación de su reserva
     * @param reserva
     */
    private void enviarNotificacionCancelacion(Reserva reserva) {
        try {
            // Obtener el correo del cliente
            String correoCliente = reserva.getCliente().getCorreo();

            // Crear asunto y cuerpo del correo
            String asunto = "Cancelación de Reserva - Agencia Turística";
            String contenido = "<h2>Cancelación de Reserva</h2>" +
                    "<p>Estimado/a " + reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido() + ",</p>" +
                    "<p>Le informamos que su reserva con ID <strong>" + reserva.getId() + "</strong> ha sido cancelada.</p>" +
                    "<p>Detalles de la reserva:</p>" +
                    "<ul>" +
                    "<li>Fecha de inicio: " + reserva.getFechaInicio().format(dateFormatter) + "</li>" +
                    "<li>Fecha de fin: " + reserva.getFechaFin().format(dateFormatter) + "</li>";

            if (reserva.getPaqueteTuristico() != null) {
                contenido += "<li>Paquete: " + reserva.getPaqueteTuristico().getNombre() + "</li>";
            }

            contenido += "<li>Precio total: $" + String.format("%.2f", reserva.getPrecioTotal()) + "</li>" +
                    "</ul>" +
                    "<p>Si tiene alguna duda, por favor contáctenos.</p>" +
                    "<p>Atentamente,<br>Agencia Turística</p>";

            // Enviar correo
            EmailSender.enviarEmail(correoCliente, asunto, contenido, null);

        } catch (MessagingException | IOException e) {
            mostrarAlerta("Advertencia", "La reserva se canceló correctamente, pero no se pudo enviar la notificación por correo: " +
                    e.getMessage(), AlertType.WARNING);
        }
    }

    /**
     * Método para generar un reporte de ventas
     *
     * @param event
     */
    @FXML
    void generarReporteVentas(ActionEvent event) {
        try {
            // Solicitar rango de fechas
            Dialog<LocalDate[]> dialog = new Dialog<>();
            dialog.setTitle("Generar Reporte de Ventas");
            dialog.setHeaderText("Seleccione el rango de fechas para el reporte");

            // Configurar botones
            dialog.getDialogPane().getButtonTypes().addAll(
                    javafx.scene.control.ButtonType.OK,
                    javafx.scene.control.ButtonType.CANCEL);

            // Crear layout para datepickers
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            DatePicker startDate = new DatePicker(LocalDate.now().minusMonths(1));
            DatePicker endDate = new DatePicker(LocalDate.now());

            grid.add(new Label("Fecha Inicio:"), 0, 0);
            grid.add(startDate, 1, 0);
            grid.add(new Label("Fecha Fin:"), 0, 1);
            grid.add(endDate, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Convertir el resultado
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == javafx.scene.control.ButtonType.OK) {
                    return new LocalDate[]{startDate.getValue(), endDate.getValue()};
                }
                return null;
            });

            Optional<LocalDate[]> result = dialog.showAndWait();

            if (result.isPresent()) {
                LocalDate fechaInicio = result.get()[0];
                LocalDate fechaFin = result.get()[1];

                // Validar fechas
                if (fechaInicio.isAfter(fechaFin)) {
                    mostrarAlerta("Error", "La fecha de inicio debe ser anterior a la fecha de fin", AlertType.ERROR);
                    return;
                }

                // Generar reporte de ventas
                List<Reserva> reservasReporte = ModelFactoryController.getInstance()
                        .getSistema().generarReporteVentas(fechaInicio, fechaFin);

                double totalVentas = ModelFactoryController.getInstance()
                        .getSistema().calcularTotalVentas(fechaInicio, fechaFin);

                // Crear reporte
                Reporte reporte = new Reporte();
                reporte.setTitulo("Reporte de Ventas");
                reporte.setDescripcion("Ventas del " + fechaInicio.format(dateFormatter) +
                        " al " + fechaFin.format(dateFormatter));
                reporte.setFechaGeneracion(LocalDateTime.now());
                reporte.setTipoReporte(TipoReporte.VENTAS_DIARIAS);

                // Generar contenido del reporte
                StringBuilder contenido = new StringBuilder();
                contenido.append("# REPORTE DE VENTAS\n\n");
                contenido.append("Período: ").append(fechaInicio.format(dateFormatter))
                        .append(" - ").append(fechaFin.format(dateFormatter)).append("\n\n");
                contenido.append("Total de Ventas: $").append(String.format("%,.0f", totalVentas)).append("\n\n");
                contenido.append("Cantidad de Reservas: ").append(reservasReporte.size()).append("\n\n");
                contenido.append("## DETALLE DE RESERVAS\n\n");

                if (reservasReporte.isEmpty()) {
                    contenido.append("No se encontraron reservas en el período seleccionado.\n");
                } else {
                    contenido.append("| ID Reserva | Cliente | Fecha Inicio | Fecha Fin | Estado | Precio Total |\n");
                    contenido.append("|------------|---------|--------------|-----------|--------|-------------|\n");

                    for (Reserva r : reservasReporte) {
                        contenido.append("| ").append(r.getId()).append(" | ")
                                .append(r.getCliente().getNombre()).append(" ").append(r.getCliente().getApellido()).append(" | ")
                                .append(r.getFechaInicio().format(dateFormatter)).append(" | ")
                                .append(r.getFechaFin().format(dateFormatter)).append(" | ")
                                .append(r.getEstadoReserva()).append(" | ")
                                .append("$").append(String.format("%,.0f", r.getPrecioTotal())).append(" |\n");
                    }
                }

                reporte.setContenido(contenido.toString());

                // Guardar reporte (simulado)
                mostrarReporte(reporte);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al generar el reporte de ventas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para generar reporte de ocupación de habitaciones
     */
    @FXML
    void generarReporteOcupacion(ActionEvent event) {
        try {
            // Abrir diálogo para seleccionar fechas
            Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
            dialog.setTitle("Generar Reporte de Ocupación");
            dialog.setHeaderText("Seleccione el rango de fechas para el reporte");

            ButtonType generateButtonType = new ButtonType("Generar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            DatePicker startDate = new DatePicker(LocalDate.now().withDayOfMonth(1)); // Primer día del mes actual
            DatePicker endDate = new DatePicker(LocalDate.now());

            grid.add(new Label("Fecha de inicio:"), 0, 0);
            grid.add(startDate, 1, 0);
            grid.add(new Label("Fecha de fin:"), 0, 1);
            grid.add(endDate, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Convertir resultado del diálogo
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == generateButtonType) {
                    return new Pair<>(startDate.getValue(), endDate.getValue());
                }
                return null;
            });

            Optional<Pair<LocalDate, LocalDate>> result = dialog.showAndWait();

            result.ifPresent(dates -> {
                LocalDate fechaInicio = dates.getKey();
                LocalDate fechaFin = dates.getValue();

                try {
                    // Generar reporte
                    List<Habitacion> habitacionesOcupadas = ModelFactoryController.getInstance().getSistema().generarReporteOcupacion(fechaInicio, fechaFin);

                    // Crear instancia de reporte
                    Reporte reporte = new Reporte();
                    reporte.setTitulo("Reporte de Ocupación de Habitaciones");
                    reporte.setDescripcion("Ocupación del " + fechaInicio.format(dateFormatter) + " al " + fechaFin.format(dateFormatter));
                    reporte.setFechaGeneracion(LocalDateTime.now());
                    reporte.setTipoReporte(TipoReporte.OCUPACION_HABITACIONES);

                    // Generar contenido del reporte
                    StringBuilder contenido = new StringBuilder();
                    contenido.append("# REPORTE DE OCUPACIÓN DE HABITACIONES\n\n");
                    contenido.append("Periodo: ").append(fechaInicio.format(dateFormatter)).append(" - ").append(fechaFin.format(dateFormatter)).append("\n\n");
                    contenido.append("Total de habitaciones ocupadas: ").append(habitacionesOcupadas.size()).append("\n\n");
                    contenido.append("## DETALLES DE OCUPACIÓN\n\n");
                    contenido.append("| ID Habitación | Tipo | Capacidad | Precio por Noche | Disponible |\n");
                    contenido.append("|--------------|------|-----------|------------------|------------|\n");

                    for (Habitacion habitacion : habitacionesOcupadas) {
                        contenido.append("| ").append(habitacion.getId()).append(" | ")
                                .append(habitacion.getTipoHabitacion().getNombre()).append(" | ")
                                .append(habitacion.getCapacidad()).append(" | ")
                                .append("$").append(String.format("%.2f", habitacion.getPrecioPorNoche())).append(" | ")
                                .append(habitacion.isDisponible() ? "Sí" : "No").append(" |\n");
                    }

                    reporte.setContenido(contenido.toString());

                    // Mostrar reporte
                    mostrarReporte(reporte);

                } catch (SQLException e) {
                    mostrarAlerta("Error", "Error al generar el reporte de ocupación: " + e.getMessage(), AlertType.ERROR);
                }
            });

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir el diálogo de reporte: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para mostrar un reporte generado
     */
    private void mostrarReporte(Reporte reporte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/VisualizadorReporte.fxml"));
            Parent root = loader.load();

            VisualizadorReporteController controller = loader.getController();
            controller.setReporte(reporte);

            Stage stage = new Stage();
            stage.setTitle(reporte.getTitulo());
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            mostrarAlerta("Error", "Error al mostrar el reporte: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para volver a la pantalla anterior
     */
    @FXML
    void volver(ActionEvent event) {
        try {
            // Volver a la pantalla anterior depende del rol del usuario actual
            String fxmlPath;

            switch (aplicacion.getUsuarioActual().getRol()) {
                case ADMINISTRADOR:
                    fxmlPath = "/co/edu/uniquindio/agenciaturistica/Administrador.fxml";
                    break;
                case EMPLEADO:
                    fxmlPath = "/co/edu/uniquindio/agenciaturistica/Empleado.fxml";
                    break;
                default:
                    mostrarAlerta("Error", "Rol no autorizado para esta operación", AlertType.ERROR);
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Llamar al método correcto según el controlador
            if (controller instanceof AdministradorController) {
                ((AdministradorController) controller).setAplicacion(aplicacion);
                ((AdministradorController) controller).inicializarInformacion();
            } else if (controller instanceof EmpleadoController) {
                ((EmpleadoController) controller).setAplicacion(aplicacion);
                ((EmpleadoController) controller).inicializarInformacion();
            }

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            mostrarAlerta("Error", "Error al volver a la pantalla anterior: " + e.getMessage(), AlertType.ERROR);
        }
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

