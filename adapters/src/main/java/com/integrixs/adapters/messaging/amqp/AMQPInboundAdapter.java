package com.integrixs.adapters.messaging.amqp;

import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.messaging.amqp.AMQPConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.jms.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class AMQPInboundAdapter extends AbstractInboundAdapter implements MessageListener {
    
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
    
    @Override
    public AdapterType getType() {
        return AdapterType.AMQP;
    }
    
    @Override
    public String getName() {
        return "AMQP Inbound Adapter";
    }
    
    @PostConstruct
    public void initialize() {
        try {
            setupConnectionFactory();
            establishConnection();
            createConsumer();
            startMessageDeduplicationCleanup();
            isInitialized = true;
            log.info("AMQP Inbound Adapter initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize AMQP Inbound Adapter", e);
            throw new AdapterException("Failed to initialize AMQP adapter", e);
        }
    }
    
    private void setupConnectionFactory() {
        String connectionUrl = buildConnectionUrl();
        
        connectionFactory = new JmsConnectionFactory(connectionUrl);
        
        // Set connection properties
        connectionFactory.setUsername(config.getUsername());
        connectionFactory.setPassword(config.getPassword());
        connectionFactory.setClientID(config.getContainerId() != null ? 
            config.getContainerId() : "IntegrixsFlowBridge-" + UUID.randomUUID());
        
        // Configure connection factory properties
        connectionFactory.setReceiveLocalOnly(false);
        connectionFactory.setReceiveNoWaitLocalOnly(false);
        
        // SSL configuration
        if (config.isUseSsl()) {
            connectionFactory.setKeyStoreLocation(config.getKeyStore());
            connectionFactory.setKeyStorePassword(config.getKeyStorePassword());
            connectionFactory.setTrustStoreLocation(config.getTrustStore());
            connectionFactory.setTrustStorePassword(config.getTrustStorePassword());
            connectionFactory.setVerifyHost(config.isVerifyHost());
        }
        
        // Set AMQP properties
        connectionFactory.setAmqpOpenServerListAction("REPLACE");
        connectionFactory.setPopulateJMSXUserID(true);
        
        log.info("AMQP connection factory configured for: {}", connectionUrl);
    }
    
    private String buildConnectionUrl() {
        if (config.getConnectionUrl() != null && !config.getConnectionUrl().isEmpty()) {
            return config.getConnectionUrl();
        }
        
        StringBuilder urlBuilder = new StringBuilder();
        
        // Protocol
        if (config.isUseSsl()) {
            urlBuilder.append("amqps://");
        } else {
            urlBuilder.append("amqp://");
        }
        
        // Host and port
        urlBuilder.append(config.getHost()).append(":").append(config.getPort());
        
        // Connection options
        List<String> options = new ArrayList<>();
        
        if (config.getSaslMechanism() != null) {
            options.add("amqp.saslMechanisms=" + config.getSaslMechanism().getMechanism());
        }
        
        if (config.getIdleTimeout() > 0) {
            options.add("amqp.idleTimeout=" + config.getIdleTimeout());
        }
        
        if (config.getMaxFrameSize() > 0) {
            options.add("amqp.maxFrameSize=" + config.getMaxFrameSize());
        }
        
        if (config.getFeatures().isEnableTracing()) {
            options.add("amqp.traceFrames=true");
        }
        
        if (!options.isEmpty()) {
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
                if (config.getFeatures().isEnableAutoReconnect()) {
                    scheduleReconnection();
                }
            });
            
            connection.start();
            isConnected.set(true);
            
            log.info("Successfully connected to AMQP broker");
        } catch (JMSException e) {
            connectionFailures.incrementAndGet();
            throw new AdapterException("Failed to connect to AMQP broker", e);
        }
    }
    
    private void createConsumer() throws JMSException {
        // Create session
        boolean transacted = config.isEnableTransactions();
        int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
        
        if (!config.isAutoAck()) {
            acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
        }
        
        session = connection.createSession(transacted, acknowledgeMode);
        
        // Create destination
        Destination destination = createDestination();
        
        // Create consumer with optional selector
        String messageSelector = buildMessageSelector();
        consumer = session.createConsumer(destination, messageSelector);
        
        // Set message listener
        consumer.setMessageListener(this);
        
        log.info("Created AMQP consumer for: {}", config.getSourceAddress());
    }
    
    private Destination createDestination() throws JMSException {
        String address = config.getSourceAddress();
        
        if (address == null || address.isEmpty()) {
            throw new AdapterException("Source address not configured");
        }
        
        // Handle broker-specific addressing
        switch (config.getBrokerType()) {
            case ACTIVEMQ_ARTEMIS:
                if (config.getRoutingType().equals("multicast")) {
                    return session.createTopic(address);
                } else {
                    return session.createQueue(address);
                }
                
            case AZURE_SERVICE_BUS:
                // Azure Service Bus specific addressing
                String entityPath = config.getAzureSettings().getEntityPath();
                if (entityPath != null) {
                    address = entityPath;
                }
                return session.createQueue(address);
                
            default:
                // Generic AMQP addressing
                if (address.startsWith("topic://")) {
                    return session.createTopic(address.substring(8));
                } else if (address.startsWith("queue://")) {
                    return session.createQueue(address.substring(8));
                } else {
                    return session.createQueue(address);
                }
        }
    }
    
    private String buildMessageSelector() {
        if (config.getSourceFilters() == null || config.getSourceFilters().isEmpty()) {
            return null;
        }
        
        // Build JMS message selector from filters
        List<String> selectors = new ArrayList<>();
        
        config.getSourceFilters().forEach((key, value) -> {
            if (value instanceof String) {
                selectors.add(key + " = '" + value + "'");
            } else if (value instanceof Number) {
                selectors.add(key + " = " + value);
            } else if (value instanceof Boolean) {
                selectors.add(key + " = " + value);
            }
        });
        
        return selectors.isEmpty() ? null : String.join(" AND ", selectors);
    }
    
    @Override
    public void onMessage(javax.jms.Message jmsMessage) {
        messagesReceived.incrementAndGet();
        
        messageProcessor.execute(() -> {
            try {
                processMessage(jmsMessage);
                messagesProcessed.incrementAndGet();
                
                // Acknowledge if manual acknowledgment
                if (!config.isAutoAck() && !config.isEnableTransactions()) {
                    jmsMessage.acknowledge();
                }
                
            } catch (Exception e) {
                log.error("Error processing AMQP message", e);
                messagesFailed.incrementAndGet();
                handleMessageError(jmsMessage, e);
            }
        });
    }
    
    private void processMessage(javax.jms.Message jmsMessage) throws JMSException {
        String messageId = jmsMessage.getJMSMessageID();
        
        // Duplicate detection
        if (config.getFeatures().isEnableDuplicateDetection()) {
            if (messageDeduplication.putIfAbsent(messageId, Instant.now()) != null) {
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
            MessageDTO message = MessageDTO.builder()
                .id(messageId)
                .type("amqp_message")
                .source(String.format("amqp://%s:%d/%s", 
                    config.getHost(), config.getPort(), config.getSourceAddress()))
                .destination(getQueueName())
                .content(objectMapper.writeValueAsString(messageContent))
                .timestamp(jmsMessage.getJMSTimestamp())
                .metadata(new HashMap<String, String>() {{
                    put("amqpVersion", config.getVersion().getVersion());
                    put("brokerType", config.getBrokerType().name());
                    put("sourceAddress", config.getSourceAddress());
                }})
                .build();
            
            // Publish to internal queue
            publishToQueue(message);
            
            // Commit transaction if enabled
            if (config.isEnableTransactions()) {
                session.commit();
            }
            
        } catch (Exception e) {
            // Rollback transaction if enabled
            if (config.isEnableTransactions()) {
                try {
                    session.rollback();
                } catch (JMSException ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
            throw new AdapterException("Failed to process AMQP message", e);
        }
    }
    
    private String extractMessageBody(javax.jms.Message message) throws JMSException {
        if (message instanceof TextMessage) {
            return ((TextMessage) message).getText();
        } else if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            return new String(bytes);
        } else if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            Map<String, Object> map = new HashMap<>();
            Enumeration<String> names = mapMessage.getMapNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                map.put(name, mapMessage.getObject(name));
            }
            return objectMapper.writeValueAsString(map);
        } else if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            return objectMapper.writeValueAsString(objectMessage.getObject());
        } else if (message instanceof StreamMessage) {
            // Handle stream message
            return "StreamMessage[" + message.getJMSMessageID() + "]";
        }
        
        return "";
    }
    
    private Map<String, Object> extractMessageProperties(javax.jms.Message message) throws JMSException {
        Map<String, Object> properties = new HashMap<>();
        
        // Standard JMS properties
        properties.put("messageId", message.getJMSMessageID());
        properties.put("correlationId", message.getJMSCorrelationID());
        properties.put("replyTo", message.getJMSReplyTo() != null ? message.getJMSReplyTo().toString() : null);
        properties.put("type", message.getJMSType());
        properties.put("timestamp", message.getJMSTimestamp());
        properties.put("expiration", message.getJMSExpiration());
        
        // AMQP specific properties if available
        if (message instanceof JmsMessage) {
            JmsMessage amqpMessage = (JmsMessage) message;
            
            // Access AMQP properties through vendor-specific methods
            properties.put("userId", message.getStringProperty("JMSXUserID"));
            properties.put("groupId", message.getStringProperty("JMSXGroupID"));
            properties.put("groupSequence", message.getIntProperty("JMSXGroupSeq"));
        }
        
        return properties;
    }
    
    private Map<String, Object> extractMessageHeaders(javax.jms.Message message) throws JMSException {
        Map<String, Object> headers = new HashMap<>();
        
        Enumeration<String> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = propertyNames.nextElement();
            Object value = message.getObjectProperty(name);
            headers.put(name, value);
        }
        
        return headers;
    }
    
    private void handleMessageError(javax.jms.Message message, Exception error) {
        try {
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            
            if (deliveryCount < config.getMaxRetries()) {
                // Redelivery will be handled by broker
                log.warn("Message processing failed, attempt {}/{}", deliveryCount, config.getMaxRetries());
                
                if (!config.isAutoAck() && !config.isEnableTransactions()) {
                    // Don't acknowledge - message will be redelivered
                    session.recover();
                }
            } else {
                log.error("Message processing failed after {} attempts", config.getMaxRetries());
                
                // Send to dead letter if configured
                if (config.isEnableDeadLettering() && config.getDeadLetterAddress() != null) {
                    sendToDeadLetter(message);
                }
                
                // Acknowledge to remove from queue
                if (!config.isAutoAck()) {
                    message.acknowledge();
                }
            }
        } catch (Exception e) {
            log.error("Failed to handle message error", e);
        }
    }
    
    private void sendToDeadLetter(javax.jms.Message message) {
        try {
            Destination deadLetter = session.createQueue(config.getDeadLetterAddress());
            MessageProducer deadLetterProducer = session.createProducer(deadLetter);
            
            // Add dead letter headers
            message.setStringProperty("x-original-address", config.getSourceAddress());
            message.setStringProperty("x-failure-reason", "Max retries exceeded");
            message.setLongProperty("x-failure-timestamp", System.currentTimeMillis());
            
            deadLetterProducer.send(message);
            deadLetterProducer.close();
            
            log.info("Message sent to dead letter queue: {}", message.getJMSMessageID());
        } catch (Exception e) {
            log.error("Failed to send message to dead letter queue", e);
        }
    }
    
    private void startMessageDeduplicationCleanup() {
        if (!config.getFeatures().isEnableDuplicateDetection()) {
            return;
        }
        
        cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                Instant cutoff = Instant.now().minusSeconds(3600); // 1 hour
                messageDeduplication.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
            } catch (Exception e) {
                log.error("Error during deduplication cleanup", e);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }
    
    private void scheduleReconnection() {
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            if (!isConnected.get()) {
                try {
                    log.info("Attempting to reconnect to AMQP broker");
                    destroy();
                    initialize();
                } catch (Exception e) {
                    log.error("Reconnection failed, scheduling retry", e);
                    scheduleReconnection();
                }
            }
        });
    }
    
    @Override
    public void pause() {
        try {
            if (connection != null) {
                connection.stop();
                log.info("Paused AMQP consumer");
            }
        } catch (Exception e) {
            log.error("Failed to pause consumer", e);
        }
    }
    
    @Override
    public void resume() {
        try {
            if (connection != null && !isConnected.get()) {
                connection.start();
                isConnected.set(true);
                log.info("Resumed AMQP consumer");
            }
        } catch (Exception e) {
            log.error("Failed to resume consumer", e);
        }
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = super.getMetrics();
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
            if (!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessor.shutdownNow();
        }
        
        // Stop cleanup executor
        cleanupExecutor.shutdown();
        
        // Close consumer
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception e) {
                log.error("Failed to close consumer", e);
            }
        }
        
        // Close session
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Failed to close session", e);
            }
        }
        
        // Close connection
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("Failed to close connection", e);
            }
        }
        
        // Clear caches
        messageDeduplication.clear();
        
        log.info("AMQP Inbound Adapter shut down successfully");
    }
}