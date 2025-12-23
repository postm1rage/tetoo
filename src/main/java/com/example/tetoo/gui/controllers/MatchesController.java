package com.example.tetoo.gui.controllers;

import com.example.tetoo.auth.SessionManager;
import com.example.tetoo.model.UserProfile;
import com.example.tetoo.service.MatchService;
import com.example.tetoo.service.interfaces.IMatchService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MatchesController implements Initializable {

    @FXML
    private VBox mainContainer;
    @FXML
    private ScrollPane matchesScrollPane;
    @FXML
    private VBox matchesContainer;
    @FXML
    private VBox noMatchesContainer;
    @FXML
    private Label noMatchesLabel;
    @FXML
    private ProgressBar loadingProgress;

    private IMatchService matchService;
    private Integer currentUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Инициализация сервиса
        matchService = new MatchService();
        
        // Получаем текущий ID пользователя из сессии
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        
        if (currentUserId == null) {
            System.out.println("Error: No user logged in");
            return;
        }
        
        // Загружаем матчи
        loadMatches();
    }

    // Загрузить матчи
    private void loadMatches() {
        // Показываем индикатор загрузки
        loadingProgress.setVisible(true);
        matchesContainer.setVisible(false);
        noMatchesContainer.setVisible(false);
        
        // Загружаем матчи в фоновом потоке
        new Thread(() -> {
            List<UserProfile> matchedProfiles = matchService.getMatchedProfiles(currentUserId);
            
            Platform.runLater(() -> {
                loadingProgress.setVisible(false);
                
                if (matchedProfiles.isEmpty()) {
                    // Нет матчей
                    matchesContainer.setVisible(false);
                    noMatchesContainer.setVisible(true);
                    noMatchesLabel.setText("No matches yet");
                } else {
                    // Отображаем матчи
                    displayMatches(matchedProfiles);
                    matchesContainer.setVisible(true);
                    noMatchesContainer.setVisible(false);
                }
            });
        }).start();
    }

    // Отобразить матчи в интерфейсе
    private void displayMatches(List<UserProfile> matchedProfiles) {
        matchesContainer.getChildren().clear();
        
        for (UserProfile profile : matchedProfiles) {
            VBox matchCard = createMatchCard(profile);
            matchesContainer.getChildren().add(matchCard);
        }
    }

    // Создать карточку матча
    private VBox createMatchCard(UserProfile profile) {
        VBox card = new VBox();
        card.getStyleClass().add("match-card");
        card.setSpacing(10);
        card.setPadding(new Insets(12));
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        
        // Верхняя часть: аватар и имя
        HBox header = new HBox();
        header.getStyleClass().add("match-card-header");
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Аватар
        StackPane avatarContainer = new StackPane();
        avatarContainer.getStyleClass().add("match-avatar-container");
        
        Circle avatarCircle = new Circle(25);
        avatarCircle.getStyleClass().add("match-avatar-circle");
        
        ImageView avatarImage = new ImageView();
        avatarImage.setFitHeight(45);
        avatarImage.setFitWidth(45);
        avatarImage.setPreserveRatio(true);
        avatarImage.getStyleClass().add("match-avatar-image");
        
        // Устанавливаем аватар
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            try {
                // В реальном приложении нужно загружать изображение по URL
                // Здесь используем заглушку
                Image image = new Image(getClass().getResourceAsStream("/images/tetoo_logo_small.png"));
                avatarImage.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading avatar: " + e.getMessage());
                setDefaultAvatar(avatarImage);
            }
        } else {
            setDefaultAvatar(avatarImage);
        }
        
        avatarContainer.getChildren().addAll(avatarCircle, avatarImage);
        
        // Информация
        VBox infoBox = new VBox();
        infoBox.setSpacing(3);
        
        Label nameLabel = new Label(profile.getFirstName() + " " + profile.getLastName());
        nameLabel.getStyleClass().add("match-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(150);
        
        // Возраст и локация
        StringBuilder ageLocation = new StringBuilder();
        if (profile.getAge() != null) {
            ageLocation.append(profile.getAge()).append(" years");
        }
        if (profile.getCity() != null) {
            if (ageLocation.length() > 0) ageLocation.append(" • ");
            ageLocation.append(profile.getCity());
        }
        
        Label ageLocationLabel = new Label(ageLocation.toString());
        ageLocationLabel.getStyleClass().add("match-age-location");
        ageLocationLabel.setWrapText(true);
        
        infoBox.getChildren().addAll(nameLabel, ageLocationLabel);
        
        header.getChildren().addAll(avatarContainer, infoBox);
        
        // Средняя часть: дополнительная информация
        VBox infoContainer = new VBox();
        infoContainer.getStyleClass().add("match-info-container");
        infoContainer.setSpacing(5);
        
        // Пол
        if (profile.getGender() != null) {
            HBox genderBox = new HBox(5);
            genderBox.setAlignment(Pos.CENTER_LEFT);
            
            Label genderLabel = new Label("Gender:");
            genderLabel.getStyleClass().add("match-info-label");
            
            String genderText = switch (profile.getGender()) {
                case "M" -> "Male";
                case "F" -> "Female";
                case "O" -> "Other";
                default -> profile.getGender();
            };
            Label genderValue = new Label(genderText);
            genderValue.getStyleClass().add("match-info-value");
            
            genderBox.getChildren().addAll(genderLabel, genderValue);
            infoContainer.getChildren().add(genderBox);
        }
        
        // Цель отношений
        if (profile.getRelationshipGoal() != null && !profile.getRelationshipGoal().isEmpty()) {
            HBox goalBox = new HBox(5);
            goalBox.setAlignment(Pos.CENTER_LEFT);
            
            Label goalLabel = new Label("Looking for:");
            goalLabel.getStyleClass().add("match-info-label");
            
            Label goalValue = new Label(profile.getRelationshipGoal());
            goalValue.getStyleClass().add("match-info-value");
            
            goalBox.getChildren().addAll(goalLabel, goalValue);
            infoContainer.getChildren().add(goalBox);
        }
        
        // Нижняя часть: кнопки действий
        HBox actionsContainer = new HBox();
        actionsContainer.getStyleClass().add("match-actions-container");
        actionsContainer.setSpacing(10);
        actionsContainer.setAlignment(Pos.CENTER);
        
        // Кнопка профиля
        Button profileBtn = new Button("Profile");
        profileBtn.getStyleClass().add("profile-btn");
        profileBtn.setOnAction(e -> showUserProfile(profile));
        
        // Кнопка чата
        Button chatBtn = new Button("Chat");
        chatBtn.getStyleClass().add("chat-btn");
        chatBtn.setOnAction(e -> openChat(profile));
        
        // Кнопка удаления
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(e -> deleteMatch(profile));
        
        actionsContainer.getChildren().addAll(profileBtn, chatBtn, deleteBtn);
        
        // Собираем карточку
        card.getChildren().addAll(header, infoContainer, actionsContainer);
        
        return card;
    }

    // Установить аватар по умолчанию
    private void setDefaultAvatar(ImageView avatarImage) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/tetoo_logo_small.png"));
            avatarImage.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("Error loading default avatar: " + e.getMessage());
        }
    }

    // Показать профиль пользователя (заглушка)
    private void showUserProfile(UserProfile profile) {
        System.out.println("Showing profile of: " + profile.getFirstName() + " " + profile.getLastName());
        // TODO: Реализовать открытие профиля
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile View");
        alert.setHeaderText("Profile: " + profile.getFirstName() + " " + profile.getLastName());
        alert.setContentText("This feature will be implemented later.");
        alert.showAndWait();
    }

    // Открыть чат с пользователем
    private void openChat(UserProfile profile) {
        System.out.println("Opening chat with: " + profile.getFirstName() + " " + profile.getLastName());
        // TODO: Реализовать открытие чата
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Open Chat");
        alert.setHeaderText("Chat with " + profile.getFirstName());
        alert.setContentText("This feature will be implemented later.");
        alert.showAndWait();
    }

    // Удалить матч
    private void deleteMatch(UserProfile profile) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Match");
        confirmation.setHeaderText("Delete match with " + profile.getFirstName() + "?");
        confirmation.setContentText("This action cannot be undone. The match will be removed for both users.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = matchService.deleteMatch(currentUserId, profile.getUserId());
                if (success) {
                    System.out.println("Match deleted with: " + profile.getFirstName());
                    loadMatches(); // Перезагружаем список
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText("Failed to delete match");
                    error.setContentText("Please try again later.");
                    error.showAndWait();
                }
            }
        });
    }

    // Метод для обновления данных при открытии вкладки
    public void refresh() {
        loadMatches();
    }
}