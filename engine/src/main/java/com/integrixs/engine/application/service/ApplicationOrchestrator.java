package com.integrixs.engine.application.service;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.engine.api.dto.WorkflowExecutionRequestDTO;
import com.integrixs.engine.api.dto.WorkflowExecutionResponseDTO;
import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.WorkflowContext;
import com.integrixs.engine.domain.service.FlowExecutionService;
import com.integrixs.engine.domain.service.WorkflowOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application-level orchestrator that coordinates complex workflows
 * Bridges between API layer and domain services
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationOrchestrator {
    
    private final WorkflowOrchestrationService workflowOrchestrationService;
    private final FlowExecutionService flowExecutionService;
    private final IntegrationFlowRepository integrationFlowRepository;
    private final AdapterExecutionApplicationService adapterExecutionApplicationService;
    private final FlowExecutionApplicationService flowExecutionApplicationService;
    
    // Track active workflows
    private final Map<String, WorkflowContext> activeWorkflows = new ConcurrentHashMap<>();
    
    /**
     * Execute a complete workflow
     * @param request Workflow execution request
     * @return Workflow execution response
     */
    @Transactional(readOnly = true)
    public WorkflowExecutionResponseDTO executeWorkflow(WorkflowExecutionRequestDTO request) {
        log.info("Executing workflow for flow: {}", request.getFlowId());
        
        try {
            // Load flow
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(request.getFlowId()))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + request.getFlowId()));
            
            // Validate workflow
            workflowOrchestrationService.validateWorkflow(flow);
            
            // Execute workflow
            WorkflowContext context = workflowOrchestrationService.executeWorkflow(flow, request.getInputData());
            
            // Track active workflow
            activeWorkflows.put(context.getWorkflowId(), context);
            
            // Convert to response DTO
            return convertToResponseDTO(context);
            
        } catch (Exception e) {
            log.error("Error executing workflow for flow {}: {}", request.getFlowId(), e.getMessage(), e);
            return createErrorResponse(request, e);
        } finally {
            // Clean up completed workflows
            cleanupCompletedWorkflows();
        }
    }
    
    /**
     * Execute a workflow asynchronously
     * @param request Workflow execution request
     * @return Future with workflow response
     */
    public CompletableFuture<WorkflowExecutionResponseDTO> executeWorkflowAsync(WorkflowExecutionRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> executeWorkflow(request));
    }
    
    /**
     * Execute a simple flow (adapter to adapter)
     * @param flowId Flow ID
     * @param inputData Input data
     * @return Workflow response
     */
    public WorkflowExecutionResponseDTO executeSimpleFlow(String flowId, Object inputData) {
        log.info("Executing simple flow: {}", flowId);
        
        try {
            // Use flow execution service directly for simple flows
            var flowResponse = flowExecutionApplicationService.processMessage(flowId, inputData);
            
            // Convert to workflow response
            return WorkflowExecutionResponseDTO.builder()
                    .workflowId(flowResponse.getExecutionId())
                    .flowId(flowId)
                    .success(flowResponse.isSuccess())
                    .outputData(flowResponse.getProcessedData())
                    .errorMessage(flowResponse.getErrorMessage())
                    .executionTimeMs(flowResponse.getExecutionTimeMs())
                    .state(flowResponse.isSuccess() ? 
                        WorkflowContext.WorkflowState.COMPLETED.name() : 
                        WorkflowContext.WorkflowState.FAILED.name())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error executing simple flow {}: {}", flowId, e.getMessage(), e);
            var request = new WorkflowExecutionRequestDTO();
            request.setFlowId(flowId);
            return createErrorResponse(request, e);
        }
    }
    
    /**
     * Get workflow status
     * @param workflowId Workflow ID
     * @return Workflow status
     */
    public WorkflowExecutionResponseDTO getWorkflowStatus(String workflowId) {
        log.debug("Getting status for workflow: {}", workflowId);
        
        WorkflowContext context = activeWorkflows.get(workflowId);
        if (context == null) {
            context = workflowOrchestrationService.getWorkflowStatus(workflowId);
        }
        
        if (context == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }
        
        return convertToResponseDTO(context);
    }
    
    /**
     * Cancel a workflow
     * @param workflowId Workflow ID
     * @return true if cancelled
     */
    public boolean cancelWorkflow(String workflowId) {
        log.info("Cancelling workflow: {}", workflowId);
        
        boolean cancelled = workflowOrchestrationService.cancelWorkflow(workflowId);
        if (cancelled) {
            activeWorkflows.remove(workflowId);
        }
        
        return cancelled;
    }
    
    /**
     * Suspend a workflow
     * @param workflowId Workflow ID
     * @return true if suspended
     */
    public boolean suspendWorkflow(String workflowId) {
        log.info("Suspending workflow: {}", workflowId);
        return workflowOrchestrationService.suspendWorkflow(workflowId);
    }
    
    /**
     * Resume a workflow
     * @param workflowId Workflow ID
     * @return true if resumed
     */
    public boolean resumeWorkflow(String workflowId) {
        log.info("Resuming workflow: {}", workflowId);
        return workflowOrchestrationService.resumeWorkflow(workflowId);
    }
    
    /**
     * Check adapter health before workflow execution
     * @param flowId Flow ID
     * @return Health check result
     */
    public Map<String, Object> checkWorkflowHealth(String flowId) {
        try {
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));
            
            boolean sourceHealthy = adapterExecutionApplicationService.isAdapterHealthy(
                    flow.getInboundAdapterId().toString());
            boolean targetHealthy = adapterExecutionApplicationService.isAdapterHealthy(
                    flow.getOutboundAdapterId().toString());
            
            return Map.of(
                "flowId", flowId,
                "inboundAdapterHealthy", sourceHealthy,
                "outboundAdapterHealthy", targetHealthy,
                "overallHealth", sourceHealthy && targetHealthy,
                "timestamp", System.currentTimeMillis()
            );
            
        } catch (Exception e) {
            return Map.of(
                "flowId", flowId,
                "error", e.getMessage(),
                "overallHealth", false,
                "timestamp", System.currentTimeMillis()
            );
        }
    }
    
    private WorkflowExecutionResponseDTO convertToResponseDTO(WorkflowContext context) {
        return WorkflowExecutionResponseDTO.builder()
                .workflowId(context.getWorkflowId())
                .flowId(context.getFlowId())
                .executionId(context.getExecutionId())
                .state(context.getState().name())
                .success(context.getState() == WorkflowContext.WorkflowState.COMPLETED)
                .steps(context.getSteps())
                .currentStep(context.getCurrentStep())
                .globalVariables(context.getGlobalVariables())
                .metadata(context.getMetadata())
                .correlationId(context.getCorrelationId())
                .startTime(context.getStartTime())
                .endTime(context.getEndTime())
                .executionTimeMs(context.getDuration())
                .initiatedBy(context.getInitiatedBy())
                .build();
    }
    
    private WorkflowExecutionResponseDTO createErrorResponse(WorkflowExecutionRequestDTO request, Exception e) {
        return WorkflowExecutionResponseDTO.builder()
                .workflowId(UUID.randomUUID().toString())
                .flowId(request.getFlowId())
                .success(false)
                .state(WorkflowContext.WorkflowState.FAILED.name())
                .errorMessage(e.getMessage())
                .build();
    }
    
    private void cleanupCompletedWorkflows() {
        activeWorkflows.entrySet().removeIf(entry -> {
            WorkflowContext context = entry.getValue();
            return context.getState() == WorkflowContext.WorkflowState.COMPLETED ||
                   context.getState() == WorkflowContext.WorkflowState.FAILED ||
                   context.getState() == WorkflowContext.WorkflowState.CANCELLED;
        });
    }
}