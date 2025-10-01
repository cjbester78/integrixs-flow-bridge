package com.integrixs.webserver.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for outbound request
 */
public class OutboundRequestDTO {

    @NotNull(message = "Request type is required")
    private String requestType;

    @NotNull(message = "Target URL is required")
    private String targetUrl;

    @NotNull(message = "HTTP method is required")
    private String httpMethod;

    private Object payload;
    private String contentType;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();

    private AuthenticationConfigDTO authentication;
    private int timeoutSeconds = 30;

    private RetryConfigDTO retryConfig;

    private String flowId;
    private String adapterId;

    // Default constructor
    public OutboundRequestDTO() {
    }

    // All args constructor
    public OutboundRequestDTO(String requestType, String targetUrl, String httpMethod, Object payload, String contentType, AuthenticationConfigDTO authentication, int timeoutSeconds, RetryConfigDTO retryConfig, String flowId, String adapterId) {
        this.requestType = requestType;
        this.targetUrl = targetUrl;
        this.httpMethod = httpMethod;
        this.payload = payload;
        this.contentType = contentType;
        this.authentication = authentication;
        this.timeoutSeconds = timeoutSeconds;
        this.retryConfig = retryConfig;
        this.flowId = flowId;
        this.adapterId = adapterId;
    }

    // Getters
    @NotNull(message = "Request type is required")
    public String getRequestType() {
        return requestType;
    }
    @NotNull(message = "Target URL is required")
    public String getTargetUrl() {
        return targetUrl;
    }
    @NotNull(message = "HTTP method is required")
    public String getHttpMethod() {
        return httpMethod;
    }
    public Object getPayload() {
        return payload;
    }
    public String getContentType() {
        return contentType;
    }
    public AuthenticationConfigDTO getAuthentication() {
        return authentication;
    }
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    public RetryConfigDTO getRetryConfig() {
        return retryConfig;
    }
    public String getFlowId() {
        return flowId;
    }
    public String getAdapterId() {
        return adapterId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    // Setters
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public void setAuthentication(AuthenticationConfigDTO authentication) {
        this.authentication = authentication;
    }
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    public void setRetryConfig(RetryConfigDTO retryConfig) {
        this.retryConfig = retryConfig;
    }
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    // Builder
    public static OutboundRequestDTOBuilder builder() {
        return new OutboundRequestDTOBuilder();
    }

    public static class OutboundRequestDTOBuilder {
        private String requestType;
        private String targetUrl;
        private String httpMethod;
        private Object payload;
        private String contentType;
        private AuthenticationConfigDTO authentication;
        private int timeoutSeconds;
        private RetryConfigDTO retryConfig;
        private String flowId;
        private String adapterId;

        public OutboundRequestDTOBuilder requestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public OutboundRequestDTOBuilder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public OutboundRequestDTOBuilder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public OutboundRequestDTOBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public OutboundRequestDTOBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public OutboundRequestDTOBuilder authentication(AuthenticationConfigDTO authentication) {
            this.authentication = authentication;
            return this;
        }

        public OutboundRequestDTOBuilder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public OutboundRequestDTOBuilder retryConfig(RetryConfigDTO retryConfig) {
            this.retryConfig = retryConfig;
            return this;
        }

        public OutboundRequestDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public OutboundRequestDTOBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public OutboundRequestDTO build() {
            return new OutboundRequestDTO(requestType, targetUrl, httpMethod, payload, contentType, authentication, timeoutSeconds, retryConfig, flowId, adapterId);
        }
    }}
