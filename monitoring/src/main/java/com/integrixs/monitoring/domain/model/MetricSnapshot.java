package com.integrixs.monitoring.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a metric snapshot
 */
public class MetricSnapshot {
    private String metricId;
    private String metricName;
    private MetricType metricType;
    private double value;
    private String unit;
    private LocalDateTime timestamp;
    private String source;
    private String domainType;
    private String domainReferenceId;
    private Map<String, String> tags = new HashMap<>();
    private Map<String, Object> dimensions = new HashMap<>();

    // Constructors
    public MetricSnapshot() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MetricSnapshot metric = new MetricSnapshot();

        public Builder metricId(String metricId) {
            metric.metricId = metricId;
            return this;
        }

        public Builder metricName(String metricName) {
            metric.metricName = metricName;
            return this;
        }

        public Builder metricType(MetricType metricType) {
            metric.metricType = metricType;
            return this;
        }

        public Builder value(double value) {
            metric.value = value;
            return this;
        }

        public Builder unit(String unit) {
            metric.unit = unit;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            metric.timestamp = timestamp;
            return this;
        }

        public Builder source(String source) {
            metric.source = source;
            return this;
        }

        public Builder domainType(String domainType) {
            metric.domainType = domainType;
            return this;
        }

        public Builder domainReferenceId(String domainReferenceId) {
            metric.domainReferenceId = domainReferenceId;
            return this;
        }

        public Builder tags(Map<String, String> tags) {
            metric.tags = tags != null ? tags : new HashMap<>();
            return this;
        }

        public Builder dimensions(Map<String, Object> dimensions) {
            metric.dimensions = dimensions != null ? dimensions : new HashMap<>();
            return this;
        }

        public MetricSnapshot build() {
            return metric;
        }
    }

    // Getters and Setters
    public String getMetricId() {
        return metricId;
    }

    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, Object> dimensions) {
        this.dimensions = dimensions;
    }

    /**
     * Metric types
     */
    public enum MetricType {
        COUNTER,       // Monotonically increasing value
        GAUGE,         // Point - in - time value
        HISTOGRAM,     // Distribution of values
        SUMMARY,       // Statistical summary
        TIMER           // Duration measurement
    }

    /**
     * Add tag
     */
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }

    /**
     * Add dimension
     */
    public void addDimension(String key, Object value) {
        this.dimensions.put(key, value);
    }

    /**
     * Check if metric value exceeds threshold
     */
    public boolean exceedsThreshold(double threshold) {
        return this.value > threshold;
    }

    /**
     * Get formatted metric name with tags
     */
    public String getFormattedName() {
        if(tags.isEmpty()) {
            return metricName;
        }

        StringBuilder formatted = new StringBuilder(metricName);
        formatted.append(" {");
        tags.forEach((k, v) -> formatted.append(k).append(" = \"").append(v).append("\","));
        formatted.deleteCharAt(formatted.length() - 1); // Remove trailing comma
        formatted.append("}");
        return formatted.toString();
    }
}
