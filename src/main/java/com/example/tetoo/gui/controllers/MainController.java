package com.example.tetoo.gui.controllers;

import com.example.tetoo.util.FontLoader;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane mainContainer;

    @FXML
    private ImageView logoImageView;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private VBox mainContent;

    @FXML
    private VBox buttonsContainer;

    private Timeline liquidAnimation;
    private Timeline waveAnimation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FontLoader.loadCustomFont();
        setupLiquidWaveBackground();
        setupStyles();
        setupAnimations();
        loadLogo();
    }

    private void setupLiquidWaveBackground() {

        mainContainer.getChildren().clear();


        Rectangle baseBackground = new Rectangle();
        baseBackground.widthProperty().bind(mainContainer.widthProperty());
        baseBackground.heightProperty().bind(mainContainer.heightProperty());
        baseBackground.setFill(Color.web("#0a0a0a"));
        mainContainer.getChildren().add(baseBackground);


        StackPane liquidContainer = new StackPane();
        liquidContainer.prefWidthProperty().bind(mainContainer.widthProperty());
        liquidContainer.prefHeightProperty().bind(mainContainer.heightProperty());
        liquidContainer.setOpacity(0.9);


        Rectangle[] waveLayers = new Rectangle[4];


        Color[] waveColors = {
                Color.web("#ec4b6e").deriveColor(0, 1.0, 1.0, 0.6),  // Основной
                Color.web("#ff6b8b").deriveColor(0, 1.0, 1.0, 0.5),  // Светлый
                Color.web("#d43f5e").deriveColor(0, 1.0, 1.0, 0.4),  // Темный
                Color.web("#ff8fa3").deriveColor(0, 1.0, 1.0, 0.3)   // Очень светлый
        };

        for (int i = 0; i < waveLayers.length; i++) {
            Rectangle wave = new Rectangle();
            wave.widthProperty().bind(mainContainer.widthProperty());
            wave.heightProperty().bind(mainContainer.heightProperty());


            RadialGradient waveGradient = new RadialGradient(
                    0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                    new Stop(0, waveColors[i]),
                    new Stop(0.5, waveColors[i].deriveColor(0, 1.0, 1.0, 0.3)),
                    new Stop(1, Color.TRANSPARENT)
            );

            wave.setFill(waveGradient);
            wave.setOpacity(0.4 + i * 0.1);

            // Добавляем размытие для плавности
            GaussianBlur blur = new GaussianBlur();
            blur.setRadius(30 + i * 10);
            wave.setEffect(blur);

            waveLayers[i] = wave;
            liquidContainer.getChildren().add(wave);
        }

        mainContainer.getChildren().add(liquidContainer);


        waveAnimation = new Timeline();
        waveAnimation.setCycleCount(Timeline.INDEFINITE);
        waveAnimation.setAutoReverse(true);


        for (int i = 0; i < waveLayers.length; i++) {
            final Rectangle wave = waveLayers[i];


            double duration = 8 + i * 2;
            double amplitude = 50 + i * 20;


            TranslateTransition waveMove = new TranslateTransition(Duration.seconds(duration), wave);
            waveMove.setByX(i % 2 == 0 ? amplitude : -amplitude);
            waveMove.setByY(i % 2 == 0 ? amplitude/2 : -amplitude/2);
            waveMove.setAutoReverse(true);
            waveMove.setCycleCount(Timeline.INDEFINITE);


            ScaleTransition waveScale = new ScaleTransition(Duration.seconds(duration * 1.5), wave);
            waveScale.setToX(1.3);
            waveScale.setToY(1.3);
            waveScale.setAutoReverse(true);
            waveScale.setCycleCount(Timeline.INDEFINITE);


            waveMove.play();
            waveScale.play();
        }


        liquidAnimation = new Timeline();
        liquidAnimation.setCycleCount(Timeline.INDEFINITE);


        KeyFrame fadeIn = new KeyFrame(Duration.ZERO,
                new KeyValue(liquidContainer.opacityProperty(), 0.8)
        );

        KeyFrame fadeOut = new KeyFrame(Duration.seconds(5),
                new KeyValue(liquidContainer.opacityProperty(), 0.6)
        );

        KeyFrame fadeInAgain = new KeyFrame(Duration.seconds(10),
                new KeyValue(liquidContainer.opacityProperty(), 0.8)
        );

        liquidAnimation.getKeyFrames().addAll(fadeIn, fadeOut, fadeInAgain);


        Rectangle sparkleLayer = createSparkleEffect();
        liquidContainer.getChildren().add(sparkleLayer);


        Rectangle topGradient = new Rectangle();
        topGradient.widthProperty().bind(mainContainer.widthProperty());
        topGradient.heightProperty().bind(mainContainer.heightProperty());
        LinearGradient topGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0, 0, 0, 0.3)),
                new Stop(0.5, Color.TRANSPARENT),
                new Stop(1, Color.rgb(0, 0, 0, 0.2))
        );
        topGradient.setFill(topGrad);
        mainContainer.getChildren().add(topGradient);


        StackPane contentPane = new StackPane();
        contentPane.getChildren().addAll(mainContent, logoImageView);
        mainContainer.getChildren().add(contentPane);


        waveAnimation.play();
        liquidAnimation.play();
    }

    private Rectangle createSparkleEffect() {
        Rectangle sparkle = new Rectangle();
        sparkle.widthProperty().bind(mainContainer.widthProperty());
        sparkle.heightProperty().bind(mainContainer.heightProperty());
        sparkle.setOpacity(0.1);


        RadialGradient sparkleGrad = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.7, Color.TRANSPARENT),
                new Stop(0.8, Color.web("#ffffff").deriveColor(0, 1.0, 1.0, 0.1)),
                new Stop(0.9, Color.TRANSPARENT),
                new Stop(1, Color.TRANSPARENT)
        );

        sparkle.setFill(sparkleGrad);


        Timeline sparkleAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sparkle.opacityProperty(), 0.1)
                ),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(sparkle.opacityProperty(), 0.3)
                ),
                new KeyFrame(Duration.seconds(4),
                        new KeyValue(sparkle.opacityProperty(), 0.1)
                )
        );
        sparkleAnim.setCycleCount(Timeline.INDEFINITE);
        sparkleAnim.setAutoReverse(true);
        sparkleAnim.play();

        return sparkle;
    }

    private void setupStyles() {
        String fontFamily = FontLoader.getFontFamily();


        String registerStyle = String.format(
                "-fx-font-family: '%s';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #ec4b6e;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 15 50;" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(236,75,110,0.5), 15, 0, 0, 5);",
                fontFamily
        );


        String loginStyle = String.format(
                "-fx-font-family: '%s';" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #ff8fa3;" +
                        "-fx-background-color: transparent;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 45;" +
                        "-fx-border-radius: 25;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-color: #ec4b6e;" +
                        "-fx-cursor: hand;",
                fontFamily
        );

        registerButton.setStyle(registerStyle);
        loginButton.setStyle(loginStyle);


        registerButton.setOnMouseEntered(e -> {
            registerButton.setStyle(registerStyle +
                    "-fx-background-color: #ff6b8b;" +
                    "-fx-scale-x: 1.05;" +
                    "-fx-scale-y: 1.05;");
        });

        registerButton.setOnMouseExited(e -> {
            registerButton.setStyle(registerStyle);
            registerButton.setScaleX(1);
            registerButton.setScaleY(1);
        });

        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle(loginStyle +
                    "-fx-text-fill: #ff6b8b;" +
                    "-fx-border-color: #ff6b8b;" +
                    "-fx-background-color: rgba(236,75,110,0.1);" +
                    "-fx-scale-x: 1.05;" +
                    "-fx-scale-y: 1.05;");
        });

        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle(loginStyle);
            loginButton.setScaleX(1);
            loginButton.setScaleY(1);
        });


        registerButton.setCursor(Cursor.HAND);
        loginButton.setCursor(Cursor.HAND);


        mainContent.setAlignment(Pos.CENTER);
        mainContent.setSpacing(50);
        mainContent.setPadding(new Insets(0, 0, 100, 0));

        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setSpacing(25);
    }

    private void setupAnimations() {

        mainContent.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), mainContent);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void loadLogo() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/tetoo_logo.png"));
            logoImageView.setImage(logo);
            logoImageView.setFitWidth(70);
            logoImageView.setFitHeight(70);
            logoImageView.setPreserveRatio(true);


            StackPane.setAlignment(logoImageView, Pos.TOP_LEFT);
            StackPane.setMargin(logoImageView, new Insets(25, 0, 0, 25));


            DropShadow logoShadow = new DropShadow();
            logoShadow.setColor(Color.web("#ec4b6e").deriveColor(0, 1.0, 1.0, 0.3));
            logoShadow.setRadius(5);
            logoImageView.setEffect(logoShadow);

        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
            logoImageView.setVisible(false);
        }
    }

    @FXML
    private void handleRegister() {
        animateButtonClick(registerButton);
        System.out.println("Registration button pressed");
    }

    @FXML
    private void handleLogin() {
        animateButtonClick(loginButton);
        System.out.println("Login button pressed");
    }

    private void animateButtonClick(Button button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1);
        scaleUp.setToY(1);

        SequentialTransition clickAnim = new SequentialTransition(scaleDown, scaleUp);
        clickAnim.play();
    }


    public void stopAnimations() {
        if (liquidAnimation != null) {
            liquidAnimation.stop();
        }
        if (waveAnimation != null) {
            waveAnimation.stop();
        }
    }
}