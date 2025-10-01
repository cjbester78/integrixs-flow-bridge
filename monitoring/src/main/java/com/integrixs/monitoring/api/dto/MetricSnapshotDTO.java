package com.integrixs.monitoring.api.dto;


import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for metric snapshot
 */
public class MetricSnapshotDTO {
    private String metricId;
    private String metricName;
    private String metricType;
    private double value;
    private String unit;
    private LocalDateTime timestamp;
    private String source;
    private String domainType;
    private String domainReferenceId;
    private Map<String, String> tags;
    private Map<String, Object> dimensions;

    // Constructors
    public MetricSnapshotDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MetricSnapshotDTO dto = new MetricSnapshotDTO();

        public Builder metricId(String metricId) {
            dto.metricId = metricId;
            return this;
        }

        public Builder metricName(String metricName) {
            dto.metricName = metricName;
            return this;
        }

        public Builder metricType(String metricType) {
            dto.metricType = metricType;
            return this;
        }

        public Builder value(double value) {
            dto.value = value;
            return this;
        }

        public Builder unit(String unit) {
            dto.unit = unit;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            dto.timestamp = timestamp;
            return this;
        }

        public Builder source(String source) {
            dto.source = source;
            return this;
        }

        public Builder domainType(String domainType) {
            dto.domainType = domainType;
            return this;
        }

        public Builder domainReferenceId(String domainReferenceId) {
            dto.domainReferenceId = domainReferenceId;
            return this;
        }

        public Builder tags(Map<String, String> tags) {
            dto.tags = tags;
            return this;
        }

        public Builder dimensions(Map<String, Object> dimensions) {
            dto.dimensions = dimensions;
            return this;
        }

        public MetricSnapshotDTO build() {
            return dto;
        }
    }

    // Getters
    public String getMetricId() {
        return metricId;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getMetricType() {
        return metricType;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    public String getDomainType() {
        return domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Map<String, Object> getDimensions() {
        return dimensions;
    }

    // Setters
    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void setDimensions(Map<String, Object> dimensions) {
        this.dimensions = dimensions;
    }

}
