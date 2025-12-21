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
        System.out.println("=== TETOO Application Launch ===");
        
        // Initialize Supabase service
        supabaseService = new SupabaseService();
        
        splashScreen = new SplashScreen(primaryStage, () -> {
            System.out.println("Switching to main screen...");
            
            // Test database connection
            testSupabaseConnection();
            supabaseService.createTestChat(10, 11);
            
            Platform.runLater(() -> showMainScreen(primaryStage));
        });
        splashScreen.show();
    }

    private void testSupabaseConnection() {
        System.out.println("\n=== Supabase Connection Test ===");
        
        boolean isConnected = supabaseService.testConnection();
        System.out.println("Connection to Supabase: " + (isConnected ? "SUCCESS" : "ERROR"));
        
        if (isConnected) {
            // Get all users
            System.out.println("\n=== Retrieving Users from Database ===");
            List<User> users = supabaseService.getAllUsers();
            
            // Output users to console
            if (users.isEmpty()) {
                System.out.println("No users found in database");
            } else {
                System.out.println("Found users: " + users.size());
                System.out.println("\nUser list:");
                for (User user : users) {
                    System.out.println(user);
                }
            }
        } else {
            System.out.println("Failed to connect to database. Check connection settings.");
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

            // Load application icon
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/tetoo_icon.png")));
            } catch (Exception e) {
                System.err.println("Failed to load application icon");
            }

            stage.setTitle("TETOO - Dating Application");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            System.out.println("\n=== Main window successfully loaded ===");

        } catch (Exception e) {
            System.err.println("Error loading main screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("=== Application Shutdown ===");
        if (splashScreen != null) {
            splashScreen.stop();
        }
        // Add Supabase resource cleanup here if needed
        if (supabaseService != null) {
            // supabaseService.close(); // if such method exists
        }
    }

    public static void main(String[] args) {
        // If only test without GUI is needed, pass the argument
        if (args.length > 0 && args[0].equals("--test-only")) {
            testOnlyMode();
        } else {
            // Launch full GUI application
            launch(args);
        }
    }
    
    private static void testOnlyMode() {
        System.out.println("=== TEST MODE (No GUI) ===");
        System.out.println("=== Supabase Connection Test ===");
        
        SupabaseService supabaseService = new SupabaseService();
        
        // Connection test
        boolean isConnected = supabaseService.testConnection();
        System.out.println("Connection to Supabase: " + (isConnected ? "SUCCESS" : "ERROR"));
        
        if (isConnected) {
            // Get all users
            System.out.println("\n=== Retrieving Users from Database ===");
            List<User> users = supabaseService.getAllUsers();
            
            // Output users to console
            if (users.isEmpty()) {
                System.out.println("No users found in database");
            } else {
                System.out.println("Found users: " + users.size());
                System.out.println("\nUser list:");
                for (User user : users) {
                    System.out.println(user);
                }
            }
        } else {
            System.out.println("Failed to connect to database. Check connection settings.");
        }
        
        System.exit(0);
    }
}