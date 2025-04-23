package co.edu.uniquindio.agenciaturistica.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.mail.MessagingException;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.collections.FXCollections;
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistroUsuarioController implements Initializable {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnIrLogin;

    @FXML
    private Button btnRegistrar;

    @FXML
    private ComboBox<String> comboTipoUsuario;

    @FXML
    private Label lblError;

    @FXML
    private TextField txtApellido;

    @FXML
    private PasswordField txtConfirmarPassword;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private PasswordField txtPassword;

    private Aplicacion aplicacion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar el combo de tipo de usuario
        comboTipoUsuario.setItems(FXCollections.observableArrayList("Cliente", "Empleado"));
        comboTipoUsuario.setValue("Cliente"); // Valor por defecto
    }

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    /**
     * Método para registrar un nuevo usuario
     * @param event
     */
    @FXML
    void registrarUsuario(ActionEvent event) {
        // Ocultar mensaje de error previo
        lblError.setVisible(false);

        // Validar los datos del formulario
        if (!validarDatosFormulario()) {
            return;
        }

        try {
            // Crear objeto Usuario con los datos del formulario
            Usuario usuario = new Usuario();
            usuario.setIdentificacion(txtIdentificacion.getText().trim());
            usuario.setNombre(txtNombre.getText().trim());
            usuario.setApellido(txtApellido.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            usuario.setPassword(txtPassword.getText());

            // Establecer el rol según la selección del combo
            String tipoSeleccionado = comboTipoUsuario.getValue();
            if ("Empleado".equals(tipoSeleccionado)) {
                usuario.setRol(Rol.EMPLEADO);
            } else {
                usuario.setRol(Rol.CLIENTE);
            }

            // Registrar usuario a través del ModelFactoryController
            Respuesta<Usuario> respuesta = ModelFactoryController.getInstance()
                    .getSistema().registrarUsuario(usuario);

            if (respuesta.isExito()) {
                // Enviar código de verificación
                String email = usuario.getEmail();
                try {
                    Respuesta<String> respuestaVerificacion = ModelFactoryController.getInstance()
                            .getSistema().enviarCodigoVerificacion(email);

                    if (respuestaVerificacion.isExito()) {
                        // Mostrar mensaje de éxito y redirigir a la pantalla de verificación
                        mostrarAlerta(
                                "Registro exitoso",
                                "Te has registrado correctamente. Por favor, verifica tu cuenta con el código enviado a tu correo electrónico.",
                                AlertType.INFORMATION
                        );

                        abrirPantallaVerificacion(email);
                    } else {
                        mostrarAlerta(
                                "Advertencia",
                                "Te has registrado correctamente, pero hubo un problema al enviar el código de verificación: " +
                                        respuestaVerificacion.getMensaje() + ". Intenta verificar tu cuenta más tarde.",
                                AlertType.WARNING
                        );

                        // Volver a la pantalla de login
                        irAlLogin(event);
                    }
                } catch (MessagingException | IOException e) {
                    mostrarAlerta(
                            "Advertencia",
                            "Te has registrado correctamente, pero hubo un problema al enviar el código de verificación: " +
                                    e.getMessage() + ". Intenta verificar tu cuenta más tarde.",
                            AlertType.WARNING
                    );

                    // Volver a la pantalla de login
                    irAlLogin(event);
                }
            } else {
                // Mostrar mensaje de error
                lblError.setText(respuesta.getMensaje());
                lblError.setVisible(true);
            }
        } catch (SQLException e) {
            lblError.setText("Error al registrar usuario: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    /**
     * Método para validar los datos del formulario
     * @return true si los datos son válidos, false en caso contrario
     */
    private boolean validarDatosFormulario() {
        // Validar identificación
        if (txtIdentificacion.getText().trim().isEmpty()) {
            lblError.setText("La identificación es obligatoria");
            lblError.setVisible(true);
            txtIdentificacion.requestFocus();
            return false;
        }

        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            lblError.setText("El nombre es obligatorio");
            lblError.setVisible(true);
            txtNombre.requestFocus();
            return false;
        }

        // Validar apellido
        if (txtApellido.getText().trim().isEmpty()) {
            lblError.setText("El apellido es obligatorio");
            lblError.setVisible(true);
            txtApellido.requestFocus();
            return false;
        }

        // Validar email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            lblError.setText("El correo electrónico es obligatorio");
            lblError.setVisible(true);
            txtEmail.requestFocus();
            return false;
        }

        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            lblError.setText("El formato del correo electrónico no es válido");
            lblError.setVisible(true);
            txtEmail.requestFocus();
            return false;
        }

        // Validar contraseña
        String password = txtPassword.getText();
        if (password.isEmpty()) {
            lblError.setText("La contraseña es obligatoria");
            lblError.setVisible(true);
            txtPassword.requestFocus();
            return false;
        }

        // Validar longitud de contraseña
        if (password.length() < 6) {
            lblError.setText("La contraseña debe tener al menos 6 caracteres");
            lblError.setVisible(true);
            txtPassword.requestFocus();
            return false;
        }

        // Validar confirmación de contraseña
        String confirmarPassword = txtConfirmarPassword.getText();
        if (confirmarPassword.isEmpty()) {
            lblError.setText("Debe confirmar la contraseña");
            lblError.setVisible(true);
            txtConfirmarPassword.requestFocus();
            return false;
        }

        // Validar que las contraseñas coincidan
        if (!password.equals(confirmarPassword)) {
            lblError.setText("Las contraseñas no coinciden");
            lblError.setVisible(true);
            txtConfirmarPassword.requestFocus();
            return false;
        }

        // Validar tipo de usuario
        if (comboTipoUsuario.getValue() == null) {
            lblError.setText("Debe seleccionar un tipo de usuario");
            lblError.setVisible(true);
            comboTipoUsuario.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Método para abrir la pantalla de verificación de cuenta
     * @param email
     */
    private void abrirPantallaVerificacion(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/VerificarCuenta.fxml")
            );
            Parent root = loader.load();

            VerificarCuentaController controller = loader.getController();
            controller.setAplicacion(aplicacion);
            controller.precargarEmail(email);

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnRegistrar.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta(
                    "Error",
                    "Error al abrir la pantalla de verificación: " + e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

    /**
     * Método para ir a la pantalla de login
     * @param event
     */
    @FXML
    void irAlLogin(ActionEvent event) {
        try {
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
                    "Error al abrir la pantalla de login: " + e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

    /**
     * Método para cancelar el registro y volver a la pantalla de login
     * @param event
     */
    @FXML
    void cancelar(ActionEvent event) {
        irAlLogin(event);
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