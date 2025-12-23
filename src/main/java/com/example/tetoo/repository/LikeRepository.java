package com.example.tetoo.repository;

import com.example.tetoo.config.SupabaseConfig;
import com.example.tetoo.model.Like;
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

public class LikeRepository {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ISO_DATE_TIME;
    private static final Gson gson = new Gson();
    
    public boolean sendLike(Like like) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + "likes";
            System.out.println("=== SENDING LIKE ===");
            System.out.println("URL: " + url);
            
            HttpPost request = new HttpPost(url);
            
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Prefer", "return=representation");
            
            // Создаем JSON объект для отправки
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("from_user_id", like.getFromUserId());
            jsonBody.addProperty("to_user_id", like.getToUserId());
            jsonBody.addProperty("type", like.getType());
            
            String jsonString = jsonBody.toString();
            System.out.println("JSON Request: " + jsonString);
            
            StringEntity entity = new StringEntity(jsonString);
            request.setEntity(entity);
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            System.out.println("Response status code: " + statusCode);
            
            // Читаем тело ответа
            HttpEntity responseEntity = response.getEntity();
            String responseBody = "";
            if (responseEntity != null) {
                responseBody = EntityUtils.toString(responseEntity);
                System.out.println("Response body: " + responseBody);
            }
            
            if (statusCode == 201) {
                System.out.println("Like sent successfully: " + like);
                return true;
            } else if (statusCode == 409) {
                System.out.println("Like already exists: " + like);
                return true;
            } else {
                System.out.println("Error sending like. Code: " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Exception sending like: " + e.getMessage());
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
    
    // Проверяем, существует ли лайк от одного пользователя другому
    public boolean hasLikeFromTo(Integer fromUserId, Integer toUserId) {
        List<Like> likes = getLikesFromTo(fromUserId, toUserId);
        return !likes.isEmpty();
    }
    
    // Получить все лайки от одного пользователя другому
    private List<Like> getLikesFromTo(Integer fromUserId, Integer toUserId) {
        List<Like> likes = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + "likes" + 
                        "?from_user_id=eq." + fromUserId + 
                        "&to_user_id=eq." + toUserId;
            
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
                    Like like = parseLike(element.getAsJsonObject());
                    likes.add(like);
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error getting likes from-to: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return likes;
    }
    
    // Получить все лайки, полученные пользователем
    public List<Like> getLikesReceived(Integer userId) {
        List<Like> likes = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + "likes" + 
                        "?to_user_id=eq." + userId + 
                        "&order=created_at.desc";
            
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
                    Like like = parseLike(element.getAsJsonObject());
                    likes.add(like);
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error getting likes received: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return likes;
    }
    
    // Удалить лайк (дизлайк из Matches)
    public boolean removeLike(Integer fromUserId, Integer toUserId) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + "likes" + 
                        "?from_user_id=eq." + fromUserId + 
                        "&to_user_id=eq." + toUserId;
            
            HttpDelete request = new HttpDelete(url);
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200 || statusCode == 204) {
                System.out.println("Like removed from " + fromUserId + " to " + toUserId);
                return true;
            } else {
                System.out.println("Error removing like. Code: " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error removing like: " + e.getMessage());
            return false;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Парсинг лайка из JSON
    private Like parseLike(JsonObject jsonObject) {
        try {
            Like like = new Like();
            
            if (jsonObject.has("id")) {
                like.setId(jsonObject.get("id").getAsInt());
            }
            if (jsonObject.has("from_user_id")) {
                like.setFromUserId(jsonObject.get("from_user_id").getAsInt());
            }
            if (jsonObject.has("to_user_id")) {
                like.setToUserId(jsonObject.get("to_user_id").getAsInt());
            }
            if (jsonObject.has("type")) {
                like.setType(jsonObject.get("type").getAsString());
            }
            if (jsonObject.has("created_at")) {
                String createdAtStr = jsonObject.get("created_at").getAsString();
                try {
                    LocalDateTime createdAt = LocalDateTime.parse(createdAtStr, DATE_FORMATTER);
                    like.setCreatedAt(createdAt);
                } catch (DateTimeParseException e) {
                    System.out.println("Could not parse date: " + createdAtStr);
                }
            }
            
            return like;
        } catch (Exception e) {
            System.out.println("Error parsing like: " + e.getMessage());
            return null;
        }
    }
}