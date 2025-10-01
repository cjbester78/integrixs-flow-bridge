package com.integrixs.shared.dto.adapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Data Transfer Object for Adapter Configuration.
 *
 * <p>This DTO represents the configuration for communication adapters in the integration platform.
 * It follows the REVERSED middleware convention where:</p>
 * <ul>
 *   <li><b>INBOUND</b> adapters receive data FROM external systems(but are considered OUTBOUND)</li>
 *   <li><b>OUTBOUND</b> adapters send data TO external systems(but are considered INBOUND)</li>
 * </ul>
 *
 * <p>The adapter configuration includes both the adapter metadata and its specific
 * configuration stored as JSON for flexibility across different adapter types.</p>
 *
 * @author Integration Team
 * @since 1.0.0
 * @see com.integrixs.adapters.core.AdapterMode
 * @see com.integrixs.adapters.core.AdapterType
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdapterConfigDTO {

    /**
     * Unique identifier for the adapter.
     */
    private String id;

    /**
     * Name of the adapter configuration.
     * Should be unique within a business component.
     */
    private String name;

    /**
     * Type of the adapter(HTTP, JDBC, FILE, etc.).
     * @see com.integrixs.shared.enums.AdapterType
     */
    private String type;

    /**
     * Adapter mode determining data flow direction(reversed convention).
     * INBOUND = receives FROM external systems(but is OUTBOUND)
     * OUTBOUND = sends TO external systems(but is INBOUND)
     */
    private String mode;

    /**
     * JSON string containing adapter - specific configuration.
     * Structure varies based on adapter type.
     */
    private String configJson;

    /**
     * Human - readable description of the adapter's purpose.
     */
    private String description;

    /**
     * Indicates if the adapter is currently active.
     * Inactive adapters are not available for flow execution.
     */
    private boolean active = true;

    /**
     * Alternative direction indicator for clarity(reversed convention).
     * OUTBOUND = INBOUND mode(receives from external)
     * INBOUND = OUTBOUND mode(sends to external)
     * @deprecated Use {@link #mode} instead for consistency
     */
    private String direction;

    /**
     * ID of the business component this adapter belongs to.
     */
    private String businessComponentId;

    /**
     * Name of the business component this adapter belongs to.
     * Used for display purposes in the UI.
     */
    private String businessComponentName;

    /**
     * ID of the external authentication configuration.
     * Used for HTTP/HTTPS - based adapters.
     */
    private String externalAuthId;

    /**
     * Name of the external authentication configuration.
     * Used for display purposes in the UI.
     */
    private String externalAuthName;

    /**
     * Determines if this adapter receives data FROM external systems.
     * Due to reversed convention, INBOUND mode = OUTBOUND direction.
     *
     * @return true if adapter is in INBOUND mode or OUTBOUND direction
     */
    @JsonIgnore
    public boolean isSender() {
        return "INBOUND".equalsIgnoreCase(mode) || "OUTBOUND".equalsIgnoreCase(direction);
    }

    /**
     * Determines if this adapter sends data TO external systems.
     * Due to reversed convention, OUTBOUND mode = INBOUND direction.
     *
     * @return true if adapter is in OUTBOUND mode or INBOUND direction
     */
    @JsonIgnore
    public boolean isReceiver() {
        return "OUTBOUND".equalsIgnoreCase(mode) || "INBOUND".equalsIgnoreCase(direction);
    }

    // Default constructor
    public AdapterConfigDTO() {
    }

    // All args constructor
    public AdapterConfigDTO(String id, String name, String type, String mode, String configJson,
                           String description, boolean active, String direction,
                           String businessComponentId, String businessComponentName,
                           String externalAuthId, String externalAuthName) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.configJson = configJson;
        this.description = description;
        this.active = active;
        this.direction = direction;
        this.businessComponentId = businessComponentId;
        this.businessComponentName = businessComponentName;
        this.externalAuthId = externalAuthId;
        this.externalAuthName = externalAuthName;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getMode() {
        return mode;
    }

    public String getConfigJson() {
        return configJson;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public String getDirection() {
        return direction;
    }

    public String getBusinessComponentId() {
        return businessComponentId;
    }

    public String getBusinessComponentName() {
        return businessComponentName;
    }

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public String getExternalAuthName() {
        return externalAuthName;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setBusinessComponentId(String businessComponentId) {
        this.businessComponentId = businessComponentId;
    }

    public void setBusinessComponentName(String businessComponentName) {
        this.businessComponentName = businessComponentName;
    }

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public void setExternalAuthName(String externalAuthName) {
        this.externalAuthName = externalAuthName;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String type;
        private String mode;
        private String configJson;
        private String description;
        private boolean active = true;
        private String direction;
        private String businessComponentId;
        private String businessComponentName;
        private String externalAuthId;
        private String externalAuthName;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder configJson(String configJson) {
            this.configJson = configJson;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public Builder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public Builder businessComponentName(String businessComponentName) {
            this.businessComponentName = businessComponentName;
            return this;
        }

        public Builder externalAuthId(String externalAuthId) {
            this.externalAuthId = externalAuthId;
            return this;
        }

        public Builder externalAuthName(String externalAuthName) {
            this.externalAuthName = externalAuthName;
            return this;
        }

        public AdapterConfigDTO build() {
            return new AdapterConfigDTO(id, name, type, mode, configJson, description,
                                       active, direction, businessComponentId, businessComponentName,
                                       externalAuthId, externalAuthName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterConfigDTO that = (AdapterConfigDTO) o;
        return active == that.active &&
               Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(type, that.type) &&
               Objects.equals(mode, that.mode) &&
               Objects.equals(configJson, that.configJson) &&
               Objects.equals(description, that.description) &&
               Objects.equals(direction, that.direction) &&
               Objects.equals(businessComponentId, that.businessComponentId) &&
               Objects.equals(businessComponentName, that.businessComponentName) &&
               Objects.equals(externalAuthId, that.externalAuthId) &&
               Objects.equals(externalAuthName, that.externalAuthName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, mode, configJson, description, active, direction,
                          businessComponentId, businessComponentName, externalAuthId, externalAuthName);
    }

    @Override
    public String toString() {
        return "AdapterConfigDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", mode='" + mode + '\'' +
                ", configJson='" + configJson + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", direction='" + direction + '\'' +
                ", businessComponentId='" + businessComponentId + '\'' +
                ", businessComponentName='" + businessComponentName + '\'' +
                ", externalAuthId='" + externalAuthId + '\'' +
                ", externalAuthName='" + externalAuthName + '\'' +
                '}';
    }
}
