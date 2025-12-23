package com.example.tetoo.model;

import java.time.LocalDateTime;

public class Match {
    private Integer id;
    private Integer user1Id;
    private Integer user2Id;
    private LocalDateTime matchedAt;
    private Boolean isActive;
    
    // Конструкторы
    public Match() {}
    
    public Match(Integer user1Id, Integer user2Id) {
        this.user1Id = Math.min(user1Id, user2Id);
        this.user2Id = Math.max(user1Id, user2Id);
        this.matchedAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Геттеры и сеттеры
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUser1Id() {
        return user1Id;
    }
    
    public void setUser1Id(Integer user1Id) {
        this.user1Id = user1Id;
    }
    
    public Integer getUser2Id() {
        return user2Id;
    }
    
    public void setUser2Id(Integer user2Id) {
        this.user2Id = user2Id;
    }
    
    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }
    
    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Проверяем, содержит ли матч пользователя
    public boolean containsUser(Integer userId) {
        return user1Id.equals(userId) || user2Id.equals(userId);
    }
    
    // Получаем ID другого пользователя в матче
    public Integer getOtherUserId(Integer currentUserId) {
        if (user1Id.equals(currentUserId)) {
            return user2Id;
        } else if (user2Id.equals(currentUserId)) {
            return user1Id;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Match{" +
                "user1Id=" + user1Id +
                ", user2Id=" + user2Id +
                ", matchedAt=" + matchedAt +
                ", isActive=" + isActive +
                '}';
    }
}