package com.integrixs.backend.domain.service;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowTransformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Domain service for field mapping engine operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MappingEngineService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Validate field mapping configuration
     */
    public void validateMapping(FieldMapping mapping) {
        if (mapping.getSourceFieldsList() == null || mapping.getSourceFieldsList().isEmpty()) {
            throw new IllegalArgumentException("Source fields are required");
        }
        
        if (mapping.getTargetField() == null || mapping.getTargetField().isBlank()) {
            throw new IllegalArgumentException("Target field is required");
        }
        
        if (mapping.isArrayMapping() && (mapping.getArrayContextPath() == null || mapping.getArrayContextPath().isBlank())) {
            throw new IllegalArgumentException("Array context path is required for array mappings");
        }
        
        // Validate function configuration if present
        if (mapping.getJavaFunction() != null && !mapping.getJavaFunction().isBlank()) {
            validateJavaFunction(mapping.getJavaFunction());
        }
    }
    
    /**
     * Sort mappings by order
     */
    public List<FieldMapping> sortMappingsByOrder(List<FieldMapping> mappings) {
        return mappings.stream()
            .sorted((a, b) -> {
                int orderA = a.getMappingOrder() != null ? a.getMappingOrder() : Integer.MAX_VALUE;
                int orderB = b.getMappingOrder() != null ? b.getMappingOrder() : Integer.MAX_VALUE;
                
                int orderCompare = Integer.compare(orderA, orderB);
                if (orderCompare != 0) {
                    return orderCompare;
                }
                
                // Fallback to ID comparison
                return a.getId().compareTo(b.getId());
            })
            .toList();
    }
    
    /**
     * Convert visual flow data object to JSON string
     */
    public String serializeVisualFlowData(Object visualFlowData) {
        if (visualFlowData == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(visualFlowData);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize visual flow data", e);
            throw new RuntimeException("Failed to serialize visual flow data", e);
        }
    }
    
    /**
     * Convert JSON string to visual flow data object
     */
    public Object deserializeVisualFlowData(String visualFlowDataJson) {
        if (visualFlowDataJson == null || visualFlowDataJson.isBlank()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(visualFlowDataJson, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize visual flow data", e);
            return null;
        }
    }
    
    /**
     * Convert function node object to JSON string
     */
    public String serializeFunctionNode(Object functionNode) {
        if (functionNode == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(functionNode);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize function node", e);
            throw new RuntimeException("Failed to serialize function node", e);
        }
    }
    
    /**
     * Convert JSON string to function node object
     */
    public Object deserializeFunctionNode(String functionNodeJson) {
        if (functionNodeJson == null || functionNodeJson.isBlank()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(functionNodeJson, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize function node", e);
            return null;
        }
    }
    
    /**
     * Create a new field mapping
     */
    public FieldMapping createMapping(FlowTransformation transformation, FieldMapping mapping) {
        mapping.setTransformation(transformation);
        mapping.setVersion("1");
        
        // Set default order if not specified
        if (mapping.getMappingOrder() == null) {
            mapping.setMappingOrder(0);
        }
        
        validateMapping(mapping);
        
        log.info("Creating field mapping for transformation {} with target field: {}", 
            transformation.getId(), mapping.getTargetField());
        
        return mapping;
    }
    
    /**
     * Update an existing field mapping
     */
    public FieldMapping updateMapping(FieldMapping existing, FieldMapping updates) {
        // Update fields
        existing.setSourceFieldsList(updates.getSourceFieldsList());
        existing.setTargetField(updates.getTargetField());
        existing.setJavaFunction(updates.getJavaFunction());
        existing.setMappingRule(updates.getMappingRule());
        existing.setInputTypes(updates.getInputTypes());
        existing.setOutputType(updates.getOutputType());
        existing.setDescription(updates.getDescription());
        existing.setFunctionName(updates.getFunctionName());
        existing.setActive(updates.isActive());
        existing.setArrayMapping(updates.isArrayMapping());
        existing.setArrayContextPath(updates.getArrayContextPath());
        existing.setSourceXPath(updates.getSourceXPath());
        existing.setTargetXPath(updates.getTargetXPath());
        existing.setMappingOrder(updates.getMappingOrder());
        existing.setVisualFlowData(updates.getVisualFlowData());
        existing.setFunctionNode(updates.getFunctionNode());
        
        // Increment version
        int currentVersion = existing.getVersion() != null ? Integer.parseInt(existing.getVersion()) : 0;
        existing.setVersion(String.valueOf(currentVersion + 1));
        
        validateMapping(existing);
        
        log.info("Updating field mapping {} with target field: {}", 
            existing.getId(), existing.getTargetField());
        
        return existing;
    }
    
    private void validateJavaFunction(String javaFunction) {
        // Basic validation - could be enhanced with more comprehensive checks
        if (!javaFunction.contains("(") || !javaFunction.contains(")")) {
            throw new IllegalArgumentException("Invalid Java function format");
        }
    }
}