package com.example.tetoo.gui.controllers.anim;

import javafx.animation.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.GaussianBlur;
import javafx.util.Duration;

public class BackgroundAnimator {

    private final StackPane mainContainer;
    private final VBox mainContent;
    private final ImageView logoImageView;
    private Timeline liquidAnimation;

    public BackgroundAnimator(StackPane mainContainer, VBox mainContent, ImageView logoImageView) {
        this.mainContainer = mainContainer;
        this.mainContent = mainContent;
        this.logoImageView = logoImageView;
        setupBackground();
    }

    private void setupBackground() {
        mainContainer.getChildren().clear();

        // 1. Базовый фон
        Rectangle baseBackground = new Rectangle();
        baseBackground.widthProperty().bind(mainContainer.widthProperty());
        baseBackground.heightProperty().bind(mainContainer.heightProperty());
        baseBackground.setFill(Color.web("#0a0a0a"));
        mainContainer.getChildren().add(baseBackground);

        // 2. Слой с волнами и sparkle
        StackPane liquidContainer = new StackPane();
        liquidContainer.prefWidthProperty().bind(mainContainer.widthProperty());
        liquidContainer.prefHeightProperty().bind(mainContainer.heightProperty());
        liquidContainer.setOpacity(0.9);

        createWaveLayers(liquidContainer);
        liquidAnimation = createLiquidFadeAnimation(liquidContainer);

        mainContainer.getChildren().add(liquidContainer);

        // 3. Gradient сверху (тоже поверх волн)
        Rectangle topGradient = new Rectangle();
        topGradient.widthProperty().bind(mainContainer.widthProperty());
        topGradient.heightProperty().bind(mainContainer.heightProperty());
        topGradient.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(0,0,0,0.3)),
                new Stop(0.5, Color.TRANSPARENT),
                new Stop(1, Color.rgb(0,0,0,0.2))
        ));
        mainContainer.getChildren().add(topGradient);

        // 4. Контент поверх всего (mainContent + логотип)
        StackPane contentPane = new StackPane();
        contentPane.getChildren().addAll(mainContent, logoImageView);
        mainContainer.getChildren().add(contentPane);

        play();
    }

    private void createWaveLayers(StackPane container) {
        Rectangle[] waveLayers = new Rectangle[4];
        Color[] waveColors = {
                Color.web("#ec4b6e").deriveColor(0,1,1,0.6),
                Color.web("#ff6b8b").deriveColor(0,1,1,0.5),
                Color.web("#d43f5e").deriveColor(0,1,1,0.4),
                Color.web("#ff8fa3").deriveColor(0,1,1,0.3)
        };

        for (int i = 0; i < waveLayers.length; i++) {
            Rectangle wave = new Rectangle();
            wave.widthProperty().bind(mainContainer.widthProperty());
            wave.heightProperty().bind(mainContainer.heightProperty());
            wave.setFill(new RadialGradient(
                    0,0,0.5,0.5,0.7,true,CycleMethod.NO_CYCLE,
                    new Stop(0, waveColors[i]),
                    new Stop(0.5, waveColors[i].deriveColor(0,1,1,0.3)),
                    new Stop(1, Color.TRANSPARENT)
            ));
            wave.setOpacity(0.4 + i*0.1);
            wave.setEffect(new GaussianBlur(30 + i*10));

            container.getChildren().add(wave);
            setupWaveAnimation(wave, i);
        }

        container.getChildren().add(createSparkleEffect());
    }

    private void setupWaveAnimation(Rectangle wave, int index) {
        double duration = 8 + index*2;
        double amplitude = 50 + index*20;

        TranslateTransition move = new TranslateTransition(Duration.seconds(duration), wave);
        move.setByX(index % 2 == 0 ? amplitude : -amplitude);
        move.setByY(index % 2 == 0 ? amplitude/2 : -amplitude/2);
        move.setAutoReverse(true);
        move.setCycleCount(Timeline.INDEFINITE);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(duration*1.5), wave);
        scale.setToX(1.3);
        scale.setToY(1.3);
        scale.setAutoReverse(true);
        scale.setCycleCount(Timeline.INDEFINITE);

        move.play();
        scale.play();
    }

    private Rectangle createSparkleEffect() {
        Rectangle sparkle = new Rectangle();
        sparkle.widthProperty().bind(mainContainer.widthProperty());
        sparkle.heightProperty().bind(mainContainer.heightProperty());
        sparkle.setOpacity(0.1);
        sparkle.setFill(new RadialGradient(
                0,0,0.5,0.5,0.5,true,CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.7, Color.TRANSPARENT),
                new Stop(0.8, Color.web("#ffffff").deriveColor(0,1,1,0.1)),
                new Stop(0.9, Color.TRANSPARENT),
                new Stop(1, Color.TRANSPARENT)
        ));

        Timeline anim = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sparkle.opacityProperty(),0.1)),
                new KeyFrame(Duration.seconds(2), new KeyValue(sparkle.opacityProperty(),0.3)),
                new KeyFrame(Duration.seconds(4), new KeyValue(sparkle.opacityProperty(),0.1))
        );
        anim.setCycleCount(Timeline.INDEFINITE);
        anim.setAutoReverse(true);
        anim.play();

        return sparkle;
    }

    private Timeline createLiquidFadeAnimation(StackPane container) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(container.opacityProperty(), 0.8)),
                new KeyFrame(Duration.seconds(5), new KeyValue(container.opacityProperty(),0.6)),
                new KeyFrame(Duration.seconds(10), new KeyValue(container.opacityProperty(),0.8))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    public void play() {
        if (liquidAnimation != null) liquidAnimation.play();
    }

    public void stop() {
        if (liquidAnimation != null) liquidAnimation.stop();
    }
}
