package com.integrixs.webclient.infrastructure.service;

import com.integrixs.webclient.domain.model.InboundMessage;
import com.integrixs.webclient.domain.service.WebMessageRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Implementation of web message routing service for handling inbound web messages
 */
@Service
public class WebMessageRoutingServiceImpl implements WebMessageRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(WebMessageRoutingServiceImpl.class);

    // In - memory routing rules(in production, this would be database - backed)
    private final Map<String, String> routingRules = new ConcurrentHashMap<>();
    private final Map<InboundMessage.MessageType, String> defaultFlows = new ConcurrentHashMap<>();

    public WebMessageRoutingServiceImpl() {
        // Initialize default routing rules
        initializeDefaultRules();
    }

    @Override
    public String routeMessage(InboundMessage message) {
        logger.info("Routing message {} of type {}", message.getMessageId(), message.getMessageType());

        // Check if message already has a flow ID
        if(message.getFlowId() != null && !message.getFlowId().isEmpty()) {
            return message.getFlowId();
        }

        // Try to find compatible flows
        List<String> compatibleFlows = findCompatibleFlows(message);
        if(!compatibleFlows.isEmpty()) {
            String flowId = compatibleFlows.get(0); // Use first compatible flow
            logger.info("Routed message {} to flow {}", message.getMessageId(), flowId);
            return flowId;
        }

        // Fall back to default flow for message type
        String defaultFlow = getDefaultFlow(message.getMessageType());
        if(defaultFlow != null) {
            logger.info("Using default flow {} for message type {}", defaultFlow, message.getMessageType());
            return defaultFlow;
        }

        logger.warn("No flow found for message {} of type {}", message.getMessageId(), message.getMessageType());
        return null;
    }

    @Override
    public List<String> findCompatibleFlows(InboundMessage message) {
        List<String> compatibleFlows = new ArrayList<>();

        // Check routing rules based on message attributes
        String messagePattern = buildMessagePattern(message);

        for(Map.Entry<String, String> rule : routingRules.entrySet()) {
            if(matchesPattern(messagePattern, rule.getKey())) {
                compatibleFlows.add(rule.getValue());
            }
        }

        return compatibleFlows;
    }

    @Override
    public boolean isFlowCompatible(InboundMessage message, String flowId) {
        List<String> compatibleFlows = findCompatibleFlows(message);
        return compatibleFlows.contains(flowId);
    }

    @Override
    public String getDefaultFlow(InboundMessage.MessageType messageType) {
        return defaultFlows.get(messageType);
    }

    @Override
    public void registerRoutingRule(String pattern, String flowId) {
        logger.info("Registering routing rule: {} -> {}", pattern, flowId);
        routingRules.put(pattern, flowId);
    }

    @Override
    public void unregisterRoutingRule(String pattern) {
        logger.info("Unregistering routing rule: {}", pattern);
        routingRules.remove(pattern);
    }

    private void initializeDefaultRules() {
        // Default flows for different message types
        defaultFlows.put(InboundMessage.MessageType.HTTP_REQUEST, "default - http - flow");
        defaultFlows.put(InboundMessage.MessageType.SOAP_REQUEST, "default - soap - flow");
        defaultFlows.put(InboundMessage.MessageType.WEBHOOK, "default - webhook - flow");
        defaultFlows.put(InboundMessage.MessageType.API_CALL, "default - api - flow");

        // Example routing rules
        routingRules.put("source:external - api,type:HTTP_REQUEST", "external - api - flow");
        routingRules.put("source:partner - system,type:WEBHOOK", "partner - webhook - flow");
        routingRules.put("adapterId:soap - adapter-1,type:SOAP_REQUEST", "soap - processing - flow");
    }

    private String buildMessagePattern(InboundMessage message) {
        StringBuilder pattern = new StringBuilder();

        if(message.getSource() != null) {
            pattern.append("source:").append(message.getSource()).append(",");
        }

        if(message.getMessageType() != null) {
            pattern.append("type:").append(message.getMessageType()).append(",");
        }

        if(message.getAdapterId() != null) {
            pattern.append("adapterId:").append(message.getAdapterId()).append(",");
        }

        // Remove trailing comma
        if(pattern.length() > 0 && pattern.charAt(pattern.length() - 1) == ',') {
            pattern.setLength(pattern.length() - 1);
        }

        return pattern.toString();
    }

    private boolean matchesPattern(String messagePattern, String rulePattern) {
        // Simple pattern matching(in production, use more sophisticated matching)
        String[] ruleParts = rulePattern.split(",");
        for(String rulePart : ruleParts) {
            if(!messagePattern.contains(rulePart)) {
                return false;
            }
        }
        return true;
    }
}
