package com.integrixs.adapters.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for adapter configuration
 */
@Data
@Builder
public class AdapterConfiguration {
    private String adapterId;
    private AdapterTypeEnum adapterType;
    private AdapterModeEnum adapterMode;
    private String name;
    private String description;
    @Builder.Default
    private Map<String, Object> connectionProperties = new HashMap<>();
    @Builder.Default
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
        HTTP,      // HTTP/HTTPS endpoints
        REST,      // RESTful web services
        SOAP,      // SOAP web services
        JDBC,      // Database connections
        FILE,      // Local file system
        FTP,       // FTP protocol
        SFTP,      // Secure FTP
        IBMMQ,     // IBM MQ (formerly WebSphere MQ)
        KAFKA,     // Apache Kafka
        MAIL,      // Email (SMTP/POP3/IMAP)
        ODATA,     // OData protocol
        IDOC,      // SAP IDoc
        RFC        // SAP RFC
    }
    
    /**
     * Adapter modes
     */
    public enum AdapterModeEnum {
        INBOUND,   // Receives FROM external systems
        OUTBOUND  // Sends TO external systems
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
}