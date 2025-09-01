package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for creating SOAP binding
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBindingRequestDTO {
    
    @NotNull(message = "Binding name is required")
    private String bindingName;
    
    @NotNull(message = "WSDL ID is required")
    private String wsdlId;
    
    @NotNull(message = "Service name is required")
    private String serviceName;
    
    @NotNull(message = "Port name is required")
    private String portName;
    
    @NotNull(message = "Endpoint URL is required")
    private String endpointUrl;
    
    @Builder.Default
    private String bindingStyle = "DOCUMENT";
    
    @Builder.Default
    private String transport = "HTTP";
    
    @Builder.Default
    private Map<String, String> soapHeaders = new HashMap<>();
    
    private SecurityConfigurationDTO security;
}