package com.example.tetoo.app;

import com.example.tetoo.gui.views.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private SplashScreen splashScreen;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Startup check"); // потом убрать


        splashScreen = new SplashScreen(primaryStage, () -> {
            System.out.println("Transition to the main screen..."); // потом убрать
            Platform.runLater(() -> showMainScreen(primaryStage));
        });
        splashScreen.show();
    }

    private void showMainScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);

            // Иконка
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/tetoo_icon.png")));
            } catch (Exception e) {
                System.err.println("Не удалось загрузить иконку");
            }

            stage.setTitle("TETOO");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            System.err.println("Ошибка загрузки главного экрана: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (splashScreen != null) {
            splashScreen.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}