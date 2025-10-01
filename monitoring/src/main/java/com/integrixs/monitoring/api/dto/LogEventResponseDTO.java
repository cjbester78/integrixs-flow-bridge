package com.integrixs.monitoring.api.dto;


import java.time.LocalDateTime;

/**
 * DTO for log event response
 */
public class LogEventResponseDTO {
    private boolean success;
    private String eventId;
    private LocalDateTime timestamp;
    private int alertsTriggered;
    private String errorMessage;

    // Constructors
    public LogEventResponseDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LogEventResponseDTO dto = new LogEventResponseDTO();

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder eventId(String eventId) {
            dto.eventId = eventId;
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

        public LogEventResponseDTO build() {
            return dto;
        }
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getEventId() {
        return eventId;
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

    public void setEventId(String eventId) {
        this.eventId = eventId;
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
