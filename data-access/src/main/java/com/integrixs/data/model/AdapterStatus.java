package com.integrixs.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing the real-time status of a communication adapter
 */
@Entity
@Table(name = "adapter_status")
public class AdapterStatus extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adapter_id", nullable = false)
    private CommunicationAdapter adapter;
    
    @Column(name = "status", nullable = false, length = 50)
    private String status; // ACTIVE, INACTIVE, ERROR, WARNING
    
    @Column(name = "health_score")
    private Integer healthScore; // 0-100
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @Column(name = "last_error")
    private LocalDateTime lastError;
    
    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;
    
    @Column(name = "total_messages_processed")
    private Long totalMessagesProcessed;
    
    @Column(name = "messages_processed_today")
    private Long messagesProcessedToday;
    
    @Column(name = "error_count_today")
    private Long errorCountToday;
    
    @Column(name = "average_response_time")
    private Long averageResponseTime; // in milliseconds
    
    @Column(name = "is_connected")
    private Boolean isConnected;
    
    @Column(name = "connection_details", columnDefinition = "TEXT")
    private String connectionDetails;
    
    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;
    
    @Column(name = "next_health_check")
    private LocalDateTime nextHealthCheck;
    
    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata; // Additional status information as JSON
    
    /**
     * Update health score based on various factors
     */
    @PrePersist
    @PreUpdate
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