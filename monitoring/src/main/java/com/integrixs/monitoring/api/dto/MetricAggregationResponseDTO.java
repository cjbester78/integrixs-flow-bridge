package com.integrixs.monitoring.api.dto;


/**
 * DTO for metric aggregation response
 */
public class MetricAggregationResponseDTO {
    private boolean success;
    private String metricName;
    private String aggregationType;
    private double value;
    private Long startTime;
    private Long endTime;
    private String errorMessage;

    // Constructors
    public MetricAggregationResponseDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MetricAggregationResponseDTO dto = new MetricAggregationResponseDTO();

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder metricName(String metricName) {
            dto.metricName = metricName;
            return this;
        }

        public Builder aggregationType(String aggregationType) {
            dto.aggregationType = aggregationType;
            return this;
        }

        public Builder value(double value) {
            dto.value = value;
            return this;
        }

        public Builder startTime(Long startTime) {
            dto.startTime = startTime;
            return this;
        }

        public Builder endTime(Long endTime) {
            dto.endTime = endTime;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            dto.errorMessage = errorMessage;
            return this;
        }

        public MetricAggregationResponseDTO build() {
            return dto;
        }
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    public double getValue() {
        return value;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
