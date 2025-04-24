package co.edu.uniquindio.agenciaturistica.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.scene.Cursor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Cliente;
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

public class EmpleadoClientesController implements Initializable {

    @FXML
    private TextField txtBusqueda;

    @FXML
    private Label lblTotalClientes;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> colIdentificacion;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colApellido;

    @FXML
    private TableColumn<Cliente, String> colCorreo;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private TableColumn<Cliente, String> colAcciones;

    @FXML
    private Label lblMensaje;

    private Aplicacion aplicacion;
    private List<Cliente> listaClientes;
    private ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList();
    private Cliente clienteSeleccionado;

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
            // Cargar lista de clientes
            listaClientes = ModelFactoryController.getInstance().getSistema().obtenerClientes();
            clientesObservable.clear();
            clientesObservable.addAll(listaClientes);

            // Actualizar tabla
            tablaClientes.setItems(clientesObservable);

            // Actualizar contador
            actualizarContador();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar datos de clientes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para configurar la tabla de clientes
     */
    private void configurarTabla() {
        // Configurar columnas
        colIdentificacion.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // Configurar columna de acciones
        colAcciones.setCellFactory(crearBotonesAcciones());
    }

    /**
     * Método para crear la columna de botones de acciones
     * @return Factory para crear celdas con botones
     */
    private Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>>() {
            @Override
            public TableCell<Cliente, String> call(TableColumn<Cliente, String> param) {
                return new TableCell<Cliente, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnVer = new Button("Ver");
                    private final HBox hBox = new HBox(5, btnVer, btnEditar);

                    {
                        // Configurar botón Editar
                        btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                        btnEditar.setCursor(Cursor.HAND);
                        btnEditar.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            editarCliente(cliente);
                        });

                        // Configurar botón Ver
                        btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnVer.setCursor(Cursor.HAND);
                        btnVer.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            verDetallesCliente(cliente);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(hBox);
                        }
                    }
                };
            }
        };
    }

    /**
     * Método para actualizar el contador de clientes
     */
    private void actualizarContador() {
        lblTotalClientes.setText(String.valueOf(clientesObservable.size()));
    }

    /**
     * Método para buscar clientes
     * @param event
     */
    @FXML
    void buscarCliente(ActionEvent event) {
        // Obtener texto de búsqueda
        String textoBusqueda = txtBusqueda.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si la búsqueda está vacía, mostrar todos los clientes
            clientesObservable.clear();
            clientesObservable.addAll(listaClientes);
        } else {
            // Filtrar clientes que coincidan con la búsqueda
            List<Cliente> clientesFiltrados = listaClientes.stream()
                    .filter(c -> c.getNombre().toLowerCase().contains(textoBusqueda) ||
                            c.getApellido().toLowerCase().contains(textoBusqueda) ||
                            c.getIdentificacion().toLowerCase().contains(textoBusqueda) ||
                            c.getCorreo().toLowerCase().contains(textoBusqueda))
                    .collect(Collectors.toList());

            clientesObservable.clear();
            clientesObservable.addAll(clientesFiltrados);
        }

        // Actualizar contador
        actualizarContador();

        // Mostrar mensaje si no hay resultados
        if (clientesObservable.isEmpty()) {
            lblMensaje.setText("No se encontraron clientes con ese criterio de búsqueda");
        } else {
            lblMensaje.setText("");
        }
    }

    /**
     * Método para exportar clientes a Excel
     * @param event
     */
    @FXML
    void exportarClientes(ActionEvent event) {
        try {
            // Crear libro de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Clientes");

            // Crear estilo para encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Identificación", "Nombre", "Apellido", "Correo", "Teléfono", "Fecha Nacimiento"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Llenar datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowNum = 1;

            for (Cliente cliente : clientesObservable) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cliente.getIdentificacion());
                row.createCell(1).setCellValue(cliente.getNombre());
                row.createCell(2).setCellValue(cliente.getApellido());
                row.createCell(3).setCellValue(cliente.getCorreo());
                row.createCell(4).setCellValue(cliente.getTelefono());

                if (cliente.getFechaNacimiento() != null) {
                    row.createCell(5).setCellValue(cliente.getFechaNacimiento().format(formatter));
                } else {
                    row.createCell(5).setCellValue("");
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Lista de Clientes");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
            fileChooser.setInitialFileName("Clientes.xlsx");

            File file = fileChooser.showSaveDialog(tablaClientes.getScene().getWindow());

            if (file != null) {
                FileOutputStream fileOut = new FileOutputStream(file);
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();

                mostrarAlerta("Éxito", "El archivo se ha guardado correctamente", AlertType.INFORMATION);
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al exportar clientes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para ver el historial de reservas de un cliente
     * @param event
     */
    @FXML
    void verHistorialReservas(ActionEvent event) {
        // Obtener cliente seleccionado
        clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            mostrarAlerta("Información", "Por favor, seleccione un cliente para ver su historial de reservas", AlertType.INFORMATION);
            return;
        }

        try {
            // Cargar la vista del historial de reservas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/HistorialReservasCliente.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            HistorialReservasClienteController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setCliente(clienteSeleccionado);
            controller.inicializarDatos();

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Historial de Reservas - " + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir historial de reservas: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para crear un nuevo cliente
     * @param event
     */
    @FXML
    void nuevoCliente(ActionEvent event) {
        try {
            // Cargar la vista del formulario de cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/FormularioCliente.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            FormularioClienteController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setModoEdicion(false);

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Nuevo Cliente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos después de cerrar la ventana
            inicializarDatos();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir formulario de cliente: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para editar un cliente existente
     * @param cliente Cliente a editar
     */
    private void editarCliente(Cliente cliente) {
        try {
            // Cargar la vista del formulario de cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/FormularioCliente.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            FormularioClienteController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setModoEdicion(true);
            controller.setCliente(cliente);
            controller.inicializarDatosCliente();

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Editar Cliente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recargar datos después de cerrar la ventana
            inicializarDatos();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir formulario de cliente: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para ver los detalles de un cliente
     * @param cliente Cliente a visualizar
     */
    private void verDetallesCliente(Cliente cliente) {
        try {
            // Cargar la vista de detalles del cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/agenciaturistica/DetallesCliente.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            DetallesClienteController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.setCliente(cliente);
            controller.inicializarDatosCliente();

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Detalles del Cliente");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir detalles del cliente: " + e.getMessage(), AlertType.ERROR);
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