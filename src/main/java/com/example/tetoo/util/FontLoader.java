package com.example.tetoo.util;

import javafx.scene.text.Font;

public class FontLoader {
    private static boolean fontLoaded = false;
    private static String fontFamily = "Arial"; // По умолчанию Arial если шрифт не загрузится

    public static void loadCustomFont() {
        if (!fontLoaded) {
            try {
                // Пробуем загрузить шрифт
                java.net.URL fontUrl = FontLoader.class.getResource("/fonts/Gothra.ttf");
                if (fontUrl != null) {
                    Font font = Font.loadFont(fontUrl.toExternalForm(), 12);
                    if (font != null) {
                        fontFamily = font.getFamily();
                        System.out.println("Font loaded: " + fontFamily);
                    }
                }
            } catch (Exception e) {
                System.err.println("Font loading error: " + e.getMessage());
                fontFamily = "Arial"; // Используем Arial если ошибка
            }
            fontLoaded = true;
        }
    }

    public static String getFontFamily() {
        return fontFamily;
    }
}