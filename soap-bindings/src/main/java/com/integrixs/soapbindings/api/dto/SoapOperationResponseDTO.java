package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for SOAP operation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoapOperationResponseDTO {
    
    private String operationId;
    private boolean success;
    private Object response;
    private String errorMessage;
    private LocalDateTime timestamp;
}