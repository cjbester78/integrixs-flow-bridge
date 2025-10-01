package com.integrixs.backend.infrastructure.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.domain.model.OrchestrationExecution;
import com.integrixs.backend.service.TransformationExecutionService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.engine.AdapterExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure service for executing orchestration steps
 */
@Service
public class OrchestrationExecutor {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationExecutor.class);


    private final TransformationExecutionService transformationService;
    private final CommunicationAdapterSqlRepository adapterRepository;
    private final AdapterExecutor adapterExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrchestrationExecutor(TransformationExecutionService transformationService,
                               CommunicationAdapterSqlRepository adapterRepository,
                               AdapterExecutor adapterExecutor) {
        this.transformationService = transformationService;
        this.adapterRepository = adapterRepository;
        this.adapterExecutor = adapterExecutor;
    }

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
            CommunicationAdapter inboundAdapter = adapterRepository.findById(flow.getInboundAdapterId())
                .orElseThrow(() -> new RuntimeException("Source adapter not found"));

            if(!inboundAdapter.isActive()) {
                execution.addLog("Source adapter is not active: " + inboundAdapter.getName());
                return false;
            }

            // Validate target adapter
            CommunicationAdapter outboundAdapter = adapterRepository.findById(flow.getOutboundAdapterId())
                .orElseThrow(() -> new RuntimeException("Target adapter not found"));

            if(!outboundAdapter.isActive()) {
                execution.addLog("Target adapter is not active: " + outboundAdapter.getName());
                return false;
            }

            // Store adapter info in context
            execution.addContext("inboundAdapter", inboundAdapter.getName());
            execution.addContext("outboundAdapter", outboundAdapter.getName());
            execution.addContext("inboundAdapterId", inboundAdapter.getId().toString());
            execution.addContext("outboundAdapterId", outboundAdapter.getId().toString());

            execution.addLog("Adapters initialized successfully");
            return true;

        } catch(Exception e) {
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

            if(transformationResult.isSuccess()) {
                execution.setTransformedData(transformationResult.getData());
                execution.addLog("Transformations executed successfully");
                return true;
            } else {
                execution.addLog("Transformation failed: " + transformationResult.getMessage());
                return false;
            }

        } catch(Exception e) {
            execution.addLog("Failed to execute transformations: " + e.getMessage());
            log.error("Error executing transformations", e);
            return false;
        }
    }

    /**
     * Send data to target adapter
     * @param execution The orchestration execution
     * @param outboundAdapterId The target adapter ID
     * @return true if successful
     */
    public boolean sendToTargetAdapter(OrchestrationExecution execution, String outboundAdapterId) {
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
            if(dataToSend instanceof String) {
                payload = (String) dataToSend;
            } else {
                payload = objectMapper.writeValueAsString(dataToSend);
            }
            adapterExecutor.sendData(outboundAdapterId, payload, context);

            // Store output
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("data", dataToSend);
            outputData.put("outboundAdapterId", outboundAdapterId);
            outputData.put("timestamp", System.currentTimeMillis());
            execution.setOutputData(outputData);

            execution.addLog("Data sent to target adapter successfully");
            return true;

        } catch(Exception e) {
            execution.addLog("Failed to send data to target adapter: " + e.getMessage());
            log.error("Error sending data to target adapter", e);
            return false;
        }
    }

    /**
     * Fetch data from source adapter
     * @param execution The orchestration execution
     * @param inboundAdapterId The source adapter ID
     * @return true if successful
     */
    public boolean fetchFromSourceAdapter(OrchestrationExecution execution, String inboundAdapterId) {
        try {
            execution.addLog("Fetching data from source adapter");

            // Fetch data
            Object data = adapterExecutor.fetchDataAsObject(inboundAdapterId);

            if(data != null) {
                execution.setInputData(data);
                execution.addContext("sourceDataReceived", true);
                execution.addLog("Data fetched from source adapter successfully");
                return true;
            } else {
                execution.addLog("No data received from source adapter");
                return false;
            }

        } catch(Exception e) {
            execution.addLog("Failed to fetch data from source adapter: " + e.getMessage());
            log.error("Error fetching data from source adapter", e);
            return false;
        }
    }
}
