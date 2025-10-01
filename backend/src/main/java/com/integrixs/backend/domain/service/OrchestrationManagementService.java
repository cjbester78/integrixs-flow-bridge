package com.integrixs.backend.domain.service;

import com.integrixs.data.model.IntegrationFlow;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for orchestration business logic
 */
@Service
public class OrchestrationManagementService {

    /**
     * Validate that a flow can be orchestrated
     * @param flow The flow to validate
     * @throws IllegalStateException if flow cannot be orchestrated
     */
    public void validateOrchestrationFlow(IntegrationFlow flow) {
        if(flow == null) {
            throw new IllegalArgumentException("Flow cannot be null");
        }

        if(!flow.isActive()) {
            throw new IllegalStateException("Cannot orchestrate inactive flow: " + flow.getName());
        }

        if(flow.getDeployedAt() == null) {
            throw new IllegalStateException("Cannot orchestrate undeployed flow: " + flow.getName());
        }

        if(flow.getInboundAdapterId() == null) {
            throw new IllegalStateException("Source adapter is required for orchestration");
        }

        if(flow.getOutboundAdapterId() == null) {
            throw new IllegalStateException("Target adapter is required for orchestration");
        }
    }

    /**
     * Generate unique execution ID
     * @return Unique execution ID
     */
    public String generateExecutionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Check if execution can be cancelled
     * @param status Current execution status
     * @return true if can be cancelled
     */
    public boolean canCancelExecution(String status) {
        return "RUNNING".equals(status) || "PENDING".equals(status);
    }

    /**
     * Calculate execution duration
     * @param startTime Start time
     * @param endTime End time(null if still running)
     * @return Duration in milliseconds
     */
    public long calculateExecutionDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if(startTime == null) {
            return 0;
        }

        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMillis();
    }

    /**
     * Determine execution status based on state
     * @param hasError Whether execution has errors
     * @param isComplete Whether execution is complete
     * @param isCancelled Whether execution was cancelled
     * @return Execution status
     */
    public String determineExecutionStatus(boolean hasError, boolean isComplete, boolean isCancelled) {
        if(isCancelled) {
            return "CANCELLED";
        }
        if(hasError) {
            return "FAILED";
        }
        if(isComplete) {
            return "COMPLETED";
        }
        return "RUNNING";
    }

    /**
     * Validate input data for orchestration
     * @param inputData The input data
     * @throws IllegalArgumentException if input is invalid
     */
    public void validateInputData(Object inputData) {
        if(inputData == null) {
            throw new IllegalArgumentException("Input data cannot be null for orchestration");
        }

        // Additional validation based on data type
        if(inputData instanceof String && ((String) inputData).trim().isEmpty()) {
            throw new IllegalArgumentException("Input data cannot be empty");
        }
    }

    /**
     * Get step display name
     * @param step The step code
     * @return Display name
     */
    public String getStepDisplayName(String step) {
        switch(step) {
            case "INITIALIZE":
                return "Initializing Process";
            case "LOAD_COMPONENTS":
                return "Loading Business Components";
            case "INITIALIZE_ADAPTERS":
                return "Initializing Adapters";
            case "EXECUTE_TRANSFORMATIONS":
                return "Executing Transformations";
            case "PROCESS_TARGETS":
                return "Processing Target Systems";
            case "COMPLETE":
                return "Completing Process";
            default:
                return step;
        }
    }

    /**
     * Check if step is critical
     * @param step The step code
     * @return true if step is critical
     */
    public boolean isStepCritical(String step) {
        return "INITIALIZE_ADAPTERS".equals(step) ||
               "EXECUTE_TRANSFORMATIONS".equals(step);
    }

    /**
     * Format log message with timestamp
     * @param message The log message
     * @return Formatted log message
     */
    public String formatLogMessage(String message) {
        return LocalDateTime.now() + ": " + message;
    }
}
