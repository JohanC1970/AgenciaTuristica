package co.edu.uniquindio.agenciaturistica.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Cliente;
import co.edu.uniquindio.agenciaturistica.model.Habitacion;
import co.edu.uniquindio.agenciaturistica.model.Hospedaje;
import co.edu.uniquindio.agenciaturistica.model.PaqueteTuristico;
import co.edu.uniquindio.agenciaturistica.model.Reserva;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

public class EmpleadoReportesController implements Initializable {

    @FXML
    private DatePicker dateFechaInicio;

    @FXML
    private DatePicker dateFechaFin;

    @FXML
    private Label lblTotalReservas;

    @FXML
    private Label lblIngresosTotales;

    @FXML
    private Label lblPaqueteMasVendido;

    @FXML
    private Label lblHospedajeMasReservado;

    @FXML
    private Label lblClienteMasFrecuente;

    @FXML
    private PieChart chartVentasPorTipo;

    @FXML
    private DatePicker dateOcupacionInicio;

    @FXML
    private DatePicker dateOcupacionFin;

    @FXML
    private BarChart<String, Number> chartOcupacionPorHospedaje;

    @FXML
    private TableView<HabitacionOcupacion> tablaHabitacionesOcupadas;

    @FXML
    private TableColumn<HabitacionOcupacion, String> colHospedaje;

    @FXML
    private TableColumn<HabitacionOcupacion, String> colTipoHabitacion;

    @FXML
    private TableColumn<HabitacionOcupacion, Integer> colCapacidad;

    @FXML
    private TableColumn<HabitacionOcupacion, Double> colTasaOcupacion;

    @FXML
    private ComboBox<String> comboClientesPeriodo;

    @FXML
    private TableView<ClienteFrecuente> tablaClientesFrecuentes;

    @FXML
    private TableColumn<ClienteFrecuente, String> colCliente;

    @FXML
    private TableColumn<ClienteFrecuente, Integer> colReservas;

    @FXML
    private TableColumn<ClienteFrecuente, Double> colValorTotal;

    @FXML
    private PieChart chartTendenciasCompra;

    private Aplicacion aplicacion;
    private List<Reserva> listaReservas;
    private List<Habitacion> listaHabitaciones;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Listas observables para las tablas
    private ObservableList<HabitacionOcupacion> habitacionesOcupacionObservable = FXCollections.observableArrayList();
    private ObservableList<ClienteFrecuente> clientesFrecuentesObservable = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar las fechas por defecto (último mes)
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        dateFechaInicio.setValue(inicioMes);
        dateFechaFin.setValue(hoy);

        dateOcupacionInicio.setValue(inicioMes);
        dateOcupacionFin.setValue(hoy);

        // Configurar combo de períodos para reporte de clientes
        comboClientesPeriodo.getItems().addAll(
                "Último mes",
                "Últimos 3 meses",
                "Último año",
                "Todo el tiempo"
        );
        comboClientesPeriodo.setValue("Último mes");

        // Configurar columnas de tablas
        configurarTablaHabitacionesOcupadas();
        configurarTablaClientesFrecuentes();
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

            // Generar reportes iniciales
            generarReporteVentas(null);
            generarReporteOcupacion(null);
            generarReporteClientes(null);

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para configurar la tabla de habitaciones ocupadas
     */
    private void configurarTablaHabitacionesOcupadas() {
        colHospedaje.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombreHospedaje()));

        colTipoHabitacion.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipoHabitacion()));

        colCapacidad.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCapacidad()).asObject());

        colTasaOcupacion.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getTasaOcupacion()).asObject());

        colTasaOcupacion.setCellFactory(column -> new TableCell<HabitacionOcupacion, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", item));
                    // Colorear según nivel de ocupación
                    if (item > 80) {
                        setStyle("-fx-text-fill: #2ecc71;"); // Verde - ocupación alta
                    } else if (item > 50) {
                        setStyle("-fx-text-fill: #f39c12;"); // Naranja - ocupación media
                    } else {
                        setStyle("-fx-text-fill: #e74c3c;"); // Rojo - ocupación baja
                    }
                }
            }
        });
    }

    /**
     * Método para configurar la tabla de clientes frecuentes
     */
    private void configurarTablaClientesFrecuentes() {
        colCliente.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombreCompleto()));

        colReservas.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCantidadReservas()).asObject());

        colValorTotal.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getValorTotal()).asObject());

        colValorTotal.setCellFactory(column -> new TableCell<ClienteFrecuente, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.0f", item));
                }
            }
        });
    }

    /**
     * Método para generar el reporte de ventas
     * @param event
     */
    @FXML
    void generarReporteVentas(ActionEvent event) {
        LocalDate fechaInicio = dateFechaInicio.getValue();
        LocalDate fechaFin = dateFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            mostrarAlerta("Información", "Por favor, seleccione ambas fechas para generar el reporte", AlertType.INFORMATION);
            return;
        }

        if (fechaFin.isBefore(fechaInicio)) {
            mostrarAlerta("Error", "La fecha de fin debe ser posterior a la fecha de inicio", AlertType.ERROR);
            return;
        }

        try {
            // Obtener reservas en el rango de fechas
            List<Reserva> reservasFiltradas = listaReservas.stream()
                    .filter(r -> r.getFechaInicio() != null &&
                            (r.getFechaInicio().isEqual(fechaInicio) || r.getFechaInicio().isAfter(fechaInicio)) &&
                            (r.getFechaInicio().isEqual(fechaFin) || r.getFechaInicio().isBefore(fechaFin)))
                    .collect(Collectors.toList());

            // Actualizar indicadores
            actualizarIndicadoresVentas(reservasFiltradas);

            // Actualizar gráfico de ventas por tipo
            actualizarGraficoVentasPorTipo(reservasFiltradas);

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al generar reporte de ventas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar los indicadores del reporte de ventas
     * @param reservas Lista de reservas en el período
     */
    private void actualizarIndicadoresVentas(List<Reserva> reservas) {
        // Total de reservas
        lblTotalReservas.setText(String.valueOf(reservas.size()));

        // Ingresos totales
        double ingresosTotales = reservas.stream()
                .mapToDouble(Reserva::getPrecioTotal)
                .sum();
        lblIngresosTotales.setText(String.format("$%,.0f", ingresosTotales));

        // Paquete más vendido
        Map<String, Integer> paquetesPorFrecuencia = new HashMap<>();
        for (Reserva reserva : reservas) {
            if (reserva.getPaqueteTuristico() != null) {
                String nombrePaquete = reserva.getPaqueteTuristico().getNombre();
                paquetesPorFrecuencia.put(nombrePaquete,
                        paquetesPorFrecuencia.getOrDefault(nombrePaquete, 0) + 1);
            }
        }

        String paqueteMasVendido = "No disponible";
        int maxFrecuenciaPaquete = 0;
        for (Map.Entry<String, Integer> entry : paquetesPorFrecuencia.entrySet()) {
            if (entry.getValue() > maxFrecuenciaPaquete) {
                maxFrecuenciaPaquete = entry.getValue();
                paqueteMasVendido = entry.getKey();
            }
        }

        lblPaqueteMasVendido.setText(paqueteMasVendido);

        // Hospedaje más reservado
        Map<String, Integer> hospedajesPorFrecuencia = new HashMap<>();
        for (Reserva reserva : reservas) {
            if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
                for (Habitacion habitacion : reserva.getHabitaciones()) {
                    if (habitacion != null && habitacion.getTipoHabitacion() != null) {
                        // Nota: Aquí se asume que se podría obtener el hospedaje desde la habitación
                        // En una implementación real, esto dependerá de la estructura de datos
                        String nombreHospedaje = "Hospedaje " + habitacion.getId(); // Simplificado
                        hospedajesPorFrecuencia.put(nombreHospedaje,
                                hospedajesPorFrecuencia.getOrDefault(nombreHospedaje, 0) + 1);
                    }
                }
            }
        }

        String hospedajeMasReservado = "No disponible";
        int maxFrecuenciaHospedaje = 0;
        for (Map.Entry<String, Integer> entry : hospedajesPorFrecuencia.entrySet()) {
            if (entry.getValue() > maxFrecuenciaHospedaje) {
                maxFrecuenciaHospedaje = entry.getValue();
                hospedajeMasReservado = entry.getKey();
            }
        }

        lblHospedajeMasReservado.setText(hospedajeMasReservado);

        // Cliente más frecuente
        Map<String, Integer> clientesPorFrecuencia = new HashMap<>();
        for (Reserva reserva : reservas) {
            if (reserva.getCliente() != null) {
                String nombreCliente = reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido();
                clientesPorFrecuencia.put(nombreCliente,
                        clientesPorFrecuencia.getOrDefault(nombreCliente, 0) + 1);
            }
        }

        String clienteMasFrecuente = "No disponible";
        int maxFrecuenciaCliente = 0;
        for (Map.Entry<String, Integer> entry : clientesPorFrecuencia.entrySet()) {
            if (entry.getValue() > maxFrecuenciaCliente) {
                maxFrecuenciaCliente = entry.getValue();
                clienteMasFrecuente = entry.getKey();
            }
        }

        lblClienteMasFrecuente.setText(clienteMasFrecuente);
    }

    /**
     * Método para actualizar el gráfico de ventas por tipo de paquete
     * @param reservas Lista de reservas en el período
     */
    private void actualizarGraficoVentasPorTipo(List<Reserva> reservas) {
        // Crear mapa para contar reservas por tipo de paquete
        Map<String, Integer> reservasPorTipoPaquete = new HashMap<>();

        for (Reserva reserva : reservas) {
            PaqueteTuristico paquete = reserva.getPaqueteTuristico();
            if (paquete != null) {
                String tipoPaquete = paquete.getNombre();
                reservasPorTipoPaquete.put(tipoPaquete,
                        reservasPorTipoPaquete.getOrDefault(tipoPaquete, 0) + 1);
            }
        }

        // Crear datos para el gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : reservasPorTipoPaquete.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        // Si no hay datos, agregar un valor por defecto
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Sin datos", 1));
        }

        // Asignar datos al gráfico
        chartVentasPorTipo.setData(pieChartData);
    }

    /**
     * Método para exportar el reporte de ventas a Excel
     * @param event
     */
    @FXML
    void exportarReporteVentas(ActionEvent event) {
        // Obtener fechas del reporte
        LocalDate fechaInicio = dateFechaInicio.getValue();
        LocalDate fechaFin = dateFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            mostrarAlerta("Información", "Por favor, seleccione ambas fechas para exportar el reporte", AlertType.INFORMATION);
            return;
        }

        try {
            // Crear libro de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reporte de Ventas");

            // Crear estilo para encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Crear fila de título con fechas
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Ventas - Período: " + fechaInicio.format(formatter) +
                    " al " + fechaFin.format(formatter));
            titleCell.setCellStyle(headerStyle);

            // Crear fila con información resumida
            Row summaryRow1 = sheet.createRow(2);
            summaryRow1.createCell(0).setCellValue("Total Reservas:");
            summaryRow1.createCell(1).setCellValue(lblTotalReservas.getText());

            Row summaryRow2 = sheet.createRow(3);
            summaryRow2.createCell(0).setCellValue("Ingresos Totales:");
            summaryRow2.createCell(1).setCellValue(lblIngresosTotales.getText());

            Row summaryRow3 = sheet.createRow(4);
            summaryRow3.createCell(0).setCellValue("Paquete más vendido:");
            summaryRow3.createCell(1).setCellValue(lblPaqueteMasVendido.getText());

            Row summaryRow4 = sheet.createRow(5);
            summaryRow4.createCell(0).setCellValue("Hospedaje más reservado:");
            summaryRow4.createCell(1).setCellValue(lblHospedajeMasReservado.getText());

            Row summaryRow5 = sheet.createRow(6);
            summaryRow5.createCell(0).setCellValue("Cliente más frecuente:");
            summaryRow5.createCell(1).setCellValue(lblClienteMasFrecuente.getText());

            // Crear encabezados para tabla de reservas
            Row headerRow = sheet.createRow(8);
            String[] headers = {"ID", "Cliente", "Paquete", "Fecha Inicio", "Fecha Fin", "Precio Total", "Estado"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Obtener reservas en el rango de fechas
            List<Reserva> reservasFiltradas = listaReservas.stream()
                    .filter(r -> r.getFechaInicio() != null &&
                            (r.getFechaInicio().isEqual(fechaInicio) || r.getFechaInicio().isAfter(fechaInicio)) &&
                            (r.getFechaInicio().isEqual(fechaFin) || r.getFechaInicio().isBefore(fechaFin)))
                    .collect(Collectors.toList());

            // Llenar datos de reservas
            int rowNum = 9;
            for (Reserva reserva : reservasFiltradas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(reserva.getId());

                if (reserva.getCliente() != null) {
                    row.createCell(1).setCellValue(reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido());
                } else {
                    row.createCell(1).setCellValue("No disponible");
                }

                if (reserva.getPaqueteTuristico() != null) {
                    row.createCell(2).setCellValue(reserva.getPaqueteTuristico().getNombre());
                } else {
                    row.createCell(2).setCellValue("No disponible");
                }

                if (reserva.getFechaInicio() != null) {
                    row.createCell(3).setCellValue(reserva.getFechaInicio().format(formatter));
                } else {
                    row.createCell(3).setCellValue("");
                }

                if (reserva.getFechaFin() != null) {
                    row.createCell(4).setCellValue(reserva.getFechaFin().format(formatter));
                } else {
                    row.createCell(4).setCellValue("");
                }

                row.createCell(5).setCellValue(reserva.getPrecioTotal());

                if (reserva.getEstadoReserva() != null) {
                    row.createCell(6).setCellValue(reserva.getEstadoReserva().toString());
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
            fileChooser.setTitle("Guardar Reporte de Ventas");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
            fileChooser.setInitialFileName("ReporteVentas.xlsx");

            File file = fileChooser.showSaveDialog(dateFechaInicio.getScene().getWindow());

            if (file != null) {
                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();

                mostrarAlerta("Éxito", "El archivo se ha guardado correctamente", AlertType.INFORMATION);
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al exportar reporte de ventas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para imprimir el reporte de ventas
     * @param event
     */
    @FXML
    void imprimirReporteVentas(ActionEvent event) {
        // Esta funcionalidad podría implementarse utilizando JavaFX PrinterJob
        // Por simplicidad, mostramos un mensaje de información
        mostrarAlerta("Información", "Funcionalidad de impresión no implementada", AlertType.INFORMATION);
    }

    /**
     * Método para generar el reporte de ocupación
     * @param event
     */
    @FXML
    void generarReporteOcupacion(ActionEvent event) {
        LocalDate fechaInicio = dateOcupacionInicio.getValue();
        LocalDate fechaFin = dateOcupacionFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            mostrarAlerta("Información", "Por favor, seleccione ambas fechas para generar el reporte", AlertType.INFORMATION);
            return;
        }

        if (fechaFin.isBefore(fechaInicio)) {
            mostrarAlerta("Error", "La fecha de fin debe ser posterior a la fecha de inicio", AlertType.ERROR);
            return;
        }

        try {
            // Obtener datos de ocupación
            List<Habitacion> habitacionesOcupadas = ModelFactoryController.getInstance().getSistema()
                    .generarReporteOcupacion(fechaInicio, fechaFin);

            // Actualizar gráfico de ocupación por hospedaje
            actualizarGraficoOcupacionPorHospedaje(habitacionesOcupadas);

            // Actualizar tabla de habitaciones ocupadas
            actualizarTablaHabitacionesOcupadas(habitacionesOcupadas);

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al generar reporte de ocupación: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el gráfico de ocupación por hospedaje
     * @param habitaciones Lista de habitaciones ocupadas
     */
    private void actualizarGraficoOcupacionPorHospedaje(List<Habitacion> habitaciones) {
        // Limpiar gráfico anterior
        chartOcupacionPorHospedaje.getData().clear();

        // Crear mapa para contar ocupación por hospedaje
        Map<String, Double> ocupacionPorHospedaje = new HashMap<>();
        Map<String, Integer> totalHabitacionesPorHospedaje = new HashMap<>();

        for (Habitacion habitacion : habitaciones) {
            // En un escenario real, se podría obtener el hospedaje desde la habitación
            // Aquí usamos un valor simulado para el ejemplo
            String nombreHospedaje = "Hospedaje " + (habitacion.getId() % 5); // Simplificado

            ocupacionPorHospedaje.put(nombreHospedaje,
                    ocupacionPorHospedaje.getOrDefault(nombreHospedaje, 0.0) +
                            (habitacion.isDisponible() ? 0 : 1));

            totalHabitacionesPorHospedaje.put(nombreHospedaje,
                    totalHabitacionesPorHospedaje.getOrDefault(nombreHospedaje, 0) + 1);
        }

        // Calcular porcentajes de ocupación
        Map<String, Double> porcentajeOcupacion = new HashMap<>();
        for (String hospedaje : ocupacionPorHospedaje.keySet()) {
            double ocupadas = ocupacionPorHospedaje.get(hospedaje);
            double total = totalHabitacionesPorHospedaje.get(hospedaje);
            double porcentaje = (ocupadas / total) * 100;
            porcentajeOcupacion.put(hospedaje, porcentaje);
        }

        // Crear serie de datos para el gráfico
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Porcentaje de Ocupación");

        for (Map.Entry<String, Double> entry : porcentajeOcupacion.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Agregar serie al gráfico
        chartOcupacionPorHospedaje.getData().add(series);
    }

    /**
     * Método para actualizar la tabla de habitaciones ocupadas
     * @param habitaciones Lista de habitaciones ocupadas
     */
    private void actualizarTablaHabitacionesOcupadas(List<Habitacion> habitaciones) {
        habitacionesOcupacionObservable.clear();

        // Agrupar habitaciones por tipo y hospedaje
        Map<String, Map<String, Integer>> ocupadasPorTipoYHospedaje = new HashMap<>();
        Map<String, Map<String, Integer>> totalesPorTipoYHospedaje = new HashMap<>();

        for (Habitacion habitacion : habitaciones) {
            // Obtener hospedaje y tipo de habitación (simplificado para el ejemplo)
            String hospedaje = "Hospedaje " + (habitacion.getId() % 5);
            String tipoHabitacion = habitacion.getTipoHabitacion() != null ?
                    habitacion.getTipoHabitacion().getNombre() : "Estándar";

            // Inicializar mapas si es necesario
            if (!ocupadasPorTipoYHospedaje.containsKey(hospedaje)) {
                ocupadasPorTipoYHospedaje.put(hospedaje, new HashMap<>());
            }
            if (!totalesPorTipoYHospedaje.containsKey(hospedaje)) {
                totalesPorTipoYHospedaje.put(hospedaje, new HashMap<>());
            }

            // Incrementar contadores
            Map<String, Integer> ocupadasPorTipo = ocupadasPorTipoYHospedaje.get(hospedaje);
            Map<String, Integer> totalesPorTipo = totalesPorTipoYHospedaje.get(hospedaje);

            ocupadasPorTipo.put(tipoHabitacion,
                    ocupadasPorTipo.getOrDefault(tipoHabitacion, 0) +
                            (habitacion.isDisponible() ? 0 : 1));

            totalesPorTipo.put(tipoHabitacion,
                    totalesPorTipo.getOrDefault(tipoHabitacion, 0) + 1);
        }

        // Crear objetos para la tabla
        for (String hospedaje : ocupadasPorTipoYHospedaje.keySet()) {
            Map<String, Integer> ocupadasPorTipo = ocupadasPorTipoYHospedaje.get(hospedaje);
            Map<String, Integer> totalesPorTipo = totalesPorTipoYHospedaje.get(hospedaje);

            for (String tipo : ocupadasPorTipo.keySet()) {
                int ocupadas = ocupadasPorTipo.get(tipo);
                int total = totalesPorTipo.get(tipo);
                double tasaOcupacion = (double) ocupadas / total * 100;

                habitacionesOcupacionObservable.add(new HabitacionOcupacion(
                        hospedaje, tipo, total, tasaOcupacion));
            }
        }

        tablaHabitacionesOcupadas.setItems(habitacionesOcupacionObservable);
    }

    /**
     * Método para generar el reporte de clientes
     * @param event
     */
    @FXML
    void generarReporteClientes(ActionEvent event) {
        String periodo = comboClientesPeriodo.getValue();

        if (periodo == null) {
            mostrarAlerta("Información", "Por favor, seleccione un período para generar el reporte", AlertType.INFORMATION);
            return;
        }

        try {
            // Definir fechas según el período seleccionado
            LocalDate fechaFin = LocalDate.now();
            LocalDate fechaInicio;

            switch (periodo) {
                case "Último mes":
                    fechaInicio = fechaFin.minusMonths(1);
                    break;
                case "Últimos 3 meses":
                    fechaInicio = fechaFin.minusMonths(3);
                    break;
                case "Último año":
                    fechaInicio = fechaFin.minusYears(1);
                    break;
                case "Todo el tiempo":
                default:
                    fechaInicio = LocalDate.of(2000, 1, 1); // Fecha pasada muy anterior
                    break;
            }

            // Obtener reservas en el rango de fechas
            List<Reserva> reservasFiltradas = listaReservas.stream()
                    .filter(r -> r.getFechaInicio() != null &&
                            (r.getFechaInicio().isEqual(fechaInicio) || r.getFechaInicio().isAfter(fechaInicio)) &&
                            (r.getFechaInicio().isEqual(fechaFin) || r.getFechaInicio().isBefore(fechaFin)))
                    .collect(Collectors.toList());

            // Actualizar tabla de clientes frecuentes
            actualizarTablaClientesFrecuentes(reservasFiltradas);

            // Actualizar gráfico de tendencias de compra
            actualizarGraficoTendenciasCompra(reservasFiltradas);

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al generar reporte de clientes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar la tabla de clientes frecuentes
     * @param reservas Lista de reservas en el período
     */
    private void actualizarTablaClientesFrecuentes(List<Reserva> reservas) {
        clientesFrecuentesObservable.clear();

        // Crear mapa para agrupar reservas por cliente
        Map<Cliente, List<Reserva>> reservasPorCliente = new HashMap<>();

        for (Reserva reserva : reservas) {
            Cliente cliente = reserva.getCliente();
            if (cliente != null) {
                if (!reservasPorCliente.containsKey(cliente)) {
                    reservasPorCliente.put(cliente, new ArrayList<>());
                }
                reservasPorCliente.get(cliente).add(reserva);
            }
        }

        // Crear objetos ClienteFrecuente para cada cliente
        for (Map.Entry<Cliente, List<Reserva>> entry : reservasPorCliente.entrySet()) {
            Cliente cliente = entry.getKey();
            List<Reserva> reservasCliente = entry.getValue();

            int cantidadReservas = reservasCliente.size();
            double valorTotal = reservasCliente.stream()
                    .mapToDouble(Reserva::getPrecioTotal)
                    .sum();

            clientesFrecuentesObservable.add(new ClienteFrecuente(cliente, cantidadReservas, valorTotal));
        }

        // Ordenar por cantidad de reservas (descendente)
        clientesFrecuentesObservable.sort((c1, c2) -> Integer.compare(c2.getCantidadReservas(), c1.getCantidadReservas()));

        tablaClientesFrecuentes.setItems(clientesFrecuentesObservable);
    }

    /**
     * Método para actualizar el gráfico de tendencias de compra
     * @param reservas Lista de reservas en el período
     */
    private void actualizarGraficoTendenciasCompra(List<Reserva> reservas) {
        // Crear mapa para contar reservas por tipo de paquete
        Map<String, Integer> reservasPorTipoPaquete = new HashMap<>();

        for (Reserva reserva : reservas) {
            PaqueteTuristico paquete = reserva.getPaqueteTuristico();
            if (paquete != null) {
                String tipoPaquete = paquete.getNombre();
                reservasPorTipoPaquete.put(tipoPaquete,
                        reservasPorTipoPaquete.getOrDefault(tipoPaquete, 0) + 1);
            }
        }

        // Crear datos para el gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : reservasPorTipoPaquete.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        // Si no hay datos, agregar un valor por defecto
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Sin datos", 1));
        }

        // Asignar datos al gráfico
        chartTendenciasCompra.setData(pieChartData);
    }

    /**
     * Método para mostrar alertas
     * @param titulo Título de la alerta
     * @param mensaje Mensaje a mostrar
     * @param tipo Tipo de alerta
     */
    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class HabitacionOcupacion {
        private final String nombreHospedaje;
        private final String tipoHabitacion;
        private final int capacidad;
        private final double tasaOcupacion;

        public HabitacionOcupacion(String nombreHospedaje, String tipoHabitacion, int capacidad, double tasaOcupacion) {
            this.nombreHospedaje = nombreHospedaje;
            this.tipoHabitacion = tipoHabitacion;
            this.capacidad = capacidad;
            this.tasaOcupacion = tasaOcupacion;
        }

        public String getNombreHospedaje() {
            return nombreHospedaje;
        }

        public String getTipoHabitacion() {
            return tipoHabitacion;
        }

        public int getCapacidad() {
            return capacidad;
        }

        public double getTasaOcupacion() {
            return tasaOcupacion;
        }
    }

    public static class ClienteFrecuente {
        private Cliente cliente;
        private int cantidadReservas;
        private double valorTotal;

        public ClienteFrecuente(Cliente cliente, int cantidadReservas, double valorTotal) {
            this.cliente = cliente;
            this.cantidadReservas = cantidadReservas;
            this.valorTotal = valorTotal;
        }

        public Cliente getCliente() {
            return cliente;
        }

        public int getCantidadReservas() {
            return cantidadReservas;
        }

        public double getValorTotal() {
            return valorTotal;
        }

        public String getNombreCompleto() {
            return cliente.getNombre() + " " + cliente.getApellido();
        }
    }
}