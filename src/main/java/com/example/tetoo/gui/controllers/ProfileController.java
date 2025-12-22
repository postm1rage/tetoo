package com.example.tetoo.gui.controllers;

import com.example.tetoo.auth.SessionManager;
import com.example.tetoo.config.SupabaseConfig;
import com.example.tetoo.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;

// Импорты для HTTP
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.*;

public class ProfileController {

    @FXML private ImageView avatarImageView;
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private Label verifiedBadge;
    @FXML private Label likesCount;
    @FXML private Label matchesCount;
    @FXML private Label visitsCount;
    @FXML private Label responseRate;
    @FXML private VBox statsSection;

    // Поля формы
    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextArea aboutMeTextArea;
    @FXML private ComboBox<String> relationshipGoalComboBox;
    @FXML private TextField interestsField;

    // Кнопки
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button changeAvatarBtn;

    private String currentUserId;
    private boolean isUserVerified = false;
    private boolean hasProfile = false;
    private Gson gson;
    private CloseableHttpClient httpClient;

    @FXML
    public void initialize() {
        System.out.println("=== ProfileController INITIALIZE ===");

        gson = new Gson();
        httpClient = HttpClients.createDefault();

        setupForm();

        // Получаем текущего пользователя из сессии
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            System.out.println("Current user: " + currentUser.getEmail() +
                    " (ID: " + currentUser.getId() +
                    ", Verified: " + currentUser.getIs_verified() + ")");

            currentUserId = String.valueOf(currentUser.getId());
            isUserVerified = Boolean.TRUE.equals(currentUser.getIs_verified());

            // Устанавливаем временное приветствие из email
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                String name = email.substring(0, email.indexOf('@'));
                welcomeLabel.setText("Welcome, " + name + "!");
            } else {
                welcomeLabel.setText("Welcome!");
            }

            System.out.println("User verified status: " + isUserVerified);

            // Загружаем данные профиля
            Platform.runLater(() -> {
                loadProfileData();
            });
        } else {
            System.out.println("ERROR: No user in session!");
            showAlert("No user logged in. Please login first.");
            Platform.runLater(() -> {
                handleBackToDashboard();
            });
        }
    }

    private void setupForm() {
        System.out.println("Setting up form...");

        // Заполняем комбобоксы
        genderComboBox.getItems().addAll("Male", "Female", "Other");
        genderComboBox.setValue("Male");

        relationshipGoalComboBox.getItems().addAll(
                "Serious relationship",
                "Casual dating",
                "Friendship",
                "Networking",
                "Just looking around"
        );
        relationshipGoalComboBox.setValue("Serious relationship");

        // Обработчики кнопок
        saveBtn.setOnAction(e -> {
            System.out.println("Save button clicked");
            saveProfile();
        });

        cancelBtn.setOnAction(e -> {
            System.out.println("Cancel button clicked");
            handleBackToDashboard();
        });

        if (changeAvatarBtn != null) {
            changeAvatarBtn.setOnAction(e -> {
                System.out.println("Change avatar clicked");
                changeAvatar();
            });
        }

        System.out.println("Form setup complete");
    }

    private void loadProfileData() {
        try {
            System.out.println("Loading profile data for user: " + currentUserId);

            String url = SupabaseConfig.FULL_API_URL + "user_profiles?user_id=eq." + currentUserId;
            HttpGet request = new HttpGet(url);

            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Load profile response: " + responseBody);

            if (responseBody.length() > 2 && !responseBody.equals("[]")) {
                JsonArray profiles = gson.fromJson(responseBody, JsonArray.class);
                if (profiles.size() > 0) {
                    JsonObject profile = profiles.get(0).getAsJsonObject();
                    hasProfile = true;

                    // Показываем статистику
                    if (statsSection != null) {
                        statsSection.setVisible(true);
                    }

                    // Заполняем поля формы
                    if (profile.has("first_name") && profile.has("last_name")) {
                        String fullName = profile.get("first_name").getAsString() + " " +
                                profile.get("last_name").getAsString();
                        fullNameField.setText(fullName);
                    } else if (profile.has("first_name")) {
                        fullNameField.setText(profile.get("first_name").getAsString());
                    }

                    if (profile.has("birth_date")) {
                        String birthDate = profile.get("birth_date").getAsString();
                        try {
                            int birthYear = Integer.parseInt(birthDate.substring(0, 4));
                            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            int age = currentYear - birthYear;
                            ageField.setText(String.valueOf(age));
                        } catch (Exception e) {
                            // Оставляем поле пустым
                        }
                    }

                    if (profile.has("city")) {
                        cityField.setText(profile.get("city").getAsString());
                    }

                    if (profile.has("gender")) {
                        String gender = profile.get("gender").getAsString();
                        if (gender.equals("M")) {
                            genderComboBox.setValue("Male");
                        } else if (gender.equals("F")) {
                            genderComboBox.setValue("Female");
                        } else if (gender.equals("O")) {
                            genderComboBox.setValue("Other");
                        }
                    }

                    if (profile.has("about_me")) {
                        aboutMeTextArea.setText(profile.get("about_me").getAsString());
                    }

                    if (profile.has("relationship_goal")) {
                        String goal = profile.get("relationship_goal").getAsString();
                        if (goal.equals("serious")) {
                            relationshipGoalComboBox.setValue("Serious relationship");
                        } else if (goal.equals("casual")) {
                            relationshipGoalComboBox.setValue("Casual dating");
                        } else if (goal.equals("friendship")) {
                            relationshipGoalComboBox.setValue("Friendship");
                        } else if (goal.equals("networking")) {
                            relationshipGoalComboBox.setValue("Networking");
                        } else {
                            relationshipGoalComboBox.setValue("Just looking around");
                        }
                    }

                    // Загружаем статистику
                    loadProfileStats();

                    statusLabel.setText("Profile loaded - you can update your information");

                    // Показываем бейдж верификации
                    if (isUserVerified && verifiedBadge != null) {
                        verifiedBadge.setText("✓ Verified");
                        verifiedBadge.setVisible(true);
                    }
                }
            } else {
                hasProfile = false;
                // Скрываем статистику
                if (statsSection != null) {
                    statsSection.setVisible(false);
                }
                statusLabel.setText("Create your profile - fill in the information below");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading profile data");
        }
    }

    private void loadProfileStats() {
        // Здесь можно добавить реальную загрузку статистики
        if (likesCount != null) likesCount.setText("12");
        if (matchesCount != null) matchesCount.setText("3");
        if (visitsCount != null) visitsCount.setText("47");
        if (responseRate != null) responseRate.setText("85%");
    }

    @FXML
    private void saveProfile() {
        System.out.println("=== Saving Profile ===");

        try {
            String fullName = fullNameField.getText().trim();
            String ageText = ageField.getText().trim();
            String cityText = cityField.getText().trim();
            String genderValue = genderComboBox.getValue();
            String aboutMeText = aboutMeTextArea.getText().trim();
            String relationshipGoalValue = relationshipGoalComboBox.getValue();
            String interestsText = interestsField.getText().trim();

            // Валидация
            if (fullName.isEmpty()) {
                showAlert("Please enter your full name!");
                fullNameField.requestFocus();
                return;
            }

            if (ageText.isEmpty()) {
                showAlert("Please enter your age!");
                ageField.requestFocus();
                return;
            }

            try {
                int age = Integer.parseInt(ageText);
                if (age < 18 || age > 100) {
                    showAlert("Please enter a valid age (18-100)!");
                    ageField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Please enter a valid age (number)!");
                ageField.requestFocus();
                return;
            }

            if (cityText.isEmpty()) {
                showAlert("Please enter your city!");
                cityField.requestFocus();
                return;
            }

            // Создаем JSON для профиля
            JsonObject profileData = new JsonObject();
            profileData.addProperty("user_id", Integer.parseInt(currentUserId));

            // Разбиваем полное имя на имя и фамилию
            String[] nameParts = fullName.split(" ");
            if (nameParts.length > 0) {
                profileData.addProperty("first_name", nameParts[0]);
            }
            if (nameParts.length > 1) {
                profileData.addProperty("last_name", nameParts[1]);
            } else {
                profileData.addProperty("last_name", ".");
            }

            // Возраст -> birth_date
            try {
                int age = Integer.parseInt(ageText);
                int birthYear = Calendar.getInstance().get(Calendar.YEAR) - age;
                profileData.addProperty("birth_date", birthYear + "-01-01");
            } catch (NumberFormatException e) {
                profileData.addProperty("birth_date", "2000-01-01");
            }

            // Город и страна
            profileData.addProperty("city", cityText);
            profileData.addProperty("country", "Russia");

            // Пол
            if (genderValue.equals("Male")) {
                profileData.addProperty("gender", "M");
            } else if (genderValue.equals("Female")) {
                profileData.addProperty("gender", "F");
            } else {
                profileData.addProperty("gender", "O");
            }

            // О себе
            if (!aboutMeText.isEmpty()) {
                profileData.addProperty("about_me", aboutMeText);
            } else {
                profileData.addProperty("about_me", "No information provided.");
            }

            // Цель отношений
            if (relationshipGoalValue.equals("Serious relationship")) {
                profileData.addProperty("relationship_goal", "serious");
            } else if (relationshipGoalValue.equals("Casual dating")) {
                profileData.addProperty("relationship_goal", "casual");
            } else if (relationshipGoalValue.equals("Friendship")) {
                profileData.addProperty("relationship_goal", "friendship");
            } else if (relationshipGoalValue.equals("Networking")) {
                profileData.addProperty("relationship_goal", "networking");
            } else {
                profileData.addProperty("relationship_goal", "looking");
            }

            // Интересы
            if (!interestsText.isEmpty()) {
                String currentAbout = profileData.has("about_me") ?
                        profileData.get("about_me").getAsString() : "";
                profileData.addProperty("about_me",
                        currentAbout + "\n\nInterests: " + interestsText);
            }

            System.out.println("Profile data to save: " + profileData);

            // Проверяем существует ли профиль
            String checkUrl = SupabaseConfig.FULL_API_URL + "user_profiles?user_id=eq." + currentUserId;
            HttpGet checkRequest = new HttpGet(checkUrl);
            checkRequest.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            checkRequest.setHeader("Authorization", SupabaseConfig.getAuthHeader());

            HttpResponse checkResponse = httpClient.execute(checkRequest);
            String responseBody = EntityUtils.toString(checkResponse.getEntity());

            HttpResponse saveResponse;

            if (responseBody.length() > 2 && !responseBody.equals("[]")) {
                // Обновляем существующий
                System.out.println("Updating existing profile");
                String updateUrl = SupabaseConfig.FULL_API_URL + "user_profiles?user_id=eq." + currentUserId;
                HttpPatch request = new HttpPatch(updateUrl);
                request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
                request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
                request.setHeader("Content-Type", "application/json");
                request.setHeader("Prefer", "return=minimal");

                StringEntity entity = new StringEntity(profileData.toString());
                request.setEntity(entity);
                saveResponse = httpClient.execute(request);
            } else {
                // Создаем новый
                System.out.println("Creating new profile");
                String postUrl = SupabaseConfig.FULL_API_URL + "user_profiles";
                HttpPost request = new HttpPost(postUrl);
                request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
                request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
                request.setHeader("Content-Type", "application/json");
                request.setHeader("Prefer", "return=minimal");

                StringEntity entity = new StringEntity(profileData.toString());
                request.setEntity(entity);
                saveResponse = httpClient.execute(request);
            }

            System.out.println("Save status: " + saveResponse.getStatusLine().getStatusCode());

            if (saveResponse.getStatusLine().getStatusCode() == 200 ||
                    saveResponse.getStatusLine().getStatusCode() == 201 ||
                    saveResponse.getStatusLine().getStatusCode() == 204) {
                showAlert("Profile saved successfully!");

                // Обновляем верификацию
                if (!isUserVerified) {
                    markUserAsVerified();
                }

                // Возвращаемся в дашборд
                handleBackToDashboard();
            } else {
                String errorBody = EntityUtils.toString(saveResponse.getEntity());
                System.err.println("Error response: " + errorBody);
                showAlert("Failed to save profile. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error saving profile: " + e.getMessage());
        }
    }

    private void markUserAsVerified() {
        try {
            JsonObject updateData = new JsonObject();
            updateData.addProperty("is_verified", true);

            String url = SupabaseConfig.FULL_API_URL + "users?id=eq." + currentUserId;
            HttpPatch request = new HttpPatch(url);

            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Prefer", "return=minimal");

            StringEntity jsonEntity = new StringEntity(updateData.toString());
            request.setEntity(jsonEntity);

            HttpResponse response = httpClient.execute(request);
            System.out.println("Mark as verified status: " + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 204 || response.getStatusLine().getStatusCode() == 200) {
                isUserVerified = true;
                User currentUser = SessionManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    currentUser.setIs_verified(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeAvatar() {
        showAlert("Avatar change feature coming soon!");
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            currentStage.setScene(scene);
            currentStage.setTitle("TETOO Dashboard");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}