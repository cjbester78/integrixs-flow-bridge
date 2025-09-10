package com.integrixs.adapters.messaging.amqp;

import com.integrixs.adapters.config.BaseAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
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
        LINK_DETACH("link-detach"),
        SESSION_END("session-end"),
        CONNECTION_CLOSE("connection-close"),
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
        SCRAM_SHA_256("SCRAM-SHA-256"),
        SCRAM_SHA_512("SCRAM-SHA-512"),
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
        public static final String MESSAGE_ID = "message-id";
        public static final String USER_ID = "user-id";
        public static final String TO = "to";
        public static final String SUBJECT = "subject";
        public static final String REPLY_TO = "reply-to";
        public static final String CORRELATION_ID = "correlation-id";
        public static final String CONTENT_TYPE = "content-type";
        public static final String CONTENT_ENCODING = "content-encoding";
        public static final String ABSOLUTE_EXPIRY_TIME = "absolute-expiry-time";
        public static final String CREATION_TIME = "creation-time";
        public static final String GROUP_ID = "group-id";
        public static final String GROUP_SEQUENCE = "group-sequence";
        public static final String REPLY_TO_GROUP_ID = "reply-to-group-id";
    }
    
    // Standard annotations
    public static class Annotations {
        public static final String X_OPT_ENQUEUED_TIME = "x-opt-enqueued-time";
        public static final String X_OPT_LOCKED_UNTIL = "x-opt-locked-until";
        public static final String X_OPT_SEQUENCE_NUMBER = "x-opt-sequence-number";
        public static final String X_OPT_PARTITION_KEY = "x-opt-partition-key";
        public static final String X_OPT_VIA_PARTITION_KEY = "x-opt-via-partition-key";
    }
    
    // Features configuration
    private Features features = new Features();
    
    @Data
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
    
    // Protocol-specific settings for different AMQP brokers
    private BrokerType brokerType = BrokerType.GENERIC;
    
    public enum BrokerType {
        GENERIC,
        ACTIVEMQ_ARTEMIS,
        QPID,
        AZURE_SERVICE_BUS,
        SOLACE,
        IBM_MQ_AMQP,
        RABBITMQ_AMQP1
    }
    
    // Artemis-specific settings
    private ArtemisSettings artemisSettings = new ArtemisSettings();
    
    @Data
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
    
    @Data
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
}