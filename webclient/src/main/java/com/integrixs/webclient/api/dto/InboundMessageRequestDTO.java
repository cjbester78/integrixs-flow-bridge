package com.integrixs.webclient.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for inbound message request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundMessageRequestDTO {
    
    @NotNull(message = "Message type is required")
    private String messageType;
    
    @NotNull(message = "Source is required")
    private String source;
    
    @NotNull(message = "Adapter ID is required")
    private String adapterId;
    
    @NotNull(message = "Payload is required")
    private Object payload;
    
    private String contentType;
    
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    
    private String correlationId;
}