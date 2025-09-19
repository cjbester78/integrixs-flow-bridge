package com.integrixs.shared.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Health check result
 */
public class HealthCheckResult {
    private HealthStatus status;
    private String message;
    private Map<String, ComponentHealth> components = new HashMap<>();

    // Default constructor
    public HealthCheckResult() {
    }

    // All args constructor
    public HealthCheckResult(HealthStatus status, String message, Map<String, ComponentHealth> components) {
        this.status = status;
        this.message = message;
        this.components = components != null ? components : new HashMap<>();
    }

    // Getters
    public HealthStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, ComponentHealth> getComponents() {
        return components;
    }

    // Setters
    public void setStatus(HealthStatus status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setComponents(Map<String, ComponentHealth> components) {
        this.components = components;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HealthStatus status;
        private String message;
        private Map<String, ComponentHealth> components = new HashMap<>();

        public Builder status(HealthStatus status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder components(Map<String, ComponentHealth> components) {
            this.components = components;
            return this;
        }

        public HealthCheckResult build() {
            return new HealthCheckResult(status, message, components);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthCheckResult that = (HealthCheckResult) o;
        return status == that.status &&
               Objects.equals(message, that.message) &&
               Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message, components);
    }

    @Override
    public String toString() {
        return "HealthCheckResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", components=" + components +
                '}';
    }

    public enum HealthStatus {
        UP,
        DOWN,
        DEGRADED,
        UNKNOWN
    }

    public static class ComponentHealth {
        private String name;
        private HealthStatus status;
        private String message;
        private Map<String, Object> details;

        // Default constructor
        public ComponentHealth() {
        }

        // All args constructor
        public ComponentHealth(String name, HealthStatus status, String message, Map<String, Object> details) {
            this.name = name;
            this.status = status;
            this.message = message;
            this.details = details;
        }

        // Getters
        public String getName() {
            return name;
        }

        public HealthStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        // Setters
        public void setName(String name) {
            this.name = name;
        }

        public void setStatus(HealthStatus status) {
            this.status = status;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private HealthStatus status;
            private String message;
            private Map<String, Object> details;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder status(HealthStatus status) {
                this.status = status;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public ComponentHealth build() {
                return new ComponentHealth(name, status, message, details);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComponentHealth that = (ComponentHealth) o;
            return Objects.equals(name, that.name) &&
                   status == that.status &&
                   Objects.equals(message, that.message) &&
                   Objects.equals(details, that.details);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, status, message, details);
        }

        @Override
        public String toString() {
            return "ComponentHealth{" +
                    "name='" + name + '\'' +
                    ", status=" + status +
                    ", message='" + message + '\'' +
                    ", details=" + details +
                    '}';
        }
    }
}
