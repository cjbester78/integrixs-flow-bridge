package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * DTO representing a critical issue in adapter health
 */
public class CriticalIssue {
    private String issueId;
    private String issueType;
    private String severity;
    private String description;
    private String affectedComponent;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String status;
    private String recommendedAction;
    private Object details;
    private String adapterId;
    private String adapterName;

    public CriticalIssue() {
    }

    public CriticalIssue(String issueId, String issueType, String severity, String description,
                        String affectedComponent, LocalDateTime detectedAt, LocalDateTime resolvedAt,
                        String status, String recommendedAction, Object details) {
        this.issueId = issueId;
        this.issueType = issueType;
        this.severity = severity;
        this.description = description;
        this.affectedComponent = affectedComponent;
        this.detectedAt = detectedAt;
        this.resolvedAt = resolvedAt;
        this.status = status;
        this.recommendedAction = recommendedAction;
        this.details = details;
    }

    // Getters and setters
    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAffectedComponent() {
        return affectedComponent;
    }

    public void setAffectedComponent(String affectedComponent) {
        this.affectedComponent = affectedComponent;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
}