package com.integrixs.shared.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Metadata about an adapter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
