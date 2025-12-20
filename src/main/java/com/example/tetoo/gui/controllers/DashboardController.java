package com.example.tetoo.gui.controllers;

import com.example.tetoo.auth.SessionManager;
import com.example.tetoo.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label statusLabel;
    @FXML private Label profileName;
    @FXML private Label profileAgeLocation;
    @FXML private Label profileRelationshipGoal;
    @FXML private Label receivedLikesCount;
    @FXML private Label sentLikesCount;

    @FXML private Button logoutBtn;
    @FXML private Button profileBtn;
    @FXML private Button chatsBtn;
    @FXML private Button discoverBtn;
    @FXML private Button likesBtn;
    @FXML private Button matchesBtn;

    @FXML private ImageView userAvatar;
    @FXML private ImageView profileAvatar;

    @FXML private VBox profileContent;
    @FXML private VBox chatsContent;
    @FXML private VBox discoverContent;
    @FXML private VBox likesContent;
    @FXML private VBox matchesContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Dashboard initialized");

        // Load user data from session
        loadUserData();

        // Set up navigation buttons
        setupNavigation();

        // Show profile by default
        showProfile();
    }

    private void loadUserData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            String name = email.substring(0, email.indexOf('@')); // Extract name from email

            welcomeLabel.setText("Welcome, " + name + "!");
            userNameLabel.setText(name);
            userEmailLabel.setText(email);

            // Profile info
            profileName.setText(name);
            profileAgeLocation.setText("Age: Unknown • Location: Unknown");
            profileRelationshipGoal.setText("Looking for: Unknown");

            // Load avatar if available
            try {
                Image avatar = new Image(getClass().getResourceAsStream("/images/default_avatar.png"));
                userAvatar.setImage(avatar);
                profileAvatar.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Could not load default avatar: " + e.getMessage());
            }

        } else {
            // No user logged in - go back to login screen
            System.out.println("No user in session, returning to login");
            returnToLogin();
        }
    }

    private void setupNavigation() {
        resetNavButtons();
        profileBtn.getStyleClass().add("nav-btn-active");
    }

    private void resetNavButtons() {
        String activeStyle = "nav-btn-active";

        profileBtn.getStyleClass().remove(activeStyle);
        chatsBtn.getStyleClass().remove(activeStyle);
        discoverBtn.getStyleClass().remove(activeStyle);
        likesBtn.getStyleClass().remove(activeStyle);
        matchesBtn.getStyleClass().remove(activeStyle);
    }

    private void hideAllContent() {
        profileContent.setVisible(false);
        chatsContent.setVisible(false);
        discoverContent.setVisible(false);
        likesContent.setVisible(false);
        matchesContent.setVisible(false);
    }

    @FXML
    private void showProfile() {
        System.out.println("Showing profile");
        resetNavButtons();
        profileBtn.getStyleClass().add("nav-btn-active");

        hideAllContent();
        profileContent.setVisible(true);
        statusLabel.setText("Viewing profile");
    }

    @FXML
    private void showChats() {
        System.out.println("Showing chats");
        resetNavButtons();
        chatsBtn.getStyleClass().add("nav-btn-active");

        hideAllContent();
        chatsContent.setVisible(true);
        statusLabel.setText("Viewing chats");
    }

    @FXML
    private void showDiscover() {
        System.out.println("Showing discover");
        resetNavButtons();
        discoverBtn.getStyleClass().add("nav-btn-active");

        hideAllContent();
        discoverContent.setVisible(true);
        statusLabel.setText("Discovering new people");
    }

    @FXML
    private void showLikes() {
        System.out.println("Showing likes");
        resetNavButtons();
        likesBtn.getStyleClass().add("nav-btn-active");

        hideAllContent();
        likesContent.setVisible(true);
        statusLabel.setText("Viewing likes");
        updateLikesCount();
    }

    @FXML
    private void showMatches() {
        System.out.println("Showing matches");
        resetNavButtons();
        matchesBtn.getStyleClass().add("nav-btn-active");

        hideAllContent();
        matchesContent.setVisible(true);
        statusLabel.setText("Viewing matches");
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        SessionManager.getInstance().logout();
        returnToLogin();
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Edit profile clicked");
        statusLabel.setText("Opening profile editor...");
        // TODO: Implement profile editing
    }

    @FXML
    private void handleLoadChats() {
        System.out.println("Load chats clicked");
        statusLabel.setText("Loading chats...");
        // TODO: Implement chats loading
    }

    @FXML
    private void handleStartDiscovery() {
        System.out.println("Start discovery clicked");
        statusLabel.setText("Starting discovery...");
        // TODO: Implement discovery feature
    }

    @FXML
    private void handleViewReceivedLikes() {
        System.out.println("View received likes clicked");
        statusLabel.setText("Loading received likes...");
        // TODO: Implement received likes
    }

    @FXML
    private void handleViewSentLikes() {
        System.out.println("View sent likes clicked");
        statusLabel.setText("Loading sent likes...");
        // TODO: Implement sent likes
    }

    @FXML
    private void handleViewMatches() {
        System.out.println("View matches clicked");
        statusLabel.setText("Loading matches...");
        // TODO: Implement matches view
    }

    private void updateLikesCount() {
        // Temporary stub - will implement with real data later
        receivedLikesCount.setText("3");
        sentLikesCount.setText("2");
    }

    private void returnToLogin() {
        try {
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent loginRoot = loader.load();

            // Get current stage
            Stage stage = (Stage) logoutBtn.getScene().getWindow();

            // Set new scene
            Scene loginScene = new Scene(loginRoot, 900, 600);
            stage.setScene(loginScene);
            stage.setTitle("TETOO - Login");
            stage.centerOnScreen();

            System.out.println("Returned to login screen");

        } catch (Exception e) {
            System.err.println("Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}