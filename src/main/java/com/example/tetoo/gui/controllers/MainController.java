package com.example.tetoo.gui.controllers;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.animation.Interpolator;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane mainContainer;
    @FXML
    private ImageView logoImageView;

    @FXML
    private VBox overlay;
    @FXML
    private Label lblOverlayTitle;
    @FXML
    private Label lblOverlaySub;
    @FXML
    private Button btnOverlay;

    private boolean isSignInActive = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        overlay.setTranslateX(450);
        setOverlayContentForSignIn();
        loadLogo();
    }

    private void loadLogo() {
        try {

            Image logo = new Image(getClass().getResourceAsStream("/images/tetoo_logo.png"));
            logoImageView.setImage(logo);


            logoImageView.setFitWidth(100);
            logoImageView.setFitHeight(100);

            logoImageView.setPreserveRatio(true);
            StackPane.setAlignment(logoImageView, Pos.TOP_LEFT);
            StackPane.setMargin(logoImageView, new Insets(25, 0, 0, 25));
        } catch (Exception e) {
            System.err.println("Failed to load logo: " + e.getMessage());
            logoImageView.setVisible(false);
        }
    }


    @FXML
    private void toggleScreens() {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.7), overlay);
        slide.setInterpolator(Interpolator.EASE_BOTH);

        if (isSignInActive) {
            // Switch to REGISTRATION (overlay moves left)
            slide.setToX(0);
            isSignInActive = false;
            slide.setOnFinished(e -> setOverlayContentForSignUp());
        } else {
            // Switch to LOGIN (overlay moves right)
            slide.setToX(450);
            isSignInActive = true;
            slide.setOnFinished(e -> setOverlayContentForSignIn());
        }

        slide.play();
    }

    private void setOverlayContentForSignIn() {
        lblOverlayTitle.setText("Hello, friend!");
        lblOverlaySub.setText("");
        btnOverlay.setText("REGISTER");
    }

    private void setOverlayContentForSignUp() {
        lblOverlayTitle.setText("Welcome back!");
        lblOverlaySub.setText("");
        btnOverlay.setText("LOGIN");
    }

    @FXML
    private void handleLogin() {
        System.out.println("Login logic...");
    }

    @FXML
    private void handleRegister() {
        System.out.println("Registration logic...");
    }
}