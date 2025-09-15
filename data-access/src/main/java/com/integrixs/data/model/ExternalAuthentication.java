package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing external authentication configurations for adapters.
 * Stores credentials and configuration for various authentication types.
 */
@Entity
@Table(name = "external_authentications", indexes = {
    @Index(name = "idx_external_auth_name", columnList = "name"),
    @Index(name = "idx_external_auth_type", columnList = "auth_type"),
    @Index(name = "idx_external_auth_user", columnList = "created_by_id"),
    @Index(name = "idx_external_auth_business_component", columnList = "business_component_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"createdBy", "businessComponent", "encryptedPassword", "encryptedClientSecret", "encryptedAccessToken", "encryptedApiKey"})
public class ExternalAuthentication {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Name of the authentication configuration
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of the authentication configuration
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Type of authentication
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 20)
    private AuthType authType;

    /**
     * Business component this authentication belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id", nullable = false)
    private BusinessComponent businessComponent;

    // Basic Authentication fields
    @Column(length = 255)
    private String username;

    @Column(name = "encrypted_password", columnDefinition = "TEXT")
    private String encryptedPassword;

    @Column(length = 100)
    private String realm;

    // OAuth 1.0 fields
    @Column(name = "consumer_key", length = 255)
    private String consumerKey;

    @Column(name = "consumer_secret", columnDefinition = "TEXT")
    private String consumerSecret;

    @Column(name = "oauth1_token", length = 500)
    private String oauth1Token;

    @Column(name = "oauth1_token_secret", columnDefinition = "TEXT")
    private String oauth1TokenSecret;

    @Column(name = "oauth1_signature_method", length = 50)
    @Builder.Default
    private String oauth1SignatureMethod = "HMAC - SHA1";

    @Column(name = "oauth1_realm", length = 255)
    private String oauth1Realm;

    // OAuth 2.0 fields
    @Column(name = "client_id", length = 255)
    private String clientId;

    @Column(name = "encrypted_client_secret", columnDefinition = "TEXT")
    private String encryptedClientSecret;

    @Column(name = "token_endpoint", length = 500)
    private String tokenEndpoint;

    @Column(name = "authorization_endpoint", length = 500)
    private String authorizationEndpoint;

    @Column(name = "redirect_uri", length = 500)
    private String redirectUri;

    @Column(columnDefinition = "TEXT")
    private String scopes;

    @Column(name = "grant_type", length = 50)
    private String grantType;

    @Column(name = "encrypted_access_token", columnDefinition = "TEXT")
    private String encryptedAccessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    // API Key fields
    @Column(name = "encrypted_api_key", columnDefinition = "TEXT")
    private String encryptedApiKey;

    @Column(name = "api_key_header", length = 100)
    private String apiKeyHeader;

    @Column(name = "api_key_prefix", length = 50)
    private String apiKeyPrefix;

    // Rate limiting fields
    @Column(name = "rate_limit")
    private Integer rateLimit;

    @Column(name = "rate_limit_window_seconds")
    private Integer rateLimitWindowSeconds;

    // HMAC fields
    @Column(name = "hmac_algorithm", length = 50)
    private String hmacAlgorithm;

    @Column(name = "hmac_secret_key", columnDefinition = "TEXT")
    private String encryptedHmacSecretKey;

    @Column(name = "hmac_header_name", length = 100)
    private String hmacHeaderName;

    @Column(name = "hmac_include_timestamp")
    private Boolean hmacIncludeTimestamp;

    @Column(name = "hmac_include_nonce")
    private Boolean hmacIncludeNonce;

    // Certificate fields
    @Column(name = "certificate_path", length = 500)
    private String certificatePath;

    @Column(name = "certificate_password", columnDefinition = "TEXT")
    private String encryptedCertificatePassword;

    @Column(name = "certificate_type", length = 50)
    private String certificateType;

    @Column(name = "trust_store_path", length = 500)
    private String trustStorePath;

    @Column(name = "trust_store_password", columnDefinition = "TEXT")
    private String encryptedTrustStorePassword;

    // Custom header fields
    @ElementCollection
    @CollectionTable(name = "external_auth_custom_headers",
                     joinColumns = @JoinColumn(name = "auth_id"))
    @MapKeyColumn(name = "header_name", length = 100)
    @Column(name = "header_value", columnDefinition = "TEXT")
    private Map<String, String> customHeaders = new HashMap<>();

    // Status fields
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    @Column(name = "error_count")
    @Builder.Default
    private Long errorCount = 0L;

    /**
     * User who created this authentication
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /**
     * Timestamp when created
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp when last updated
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Authentication types
     */
    public enum AuthType {
        BASIC,
        OAUTH1,
        OAUTH2,
        API_KEY,
        CUSTOM,
        HMAC,
        CERTIFICATE,
        CUSTOM_HEADER
    }

    /**
     * Check if token needs refresh(OAuth2)
     */
    public boolean needsTokenRefresh() {
        if(authType != AuthType.OAUTH2 || tokenExpiresAt == null) {
            return false;
        }
        // Refresh if token expires in less than 5 minutes
        return LocalDateTime.now().plusMinutes(5).isAfter(tokenExpiresAt);
    }
}
