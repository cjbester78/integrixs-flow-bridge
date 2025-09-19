package com.integrixs.shared.dto.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * DTO representing a single frontend log entry.
 * Contains all information about events, errors, and user actions from the frontend.
 */
public class FrontendLogEntry {

    private String level;
    private String category;
    private String message;
    private Map<String, Object> details;
    private Object error;
    private String stackTrace;
    private String userAgent;
    private String url;
    private LocalDateTime timestamp;
    private UUID userId;
    private String sessionId;
    private String correlationId;
    private String clientIp;
    private LocalDateTime serverReceivedAt;

    // Default constructor
    public FrontendLogEntry() {
        this.details = new HashMap<>();
    }

    // All args constructor
    public FrontendLogEntry(String level, String category, String message, Map<String, Object> details, Object error, String stackTrace, String userAgent, String url, LocalDateTime timestamp, UUID userId, String sessionId, String correlationId, String clientIp, LocalDateTime serverReceivedAt) {
        this.level = level;
        this.category = category;
        this.message = message;
        this.details = details != null ? details : new HashMap<>();
        this.error = error;
        this.stackTrace = stackTrace;
        this.userAgent = userAgent;
        this.url = url;
        this.timestamp = timestamp;
        this.userId = userId;
        this.sessionId = sessionId;
        this.correlationId = correlationId;
        this.clientIp = clientIp;
        this.serverReceivedAt = serverReceivedAt;
    }

    // Getters
    public String getLevel() { return level; }
    public String getCategory() { return category; }
    public String getMessage() { return message; }
    public Map<String, Object> getDetails() { return details; }
    public Object getError() { return error; }
    public String getStackTrace() { return stackTrace; }
    public String getUserAgent() { return userAgent; }
    public String getUrl() { return url; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public UUID getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public String getCorrelationId() { return correlationId; }
    public String getClientIp() { return clientIp; }
    public LocalDateTime getServerReceivedAt() { return serverReceivedAt; }

    // Setters
    public void setLevel(String level) { this.level = level; }
    public void setCategory(String category) { this.category = category; }
    public void setMessage(String message) { this.message = message; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
    public void setError(Object error) { this.error = error; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setUrl(String url) { this.url = url; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public void setServerReceivedAt(LocalDateTime serverReceivedAt) { this.serverReceivedAt = serverReceivedAt; }

    // Builder
    public static FrontendLogEntryBuilder builder() {
        return new FrontendLogEntryBuilder();
    }

    public static class FrontendLogEntryBuilder {
        private String level;
        private String category;
        private String message;
        private Map<String, Object> details = new HashMap<>();
        private Object error;
        private String stackTrace;
        private String userAgent;
        private String url;
        private LocalDateTime timestamp;
        private UUID userId;
        private String sessionId;
        private String correlationId;
        private String clientIp;
        private LocalDateTime serverReceivedAt;

        public FrontendLogEntryBuilder level(String level) {
            this.level = level;
            return this;
        }

        public FrontendLogEntryBuilder category(String category) {
            this.category = category;
            return this;
        }

        public FrontendLogEntryBuilder message(String message) {
            this.message = message;
            return this;
        }

        public FrontendLogEntryBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public FrontendLogEntryBuilder error(Object error) {
            this.error = error;
            return this;
        }

        public FrontendLogEntryBuilder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public FrontendLogEntryBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public FrontendLogEntryBuilder url(String url) {
            this.url = url;
            return this;
        }

        public FrontendLogEntryBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public FrontendLogEntryBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public FrontendLogEntryBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public FrontendLogEntryBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public FrontendLogEntryBuilder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public FrontendLogEntryBuilder serverReceivedAt(LocalDateTime serverReceivedAt) {
            this.serverReceivedAt = serverReceivedAt;
            return this;
        }

        public FrontendLogEntry build() {
            return new FrontendLogEntry(level, category, message, details, error, stackTrace, userAgent, url, timestamp, userId, sessionId, correlationId, clientIp, serverReceivedAt);
        }
    }
}
