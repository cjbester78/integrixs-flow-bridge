package com.integrixs.webclient.domain.service;

import com.integrixs.webclient.domain.model.InboundMessage;

import java.util.List;

/**
 * Domain service interface for routing web-based inbound messages to appropriate flows
 */
public interface WebMessageRoutingService {

    /**
     * Route message to appropriate flow
     * @param message The message to route
     * @return Flow ID if routed successfully
     */
    String routeMessage(InboundMessage message);

    /**
     * Find flows that can handle this message
     * @param message The message to check
     * @return List of compatible flow IDs
     */
    List<String> findCompatibleFlows(InboundMessage message);

    /**
     * Check if flow can handle message
     * @param message The message to check
     * @param flowId Flow ID
     * @return true if compatible
     */
    boolean isFlowCompatible(InboundMessage message, String flowId);

    /**
     * Get default flow for message type
     * @param messageType Message type
     * @return Default flow ID if configured
     */
    String getDefaultFlow(InboundMessage.MessageType messageType);

    /**
     * Register routing rule
     * @param pattern Message pattern
     * @param flowId Target flow ID
     */
    void registerRoutingRule(String pattern, String flowId);

    /**
     * Unregister routing rule
     * @param pattern Message pattern
     */
    void unregisterRoutingRule(String pattern);
}
