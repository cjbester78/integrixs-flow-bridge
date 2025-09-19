package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for message details
 */
public class MessageResponse {

    private String id;
    private String correlationId;
    private String status;
    private String source;
    private String target;
    private String type;

    private String flowId;
    private String flowName;

    private String payload;
    private Map<String, String> headers;
    private Map<String, Object> metadata;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;

    private Long executionTimeMs;
    private Integer retryCount;
    private Integer priority;

    private String errorMessage;
    private String errorDetails;

    private String businessComponentId;
    private String businessComponentName;

    // Default constructor
    public MessageResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getBusinessComponentId() {
        return businessComponentId;
    }

    public void setBusinessComponentId(String businessComponentId) {
        this.businessComponentId = businessComponentId;
    }

    public String getBusinessComponentName() {
        return businessComponentName;
    }

    public void setBusinessComponentName(String businessComponentName) {
        this.businessComponentName = businessComponentName;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static MessageResponseBuilder builder() {
        return new MessageResponseBuilder();
    }

    public static class MessageResponseBuilder {
        private String id;
        private String correlationId;
        private String status;
        private String source;
        private String target;
        private String type;
        private String flowId;
        private String flowName;
        private String payload;
        private Map<String, String> headers;
        private Map<String, Object> metadata;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
        private LocalDateTime completedAt;
        private Long executionTimeMs;
        private Integer retryCount;
        private Integer priority;
        private String errorMessage;
        private String errorDetails;
        private String businessComponentId;
        private String businessComponentName;

        public MessageResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MessageResponseBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public MessageResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public MessageResponseBuilder source(String source) {
            this.source = source;
            return this;
        }

        public MessageResponseBuilder target(String target) {
            this.target = target;
            return this;
        }

        public MessageResponseBuilder type(String type) {
            this.type = type;
            return this;
        }

        public MessageResponseBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public MessageResponseBuilder flowName(String flowName) {
            this.flowName = flowName;
            return this;
        }

        public MessageResponseBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public MessageResponseBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public MessageResponseBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public MessageResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MessageResponseBuilder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public MessageResponseBuilder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public MessageResponseBuilder executionTimeMs(Long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public MessageResponseBuilder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public MessageResponseBuilder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public MessageResponseBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public MessageResponseBuilder errorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }

        public MessageResponseBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public MessageResponseBuilder businessComponentName(String businessComponentName) {
            this.businessComponentName = businessComponentName;
            return this;
        }

        public MessageResponse build() {
            MessageResponse response = new MessageResponse();
            response.id = this.id;
            response.correlationId = this.correlationId;
            response.status = this.status;
            response.source = this.source;
            response.target = this.target;
            response.type = this.type;
            response.flowId = this.flowId;
            response.flowName = this.flowName;
            response.payload = this.payload;
            response.headers = this.headers;
            response.metadata = this.metadata;
            response.createdAt = this.createdAt;
            response.processedAt = this.processedAt;
            response.completedAt = this.completedAt;
            response.executionTimeMs = this.executionTimeMs;
            response.retryCount = this.retryCount;
            response.priority = this.priority;
            response.errorMessage = this.errorMessage;
            response.errorDetails = this.errorDetails;
            response.businessComponentId = this.businessComponentId;
            response.businessComponentName = this.businessComponentName;
            return response;
        }
    }
}
