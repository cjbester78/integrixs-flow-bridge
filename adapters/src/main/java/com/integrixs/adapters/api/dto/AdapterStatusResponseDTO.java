package com.integrixs.adapters.api.dto;

import java.util.Map;

/**
 * DTO for adapter status responses
 */
public class AdapterStatusResponseDTO {
    public AdapterStatusResponseDTO() {
    }


    private String adapterId;
    private String adapterName;
    private String adapterType;
    private String adapterMode;
    private boolean isActive;
    private String status;
    private Map<String, Object> metadata;
    private String lastError;
    private Long lastActivityTimestamp;
    private Integer messagesProcessed;
    private Integer errorCount;
    // Getters and Setters
    public String getAdapterId() {
        return adapterId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }
    public String getAdapterName() {
        return adapterName;
    }
    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
    public String getAdapterType() {
        return adapterType;
    }
    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }
    public String getAdapterMode() {
        return adapterMode;
    }
    public void setAdapterMode(String adapterMode) {
        this.adapterMode = adapterMode;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    public String getLastError() {
        return lastError;
    }
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
    public Long getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }
    public void setLastActivityTimestamp(Long lastActivityTimestamp) {
        this.lastActivityTimestamp = lastActivityTimestamp;
    }
    public Integer getMessagesProcessed() {
        return messagesProcessed;
    }
    public void setMessagesProcessed(Integer messagesProcessed) {
        this.messagesProcessed = messagesProcessed;
    }
    public Integer getErrorCount() {
        return errorCount;
    }
    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterId;
        private String adapterName;
        private String adapterType;
        private String adapterMode;
        private boolean isActive;
        private String status;
        private Map<String, Object> metadata;
        private String lastError;
        private Long lastActivityTimestamp;
        private Integer messagesProcessed;
        private Integer errorCount;

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder adapterName(String adapterName) {
            this.adapterName = adapterName;
            return this;
        }

        public Builder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public Builder adapterMode(String adapterMode) {
            this.adapterMode = adapterMode;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder lastError(String lastError) {
            this.lastError = lastError;
            return this;
        }

        public Builder lastActivityTimestamp(Long lastActivityTimestamp) {
            this.lastActivityTimestamp = lastActivityTimestamp;
            return this;
        }

        public Builder messagesProcessed(Integer messagesProcessed) {
            this.messagesProcessed = messagesProcessed;
            return this;
        }

        public Builder errorCount(Integer errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public AdapterStatusResponseDTO build() {
            AdapterStatusResponseDTO obj = new AdapterStatusResponseDTO();
            obj.adapterId = this.adapterId;
            obj.adapterName = this.adapterName;
            obj.adapterType = this.adapterType;
            obj.adapterMode = this.adapterMode;
            obj.isActive = this.isActive;
            obj.status = this.status;
            obj.metadata = this.metadata;
            obj.lastError = this.lastError;
            obj.lastActivityTimestamp = this.lastActivityTimestamp;
            obj.messagesProcessed = this.messagesProcessed;
            obj.errorCount = this.errorCount;
            return obj;
        }
    }
}
