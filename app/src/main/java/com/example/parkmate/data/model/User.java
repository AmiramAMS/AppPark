package com.example.parkmate.data.model;

import com.google.firebase.Timestamp;

public class User {
    private String uid;
    private String email;
    private String fullName;
    private int radiusPreference;
    private Timestamp createdAt;

    public User() {
    }

    public User(String uid, String email, String fullName, int radiusPreference, Timestamp createdAt) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.radiusPreference = radiusPreference;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getRadiusPreference() {
        return radiusPreference;
    }

    public void setRadiusPreference(int radiusPreference) {
        this.radiusPreference = radiusPreference;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

