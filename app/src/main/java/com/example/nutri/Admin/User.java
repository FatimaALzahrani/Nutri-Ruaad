package com.example.nutri.Admin;

public class User {
    private String userId;
    private String name;
    private String email;
    private String age;
    private String height;
    private String job;
    private String location;
    private String imageUrl;

    public User(String userId, String username, String email, String age, String height, String job, String location, String imageUrl) {
        this.userId = userId;
        this.name = username;
        this.email = email;
        this.age = age;
        this.height = height;
        this.job = job;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    // Getters and setters for all fields
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String username) {
        this.name = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
