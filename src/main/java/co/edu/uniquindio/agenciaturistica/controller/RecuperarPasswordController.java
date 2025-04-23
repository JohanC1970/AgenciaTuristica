package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.mail.MessagingException;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.dao.UsuarioDAO;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RecuperarPasswordController implements Initializable {

    @FXML
    private Button btnCambiarPassword;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnSolicitarCodigo;

    @FXML
    private Button btnVerificarCodigo;

    @FXML
    private Label lblError;

    @FXML
    private Label lblInstrucciones;

    @FXML
    private VBox paso1Container;

    @FXML
    private VBox paso2Container;

    @FXML
    private VBox paso3Container;

    @FXML
    private PasswordField txtConfirmarPassword;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtNuevaPassword;

    private Aplicacion aplicacion;
    private String codigoGenerado;
    private String emailActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No necesitamos crear UsuarioDAO directamente, usaremos ModelFactoryController
    }

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    /**
     * Método para solicitar el código de recuperación
     * @param event
     */
    @FXML
    void solicitarCodigo(ActionEvent event) {
        String email = txtEmail.getText();

        if (email.isEmpty()) {
            lblError.setText("Por favor ingresa tu correo electrónico");
            lblError.setVisible(true);
            return;
        }

        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            lblError.setText("El formato del correo electrónico no es válido");
            lblError.setVisible(true);
            return;
        }

        try {
            // Verificar si el email existe usando ModelFactoryController
            boolean existeEmail = ModelFactoryController.getInstance().getSistema().verificarExistenciaEmail(email);
            if (!existeEmail) {
                lblError.setText("El correo electrónico no está registrado en el sistema");
                lblError.setVisible(true);
                return;
            }

            // Enviar código de recuperación
            Respuesta<String> respuesta = ModelFactoryController.getInstance()
                    .getSistema().enviarCodigoRecuperacion(email);

            if (respuesta.isExito()) {
                // Guardar el código y el email para usarlos después
                this.codigoGenerado = respuesta.getData();
                this.emailActual = email;

                // Ocultar paso 1 y mostrar paso 2
                paso1Container.setVisible(false);
                paso2Container.setVisible(true);
                lblInstrucciones.setText("Hemos enviado un código a tu correo. Por favor, ingrésalo a continuación:");
                lblError.setVisible(false);
            } else {
                lblError.setText(respuesta.getMensaje());
                lblError.setVisible(true);
            }

        } catch (Exception e) {
            lblError.setText("Error al enviar el código: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    /**
     * Método para verificar el código ingresado
     * @param event
     */
    @FXML
    void verificarCodigo(ActionEvent event) {
        String codigo = txtCodigo.getText();

        if (codigo.isEmpty()) {
            lblError.setText("Por favor ingresa el código de verificación");
            lblError.setVisible(true);
            return;
        }

        // Verificar si el código coincide
        if (codigo.equals(codigoGenerado)) {
            // Ocultar paso 2 y mostrar paso 3
            paso2Container.setVisible(false);
            paso3Container.setVisible(true);
            lblInstrucciones.setText("Por favor, ingresa tu nueva contraseña:");
            lblError.setVisible(false);
        } else {
            lblError.setText("El código ingresado no es válido");
            lblError.setVisible(true);
        }
    }

    /**
     * Método para cambiar la contraseña
     * @param event
     */
    @FXML
    void cambiarPassword(ActionEvent event) {
        String nuevaPassword = txtNuevaPassword.getText();
        String confirmarPassword = txtConfirmarPassword.getText();

        if (nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
            lblError.setText("Por favor completa todos los campos");
            lblError.setVisible(true);
            return;
        }

        // Verificar que las contraseñas coincidan
        if (!nuevaPassword.equals(confirmarPassword)) {
            lblError.setText("Las contraseñas no coinciden");
            lblError.setVisible(true);
            return;
        }

        // Verificar que la contraseña cumpla con requisitos mínimos
        if (nuevaPassword.length() < 6) {
            lblError.setText("La contraseña debe tener al menos 6 caracteres");
            lblError.setVisible(true);
            return;
        }

        try {
            // Actualizar la contraseña en la base de datos
            // Aquí se necesita crear un método en UsuarioDAO para actualizar la contraseña
            // Este método no está implementado en el código proporcionado, por lo que hay que agregarlo
            Respuesta<String> respuesta = cambiarPasswordUsuario(emailActual, nuevaPassword);

            if (respuesta.isExito()) {
                mostrarAlerta(
                        "Contraseña actualizada",
                        "Tu contraseña ha sido actualizada correctamente",
                        AlertType.INFORMATION
                );

                // Cerrar la ventana
                ((Stage) btnCambiarPassword.getScene().getWindow()).close();
            } else {
                lblError.setText(respuesta.getMensaje());
                lblError.setVisible(true);
            }
        } catch (Exception e) {
            lblError.setText("Error al cambiar la contraseña: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    /**
     * Método para actualizar la contraseña del usuario
     * @param email
     * @param nuevaPassword
     * @return
     */
    private Respuesta<String> cambiarPasswordUsuario(String email, String nuevaPassword) {
        try {
            // Usar ModelFactoryController para actualizar la contraseña
            return ModelFactoryController.getInstance().getSistema().actualizarPassword(email, nuevaPassword);
        } catch (Exception e) {
            return new Respuesta<>(false, "Error al actualizar la contraseña: " + e.getMessage(), null);
        }
    }

    /**
     * Método para cancelar la operación y cerrar la ventana
     * @param event
     */
    @FXML
    void cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
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