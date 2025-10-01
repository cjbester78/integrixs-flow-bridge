package com.integrixs.backend.marketplace.dto;

import java.time.LocalDateTime;

public class TemplateStatsDto {
    private long downloadCount;
    private long installCount;
    private double averageRating;
    private long ratingCount;
    private long versionCount;
    private long commentCount;
    private LocalDateTime lastUpdated;
    private int totalDownloads;
    private int monthlyDownloads;
    private int weeklyDownloads;
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

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public long getInstallCount() {
        return installCount;
    }

    public void setInstallCount(long installCount) {
        this.installCount = installCount;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public long getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(long versionCount) {
        this.versionCount = versionCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Builder
    public static TemplateStatsDtoBuilder builder() {
        return new TemplateStatsDtoBuilder();
    }

    public static class TemplateStatsDtoBuilder {
        private long downloadCount;
        private long installCount;
        private double averageRating;
        private long ratingCount;
        private long versionCount;
        private long commentCount;
        private LocalDateTime lastUpdated;

        public TemplateStatsDtoBuilder downloadCount(long downloadCount) {
            this.downloadCount = downloadCount;
            return this;
        }

        public TemplateStatsDtoBuilder installCount(long installCount) {
            this.installCount = installCount;
            return this;
        }

        public TemplateStatsDtoBuilder averageRating(double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public TemplateStatsDtoBuilder ratingCount(long ratingCount) {
            this.ratingCount = ratingCount;
            return this;
        }

        public TemplateStatsDtoBuilder versionCount(long versionCount) {
            this.versionCount = versionCount;
            return this;
        }

        public TemplateStatsDtoBuilder commentCount(long commentCount) {
            this.commentCount = commentCount;
            return this;
        }

        public TemplateStatsDtoBuilder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public TemplateStatsDto build() {
            TemplateStatsDto dto = new TemplateStatsDto();
            dto.setDownloadCount(this.downloadCount);
            dto.setInstallCount(this.installCount);
            dto.setAverageRating(this.averageRating);
            dto.setRatingCount(this.ratingCount);
            dto.setVersionCount(this.versionCount);
            dto.setCommentCount(this.commentCount);
            dto.setLastUpdated(this.lastUpdated);
            return dto;
        }
    }
}