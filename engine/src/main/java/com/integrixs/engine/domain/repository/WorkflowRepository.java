package com.integrixs.engine.domain.repository;

import com.integrixs.engine.domain.model.WorkflowContext;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for workflow persistence
 */
public interface WorkflowRepository {

    /**
     * Save a workflow context
     * @param context The workflow context to save
     * @return Saved workflow context
     */
    WorkflowContext save(WorkflowContext context);

    /**
     * Find a workflow by ID
     * @param workflowId The workflow ID
     * @return Optional workflow context
     */
    Optional<WorkflowContext> findById(String workflowId);

    /**
     * Find workflows by flow ID
     * @param flowId The flow ID
     * @return List of workflow contexts
     */
    List<WorkflowContext> findByFlowId(String flowId);

    /**
     * Find workflows by state
     * @param state The workflow state
     * @return List of workflow contexts
     */
    List<WorkflowContext> findByState(WorkflowContext.WorkflowState state);

    /**
     * Find active workflows(IN_PROGRESS or SUSPENDED)
     * @return List of active workflow contexts
     */
    List<WorkflowContext> findActiveWorkflows();

    /**
     * Delete a workflow
     * @param workflowId The workflow ID
     */
    void deleteById(String workflowId);

    /**
     * Check if a workflow exists
     * @param workflowId The workflow ID
     * @return true if exists
     */
    boolean existsById(String workflowId);
}
