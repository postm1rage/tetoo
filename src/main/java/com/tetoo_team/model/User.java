package com.tetoo_team.model;

public class User {
    private Integer id;
    private String email;
    private String password_hash;
    private String phone;
    private Boolean is_verified;
    private String created_at;
    
    // Конструкторы
    public User() {
    }
    
    public User(Integer id, String email, String password_hash, String phone, Boolean is_verified, String created_at) {
        this.id = id;
        this.email = email;
        this.password_hash = password_hash;
        this.phone = phone;
        this.is_verified = is_verified;
        this.created_at = created_at;
    }
    
    // Геттеры и сеттеры
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword_hash() {
        return password_hash;
    }
    
    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Boolean getIs_verified() {
        return is_verified;
    }
    
    public void setIs_verified(Boolean is_verified) {
        this.is_verified = is_verified;
    }
    
    public String getCreated_at() {
        return created_at;
    }
    
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", is_verified=" + is_verified +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}