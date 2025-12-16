package com.example.tetoo.auth;

import com.example.tetoo.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private boolean isLoggedIn = false;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void login(User user) {
        this.currentUser = user;
        this.isLoggedIn = true;
        System.out.println("User logged in: " + user.getEmail());
    }
    
    public void logout() {
        this.currentUser = null;
        this.isLoggedIn = false;
        System.out.println("User logged out");
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }
}