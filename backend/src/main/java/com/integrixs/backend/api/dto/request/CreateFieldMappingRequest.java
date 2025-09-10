package com.integrixs.backend.api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for creating a field mapping
 */
@Data
public class CreateFieldMappingRequest {
    
    @NotNull(message = "Source fields are required")
    private List<String> sourceFields;
    
    private String targetField;  // Deprecated - use targetFields
    
    private List<String> targetFields;  // New - supports 1-to-many mappings
    
    private String mappingType = "DIRECT";  // DIRECT, SPLIT, AGGREGATE, CONDITIONAL, ITERATE
    
    private Object splitConfiguration;  // Configuration for SPLIT type mappings
    
    private String javaFunction;
    private String mappingRule;
    private String inputTypes;
    private String outputType;
    private String description;
    private String functionName;
    private boolean active = true;
    private boolean arrayMapping = false;
    private String arrayContextPath;
    private String sourceXPath;
    private String targetXPath;
    private Integer mappingOrder;
    private Object visualFlowData;
    private Object functionNode;
    
    // Helper methods for backward compatibility
    public List<String> getTargetFieldsList() {
        if (targetFields != null && !targetFields.isEmpty()) {
            return targetFields;
        } else if (targetField != null && !targetField.isEmpty()) {
            return List.of(targetField);
        }
        return List.of();
    }
    
    public void setTargetFieldsList(List<String> fields) {
        this.targetFields = fields;
        // Set single field for backward compatibility
        if (fields != null && fields.size() == 1) {
            this.targetField = fields.get(0);
        } else {
            this.targetField = null;
        }
    }
}