package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Cliente;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormularioClienteController implements Initializable {

    @FXML
    private Label lblTitulo;

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtTelefono;

    @FXML
    private DatePicker dateFechaNacimiento;

    @FXML
    private Label lblError;

    @FXML
    private Button btnGuardar;

    private Aplicacion aplicacion;
    private boolean modoEdicion = false;
    private Cliente cliente;

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
     * Método para establecer el modo de edición
     * @param modoEdicion true si se está editando un cliente existente, false si es un nuevo cliente
     */
    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;

        if (modoEdicion) {
            lblTitulo.setText("Editar Cliente");
            btnGuardar.setText("Actualizar");
            txtIdentificacion.setDisable(true); // No permitir cambiar la identificación
        } else {
            lblTitulo.setText("Nuevo Cliente");
            btnGuardar.setText("Guardar");
            txtIdentificacion.setDisable(false);
        }
    }

    /**
     * Método para establecer el cliente a editar
     * @param cliente
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Método para inicializar los datos del cliente en el formulario
     */
    public void inicializarDatosCliente() {
        if (cliente != null) {
            txtIdentificacion.setText(cliente.getIdentificacion());
            txtNombre.setText(cliente.getNombre());
            txtApellido.setText(cliente.getApellido());
            txtCorreo.setText(cliente.getCorreo());
            txtTelefono.setText(cliente.getTelefono());
            dateFechaNacimiento.setValue(cliente.getFechaNacimiento());
        }
    }

    /**
     * Método para guardar o actualizar un cliente
     * @param event
     */
    @FXML
    void guardarCliente(ActionEvent event) {
        try {
            // Validar campos
            if (!validarCampos()) {
                return;
            }

            // Obtener datos del formulario
            String identificacion = txtIdentificacion.getText().trim();
            String nombre = txtNombre.getText().trim();
            String apellido = txtApellido.getText().trim();
            String correo = txtCorreo.getText().trim();
            String telefono = txtTelefono.getText().trim();
            LocalDate fechaNacimiento = dateFechaNacimiento.getValue();

            // Crear objeto cliente
            Cliente nuevoCliente = new Cliente(nombre, apellido, identificacion, correo, telefono, fechaNacimiento);

            Respuesta<Cliente> respuesta;

            if (modoEdicion) {
                // Actualizar cliente existente
                respuesta = ModelFactoryController.getInstance().getSistema().actualizarCliente(nuevoCliente);
            } else {
                // Registrar nuevo cliente
                respuesta = ModelFactoryController.getInstance().getSistema().registrarCliente(nuevoCliente);
            }

            if (respuesta.isExito()) {
                mostrarAlerta("Éxito", respuesta.getMensaje(), AlertType.INFORMATION);
                cerrarVentana();
            } else {
                lblError.setText(respuesta.getMensaje());
            }

        } catch (SQLException e) {
            lblError.setText("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            lblError.setText("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Método para validar los campos del formulario
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        // Validar identificación
        if (txtIdentificacion.getText().trim().isEmpty()) {
            errores.append("La identificación es obligatoria\n");
        } else if (!txtIdentificacion.getText().trim().matches("\\d+")) {
            errores.append("La identificación debe contener solo números\n");
        }

        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("El nombre es obligatorio\n");
        }

        // Validar apellido
        if (txtApellido.getText().trim().isEmpty()) {
            errores.append("El apellido es obligatorio\n");
        }

        // Validar correo
        if (txtCorreo.getText().trim().isEmpty()) {
            errores.append("El correo electrónico es obligatorio\n");
        } else if (!txtCorreo.getText().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errores.append("El formato del correo electrónico no es válido\n");
        }

        // Validar teléfono
        if (txtTelefono.getText().trim().isEmpty()) {
            errores.append("El teléfono es obligatorio\n");
        } else if (!txtTelefono.getText().trim().matches("\\d{7,10}")) {
            errores.append("El teléfono debe contener entre 7 y 10 dígitos\n");
        }

        // Validar fecha de nacimiento
        if (dateFechaNacimiento.getValue() == null) {
            errores.append("La fecha de nacimiento es obligatoria\n");
        } else if (dateFechaNacimiento.getValue().isAfter(LocalDate.now())) {
            errores.append("La fecha de nacimiento no puede ser en el futuro\n");
        }

        // Si hay errores, mostrarlos y retornar false
        if (errores.length() > 0) {
            lblError.setText(errores.toString());
            return false;
        }

        return true;
    }

    /**
     * Método para cancelar la operación y cerrar la ventana
     * @param event
     */
    @FXML
    void cancelar(ActionEvent event) {
        cerrarVentana();
    }

    /**
     * Método para cerrar la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
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