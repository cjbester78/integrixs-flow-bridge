package com.integrixs.backend.plugin.test;

import com.integrixs.backend.plugin.api.*;
import com.integrixs.backend.plugin.api.AdapterPlugin.Direction;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test harness for plugin development and testing
 */
public class PluginTestHarness {

    private static final Logger log = LoggerFactory.getLogger(PluginTestHarness.class);

    /**
     * Convert Map<String, Object> to Map<String, String>
     */
    private static Map<String, String> convertToStringMap(Map<String, Object> objectMap) {
        Map<String, String> stringMap = new HashMap<>();
        if (objectMap != null) {
            objectMap.forEach((key, value) -> stringMap.put(key, String.valueOf(value)));
        }
        return stringMap;
    }


    private final Class<? extends AdapterPlugin> pluginClass;
    private AdapterPlugin pluginInstance;
    private TestMessageCollector messageCollector;
    private Map<String, Object> configuration;

    public PluginTestHarness(Class<? extends AdapterPlugin> pluginClass) {
        this.pluginClass = pluginClass;
        this.messageCollector = new TestMessageCollector();
    }

    /**
     * Configure the plugin
     */
    public void configure(Map<String, Object> configuration) {
        this.configuration = configuration;
        initialize();
    }

    /**
     * Initialize the plugin
     */
    private void initialize() {
        try {
            pluginInstance = pluginClass.getDeclaredConstructor().newInstance();
            pluginInstance.initialize(configuration);
            log.info("Plugin initialized successfully");
        } catch(Exception e) {
            throw new RuntimeException("Failed to initialize plugin", e);
        }
    }

    /**
     * Test plugin metadata
     */
    public AdapterMetadata getMetadata() {
        return createInstance().getMetadata();
    }

    /**
     * Test configuration schema
     */
    public ConfigurationSchema getConfigurationSchema() {
        return createInstance().getConfigurationSchema();
    }

    /**
     * Test connection
     */
    public ConnectionTestResult testConnection(Direction direction) {
        ensureInitialized();
        return pluginInstance.testConnection(direction);
    }

    /**
     * Test health check
     */
    public HealthStatus checkHealth() {
        ensureInitialized();
        return pluginInstance.checkHealth();
    }

    /**
     * Send a test message
     */
    public SendResult send(PluginMessage message) {
        ensureInitialized();
        OutboundHandler handler = pluginInstance.getOutboundHandler();
        if(handler == null) {
            throw new IllegalStateException("Plugin does not support outbound messages");
        }
        return handler.send(message);
    }

    /**
     * Send a batch of test messages
     */
    public BatchSendResult sendBatch(List<PluginMessage> messages) {
        ensureInitialized();
        OutboundHandler handler = pluginInstance.getOutboundHandler();
        if(handler == null) {
            throw new IllegalStateException("Plugin does not support outbound messages");
        }
        return handler.sendBatch(messages);
    }

    /**
     * Start listening for inbound messages
     */
    public void startListening() {
        ensureInitialized();
        InboundHandler handler = pluginInstance.getInboundHandler();
        if(handler == null) {
            throw new IllegalStateException("Plugin does not support inbound messages");
        }

        try {
            handler.startListening(messageCollector);
            log.info("Started listening for inbound messages");
        } catch(PluginException e) {
            throw new RuntimeException("Failed to start listening", e);
        }
    }

    /**
     * Stop listening for inbound messages
     */
    public void stopListening() {
        ensureInitialized();
        InboundHandler handler = pluginInstance.getInboundHandler();
        if(handler != null && handler.isListening()) {
            handler.stopListening();
            log.info("Stopped listening for inbound messages");
        }
    }

    /**
     * Poll for inbound messages
     */
    public PollingResult poll() {
        ensureInitialized();
        InboundHandler handler = pluginInstance.getInboundHandler();
        if(handler == null) {
            throw new IllegalStateException("Plugin does not support inbound messages");
        }
        return handler.poll();
    }

    /**
     * Wait for inbound messages
     */
    public List<PluginMessage> waitForMessages(int count, long timeout, TimeUnit unit) {
        return messageCollector.waitForMessages(count, timeout, unit);
    }

    /**
     * Get all collected messages
     */
    public List<PluginMessage> getCollectedMessages() {
        return messageCollector.getMessages();
    }

    /**
     * Clear collected messages
     */
    public void clearCollectedMessages() {
        messageCollector.clear();
    }

    /**
     * Get collected errors
     */
    public List<Exception> getCollectedErrors() {
        return messageCollector.getErrors();
    }

    /**
     * Destroy the plugin
     */
    public void destroy() {
        if(pluginInstance != null) {
            pluginInstance.destroy();
            pluginInstance = null;
            log.info("Plugin destroyed");
        }
    }

    /**
     * Create a test message
     */
    public static PluginMessage createTestMessage(String id, Map<String, Object> body) {
        return PluginMessage.builder()
                .id(id)
                .headers(Map.of(
                    "test", "true",
                    "timestamp", String.valueOf(System.currentTimeMillis())
               ))
                .body(body)
                .contentType("application/json")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a test message with headers
     */
    public static PluginMessage createTestMessage(String id, Map<String, Object> headers,
                                                  Map<String, Object> body) {
        return PluginMessage.builder()
                .id(id)
                .headers(convertToStringMap(headers))
                .body(body)
                .contentType("application/json")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private AdapterPlugin createInstance() {
        try {
            return pluginClass.getDeclaredConstructor().newInstance();
        } catch(Exception e) {
            throw new RuntimeException("Failed to create plugin instance", e);
        }
    }

    private void ensureInitialized() {
        if(pluginInstance == null) {
            throw new IllegalStateException("Plugin not initialized. Call configure() first.");
        }
    }

    /**
     * Test message collector
     */
    private static class TestMessageCollector implements MessageCallback {
        private final List<PluginMessage> messages = Collections.synchronizedList(new ArrayList<>());
        private final List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
        private final List<CompletableFuture<Void>> waiters = new ArrayList<>();

        @Override
        public void onMessage(PluginMessage message) {
            log.debug("Received test message: {}", message.getId());
            messages.add(message);
            notifyWaiters();
        }

        @Override
        public void onError(Throwable error) {
            log.error("Received error", error);
            if (error instanceof Exception) {
                errors.add((Exception) error);
            } else {
                errors.add(new RuntimeException("Error in message processing", error));
            }
        }

        public List<PluginMessage> getMessages() {
            return new ArrayList<>(messages);
        }

        public List<Exception> getErrors() {
            return new ArrayList<>(errors);
        }

        public void clear() {
            messages.clear();
            errors.clear();
        }

        public List<PluginMessage> waitForMessages(int count, long timeout, TimeUnit unit) {
            long deadline = System.currentTimeMillis() + unit.toMillis(timeout);

            while(messages.size() < count && System.currentTimeMillis() < deadline) {
                CompletableFuture<Void> waiter = new CompletableFuture<>();
                synchronized(waiters) {
                    waiters.add(waiter);
                }

                try {
                    waiter.get(deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                } catch(Exception e) {
                    // Timeout or interruption
                    break;
                }
            }

            return getMessages();
        }

        private void notifyWaiters() {
            synchronized(waiters) {
                waiters.forEach(w -> w.complete(null));
                waiters.clear();
            }
        }
    }
}
