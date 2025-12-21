package com.example.tetoo.gui.controllers;

import com.example.tetoo.auth.SessionManager;
import com.example.tetoo.gui.controllers.ChatController;
import com.example.tetoo.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Node;


import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private double xOffset = 0;
    private double yOffset = 0;
    // Header and navigation
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label statusLabel;
    
    // Profile section
    @FXML private Label profileName;
    @FXML private Label profileAgeLocation;
    @FXML private Label profileRelationshipGoal;
    
    // Likes section
    @FXML private Label receivedLikesCount;
    @FXML private Label sentLikesCount;
    
    // Navigation buttons
    @FXML private Button logoutBtn;
    @FXML private Button profileBtn;
    @FXML private Button chatsBtn;
    @FXML private Button likesBtn;
    @FXML private Button matchesBtn;
    // @FXML private Button notificationsBtn; // Раскомментировать если добавишь в FXML
    
    // Avatars
    @FXML private ImageView userAvatar;
    @FXML private ImageView profileAvatar;
    
    // Content areas
    @FXML private VBox profileContent;
    @FXML private VBox chatsContent;
    @FXML private VBox likesContent;
    @FXML private VBox matchesContent;

    @FXML
    private void handleMousePressed(MouseEvent event) {
        // Запоминаем позицию мыши относительно окна
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
    @FXML
    private void handleMouseDragged(MouseEvent event) {
        // Получаем Stage (окно)
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        
        // Двигаем окно
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
    // Chat controller reference (injected via fx:include)
    @FXML private ChatController chatPanelController;
    
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
                InputStream avatarStream = getClass().getResourceAsStream("/images/default_avatar.png");
                if (avatarStream != null) {
                    Image avatar = new Image(avatarStream);
                    userAvatar.setImage(avatar);
                    profileAvatar.setImage(avatar);
                } else {
                    System.out.println("Default avatar not found, using placeholder");
                    userAvatar.setImage(createDefaultAvatar());
                    profileAvatar.setImage(createDefaultAvatar());
                }
            } catch (Exception e) {
                System.err.println("Could not load default avatar, using placeholder: " + e.getMessage());
                userAvatar.setImage(createDefaultAvatar());
                profileAvatar.setImage(createDefaultAvatar());
            }
            
        } else {
            // No user logged in - go back to login screen
            System.out.println("No user in session, returning to login");
            returnToLogin();
        }
    }
    
    private Image createDefaultAvatar() {
        // Create a simple colored circle as placeholder
        WritableImage image = new WritableImage(100, 100);
        PixelWriter writer = image.getPixelWriter();
        
        int centerX = 50;
        int centerY = 50;
        int radius = 40;
        
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 100; x++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                if (distance <= radius) {
                    // Purple circle for avatar
                    writer.setColor(x, y, Color.web("#8A2BE2"));
                } else {
                    // Transparent background
                    writer.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }
        
        return image;
    }
    
    private void setupNavigation() {
        resetNavButtons();
        profileBtn.getStyleClass().add("nav-btn-active");
    }
    
    private void resetNavButtons() {
        String activeStyle = "nav-btn-active";
        
        profileBtn.getStyleClass().remove(activeStyle);
        chatsBtn.getStyleClass().remove(activeStyle);
        likesBtn.getStyleClass().remove(activeStyle);
        matchesBtn.getStyleClass().remove(activeStyle);
        
        // Only remove if button exists in FXML
        // if (notificationsBtn != null) {
        //     notificationsBtn.getStyleClass().remove(activeStyle);
        // }
    }
    
    private void hideAllContent() {
        profileContent.setVisible(false);
        chatsContent.setVisible(false);
        likesContent.setVisible(false);
        matchesContent.setVisible(false);
        
        // Only hide if exists in FXML
        // if (notificationsContent != null) {
        //     notificationsContent.setVisible(false);
        // }
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
        
        // Load chats through ChatController if available
        if (chatPanelController != null) {
            chatPanelController.loadUserChats();
        }
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
    
    // Uncomment if you add notifications button in FXML
    /*
    @FXML
    private void showNotifications() {
        System.out.println("Showing notifications");
        resetNavButtons();
        notificationsBtn.getStyleClass().add("nav-btn-active");
        
        hideAllContent();
        notificationsContent.setVisible(true);
        statusLabel.setText("Viewing notifications");
    }
    */
    
    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        
        // Shutdown chat controller if exists
        if (chatPanelController != null) {
            chatPanelController.shutdown();
        }
        
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
    
    // Uncomment if you add notifications in FXML
    /*
    @FXML
    private void handleMarkAllAsRead() {
        System.out.println("Mark all as read clicked");
        statusLabel.setText("Marking all as read...");
        // TODO: Implement notifications
    }
    */
    
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
    
    // Method called when application is closing
    public void shutdown() {
        System.out.println("DashboardController shutdown initiated");
        
        // Shutdown chat controller
        if (chatPanelController != null) {
            chatPanelController.shutdown();
        }
        
        System.out.println("DashboardController shutdown completed");
    }
}