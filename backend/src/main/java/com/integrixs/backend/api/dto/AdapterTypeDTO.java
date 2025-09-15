package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdapterTypeDTO {
    private UUID id;
    private String code;
    private String name;
    private UUID categoryId;
    private String categoryName;
    private String vendor;
    private String version;
    private String description;
    private String icon;

    // Direction support
    private boolean supportsInbound;
    private boolean supportsOutbound;
    private boolean supportsBidirectional;

    // Configuration schemas
    private Map<String, Object> inboundConfigSchema;
    private Map<String, Object> outboundConfigSchema;
    private Map<String, Object> commonConfigSchema;

    // Capabilities and metadata
    private Map<String, Object> capabilities;
    private String[] supportedProtocols;
    private String[] supportedFormats;
    private String[] authenticationMethods;

    // Documentation and support
    private String documentationUrl;
    private String supportUrl;
    private String pricingTier;

    // Status
    private String status;
    private boolean isCertified;
    private LocalDateTime certificationDate;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
