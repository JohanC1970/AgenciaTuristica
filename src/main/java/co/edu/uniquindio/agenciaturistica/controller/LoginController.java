package co.edu.uniquindio.agenciaturistica.controller;

import java.net.URL;
import java.util.ResourceBundle;

import co.edu.uniquindio.agenciaturistica.application.Aplicacion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private HBox registerContainer;

    @FXML
    private Hyperlink forgotPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private HBox optionsContainer;

    @FXML
    private VBox headerContaine;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    private Aplicacion aplicacion;

    private Stage stage;

    public Aplicacion getAplicacion() {
        return aplicacion;
    }

    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }


    @FXML
    void iniciarSesion(ActionEvent event) {

        String password = txtPassword.getText();
        String email = txtEmail.getText();

        if(validarCamposTexto(password, email)) {

            //Falta la logica para iniciar sesion
        } else {
            // Mostrar mensaje de error
            System.out.println("Por favor complete todos los campos");
        }
    }

    private boolean validarCamposTexto(String password, String email) {
        if(password.isEmpty() || email.isEmpty()) {
            return false;
        }
        return true;
    }

    @FXML
    void mostrarVentanaRecuperarPassword(ActionEvent event) {

    }

}
