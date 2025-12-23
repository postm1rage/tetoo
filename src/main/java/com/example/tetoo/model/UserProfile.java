package com.example.tetoo.model;

public class UserProfile {
    private Integer id;                 // ID профиля из базы
    private Integer userId;             // ID пользователя
    private String email;              // Из версии HEAD
    private String firstName;
    private String lastName;
    private String gender;             // "M", "F", "O" (в версии HEAD), "M" или "F" (в section-design)
    private Integer age;               // Из версии HEAD (вычисляется)
    private String birthDate;          // Из версии section-design (хранится как String)
    private String aboutMe;
    private Integer height;            // Из версии section-design
    private String relationshipGoal;
    private String city;
    private String country;
    private String avatarUrl;          // Из версии HEAD
    private String createdAt;
    private String updatedAt;
    
    // Конструкторы
    public UserProfile() {
    }
    
    // Конструктор из User (базовый) - из версии HEAD
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
    
    // Полный конструктор - объединенный
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
    
    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { 
        return firstName != null ? firstName : "Unknown"; 
    }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { 
        return lastName != null ? lastName : ""; 
    }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Integer getAge() { 
        // Приоритет: явно установленный возраст из HEAD версии
        if (age != null) {
            return age;
        }
        // Если возраст не установлен, вычисляем из birthDate из section-design
        return calculateAgeFromBirthDate();
    }
    public void setAge(Integer age) { this.age = age; }
    
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { 
        this.birthDate = birthDate;
        // При установке birthDate автоматически обновляем age
        if (birthDate != null) {
            this.age = calculateAgeFromBirthDate();
        }
    }
    
    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    
    public String getRelationshipGoal() { return relationshipGoal; }
    public void setRelationshipGoal(String relationshipGoal) { this.relationshipGoal = relationshipGoal; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // Вспомогательные методы
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }
    
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
    
    // Вычисление возраста из birthDate
    private Integer calculateAgeFromBirthDate() {
        if (birthDate != null && birthDate.length() >= 4) {
            try {
                int birthYear = Integer.parseInt(birthDate.substring(0, 4));
                int currentYear = java.time.Year.now().getValue();
                return currentYear - birthYear;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return getFullName() + (email != null ? " (" + email + ")" : "");
    }
}