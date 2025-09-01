package com.integrixs.shared.dto.log;

import com.integrixs.data.model.SystemLog;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a group of correlated logs.
 */
@Data
public class CorrelatedLogGroup {
    
    private String correlationId;
    private String flowId;
    private List<SystemLog> logs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private List<String> involvedComponents;
    private Map<String, List<TimelineEntry>> componentTimeline;
    private List<ErrorChainEntry> errorChain;
    private Map<String, Object> statistics;
    private String relationType; // For related logs
    
    /**
     * Timeline entry for a component.
     */
    @Data
    public static class TimelineEntry {
        private LocalDateTime timestamp;
        private SystemLog.LogLevel level;
        private String message;
        private String category;
    }
    
    /**
     * Error chain entry for debugging.
     */
    @Data
    public static class ErrorChainEntry {
        private LocalDateTime timestamp;
        private String component;
        private String message;
        private String stackTrace;
        private Duration propagationDelay;
    }
}