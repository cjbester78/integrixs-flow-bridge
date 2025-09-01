package com.integrixs.shared.integration;

import java.util.Map;

/**
 * Interface for routing messages between modules
 * Used by WebClient to route messages to Engine
 */
public interface MessageRoutingService {
    
    /**
     * Route a message to the appropriate flow
     * @param flowId The flow ID to route to
     * @param message The message payload
     * @return The execution result
     */
    ExecutionResult routeMessage(String flowId, Map<String, Object> message);
    
    /**
     * Route a message asynchronously
     * @param flowId The flow ID to route to
     * @param message The message payload
     * @param callback Callback for async result
     */
    void routeMessageAsync(String flowId, Map<String, Object> message, ExecutionCallback callback);
    
    /**
     * Check if a flow can accept messages
     * @param flowId The flow ID
     * @return True if the flow is active and can accept messages
     */
    boolean canRouteToFlow(String flowId);
    
    /**
     * Callback interface for async routing
     */
    interface ExecutionCallback {
        void onSuccess(ExecutionResult result);
        void onFailure(String error);
    }
}