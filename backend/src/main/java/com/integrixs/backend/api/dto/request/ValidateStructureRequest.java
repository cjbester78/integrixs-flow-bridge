package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateStructureRequest {

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Structure type is required")
    private String structureType; // WSDL, JSON_SCHEMA, XSD

    private boolean strictMode = false;

    private boolean extractMetadata = true;
}
