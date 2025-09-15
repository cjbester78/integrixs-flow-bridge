package com.integrixs.adapters.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for adapter configuration
 */
public class AdapterConfiguration {
    private String adapterId;
    private AdapterTypeEnum adapterType;
    private AdapterModeEnum adapterMode;
    private String name;
    private String description;
    private Map<String, Object> connectionProperties = new HashMap<>();
    private Map<String, Object> operationProperties = new HashMap<>();
    private AuthenticationConfig authentication;
    private RetryConfig retryConfig;
    private boolean enableLogging;
    private boolean enableMonitoring;
    private Integer timeout; // milliseconds

    /**
     * Adapter types
     */
    public enum AdapterTypeEnum {
        HTTP,     // HTTP/HTTPS endpoints
        REST,     // RESTful web services
        SOAP,     // SOAP web services
        JDBC,     // Database connections
        FILE,     // Local file system
        FTP,      // FTP protocol
        SFTP,     // Secure FTP
        IBMMQ,    // IBM MQ(formerly WebSphere MQ)
        KAFKA,    // Apache Kafka
        MAIL,     // Email(SMTP/POP3/IMAP)
        ODATA,    // OData protocol
        IDOC,     // SAP IDoc
        RFC        // SAP RFC
    ,
        // Social Media Adapters
        FACEBOOK,
        TWITTER,
        LINKEDIN,
        INSTAGRAM,
        REDDIT,
        PINTEREST,
        YOUTUBE,
        TIKTOK,
        SNAPCHAT,
        WHATSAPP,
        TELEGRAM,
        DISCORD,

        // Collaboration Adapters
        SLACK,
        TEAMS,

        // Messaging Adapters
        SMS,
        AMQP,
        RABBITMQ,
        ACTIVEMQ,

        // Other Adapters
        WEBHOOK,
        WEBSOCKET,
        STREAMING}

    /**
     * Adapter modes
     */
    public enum AdapterModeEnum {
        INBOUND, // Receives FROM external systems
        OUTBOUND // Sends TO external systems
    }

    /**
     * Add connection property
     * @param key Property key
     * @param value Property value
     */
    public void addConnectionProperty(String key, Object value) {
        this.connectionProperties.put(key, value);
    }

    /**
     * Add operation property
     * @param key Property key
     * @param value Property value
     */
    public void addOperationProperty(String key, Object value) {
        this.operationProperties.put(key, value);
    }
    // Getters and Setters
    public String getAdapterId() {
        return adapterId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }
    public AdapterTypeEnum getAdapterType() {
        return adapterType;
    }
    public void setAdapterType(AdapterTypeEnum adapterType) {
        this.adapterType = adapterType;
    }
    public AdapterModeEnum getAdapterMode() {
        return adapterMode;
    }
    public void setAdapterMode(AdapterModeEnum adapterMode) {
        this.adapterMode = adapterMode;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Map<String, Object> getConnectionProperties() {
        return connectionProperties;
    }
    public void setConnectionProperties(Map<String, Object> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }
    public Map<String, Object> getOperationProperties() {
        return operationProperties;
    }
    public void setOperationProperties(Map<String, Object> operationProperties) {
        this.operationProperties = operationProperties;
    }
    public AuthenticationConfig getAuthentication() {
        return authentication;
    }
    public void setAuthentication(AuthenticationConfig authentication) {
        this.authentication = authentication;
    }
    public RetryConfig getRetryConfig() {
        return retryConfig;
    }
    public void setRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }
    public boolean isEnableLogging() {
        return enableLogging;
    }
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
    public boolean isEnableMonitoring() {
        return enableMonitoring;
    }
    public void setEnableMonitoring(boolean enableMonitoring) {
        this.enableMonitoring = enableMonitoring;
    }
    public Integer getTimeout() {
        return timeout;
    }
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterId;
        private AdapterTypeEnum adapterType;
        private AdapterModeEnum adapterMode;
        private String name;
        private String description;
        private Map<String, Object> connectionProperties;
        private Map<String, Object> operationProperties;
        private AuthenticationConfig authentication;
        private RetryConfig retryConfig;
        private boolean enableLogging;
        private boolean enableMonitoring;
        private Integer timeout;

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder adapterType(AdapterTypeEnum adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public Builder adapterMode(AdapterModeEnum adapterMode) {
            this.adapterMode = adapterMode;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder connectionProperties(Map<String, Object> connectionProperties) {
            this.connectionProperties = connectionProperties;
            return this;
        }

        public Builder operationProperties(Map<String, Object> operationProperties) {
            this.operationProperties = operationProperties;
            return this;
        }

        public Builder authentication(AuthenticationConfig authentication) {
            this.authentication = authentication;
            return this;
        }

        public Builder retryConfig(RetryConfig retryConfig) {
            this.retryConfig = retryConfig;
            return this;
        }

        public Builder enableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        public Builder enableMonitoring(boolean enableMonitoring) {
            this.enableMonitoring = enableMonitoring;
            return this;
        }

        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public AdapterConfiguration build() {
            AdapterConfiguration obj = new AdapterConfiguration();
            obj.adapterId = this.adapterId;
            obj.adapterType = this.adapterType;
            obj.adapterMode = this.adapterMode;
            obj.name = this.name;
            obj.description = this.description;
            obj.connectionProperties = this.connectionProperties;
            obj.operationProperties = this.operationProperties;
            obj.authentication = this.authentication;
            obj.retryConfig = this.retryConfig;
            obj.enableLogging = this.enableLogging;
            obj.enableMonitoring = this.enableMonitoring;
            obj.timeout = this.timeout;
            return obj;
        }
    }
}
