package com.integrixs.backend.plugin.dto;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import com.integrixs.backend.plugin.api.ConfigurationSchema;
import com.integrixs.backend.plugin.api.HealthStatus;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for detailed plugin information
 */
@Data
@Builder
public class PluginDetailsDto {
    private AdapterMetadata metadata;
    private ConfigurationSchema configurationSchema;
    private HealthStatus health;
    private boolean isInitialized;
}
