package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for field mapping data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMappingDTO {
    private String id;
    private String transformationId;
    private List<String> sourceFields; // Changed from String to List<String>
    private String targetField; // Deprecated - use targetFields
    private List<String> targetFields; // New - supports 1 - to - many mappings
    private String mappingType; // DIRECT, SPLIT, AGGREGATE, CONDITIONAL, ITERATE
    private Object splitConfiguration; // Configuration for SPLIT type mappings
    private String javaFunction;
    private String mappingRule;
    private String sourceXPath;
    private String targetXPath;
    private boolean isArrayMapping;
    private String arrayContextPath;
    private boolean namespaceAware;
    private String inputTypes;
    private String outputType;
    private String description;
    private String version;
    private String functionName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields used by frontend
    private String name;
    private List<String> sourcePaths;
    private String targetPath;
    private boolean requiresTransformation;
    private Object functionNode; // For function - based mappings
    private Object visualFlowData; // For visual flow persistence
    private Integer mappingOrder;

    // Helper methods for backward compatibility
    public List<String> getTargetFieldsList() {
        if(targetFields != null && !targetFields.isEmpty()) {
            return targetFields;
        } else if(targetField != null && !targetField.isEmpty()) {
            return List.of(targetField);
        }
        return List.of();
    }

    public void setTargetFieldsList(List<String> fields) {
        this.targetFields = fields;
        // Set single field for backward compatibility
        if(fields != null && fields.size() == 1) {
            this.targetField = fields.get(0);
        } else {
            this.targetField = null;
        }
    }

    public boolean isOneToManyMapping() {
        return targetFields != null && targetFields.size() > 1;
    }
}
