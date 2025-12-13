module com.example.tetoo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media; // ← добавьте эту строку

    opens com.example.tetoo.app to javafx.graphics;
    opens com.example.tetoo.gui.controllers to javafx.fxml;
    opens com.example.tetoo.gui.views to javafx.graphics;

    exports com.example.tetoo.app;
    exports com.example.tetoo.gui.controllers;
    exports com.example.tetoo.gui.views;
    exports com.example.tetoo.util;
}