package com.integrixs.engine.infrastructure.workflow;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.FieldMappingSqlRepository;
import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.WorkflowContext;
import com.integrixs.engine.domain.model.WorkflowEvent;
import com.integrixs.engine.domain.model.WorkflowStep;
import com.integrixs.engine.domain.repository.WorkflowEventRepository;
import com.integrixs.engine.domain.repository.WorkflowRepository;
import com.integrixs.engine.domain.service.FlowAdapterExecutor;
import com.integrixs.engine.domain.service.FlowExecutionService;
import com.integrixs.engine.domain.service.WorkflowOrchestrationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure implementation of WorkflowOrchestrationService
 * Handles complex workflow execution and coordination
 */
@Service
public class WorkflowOrchestrationServiceImpl implements WorkflowOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowOrchestrationServiceImpl.class);


    private final FlowExecutionService flowExecutionService;
    private final FlowAdapterExecutor adapterExecutionService;
    private final FieldMappingSqlRepository fieldMappingRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowEventRepository workflowEventRepository;

    // Track active workflows
    private final Map<String, WorkflowContext> activeWorkflows = new ConcurrentHashMap<>();

    public WorkflowOrchestrationServiceImpl(FlowExecutionService flowExecutionService, FlowAdapterExecutor adapterExecutionService, FieldMappingSqlRepository fieldMappingRepository, WorkflowRepository workflowRepository, WorkflowEventRepository workflowEventRepository) {
        this.flowExecutionService = flowExecutionService;
        this.adapterExecutionService = adapterExecutionService;
        this.fieldMappingRepository = fieldMappingRepository;
        this.workflowRepository = workflowRepository;
        this.workflowEventRepository = workflowEventRepository;
    }

    @Override
    public WorkflowContext executeWorkflow(IntegrationFlow flow, Object inputData) {
        String workflowId = UUID.randomUUID().toString();
        log.info("Starting workflow execution: {} for flow: {}", workflowId, flow.getName());

        // Initialize workflow context
        WorkflowContext context = WorkflowContext.builder()
                .workflowId(workflowId)
                .flowId(flow.getId().toString())
                .executionId(UUID.randomUUID().toString())
                .state(WorkflowContext.WorkflowState.INITIATED)
                .startTime(System.currentTimeMillis())
                .build();

        activeWorkflows.put(workflowId, context);

        // Save initial input data for potential resume
        context.addGlobalVariable("inputData", inputData);

        // Save workflow to repository
        workflowRepository.save(context);

        // Log workflow started event
        saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_STARTED, "Workflow execution started");

        try {
            // Update state
            context.setState(WorkflowContext.WorkflowState.IN_PROGRESS);
            workflowRepository.save(context);

            // Create workflow steps
            createWorkflowSteps(flow, context);

            // Execute steps
            Object currentData = inputData;
            for(WorkflowStep step : context.getSteps()) {
                context.setCurrentStep(step);
                currentData = executeStepWithData(step, context, currentData);

                if(step.getStatus() == WorkflowStep.StepStatus.FAILED) {
                    context.setState(WorkflowContext.WorkflowState.FAILED);
                    break;
                }
            }

            // Mark as completed if all steps succeeded
            if(context.getState() != WorkflowContext.WorkflowState.FAILED) {
                context.setState(WorkflowContext.WorkflowState.COMPLETED);
                context.addGlobalVariable("outputData", currentData);
                saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_COMPLETED, "Workflow completed successfully");
            }

            context.setEndTime(System.currentTimeMillis());
            workflowRepository.save(context);
            log.info("Workflow {} completed with state: {}", workflowId, context.getState());

        } catch(Exception e) {
            log.error("Workflow {} failed: {}", workflowId, e.getMessage(), e);
            context.setState(WorkflowContext.WorkflowState.FAILED);
            context.setEndTime(System.currentTimeMillis());
            context.addMetadata("error", e.getMessage());
            workflowRepository.save(context);
            saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_FAILED, "Workflow failed: " + e.getMessage());
        }

        return context;
    }

    @Override
    public CompletableFuture<WorkflowContext> executeWorkflowAsync(IntegrationFlow flow, Object inputData) {
        return CompletableFuture.supplyAsync(() -> executeWorkflow(flow, inputData));
    }

    @Override
    public WorkflowStep executeStep(WorkflowStep step, WorkflowContext context) {
        log.debug("Executing workflow step: {} of type: {}", step.getStepName(), step.getStepType());

        step.setStartTime(LocalDateTime.now());
        step.setStatus(WorkflowStep.StepStatus.IN_PROGRESS);

        try {
            switch(step.getStepType()) {
                case SOURCE_ADAPTER:
                    executeSourceAdapterStep(step, context);
                    break;
                case TARGET_ADAPTER:
                    executeTargetAdapterStep(step, context);
                    break;
                case TRANSFORMATION:
                    executeTransformationStep(step, context);
                    break;
                case VALIDATION:
                    executeValidationStep(step, context);
                    break;
                default:
                    log.warn("Unsupported step type: {}", step.getStepType());
                    step.setStatus(WorkflowStep.StepStatus.SKIPPED);
            }

            if(step.getStatus() == WorkflowStep.StepStatus.IN_PROGRESS) {
                step.setStatus(WorkflowStep.StepStatus.COMPLETED);
            }

        } catch(Exception e) {
            log.error("Step {} failed: {}", step.getStepName(), e.getMessage(), e);
            step.setStatus(WorkflowStep.StepStatus.FAILED);
            step.setErrorMessage(e.getMessage());
        } finally {
            step.setEndTime(LocalDateTime.now());
        }

        return step;
    }

    @Override
    public void validateWorkflow(IntegrationFlow flow) {
        flowExecutionService.validateFlow(flow);
    }

    @Override
    public boolean cancelWorkflow(String workflowId) {
        WorkflowContext context = activeWorkflows.get(workflowId);
        if(context != null && context.getState() == WorkflowContext.WorkflowState.IN_PROGRESS) {
            context.setState(WorkflowContext.WorkflowState.CANCELLED);
            context.setEndTime(System.currentTimeMillis());
            log.info("Workflow {} cancelled", workflowId);
            return true;
        }
        return false;
    }

    @Override
    public boolean suspendWorkflow(String workflowId) {
        WorkflowContext context = activeWorkflows.get(workflowId);
        if(context != null && context.getState() == WorkflowContext.WorkflowState.IN_PROGRESS) {
            // Set state to SUSPENDED
            context.setState(WorkflowContext.WorkflowState.SUSPENDED);

            // Save any current data to global variables for resume
            if(context.getCurrentStep() != null) {
                context.addMetadata("suspendedAtStep", context.getCurrentStep().getStepId());
                context.addMetadata("suspendedAtStepName", context.getCurrentStep().getStepName());

                // If there's output data from the last completed step, save it
                for(WorkflowStep step : context.getSteps()) {
                    if(step.getStatus() == WorkflowStep.StepStatus.COMPLETED &&
                        step.getOutputData() != null) {
                        context.addGlobalVariable("lastStepOutputData", step.getOutputData());
                    }
                }
            }

            // Save suspension time
            context.addMetadata("suspendedAt", LocalDateTime.now().toString());

            // Persist to repository
            workflowRepository.save(context);

            // Log suspension event
            saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_SUSPENDED,
                "Workflow suspended at step: " +
                (context.getCurrentStep() != null ? context.getCurrentStep().getStepName() : "unknown"));

            log.info("Workflow {} suspended successfully", workflowId);
            return true;
        }
        return false;
    }

    @Override
    public boolean resumeWorkflow(String workflowId) {
        // Get context from memory or load from repository
        WorkflowContext context = activeWorkflows.get(workflowId);
        if(context == null) {
            Optional<WorkflowContext> savedContext = workflowRepository.findById(workflowId);
            if(savedContext.isPresent()) {
                context = savedContext.get();
                activeWorkflows.put(workflowId, context);
            } else {
                log.warn("Workflow {} not found for resume", workflowId);
                return false;
            }
        }

        // Check if workflow is in suspended state
        if(context.getState() != WorkflowContext.WorkflowState.SUSPENDED) {
            log.warn("Workflow {} is not in SUSPENDED state, current state: {}", workflowId, context.getState());
            return false;
        }

        // Update state to IN_PROGRESS
        context.setState(WorkflowContext.WorkflowState.IN_PROGRESS);
        workflowRepository.save(context);
        log.info("Workflow {} state changed to IN_PROGRESS", workflowId);

        // Log resume event
        saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_RESUMED, "Workflow resumed from suspended state");

        // Execute resume logic asynchronously
        final WorkflowContext finalContext = context;
        CompletableFuture.runAsync(() -> {
            try {
                resumeWorkflowExecution(finalContext);
            } catch(Exception e) {
                log.error("Failed to resume workflow {}: {}", workflowId, e.getMessage(), e);
                finalContext.setState(WorkflowContext.WorkflowState.FAILED);
                finalContext.addMetadata("resumeError", e.getMessage());
                workflowRepository.save(finalContext);
                saveWorkflowEvent(finalContext, WorkflowEvent.EventType.WORKFLOW_FAILED,
                    "Workflow failed during resume: " + e.getMessage());
            }
        });

        return true;
    }

    @Override
    public WorkflowContext getWorkflowStatus(String workflowId) {
        WorkflowContext context = activeWorkflows.get(workflowId);
        if(context == null) {
            // Try to load from repository
            context = workflowRepository.findById(workflowId).orElse(null);
        }
        return context;
    }

    @Override
    public WorkflowContext handleError(WorkflowContext context, WorkflowStep step, Exception error) {
        log.error("Handling error for workflow {} at step {}: {}",
                context.getWorkflowId(), step.getStepName(), error.getMessage());

        step.setStatus(WorkflowStep.StepStatus.FAILED);
        step.setErrorMessage(error.getMessage());

        // Check retry logic
        if(step.getRetryCount() != null && step.getRetryCount() < 3) {
            step.setRetryCount(step.getRetryCount() + 1);
            step.setStatus(WorkflowStep.StepStatus.RETRY);
            log.info("Retrying step {} (attempt {})", step.getStepName(), step.getRetryCount());
        } else {
            context.setState(WorkflowContext.WorkflowState.FAILED);
            context.addMetadata("failedStep", step.getStepName());
            context.addMetadata("errorMessage", error.getMessage());
        }

        return context;
    }

    private void createWorkflowSteps(IntegrationFlow flow, WorkflowContext context) {
        // Step 1: Source Adapter
        WorkflowStep sourceStep = WorkflowStep.builder()
                .stepId(UUID.randomUUID().toString())
                .stepName("Fetch from Source Adapter")
                .stepType(WorkflowStep.StepType.SOURCE_ADAPTER)
                .status(WorkflowStep.StepStatus.PENDING)
                .build();
        sourceStep.addStepVariable("adapterId", flow.getInboundAdapterId().toString());
        context.addStep(sourceStep);

        // Step 2: Transformation(if needed)
        List<FieldMapping> mappings = fieldMappingRepository
                .findByTransformationFlowIdAndIsActiveTrueOrderByTransformationExecutionOrder(flow.getId());

        if(!mappings.isEmpty()) {
            WorkflowStep transformStep = WorkflowStep.builder()
                    .stepId(UUID.randomUUID().toString())
                    .stepName("Apply Field Mappings")
                    .stepType(WorkflowStep.StepType.TRANSFORMATION)
                    .status(WorkflowStep.StepStatus.PENDING)
                    .build();
            transformStep.addStepVariable("mappingCount", mappings.size());
            context.addStep(transformStep);
        }

        // Step 3: Target Adapter
        WorkflowStep targetStep = WorkflowStep.builder()
                .stepId(UUID.randomUUID().toString())
                .stepName("Send to Target Adapter")
                .stepType(WorkflowStep.StepType.TARGET_ADAPTER)
                .status(WorkflowStep.StepStatus.PENDING)
                .build();
        targetStep.addStepVariable("adapterId", flow.getOutboundAdapterId().toString());
        context.addStep(targetStep);
    }

    private Object executeStepWithData(WorkflowStep step, WorkflowContext context, Object inputData) {
        step.setInputData(inputData);
        executeStep(step, context);

        // Return output data or input data if no output
        return step.getOutputData() != null ? step.getOutputData() : inputData;
    }

    private void executeSourceAdapterStep(WorkflowStep step, WorkflowContext context) {
        String adapterId = (String) step.getStepVariables().get("adapterId");

        // For source adapter, we typically don't have input data
        // The adapter itself fetches the data
        Object data = step.getInputData();

        // Store the fetched data as output
        step.setOutputData(data);
        log.debug("Source adapter step completed, data fetched");
    }

    private void executeTargetAdapterStep(WorkflowStep step, WorkflowContext context) {
        String adapterId = (String) step.getStepVariables().get("adapterId");
        Object data = step.getInputData();

        // Build execution context
        FlowExecutionContext execContext = FlowExecutionContext.builder()
                .executionId(context.getExecutionId())
                .flowId(context.getFlowId())
                .correlationId(context.getCorrelationId())
                .build();

        // Send data through flow execution
        flowExecutionService.sendToTargetAdapter(data, adapterId, execContext);

        step.setOutputData(data);
        log.debug("Target adapter step completed, data sent");
    }

    private void executeTransformationStep(WorkflowStep step, WorkflowContext context) {
        Object data = step.getInputData();

        // For now, pass through - actual transformation is handled by flow execution
        step.setOutputData(data);
        log.debug("Transformation step completed");
    }

    private void executeValidationStep(WorkflowStep step, WorkflowContext context) {
        // Placeholder for validation logic
        step.setOutputData(step.getInputData());
        log.debug("Validation step completed");
    }

    private void saveWorkflowEvent(WorkflowContext context, WorkflowEvent.EventType eventType, String description) {
        WorkflowEvent event = WorkflowEvent.builder()
                .workflowId(context.getWorkflowId())
                .flowId(context.getFlowId())
                .eventType(eventType)
                .eventName(eventType.name())
                .description(description)
                .userId(context.getInitiatedBy())
                .source("WorkflowOrchestrationService")
                .build();

        if(context.getCurrentStep() != null) {
            event.setStepId(context.getCurrentStep().getStepId());
            event.setStepName(context.getCurrentStep().getStepName());
        }

        workflowEventRepository.save(event);
    }

    /**
     * Resume workflow execution from the last completed step
     */
    private void resumeWorkflowExecution(WorkflowContext context) {
        log.info("Resuming workflow {} execution", context.getWorkflowId());

        // Find the last completed step and the next step to execute
        WorkflowStep lastCompletedStep = null;
        WorkflowStep nextStepToExecute = null;
        Object lastOutputData = null;

        for(int i = 0; i < context.getSteps().size(); i++) {
            WorkflowStep step = context.getSteps().get(i);

            if(step.getStatus() == WorkflowStep.StepStatus.COMPLETED) {
                lastCompletedStep = step;
                // Get the output data from the last completed step
                if(step.getOutputData() != null) {
                    lastOutputData = step.getOutputData();
                }
            } else if(step.getStatus() == WorkflowStep.StepStatus.IN_PROGRESS ||
                       step.getStatus() == WorkflowStep.StepStatus.PENDING ||
                       step.getStatus() == WorkflowStep.StepStatus.RETRY) {
                nextStepToExecute = step;
                break;
            }
        }

        // If no next step found, check if workflow should be completed
        if(nextStepToExecute == null) {
            if(lastCompletedStep != null &&
                context.getSteps().indexOf(lastCompletedStep) == context.getSteps().size() - 1) {
                // All steps completed
                context.setState(WorkflowContext.WorkflowState.COMPLETED);
                context.setEndTime(System.currentTimeMillis());
                if(lastOutputData != null) {
                    context.addGlobalVariable("outputData", lastOutputData);
                }
                workflowRepository.save(context);
                saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_COMPLETED,
                    "Workflow completed after resume");
                log.info("Workflow {} completed after resume", context.getWorkflowId());
                return;
            }
        }

        // Prepare data for resume
        Object currentData = lastOutputData;
        if(currentData == null) {
            // Try to get initial input data from global variables
            currentData = context.getGlobalVariables().get("inputData");
        }

        // Resume execution from the next step
        if(nextStepToExecute != null) {
            log.info("Resuming workflow {} from step: {}", context.getWorkflowId(), nextStepToExecute.getStepName());

            // If the step was in progress, reset it to retry
            if(nextStepToExecute.getStatus() == WorkflowStep.StepStatus.IN_PROGRESS) {
                nextStepToExecute.setStatus(WorkflowStep.StepStatus.RETRY);
                if(nextStepToExecute.getRetryCount() == null) {
                    nextStepToExecute.setRetryCount(0);
                }
                nextStepToExecute.setRetryCount(nextStepToExecute.getRetryCount() + 1);
            }

            // Continue execution from this step
            boolean foundStartingStep = false;
            for(WorkflowStep step : context.getSteps()) {
                if(!foundStartingStep) {
                    if(step.getStepId().equals(nextStepToExecute.getStepId())) {
                        foundStartingStep = true;
                    } else {
                        continue;
                    }
                }

                // Execute the step
                context.setCurrentStep(step);
                currentData = executeStepWithData(step, context, currentData);

                // Check if step failed
                if(step.getStatus() == WorkflowStep.StepStatus.FAILED) {
                    context.setState(WorkflowContext.WorkflowState.FAILED);
                    context.setEndTime(System.currentTimeMillis());
                    context.addMetadata("failedDuringResume", true);
                    context.addMetadata("failedStep", step.getStepName());
                    workflowRepository.save(context);
                    saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_FAILED,
                        "Workflow failed during resume at step: " + step.getStepName());
                    log.error("Workflow {} failed during resume at step: {}",
                        context.getWorkflowId(), step.getStepName());
                    return;
                }

                // Save progress after each step
                workflowRepository.save(context);
            }

            // Mark workflow as completed if all steps finished
            context.setState(WorkflowContext.WorkflowState.COMPLETED);
            context.setEndTime(System.currentTimeMillis());
            context.addGlobalVariable("outputData", currentData);
            workflowRepository.save(context);
            saveWorkflowEvent(context, WorkflowEvent.EventType.WORKFLOW_COMPLETED,
                "Workflow completed successfully after resume");
            log.info("Workflow {} completed successfully after resume", context.getWorkflowId());

        } else {
            // No steps to execute - this shouldn't happen normally
            log.warn("No steps found to resume for workflow {}", context.getWorkflowId());
            context.setState(WorkflowContext.WorkflowState.FAILED);
            context.addMetadata("resumeError", "No steps found to resume");
            workflowRepository.save(context);
        }
    }
}
