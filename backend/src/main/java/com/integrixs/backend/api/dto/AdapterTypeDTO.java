package com.integrixs.backend.api.dto;

import com.integrixs.data.model.AdapterType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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

    // Default constructor
    public AdapterTypeDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public boolean isSupportsInbound() {
        return supportsInbound;
    }

    public void setSupportsInbound(boolean supportsInbound) {
        this.supportsInbound = supportsInbound;
    }

    public boolean isSupportsOutbound() {
        return supportsOutbound;
    }

    public void setSupportsOutbound(boolean supportsOutbound) {
        this.supportsOutbound = supportsOutbound;
    }

    public boolean isSupportsBidirectional() {
        return supportsBidirectional;
    }

    public void setSupportsBidirectional(boolean supportsBidirectional) {
        this.supportsBidirectional = supportsBidirectional;
    }

    public Map<String, Object> getInboundConfigSchema() {
        return inboundConfigSchema;
    }

    public void setInboundConfigSchema(Map<String, Object> inboundConfigSchema) {
        this.inboundConfigSchema = inboundConfigSchema;
    }

    public Map<String, Object> getOutboundConfigSchema() {
        return outboundConfigSchema;
    }

    public void setOutboundConfigSchema(Map<String, Object> outboundConfigSchema) {
        this.outboundConfigSchema = outboundConfigSchema;
    }

    public Map<String, Object> getCommonConfigSchema() {
        return commonConfigSchema;
    }

    public void setCommonConfigSchema(Map<String, Object> commonConfigSchema) {
        this.commonConfigSchema = commonConfigSchema;
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
    }

    public String[] getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(String[] supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    public String[] getSupportedFormats() {
        return supportedFormats;
    }

    public void setSupportedFormats(String[] supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    public String[] getAuthenticationMethods() {
        return authenticationMethods;
    }

    public void setAuthenticationMethods(String[] authenticationMethods) {
        this.authenticationMethods = authenticationMethods;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    public String getPricingTier() {
        return pricingTier;
    }

    public void setPricingTier(String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCertified() {
        return isCertified;
    }

    public void setIsCertified(boolean isCertified) {
        this.isCertified = isCertified;
    }

    public LocalDateTime getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(LocalDateTime certificationDate) {
        this.certificationDate = certificationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Static factory method to convert from entity
    public static AdapterTypeDTO from(AdapterType adapterType) {
        AdapterTypeDTO dto = new AdapterTypeDTO();
        dto.setId(adapterType.getId());
        dto.setCode(adapterType.getCode());
        dto.setName(adapterType.getName());
        if (adapterType.getCategory() != null) {
            dto.setCategoryId(adapterType.getCategory().getId());
            dto.setCategoryName(adapterType.getCategory().getName());
        }
        dto.setVendor(adapterType.getVendor());
        dto.setVersion(adapterType.getVersion());
        dto.setDescription(adapterType.getDescription());
        dto.setIcon(adapterType.getIcon());
        dto.setSupportsInbound(adapterType.isSupportsInbound());
        dto.setSupportsOutbound(adapterType.isSupportsOutbound());
        dto.setSupportsBidirectional(adapterType.isSupportsBidirectional());
        dto.setInboundConfigSchema(adapterType.getInboundConfigSchema());
        dto.setOutboundConfigSchema(adapterType.getOutboundConfigSchema());
        dto.setCommonConfigSchema(adapterType.getCommonConfigSchema());
        dto.setCapabilities(adapterType.getCapabilities());
        dto.setSupportedProtocols(adapterType.getSupportedProtocols());
        dto.setSupportedFormats(adapterType.getSupportedFormats());
        dto.setAuthenticationMethods(adapterType.getAuthenticationMethods());
        dto.setDocumentationUrl(adapterType.getDocumentationUrl());
        dto.setSupportUrl(adapterType.getSupportUrl());
        dto.setPricingTier(adapterType.getPricingTier());
        dto.setStatus(adapterType.getStatus());
        dto.setIsCertified(adapterType.isCertified());
        dto.setCertificationDate(adapterType.getCertificationDate());
        dto.setCreatedAt(adapterType.getCreatedAt());
        dto.setUpdatedAt(adapterType.getUpdatedAt());
        return dto;
    }
}
