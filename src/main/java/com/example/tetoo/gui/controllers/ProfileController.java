package com.example.tetoo.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ProfileController {

    // Avatar Section
    @FXML private ImageView avatarImageView;
    @FXML private Button changeAvatarBtn;

    // Basic Info
    @FXML private Label welcomeLabel;
    @FXML private Label profileName;
    @FXML private Label profileAge;
    @FXML private Label verifiedBadge;
    @FXML private Label profileLocation;
    @FXML private Label profileStatus;

    // Form Fields
    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextArea aboutMeTextArea;
    @FXML private ComboBox<String> relationshipGoalComboBox;
    @FXML private TextField interestsField;

    // Buttons
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    // Stats
    @FXML private Label likesCount;
    @FXML private Label matchesCount;
    @FXML private Label visitsCount;
    @FXML private Label responseRate;

    // Status
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        System.out.println("ProfileController initialized");
        // Заглушка для инициализации
    }

    @FXML
    private void handleBackToDashboard() {
        System.out.println("Back to Dashboard clicked");
        // Заглушка
    }

    // Все методы-заглушки для кнопок
    @FXML
    private void handleChangeAvatar() {
        System.out.println("Change Avatar clicked - STUB");
        // Заглушка для загрузки аватарки
    }

    @FXML
    private void handleCancel() {
        System.out.println("Cancel clicked - STUB");
        // Заглушка для отмены изменений
    }

    @FXML
    private void handleSave() {
        System.out.println("Save Changes clicked - STUB");
        // Заглушка для сохранения изменений
    }
}