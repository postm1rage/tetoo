package com.example.tetoo.gui.controllers.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class LogoLoader {

    public static void loadLogo(ImageView logoImageView) {
        try {
            Image logo = new Image(LogoLoader.class.getResourceAsStream("/images/tetoo_logo.png"));
            logoImageView.setImage(logo);
            logoImageView.setFitWidth(70);
            logoImageView.setFitHeight(70);
            logoImageView.setPreserveRatio(true);
            StackPane.setAlignment(logoImageView, Pos.TOP_LEFT);
            StackPane.setMargin(logoImageView, new Insets(25,0,0,25));

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#ec4b6e").deriveColor(0,1,1,0.3));
            shadow.setRadius(5);
            logoImageView.setEffect(shadow);

        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
            logoImageView.setVisible(false);
        }
    }
}
