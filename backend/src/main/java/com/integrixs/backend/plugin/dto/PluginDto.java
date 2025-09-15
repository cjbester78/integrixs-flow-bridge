package com.integrixs.backend.plugin.dto;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * DTO for basic plugin information
 */
@Data
@Builder
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
}
