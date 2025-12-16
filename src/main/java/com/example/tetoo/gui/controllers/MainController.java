package com.example.tetoo.gui.controllers;

import com.example.tetoo.service.SupabaseService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    
    // Registration fields
    @FXML
    private TextField regEmailField;
    @FXML
    private PasswordField regPasswordField;
    
    // Login fields
    @FXML
    private TextField loginEmailField;
    @FXML
    private PasswordField loginPasswordField;
    
    // Service
    private SupabaseService supabaseService;

    private boolean isSignInActive = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supabaseService = new SupabaseService();
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
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Login error: Email and password are required");
        } else {
            System.out.println("Login attempt with email: " + email);
            // TODO: Add actual login logic
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("=== Registration attempt ===");
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();
        
        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Registration error: Email and password are required");
            return;
        }
        
        if (!isValidEmail(email)) {
            System.out.println("Registration error: Invalid email format");
            return;
        }
        
        if (password.length() < 6) {
            System.out.println("Registration error: Password must be at least 6 characters");
            return;
        }
        
        // Call Supabase service to register user
        System.out.println("Attempting to register user...");
        boolean success = supabaseService.registerUser(email, password);
        
        if (success) {
            System.out.println("Registration successful!");
            // Clear fields after successful registration
            regEmailField.clear();
            regPasswordField.clear();
            // TODO: Show success message to user
        } else {
            System.out.println("Registration failed. Please try again.");
            // TODO: Show error message to user
        }
    }
    
    private boolean isValidEmail(String email) {
        // Basic email validation
        return email != null && email.contains("@") && email.contains(".");
    }
}