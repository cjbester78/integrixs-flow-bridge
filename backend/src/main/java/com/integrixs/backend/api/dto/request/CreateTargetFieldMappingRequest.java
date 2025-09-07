package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.TargetFieldMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a target field mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTargetFieldMappingRequest {
    
    @NotBlank(message = "Source field path is required")
    @Size(max = 500, message = "Source field path cannot exceed 500 characters")
    private String sourceFieldPath;
    
    @NotBlank(message = "Target field path is required")
    @Size(max = 500, message = "Target field path cannot exceed 500 characters")
    private String targetFieldPath;
    
    @Builder.Default
    private TargetFieldMapping.MappingType mappingType = TargetFieldMapping.MappingType.DIRECT;
    
    private String transformationExpression;
    
    @Size(max = 1000, message = "Constant value cannot exceed 1000 characters")
    private String constantValue;
    
    private String conditionExpression;
    
    @Size(max = 1000, message = "Default value cannot exceed 1000 characters")
    private String defaultValue;
    
    private String targetDataType;
    
    @Builder.Default
    private boolean required = false;
    
    @Min(value = 0, message = "Mapping order must be non-negative")
    private Integer mappingOrder;
    
    private String visualFlowData;
    
    private String validationRules;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Builder.Default
    private boolean active = true;
}