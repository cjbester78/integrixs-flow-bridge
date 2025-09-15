package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Flow execution heatmap data.
 */
@Data
public class FlowExecutionHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String flowId;
    private String granularity; // HOURLY, DAILY
    private List<List<HeatmapCell>> grid;
    private Map<String, Object> statistics;
    private List<PeakPeriodInfo> peakPeriods;
}
