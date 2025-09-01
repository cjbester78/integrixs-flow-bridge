package com.integrixs.engine.infrastructure.flow;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.MappingMode;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.engine.domain.model.AdapterExecutionContext;
import com.integrixs.engine.domain.model.AdapterExecutionResult;
import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.FlowExecutionResult;
import com.integrixs.engine.domain.service.AdapterExecutionService;
import com.integrixs.engine.domain.service.FlowExecutionService;
import com.integrixs.engine.mapper.HierarchicalXmlFieldMapper;
import com.integrixs.engine.service.MessageRoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Infrastructure implementation of FlowExecutionService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowExecutionServiceImpl implements FlowExecutionService {
    
    private final AdapterExecutionService adapterExecutionService;
    private final MessageRoutingService messageRoutingService;
    private final HierarchicalXmlFieldMapper xmlFieldMapper;
    private final IntegrationFlowRepository integrationFlowRepository;
    private final CommunicationAdapterRepository communicationAdapterRepository;
    
    @Override
    public FlowExecutionResult executeFlow(IntegrationFlow flow, Object message, FlowExecutionContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting flow execution: {} with execution ID: {}", flow.getName(), context.getExecutionId());
        
        try {
            // Process through source adapter
            Object processedMessage = processSourceAdapter(message, flow.getSourceAdapterId().toString(), context);
            
            // Apply field mappings if required
            if (flow.getMappingMode() == MappingMode.WITH_MAPPING) {
                List<FieldMapping> mappings = (List<FieldMapping>) context.getMetadata().get("fieldMappings");
                if (mappings != null && !mappings.isEmpty()) {
                    processedMessage = applyFieldMappings(processedMessage, mappings, context);
                }
            }
            
            // Send through target adapter
            sendToTargetAdapter(processedMessage, flow.getTargetAdapterId().toString(), context);
            
            // Calculate execution time
            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();
            
            // Build success result
            FlowExecutionResult result = FlowExecutionResult.builder()
                    .executionId(context.getExecutionId())
                    .flowId(flow.getId().toString())
                    .success(true)
                    .processedData(processedMessage)
                    .timestamp(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .sourceAdapterId(flow.getSourceAdapterId().toString())
                    .targetAdapterId(flow.getTargetAdapterId().toString())
                    .build();
            
            result.addMetadata("flowName", flow.getName());
            result.addMetadata("mappingMode", flow.getMappingMode());
            
            log.info("Flow execution completed successfully: {} in {}ms", flow.getName(), executionTime);
            return result;
            
        } catch (Exception e) {
            log.error("Error executing flow {}: {}", flow.getName(), e.getMessage(), e);
            
            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();
            
            return FlowExecutionResult.builder()
                    .executionId(context.getExecutionId())
                    .flowId(flow.getId().toString())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorCode("FLOW_EXECUTION_ERROR")
                    .timestamp(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .sourceAdapterId(flow.getSourceAdapterId().toString())
                    .targetAdapterId(flow.getTargetAdapterId().toString())
                    .build();
        }
    }
    
    @Override
    public CompletableFuture<FlowExecutionResult> executeFlowAsync(IntegrationFlow flow, Object message, FlowExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> executeFlow(flow, message, context));
    }
    
    @Override
    public Object processSourceAdapter(Object message, String sourceAdapterId, FlowExecutionContext context) {
        log.debug("Processing message through source adapter: {}", sourceAdapterId);
        
        try {
            // Get adapter configuration
            var sourceAdapter = communicationAdapterRepository.findById(UUID.fromString(sourceAdapterId))
                    .orElseThrow(() -> new IllegalArgumentException("Source adapter not found: " + sourceAdapterId));
            
            // Use message routing service for initial processing
            return messageRoutingService.processMessage(message, null, sourceAdapter.getConfiguration());
            
        } catch (Exception e) {
            log.error("Error processing source adapter {}: {}", sourceAdapterId, e.getMessage());
            throw new RuntimeException("Source adapter processing failed", e);
        }
    }
    
    @Override
    public Object applyFieldMappings(Object message, List<FieldMapping> mappings, FlowExecutionContext context) {
        log.debug("Applying {} field mappings", mappings.size());
        
        try {
            if (message instanceof String) {
                String xmlMessage = (String) message;
                Map<String, String> namespaces = (Map<String, String>) context.getMetadata().get("namespaces");
                
                return xmlFieldMapper.mapXmlFields(xmlMessage, null, mappings, namespaces);
            }
            
            // For non-XML messages, return as-is for now
            log.warn("Field mappings only supported for XML messages currently");
            return message;
            
        } catch (Exception e) {
            log.error("Error applying field mappings: {}", e.getMessage());
            throw new RuntimeException("Field mapping failed", e);
        }
    }
    
    @Override
    public void sendToTargetAdapter(Object message, String targetAdapterId, FlowExecutionContext context) {
        log.debug("Sending message to target adapter: {}", targetAdapterId);
        
        try {
            // Build adapter execution context
            AdapterExecutionContext adapterContext = AdapterExecutionContext.builder()
                    .executionId(context.getExecutionId())
                    .flowId(context.getFlowId())
                    .parameters(context.getParameters())
                    .headers(context.getHeaders())
                    .metadata(context.getMetadata())
                    .correlationId(context.getCorrelationId())
                    .timeout(context.getTimeout())
                    .build();
            
            // Execute adapter
            AdapterExecutionResult result = adapterExecutionService.sendData(targetAdapterId, message, adapterContext);
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Target adapter execution failed: " + result.getErrorMessage());
            }
            
            log.debug("Message sent successfully to target adapter");
            
        } catch (Exception e) {
            log.error("Error sending to target adapter {}: {}", targetAdapterId, e.getMessage());
            throw new RuntimeException("Target adapter send failed", e);
        }
    }
    
    @Override
    public void validateFlow(IntegrationFlow flow) {
        if (flow == null) {
            throw new IllegalArgumentException("Flow cannot be null");
        }
        
        if (flow.getSourceAdapterId() == null) {
            throw new IllegalArgumentException("Flow must have a source adapter");
        }
        
        if (flow.getTargetAdapterId() == null) {
            throw new IllegalArgumentException("Flow must have a target adapter");
        }
        
        if (!flow.isActive()) {
            throw new IllegalArgumentException("Flow is not active");
        }
        
        // Check if adapters exist and are active
        if (!adapterExecutionService.isAdapterReady(flow.getSourceAdapterId().toString())) {
            throw new IllegalArgumentException("Source adapter is not ready");
        }
        
        if (!adapterExecutionService.isAdapterReady(flow.getTargetAdapterId().toString())) {
            throw new IllegalArgumentException("Target adapter is not ready");
        }
    }
    
    @Override
    public boolean isFlowReady(String flowId) {
        try {
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElse(null);
            
            if (flow == null) {
                return false;
            }
            
            validateFlow(flow);
            return true;
            
        } catch (Exception e) {
            log.debug("Flow {} is not ready: {}", flowId, e.getMessage());
            return false;
        }
    }
}