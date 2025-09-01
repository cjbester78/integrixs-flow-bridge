package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.request.CreateFieldMappingRequest;
import com.integrixs.backend.api.dto.request.UpdateFieldMappingRequest;
import com.integrixs.backend.api.dto.response.FieldMappingResponse;
import com.integrixs.backend.domain.repository.FieldMappingRepository;
import com.integrixs.backend.domain.repository.FlowTransformationRepository;
import com.integrixs.backend.domain.service.MappingEngineService;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for field mapping management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldMappingApplicationService {
    
    private final FieldMappingRepository fieldMappingRepository;
    private final FlowTransformationRepository transformationRepository;
    private final MappingEngineService mappingEngineService;
    private final AuditTrailService auditTrailService;
    
    /**
     * Get all field mappings for a transformation
     */
    @Transactional(readOnly = true)
    public List<FieldMappingResponse> getMappingsByTransformationId(String transformationId) {
        log.debug("Getting field mappings for transformation: {}", transformationId);
        
        UUID transformationUuid = UUID.fromString(transformationId);
        
        // Verify transformation exists
        if (!transformationRepository.existsById(transformationUuid)) {
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
    @Transactional(readOnly = true)
    public FieldMappingResponse getMappingById(String id) {
        log.debug("Getting field mapping by ID: {}", id);
        
        FieldMapping mapping = fieldMappingRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Field mapping not found: " + id));
        
        return toResponse(mapping);
    }
    
    /**
     * Create a new field mapping
     */
    @Transactional
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
    @Transactional
    public FieldMappingResponse updateMapping(String id, String transformationId, UpdateFieldMappingRequest request, User performedBy) {
        log.info("Updating field mapping: {} by user: {}", id, performedBy.getUsername());
        
        // Get existing mapping
        FieldMapping existingMapping = fieldMappingRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Field mapping not found: " + id));
        
        // Verify it belongs to the specified transformation
        if (!existingMapping.getTransformation().getId().toString().equals(transformationId)) {
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
    @Transactional
    public void deleteMapping(String id, String transformationId, User performedBy) {
        log.info("Deleting field mapping: {} by user: {}", id, performedBy.getUsername());
        
        // Verify mapping exists and belongs to transformation
        FieldMapping mapping = fieldMappingRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Field mapping not found: " + id));
        
        if (!mapping.getTransformation().getId().toString().equals(transformationId)) {
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
    @Transactional(readOnly = true)
    public long countMappingsForTransformation(String transformationId) {
        return fieldMappingRepository.countByTransformationId(UUID.fromString(transformationId));
    }
    
    /**
     * Convert request to entity fields
     */
    private void updateMappingFromRequest(FieldMapping mapping, CreateFieldMappingRequest request) {
        mapping.setSourceFieldsList(request.getSourceFields());
        mapping.setTargetField(request.getTargetField());
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