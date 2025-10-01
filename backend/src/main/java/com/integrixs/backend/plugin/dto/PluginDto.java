package com.integrixs.backend.plugin.dto;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for basic plugin information
 */
public class PluginDto {
    private String id;
    private String name;
    private String version;
    private String vendor;
    private String description;
    private String icon;
    private String category;
    private List<String> supportedProtocols;
    private List<String> supportedFormats;
    private List<String> authenticationMethods;
    private Map<String, Object> capabilities;
    private String documentationUrl;
    private String license;
    private List<String> tags;

    public static PluginDto fromMetadata(AdapterMetadata metadata) {
        return PluginDto.builder()
                .id(metadata.getId())
                .name(metadata.getName())
                .version(metadata.getVersion())
                .vendor(metadata.getVendor())
                .description(metadata.getDescription())
                .icon(metadata.getIcon())
                .category(metadata.getCategory())
                .supportedProtocols(metadata.getSupportedProtocols())
                .supportedFormats(metadata.getSupportedFormats())
                .authenticationMethods(metadata.getAuthenticationMethods())
                .capabilities(metadata.getCapabilities() != null ?
                    new HashMap<>(metadata.getCapabilities()) : null)
                .documentationUrl(metadata.getDocumentationUrl())
                .license(metadata.getLicense())
                .tags(metadata.getTags())
                .build();
    }

    // Default constructor
    public PluginDto() {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(List<String> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    public List<String> getSupportedFormats() {
        return supportedFormats;
    }

    public void setSupportedFormats(List<String> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    public List<String> getAuthenticationMethods() {
        return authenticationMethods;
    }

    public void setAuthenticationMethods(List<String> authenticationMethods) {
        this.authenticationMethods = authenticationMethods;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
    }

    // Builder pattern
    public static PluginDtoBuilder builder() {
        return new PluginDtoBuilder();
    }

    public static class PluginDtoBuilder {
        private String id;
        private String name;
        private String version;
        private String vendor;
        private String description;
        private String icon;
        private String category;
        private List<String> supportedProtocols;
        private List<String> supportedFormats;
        private List<String> authenticationMethods;
        private Map<String, Object> capabilities;
        private String documentationUrl;
        private String license;
        private List<String> tags;

        public PluginDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PluginDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PluginDtoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public PluginDtoBuilder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public PluginDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PluginDtoBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public PluginDtoBuilder category(String category) {
            this.category = category;
            return this;
        }

        public PluginDtoBuilder supportedProtocols(List<String> supportedProtocols) {
            this.supportedProtocols = supportedProtocols;
            return this;
        }

        public PluginDtoBuilder supportedFormats(List<String> supportedFormats) {
            this.supportedFormats = supportedFormats;
            return this;
        }

        public PluginDtoBuilder authenticationMethods(List<String> authenticationMethods) {
            this.authenticationMethods = authenticationMethods;
            return this;
        }

        public PluginDtoBuilder capabilities(Map<String, Object> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public PluginDtoBuilder documentationUrl(String documentationUrl) {
            this.documentationUrl = documentationUrl;
            return this;
        }

        public PluginDtoBuilder license(String license) {
            this.license = license;
            return this;
        }

        public PluginDtoBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public PluginDto build() {
            PluginDto dto = new PluginDto();
            dto.id = this.id;
            dto.name = this.name;
            dto.version = this.version;
            dto.vendor = this.vendor;
            dto.description = this.description;
            dto.icon = this.icon;
            dto.category = this.category;
            dto.supportedProtocols = this.supportedProtocols;
            dto.supportedFormats = this.supportedFormats;
            dto.authenticationMethods = this.authenticationMethods;
            dto.capabilities = this.capabilities;
            dto.documentationUrl = this.documentationUrl;
            dto.license = this.license;
            dto.tags = this.tags;
            return dto;
        }
    }
}
