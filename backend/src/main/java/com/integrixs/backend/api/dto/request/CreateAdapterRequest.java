package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request object for creating a communication adapter
 */
public class CreateAdapterRequest {

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

    @NotBlank(message = "Business component ID is required")
    private String businessComponentId;

    private String externalAuthId;

    private boolean active = true;

    // Default constructor
    public CreateAdapterRequest() {
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

    // Builder
    public static CreateAdapterRequestBuilder builder() {
        return new CreateAdapterRequestBuilder();
    }

    public static class CreateAdapterRequestBuilder {
        private String name;
        private String type;
        private String mode;
        private String direction;
        private String configuration;
        private String description;
        private String businessComponentId;
        private String externalAuthId;
        private boolean active = true;

        public CreateAdapterRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateAdapterRequestBuilder type(String type) {
            this.type = type;
            return this;
        }

        public CreateAdapterRequestBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public CreateAdapterRequestBuilder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public CreateAdapterRequestBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public CreateAdapterRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateAdapterRequestBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public CreateAdapterRequestBuilder externalAuthId(String externalAuthId) {
            this.externalAuthId = externalAuthId;
            return this;
        }

        public CreateAdapterRequestBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public CreateAdapterRequest build() {
            CreateAdapterRequest request = new CreateAdapterRequest();
            request.setName(this.name);
            request.setType(this.type);
            request.setMode(this.mode);
            request.setDirection(this.direction);
            request.setConfiguration(this.configuration);
            request.setDescription(this.description);
            request.setBusinessComponentId(this.businessComponentId);
            request.setExternalAuthId(this.externalAuthId);
            request.setActive(this.active);
            return request;
        }
    }
}
