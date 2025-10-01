package com.integrixs.adapters.api.dto;

import java.util.Map;

/**
 * DTO for adapter operation responses
 */
public class AdapterOperationResponseDTO {
    private String adapterId;
    private boolean success;
    private String message;
    private String errorMessage;
    private Object data;
    private Map<String, Object> metadata;
    private int recordsProcessed;

    public AdapterOperationResponseDTO() {
    }

    public AdapterOperationResponseDTO(String adapterId, boolean success, String message, String errorMessage,
                                       Object data, Map<String, Object> metadata, int recordsProcessed) {
        this.adapterId = adapterId;
        this.success = success;
        this.message = message;
        this.errorMessage = errorMessage;
        this.data = data;
        this.metadata = metadata;
        this.recordsProcessed = recordsProcessed;
    }


    // Getters and Setters
    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(int recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterId;
        private boolean success;
        private String message;
        private String errorMessage;
        private Object data;
        private Map<String, Object> metadata;
        private int recordsProcessed;

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder recordsProcessed(int recordsProcessed) {
            this.recordsProcessed = recordsProcessed;
            return this;
        }

        public AdapterOperationResponseDTO build() {
            return new AdapterOperationResponseDTO(adapterId, success, message, errorMessage, data, metadata, recordsProcessed);
        }
    }
}
