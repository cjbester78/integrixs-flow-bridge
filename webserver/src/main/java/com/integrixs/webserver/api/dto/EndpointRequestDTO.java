package com.integrixs.webserver.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for request using endpoint configuration
 */
public class EndpointRequestDTO {

    private String path;

    @NotNull(message = "HTTP method is required")
    private String method;

    private Object payload;

    private String contentType;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();

    private String flowId;

    private String adapterId;

    // Default constructor
    public EndpointRequestDTO() {
    }

    // All args constructor
    public EndpointRequestDTO(String path, String method, Object payload, String contentType, String flowId, String adapterId) {
        this.path = path;
        this.method = method;
        this.payload = payload;
        this.contentType = contentType;
        this.flowId = flowId;
        this.adapterId = adapterId;
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }

    // Getters
    public String getPath() {
        return path;
    }
    @NotNull(message = "HTTP method is required")
    public String getMethod() {
        return method;
    }
    public Object getPayload() {
        return payload;
    }
    public String getContentType() {
        return contentType;
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
    public void setPath(String path) {
        this.path = path;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
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
    public static EndpointRequestDTOBuilder builder() {
        return new EndpointRequestDTOBuilder();
    }

    public static class EndpointRequestDTOBuilder {
        private String path;
        private String method;
        private Object payload;
        private String contentType;
        private String flowId;
        private String adapterId;

        public EndpointRequestDTOBuilder path(String path) {
            this.path = path;
            return this;
        }

        public EndpointRequestDTOBuilder method(String method) {
            this.method = method;
            return this;
        }

        public EndpointRequestDTOBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public EndpointRequestDTOBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public EndpointRequestDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public EndpointRequestDTOBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public EndpointRequestDTO build() {
            return new EndpointRequestDTO(path, method, payload, contentType, flowId, adapterId);
        }
    }}
