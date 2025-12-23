package com.example.tetoo.repository;

import com.example.tetoo.config.SupabaseConfig;
import com.example.tetoo.model.Match;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class MatchRepository {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ISO_DATE_TIME;
    private static final Gson gson = new Gson();
    
    // Создать матч
    public boolean createMatch(Integer user1Id, Integer user2Id) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Убеждаемся, что user1Id < user2Id (как в constraint базы данных)
            int smallerId = Math.min(user1Id, user2Id);
            int largerId = Math.max(user1Id, user2Id);
            
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MATCHES_TABLE;
            HttpPost request = new HttpPost(url);
            
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Prefer", "return=representation");
            
            // Создаем JSON объект для отправки
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("user1_id", smallerId);
            jsonBody.addProperty("user2_id", largerId);
            jsonBody.addProperty("is_active", true);
            
            StringEntity entity = new StringEntity(jsonBody.toString());
            request.setEntity(entity);
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 201) {
                System.out.println("Match created: " + smallerId + " <-> " + largerId);
                return true;
            } else {
                // Если матч уже существует (код 409), то это тоже успех
                if (statusCode == 409) {
                    System.out.println("Match already exists: " + smallerId + " <-> " + largerId);
                    return true;
                }
                System.out.println("Error creating match. Code: " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error creating match: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Получить все активные матчи пользователя
    public List<Match> getMatchesForUser(Integer userId) {
        List<Match> matches = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Ищем матчи, где пользователь является user1 или user2
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MATCHES_TABLE + 
                        "?or=(user1_id.eq." + userId + ",user2_id.eq." + userId + ")" +
                        "&is_active=eq.true" +
                        "&order=matched_at.desc";
            
            HttpGet request = new HttpGet(url);
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                
                JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
                
                for (JsonElement element : jsonArray) {
                    Match match = parseMatch(element.getAsJsonObject());
                    matches.add(match);
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error getting matches for user: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return matches;
    }
    
    // Удалить матч
    public boolean deleteMatch(Integer user1Id, Integer user2Id) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Убеждаемся, что user1Id < user2Id
            int smallerId = Math.min(user1Id, user2Id);
            int largerId = Math.max(user1Id, user2Id);
            
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MATCHES_TABLE + 
                        "?user1_id=eq." + smallerId + 
                        "&user2_id=eq." + largerId;
            
            HttpDelete request = new HttpDelete(url);
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200 || statusCode == 204) {
                System.out.println("Match deleted: " + smallerId + " <-> " + largerId);
                return true;
            } else {
                System.out.println("Error deleting match. Code: " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error deleting match: " + e.getMessage());
            return false;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Деактивировать матч (установить is_active = false)
    public boolean deactivateMatch(Integer user1Id, Integer user2Id) {
        // Этот метод можно реализовать позже, если нужно
        // Пока будем удалять матч полностью
        return deleteMatch(user1Id, user2Id);
    }
    
    // Проверить, существует ли матч между пользователями
    public boolean matchExists(Integer user1Id, Integer user2Id) {
        List<Match> matches = getMatchesForUser(user1Id);
        for (Match match : matches) {
            if (match.containsUser(user2Id)) {
                return true;
            }
        }
        return false;
    }
    
    // Парсинг матча из JSON
    private Match parseMatch(JsonObject jsonObject) {
        try {
            Match match = new Match();
            
            if (jsonObject.has("id")) {
                match.setId(jsonObject.get("id").getAsInt());
            }
            if (jsonObject.has("user1_id")) {
                match.setUser1Id(jsonObject.get("user1_id").getAsInt());
            }
            if (jsonObject.has("user2_id")) {
                match.setUser2Id(jsonObject.get("user2_id").getAsInt());
            }
            if (jsonObject.has("matched_at")) {
                String matchedAtStr = jsonObject.get("matched_at").getAsString();
                try {
                    LocalDateTime matchedAt = LocalDateTime.parse(matchedAtStr, DATE_FORMATTER);
                    match.setMatchedAt(matchedAt);
                } catch (DateTimeParseException e) {
                    System.out.println("Could not parse date: " + matchedAtStr);
                }
            }
            if (jsonObject.has("is_active")) {
                match.setIsActive(jsonObject.get("is_active").getAsBoolean());
            }
            
            return match;
        } catch (Exception e) {
            System.out.println("Error parsing match: " + e.getMessage());
            return null;
        }
    }
}