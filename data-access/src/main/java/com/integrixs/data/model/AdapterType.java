package com.integrixs.data.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "adapter_types")
public class AdapterType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AdapterCategory category;

    @Column(length = 100)
    private String vendor;

    @Column(length = 20)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String icon;

    // Direction support
    @Column(name = "supports_inbound")
    private boolean supportsInbound = false;

    @Column(name = "supports_outbound")
    private boolean supportsOutbound = false;

    @Column(name = "supports_bidirectional")
    private boolean supportsBidirectional = false;

    // Configuration schemas
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inbound_config_schema", columnDefinition = "jsonb")
    private Map<String, Object> inboundConfigSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outbound_config_schema", columnDefinition = "jsonb")
    private Map<String, Object> outboundConfigSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "common_config_schema", columnDefinition = "jsonb")
    private Map<String, Object> commonConfigSchema;

    // Capabilities and metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> capabilities;

    @Column(name = "supported_protocols", columnDefinition = "TEXT[]")
    private String[] supportedProtocols;

    @Column(name = "supported_formats", columnDefinition = "TEXT[]")
    private String[] supportedFormats;

    @Column(name = "authentication_methods", columnDefinition = "TEXT[]")
    private String[] authenticationMethods;

    // Documentation and support
    @Column(name = "documentation_url")
    private String documentationUrl;

    @Column(name = "support_url")
    private String supportUrl;

    @Column(name = "pricing_tier", length = 50)
    private String pricingTier;

    // Status
    @Column(length = 50)
    private String status = "active";

    @Column(name = "is_certified")
    private boolean isCertified = false;

    @Column(name = "certification_date")
    private LocalDateTime certificationDate;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
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
