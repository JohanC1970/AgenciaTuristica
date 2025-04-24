package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Cliente;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Reserva;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EmpleadoDashboardController implements Initializable {

    @FXML
    private Label lblReservasPendientes;

    @FXML
    private Label lblReservasConfirmadas;

    @FXML
    private Label lblClientesRegistrados;

    @FXML
    private BarChart<String, Number> chartReservasPorMes;

    @FXML
    private PieChart chartReservasPorEstado;

    private Aplicacion aplicacion;
    private List<Reserva> listaReservas;
    private List<Cliente> listaClientes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Este método se llama cuando se carga el FXML
    }

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    /**
     * Método para inicializar los datos del dashboard
     */
    public void inicializarDatos() {
        try {
            // Cargar datos de reservas
            cargarReservas();

            // Cargar datos de clientes
            cargarClientes();

            // Inicializar contadores
            actualizarContadores();

            // Inicializar gráficos
            inicializarGraficoReservasPorMes();
            inicializarGraficoReservasPorEstado();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cargar las reservas desde la base de datos
     * @throws SQLException
     */
    private void cargarReservas() throws SQLException {
        listaReservas = ModelFactoryController.getInstance().getSistema().obtenerReservas();
    }

    /**
     * Método para cargar los clientes desde la base de datos
     * @throws SQLException
     */
    private void cargarClientes() throws SQLException {
        listaClientes = ModelFactoryController.getInstance().getSistema().obtenerClientes();
    }

    /**
     * Método para actualizar los contadores del dashboard
     */
    private void actualizarContadores() {
        if (listaReservas != null) {
            // Contar reservas pendientes
            long reservasPendientes = listaReservas.stream()
                    .filter(r -> r.getEstadoReserva() == EstadoReserva.PENDIENTE)
                    .count();
            lblReservasPendientes.setText(String.valueOf(reservasPendientes));

            // Contar reservas confirmadas
            long reservasConfirmadas = listaReservas.stream()
                    .filter(r -> r.getEstadoReserva() == EstadoReserva.CONFIRMADA)
                    .count();
            lblReservasConfirmadas.setText(String.valueOf(reservasConfirmadas));
        }

        if (listaClientes != null) {
            // Mostrar cantidad de clientes
            lblClientesRegistrados.setText(String.valueOf(listaClientes.size()));
        }
    }

    /**
     * Método para inicializar el gráfico de reservas por mes
     */
    private void inicializarGraficoReservasPorMes() {
        // Crear la serie de datos
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservas");

        // Crear mapa para contar reservas por mes
        Map<Month, Integer> reservasPorMes = new HashMap<>();

        // Inicializar mapa con todos los meses
        for (Month mes : Month.values()) {
            reservasPorMes.put(mes, 0);
        }

        // Contar reservas por mes
        if (listaReservas != null) {
            for (Reserva reserva : listaReservas) {
                if (reserva.getFechaInicio() != null) {
                    Month mes = reserva.getFechaInicio().getMonth();
                    reservasPorMes.put(mes, reservasPorMes.get(mes) + 1);
                }
            }
        }

        // Añadir datos a la serie para los últimos 6 meses
        Month mesActual = LocalDate.now().getMonth();
        for (int i = 5; i >= 0; i--) {
            Month mes = mesActual.minus(i);
            String nombreMes = mes.getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            series.getData().add(new XYChart.Data<>(nombreMes, reservasPorMes.get(mes)));
        }

        // Limpiar gráfico anterior y añadir nueva serie
        chartReservasPorMes.getData().clear();
        chartReservasPorMes.getData().add(series);
    }

    /**
     * Método para inicializar el gráfico de reservas por estado
     */
    private void inicializarGraficoReservasPorEstado() {
        // Crear mapa para contar reservas por estado
        Map<EstadoReserva, Integer> reservasPorEstado = new HashMap<>();

        // Inicializar mapa con todos los estados
        for (EstadoReserva estado : EstadoReserva.values()) {
            reservasPorEstado.put(estado, 0);
        }

        // Contar reservas por estado
        if (listaReservas != null) {
            for (Reserva reserva : listaReservas) {
                if (reserva.getEstadoReserva() != null) {
                    EstadoReserva estado = reserva.getEstadoReserva();
                    reservasPorEstado.put(estado, reservasPorEstado.get(estado) + 1);
                }
            }
        }

        // Crear datos para el gráfico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<EstadoReserva, Integer> entry : reservasPorEstado.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey().toString(), entry.getValue()));
            }
        }

        // Añadir datos al gráfico
        chartReservasPorEstado.setData(pieChartData);
    }

    /**
     * Método para ver las reservas pendientes
     * @param event
     */
    @FXML
    void verReservasPendientes(ActionEvent event) {
        try {
            // Obtener el BorderPane principal
            BorderPane rootPane = (BorderPane) lblClientesRegistrados.getScene().getRoot();

            // Cargar la vista de reservas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/EmpleadoReservas.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            EmpleadoReservasController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();
            controller.filtrarPorEstado(EstadoReserva.PENDIENTE);

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar panel de reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para ver las reservas confirmadas
     * @param event
     */
    @FXML
    void verReservasConfirmadas(ActionEvent event) {
        try {
            // Obtener el BorderPane principal
            BorderPane rootPane = (BorderPane) lblClientesRegistrados.getScene().getRoot();

            // Cargar la vista de reservas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/EmpleadoReservas.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            EmpleadoReservasController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();
            controller.filtrarPorEstado(EstadoReserva.CONFIRMADA);

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar panel de reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para ver los clientes
     * @param event
     */
    @FXML
    void verClientes(ActionEvent event) {
        try {
            // Obtener el BorderPane principal
            BorderPane rootPane = (BorderPane) lblClientesRegistrados.getScene().getRoot();

            // Cargar la vista de clientes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/EmpleadoClientes.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            EmpleadoClientesController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar panel de clientes: " + e.getMessage(), AlertType.ERROR);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/NuevaReserva.fxml"));
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
     * Método para ver los reportes
     * @param event
     */
    @FXML
    void verReportes(ActionEvent event) {
        try {
            // Obtener el BorderPane principal
            BorderPane rootPane = (BorderPane) lblClientesRegistrados.getScene().getRoot();

            // Cargar la vista de reportes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/EmpleadoReportes.fxml"));
            Parent panel = loader.load();

            // Obtener el controlador y establecer la aplicación
            EmpleadoReportesController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarDatos();

            // Reemplazar el contenido central
            rootPane.setCenter(panel);

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar panel de reportes: " + e.getMessage(), AlertType.ERROR);
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