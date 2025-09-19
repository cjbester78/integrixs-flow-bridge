package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class ValidateStructureRequest {

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Structure type is required")
    private String structureType; // WSDL, JSON_SCHEMA, XSD

    private boolean strictMode = false;

    private boolean extractMetadata = true;

    // Default constructor
    public ValidateStructureRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStructureType() {
        return structureType;
    }

    public void setStructureType(String structureType) {
        this.structureType = structureType;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public boolean isExtractMetadata() {
        return extractMetadata;
    }

    public void setExtractMetadata(boolean extractMetadata) {
        this.extractMetadata = extractMetadata;
    }
}
