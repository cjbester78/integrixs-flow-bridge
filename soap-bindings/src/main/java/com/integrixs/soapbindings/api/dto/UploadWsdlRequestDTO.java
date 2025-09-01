package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for uploading WSDL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadWsdlRequestDTO {
    
    @NotNull(message = "WSDL name is required")
    private String name;
    
    @NotNull(message = "WSDL content is required")
    private String wsdlContent;
    
    private String location;
    
    private String description;
}