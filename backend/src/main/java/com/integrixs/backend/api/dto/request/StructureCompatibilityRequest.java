package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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

    // Default constructor
    public StructureCompatibilityRequest() {
    }

    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetContent() {
        return targetContent;
    }

    public void setTargetContent(String targetContent) {
        this.targetContent = targetContent;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public boolean isIncludeDetailedAnalysis() {
        return includeDetailedAnalysis;
    }

    public void setIncludeDetailedAnalysis(boolean includeDetailedAnalysis) {
        this.includeDetailedAnalysis = includeDetailedAnalysis;
    }

    public boolean isGenerateMappingSuggestions() {
        return generateMappingSuggestions;
    }

    public void setGenerateMappingSuggestions(boolean generateMappingSuggestions) {
        this.generateMappingSuggestions = generateMappingSuggestions;
    }
}
