package com.integrixs.shared.dto;

import java.time.LocalDateTime;
public class AdapterStatusDTO {

    private String id;
    private String name;
    private String type;
    private String mode;
    private String status;
    private Integer load;
    private String businessComponentId;
    private String businessComponentName;
    private Long messagesProcessed;
    private Long errorsCount;
    private LocalDateTime lastActivity;
    private String lastError;

    // Default constructor
    public AdapterStatusDTO() {
    }

    // All args constructor
    public AdapterStatusDTO(String id, String name, String type, String mode, String status, Integer load, String businessComponentId, String businessComponentName, Long messagesProcessed, Long errorsCount, LocalDateTime lastActivity, String lastError) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.status = status;
        this.load = load;
        this.businessComponentId = businessComponentId;
        this.businessComponentName = businessComponentName;
        this.messagesProcessed = messagesProcessed;
        this.errorsCount = errorsCount;
        this.lastActivity = lastActivity;
        this.lastError = lastError;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getMode() { return mode; }
    public String getStatus() { return status; }
    public Integer getLoad() { return load; }
    public String getBusinessComponentId() { return businessComponentId; }
    public String getBusinessComponentName() { return businessComponentName; }
    public Long getMessagesProcessed() { return messagesProcessed; }
    public Long getErrorsCount() { return errorsCount; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public String getLastError() { return lastError; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setMode(String mode) { this.mode = mode; }
    public void setStatus(String status) { this.status = status; }
    public void setLoad(Integer load) { this.load = load; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    public void setBusinessComponentName(String businessComponentName) { this.businessComponentName = businessComponentName; }
    public void setMessagesProcessed(Long messagesProcessed) { this.messagesProcessed = messagesProcessed; }
    public void setErrorsCount(Long errorsCount) { this.errorsCount = errorsCount; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    // Builder
    public static AdapterStatusDTOBuilder builder() {
        return new AdapterStatusDTOBuilder();
    }

    public static class AdapterStatusDTOBuilder {
        private String id;
        private String name;
        private String type;
        private String mode;
        private String status;
        private Integer load;
        private String businessComponentId;
        private String businessComponentName;
        private Long messagesProcessed;
        private Long errorsCount;
        private LocalDateTime lastActivity;
        private String lastError;

        public AdapterStatusDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AdapterStatusDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AdapterStatusDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public AdapterStatusDTOBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public AdapterStatusDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AdapterStatusDTOBuilder load(Integer load) {
            this.load = load;
            return this;
        }

        public AdapterStatusDTOBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public AdapterStatusDTOBuilder businessComponentName(String businessComponentName) {
            this.businessComponentName = businessComponentName;
            return this;
        }

        public AdapterStatusDTOBuilder messagesProcessed(Long messagesProcessed) {
            this.messagesProcessed = messagesProcessed;
            return this;
        }

        public AdapterStatusDTOBuilder errorsCount(Long errorsCount) {
            this.errorsCount = errorsCount;
            return this;
        }

        public AdapterStatusDTOBuilder lastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
            return this;
        }

        public AdapterStatusDTOBuilder lastError(String lastError) {
            this.lastError = lastError;
            return this;
        }

        public AdapterStatusDTO build() {
            return new AdapterStatusDTO(id, name, type, mode, status, load, businessComponentId, businessComponentName, messagesProcessed, errorsCount, lastActivity, lastError);
        }
    }
}
