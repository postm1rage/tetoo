package com.example.tetoo.test;

import com.example.tetoo.service.SupabaseService;
import com.example.tetoo.model.User;
import com.example.tetoo.auth.SessionManager;
import com.example.tetoo.gui.controllers.ProfileController;

public class TestFullProfileFlow {
    public static void main(String[] args) {
        System.out.println("=== Полный тест потока профиля ===");

        SupabaseService service = new SupabaseService();

        // 1. Тест неверифицированного пользователя (должен увидеть тест)
        System.out.println("\n1. Тест с пользователем b@b.ru (не верифицирован):");
        User testUser = service.getUserByEmail("b@b.ru");
        if (testUser != null) {
            SessionManager.getInstance().login(testUser);
            System.out.println("ID пользователя: " + testUser.getId());
            System.out.println("Верифицирован: " + testUser.getIs_verified());

            // Проверяем, есть ли у него профиль
            boolean hasProfile = checkIfUserHasProfile(String.valueOf(testUser.getId()));
            System.out.println("Есть профиль в БД: " + hasProfile);

            if (!hasProfile && !Boolean.TRUE.equals(testUser.getIs_verified())) {
                System.out.println("✅ Этот пользователь УВИДИТ тест создания профиля");
            }
        }

        // 2. Тест верифицированного пользователя (должен увидеть профиль)
        System.out.println("\n2. Тест с пользователем alex.petrov@example.com (верифицирован):");
        User verifiedUser = service.getUserByEmail("alex.petrov@example.com");
        if (verifiedUser != null) {
            SessionManager.getInstance().login(verifiedUser);
            System.out.println("ID пользователя: " + verifiedUser.getId());

            boolean hasProfile = checkIfUserHasProfile(String.valueOf(verifiedUser.getId()));
            System.out.println("Есть профиль в БД: " + hasProfile);

            if (hasProfile && Boolean.TRUE.equals(verifiedUser.getIs_verified())) {
                System.out.println("✅ Этот пользователь УВИДИТ свой профиль");

                // Можем даже показать профиль
                System.out.println("\n3. Показ профиля пользователя ID=1:");
                // Здесь можно вызвать showUserProfile если сделать метод статическим
                // или создать экземпляр контроллера
            }
        }

        System.out.println("\n=== Для полного теста запустите приложение и войдите как: ===");
        System.out.println("1. b@b.ru (без пароля) -> должен увидеть тест создания профиля");
        System.out.println("2. alex.petrov@example.com (без пароля) -> должен увидеть готовый профиль");
    }

    private static boolean checkIfUserHasProfile(String userId) {
        try {
            // Простая проверка через HTTP
            java.net.URL url = new java.net.URL(
                    "https://eruzshyrvfwjcehpwjmp.supabase.co/rest/v1/user_profiles?user_id=eq." + userId);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVydXpzaHlydmZ3amNlaHB3am1wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU2NzQ5MTMsImV4cCI6MjA4MTI1MDkxM30.mA5cbodteVMWC92Ui_JpERHtboxE2hB5P30qtpvYSYk");
            conn.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVydXpzaHlydmZ3amNlaHB3am1wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU2NzQ5MTMsImV4cCI6MjA4MTI1MDkxM30.mA5cbodteVMWC92Ui_JpERHtboxE2hB5P30qtpvYSYk");

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return !response.toString().equals("[]") && response.length() > 2;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}