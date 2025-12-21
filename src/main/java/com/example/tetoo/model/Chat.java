package com.example.tetoo.model;

import java.time.LocalDateTime;

public class Chat {
    private Integer id;
    private Integer user1Id;
    private Integer user2Id;
    private UserProfile otherUser;        // Профиль собеседника
    private Message lastMessage;          // Последнее сообщение
    private Integer unreadCount;          // Количество непрочитанных
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Конструкторы
    public Chat() {
        this.unreadCount = 0;
    }
    
    public Chat(Integer user1Id, Integer user2Id, UserProfile otherUser) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.otherUser = otherUser;
        this.unreadCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Геттеры
    public Integer getId() {
        return id;
    }
    
    public Integer getUser1Id() {
        return user1Id;
    }
    
    public Integer getUser2Id() {
        return user2Id;
    }
    
    public UserProfile getOtherUser() {
        return otherUser;
    }
    
    public Message getLastMessage() {
        return lastMessage;
    }
    
    public Integer getUnreadCount() {
        return unreadCount != null ? unreadCount : 0;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Сеттеры
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setUser1Id(Integer user1Id) {
        this.user1Id = user1Id;
    }
    
    public void setUser2Id(Integer user2Id) {
        this.user2Id = user2Id;
    }
    
    public void setOtherUser(UserProfile otherUser) {
        this.otherUser = otherUser;
    }
    
    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
        if (lastMessage != null) {
            this.updatedAt = lastMessage.getCreatedAt();
        }
    }
    
    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Вспомогательные методы
    public Integer getOtherUserId(Integer currentUserId) {
        if (user1Id != null && user1Id.equals(currentUserId)) {
            return user2Id;
        } else if (user2Id != null && user2Id.equals(currentUserId)) {
            return user1Id;
        }
        return null;
    }
    
    public void incrementUnreadCount() {
        if (unreadCount == null) {
            unreadCount = 1;
        } else {
            unreadCount++;
        }
    }
    
    public void resetUnreadCount() {
        this.unreadCount = 0;
    }
    
    @Override
    public String toString() {
        String otherUserName = otherUser != null ? otherUser.getFirstName() : "Unknown";
        String lastMsg = lastMessage != null ? lastMessage.getMessageText() : "No messages";
        return "Chat with " + otherUserName + 
               " (" + getUnreadCount() + " unread): " + 
               lastMsg;
    }
}