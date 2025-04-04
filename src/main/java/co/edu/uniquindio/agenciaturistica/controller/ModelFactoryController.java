package co.edu.uniquindio.agenciaturistica.controller;

import co.edu.uniquindio.agenciaturistica.model.Sistema;

public class ModelFactoryController {

    Sistema sistema = null;

    private static class SingletonHolder {
        private static final ModelFactoryController eINSTANCE = new ModelFactoryController();
    }

    public static ModelFactoryController getInstance() {
        return SingletonHolder.eINSTANCE;
    }

    public ModelFactoryController() {
        System.out.println("Invocacion de la clase singleton");
        sistema = new Sistema("Agencia Turistica");
    }

    public Sistema getSistema() {
        return sistema;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }

    //--------------------METODOS NECESARIOS PARA CONECTAR LA LOGICA CON LA INTERFAZ--------------------//



}