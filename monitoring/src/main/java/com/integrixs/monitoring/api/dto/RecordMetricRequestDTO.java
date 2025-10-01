package com.integrixs.monitoring.api.dto;


import java.util.Map;

/**
 * DTO for record metric request
 */
public class RecordMetricRequestDTO {
    private String metricName;
    private String metricType;
    private double value;
    private String unit;
    private String source;
    private String domainType;
    private String domainReferenceId;
    private Map<String, String> tags;
    private Map<String, Object> dimensions;


    // Getters
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
