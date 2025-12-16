package com.tetoo_team;

import com.tetoo_team.service.SupabaseService;
import com.tetoo_team.model.User;
import java.util.List;

public class App {
    public static void main(String[] args) {
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
    }
}