package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionClientesController implements Initializable {

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnVolver;

    @FXML
    private TableColumn<Cliente, String> colAcciones;

    @FXML
    private TableColumn<Cliente, String> colApellido;

    @FXML
    private TableColumn<Cliente, String> colEmail;

    @FXML
    private TableColumn<Cliente, String> colFechaNacimiento;

    @FXML
    private TableColumn<Cliente, String> colIdentificacion;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private DatePicker dateFechaNacimiento;

    @FXML
    private Label lblError;

    @FXML
    private Label lblTotalClientes;

    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    private Aplicacion aplicacion;
    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private Cliente clienteSeleccionado;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración de la tabla de clientes
        configurarTabla();

        // Ocultar el panel de formulario al inicio
        panelFormulario.setVisible(false);

        // Inicializar fecha de nacimiento con un valor por defecto (18 años atrás)
        dateFechaNacimiento.setValue(LocalDate.now().minusYears(18));
    }

    /**
     * Método para configurar la tabla de clientes
     */
    private void configurarTabla() {
        colIdentificacion.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // Fecha de nacimiento formateada
        colFechaNacimiento.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            String fechaStr = cliente.getFechaNacimiento() != null ?
                    cliente.getFechaNacimiento().toString() : "No disponible";
            return new SimpleStringProperty(fechaStr);
        });

        // Columna de acciones (editar y eliminar)
        colAcciones.setCellFactory(crearBotonesAcciones());

        tablaClientes.setItems(listaClientes);
    }

    /**
     * Método para crear los botones de acciones (editar y eliminar)
     *
     * @return
     */
    private Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>>() {
            @Override
            public TableCell<Cliente, String> call(final TableColumn<Cliente, String> param) {
                return new TableCell<Cliente, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox pane = new HBox(5, btnEditar, btnEliminar);

                    {
                        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnEditar.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            editarCliente(cliente);
                        });

                        btnEliminar.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            eliminarCliente(cliente);
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
     *
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;

        // Cargar la lista de clientes
        cargarClientes();
    }

    /**
     * Método para cargar la lista de clientes
     */
    private void cargarClientes() {
        try {
            // Obtener la lista de clientes a través del ModelFactoryController
            List<Cliente> clientes = ModelFactoryController.getInstance().getSistema().obtenerClientes();

            // Limpiar y agregar los clientes a la lista observable
            listaClientes.clear();
            listaClientes.addAll(clientes);

            // Actualizar el contador de clientes
            actualizarContadorClientes();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar los clientes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el contador de clientes
     */
    private void actualizarContadorClientes() {
        lblTotalClientes.setText("Total de clientes: " + listaClientes.size());
    }

    /**
     * Método para buscar un cliente
     *
     * @param event
     */
    @FXML
    void buscarCliente(ActionEvent event) {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si el campo de búsqueda está vacío, mostrar todos los clientes
            tablaClientes.setItems(listaClientes);
            return;
        }

        // Filtrar los clientes que coincidan con la búsqueda
        ObservableList<Cliente> clientesFiltrados = FXCollections.observableArrayList();

        for (Cliente cliente : listaClientes) {
            // Buscar por nombre, apellido, identificación o correo
            if (cliente.getNombre().toLowerCase().contains(textoBusqueda) ||
                    cliente.getApellido().toLowerCase().contains(textoBusqueda) ||
                    cliente.getIdentificacion().toLowerCase().contains(textoBusqueda) ||
                    cliente.getCorreo().toLowerCase().contains(textoBusqueda)) {
                clientesFiltrados.add(cliente);
            }
        }

        // Actualizar la tabla con los resultados
        tablaClientes.setItems(clientesFiltrados);
        lblTotalClientes.setText("Resultados: " + clientesFiltrados.size());
    }

    /**
     * Método para mostrar el formulario de agregar cliente
     *
     * @param event
     */
    @FXML
    void mostrarFormularioAgregar(ActionEvent event) {
        // Limpiar el formulario
        limpiarFormulario();

        // Establecer modo de agregación
        modoEdicion = false;

        // Mostrar el panel de formulario
        panelFormulario.setVisible(true);

        // Enfocar en el primer campo
        txtIdentificacion.requestFocus();
    }

    /**
     * Método para editar un cliente
     *
     * @param cliente
     */
    private void editarCliente(Cliente cliente) {
        // Establecer el cliente seleccionado
        clienteSeleccionado = cliente;

        // Cargar los datos en el formulario
        txtIdentificacion.setText(cliente.getIdentificacion());
        txtIdentificacion.setDisable(true); // No permitir editar la identificación
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtEmail.setText(cliente.getCorreo());
        txtTelefono.setText(cliente.getTelefono());

        // Establecer la fecha de nacimiento
        if (cliente.getFechaNacimiento() != null) {
            dateFechaNacimiento.setValue(cliente.getFechaNacimiento());
        }

        // Establecer modo de edición
        modoEdicion = true;

        // Mostrar el panel de formulario
        panelFormulario.setVisible(true);
    }

    /**
     * Método para eliminar un cliente
     *
     * @param cliente
     */
    private void eliminarCliente(Cliente cliente) {
        try {
            // Confirmar la eliminación
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar este cliente?");
            alert.setContentText("Esta acción no se puede deshacer.");

            if (alert.showAndWait().get().getButtonData().isCancelButton()) {
                return; // El usuario canceló la operación
            }

            // Eliminar el cliente a través del ModelFactoryController
            Respuesta<Cliente> respuesta = ModelFactoryController.getInstance().getSistema().eliminarCliente(cliente.getIdentificacion());

            if (respuesta.isExito()) {
                // Eliminar el cliente de la lista y actualizar la tabla
                listaClientes.remove(cliente);
                actualizarContadorClientes();
                mostrarAlerta("Eliminación exitosa", "El cliente ha sido eliminado correctamente.", AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar el cliente: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para guardar un cliente (agregar o actualizar)
     *
     * @param event
     */
    @FXML
    void guardarCliente(ActionEvent event) {
        // Validar los datos del formulario
        if (!validarDatosFormulario()) {
            return;
        }

        try {
            // Crear un objeto Cliente con los datos del formulario
            Cliente cliente = new Cliente();
            cliente.setIdentificacion(txtIdentificacion.getText().trim());
            cliente.setNombre(txtNombre.getText().trim());
            cliente.setApellido(txtApellido.getText().trim());
            cliente.setCorreo(txtEmail.getText().trim());
            cliente.setTelefono(txtTelefono.getText().trim());
            cliente.setFechaNacimiento(dateFechaNacimiento.getValue());

            if (modoEdicion) {
                // Actualizar el cliente
                Respuesta<Cliente> respuesta = ModelFactoryController.getInstance().getSistema().actualizarCliente(cliente);

                if (respuesta.isExito()) {
                    // Actualizar la lista de clientes
                    cargarClientes();

                    // Ocultar el formulario
                    panelFormulario.setVisible(false);

                    mostrarAlerta("Actualización exitosa", "El cliente ha sido actualizado correctamente.", AlertType.INFORMATION);
                } else {
                    lblError.setText(respuesta.getMensaje());
                }
            } else {
                // Registrar el nuevo cliente
                Respuesta<Cliente> respuesta = ModelFactoryController.getInstance().getSistema().registrarCliente(cliente);

                if (respuesta.isExito()) {
                    // Actualizar la lista de clientes
                    cargarClientes();

                    // Ocultar el formulario
                    panelFormulario.setVisible(false);

                    mostrarAlerta("Registro exitoso", "El cliente ha sido registrado correctamente.", AlertType.INFORMATION);
                } else {
                    lblError.setText(respuesta.getMensaje());
                }
            }
        } catch (SQLException e) {
            lblError.setText("Error al guardar el cliente: " + e.getMessage());
        }
    }

    /**
     * Método para validar los datos del formulario
     *
     * @return
     */
    private boolean validarDatosFormulario() {
        // Limpiar mensaje de error
        lblError.setText("");

        // Validar identificación
        if (txtIdentificacion.getText().trim().isEmpty()) {
            lblError.setText("La identificación es obligatoria.");
            txtIdentificacion.requestFocus();
            return false;
        }

        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            lblError.setText("El nombre es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }

        // Validar apellido
        if (txtApellido.getText().trim().isEmpty()) {
            lblError.setText("El apellido es obligatorio.");
            txtApellido.requestFocus();
            return false;
        }

        // Validar email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            lblError.setText("El email es obligatorio.");
            txtEmail.requestFocus();
            return false;
        }

        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            lblError.setText("El formato del email no es válido.");
            txtEmail.requestFocus();
            return false;
        }

        // Validar teléfono
        if (txtTelefono.getText().trim().isEmpty()) {
            lblError.setText("El teléfono es obligatorio.");
            txtTelefono.requestFocus();
            return false;
        }

        // Validar fecha de nacimiento
        if (dateFechaNacimiento.getValue() == null) {
            lblError.setText("La fecha de nacimiento es obligatoria.");
            dateFechaNacimiento.requestFocus();
            return false;
        }

        // Validar que la fecha de nacimiento no sea en el futuro
        if (dateFechaNacimiento.getValue().isAfter(LocalDate.now())) {
            lblError.setText("La fecha de nacimiento no puede ser en el futuro.");
            dateFechaNacimiento.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Método para cancelar el formulario
     *
     * @param event
     */
    @FXML
    void cancelarFormulario(ActionEvent event) {
        panelFormulario.setVisible(false);
        limpiarFormulario();
    }

    /**
     * Método para limpiar el formulario
     */
    private void limpiarFormulario() {
        txtIdentificacion.setText("");
        txtIdentificacion.setDisable(false);
        txtNombre.setText("");
        txtApellido.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        dateFechaNacimiento.setValue(LocalDate.now().minusYears(18));
        lblError.setText("");
        clienteSeleccionado = null;
    }


    /**
     * Método para mostrar alertas
     *
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