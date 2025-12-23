package com.example.tetoo.model;

import java.time.LocalDateTime;

public class Like {
    private Integer id;
    private Integer fromUserId;
    private Integer toUserId;
    private String type; // "like", "dislike", "superlike"
    private LocalDateTime createdAt;

    // Конструкторы
    public Like() {}

    public Like(Integer fromUserId, Integer toUserId, String type) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Integer fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Integer getToUserId() {
        return toUserId;
    }

    public void setToUserId(Integer toUserId) {
        this.toUserId = toUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Like{" +
                "fromUserId=" + fromUserId +
                ", toUserId=" + toUserId +
                ", type='" + type + '\'' +
                '}';
    }
}