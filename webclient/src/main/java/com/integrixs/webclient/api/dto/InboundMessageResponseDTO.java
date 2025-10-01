package com.integrixs.webclient.api.dto;


import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for inbound message response
 */
public class InboundMessageResponseDTO {

    private String messageId;
    private boolean success;
    private String status;
    private String flowId;
    private String executionId;
    private Object responseData;
    private String error;
    private List<ValidationErrorDTO> validationErrors;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private Long processingTimeMillis;

    // Getters
    public String getMessageId() {
        return messageId;
    }
    public boolean isSuccess() {
        return success;
    }
    public String getStatus() {
        return status;
    }
    public String getFlowId() {
        return flowId;
    }
    public String getExecutionId() {
        return executionId;
    }
    public Object getResponseData() {
        return responseData;
    }
    public String getError() {
        return error;
    }
    public List<ValidationErrorDTO> getValidationErrors() {
        return validationErrors;
    }
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    public Long getProcessingTimeMillis() {
        return processingTimeMillis;
    }

    // Setters
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }
    public void setError(String error) {
        this.error = error;
    }
    public void setValidationErrors(List<ValidationErrorDTO> validationErrors) {
        this.validationErrors = validationErrors;
    }
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    public void setProcessingTimeMillis(Long processingTimeMillis) {
        this.processingTimeMillis = processingTimeMillis;
    }

    // Builder
    public static InboundMessageResponseDTOBuilder builder() {
        return new InboundMessageResponseDTOBuilder();
    }

    public static class InboundMessageResponseDTOBuilder {
        private String messageId;
        private boolean success;
        private String status;
        private String flowId;
        private String executionId;
        private Object responseData;
        private String error;
        private List<ValidationErrorDTO> validationErrors;
        private LocalDateTime receivedAt;
        private LocalDateTime processedAt;
        private Long processingTimeMillis;

        public InboundMessageResponseDTOBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public InboundMessageResponseDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public InboundMessageResponseDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public InboundMessageResponseDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public InboundMessageResponseDTOBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public InboundMessageResponseDTOBuilder responseData(Object responseData) {
            this.responseData = responseData;
            return this;
        }

        public InboundMessageResponseDTOBuilder error(String error) {
            this.error = error;
            return this;
        }

        public InboundMessageResponseDTOBuilder validationErrors(List<ValidationErrorDTO> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public InboundMessageResponseDTOBuilder receivedAt(LocalDateTime receivedAt) {
            this.receivedAt = receivedAt;
            return this;
        }

        public InboundMessageResponseDTOBuilder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public InboundMessageResponseDTOBuilder processingTimeMillis(Long processingTimeMillis) {
            this.processingTimeMillis = processingTimeMillis;
            return this;
        }

        public InboundMessageResponseDTO build() {
            InboundMessageResponseDTO result = new InboundMessageResponseDTO();
            result.messageId = this.messageId;
            result.success = this.success;
            result.status = this.status;
            result.flowId = this.flowId;
            result.executionId = this.executionId;
            result.responseData = this.responseData;
            result.error = this.error;
            result.validationErrors = this.validationErrors;
            result.receivedAt = this.receivedAt;
            result.processedAt = this.processedAt;
            result.processingTimeMillis = this.processingTimeMillis;
            return result;
        }
    }
}
