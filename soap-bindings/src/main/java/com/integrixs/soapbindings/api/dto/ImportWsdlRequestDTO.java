package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for importing WSDL from URL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportWsdlRequestDTO {
    
    @NotNull(message = "WSDL name is required")
    private String name;
    
    @NotNull(message = "WSDL URL is required")
    private String wsdlUrl;
    
    private String description;
}