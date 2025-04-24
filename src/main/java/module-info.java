module co.edu.uniquindio.agenciaturistica {  // ¡Este nombre debe usarse en todas partes!
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.mail;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens co.edu.uniquindio.agenciaturistica.application to javafx.fxml;
    opens co.edu.uniquindio.agenciaturistica.controller to javafx.fxml;
    opens co.edu.uniquindio.agenciaturistica.model to javafx.base;
    opens co.edu.uniquindio.agenciaturistica.model.Enums to javafx.base;
    opens co.edu.uniquindio.agenciaturistica.util to javafx.base;

    exports co.edu.uniquindio.agenciaturistica.application;
    exports co.edu.uniquindio.agenciaturistica.controller;
}