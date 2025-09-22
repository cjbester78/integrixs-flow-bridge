package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Component interaction heatmap showing data flow between components.
 */
public class ComponentInteractionHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Map<String, InteractionMetrics>> interactions;
    private List<String> components;
    private List<HotPath> hotPaths;
    private double[][] interactionMatrix;
    private Map<String, Double> componentCentrality;
    private List<List<String>> criticalPaths;
    private Map<String, Object> statistics;

    // Default constructor
    public ComponentInteractionHeatmap() {
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public List<HotPath> getHotPaths() {
        return hotPaths;
    }

    public void setHotPaths(List<HotPath> hotPaths) {
        this.hotPaths = hotPaths;
    }

    public Map<String, Map<String, InteractionMetrics>> getInteractions() {
        return interactions;
    }

    public void setInteractions(Map<String, Map<String, InteractionMetrics>> interactions) {
        this.interactions = interactions;
    }

    // Alias method for services expecting this method
    public void setAnalysisPeriodDays(int days) {
        // This method is used by services to set analysis period
        // The actual period is determined by startTime and endTime
        // This is here for compatibility
    }

    public double[][] getInteractionMatrix() {
        return interactionMatrix;
    }

    public void setInteractionMatrix(double[][] interactionMatrix) {
        this.interactionMatrix = interactionMatrix;
    }

    public Map<String, Double> getComponentCentrality() {
        return componentCentrality;
    }

    public void setComponentCentrality(Map<String, Double> componentCentrality) {
        this.componentCentrality = componentCentrality;
    }

    public List<List<String>> getCriticalPaths() {
        return criticalPaths;
    }

    public void setCriticalPaths(List<List<String>> criticalPaths) {
        this.criticalPaths = criticalPaths;
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }
}
