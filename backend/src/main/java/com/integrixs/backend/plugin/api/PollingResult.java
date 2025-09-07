package com.integrixs.backend.plugin.api;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Result of a polling operation
 */
@Data
@Builder
public class PollingResult {
    
    /**
     * Messages retrieved during polling
     */
    private List<PluginMessage> messages;
    
    /**
     * Whether more messages are available
     */
    private boolean hasMore;
    
    /**
     * Token/marker for next poll (for pagination)
     */
    private String nextToken;
    
    /**
     * Number of messages retrieved
     */
    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }
    
    /**
     * Create empty result
     */
    public static PollingResult empty() {
        return PollingResult.builder()
                .messages(List.of())
                .hasMore(false)
                .build();
    }
}