package com.integrixs.backend.dto;

import com.integrixs.backend.service.TransactionalPackageCreationService.PackageCreationContext;
import com.integrixs.backend.service.TransactionalPackageCreationService.PackageCreationStatus;
import com.integrixs.data.model.IntegrationFlow;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Result of package creation operation
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageCreationResult {

    private boolean success;
    private UUID correlationId;
    private UUID flowId;
    private String flowName;
    private PackageCreationStatus status;
    private String message;
    private String errorMessage;
    private Map<String, Object> createdResources;
    private List<String> checkpoints;
    private int progress;
    private String currentStep;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMillis;
    private IntegrationFlow flow;

    private PackageCreationResult() {
    }

    /**
     * Create success result
     */
    public static PackageCreationResult success(IntegrationFlow flow, PackageCreationContext context) {
        PackageCreationResult result = new PackageCreationResult();
        result.success = true;
        result.flow = flow;
        result.flowId = flow.getId();
        result.flowName = flow.getName();
        result.correlationId = context.getCorrelationId();
        result.status = context.getStatus();
        result.message = "Package created successfully";
        result.createdResources = context.getCreatedResources();
        result.checkpoints = context.getCheckpoints();
        result.progress = 100;
        result.currentStep = "Completed";
        result.startTime = context.getStartTime();
        result.endTime = context.getEndTime();

        if(context.getStartTime() != null && context.getEndTime() != null) {
            result.durationMillis = Duration.between(context.getStartTime(), context.getEndTime()).toMillis();
        }

        return result;
    }

    /**
     * Create failure result
     */
    public static PackageCreationResult failure(String errorMessage, PackageCreationContext context) {
        PackageCreationResult result = new PackageCreationResult();
        result.success = false;
        result.correlationId = context.getCorrelationId();
        result.status = context.getStatus();
        result.message = "Package creation failed";
        result.errorMessage = errorMessage;
        result.createdResources = context.getCreatedResources();
        result.checkpoints = context.getCheckpoints();
        result.progress = context.getProgress();
        result.currentStep = context.getCurrentStep();
        result.startTime = context.getStartTime();
        result.endTime = context.getEndTime();

        if(context.getStartTime() != null && context.getEndTime() != null) {
            result.durationMillis = Duration.between(context.getStartTime(), context.getEndTime()).toMillis();
        }

        return result;
    }

    /**
     * Create in - progress result
     */
    public static PackageCreationResult inProgress(PackageCreationContext context) {
        PackageCreationResult result = new PackageCreationResult();
        result.success = false;
        result.correlationId = context.getCorrelationId();
        result.status = PackageCreationStatus.IN_PROGRESS;
        result.message = "Package creation in progress";
        result.progress = context.getProgress();
        result.currentStep = context.getCurrentStep();
        result.checkpoints = context.getCheckpoints();
        result.startTime = context.getStartTime();

        return result;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID getFlowId() {
        return flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public PackageCreationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, Object> getCreatedResources() {
        return createdResources;
    }

    public List<String> getCheckpoints() {
        return checkpoints;
    }

    public int getProgress() {
        return progress;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }
}
