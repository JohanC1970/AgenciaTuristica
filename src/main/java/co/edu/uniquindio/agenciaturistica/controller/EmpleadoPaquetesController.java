package co.edu.uniquindio.agenciaturistica.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.PaqueteTuristico;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class EmpleadoPaquetesController implements Initializable {

    @FXML
    private TextField txtBusqueda;

    @FXML
    private Label lblTotalPaquetes;

    @FXML
    private TableView<PaqueteTuristico> tablaPaquetes;

    @FXML
    private TableColumn<PaqueteTuristico, String> colNombre;

    @FXML
    private TableColumn<PaqueteTuristico, String> colDescripcion;

    @FXML
    private TableColumn<PaqueteTuristico, Integer> colDuracion;

    @FXML
    private TableColumn<PaqueteTuristico, LocalDate> colFechaInicio;

    @FXML
    private TableColumn<PaqueteTuristico, LocalDate> colFechaFin;

    @FXML
    private TableColumn<PaqueteTuristico, String> colPrecioBase;

    @FXML
    private TableColumn<PaqueteTuristico, Integer> colCuposDisponibles;

    @FXML
    private TableColumn<PaqueteTuristico, String> colAcciones;

    @FXML
    private Label lblMensaje;

    private Aplicacion aplicacion;
    private List<PaqueteTuristico> listaPaquetes;
    private ObservableList<PaqueteTuristico> paquetesObservable = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas de la tabla
        configurarTabla();
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
            // Cargar lista de paquetes turísticos
            listaPaquetes = ModelFactoryController.getInstance().getSistema().obtenerPaquetesTuristicos();
            paquetesObservable.clear();

            if (listaPaquetes != null) {
                paquetesObservable.addAll(listaPaquetes);
            }

            // Actualizar tabla
            tablaPaquetes.setItems(paquetesObservable);

            // Actualizar contador
            lblTotalPaquetes.setText(String.valueOf(paquetesObservable.size()));

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos de paquetes turísticos: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para configurar la tabla de paquetes
     */
    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colDescripcion.setCellValueFactory(cellData -> {
            String descripcion = cellData.getValue().getDescripcion();
            if (descripcion != null && descripcion.length() > 50) {
                descripcion = descripcion.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(descripcion);
        });

        colDuracion.setCellValueFactory(new PropertyValueFactory<>("duracionDias"));

        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaInicio.setCellFactory(column -> new TableCell<PaqueteTuristico, LocalDate>() {
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
        colFechaFin.setCellFactory(column -> new TableCell<PaqueteTuristico, LocalDate>() {
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

        colPrecioBase.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("$%,.0f", cellData.getValue().getPrecioBase())));

        colCuposDisponibles.setCellValueFactory(new PropertyValueFactory<>("cuposDisponibles"));
        colCuposDisponibles.setCellFactory(column -> new TableCell<PaqueteTuristico, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    // Colorear los cupos según disponibilidad
                    PaqueteTuristico paquete = getTableView().getItems().get(getIndex());
                    double porcentajeDisponible = (double) item / paquete.getCupoMaximo();

                    if (porcentajeDisponible <= 0.1) {
                        setStyle("-fx-text-fill: #e74c3c;"); // Rojo - pocos cupos
                    } else if (porcentajeDisponible <= 0.3) {
                        setStyle("-fx-text-fill: #e67e22;"); // Naranja - cupos limitados
                    } else {
                        setStyle("-fx-text-fill: #2ecc71;"); // Verde - cupos disponibles
                    }
                }
            }
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());
    }

    /**
     * Método para crear la columna de botones de acciones
     * @return Factory para crear celdas con botones
     */
    private Callback<TableColumn<PaqueteTuristico, String>, TableCell<PaqueteTuristico, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<PaqueteTuristico, String>, TableCell<PaqueteTuristico, String>>() {
            @Override
            public TableCell<PaqueteTuristico, String> call(TableColumn<PaqueteTuristico, String> param) {
                return new TableCell<PaqueteTuristico, String>() {
                    private final Button btnVer = new Button("Ver");
                    private final Button btnReservar = new Button("Reservar");
                    private final HBox hbox = new HBox(5, btnVer, btnReservar);

                    {
                        // Configurar botón Ver
                        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnVer.setCursor(javafx.scene.Cursor.HAND);
                        btnVer.setOnAction(event -> {
                            PaqueteTuristico paquete = getTableView().getItems().get(getIndex());
                            verDetallesPaquete(paquete);
                        });

                        // Configurar botón Reservar
                        btnReservar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                        btnReservar.setCursor(javafx.scene.Cursor.HAND);
                        btnReservar.setOnAction(event -> {
                            PaqueteTuristico paquete = getTableView().getItems().get(getIndex());
                            crearReservaPaquete(paquete);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            PaqueteTuristico paquete = getTableView().getItems().get(getIndex());
                            // Deshabilitar botón de reserva si no hay cupos disponibles
                            btnReservar.setDisable(paquete.getCuposDisponibles() <= 0);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para buscar paquetes por nombre o descripción
     * @param event
     */
    @FXML
    void buscarPaquete(ActionEvent event) {
        String textoBusqueda = txtBusqueda.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos los paquetes
            tablaPaquetes.setItems(paquetesObservable);
            lblMensaje.setText("");
            return;
        }

        // Filtrar paquetes por nombre o descripción
        ObservableList<PaqueteTuristico> paquetesFiltrados = FXCollections.observableArrayList();

        for (PaqueteTuristico paquete : paquetesObservable) {
            if (paquete.getNombre().toLowerCase().contains(textoBusqueda) ||
                    (paquete.getDescripcion() != null && paquete.getDescripcion().toLowerCase().contains(textoBusqueda))) {
                paquetesFiltrados.add(paquete);
            }
        }

        tablaPaquetes.setItems(paquetesFiltrados);

        // Mostrar mensaje si no hay resultados
        if (paquetesFiltrados.isEmpty()) {
            lblMensaje.setText("No se encontraron paquetes que coincidan con la búsqueda");
        } else {
            lblMensaje.setText("");
        }

        // Actualizar contador
        lblTotalPaquetes.setText(String.valueOf(paquetesFiltrados.size()));
    }

    /**
     * Método para crear un nuevo paquete turístico
     * @param event
     */
    @FXML
    void nuevoPaquete(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/GestionPaquetes.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario cargado
            GestionPaquetesController controller = loader.getController();

            // Si tu controlador necesita referencia a la clase principal
            controller.setAplicacion(aplicacion);

            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setTitle("Nuevo Paquete Turístico");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea otras ventanas hasta que se cierre
            stage.initOwner(((Node) event.getSource()).getScene().getWindow()); // Padre

            stage.showAndWait(); // Espera hasta que se cierre

        } catch (IOException e) {
            mostrarAlerta("Error", "Error al abrir formulario de nuevo paquete: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    /**
     * Método para ver los detalles de un paquete turístico
     * @param paquete Paquete a visualizar
     */
    private void verDetallesPaquete(PaqueteTuristico paquete) {
        try {
            // Aquí se podría abrir una ventana con los detalles del paquete
            mostrarAlerta("Detalles del Paquete",
                    "Nombre: " + paquete.getNombre() + "\n" +
                            "Descripción: " + paquete.getDescripcion() + "\n" +
                            "Precio Base: $" + String.format("%,.0f", paquete.getPrecioBase()) + "\n" +
                            "Duración: " + paquete.getDuracionDias() + " días\n" +
                            "Fecha Inicio: " + paquete.getFechaInicio().format(formatter) + "\n" +
                            "Fecha Fin: " + paquete.getFechaFin().format(formatter) + "\n" +
                            "Cupos Disponibles: " + paquete.getCuposDisponibles() + " de " + paquete.getCupoMaximo(),
                    AlertType.INFORMATION);
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al mostrar detalles del paquete: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para crear una reserva a partir de un paquete turístico
     * @param paquete Paquete a reservar
     */
    private void crearReservaPaquete(PaqueteTuristico paquete) {
        try {
            // Cargar la vista de nueva reserva
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/NuevaReserva.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            NuevaReservaController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setPaquete(paquete);
            controller.inicializarDatos();

            // Mostrar la ventana
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