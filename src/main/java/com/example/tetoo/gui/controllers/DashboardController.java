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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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

    private Gson gson = new Gson();
    private CloseableHttpClient httpClient = HttpClients.createDefault();

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

    private void loadUserData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();

            // Пытаемся загрузить имя из профиля
            String firstName = getUserFirstName(currentUser.getId());

            if (firstName != null && !firstName.isEmpty()) {
                welcomeLabel.setText("Welcome, " + firstName + "!");
                userNameLabel.setText(firstName);
                profileName.setText(firstName);
            } else {
                // Используем email как временное имя
                String nameFromEmail = email.substring(0, email.indexOf('@'));
                welcomeLabel.setText("Welcome, " + nameFromEmail + "!");
                userNameLabel.setText(nameFromEmail);
                profileName.setText(nameFromEmail);
            }

            userEmailLabel.setText(email);

            // Загружаем дополнительную информацию профиля
            loadProfileInfo(currentUser.getId());

            // Load avatar if available
            try {
                Image avatar = new Image(getClass().getResourceAsStream("/images/default_avatar.png"));
                userAvatar.setImage(avatar);
                profileAvatar.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Could not load default avatar: " + e.getMessage());
                userAvatar.setImage(null);
                profileAvatar.setImage(null);
            }

        } else {
            // No user logged in - go back to login screen
            System.out.println("No user in session, returning to login");
            returnToLogin();
        }
    }

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

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        SessionManager.getInstance().logout();
        returnToLogin();
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

    @FXML
    private void handleEditProfile() {
        System.out.println("=== Opening Profile ===");
        statusLabel.setText("Opening profile editor...");

        try {
            // Загружаем Profile.fxml
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

            // Покажем детальную ошибку
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

    @FXML
    private void handleLoadChats() {
        System.out.println("Load chats clicked");
        statusLabel.setText("Loading chats...");
        // TODO: Implement chats loading
        showAlert("Chats feature coming soon!");
    }

    @FXML
    private void handleStartDiscovery() {
        System.out.println("Start discovery clicked");
        statusLabel.setText("Starting discovery...");
        // TODO: Implement discovery feature
        showAlert("Discovery feature coming soon!");
    }

    @FXML
    private void handleViewReceivedLikes() {
        System.out.println("View received likes clicked");
        statusLabel.setText("Loading received likes...");
        // TODO: Implement received likes
        showAlert("Likes feature coming soon!");
    }

    @FXML
    private void handleViewSentLikes() {
        System.out.println("View sent likes clicked");
        statusLabel.setText("Loading sent likes...");
        // TODO: Implement sent likes
        showAlert("Likes feature coming soon!");
    }

    @FXML
    private void handleViewMatches() {
        System.out.println("View matches clicked");
        statusLabel.setText("Loading matches...");
        // TODO: Implement matches view
        showAlert("Matches feature coming soon!");
    }

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
}