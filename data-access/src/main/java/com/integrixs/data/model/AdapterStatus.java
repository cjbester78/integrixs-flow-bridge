package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing the real-time status of a communication adapter
 */
public class AdapterStatus extends BaseEntity {

    private UUID id;

    private CommunicationAdapter adapter;

    private String status; // ACTIVE, INACTIVE, ERROR, WARNING

    private Integer healthScore; // 0-100

    private LocalDateTime lastActivity;

    private LocalDateTime lastError;

    private String lastErrorMessage;

    private Long totalMessagesProcessed;

    private Long messagesProcessedToday;

    private Long errorCountToday;

    private Long averageResponseTime; // in milliseconds

    private Boolean isConnected;

    private String connectionDetails;

    private LocalDateTime lastHealthCheck;

    private LocalDateTime nextHealthCheck;

    private String metadata; // Additional status information as JSON

    /**
     * Update health score based on various factors
     */
    public void updateHealthScore() {
        if (this.status == null) {
            this.healthScore = 0;
            return;
        }

        switch (this.status) {
            case "ACTIVE":
                this.healthScore = 100;
                break;
            case "WARNING":
                this.healthScore = 70;
                break;
            case "ERROR":
                this.healthScore = 30;
                break;
            case "INACTIVE":
            default:
                this.healthScore = 0;
                break;
        }

        // Adjust based on error rate
        if (errorCountToday != null && messagesProcessedToday != null && messagesProcessedToday > 0) {
            double errorRate = (double) errorCountToday / messagesProcessedToday;
            if (errorRate > 0.1) {
                this.healthScore = Math.max(0, this.healthScore - 20);
            }
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CommunicationAdapter getAdapter() {
        return adapter;
    }

    public String getStatus() {
        return status;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public LocalDateTime getLastError() {
        return lastError;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Long getTotalMessagesProcessed() {
        return totalMessagesProcessed;
    }

    public Long getMessagesProcessedToday() {
        return messagesProcessedToday;
    }

    public Long getErrorCountToday() {
        return errorCountToday;
    }

    public Long getAverageResponseTime() {
        return averageResponseTime;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public String getConnectionDetails() {
        return connectionDetails;
    }

    public LocalDateTime getLastHealthCheck() {
        return lastHealthCheck;
    }

    public LocalDateTime getNextHealthCheck() {
        return nextHealthCheck;
    }

    public String getMetadata() {
        return metadata;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setAdapter(CommunicationAdapter adapter) {
        this.adapter = adapter;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public void setLastError(LocalDateTime lastError) {
        this.lastError = lastError;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public void setTotalMessagesProcessed(Long totalMessagesProcessed) {
        this.totalMessagesProcessed = totalMessagesProcessed;
    }

    public void setMessagesProcessedToday(Long messagesProcessedToday) {
        this.messagesProcessedToday = messagesProcessedToday;
    }

    public void setErrorCountToday(Long errorCountToday) {
        this.errorCountToday = errorCountToday;
    }

    public void setAverageResponseTime(Long averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void setConnectionDetails(String connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    public void setLastHealthCheck(LocalDateTime lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    public void setNextHealthCheck(LocalDateTime nextHealthCheck) {
        this.nextHealthCheck = nextHealthCheck;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}