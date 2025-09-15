package com.integrixs.adapters.config;

import java.io.Serializable;

/**
 * Configuration for Kafka inbound adapter(consumer)
 */
public class KafkaInboundAdapterConfig implements Serializable {

    // Kafka connection properties
    private String bootstrapServers = "localhost:9092";
    private String groupId = "integrix - consumer - group";
    private String topics; // comma - separated list of topics

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
    private String saslMechanism; // PLAIN, SCRAM - SHA-256, SCRAM - SHA-512
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
        // Kafka's max.poll.interval.ms default is 5 minutes(300000ms)
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
    // Getters and Setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public String getTopics() {
        return topics;
    }
    public void setTopics(String topics) {
        this.topics = topics;
    }
    public String getKeyDeserializer() {
        return keyDeserializer;
    }
    public void setKeyDeserializer(String keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
    }
    public String getValueDeserializer() {
        return valueDeserializer;
    }
    public void setValueDeserializer(String valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
    }
    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }
    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }
    public boolean isEnableAutoCommit() {
        return enableAutoCommit;
    }
    public void setEnableAutoCommit(boolean enableAutoCommit) {
        this.enableAutoCommit = enableAutoCommit;
    }
    public int getAutoCommitIntervalMs() {
        return autoCommitIntervalMs;
    }
    public void setAutoCommitIntervalMs(int autoCommitIntervalMs) {
        this.autoCommitIntervalMs = autoCommitIntervalMs;
    }
    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }
    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }
    public int getMaxPollRecords() {
        return maxPollRecords;
    }
    public void setMaxPollRecords(int maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }
    public long getPollTimeoutMs() {
        return pollTimeoutMs;
    }
    public void setPollTimeoutMs(long pollTimeoutMs) {
        this.pollTimeoutMs = pollTimeoutMs;
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
}
