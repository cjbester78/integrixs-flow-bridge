package com.integrixs.backend.plugin.api;

import java.util.List;
import java.util.Map;

/**
 * Metadata describing an adapter plugin
 */
public class AdapterMetadata {

    /**
     * Unique identifier for the adapter
     */
    private String id;

    /**
     * Display name of the adapter
     */
    private String name;

    /**
     * Adapter version
     */
    private String version;

    /**
     * Vendor/author of the adapter
     */
    private String vendor;

    /**
     * Description of what this adapter does
     */
    private String description;

    /**
     * Icon name or path
     */
    private String icon;

    /**
     * Category for marketplace organization
     */
    private String category;

    /**
     * Supported protocols
     */
    private List<String> supportedProtocols;

    /**
     * Supported data formats
     */
    private List<String> supportedFormats;

    /**
     * Authentication methods supported
     */
    private List<String> authenticationMethods;

    /**
     * Capabilities of this adapter
     */
    private Map<String, Boolean> capabilities;

    /**
     * Documentation URL
     */
    private String documentationUrl;

    /**
     * Minimum required platform version
     */
    private String minPlatformVersion;

    /**
     * Maximum supported platform version
     */
    private String maxPlatformVersion;

    /**
     * License information
     */
    private String license;

    /**
     * Tags for search and filtering
     */
    private List<String> tags;

    // Getters and Setters
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

    public Map<String, Boolean> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Boolean> capabilities) {
        this.capabilities = capabilities;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getMinPlatformVersion() {
        return minPlatformVersion;
    }

    public void setMinPlatformVersion(String minPlatformVersion) {
        this.minPlatformVersion = minPlatformVersion;
    }

    public String getMaxPlatformVersion() {
        return maxPlatformVersion;
    }

    public void setMaxPlatformVersion(String maxPlatformVersion) {
        this.maxPlatformVersion = maxPlatformVersion;
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

    // Builder pattern
    public static AdapterMetadataBuilder builder() {
        return new AdapterMetadataBuilder();
    }

    public static class AdapterMetadataBuilder {
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
        private Map<String, Boolean> capabilities;
        private String documentationUrl;
        private String minPlatformVersion;
        private String maxPlatformVersion;
        private String license;
        private List<String> tags;

        public AdapterMetadataBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AdapterMetadataBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AdapterMetadataBuilder version(String version) {
            this.version = version;
            return this;
        }

        public AdapterMetadataBuilder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public AdapterMetadataBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AdapterMetadataBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public AdapterMetadataBuilder category(String category) {
            this.category = category;
            return this;
        }

        public AdapterMetadataBuilder supportedProtocols(List<String> supportedProtocols) {
            this.supportedProtocols = supportedProtocols;
            return this;
        }

        public AdapterMetadataBuilder supportedFormats(List<String> supportedFormats) {
            this.supportedFormats = supportedFormats;
            return this;
        }

        public AdapterMetadataBuilder authenticationMethods(List<String> authenticationMethods) {
            this.authenticationMethods = authenticationMethods;
            return this;
        }

        public AdapterMetadataBuilder capabilities(Map<String, Boolean> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public AdapterMetadataBuilder documentationUrl(String documentationUrl) {
            this.documentationUrl = documentationUrl;
            return this;
        }

        public AdapterMetadataBuilder minPlatformVersion(String minPlatformVersion) {
            this.minPlatformVersion = minPlatformVersion;
            return this;
        }

        public AdapterMetadataBuilder maxPlatformVersion(String maxPlatformVersion) {
            this.maxPlatformVersion = maxPlatformVersion;
            return this;
        }

        public AdapterMetadataBuilder license(String license) {
            this.license = license;
            return this;
        }

        public AdapterMetadataBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public AdapterMetadata build() {
            AdapterMetadata metadata = new AdapterMetadata();
            metadata.id = this.id;
            metadata.name = this.name;
            metadata.version = this.version;
            metadata.vendor = this.vendor;
            metadata.description = this.description;
            metadata.icon = this.icon;
            metadata.category = this.category;
            metadata.supportedProtocols = this.supportedProtocols;
            metadata.supportedFormats = this.supportedFormats;
            metadata.authenticationMethods = this.authenticationMethods;
            metadata.capabilities = this.capabilities;
            metadata.documentationUrl = this.documentationUrl;
            metadata.minPlatformVersion = this.minPlatformVersion;
            metadata.maxPlatformVersion = this.maxPlatformVersion;
            metadata.license = this.license;
            metadata.tags = this.tags;
            return metadata;
        }
    }
}
