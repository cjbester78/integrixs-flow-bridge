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
}
