module co.edu.uniquindio.agenciaturistica {  // ¡Este nombre debe usarse en todas partes!
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.agenciaturistica.application to javafx.fxml;
    opens co.edu.uniquindio.agenciaturistica.controller to javafx.fxml;

    exports co.edu.uniquindio.agenciaturistica.application;
    exports co.edu.uniquindio.agenciaturistica.controller;
}