package com.integrixs.engine.domain.repository;

import com.integrixs.engine.domain.model.WorkflowEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain repository interface for workflow event persistence
 */
public interface WorkflowEventRepository {

    /**
     * Save a workflow event
     * @param event The workflow event to save
     * @return Saved workflow event
     */
    WorkflowEvent save(WorkflowEvent event);

    /**
     * Find events by workflow ID
     * @param workflowId The workflow ID
     * @return List of workflow events
     */
    List<WorkflowEvent> findByWorkflowId(String workflowId);

    /**
     * Find events by flow ID
     * @param flowId The flow ID
     * @return List of workflow events
     */
    List<WorkflowEvent> findByFlowId(String flowId);

    /**
     * Find events by type
     * @param eventType The event type
     * @return List of workflow events
     */
    List<WorkflowEvent> findByEventType(WorkflowEvent.EventType eventType);

    /**
     * Find events within a time range
     * @param startTime Start time
     * @param endTime End time
     * @return List of workflow events
     */
    List<WorkflowEvent> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find events by workflow ID and type
     * @param workflowId The workflow ID
     * @param eventType The event type
     * @return List of workflow events
     */
    List<WorkflowEvent> findByWorkflowIdAndEventType(String workflowId, WorkflowEvent.EventType eventType);

    /**
     * Delete old events
     * @param beforeDate Delete events before this date
     * @return Number of deleted events
     */
    int deleteByTimestampBefore(LocalDateTime beforeDate);

    /**
     * Count events by workflow ID
     * @param workflowId The workflow ID
     * @return Event count
     */
    long countByWorkflowId(String workflowId);
}
