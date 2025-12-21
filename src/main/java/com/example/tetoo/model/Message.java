package com.example.tetoo.model;

import java.time.LocalDateTime;

public class Message {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String messageText;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    // Конструкторы
    public Message() {
    }
    
    public Message(Integer id, Integer senderId, Integer receiverId, 
                   String messageText, Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
    
    // Геттеры
    public Integer getId() {
        return id;
    }
    
    public Integer getSenderId() {
        return senderId;
    }
    
    public Integer getReceiverId() {
        return receiverId;
    }
    
    public String getMessageText() {
        return messageText;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Сеттеры
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }
    
    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }
    
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Вспомогательные методы
    public boolean isFromUser(Integer userId) {
        return senderId != null && senderId.equals(userId);
    }
    
    public boolean isToUser(Integer userId) {
        return receiverId != null && receiverId.equals(userId);
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", messageText='" + messageText + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}