module com.example.tetoo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    
    // Добавляем зависимости для Supabase
    requires com.google.gson;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires ch.qos.logback.classic;
    requires java.sql;
    
    // Открываем пакеты для рефлексии
    opens com.example.tetoo.app to javafx.graphics;
    opens com.example.tetoo.gui.controllers to javafx.fxml;
    opens com.example.tetoo.gui.views to javafx.graphics;
    opens com.example.tetoo.model to com.google.gson, javafx.base;
    opens com.example.tetoo.service to javafx.fxml;
    opens com.example.tetoo.config to javafx.fxml;
    opens com.example.tetoo.repository to javafx.fxml;
    
    // Экспортируем публичные API
    exports com.example.tetoo.app;
    exports com.example.tetoo.gui.controllers;
    exports com.example.tetoo.gui.views;
    exports com.example.tetoo.util;
    exports com.example.tetoo.model;
    exports com.example.tetoo.service;
    exports com.example.tetoo.config;
}