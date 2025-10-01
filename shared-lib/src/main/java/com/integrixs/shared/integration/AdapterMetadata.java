package com.integrixs.shared.integration;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Metadata about an adapter
 */
public class AdapterMetadata {
    private String adapterId;
    private String name;
    private String type;
    private String version;
    private String description;
    private List<String> supportedOperations;
    private Map<String, String> requiredConfig;
    private Map<String, String> optionalConfig;
    private boolean active;
    private String iconUrl;

    // Default constructor
    public AdapterMetadata() {
    }

    // All args constructor
    public AdapterMetadata(String adapterId, String name, String type, String version,
                          String description, List<String> supportedOperations,
                          Map<String, String> requiredConfig, Map<String, String> optionalConfig,
                          boolean active, String iconUrl) {
        this.adapterId = adapterId;
        this.name = name;
        this.type = type;
        this.version = version;
        this.description = description;
        this.supportedOperations = supportedOperations;
        this.requiredConfig = requiredConfig;
        this.optionalConfig = optionalConfig;
        this.active = active;
        this.iconUrl = iconUrl;
    }

    // Getters
    public String getAdapterId() {
        return adapterId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSupportedOperations() {
        return supportedOperations;
    }

    public Map<String, String> getRequiredConfig() {
        return requiredConfig;
    }

    public Map<String, String> getOptionalConfig() {
        return optionalConfig;
    }

    public boolean isActive() {
        return active;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    // Setters
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSupportedOperations(List<String> supportedOperations) {
        this.supportedOperations = supportedOperations;
    }

    public void setRequiredConfig(Map<String, String> requiredConfig) {
        this.requiredConfig = requiredConfig;
    }

    public void setOptionalConfig(Map<String, String> optionalConfig) {
        this.optionalConfig = optionalConfig;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterId;
        private String name;
        private String type;
        private String version;
        private String description;
        private List<String> supportedOperations;
        private Map<String, String> requiredConfig;
        private Map<String, String> optionalConfig;
        private boolean active;
        private String iconUrl;

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
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

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder supportedOperations(List<String> supportedOperations) {
            this.supportedOperations = supportedOperations;
            return this;
        }

        public Builder requiredConfig(Map<String, String> requiredConfig) {
            this.requiredConfig = requiredConfig;
            return this;
        }

        public Builder optionalConfig(Map<String, String> optionalConfig) {
            this.optionalConfig = optionalConfig;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public AdapterMetadata build() {
            return new AdapterMetadata(adapterId, name, type, version, description,
                                     supportedOperations, requiredConfig, optionalConfig,
                                     active, iconUrl);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterMetadata that = (AdapterMetadata) o;
        return active == that.active &&
               Objects.equals(adapterId, that.adapterId) &&
               Objects.equals(name, that.name) &&
               Objects.equals(type, that.type) &&
               Objects.equals(version, that.version) &&
               Objects.equals(description, that.description) &&
               Objects.equals(supportedOperations, that.supportedOperations) &&
               Objects.equals(requiredConfig, that.requiredConfig) &&
               Objects.equals(optionalConfig, that.optionalConfig) &&
               Objects.equals(iconUrl, that.iconUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adapterId, name, type, version, description, supportedOperations,
                          requiredConfig, optionalConfig, active, iconUrl);
    }

    @Override
    public String toString() {
        return "AdapterMetadata{" +
                "adapterId='" + adapterId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", supportedOperations=" + supportedOperations +
                ", requiredConfig=" + requiredConfig +
                ", optionalConfig=" + optionalConfig +
                ", active=" + active +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }
}
