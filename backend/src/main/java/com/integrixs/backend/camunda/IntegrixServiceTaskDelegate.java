package com.integrixs.backend.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.integrixs.backend.service.BackendAdapterExecutor;
import com.integrixs.backend.service.TransformationExecutionService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service task delegate for executing Integrix operations in Camunda processes
 */
@Component("integrixServiceTaskDelegate")
public class IntegrixServiceTaskDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(IntegrixServiceTaskDelegate.class);

    @Autowired
    private BackendAdapterExecutor adapterExecutionService;

    @Autowired
    private TransformationExecutionService transformationService;

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Executing Integrix service task: {} in process: {}", 
            execution.getCurrentActivityName(), execution.getProcessInstanceId());

        String taskType = (String) execution.getVariable("taskType");
        if (taskType == null) {
            taskType = "generic";
        }

        try {
            switch (taskType.toLowerCase()) {
                case "adapter":
                    executeAdapterTask(execution);
                    break;
                case "transformation":
                    executeTransformationTask(execution);
                    break;
                case "enrichment":
                    executeEnrichmentTask(execution);
                    break;
                default:
                    executeGenericTask(execution);
                    break;
            }

            execution.setVariable("taskExecutionSuccess", true);
            logger.info("Service task completed successfully: {}", execution.getCurrentActivityName());

        } catch (Exception e) {
            logger.error("Service task execution failed: {}", execution.getCurrentActivityName(), e);
            execution.setVariable("taskExecutionSuccess", false);
            execution.setVariable("errorMessage", e.getMessage());
            throw e;
        }
    }

    private void executeAdapterTask(DelegateExecution execution) throws Exception {
        String adapterId = (String) execution.getVariable("adapterId");
        if (adapterId == null) {
            throw new IllegalArgumentException("Adapter ID is required for adapter task");
        }

        Object messageData = execution.getVariable("messageData");
        Map<String, Object> context = new HashMap<>();
        execution.getVariables().forEach(context::put);

        CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
            .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));

        String result = adapterExecutionService.executeAdapter(
            adapter,
            messageData != null ? messageData.toString() : "",
            context
        );

        execution.setVariable("adapterResult", result);
        execution.setVariable("messageData", result);
    }

    private void executeTransformationTask(DelegateExecution execution) throws Exception {
        String transformationId = (String) execution.getVariable("transformationId");
        Object inputData = execution.getVariable("messageData");

        if (transformationId == null) {
            transformationId = execution.getCurrentActivityId();
        }

        var result = transformationService.executeTransformation(transformationId, inputData);

        if (result.isSuccess()) {
            execution.setVariable("transformationResult", result.getData());
            execution.setVariable("messageData", result.getData());
        } else {
            throw new RuntimeException("Transformation failed: " + result.getMessage());
        }
    }

    private void executeEnrichmentTask(DelegateExecution execution) throws Exception {
        Object messageData = execution.getVariable("messageData");
        
        // Simple enrichment - add metadata
        Map<String, Object> enrichedData = new HashMap<>();
        enrichedData.put("originalData", messageData);
        enrichedData.put("processInstanceId", execution.getProcessInstanceId());
        enrichedData.put("activityId", execution.getCurrentActivityId());
        enrichedData.put("enrichmentTimestamp", System.currentTimeMillis());

        execution.setVariable("messageData", enrichedData);
        execution.setVariable("enrichmentApplied", true);
    }

    private void executeGenericTask(DelegateExecution execution) throws Exception {
        // Generic task processing
        execution.setVariable("taskExecutionTimestamp", System.currentTimeMillis());
        execution.setVariable("genericTaskExecuted", true);
        
        logger.info("Generic task executed: {}", execution.getCurrentActivityName());
    }
}