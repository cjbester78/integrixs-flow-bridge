package com.integrixs.backend.api.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for router configuration
 */
public class RouterConfigDTO {
    private String routerId;
    private String routerType;
    private List<Map<String, Object>> choices;
    private Map<String, String> contentRoutes;
    private String extractionPath;
    private String sourceType;
    private List<String> recipients;
    private String recipientListVariable;
    private List<String> roundRobinTargets;
    private Map<String, Integer> weightedTargets;

    // Default constructor
    public RouterConfigDTO() {
    }

    public String getRouterId() {
        return routerId;
    }

    public void setRouterId(String routerId) {
        this.routerId = routerId;
    }

    public String getRouterType() {
        return routerType;
    }

    public void setRouterType(String routerType) {
        this.routerType = routerType;
    }

    public String getExtractionPath() {
        return extractionPath;
    }

    public void setExtractionPath(String extractionPath) {
        this.extractionPath = extractionPath;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
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

    public List<Map<String, Object>> getChoices() {
        return choices;
    }

    public void setChoices(List<Map<String, Object>> choices) {
        this.choices = choices;
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
