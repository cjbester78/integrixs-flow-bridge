package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an error record in the system
 */
public class ErrorRecord extends BaseEntity {

    private String errorId;

    private IntegrationFlow flow;

    private FlowExecution flowExecution;

    private Message message;

    private ErrorType errorType;

    private String errorCode;

    private String errorMessage;

    private String stackTrace;

    private String componentName;

    private LocalDateTime occurredAt;

    private ErrorSeverity severity = ErrorSeverity.MEDIUM;

    private boolean resolved = false;

    private LocalDateTime resolvedAt;

    private String resolvedBy;

    private String resolutionNotes;

    private Integer retryCount = 0;

    private String contextData;

    public enum ErrorType {
        CONNECTION_ERROR,
        AUTHENTICATION_ERROR,
        TRANSFORMATION_ERROR,
        VALIDATION_ERROR,
        ROUTING_ERROR,
        ADAPTER_ERROR,
        SYSTEM_ERROR,
        CONFIGURATION_ERROR,
        TIMEOUT_ERROR,
        UNKNOWN_ERROR
    }

    public enum ErrorSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // Getters and setters

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public FlowExecution getFlowExecution() {
        return flowExecution;
    }

    public void setFlowExecution(FlowExecution flowExecution) {
        this.flowExecution = flowExecution;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ErrorSeverity severity) {
        this.severity = severity;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getContextData() {
        return contextData;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }

    // Additional helper methods
    public void setFlowId(UUID flowId) {
        if(this.flow == null) {
            this.flow = new IntegrationFlow();
        }
        this.flow.setId(flowId);
    }
}
