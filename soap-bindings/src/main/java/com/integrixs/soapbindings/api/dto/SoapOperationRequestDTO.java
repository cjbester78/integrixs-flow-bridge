package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for SOAP operation request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoapOperationRequestDTO {
    
    @NotNull(message = "Operation name is required")
    private String operationName;
    
    @NotNull(message = "Payload is required")
    private Object payload;
    
    @Builder.Default
    private Map<String, String> soapHeaders = new HashMap<>();
    
    private Long timeoutMillis;
}