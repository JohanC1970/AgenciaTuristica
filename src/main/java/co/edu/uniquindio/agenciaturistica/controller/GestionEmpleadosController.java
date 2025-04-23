package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Empleado;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class GestionEmpleadosController implements Initializable {

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
    private TableColumn<Usuario, String> colAcciones;

    @FXML
    private TableColumn<Usuario, String> colApellido;

    @FXML
    private TableColumn<Usuario, String> colEmail;

    @FXML
    private TableColumn<Usuario, String> colEstado;

    @FXML
    private TableColumn<Usuario, String> colFechaContratacion;

    @FXML
    private TableColumn<Usuario, String> colIdentificacion;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private ComboBox<String> comboEstado;

    @FXML
    private DatePicker dateFechaContratacion;

    @FXML
    private Label lblError;

    @FXML
    private Label lblTotalEmpleados;

    @FXML
    private VBox panelFormulario;

    @FXML
    private TableView<Usuario> tablaEmpleados;

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
    private PasswordField txtPassword;

    private Aplicacion aplicacion;
    private ObservableList<Usuario> listaEmpleados = FXCollections.observableArrayList();
    private Usuario empleadoSeleccionado;
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración de la tabla de empleados
        configurarTabla();

        // Configurar el combo de estado
        comboEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));

        // Ocultar el panel de formulario al inicio
        panelFormulario.setVisible(false);

        // Inicializar fecha de contratación con la fecha actual
        dateFechaContratacion.setValue(LocalDate.now());
    }

    /**
     * Método para configurar la tabla de empleados
     */
    private void configurarTabla() {
        colIdentificacion.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Fecha de contratación (se debe obtener de la tabla empleado)
        colFechaContratacion.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String fechaStr = "No disponible";
            if (usuario instanceof Empleado) {
                Empleado empleado = (Empleado) usuario;
                if (empleado.getFechaContratacion() != null) {
                    fechaStr = empleado.getFechaContratacion().toString();
                }
            }
            return new SimpleStringProperty(fechaStr);
        });

        // Estado de la cuenta
        colEstado.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            String estado = usuario.isCuentaVerificada() ? "Activo" : "Inactivo";
            return new SimpleStringProperty(estado);
        });

        // Columna de acciones (editar y eliminar)
        colAcciones.setCellFactory(crearBotonesAcciones());

        tablaEmpleados.setItems(listaEmpleados);
    }

    /**
     * Método para crear los botones de acciones (editar y eliminar)
     * @return
     */
    private Callback<TableColumn<Usuario, String>, TableCell<Usuario, String>> crearBotonesAcciones() {
        return new Callback<TableColumn<Usuario, String>, TableCell<Usuario, String>>() {
            @Override
            public TableCell<Usuario, String> call(final TableColumn<Usuario, String> param) {
                return new TableCell<Usuario, String>() {
                    private final Button btnEditar = new Button("Editar");
                    private final Button btnEliminar = new Button("Eliminar");
                    private final HBox pane = new HBox(5, btnEditar, btnEliminar);

                    {
                        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                        btnEditar.setOnAction(event -> {
                            Usuario usuario = getTableView().getItems().get(getIndex());
                            editarEmpleado(usuario);
                        });

                        btnEliminar.setOnAction(event -> {
                            Usuario usuario = getTableView().getItems().get(getIndex());
                            eliminarEmpleado(usuario);
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
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;

        // Cargar la lista de empleados
        cargarEmpleados();
    }

    /**
     * Método para cargar la lista de empleados
     */
    private void cargarEmpleados() {
        try {
            // Obtener la lista de empleados a través del ModelFactoryController
            List<Usuario> empleados = ModelFactoryController.getInstance().getSistema().obtenerEmpleados();

            // Limpiar y agregar los empleados a la lista observable
            listaEmpleados.clear();
            listaEmpleados.addAll(empleados);

            // Actualizar el contador de empleados
            actualizarContadorEmpleados();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar los empleados: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para actualizar el contador de empleados
     */
    private void actualizarContadorEmpleados() {
        lblTotalEmpleados.setText("Total de empleados: " + listaEmpleados.size());
    }

    /**
     * Método para buscar un empleado
     * @param event
     */
    @FXML
    void buscarEmpleado(ActionEvent event) {
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            // Si el campo de búsqueda está vacío, mostrar todos los empleados
            tablaEmpleados.setItems(listaEmpleados);
            return;
        }

        // Filtrar los empleados que coincidan con la búsqueda
        ObservableList<Usuario> empleadosFiltrados = FXCollections.observableArrayList();

        for (Usuario usuario : listaEmpleados) {
            // Buscar por nombre, apellido o identificación
            if (usuario.getNombre().toLowerCase().contains(textoBusqueda) ||
                    usuario.getApellido().toLowerCase().contains(textoBusqueda) ||
                    usuario.getIdentificacion().toLowerCase().contains(textoBusqueda) ||
                    usuario.getEmail().toLowerCase().contains(textoBusqueda)) {
                empleadosFiltrados.add(usuario);
            }
        }

        // Actualizar la tabla con los resultados
        tablaEmpleados.setItems(empleadosFiltrados);
        lblTotalEmpleados.setText("Resultados: " + empleadosFiltrados.size());
    }

    /**
     * Método para mostrar el formulario de agregar empleado
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
     * Método para editar un empleado
     * @param usuario
     */
    private void editarEmpleado(Usuario usuario) {
        // Establecer el empleado seleccionado
        empleadoSeleccionado = usuario;

        // Cargar los datos en el formulario
        txtIdentificacion.setText(usuario.getIdentificacion());
        txtIdentificacion.setDisable(true); // No permitir editar la identificación
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtEmail.setText(usuario.getEmail());
        txtPassword.setText(""); // No mostrar la contraseña actual por seguridad

        // Establecer el estado
        comboEstado.setValue(usuario.isCuentaVerificada() ? "Activo" : "Inactivo");

        // Establecer la fecha de contratación si es un empleado
        if (usuario instanceof Empleado) {
            Empleado empleado = (Empleado) usuario;
            if (empleado.getFechaContratacion() != null) {
                dateFechaContratacion.setValue(empleado.getFechaContratacion());
            }
        }

        // Establecer modo de edición
        modoEdicion = true;

        // Mostrar el panel de formulario
        panelFormulario.setVisible(true);
    }

    /**
     * Método para eliminar un empleado
     * @param usuario
     */
    private void eliminarEmpleado(Usuario usuario) {
        try {
            // Confirmar la eliminación
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Está seguro de eliminar este empleado?");
            alert.setContentText("Esta acción no se puede deshacer.");

            if (alert.showAndWait().get().getButtonData().isCancelButton()) {
                return; // El usuario canceló la operación
            }

            // Eliminar el empleado a través del ModelFactoryController
            Respuesta<Usuario> respuesta = ModelFactoryController.getInstance().getSistema().eliminarUsuario(usuario.getIdentificacion());

            if (respuesta.isExito()) {
                // Eliminar el empleado de la lista y actualizar la tabla
                listaEmpleados.remove(usuario);
                actualizarContadorEmpleados();
                mostrarAlerta("Eliminación exitosa", "El empleado ha sido eliminado correctamente.", AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", respuesta.getMensaje(), AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al eliminar el empleado: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para guardar un empleado (agregar o actualizar)
     * @param event
     */
    @FXML
    void guardarEmpleado(ActionEvent event) {
        // Validar los datos del formulario
        if (!validarDatosFormulario()) {
            return;
        }

        try {
            // Crear un objeto Usuario con los datos del formulario
            Usuario usuario = new Usuario();
            usuario.setIdentificacion(txtIdentificacion.getText().trim());
            usuario.setNombre(txtNombre.getText().trim());
            usuario.setApellido(txtApellido.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setRol(Rol.EMPLEADO);
            usuario.setCuentaVerificada(comboEstado.getValue().equals("Activo"));

            // Si es modo edición, no se requiere contraseña si el campo está vacío
            if (modoEdicion) {
                if (!txtPassword.getText().isEmpty()) {
                    usuario.setPassword(txtPassword.getText());
                } else {
                    // Conservar la contraseña actual
                    usuario.setPassword(empleadoSeleccionado.getPassword());
                }

                // Actualizar el empleado
                Respuesta<Usuario> respuesta = ModelFactoryController.getInstance().getSistema().actualizarUsuario(usuario);

                if (respuesta.isExito()) {
                    // Actualizar la lista de empleados
                    cargarEmpleados();

                    // Ocultar el formulario
                    panelFormulario.setVisible(false);

                    mostrarAlerta("Actualización exitosa", "El empleado ha sido actualizado correctamente.", AlertType.INFORMATION);
                } else {
                    lblError.setText(respuesta.getMensaje());
                }
            } else {
                // Modo agregar, se requiere contraseña
                if (txtPassword.getText().isEmpty()) {
                    lblError.setText("La contraseña es obligatoria para agregar un empleado.");
                    txtPassword.requestFocus();
                    return;
                }

                usuario.setPassword(txtPassword.getText());

                // Registrar el nuevo empleado
                Respuesta<Usuario> respuesta = ModelFactoryController.getInstance().getSistema().registrarUsuario(usuario);

                if (respuesta.isExito()) {
                    // Actualizar la lista de empleados
                    cargarEmpleados();

                    // Ocultar el formulario
                    panelFormulario.setVisible(false);

                    mostrarAlerta("Registro exitoso", "El empleado ha sido registrado correctamente.", AlertType.INFORMATION);
                } else {
                    lblError.setText(respuesta.getMensaje());
                }
            }
        } catch (SQLException e) {
            lblError.setText("Error al guardar el empleado: " + e.getMessage());
        }
    }

    /**
     * Método para validar los datos del formulario
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

        // Validar estado
        if (comboEstado.getValue() == null) {
            lblError.setText("El estado es obligatorio.");
            comboEstado.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Método para cancelar el formulario
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
        txtPassword.setText("");
        comboEstado.setValue("Activo");
        dateFechaContratacion.setValue(LocalDate.now());
        lblError.setText("");
        empleadoSeleccionado = null;
    }

    /**
     * Método para volver a la pantalla anterior
     * @param event
     */
    @FXML
    void volver(ActionEvent event) {
        try {
            // Cargar la pantalla anterior (depende del flujo de la aplicación)
            // Por ahora, asumimos que volvemos al panel de administrador
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Administrador.fxml")
            );
            Parent root = loader.load();

            AdministradorController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.inicializarInformacion();

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al volver a la pantalla anterior: " + e.getMessage(), AlertType.ERROR);
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