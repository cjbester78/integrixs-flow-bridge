package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class ConnectionTestResponse {

    private boolean success;
    private String message;

    private List<ConnectionDiagnostic> diagnostics = new ArrayList<>();

    private long duration; // in milliseconds
    private LocalDateTime timestamp;

    private Map<String, Object> metadata = new HashMap<>();

    // Connection health score(0-100)
    private Integer healthScore;

    // Recommendations for connection improvement
    private List<String> recommendations = new ArrayList<>();

    // Default constructor
    public ConnectionTestResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ConnectionDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<ConnectionDiagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static ConnectionTestResponseBuilder builder() {
        return new ConnectionTestResponseBuilder();
    }

    public static class ConnectionTestResponseBuilder {
        private boolean success;
        private String message;
        private List<ConnectionDiagnostic> diagnostics = new ArrayList<>();
        private long duration;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata = new HashMap<>();
        private Integer healthScore;
        private List<String> recommendations = new ArrayList<>();

        public ConnectionTestResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public ConnectionTestResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ConnectionTestResponseBuilder diagnostics(List<ConnectionDiagnostic> diagnostics) {
            this.diagnostics = diagnostics;
            return this;
        }

        public ConnectionTestResponseBuilder addDiagnostic(ConnectionDiagnostic diagnostic) {
            this.diagnostics.add(diagnostic);
            return this;
        }

        public ConnectionTestResponseBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public ConnectionTestResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ConnectionTestResponseBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ConnectionTestResponseBuilder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public ConnectionTestResponseBuilder healthScore(Integer healthScore) {
            this.healthScore = healthScore;
            return this;
        }

        public ConnectionTestResponseBuilder recommendations(List<String> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public ConnectionTestResponseBuilder addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
            return this;
        }

        public ConnectionTestResponse build() {
            ConnectionTestResponse response = new ConnectionTestResponse();
            response.setSuccess(this.success);
            response.setMessage(this.message);
            response.setDiagnostics(this.diagnostics);
            response.setDuration(this.duration);
            response.setTimestamp(this.timestamp);
            response.setMetadata(this.metadata);
            response.setHealthScore(this.healthScore);
            response.setRecommendations(this.recommendations);
            return response;
        }
    }
}
