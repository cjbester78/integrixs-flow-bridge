package com.integrixs.soapbindings.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for SOAP operation request
 */
@Data
@Builder
public class SoapOperationRequest {
    private String operationName;
    private Object payload;
    @Builder.Default
    private Map<String, String> soapHeaders = new HashMap<>();
    private Long timeoutMillis;
}
