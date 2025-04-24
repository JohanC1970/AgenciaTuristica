package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.model.Habitacion;
import co.edu.uniquindio.agenciaturistica.model.Hospedaje;
import co.edu.uniquindio.agenciaturistica.model.TipoHabitacion;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class SeleccionHabitacionController implements Initializable {

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSeleccionar;

    @FXML
    private TableColumn<Habitacion, Boolean> colAcciones;

    @FXML
    private TableColumn<Habitacion, Integer> colCapacidad;

    @FXML
    private TableColumn<Habitacion, String> colHospedaje;

    @FXML
    private TableColumn<Habitacion, Integer> colId;

    @FXML
    private TableColumn<Habitacion, Double> colPrecio;

    @FXML
    private TableColumn<Habitacion, String> colTipo;

    @FXML
    private ComboBox<Hospedaje> comboHospedaje;

    @FXML
    private ComboBox<TipoHabitacion> comboTipo;

    @FXML
    private Label lblFechas;

    @FXML
    private Label lblError;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<Habitacion> tablaHabitaciones;

    @FXML
    private TextField txtCapacidadMin;

    private ObservableList<Habitacion> listaHabitaciones = FXCollections.observableArrayList();
    private ObservableList<Hospedaje> listaHospedajes = FXCollections.observableArrayList();
    private ObservableList<TipoHabitacion> listaTiposHabitacion = FXCollections.observableArrayList();
    private List<Habitacion> habitacionesSeleccionadas = new ArrayList<>();

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tabla
        configurarTabla();

        // Configurar listeners para combos
        configurarComboHospedaje();
        configurarComboTipoHabitacion();
    }

    /**
     * Método para configurar la tabla de habitaciones
     */
    private void configurarTabla() {
        colId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colHospedaje.setCellValueFactory(cellData ->
                new SimpleStringProperty(findHospedajeName(cellData.getValue().getId())));

        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTipoHabitacion().getNombre()));

        colCapacidad.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCapacidad()).asObject());

        colPrecio.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrecioPorNoche()).asObject());

        // Formatear precio como moneda
        colPrecio.setCellFactory(column -> {
            return new TableCell<Habitacion, Double>() {
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

        // Configurar columna de selección con checkboxes
        colAcciones.setCellValueFactory(cellData -> {
            Habitacion habitacion = cellData.getValue();
            SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(estaSeleccionada(habitacion));

            // Listener para actualizar la lista de habitaciones seleccionadas
            booleanProp.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    agregarHabitacionSeleccionada(habitacion);
                } else {
                    quitarHabitacionSeleccionada(habitacion);
                }
                actualizarTotalSeleccionadas();
            });

            return booleanProp;
        });

        colAcciones.setCellFactory(col -> {
            TableCell<Habitacion, Boolean> cell = new TableCell<Habitacion, Boolean>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    // Configurar acción del checkbox
                    checkBox.setOnAction(event -> {
                        Boolean isSelected = checkBox.isSelected();
                        Habitacion habitacion = getTableView().getItems().get(getIndex());

                        if (isSelected) {
                            agregarHabitacionSeleccionada(habitacion);
                        } else {
                            quitarHabitacionSeleccionada(habitacion);
                        }
                        actualizarTotalSeleccionadas();
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        checkBox.setSelected(item != null && item);
                        setGraphic(checkBox);
                    }
                }
            };

            return cell;
        });

        tablaHabitaciones.setItems(listaHabitaciones);
    }

    /**
     * Método para encontrar el nombre del hospedaje por id de habitación
     */
    private String findHospedajeName(int habitacionId) {
        // En un sistema real, aquí buscaríamos el hospedaje asociado a la habitación
        // Como simplificación, podríamos usar un mapa o cargar los datos completos
        for (Hospedaje hospedaje : listaHospedajes) {
            for (Habitacion habitacion : hospedaje.getHabitaciones()) {
                if (habitacion.getId() == habitacionId) {
                    return hospedaje.getNombre();
                }
            }
        }
        return "No especificado";
    }

    /**
     * Método para configurar el combo de hospedajes
     */
    private void configurarComboHospedaje() {
        comboHospedaje.setCellFactory(param -> new javafx.scene.control.ListCell<Hospedaje>() {
            @Override
            protected void updateItem(Hospedaje item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " - " + item.getCiudad());
                }
            }
        });

        comboHospedaje.setButtonCell(new javafx.scene.control.ListCell<Hospedaje>() {
            @Override
            protected void updateItem(Hospedaje item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccione hospedaje");
                } else {
                    setText(item.getNombre() + " - " + item.getCiudad());
                }
            }
        });
    }

    /**
     * Método para configurar el combo de tipos de habitación
     */
    private void configurarComboTipoHabitacion() {
        comboTipo.setCellFactory(param -> new javafx.scene.control.ListCell<TipoHabitacion>() {
            @Override
            protected void updateItem(TipoHabitacion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre());
                }
            }
        });

        comboTipo.setButtonCell(new javafx.scene.control.ListCell<TipoHabitacion>() {
            @Override
            protected void updateItem(TipoHabitacion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccione tipo");
                } else {
                    setText(item.getNombre());
                }
            }
        });
    }

    /**
     * Método para verificar si una habitación está en la lista de seleccionadas
     */
    private boolean estaSeleccionada(Habitacion habitacion) {
        return habitacionesSeleccionadas.stream()
                .anyMatch(h -> h.getId() == habitacion.getId());
    }

    /**
     * Método para agregar una habitación a la lista de seleccionadas
     */
    private void agregarHabitacionSeleccionada(Habitacion habitacion) {
        if (!estaSeleccionada(habitacion)) {
            habitacionesSeleccionadas.add(habitacion);
        }
    }

    /**
     * Método para quitar una habitación de la lista de seleccionadas
     */
    private void quitarHabitacionSeleccionada(Habitacion habitacion) {
        habitacionesSeleccionadas.removeIf(h -> h.getId() == habitacion.getId());
    }

    /**
     * Método para actualizar el contador de habitaciones seleccionadas
     */
    private void actualizarTotalSeleccionadas() {
        double total = habitacionesSeleccionadas.stream()
                .mapToDouble(Habitacion::getPrecioPorNoche)
                .sum();

        lblTotal.setText(String.format("Total seleccionadas: %d - Precio: $%,.0f",
                habitacionesSeleccionadas.size(), total));
    }

    /**
     * Método para establecer las fechas de la reserva
     */
    public void setFechasReserva(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;

        // Actualizar etiqueta con fechas
        lblFechas.setText("Habitaciones disponibles del " +
                fechaInicio.format(dateFormatter) + " al " +
                fechaFin.format(dateFormatter));
    }

    /**
     * Método para establecer las habitaciones que ya están seleccionadas
     */
    public void setHabitacionesSeleccionadas(List<Habitacion> habitaciones) {
        this.habitacionesSeleccionadas = new ArrayList<>(habitaciones);
    }

    /**
     * Método para obtener las habitaciones seleccionadas
     */
    public List<Habitacion> getHabitacionesSeleccionadas() {
        return habitacionesSeleccionadas;
    }

    /**
     * Método para inicializar datos
     */
    public void inicializarDatos() {
        try {
            // Cargar hospedajes
            cargarHospedajes();

            // Cargar tipos de habitación
            cargarTiposHabitacion();

            // Cargar habitaciones disponibles para las fechas seleccionadas
            cargarHabitacionesDisponibles();

            // Actualizar contador de seleccionadas
            actualizarTotalSeleccionadas();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cargar los hospedajes disponibles
     */
    private void cargarHospedajes() throws SQLException {
        listaHospedajes.clear();
        List<Hospedaje> hospedajes = ModelFactoryController.getInstance()
                .getSistema().obtenerHospedajes();
        listaHospedajes.addAll(hospedajes);

        // Agregar opción "Todos los hospedajes" al inicio
        Hospedaje todosHospedajes = new Hospedaje();
        todosHospedajes.setId(-1);
        todosHospedajes.setNombre("Todos los hospedajes");
        listaHospedajes.add(0, todosHospedajes);

        comboHospedaje.setItems(listaHospedajes);
        comboHospedaje.getSelectionModel().selectFirst();
    }

    /**
     * Método para cargar los tipos de habitación disponibles
     */
    private void cargarTiposHabitacion() throws SQLException {
        listaTiposHabitacion.clear();
        List<TipoHabitacion> tiposHabitacion = ModelFactoryController.getInstance()
                .getSistema().obtenerTiposHabitacion();
        listaTiposHabitacion.addAll(tiposHabitacion);

        // Agregar opción "Todos los tipos" al inicio
        TipoHabitacion todosTipos = new TipoHabitacion();
        todosTipos.setId(-1);
        todosTipos.setNombre("Todos los tipos");
        listaTiposHabitacion.add(0, todosTipos);

        comboTipo.setItems(listaTiposHabitacion);
        comboTipo.getSelectionModel().selectFirst();
    }

    /**
     * Método para cargar las habitaciones disponibles para las fechas seleccionadas
     */
    private void cargarHabitacionesDisponibles() throws SQLException {
        listaHabitaciones.clear();

        if (fechaInicio == null || fechaFin == null) {
            lblError.setText("Debe seleccionar las fechas para buscar habitaciones disponibles");
            return;
        }

        // Recorrer cada hospedaje para obtener sus habitaciones disponibles
        for (Hospedaje hospedaje : listaHospedajes) {
            // Saltar la opción "Todos los hospedajes"
            if (hospedaje.getId() == -1) continue;

            // Obtener habitaciones del hospedaje
            List<Habitacion> habitacionesHospedaje = ModelFactoryController.getInstance()
                    .getSistema().obtenerHabitacionesPorHospedaje(hospedaje.getId());

            // Filtrar solo las que están disponibles para las fechas seleccionadas
            for (Habitacion habitacion : habitacionesHospedaje) {
                if (habitacion.isDisponible() && verificarDisponibilidadFechas(habitacion.getId())) {
                    listaHabitaciones.add(habitacion);
                }
            }
        }

        // Actualizar tabla
        tablaHabitaciones.refresh();
    }

    /**
     * Método para verificar si una habitación está disponible en las fechas seleccionadas
     */
    private boolean verificarDisponibilidadFechas(int habitacionId) {
        try {
            return ModelFactoryController.getInstance().getSistema()
                    .verificarDisponibilidadHabitacion(habitacionId, fechaInicio, fechaFin);
        } catch (Exception e) {
            // En caso de error, asumimos que no está disponible
            return false;
        }
    }

    /**
     * Método para buscar habitaciones según los filtros aplicados
     */
    @FXML
    void buscarHabitaciones(ActionEvent event) {
        try {
            // Obtener filtros
            Hospedaje hospedajeSeleccionado = comboHospedaje.getValue();
            TipoHabitacion tipoSeleccionado = comboTipo.getValue();
            String capacidadMinStr = txtCapacidadMin.getText().trim();
            int capacidadMin = capacidadMinStr.isEmpty() ? 0 : Integer.parseInt(capacidadMinStr);

            // Recargar todas las habitaciones disponibles
            cargarHabitacionesDisponibles();

            // Aplicar filtros
            ObservableList<Habitacion> habitacionesFiltradas = FXCollections.observableArrayList();

            for (Habitacion habitacion : listaHabitaciones) {
                boolean cumpleFiltroHospedaje = hospedajeSeleccionado.getId() == -1 ||
                        findHospedajeByHabitacionId(habitacion.getId()) == hospedajeSeleccionado.getId();

                boolean cumpleFiltrTipo = tipoSeleccionado.getId() == -1 ||
                        habitacion.getTipoHabitacion().getId() == tipoSeleccionado.getId();

                boolean cumpleFiltroCapacidad = habitacion.getCapacidad() >= capacidadMin;

                if (cumpleFiltroHospedaje && cumpleFiltrTipo && cumpleFiltroCapacidad) {
                    habitacionesFiltradas.add(habitacion);
                }
            }

            // Actualizar tabla con resultados filtrados
            listaHabitaciones.clear();
            listaHabitaciones.addAll(habitacionesFiltradas);

            // Actualizar mensaje de resultado
            lblError.setText("");

        } catch (NumberFormatException e) {
            lblError.setText("La capacidad mínima debe ser un número entero");
        } catch (SQLException e) {
            lblError.setText("Error al buscar habitaciones: " + e.getMessage());
        }
    }

    /**
     * Método para encontrar el ID del hospedaje por ID de habitación
     */
    private int findHospedajeByHabitacionId(int habitacionId) {
        for (Hospedaje hospedaje : listaHospedajes) {
            for (Habitacion habitacion : hospedaje.getHabitaciones()) {
                if (habitacion.getId() == habitacionId) {
                    return hospedaje.getId();
                }
            }
        }
        return -1;
    }

    /**
     * Método para confirmar la selección de habitaciones
     */
    @FXML
    void confirmarSeleccion(ActionEvent event) {
        // Cerrar la ventana y devolver las habitaciones seleccionadas
        ((Stage) btnSeleccionar.getScene().getWindow()).close();
    }

    /**
     * Método para cancelar la selección
     */
    @FXML
    void cancelarSeleccion(ActionEvent event) {
        // Restaurar lista original de seleccionadas y cerrar
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }

    /**
     * Método auxiliar para mostrar alertas
     */
    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Método especial para verificar disponibilidad de habitación
     * Este es un método temporal hasta que implementemos la conexión real a la base de datos
     */
    public boolean verificarDisponibilidadHabitacion(int habitacionId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            return ModelFactoryController.getInstance().getSistema()
                    .verificarDisponibilidadHabitacion(habitacionId, fechaInicio, fechaFin);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}