package com.integrixs.adapters.messaging.amqp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.messaging.amqp.AMQPConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.jms.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import com.integrixs.adapters.domain.model.AdapterConfiguration;

public class AMQPInboundAdapter extends AbstractInboundAdapter implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(AMQPInboundAdapter.class);

    public AMQPInboundAdapter() {
        super(AdapterConfiguration.AdapterTypeEnum.AMQP);
    }

    @Autowired
    private AMQPConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    private JmsConnectionFactory connectionFactory;
    private JmsConnection connection;
    private Session session;
    private MessageConsumer consumer;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    // Metrics
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong messagesProcessed = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);
    private final AtomicLong connectionFailures = new AtomicLong(0);

    // Message processing
    private final ExecutorService messageProcessor = Executors.newCachedThreadPool();
    private final Map<String, Instant> messageDeduplication = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    // AbstractInboundAdapter required methods
    @Override
    protected void doSenderInitialize() throws Exception {
        setupConnectionFactory();
        establishConnection();
        createConsumer();
        startMessageDeduplicationCleanup();
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        destroy();
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // This is an inbound adapter - it receives messages, not sends them
        // But the interface requires this method
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        if (connection != null && isConnected.get()) {
            return AdapterResult.success(null, "AMQP connection is active");
        }

        try {
            establishConnection();
            return AdapterResult.success(null, "Successfully connected to AMQP broker");
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("AMQP Inbound: %s:%d/%s",
            config.getHost(), config.getPort(), config.getSourceAddress());
    }

    @PostConstruct
    public void initialize() {
        try {
            doInitialize();
            log.info("AMQP Inbound Adapter initialized successfully");
        } catch(Exception e) {
            log.error("Failed to initialize AMQP Inbound Adapter", e);
            throw new RuntimeException("Failed to initialize AMQP adapter", e);
        }
    }

    private void setupConnectionFactory() {
        // Build connection URL
        String connectionUrl = buildConnectionUrl();

        // Create connection factory
        connectionFactory = new JmsConnectionFactory(connectionUrl);

        if (config.getUsername() != null) {
            connectionFactory.setUsername(config.getUsername());
        }

        if (config.getPassword() != null) {
            connectionFactory.setPassword(config.getPassword());
        }
    }

    private String buildConnectionUrl() {
        if(config.getConnectionUrl() != null && !config.getConnectionUrl().isEmpty()) {
            return config.getConnectionUrl();
        }

        StringBuilder urlBuilder = new StringBuilder();

        // Protocol
        if(config.isUseSsl()) {
            urlBuilder.append("amqps://");
        } else {
            urlBuilder.append("amqp://");
        }

        // Host and port
        urlBuilder.append(config.getHost()).append(":").append(config.getPort());

        // Connection options
        List<String> options = new ArrayList<>();

        if(config.getSaslMechanism() != null) {
            options.add("amqp.saslMechanisms=" + config.getSaslMechanism().getMechanism());
        }

        if(config.getIdleTimeout() > 0) {
            options.add("amqp.idleTimeout=" + config.getIdleTimeout());
        }

        if(config.getMaxFrameSize() > 0) {
            options.add("amqp.maxFrameSize=" + config.getMaxFrameSize());
        }

        if(config.isEnableTracing()) {
            options.add("amqp.traceFrames=true");
        }

        if(!options.isEmpty()) {
            urlBuilder.append("?").append(String.join("&", options));
        }

        return urlBuilder.toString();
    }

    private void establishConnection() throws JMSException {
        try {
            connection = (JmsConnection) connectionFactory.createConnection();

            // Set exception listener
            connection.setExceptionListener(exception -> {
                log.error("AMQP connection exception", exception);
                connectionFailures.incrementAndGet();
                isConnected.set(false);

                // Attempt reconnection
                if(config.isEnableAutoReconnect()) {
                    scheduleReconnection();
                }
            });

            connection.start();
            isConnected.set(true);

            log.info("Successfully connected to AMQP broker");
        } catch(JMSException e) {
            connectionFailures.incrementAndGet();
            throw new RuntimeException("Failed to connect to AMQP broker", e);
        }
    }

    private void createConsumer() throws JMSException {
        if(session == null) {
            createSession();
        }

        Destination destination = createDestination();

        // Create consumer with message selector if configured
        // Message selector from buildMessageSelector
        String messageSelector = buildMessageSelector();
        consumer = (messageSelector != null && !messageSelector.isEmpty()) ?
                session.createConsumer(destination, messageSelector) :
                session.createConsumer(destination);

        // Set message listener
        consumer.setMessageListener(this);

        log.info("Created AMQP consumer for: {}", config.getSourceAddress());
    }

    private Destination createDestination() throws JMSException {
        String address = config.getSourceAddress();

        if(address == null || address.isEmpty()) {
            throw new RuntimeException("Source address not configured");
        }

        // Handle broker - specific addressing
        switch(config.getBrokerType()) {
            case ACTIVEMQ_ARTEMIS:
                if(config.getRoutingType().equals("multicast")) {
                    return session.createTopic(address);
                } else {
                    return session.createQueue(address);
                }

            case AZURE_SERVICE_BUS:
                // Azure Service Bus specific addressing
                String entityPath = config.getEntityPath();
                if(entityPath != null) {
                    address = entityPath;
                }
                return session.createQueue(address);

            default:
                // Generic AMQP addressing
                if(address.startsWith("topic://")) {
                    return session.createTopic(address.substring(8));
                } else if(address.startsWith("queue://")) {
                    return session.createQueue(address.substring(8));
                } else {
                    return session.createQueue(address);
                }
        }
    }

    private String buildMessageSelector() {
        if(config.getSourceFilters() == null || config.getSourceFilters().isEmpty()) {
            return null;
        }

        // Build JMS message selector from filters
        List<String> selectors = new ArrayList<>();

        config.getSourceFilters().forEach((key, value) -> {
            selectors.add(String.format("%s = '%s'", key, value));
        });

        return String.join(" AND ", selectors);
    }

    private void createSession() throws JMSException {
        if(connection == null) {
            throw new jakarta.jms.IllegalStateException("Connection not established");
        }

        // Create session based on configuration
        session = connection.createSession(
            config.isEnableTransactions(),
            Session.AUTO_ACKNOWLEDGE // Always use auto-ack for now
       );
    }

    @Override
    public void onMessage(jakarta.jms.Message jmsMessage) {
        messagesReceived.incrementAndGet();

        // Process asynchronously
        messageProcessor.submit(() -> {
            try {
                processMessage(jmsMessage);
                messagesProcessed.incrementAndGet();
            } catch(Exception e) {
                messagesFailed.incrementAndGet();
                handleMessageError(jmsMessage, e);
            }
        });
    }

    private void processMessage(jakarta.jms.Message jmsMessage) throws JMSException {
        String messageId = jmsMessage.getJMSMessageID();

        // Duplicate detection
        if(config.isEnableDuplicateDetection()) {
            if(messageDeduplication.putIfAbsent(messageId, Instant.now()) != null) {
                log.debug("Duplicate message detected: {}", messageId);
                return;
            }
        }

        try {
            // Extract message content
            Map<String, Object> messageContent = new HashMap<>();

            // Get message body
            String body = extractMessageBody(jmsMessage);
            messageContent.put("body", body);

            // Extract properties
            Map<String, Object> properties = extractMessageProperties(jmsMessage);
            messageContent.put("properties", properties);

            // Extract headers
            Map<String, Object> headers = extractMessageHeaders(jmsMessage);
            messageContent.put("headers", headers);

            // Add AMQP specific info
            messageContent.put("messageType", jmsMessage.getClass().getSimpleName());
            messageContent.put("destination", jmsMessage.getJMSDestination().toString());
            messageContent.put("deliveryMode", jmsMessage.getJMSDeliveryMode());
            messageContent.put("priority", jmsMessage.getJMSPriority());
            messageContent.put("redelivered", jmsMessage.getJMSRedelivered());

            // Create MessageDTO
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(messageId);
            // Set message type if needed
            // message.setMessageType("amqp_message");
            message.setSource(String.format("amqp://%s:%d/%s",
                config.getHost(), config.getPort(), config.getSourceAddress()));
            message.setTarget(config.getSourceAddress());
            message.setPayload(objectMapper.writeValueAsString(messageContent));
            message.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(jmsMessage.getJMSTimestamp()), java.time.ZoneOffset.UTC));

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("amqpVersion", config.getVersion().getVersion());
            metadata.put("brokerType", config.getBrokerType().name());
            metadata.put("sourceAddress", config.getSourceAddress());
            message.setHeaders(metadata);

            // Publish to internal queue
            // Process the message - implementation needed
            processIncomingMessage(message);

            // Commit transaction if enabled
            if(config.isEnableTransactions()) {
                session.commit();
            }

        } catch(Exception e) {
            // Rollback transaction if enabled
            if(config.isEnableTransactions()) {
                try {
                    session.rollback();
                } catch(JMSException ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            throw new RuntimeException("Failed to process AMQP message", e);
        }
    }

    private String extractMessageBody(jakarta.jms.Message message) throws JMSException {
        if(message instanceof TextMessage) {
            return((TextMessage) message).getText();
        } else if(message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            return new String(bytes);
        } else if(message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            Map<String, Object> map = new HashMap<>();
            Enumeration<String> names = mapMessage.getMapNames();
            while(names.hasMoreElements()) {
                String name = names.nextElement();
                map.put(name, mapMessage.getObject(name));
            }
            try {
                return objectMapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize map message", e);
                return map.toString();
            }
        } else if(message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                return objectMapper.writeValueAsString(objectMessage.getObject());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize object message", e);
                return objectMessage.getObject().toString();
            }
        } else if(message instanceof StreamMessage) {
            // Handle stream message
            return "StreamMessage[" + message.getJMSMessageID() + "]";
        }

        return "";
    }

    private Map<String, Object> extractMessageProperties(jakarta.jms.Message message) throws JMSException {
        Map<String, Object> properties = new HashMap<>();

        // Standard JMS properties
        properties.put("messageId", message.getJMSMessageID());
        properties.put("correlationId", message.getJMSCorrelationID());
        properties.put("replyTo", message.getJMSReplyTo() != null ? message.getJMSReplyTo().toString() : null);
        properties.put("type", message.getJMSType());
        properties.put("timestamp", message.getJMSTimestamp());
        properties.put("expiration", message.getJMSExpiration());

        // AMQP specific properties if available
        // AMQP specific properties
        try {

            properties.put("userId", message.getStringProperty("JMSXUserID"));
            properties.put("groupId", message.getStringProperty("JMSXGroupID"));
            properties.put("groupSequence", message.getIntProperty("JMSXGroupSeq"));
        } catch (Exception e) {
            // Ignore if properties not available
        }

        return properties;
    }

    private Map<String, Object> extractMessageHeaders(jakarta.jms.Message message) throws JMSException {
        Map<String, Object> headers = new HashMap<>();

        Enumeration<String> propertyNames = message.getPropertyNames();
        while(propertyNames.hasMoreElements()) {
            String name = propertyNames.nextElement();
            Object value = message.getObjectProperty(name);
            headers.put(name, value);
        }

        return headers;
    }

    private void handleMessageError(jakarta.jms.Message message, Exception error) {
        try {
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");

            if(deliveryCount < config.getMaxRetries()) {
                // Redelivery will be handled by broker
                log.warn("Message processing failed, attempt {}/ {}", deliveryCount, config.getMaxRetries());

                if(!config.isEnableTransactions()) {
                    // Don't acknowledge - message will be redelivered
                    session.recover();
                }
            } else {
                log.error("Message processing failed after {} attempts", config.getMaxRetries());

                // Send to dead letter if configured
                if(config.isEnableDeadLettering() && config.getDeadLetterAddress() != null) {
                    sendToDeadLetter(message);
                }

                // Acknowledge to remove from queue
                // Always acknowledge in error cases
                {
                    message.acknowledge();
                }
            }
        } catch(Exception e) {
            log.error("Failed to handle message error", e);
        }
    }

    private void sendToDeadLetter(jakarta.jms.Message message) {
        try {
            Destination deadLetter = session.createQueue(config.getDeadLetterAddress());
            MessageProducer deadLetterProducer = session.createProducer(deadLetter);

            // Add dead letter headers
            message.setStringProperty("x - original - address", config.getSourceAddress());
            message.setStringProperty("x - failure - reason", "Max retries exceeded");
            message.setLongProperty("x - failure - timestamp", System.currentTimeMillis());

            deadLetterProducer.send(message);
            deadLetterProducer.close();

            log.info("Message sent to dead letter queue: {}", message.getJMSMessageID());
        } catch(Exception e) {
            log.error("Failed to send message to dead letter queue", e);
        }
    }

    private void startMessageDeduplicationCleanup() {
        if(!config.isEnableDuplicateDetection()) {
            return;
        }

        cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                Instant cutoff = Instant.now().minusSeconds(config.getDeduplicationTtlSeconds());
                messageDeduplication.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
            } catch(Exception e) {
                log.error("Error during deduplication cleanup", e);
            }
        }, config.getCleanupIntervalSeconds(), config.getCleanupIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void scheduleReconnection() {
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            if(!isConnected.get()) {
                try {
                    log.info("Attempting to reconnect to AMQP broker");
                    destroy();
                    initialize();
                } catch(Exception e) {
                    log.error("Reconnection failed, scheduling retry", e);
                    scheduleReconnection();
                }
            }
        });
    }

    private void processIncomingMessage(MessageDTO message) {
        // Implementation to process incoming messages
        // This would typically publish to an internal queue or process directly
        log.debug("Processing incoming AMQP message: {}", message.getId());
    }

    public void pause() {
        try {
            if(connection != null) {
                connection.stop();
                log.info("Paused AMQP consumer");
            }
        } catch(Exception e) {
            log.error("Failed to pause consumer", e);
        }
    }

    public void resume() {
        try {
            if(connection != null && !isConnected.get()) {
                connection.start();
                isConnected.set(true);
                log.info("Resumed AMQP consumer");
            }
        } catch(Exception e) {
            log.error("Failed to resume consumer", e);
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("messagesReceived", messagesReceived.get());
        metrics.put("messagesProcessed", messagesProcessed.get());
        metrics.put("messagesFailed", messagesFailed.get());
        metrics.put("connectionFailures", connectionFailures.get());
        metrics.put("isConnected", isConnected.get());
        metrics.put("deduplicationCacheSize", messageDeduplication.size());

        return metrics;
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down AMQP Inbound Adapter");

        // Stop message processor
        messageProcessor.shutdown();
        try {
            if(!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch(InterruptedException e) {
            messageProcessor.shutdownNow();
        }

        // Stop cleanup executor
        cleanupExecutor.shutdown();

        // Close consumer
        if(consumer != null) {
            try {
                consumer.close();
            } catch(Exception e) {
                log.error("Failed to close consumer", e);
            }
        }

        // Close session
        if(session != null) {
            try {
                session.close();
            } catch(Exception e) {
                log.error("Failed to close session", e);
            }
        }

        // Close connection
        if(connection != null) {
            try {
                connection.close();
            } catch(Exception e) {
                log.error("Failed to close connection", e);
            }
        }

        // Clear caches
        messageDeduplication.clear();

        log.info("AMQP Inbound Adapter shut down successfully");
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        // AMQP Inbound adapter doesn't send - it receives messages
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected void doInitialize() {
        try {
            doSenderInitialize();
        } catch (Exception e) {
            log.error("Failed to initialize AMQP adapter", e);
            throw new RuntimeException("Failed to initialize AMQP adapter", e);
        }
    }

    @Override
    protected void doDestroy() {
        try {
            doSenderDestroy();
        } catch (Exception e) {
            log.error("Failed to destroy AMQP adapter", e);
        }
    }

    protected long getPollingIntervalMs() {
        // AMQP uses push model, not polling
        return 0;
    }
}
