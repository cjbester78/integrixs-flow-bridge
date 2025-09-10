package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for external authentication configurations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAuthenticationDTO {
    
    private String id;
    private String name;
    private String description;
    private String authType; // BASIC, OAUTH2, API_KEY, OAUTH1
    private String businessComponentId;
    private String businessComponentName;
    
    // Basic Auth fields
    private String username;
    private String password; // Only used for create/update, never returned
    private String realm;
    private boolean hasPassword; // Indicates if password is set
    
    // OAuth 2.0 fields
    private String clientId;
    private String clientSecret; // Only used for create/update, never returned
    private String tokenEndpoint;
    private String authorizationEndpoint;
    private String redirectUri;
    private String scopes;
    private String grantType;
    private String accessToken; // Only used for create/update, never returned
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    private boolean hasClientSecret;
    private boolean hasAccessToken;
    private boolean hasRefreshToken;
    private boolean tokenValid;
    
    // API Key fields
    private String apiKey; // Only used for create/update, never returned
    private String apiKeyHeader;
    private String apiKeyPrefix;
    private Integer rateLimit;
    private Integer rateLimitWindowSeconds;
    private boolean hasApiKey;
    
    // OAuth 1.0 fields
    private String consumerKey;
    private String consumerSecret; // Only used for create/update, never returned
    private String oauth1Token;
    private String oauth1TokenSecret; // Only used for create/update, never returned
    private String oauth1SignatureMethod; // HMAC-SHA1, HMAC-SHA256, PLAINTEXT
    private String oauth1Realm;
    private boolean hasConsumerSecret;
    private boolean hasOauth1TokenSecret;
    
    // HMAC fields
    private String hmacAlgorithm;
    private String hmacSecretKey; // Only used for create/update, never returned
    private String hmacHeaderName;
    private boolean hmacIncludeTimestamp;
    private boolean hmacIncludeNonce;
    private boolean hasHmacSecretKey;
    
    // Certificate fields
    private String certificatePath;
    private String certificatePassword; // Only used for create/update, never returned
    private String certificateType;
    private String trustStorePath;
    private String trustStorePassword; // Only used for create/update, never returned
    private boolean hasCertificatePassword;
    private boolean hasTrustStorePassword;
    
    // Custom header fields
    private Map<String, String> customHeaders;
    
    // Status fields
    private boolean active;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private Long errorCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}