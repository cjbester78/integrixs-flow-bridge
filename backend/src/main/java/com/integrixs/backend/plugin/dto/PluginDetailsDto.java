package com.integrixs.backend.plugin.dto;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import com.integrixs.backend.plugin.api.ConfigurationSchema;
import com.integrixs.backend.plugin.api.HealthStatus;
/**
 * DTO for detailed plugin information
 */
public class PluginDetailsDto {
    private AdapterMetadata metadata;
    private ConfigurationSchema configurationSchema;
    private HealthStatus health;
    private boolean isInitialized;

    // Default constructor
    public PluginDetailsDto() {
    }

    public AdapterMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AdapterMetadata metadata) {
        this.metadata = metadata;
    }

    public ConfigurationSchema getConfigurationSchema() {
        return configurationSchema;
    }

    public void setConfigurationSchema(ConfigurationSchema configurationSchema) {
        this.configurationSchema = configurationSchema;
    }

    public HealthStatus getHealth() {
        return health;
    }

    public void setHealth(HealthStatus health) {
        this.health = health;
    }

    public boolean isIsInitialized() {
        return isInitialized;
    }

    public void setIsInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    // Builder pattern
    public static PluginDetailsDtoBuilder builder() {
        return new PluginDetailsDtoBuilder();
    }

    public static class PluginDetailsDtoBuilder {
        private AdapterMetadata metadata;
        private ConfigurationSchema configurationSchema;
        private HealthStatus health;
        private boolean isInitialized;

        public PluginDetailsDtoBuilder metadata(AdapterMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public PluginDetailsDtoBuilder configurationSchema(ConfigurationSchema configurationSchema) {
            this.configurationSchema = configurationSchema;
            return this;
        }

        public PluginDetailsDtoBuilder health(HealthStatus health) {
            this.health = health;
            return this;
        }

        public PluginDetailsDtoBuilder isInitialized(boolean isInitialized) {
            this.isInitialized = isInitialized;
            return this;
        }

        public PluginDetailsDto build() {
            PluginDetailsDto dto = new PluginDetailsDto();
            dto.setMetadata(this.metadata);
            dto.setConfigurationSchema(this.configurationSchema);
            dto.setHealth(this.health);
            dto.setIsInitialized(this.isInitialized);
            return dto;
        }
    }
}
