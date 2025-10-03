package com.integrixs.backend.api.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for orchestration execution details
 */
public class OrchestrationExecutionDTO {
    private String executionId;
    private String flowId;
    private String flowName;
    private String status;
    private String currentStep;
    private String currentStepDisplay;
    private String stateMachineState;
    private String lastEvent;
    private String nextPossibleStates;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration; // milliseconds
    private Object inputData;
    private Object outputData;
    private List<String> logs;
    private Map<String, Object> metadata = new HashMap<>();
    private boolean inProgress;
    private boolean complete;

    // Default constructor
    public OrchestrationExecutionDTO() {
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public String getCurrentStepDisplay() {
        return currentStepDisplay;
    }

    public void setCurrentStepDisplay(String currentStepDisplay) {
        this.currentStepDisplay = currentStepDisplay;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Object getInputData() {
        return inputData;
    }

    public void setInputData(Object inputData) {
        this.inputData = inputData;
    }

    public Object getOutputData() {
        return outputData;
    }

    public void setOutputData(Object outputData) {
        this.outputData = outputData;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getStateMachineState() {
        return stateMachineState;
    }

    public void setStateMachineState(String stateMachineState) {
        this.stateMachineState = stateMachineState;
    }

    public String getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(String lastEvent) {
        this.lastEvent = lastEvent;
    }

    public String getNextPossibleStates() {
        return nextPossibleStates;
    }

    public void setNextPossibleStates(String nextPossibleStates) {
        this.nextPossibleStates = nextPossibleStates;
    }
}
