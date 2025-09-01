package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Component interaction heatmap showing data flow between components.
 */
@Data
public class ComponentInteractionHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Map<String, InteractionMetrics>> interactions;
    private List<String> components;
    private List<HotPath> hotPaths;
}