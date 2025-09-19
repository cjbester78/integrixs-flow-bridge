package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request object for updating a communication adapter
 */
public class UpdateAdapterRequest {

    @NotBlank(message = "Adapter name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Adapter type is required")
    private String type;

    @NotBlank(message = "Adapter mode is required")
    private String mode;

    private String direction;

    @NotNull(message = "Configuration is required")
    private String configuration;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String businessComponentId;

    private String externalAuthId;

    private boolean active;

    // Default constructor
    public UpdateAdapterRequest() {
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

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
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

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
