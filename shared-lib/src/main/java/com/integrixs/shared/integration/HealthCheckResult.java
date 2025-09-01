package com.integrixs.shared.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResult {
    private HealthStatus status;
    private String message;
    @Builder.Default
    private Map<String, ComponentHealth> components = new HashMap<>();
    
    public enum HealthStatus {
        UP,
        DOWN,
        DEGRADED,
        UNKNOWN
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentHealth {
        private String name;
        private HealthStatus status;
        private String message;
        private Map<String, Object> details;
    }
}