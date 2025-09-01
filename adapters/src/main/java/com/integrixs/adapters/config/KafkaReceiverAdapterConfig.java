package com.integrixs.adapters.config;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Configuration for Kafka receiver adapter (producer)
 */
@Data

public class KafkaReceiverAdapterConfig implements Serializable {
    
    // Kafka connection properties
    private String bootstrapServers;
    private String topic; // target topic
    
    // Producer configuration
    private String keySerializer;
    private String valueSerializer;
    private String acks; // 0, 1, all
    private Integer retries;
    private Integer batchSize;
    private Long lingerMs;
    private Long bufferMemory;
    private String compressionType; // none, gzip, snappy, lz4, zstd
    
    // Security properties
    private String securityProtocol; // PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL
    private String saslMechanism; // PLAIN, SCRAM-SHA-256, SCRAM-SHA-512
    private String saslJaasConfig;
    
    // SSL properties
    private String sslTruststoreLocation;
    private String sslTruststorePassword;
    private String sslKeystoreLocation;
    private String sslKeystorePassword;
    private String sslKeyPassword;
    
    // Message properties
    private Boolean includeHeaders;
    private String partitionKey; // field name to use as partition key
    
    // Timeout configuration
    private Long requestTimeoutMs = 30000L; // Default 30 seconds
    
    public void initializeDefaultProperties() {
        // Set default properties if needed
    }
    
    // Add missing method required by adapter
    public Long getTimeout() {
        return requestTimeoutMs != null ? requestTimeoutMs : 30000L;
    }
}