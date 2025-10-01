package com.integrixs.monitoring.api.dto;


import java.util.Map;

/**
 * DTO for metric aggregation request
 */
public class MetricAggregationRequestDTO {
    private String metricName;
    private String aggregationType;
    private Long startTime;
    private Long endTime;
    private Map<String, String> tags;


    // Getters
    public String getMetricName() {
        return metricName;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    // Setters
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

}
