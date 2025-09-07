package ${package};

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Outbound handler for ${pluginName}
 */
@Slf4j
public class ${pluginClass}OutboundHandler implements OutboundHandler {
    
    private final Map<String, Object> configuration;
    
    public ${pluginClass}OutboundHandler(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public SendResult send(PluginMessage message) throws PluginException {
        try {
            // TODO: Implement message sending logic
            log.debug("Sending message: {}", message.getId());
            
            // Example: Extract data from message
            Map<String, Object> body = (Map<String, Object>) message.getBody();
            
            // TODO: Send to external system
            String externalId = sendToExternalSystem(body);
            
            return SendResult.builder()
                    .successful(true)
                    .messageId(message.getId())
                    .externalMessageId(externalId)
                    .response("Message sent successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return SendResult.failure(message.getId(), e.getMessage());
        }
    }
    
    @Override
    public BatchSendResult sendBatch(List<PluginMessage> messages) throws PluginException {
        // Default implementation - send messages one by one
        List<SendResult> results = messages.stream()
                .map(this::send)
                .collect(Collectors.toList());
        
        long successCount = results.stream()
                .filter(SendResult::isSuccessful)
                .count();
        
        return BatchSendResult.builder()
                .totalMessages(messages.size())
                .successCount((int) successCount)
                .failureCount(messages.size() - (int) successCount)
                .results(results)
                .build();
    }
    
    @Override
    public boolean supportsBatch() {
        // TODO: Return true if your service supports batch operations
        return false;
    }
    
    private String sendToExternalSystem(Map<String, Object> data) {
        // TODO: Implement actual sending logic
        return UUID.randomUUID().toString();
    }
}