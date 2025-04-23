package co.edu.uniquindio.agenciaturistica.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.mail.MessagingException;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VerificarCuentaController implements Initializable {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnIrLogin;

    @FXML
    private Button btnReenviarCodigo;

    @FXML
    private Button btnVerificar;

    @FXML
    private VBox formularioContainer;

    @FXML
    private Label lblError;

    @FXML
    private Label lblInstrucciones;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtEmail;

    @FXML
    private VBox verificacionExitosaContainer;

    private Aplicacion aplicacion;
    private String emailPreCargado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización del controlador
    }

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    /**
     * Método para precargar el email (cuando se viene de registro)
     * @param email
     */
    public void precargarEmail(String email) {
        this.emailPreCargado = email;
        txtEmail.setText(email);
        txtEmail.setEditable(false); // No permitir editar el email precargado
        txtCodigo.requestFocus();
    }

    /**
     * Método para verificar la cuenta
     * @param event
     */
    @FXML
    void verificarCuenta(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String codigo = txtCodigo.getText().trim();

        // Validar campos
        if (email.isEmpty() || codigo.isEmpty()) {
            lblError.setText("Por favor complete todos los campos");
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
            // Verificar la cuenta a través del ModelFactoryController
            Respuesta<Usuario> respuesta = ModelFactoryController.getInstance()
                    .getSistema().verificarCuenta(email, codigo);

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                formularioContainer.setVisible(false);
                verificacionExitosaContainer.setVisible(true);
                lblError.setVisible(false);
            } else {
                // Mostrar mensaje de error
                lblError.setText(respuesta.getMensaje());
                lblError.setVisible(true);
            }
        } catch (SQLException e) {
            lblError.setText("Error al verificar la cuenta: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    /**
     * Método para reenviar el código de verificación
     * @param event
     */
    @FXML
    void reenviarCodigo(ActionEvent event) {
        String email = txtEmail.getText().trim();

        // Validar email
        if (email.isEmpty()) {
            lblError.setText("Por favor ingrese su correo electrónico");
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
            // Verificar si el email existe
            boolean existeEmail = ModelFactoryController.getInstance().getSistema().verificarExistenciaEmail(email);
            if (!existeEmail) {
                lblError.setText("El correo electrónico no está registrado en el sistema");
                lblError.setVisible(true);
                return;
            }

            // Enviar código de verificación
            Respuesta<String> respuesta = ModelFactoryController.getInstance()
                    .getSistema().enviarCodigoVerificacion(email);

            if (respuesta.isExito()) {
                // Mostrar mensaje de éxito
                mostrarAlerta(
                        "Código enviado",
                        "Se ha enviado un nuevo código de verificación a tu correo electrónico",
                        AlertType.INFORMATION
                );
                lblError.setVisible(false);
                txtCodigo.requestFocus();
            } else {
                // Mostrar mensaje de error
                lblError.setText(respuesta.getMensaje());
                lblError.setVisible(true);
            }
        } catch (MessagingException | IOException | SQLException e) {
            lblError.setText("Error al enviar el código: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    /**
     * Método para ir a la pantalla de login
     * @param event
     */
    @FXML
    void irAlLogin(ActionEvent event) {
        try {
            // Volver a la pantalla de login
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Login.fxml")
            );
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAplicacion(aplicacion);

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnIrLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(
                    "Error",
                    "Error al cargar la pantalla de login: " + e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

    /**
     * Método para cancelar la operación y volver a la pantalla anterior
     * @param event
     */
    @FXML
    void cancelar(ActionEvent event) {
        try {
            // Volver a la pantalla de login
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Login.fxml")
            );
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setAplicacion(aplicacion);

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(
                    "Error",
                    "Error al cargar la pantalla de login: " + e.getMessage(),
                    AlertType.ERROR
            );
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