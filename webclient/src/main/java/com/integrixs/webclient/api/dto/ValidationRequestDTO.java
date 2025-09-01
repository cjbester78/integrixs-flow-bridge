package com.integrixs.webclient.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for message validation request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequestDTO {
    
    @NotNull(message = "Message type is required")
    private String messageType;
    
    @NotNull(message = "Payload is required")
    private Object payload;
    
    private String contentType;
    
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    
    private String schemaId;
}