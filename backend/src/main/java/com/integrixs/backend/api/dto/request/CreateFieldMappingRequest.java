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
    
    @NotBlank(message = "Target field is required")
    private String targetField;
    
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
}