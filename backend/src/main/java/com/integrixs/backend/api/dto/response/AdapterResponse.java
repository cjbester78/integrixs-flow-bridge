package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for communication adapter
 */
public class AdapterResponse {

    private String id;
    private String name;
    private String type;
    private String mode;
    private String direction;
    private String description;

    private String businessComponentId;
    private String businessComponentName;

    private String externalAuthId;
    private String externalAuthName;

    private boolean active;
    private boolean healthy;
    private String status;

    private LocalDateTime lastHealthCheck;
    private LocalDateTime lastTestDate;
    private String lastTestResult;

    private Map<String, Object> configuration;

    private Integer usageCount;
    private LocalDateTime lastUsedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Default constructor
    public AdapterResponse() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AdapterResponse response = new AdapterResponse();

        public Builder id(String id) {
            response.id = id;
            return this;
        }

        public Builder name(String name) {
            response.name = name;
            return this;
        }

        public Builder type(String type) {
            response.type = type;
            return this;
        }

        public Builder mode(String mode) {
            response.mode = mode;
            return this;
        }

        public Builder direction(String direction) {
            response.direction = direction;
            return this;
        }

        public Builder description(String description) {
            response.description = description;
            return this;
        }

        public Builder businessComponentId(String businessComponentId) {
            response.businessComponentId = businessComponentId;
            return this;
        }

        public Builder businessComponentName(String businessComponentName) {
            response.businessComponentName = businessComponentName;
            return this;
        }

        public Builder externalAuthId(String externalAuthId) {
            response.externalAuthId = externalAuthId;
            return this;
        }

        public Builder externalAuthName(String externalAuthName) {
            response.externalAuthName = externalAuthName;
            return this;
        }

        public Builder active(boolean active) {
            response.active = active;
            return this;
        }

        public Builder healthy(boolean healthy) {
            response.healthy = healthy;
            return this;
        }

        public Builder status(String status) {
            response.status = status;
            return this;
        }

        public Builder lastHealthCheck(LocalDateTime lastHealthCheck) {
            response.lastHealthCheck = lastHealthCheck;
            return this;
        }

        public Builder lastTestDate(LocalDateTime lastTestDate) {
            response.lastTestDate = lastTestDate;
            return this;
        }

        public Builder lastTestResult(String lastTestResult) {
            response.lastTestResult = lastTestResult;
            return this;
        }

        public Builder configuration(Map<String, Object> configuration) {
            response.configuration = configuration;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public Builder createdBy(String createdBy) {
            response.createdBy = createdBy;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            response.updatedBy = updatedBy;
            return this;
        }

        public AdapterResponse build() {
            return response;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public String getExternalAuthName() {
        return externalAuthName;
    }

    public void setExternalAuthName(String externalAuthName) {
        this.externalAuthName = externalAuthName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(LocalDateTime lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    public LocalDateTime getLastTestDate() {
        return lastTestDate;
    }

    public void setLastTestDate(LocalDateTime lastTestDate) {
        this.lastTestDate = lastTestDate;
    }

    public String getLastTestResult() {
        return lastTestResult;
    }

    public void setLastTestResult(String lastTestResult) {
        this.lastTestResult = lastTestResult;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
