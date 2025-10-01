package com.integrixs.webserver.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for retry information
 */
public class RetryInfoDTO {

    private int attemptCount;
    private boolean wasRetried;
    private String lastRetryReason;
    private LocalDateTime lastRetryTime;

    // Default constructor
    public RetryInfoDTO() {
    }

    // All args constructor
    public RetryInfoDTO(int attemptCount, boolean wasRetried, String lastRetryReason, LocalDateTime lastRetryTime) {
        this.attemptCount = attemptCount;
        this.wasRetried = wasRetried;
        this.lastRetryReason = lastRetryReason;
        this.lastRetryTime = lastRetryTime;
    }

    // Getters
    public int getAttemptCount() {
        return attemptCount;
    }
    public boolean isWasRetried() {
        return wasRetried;
    }
    public String getLastRetryReason() {
        return lastRetryReason;
    }
    public LocalDateTime getLastRetryTime() {
        return lastRetryTime;
    }

    // Setters
    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }
    public void setWasRetried(boolean wasRetried) {
        this.wasRetried = wasRetried;
    }
    public void setLastRetryReason(String lastRetryReason) {
        this.lastRetryReason = lastRetryReason;
    }
    public void setLastRetryTime(LocalDateTime lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
    }

    // Builder
    public static RetryInfoDTOBuilder builder() {
        return new RetryInfoDTOBuilder();
    }

    public static class RetryInfoDTOBuilder {
        private int attemptCount;
        private boolean wasRetried;
        private String lastRetryReason;
        private LocalDateTime lastRetryTime;

        public RetryInfoDTOBuilder attemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
            return this;
        }

        public RetryInfoDTOBuilder wasRetried(boolean wasRetried) {
            this.wasRetried = wasRetried;
            return this;
        }

        public RetryInfoDTOBuilder lastRetryReason(String lastRetryReason) {
            this.lastRetryReason = lastRetryReason;
            return this;
        }

        public RetryInfoDTOBuilder lastRetryTime(LocalDateTime lastRetryTime) {
            this.lastRetryTime = lastRetryTime;
            return this;
        }

        public RetryInfoDTO build() {
            return new RetryInfoDTO(attemptCount, wasRetried, lastRetryReason, lastRetryTime);
        }
    }}
