package com.integrixs.soapbindings.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Domain model for SOAP operation response
 */
@Data
@Builder
public class SoapOperationResponse {
    private String operationId;
    private boolean success;
    private Object response;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}