package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.config.IbmmqInboundAdapterConfig;
import jakarta.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.List;import java.util.concurrent.CompletableFuture;
import java.util.List;import java.util.concurrent.ConcurrentHashMap;
import java.util.List;import com.integrixs.adapters.domain.model.*;
import java.util.Map;
import java.util.List;import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * IBM MQ Inbound Adapter implementation for IBM MQ message consumption(INBOUND).
 * Follows middleware convention: Inbound = receives data FROM external systems.
 * Listens to IBM MQ queues/topics and receives messages from external systems.
 * Supports WebSphere MQ(now IBM MQ) specific features like queue managers and channels.
 */
public class IbmmqInboundAdapter extends AbstractAdapter implements InboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(IbmmqInboundAdapter.class);


    private final IbmmqInboundAdapterConfig config;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private ConnectionFactory connectionFactory;
    private final Map<String, Object> receivedMessages = new ConcurrentHashMap<>();

    // Polling mechanism fields
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private ScheduledExecutorService pollingExecutor;
    private DataReceivedCallback dataCallback;
    public IbmmqInboundAdapter(IbmmqInboundAdapterConfig config) {
        super();
        this.config = config;
    }
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing IBM MQ inbound adapter for destination: {}", config.getDestinationName());

        try {
            initializeIbmmqConnection();
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("IBM MQ inbound adapter initialized successfully");
        return AdapterOperationResult.success("IBM MQ inbound adapter initialized");
    }
    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying IBM MQ inbound adapter");

        // Stop polling if active
        stopPolling();

        try {
            if(consumer != null) {
                consumer.close();
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
        receivedMessages.clear();
        return AdapterOperationResult.success("IBM MQ inbound adapter destroyed");
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
        // Test 3: Message selector validation
        if(config.getMessageSelector() != null && !config.getMessageSelector().isEmpty()) {
            testResults.add(
                performMessageSelectorValidationTest()
           );
        }
        return AdapterOperationResult.success(testResults);
    }


    private AdapterOperationResult receiveIbmmqMessages() throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();
        try {
            // Receive messages based on configuration
            if(config.isEnableBatchReceive() && config.getBatchSize() > 0) {
                // Batch receive
                for(int i = 0; i < config.getBatchSize(); i++) {
                    Message message = consumer.receive(config.getReceiveTimeout());
                    if(message == null) {
                        break; // No more messages
                    }

                    Map<String, Object> messageData = processIbmmqMessage(message);
                    messages.add(messageData);
                    // Acknowledge if configured
                    if(config.getAcknowledgementMode() == Session.CLIENT_ACKNOWLEDGE) {
                        message.acknowledge();
                    }
                }
            } else {
                // Single message receive
                Message message = consumer.receive(config.getReceiveTimeout());
                if(message != null) {
                    Map<String, Object> messageData = processIbmmqMessage(message);
                    messages.add(messageData);
                    if(config.getAcknowledgementMode() == Session.CLIENT_ACKNOWLEDGE) {
                        message.acknowledge();
                    }
                }
            }

            if(messages.isEmpty()) {
                return AdapterOperationResult.success(messages, "No messages available");
            }

            log.info("IBM MQ inbound adapter received {} messages", messages.size());
            return AdapterOperationResult.success(messages,
                    String.format("Successfully received %d IBM MQ messages", messages.size()));
        } catch(Exception e) {
            log.error("Error receiving IBM MQ messages", e);
            throw new AdapterException(
                    "Failed to receive IBM MQ messages: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> processIbmmqMessage(Message message) throws JMSException {
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
        while(propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            properties.put(propertyName, message.getObjectProperty(propertyName));
        }
        messageData.put("properties", properties);
        // Extract message body based on type
        if(message instanceof TextMessage) {
            messageData.put("messageType", "TextMessage");
            messageData.put("body", ((TextMessage) message).getText());
        } else if(message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            messageData.put("messageType", "BytesMessage");
            messageData.put("body", bytes);
        } else if(message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            Map<String, Object> mapData = new HashMap<>();
            Enumeration<?> mapNames = mapMessage.getMapNames();
            while(mapNames.hasMoreElements()) {
                String name = (String) mapNames.nextElement();
                mapData.put(name, mapMessage.getObject(name));
            }
            messageData.put("messageType", "MapMessage");
            messageData.put("body", mapData);
        } else if(message instanceof ObjectMessage) {
            messageData.put("messageType", "ObjectMessage");
            messageData.put("body", ((ObjectMessage) message).getObject());
        } else {
            messageData.put("messageType", message.getClass().getSimpleName());
            messageData.put("body", message.toString());
        }
        return messageData;
    }

    private void initializeIbmmqConnection() throws Exception {
        // Initialize connection factory
        initializeConnectionFactory();
        // Create connection
        connection = connectionFactory.createConnection(config.getUsername(), config.getPassword());
        // Set client ID if configured(required for durable subscriptions)
        if(config.getClientId() != null && !config.getClientId().isEmpty()) {
            connection.setClientID(config.getClientId());
        }
        // Create session
        session = connection.createSession(config.isTransacted(), config.getAcknowledgementMode());
        // Create destination
        Destination destination;
        if("topic".equalsIgnoreCase(config.getDestinationType())) {
            destination = session.createTopic(config.getDestinationName());
            // Create durable subscriber if configured
            if(config.isDurableSubscription() && config.getSubscriptionName() != null) {
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
        if(config.getReceiveTimeout() == 0) {
            config.setReceiveTimeout(1000); // Default 1 second timeout
        }
    }
    public long getPollingInterval() {
        return config.getPollingInterval() != null ? config.getPollingInterval() : 5000;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("IBM MQ(Inbound): %s %s, Selector: %s, Polling: %sms",
                config.getDestinationType(),
                config.getDestinationName(),
                config.getMessageSelector() != null ? config.getMessageSelector() : "None",
                getPollingInterval());
    }

    // InboundAdapterPort implementation methods
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            return receiveIbmmqMessages();
        } catch(Exception e) {
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
        log.debug("Push-based listening not supported by this adapter type");
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
        if(polling.get()) {
            log.warn("IBM MQ polling already active");
            return;
        }

        log.info("Starting IBM MQ polling with interval: {} ms", intervalMillis);
        polling.set(true);

        // Create scheduled executor for polling
        pollingExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ibmmq - polling-" + config.getDestinationName());
            t.setDaemon(true);
            return t;
        });

        // Schedule polling task
        pollingExecutor.scheduleWithFixedDelay(() -> {
            if(!polling.get()) {
                return;
            }

            try {
                log.debug("Executing IBM MQ polling cycle");
                AdapterOperationResult result = receiveIbmmqMessages();

                // If we have a callback and found messages, notify
                if(dataCallback != null && result.isSuccess() && result.getData() != null) {
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) result.getData();
                    if(!messages.isEmpty()) {
                        log.info("IBM MQ polling received {} messages", messages.size());
                        dataCallback.onDataReceived(messages, result);
                    }
                }
            } catch(Exception e) {
                log.error("Error during IBM MQ polling", e);
                if(dataCallback != null) {
                    dataCallback.onDataReceived(null,
                        AdapterOperationResult.failure("Polling error: " + e.getMessage()));
                }
            }
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);

        log.info("IBM MQ polling started successfully");
    }

    public void stopPolling() {
        if(polling.compareAndSet(true, false)) {
            log.info("Stopping IBM MQ polling");

            if(pollingExecutor != null) {
                pollingExecutor.shutdown();
                try {
                    if(!pollingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        pollingExecutor.shutdownNow();
                    }
                } catch(InterruptedException e) {
                    log.warn("Interrupted while waiting for polling executor to shutdown");
                    pollingExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                pollingExecutor = null;
            }

            log.info("IBM MQ polling stopped");
        }
    }

    public void setDataReceivedCallback(DataReceivedCallback callback) {
        this.dataCallback = callback;
        log.debug("Data callback registered for IBM MQ adapter");
    }

    public boolean isPolling() {
        return polling.get();
    }
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.IBMMQ)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("Inbound adapter implementation")
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

    private AdapterOperationResult performMessageSelectorValidationTest() {
        try {
            String selector = config.getMessageSelector();
            // Basic validation - in real implementation would parse the selector
            if(selector.contains(" = ") || selector.contains("<") || selector.contains(">")) {
                return AdapterOperationResult.success(
                        "Message Selector", "Valid selector: " + selector);
            } else {
                return AdapterOperationResult.failure(
                        "Invalid selector syntax: " + selector);
            }
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to validate selector: " + e.getMessage());
        }
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.IBMMQ;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.INBOUND;
    }

}
