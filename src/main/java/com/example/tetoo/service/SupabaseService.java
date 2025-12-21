package com.example.tetoo.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.example.tetoo.model.Chat;
import com.example.tetoo.model.Message;
import com.example.tetoo.model.UserProfile;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.google.gson.JsonObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.http.client.methods.HttpPatch;


public class SupabaseService {
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ISO_DATE_TIME;
    private static final Gson gson = new Gson();
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Method for testing connection and getting all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Create GET request to users table
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE;
            HttpGet request = new HttpGet(url);
            
            // Set headers
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            // Execute request
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                
                // Parse JSON response
                JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
                
                for (JsonElement element : jsonArray) {
                    User user = gson.fromJson(element, User.class);
                    users.add(user);
                }
                
                System.out.println("Successfully retrieved " + users.size() + " users");
            } else {
                System.out.println("Error in request. Code: " + statusCode);
            }
            
        } catch (Exception e) {
            System.out.println("Error connecting to Supabase: " + e.getMessage());
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
    
    // Method for connection test (minimal request)
    public boolean testConnection() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Simple request to users table with limit=1
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE + "?limit=1";
            HttpGet request = new HttpGet(url);
            
            // Set headers
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            // Execute request
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            return statusCode == 200;
            
        } catch (Exception e) {
            System.out.println("Error testing connection: " + e.getMessage());
            return false;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Method to check if user exists by email (old method, keep for compatibility)
    public User getUserByEmail(String email) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Create GET request with email filter
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE + 
                        "?email=eq." + email;
            HttpGet request = new HttpGet(url);
            
            // Set headers
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            // Execute request
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                
                // Parse JSON response
                JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();
                
                if (jsonArray.size() > 0) {
                    // User found
                    JsonElement element = jsonArray.get(0);
                    User user = gson.fromJson(element, User.class);
                    System.out.println("User found: " + user.getEmail());
                    return user;
                } else {
                    System.out.println("No user found with email: " + email);
                    return null;
                }
            } else {
                System.out.println("Error checking user. Code: " + statusCode);
                return null;
            }
            
        } catch (Exception e) {
            System.out.println("Error checking user by email: " + e.getMessage());
            return null;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Method for user registration with bcrypt hashing
    public boolean registerUser(String email, String password) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Hash password with bcrypt
            String hashedPassword = passwordEncoder.encode(password);
            System.out.println("Password hashed with bcrypt: " + hashedPassword);
            
            // Create POST request to users table
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE;
            HttpPost request = new HttpPost(url);
            
            // Set headers
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Prefer", "return=representation");
            
            // Create JSON request body with hashed password
            String jsonBody = String.format(
                "{\"email\": \"%s\", \"password_hash\": \"%s\", \"is_verified\": false}",
                email, hashedPassword
            );
            
            StringEntity entity = new StringEntity(jsonBody);
            request.setEntity(entity);
            
            // Execute request
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 201) {
                System.out.println("User registration successful for email: " + email);
                return true;
            } else {
                // Try to read error message
                try {
                    HttpEntity errorEntity = response.getEntity();
                    String errorBody = EntityUtils.toString(errorEntity);
                    System.out.println("Registration error. Code: " + statusCode + ", Body: " + errorBody);
                } catch (Exception e) {
                    System.out.println("Registration error. Code: " + statusCode);
                }
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error during registration: " + e.getMessage());
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
    
    // Method to login with password verification using bcrypt
    public User loginWithPassword(String email, String password) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Get user with password_hash from database
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE + 
                        "?email=eq." + email + "&select=*";
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
                
                if (jsonArray.size() > 0) {
                    JsonObject userObject = jsonArray.get(0).getAsJsonObject();
                    
                    // Get stored password hash
                    String storedHash = userObject.get("password_hash").getAsString();
                    
                    // Check password
                    boolean passwordValid = false;
                    
                    if (storedHash != null && !storedHash.isEmpty()) {
                        // Check if hash is bcrypt format (starts with $2)
                        if (storedHash.startsWith("$2")) {
                            // Verify password with bcrypt
                            try {
                                passwordValid = passwordEncoder.matches(password, storedHash);
                                System.out.println("BCrypt verification: " + passwordValid);
                            } catch (Exception e) {
                                System.out.println("BCrypt error: " + e.getMessage());
                                passwordValid = false;
                            }
                        } else {
                            // Legacy or plain text (for test users)
                            System.out.println("Warning: Plain text comparison for hash: " + storedHash);
                            passwordValid = storedHash.equals(password);
                        }
                    }
                    
                    if (passwordValid) {
                        // Password is correct, create User object
                        User user = gson.fromJson(userObject, User.class);
                        System.out.println("Login successful for: " + email);
                        return user;
                    } else {
                        System.out.println("Invalid password for: " + email);
                        return null;
                    }
                } else {
                    System.out.println("User not found: " + email);
                    return null;
                }
            } else {
                System.out.println("Error fetching user. Code: " + statusCode);
                return null;
            }
            
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Получить все чаты пользователя (диалоги, где есть сообщения)
    public List<Chat> getUserChats(Integer userId) {
        List<Chat> chats = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Получаем всех пользователей, с которыми есть сообщения
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MESSAGES_TABLE + 
                        "?or=(sender_id.eq." + userId + ",receiver_id.eq." + userId + ")" +
                        "&select=sender_id,receiver_id" +
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
                
                // Собираем уникальных собеседников
                Set<Integer> chatUserIds = new HashSet<>();
                for (JsonElement element : jsonArray) {
                    JsonObject msgObj = element.getAsJsonObject();
                    Integer senderId = msgObj.get("sender_id").getAsInt();
                    Integer receiverId = msgObj.get("receiver_id").getAsInt();
                    
                    // Определяем ID собеседника
                    Integer otherUserId = senderId.equals(userId) ? receiverId : senderId;
                    chatUserIds.add(otherUserId);
                }
                
                // Для каждого собеседника создаём Chat объект
                for (Integer otherUserId : chatUserIds) {
                    // Получаем профиль собеседника
                    UserProfile otherUser = getUserProfile(otherUserId);
                    
                    if (otherUser != null) {
                        Chat chat = new Chat(userId, otherUserId, otherUser);
                        
                        // Получаем последнее сообщение
                        Message lastMessage = getLastMessage(userId, otherUserId);
                        chat.setLastMessage(lastMessage);
                        
                        // Получаем количество непрочитанных
                        Integer unreadCount = getUnreadCount(userId, otherUserId);
                        chat.setUnreadCount(unreadCount);
                        
                        chats.add(chat);
                    }
                }
                
                // Сортируем по времени последнего сообщения
                chats.sort(Comparator.comparing(
                    chat -> chat.getLastMessage() != null ? 
                        chat.getLastMessage().getCreatedAt() : 
                        LocalDateTime.MIN,
                    Comparator.reverseOrder()
                ));
                
                System.out.println("Found " + chats.size() + " chats for user " + userId);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting user chats: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return chats;
    }
    // Получить профиль пользователя по ID
    private UserProfile getUserProfile(Integer userId) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Сначала получаем базовую информацию из users
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USERS_TABLE + 
                        "?id=eq." + userId + "&select=id,email,created_at";
            
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
                
                if (jsonArray.size() > 0) {
                    JsonObject userObj = jsonArray.get(0).getAsJsonObject();
                    
                    // Создаём User объект
                    User user = gson.fromJson(userObj, User.class);
                    
                    // Создаём UserProfile
                    UserProfile profile = new UserProfile(user);
                    
                    // Пытаемся получить доп. информацию из user_profiles
                    try {
                        UserProfile detailedProfile = getUserProfileDetails(userId);
                        if (detailedProfile != null) {
                            // Объединяем информацию
                            profile.setFirstName(detailedProfile.getFirstName());
                            profile.setLastName(detailedProfile.getLastName());
                            profile.setGender(detailedProfile.getGender());
                            profile.setAge(detailedProfile.getAge());
                            profile.setCity(detailedProfile.getCity());
                            profile.setCountry(detailedProfile.getCountry());
                            profile.setAboutMe(detailedProfile.getAboutMe());
                            profile.setRelationshipGoal(detailedProfile.getRelationshipGoal());
                        }
                    } catch (Exception e) {
                        System.out.println("Could not get detailed profile for user " + userId);
                    }
                    
                    return profile;
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error getting user profile: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    // Получить детальную информацию профиля из user_profiles
    private UserProfile getUserProfileDetails(Integer userId) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.USER_PROFILES_TABLE + 
                        "?user_id=eq." + userId + "&select=*";
            
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
                
                if (jsonArray.size() > 0) {
                    JsonObject profileObj = jsonArray.get(0).getAsJsonObject();
                    
                    // Парсим базовые поля
                    String firstName = getJsonString(profileObj, "first_name");
                    String lastName = getJsonString(profileObj, "last_name");
                    String gender = getJsonString(profileObj, "gender");
                    String city = getJsonString(profileObj, "city");
                    String country = getJsonString(profileObj, "country");
                    String aboutMe = getJsonString(profileObj, "about_me");
                    String relationshipGoal = getJsonString(profileObj, "relationship_goal");
                    
                    // Вычисляем возраст из даты рождения
                    Integer age = null;
                    if (profileObj.has("birth_date")) {
                        String birthDateStr = profileObj.get("birth_date").getAsString();
                        try {
                            LocalDateTime birthDate = LocalDateTime.parse(birthDateStr, DATE_FORMATTER);
                            age = LocalDateTime.now().getYear() - birthDate.getYear();
                        } catch (DateTimeParseException e) {
                            System.out.println("Could not parse birth date: " + birthDateStr);
                        }
                    }
                    
                    // Получаем аватар
                    String avatarUrl = getUserAvatar(userId);
                    
                    return new UserProfile(userId, null, firstName, lastName, gender, 
                                        age, city, country, aboutMe, relationshipGoal, avatarUrl);
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error getting user profile details: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    // Получить URL аватара пользователя
    private String getUserAvatar(Integer userId) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.PHOTOS_TABLE + 
                        "?user_id=eq." + userId + "&is_avatar=eq.true&select=file_path&limit=1";
            
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
                
                if (jsonArray.size() > 0) {
                    JsonObject photoObj = jsonArray.get(0).getAsJsonObject();
                    return getJsonString(photoObj, "file_path");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error getting user avatar: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    // Получить последнее сообщение между двумя пользователями
    private Message getLastMessage(Integer user1Id, Integer user2Id) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MESSAGES_TABLE + 
                        "?or=(and(sender_id.eq." + user1Id + ",receiver_id.eq." + user2Id + ")," +
                        "and(sender_id.eq." + user2Id + ",receiver_id.eq." + user1Id + "))" +
                        "&order=created_at.desc&limit=1";
            
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
                
                if (jsonArray.size() > 0) {
                    return parseMessage(jsonArray.get(0).getAsJsonObject());
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error getting last message: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    // Получить количество непрочитанных сообщений от пользователя
    private Integer getUnreadCount(Integer userId, Integer otherUserId) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MESSAGES_TABLE + 
                        "?sender_id=eq." + otherUserId + 
                        "&receiver_id=eq." + userId + 
                        "&is_read=eq.false" +
                        "&select=id";
            
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
                return jsonArray.size();
            }
            
        } catch (Exception e) {
            System.out.println("Error getting unread count: " + e.getMessage());
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return 0;
    }
    // Получить историю сообщений между двумя пользователями
    public List<Message> getChatHistory(Integer user1Id, Integer user2Id) {
        List<Message> messages = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MESSAGES_TABLE + 
                        "?or=(and(sender_id.eq." + user1Id + ",receiver_id.eq." + user2Id + ")," +
                        "and(sender_id.eq." + user2Id + ",receiver_id.eq." + user1Id + "))" +
                        "&order=created_at.asc";
            
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
                    Message message = parseMessage(element.getAsJsonObject());
                    if (message != null) {
                        messages.add(message);
                    }
                }
                
                System.out.println("Loaded " + messages.size() + " messages for chat " + 
                                user1Id + " <-> " + user2Id);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting chat history: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return messages;
    }
    // Отправить сообщение
    public boolean sendMessage(Integer senderId, Integer receiverId, String messageText) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MESSAGES_TABLE;
            HttpPost request = new HttpPost(url);
            
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Prefer", "return=representation");
            
            // Создаём JSON тело
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("sender_id", senderId);
            jsonBody.addProperty("receiver_id", receiverId);
            jsonBody.addProperty("message_text", messageText);
            jsonBody.addProperty("is_read", false);
            
            StringEntity entity = new StringEntity(jsonBody.toString());
            request.setEntity(entity);
            
            // Выполняем запрос
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 201) {
                System.out.println("Message sent from " + senderId + " to " + receiverId);
                return true;
            } else {
                System.out.println("Error sending message. Code: " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error sending message: " + e.getMessage());
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
    // Пометить все сообщения от пользователя как прочитанные
    public boolean markMessagesAsRead(Integer userId, Integer otherUserId) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        try {
            // Supabase PATCH запрос для обновления
            String url = SupabaseConfig.FULL_API_URL + SupabaseConfig.MESSAGES_TABLE + 
                        "?sender_id=eq." + otherUserId + "&receiver_id=eq." + userId;
            
            HttpPatch request = new HttpPatch(url);
            request.setHeader("apikey", SupabaseConfig.SUPABASE_API_KEY);
            request.setHeader("Authorization", SupabaseConfig.getAuthHeader());
            request.setHeader("Content-Type", "application/json");
            
            // Создаём JSON для обновления
            JsonObject updateJson = new JsonObject();
            updateJson.addProperty("is_read", true);
            
            StringEntity entity = new StringEntity(updateJson.toString());
            request.setEntity(entity);
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200 || statusCode == 204) {
                System.out.println("Marked messages as read from " + otherUserId + " to " + userId);
                return true;
            } else {
                System.out.println("Error marking messages as read. Code: " + statusCode);
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("Error marking messages as read: " + e.getMessage());
            return false;
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Парсинг сообщения из JSON
    private Message parseMessage(JsonObject jsonObject) {
        try {
            Message message = new Message();
            
            if (jsonObject.has("id")) {
                message.setId(jsonObject.get("id").getAsInt());
            }
            if (jsonObject.has("sender_id")) {
                message.setSenderId(jsonObject.get("sender_id").getAsInt());
            }
            if (jsonObject.has("receiver_id")) {
                message.setReceiverId(jsonObject.get("receiver_id").getAsInt());
            }
            if (jsonObject.has("message_text")) {
                message.setMessageText(jsonObject.get("message_text").getAsString());
            }
            if (jsonObject.has("is_read")) {
                message.setIsRead(jsonObject.get("is_read").getAsBoolean());
            }
            if (jsonObject.has("created_at")) {
                String createdAtStr = jsonObject.get("created_at").getAsString();
                try {
                    LocalDateTime createdAt = LocalDateTime.parse(createdAtStr, DATE_FORMATTER);
                    message.setCreatedAt(createdAt);
                } catch (DateTimeParseException e) {
                    System.out.println("Could not parse date: " + createdAtStr);
                }
            }
            
            return message;
        } catch (Exception e) {
            System.out.println("Error parsing message: " + e.getMessage());
            return null;
        }
    }

    // Безопасное получение строки из JSON
    private String getJsonString(JsonObject jsonObject, String key) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsString();
        }
        return null;
    }


    public void createTestChat(Integer user1Id, Integer user2Id) {
        System.out.println("Creating test chat between users " + user1Id + " and " + user2Id);
        
        sendMessage(user1Id, user2Id, "Привет от пользователя " + user1Id + "!");
        sendMessage(user2Id, user1Id, "Привет от пользователя " + user2Id + "!");
        sendMessage(user1Id, user2Id, "Как дела?");
        sendMessage(user2Id, user1Id, "Отлично! А у тебя?");
        
        System.out.println("Test chat created");
    }
}