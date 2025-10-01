package com.integrixs.adapters.messaging.rabbitmq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.messaging.rabbitmq.RabbitMQConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// @Component  // REMOVED - was causing auto-initialization
public class RabbitMQOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQOutboundAdapter.class);

    public RabbitMQOutboundAdapter() {
        super(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.RABBITMQ);
    }

    @Autowired
    private RabbitMQConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private final Map<String, Channel> channelPool = new ConcurrentHashMap<>();
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    // Publisher confirms
    private final Map<Long, PendingConfirm> pendingConfirms = new ConcurrentHashMap<>();
    private final AtomicLong nextPublishSeqNo = new AtomicLong(1);

    // Metrics
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesConfirmed = new AtomicLong(0);
    private final AtomicLong messagesReturned = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);

    // Batching
    private final BlockingQueue<PublishRequest> publishQueue = new LinkedBlockingQueue<>();
    private ScheduledExecutorService batchExecutor;

    // RPC support
    private final Map<String, CompletableFuture<String>> rpcCallbacks = new ConcurrentHashMap<>();
    private String replyQueueName;

    // Required abstract methods from AbstractOutboundAdapter
    @Override
    protected void doReceiverInitialize() throws Exception {
        setupConnectionFactory();
        establishConnection();
        setupPublisherConfirms();
        setupBatchPublisher();
        if(config.isEnableRpcPattern()) {
            setupRpcSupport();
        }
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        destroy();
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // This is an outbound adapter - it sends messages to RabbitMQ, not receives
        log.debug("Outbound adapter does not receive data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        if (connection != null && connection.isOpen() && isConnected.get()) {
            return AdapterResult.success(null, "RabbitMQ connection is active");
        }

        try {
            establishConnection();
            return AdapterResult.success(null, "Successfully connected to RabbitMQ broker");
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
        }
    }

    @Override
    protected long getPollingIntervalMs() {
        // Not used for outbound adapter
        return 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("RabbitMQ Outbound: %s:%d/%s",
            config.getHost(), config.getPort(), config.getExchangeName());
    }

    @PostConstruct
    public void initialize() {
        try {
            setupConnectionFactory();
            establishConnection();
            setupPublisherConfirms();
            setupBatchPublisher();
            if(config.isEnableRpcPattern()) {
                setupRpcSupport();
            }
            // isInitialized managed by parent class
            log.info("RabbitMQ Outbound Adapter initialized successfully");
        } catch(Exception e) {
            log.error("Failed to initialize RabbitMQ Outbound Adapter", e);
            throw new RuntimeException("Failed to initialize RabbitMQ adapter", e);
        }
    }

    private void setupConnectionFactory() {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(config.getHost());
        connectionFactory.setPort(config.getPort());
        connectionFactory.setUsername(config.getUsername());
        connectionFactory.setPassword(config.getPassword());

        if(config.getVirtualHost() != null) {
            connectionFactory.setVirtualHost(config.getVirtualHost());
        }

        // Connection recovery
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(config.getNetworkRecoveryInterval());

        // Connection timeout
        connectionFactory.setConnectionTimeout(config.getConnectionTimeout());
        connectionFactory.setRequestedHeartbeat(config.getRequestedHeartbeat());

        // Set connection name
        Map<String, Object> clientProperties = new HashMap<>();
        clientProperties.put("connection_name", config.getConnectionNamePrefix() + UUID.randomUUID());
        connectionFactory.setClientProperties(clientProperties);

        // SSL configuration
        if(config.isSslEnabled()) {
            try {
                connectionFactory.useSslProtocol();
                log.info("SSL enabled for RabbitMQ connection");
            } catch(Exception e) {
                log.error("Failed to enable SSL", e);
                throw new RuntimeException("Failed to configure SSL", e);
            }
        }
    }

    private void establishConnection() throws IOException, TimeoutException {
        try {
            if(config.getClusterAddresses() != null && config.getClusterAddresses().length > 0) {
                Address[] addresses = Arrays.stream(config.getClusterAddresses())
                    .map(addr -> {
                        String[] parts = addr.split(":");
                        return new Address(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : config.getDefaultPort());
                    })
                    .toArray(Address[]::new);
                connection = connectionFactory.newConnection(addresses);
            } else {
                connection = connectionFactory.newConnection();
            }

            isConnected.set(true);
            log.info("Successfully connected to RabbitMQ");

        } catch(Exception e) {
            throw new RuntimeException("Failed to connect to RabbitMQ", e);
        }
    }

    private Channel getChannel() throws IOException {
        String channelKey = Thread.currentThread().getName();
        Channel channel = channelPool.get(channelKey);

        if(channel == null || !channel.isOpen()) {
            channel = connection.createChannel();

            // Configure channel
            if(config.getPrefetchCount() > 0) {
                channel.basicQos(config.getPrefetchCount());
            }

            // Enable publisher confirms
            if(config.isPublisherConfirms()) {
                channel.confirmSelect();
            }

            // Enable transactions if configured
            if(config.isEnableTransactions()) {
                channel.txSelect();
            }

            channelPool.put(channelKey, channel);
        }

        return channel;
    }

    private void setupPublisherConfirms() {
        if(!config.isPublisherConfirms()) {
            return;
        }

        try {
            Channel channel = getChannel();

            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) {
                    if(multiple) {
                        // Clear all confirms up to and including deliveryTag
                    pendingConfirms.entrySet().removeIf(entry -> entry.getKey() <= deliveryTag);
                    } else {
                        pendingConfirms.remove(deliveryTag);
                    }
                    messagesConfirmed.incrementAndGet();
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) {
                    if(multiple) {
                        // Handle multiple nacks
                        pendingConfirms.entrySet().stream()
                            .filter(entry -> entry.getKey() <= deliveryTag)
                            .forEach(entry -> {
                                entry.getValue().future.completeExceptionally(
                                    new IOException("Message nacked by broker"));
                            });
                        pendingConfirms.entrySet().removeIf(entry -> entry.getKey() <= deliveryTag);
                    } else {
                        PendingConfirm confirm = pendingConfirms.remove(deliveryTag);
                        if(confirm != null) {
                            confirm.future.completeExceptionally(
                                new IOException("Message nacked by broker"));
                        }
                    }
                }
            });

            // Setup return listener
            channel.addReturnListener(returnMessage -> {
                messagesReturned.incrementAndGet();
                log.warn("Message returned: exchange = {}, routingKey = {}, replyCode = {}, replyText = {}",
                    returnMessage.getExchange(),
                    returnMessage.getRoutingKey(),
                    returnMessage.getReplyCode(),
                    returnMessage.getReplyText());
            });

        } catch(Exception e) {
            log.error("Failed to setup publisher confirms", e);
        }
    }

    private void setupBatchPublisher() {
        if(!config.isEnablePublisherBatching()) {
            return;
        }

        batchExecutor = Executors.newSingleThreadScheduledExecutor();
        batchExecutor.scheduleWithFixedDelay(this::processBatch,
            config.getPublisherBatchTimeout(),
            config.getPublisherBatchTimeout(),
            TimeUnit.MILLISECONDS);
    }

    private void setupRpcSupport() throws IOException {
        Channel channel = getChannel();

        // Declare exclusive reply queue
        replyQueueName = channel.queueDeclare().getQueue();

        // Setup consumer for replies
        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                     AMQP.BasicProperties properties, byte[] body) {
                String correlationId = properties.getCorrelationId();
                CompletableFuture<String> future = rpcCallbacks.remove(correlationId);
                if(future != null) {
                    future.complete(new String(body, StandardCharsets.UTF_8));
                }
            }
        });

        log.info("RPC support enabled with reply queue: {}", replyQueueName);
    }

    // Send method for RabbitMQ messages
    public CompletableFuture<MessageDTO> send(MessageDTO message) {
        CompletableFuture<MessageDTO> future = new CompletableFuture<>();

        try {
            // Parse message content
            JsonNode messageContent = objectMapper.readTree(message.getContent());
            String body = messageContent.path("body").asText();
            JsonNode properties = messageContent.path("properties");

            // Build AMQP properties
            AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();

            // Set delivery mode
            propsBuilder.deliveryMode(config.getDeliveryMode().getMode());

            // Set properties from message
            if(properties.has("contentType"))
                propsBuilder.contentType(properties.get("contentType").asText());
            if(properties.has("contentEncoding"))
                propsBuilder.contentEncoding(properties.get("contentEncoding").asText());
            if(properties.has("correlationId"))
                propsBuilder.correlationId(properties.get("correlationId").asText());
            if(properties.has("replyTo"))
                propsBuilder.replyTo(properties.get("replyTo").asText());
            if(properties.has("expiration"))
                propsBuilder.expiration(properties.get("expiration").asText());
            if(properties.has("messageId"))
                propsBuilder.messageId(properties.get("messageId").asText());
            if(properties.has("type"))
                propsBuilder.type(properties.get("type").asText());
            if(properties.has("userId"))
                propsBuilder.userId(properties.get("userId").asText());
            if(properties.has("appId"))
                propsBuilder.appId(properties.get("appId").asText());

            // Set timestamp
            propsBuilder.timestamp(new Date());

            // Set priority
            if(config.isEnablePriorityQueues() && properties.has("priority")) {
                propsBuilder.priority(properties.get("priority").asInt(config.getPriority()));
            } else {
                propsBuilder.priority(config.getPriority());
            }

            // Set headers
            Map<String, Object> headers = new HashMap<>();
            if(properties.has("headers") && properties.get("headers").isObject()) {
                properties.get("headers").fields().forEachRemaining(entry -> {
                    headers.put(entry.getKey(), entry.getValue().asText());
                });
            }

            // Add tracing headers if enabled
            if(config.isEnableMessageTracing()) {
                headers.put(Headers.TRACE_ID, UUID.randomUUID().toString());
                headers.put(Headers.SPAN_ID, UUID.randomUUID().toString());
            }

            propsBuilder.headers(headers);

            AMQP.BasicProperties finalProps = propsBuilder.build();

            // Determine routing
            String exchange = message.getHeaders() != null ? (String) message.getHeaders().getOrDefault("exchange", config.getExchangeName()) : config.getExchangeName();
            String routingKey = message.getHeaders() != null ? (String) message.getHeaders().getOrDefault("routingKey", config.getRoutingKey()) : config.getRoutingKey();

            // Handle batching
            if(config.isEnablePublisherBatching()) {
                publishQueue.offer(new PublishRequest(exchange, routingKey, finalProps,
                    body.getBytes(StandardCharsets.UTF_8), future, message));
            } else {
                // Direct publish
                publishMessage(exchange, routingKey, finalProps, body.getBytes(StandardCharsets.UTF_8),
                    future, message);
            }

            messagesSent.incrementAndGet();

        } catch(Exception e) {
            messagesFailed.incrementAndGet();
            future.completeExceptionally(e);
            log.error("Failed to send message", e);
        }

        return future;
    }

    private void publishMessage(String exchange, String routingKey, AMQP.BasicProperties properties,
                              byte[] body, CompletableFuture<MessageDTO> future, MessageDTO originalMessage) {
        try {
            Channel channel = getChannel();

            if(config.isPublisherConfirms()) {
                long seqNo = channel.getNextPublishSeqNo();
                pendingConfirms.put(seqNo, new PendingConfirm(future, originalMessage));
            }

            channel.basicPublish(
                exchange != null ? exchange : "",
                routingKey != null ? routingKey : "",
                config.isMandatory(),
                config.isImmediate(),
                properties,
                body
           );

            if(config.isEnableTransactions()) {
                channel.txCommit();
            }

            if(!config.isPublisherConfirms()) {
                // Complete immediately if confirms are disabled
                future.complete(originalMessage);
            }

        } catch(Exception e) {
            future.completeExceptionally(e);
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    private void processBatch() {
        if(publishQueue.isEmpty()) {
            return;
        }

        List<PublishRequest> batch = new ArrayList<>();
        publishQueue.drainTo(batch, config.getPublisherBatchSize());

        if(batch.isEmpty()) {
            return;
        }

        try {
            Channel channel = getChannel();

            for(PublishRequest request : batch) {
                publishMessage(request.exchange, request.routingKey, request.properties,
                    request.body, request.future, request.originalMessage);
            }

            log.debug("Published batch of {} messages", batch.size());

        } catch(Exception e) {
            log.error("Failed to publish batch", e);
            batch.forEach(req -> req.future.completeExceptionally(e));
        }
    }

    // RPC - style request/response
    public CompletableFuture<String> call(String exchange, String routingKey, String message, long timeout) {
        if(!config.isEnableRpcPattern()) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new AdapterException("RPC pattern is not enabled in configuration"));
            return future;
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        String correlationId = UUID.randomUUID().toString();

        try {
            Channel channel = getChannel();

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .replyTo(replyQueueName)
                .deliveryMode(1) // Non - persistent for RPC
                .expiration(String.valueOf(timeout))
                .build();

            rpcCallbacks.put(correlationId, future);

            channel.basicPublish(exchange, routingKey, props, message.getBytes(StandardCharsets.UTF_8));

            // Setup timeout
            CompletableFuture.delayedExecutor(timeout, TimeUnit.MILLISECONDS).execute(() -> {
                if(rpcCallbacks.remove(correlationId) != null) {
                    future.completeExceptionally(new TimeoutException("RPC call timed out"));
                }
            });

        } catch(Exception e) {
            rpcCallbacks.remove(correlationId);
            future.completeExceptionally(e);
        }

        return future;
    }

    // Management API operations
    public Map<String, Object> getQueueInfo(String queueName) throws IOException {
        // Return empty map when management API is not enabled
        // This would normally make HTTP calls to RabbitMQ Management API
        // Implementation would depend on HTTP client library
        Map<String, Object> info = new HashMap<>();
        info.put("queueName", queueName);
        info.put("status", "Management API not configured");
        return info;
    }

    public void purgeQueue(String queueName) throws IOException {
        Channel channel = getChannel();
        channel.queuePurge(queueName);
        log.info("Purged queue: {}", queueName);
    }

    public void deleteQueue(String queueName) throws IOException {
        Channel channel = getChannel();
        channel.queueDelete(queueName);
        log.info("Deleted queue: {}", queueName);
    }

    // Remove duplicate testConnection - it's now in doTestConnection()

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("messagesSent", messagesSent.get());
        metrics.put("messagesConfirmed", messagesConfirmed.get());
        metrics.put("messagesReturned", messagesReturned.get());
        metrics.put("messagesFailed", messagesFailed.get());
        metrics.put("isConnected", isConnected.get());
        metrics.put("channelPoolSize", channelPool.size());
        metrics.put("pendingConfirms", pendingConfirms.size());
        metrics.put("publishQueueSize", publishQueue.size());

        return metrics;
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down RabbitMQ Outbound Adapter");

        // Shutdown batch executor
        if(batchExecutor != null) {
            batchExecutor.shutdown();
            try {
                if(!batchExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    batchExecutor.shutdownNow();
                }
            } catch(InterruptedException e) {
                batchExecutor.shutdownNow();
            }
        }

        // Process remaining messages
        processBatch();

        // Wait for pending confirms
        if(config.isPublisherConfirms() && !pendingConfirms.isEmpty()) {
            try {
                Thread.sleep(config.getConfirmWaitTime()); // Give confirms time to arrive
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Close channels
        channelPool.values().forEach(channel -> {
            try {
                if(channel.isOpen()) {
                    channel.close();
                }
            } catch(Exception e) {
                log.error("Failed to close channel", e);
            }
        });

        // Close connection
        if(connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch(Exception e) {
                log.error("Failed to close connection", e);
            }
        }

        log.info("RabbitMQ Outbound Adapter shut down successfully");
    }

    // Helper classes
    private static class PendingConfirm {
        final CompletableFuture<MessageDTO> future;
        final MessageDTO originalMessage;

        PendingConfirm(CompletableFuture<MessageDTO> future, MessageDTO originalMessage) {
            this.future = future;
            this.originalMessage = originalMessage;
        }
    }

    private static class PublishRequest {
        final String exchange;
        final String routingKey;
        final AMQP.BasicProperties properties;
        final byte[] body;
        final CompletableFuture<MessageDTO> future;
        final MessageDTO originalMessage;

        PublishRequest(String exchange, String routingKey, AMQP.BasicProperties properties,
                      byte[] body, CompletableFuture<MessageDTO> future, MessageDTO originalMessage) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.properties = properties;
            this.body = body;
            this.future = future;
            this.originalMessage = originalMessage;
        }
    }
}
