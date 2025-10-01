package com.integrixs.backend.service;

import com.integrixs.backend.application.service.FlowTransformationApplicationService;
import com.integrixs.backend.application.service.FieldMappingApplicationService;
import com.integrixs.data.model.*;
import com.integrixs.data.sql.repository.*;
import com.integrixs.shared.dto.flow.FlowTransformationDTO;
import com.integrixs.shared.dto.FieldMappingDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FlowCompositionService {

    private static final Logger log = LoggerFactory.getLogger(FlowCompositionService.class);


    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    @Autowired
    private BusinessComponentSqlRepository businessComponentRepository;

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Autowired
    private FlowTransformationSqlRepository transformationRepository;

    @Autowired
    private FieldMappingSqlRepository fieldMappingRepository;

    @Autowired
    private FlowTransformationApplicationService transformationService;

    @Autowired
    private FieldMappingApplicationService fieldMappingService;

    @Autowired
    private FieldMappingServiceAdapter fieldMappingServiceAdapter;

    @Autowired
    private FlowOrchestrationStepSqlRepository orchestrationStepRepository;

    @Autowired
    private UserSqlRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a complete direct mapping flow with business components, adapters, and field mappings
     */
    public IntegrationFlow createDirectMappingFlow(DirectMappingFlowRequest request) {
        // Check if flow name already exists
        if(flowRepository.existsByName(request.getFlowName())) {
            throw new IllegalArgumentException("A flow with the name '" + request.getFlowName() + "' already exists");
        }

        // Validate business components exist(only if provided)
        if(request.getSourceBusinessComponentId() != null) {
            validateBusinessComponent(request.getSourceBusinessComponentId());
        }
        if(request.getTargetBusinessComponentId() != null) {
            validateBusinessComponent(request.getTargetBusinessComponentId());
        }

        // Validate adapters exist
        validateAdapter(request.getInboundAdapterId());
        validateAdapter(request.getOutboundAdapterId());

        // Create the integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(request.getFlowName());
        flow.setDescription(request.getDescription());
        // Convert String adapter IDs to UUID
        if(request.getInboundAdapterId() != null) {
            flow.setInboundAdapterId(UUID.fromString(request.getInboundAdapterId()));
        }
        if(request.getOutboundAdapterId() != null) {
            flow.setOutboundAdapterId(UUID.fromString(request.getOutboundAdapterId()));
        }
        // Convert String structure IDs to UUID
        if(request.getSourceFlowStructureId() != null) {
            flow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
        }
        if(request.getTargetFlowStructureId() != null) {
            flow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
        }
        // Deprecated fields - no longer used
        // Source and target structures are now handled through flow structures
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        // Load user from createdBy string
        if(request.getCreatedBy() != null) {
            User createdByUser = userRepository.findById(UUID.fromString(request.getCreatedBy()))
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getCreatedBy()));
            flow.setCreatedBy(createdByUser);
        }

        // Use mapping mode from request if provided, otherwise determine based on field mappings
        if(request.getMappingMode() != null) {
            flow.setMappingMode(MappingMode.valueOf(request.getMappingMode()));
        } else {
            // Fallback to determining based on field mappings
            boolean hasMappings = (request.getFieldMappings() != null && !request.getFieldMappings().isEmpty()) ||
                                 (request.getAdditionalMappings() != null && !request.getAdditionalMappings().isEmpty());
            flow.setMappingMode(hasMappings ? MappingMode.WITH_MAPPING : MappingMode.PASS_THROUGH);
        }

        flow.setSkipXmlConversion(request.isSkipXmlConversion());

        // Set the source business component as the primary business component
        if(request.getSourceBusinessComponentId() != null) {
            BusinessComponent businessComponent = businessComponentRepository.findById(UUID.fromString(request.getSourceBusinessComponentId())).orElse(null);
            if(businessComponent != null) {
                flow.setBusinessComponent(businessComponent);
            }
        }

        // Set flow type
        flow.setFlowType(FlowType.DIRECT_MAPPING);

        // Save the flow
        IntegrationFlow savedFlow = flowRepository.save(flow);

        // Create transformation if field mappings are provided
        if(request.getFieldMappings() != null && !request.getFieldMappings().isEmpty()) {
            FlowTransformationDTO transformation = new FlowTransformationDTO();
            transformation.setFlowId(savedFlow.getId().toString());
            transformation.setType("FIELD_MAPPING");
            transformation.setName(request.getRequestMappingName() != null ? request.getRequestMappingName() : "Request Mapping");
            // Build configuration with mapping type and WSDL operations
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("mappingType", "request");
            if(request.getSourceWsdlOperation() != null) {
                configMap.put("sourceWsdlOperation", request.getSourceWsdlOperation());
            }
            if(request.getTargetWsdlOperation() != null) {
                configMap.put("targetWsdlOperation", request.getTargetWsdlOperation());
            }
            try {
                transformation.setConfiguration(objectMapper.writeValueAsString(configMap));
            } catch(JsonProcessingException e) {
                transformation.setConfiguration(" {\"mappingType\":\"request\"}");
            }
            transformation.setExecutionOrder(1);
            transformation.setActive(true);

            FlowTransformationDTO savedTransformation = transformationService.save(transformation);

            // Save field mappings
            int mappingOrder = 1;
            for(FieldMappingDTO mapping : request.getFieldMappings()) {
                mapping.setTransformationId(savedTransformation.getId().toString());
                mapping.setMappingOrder(mappingOrder++);
                fieldMappingServiceAdapter.createMapping(mapping, request.getCreatedBy());
            }
        }

        // Handle additional mappings for synchronous flows(response, fault mappings)
        if(request.getAdditionalMappings() != null && !request.getAdditionalMappings().isEmpty()) {
            int order = 2;
            for(AdditionalMapping additionalMapping : request.getAdditionalMappings()) {
                if(additionalMapping.getFieldMappings() != null && !additionalMapping.getFieldMappings().isEmpty()) {
                    FlowTransformationDTO transformation = new FlowTransformationDTO();
                    transformation.setFlowId(savedFlow.getId().toString());
                    transformation.setType("FIELD_MAPPING");
                    transformation.setName(additionalMapping.getName()); // Save the user - entered name

                    // Determine message type based on order and flow mode
                    String messageType;
                    if(savedFlow.getMappingMode() == MappingMode.PASS_THROUGH) {
                        // Async mode: second mapping is fault
                        messageType = "fault";
                    } else {
                        // Sync mode: second is response, third is fault
                        messageType = order == 2 ? "response" : "fault";
                    }

                    // Store mapping type and WSDL operations in configuration
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> configMap = new HashMap<>();
                        configMap.put("mappingType", messageType);
                        if(additionalMapping.getSourceWsdlOperation() != null) {
                            configMap.put("sourceWsdlOperation", additionalMapping.getSourceWsdlOperation());
                        }
                        if(additionalMapping.getTargetWsdlOperation() != null) {
                            configMap.put("targetWsdlOperation", additionalMapping.getTargetWsdlOperation());
                        }
                        String config = mapper.writeValueAsString(configMap);
                        transformation.setConfiguration(config);
                    } catch(Exception e) {
                        transformation.setConfiguration(" {\"mappingType\":\"" + messageType + "\"}");
                    }

                    transformation.setExecutionOrder(order++);
                    transformation.setActive(true);

                    FlowTransformationDTO savedTransformation = transformationService.save(transformation);

                    // Save field mappings for this additional mapping
                    int fieldMappingOrder = 1;
                    for(FieldMappingDTO mapping : additionalMapping.getFieldMappings()) {
                        mapping.setTransformationId(savedTransformation.getId().toString());
                        mapping.setMappingOrder(fieldMappingOrder++);
                        fieldMappingServiceAdapter.createMapping(mapping, request.getCreatedBy());
                    }
                }
            }
        }

        return savedFlow;
    }

    /**
     * Update an existing direct mapping flow
     */
    public IntegrationFlow updateDirectMappingFlow(String flowId, DirectMappingFlowRequest request) {
        // Find existing flow
        IntegrationFlow existingFlow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

        // Validate that we're not creating a duplicate name
        if(flowRepository.existsByNameAndIdNot(request.getFlowName(), UUID.fromString(flowId))) {
            throw new IllegalArgumentException("A flow with the name '" + request.getFlowName() + "' already exists");
        }

        // Update basic flow properties
        existingFlow.setName(request.getFlowName());
        existingFlow.setDescription(request.getDescription());

        // Update business component(IntegrationFlow has a single businessComponent field)
        if(request.getSourceBusinessComponentId() != null) {
            BusinessComponent businessComponent = businessComponentRepository.findById(UUID.fromString(request.getSourceBusinessComponentId())).orElse(null);
            if(businessComponent != null) {
                existingFlow.setBusinessComponent(businessComponent);
            }
        }

        // Update adapters
        if(request.getInboundAdapterId() != null) {
            validateAdapter(request.getInboundAdapterId());
            existingFlow.setInboundAdapterId(UUID.fromString(request.getInboundAdapterId()));
        }
        if(request.getOutboundAdapterId() != null) {
            validateAdapter(request.getOutboundAdapterId());
            existingFlow.setOutboundAdapterId(UUID.fromString(request.getOutboundAdapterId()));
        }

        // Update flow structures
        if(request.getSourceFlowStructureId() != null) {
            existingFlow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
        }
        if(request.getTargetFlowStructureId() != null) {
            existingFlow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
        }

        // Note: IntegrationFlow entity doesn't have sourceStructureId/targetStructureId fields
        // Those would need to be added to the entity if required

        // Update mapping mode
        if(request.getMappingMode() != null) {
            existingFlow.setMappingMode(MappingMode.valueOf(request.getMappingMode()));
        }

        existingFlow.setSkipXmlConversion(request.isSkipXmlConversion());

        // Save the updated flow
        IntegrationFlow savedFlow = flowRepository.save(existingFlow);

        // Delete existing transformations and mappings
        List<FlowTransformationDTO> existingTransformations = transformationService.getByFlowId(flowId);
        for(FlowTransformationDTO transformation : existingTransformations) {
            // Delete field mappings for this transformation
            List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(UUID.fromString(transformation.getId()));
            for(FieldMapping mapping : mappings) {
                fieldMappingRepository.deleteById(mapping.getId());
            }
            // Delete the transformation
            transformationService.delete(transformation.getId());
        }

        // Recreate transformations and mappings if mapping is required
        if(existingFlow.getMappingMode() == MappingMode.WITH_MAPPING && request.getFieldMappings() != null) {
            // Create request transformation
            FlowTransformationDTO requestTransformation = new FlowTransformationDTO();
            requestTransformation.setFlowId(savedFlow.getId().toString());
            requestTransformation.setName(request.getRequestMappingName() != null ? request.getRequestMappingName() : "Request Mapping");
            requestTransformation.setType("FIELD_MAPPING");
            requestTransformation.setExecutionOrder(1);
            requestTransformation.setActive(true);

            // Set WSDL operations
            if(request.getSourceWsdlOperation() != null) {
                Map<String, Object> config = new HashMap<>();
                config.put("sourceWsdlOperation", request.getSourceWsdlOperation());
                config.put("targetWsdlOperation", request.getTargetWsdlOperation());
                try {
                    requestTransformation.setConfiguration(objectMapper.writeValueAsString(config));
                } catch(JsonProcessingException e) {
                    log.error("Error serializing configuration", e);
                }
            }

            FlowTransformationDTO savedRequestTransformation = transformationService.save(requestTransformation);

            // Save field mappings for request
            int mappingOrder = 1;
            for(FieldMappingDTO mapping : request.getFieldMappings()) {
                mapping.setTransformationId(savedRequestTransformation.getId().toString());
                mapping.setMappingOrder(mappingOrder++);
                fieldMappingServiceAdapter.createMapping(mapping, request.getCreatedBy());
            }

            // Handle additional mappings(e.g., response mappings for synchronous flows)
            if(request.getAdditionalMappings() != null) {
                int transformationOrder = 2;
                for(AdditionalMapping additionalMapping : request.getAdditionalMappings()) {
                    FlowTransformationDTO transformation = new FlowTransformationDTO();
                    transformation.setFlowId(savedFlow.getId().toString());
                    transformation.setName(additionalMapping.getName());
                    transformation.setType("FIELD_MAPPING");
                    transformation.setExecutionOrder(transformationOrder++);
                    transformation.setActive(true);

                    // Set WSDL operations for additional mapping
                    if(additionalMapping.getSourceWsdlOperation() != null) {
                        Map<String, Object> config = new HashMap<>();
                        config.put("sourceWsdlOperation", additionalMapping.getSourceWsdlOperation());
                        config.put("targetWsdlOperation", additionalMapping.getTargetWsdlOperation());
                        try {
                            transformation.setConfiguration(objectMapper.writeValueAsString(config));
                        } catch(JsonProcessingException e) {
                            log.error("Error serializing configuration", e);
                        }
                    }

                    FlowTransformationDTO savedTransformation = transformationService.save(transformation);

                    // Save field mappings for this additional mapping
                    int fieldMappingOrder = 1;
                    for(FieldMappingDTO mapping : additionalMapping.getFieldMappings()) {
                        mapping.setTransformationId(savedTransformation.getId().toString());
                        mapping.setMappingOrder(fieldMappingOrder++);
                        fieldMappingServiceAdapter.createMapping(mapping, request.getCreatedBy());
                    }
                }
            }
        }

        return savedFlow;
    }

    /**
     * Create a complete orchestrated flow with multiple steps and routing
     */
    public IntegrationFlow createOrchestrationFlow(OrchestrationFlowRequest request) {
        // Validate business components
        for(String componentId : request.getBusinessComponentIds()) {
            validateBusinessComponent(componentId);
        }

        // Validate adapters
        for(String adapterId : request.getAdapterIds()) {
            validateAdapter(adapterId);
        }

        // Create the integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(request.getFlowName());
        flow.setDescription(request.getDescription());
        // Convert String adapter IDs to UUID
        if(request.getInboundAdapterId() != null) {
            flow.setInboundAdapterId(UUID.fromString(request.getInboundAdapterId()));
        }
        if(request.getOutboundAdapterId() != null) {
            flow.setOutboundAdapterId(UUID.fromString(request.getOutboundAdapterId()));
        }
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        // Load user from createdBy string
        if(request.getCreatedBy() != null) {
            User createdByUser = userRepository.findById(UUID.fromString(request.getCreatedBy()))
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getCreatedBy()));
            flow.setCreatedBy(createdByUser);
        }

        // Set flow type to orchestration
        flow.setFlowType(FlowType.ORCHESTRATION);

        // Save the flow
        IntegrationFlow savedFlow = flowRepository.save(flow);

        // Store orchestration steps in the proper table structure
        if(request.getOrchestrationSteps() != null) {
            int order = 1;
            for(OrchestrationStep step : request.getOrchestrationSteps()) {
                // Create orchestration step entity
                FlowOrchestrationStep orchestrationStep = FlowOrchestrationStep.builder()
                    .flow(savedFlow)
                    .stepType(step.getType())
                    .stepName(step.getType() + "_" + order)
                    .executionOrder(order++)
                    .configuration(step.getConfiguration() instanceof Map ?
                        (Map<String, Object>) step.getConfiguration() :
                        Map.of("data", step.getConfiguration()))
                    .isActive(true)
                    .build();

                // Extract specific fields based on configuration
                if(step.getConfiguration() instanceof Map) {
                    Map<String, Object> config = (Map<String, Object>) step.getConfiguration();

                    // Extract common fields
                    orchestrationStep.setStepName((String) config.getOrDefault("name", orchestrationStep.getStepName()));
                    orchestrationStep.setDescription((String) config.get("description"));
                    orchestrationStep.setConditionExpression((String) config.get("condition"));
                    orchestrationStep.setIsConditional(config.containsKey("condition"));

                    // Extract routing specific fields
                    if("ROUTE".equalsIgnoreCase(step.getType())) {
                        String targetAdapterId = (String) config.get("targetAdapterId");
                        if(targetAdapterId != null) {
                            orchestrationStep.setTargetAdapterId(UUID.fromString(targetAdapterId));
                        }
                        String targetFlowStructureId = (String) config.get("targetFlowStructureId");
                        if(targetFlowStructureId != null) {
                            orchestrationStep.setTargetFlowStructureId(UUID.fromString(targetFlowStructureId));
                        }
                    }

                    // Extract transformation specific fields
                    if("TRANSFORM".equalsIgnoreCase(step.getType())) {
                        String transformationId = (String) config.get("transformationId");
                        if(transformationId != null) {
                            orchestrationStep.setTransformationId(UUID.fromString(transformationId));
                        }
                    }

                    // Extract timeout and retry settings
                    if(config.containsKey("timeout")) {
                        orchestrationStep.setTimeoutSeconds(((Number) config.get("timeout")).intValue());
                    }
                    if(config.containsKey("retryAttempts")) {
                        orchestrationStep.setRetryAttempts(((Number) config.get("retryAttempts")).intValue());
                    }
                    if(config.containsKey("retryDelay")) {
                        orchestrationStep.setRetryDelaySeconds(((Number) config.get("retryDelay")).intValue());
                    }
                }

                orchestrationStepRepository.save(orchestrationStep);

                // Also create transformations for backward compatibility if needed
                FlowTransformationDTO transformation = new FlowTransformationDTO();
                transformation.setFlowId(savedFlow.getId().toString());
                transformation.setType(step.getType());
                try {
                    transformation.setConfiguration(objectMapper.writeValueAsString(step.getConfiguration()));
                } catch(JsonProcessingException e) {
                    log.error("Error serializing orchestration step configuration", e);
                    transformation.setConfiguration(" {}");
                }
                transformation.setExecutionOrder(orchestrationStep.getExecutionOrder());
                transformation.setActive(true);

                transformationService.save(transformation);
            }
        }

        return savedFlow;
    }

    /**
     * Update an existing flow with new configuration
     */
    public Optional<IntegrationFlow> updateFlowComposition(String flowId, UpdateFlowRequest request) {
        return flowRepository.findById(UUID.fromString(flowId)).map(flow -> {
            flow.setName(request.getFlowName());
            flow.setDescription(request.getDescription());

            if(request.getInboundAdapterId() != null) {
                validateAdapter(request.getInboundAdapterId());
                flow.setInboundAdapterId(UUID.fromString(request.getInboundAdapterId()));
            }

            if(request.getOutboundAdapterId() != null) {
                validateAdapter(request.getOutboundAdapterId());
                flow.setOutboundAdapterId(UUID.fromString(request.getOutboundAdapterId()));
            }

            // Convert String structure IDs to UUID
            if(request.getSourceFlowStructureId() != null) {
                flow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
            }
            if(request.getTargetFlowStructureId() != null) {
                flow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
            }
            // Deprecated fields - no longer used
            // Source and target structures are now handled through flow structures

            return flowRepository.save(flow);
        });
    }

    /**
     * Get complete flow composition including all related components
     */
    public Optional<CompleteFlowComposition> getCompleteFlowComposition(String flowId) {
        return flowRepository.findById(UUID.fromString(flowId)).map(flow -> {
            CompleteFlowComposition composition = new CompleteFlowComposition();
            composition.setFlow(flow);

            // Business components are now stored directly in the flow
            // No need to parse JSON configuration anymore

            // Get adapters
            composition.setInboundAdapter(adapterRepository.findById(flow.getInboundAdapterId()).orElse(null));
            composition.setOutboundAdapter(adapterRepository.findById(flow.getOutboundAdapterId()).orElse(null));

            // Get transformations
            composition.setTransformations(transformationService.getByFlowId(flowId));

            return composition;
        });
    }

    /**
     * Delete a complete flow and all its related components
     */
    public boolean deleteFlowComposition(String flowId) {
        return flowRepository.findById(UUID.fromString(flowId)).map(flow -> {
            // Delete field mappings first(cascade should handle this, but being explicit)
            List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
            for(FlowTransformation transformation : transformations) {
                fieldMappingRepository.deleteByTransformationId(transformation.getId());
            }

            // Delete transformations
            transformationRepository.deleteByFlowId(flow.getId());

            // Delete the flow
            flowRepository.deleteById(flow.getId());

            return true;
        }).orElse(false);
    }

    private void validateBusinessComponent(String componentId) {
        if(componentId != null && !businessComponentRepository.existsById(UUID.fromString(componentId))) {
            throw new IllegalArgumentException("Business component not found: " + componentId);
        }
    }

    private void validateAdapter(String adapterId) {
        if(adapterId != null && !adapterRepository.existsById(UUID.fromString(adapterId))) {
            throw new IllegalArgumentException("Communication adapter not found: " + adapterId);
        }
    }

    // DTOs for request/response
    public static class DirectMappingFlowRequest {
        private String flowName;
        private String description;
        private String sourceBusinessComponentId;
        private String targetBusinessComponentId;
        private String inboundAdapterId;
        private String outboundAdapterId;
        private String sourceFlowStructureId;
        private String targetFlowStructureId;
        private String createdBy;
        private String requestMappingName;
        private List<FieldMappingDTO> fieldMappings;
        private List<AdditionalMapping> additionalMappings;
        private boolean skipXmlConversion;
        private String mappingMode; // Add mappingMode field
        private String sourceWsdlOperation; // Selected WSDL operation for source
        private String targetWsdlOperation; // Selected WSDL operation for target

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSourceBusinessComponentId() { return sourceBusinessComponentId; }
        public void setSourceBusinessComponentId(String sourceBusinessComponentId) { this.sourceBusinessComponentId = sourceBusinessComponentId; }
        public String getTargetBusinessComponentId() { return targetBusinessComponentId; }
        public void setTargetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; }
        public String getInboundAdapterId() { return inboundAdapterId; }
        public void setInboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; }
        public String getOutboundAdapterId() { return outboundAdapterId; }
        public void setOutboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; }
        public String getSourceFlowStructureId() { return sourceFlowStructureId; }
        public void setSourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; }
        public String getTargetFlowStructureId() { return targetFlowStructureId; }
        public void setTargetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getRequestMappingName() { return requestMappingName; }
        public void setRequestMappingName(String requestMappingName) { this.requestMappingName = requestMappingName; }
        public List<FieldMappingDTO> getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(List<FieldMappingDTO> fieldMappings) { this.fieldMappings = fieldMappings; }
        public List<AdditionalMapping> getAdditionalMappings() { return additionalMappings; }
        public void setAdditionalMappings(List<AdditionalMapping> additionalMappings) { this.additionalMappings = additionalMappings; }
        public boolean isSkipXmlConversion() { return skipXmlConversion; }
        public void setSkipXmlConversion(boolean skipXmlConversion) { this.skipXmlConversion = skipXmlConversion; }
        public String getMappingMode() { return mappingMode; }
        public void setMappingMode(String mappingMode) { this.mappingMode = mappingMode; }
        public String getSourceWsdlOperation() { return sourceWsdlOperation; }
        public void setSourceWsdlOperation(String sourceWsdlOperation) { this.sourceWsdlOperation = sourceWsdlOperation; }
        public String getTargetWsdlOperation() { return targetWsdlOperation; }
        public void setTargetWsdlOperation(String targetWsdlOperation) { this.targetWsdlOperation = targetWsdlOperation; }
    }

    public static class OrchestrationFlowRequest {
        private String flowName;
        private String description;
        private String inboundAdapterId;
        private String outboundAdapterId;
        private String createdBy;
        private List<String> businessComponentIds;
        private List<String> adapterIds;
        private List<OrchestrationStep> orchestrationSteps;

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getInboundAdapterId() { return inboundAdapterId; }
        public void setInboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; }
        public String getOutboundAdapterId() { return outboundAdapterId; }
        public void setOutboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public List<String> getBusinessComponentIds() { return businessComponentIds; }
        public void setBusinessComponentIds(List<String> businessComponentIds) { this.businessComponentIds = businessComponentIds; }
        public List<String> getAdapterIds() { return adapterIds; }
        public void setAdapterIds(List<String> adapterIds) { this.adapterIds = adapterIds; }
        public List<OrchestrationStep> getOrchestrationSteps() { return orchestrationSteps; }
        public void setOrchestrationSteps(List<OrchestrationStep> orchestrationSteps) { this.orchestrationSteps = orchestrationSteps; }
    }

    public static class UpdateFlowRequest {
        private String flowName;
        private String description;
        private String inboundAdapterId;
        private String outboundAdapterId;
        private String sourceFlowStructureId;
        private String targetFlowStructureId;

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getInboundAdapterId() { return inboundAdapterId; }
        public void setInboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; }
        public String getOutboundAdapterId() { return outboundAdapterId; }
        public void setOutboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; }
        public String getSourceFlowStructureId() { return sourceFlowStructureId; }
        public void setSourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; }
        public String getTargetFlowStructureId() { return targetFlowStructureId; }
        public void setTargetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; }
    }

    public static class CompleteFlowComposition {
        private IntegrationFlow flow;
        private BusinessComponent sourceBusinessComponent;
        private BusinessComponent targetBusinessComponent;
        private CommunicationAdapter inboundAdapter;
        private CommunicationAdapter outboundAdapter;
        private List<FlowTransformationDTO> transformations;

        // Getters and setters
        public IntegrationFlow getFlow() { return flow; }
        public void setFlow(IntegrationFlow flow) { this.flow = flow; }
        public BusinessComponent getSourceBusinessComponent() { return sourceBusinessComponent; }
        public void setSourceBusinessComponent(BusinessComponent sourceBusinessComponent) { this.sourceBusinessComponent = sourceBusinessComponent; }
        public BusinessComponent getTargetBusinessComponent() { return targetBusinessComponent; }
        public void setTargetBusinessComponent(BusinessComponent targetBusinessComponent) { this.targetBusinessComponent = targetBusinessComponent; }
        public CommunicationAdapter getInboundAdapter() { return inboundAdapter; }
        public void setInboundAdapter(CommunicationAdapter inboundAdapter) { this.inboundAdapter = inboundAdapter; }
        public CommunicationAdapter getOutboundAdapter() { return outboundAdapter; }
        public void setOutboundAdapter(CommunicationAdapter outboundAdapter) { this.outboundAdapter = outboundAdapter; }
        public List<FlowTransformationDTO> getTransformations() { return transformations; }
        public void setTransformations(List<FlowTransformationDTO> transformations) { this.transformations = transformations; }
    }

    // Configuration classes
    public static class FlowConfiguration {
        private String sourceBusinessComponentId;
        private String targetBusinessComponentId;
        private String flowType;

        public String getSourceBusinessComponentId() { return sourceBusinessComponentId; }
        public void setSourceBusinessComponentId(String sourceBusinessComponentId) { this.sourceBusinessComponentId = sourceBusinessComponentId; }
        public String getTargetBusinessComponentId() { return targetBusinessComponentId; }
        public void setTargetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; }
        public String getFlowType() { return flowType; }
        public void setFlowType(String flowType) { this.flowType = flowType; }
    }

    public static class OrchestrationConfiguration {
        private List<String> businessComponentIds;
        private List<String> adapterIds;
        private List<OrchestrationStep> orchestrationSteps;
        private String flowType;

        public List<String> getBusinessComponentIds() { return businessComponentIds; }
        public void setBusinessComponentIds(List<String> businessComponentIds) { this.businessComponentIds = businessComponentIds; }
        public List<String> getAdapterIds() { return adapterIds; }
        public void setAdapterIds(List<String> adapterIds) { this.adapterIds = adapterIds; }
        public List<OrchestrationStep> getOrchestrationSteps() { return orchestrationSteps; }
        public void setOrchestrationSteps(List<OrchestrationStep> orchestrationSteps) { this.orchestrationSteps = orchestrationSteps; }
        public String getFlowType() { return flowType; }
        public void setFlowType(String flowType) { this.flowType = flowType; }
    }

    public static class OrchestrationStep {
        private String type;
        private Object configuration;
        private int order;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getConfiguration() { return configuration; }
        public void setConfiguration(Object configuration) { this.configuration = configuration; }
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
    }

    public static class AdditionalMapping {
        private String name;
        private List<FieldMappingDTO> fieldMappings;
        private String sourceWsdlOperation; // Selected WSDL operation for source
        private String targetWsdlOperation; // Selected WSDL operation for target

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<FieldMappingDTO> getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(List<FieldMappingDTO> fieldMappings) { this.fieldMappings = fieldMappings; }
        public String getSourceWsdlOperation() { return sourceWsdlOperation; }
        public void setSourceWsdlOperation(String sourceWsdlOperation) { this.sourceWsdlOperation = sourceWsdlOperation; }
        public String getTargetWsdlOperation() { return targetWsdlOperation; }
        public void setTargetWsdlOperation(String targetWsdlOperation) { this.targetWsdlOperation = targetWsdlOperation; }
    }
}
