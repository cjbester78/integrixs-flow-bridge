package com.integrixs.backend.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.domain.service.FlowExecutionService;
import com.integrixs.backend.infrastructure.adapter.AdapterConfigurationService;
import com.integrixs.backend.service.*;
import com.integrixs.backend.service.MessageService;
import com.integrixs.backend.service.transformation.EnrichmentTransformationService;
import com.integrixs.backend.service.transformation.FilterTransformationService;
import com.integrixs.backend.service.transformation.ValidationTransformationService;
import com.integrixs.backend.util.FieldMapper;
import com.integrixs.backend.service.JavaTransformationEngine;
import com.integrixs.backend.util.JavaFunctionRunner;
import com.integrixs.data.model.*;
import com.integrixs.data.sql.repository.*;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for orchestrating asynchronous flow execution
 * Handles ETL operations, file transfers, and data synchronization
 */
@Service
public class FlowExecutionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionApplicationService.class);


    private final FlowExecutionService flowExecutionService;
    private final AdapterConfigurationService adapterConfigurationService;
    private final IntegrationFlowSqlRepository flowRepository;
    private final CommunicationAdapterSqlRepository adapterRepository;
    private final FlowTransformationSqlRepository transformationRepository;
    private final FieldMappingSqlRepository fieldMappingRepository;
    private final AdapterExecutor adapterExecutor;
    private final FormatConversionService formatConversionService;
    private final DirectFileTransferService directFileTransferService;
    private final MessageService messageService;
    private final FilterTransformationService filterTransformationService;
    private final EnrichmentTransformationService enrichmentTransformationService;
    private final ValidationTransformationService validationTransformationService;
    private final DevelopmentFunctionService developmentFunctionService;
    private final JavaTransformationEngine javaTransformationEngine;
    private final EnhancedFlowExecutionLogger flowLogger;
    private final JavaFunctionRunner javaFunctionRunner;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public FlowExecutionApplicationService(
            FlowExecutionService flowExecutionService,
            AdapterConfigurationService adapterConfigurationService,
            IntegrationFlowSqlRepository flowRepository,
            CommunicationAdapterSqlRepository adapterRepository,
            FlowTransformationSqlRepository transformationRepository,
            FieldMappingSqlRepository fieldMappingRepository,
            AdapterExecutor adapterExecutor,
            FormatConversionService formatConversionService,
            DirectFileTransferService directFileTransferService,
            MessageService messageService,
            FilterTransformationService filterTransformationService,
            EnrichmentTransformationService enrichmentTransformationService,
            ValidationTransformationService validationTransformationService,
            DevelopmentFunctionService developmentFunctionService,
            JavaTransformationEngine javaTransformationEngine,
            JavaFunctionRunner javaFunctionRunner,
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
        this.messageService = messageService;
        this.filterTransformationService = filterTransformationService;
        this.enrichmentTransformationService = enrichmentTransformationService;
        this.validationTransformationService = validationTransformationService;
        this.developmentFunctionService = developmentFunctionService;
        this.javaTransformationEngine = javaTransformationEngine;
        this.javaFunctionRunner = javaFunctionRunner;
        this.flowLogger = flowLogger != null ? flowLogger : new EnhancedFlowExecutionLogger();
    }

    /**
     * Execute a flow asynchronously
     * @param flowId The ID of the flow to execute
     */
    @Async
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

            // Check if we should skip XML conversion(direct passthrough)
            if(flowExecutionService.shouldUseDirectTransfer(flow)) {
                log.info("Executing direct transfer(skip XML conversion) for flow: {}", flow.getName());
                directFileTransferService.executeDirectTransfer(flow, inboundAdapter, outboundAdapter);
                return;
            }

            // Execute the flow
            long flowStartTime = System.currentTimeMillis();
            try {
                executeFlowWithTransformations(flow, inboundAdapter, outboundAdapter, correlationId);

                // Log flow completion
                long duration = System.currentTimeMillis() - flowStartTime;
                flowContext = FlowExecutionContext.builder()
                    .flowId(flow.getId().toString())
                    .flowName(flow.getName())
                    .flowVersion("1.0")
                    .sourceSystem(inboundAdapter.getName())
                    .targetSystem(outboundAdapter.getName())
                    .correlationId(correlationId)
                    .messageId(messageId)
                    .stepsExecuted(1)
                    .messagesProcessed(1)
                    .build();

                flowLogger.logFlowComplete(flowContext, duration);

            } catch(Exception e) {
                throw new RuntimeException("Flow execution failed: " + e.getMessage(), e);
            }

        } catch(XmlConversionException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("XML conversion error executing flow: {}", flow.getName(), e);

            flowContext.setCurrentStep("XML Conversion");
            flowLogger.logFlowError(flowContext, e, duration);

            throw new RuntimeException("XML conversion failed: " + e.getMessage(), e);
        } catch(Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error executing flow: {}", flow.getName(), e);

            if(flowContext != null) {
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
            .adapterType(inboundAdapter.getType().toString())
            .direction("INBOUND")
            .endpoint(getConfigValue(inboundAdapter.getConfiguration(), "url"))
            .protocol(inboundAdapter.getType().toString())
            .sourceSystem(inboundAdapter.getName())
            .payloadSize(0)
            .build();

        flowLogger.logAdapterCommunication(inboundContext);

        Object rawData = adapterExecutor.fetchDataAsObject(flow.getInboundAdapterId().toString());
        log.info("Fetched data from source adapter: {}", inboundAdapter.getName());

        // Log source adapter payload
        String rawDataStr = flowExecutionService.convertRawDataToString(rawData);
        messageService.logAdapterPayload(correlationId, inboundAdapter, "REQUEST", rawDataStr, "INBOUND");

        // Check if the data is binary and should skip XML conversion
        if(flowExecutionService.isBinaryData(rawData)) {
            log.info("Binary file detected, using direct transfer for flow: {}", flow.getName());
            directFileTransferService.executeDirectTransfer(flow, inboundAdapter, outboundAdapter);
            return;
        }

        String processedData = processData(flow, rawData, inboundAdapter, outboundAdapter);

        // Step 3: Send to target adapter
        // Log outbound adapter communication
        AdapterCommunicationContext outboundContext = AdapterCommunicationContext.builder()
            .adapterName(outboundAdapter.getName())
            .adapterType(outboundAdapter.getType().toString())
            .direction("OUTBOUND")
            .endpoint(getConfigValue(outboundAdapter.getConfiguration(), "url"))
            .protocol(outboundAdapter.getType().toString())
            .targetSystem(outboundAdapter.getName())
            .payloadSize(processedData.length())
            .build();

        flowLogger.logAdapterCommunication(outboundContext);

        Map<String, Object> context = flowExecutionService.buildExecutionContext(correlationId, flow.getId());
        adapterExecutor.sendData(flow.getOutboundAdapterId().toString(), processedData, context);
        log.info("Sent data to target adapter: {}", outboundAdapter.getName());

        // Log target adapter payload
        messageService.logAdapterPayload(correlationId, outboundAdapter, "REQUEST", processedData, "OUTBOUND");

        // Step 4: Log success
        log.info("Flow execution successful for flow: {} - correlationId: {}", flow.getName(), correlationId);

        // Log response mapping if applicable
        if(flowExecutionService.isMappingRequired(flow)) {
            flowLogger.logResponseMapping(flow.getName(), "1.0",
                String.valueOf(System.currentTimeMillis() - System.currentTimeMillis()));
        }
    }

    private String processData(
            IntegrationFlow flow,
            Object rawData,
            CommunicationAdapter inboundAdapter,
            CommunicationAdapter outboundAdapter) throws Exception {

        if(flowExecutionService.isMappingRequired(flow)) {
            log.info("Mapping required for flow: {}", flow.getName());

            // Convert source data to XML
            flowLogger.logRequestMapping(flow.getName(), "1.0",
                inboundAdapter.getType().toString(), "XML");

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
            log.info("Converted XML to target format: {}", outboundAdapter.getType().toString());

            return targetData.toString();
        } else {
            log.info("Pass - through mode for flow: {}", flow.getName());
            return rawData.toString();
        }
    }

    private String applyTransformations(IntegrationFlow flow, String inputData) throws Exception {
        List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
        transformations = flowExecutionService.orderTransformations(transformations);

        String currentData = inputData;
        for(FlowTransformation transformation : transformations) {
            if(!flowExecutionService.isTransformationTypeSupported(transformation.getType())) {
                log.warn("Transformation type not supported: {}. Skipping transformation.", transformation.getType());
                continue;
            }

            TransformationContext transformContext = TransformationContext.builder()
                .stepNumber(transformations.indexOf(transformation) + 1)
                .totalSteps(transformations.size())
                .transformationName(transformation.getName())
                .transformationType(transformation.getType().toString())
                .inputFormat("XML")
                .outputFormat("XML")
                .configuration(parseConfigToMap(transformation.getConfiguration()))
                .build();

            flowLogger.logTransformationStep(transformContext);

            currentData = applyTransformation(transformation, currentData);
        }
        return currentData;
    }

    private String applyTransformation(FlowTransformation transformation, String currentData) throws Exception {
        switch(transformation.getType()) {
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
                log.warn("Transformation type not supported: {}. Returning original data.", transformation.getType());
                return currentData;
        }
    }

    private String applyCustomFunctionTransformation(FlowTransformation transformation, String currentData)
            throws Exception {
        if(transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
            throw new RuntimeException("Custom function configuration is missing");
        }

        CustomFunctionConfigDTO config = objectMapper.readValue(
            transformation.getConfiguration(),
            CustomFunctionConfigDTO.class
       );
        Map<String, Object> inputMap = objectMapper.readValue(currentData, Map.class);

        String functionBody = config.getJavaFunction();
        if(functionBody == null || functionBody.isBlank()) {
            throw new RuntimeException("Custom function name/body is missing");
        }

        // Try to find the function in transformation_custom_functions table
        try {
            TransformationCustomFunction customFunction =
                developmentFunctionService.getBuiltInFunctionByName(functionBody);
            functionBody = customFunction.getFunctionBody();
        } catch(Exception e) {
            // Function not found in database, assume functionBody contains the actual code
            log.debug("Function ' {}' not found in transformation_custom_functions, using as direct code",
                functionBody);
        }

        Object result = javaFunctionRunner.execute("customFunc_" + System.currentTimeMillis(), functionBody, "transform", config.getSourceFields(), inputMap);
        return result != null ? result.toString() : null;
    }

    private String applyFilterTransformation(FlowTransformation transformation, String currentData)
            throws Exception {
        if(transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
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
        if(transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
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
        if(transformation.getConfiguration() == null || transformation.getConfiguration().isBlank()) {
            throw new RuntimeException("Validation transformation configuration is missing");
        }
        ValidationTransformationConfigDTO validationConfig = objectMapper.readValue(
            transformation.getConfiguration(),
            ValidationTransformationConfigDTO.class
       );
        return validationTransformationService.applyValidation(currentData, validationConfig);
    }

    private String getConfigValue(String configJson, String key) {
        if (configJson == null || configJson.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            Object value = config.get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to parse configuration JSON", e);
            return null;
        }
    }

    private Map<String, String> parseConfigToMap(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            Map<String, String> result = new HashMap<>();
            config.forEach((k, v) -> result.put(k, v != null ? v.toString() : null));
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse configuration JSON", e);
            return new HashMap<>();
        }
    }
}
