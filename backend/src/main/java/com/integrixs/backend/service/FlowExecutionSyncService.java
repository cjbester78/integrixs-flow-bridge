package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.sql.repository.FieldMappingSqlRepository;
import com.integrixs.data.sql.repository.FlowStructureSqlRepository;
import com.integrixs.engine.mapper.HierarchicalXmlFieldMapper;
import com.integrixs.backend.utils.WsdlNamespaceExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

/**
 * Service that handles synchronous flow execution for real - time request/response processing.
 * Used for API integrations where an immediate response is required(SOAP, REST endpoints).
 */
@Service
public class FlowExecutionSyncService {

    private static final Logger logger = LoggerFactory.getLogger(FlowExecutionSyncService.class);

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Autowired
    private TransformationExecutionService transformationService;

    @Autowired
    private BackendAdapterExecutor adapterExecutionService;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private FieldMappingSqlRepository fieldMappingRepository;

    @Autowired
    private HierarchicalXmlFieldMapper xmlFieldMapper;

    @Autowired
    private FlowStructureSqlRepository flowStructureRepository;

    @Autowired
    private XmlValidationService xmlValidationService;

    @Autowired
    private WsdlSampleExtractorService wsdlSampleExtractor;

    /**
     * Process a message through an integration flow
     */
    public String processMessage(IntegrationFlow flow, String message, Map<String, String> headers, String protocol) throws Exception {
        logger.info("Processing message through flow: {} with protocol: {}", flow.getName(), protocol);

        // Get source and target adapters
        logger.info("Loading source adapter: {}", flow.getInboundAdapterId());
        CommunicationAdapter inboundAdapter = adapterRepository.findById(flow.getInboundAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Source adapter not found"));
        logger.info("Source adapter: {} (Type: {}, Mode: {})", inboundAdapter.getName(), inboundAdapter.getType(), inboundAdapter.getMode());

        logger.info("Loading target adapter: {}", flow.getOutboundAdapterId());
        CommunicationAdapter outboundAdapter = adapterRepository.findById(flow.getOutboundAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Target adapter not found"));
        logger.info("Target adapter: {} (Type: {}, Mode: {})", outboundAdapter.getName(), outboundAdapter.getType(), outboundAdapter.getMode());

        // Track processing context
        Map<String, Object> context = new HashMap<>();
        context.put("flowId", flow.getId().toString());
        context.put("flowName", flow.getName());
        context.put("protocol", protocol);
        context.put("headers", headers);

        // Get or create correlation ID
        String correlationId = headers.get("correlationId");
        if(correlationId == null) {
            // Create new correlation ID if not provided
            try {
                correlationId = UUID.randomUUID().toString();
                logger.info("Created message with correlationId: {} for flow: {}", correlationId, flow.getName());
            } catch(Exception e) {
                logger.error("Error creating message log, continuing with generated ID: {}", e.getMessage());
                correlationId = UUID.randomUUID().toString();
            }
        } else {
            // Use existing correlation ID
            try {
                logger.info("Using existing correlationId: {} for flow: {}", correlationId, flow.getName());
            } catch(Exception e) {
                logger.error("Error creating message log, continuing with existing ID: {}", e.getMessage());
            }
        }
        context.put("correlationId", correlationId);
        context.put("flowId", flow.getId().toString());
        // Pass through the isEndpointFlow flag if present
        if(headers.containsKey("isEndpointFlow")) {
            context.put("isEndpointFlow", "true".equals(headers.get("isEndpointFlow")));
        }
        logger.info("Using correlation ID: {}", correlationId);

        try {
            // Step 1: Validate incoming message against source data structure
            try {
                logger.info("Processing step - correlationId: {}, flow: {}, ", correlationId, flow.getName() + ", " +
                    "Validating message against source structure",
                    "Checking message format and structure",
                    com.integrixs.data.model.SystemLog.LogLevel.INFO);
            } catch(Exception e) {
                logger.warn("Failed to log validation step: {}", e.getMessage());
            }

            String validatedMessage = message;
            if(flow.getSourceFlowStructureId() != null) {
                FlowStructure sourceFlowStructure = flowStructureRepository.findById(flow.getSourceFlowStructureId())
                    .orElse(null);
                if(sourceFlowStructure != null) {
                    // Validate message against FlowStructure
                    XmlValidationService.ValidationResult validationResult =
                        xmlValidationService.validateMessageAgainstFlowStructure(message, sourceFlowStructure, context);

                    if(validationResult.isValid()) {
                        validatedMessage = validationResult.getValidatedMessage();
                        logger.info("Message validated successfully against FlowStructure: {}", sourceFlowStructure.getName());
                    } else {
                        logger.error("Message validation failed against FlowStructure {}: {}",
                            sourceFlowStructure.getName(), String.join(", ", validationResult.getErrors()));
                        // For now, log the error but continue processing
                        // In the future, could add a strict validation flag to flow configuration
                        logger.warn("Continuing despite validation errors");
                    }

                    try {
                        logger.info("Processing step - correlationId: {}, flow: {}, " +
                            "Message validation completed",
                            "Structure: " + sourceFlowStructure.getName() + ", Valid: " + validationResult.isValid(),
                            com.integrixs.data.model.SystemLog.LogLevel.INFO);
                    } catch(Exception e) {
                        logger.warn("Failed to log validation completion: {}", e.getMessage());
                    }
                }
            }

            // Step 2: Apply transformation if flow has mapping
            String transformedMessage = validatedMessage;
            if("WITH_MAPPING".equals(flow.getMappingMode().toString())) {
                logger.info("Applying field mapping transformation for flow: {}", flow.getName());

                try {
                    logger.info("Processing step - correlationId: {}, flow: {},", correlationId, flow.getName() + ", " +
                        "Applying data transformations",
                        "Mapping mode: WITH_MAPPING",
                        com.integrixs.data.model.SystemLog.LogLevel.INFO);
                } catch(Exception e) {
                    logger.warn("Failed to log transformation start: {}", e.getMessage());
                }

                // Get the flow's transformation
                if(flow.getTransformations() != null && !flow.getTransformations().isEmpty()) {
                    try {
                        // Get the transformation with the lowest execution order(for request mapping)
                        FlowTransformation transformation = flow.getTransformations().stream()
                            .min((t1, t2) -> Integer.compare(t1.getExecutionOrder(), t2.getExecutionOrder()))
                            .orElse(flow.getTransformations().get(0));

                        String transformationId = transformation.getId().toString();
                        logger.info("Using transformation: {} (ID: {}, execution order: {})",
                            transformation.getName(), transformationId, transformation.getExecutionOrder());

                        // Get field mappings for this transformation
                        List<FieldMapping> fieldMappings = fieldMappingRepository.findByTransformationId(UUID.fromString(transformationId));
                        logger.info("Found {} field mappings", fieldMappings.size());

                        if(fieldMappings.isEmpty()) {
                            logger.warn("No field mappings found for transformation: {}", transformationId);
                            transformedMessage = validatedMessage;
                        } else {
                            // For field mappings, we always work with XML
                            // Get the target template from the flow structure
                            String targetTemplate = null;
                            Map<String, String> namespaces = new HashMap<>();

                            // Extract namespaces from source flow structure
                            if(flow.getSourceFlowStructureId() != null) {
                                FlowStructure sourceFlowStructure = flowStructureRepository.findById(flow.getSourceFlowStructureId()).orElse(null);
                                if(sourceFlowStructure != null && sourceFlowStructure.getWsdlContent() != null) {
                                    logger.info("Extracting namespaces from source flow structure: {}", sourceFlowStructure.getName());
                                    Map<String, String> sourceNamespaces = WsdlNamespaceExtractor.extractNamespaces(sourceFlowStructure.getWsdlContent());
                                    namespaces.putAll(sourceNamespaces);
                                }
                            }

                            // Extract namespaces from target flow structure
                            if(flow.getTargetFlowStructureId() != null) {
                                logger.info("Target flow structure ID: {}", flow.getTargetFlowStructureId());
                                FlowStructure targetFlowStructure = flowStructureRepository.findById(flow.getTargetFlowStructureId()).orElse(null);
                                if(targetFlowStructure != null) {
                                    logger.info("Target flow structure found: {}", targetFlowStructure.getName());
                                    if(targetFlowStructure.getWsdlContent() != null) {
                                        logger.info("Target flow structure has WSDL content");

                                        // Extract the service namespace specifically
                                        Map<String, String> serviceNs = WsdlNamespaceExtractor.extractServiceNamespace(targetFlowStructure.getWsdlContent());
                                        if(serviceNs.containsKey("prefix") && serviceNs.containsKey("uri")) {
                                            String prefix = serviceNs.get("prefix");
                                            String uri = serviceNs.get("uri");
                                            namespaces.put(prefix, uri);
                                            logger.info("Using target service namespace: {} = {}", prefix, uri);
                                        }

                                        // Also extract all namespaces for completeness
                                        Map<String, String> targetNamespaces = WsdlNamespaceExtractor.extractNamespaces(targetFlowStructure.getWsdlContent());

                                        // Add other namespaces but don't override the service namespace
                                        for(Map.Entry<String, String> entry : targetNamespaces.entrySet()) {
                                            if(!namespaces.containsKey(entry.getKey())) {
                                                namespaces.put(entry.getKey(), entry.getValue());
                                            }
                                        }

                                        // Extract sample XML from WSDL for the target structure
                                        String operationName = (String) context.get("operationName");
                                        if(operationName != null) {
                                            targetTemplate = wsdlSampleExtractor.extractSampleRequestXml(
                                                targetFlowStructure.getWsdlContent(), operationName);
                                            logger.info("Extracted sample XML template for operation: {}", operationName);
                                        } else {
                                            targetTemplate = null; // Let the mapper create the structure
                                            logger.debug("No operation name in context, mapper will create structure");
                                        }
                                    }
                                } else {
                                    logger.warn("Target flow structure not found!");
                                }
                            } else {
                                logger.warn("Flow has no target flow structure ID!");
                            }

                            logger.info("Total namespaces extracted: {}", namespaces.size());
                            for(Map.Entry<String, String> ns : namespaces.entrySet()) {
                                logger.debug("Namespace: {} = {}", ns.getKey(), ns.getValue());
                            }

                            logger.debug("Field mappings count: {}", fieldMappings.size());
                            if(logger.isDebugEnabled()) {
                                for(int i = 0; i < fieldMappings.size(); i++) {
                                    FieldMapping fm = fieldMappings.get(i);
                                    logger.debug("Mapping {}: source = ' {}', target = ' {}', sourceXPath = ' {}', targetXPath = ' {}'",
                                        i + 1, fm.getSourceFields(), fm.getTargetField(), fm.getSourceXPath(), fm.getTargetXPath());
                                }
                            }
                            // Apply XML field mappings
                            transformedMessage = xmlFieldMapper.mapXmlFields(
                                validatedMessage, // source XML
                                targetTemplate,   // target template(can be null)
                                fieldMappings,    // field mappings
                                namespaces        // namespaces extracted from flow structures
                           );

                            logger.debug("Transformed message: {}", transformedMessage);
                            logger.info("Successfully applied {} field mappings", fieldMappings.size());
                            try {
                                logger.info("Processing step - correlationId: {}, flow: {}, " +
                                    "Transformation completed successfully",
                                    "Applied " + fieldMappings.size() + " field mappings",
                                    com.integrixs.data.model.SystemLog.LogLevel.INFO);
                            } catch(Exception e) {
                                logger.warn("Failed to log transformation completion: {}", e.getMessage());
                            }
                        }
                    } catch(Exception e) {
                        logger.error("Error applying XML transformation: {}", e.getMessage(), e);
                        try {
                            logger.info("Processing step - correlationId: {}, flow: {}, " +
                                "Transformation error",
                                e.getMessage(),
                                com.integrixs.data.model.SystemLog.LogLevel.ERROR);
                        } catch(Exception logEx) {
                            logger.warn("Failed to log transformation error: {}", logEx.getMessage());
                        }
                        throw new RuntimeException("Failed to apply XML transformation: " + e.getMessage(), e);
                    }
                } else {
                    logger.warn("Flow has WITH_MAPPING mode but no transformations!");
                    transformedMessage = validatedMessage;
                }
            } else if("PASS_THROUGH".equals(flow.getMappingMode().toString())) {
                logger.info("Pass - through mode - no transformation applied");
                try {
                    logger.info("Processing step - correlationId: {}, flow: {},", correlationId, flow.getName() + ", " +
                        "Pass - through mode",
                        "No transformations applied - direct passthrough",
                        com.integrixs.data.model.SystemLog.LogLevel.INFO);
                } catch(Exception e) {
                    logger.warn("Failed to log pass - through mode: {}", e.getMessage());
                }
                transformedMessage = validatedMessage;
            }

            // Step 3: Execute target adapter
            logger.info("Executing target adapter: {} (ID: {})", outboundAdapter.getName(), outboundAdapter.getId());
            logger.debug("Original message: {}", validatedMessage);
            logger.debug("Transformed message: {}", transformedMessage);
            if(!validatedMessage.equals(transformedMessage)) {
                logger.info("Message was transformed successfully");
            } else {
                logger.warn("Message was not transformed - original and transformed messages are identical");
            }

            logger.info("Executing target adapter - correlationId: {}, flow: {}, adapter: {}, type: {}, mode: {}",
                correlationId, flow.getName(), outboundAdapter.getName(), outboundAdapter.getType(), outboundAdapter.getMode());

            // Note: Source adapter payload logging is handled by IntegrationEndpointService for SOAP/REST flows
            // Only log here if not coming from IntegrationEndpointService(check protocol type)
            if(!"SOAP".equals(protocol) && !"REST".equals(protocol)) {
                // Log source payload for non - SOAP/REST flows(e.g., direct adapter tests)
                logger.debug("Source adapter payload - correlationId: {}, adapter: {}, type: REQUEST", correlationId, inboundAdapter.getName());
            }

            String response = adapterExecutionService.executeAdapter(outboundAdapter, transformedMessage, context);

            // Note: Target adapter response logging is handled by IntegrationEndpointService for SOAP/REST flows
            if(response != null && !"SOAP".equals(protocol) && !"REST".equals(protocol)) {
                // Log target response for non - SOAP/REST flows(e.g., direct adapter tests)
                logger.debug("Target adapter response - correlationId: {}, adapter: {}, type: RESPONSE", correlationId, outboundAdapter.getName());
            }

            logger.info("Target adapter execution completed");

            // Step 4: Process response transformations if needed
            String finalResponse = response;
            if(response != null && "WITH_MAPPING".equals(flow.getMappingMode().toString()) &&
                flow.getTransformations() != null && flow.getTransformations().size() > 1) {

                logger.info("Processing response transformation for flow: {}", flow.getName());

                // Get the response transformation(higher execution order)
                FlowTransformation responseTransformation = flow.getTransformations().stream()
                    .filter(t -> t.getExecutionOrder() > 1)
                    .min((t1, t2) -> Integer.compare(t1.getExecutionOrder(), t2.getExecutionOrder()))
                    .orElse(null);

                if(responseTransformation != null) {
                    try {
                        String transformationId = responseTransformation.getId().toString();
                        logger.info("Using response transformation: {} (ID: {})",
                            responseTransformation.getName(), transformationId);

                        // Get field mappings for response transformation
                        List<FieldMapping> responseMappings = fieldMappingRepository.findByTransformationId(
                            UUID.fromString(transformationId));
                        logger.info("Found {} response field mappings", responseMappings.size());

                        if(!responseMappings.isEmpty()) {
                            // Extract namespaces for response
                            Map<String, String> responseNamespaces = new HashMap<>();

                            // Use target structure namespaces for response source
                            if(flow.getTargetFlowStructureId() != null) {
                                FlowStructure targetFlowStructure = flowStructureRepository.findById(
                                    flow.getTargetFlowStructureId()).orElse(null);
                                if(targetFlowStructure != null && targetFlowStructure.getWsdlContent() != null) {
                                    Map<String, String> targetNamespaces =
                                        WsdlNamespaceExtractor.extractNamespaces(targetFlowStructure.getWsdlContent());
                                    responseNamespaces.putAll(targetNamespaces);
                                }
                            }

                            // Apply response transformation
                            finalResponse = xmlFieldMapper.mapXmlFields(
                                response,
                                null, // Let mapper create structure
                                responseMappings,
                                responseNamespaces
                           );

                            logger.info("Successfully applied response transformation");
                        }
                    } catch(Exception e) {
                        logger.error("Error applying response transformation: {}", e.getMessage(), e);
                        // Continue with original response if transformation fails
                        finalResponse = response;
                    }
                }
            }

            logger.info("Successfully processed message through flow: {}", flow.getName());

            // Update message status to completed
            try {
                logger.info("Flow execution completed - correlationId: {}, response length: {}", correlationId, response != null ? response.length() : 0);
            } catch(Exception e) {
                logger.warn("Failed to update message status to completed: {}", e.getMessage());
            }

            return finalResponse;

        } catch(Exception e) {
            logger.error("Error processing message through flow: {} (ID: {}). Error type: {}, Message: {}",
                        flow.getName(), flow.getId(), e.getClass().getName(), e.getMessage());
            logger.error("Full exception:", e);

            // Check for transaction rollback in the exception chain
            Throwable current = e;
            int depth = 0;
            while(current != null && depth < 10) {
                logger.error("Exception at depth {}: {} - {}", depth,
                    current.getClass().getName(), current.getMessage());
                if(current.getMessage() != null &&
                    (current.getMessage().contains("rollback") ||
                     current.getMessage().contains("Transaction"))) {
                    logger.error("FOUND TRANSACTION ISSUE at depth {}: {}", depth, current.getMessage());
                }
                current = current.getCause();
                depth++;
            }

            // Log the root cause if it's different
            Throwable rootCause = e;
            while(rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            if(rootCause != e) {
                logger.error("Root cause: {} - {}", rootCause.getClass().getName(), rootCause.getMessage());
            }

            // Update message status to failed
            try {
                logger.error("Flow execution failed - correlationId: {}, error: {}", correlationId, e.getMessage());
            } catch(Exception logEx) {
                logger.warn("Failed to update message status to failed: {}", logEx.getMessage());
            }

            throw new RuntimeException("Flow processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates a message against a FlowStructure
     */
    private String validateMessage(String message, FlowStructure flowStructure, Map<String, Object> context)
            throws Exception {
        XmlValidationService.ValidationResult result =
            xmlValidationService.validateMessageAgainstFlowStructure(message, flowStructure, context);

        if(!result.isValid()) {
            throw new RuntimeException("Message validation failed: " + String.join(", ", result.getErrors()));
        }

        return result.getValidatedMessage();
    }

    /**
     * Validates a message against a MessageStructure
     */
    private String validateMessage(String message, MessageStructure messageStructure, Map<String, Object> context)
            throws Exception {
        XmlValidationService.ValidationResult result =
            xmlValidationService.validateMessageAgainstMessageStructure(message, messageStructure, context);

        if(!result.isValid()) {
            throw new RuntimeException("Message validation failed: " + String.join(", ", result.getErrors()));
        }

        return result.getValidatedMessage();
    }
}
