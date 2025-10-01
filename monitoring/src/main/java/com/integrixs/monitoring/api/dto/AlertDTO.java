package com.integrixs.monitoring.api.dto;


import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for alert
 */
public class AlertDTO {
    private String alertId;
    private String alertName;
    private String alertType;
    private String severity;
    private String status;
    private String source;
    private String message;
    private String condition;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata;

    // Constructors
    public AlertDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertDTO dto = new AlertDTO();

        public Builder alertId(String alertId) {
            dto.alertId = alertId;
            return this;
        }

        public Builder alertName(String alertName) {
            dto.alertName = alertName;
            return this;
        }

        public Builder alertType(String alertType) {
            dto.alertType = alertType;
            return this;
        }

        public Builder severity(String severity) {
            dto.severity = severity;
            return this;
        }

        public Builder status(String status) {
            dto.status = status;
            return this;
        }

        public Builder source(String source) {
            dto.source = source;
            return this;
        }

        public Builder message(String message) {
            dto.message = message;
            return this;
        }

        public Builder condition(String condition) {
            dto.condition = condition;
            return this;
        }

        public Builder triggeredAt(LocalDateTime triggeredAt) {
            dto.triggeredAt = triggeredAt;
            return this;
        }

        public Builder resolvedAt(LocalDateTime resolvedAt) {
            dto.resolvedAt = resolvedAt;
            return this;
        }

        public Builder acknowledgedAt(LocalDateTime acknowledgedAt) {
            dto.acknowledgedAt = acknowledgedAt;
            return this;
        }

        public Builder acknowledgedBy(String acknowledgedBy) {
            dto.acknowledgedBy = acknowledgedBy;
            return this;
        }

        public Builder domainType(String domainType) {
            dto.domainType = domainType;
            return this;
        }

        public Builder domainReferenceId(String domainReferenceId) {
            dto.domainReferenceId = domainReferenceId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            dto.metadata = metadata;
            return this;
        }

        public AlertDTO build() {
            return dto;
        }
    }

    // Getters
    public String getAlertId() {
        return alertId;
    }

    public String getAlertName() {
        return alertName;
    }

    public String getAlertType() {
        return alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public String getCondition() {
        return condition;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public String getDomainType() {
        return domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // Setters
    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

}
