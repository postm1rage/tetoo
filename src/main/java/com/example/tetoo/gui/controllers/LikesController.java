package com.example.tetoo.gui.controllers;

import com.example.tetoo.auth.SessionManager;
import com.example.tetoo.model.UserProfile;
import com.example.tetoo.service.LikeService;
import com.example.tetoo.service.interfaces.ILikeService;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class LikesController implements Initializable {

    @FXML
    private VBox mainContainer;
    @FXML
    private StackPane userCardContainer;
    @FXML
    private ImageView userAvatar;
    @FXML
    private Label userName;
    @FXML
    private Label userAge;
    @FXML
    private Label userLocation;
    @FXML
    private Label userGender;
    @FXML
    private Label userGoal;
    @FXML
    private TextArea userAbout;
    @FXML
    private FlowPane interestsPane;
    @FXML
    private VBox noUsersContainer;
    @FXML
    private Label noUsersLabel;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button dislikeBtn;
    @FXML
    private Button likeBtn;
    @FXML
    private Label sentTodayCount;
    @FXML
    private Label totalLikesCount;
    @FXML
    private Label matchesCount;
    @FXML
    private ProgressBar loadingProgress;

    private ILikeService likeService;
    private UserProfile currentUserProfile;
    private Integer currentUserId;
    private List<UserProfile> allUsers;
    private int likesSentToday = 0;
    private int totalLikesSent = 0;
    private int matchesMade = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Инициализация сервиса лайков
        likeService = new LikeService();
        
        // Получаем текущий ID пользователя из сессии
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        
        if (currentUserId == null) {
            System.out.println("Error: No user logged in");
            return;
        }
        
        // Настройка кнопок
        setupButtons();
        
        // Загружаем первого пользователя
        loadNextUser();
        
        // Обновляем статистику
        updateStats();
    }

    private void setupButtons() {
        // Настройка обработчиков кнопок
        refreshBtn.setOnAction(e -> handleRefresh());
        
        // Анимация при наведении на кнопки
        likeBtn.setOnMouseEntered(e -> {
            likeBtn.setScaleX(1.1);
            likeBtn.setScaleY(1.1);
        });
        
        likeBtn.setOnMouseExited(e -> {
            likeBtn.setScaleX(1.0);
            likeBtn.setScaleY(1.0);
        });
        
        dislikeBtn.setOnMouseEntered(e -> {
            dislikeBtn.setScaleX(1.1);
            dislikeBtn.setScaleY(1.1);
        });
        
        dislikeBtn.setOnMouseExited(e -> {
            dislikeBtn.setScaleX(1.0);
            dislikeBtn.setScaleY(1.0);
        });
    }

    @FXML
    private void handleLike() {
        if (currentUserProfile == null) return;
        
        Integer targetUserId = currentUserProfile.getUserId(); // Изменяем getUserId()
        
        // Отладочная информация
        System.out.println("Attempting to like user ID: " + targetUserId);
        System.out.println("Current user ID: " + currentUserId);
        
        if (targetUserId == null) {
            System.out.println("ERROR: Target user ID is null!");
            showError("Cannot like user: user ID not found");
            return;
        }
        
        // Отправляем лайк
        boolean success = likeService.sendLike(currentUserId, targetUserId, "like");
        
        if (success) {
            System.out.println("Liked user: " + currentUserProfile.getFirstName());
            likesSentToday++;
            totalLikesSent++;
            
            // Проверяем на взаимный лайк
            boolean isMutualLike = likeService.checkMutualLike(currentUserId, targetUserId);
            if (isMutualLike) {
                matchesMade++;
                System.out.println("Mutual like! Match created with " + currentUserProfile.getFirstName());
            }
            
            // Показываем анимацию лайка
            showLikeAnimation();
        } else {
            System.out.println("Failed to send like");
            showError("Failed to send like");
        }
        
        // Загружаем следующего пользователя через 500ms для анимации
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                loadNextUser();
                updateStats();
            });
        }).start();
    }

    @FXML
    private void handleDislike() {
        if (currentUserProfile == null) return;
        
        Integer targetUserId = currentUserProfile.getUserId(); // Изменяем getUserId()
        
        if (targetUserId == null) {
            System.out.println("ERROR: Target user ID is null!");
            showError("Cannot dislike user: user ID not found");
            return;
        }
        
        // Отправляем дизлайк
        boolean success = likeService.sendLike(currentUserId, targetUserId, "dislike");
        
        if (success) {
            System.out.println("Disliked user: " + currentUserProfile.getFirstName());
            
            // Показываем анимацию дизлайка
            showDislikeAnimation();
        } else {
            System.out.println("Failed to send dislike");
            showError("Failed to send dislike");
        }
        
        // Загружаем следующего пользователя через 500ms для анимации
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                loadNextUser();
                updateStats();
            });
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadNextUser();
    }

    private void loadNextUser() {
        // Показываем индикатор загрузки
        loadingProgress.setVisible(true);
        noUsersContainer.setVisible(false);
        userCardContainer.setVisible(false);
        
        // Загружаем случайного пользователя в фоновом потоке
        new Thread(() -> {
            UserProfile randomUser = likeService.getRandomUserForLikes(currentUserId);
            
            Platform.runLater(() -> {
                loadingProgress.setVisible(false);
                
                if (randomUser == null) {
                    // Нет пользователей для показа
                    userCardContainer.setVisible(false);
                    noUsersContainer.setVisible(true);
                    currentUserProfile = null;
                    noUsersLabel.setText("No users available right now. Try again later!");
                } else {
                    // Заполняем карточку данными пользователя
                    currentUserProfile = randomUser;
                    updateUserCard(randomUser);
                    userCardContainer.setVisible(true);
                    noUsersContainer.setVisible(false);
                }
            });
        }).start();
    }

    private void updateUserCard(UserProfile user) {
        System.out.println("Loading user profile: " + user.getFirstName() + " " + user.getLastName());
        System.out.println("About me: " + user.getAboutMe());
        System.out.println("Location: " + user.getCity() + ", " + user.getCountry());
        // Устанавливаем имя
        String fullName = user.getFirstName() + " " + user.getLastName();
        userName.setText(fullName);
        
        // Проверяем ID пользователя
        System.out.println("Loading user profile with ID: " + user.getUserId());
        
        // Устанавливаем возраст
        if (user.getAge() != null) {
            userAge.setText(user.getAge() + " years");
        } else {
            userAge.setText("Age not specified");
        }
        
        // Устанавливаем локацию
        if (user.getCity() != null && user.getCountry() != null) {
            userLocation.setText(user.getCity() + ", " + user.getCountry());
        } else if (user.getCity() != null) {
            userLocation.setText(user.getCity());
        } else if (user.getCountry() != null) {
            userLocation.setText(user.getCountry());
        } else {
            userLocation.setText("Location not specified");
        }
        
        // Устанавливаем пол
        if (user.getGender() != null) {
            String genderText = switch (user.getGender()) {
                case "M" -> "Male";
                case "F" -> "Female";
                case "O" -> "Other";
                default -> user.getGender();
            };
            userGender.setText(genderText);
        } else {
            userGender.setText("Gender not specified");
        }
        
        // Устанавливаем цель отношений
        if (user.getRelationshipGoal() != null && !user.getRelationshipGoal().isEmpty()) {
            userGoal.setText(user.getRelationshipGoal());
            userGoal.setVisible(true);
        } else {
            userGoal.setVisible(false);
        }
        
        // Устанавливаем "О себе"
        if (user.getAboutMe() != null && !user.getAboutMe().isEmpty()) {
            userAbout.setText(user.getAboutMe());
            userAbout.setVisible(true);
        } else {
            userAbout.setText("No information provided");
            userAbout.setVisible(true);
        }
        
        // Устанавливаем аватар
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                // В реальном приложении нужно загружать изображение по URL
                // Здесь используем заглушку
                Image image = new Image(getClass().getResourceAsStream("/images/tetoo_logo_small.png"));
                userAvatar.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading avatar: " + e.getMessage());
                setDefaultAvatar();
            }
        } else {
            setDefaultAvatar();
        }
        
        // Очищаем интересы и добавляем новые
        interestsPane.getChildren().clear();
        
        // Заглушка для интересов - можно будет доработать позже
        String[] sampleInterests = {"Music", "Travel", "Sports", "Movies", "Cooking", "Reading", "Gaming", "Fitness"};
        Random random = new Random();
        int interestCount = random.nextInt(4) + 2; // 2-5 интересов
        
        for (int i = 0; i < interestCount; i++) {
            Label interestLabel = new Label(sampleInterests[random.nextInt(sampleInterests.length)]);
            interestLabel.setStyle(
                "-fx-text-fill: white; " +
                "-fx-font-size: 12px; " +
                "-fx-padding: 3px 8px; " +
                "-fx-background-color: #333333; " +
                "-fx-background-radius: 12px; " +
                "-fx-border-color: #ec4b6e; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 12px;"
            );
            interestsPane.getChildren().add(interestLabel);
        }
    }

    private void setDefaultAvatar() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/tetoo_logo_small.png"));
            userAvatar.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("Error loading default avatar: " + e.getMessage());
        }
    }

    private void updateStats() {
        // Обновляем счетчики
        sentTodayCount.setText(String.valueOf(likesSentToday));
        totalLikesCount.setText(String.valueOf(totalLikesSent));
        matchesCount.setText(String.valueOf(matchesMade));
    }

    private void showLikeAnimation() {
        // Получаем корневой контейнер сцены
        Pane root = (Pane) mainContainer.getScene().getRoot();
        
        // Создаем анимацию лайка
        Label likeAnimation = new Label("♥");
        likeAnimation.setFont(Font.font("System", FontWeight.BOLD, 48));
        likeAnimation.setTextFill(Color.web("#44cc44"));
        likeAnimation.setOpacity(0);
        
        root.getChildren().add(likeAnimation);
        
        // Позиционируем анимацию относительно кнопки лайка
        double sceneX = likeBtn.localToScene(likeBtn.getLayoutX() + likeBtn.getWidth() / 2, 0).getX();
        double sceneY = likeBtn.localToScene(0, likeBtn.getLayoutY() + likeBtn.getHeight() / 2).getY();
        
        likeAnimation.setLayoutX(sceneX - 24);
        likeAnimation.setLayoutY(sceneY - 24);
        
        // Анимация
        Timeline timeline = new Timeline(
            new KeyFrame(
                Duration.millis(0),
                new KeyValue(likeAnimation.opacityProperty(), 0),
                new KeyValue(likeAnimation.translateYProperty(), 0)
            ),
            new KeyFrame(
                Duration.millis(500),
                new KeyValue(likeAnimation.opacityProperty(), 1),
                new KeyValue(likeAnimation.translateYProperty(), -50)
            ),
            new KeyFrame(
                Duration.millis(1000),
                new KeyValue(likeAnimation.opacityProperty(), 0),
                new KeyValue(likeAnimation.translateYProperty(), -100)
            )
        );
        
        timeline.setOnFinished(e -> root.getChildren().remove(likeAnimation));
        timeline.play();
    }

    private void showDislikeAnimation() {
        // Получаем корневой контейнер сцены
        Pane root = (Pane) mainContainer.getScene().getRoot();
        
        // Создаем анимацию дизлайка
        Label dislikeAnimation = new Label("✕");
        dislikeAnimation.setFont(Font.font("System", FontWeight.BOLD, 48));
        dislikeAnimation.setTextFill(Color.web("#ff4444"));
        dislikeAnimation.setOpacity(0);
        
        root.getChildren().add(dislikeAnimation);
        
        // Позиционируем анимацию относительно кнопки дизлайка
        double sceneX = dislikeBtn.localToScene(dislikeBtn.getLayoutX() + dislikeBtn.getWidth() / 2, 0).getX();
        double sceneY = dislikeBtn.localToScene(0, dislikeBtn.getLayoutY() + dislikeBtn.getHeight() / 2).getY();
        
        dislikeAnimation.setLayoutX(sceneX - 24);
        dislikeAnimation.setLayoutY(sceneY - 24);
        
        // Анимация
        Timeline timeline = new Timeline(
            new KeyFrame(
                Duration.millis(0),
                new KeyValue(dislikeAnimation.opacityProperty(), 0),
                new KeyValue(dislikeAnimation.translateYProperty(), 0)
            ),
            new KeyFrame(
                Duration.millis(500),
                new KeyValue(dislikeAnimation.opacityProperty(), 1),
                new KeyValue(dislikeAnimation.translateYProperty(), -50)
            ),
            new KeyFrame(
                Duration.millis(1000),
                new KeyValue(dislikeAnimation.opacityProperty(), 0),
                new KeyValue(dislikeAnimation.translateYProperty(), -100)
            )
        );
        
        timeline.setOnFinished(e -> root.getChildren().remove(dislikeAnimation));
        timeline.play();
    }

    private void showError(String message) {
        // Простое сообщение об ошибке
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Метод для обновления данных при открытии вкладки
    public void refresh() {
        loadNextUser();
        updateStats();
    }
}