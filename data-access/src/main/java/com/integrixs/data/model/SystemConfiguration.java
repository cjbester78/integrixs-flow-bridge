package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing system configuration settings.
 */
public class SystemConfiguration {

    private UUID id;

    private String configKey;

    private String configValue;

    private String configType;

    private String description;

        private LocalDateTime updatedAt;

    private User createdBy;

        private LocalDateTime createdAt;

    private User updatedBy;

    // Default constructor
    public SystemConfiguration() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Builder
    public static SystemConfigurationBuilder builder() {
        return new SystemConfigurationBuilder();
    }

    public static class SystemConfigurationBuilder {
        private UUID id;
        private String configKey;
        private String configValue;
        private String configType;
        private String description;
        private LocalDateTime updatedAt;
        private User createdBy;
        private LocalDateTime createdAt;
        private User updatedBy;

        public SystemConfigurationBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public SystemConfigurationBuilder configKey(String configKey) {
            this.configKey = configKey;
            return this;
        }

        public SystemConfigurationBuilder configValue(String configValue) {
            this.configValue = configValue;
            return this;
        }

        public SystemConfigurationBuilder configType(String configType) {
            this.configType = configType;
            return this;
        }

        public SystemConfigurationBuilder description(String description) {
            this.description = description;
            return this;
        }

        public SystemConfigurationBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SystemConfigurationBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public SystemConfigurationBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SystemConfigurationBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public SystemConfiguration build() {
            SystemConfiguration instance = new SystemConfiguration();
            instance.setId(this.id);
            instance.setConfigKey(this.configKey);
            instance.setConfigValue(this.configValue);
            instance.setConfigType(this.configType);
            instance.setDescription(this.description);
            instance.setUpdatedAt(this.updatedAt);
            instance.setCreatedBy(this.createdBy);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedBy(this.updatedBy);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "SystemConfiguration{" +
                "id=" + id + "configKey=" + configKey + "configValue=" + configValue + "configType=" + configType + "description=" + description + "..." +
                '}';
    }
}
