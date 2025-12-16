package com.tetoo_team.config;

public class SupabaseConfig {
    // URL Supabase проекта
    public static final String SUPABASE_URL = "https://eruzshyrvfwjcehpwjmp.supabase.co";
    
    // Anon API ключ
    public static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVydXpzaHlydmZ3amNlaHB3am1wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU2NzQ5MTMsImV4cCI6MjA4MTI1MDkxM30.mA5cbodteVMWC92Ui_JpERHtboxE2hB5P30qtpvYSYk";
    
    // Заголовки для HTTP запросов
    public static final String API_KEY_HEADER = "apikey";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // Версия API Supabase
    public static final String API_VERSION = "/rest/v1/";
    
    // Полный URL для доступа к API
    public static final String FULL_API_URL = SUPABASE_URL + API_VERSION;
    
    // Таблицы
    public static final String USERS_TABLE = "users";
    public static final String USER_PROFILES_TABLE = "user_profiles";
    public static final String PHOTOS_TABLE = "photos";
    public static final String INTERESTS_TABLE = "interests";
    public static final String USER_INTERESTS_TABLE = "user_interests";
    public static final String LIKES_TABLE = "likes";
    public static final String MATCHES_TABLE = "matches";
    public static final String MESSAGES_TABLE = "messages";
    
    // Метод для получения заголовков авторизации
    public static String getAuthHeader() {
        return BEARER_PREFIX + SUPABASE_API_KEY;
    }
}