package com.integrixs.backend.plugin.api;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Metadata describing an adapter plugin
 */
@Data
@Builder
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
}