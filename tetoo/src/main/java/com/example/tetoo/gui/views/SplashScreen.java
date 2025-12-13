package com.example.tetoo.gui.views;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {
    private Stage stage;
    private Runnable onComplete;

    public SplashScreen(Stage stage, Runnable onComplete) {
        this.stage = stage;
        this.onComplete = onComplete;
    }

    public void show() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #ec4b6e;");

        // Просто показываем логотип
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/tetoo_logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(400);
            logoView.setFitHeight(400);
            logoView.setPreserveRatio(true);
            logoView.setOpacity(0); // Начинаем невидимым

            StackPane.setAlignment(logoView, Pos.CENTER);
            root.getChildren().add(logoView);


            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), logoView);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            System.err.println("Не удалось загрузить логотип");
        }

        // Сцена
        Scene scene = new Scene(root, 800, 600);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);

        // Иконка
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/tetoo_icon.png")));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить иконку");
        }

        stage.centerOnScreen();
        stage.show();


        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), root);
        fadeOut.setDelay(Duration.seconds(2));
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> onComplete.run());
        fadeOut.play();
    }

    public void stop() {

    }
}