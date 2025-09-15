package com.integrixs.adapters.config;

import java.io.Serializable;

/**
 * Configuration for Kafka outbound adapter(producer)
 */
public class KafkaOutboundAdapterConfig implements Serializable {

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
    private String saslMechanism; // PLAIN, SCRAM - SHA-256, SCRAM - SHA-512
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
    // Getters and Setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getKeySerializer() {
        return keySerializer;
    }
    public void setKeySerializer(String keySerializer) {
        this.keySerializer = keySerializer;
    }
    public String getValueSerializer() {
        return valueSerializer;
    }
    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }
    public String getAcks() {
        return acks;
    }
    public void setAcks(String acks) {
        this.acks = acks;
    }
    public Integer getRetries() {
        return retries;
    }
    public void setRetries(Integer retries) {
        this.retries = retries;
    }
    public Integer getBatchSize() {
        return batchSize;
    }
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
    public Long getLingerMs() {
        return lingerMs;
    }
    public void setLingerMs(Long lingerMs) {
        this.lingerMs = lingerMs;
    }
    public Long getBufferMemory() {
        return bufferMemory;
    }
    public void setBufferMemory(Long bufferMemory) {
        this.bufferMemory = bufferMemory;
    }
    public String getCompressionType() {
        return compressionType;
    }
    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }
    public String getSecurityProtocol() {
        return securityProtocol;
    }
    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }
    public String getSaslMechanism() {
        return saslMechanism;
    }
    public void setSaslMechanism(String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }
    public String getSaslJaasConfig() {
        return saslJaasConfig;
    }
    public void setSaslJaasConfig(String saslJaasConfig) {
        this.saslJaasConfig = saslJaasConfig;
    }
    public String getSslTruststoreLocation() {
        return sslTruststoreLocation;
    }
    public void setSslTruststoreLocation(String sslTruststoreLocation) {
        this.sslTruststoreLocation = sslTruststoreLocation;
    }
    public String getSslTruststorePassword() {
        return sslTruststorePassword;
    }
    public void setSslTruststorePassword(String sslTruststorePassword) {
        this.sslTruststorePassword = sslTruststorePassword;
    }
    public String getSslKeystoreLocation() {
        return sslKeystoreLocation;
    }
    public void setSslKeystoreLocation(String sslKeystoreLocation) {
        this.sslKeystoreLocation = sslKeystoreLocation;
    }
    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }
    public void setSslKeystorePassword(String sslKeystorePassword) {
        this.sslKeystorePassword = sslKeystorePassword;
    }
    public String getSslKeyPassword() {
        return sslKeyPassword;
    }
    public void setSslKeyPassword(String sslKeyPassword) {
        this.sslKeyPassword = sslKeyPassword;
    }
    public Boolean isIncludeHeaders() {
        return includeHeaders;
    }
    public void setIncludeHeaders(Boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }
    public String getPartitionKey() {
        return partitionKey;
    }
    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }
    public Long getRequestTimeoutMs() {
        return requestTimeoutMs;
    }
    public void setRequestTimeoutMs(Long requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }
}
