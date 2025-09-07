package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.TargetFieldMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * Request DTO for updating a target field mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTargetFieldMappingRequest {
    
    @Size(max = 500, message = "Source field path cannot exceed 500 characters")
    private String sourceFieldPath;
    
    @Size(max = 500, message = "Target field path cannot exceed 500 characters")
    private String targetFieldPath;
    
    private TargetFieldMapping.MappingType mappingType;
    
    private String transformationExpression;
    
    @Size(max = 1000, message = "Constant value cannot exceed 1000 characters")
    private String constantValue;
    
    private String conditionExpression;
    
    @Size(max = 1000, message = "Default value cannot exceed 1000 characters")
    private String defaultValue;
    
    private String targetDataType;
    
    private Boolean required;
    
    @Min(value = 0, message = "Mapping order must be non-negative")
    private Integer mappingOrder;
    
    private String visualFlowData;
    
    private String validationRules;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}