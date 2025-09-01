package com.integrixs.monitoring.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a metric snapshot
 */
@Data
@Builder
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
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();
    @Builder.Default
    private Map<String, Object> dimensions = new HashMap<>();
    
    /**
     * Metric types
     */
    public enum MetricType {
        COUNTER,        // Monotonically increasing value
        GAUGE,          // Point-in-time value
        HISTOGRAM,      // Distribution of values
        SUMMARY,        // Statistical summary
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
        if (tags.isEmpty()) {
            return metricName;
        }
        
        StringBuilder formatted = new StringBuilder(metricName);
        formatted.append("{");
        tags.forEach((k, v) -> formatted.append(k).append("=\"").append(v).append("\","));
        formatted.deleteCharAt(formatted.length() - 1); // Remove trailing comma
        formatted.append("}");
        return formatted.toString();
    }
}