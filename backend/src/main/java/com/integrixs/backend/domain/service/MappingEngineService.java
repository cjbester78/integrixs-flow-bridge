package com.integrixs.backend.domain.service;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowTransformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for field mapping engine operations
 */
@Service
public class MappingEngineService {

    private static final Logger log = LoggerFactory.getLogger(MappingEngineService.class);


    private final ObjectMapper objectMapper;

    public MappingEngineService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Validate field mapping configuration
     */
    public void validateMapping(FieldMapping mapping) {
        if(mapping.getSourceFieldsList() == null || mapping.getSourceFieldsList().isEmpty()) {
            throw new IllegalArgumentException("Source fields are required");
        }

        // Validate target fields - must have at least one
        List<String> targetFields = mapping.getTargetFieldsList();
        if(targetFields == null || targetFields.isEmpty()) {
            throw new IllegalArgumentException("At least one target field is required");
        }

        // Validate split configuration for SPLIT type
        if(mapping.getMappingType() == FieldMapping.MappingType.SPLIT &&
            mapping.getSplitConfiguration() == null) {
            throw new IllegalArgumentException("Split configuration is required for SPLIT mapping type");
        }

        // For 1 - to - many mappings, ensure proper configuration
        if(targetFields.size() > 1 && mapping.getMappingType() == FieldMapping.MappingType.DIRECT) {
            throw new IllegalArgumentException("Direct mapping type cannot have multiple target fields. Use SPLIT type instead.");
        }

        if(mapping.isArrayMapping() && (mapping.getArrayContextPath() == null || mapping.getArrayContextPath().isBlank())) {
            throw new IllegalArgumentException("Array context path is required for array mappings");
        }

        // Validate function configuration if present
        if(mapping.getJavaFunction() != null && !mapping.getJavaFunction().isBlank()) {
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
                if(orderCompare != 0) {
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
        if(visualFlowData == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(visualFlowData);
        } catch(JsonProcessingException e) {
            log.error("Failed to serialize visual flow data", e);
            throw new RuntimeException("Failed to serialize visual flow data", e);
        }
    }

    /**
     * Convert JSON string to visual flow data object
     */
    public Object deserializeVisualFlowData(String visualFlowDataJson) {
        if(visualFlowDataJson == null || visualFlowDataJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(visualFlowDataJson, Object.class);
        } catch(JsonProcessingException e) {
            log.error("Failed to deserialize visual flow data", e);
            return null;
        }
    }

    /**
     * Convert function node object to JSON string
     */
    public String serializeFunctionNode(Object functionNode) {
        if(functionNode == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(functionNode);
        } catch(JsonProcessingException e) {
            log.error("Failed to serialize function node", e);
            throw new RuntimeException("Failed to serialize function node", e);
        }
    }

    /**
     * Convert JSON string to function node object
     */
    public Object deserializeFunctionNode(String functionNodeJson) {
        if(functionNodeJson == null || functionNodeJson.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(functionNodeJson, Object.class);
        } catch(JsonProcessingException e) {
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
        if(mapping.getMappingOrder() == null) {
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

        // Update target fields - handle both single and multiple
        if(updates.getTargetFieldsList() != null && !updates.getTargetFieldsList().isEmpty()) {
            existing.setTargetFieldsList(updates.getTargetFieldsList());
        } else if(updates.getTargetField() != null) {
            existing.setTargetField(updates.getTargetField());
        }

        // Update mapping type and split configuration
        if(updates.getMappingType() != null) {
            existing.setMappingType(updates.getMappingType());
        }
        if(updates.getSplitConfiguration() != null) {
            existing.setSplitConfiguration(updates.getSplitConfiguration());
        }

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

        log.info("Updating field mapping {} with {} target field(s)",
            existing.getId(), existing.getTargetFieldsList().size());

        return existing;
    }

    private void validateJavaFunction(String javaFunction) {
        // Basic validation - could be enhanced with more comprehensive checks
        if(!javaFunction.contains("(") || !javaFunction.contains(")")) {
            throw new IllegalArgumentException("Invalid Java function format");
        }
    }

    /**
     * Serialize split configuration to JSON string
     */
    public String serializeSplitConfiguration(Object splitConfiguration) {
        if(splitConfiguration == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(splitConfiguration);
        } catch(JsonProcessingException e) {
            log.error("Error serializing split configuration", e);
            return " {}";
        }
    }

    /**
     * Deserialize split configuration from JSON string
     */
    public Object deserializeSplitConfiguration(String splitConfigurationJson) {
        if(splitConfigurationJson == null || splitConfigurationJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(splitConfigurationJson, Object.class);
        } catch(Exception e) {
            log.error("Error deserializing split configuration: {}", splitConfigurationJson, e);
            return null;
        }
    }
}
