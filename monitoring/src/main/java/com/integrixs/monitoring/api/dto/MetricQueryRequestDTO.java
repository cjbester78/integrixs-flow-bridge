package com.integrixs.monitoring.api.dto;


import java.util.Map;

/**
 * DTO for metric query request
 */
public class MetricQueryRequestDTO {
    private String metricName;
    private String metricType;
    private Map<String, String> tags;
    private Long startTime;
    private Long endTime;
    private Integer limit;
    private String orderBy;


    // Getters
    public String getMetricName() {
        return metricName;
    }

    public String getMetricType() {
        return metricType;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getOrderBy() {
        return orderBy;
    }

    // Setters
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

}
