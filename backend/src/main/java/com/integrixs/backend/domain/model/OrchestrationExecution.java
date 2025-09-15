package com.integrixs.backend.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for orchestration execution
 */
@Data
@NoArgsConstructor
public class OrchestrationExecution {
    private String executionId;
    private String flowId;
    private String flowName;
    private String status = "PENDING";
    private String currentStep;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Object inputData;
    private Object transformedData;
    private Object outputData;
    private Map<String, Object> executionContext = new HashMap<>();
    private List<String> logs = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Add a log entry
     * @param message The log message
     */
    public void addLog(String message) {
        this.logs.add(message);
    }

    /**
     * Update execution status
     * @param status New status
     */
    public void updateStatus(String status) {
        this.status = status;
        if("RUNNING".equals(status) && startTime == null) {
            this.startTime = LocalDateTime.now();
        } else if(("COMPLETED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status))
                   && endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }

    /**
     * Add context data
     * @param key Context key
     * @param value Context value
     */
    public void addContext(String key, Object value) {
        this.executionContext.put(key, value);
    }

    /**
     * Add metadata
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Check if execution is in progress
     * @return true if in progress
     */
    public boolean isInProgress() {
        return "RUNNING".equals(status) || "PENDING".equals(status);
    }

    /**
     * Check if execution is complete
     * @return true if complete
     */
    public boolean isComplete() {
        return "COMPLETED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status);
    }
}
