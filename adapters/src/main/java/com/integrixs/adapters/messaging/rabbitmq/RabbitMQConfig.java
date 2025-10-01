package com.integrixs.adapters.messaging.rabbitmq;

import com.integrixs.adapters.config.BaseAdapterConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

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

    // Recovery settings
    private long networkRecoveryInterval = 5000; // milliseconds

    // Connection settings
    private String connectionNamePrefix = "IntegrixsFlowBridge_Outbound_";
    private int defaultPort = 5672;
    private long confirmWaitTime = 1000; // milliseconds to wait for confirms

    // Priority settings
    private int maxPriority = 10; // max priority level for priority queues

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
        DELAYED_MESSAGE("x - delayed - message"),
        CONSISTENT_HASH("x - consistent - hash"),
        RANDOM("x - random"),
        RECENT_HISTORY("x - recent - history");

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
        public static final String MESSAGE_TTL = "x - message - ttl";
        public static final String EXPIRES = "x - expires";
        public static final String MAX_LENGTH = "x - max - length";
        public static final String MAX_LENGTH_BYTES = "x - max - length - bytes";
        public static final String OVERFLOW = "x - overflow";
        public static final String DEAD_LETTER_EXCHANGE = "x - dead - letter - exchange";
        public static final String DEAD_LETTER_ROUTING_KEY = "x - dead - letter - routing - key";
        public static final String MAX_PRIORITY = "x - max - priority";
        public static final String QUEUE_MODE = "x - queue - mode";
        public static final String SINGLE_ACTIVE_CONSUMER = "x - single - active - consumer";
        public static final String QUEUE_TYPE = "x - queue - type";
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
        public static final String RETRY_COUNT = "x - retry - count";
        public static final String ORIGINAL_EXCHANGE = "x - original - exchange";
        public static final String ORIGINAL_ROUTING_KEY = "x - original - routing - key";
        public static final String DEATH_COUNT = "x - death - count";
        public static final String FIRST_DEATH_REASON = "x - first - death - reason";
        public static final String TRACE_ID = "x - trace - id";
        public static final String SPAN_ID = "x - span - id";
    }

    // Features configuration
    private Features features = new Features();

    // Feature flags - these need to be in the outer class for the getters/setters
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

    // Management API fields referenced by getters/setters
    private boolean enabled = false;
    private String url;
    private int readTimeout = 10000;

    public static class ManagementApi {
        private boolean enabled = false;
        private String url;
        private String username = "guest";
        private String password = "guest";
        private int connectionTimeout = 5000;
        private int readTimeout = 10000;
    }
    // Getters and Setters
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
    public String getVirtualHost() {
        return virtualHost;
    }
    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
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
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    public int getRequestedHeartbeat() {
        return requestedHeartbeat;
    }
    public void setRequestedHeartbeat(int requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }
    public int getChannelCacheSize() {
        return channelCacheSize;
    }
    public void setChannelCacheSize(int channelCacheSize) {
        this.channelCacheSize = channelCacheSize;
    }
    public long getChannelCheckoutTimeout() {
        return channelCheckoutTimeout;
    }
    public void setChannelCheckoutTimeout(long channelCheckoutTimeout) {
        this.channelCheckoutTimeout = channelCheckoutTimeout;
    }
    public String getExchangeName() {
        return exchangeName;
    }
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }
    public ExchangeType getExchangeType() {
        return exchangeType;
    }
    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }
    public boolean isExchangeDurable() {
        return exchangeDurable;
    }
    public void setExchangeDurable(boolean exchangeDurable) {
        this.exchangeDurable = exchangeDurable;
    }
    public boolean isExchangeAutoDelete() {
        return exchangeAutoDelete;
    }
    public void setExchangeAutoDelete(boolean exchangeAutoDelete) {
        this.exchangeAutoDelete = exchangeAutoDelete;
    }
    public Map<String, Object> getExchangeArguments() {
        return exchangeArguments;
    }
    public void setExchangeArguments(Map<String, Object> exchangeArguments) {
        this.exchangeArguments = exchangeArguments;
    }
    public String getQueueName() {
        return queueName;
    }
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    public boolean isQueueDurable() {
        return queueDurable;
    }
    public void setQueueDurable(boolean queueDurable) {
        this.queueDurable = queueDurable;
    }
    public boolean isQueueExclusive() {
        return queueExclusive;
    }
    public void setQueueExclusive(boolean queueExclusive) {
        this.queueExclusive = queueExclusive;
    }
    public boolean isQueueAutoDelete() {
        return queueAutoDelete;
    }
    public void setQueueAutoDelete(boolean queueAutoDelete) {
        this.queueAutoDelete = queueAutoDelete;
    }
    public Map<String, Object> getQueueArguments() {
        return queueArguments;
    }
    public void setQueueArguments(Map<String, Object> queueArguments) {
        this.queueArguments = queueArguments;
    }
    public String getRoutingKey() {
        return routingKey;
    }
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
    public String getBindingKey() {
        return bindingKey;
    }
    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }
    public int getPrefetchCount() {
        return prefetchCount;
    }
    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }
    public boolean isAutoAck() {
        return autoAck;
    }
    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }
    public String getConsumerTag() {
        return consumerTag;
    }
    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }
    public boolean isExclusive() {
        return exclusive;
    }
    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }
    public boolean isNoLocal() {
        return noLocal;
    }
    public void setNoLocal(boolean noLocal) {
        this.noLocal = noLocal;
    }
    public boolean isPublisherConfirms() {
        return publisherConfirms;
    }
    public void setPublisherConfirms(boolean publisherConfirms) {
        this.publisherConfirms = publisherConfirms;
    }
    public boolean isPublisherReturns() {
        return publisherReturns;
    }
    public void setPublisherReturns(boolean publisherReturns) {
        this.publisherReturns = publisherReturns;
    }
    public boolean isMandatory() {
        return mandatory;
    }
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
    public boolean isImmediate() {
        return immediate;
    }
    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }
    public MessageDeliveryMode getDeliveryMode() {
        return deliveryMode;
    }
    public void setDeliveryMode(MessageDeliveryMode deliveryMode) {
        this.deliveryMode = deliveryMode;
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
    public boolean isEnableDeadLetterExchange() {
        return enableDeadLetterExchange;
    }
    public void setEnableDeadLetterExchange(boolean enableDeadLetterExchange) {
        this.enableDeadLetterExchange = enableDeadLetterExchange;
    }
    public String getDeadLetterExchangeName() {
        return deadLetterExchangeName;
    }
    public void setDeadLetterExchangeName(String deadLetterExchangeName) {
        this.deadLetterExchangeName = deadLetterExchangeName;
    }
    public String getDeadLetterRoutingKey() {
        return deadLetterRoutingKey;
    }
    public void setDeadLetterRoutingKey(String deadLetterRoutingKey) {
        this.deadLetterRoutingKey = deadLetterRoutingKey;
    }
    public boolean isSslEnabled() {
        return sslEnabled;
    }
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
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
    public boolean isUseConnectionPool() {
        return useConnectionPool;
    }
    public void setUseConnectionPool(boolean useConnectionPool) {
        this.useConnectionPool = useConnectionPool;
    }
    public int getMaxConnections() {
        return maxConnections;
    }
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    public int getMinConnections() {
        return minConnections;
    }
    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }
    public long getConnectionIdleTimeout() {
        return connectionIdleTimeout;
    }
    public void setConnectionIdleTimeout(long connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }
    public boolean isEnableMetrics() {
        return enableMetrics;
    }
    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }
    public boolean isEnableTracing() {
        return enableTracing;
    }
    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }
    public boolean isEnablePublisherBatching() {
        return enablePublisherBatching;
    }
    public void setEnablePublisherBatching(boolean enablePublisherBatching) {
        this.enablePublisherBatching = enablePublisherBatching;
    }
    public int getPublisherBatchSize() {
        return publisherBatchSize;
    }
    public void setPublisherBatchSize(int publisherBatchSize) {
        this.publisherBatchSize = publisherBatchSize;
    }
    public long getPublisherBatchTimeout() {
        return publisherBatchTimeout;
    }
    public void setPublisherBatchTimeout(long publisherBatchTimeout) {
        this.publisherBatchTimeout = publisherBatchTimeout;
    }
    public boolean isEnableTransactions() {
        return enableTransactions;
    }
    public void setEnableTransactions(boolean enableTransactions) {
        this.enableTransactions = enableTransactions;
    }
    public String[] getClusterAddresses() {
        return clusterAddresses;
    }
    public void setClusterAddresses(String[] clusterAddresses) {
        this.clusterAddresses = clusterAddresses;
    }
    public boolean isEnableClusterFailover() {
        return enableClusterFailover;
    }
    public void setEnableClusterFailover(boolean enableClusterFailover) {
        this.enableClusterFailover = enableClusterFailover;
    }
    public Features getFeatures() {
        return features;
    }
    public void setFeatures(Features features) {
        this.features = features;
    }
    public boolean isEnableMessageOrdering() {
        return enableMessageOrdering;
    }
    public void setEnableMessageOrdering(boolean enableMessageOrdering) {
        this.enableMessageOrdering = enableMessageOrdering;
    }
    public boolean isEnableMessageDeduplication() {
        return enableMessageDeduplication;
    }
    public void setEnableMessageDeduplication(boolean enableMessageDeduplication) {
        this.enableMessageDeduplication = enableMessageDeduplication;
    }
    public boolean isEnablePriorityQueues() {
        return enablePriorityQueues;
    }
    public void setEnablePriorityQueues(boolean enablePriorityQueues) {
        this.enablePriorityQueues = enablePriorityQueues;
    }
    public boolean isEnableLazyQueues() {
        return enableLazyQueues;
    }
    public void setEnableLazyQueues(boolean enableLazyQueues) {
        this.enableLazyQueues = enableLazyQueues;
    }
    public boolean isEnableQuorumQueues() {
        return enableQuorumQueues;
    }
    public void setEnableQuorumQueues(boolean enableQuorumQueues) {
        this.enableQuorumQueues = enableQuorumQueues;
    }
    public boolean isEnableStreamQueues() {
        return enableStreamQueues;
    }
    public void setEnableStreamQueues(boolean enableStreamQueues) {
        this.enableStreamQueues = enableStreamQueues;
    }
    public boolean isEnableRpcPattern() {
        return enableRpcPattern;
    }
    public void setEnableRpcPattern(boolean enableRpcPattern) {
        this.enableRpcPattern = enableRpcPattern;
    }
    public boolean isEnableDelayedMessaging() {
        return enableDelayedMessaging;
    }
    public void setEnableDelayedMessaging(boolean enableDelayedMessaging) {
        this.enableDelayedMessaging = enableDelayedMessaging;
    }
    public boolean isEnableMessageTracing() {
        return enableMessageTracing;
    }
    public void setEnableMessageTracing(boolean enableMessageTracing) {
        this.enableMessageTracing = enableMessageTracing;
    }
    public boolean isEnableSchemaValidation() {
        return enableSchemaValidation;
    }
    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
    }
    public ManagementApi getManagementApi() {
        return managementApi;
    }
    public void setManagementApi(ManagementApi managementApi) {
        this.managementApi = managementApi;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public int getReadTimeout() {
        return readTimeout;
    }
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    public long getNetworkRecoveryInterval() {
        return networkRecoveryInterval;
    }
    public void setNetworkRecoveryInterval(long networkRecoveryInterval) {
        this.networkRecoveryInterval = networkRecoveryInterval;
    }
    public String getConnectionNamePrefix() {
        return connectionNamePrefix;
    }
    public void setConnectionNamePrefix(String connectionNamePrefix) {
        this.connectionNamePrefix = connectionNamePrefix;
    }
    public int getDefaultPort() {
        return defaultPort;
    }
    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }
    public long getConfirmWaitTime() {
        return confirmWaitTime;
    }
    public void setConfirmWaitTime(long confirmWaitTime) {
        this.confirmWaitTime = confirmWaitTime;
    }
    public int getMaxPriority() {
        return maxPriority;
    }
    public void setMaxPriority(int maxPriority) {
        this.maxPriority = maxPriority;
    }
}
