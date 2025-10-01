package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class AdapterType {

    private UUID id;

    private String code;

    private String name;

    private AdapterCategory category;

    private String vendor;

    private String version;

    private String description;

    private String icon;

    // Direction support
    private boolean supportsInbound = false;

    private boolean supportsOutbound = false;

    private boolean supportsBidirectional = false;

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
    private String status = "active";

    private boolean isCertified = false;

    private LocalDateTime certificationDate;

    // Audit
        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

    private User createdBy;

    private User updatedBy;

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

    public AdapterCategory getCategory() {
        return category;
    }

    public void setCategory(AdapterCategory category) {
        this.category = category;
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

    public void setCertified(boolean isCertified) {
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }
}
