package com.example.tetoo.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.example.tetoo.config.SupabaseConfig;
import com.example.tetoo.model.User;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.util.ArrayList;
import java.util.List;

public class SupabaseService {
    private static final Gson gson = new Gson();
    
    // Метод для тестирования подключения и получения всех пользователей
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Создаем GET запрос к таблице users
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE;
            HttpGet request = new HttpGet(url);
            
            // Устанавливаем заголовки
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            // Выполняем запрос
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                
                // Парсим JSON ответ
                JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
                
                for (JsonElement element : jsonArray) {
                    User user = gson.fromJson(element, User.class);
                    users.add(user);
                }
                
                System.out.println("Успешно получено " + users.size() + " пользователей");
            } else {
                System.out.println("Ошибка при запросе. Код: " + statusCode);
            }
            
        } catch (Exception e) {
            System.out.println("Ошибка при подключении к Supabase: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return users;
    }
    
    // Метод для теста подключения (минимальный запрос)
    public boolean testConnection() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Простой запрос к таблице users с limit=1
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE + "?limit=1";
            HttpGet request = new HttpGet(url);
            
            // Устанавливаем заголовки
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            // Выполняем запрос
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            return statusCode == 200;
            
        } catch (Exception e) {
            System.out.println("Ошибка при тестировании подключения: " + e.getMessage());
            return false;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
