package com.integrixs.webserver.api.dto;

/**
 * DTO for request history
 */
public class RequestHistoryDTO {

    private String requestId;
    private String requestType;
    private String targetUrl;
    private String httpMethod;
    private String flowId;
    private String adapterId;
    private Integer statusCode;
    private Boolean success;
    private Long responseTime;
    private String errorMessage;

    // Default constructor
    public RequestHistoryDTO() {
    }

    // All args constructor
    public RequestHistoryDTO(String requestId, String requestType, String targetUrl, String httpMethod, String flowId, String adapterId, Integer statusCode, Boolean success, Long responseTime, String errorMessage) {
        this.requestId = requestId;
        this.requestType = requestType;
        this.targetUrl = targetUrl;
        this.httpMethod = httpMethod;
        this.flowId = flowId;
        this.adapterId = adapterId;
        this.statusCode = statusCode;
        this.success = success;
        this.responseTime = responseTime;
        this.errorMessage = errorMessage;
    }

    // Getters
    public String getRequestId() {
        return requestId;
    }
    public String getRequestType() {
        return requestType;
    }
    public String getTargetUrl() {
        return targetUrl;
    }
    public String getHttpMethod() {
        return httpMethod;
    }
    public String getFlowId() {
        return flowId;
    }
    public String getAdapterId() {
        return adapterId;
    }
    public Integer getStatusCode() {
        return statusCode;
    }
    public Boolean getSuccess() {
        return success;
    }
    public Long getResponseTime() {
        return responseTime;
    }
    public String getErrorMessage() {
        return errorMessage;
    }

    // Setters
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // Builder
    public static RequestHistoryDTOBuilder builder() {
        return new RequestHistoryDTOBuilder();
    }

    public static class RequestHistoryDTOBuilder {
        private String requestId;
        private String requestType;
        private String targetUrl;
        private String httpMethod;
        private String flowId;
        private String adapterId;
        private Integer statusCode;
        private Boolean success;
        private Long responseTime;
        private String errorMessage;

        public RequestHistoryDTOBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RequestHistoryDTOBuilder requestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public RequestHistoryDTOBuilder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public RequestHistoryDTOBuilder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public RequestHistoryDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public RequestHistoryDTOBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public RequestHistoryDTOBuilder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public RequestHistoryDTOBuilder success(Boolean success) {
            this.success = success;
            return this;
        }

        public RequestHistoryDTOBuilder responseTime(Long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public RequestHistoryDTOBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public RequestHistoryDTO build() {
            return new RequestHistoryDTO(requestId, requestType, targetUrl, httpMethod, flowId, adapterId, statusCode, success, responseTime, errorMessage);
        }
    }}
