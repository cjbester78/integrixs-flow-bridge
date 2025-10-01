package com.integrixs.backend.plugin.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Health status of an adapter
 */
public class HealthStatus {

    /**
     * Overall health state
     */
    private HealthState state;

    /**
     * Human - readable status message
     */
    private String message;

    /**
     * Timestamp of health check
     */
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

    // Getters and Setters
    public HealthState getState() {
        return state;
    }

    public void setState(HealthState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<ComponentHealth> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentHealth> components) {
        this.components = components;
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(PerformanceMetrics metrics) {
        this.metrics = metrics;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

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
    public static class ComponentHealth {
        private String name;
        private HealthState state;
        private String message;
        private Map<String, Object> details;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public HealthState getState() {
            return state;
        }

        public void setState(HealthState state) {
            this.state = state;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }

        // Builder pattern
        public static ComponentHealthBuilder builder() {
            return new ComponentHealthBuilder();
        }

        public static class ComponentHealthBuilder {
            private String name;
            private HealthState state;
            private String message;
            private Map<String, Object> details;

            public ComponentHealthBuilder name(String name) {
                this.name = name;
                return this;
            }

            public ComponentHealthBuilder state(HealthState state) {
                this.state = state;
                return this;
            }

            public ComponentHealthBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ComponentHealthBuilder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public ComponentHealth build() {
                ComponentHealth health = new ComponentHealth();
                health.name = this.name;
                health.state = this.state;
                health.message = this.message;
                health.details = this.details;
                return health;
            }
        }
    }

    /**
     * Performance metrics
     */
    public static class PerformanceMetrics {
        private Long messagesProcessed;
        private Long errors;
        private Double averageResponseTimeMs;
        private Double successRate;
        private Long activeConnections;
        private Map<String, Number> customMetrics;

        // Getters and Setters
        public Long getMessagesProcessed() {
            return messagesProcessed;
        }

        public void setMessagesProcessed(Long messagesProcessed) {
            this.messagesProcessed = messagesProcessed;
        }

        public Long getErrors() {
            return errors;
        }

        public void setErrors(Long errors) {
            this.errors = errors;
        }

        public Double getAverageResponseTimeMs() {
            return averageResponseTimeMs;
        }

        public void setAverageResponseTimeMs(Double averageResponseTimeMs) {
            this.averageResponseTimeMs = averageResponseTimeMs;
        }

        public Double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(Double successRate) {
            this.successRate = successRate;
        }

        public Long getActiveConnections() {
            return activeConnections;
        }

        public void setActiveConnections(Long activeConnections) {
            this.activeConnections = activeConnections;
        }

        public Map<String, Number> getCustomMetrics() {
            return customMetrics;
        }

        public void setCustomMetrics(Map<String, Number> customMetrics) {
            this.customMetrics = customMetrics;
        }

        // Builder pattern
        public static PerformanceMetricsBuilder builder() {
            return new PerformanceMetricsBuilder();
        }

        public static class PerformanceMetricsBuilder {
            private Long messagesProcessed;
            private Long errors;
            private Double averageResponseTimeMs;
            private Double successRate;
            private Long activeConnections;
            private Map<String, Number> customMetrics;

            public PerformanceMetricsBuilder messagesProcessed(Long messagesProcessed) {
                this.messagesProcessed = messagesProcessed;
                return this;
            }

            public PerformanceMetricsBuilder errors(Long errors) {
                this.errors = errors;
                return this;
            }

            public PerformanceMetricsBuilder averageResponseTimeMs(Double averageResponseTimeMs) {
                this.averageResponseTimeMs = averageResponseTimeMs;
                return this;
            }

            public PerformanceMetricsBuilder successRate(Double successRate) {
                this.successRate = successRate;
                return this;
            }

            public PerformanceMetricsBuilder activeConnections(Long activeConnections) {
                this.activeConnections = activeConnections;
                return this;
            }

            public PerformanceMetricsBuilder customMetrics(Map<String, Number> customMetrics) {
                this.customMetrics = customMetrics;
                return this;
            }

            public PerformanceMetrics build() {
                PerformanceMetrics metrics = new PerformanceMetrics();
                metrics.messagesProcessed = this.messagesProcessed;
                metrics.errors = this.errors;
                metrics.averageResponseTimeMs = this.averageResponseTimeMs;
                metrics.successRate = this.successRate;
                metrics.activeConnections = this.activeConnections;
                metrics.customMetrics = this.customMetrics;
                return metrics;
            }
        }
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

    // Builder pattern
    public static HealthStatusBuilder builder() {
        return new HealthStatusBuilder();
    }

    public static class HealthStatusBuilder {
        private HealthState state;
        private String message;
        private LocalDateTime timestamp = LocalDateTime.now();
        private List<ComponentHealth> components;
        private PerformanceMetrics metrics;
        private Map<String, Object> details;

        public HealthStatusBuilder state(HealthState state) {
            this.state = state;
            return this;
        }

        public HealthStatusBuilder message(String message) {
            this.message = message;
            return this;
        }

        public HealthStatusBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public HealthStatusBuilder components(List<ComponentHealth> components) {
            this.components = components;
            return this;
        }

        public HealthStatusBuilder metrics(PerformanceMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public HealthStatusBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public HealthStatus build() {
            HealthStatus status = new HealthStatus();
            status.state = this.state;
            status.message = this.message;
            status.timestamp = this.timestamp;
            status.components = this.components;
            status.metrics = this.metrics;
            status.details = this.details;
            return status;
        }
    }
}
