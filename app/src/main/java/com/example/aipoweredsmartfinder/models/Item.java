package com.example.aipoweredsmartfinder.models;

import java.io.Serializable;

public class Item implements Serializable {
    private int id;
    private String title;
    private String description;
    private String location;
    private String date;
    private String category;
    private String photoUrl;
    private String type; // "lost" or "found"
    private int userId;
    private String status; // "pending", "resolved", etc.

    // Constructors
    public Item() {
        // Empty constructor needed for serialization
    }

    public Item(String title, String description, String location, String date, 
                String category, String photoUrl, String type, int userId) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.category = category;
        this.photoUrl = photoUrl;
        this.type = type;
        this.userId = userId;
        this.status = "pending";
    }
    
    // Constructor needed for creating items from the database responses
    public Item(int id, String title, String description, String location, String date, 
                String category, String type, int userId, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.category = category;
        this.photoUrl = imageUrl;
        this.type = type;
        this.userId = userId;
        this.status = "pending";
    }

    // Full constructor with ID
    public Item(int id, String title, String description, String location, String date, 
                String category, String photoUrl, String type, int userId, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.category = category;
        this.photoUrl = photoUrl;
        this.type = type;
        this.userId = userId;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 