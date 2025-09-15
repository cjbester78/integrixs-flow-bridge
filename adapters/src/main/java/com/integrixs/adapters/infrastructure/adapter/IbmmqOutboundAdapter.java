package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.config.IbmmqOutboundAdapterConfig;
import jakarta.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.*;
import java.util.HashMap;import java.util.List;import java.util.concurrent.CompletableFuture;
import java.util.HashMap;import java.util.List;import com.integrixs.adapters.domain.model.*;
import java.util.HashMap;import java.util.Map;
import java.util.HashMap;
import java.util.List;
/**
 * IBM MQ Outbound Adapter implementation for IBM MQ message publishing(OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Sends messages to IBM MQ queues/topics in external systems.
 * Supports IBM MQ(formerly WebSphere MQ) specific features.
 */
public class IbmmqOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(IbmmqOutboundAdapter.class);


    private final IbmmqOutboundAdapterConfig config;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private Destination destination;
    private ConnectionFactory connectionFactory;
    public IbmmqOutboundAdapter(IbmmqOutboundAdapterConfig config) {
        super();
        this.config = config;
    }
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing IBM MQ outbound adapter for destination: {}", config.getDestinationName());

        try {
            initializeIbmmqConnection();
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("IBM MQ outbound adapter initialized successfully");
        return AdapterOperationResult.success("IBM MQ outbound adapter initialized");
    }
    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying IBM MQ outbound adapter");
        try {
            if(producer != null) {
                producer.close();
            }
            if(session != null) {
                session.close();
            }
            if(connection != null) {
                connection.stop();
                connection.close();
            }
        } catch(Exception e) {
            log.warn("Error closing IBM MQ resources", e);
        }
        return AdapterOperationResult.success("IBM MQ outbound adapter destroyed");
    }
    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        // Test 1: IBM MQ connection factory
        testResults.add(
            performIbmmqConnectionTest()
       );
        // Test 2: Destination validation
        testResults.add(
            performDestinationValidationTest()
       );
        // Test 3: Producer configuration
        testResults.add(
            performProducerConfigTest()
       );
        return AdapterOperationResult.success(testResults);
    }

    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return sendIbmmqMessage(request.getPayload());
        } catch(Exception e) {
            return AdapterOperationResult.failure("Failed to send IBM MQ message: " + e.getMessage());
        }
    }

    private AdapterOperationResult sendIbmmqMessage(Object payload) throws Exception {
        try {
            Message message;

            // Create appropriate IBM MQ message based on payload type
            if(payload instanceof String) {
                message = session.createTextMessage((String) payload);
            } else if(payload instanceof byte[]) {
                BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes((byte[]) payload);
                message = bytesMessage;
            } else if(payload instanceof Map) {
                MapMessage mapMessage = session.createMapMessage();
                Map<String, Object> map = (Map<String, Object>) payload;
                // Check for special fields
                Object body = map.get("body");
                Map<String, Object> properties = (Map<String, Object>) map.get("properties");
                Map<String, Object> headers = (Map<String, Object>) map.get("headers");
                if(body != null) {
                    // Body is provided separately
                    if(body instanceof String) {
                        message = session.createTextMessage((String) body);
                    } else if(body instanceof Map) {
                        // Create map message from body
                        Map<String, Object> bodyMap = (Map<String, Object>) body;
                        for(Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                            mapMessage.setObject(entry.getKey(), entry.getValue());
                        }
                        message = mapMessage;
                    } else {
                        message = session.createObjectMessage((Serializable) body);
                    }
                } else {
                    // Use entire map as message content
                    for(Map.Entry<String, Object> entry : map.entrySet()) {
                        mapMessage.setObject(entry.getKey(), entry.getValue());
                    }
                    message = mapMessage;
                }
                // Set properties if provided
                if(properties != null) {
                    for(Map.Entry<String, Object> entry : properties.entrySet()) {
                        message.setObjectProperty(entry.getKey(), entry.getValue());
                    }
                }
                // Set headers if provided
                if(headers != null) {
                    String correlationId = (String) headers.get("correlationId");
                    if(correlationId != null) {
                        message.setJMSCorrelationID(correlationId);
                    }

                    String replyTo = (String) headers.get("replyTo");
                    if(replyTo != null) {
                        Destination replyToDestination = session.createQueue(replyTo);
                        message.setJMSReplyTo(replyToDestination);
                    }
                }
            } else if(payload instanceof Serializable) {
                message = session.createObjectMessage((Serializable) payload);
            } else {
                throw new AdapterException(
                        "Unsupported payload type: " + payload.getClass().getName());
            }

            // Set message properties from configuration
            if(config.getMessageProperties() != null && !config.getMessageProperties().isEmpty()) {
                String[] props = config.getMessageProperties().split(",");
                for(String prop : props) {
                    String[] keyValue = prop.split(" = ");
                    if(keyValue.length == 2) {
                        message.setStringProperty(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }

            // Send the message
            if(config.isPersistent()) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            producer.setPriority(config.getPriority());
            producer.setTimeToLive(config.getTimeToLive());
            producer.send(message);
            // Commit if transacted
            if(config.isTransacted()) {
                session.commit();
            }

            log.info("IBM MQ outbound adapter sent message with ID: {}", message.getJMSMessageID());
            Map<String, Object> result = new HashMap<>();
            result.put("messageId", message.getJMSMessageID());
            result.put("timestamp", new Date(message.getJMSTimestamp()));
            result.put("destination", destination.toString());
            return AdapterOperationResult.success(result,
                    String.format("Successfully sent IBM MQ message: %s", message.getJMSMessageID()));
        } catch(Exception e) {
            // Rollback if transacted
            if(config.isTransacted() && session != null) {
                try {
                    session.rollback();
                } catch(JMSException rollbackEx) {
                    log.warn("Failed to rollback transaction", rollbackEx);
                }
            }
            log.error("Error sending IBM MQ message", e);
            throw new AdapterException(
                    "Failed to send IBM MQ message: " + e.getMessage(), e);
        }
    }

    private void initializeIbmmqConnection() throws Exception {
        // Initialize connection factory
        initializeConnectionFactory();
        // Create connection
        connection = connectionFactory.createConnection(config.getUsername(), config.getPassword());
        // Set client ID if configured
        if(config.getClientId() != null && !config.getClientId().isEmpty()) {
            connection.setClientID(config.getClientId());
        }

        // Create session
        session = connection.createSession(config.isTransacted(), config.getAcknowledgementMode());
        // Create destination
        if("topic".equalsIgnoreCase(config.getDestinationType())) {
            destination = session.createTopic(config.getDestinationName());
        } else {
            destination = session.createQueue(config.getDestinationName());
        }
        // Create producer
        producer = session.createProducer(destination);
        // Start connection
        connection.start();
    }

    private void initializeConnectionFactory() throws Exception {
        if(config.getJndiName() != null && !config.getJndiName().isEmpty()) {
            // Look up connection factory from JNDI
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, config.getInitialContextFactory());
            props.put(Context.PROVIDER_URL, config.getProviderUrl());
            if(config.getJndiProperties() != null) {
                // Add additional JNDI properties
                String[] jndiProps = config.getJndiProperties().split(",");
                for(String prop : jndiProps) {
                    String[] keyValue = prop.split(" = ");
                    if(keyValue.length == 2) {
                        props.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }
            Context context = new InitialContext(props);
            connectionFactory = (ConnectionFactory) context.lookup(config.getJndiName());
        } else {
            // For simulation/testing, would create vendor - specific connection factory here
            throw new AdapterException("IBM MQ connection factory setup not implemented for non-JNDI mode");
        }
        if(config.getDestinationType() == null || config.getDestinationType().trim().isEmpty()) {
            throw new AdapterException("Destination type is required", null);
        }

        // Set defaults
        if(config.getAcknowledgementMode() == 0) {
            config.setAcknowledgementMode(Session.AUTO_ACKNOWLEDGE);
        }
        if(config.getPriority() < 0 || config.getPriority() > 9) {
            config.setPriority(4); // Default priority
        }
        if(config.getTimeToLive() < 0) {
            config.setTimeToLive(0); // Never expires
        }
    }
    public long getPollingInterval() {
        // IBM MQ outbound adapters don't poll, they push messages
        return 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("IBM MQ(Outbound): %s %s, Persistent: %s, Priority: %d",
                config.getDestinationType(),
                config.getDestinationName(),
                config.isPersistent() ? "Yes" : "No",
                config.getPriority());
    }

    // Helper methods for connection testing
    private AdapterOperationResult performIbmmqConnectionTest() {
        try {
            if(connectionFactory == null) {
                initializeConnectionFactory();
            }

            // Test creating a connection
            Connection testConnection = connectionFactory.createConnection(
                    config.getUsername(), config.getPassword());
            testConnection.close();
            return AdapterOperationResult.success(
                    "IBM MQ Connection", "Successfully connected to IBM MQ");
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to connect to IBM MQ: " + e.getMessage());
        }
    }
    private AdapterOperationResult performDestinationValidationTest() {
        try {
            String destinationType = config.getDestinationType();
            String destinationName = config.getDestinationName();
            if(!"queue".equalsIgnoreCase(destinationType) && !"topic".equalsIgnoreCase(destinationType)) {
                return AdapterOperationResult.failure(
                        "Invalid destination type: " + destinationType);
            }
            return AdapterOperationResult.success(
                    "Destination Config", String.format("Configured for %s: %s", destinationType, destinationName));
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to validate destination: " + e.getMessage());
        }
    }
    private AdapterOperationResult performProducerConfigTest() {
        try {
            String deliveryMode = config.isPersistent() ? "PERSISTENT" : "NON_PERSISTENT";
            String info = String.format("Delivery: %s, Priority: %d, TTL: %dms",
                    deliveryMode, config.getPriority(), config.getTimeToLive());
            return AdapterOperationResult.success(
                    "Producer Config", info);
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Invalid producer configuration: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }

    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        List<AdapterOperationResult> results = new ArrayList<>();
        for(SendRequest request : requests) {
            results.add(send(request));
        }

        long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
        return AdapterOperationResult.success(
                String.format("Batch sent %d/%d messages successfully", successCount, results.size()));
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }
    public boolean isBatchingEnabled() {
        return true;
    }
    public int getBatchSize() {
        return config.getBatchSize() > 0 ? config.getBatchSize() : 1000;
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.IBMMQ)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("IBM MQ Outbound adapter - sends messages to IBM MQ queues")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.IBMMQ;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }

    @Override
    public boolean supportsBatchOperations() {
        return true; // IBM MQ supports batch operations
    }

    @Override
    public int getMaxBatchSize() {
        return config.getMaxBatchSize(); // Already returns int from config
    }
}
