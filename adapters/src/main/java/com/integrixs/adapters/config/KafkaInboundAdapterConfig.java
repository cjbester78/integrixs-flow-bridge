package com.integrixs.adapters.config;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Configuration for Kafka inbound adapter (consumer)
 */
@Data

public class KafkaInboundAdapterConfig implements Serializable {
    
    // Kafka connection properties
    private String bootstrapServers = "localhost:9092";
    private String groupId = "integrix-consumer-group";
    private String topics; // comma-separated list of topics
    
    // Consumer configuration
    private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private String autoOffsetReset = "earliest"; // earliest, latest, none
    private boolean enableAutoCommit = true;
    private int autoCommitIntervalMs = 5000;
    private int sessionTimeoutMs = 30000;
    private int maxPollRecords = 500;
    private long pollTimeoutMs = 1000;
    
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
    
    public void initializeDefaultProperties() {
        // Set default properties if needed
    }
    
    // Add missing methods required by adapter
    public Long getMaxPollIntervalMs() {
        // Kafka's max.poll.interval.ms default is 5 minutes (300000ms)
        return 300000L;
    }
    
    public java.util.Map<String, String> getAdditionalProperties() {
        // Return empty map for now, could be enhanced to support additional Kafka properties
        return new java.util.HashMap<>();
    }
    
    public Long getPollingInterval() {
        // Use poll timeout as polling interval
        return pollTimeoutMs;
    }
}