package com.integrixs.monitoring.api.dto;


import java.time.LocalDateTime;

/**
 * DTO for record metric response
 */
public class RecordMetricResponseDTO {
    private boolean success;
    private String metricId;
    private LocalDateTime timestamp;
    private int alertsTriggered;
    private String errorMessage;

    // Constructors
    public RecordMetricResponseDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RecordMetricResponseDTO dto = new RecordMetricResponseDTO();

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder metricId(String metricId) {
            dto.metricId = metricId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            dto.timestamp = timestamp;
            return this;
        }

        public Builder alertsTriggered(int alertsTriggered) {
            dto.alertsTriggered = alertsTriggered;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            dto.errorMessage = errorMessage;
            return this;
        }

        public RecordMetricResponseDTO build() {
            return dto;
        }
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMetricId() {
        return metricId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getAlertsTriggered() {
        return alertsTriggered;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setAlertsTriggered(int alertsTriggered) {
        this.alertsTriggered = alertsTriggered;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
