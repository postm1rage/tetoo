package com.example.tetoo.model;

public class UserProfile {
    private Integer userId;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer age;
    private String city;
    private String country;
    private String aboutMe;
    private String relationshipGoal;
    private String avatarUrl;
    
    // Конструкторы
    public UserProfile() {
    }
    
    // Конструктор из User (базовый)
    public UserProfile(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        // Имя по умолчанию из email (до @)
        if (email != null && email.contains("@")) {
            this.firstName = email.substring(0, email.indexOf('@'));
        } else {
            this.firstName = "User";
        }
        this.lastName = "";
    }
    
    // Полный конструктор
    public UserProfile(Integer userId, String email, String firstName, String lastName, 
                      String gender, Integer age, String city, String country, 
                      String aboutMe, String relationshipGoal, String avatarUrl) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.city = city;
        this.country = country;
        this.aboutMe = aboutMe;
        this.relationshipGoal = relationshipGoal;
        this.avatarUrl = avatarUrl;
    }
    
    // Геттеры
    public Integer getUserId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFirstName() {
        return firstName != null ? firstName : "Unknown";
    }
    
    public String getLastName() {
        return lastName != null ? lastName : "";
    }
    
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }
    
    public String getGender() {
        return gender;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getAboutMe() {
        return aboutMe;
    }
    
    public String getRelationshipGoal() {
        return relationshipGoal;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    // Сеттеры
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }
    
    public void setRelationshipGoal(String relationshipGoal) {
        this.relationshipGoal = relationshipGoal;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    // Вспомогательные методы
    public String getLocation() {
        if (city != null && country != null) {
            return city + ", " + country;
        } else if (city != null) {
            return city;
        } else if (country != null) {
            return country;
        }
        return "Location unknown";
    }
    
    @Override
    public String toString() {
        return getFullName() + " (" + email + ")";
    }
}