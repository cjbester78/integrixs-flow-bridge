package com.integrixs.backend.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Domain model for router configuration
 */
@Data
@NoArgsConstructor
public class RouterConfiguration {
    private String routerId;
    private RouterType routerType;
    private List<RouteChoice> choices;
    private Map<String, String> contentRoutes;
    private String extractionPath;
    private SourceType sourceType;
    private List<String> recipients;
    private String recipientListVariable;
    private List<String> roundRobinTargets;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private Map<String, Integer> weightedTargets;
    
    /**
     * Router types
     */
    public enum RouterType {
        CHOICE,         // If-else style routing
        CONTENT_BASED,  // Route based on message content
        RECIPIENT_LIST, // Send to multiple recipients
        ROUND_ROBIN,    // Distribute load in round-robin fashion
        WEIGHTED        // Distribute based on weights
    }
    
    /**
     * Source types for value extraction
     */
    public enum SourceType {
        HEADER,
        VARIABLE,
        XPATH,
        JSONPATH,
        CONSTANT
    }
    
    /**
     * Route choice for choice router
     */
    @Data
    @NoArgsConstructor
    public static class RouteChoice {
        private String condition;
        private String targetStepId;
        private boolean isDefault;
        
        public RouteChoice(String condition, String targetStepId, boolean isDefault) {
            this.condition = condition;
            this.targetStepId = targetStepId;
            this.isDefault = isDefault;
        }
    }
}