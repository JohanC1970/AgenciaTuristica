package co.edu.uniquindio.agenciaturistica.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.Cursor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Cliente;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Reserva;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class EmpleadoReservasController implements Initializable {

    @FXML
    private TextField txtBusqueda;

    @FXML
    private DatePicker dateFechaInicio;

    @FXML
    private DatePicker dateFechaFin;

    @FXML
    private ComboBox<EstadoReserva> comboEstado;

    @FXML
    private Label lblTotalReservas;

    @FXML
    private TableView<Reserva> tablaReservas;

    @FXML
    private TableColumn<Reserva, String> colId;

    @FXML
    private TableColumn<Reserva, String> colCliente;

    @FXML
    private TableColumn<Reserva, LocalDate> colFechaInicio;

    @FXML
    private TableColumn<Reserva, LocalDate> colFechaFin;

    @FXML
    private TableColumn<Reserva, String> colPrecioTotal;

    @FXML
    private TableColumn<Reserva, EstadoReserva> colEstado;

    @FXML
    private TableColumn<Reserva, String> colAcciones;

    @FXML
    private Label lblMensaje;

    private Aplicacion aplicacion;
    private List<Reserva> listaReservas;
    private ObservableList<Reserva> reservasObservable = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas de la tabla
        configurarTabla();

        // Configurar combo de estados
        configurarComboEstados();
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
            // Cargar lista de reservas
            listaReservas = ModelFactoryController.getInstance().getSistema().obtenerReservas();
            cargarReservas(listaReservas);

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos de reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cargar las reservas en la tabla
     * @param reservas Lista de reservas a mostrar
     */
    private void cargarReservas(List<Reserva> reservas) {
        reservasObservable.clear();

        if (reservas != null) {
            reservasObservable.addAll(reservas);
        }

        // Actualizar tabla
        tablaReservas.setItems(reservasObservable);

        // Actualizar contador
        lblTotalReservas.setText(String.valueOf(reservasObservable.size()));

        // Limpiar mensaje
        lblMensaje.setText("");
    }

    /**
     * Método para configurar la tabla de reservas
     */
    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue().getCliente();
            if (cliente != null) {
                return new SimpleStringProperty(cliente.getNombre() + " " + cliente.getApellido());
            }
            return new SimpleStringProperty("No disponible");
        });

        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaInicio.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colFechaFin.setCellFactory(column -> new TableCell<Reserva, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        colPrecioTotal.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("$%,.0f", cellData.getValue().getPrecioTotal())));

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoReserva"));
        colEstado.setCellFactory(column -> new TableCell<Reserva, EstadoReserva>() {
            @Override
            protected void updateItem(EstadoReserva item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case PENDIENTE:
                            setStyle("-fx-text-fill: #e67e22;");
                            break;
                        case CONFIRMADA:
                            setStyle("-fx-text-fill: #2ecc71;");
                            break;
                        case CANCELADA:
                            setStyle("-fx-text-fill: #e74c3c;");
                            break;
                        case COMPLETADA:
                            setStyle("-fx-text-fill: #3498db;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());
    }

    /**
     * Método para configurar el combo de estados
     */
    private void configurarComboEstados() {
        // Crear lista con todos los estados y una opción para "Todos"
        ObservableList<EstadoReserva> estados = FXCollections.observableArrayList(EstadoReserva.values());

        // Configurar el combo
        comboEstado.setItems(estados);
        comboEstado.setPromptText("Todos los estados");
    }

    /**
     * Método para crear la columna de botones de acciones
     * @return Factory para crear celdas con botones
     */
    private Callback<TableColumn<Reserva, String>, TableCell<Reserva, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Reserva, String>, TableCell<Reserva, String>>() {
            @Override
            public TableCell<Reserva, String> call(TableColumn<Reserva, String> param) {
                return new TableCell<Reserva, String>() {
                    private final Button btnVer = new Button("Ver");
                    private final Button btnEditar = new Button("Editar");
                    private final HBox hbox = new HBox(5, btnVer, btnEditar);

                    {
                        // Configurar botón Ver
                        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnVer.setCursor(Cursor.HAND);
                        btnVer.setOnAction(event -> {
                            Reserva reserva = getTableView().getItems().get(getIndex());
                            verDetallesReserva(reserva);
                        });

                        // Configurar botón Editar
                        btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                        btnEditar.setCursor(Cursor.HAND);
                        btnEditar.setOnAction(event -> {
                            Reserva reserva = getTableView().getItems().get(getIndex());
                            editarReserva(reserva);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Reserva reserva = getTableView().getItems().get(getIndex());

                            // Desactivar edición para reservas completadas o canceladas
                            btnEditar.setDisable(reserva.getEstadoReserva() == EstadoReserva.COMPLETADA ||
                                    reserva.getEstadoReserva() == EstadoReserva.CANCELADA);

                            setGraphic(hbox);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para buscar reservas por ID o cliente
     * @param event
     */
    @FXML
    void buscarReserva(ActionEvent event) {
        String textoBusqueda = txtBusqueda.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            cargarReservas(listaReservas);
            return;
        }

        try {
            // Intentar buscar por ID exacto
            if (textoBusqueda.matches("[A-Za-z0-9-]+")) {
                Respuesta<Reserva> respuesta = ModelFactoryController.getInstance().getSistema()
                        .buscarReservaPorId(textoBusqueda);

                if (respuesta.isExito()) {
                    cargarReservas(List.of(respuesta.getData()));
                    return;
                }
            }

            // Si no encuentra por ID, buscar por cliente (nombre, apellido o identificación)
            List<Reserva> reservasFiltradas = ModelFactoryController.getInstance().getSistema()
                    .buscarReservasPorCliente(textoBusqueda);

            cargarReservas(reservasFiltradas);

            if (reservasFiltradas.isEmpty()) {
                lblMensaje.setText("No se encontraron reservas con ese criterio de búsqueda");
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para filtrar reservas por fecha
     * @param event
     */
    @FXML
    void filtrarPorFechas(ActionEvent event) {
        LocalDate fechaInicio = dateFechaInicio.getValue();
        LocalDate fechaFin = dateFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            mostrarAlerta("Información", "Por favor, seleccione ambas fechas para filtrar", AlertType.INFORMATION);
            return;
        }

        if (fechaFin.isBefore(fechaInicio)) {
            mostrarAlerta("Error", "La fecha de fin debe ser posterior a la fecha de inicio", AlertType.ERROR);
            return;
        }

        try {
            List<Reserva> reservasFiltradas = ModelFactoryController.getInstance().getSistema()
                    .buscarReservasPorFechas(fechaInicio, fechaFin);

            cargarReservas(reservasFiltradas);

            if (reservasFiltradas.isEmpty()) {
                lblMensaje.setText("No se encontraron reservas en ese rango de fechas");
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al filtrar reservas por fechas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para filtrar reservas por estado
     * @param event
     */
    @FXML
    void filtrarPorEstado(ActionEvent event) {
        EstadoReserva estado = comboEstado.getValue();

        if (estado == null) {
            cargarReservas(listaReservas);
            return;
        }

        try {
            List<Reserva> reservasFiltradas = ModelFactoryController.getInstance().getSistema()
                    .buscarReservasPorEstado(estado);

            cargarReservas(reservasFiltradas);

            if (reservasFiltradas.isEmpty()) {
                lblMensaje.setText("No se encontraron reservas con el estado " + estado);
            }

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al filtrar reservas por estado: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para filtrar reservas por un estado específico (llamado desde otra clase)
     * @param estado Estado de las reservas a filtrar
     */
    public void filtrarPorEstado(EstadoReserva estado) {
        comboEstado.setValue(estado);
        filtrarPorEstado(new ActionEvent());
    }

    /**
     * Método para exportar reservas a Excel
     * @param event
     */
    @FXML
    void exportarReservas(ActionEvent event) {
        try {
            // Crear libro de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reservas");

            // Crear estilo para encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Cliente", "Fecha Inicio", "Fecha Fin", "Precio Total", "Estado", "Forma Pago"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Llenar datos
            int rowNum = 1;

            for (Reserva reserva : reservasObservable) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(reserva.getId());

                if (reserva.getCliente() != null) {
                    row.createCell(1).setCellValue(reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido());
                } else {
                    row.createCell(1).setCellValue("No disponible");
                }

                if (reserva.getFechaInicio() != null) {
                    row.createCell(2).setCellValue(reserva.getFechaInicio().format(formatter));
                } else {
                    row.createCell(2).setCellValue("");
                }

                if (reserva.getFechaFin() != null) {
                    row.createCell(3).setCellValue(reserva.getFechaFin().format(formatter));
                } else {
                    row.createCell(3).setCellValue("");
                }

                row.createCell(4).setCellValue(reserva.getPrecioTotal());

                if (reserva.getEstadoReserva() != null) {
                    row.createCell(5).setCellValue(reserva.getEstadoReserva().toString());
                } else {
                    row.createCell(5).setCellValue("");
                }

                if (reserva.getFormaPago() != null) {
                    row.createCell(6).setCellValue(reserva.getFormaPago().toString());
                } else {
                    row.createCell(6).setCellValue("");
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Lista de Reservas");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
            fileChooser.setInitialFileName("Reservas.xlsx");

            File file = fileChooser.showSaveDialog(tablaReservas.getScene().getWindow());

            if (file != null) {
                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();

                mostrarAlerta("Éxito", "El archivo se ha guardado correctamente", AlertType.INFORMATION);
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al exportar reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para generar un reporte de reservas
     * @param event
     */
    @FXML
    void generarReporteReservas(ActionEvent event) {
        try {
            // Obtener fechas para el reporte
            LocalDate fechaInicio = dateFechaInicio.getValue();
            LocalDate fechaFin = dateFechaFin.getValue();

            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta("Información", "Por favor, seleccione el rango de fechas para generar el reporte", AlertType.INFORMATION);
                return;
            }

            if (fechaFin.isBefore(fechaInicio)) {
                mostrarAlerta("Error", "La fecha de fin debe ser posterior a la fecha de inicio", AlertType.ERROR);
                return;
            }

            // Cargar la vista del generador de reportes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GenerarReporte.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
//            GenerarReporteController controller = loader.getController();
//            controller.setAplicacion(aplicacion);
//            controller.setFechas(fechaInicio, fechaFin);
//            controller.inicializarDatos();

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Generar Reporte de Reservas");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir el generador de reportes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para crear una nueva reserva
     * @param event
     */
    @FXML
    void nuevaReserva(ActionEvent event) {
        try {
            // Cargar la vista de nueva reserva
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/NuevaReserva2.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            NuevaReservaController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Nueva Reserva");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos después de cerrar la ventana
            inicializarDatos();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir formulario de nueva reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para ver los detalles de una reserva
     * @param reserva Reserva a visualizar
     */
    private void verDetallesReserva(Reserva reserva) {
        try {
            // Cargar la vista de detalles de reserva
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/DetallesReserva.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            DetallesReservaController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setModoVisualizacion(true);
            controller.inicializarDatos(reserva);

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Detalles de Reserva");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos por si hubo cambios (por ejemplo, cambio de estado)
            inicializarDatos();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir detalles de reserva: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para editar una reserva existente
     * @param reserva Reserva a editar
     */
    private void editarReserva(Reserva reserva) {
        // No permitir editar reservas completadas o canceladas
        if (reserva.getEstadoReserva() == EstadoReserva.COMPLETADA ||
                reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
            mostrarAlerta("Información", "No se pueden editar reservas completadas o canceladas", AlertType.INFORMATION);
            return;
        }

        try {
            // Cargar la vista de edición de reserva
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/NuevaReserva2.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
//            NuevaReservaController controller = loader.getController();
//            controller.setAplicacion(aplicacion);
//            controller.setReserva(reserva);
//            controller.setModoEdicion(true);
//            controller.inicializarDatos(reserva);

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Editar Reserva");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos después de cerrar la ventana
            inicializarDatos();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir formulario de edición de reserva: " + e.getMessage(), AlertType.ERROR);
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