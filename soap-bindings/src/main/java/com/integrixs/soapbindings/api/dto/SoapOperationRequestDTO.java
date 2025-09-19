package com.integrixs.soapbindings.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for SOAP operation request
 */
public class SoapOperationRequestDTO {

    private String operationName;
    private Object payload;
    private Map<String, String> soapHeaders = new HashMap<>();
    private Long timeoutMillis;

    // Default constructor
    public SoapOperationRequestDTO() {
    }

    // All args constructor
    public SoapOperationRequestDTO(String operationName, Object payload, Map<String, String> soapHeaders, Long timeoutMillis) {
        this.operationName = operationName;
        this.payload = payload;
        this.soapHeaders = soapHeaders != null ? soapHeaders : new HashMap<>();
        this.timeoutMillis = timeoutMillis;
    }

    // Getters
    public String getOperationName() { return operationName; }
    public Object getPayload() { return payload; }
    public Map<String, String> getSoapHeaders() { return soapHeaders; }
    public Long getTimeoutMillis() { return timeoutMillis; }

    // Setters
    public void setOperationName(String operationName) { this.operationName = operationName; }
    public void setPayload(Object payload) { this.payload = payload; }
    public void setSoapHeaders(Map<String, String> soapHeaders) { this.soapHeaders = soapHeaders; }
    public void setTimeoutMillis(Long timeoutMillis) { this.timeoutMillis = timeoutMillis; }

    // Builder
    public static SoapOperationRequestDTOBuilder builder() {
        return new SoapOperationRequestDTOBuilder();
    }

    public static class SoapOperationRequestDTOBuilder {
        private String operationName;
        private Object payload;
        private Map<String, String> soapHeaders = new HashMap<>();
        private Long timeoutMillis;

        public SoapOperationRequestDTOBuilder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public SoapOperationRequestDTOBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public SoapOperationRequestDTOBuilder soapHeaders(Map<String, String> soapHeaders) {
            this.soapHeaders = soapHeaders;
            return this;
        }

        public SoapOperationRequestDTOBuilder timeoutMillis(Long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public SoapOperationRequestDTO build() {
            return new SoapOperationRequestDTO(operationName, payload, soapHeaders, timeoutMillis);
        }
    }
}
