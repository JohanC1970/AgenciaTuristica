package co.edu.uniquindio.agenciaturistica.application;

import co.edu.uniquindio.agenciaturistica.controller.LoginController;
import co.edu.uniquindio.agenciaturistica.model.Usuario;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Aplicacion extends Application {

    private Stage primaryStage;
    private Usuario usuarioActual;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Sistema de Gestión - Agencia Turística");
        mostrarVentanaPrincipal();
    }

    /**
     * Método para mostrar la ventana principal (Login)
     */
    public void mostrarVentanaPrincipal() {
        try {
            // Carga el FXML usando ruta absoluta desde resources
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/agenciaturistica/Login.fxml")
            );

            AnchorPane anchorPane = loader.load();
            LoginController loginController = loader.getController();
            loginController.setAplicacion(this);

            Scene scene = new Scene(anchorPane);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar el FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para cerrar sesión y volver a la ventana de login
     */
    public void cerrarSesion() {
        this.usuarioActual = null;
        mostrarVentanaPrincipal();
    }

    /**
     * @return El stage principal de la aplicación
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * @param primaryStage El stage principal a establecer
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * @return El usuario que ha iniciado sesión actualmente
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * @param usuarioActual El usuario que ha iniciado sesión a establecer
     */
    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    /**
     * Método principal
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }
}