package com.integrixs.backend.plugin.registry;

import com.integrixs.backend.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates adapter plugins
 */
@Component
public class PluginValidator {

    private static final Logger logger = LoggerFactory.getLogger(PluginValidator.class);

    /**
     * Validate a plugin
     */
    public ValidationResult validate(AdapterPlugin plugin) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate metadata
        AdapterMetadata metadata = plugin.getMetadata();
        if(metadata == null) {
            errors.add("Plugin metadata is null");
            return ValidationResult.failure(errors);
        }

        // Required metadata fields
        if(isNullOrEmpty(metadata.getId())) {
            errors.add("Plugin ID is required");
        }
        if(isNullOrEmpty(metadata.getName())) {
            errors.add("Plugin name is required");
        }
        if(isNullOrEmpty(metadata.getVersion())) {
            errors.add("Plugin version is required");
        }
        if(isNullOrEmpty(metadata.getVendor())) {
            errors.add("Plugin vendor is required");
        }
        if(isNullOrEmpty(metadata.getDescription())) {
            warnings.add("Plugin description is recommended");
        }

        // Validate plugin ID format
        if(metadata.getId() != null && !metadata.getId().matches("^[a - zA - Z0-9 - _.] + $")) {
            errors.add("Plugin ID must contain only alphanumeric characters, hyphens, dots, and underscores");
        }

        // Validate handlers
        InboundHandler inboundHandler = plugin.getInboundHandler();
        OutboundHandler outboundHandler = plugin.getOutboundHandler();

        if(inboundHandler == null && outboundHandler == null) {
            errors.add("Plugin must implement at least one handler(inbound or outbound)");
        }

        // Validate configuration schema if provided
        ConfigurationSchema schema = plugin.getConfigurationSchema();
        if(schema != null) {
            validateConfigurationSchema(schema, errors, warnings);
        }

        // Test basic plugin operations
        try {
            // Test that plugin can be initialized
            plugin.initialize(new java.util.HashMap<>());

            // Test connection test method
            if(inboundHandler != null) {
                ConnectionTestResult result = plugin.testConnection(AdapterPlugin.Direction.INBOUND);
                if(result == null) {
                    warnings.add("testConnection(INBOUND) returned null");
                }
            }

            if(outboundHandler != null) {
                ConnectionTestResult result = plugin.testConnection(AdapterPlugin.Direction.OUTBOUND);
                if(result == null) {
                    warnings.add("testConnection(OUTBOUND) returned null");
                }
            }

            // Test health check
            HealthStatus health = plugin.checkHealth();
            if(health == null) {
                warnings.add("checkHealth() returned null");
            }

            // Clean up
            plugin.destroy();

        } catch(Exception e) {
            errors.add("Plugin initialization test failed: " + e.getMessage());
            logger.error("Plugin validation error", e);
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    private void validateConfigurationSchema(ConfigurationSchema schema,
                                           List<String> errors,
                                           List<String> warnings) {
        // Validate schema structure
        if(schema.getSections() == null || schema.getSections().isEmpty()) {
            warnings.add("Configuration schema has no sections defined");
        } else {
            for(ConfigurationSchema.Section section : schema.getSections()) {
                if(isNullOrEmpty(section.getId())) {
                    errors.add("Configuration section ID is required");
                }
                if(isNullOrEmpty(section.getTitle())) {
                    warnings.add("Configuration section title is recommended");
                }
                if(section.getFields() == null || section.getFields().isEmpty()) {
                    warnings.add("Configuration section '" + section.getId() + "' has no fields");
                } else {
                    for(ConfigurationSchema.Field field : section.getFields()) {
                        validateField(field, errors, warnings);
                    }
                }
            }
        }
    }

    private void validateField(ConfigurationSchema.Field field,
                             List<String> errors,
                             List<String> warnings) {
        if(isNullOrEmpty(field.getName())) {
            errors.add("Field name is required");
        }
        if(isNullOrEmpty(field.getType())) {
            errors.add("Field type is required for field '" + field.getName() + "'");
        }
        if(isNullOrEmpty(field.getLabel())) {
            warnings.add("Field label is recommended for field '" + field.getName() + "'");
        }

        // Validate field type
        if(field.getType() != null) {
            String type = field.getType();
            List<String> validTypes = List.of(
                "text", "password", "number", "boolean", "select", "multiselect",
                "textarea", "json", "keyvalue", "file", "date", "time", "datetime"
           );
            if(!validTypes.contains(type)) {
                warnings.add("Unknown field type '" + type + "' for field '" + field.getName() + "'");
            }
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
