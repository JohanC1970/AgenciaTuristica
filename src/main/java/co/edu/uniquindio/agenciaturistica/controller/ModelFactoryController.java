package co.edu.uniquindio.agenciaturistica.controller;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import co.edu.uniquindio.agenciaturistica.model.Sistema;

/**
 * Clase que implementa el patrón Singleton para el controlador de la fábrica de modelos.
 * Gestiona la conexión entre la interfaz de usuario y el modelo de la aplicación.
 */
public class ModelFactoryController {

    private static final Logger LOGGER = Logger.getLogger(ModelFactoryController.class.getName());

    private static ModelFactoryController instancia;
    private Sistema sistema;

    /**
     * Constructor privado para implementar patrón Singleton
     */
    private ModelFactoryController() {
        inicializarSistema();
    }

    /**
     * Método estático para obtener la única instancia del controlador
     * @return Instancia única del ModelFactoryController
     */
    public static ModelFactoryController getInstance() {
        if (instancia == null) {
            instancia = new ModelFactoryController();
        }
        return instancia;
    }

    /**
     * Método para inicializar el sistema
     */
    private void inicializarSistema() {
        try {
            LOGGER.log(Level.INFO, "Inicializando el sistema...");
            sistema = new Sistema("Agencia Turística");
            LOGGER.log(Level.INFO, "Sistema inicializado correctamente");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar el sistema", e);
        }
    }

    /**
     * Método para obtener el sistema
     * @return Sistema inicializado
     */
    public Sistema getSistema() {
        return sistema;
    }

    /**
     * Método para obtener una nueva instancia del sistema
     * Útil cuando se necesita una conexión fresca a la base de datos
     * @return Nueva instancia del sistema
     */
    public Sistema getSistemaFresco() {
        try {
            LOGGER.log(Level.INFO, "Creando nueva instancia del sistema...");
            return new Sistema("Agencia Turística");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al crear nueva instancia del sistema", e);
            return null;
        }
    }
}