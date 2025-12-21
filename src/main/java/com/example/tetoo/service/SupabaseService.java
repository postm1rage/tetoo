package com.example.tetoo.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

public class SupabaseService {
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
}