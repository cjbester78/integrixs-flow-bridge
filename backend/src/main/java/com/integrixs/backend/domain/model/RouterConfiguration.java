package com.integrixs.backend.domain.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Domain model for router configuration
 */
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
    private AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private Map<String, Integer> weightedTargets;

    /**
     * Router types
     */
    public enum RouterType {
        CHOICE,        // If - else style routing
        CONTENT_BASED, // Route based on message content
        RECIPIENT_LIST, // Send to multiple recipients
        ROUND_ROBIN,   // Distribute load in round - robin fashion
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
    public static class RouteChoice {
        private String condition;
        private String targetStepId;
        private boolean isDefault;

        public RouteChoice() {
        }

        public RouteChoice(String condition, String targetStepId, boolean isDefault) {
            this.condition = condition;
            this.targetStepId = targetStepId;
            this.isDefault = isDefault;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getTargetStepId() {
            return targetStepId;
        }

        public void setTargetStepId(String targetStepId) {
            this.targetStepId = targetStepId;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }
    }

    // Default constructor
    public RouterConfiguration() {
    }

    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId;
    }

    public RouterType getRouterType() {
        return routerType;
    }

    public void setRouterType(RouterType routerType) {
        this.routerType = routerType;
    }

    public List<RouteChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<RouteChoice> choices) {
        this.choices = choices;
    }

    public String getExtractionPath() {
        return extractionPath;
    }

    public void setExtractionPath(String extractionPath) {
        this.extractionPath = extractionPath;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getRecipientListVariable() {
        return recipientListVariable;
    }

    public void setRecipientListVariable(String recipientListVariable) {
        this.recipientListVariable = recipientListVariable;
    }

    public List<String> getRoundRobinTargets() {
        return roundRobinTargets;
    }

    public void setRoundRobinTargets(List<String> roundRobinTargets) {
        this.roundRobinTargets = roundRobinTargets;
    }

    public AtomicInteger getRoundRobinIndex() {
        return roundRobinIndex;
    }

    public void setRoundRobinIndex(AtomicInteger roundRobinIndex) {
        this.roundRobinIndex = roundRobinIndex;
    }

    public Map<String, String> getContentRoutes() {
        return contentRoutes;
    }

    public void setContentRoutes(Map<String, String> contentRoutes) {
        this.contentRoutes = contentRoutes;
    }

    public Map<String, Integer> getWeightedTargets() {
        return weightedTargets;
    }

    public void setWeightedTargets(Map<String, Integer> weightedTargets) {
        this.weightedTargets = weightedTargets;
    }
}
