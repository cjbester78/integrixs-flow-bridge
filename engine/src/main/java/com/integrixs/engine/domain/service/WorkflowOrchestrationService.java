package com.integrixs.engine.domain.service;

import com.integrixs.engine.domain.model.WorkflowContext;
import com.integrixs.engine.domain.model.WorkflowStep;
import com.integrixs.data.model.IntegrationFlow;

import java.util.concurrent.CompletableFuture;

/**
 * Domain service interface for workflow orchestration
 * Coordinates the execution of complex multi - step workflows
 */
public interface WorkflowOrchestrationService {

    /**
     * Execute a complete workflow
     * @param flow The integration flow
     * @param inputData Input data for the workflow
     * @return Workflow execution context
     */
    WorkflowContext executeWorkflow(IntegrationFlow flow, Object inputData);

    /**
     * Execute a workflow asynchronously
     * @param flow The integration flow
     * @param inputData Input data for the workflow
     * @return Future with workflow context
     */
    CompletableFuture<WorkflowContext> executeWorkflowAsync(IntegrationFlow flow, Object inputData);

    /**
     * Execute a specific workflow step
     * @param step The workflow step
     * @param context The workflow context
     * @return Updated workflow step
     */
    WorkflowStep executeStep(WorkflowStep step, WorkflowContext context);

    /**
     * Validate workflow before execution
     * @param flow The integration flow
     * @throws IllegalArgumentException if workflow is invalid
     */
    void validateWorkflow(IntegrationFlow flow);

    /**
     * Cancel a running workflow
     * @param workflowId The workflow ID
     * @return true if cancelled successfully
     */
    boolean cancelWorkflow(String workflowId);

    /**
     * Suspend a running workflow
     * @param workflowId The workflow ID
     * @return true if suspended successfully
     */
    boolean suspendWorkflow(String workflowId);

    /**
     * Resume a suspended workflow
     * @param workflowId The workflow ID
     * @return true if resumed successfully
     */
    boolean resumeWorkflow(String workflowId);

    /**
     * Get workflow status
     * @param workflowId The workflow ID
     * @return Current workflow context
     */
    WorkflowContext getWorkflowStatus(String workflowId);

    /**
     * Handle workflow error
     * @param context The workflow context
     * @param step The failed step
     * @param error The error
     * @return Updated workflow context
     */
    WorkflowContext handleError(WorkflowContext context, WorkflowStep step, Exception error);
}
