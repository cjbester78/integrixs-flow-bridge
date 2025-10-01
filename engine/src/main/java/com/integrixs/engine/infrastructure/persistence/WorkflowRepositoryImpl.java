package com.integrixs.engine.infrastructure.persistence;

import com.integrixs.engine.domain.model.WorkflowContext;
import com.integrixs.engine.domain.repository.WorkflowRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure implementation of WorkflowRepository
 * Currently uses in - memory storage, can be replaced with database persistence
 */
@Repository
public class WorkflowRepositoryImpl implements WorkflowRepository {

    // In - memory storage for workflows

    private static final Logger log = LoggerFactory.getLogger(WorkflowRepositoryImpl.class);

    private final Map<String, WorkflowContext> workflowStore = new ConcurrentHashMap<>();

    @Override
    public WorkflowContext save(WorkflowContext context) {
        log.debug("Saving workflow: {} with state: {}", context.getWorkflowId(), context.getState());
        workflowStore.put(context.getWorkflowId(), context);
        return context;
    }

    @Override
    public Optional<WorkflowContext> findById(String workflowId) {
        return Optional.ofNullable(workflowStore.get(workflowId));
    }

    @Override
    public List<WorkflowContext> findByFlowId(String flowId) {
        return workflowStore.values().stream()
                .filter(context -> flowId.equals(context.getFlowId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowContext> findByState(WorkflowContext.WorkflowState state) {
        return workflowStore.values().stream()
                .filter(context -> state.equals(context.getState()))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowContext> findActiveWorkflows() {
        return workflowStore.values().stream()
                .filter(context ->
                    context.getState() == WorkflowContext.WorkflowState.IN_PROGRESS ||
                    context.getState() == WorkflowContext.WorkflowState.SUSPENDED)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String workflowId) {
        log.debug("Deleting workflow: {}", workflowId);
        workflowStore.remove(workflowId);
    }

    @Override
    public boolean existsById(String workflowId) {
        return workflowStore.containsKey(workflowId);
    }

    /**
     * Clear all workflows(for testing purposes)
     */
    public void clearAll() {
        workflowStore.clear();
    }

    /**
     * Get total count of workflows
     * @return Total workflow count
     */
    public int count() {
        return workflowStore.size();
    }
}
