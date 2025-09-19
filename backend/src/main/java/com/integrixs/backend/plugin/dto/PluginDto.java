package com.integrixs.backend.plugin.dto;

import com.integrixs.backend.plugin.api.AdapterMetadata;
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
                .capabilities(metadata.getCapabilities())
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
}
