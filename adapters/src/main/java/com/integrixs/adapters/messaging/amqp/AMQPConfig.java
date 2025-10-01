package com.integrixs.adapters.messaging.amqp;

import com.integrixs.adapters.config.BaseAdapterConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

@Configuration
@ConfigurationProperties(prefix = "integrixs.adapters.amqp")
public class AMQPConfig extends BaseAdapterConfig {

    // AMQP version
    private AMQPVersion version = AMQPVersion.AMQP_1_0;

    // Connection settings
    private String host = "localhost";
    private int port = 5672;
    private String username;
    private String password;
    private String connectionUrl; // For advanced AMQP URL configuration
    private int connectionTimeout = 30000; // 30 seconds
    private int idleTimeout = 60000; // 60 seconds

    // Container settings
    private String containerId;
    private String containerName = "IntegrixsFlowBridge";
    private int maxFrameSize = 65536; // 64KB
    private int channelMax = 65535;

    // Link settings
    private String linkName;
    private LinkRole linkRole = LinkRole.SENDER;
    private int linkCredit = 100;
    private TerminusDurability durability = TerminusDurability.NONE;
    private TerminusExpiryPolicy expiryPolicy = TerminusExpiryPolicy.SESSION_END;

    // Message settings
    private boolean durable = true;
    private int priority = 4; // 0-9
    private long ttl = 0; // milliseconds, 0 = no expiration
    private boolean firstAcquirer = false;
    private int deliveryCount = 0;

    // Source/Target settings
    private String sourceAddress;
    private String targetAddress;
    private DistributionMode distributionMode = DistributionMode.MOVE;
    private Map<String, Object> sourceFilters = new HashMap<>();
    private Map<String, Object> targetProperties = new HashMap<>();

    // Session settings
    private int sessionWindowSize = 1024;
    private long sessionTimeout = 30000; // milliseconds

    // Transaction support
    private boolean enableTransactions = false;
    private TransactionMode transactionMode = TransactionMode.LOCAL;
    private long transactionTimeout = 60000; // milliseconds

    // Security settings
    private SaslMechanism saslMechanism = SaslMechanism.PLAIN;
    private boolean useSsl = false;
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;
    private String sslProtocol = "TLSv1.2";
    private boolean verifyHost = true;

    // Flow control
    private boolean enableFlowControl = true;
    private int incomingWindow = 1024;
    private int outgoingWindow = 1024;

    // Message routing
    private String routingType = "anycast"; // anycast or multicast
    private String routingKey;
    private Map<String, Object> messageAnnotations = new HashMap<>();
    private Map<String, Object> deliveryAnnotations = new HashMap<>();

    // Error handling
    private int maxRetries = 3;
    private long retryDelay = 5000; // milliseconds
    private boolean enableDeadLettering = true;
    private String deadLetterAddress;

    // Performance tuning
    private int prefetchSize = 100;
    private boolean enableBatching = false;
    private int batchSize = 100;
    private long batchTimeout = 1000; // milliseconds

    // Advanced features
    private boolean enableMessageGrouping = false;
    private String groupId;
    private int groupSequence = 0;
    private boolean enableLargeMessages = false;
    private long maxMessageSize = 1048576; // 1MB

    // Additional fields for missing getters/setters
    private boolean enableAutoReconnect = true;
    private boolean enableHeartbeat = true;
    private boolean enableCompression = false;
    private boolean enableTracing = false;
    private boolean enableMetrics = false;
    private boolean enableMessageValidation = true;
    private boolean enableDuplicateDetection = false;
    private boolean enableOrderingGuarantee = false;
    private boolean enableExactlyOnceDelivery = false;
    private boolean enableEndToEndEncryption = false;

    // Deduplication and cleanup settings
    private long deduplicationTtlSeconds = 3600; // 1 hour
    private long cleanupIntervalSeconds = 60; // 1 minute

    // Broker-specific settings
    private BrokerType brokerType = BrokerType.GENERIC;
    private ArtemisSettings artemisSettings = new ArtemisSettings();
    private String addressPrefix = "";
    private String queuePrefix = "";
    private boolean autoCreateQueues = false;
    private boolean autoCreateAddresses = false;
    private String addressFullPolicy = "BLOCK";

    // Azure Service Bus specific
    private String namespace;
    private String sharedAccessKeyName;
    private String sharedAccessKey;
    private String entityPath;
    private boolean enablePartitioning = false;
    private boolean requiresDuplicateDetection = false;
    private boolean requiresSession = false;
    private long lockDuration = 60000;

    public enum AMQPVersion {
        AMQP_0_9_1("0-9-1"),
        AMQP_1_0("1.0");

        private final String version;

        AMQPVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    public enum LinkRole {
        SENDER,
        RECEIVER
    }

    public enum TerminusDurability {
        NONE(0),
        CONFIGURATION(1),
        UNSETTLED_STATE(2);

        private final int value;

        TerminusDurability(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum TerminusExpiryPolicy {
        LINK_DETACH("link - detach"),
        SESSION_END("session - end"),
        CONNECTION_CLOSE("connection - close"),
        NEVER("never");

        private final String policy;

        TerminusExpiryPolicy(String policy) {
            this.policy = policy;
        }

        public String getPolicy() {
            return policy;
        }
    }

    public enum DistributionMode {
        MOVE("move"),
        COPY("copy");

        private final String mode;

        DistributionMode(String mode) {
            this.mode = mode;
        }

        public String getMode() {
            return mode;
        }
    }

    public enum TransactionMode {
        LOCAL,
        DISTRIBUTED
    }

    public enum SaslMechanism {
        PLAIN("PLAIN"),
        ANONYMOUS("ANONYMOUS"),
        EXTERNAL("EXTERNAL"),
        SCRAM_SHA_256("SCRAM - SHA-256"),
        SCRAM_SHA_512("SCRAM - SHA-512"),
        GSSAPI("GSSAPI");

        private final String mechanism;

        SaslMechanism(String mechanism) {
            this.mechanism = mechanism;
        }

        public String getMechanism() {
            return mechanism;
        }
    }

    // Message properties
    public static class MessageProperties {
        public static final String MESSAGE_ID = "message - id";
        public static final String USER_ID = "user - id";
        public static final String TO = "to";
        public static final String SUBJECT = "subject";
        public static final String REPLY_TO = "reply - to";
        public static final String CORRELATION_ID = "correlation - id";
        public static final String CONTENT_TYPE = "content - type";
        public static final String CONTENT_ENCODING = "content - encoding";
        public static final String ABSOLUTE_EXPIRY_TIME = "absolute - expiry - time";
        public static final String CREATION_TIME = "creation - time";
        public static final String GROUP_ID = "group - id";
        public static final String GROUP_SEQUENCE = "group - sequence";
        public static final String REPLY_TO_GROUP_ID = "reply - to - group - id";
    }

    // Standard annotations
    public static class Annotations {
        public static final String X_OPT_ENQUEUED_TIME = "x - opt - enqueued - time";
        public static final String X_OPT_LOCKED_UNTIL = "x - opt - locked - until";
        public static final String X_OPT_SEQUENCE_NUMBER = "x - opt - sequence - number";
        public static final String X_OPT_PARTITION_KEY = "x - opt - partition - key";
        public static final String X_OPT_VIA_PARTITION_KEY = "x - opt - via - partition - key";
    }

    // Features configuration
    private Features features = new Features();

    public static class Features {
        private boolean enableAutoReconnect = true;
        private boolean enableHeartbeat = true;
        private boolean enableCompression = false;
        private boolean enableTracing = false;
        private boolean enableMetrics = true;
        private boolean enableMessageValidation = false;
        private boolean enableDuplicateDetection = false;
        private boolean enableOrderingGuarantee = false;
        private boolean enableExactlyOnceDelivery = false;
        private boolean enableEndToEndEncryption = false;
    }

    public enum BrokerType {
        GENERIC,
        ACTIVEMQ_ARTEMIS,
        QPID,
        AZURE_SERVICE_BUS,
        SOLACE,
        IBM_MQ_AMQP,
        RABBITMQ_AMQP1
    }

    // Artemis - specific settings
    public static class ArtemisSettings {
        private boolean enableLargeMessages = true;
        private String addressPrefix = "";
        private String queuePrefix = "";
        private boolean autoCreateQueues = false;
        private boolean autoCreateAddresses = false;
        private String addressFullPolicy = "BLOCK"; // BLOCK, FAIL, PAGE, DROP
    }

    // Azure Service Bus specific settings
    private AzureServiceBusSettings azureSettings = new AzureServiceBusSettings();

    public static class AzureServiceBusSettings {
        private String namespace;
        private String sharedAccessKeyName;
        private String sharedAccessKey;
        private String entityPath;
        private boolean enablePartitioning = false;
        private boolean requiresDuplicateDetection = false;
        private boolean requiresSession = false;
        private long lockDuration = 60000; // milliseconds
    }
    // Getters and Setters
    public AMQPVersion getVersion() {
        return version;
    }
    public void setVersion(AMQPVersion version) {
        this.version = version;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getConnectionUrl() {
        return connectionUrl;
    }
    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    public int getIdleTimeout() {
        return idleTimeout;
    }
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }
    public String getContainerId() {
        return containerId;
    }
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
    public String getContainerName() {
        return containerName;
    }
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    public int getMaxFrameSize() {
        return maxFrameSize;
    }
    public void setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }
    public int getChannelMax() {
        return channelMax;
    }
    public void setChannelMax(int channelMax) {
        this.channelMax = channelMax;
    }
    public String getLinkName() {
        return linkName;
    }
    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }
    public LinkRole getLinkRole() {
        return linkRole;
    }
    public void setLinkRole(LinkRole linkRole) {
        this.linkRole = linkRole;
    }
    public int getLinkCredit() {
        return linkCredit;
    }
    public void setLinkCredit(int linkCredit) {
        this.linkCredit = linkCredit;
    }
    public TerminusDurability getDurability() {
        return durability;
    }
    public void setDurability(TerminusDurability durability) {
        this.durability = durability;
    }
    public TerminusExpiryPolicy getExpiryPolicy() {
        return expiryPolicy;
    }
    public void setExpiryPolicy(TerminusExpiryPolicy expiryPolicy) {
        this.expiryPolicy = expiryPolicy;
    }
    public boolean isDurable() {
        return durable;
    }
    public void setDurable(boolean durable) {
        this.durable = durable;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public long getTtl() {
        return ttl;
    }
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
    public boolean isFirstAcquirer() {
        return firstAcquirer;
    }
    public void setFirstAcquirer(boolean firstAcquirer) {
        this.firstAcquirer = firstAcquirer;
    }
    public int getDeliveryCount() {
        return deliveryCount;
    }
    public void setDeliveryCount(int deliveryCount) {
        this.deliveryCount = deliveryCount;
    }
    public String getSourceAddress() {
        return sourceAddress;
    }
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
    public String getTargetAddress() {
        return targetAddress;
    }
    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }
    public DistributionMode getDistributionMode() {
        return distributionMode;
    }
    public void setDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
    }
    public Map<String, Object> getSourceFilters() {
        return sourceFilters;
    }
    public void setSourceFilters(Map<String, Object> sourceFilters) {
        this.sourceFilters = sourceFilters;
    }
    public Map<String, Object> getTargetProperties() {
        return targetProperties;
    }
    public void setTargetProperties(Map<String, Object> targetProperties) {
        this.targetProperties = targetProperties;
    }
    public int getSessionWindowSize() {
        return sessionWindowSize;
    }
    public void setSessionWindowSize(int sessionWindowSize) {
        this.sessionWindowSize = sessionWindowSize;
    }
    public long getSessionTimeout() {
        return sessionTimeout;
    }
    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
    public boolean isEnableTransactions() {
        return enableTransactions;
    }
    public void setEnableTransactions(boolean enableTransactions) {
        this.enableTransactions = enableTransactions;
    }
    public TransactionMode getTransactionMode() {
        return transactionMode;
    }
    public void setTransactionMode(TransactionMode transactionMode) {
        this.transactionMode = transactionMode;
    }
    public long getTransactionTimeout() {
        return transactionTimeout;
    }
    public void setTransactionTimeout(long transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }
    public SaslMechanism getSaslMechanism() {
        return saslMechanism;
    }
    public void setSaslMechanism(SaslMechanism saslMechanism) {
        this.saslMechanism = saslMechanism;
    }
    public boolean isUseSsl() {
        return useSsl;
    }
    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }
    public String getTrustStore() {
        return trustStore;
    }
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }
    public String getTrustStorePassword() {
        return trustStorePassword;
    }
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }
    public String getKeyStore() {
        return keyStore;
    }
    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }
    public String getKeyStorePassword() {
        return keyStorePassword;
    }
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }
    public String getSslProtocol() {
        return sslProtocol;
    }
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    public boolean isVerifyHost() {
        return verifyHost;
    }
    public void setVerifyHost(boolean verifyHost) {
        this.verifyHost = verifyHost;
    }
    public boolean isEnableFlowControl() {
        return enableFlowControl;
    }
    public void setEnableFlowControl(boolean enableFlowControl) {
        this.enableFlowControl = enableFlowControl;
    }
    public int getIncomingWindow() {
        return incomingWindow;
    }
    public void setIncomingWindow(int incomingWindow) {
        this.incomingWindow = incomingWindow;
    }
    public int getOutgoingWindow() {
        return outgoingWindow;
    }
    public void setOutgoingWindow(int outgoingWindow) {
        this.outgoingWindow = outgoingWindow;
    }
    public String getRoutingType() {
        return routingType;
    }
    public void setRoutingType(String routingType) {
        this.routingType = routingType;
    }
    public String getRoutingKey() {
        return routingKey;
    }
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
    public Map<String, Object> getMessageAnnotations() {
        return messageAnnotations;
    }
    public void setMessageAnnotations(Map<String, Object> messageAnnotations) {
        this.messageAnnotations = messageAnnotations;
    }
    public Map<String, Object> getDeliveryAnnotations() {
        return deliveryAnnotations;
    }
    public void setDeliveryAnnotations(Map<String, Object> deliveryAnnotations) {
        this.deliveryAnnotations = deliveryAnnotations;
    }
    public int getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    public long getRetryDelay() {
        return retryDelay;
    }
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
    public boolean isEnableDeadLettering() {
        return enableDeadLettering;
    }
    public void setEnableDeadLettering(boolean enableDeadLettering) {
        this.enableDeadLettering = enableDeadLettering;
    }
    public String getDeadLetterAddress() {
        return deadLetterAddress;
    }
    public void setDeadLetterAddress(String deadLetterAddress) {
        this.deadLetterAddress = deadLetterAddress;
    }
    public int getPrefetchSize() {
        return prefetchSize;
    }
    public void setPrefetchSize(int prefetchSize) {
        this.prefetchSize = prefetchSize;
    }
    public boolean isEnableBatching() {
        return enableBatching;
    }
    public void setEnableBatching(boolean enableBatching) {
        this.enableBatching = enableBatching;
    }
    public int getBatchSize() {
        return batchSize;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    public long getBatchTimeout() {
        return batchTimeout;
    }
    public void setBatchTimeout(long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }
    public boolean isEnableMessageGrouping() {
        return enableMessageGrouping;
    }
    public void setEnableMessageGrouping(boolean enableMessageGrouping) {
        this.enableMessageGrouping = enableMessageGrouping;
    }
    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public int getGroupSequence() {
        return groupSequence;
    }
    public void setGroupSequence(int groupSequence) {
        this.groupSequence = groupSequence;
    }
    public boolean isEnableLargeMessages() {
        return enableLargeMessages;
    }
    public void setEnableLargeMessages(boolean enableLargeMessages) {
        this.enableLargeMessages = enableLargeMessages;
    }
    public long getMaxMessageSize() {
        return maxMessageSize;
    }
    public void setMaxMessageSize(long maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }
    public Features getFeatures() {
        return features;
    }
    public void setFeatures(Features features) {
        this.features = features;
    }
    public boolean isEnableAutoReconnect() {
        return enableAutoReconnect;
    }
    public void setEnableAutoReconnect(boolean enableAutoReconnect) {
        this.enableAutoReconnect = enableAutoReconnect;
    }
    public boolean isEnableHeartbeat() {
        return enableHeartbeat;
    }
    public void setEnableHeartbeat(boolean enableHeartbeat) {
        this.enableHeartbeat = enableHeartbeat;
    }
    public boolean isEnableCompression() {
        return enableCompression;
    }
    public void setEnableCompression(boolean enableCompression) {
        this.enableCompression = enableCompression;
    }
    public boolean isEnableTracing() {
        return enableTracing;
    }
    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }
    public boolean isEnableMetrics() {
        return enableMetrics;
    }
    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }
    public boolean isEnableMessageValidation() {
        return enableMessageValidation;
    }
    public void setEnableMessageValidation(boolean enableMessageValidation) {
        this.enableMessageValidation = enableMessageValidation;
    }
    public boolean isEnableDuplicateDetection() {
        return enableDuplicateDetection;
    }
    public void setEnableDuplicateDetection(boolean enableDuplicateDetection) {
        this.enableDuplicateDetection = enableDuplicateDetection;
    }
    public boolean isEnableOrderingGuarantee() {
        return enableOrderingGuarantee;
    }
    public void setEnableOrderingGuarantee(boolean enableOrderingGuarantee) {
        this.enableOrderingGuarantee = enableOrderingGuarantee;
    }
    public boolean isEnableExactlyOnceDelivery() {
        return enableExactlyOnceDelivery;
    }
    public void setEnableExactlyOnceDelivery(boolean enableExactlyOnceDelivery) {
        this.enableExactlyOnceDelivery = enableExactlyOnceDelivery;
    }
    public boolean isEnableEndToEndEncryption() {
        return enableEndToEndEncryption;
    }
    public void setEnableEndToEndEncryption(boolean enableEndToEndEncryption) {
        this.enableEndToEndEncryption = enableEndToEndEncryption;
    }
    public BrokerType getBrokerType() {
        return brokerType;
    }
    public void setBrokerType(BrokerType brokerType) {
        this.brokerType = brokerType;
    }
    public ArtemisSettings getArtemisSettings() {
        return artemisSettings;
    }
    public void setArtemisSettings(ArtemisSettings artemisSettings) {
        this.artemisSettings = artemisSettings;
    }
    public String getAddressPrefix() {
        return addressPrefix;
    }
    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }
    public String getQueuePrefix() {
        return queuePrefix;
    }
    public void setQueuePrefix(String queuePrefix) {
        this.queuePrefix = queuePrefix;
    }
    public boolean isAutoCreateQueues() {
        return autoCreateQueues;
    }
    public void setAutoCreateQueues(boolean autoCreateQueues) {
        this.autoCreateQueues = autoCreateQueues;
    }
    public boolean isAutoCreateAddresses() {
        return autoCreateAddresses;
    }
    public void setAutoCreateAddresses(boolean autoCreateAddresses) {
        this.autoCreateAddresses = autoCreateAddresses;
    }
    public String getAddressFullPolicy() {
        return addressFullPolicy;
    }
    public void setAddressFullPolicy(String addressFullPolicy) {
        this.addressFullPolicy = addressFullPolicy;
    }
    public AzureServiceBusSettings getAzureSettings() {
        return azureSettings;
    }
    public void setAzureSettings(AzureServiceBusSettings azureSettings) {
        this.azureSettings = azureSettings;
    }
    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }
    public void setSharedAccessKeyName(String sharedAccessKeyName) {
        this.sharedAccessKeyName = sharedAccessKeyName;
    }
    public String getSharedAccessKey() {
        return sharedAccessKey;
    }
    public void setSharedAccessKey(String sharedAccessKey) {
        this.sharedAccessKey = sharedAccessKey;
    }
    public String getEntityPath() {
        return entityPath;
    }
    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
    }
    public boolean isEnablePartitioning() {
        return enablePartitioning;
    }
    public void setEnablePartitioning(boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
    }
    public boolean isRequiresDuplicateDetection() {
        return requiresDuplicateDetection;
    }
    public void setRequiresDuplicateDetection(boolean requiresDuplicateDetection) {
        this.requiresDuplicateDetection = requiresDuplicateDetection;
    }
    public boolean isRequiresSession() {
        return requiresSession;
    }
    public void setRequiresSession(boolean requiresSession) {
        this.requiresSession = requiresSession;
    }
    public long getLockDuration() {
        return lockDuration;
    }
    public void setLockDuration(long lockDuration) {
        this.lockDuration = lockDuration;
    }

    public long getDeduplicationTtlSeconds() {
        return deduplicationTtlSeconds;
    }
    public void setDeduplicationTtlSeconds(long deduplicationTtlSeconds) {
        this.deduplicationTtlSeconds = deduplicationTtlSeconds;
    }

    public long getCleanupIntervalSeconds() {
        return cleanupIntervalSeconds;
    }
    public void setCleanupIntervalSeconds(long cleanupIntervalSeconds) {
        this.cleanupIntervalSeconds = cleanupIntervalSeconds;
    }
}
