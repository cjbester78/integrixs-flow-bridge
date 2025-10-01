package com.integrixs.soapbindings.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for SOAP operation request
 */
public class SoapOperationRequest {

    private String operationName;
    private Object payload;
    private Map<String, String> soapHeaders = new HashMap<>();
    private Long timeoutMillis;

    // Default constructor
    public SoapOperationRequest() {
    }

    // All args constructor
    public SoapOperationRequest(String operationName, Object payload, Map<String, String> soapHeaders, Long timeoutMillis) {
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
    public static SoapOperationRequestBuilder builder() {
        return new SoapOperationRequestBuilder();
    }

    public static class SoapOperationRequestBuilder {
        private String operationName;
        private Object payload;
        private Map<String, String> soapHeaders = new HashMap<>();
        private Long timeoutMillis;

        public SoapOperationRequestBuilder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public SoapOperationRequestBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public SoapOperationRequestBuilder soapHeaders(Map<String, String> soapHeaders) {
            this.soapHeaders = soapHeaders;
            return this;
        }

        public SoapOperationRequestBuilder timeoutMillis(Long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public SoapOperationRequest build() {
            return new SoapOperationRequest(operationName, payload, soapHeaders, timeoutMillis);
        }
    }
}
