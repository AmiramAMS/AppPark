package com.example.parkmate.data.model;

public class UserStats {
    private final int reportsCount;
    private final int totalLikesReceived;
    private final double ratingAverageReceived;

    public UserStats(int reportsCount, int totalLikesReceived, double ratingAverageReceived) {
        this.reportsCount = reportsCount;
        this.totalLikesReceived = totalLikesReceived;
        this.ratingAverageReceived = ratingAverageReceived;
    }

    public int getReportsCount() {
        return reportsCount;
    }

    public int getTotalLikesReceived() {
        return totalLikesReceived;
    }

    public double getRatingAverageReceived() {
        return ratingAverageReceived;
    }
}

