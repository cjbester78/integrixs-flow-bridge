package com.integrixs.backend.marketplace.dto;

public class TemplateStatsDto {
    private int totalDownloads;
    private int monthlyDownloads;
    private int weeklyDownloads;
    private double averageRating;
    private int totalRatings;
    private int totalComments;
    private int activeInstallations;

    // Default constructor
    public TemplateStatsDto() {
    }

    public int getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(int totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public int getMonthlyDownloads() {
        return monthlyDownloads;
    }

    public void setMonthlyDownloads(int monthlyDownloads) {
        this.monthlyDownloads = monthlyDownloads;
    }

    public int getWeeklyDownloads() {
        return weeklyDownloads;
    }

    public void setWeeklyDownloads(int weeklyDownloads) {
        this.weeklyDownloads = weeklyDownloads;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public int getActiveInstallations() {
        return activeInstallations;
    }

    public void setActiveInstallations(int activeInstallations) {
        this.activeInstallations = activeInstallations;
    }
}