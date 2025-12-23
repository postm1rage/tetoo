package com.example.tetoo.gui.controllers;

import com.example.tetoo.auth.SessionManager;
import com.example.tetoo.config.SupabaseConfig;
import com.example.tetoo.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Node;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.example.tetoo.gui.controllers.LikesController;
import com.example.tetoo.gui.controllers.MatchesController;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    private double xOffset = 0;
    private double yOffset = 0;
    private Gson gson = new Gson();
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    
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
    
    // Avatars
    @FXML private ImageView userAvatar;
    @FXML private ImageView profileAvatar;
    
    // Content areas
    @FXML private VBox profileContent;
    @FXML private VBox chatsContent;
    @FXML private VBox likesContent;
    @FXML private VBox matchesContent;
    
    // Chat controller reference (injected via fx:include)
    @FXML private ChatController chatPanelController;
    
    @FXML
    private LikesController likesPanelController;

    // Likes panel reference
    @FXML
    private VBox likesPanel;

    // Matches controller reference (injected via fx:include)
    @FXML
    private MatchesController matchesPanelController;

    // Matches panel reference
    @FXML
    private VBox matchesPanel;

    // Методы для перемещения окна (из версии HEAD)
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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Dashboard initialized");
        
        // Load user data from session
        loadUserData();
        
        // Set up navigation buttons
        setupNavigation();
        
        // Show profile by default
        showProfile();

        // Инициализация контроллера лайков
        if (likesPanel != null) {
            // Контроллер автоматически инжектируется через fx:include
            // Нужно просто проверить, что он существует
            System.out.println("Likes panel found");
        } else {
            System.out.println("Likes panel not found - check fx:include in dashboard.fxml");
        }

        // Инициализация контроллера матчей
        if (matchesPanel != null) {
            System.out.println("Matches panel found");
        } else {
            System.out.println("Matches panel not found - check fx:include in dashboard.fxml");
        }
    }
    
    private void loadUserData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            String email = currentUser.getEmail();

            // Пытаемся загрузить имя из профиля (из версии section-design)
            String firstName = getUserFirstName(currentUser.getId());

            if (firstName != null && !firstName.isEmpty()) {
                welcomeLabel.setText("Welcome, " + firstName + "!");
                userNameLabel.setText(firstName);
                profileName.setText(firstName);
            } else {
                // Используем email как временное имя (из версии HEAD)
                String nameFromEmail = email.substring(0, email.indexOf('@'));
                welcomeLabel.setText("Welcome, " + nameFromEmail + "!");
                userNameLabel.setText(nameFromEmail);
                profileName.setText(nameFromEmail);
            }

            userEmailLabel.setText(email);

            // Загружаем дополнительную информацию профиля (из section-design)
            loadProfileInfo(currentUser.getId());

            // Load avatar (объединенная версия)
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
    
    // Метод из версии section-design для получения имени пользователя
    private String getUserFirstName(int userId) {
        try {
            String url = SupabaseConfig.FULL_API_URL + "user_profiles?user_id=eq." + userId;
            HttpGet request = new HttpGet(url);

            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            if (responseBody.length() > 2 && !responseBody.equals("[]")) {
                JsonArray profiles = gson.fromJson(responseBody, JsonArray.class);
                if (profiles.size() > 0) {
                    JsonObject profile = profiles.get(0).getAsJsonObject();
                    if (profile.has("first_name")) {
                        return profile.get("first_name").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user profile: " + e.getMessage());
        }
        return null;
    }

    // Метод из версии section-design для загрузки информации профиля
    private void loadProfileInfo(int userId) {
        try {
            String url = SupabaseConfig.FULL_API_URL + "user_profiles?user_id=eq." + userId;
            HttpGet request = new HttpGet(url);

            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            if (responseBody.length() > 2 && !responseBody.equals("[]")) {
                JsonArray profiles = gson.fromJson(responseBody, JsonArray.class);
                if (profiles.size() > 0) {
                    JsonObject profile = profiles.get(0).getAsJsonObject();

                    StringBuilder ageLocation = new StringBuilder();

                    if (profile.has("birth_date")) {
                        try {
                            String birthDate = profile.get("birth_date").getAsString();
                            int birthYear = Integer.parseInt(birthDate.substring(0, 4));
                            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                            int age = currentYear - birthYear;
                            ageLocation.append("Age: ").append(age);
                        } catch (Exception e) {
                            ageLocation.append("Birth: ").append(profile.get("birth_date").getAsString());
                        }
                    } else {
                        ageLocation.append("Age: Unknown");
                    }

                    if (profile.has("city")) {
                        ageLocation.append(" • Location: ").append(profile.get("city").getAsString());
                    } else {
                        ageLocation.append(" • Location: Unknown");
                    }

                    profileAgeLocation.setText(ageLocation.toString());

                    if (profile.has("relationship_goal")) {
                        String goal = profile.get("relationship_goal").getAsString();
                        String goalText = "";
                        switch (goal) {
                            case "serious": goalText = "Serious relationship"; break;
                            case "casual": goalText = "Casual dating"; break;
                            case "friendship": goalText = "Friendship"; break;
                            case "networking": goalText = "Networking"; break;
                            default: goalText = goal;
                        }
                        profileRelationshipGoal.setText("Looking for: " + goalText);
                    } else {
                        profileRelationshipGoal.setText("Looking for: Unknown");
                    }

                }
            } else {
                // No profile found
                profileAgeLocation.setText("Age: Unknown • Location: Unknown");
                profileRelationshipGoal.setText("Looking for: Unknown");
            }

        } catch (Exception e) {
            System.err.println("Error loading profile info: " + e.getMessage());
            profileAgeLocation.setText("Age: Unknown • Location: Unknown");
            profileRelationshipGoal.setText("Looking for: Unknown");
        }
    }
    
    // Метод из версии HEAD для создания дефолтного аватара
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
    }
    
    private void hideAllContent() {
        profileContent.setVisible(false);
        chatsContent.setVisible(false);
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
        
        // Load chats through ChatController if available (из версии HEAD)
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
        
        // Обновляем данные в контроллере лайков
        if (likesPanelController != null) {
            System.out.println("Refreshing likes panel");
            likesPanelController.refresh();
        } else {
            System.out.println("Likes panel controller is null");
        }
        
    }
    
    @FXML
    private void showMatches() {
        System.out.println("Showing matches");
        resetNavButtons();
        matchesBtn.getStyleClass().add("nav-btn-active");
        
        hideAllContent();
        matchesContent.setVisible(true);
        statusLabel.setText("Viewing matches");
        
        // Обновляем данные в контроллере матчей
        if (matchesPanelController != null) {
            System.out.println("Refreshing matches panel");
            matchesPanelController.refresh();
        } else {
            System.out.println("Matches panel controller is null");
        }
    }
    
    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        
        // Shutdown chat controller if exists (из версии HEAD)
        if (chatPanelController != null) {
            chatPanelController.shutdown();
        }
        
        SessionManager.getInstance().logout();
        returnToLogin();
    }
    
    // Объединенный метод handleEditProfile
    @FXML
    private void handleEditProfile() {
        System.out.println("=== Opening Profile ===");
        statusLabel.setText("Opening profile editor...");

        try {
            // Загружаем Profile.fxml (из section-design)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            Parent profileRoot = loader.load();

            // Получаем текущее окно
            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();

            // Создаем новую сцену
            Scene profileScene = new Scene(profileRoot, 1000, 700);
            currentStage.setScene(profileScene);
            currentStage.setTitle("TETOO - Profile");
            currentStage.centerOnScreen();

            System.out.println("Successfully switched to profile");

        } catch (Exception e) {
            System.err.println("Error opening profile: " + e.getMessage());
            e.printStackTrace();

            // Покажем детальную ошибку (из section-design)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Opening Profile");
            alert.setHeaderText("Cannot open profile editor");

            // Создаем TextArea для деталей ошибки
            TextArea textArea = new TextArea(e.toString() + "\n\n" + getStackTrace(e));
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        }
    }
    
    // Методы из версии section-design
    @FXML
    private void handleLoadChats() {
        System.out.println("Load chats clicked");
        statusLabel.setText("Loading chats...");
        showAlert("Chats feature coming soon!");
    }
    
    @FXML
    private void handleStartDiscovery() {
        System.out.println("Start discovery clicked");
        statusLabel.setText("Starting discovery...");
        showAlert("Discovery feature coming soon!");
    }
    
    @FXML
    private void handleViewReceivedLikes() {
        System.out.println("View received likes clicked");
        statusLabel.setText("Loading received likes...");
        showAlert("Likes feature coming soon!");
    }
    
    @FXML
    private void handleViewSentLikes() {
        System.out.println("View sent likes clicked");
        statusLabel.setText("Loading sent likes...");
        showAlert("Likes feature coming soon!");
    }
    
    @FXML
    private void handleViewMatches() {
        System.out.println("View matches clicked");
        statusLabel.setText("Loading matches...");
        showAlert("Matches feature coming soon!");
    }
    
    // Метод из section-design для тестирования профиля
    @FXML
    private void debugTestProfile() {
        System.out.println("=== DEBUG Testing Profile ===");

        // Проверяем наличие файла
        java.net.URL url = getClass().getResource("/fxml/Profile.fxml");
        if (url == null) {
            System.err.println("ERROR: Profile.fxml not found at /fxml/Profile.fxml");

            // Покажем все что есть в fxml папке
            try {
                System.err.println("Searching for resources in /fxml/");
                java.util.Enumeration<java.net.URL> resources =
                        getClass().getClassLoader().getResources("fxml");
                while (resources.hasMoreElements()) {
                    System.err.println("  Found: " + resources.nextElement());
                }
            } catch (Exception e) {
                System.err.println("Error searching resources: " + e.getMessage());
            }
        } else {
            System.out.println("Profile.fxml found at: " + url);
            System.out.println("File exists: " + new java.io.File(url.getFile()).exists());
        }

        // Пробуем загрузить
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            System.out.println("Loader created successfully");

            // Попробуем загрузить
            Parent testRoot = loader.load();
            System.out.println("SUCCESS: Profile.fxml loaded!");

            // Покажем маленькое тестовое окно
            Stage testStage = new Stage();
            Scene testScene = new Scene(testRoot, 400, 300);
            testStage.setScene(testScene);
            testStage.setTitle("Profile Test - SUCCESS");
            testStage.show();

        } catch (Exception e) {
            System.err.println("Loader creation/loading error: " + e.getMessage());
            e.printStackTrace();
        }
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
    
    // Вспомогательные методы из section-design
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    // Метод из версии HEAD для завершения работы
    public void shutdown() {
        System.out.println("DashboardController shutdown initiated");
        
        // Shutdown chat controller
        if (chatPanelController != null) {
            chatPanelController.shutdown();
        }
        
        System.out.println("DashboardController shutdown completed");
    }
}