// потом удалить

package com.example.tetoo.util;

import java.io.InputStream;
import java.net.URL;

public class ResourceTester {
    public static void testResources() {
        System.out.println("=== ТЕСТИРОВАНИЕ РЕСУРСОВ ===");

        // Тестируем FXML
        testResource("/fxml/Main.fxml", "FXML файл");

        // Тестируем изображения
        testResource("/images/tetoo_logo.png", "Логотип");
        testResource("/images/tetoo_icon.png", "Иконка");

        // Тестируем шрифт
        testResource("/fonts/Gothra.ttf", "Шрифт Gothra");
    }

    private static void testResource(String path, String description) {
        System.out.println("\n🔍 Проверка: " + description);
        System.out.println("Путь: " + path);

        try {
            // Способ 1: через getResource
            URL url = ResourceTester.class.getResource(path);
            if (url != null) {
                System.out.println("✅ URL найден: " + url);
            } else {
                System.out.println("❌ URL не найден");
            }

            // Способ 2: через getResourceAsStream
            InputStream stream = ResourceTester.class.getResourceAsStream(path);
            if (stream != null) {
                System.out.println("✅ InputStream найден, размер: " + stream.available() + " байт");
                stream.close();
            } else {
                System.out.println("❌ InputStream null");
            }

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
}