package co.edu.uniquindio.agenciaturistica.controller;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.*;
import co.edu.uniquindio.agenciaturistica.model.Enums.EstadoReserva;
import co.edu.uniquindio.agenciaturistica.model.Enums.FormaPago;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class NuevaReservaController {

    // Referencias a la aplicación y datos
    private Aplicacion aplicacion;
    private PaqueteTuristico paquete;
    private Empleado empleado;

    // Componentes de la interfaz
    @FXML private Label lblNombrePaquete;
    @FXML private Label lblDescripcion;
    @FXML private Label lblDuracion;
    @FXML private Label lblPrecioBase;
    @FXML private Label lblCuposDisponibles;

    @FXML private ComboBox<Cliente> cbClientes;
    @FXML private TableView<HabitacionWrapper> tblHabitaciones;

    @FXML private DatePicker dpFechaInicio;
    @FXML private Label lblFechaFin;
    @FXML private ComboBox<FormaPago> cbFormaPago;
    @FXML private Label lblPrecioTotal;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    // Clase wrapper para las habitaciones con selección
    private class HabitacionWrapper {
        private final Habitacion habitacion;
        private final SimpleBooleanProperty seleccionada;

        public HabitacionWrapper(Habitacion habitacion) {
            this.habitacion = habitacion;
            this.seleccionada = new SimpleBooleanProperty(false);
        }

        // Getters para las propiedades de la habitación
        public int getId() { return habitacion.getId(); }
        public TipoHabitacion getTipoHabitacion() { return habitacion.getTipoHabitacion(); }
        public int getCapacidad() { return habitacion.getCapacidad(); }
        public double getPrecioPorNoche() { return habitacion.getPrecioPorNoche(); }
        public boolean isSeleccionada() { return seleccionada.get(); }
        public SimpleBooleanProperty seleccionadaProperty() { return seleccionada; }
    }

    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    public void setPaquete(PaqueteTuristico paquete) {
        this.paquete = paquete;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public void inicializarDatos() throws SQLException {
        // Inicializar datos del paquete
        lblNombrePaquete.setText(paquete.getNombre());
        lblDescripcion.setText(paquete.getDescripcion());
        lblDuracion.setText(String.valueOf(paquete.getDuracionDias()));
        lblPrecioBase.setText(String.format("$%,.2f", paquete.getPrecioBase()));
        lblCuposDisponibles.setText(String.valueOf(paquete.getCuposDisponibles()));

        // Configurar combo de clientes
        cbClientes.setItems(FXCollections.observableArrayList(ModelFactoryController.getInstance().getSistema().obtenerClientes()));
        cbClientes.setCellFactory(param -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " " + item.getApellido() + " (" + item.getIdentificacion() + ")");
                }
            }
        });

        // Configurar combo de formas de pago
        cbFormaPago.setItems(FXCollections.observableArrayList(FormaPago.values()));

        // Configurar tabla de habitaciones
        List<Habitacion> habitacionesDisponibles = paquete.getHospedajes().stream()
                .flatMap(h -> h.getHabitaciones().stream())
                .filter(Habitacion::isDisponible)
                .toList();

        tblHabitaciones.setItems(FXCollections.observableArrayList(
                habitacionesDisponibles.stream().map(HabitacionWrapper::new).toList()
        ));

        // Listener para calcular fechas y precios
        dpFechaInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && paquete != null) {
                lblFechaFin.setText(newVal.plusDays(paquete.getDuracionDias()).toString());
                calcularPrecioTotal();
            }
        });

        tblHabitaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            calcularPrecioTotal();
        });
    }

    private void calcularPrecioTotal() {
        if (paquete == null || dpFechaInicio.getValue() == null) return;

        double precioHabitaciones = tblHabitaciones.getItems().stream()
                .filter(HabitacionWrapper::isSeleccionada)
                .mapToDouble(w -> w.getPrecioPorNoche() * paquete.getDuracionDias())
                .sum();

        double precioTotal = paquete.getPrecioBase() + precioHabitaciones;
        lblPrecioTotal.setText(String.format("$%,.2f", precioTotal));
    }

    @FXML
    private void guardarReserva() {
        try {
            // Validaciones
            if (cbClientes.getValue() == null) {
                mostrarAlerta("Error", "Debe seleccionar un cliente", Alert.AlertType.ERROR);
                return;
            }

            if (dpFechaInicio.getValue() == null) {
                mostrarAlerta("Error", "Debe seleccionar una fecha de inicio", Alert.AlertType.ERROR);
                return;
            }

            if (cbFormaPago.getValue() == null) {
                mostrarAlerta("Error", "Debe seleccionar una forma de pago", Alert.AlertType.ERROR);
                return;
            }

            // Crear la reserva
            Reserva reserva = new Reserva();
            reserva.setCliente(cbClientes.getValue());
            reserva.setEmpleado(empleado);
            reserva.setPaqueteTuristico(paquete);
            reserva.setFechaInicio(dpFechaInicio.getValue());
            reserva.setFechaFin(dpFechaInicio.getValue().plusDays(paquete.getDuracionDias()));
            reserva.setFormaPago(cbFormaPago.getValue());
            reserva.setEstadoReserva(EstadoReserva.PENDIENTE);

            // Agregar habitaciones seleccionadas
            List<Habitacion> habitacionesSeleccionadas = tblHabitaciones.getItems().stream()
                    .filter(HabitacionWrapper::isSeleccionada)
                    .map(w -> w.habitacion)
                    .toList();

            reserva.setHabitaciones(habitacionesSeleccionadas);

            // Calcular y establecer precio total
            calcularPrecioTotal();
            reserva.setPrecioTotal(Double.parseDouble(lblPrecioTotal.getText().replace("$", "").replace(",", "")));

            // Guardar la reserva
            ModelFactoryController.getInstance().getSistema().crearReserva(reserva);

            // Cerrar la ventana
            ((Stage) btnGuardar.getScene().getWindow()).close();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo crear la reserva: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
