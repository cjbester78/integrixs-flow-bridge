package com.integrixs.shared.integration;

import java.util.Map;
import java.util.Objects;

/**
 * Result of adapter execution
 */
public class AdapterResult {
    private boolean success;
    private Map<String, Object> outputData;
    private String errorMessage;
    private String errorCode;
    private Long processingTime;
    private Map<String, Object> metadata;

    // Default constructor
    public AdapterResult() {
    }

    // All args constructor
    public AdapterResult(boolean success, Map<String, Object> outputData, String errorMessage,
                        String errorCode, Long processingTime, Map<String, Object> metadata) {
        this.success = success;
        this.outputData = outputData;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.processingTime = processingTime;
        this.metadata = metadata;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getOutputData() {
        return outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setOutputData(Map<String, Object> outputData) {
        this.outputData = outputData;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private Map<String, Object> outputData;
        private String errorMessage;
        private String errorCode;
        private Long processingTime;
        private Map<String, Object> metadata;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder outputData(Map<String, Object> outputData) {
            this.outputData = outputData;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder processingTime(Long processingTime) {
            this.processingTime = processingTime;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public AdapterResult build() {
            return new AdapterResult(success, outputData, errorMessage, errorCode, processingTime, metadata);
        }
    }

    // Static factory methods
    public static AdapterResult success(Map<String, Object> outputData) {
        return AdapterResult.builder()
                .success(true)
                .outputData(outputData)
                .build();
    }

    public static AdapterResult failure(String errorMessage, String errorCode) {
        return AdapterResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterResult that = (AdapterResult) o;
        return success == that.success &&
               Objects.equals(outputData, that.outputData) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(processingTime, that.processingTime) &&
               Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, outputData, errorMessage, errorCode, processingTime, metadata);
    }

    @Override
    public String toString() {
        return "AdapterResult{" +
                "success=" + success +
                ", outputData=" + outputData +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", processingTime=" + processingTime +
                ", metadata=" + metadata +
                '}';
    }
}
