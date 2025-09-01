package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.core.AdapterException;

import com.integrixs.adapters.domain.port.SenderAdapterPort;
import com.integrixs.adapters.config.JmsSenderAdapterConfig;
import jakarta.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.List;import java.util.concurrent.CompletableFuture;
import java.util.List;import java.util.concurrent.ConcurrentHashMap;
import java.util.List;import com.integrixs.adapters.domain.model.*;
import java.util.Map;
import java.util.List;import lombok.extern.slf4j.Slf4j;
/**
 * JMS Sender Adapter implementation for JMS message consumption (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Listens to JMS queues/topics and receives messages from external systems.
 */
@Slf4j
public class JmsSenderAdapter extends AbstractAdapter implements SenderAdapterPort {
    
    private final JmsSenderAdapterConfig config;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private ConnectionFactory connectionFactory;
    private final Map<String, Object> receivedMessages = new ConcurrentHashMap<>();
    public JmsSenderAdapter(JmsSenderAdapterConfig config) {
        super();
        this.config = config;
    }
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing JMS sender adapter (inbound) for destination: {}", config.getDestinationName());
        
        try {
            validateConfiguration();
            initializeJmsConnection();
        } catch (Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }
        
        log.info("JMS sender adapter initialized successfully");
        return AdapterOperationResult.success("JMS sender adapter initialized");
    }
    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying JMS sender adapter");
        try {
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.stop();
                connection.close();
            }
        } catch (Exception e) {
            log.warn("Error closing JMS resources", e);
        }
        receivedMessages.clear();
        return AdapterOperationResult.success("JMS sender adapter destroyed");
    }
    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        // Test 1: JMS connection factory
        testResults.add(
            performJmsConnectionTest()
        );
        // Test 2: Destination validation
        testResults.add(
            performDestinationValidationTest()
        );
        // Test 3: Message selector validation
        if (config.getMessageSelector() != null && !config.getMessageSelector().isEmpty()) {
            testResults.add(
                performMessageSelectorValidationTest()
            );
        }
        return AdapterOperationResult.success(testResults);
    }
    
    
    private AdapterOperationResult receiveJmsMessages() throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();
        try {
            // Receive messages based on configuration
            if (config.isEnableBatchReceive() && config.getBatchSize() > 0) {
                // Batch receive
                for (int i = 0; i < config.getBatchSize(); i++) {
                    Message message = consumer.receive(config.getReceiveTimeout());
                    if (message == null) {
                        break; // No more messages
                    }
                    
                    Map<String, Object> messageData = processJmsMessage(message);
                    messages.add(messageData);
                    // Acknowledge if configured
                    if (config.getAcknowledgementMode() == Session.CLIENT_ACKNOWLEDGE) {
                        message.acknowledge();
                    }
                }
            } else {
                // Single message receive
                Message message = consumer.receive(config.getReceiveTimeout());
                if (message != null) {
                    Map<String, Object> messageData = processJmsMessage(message);
                    messages.add(messageData);
                    if (config.getAcknowledgementMode() == Session.CLIENT_ACKNOWLEDGE) {
                        message.acknowledge();
                    }
                }
            }
            
            if (messages.isEmpty()) {
                return AdapterOperationResult.success(messages, "No messages available");
            }
            
            log.info("JMS sender adapter received {} messages", messages.size());
            return AdapterOperationResult.success(messages, 
                    String.format("Successfully received %d JMS messages", messages.size()));
        } catch (Exception e) {
            log.error("Error receiving JMS messages", e);
            throw new AdapterException.OperationException(AdapterConfiguration.AdapterTypeEnum.JMS, 
                    "Failed to receive JMS messages: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> processJmsMessage(Message message) throws JMSException {
        Map<String, Object> messageData = new HashMap<>();
        // Extract message ID and correlation ID
        messageData.put("messageId", message.getJMSMessageID());
        messageData.put("correlationId", message.getJMSCorrelationID());
        messageData.put("timestamp", new Date(message.getJMSTimestamp()));
        messageData.put("priority", message.getJMSPriority());
        messageData.put("redelivered", message.getJMSRedelivered());
        // Extract message properties
        Map<String, Object> properties = new HashMap<>();
        Enumeration<?> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            properties.put(propertyName, message.getObjectProperty(propertyName));
        }
        messageData.put("properties", properties);
        // Extract message body based on type
        if (message instanceof TextMessage) {
            messageData.put("messageType", "TextMessage");
            messageData.put("body", ((TextMessage) message).getText());
        } else if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            messageData.put("messageType", "BytesMessage");
            messageData.put("body", bytes);
        } else if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            Map<String, Object> mapData = new HashMap<>();
            Enumeration<?> mapNames = mapMessage.getMapNames();
            while (mapNames.hasMoreElements()) {
                String name = (String) mapNames.nextElement();
                mapData.put(name, mapMessage.getObject(name));
            }
            messageData.put("messageType", "MapMessage");
            messageData.put("body", mapData);
        } else if (message instanceof ObjectMessage) {
            messageData.put("messageType", "ObjectMessage");
            messageData.put("body", ((ObjectMessage) message).getObject());
        } else {
            messageData.put("messageType", message.getClass().getSimpleName());
            messageData.put("body", message.toString());
        }
        return messageData;
    }
    
    private void initializeJmsConnection() throws Exception {
        // Initialize connection factory
        initializeConnectionFactory();
        // Create connection
        connection = connectionFactory.createConnection(config.getUsername(), config.getPassword());
        // Set client ID if configured (required for durable subscriptions)
        if (config.getClientId() != null && !config.getClientId().isEmpty()) {
            connection.setClientID(config.getClientId());
        }
        // Create session
        session = connection.createSession(config.isTransacted(), config.getAcknowledgementMode());
        // Create destination
        Destination destination;
        if ("topic".equalsIgnoreCase(config.getDestinationType())) {
            destination = session.createTopic(config.getDestinationName());
            // Create durable subscriber if configured
            if (config.isDurableSubscription() && config.getSubscriptionName() != null) {
                consumer = session.createDurableSubscriber((Topic) destination, 
                        config.getSubscriptionName(), config.getMessageSelector(), false);
            } else {
                consumer = session.createConsumer(destination, config.getMessageSelector());
            }
        } else {
            destination = session.createQueue(config.getDestinationName());
            consumer = session.createConsumer(destination, config.getMessageSelector());
        }
        // Start connection
        connection.start();
    }
    
    private void initializeConnectionFactory() throws Exception {
        if (config.getJndiName() != null && !config.getJndiName().isEmpty()) {
            // Look up connection factory from JNDI
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, config.getInitialContextFactory());
            props.put(Context.PROVIDER_URL, config.getProviderUrl());
            if (config.getJndiProperties() != null) {
                // Add additional JNDI properties
                String[] jndiProps = config.getJndiProperties().split(",");
                for (String prop : jndiProps) {
                    String[] keyValue = prop.split("=");
                    if (keyValue.length == 2) {
                        props.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }
            Context context = new InitialContext(props);
            connectionFactory = (ConnectionFactory) context.lookup(config.getJndiName());
        } else {
            // For simulation/testing, would create vendor-specific connection factory here
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JMS, 
                    "JMS connection factory configuration required");
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getDestinationName() == null || config.getDestinationName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JMS, "Destination name is required");
        }
        if (config.getDestinationType() == null || config.getDestinationType().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JMS, "Destination type is required");
        }
        
        // Set defaults
        if (config.getAcknowledgementMode() == 0) {
            config.setAcknowledgementMode(Session.AUTO_ACKNOWLEDGE);
        }
        if (config.getReceiveTimeout() == 0) {
            config.setReceiveTimeout(1000); // Default 1 second timeout
        }
    }
    public long getPollingInterval() {
        return config.getPollingInterval() != null ? config.getPollingInterval() : 5000;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("JMS Sender (Inbound): %s %s, Selector: %s", 
                config.getDestinationType(),
                config.getDestinationName(),
                config.getMessageSelector() != null ? config.getMessageSelector() : "None");
    }
    
    // SenderAdapterPort implementation methods
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            return receiveJmsMessages();
        } catch (Exception e) {
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }
    
    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }
    
    @Override
    public void startListening(DataReceivedCallback callback) {
        // Not implemented for this adapter type
        throw new UnsupportedOperationException("This adapter does not support push-based listening");
    }
    
    @Override
    public void stopListening() {
        // Not implemented
    }
    
    @Override
    public boolean isListening() {
        return false;
    }
    public void startPolling(long intervalMillis) {
        // Implement if polling is supported
        throw new UnsupportedOperationException("Polling not implemented");
    }
    public void stopPolling() {
        // Not implemented
    }
    
    public void setDataReceivedCallback(DataReceivedCallback callback) {
        // Implement if callbacks are supported
    }
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.JMS)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.SENDER)
                .description("Sender adapter implementation")
                .version("1.0.0")
                .supportsBatch(false)
                .supportsAsync(true)
                .build();
    }
    
    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }
    
    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }
    
    // Helper methods for connection testing
    private AdapterOperationResult performJmsConnectionTest() {
        try {
            if (connectionFactory == null) {
                initializeConnectionFactory();
            }
            
            // Test creating a connection
            Connection testConnection = connectionFactory.createConnection(
                    config.getUsername(), config.getPassword());
            testConnection.close();
            return AdapterOperationResult.success(
                    "JMS Connection", "Successfully connected to JMS provider");
        } catch (Exception e) {
            return AdapterOperationResult.failure(
                    "JMS Connection", "Failed to connect to JMS provider: " + e.getMessage(), e);
        }
    }
    private AdapterOperationResult performDestinationValidationTest() {
        try {
            String destinationType = config.getDestinationType();
            String destinationName = config.getDestinationName();
            if (!"queue".equalsIgnoreCase(destinationType) && !"topic".equalsIgnoreCase(destinationType)) {
                return AdapterOperationResult.failure(
                        "Destination Type", "Invalid destination type: " + destinationType, null);
            }
            return AdapterOperationResult.success(
                    "Destination Config", String.format("Configured for %s: %s", destinationType, destinationName));
        } catch (Exception e) {
            return AdapterOperationResult.failure(
                    "Destination Config", "Failed to validate destination: " + e.getMessage(), e);
        }
    }
    
    private AdapterOperationResult performMessageSelectorValidationTest() {
        try {
            String selector = config.getMessageSelector();
            // Basic validation - in real implementation would parse the selector
            if (selector.contains("=") || selector.contains("<") || selector.contains(">")) {
                return AdapterOperationResult.success(
                        "Message Selector", "Valid selector: " + selector);
            } else {
                return AdapterOperationResult.failure(
                        "Message Selector", "Invalid selector syntax: " + selector, null);
            }
        } catch (Exception e) {
            return AdapterOperationResult.failure(
                    "Message Selector", "Failed to validate selector: " + e.getMessage(), e);
        }
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.JMS;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.SENDER;
    }

}
