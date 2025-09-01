package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for generating SOAP binding
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateBindingRequestDTO {
    
    @NotNull(message = "Package name is required")
    private String packageName;
    
    @Builder.Default
    private boolean autoCompile = true;
    
    private String outputDirectory;
}