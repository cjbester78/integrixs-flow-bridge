package com.integrixs.backend.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.domain.service.FlowExecutionService;
import com.integrixs.backend.infrastructure.adapter.AdapterConfigurationService;
import com.integrixs.backend.service.*;
// import com.integrixs.backend.service.deprecated.MessageService;
// import com.integrixs.backend.service.deprecated.LogService;
import com.integrixs.backend.service.transformation.EnrichmentTransformationService;
import com.integrixs.backend.service.transformation.FilterTransformationService;
import com.integrixs.backend.service.transformation.ValidationTransformationService;
import com.integrixs.backend.util.FieldMapper;
import com.integrixs.backend.service.JavaTransformationEngine;
import com.integrixs.backend.util.JavaFunctionRunner;
import com.integrixs.data.model.*;
import com.integrixs.data.repository.*;
import com.integrixs.engine.AdapterExecutor;
import com.integrixs.engine.service.FormatConversionService;
import com.integrixs.engine.xml.XmlConversionException;
import com.integrixs.shared.dto.transformation.CustomFunctionConfigDTO;
import com.integrixs.shared.dto.transformation.EnrichmentTransformationConfigDTO;
import com.integrixs.shared.dto.transformation.FilterTransformationConfigDTO;
import com.integrixs.shared.dto.transformation.ValidationTransformationConfigDTO;
import com.integrixs.backend.logging.EnhancedFlowExecutionLogger;
import com.integrixs.backend.logging.EnhancedFlowExecutionLogger.*;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for orchestrating asynchronous flow execution
 * Handles ETL operations, file transfers, and data synchronization
 */
@Slf4j
@Service
public class FlowExecutionApplicationService {
    
    private final FlowExecutionService flowExecutionService;
    private final AdapterConfigurationService adapterConfigurationService;
    private final IntegrationFlowRepository flowRepository;
    private final CommunicationAdapterRepository adapterRepository;
    private final FlowTransformationRepository transformationRepository;
    private final FieldMappingRepository fieldMappingRepository;
    private final AdapterExecutor adapterExecutor;
    private final FormatConversionService formatConversionService;
    private final DirectFileTransferService directFileTransferService;
    // private final MessageService messageService;
    // private final LogService logService;
    private final FilterTransformationService filterTransformationService;
    private final EnrichmentTransformationService enrichmentTransformationService;
    private final ValidationTransformationService validationTransformationService;
    private final DevelopmentFunctionService developmentFunctionService;
    private final JavaTransformationEngine javaTransformationEngine;
    private final EnhancedFlowExecutionLogger flowLogger;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    public FlowExecutionApplicationService(
            FlowExecutionService flowExecutionService,
            AdapterConfigurationService adapterConfigurationService,
            IntegrationFlowRepository flowRepository,
            CommunicationAdapterRepository adapterRepository,
            FlowTransformationRepository transformationRepository,
            FieldMappingRepository fieldMappingRepository,
            AdapterExecutor adapterExecutor,
            FormatConversionService formatConversionService,
            DirectFileTransferService directFileTransferService,
            FilterTransformationService filterTransformationService,
            EnrichmentTransformationService enrichmentTransformationService,
            ValidationTransformationService validationTransformationService,
            DevelopmentFunctionService developmentFunctionService,
            JavaTransformationEngine javaTransformationEngine,
            @Autowired(required = false) EnhancedFlowExecutionLogger flowLogger) {
        this.flowExecutionService = flowExecutionService;
        this.adapterConfigurationService = adapterConfigurationService;
        this.flowRepository = flowRepository;
        this.adapterRepository = adapterRepository;
        this.transformationRepository = transformationRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.adapterExecutor = adapterExecutor;
        this.formatConversionService = formatConversionService;
        this.directFileTransferService = directFileTransferService;
        this.filterTransformationService = filterTransformationService;
        this.enrichmentTransformationService = enrichmentTransformationService;
        this.validationTransformationService = validationTransformationService;
        this.developmentFunctionService = developmentFunctionService;
        this.javaTransformationEngine = javaTransformationEngine;
        this.flowLogger = flowLogger != null ? flowLogger : new EnhancedFlowExecutionLogger();
    }
    
    /**
     * Execute a flow asynchronously
     * @param flowId The ID of the flow to execute
     */
    @Async
    @Transactional
    public void executeFlow(String flowId) {
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
                .orElseThrow(() -> new RuntimeException("Flow not found"));

        // Create correlation ID for this flow execution
        String correlationId = UUID.randomUUID().toString();
        String messageId = "MSG-" + UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        FlowExecutionContext flowContext = null;
        
        log.info("Starting flow execution with correlation ID: {}", correlationId);

        try {
            // Get adapters
            CommunicationAdapter inboundAdapter = adapterRepository.findById(flow.getInboundAdapterId())
                    .orElseThrow(() -> new RuntimeException("Source adapter not found"));
            CommunicationAdapter outboundAdapter = adapterRepository.findById(flow.getOutboundAdapterId())
                    .orElseThrow(() -> new RuntimeException("Target adapter not found"));

            // Validate flow can be executed
            flowExecutionService.validateFlowExecution(flow, inboundAdapter, outboundAdapter);
            
            // Log flow execution start
            flowContext = FlowExecutionContext.builder()
                .flowId(flow.getId().toString())
                .flowName(flow.getName())
                .flowVersion(flow.getVersion() != null ? flow.getVersion().toString() : "1.0")
                .sourceSystem(inboundAdapter.getName())
                .targetSystem(outboundAdapter.getName())
                .correlationId(correlationId)
                .messageId(messageId)
                .payloadSize(0)
                .build();
            
            flowLogger.logFlowStart(flowContext);

            // Check if we should skip XML conversion (direct passthrough)
            if (flowExecutionService.shouldUseDirectTransfer(flow)) {
                log.info("Executing direct transfer (skip XML conversion) for flow: {}", flow.getName());
                directFileTransferService.executeDirectTransfer(flow, inboundAdapter, outboundAdapter);
                return;
            }

            // Execute the flow
            long startTime = System.currentTimeMillis();
            try {
                executeFlowWithTransformations(flow, inboundAdapter, outboundAdapter, correlationId);
                
                // Log flow completion
                long duration = System.currentTimeMillis() - startTime;
                flowContext = FlowExecutionContext.builder()
                    .flowId(flow.getId().toString())
                    .flowName(flow.getName())
                    .flowVersion(flow.getVersion() != null ? flow.getVersion().toString() : "1.0")
                    .sourceSystem(inboundAdapter.getName())
                    .targetSystem(outboundAdapter.getName())
                    .correlationId(correlationId)
                    .messageId(messageId)
                    .stepsExecuted(1)
                    .messagesProcessed(1)
                    .build();
                    
                flowLogger.logFlowComplete(flowContext, duration);
                
            } catch (Exception e) {
                throw new RuntimeException("Flow execution failed: " + e.getMessage(), e);
            }

        } catch (XmlConversionException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("XML conversion error executing flow: {}", flow.getName(), e);
            
            flowContext.setCurrentStep("XML Conversion");
            flowLogger.logFlowError(flowContext, e, duration);
            
            throw new RuntimeException("XML conversion failed: " + e.getMessage(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error executing flow: {}", flow.getName(), e);
            
            if (flowContext != null) {
                flowLogger.logFlowError(flowContext, e, duration);
            }
            
            throw new RuntimeException("Error executing flow: " + flow.getName(), e);
        }
    }
    
    private void executeFlowWithTransformations(
            IntegrationFlow flow,
            CommunicationAdapter inboundAdapter,
            CommunicationAdapter outboundAdapter,
            String correlationId) throws Exception {
        
        // Step 1: Fetch source data
        // Log adapter communication
        AdapterCommunicationContext inboundContext = AdapterCommunicationContext.builder()
            .adapterName(inboundAdapter.getName())
            .adapterType(inboundAdapter.getType())
            .direction("INBOUND")
            .endpoint(inboundAdapter.getConfiguration().get("url"))
            .protocol(inboundAdapter.getType())
            .sourceSystem(inboundAdapter.getName())
            .payloadSize(0)
            .build();
        
        flowLogger.logAdapterCommunication(inboundContext);
        
        Object rawData = adapterExecutor.fetchDataAsObject(flow.getInboundAdapterId().toString());
        log.info("Fetched data from source adapter: {}", inboundAdapter.getName());
        
        // Log source adapter payload
        String rawDataStr = flowExecutionService.convertRawDataToString(rawData);
        // TODO: Replace messageService.logAdapterPayload(correlationId, inboundAdapter, "REQUEST", rawDataStr, "INBOUND");
        log.debug("Source adapter payload - correlationId: {}, adapter: {}, payload: {}", correlationId, inboundAdapter.getName(), rawDataStr);

        // Check if the data is binary and should skip XML conversion
        if (flowExecutionService.isBinaryData(rawData)) {
            log.info("Binary file detected, using direct transfer for flow: {}", flow.getName());
            directFileTransferService.executeDirectTransfer(flow, inboundAdapter, outboundAdapter);
            return;
        }

        String processedData = processData(flow, rawData, inboundAdapter, outboundAdapter);

        // Step 3: Send to target adapter
        // Log outbound adapter communication
        AdapterCommunicationContext outboundContext = AdapterCommunicationContext.builder()
            .adapterName(outboundAdapter.getName())
            .adapterType(outboundAdapter.getType())
            .direction("OUTBOUND")
            .endpoint(outboundAdapter.getConfiguration().get("url"))
            .protocol(outboundAdapter.getType())
            .targetSystem(outboundAdapter.getName())
            .payloadSize(processedData.length())
            .build();
            
        flowLogger.logAdapterCommunication(outboundContext);
        
        Map<String, Object> context = flowExecutionService.buildExecutionContext(correlationId, flow.getId());
        adapterExecutor.sendData(flow.getOutboundAdapterId().toString(), processedData, context);
        log.info("Sent data to target adapter: {}", outboundAdapter.getName());
        
        // Log target adapter payload
        // TODO: Replace messageService.logAdapterPayload(correlationId, outboundAdapter, "REQUEST", processedData, "OUTBOUND");
        log.debug("Target adapter payload - correlationId: {}, adapter: {}, payload: {}", correlationId, outboundAdapter.getName(), processedData);

        // Step 4: Log success
        log.info("Flow execution successful for flow: {} - correlationId: {}", flow.getName(), correlationId);
        
        // Log response mapping if applicable
        if (flowExecutionService.isMappingRequired(flow)) {
            flowLogger.logResponseMapping(flow.getName(), flow.getVersion() != null ? flow.getVersion().toString() : "1.0", 
                String.valueOf(System.currentTimeMillis() - System.currentTimeMillis()));
        }
    }
    
    private String processData(
            IntegrationFlow flow,
            Object rawData,
            CommunicationAdapter inboundAdapter,
            CommunicationAdapter outboundAdapter) throws Exception {
        
        if (flowExecutionService.isMappingRequired(flow)) {
            log.info("Mapping required for flow: {}", flow.getName());
            
            // Convert source data to XML
            flowLogger.logRequestMapping(flow.getName(), flow.getVersion() != null ? flow.getVersion().toString() : "1.0", 
                inboundAdapter.getType(), "XML");
            
            String xmlData = formatConversionService.convertToXml(rawData, inboundAdapter);
            log.debug("Converted source data to XML");
            
            // Apply transformations
            String transformedXml = applyTransformations(flow, xmlData);
            log.debug("Applied transformations to XML data");
            
            // Get conversion config
            List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
            Map<String, Object> conversionConfig = adapterConfigurationService.buildConversionConfig(
                flow, 
                outboundAdapter, 
                transformations,
                transformationId -> fieldMappingRepository.findByTransformationId(transformationId)
            );
            
            // Convert XML back to target format
            Object targetData = formatConversionService.convertFromXml(
                transformedXml, 
                outboundAdapter, 
                conversionConfig
            );
            log.info("Converted XML to target format: {}", outboundAdapter.getType());
            
            return targetData.toString();
        } else {
            log.info("Pass-through mode for flow: {}", flow.getName());
            return rawData.toString();
        }
    }
    
    private String applyTransformations(IntegrationFlow flow, String inputData) throws Exception {
        List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
        transformations = flowExecutionService.orderTransformations(transformations);

        String currentData = inputData;
        for (FlowTransformation transformation : transformations) {
            if (!flowExecutionService.isTransformationTypeSupported(transformation.getType())) {
                throw new UnsupportedOperationException(
                    "Transformation type not supported: " + transformation.getType()
                );
            }

            TransformationContext transformContext = TransformationContext.builder()
                .stepNumber(transformations.indexOf(transformation) + 1)
                .totalSteps(transformations.size())
                .transformationName(transformation.getName())
                .transformationType(transformation.getType())
                .inputFormat("XML")
                .outputFormat("XML")
                .configuration(transformation.getConfiguration())
                .build();
                
            flowLogger.logTransformationStep(transformContext);
            
            currentData = applyTransformation(transformation, currentData);
        }
        return currentData;
    }
    
    private String applyTransformation(FlowTransformation transformation, String currentData) throws Exception {
        switch (transformation.getType()) {
            case FIELD_MAPPING:
                List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformation.getId());
                return FieldMapper.apply(currentData, mappings, javaTransformationEngine);
                
            case CUSTOM_FUNCTION:
                return applyCustomFunctionTransformation(transformation, currentData);
                
            case FILTER:
                return applyFilterTransformation(transformation, currentData);
                
            case ENRICHMENT:
                return applyEnrichmentTransformation(transformation, currentData);
                
            case VALIDATION:
                return applyValidationTransformation(transformation, currentData);
                
            default:
                throw new UnsupportedOperationException(
                    "Transformation type not supported: " + transformation.getType()
                );
        }
    }
    
    private String applyCustomFunctionTransformation(FlowTransformation transformation, String currentData) 
            throws Exception {
        if (transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
            throw new RuntimeException("Custom function configuration is missing");
        }
        
        CustomFunctionConfigDTO config = objectMapper.readValue(
            transformation.getConfiguration(), 
            CustomFunctionConfigDTO.class
        );
        Map<String, Object> inputMap = objectMapper.readValue(currentData, Map.class);

        String functionBody = config.getJavaFunction();
        if (functionBody == null || functionBody.isBlank()) {
            throw new RuntimeException("Custom function name/body is missing");
        }

        // Try to find the function in transformation_custom_functions table
        try {
            TransformationCustomFunction customFunction = 
                developmentFunctionService.getBuiltInFunctionByName(functionBody);
            functionBody = customFunction.getFunctionBody();
        } catch (Exception e) {
            // Function not found in database, assume functionBody contains the actual code
            log.debug("Function '{}' not found in transformation_custom_functions, using as direct code", 
                functionBody);
        }

        Object result = JavaFunctionRunner.run(functionBody, config.getSourceFields(), inputMap);
        return result != null ? result.toString() : null;
    }
    
    private String applyFilterTransformation(FlowTransformation transformation, String currentData) 
            throws Exception {
        if (transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
            throw new RuntimeException("Filter transformation configuration is missing");
        }
        FilterTransformationConfigDTO filterConfig = objectMapper.readValue(
            transformation.getConfiguration(), 
            FilterTransformationConfigDTO.class
        );
        return filterTransformationService.applyFilter(currentData, filterConfig);
    }
    
    private String applyEnrichmentTransformation(FlowTransformation transformation, String currentData) 
            throws Exception {
        if (transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
            throw new RuntimeException("Enrichment transformation configuration is missing");
        }
        EnrichmentTransformationConfigDTO enrichConfig = objectMapper.readValue(
            transformation.getConfiguration(), 
            EnrichmentTransformationConfigDTO.class
        );
        return enrichmentTransformationService.applyEnrichment(currentData, enrichConfig);
    }
    
    private String applyValidationTransformation(FlowTransformation transformation, String currentData) 
            throws Exception {
        if (transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
            throw new RuntimeException("Validation transformation configuration is missing");
        }
        ValidationTransformationConfigDTO validationConfig = objectMapper.readValue(
            transformation.getConfiguration(), 
            ValidationTransformationConfigDTO.class
        );
        return validationTransformationService.applyValidation(currentData, validationConfig);
    }
}