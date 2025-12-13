package com.example.tetoo.gui.controllers.ui;

import com.example.tetoo.util.FontLoader;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ButtonStyler {

    public static void setupButtonStyles(Button register, Button login, VBox mainContent, VBox buttonsContainer) {
        String font = FontLoader.getFontFamily();

        String registerStyle = String.format( "-fx-font-family: '%s';" + "-fx-font-size: 18px;" + 
                                            "-fx-font-weight: bold;" + "-fx-text-fill: white;" + 
                                            "-fx-background-color: #ec4b6e;" + "-fx-background-radius: 30;" + 
                                            "-fx-padding: 15 50;" + "-fx-border-radius: 30;" + "-fx-border-width: 0;" + 
                                            "-fx-cursor: hand;" + "-fx-effect: dropshadow(gaussian, rgba(236,75,110,0.5), 15, 0, 0, 5);", 
                                            font);

        String loginStyle = String.format(
                "-fx-font-family: '%s'; -fx-font-size: 16px; -fx-font-weight: bold;" +
                "-fx-text-fill: #ff8fa3; -fx-background-color: transparent; -fx-background-radius: 25;" +
                "-fx-padding: 12 45; -fx-border-radius: 25; -fx-border-width: 2; -fx-border-color: #ec4b6e; -fx-cursor: hand;", font);

        register.setStyle(registerStyle);
        login.setStyle(loginStyle);

        register.setOnMouseEntered(e -> register.setStyle(registerStyle + "-fx-background-color: #ff6b8b;-fx-scale-x:1.05;-fx-scale-y:1.05;"));
        register.setOnMouseExited(e -> { register.setStyle(registerStyle); register.setScaleX(1); register.setScaleY(1); });

        login.setOnMouseEntered(e -> login.setStyle(loginStyle + "-fx-text-fill: #ff6b8b;-fx-border-color:#ff6b8b;-fx-background-color:rgba(236,75,110,0.1);-fx-scale-x:1.05;-fx-scale-y:1.05;"));
        login.setOnMouseExited(e -> { login.setStyle(loginStyle); login.setScaleX(1); login.setScaleY(1); });

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), mainContent);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public static void animateClick(Button button) {
        ScaleTransition down = new ScaleTransition(Duration.millis(100), button);
        down.setToX(0.95); down.setToY(0.95);

        ScaleTransition up = new ScaleTransition(Duration.millis(100), button);
        up.setToX(1); up.setToY(1);

        new SequentialTransition(down, up).play();
    }
}
