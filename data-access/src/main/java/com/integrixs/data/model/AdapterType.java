package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "adapter_types")
@Getter
@Setter
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
}