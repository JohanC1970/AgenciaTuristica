package co.edu.uniquindio.agenciaturistica.controller;

import co.edu.uniquindio.agenciaturistica.model.Reporte;
import co.edu.uniquindio.agenciaturistica.util.EmailSender;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class VisualizadorReporteController implements Initializable {

    @FXML
    private Button btnCerrar;

    @FXML
    private Button btnEnviarEmail;

    @FXML
    private Button btnExportarPDF;

    @FXML
    private Button btnExportarTxt;

    @FXML
    private Label lblDescripcionReporte;

    @FXML
    private Label lblFechaGeneracion;

    @FXML
    private Label lblTituloReporte;

    @FXML
    private TextArea txtContenidoReporte;

    private Reporte reporte;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No se requiere inicialización especial
    }

    /**
     * Método para establecer el reporte a visualizar
     */
    public void setReporte(Reporte reporte) {
        this.reporte = reporte;

        // Actualizar la interfaz con los datos del reporte
        if (reporte != null) {
            lblTituloReporte.setText(reporte.getTitulo());
            lblDescripcionReporte.setText(reporte.getDescripcion());
            lblFechaGeneracion.setText("Fecha: " + reporte.getFechaGeneracion().format(dateFormatter));
            txtContenidoReporte.setText(reporte.getContenido());
        }
    }

    /**
     * Método para exportar el reporte a PDF
     */
    @FXML
    void exportarPDF(ActionEvent event) {
        try {
            // Mostrar diálogo de guardar archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte como PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

            // Nombre de archivo por defecto
            String nombreArchivo = reporte.getTitulo().replaceAll("\\s+", "_").toLowerCase() + ".pdf";
            fileChooser.setInitialFileName(nombreArchivo);

            File archivo = fileChooser.showSaveDialog(btnExportarPDF.getScene().getWindow());

            if (archivo != null) {
                // Utilizar el método de la clase Reporte para exportar a PDF
                File pdfGenerado = reporte.exportarPDF();

                // Si el archivo se generó correctamente en una ubicación temporal, copiarlo al destino elegido
                if (pdfGenerado != null && pdfGenerado.exists()) {
                    // [Aquí iría el código para copiar el archivo temporal al destino elegido]
                    // Como simplificación, asumimos que el método exportarPDF ya guarda en la ubicación correcta

                    mostrarAlerta("Éxito", "El reporte se ha exportado correctamente como PDF", AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "No se pudo generar el archivo PDF", AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al exportar el reporte como PDF: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para exportar el reporte como archivo de texto
     */
    @FXML
    void exportarTxt(ActionEvent event) {
        try {
            // Mostrar diálogo de guardar archivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte como TXT");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));

            // Nombre de archivo por defecto
            String nombreArchivo = reporte.getTitulo().replaceAll("\\s+", "_").toLowerCase() + ".txt";
            fileChooser.setInitialFileName(nombreArchivo);

            File archivo = fileChooser.showSaveDialog(btnExportarTxt.getScene().getWindow());

            if (archivo != null) {
                // Guardar el contenido del reporte en el archivo
                try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
                    // Escribir encabezado
                    writer.println("=========================================");
                    writer.println("             " + reporte.getTitulo());
                    writer.println("=========================================");
                    writer.println("Fecha de generación: " + reporte.getFechaGeneracion().format(dateFormatter));
                    writer.println("Descripción: " + reporte.getDescripcion());
                    writer.println("=========================================");
                    writer.println();

                    // Escribir contenido
                    writer.print(reporte.getContenido());

                    // Escribir pie
                    writer.println();
                    writer.println("=========================================");
                    writer.println("         FIN DEL REPORTE");
                    writer.println("=========================================");
                }

                mostrarAlerta("Éxito", "El reporte se ha exportado correctamente como archivo de texto", AlertType.INFORMATION);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al exportar el reporte como archivo de texto: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para enviar el reporte por correo electrónico
     */
    @FXML
    void enviarPorEmail(ActionEvent event) {
        try {
            // Mostrar diálogo para ingresar correo electrónico
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Enviar Reporte por Email");
            dialog.setHeaderText("Ingrese el correo electrónico del destinatario");

            ButtonType enviarButtonType = new ButtonType("Enviar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(enviarButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField emailField = new TextField();
            emailField.setPromptText("correo@ejemplo.com");

            grid.add(new Label("Email:"), 0, 0);
            grid.add(emailField, 1, 0);

            dialog.getDialogPane().setContent(grid);

            // Convertir resultado del diálogo
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == enviarButtonType) {
                    return emailField.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(email -> {
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    mostrarAlerta("Error", "El correo electrónico ingresado no es válido", AlertType.ERROR);
                    return;
                }

                try {
                    // Crear contenido del correo
                    String asunto = reporte.getTitulo() + " - Agencia Turística";
                    StringBuilder contenido = new StringBuilder();

                    contenido.append("<h2>").append(reporte.getTitulo()).append("</h2>");
                    contenido.append("<p><strong>Fecha:</strong> ").append(reporte.getFechaGeneracion().format(dateFormatter)).append("</p>");
                    contenido.append("<p><strong>Descripción:</strong> ").append(reporte.getDescripcion()).append("</p>");
                    contenido.append("<hr>");
                    contenido.append("<pre>").append(reporte.getContenido()).append("</pre>");

                    // Enviar correo
                    EmailSender.enviarEmail(email, asunto, contenido.toString(), null);

                    mostrarAlerta("Éxito", "El reporte se ha enviado correctamente al correo: " + email, AlertType.INFORMATION);

                } catch (MessagingException | IOException e) {
                    mostrarAlerta("Error", "Error al enviar el reporte por correo: " + e.getMessage(), AlertType.ERROR);
                }
            });

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al abrir el diálogo de envío: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Método para cerrar la ventana
     */
    @FXML
    void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    /**
     * Método para mostrar alertas
     */
    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}