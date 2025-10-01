package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing system logs including frontend application logs.
 * Enhanced to support comprehensive logging from both backend and frontend.
 */

    public class SystemLog {

    private UUID id;

    private LocalDateTime timestamp;

    private LogLevel level;

    private String message;

    private String details;

    private String source;

    private String sourceId;

    private String sourceName;

    private String component;

    private String componentId;

    private String domainType;

    private String domainReferenceId;

    private UUID userId;

    private String username;

    // Frontend - specific fields

    private String category;

    private String ipAddress;

    private String userAgent;

    private String correlationId;

    private String sessionId;

    private String stackTrace;

    private String url;

    private String browser;

    private String os;

    private String deviceType;

        private LocalDateTime createdAt;

    /**
     * Log level enumeration
     */
    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }

    /**
     * Pre - persist method to set defaults
     */
    protected void onCreate() {
        if(timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if(level == null) {
            level = LogLevel.INFO;
        }
    }

    // Additional helper methods for backward compatibility
    public String getDomainId() {
        return domainReferenceId;
    }

    public void setDomainId(String domainId) {
        this.domainReferenceId = domainId;
    }

    public void setAction(String action) {
        // Store action in the message or details field
        if(this.message == null || this.message.isEmpty()) {
            this.message = action;
        } else {
            this.details = "Action: " + action + (this.details != null ? "; " + this.details : "");
        }
    }

    // Default constructor
    public SystemLog() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Builder
    public static SystemLogBuilder builder() {
        return new SystemLogBuilder();
    }

    public static class SystemLogBuilder {
        private UUID id;
        private LocalDateTime timestamp;
        private LogLevel level;
        private String message;
        private String details;
        private String source;
        private String sourceId;
        private String sourceName;
        private String component;
        private String componentId;
        private String domainType;
        private String domainReferenceId;
        private UUID userId;
        private String username;
        private String category;
        private String ipAddress;
        private String userAgent;
        private String correlationId;
        private String sessionId;
        private String stackTrace;
        private String url;
        private String browser;
        private String os;
        private String deviceType;
        private LocalDateTime createdAt;

        public SystemLogBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public SystemLogBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SystemLogBuilder level(LogLevel level) {
            this.level = level;
            return this;
        }

        public SystemLogBuilder message(String message) {
            this.message = message;
            return this;
        }

        public SystemLogBuilder details(String details) {
            this.details = details;
            return this;
        }

        public SystemLogBuilder source(String source) {
            this.source = source;
            return this;
        }

        public SystemLogBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public SystemLogBuilder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public SystemLogBuilder component(String component) {
            this.component = component;
            return this;
        }

        public SystemLogBuilder componentId(String componentId) {
            this.componentId = componentId;
            return this;
        }

        public SystemLogBuilder domainType(String domainType) {
            this.domainType = domainType;
            return this;
        }

        public SystemLogBuilder domainReferenceId(String domainReferenceId) {
            this.domainReferenceId = domainReferenceId;
            return this;
        }

        public SystemLogBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public SystemLogBuilder username(String username) {
            this.username = username;
            return this;
        }

        public SystemLogBuilder category(String category) {
            this.category = category;
            return this;
        }

        public SystemLogBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public SystemLogBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public SystemLogBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public SystemLogBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public SystemLogBuilder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public SystemLogBuilder url(String url) {
            this.url = url;
            return this;
        }

        public SystemLogBuilder browser(String browser) {
            this.browser = browser;
            return this;
        }

        public SystemLogBuilder os(String os) {
            this.os = os;
            return this;
        }

        public SystemLogBuilder deviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public SystemLogBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SystemLog build() {
            SystemLog instance = new SystemLog();
            instance.setId(this.id);
            instance.setTimestamp(this.timestamp);
            instance.setLevel(this.level);
            instance.setMessage(this.message);
            instance.setDetails(this.details);
            instance.setSource(this.source);
            instance.setSourceId(this.sourceId);
            instance.setSourceName(this.sourceName);
            instance.setComponent(this.component);
            instance.setComponentId(this.componentId);
            instance.setDomainType(this.domainType);
            instance.setDomainReferenceId(this.domainReferenceId);
            instance.setUserId(this.userId);
            instance.setUsername(this.username);
            instance.setCategory(this.category);
            instance.setIpAddress(this.ipAddress);
            instance.setUserAgent(this.userAgent);
            instance.setCorrelationId(this.correlationId);
            instance.setSessionId(this.sessionId);
            instance.setStackTrace(this.stackTrace);
            instance.setUrl(this.url);
            instance.setBrowser(this.browser);
            instance.setOs(this.os);
            instance.setDeviceType(this.deviceType);
            instance.setCreatedAt(this.createdAt);
            return instance;
        }
    }
}
