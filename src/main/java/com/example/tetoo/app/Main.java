package com.example.tetoo.app;

import com.example.tetoo.service.SupabaseService;
import com.example.tetoo.model.User;
import com.example.tetoo.gui.views.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private SplashScreen splashScreen;
    private SupabaseService supabaseService;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("=== Запуск приложения TETOO ===");
        
        // Инициализируем сервис Supabase
        supabaseService = new SupabaseService();
        
        splashScreen = new SplashScreen(primaryStage, () -> {
            System.out.println("Переход к главному экрану...");
            
            // Тестируем подключение к базе данных
            testSupabaseConnection();
            
            Platform.runLater(() -> showMainScreen(primaryStage));
        });
        splashScreen.show();
    }

    private void testSupabaseConnection() {
        System.out.println("\n=== Тест подключения к Supabase ===");
        
        boolean isConnected = supabaseService.testConnection();
        System.out.println("Подключение к Supabase: " + (isConnected ? "УСПЕШНО" : "ОШИБКА"));
        
        if (isConnected) {
            // Получаем всех пользователей
            System.out.println("\n=== Получение пользователей из базы данных ===");
            List<User> users = supabaseService.getAllUsers();
            
            // Выводим пользователей в консоль
            if (users.isEmpty()) {
                System.out.println("В базе данных нет пользователей");
            } else {
                System.out.println("Найдено пользователей: " + users.size());
                System.out.println("\nСписок пользователей:");
                for (User user : users) {
                    System.out.println(user);
                }
            }
        } else {
            System.out.println("Не удалось подключиться к базе данных. Проверьте настройки подключения.");
        }
    }

    private void showMainScreen(Stage stage) {
        if (splashScreen != null) {
            splashScreen.stop();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 600);

            // Загружаем иконку приложения
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/tetoo_icon.png")));
            } catch (Exception e) {
                System.err.println("Не удалось загрузить иконку приложения");
            }

            stage.setTitle("TETOO - Сайт знакомств");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            System.out.println("\n=== Главное окно успешно загружено ===");

        } catch (Exception e) {
            System.err.println("Ошибка загрузки главного экрана: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("=== Завершение работы приложения ===");
        if (splashScreen != null) {
            splashScreen.stop();
        }
        // Здесь можно добавить закрытие ресурсов Supabase
        if (supabaseService != null) {
            // supabaseService.close(); // если такой метод есть
        }
    }

    public static void main(String[] args) {
        // Если нужен только тест без GUI, можно передать аргумент
        if (args.length > 0 && args[0].equals("--test-only")) {
            testOnlyMode();
        } else {
            // Запускаем полноценное GUI приложение
            launch(args);
        }
    }
    
    private static void testOnlyMode() {
        System.out.println("=== ТЕСТОВЫЙ РЕЖИМ (без GUI) ===");
        System.out.println("=== Тест подключения к Supabase ===");
        
        SupabaseService supabaseService = new SupabaseService();
        
        // Тест подключения
        boolean isConnected = supabaseService.testConnection();
        System.out.println("Подключение к Supabase: " + (isConnected ? "УСПЕШНО" : "ОШИБКА"));
        
        if (isConnected) {
            // Получаем всех пользователей
            System.out.println("\n=== Получение пользователей из базы данных ===");
            List<User> users = supabaseService.getAllUsers();
            
            // Выводим пользователей в консоль
            if (users.isEmpty()) {
                System.out.println("В базе данных нет пользователей");
            } else {
                System.out.println("Найдено пользователей: " + users.size());
                System.out.println("\nСписок пользователей:");
                for (User user : users) {
                    System.out.println(user);
                }
            }
        } else {
            System.out.println("Не удалось подключиться к базе данных. Проверьте настройки подключения.");
        }
        
        System.exit(0);
    }
}