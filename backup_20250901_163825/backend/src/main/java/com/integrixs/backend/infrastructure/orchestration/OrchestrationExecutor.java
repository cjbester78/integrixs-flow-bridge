package com.integrixs.backend.infrastructure.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.domain.model.OrchestrationExecution;
import com.integrixs.backend.service.TransformationExecutionService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.engine.AdapterExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure service for executing orchestration steps
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestrationExecutor {
    
    private final TransformationExecutionService transformationService;
    private final CommunicationAdapterRepository adapterRepository;
    private final AdapterExecutor adapterExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Initialize adapters for execution
     * @param execution The orchestration execution
     * @param flow The integration flow
     * @return true if successful
     */
    public boolean initializeAdapters(OrchestrationExecution execution, IntegrationFlow flow) {
        try {
            execution.addLog("Initializing communication adapters");
            
            // Validate source adapter
            CommunicationAdapter sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
                .orElseThrow(() -> new RuntimeException("Source adapter not found"));
            
            if (!sourceAdapter.isActive()) {
                execution.addLog("Source adapter is not active: " + sourceAdapter.getName());
                return false;
            }
            
            // Validate target adapter
            CommunicationAdapter targetAdapter = adapterRepository.findById(flow.getTargetAdapterId())
                .orElseThrow(() -> new RuntimeException("Target adapter not found"));
                
            if (!targetAdapter.isActive()) {
                execution.addLog("Target adapter is not active: " + targetAdapter.getName());
                return false;
            }
            
            // Store adapter info in context
            execution.addContext("sourceAdapter", sourceAdapter.getName());
            execution.addContext("targetAdapter", targetAdapter.getName());
            execution.addContext("sourceAdapterId", sourceAdapter.getId().toString());
            execution.addContext("targetAdapterId", targetAdapter.getId().toString());
            
            execution.addLog("Adapters initialized successfully");
            return true;
            
        } catch (Exception e) {
            execution.addLog("Failed to initialize adapters: " + e.getMessage());
            log.error("Error initializing adapters", e);
            return false;
        }
    }
    
    /**
     * Execute transformations
     * @param execution The orchestration execution
     * @param flowId The flow ID
     * @return true if successful
     */
    public boolean executeTransformations(OrchestrationExecution execution, String flowId) {
        try {
            execution.addLog("Executing transformation functions");
            
            // Execute transformations
            String transformationId = flowId + "_transformation";
            var transformationResult = transformationService.executeTransformation(
                transformationId, 
                execution.getInputData()
            );
            
            if (transformationResult.isSuccess()) {
                execution.setTransformedData(transformationResult.getData());
                execution.addLog("Transformations executed successfully");
                return true;
            } else {
                execution.addLog("Transformation failed: " + transformationResult.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            execution.addLog("Failed to execute transformations: " + e.getMessage());
            log.error("Error executing transformations", e);
            return false;
        }
    }
    
    /**
     * Send data to target adapter
     * @param execution The orchestration execution
     * @param targetAdapterId The target adapter ID
     * @return true if successful
     */
    public boolean sendToTargetAdapter(OrchestrationExecution execution, String targetAdapterId) {
        try {
            execution.addLog("Sending data to target adapter");
            
            // Prepare data for sending
            Object dataToSend = execution.getTransformedData() != null ? 
                execution.getTransformedData() : execution.getInputData();
            
            // Build context for adapter
            Map<String, Object> context = new HashMap<>();
            context.put("executionId", execution.getExecutionId());
            context.put("flowId", execution.getFlowId());
            
            // Send data
            String payload;
            if (dataToSend instanceof String) {
                payload = (String) dataToSend;
            } else {
                payload = objectMapper.writeValueAsString(dataToSend);
            }
            adapterExecutor.sendData(targetAdapterId, payload, context);
            
            // Store output
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("data", dataToSend);
            outputData.put("targetAdapterId", targetAdapterId);
            outputData.put("timestamp", System.currentTimeMillis());
            execution.setOutputData(outputData);
            
            execution.addLog("Data sent to target adapter successfully");
            return true;
            
        } catch (Exception e) {
            execution.addLog("Failed to send data to target adapter: " + e.getMessage());
            log.error("Error sending data to target adapter", e);
            return false;
        }
    }
    
    /**
     * Fetch data from source adapter
     * @param execution The orchestration execution
     * @param sourceAdapterId The source adapter ID
     * @return true if successful
     */
    public boolean fetchFromSourceAdapter(OrchestrationExecution execution, String sourceAdapterId) {
        try {
            execution.addLog("Fetching data from source adapter");
            
            // Fetch data
            Object data = adapterExecutor.fetchDataAsObject(sourceAdapterId);
            
            if (data != null) {
                execution.setInputData(data);
                execution.addContext("sourceDataReceived", true);
                execution.addLog("Data fetched from source adapter successfully");
                return true;
            } else {
                execution.addLog("No data received from source adapter");
                return false;
            }
            
        } catch (Exception e) {
            execution.addLog("Failed to fetch data from source adapter: " + e.getMessage());
            log.error("Error fetching data from source adapter", e);
            return false;
        }
    }
}