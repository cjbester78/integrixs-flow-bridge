package com.integrixs.adapters.messaging.rabbitmq;

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
@ConfigurationProperties(prefix = "integrixs.adapters.rabbitmq")
public class RabbitMQConfig extends BaseAdapterConfig {
    
    // Connection settings
    private String host = "localhost";
    private int port = 5672;
    private String virtualHost = "/";
    private String username;
    private String password;
    private int connectionTimeout = 60000; // 60 seconds
    private int requestedHeartbeat = 60; // seconds
    
    // Channel settings
    private int channelCacheSize = 25;
    private long channelCheckoutTimeout = 0; // milliseconds, 0 = no limit
    
    // Exchange settings
    private String exchangeName;
    private ExchangeType exchangeType = ExchangeType.DIRECT;
    private boolean exchangeDurable = true;
    private boolean exchangeAutoDelete = false;
    private Map<String, Object> exchangeArguments = new HashMap<>();
    
    // Queue settings
    private String queueName;
    private boolean queueDurable = true;
    private boolean queueExclusive = false;
    private boolean queueAutoDelete = false;
    private Map<String, Object> queueArguments = new HashMap<>();
    
    // Routing
    private String routingKey = "";
    private String bindingKey = "";
    
    // Consumer settings
    private int prefetchCount = 1;
    private boolean autoAck = false;
    private String consumerTag;
    private boolean exclusive = false;
    private boolean noLocal = false;
    
    // Publisher settings
    private boolean publisherConfirms = true;
    private boolean publisherReturns = true;
    private boolean mandatory = false;
    private boolean immediate = false;
    
    // Message settings
    private MessageDeliveryMode deliveryMode = MessageDeliveryMode.PERSISTENT;
    private int priority = 0; // 0-9, higher = higher priority
    private long ttl = 0; // milliseconds, 0 = no expiration
    
    // Retry and error handling
    private int maxRetries = 3;
    private long retryDelay = 5000; // milliseconds
    private boolean enableDeadLetterExchange = true;
    private String deadLetterExchangeName;
    private String deadLetterRoutingKey;
    
    // SSL/TLS settings
    private boolean sslEnabled = false;
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;
    private String sslProtocol = "TLSv1.2";
    
    // Connection pool settings
    private boolean useConnectionPool = true;
    private int maxConnections = 10;
    private int minConnections = 1;
    private long connectionIdleTimeout = 600000; // 10 minutes
    
    // Monitoring and metrics
    private boolean enableMetrics = true;
    private boolean enableTracing = false;
    
    // Advanced features
    private boolean enablePublisherBatching = false;
    private int publisherBatchSize = 100;
    private long publisherBatchTimeout = 1000; // milliseconds
    
    // Transaction support
    private boolean enableTransactions = false;
    
    // Cluster settings
    private String[] clusterAddresses;
    private boolean enableClusterFailover = true;
    
    public enum ExchangeType {
        DIRECT("direct"),
        TOPIC("topic"),
        FANOUT("fanout"),
        HEADERS("headers"),
        DELAYED_MESSAGE("x-delayed-message"),
        CONSISTENT_HASH("x-consistent-hash"),
        RANDOM("x-random"),
        RECENT_HISTORY("x-recent-history");
        
        private final String type;
        
        ExchangeType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    public enum MessageDeliveryMode {
        NON_PERSISTENT(1),
        PERSISTENT(2);
        
        private final int mode;
        
        MessageDeliveryMode(int mode) {
            this.mode = mode;
        }
        
        public int getMode() {
            return mode;
        }
    }
    
    // Queue argument presets
    public static class QueueArguments {
        public static final String MESSAGE_TTL = "x-message-ttl";
        public static final String EXPIRES = "x-expires";
        public static final String MAX_LENGTH = "x-max-length";
        public static final String MAX_LENGTH_BYTES = "x-max-length-bytes";
        public static final String OVERFLOW = "x-overflow";
        public static final String DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
        public static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
        public static final String MAX_PRIORITY = "x-max-priority";
        public static final String QUEUE_MODE = "x-queue-mode";
        public static final String SINGLE_ACTIVE_CONSUMER = "x-single-active-consumer";
        public static final String QUEUE_TYPE = "x-queue-type";
    }
    
    // Message properties
    public static class MessageProperties {
        public static final String CONTENT_TYPE = "contentType";
        public static final String CONTENT_ENCODING = "contentEncoding";
        public static final String HEADERS = "headers";
        public static final String DELIVERY_MODE = "deliveryMode";
        public static final String PRIORITY = "priority";
        public static final String CORRELATION_ID = "correlationId";
        public static final String REPLY_TO = "replyTo";
        public static final String EXPIRATION = "expiration";
        public static final String MESSAGE_ID = "messageId";
        public static final String TIMESTAMP = "timestamp";
        public static final String TYPE = "type";
        public static final String USER_ID = "userId";
        public static final String APP_ID = "appId";
    }
    
    // Common headers
    public static class Headers {
        public static final String RETRY_COUNT = "x-retry-count";
        public static final String ORIGINAL_EXCHANGE = "x-original-exchange";
        public static final String ORIGINAL_ROUTING_KEY = "x-original-routing-key";
        public static final String DEATH_COUNT = "x-death-count";
        public static final String FIRST_DEATH_REASON = "x-first-death-reason";
        public static final String TRACE_ID = "x-trace-id";
        public static final String SPAN_ID = "x-span-id";
    }
    
    // Features configuration
    private Features features = new Features();
    
    @Data
    public static class Features {
        private boolean enableMessageOrdering = false;
        private boolean enableMessageDeduplication = false;
        private boolean enablePriorityQueues = false;
        private boolean enableLazyQueues = false;
        private boolean enableQuorumQueues = false;
        private boolean enableStreamQueues = false;
        private boolean enableRpcPattern = false;
        private boolean enableDelayedMessaging = false;
        private boolean enableMessageTracing = false;
        private boolean enableSchemaValidation = false;
    }
    
    // RabbitMQ Management API settings
    private ManagementApi managementApi = new ManagementApi();
    
    @Data
    public static class ManagementApi {
        private boolean enabled = false;
        private String url = "http://localhost:15672";
        private String username = "guest";
        private String password = "guest";
        private int connectionTimeout = 5000;
        private int readTimeout = 10000;
    }
}