package com.integrixs.adapters.messaging.amqp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.messaging.amqp.AMQPConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.qpid.jms.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AMQPOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(AMQPOutboundAdapter.class);

    
    @Autowired
    private AMQPConfig config;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private JmsConnectionFactory connectionFactory;
    private JmsConnection connection;
    private Session session;
    private MessageProducer producer;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    
    // Metrics
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);
    private final AtomicLong transactionCommits = new AtomicLong(0);
    private final AtomicLong transactionRollbacks = new AtomicLong(0);
    
    // Batching
    private final BlockingQueue<SendRequest> sendQueue = new LinkedBlockingQueue<>();
    private ScheduledExecutorService batchExecutor;
    
    // Producer pool for high throughput
    private final Map<String, MessageProducer> producerPool = new ConcurrentHashMap<>();
    
    @Override
    public AdapterType getType() {
        return AdapterType.AMQP;
    }
    
    @Override
    public String getName() {
        return "AMQP Outbound Adapter";
    }
    
    @PostConstruct
    public void initialize() {
        try {
            setupConnectionFactory();
            establishConnection();
            createProducer();
            setupBatchSender();
            isInitialized = true;
            log.info("AMQP Outbound Adapter initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize AMQP Outbound Adapter", e);
            throw new AdapterException("Failed to initialize AMQP adapter", e);
        }
    }
    
    private String buildConnectionUrl() {
        if (config.getConnectionUrl() != null && !config.getConnectionUrl().isEmpty()) {
            return config.getConnectionUrl();
        }
        
        StringBuilder urlBuilder = new StringBuilder();
        
        if (config.isUseSsl()) {
            urlBuilder.append("amqps://");
        } else {
            urlBuilder.append("amqp://");
        }
        
        urlBuilder.append(config.getHost()).append(":").append(config.getPort());
        
        List<String> options = new ArrayList<>();
        
        if (config.getSaslMechanism() != null) {
            options.add("amqp.saslMechanisms=" + config.getSaslMechanism().getMechanism());
        }
        
        if (config.getIdleTimeout() > 0) {
            options.add("amqp.idleTimeout=" + config.getIdleTimeout());
        }
        
        if (!options.isEmpty()) {
            urlBuilder.append("?").append(String.join("&", options));
        }
        
        return urlBuilder.toString();
    }
    
    private void setupConnectionFactory() {
        String connectionUrl = buildConnectionUrl();
        connectionFactory = new JmsConnectionFactory(connectionUrl);
        
        // Configure factory
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            connectionFactory.setUsername(config.getUsername());
        }
        
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            connectionFactory.setPassword(config.getPassword());
        }
        
        // SSL configuration
        if (config.isUseSsl() && config.getSslConfiguration() != null) {
            // SSL configuration would be set here
            log.info("SSL enabled for AMQP connection");
        }
        
        log.info("AMQP connection factory configured: {}", connectionUrl);
    }
    
    private void establishConnection() throws JMSException {
        try {
            connection = (JmsConnection) connectionFactory.createConnection();
            
            connection.setExceptionListener(exception -> {
                log.error("AMQP connection exception", exception);
                isConnected.set(false);
                
                if (config.getFeatures().isEnableAutoReconnect()) {
                    scheduleReconnection();
                }
            });
            
            connection.start();
            isConnected.set(true);
            
            log.info("Successfully connected to AMQP broker");
        } catch (JMSException e) {
            throw new AdapterException("Failed to connect to AMQP broker", e);
        }
    }
    
    private void createProducer() throws JMSException {
        if (session == null) {
            session = connection.createSession(
                config.isEnableTransactions(),
                config.isAutoAck() ? Session.AUTO_ACKNOWLEDGE : Session.CLIENT_ACKNOWLEDGE
            );
        }
        
        // Create default producer
        if (config.getTargetAddress() != null && !config.getTargetAddress().isEmpty()) {
            Destination destination = createDestination(config.getTargetAddress());
            producer = session.createProducer(destination);
        } else {
            // Create anonymous producer
            producer = session.createProducer(null);
        }
        
        // Configure producer
        producer.setDeliveryMode(config.isDurable() ? 
            DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
        producer.setPriority(config.getPriority());
        
        if (config.getTtl() > 0) {
            producer.setTimeToLive(config.getTtl());
        }
        
        log.info("Created AMQP producer for: {}", config.getTargetAddress());
    }
    
    private Destination createDestination(String address) throws JMSException {
        if (address == null || address.isEmpty()) {
            return null;
        }
        
        switch (config.getBrokerType()) {
            case ACTIVEMQ_ARTEMIS:
                String fullAddress = address;
                if (config.getArtemisSettings().getAddressPrefix() != null) {
                    fullAddress = config.getArtemisSettings().getAddressPrefix() + address;
                }
                
                if (config.getRoutingType().equals("multicast")) {
                    return session.createTopic(fullAddress);
                } else {
                    return session.createQueue(fullAddress);
                }
                
            case AZURE_SERVICE_BUS:
                String entityPath = config.getAzureSettings().getEntityPath();
                if (entityPath != null) {
                    address = entityPath;
                }
                return session.createQueue(address);
                
            default:
                if (address.startsWith("topic://")) {
                    return session.createTopic(address.substring(8));
                } else if (address.startsWith("queue://")) {
                    return session.createQueue(address.substring(8));
                } else {
                    return session.createQueue(address);
                }
        }
    }
    
    private void setupBatchSender() {
        if (!config.isEnableBatching()) {
            return;
        }
        
        batchExecutor = Executors.newSingleThreadScheduledExecutor();
        batchExecutor.scheduleWithFixedDelay(this::processBatch, 
            config.getBatchTimeout(), 
            config.getBatchTimeout(), 
            TimeUnit.MILLISECONDS);
    }
    
    @Override
    public CompletableFuture<MessageDTO> send(MessageDTO message) {
        CompletableFuture<MessageDTO> future = new CompletableFuture<>();
        
        try {
            if (config.isEnableBatching()) {
                sendQueue.offer(new SendRequest(message, future));
            } else {
                sendMessage(message, future);
            }
        } catch (Exception e) {
            messagesFailed.incrementAndGet();
            future.completeExceptionally(e);
            log.error("Failed to send message", e);
        }
        
        return future;
    }
    
    private void sendMessage(MessageDTO messageDTO, CompletableFuture<MessageDTO> future) {
        try {
            // Parse message content
            JsonNode messageContent = objectMapper.readTree(messageDTO.getContent());
            
            // Create JMS message
            jakarta.jms.Message jmsMessage = createJmsMessage(messageContent);
            
            // Set message properties
            setMessageProperties(jmsMessage, messageContent.path("properties"));
            
            // Set headers
            setMessageHeaders(jmsMessage, messageContent.path("headers"));
            
            // Add AMQP annotations
            setAmqpAnnotations(jmsMessage, messageDTO);
            
            // Determine destination
            Destination destination = null;
            String targetAddress = messageDTO.getMetadata().get("targetAddress");
            if (targetAddress != null) {
                destination = createDestination(targetAddress);
            }
            
            // Send message
            if (destination != null) {
                producer.send(destination, jmsMessage);
            } else if (config.getTargetAddress() != null) {
                producer.send(jmsMessage);
            } else {
                throw new AdapterException("No target address specified", null);
            }
            
            messagesSent.incrementAndGet();
            
            // Commit transaction if enabled
            if (config.isEnableTransactions()) {
                session.commit();
                transactionCommits.incrementAndGet();
            }
            
            // Complete future
            messageDTO.getMetadata().put("amqpMessageId", jmsMessage.getJMSMessageID());
            messageDTO.getMetadata().put("amqpTimestamp", String.valueOf(System.currentTimeMillis()));
            future.complete(messageDTO);
            
        } catch (Exception e) {
            messagesFailed.incrementAndGet();
            future.completeExceptionally(e);
            log.error("Failed to send AMQP message", e);
            
            // Rollback transaction if enabled
            if (config.isEnableTransactions()) {
                try {
                    session.rollback();
                    transactionRollbacks.incrementAndGet();
                } catch (JMSException ex) {
                    log.error("Failed to rollback transaction", ex);
                }
            }
        }
    }
    
    private jakarta.jms.Message createJmsMessage(JsonNode content) throws JMSException {
        JsonNode body = content.path("body");
        String messageType = content.path("type").asText("text");
        
        switch (messageType.toLowerCase()) {
            case "text":
                return session.createTextMessage(body.asText());
                
            case "bytes":
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(body.asText().getBytes(StandardCharsets.UTF_8));
                return bytesMessage;
                
            case "map":
                MapMessage mapMessage = session.createMapMessage();
                if (body.isObject()) {
                    body.fields().forEachRemaining(entry -> {
                        try {
                            mapMessage.setObject(entry.getKey(), entry.getValue().asText());
                        } catch (JMSException e) {
                            log.error("Failed to set map entry: {}", entry.getKey(), e);
                        }
                    });
                }
                return mapMessage;
                
            case "stream":
                StreamMessage streamMessage = session.createStreamMessage();
                if (body.isArray()) {
                    body.forEach(item -> {
                        try {
                            streamMessage.writeObject(item.asText());
                        } catch (JMSException e) {
                            log.error("Failed to write stream item", e);
                        }
                    });
                }
                return streamMessage;
                
            default:
                return session.createTextMessage(body.asText());
        }
    }
    
    private void setMessageProperties(jakarta.jms.Message message, JsonNode properties) throws JMSException {
        if (properties == null || !properties.isObject()) {
            return;
        }
        
        // Standard properties
        if (properties.has("correlationId")) {
            message.setJMSCorrelationID(properties.get("correlationId").asText());
        }
        
        if (properties.has("replyTo")) {
            String replyTo = properties.get("replyTo").asText();
            message.setJMSReplyTo(createDestination(replyTo));
        }
        
        if (properties.has("type")) {
            message.setJMSType(properties.get("type").asText());
        }
        
        if (properties.has("expiration")) {
            message.setJMSExpiration(properties.get("expiration").asLong());
        }
        
        if (properties.has("priority")) {
            message.setJMSPriority(properties.get("priority").asInt());
        }
        
        // AMQP specific properties
        if (config.getFeatures().isEnableMessageGrouping() && config.getGroupId() != null) {
            message.setStringProperty("JMSXGroupID", config.getGroupId());
            message.setIntProperty("JMSXGroupSeq", config.getGroupSequence());
        }
    }
    
    private void setMessageHeaders(jakarta.jms.Message message, JsonNode headers) throws JMSException {
        if (headers == null || !headers.isObject()) {
            return;
        }
        
        headers.fields().forEachRemaining(entry -> {
            try {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                if (value.isTextual()) {
                    message.setStringProperty(key, value.asText());
                } else if (value.isNumber()) {
                    if (value.isIntegralNumber()) {
                        message.setLongProperty(key, value.asLong());
                    } else {
                        message.setDoubleProperty(key, value.asDouble());
                    }
                } else if (value.isBoolean()) {
                    message.setBooleanProperty(key, value.asBoolean());
                } else {
                    message.setStringProperty(key, value.toString());
                }
            } catch (JMSException e) {
                log.error("Failed to set header: {}", entry.getKey(), e);
            }
        });
    }
    
    private void setAmqpAnnotations(jakarta.jms.Message message, MessageDTO messageDTO) throws JMSException {
        // Add message annotations
        config.getMessageAnnotations().forEach((key, value) -> {
            try {
                message.setObjectProperty("x-amqp-annotation-" + key, value);
            } catch (JMSException e) {
                log.error("Failed to set annotation: {}", key, e);
            }
        });
        
        // Add tracing information if enabled
        if (config.getFeatures().isEnableTracing()) {
            message.setStringProperty("x-trace-id", UUID.randomUUID().toString());
            message.setLongProperty("x-trace-timestamp", System.currentTimeMillis());
        }
        
        // Add routing information
        if (config.getRoutingKey() != null) {
            message.setStringProperty("x-routing-key", config.getRoutingKey());
        }
    }
    
    private void processBatch() {
        if (sendQueue.isEmpty()) {
            return;
        }
        
        List<SendRequest> batch = new ArrayList<>();
        sendQueue.drainTo(batch, config.getBatchSize());
        
        if (batch.isEmpty()) {
            return;
        }
        
        try {
            for (SendRequest request : batch) {
                sendMessage(request.message, request.future);
            }
            
            // Commit batch transaction
            if (config.isEnableTransactions()) {
                session.commit();
                transactionCommits.incrementAndGet();
            }
            
            log.debug("Sent batch of {} messages", batch.size());
            
        } catch (Exception e) {
            log.error("Failed to send batch", e);
            
            // Rollback on failure
            if (config.isEnableTransactions()) {
                try {
                    session.rollback();
                    transactionRollbacks.incrementAndGet();
                } catch (JMSException ex) {
                    log.error("Failed to rollback batch transaction", ex);
                }
            }
            
            batch.forEach(req -> req.future.completeExceptionally(e));
        }
    }
    
    // Advanced operations
    public void createQueue(String queueName, Map<String, Object> properties) throws JMSException {
        if (config.getBrokerType() != BrokerType.ACTIVEMQ_ARTEMIS) {
            throw new UnsupportedOperationException("Queue creation not supported for " + config.getBrokerType());
        }
        
        // Artemis-specific queue creation would go here
        log.info("Queue creation requested: {}", queueName);
    }
    
    public void deleteQueue(String queueName) throws JMSException {
        if (config.getBrokerType() != BrokerType.ACTIVEMQ_ARTEMIS) {
            throw new UnsupportedOperationException("Queue deletion not supported for " + config.getBrokerType());
        }
        
        // Artemis-specific queue deletion would go here
        log.info("Queue deletion requested: {}", queueName);
    }
    
    @Override
    public boolean testConnection() {
        try {
            if (!isConnected.get()) {
                establishConnection();
            }
            
            // Test by creating a temporary queue
            TemporaryQueue tempQueue = session.createTemporaryQueue();
            tempQueue.delete();
            
            return true;
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return false;
        }
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
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = super.getMetrics();
        metrics.put("messagesSent", messagesSent.get());
        metrics.put("messagesFailed", messagesFailed.get());
        metrics.put("transactionCommits", transactionCommits.get());
        metrics.put("transactionRollbacks", transactionRollbacks.get());
        metrics.put("isConnected", isConnected.get());
        metrics.put("sendQueueSize", sendQueue.size());
        metrics.put("producerPoolSize", producerPool.size());
        
        return metrics;
    }
    
    @PreDestroy
    public void destroy() {
        log.info("Shutting down AMQP Outbound Adapter");
        
        // Shutdown batch executor
        if (batchExecutor != null) {
            batchExecutor.shutdown();
            try {
                if (!batchExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    batchExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                batchExecutor.shutdownNow();
            }
        }
        
        // Process remaining messages
        processBatch();
        
        // Close producer pool
        producerPool.values().forEach(prod -> {
            try {
                prod.close();
            } catch (Exception e) {
                log.error("Failed to close producer", e);
            }
        });
        
        // Close default producer
        if (producer != null) {
            try {
                producer.close();
            } catch (Exception e) {
                log.error("Failed to close producer", e);
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
        
        log.info("AMQP Outbound Adapter shut down successfully");
    }
    
    // Helper class
    private static class SendRequest {
        final MessageDTO message;
        final CompletableFuture<MessageDTO> future;
        
        SendRequest(MessageDTO message, CompletableFuture<MessageDTO> future) {
            this.message = message;
            this.future = future;
        }
    }
}