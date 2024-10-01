package com.example.nutri;

public class Aroma {
    private String name;
    private long likes;
    private long dislikes;
    private String imageUrl;
    private String id;

    public Aroma() {
    }

    public Aroma(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.likes = 0;
        this.dislikes = 0;
    }

    public Aroma(String name, long likes, long dislikes, String imageUrl) {
        this.name = name;
        this.likes = likes;
        this.dislikes = dislikes;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}