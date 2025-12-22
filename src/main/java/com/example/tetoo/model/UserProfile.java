package com.example.tetoo.model;

public class UserProfile {
    private Integer id;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String gender; // "M" или "F"
    private String birthDate; // "1990-05-15"
    private String aboutMe;
    private Integer height;
    private String relationshipGoal; // "serious", "casual" и т.д.
    private String city;
    private String country;
    private String createdAt;
    private String updatedAt;

    // Конструкторы
    public UserProfile() {}

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Вспомогательные методы
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }

    public Integer getAge() {
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
}