package com.integrixs.backend.plugin.api;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Health status of an adapter
 */
@Data
@Builder
public class HealthStatus {
    
    /**
     * Overall health state
     */
    private HealthState state;
    
    /**
     * Human-readable status message
     */
    private String message;
    
    /**
     * Timestamp of health check
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Individual component health checks
     */
    private List<ComponentHealth> components;
    
    /**
     * Performance metrics
     */
    private PerformanceMetrics metrics;
    
    /**
     * Additional details
     */
    private Map<String, Object> details;
    
    /**
     * Health states
     */
    public enum HealthState {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }
    
    /**
     * Component health information
     */
    @Data
    @Builder
    public static class ComponentHealth {
        private String name;
        private HealthState state;
        private String message;
        private Map<String, Object> details;
    }
    
    /**
     * Performance metrics
     */
    @Data
    @Builder
    public static class PerformanceMetrics {
        private Long messagesProcessed;
        private Long errors;
        private Double averageResponseTimeMs;
        private Double successRate;
        private Long activeConnections;
        private Map<String, Number> customMetrics;
    }
    
    /**
     * Static factory methods
     */
    public static HealthStatus healthy() {
        return HealthStatus.builder()
                .state(HealthState.HEALTHY)
                .message("Adapter is healthy")
                .build();
    }
    
    public static HealthStatus unhealthy(String message) {
        return HealthStatus.builder()
                .state(HealthState.UNHEALTHY)
                .message(message)
                .build();
    }
}