package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.request.CreateFieldMappingRequest;
import com.integrixs.backend.api.dto.request.UpdateFieldMappingRequest;
import com.integrixs.backend.api.dto.response.FieldMappingResponse;
import com.integrixs.data.sql.repository.FieldMappingSqlRepository;
import com.integrixs.data.sql.repository.FlowTransformationSqlRepository;
import com.integrixs.backend.domain.service.MappingEngineService;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for field mapping management
 */
@Service
public class FieldMappingApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FieldMappingApplicationService.class);


    private final FieldMappingSqlRepository fieldMappingRepository;
    private final FlowTransformationSqlRepository transformationRepository;
    private final MappingEngineService mappingEngineService;
    private final AuditTrailService auditTrailService;

    public FieldMappingApplicationService(FieldMappingSqlRepository fieldMappingRepository,
                                        FlowTransformationSqlRepository transformationRepository,
                                        MappingEngineService mappingEngineService,
                                        AuditTrailService auditTrailService) {
        this.fieldMappingRepository = fieldMappingRepository;
        this.transformationRepository = transformationRepository;
        this.mappingEngineService = mappingEngineService;
        this.auditTrailService = auditTrailService;
    }

    /**
     * Get all field mappings for a transformation
     */
    public List<FieldMappingResponse> getMappingsByTransformationId(String transformationId) {
        log.debug("Getting field mappings for transformation: {}", transformationId);

        UUID transformationUuid = UUID.fromString(transformationId);

        // Verify transformation exists
        if(!transformationRepository.existsById(transformationUuid)) {
            throw new ResourceNotFoundException("Transformation not found: " + transformationId);
        }

        List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformationUuid);
        List<FieldMapping> sortedMappings = mappingEngineService.sortMappingsByOrder(mappings);

        return sortedMappings.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a single field mapping by ID
     */
    public FieldMappingResponse getMappingById(String id) {
        log.debug("Getting field mapping by ID: {}", id);

        FieldMapping mapping = fieldMappingRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Field mapping not found: " + id));

        return toResponse(mapping);
    }

    /**
     * Create a new field mapping
     */
    public FieldMappingResponse createMapping(String transformationId, CreateFieldMappingRequest request, User performedBy) {
        log.info("Creating field mapping for transformation: {} by user: {}", transformationId, performedBy.getUsername());

        // Get transformation
        FlowTransformation transformation = transformationRepository.findById(UUID.fromString(transformationId))
            .orElseThrow(() -> new ResourceNotFoundException("Transformation not found: " + transformationId));

        // Create mapping entity
        FieldMapping mapping = new FieldMapping();
        updateMappingFromRequest(mapping, request);

        // Process through domain service
        mapping = mappingEngineService.createMapping(transformation, mapping);

        // Save
        mapping = fieldMappingRepository.save(mapping);

        // Audit
        auditTrailService.logUserAction(
            performedBy,
            "FieldMapping",
            mapping.getId().toString(),
            "CREATE"
       );

        log.info("Created field mapping {} for transformation {}", mapping.getId(), transformationId);

        return toResponse(mapping);
    }

    /**
     * Update an existing field mapping
     */
    public FieldMappingResponse updateMapping(String id, String transformationId, UpdateFieldMappingRequest request, User performedBy) {
        log.info("Updating field mapping: {} by user: {}", id, performedBy.getUsername());

        // Get existing mapping
        FieldMapping existingMapping = fieldMappingRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Field mapping not found: " + id));

        // Verify it belongs to the specified transformation
        if(!existingMapping.getTransformation().getId().toString().equals(transformationId)) {
            throw new IllegalArgumentException("Field mapping does not belong to transformation: " + transformationId);
        }

        // Create update entity
        FieldMapping updates = new FieldMapping();
        updateMappingFromRequest(updates, request);

        // Process through domain service
        existingMapping = mappingEngineService.updateMapping(existingMapping, updates);

        // Save
        existingMapping = fieldMappingRepository.save(existingMapping);

        // Audit
        auditTrailService.logUserAction(
            performedBy,
            "FieldMapping",
            id,
            "UPDATE"
       );

        log.info("Updated field mapping {}", id);

        return toResponse(existingMapping);
    }

    /**
     * Delete a field mapping
     */
    public void deleteMapping(String id, String transformationId, User performedBy) {
        log.info("Deleting field mapping: {} by user: {}", id, performedBy.getUsername());

        // Verify mapping exists and belongs to transformation
        FieldMapping mapping = fieldMappingRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Field mapping not found: " + id));

        if(!mapping.getTransformation().getId().toString().equals(transformationId)) {
            throw new IllegalArgumentException("Field mapping does not belong to transformation: " + transformationId);
        }

        fieldMappingRepository.deleteById(UUID.fromString(id));

        // Audit
        auditTrailService.logUserAction(
            performedBy,
            "FieldMapping",
            id,
            "DELETE"
       );

        log.info("Deleted field mapping {}", id);
    }

    /**
     * Count mappings for a transformation
     */
    public long countMappingsForTransformation(String transformationId) {
        return fieldMappingRepository.countByTransformationId(UUID.fromString(transformationId));
    }

    /**
     * Convert request to entity fields
     */
    private void updateMappingFromRequest(FieldMapping mapping, CreateFieldMappingRequest request) {
        mapping.setSourceFieldsList(request.getSourceFields());

        // Handle target fields - support both single and multiple
        if(request.getTargetFields() != null && !request.getTargetFields().isEmpty()) {
            mapping.setTargetFieldsList(request.getTargetFields());
        } else if(request.getTargetField() != null) {
            mapping.setTargetField(request.getTargetField());
        }

        // Set mapping type
        if(request.getMappingType() != null) {
            try {
                mapping.setMappingType(FieldMapping.MappingType.valueOf(request.getMappingType()));
            } catch(IllegalArgumentException e) {
                mapping.setMappingType(FieldMapping.MappingType.DIRECT);
            }
        }

        // Set split configuration if present
        if(request.getSplitConfiguration() != null) {
            String splitConfigJson = mappingEngineService.serializeSplitConfiguration(request.getSplitConfiguration());
            mapping.setSplitConfiguration(splitConfigJson);
        }

        mapping.setJavaFunction(request.getJavaFunction());
        mapping.setMappingRule(request.getMappingRule());
        mapping.setInputTypes(request.getInputTypes());
        mapping.setOutputType(request.getOutputType());
        mapping.setDescription(request.getDescription());
        mapping.setFunctionName(request.getFunctionName());
        mapping.setActive(request.isActive());
        mapping.setArrayMapping(request.isArrayMapping());
        mapping.setArrayContextPath(request.getArrayContextPath());
        mapping.setSourceXPath(request.getSourceXPath());
        mapping.setTargetXPath(request.getTargetXPath());
        mapping.setMappingOrder(request.getMappingOrder());

        // Serialize visual flow data and function node
        String visualFlowDataJson = mappingEngineService.serializeVisualFlowData(request.getVisualFlowData());
        mapping.setVisualFlowData(visualFlowDataJson);

        String functionNodeJson = mappingEngineService.serializeFunctionNode(request.getFunctionNode());
        mapping.setFunctionNode(functionNodeJson);
    }

    /**
     * Convert entity to response
     */
    private FieldMappingResponse toResponse(FieldMapping mapping) {
        return FieldMappingResponse.builder()
            .id(mapping.getId().toString())
            .transformationId(mapping.getTransformation() != null ? mapping.getTransformation().getId().toString() : null)
            .sourceFields(mapping.getSourceFieldsList())
            .targetField(mapping.getTargetField())
            .targetFields(mapping.getTargetFieldsList())
            .mappingType(mapping.getMappingType() != null ? mapping.getMappingType().toString() : "DIRECT")
            .splitConfiguration(mapping.getSplitConfiguration() != null ?
                mappingEngineService.deserializeSplitConfiguration(mapping.getSplitConfiguration()) : null)
            .javaFunction(mapping.getJavaFunction())
            .mappingRule(mapping.getMappingRule())
            .inputTypes(mapping.getInputTypes())
            .outputType(mapping.getOutputType())
            .description(mapping.getDescription())
            .version(mapping.getVersion())
            .functionName(mapping.getFunctionName())
            .active(mapping.isActive())
            .arrayMapping(mapping.isArrayMapping())
            .arrayContextPath(mapping.getArrayContextPath())
            .sourceXPath(mapping.getSourceXPath())
            .targetXPath(mapping.getTargetXPath())
            .mappingOrder(mapping.getMappingOrder())
            .visualFlowData(mappingEngineService.deserializeVisualFlowData(mapping.getVisualFlowData()))
            .functionNode(mappingEngineService.deserializeFunctionNode(mapping.getFunctionNode()))
            .createdAt(mapping.getCreatedAt())
            .updatedAt(mapping.getUpdatedAt())
            .build();
    }
}
