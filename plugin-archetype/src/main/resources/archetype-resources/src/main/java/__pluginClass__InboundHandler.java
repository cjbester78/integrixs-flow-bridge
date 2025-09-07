package ${package};

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Inbound handler for ${pluginName}
 */
@Slf4j
public class ${pluginClass}InboundHandler implements InboundHandler {
    
    private final Map<String, Object> configuration;
    private ScheduledExecutorService scheduler;
    private MessageCallback callback;
    private boolean listening = false;
    
    public ${pluginClass}InboundHandler(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public void startListening(MessageCallback callback) throws PluginException {
        if (listening) {
            throw new PluginException("Already listening");
        }
        
        this.callback = callback;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // TODO: Configure polling interval from configuration
        int pollingInterval = 60; // seconds
        
        scheduler.scheduleWithFixedDelay(
            this::pollForData,
            0,
            pollingInterval,
            TimeUnit.SECONDS
        );
        
        listening = true;
        log.info("Started listening for data (polling every {} seconds)", pollingInterval);
    }
    
    @Override
    public void stopListening() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        listening = false;
        log.info("Stopped listening for data");
    }
    
    @Override
    public PollingResult poll() {
        // Manual polling implementation
        List<PluginMessage> messages = fetchData();
        return PollingResult.builder()
                .messages(messages)
                .hasMore(false)
                .build();
    }
    
    @Override
    public boolean isListening() {
        return listening;
    }
    
    private void pollForData() {
        try {
            List<PluginMessage> messages = fetchData();
            for (PluginMessage message : messages) {
                callback.onMessage(message);
            }
        } catch (Exception e) {
            log.error("Error polling for data", e);
            callback.onError(e);
        }
    }
    
    private List<PluginMessage> fetchData() {
        // TODO: Implement data fetching logic
        List<PluginMessage> messages = new ArrayList<>();
        
        // Example: Create a test message
        messages.add(PluginMessage.builder()
                .headers(Map.of(
                    "source", "${pluginId}",
                    "timestamp", System.currentTimeMillis()
                ))
                .body(Map.of(
                    "example", "data",
                    "value", 123
                ))
                .contentType("application/json")
                .build());
        
        return messages;
    }
}