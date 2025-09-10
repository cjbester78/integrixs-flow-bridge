package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for field mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMappingResponse {
    
    private String id;
    private String transformationId;
    private List<String> sourceFields;
    private String targetField;  // Deprecated - use targetFields
    private List<String> targetFields;  // New - supports 1-to-many mappings
    private String mappingType;  // DIRECT, SPLIT, AGGREGATE, CONDITIONAL, ITERATE
    private Object splitConfiguration;  // Configuration for SPLIT type mappings
    private String javaFunction;
    private String mappingRule;
    private String inputTypes;
    private String outputType;
    private String description;
    private String version;
    private String functionName;
    private boolean active;
    private boolean arrayMapping;
    private String arrayContextPath;
    private String sourceXPath;
    private String targetXPath;
    private Integer mappingOrder;
    private Object visualFlowData;
    private Object functionNode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods for backward compatibility
    public List<String> getTargetFieldsList() {
        if (targetFields != null && !targetFields.isEmpty()) {
            return targetFields;
        } else if (targetField != null && !targetField.isEmpty()) {
            return List.of(targetField);
        }
        return List.of();
    }
    
    public boolean isOneToManyMapping() {
        return targetFields != null && targetFields.size() > 1;
    }
}