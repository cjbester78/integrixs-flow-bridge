package com.integrixs.adapters.messaging.rabbitmq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.messaging.rabbitmq.RabbitMQConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import java.time.LocalDateTime;
import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// @Component  // REMOVED - was causing auto-initialization
public class RabbitMQInboundAdapter extends AbstractInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQInboundAdapter.class);

    public RabbitMQInboundAdapter() {
        super(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.RABBITMQ);
    }

    @Autowired
    private RabbitMQConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private final Map<String, Channel> channelPool = new ConcurrentHashMap<>();
    private final ExecutorService consumerExecutor = Executors.newCachedThreadPool();
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isConsuming = new AtomicBoolean(false);

    // Metrics
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong messagesAcknowledged = new AtomicLong(0);
    private final AtomicLong messagesRejected = new AtomicLong(0);
    private final AtomicLong connectionFailures = new AtomicLong(0);

    // Consumer management
    private String consumerTag;
    private final Set<String> activeConsumers = ConcurrentHashMap.newKeySet();

    // Required abstract methods from AbstractInboundAdapter
    @Override
    protected void doSenderInitialize() throws Exception {
        setupConnectionFactory();
        establishConnection();
        declareExchangeAndQueue();
        startConsuming();
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        destroy();
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // This is an inbound adapter - it receives messages from RabbitMQ
        log.debug("Inbound adapter does not send data");
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
    public String getConfigurationSummary() {
        return String.format("RabbitMQ Inbound: %s:%d/%s",
            config.getHost(), config.getPort(), config.getQueueName());
    }

    @PostConstruct
    public void initialize() {
        try {
            setupConnectionFactory();
            establishConnection();
            declareExchangeAndQueue();
            startConsuming();
            // isInitialized managed by parent class
            log.info("RabbitMQ Inbound Adapter initialized successfully");
        } catch(Exception e) {
            log.error("Failed to initialize RabbitMQ Inbound Adapter", e);
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
        connectionFactory.setNetworkRecoveryInterval((int) config.getNetworkRecoveryInterval());

        // Connection timeout
        connectionFactory.setConnectionTimeout(config.getConnectionTimeout());
        connectionFactory.setRequestedHeartbeat(config.getRequestedHeartbeat());

        // Set connection name for monitoring
        Map<String, Object> clientProperties = new HashMap<>();
        clientProperties.put("connection_name", "IntegrixsFlowBridge_Inbound_" + UUID.randomUUID());
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
                // Cluster connection
                Address[] addresses = Arrays.stream(config.getClusterAddresses())
                    .map(addr -> {
                        String[] parts = addr.split(":");
                        return new Address(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : config.getPort());
                    })
                    .toArray(Address[]::new);
                connection = connectionFactory.newConnection(addresses);
            } else {
                // Single node connection
                connection = connectionFactory.newConnection();
            }

            isConnected.set(true);
            log.info("Successfully connected to RabbitMQ");

            // Add connection listener
            ((Recoverable) connection).addRecoveryListener(new RecoveryListener() {
                @Override
                public void handleRecovery(Recoverable recoverable) {
                    log.info("RabbitMQ connection recovered");
                    isConnected.set(true);
                }

                @Override
                public void handleRecoveryStarted(Recoverable recoverable) {
                    log.info("RabbitMQ connection recovery started");
                    isConnected.set(false);
                }
            });

        } catch(Exception e) {
            connectionFailures.incrementAndGet();
            throw new RuntimeException("Failed to connect to RabbitMQ", e);
        }
    }

    private Channel createChannel() throws IOException {
        Channel channel = connection.createChannel();

        try {
            // Configure channel
            if(config.getPrefetchCount() > 0) {
                channel.basicQos(config.getPrefetchCount());
            }

            // Add channel listeners
            channel.addReturnListener(returnMessage -> {
                log.warn("Message returned: exchange = {}, routingKey = {}, replyText = {}",
                    returnMessage.getExchange(),
                    returnMessage.getRoutingKey(),
                    returnMessage.getReplyText());
            });

            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) {
                    log.debug("Message confirmed: deliveryTag = {}, multiple = {}", deliveryTag, multiple);
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) {
                    log.warn("Message not confirmed: deliveryTag = {}, multiple = {}", deliveryTag, multiple);
                }
            });

            return channel;
        } catch(Exception e) {
            log.error("Failed to create channel", e);
            throw new IOException("Failed to create channel", e);
        }
    }

    private Channel getChannel() throws IOException {
        String channelKey = "default";
        Channel channel = channelPool.get(channelKey);

        if(channel == null || !channel.isOpen()) {
            channel = createChannel();
            channelPool.put(channelKey, channel);
        }

        return channel;
    }

    private void declareExchangeAndQueue() throws IOException {
        Channel channel = getChannel();

        try {
            // Declare exchange if specified
            if(config.getExchangeName() != null && !config.getExchangeName().isEmpty()) {
                channel.exchangeDeclare(
                    config.getExchangeName(),
                    config.getExchangeType().getType(),
                    config.isExchangeDurable(),
                    config.isExchangeAutoDelete(),
                    config.getExchangeArguments()
               );
                log.info("Declared exchange: {}", config.getExchangeName());
            }

            // Configure queue arguments
            Map<String, Object> queueArgs = new HashMap<>(config.getQueueArguments());

            // Dead letter configuration
            if(config.isEnableDeadLetterExchange()) {
                if(config.getDeadLetterExchangeName() != null) {
                    queueArgs.put(QueueArguments.DEAD_LETTER_EXCHANGE, config.getDeadLetterExchangeName());
                }
                if(config.getDeadLetterRoutingKey() != null) {
                    queueArgs.put(QueueArguments.DEAD_LETTER_ROUTING_KEY, config.getDeadLetterRoutingKey());
                }
            }

            // TTL configuration
            if(config.getTtl() > 0) {
                queueArgs.put(QueueArguments.MESSAGE_TTL, config.getTtl());
            }

            // Priority queue configuration
            if(config.isEnablePriorityQueues()) {
                queueArgs.put(QueueArguments.MAX_PRIORITY, config.getMaxPriority());
            }

            // Declare queue
            if(config.getQueueName() != null && !config.getQueueName().isEmpty()) {
                channel.queueDeclare(
                    config.getQueueName(),
                    config.isQueueDurable(),
                    config.isQueueExclusive(),
                    config.isQueueAutoDelete(),
                    queueArgs
               );
                log.info("Declared queue: {}", config.getQueueName());

                // Bind queue to exchange
                if(config.getExchangeName() != null && !config.getExchangeName().isEmpty()) {
                    channel.queueBind(
                        config.getQueueName(),
                        config.getExchangeName(),
                        config.getBindingKey() != null ? config.getBindingKey() : ""
                   );
                    log.info("Bound queue {} to exchange {} with key {}",
                        config.getQueueName(), config.getExchangeName(), config.getBindingKey());
                }
            }
        } catch(Exception e) {
            log.error("Failed to declare exchange/queue", e);
            throw new RuntimeException("Failed to setup RabbitMQ topology", e);
        }
    }

    private void startConsuming() throws IOException {
        if(!isConnected.get() || config.getQueueName() == null || config.getQueueName().isEmpty()) {
            log.warn("Cannot start consuming: not connected or no queue specified");
            return;
        }

        Channel channel = getChannel();

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
                messagesReceived.incrementAndGet();

                consumerExecutor.execute(() -> {
                    try {
                        processMessage(channel, envelope, properties, body);
                    } catch(Exception e) {
                        log.error("Error processing message", e);
                        handleMessageError(channel, envelope, e);
                    }
                });
            }

            @Override
            public void handleCancel(String consumerTag) throws IOException {
                log.warn("Consumer cancelled: {}", consumerTag);
                activeConsumers.remove(consumerTag);
                isConsuming.set(false);
            }

            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                log.warn("Consumer shutdown: {}", consumerTag, sig);
                activeConsumers.remove(consumerTag);
                isConsuming.set(false);
            }
        };

        // Start consuming
        String tag = config.getConsumerTag() != null ? config.getConsumerTag() : "";
        consumerTag = channel.basicConsume(
            config.getQueueName(),
            config.isAutoAck(),
            tag,
            config.isNoLocal(),
            config.isExclusive(),
            null,
            consumer
       );

        activeConsumers.add(consumerTag);
        isConsuming.set(true);
        log.info("Started consuming from queue: {} with consumer tag: {}", config.getQueueName(), consumerTag);
    }

    private void processMessage(Channel channel, Envelope envelope,
                              AMQP.BasicProperties properties, byte[] body) throws IOException {

        String messageId = properties.getMessageId() != null ? properties.getMessageId() : UUID.randomUUID().toString();

        try {
            // Build message content
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("body", new String(body, StandardCharsets.UTF_8));
            messageContent.put("exchange", envelope.getExchange());
            messageContent.put("routingKey", envelope.getRoutingKey());
            messageContent.put("deliveryTag", envelope.getDeliveryTag());
            messageContent.put("redelivered", envelope.isRedeliver());

            // Add properties
            Map<String, Object> props = new HashMap<>();
            if(properties.getContentType() != null) props.put("contentType", properties.getContentType());
            if(properties.getContentEncoding() != null) props.put("contentEncoding", properties.getContentEncoding());
            if(properties.getCorrelationId() != null) props.put("correlationId", properties.getCorrelationId());
            if(properties.getReplyTo() != null) props.put("replyTo", properties.getReplyTo());
            if(properties.getExpiration() != null) props.put("expiration", properties.getExpiration());
            if(properties.getType() != null) props.put("type", properties.getType());
            if(properties.getUserId() != null) props.put("userId", properties.getUserId());
            if(properties.getAppId() != null) props.put("appId", properties.getAppId());
            if(properties.getTimestamp() != null) props.put("timestamp", properties.getTimestamp().getTime());
            if(properties.getPriority() != null) props.put("priority", properties.getPriority());
            if(properties.getDeliveryMode() != null) props.put("deliveryMode", properties.getDeliveryMode());

            // Add headers
            if(properties.getHeaders() != null) {
                props.put("headers", properties.getHeaders());
            }

            messageContent.put("properties", props);

            // Create MessageDTO
            MessageDTO message = new MessageDTO();
            message.setId(messageId);
            message.setType("rabbitmq_message");
            message.setSource(String.format("rabbitmq://%s:%d%s/%s",
                config.getHost(), config.getPort(), config.getVirtualHost(), config.getQueueName()));
            message.setTarget(config.getQueueName());
            message.setPayload(objectMapper.writeValueAsString(messageContent));
            message.setTimestamp(LocalDateTime.now());

            Map<String, String> metadata = new HashMap<>();
            metadata.put("exchange", envelope.getExchange());
            metadata.put("routingKey", envelope.getRoutingKey());
            metadata.put("queueName", config.getQueueName());
            message.setHeaders(new HashMap<>(metadata));

            // Publish to internal queue
            // Process the message - implementation needed
            processIncomingMessage(message);

            // Acknowledge message if not auto - ack
            if(!config.isAutoAck()) {
                channel.basicAck(envelope.getDeliveryTag(), false);
                messagesAcknowledged.incrementAndGet();
            }

        } catch(Exception e) {
            log.error("Failed to process message: {}", messageId, e);
            throw e;
        }
    }

    private void handleMessageError(Channel channel, Envelope envelope, Exception error) {
        try {
            if(!config.isAutoAck() && channel.isOpen()) {
                // Get retry count from message headers
                int retryCount = 0;
                // This would need to be extracted from message headers

                if(retryCount < config.getMaxRetries()) {
                    // Requeue for retry
                    channel.basicReject(envelope.getDeliveryTag(), true);
                    log.warn("Message requeued for retry: deliveryTag = {}", envelope.getDeliveryTag());
                } else {
                    // Reject and don't requeue(will go to DLX if configured)
                    channel.basicReject(envelope.getDeliveryTag(), false);
                    messagesRejected.incrementAndGet();
                    log.error("Message rejected after {} retries: deliveryTag = {}",
                        config.getMaxRetries(), envelope.getDeliveryTag());
                }
            }
        } catch(Exception e) {
            log.error("Failed to handle message error", e);
        }
    }

    public void pause() {
        try {
            if(isConsuming.get() && consumerTag != null) {
                Channel channel = getChannel();
                channel.basicCancel(consumerTag);
                isConsuming.set(false);
                log.info("Paused RabbitMQ consumer");
            }
        } catch(Exception e) {
            log.error("Failed to pause consumer", e);
        }
    }

    public void resume() {
        try {
            if(!isConsuming.get() && isConnected.get()) {
                startConsuming();
                log.info("Resumed RabbitMQ consumer");
            }
        } catch(Exception e) {
            log.error("Failed to resume consumer", e);
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("messagesReceived", messagesReceived.get());
        metrics.put("messagesAcknowledged", messagesAcknowledged.get());
        metrics.put("messagesRejected", messagesRejected.get());
        metrics.put("connectionFailures", connectionFailures.get());
        metrics.put("isConnected", isConnected.get());
        metrics.put("isConsuming", isConsuming.get());
        metrics.put("activeConsumers", activeConsumers.size());
        metrics.put("channelPoolSize", channelPool.size());

        // Add connection metrics
        if(connection != null && connection.isOpen()) {
            metrics.put("channelMax", connection.getChannelMax());
            metrics.put("frameMax", connection.getFrameMax());
            metrics.put("heartbeat", connection.getHeartbeat());
        }

        return metrics;
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down RabbitMQ Inbound Adapter");

        // Cancel consumers
        activeConsumers.forEach(tag -> {
            try {
                Channel channel = getChannel();
                channel.basicCancel(tag);
            } catch(Exception e) {
                log.error("Failed to cancel consumer: {}", tag, e);
            }
        });

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

        // Shutdown executor
        consumerExecutor.shutdown();
        try {
            if(!consumerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                consumerExecutor.shutdownNow();
            }
        } catch(InterruptedException e) {
            consumerExecutor.shutdownNow();
        }

        log.info("RabbitMQ Inbound Adapter shut down successfully");
    }

    private void processIncomingMessage(MessageDTO message) {
        // Implementation to process incoming messages
        // This would typically publish to an internal queue or process directly
        log.debug("Processing incoming RabbitMQ message: {}", message.getId());
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        // RabbitMQ Inbound adapter doesn't send - it receives messages
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }
}
