package com.example.tetoo.gui.controllers;

import com.example.tetoo.gui.controllers.anim.BackgroundAnimator;
import com.example.tetoo.gui.controllers.ui.ButtonStyler;
import com.example.tetoo.gui.controllers.ui.LogoLoader;
import com.example.tetoo.util.FontLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane mainContainer;
    @FXML private ImageView logoImageView;

    @FXML private VBox mainContent;
    @FXML private VBox buttonsContainer;
    @FXML private VBox registerForm;

    @FXML private Button registerButton;
    @FXML private Button loginButton;

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;

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

        buttonsContainer.setVisible(false);
        buttonsContainer.setManaged(false);

        registerForm.setVisible(true);
        registerForm.setManaged(true);
    }

    @FXML
    private void handleLogin() {
        ButtonStyler.animateClick(loginButton);

        // пока делаем то же самое, потом разведёшь
        buttonsContainer.setVisible(false);
        buttonsContainer.setManaged(false);

        registerForm.setVisible(true);
        registerForm.setManaged(true);
    }

    @FXML
    private void handleBack() {
        registerForm.setVisible(false);
        registerForm.setManaged(false);

        buttonsContainer.setVisible(true);
        buttonsContainer.setManaged(true);

        loginField.clear();
        passwordField.clear();
    }


    @FXML
    private void handleRegisterSubmit() {
        String login = loginField.getText();
        String password = passwordField.getText();

        System.out.println("LOGIN = " + login);
        System.out.println("PASSWORD = " + password);
    }

    public void stopAnimations() {
        if (backgroundAnimator != null) {
            backgroundAnimator.stop();
        }
    }
}
