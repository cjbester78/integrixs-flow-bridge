package com.integrixs.shared.integration;

import java.util.Map;

/**
 * Client interface for flow execution operations
 * Used by Backend module to communicate with Engine module
 */
public interface FlowExecutionClient {

    /**
     * Execute a flow with given payload
     * @param flowId Integration flow ID
     * @param payload Message payload
     * @return Execution result
     */
    ExecutionResult executeFlow(String flowId, Map<String, Object> payload);

    /**
     * Stop a running flow execution
     * @param executionId Execution ID
     */
    void stopExecution(String executionId);

    /**
     * Get current status of flow execution
     * @param executionId Execution ID
     * @return Execution status
     */
    ExecutionStatus getExecutionStatus(String executionId);

    /**
     * Execute flow asynchronously
     * @param flowId Integration flow ID
     * @param payload Message payload
     * @param callbackUrl Webhook URL for completion notification
     * @return Execution ID for tracking
     */
    String executeFlowAsync(String flowId, Map<String, Object> payload, String callbackUrl);

    /**
     * Validate flow configuration
     * @param flowId Integration flow ID
     * @return Validation result
     */
    ValidationResult validateFlow(String flowId);
}
