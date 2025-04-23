package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import co.edu.uniquindio.agenciaturistica.model.Enums.Rol;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import co.edu.uniquindio.agenciaturistica.util.Respuesta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Hyperlink forgotPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private HBox optionsContainer;

    @FXML
    private VBox headerContainer;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    private Aplicacion aplicacion;

    /**
     * Método para establecer la aplicación principal
     * @param aplicacion
     */
    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    /**
     * Método para iniciar sesión en el sistema
     * @param event
     */
    @FXML
    void iniciarSesion(ActionEvent event) {
        String password = txtPassword.getText();
        String email = txtEmail.getText();

        if(validarCamposTexto(password, email)) {
            try {
                // Obtener la respuesta del sistema
                Respuesta<Usuario> respuesta = ModelFactoryController.getInstance()
                        .getSistema().iniciarSesion(email, password);

                if(respuesta.isExito()) {
                    // Obtener el usuario que inició sesión
                    Usuario usuario = respuesta.getData();

                    // Almacenar el usuario en la aplicación
                    aplicacion.setUsuarioActual(usuario);

                    // Mostrar la ventana correspondiente según el rol
                    switch(usuario.getRol()) {
                        case ADMINISTRADOR:
                            mostrarVentanaAdministrador();
                            break;
                        case EMPLEADO:
                            mostrarVentanaEmpleado();
                            break;
                        case CLIENTE:
                            mostrarVentanaCliente();
                            break;
                    }
                } else {
                    // Mostrar mensaje de error
                    lblError.setText(respuesta.getMensaje());
                    lblError.setVisible(true);
                }
            } catch (SQLException e) {
                mostrarAlerta(
                        "Error de conexión",
                        "No se pudo conectar con la base de datos. Intente nuevamente.",
                        AlertType.ERROR
                );
            }
        } else {
            lblError.setText("Por favor complete todos los campos");
            lblError.setVisible(true);
        }
    }

    /**
     * Método para validar los campos de texto
     * @param password
     * @param email
     * @return
     */
    private boolean validarCamposTexto(String password, String email) {
        return !password.isEmpty() && !email.isEmpty();
    }

    /**
     * Método para mostrar la ventana de recuperación de contraseña
     * @param event
     */
    @FXML
    void mostrarVentanaRecuperarPassword(ActionEvent event) {
        try {
            // Cargar el FXML de recuperación de contraseña
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/RecuperarPassword.fxml")
            );
            Parent root = loader.load();

            // Obtener el controlador
            RecuperarPasswordController controlador = loader.getController();
            controlador.setAplicacion(aplicacion);

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Recuperar Contraseña");
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "Error al abrir la ventana de recuperación de contraseña: " + e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

    /**
     * Método para mostrar la ventana de administrador
     */
    private void mostrarVentanaAdministrador() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Administrador.fxml")
            );
            Parent root = loader.load();

            AdministradorController controlador = loader.getController();
            controlador.setAplicacion(aplicacion);
            controlador.inicializarInformacion();

            Scene scene = new Scene(root);
            aplicacion.getPrimaryStage().setScene(scene);
            aplicacion.getPrimaryStage().setTitle("Panel de Administrador");
            aplicacion.getPrimaryStage().centerOnScreen();

        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "Error al abrir el panel de administrador: " + e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

    /**
     * Método para mostrar la ventana de empleado
     */
    private void mostrarVentanaEmpleado() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Empleado.fxml")
            );
            Parent root = loader.load();

            EmpleadoController controlador = loader.getController();
            controlador.setAplicacion(aplicacion);
            controlador.inicializarInformacion();

            Scene scene = new Scene(root);
            aplicacion.getPrimaryStage().setScene(scene);
            aplicacion.getPrimaryStage().setTitle("Panel de Empleado");
            aplicacion.getPrimaryStage().centerOnScreen();

        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "Error al abrir el panel de empleado: " + e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

    /**
     * Método para mostrar la ventana de cliente
     */
    private void mostrarVentanaCliente() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Cliente.fxml")
            );
            Parent root = loader.load();

            ClienteController controlador = loader.getController();
            controlador.setAplicacion(aplicacion);
            controlador.inicializarInformacion();

            Scene scene = new Scene(root);
            aplicacion.getPrimaryStage().setScene(scene);
            aplicacion.getPrimaryStage().setTitle("Panel de Cliente");
            aplicacion.getPrimaryStage().centerOnScreen();

        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "Error al abrir el panel de cliente: " + e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

    /**
     * Método para mostrar la ventana de registro de usuario
     * @param event
     */
    @FXML
    void mostrarVentanaRegistro(ActionEvent event) {
        try {
            // Cargar el FXML de registro
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/RegistroUsuario.fxml")
            );
            Parent root = loader.load();

            // Obtener el controlador
            RegistroUsuarioController controlador = loader.getController();
            controlador.setAplicacion(aplicacion);

            // Crear una nueva escena
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "Error al abrir la ventana de registro: " + e.getMessage(),
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