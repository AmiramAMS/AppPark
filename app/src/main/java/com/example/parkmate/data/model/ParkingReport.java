package com.example.parkmate.data.model;

import com.google.firebase.Timestamp;

public class ParkingReport {
    private String id;
    private String ownerId;
    private double latitude;
    private double longitude;
    private String address;
    private String placeType; // parking_lot | entertainment
    private String status; // available | occupied | not_relevant
    private Timestamp createdAt;
    private int likesCount;
    private int ratingsCount;
    private double ratingsSum;
    private double ratingAverage;

    public ParkingReport() {
    }

    public ParkingReport(String id, String ownerId, double latitude, double longitude, String address, String placeType, String status, Timestamp createdAt, int likesCount, int ratingsCount, double ratingsSum, double ratingAverage) {
        this.id = id;
        this.ownerId = ownerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.placeType = placeType;
        this.status = status;
        this.createdAt = createdAt;
        this.likesCount = likesCount;
        this.ratingsCount = ratingsCount;
        this.ratingsSum = ratingsSum;
        this.ratingAverage = ratingAverage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public double getRatingsSum() {
        return ratingsSum;
    }

    public void setRatingsSum(double ratingsSum) {
        this.ratingsSum = ratingsSum;
    }

    public double getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }
}

