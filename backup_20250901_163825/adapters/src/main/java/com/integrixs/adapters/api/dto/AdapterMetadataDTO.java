package com.integrixs.adapters.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for adapter metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterMetadataDTO {
    private String adapterName;
    private String adapterType;
    private String adapterMode;
    private String version;
    private String description;
    private List<String> supportedOperations;
    private List<String> requiredProperties;
    private List<String> optionalProperties;
    private Map<String, String> capabilities;
    private boolean supportsAsync;
    private boolean supportsBatch;
    private boolean supportsStreaming;
    private Map<String, String> propertyDescriptions;
    private Map<String, Object> defaultPropertyValues;
}