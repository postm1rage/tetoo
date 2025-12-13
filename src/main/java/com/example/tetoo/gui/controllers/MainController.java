package com.example.tetoo.gui.controllers;

import com.example.tetoo.gui.controllers.anim.BackgroundAnimator;
import com.example.tetoo.gui.controllers.ui.ButtonStyler;
import com.example.tetoo.gui.controllers.ui.LogoLoader;
import com.example.tetoo.util.FontLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane mainContainer;
    @FXML private ImageView logoImageView;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private VBox mainContent;
    @FXML private VBox buttonsContainer;

    private BackgroundAnimator backgroundAnimator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FontLoader.loadCustomFont();

        backgroundAnimator = new BackgroundAnimator(mainContainer, mainContent, logoImageView);
        backgroundAnimator.play();

        ButtonStyler.setupButtonStyles(registerButton, loginButton, mainContent, buttonsContainer);
        LogoLoader.loadLogo(logoImageView);
    }

    @FXML
    private void handleRegister() {
        ButtonStyler.animateClick(registerButton);
        System.out.println("Registration button pressed");
    }

    @FXML
    private void handleLogin() {
        ButtonStyler.animateClick(loginButton);
        System.out.println("Login button pressed");
    }

    public void stopAnimations() {
        if (backgroundAnimator != null) backgroundAnimator.stop();
    }
}
