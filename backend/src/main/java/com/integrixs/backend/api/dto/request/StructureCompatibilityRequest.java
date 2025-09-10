package com.integrixs.backend.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructureCompatibilityRequest {
    
    @NotBlank(message = "Source content is required")
    private String sourceContent;
    
    @NotBlank(message = "Source type is required")
    @Pattern(regexp = "WSDL|JSON_SCHEMA|XSD", message = "Source type must be WSDL, JSON_SCHEMA, or XSD")
    private String sourceType;
    
    @NotBlank(message = "Target content is required")
    private String targetContent;
    
    @NotBlank(message = "Target type is required")
    @Pattern(regexp = "WSDL|JSON_SCHEMA|XSD", message = "Target type must be WSDL, JSON_SCHEMA, or XSD")
    private String targetType;
    
    // Optional parameters
    private boolean includeDetailedAnalysis = true;
    private boolean generateMappingSuggestions = true;
}