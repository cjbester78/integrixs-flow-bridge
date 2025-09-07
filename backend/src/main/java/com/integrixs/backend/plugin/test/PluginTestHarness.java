package com.integrixs.backend.plugin.test;

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test harness for plugin development and testing
 */
@Slf4j
public class PluginTestHarness {
    
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
        } catch (Exception e) {
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
        if (handler == null) {
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
        if (handler == null) {
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
        if (handler == null) {
            throw new IllegalStateException("Plugin does not support inbound messages");
        }
        
        try {
            handler.startListening(messageCollector);
            log.info("Started listening for inbound messages");
        } catch (PluginException e) {
            throw new RuntimeException("Failed to start listening", e);
        }
    }
    
    /**
     * Stop listening for inbound messages
     */
    public void stopListening() {
        ensureInitialized();
        InboundHandler handler = pluginInstance.getInboundHandler();
        if (handler != null && handler.isListening()) {
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
        if (handler == null) {
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
        if (pluginInstance != null) {
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
                    "timestamp", System.currentTimeMillis()
                ))
                .body(body)
                .contentType("application/json")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Create a test message with headers
     */
    public static PluginMessage createTestMessage(String id, Map<String, Object> headers, 
                                                  Map<String, Object> body) {
        return PluginMessage.builder()
                .id(id)
                .headers(headers)
                .body(body)
                .contentType("application/json")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private AdapterPlugin createInstance() {
        try {
            return pluginClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create plugin instance", e);
        }
    }
    
    private void ensureInitialized() {
        if (pluginInstance == null) {
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
        public void onError(Exception e) {
            log.error("Received error", e);
            errors.add(e);
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
            
            while (messages.size() < count && System.currentTimeMillis() < deadline) {
                CompletableFuture<Void> waiter = new CompletableFuture<>();
                synchronized (waiters) {
                    waiters.add(waiter);
                }
                
                try {
                    waiter.get(deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    // Timeout or interruption
                    break;
                }
            }
            
            return getMessages();
        }
        
        private void notifyWaiters() {
            synchronized (waiters) {
                waiters.forEach(w -> w.complete(null));
                waiters.clear();
            }
        }
    }
}