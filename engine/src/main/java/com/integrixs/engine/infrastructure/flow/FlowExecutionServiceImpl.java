package com.integrixs.engine.infrastructure.flow;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.MappingMode;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.engine.domain.model.AdapterExecutionContext;
import com.integrixs.engine.domain.model.AdapterExecutionResult;
import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.FlowExecutionResult;
import com.integrixs.engine.domain.service.FlowAdapterExecutor;
import com.integrixs.engine.domain.service.FlowExecutionService;
import com.integrixs.engine.mapper.HierarchicalXmlFieldMapper;
import com.integrixs.engine.service.FlowMessageProcessor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure implementation of FlowExecutionService
 */
@Service
public class FlowExecutionServiceImpl implements FlowExecutionService {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionServiceImpl.class);


    private final FlowAdapterExecutor adapterExecutionService;
    private final FlowMessageProcessor flowMessageProcessor;
    private final HierarchicalXmlFieldMapper xmlFieldMapper;
    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final CommunicationAdapterSqlRepository communicationAdapterRepository;

    public FlowExecutionServiceImpl(FlowAdapterExecutor adapterExecutionService, FlowMessageProcessor flowMessageProcessor, HierarchicalXmlFieldMapper xmlFieldMapper, IntegrationFlowSqlRepository integrationFlowRepository, CommunicationAdapterSqlRepository communicationAdapterRepository) {
        this.adapterExecutionService = adapterExecutionService;
        this.flowMessageProcessor = flowMessageProcessor;
        this.xmlFieldMapper = xmlFieldMapper;
        this.integrationFlowRepository = integrationFlowRepository;
        this.communicationAdapterRepository = communicationAdapterRepository;
    }

    @Override
    public FlowExecutionResult executeFlow(IntegrationFlow flow, Object message, FlowExecutionContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting flow execution: {} with execution ID: {}", flow.getName(), context.getExecutionId());

        try {
            // Process through source adapter
            Object processedMessage = processSourceAdapter(message, flow.getInboundAdapterId().toString(), context);

            // Apply field mappings if required
            if(flow.getMappingMode() == MappingMode.WITH_MAPPING) {
                List<FieldMapping> mappings = (List<FieldMapping>) context.getMetadata().get("fieldMappings");
                if(mappings != null && !mappings.isEmpty()) {
                    processedMessage = applyFieldMappings(processedMessage, mappings, context);
                }
            }

            // Send through target adapter
            sendToTargetAdapter(processedMessage, flow.getOutboundAdapterId().toString(), context);

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
                    .inboundAdapterId(flow.getInboundAdapterId().toString())
                    .outboundAdapterId(flow.getOutboundAdapterId().toString())
                    .build();

            result.addMetadata("flowName", flow.getName());
            result.addMetadata("mappingMode", flow.getMappingMode());

            log.info("Flow execution completed successfully: {} in {}ms", flow.getName(), executionTime);
            return result;

        } catch(Exception e) {
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
                    .inboundAdapterId(flow.getInboundAdapterId().toString())
                    .outboundAdapterId(flow.getOutboundAdapterId().toString())
                    .build();
        }
    }

    @Override
    public CompletableFuture<FlowExecutionResult> executeFlowAsync(IntegrationFlow flow, Object message, FlowExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> executeFlow(flow, message, context));
    }

    @Override
    public Object processSourceAdapter(Object message, String inboundAdapterId, FlowExecutionContext context) {
        log.debug("Processing message through source adapter: {}", inboundAdapterId);

        try {
            // Get adapter configuration
            var inboundAdapter = communicationAdapterRepository.findById(UUID.fromString(inboundAdapterId))
                    .orElseThrow(() -> new IllegalArgumentException("Source adapter not found: " + inboundAdapterId));

            // Use message routing service for initial processing
            return flowMessageProcessor.processMessage(message, null, inboundAdapter.getConfiguration());

        } catch(Exception e) {
            log.error("Error processing source adapter {}: {}", inboundAdapterId, e.getMessage());
            throw new RuntimeException("Source adapter processing failed", e);
        }
    }

    @Override
    public Object applyFieldMappings(Object message, List<FieldMapping> mappings, FlowExecutionContext context) {
        log.debug("Applying {} field mappings", mappings.size());

        try {
            if(message instanceof String) {
                String xmlMessage = (String) message;
                Map<String, String> namespaces = (Map<String, String>) context.getMetadata().get("namespaces");

                return xmlFieldMapper.mapXmlFields(xmlMessage, null, mappings, namespaces);
            }

            // For non - XML messages, return as - is for now
            log.warn("Field mappings only supported for XML messages currently");
            return message;

        } catch(Exception e) {
            log.error("Error applying field mappings: {}", e.getMessage());
            throw new RuntimeException("Field mapping failed", e);
        }
    }

    @Override
    public void sendToTargetAdapter(Object message, String outboundAdapterId, FlowExecutionContext context) {
        log.debug("Sending message to target adapter: {}", outboundAdapterId);

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
            AdapterExecutionResult result = adapterExecutionService.sendData(outboundAdapterId, message, adapterContext);

            if(!result.isSuccess()) {
                throw new RuntimeException("Target adapter execution failed: " + result.getErrorMessage());
            }

            log.debug("Message sent successfully to target adapter");

        } catch(Exception e) {
            log.error("Error sending to target adapter {}: {}", outboundAdapterId, e.getMessage());
            throw new RuntimeException("Target adapter send failed", e);
        }
    }

    @Override
    public void validateFlow(IntegrationFlow flow) {
        if(flow == null) {
            throw new IllegalArgumentException("Flow cannot be null");
        }

        if(flow.getInboundAdapterId() == null) {
            throw new IllegalArgumentException("Flow must have a source adapter");
        }

        if(flow.getOutboundAdapterId() == null) {
            throw new IllegalArgumentException("Flow must have a target adapter");
        }

        if(!flow.isActive()) {
            throw new IllegalArgumentException("Flow is not active");
        }

        // Check if adapters exist and are active
        if(!adapterExecutionService.isAdapterReady(flow.getInboundAdapterId().toString())) {
            throw new IllegalArgumentException("Source adapter is not ready");
        }

        if(!adapterExecutionService.isAdapterReady(flow.getOutboundAdapterId().toString())) {
            throw new IllegalArgumentException("Target adapter is not ready");
        }
    }

    @Override
    public boolean isFlowReady(String flowId) {
        try {
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElse(null);

            if(flow == null) {
                return false;
            }

            validateFlow(flow);
            return true;

        } catch(Exception e) {
            log.debug("Flow {} is not ready: {}", flowId, e.getMessage());
            return false;
        }
    }
}
