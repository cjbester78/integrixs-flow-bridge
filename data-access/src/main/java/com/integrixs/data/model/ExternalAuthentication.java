package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing external authentication configurations for adapters.
 * Stores credentials and configuration for various authentication types.
 */
public class ExternalAuthentication {

        private UUID id;

    /**
     * Name of the authentication configuration
     */
    private String name;

    /**
     * Description of the authentication configuration
     */
    private String description;

    /**
     * Type of authentication
     */
    private AuthType authType;

    /**
     * Business component this authentication belongs to
     */
    private BusinessComponent businessComponent;

    // Basic Authentication fields
    private String username;

    private String encryptedPassword;

    private String realm;

    // OAuth 1.0 fields
    private String consumerKey;

    private String consumerSecret;

    private String oauth1Token;

    private String oauth1TokenSecret;

    private String oauth1SignatureMethod = "HMAC - SHA1";

    private String oauth1Realm;

    // OAuth 2.0 fields
    private String clientId;

    private String encryptedClientSecret;

    private String tokenEndpoint;

    private String authorizationEndpoint;

    private String redirectUri;

    private String scopes;

    private String grantType;

    private String encryptedAccessToken;

    private String refreshToken;

    private LocalDateTime tokenExpiresAt;

    // API Key fields
    private String encryptedApiKey;

    private String apiKeyHeader;

    private String apiKeyPrefix;

    // Rate limiting fields
    private Integer rateLimit;

    private Integer rateLimitWindowSeconds;

    // HMAC fields
    private String hmacAlgorithm;

    private String encryptedHmacSecretKey;

    private String hmacHeaderName;

    private Boolean hmacIncludeTimestamp;

    private Boolean hmacIncludeNonce;

    // Certificate fields
    private String certificatePath;

    private String encryptedCertificatePassword;

    private String certificateType;

    private String trustStorePath;

    private String encryptedTrustStorePassword;

    // Custom header fields
    private Map<String, String> customHeaders = new HashMap<>();

    // Status fields
    private boolean isActive = true;

    private LocalDateTime lastUsedAt;

    private Long usageCount = 0L;

    private Long errorCount = 0L;

    /**
     * User who created this authentication
     */
    private User createdBy;

    /**
     * Timestamp when created
     */
        private LocalDateTime createdAt;

    /**
     * Timestamp when last updated
     */
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

    // Default constructor
    public ExternalAuthentication() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public BusinessComponent getBusinessComponent() {
        return businessComponent;
    }

    public void setBusinessComponent(BusinessComponent businessComponent) {
        this.businessComponent = businessComponent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getOauth1Token() {
        return oauth1Token;
    }

    public void setOauth1Token(String oauth1Token) {
        this.oauth1Token = oauth1Token;
    }

    public String getOauth1TokenSecret() {
        return oauth1TokenSecret;
    }

    public void setOauth1TokenSecret(String oauth1TokenSecret) {
        this.oauth1TokenSecret = oauth1TokenSecret;
    }

    public String getOauth1SignatureMethod() {
        return oauth1SignatureMethod;
    }

    public void setOauth1SignatureMethod(String oauth1SignatureMethod) {
        this.oauth1SignatureMethod = oauth1SignatureMethod;
    }

    public String getOauth1Realm() {
        return oauth1Realm;
    }

    public void setOauth1Realm(String oauth1Realm) {
        this.oauth1Realm = oauth1Realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getEncryptedClientSecret() {
        return encryptedClientSecret;
    }

    public void setEncryptedClientSecret(String encryptedClientSecret) {
        this.encryptedClientSecret = encryptedClientSecret;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getEncryptedAccessToken() {
        return encryptedAccessToken;
    }

    public void setEncryptedAccessToken(String encryptedAccessToken) {
        this.encryptedAccessToken = encryptedAccessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public String getEncryptedApiKey() {
        return encryptedApiKey;
    }

    public void setEncryptedApiKey(String encryptedApiKey) {
        this.encryptedApiKey = encryptedApiKey;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }

    public String getApiKeyPrefix() {
        return apiKeyPrefix;
    }

    public void setApiKeyPrefix(String apiKeyPrefix) {
        this.apiKeyPrefix = apiKeyPrefix;
    }

    public Integer getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Integer rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Integer getRateLimitWindowSeconds() {
        return rateLimitWindowSeconds;
    }

    public void setRateLimitWindowSeconds(Integer rateLimitWindowSeconds) {
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
    }

    public String getHmacAlgorithm() {
        return hmacAlgorithm;
    }

    public void setHmacAlgorithm(String hmacAlgorithm) {
        this.hmacAlgorithm = hmacAlgorithm;
    }

    public String getEncryptedHmacSecretKey() {
        return encryptedHmacSecretKey;
    }

    public void setEncryptedHmacSecretKey(String encryptedHmacSecretKey) {
        this.encryptedHmacSecretKey = encryptedHmacSecretKey;
    }

    public String getHmacHeaderName() {
        return hmacHeaderName;
    }

    public void setHmacHeaderName(String hmacHeaderName) {
        this.hmacHeaderName = hmacHeaderName;
    }

    public Boolean getHmacIncludeTimestamp() {
        return hmacIncludeTimestamp;
    }

    public void setHmacIncludeTimestamp(Boolean hmacIncludeTimestamp) {
        this.hmacIncludeTimestamp = hmacIncludeTimestamp;
    }

    public Boolean getHmacIncludeNonce() {
        return hmacIncludeNonce;
    }

    public void setHmacIncludeNonce(Boolean hmacIncludeNonce) {
        this.hmacIncludeNonce = hmacIncludeNonce;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getEncryptedCertificatePassword() {
        return encryptedCertificatePassword;
    }

    public void setEncryptedCertificatePassword(String encryptedCertificatePassword) {
        this.encryptedCertificatePassword = encryptedCertificatePassword;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getEncryptedTrustStorePassword() {
        return encryptedTrustStorePassword;
    }

    public void setEncryptedTrustStorePassword(String encryptedTrustStorePassword) {
        this.encryptedTrustStorePassword = encryptedTrustStorePassword;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    // Builder
    public static ExternalAuthenticationBuilder builder() {
        return new ExternalAuthenticationBuilder();
    }

    public static class ExternalAuthenticationBuilder {
        private UUID id;
        private String name;
        private String description;
        private AuthType authType;
        private BusinessComponent businessComponent;
        private String username;
        private String encryptedPassword;
        private String realm;
        private String consumerKey;
        private String consumerSecret;
        private String oauth1Token;
        private String oauth1TokenSecret;
        private String oauth1SignatureMethod;
        private String oauth1Realm;
        private String clientId;
        private String encryptedClientSecret;
        private String tokenEndpoint;
        private String authorizationEndpoint;
        private String redirectUri;
        private String scopes;
        private String grantType;
        private String encryptedAccessToken;
        private String refreshToken;
        private LocalDateTime tokenExpiresAt;
        private String encryptedApiKey;
        private String apiKeyHeader;
        private String apiKeyPrefix;
        private Integer rateLimit;
        private Integer rateLimitWindowSeconds;
        private String hmacAlgorithm;
        private String encryptedHmacSecretKey;
        private String hmacHeaderName;
        private Boolean hmacIncludeTimestamp;
        private Boolean hmacIncludeNonce;
        private String certificatePath;
        private String encryptedCertificatePassword;
        private String certificateType;
        private String trustStorePath;
        private String encryptedTrustStorePassword;
        private boolean isActive;
        private LocalDateTime lastUsedAt;
        private Long usageCount;
        private Long errorCount;
        private User createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ExternalAuthenticationBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ExternalAuthenticationBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ExternalAuthenticationBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ExternalAuthenticationBuilder authType(AuthType authType) {
            this.authType = authType;
            return this;
        }

        public ExternalAuthenticationBuilder businessComponent(BusinessComponent businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public ExternalAuthenticationBuilder username(String username) {
            this.username = username;
            return this;
        }

        public ExternalAuthenticationBuilder encryptedPassword(String encryptedPassword) {
            this.encryptedPassword = encryptedPassword;
            return this;
        }

        public ExternalAuthenticationBuilder realm(String realm) {
            this.realm = realm;
            return this;
        }

        public ExternalAuthenticationBuilder consumerKey(String consumerKey) {
            this.consumerKey = consumerKey;
            return this;
        }

        public ExternalAuthenticationBuilder consumerSecret(String consumerSecret) {
            this.consumerSecret = consumerSecret;
            return this;
        }

        public ExternalAuthenticationBuilder oauth1Token(String oauth1Token) {
            this.oauth1Token = oauth1Token;
            return this;
        }

        public ExternalAuthenticationBuilder oauth1TokenSecret(String oauth1TokenSecret) {
            this.oauth1TokenSecret = oauth1TokenSecret;
            return this;
        }

        public ExternalAuthenticationBuilder oauth1SignatureMethod(String oauth1SignatureMethod) {
            this.oauth1SignatureMethod = oauth1SignatureMethod;
            return this;
        }

        public ExternalAuthenticationBuilder oauth1Realm(String oauth1Realm) {
            this.oauth1Realm = oauth1Realm;
            return this;
        }

        public ExternalAuthenticationBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ExternalAuthenticationBuilder encryptedClientSecret(String encryptedClientSecret) {
            this.encryptedClientSecret = encryptedClientSecret;
            return this;
        }

        public ExternalAuthenticationBuilder tokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        public ExternalAuthenticationBuilder authorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
            return this;
        }

        public ExternalAuthenticationBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public ExternalAuthenticationBuilder scopes(String scopes) {
            this.scopes = scopes;
            return this;
        }

        public ExternalAuthenticationBuilder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public ExternalAuthenticationBuilder encryptedAccessToken(String encryptedAccessToken) {
            this.encryptedAccessToken = encryptedAccessToken;
            return this;
        }

        public ExternalAuthenticationBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public ExternalAuthenticationBuilder tokenExpiresAt(LocalDateTime tokenExpiresAt) {
            this.tokenExpiresAt = tokenExpiresAt;
            return this;
        }

        public ExternalAuthenticationBuilder encryptedApiKey(String encryptedApiKey) {
            this.encryptedApiKey = encryptedApiKey;
            return this;
        }

        public ExternalAuthenticationBuilder apiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
            return this;
        }

        public ExternalAuthenticationBuilder apiKeyPrefix(String apiKeyPrefix) {
            this.apiKeyPrefix = apiKeyPrefix;
            return this;
        }

        public ExternalAuthenticationBuilder rateLimit(Integer rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }

        public ExternalAuthenticationBuilder rateLimitWindowSeconds(Integer rateLimitWindowSeconds) {
            this.rateLimitWindowSeconds = rateLimitWindowSeconds;
            return this;
        }

        public ExternalAuthenticationBuilder hmacAlgorithm(String hmacAlgorithm) {
            this.hmacAlgorithm = hmacAlgorithm;
            return this;
        }

        public ExternalAuthenticationBuilder encryptedHmacSecretKey(String encryptedHmacSecretKey) {
            this.encryptedHmacSecretKey = encryptedHmacSecretKey;
            return this;
        }

        public ExternalAuthenticationBuilder hmacHeaderName(String hmacHeaderName) {
            this.hmacHeaderName = hmacHeaderName;
            return this;
        }

        public ExternalAuthenticationBuilder hmacIncludeTimestamp(Boolean hmacIncludeTimestamp) {
            this.hmacIncludeTimestamp = hmacIncludeTimestamp;
            return this;
        }

        public ExternalAuthenticationBuilder hmacIncludeNonce(Boolean hmacIncludeNonce) {
            this.hmacIncludeNonce = hmacIncludeNonce;
            return this;
        }

        public ExternalAuthenticationBuilder certificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
            return this;
        }

        public ExternalAuthenticationBuilder encryptedCertificatePassword(String encryptedCertificatePassword) {
            this.encryptedCertificatePassword = encryptedCertificatePassword;
            return this;
        }

        public ExternalAuthenticationBuilder certificateType(String certificateType) {
            this.certificateType = certificateType;
            return this;
        }

        public ExternalAuthenticationBuilder trustStorePath(String trustStorePath) {
            this.trustStorePath = trustStorePath;
            return this;
        }

        public ExternalAuthenticationBuilder encryptedTrustStorePassword(String encryptedTrustStorePassword) {
            this.encryptedTrustStorePassword = encryptedTrustStorePassword;
            return this;
        }

        public ExternalAuthenticationBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public ExternalAuthenticationBuilder lastUsedAt(LocalDateTime lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public ExternalAuthenticationBuilder usageCount(Long usageCount) {
            this.usageCount = usageCount;
            return this;
        }

        public ExternalAuthenticationBuilder errorCount(Long errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public ExternalAuthenticationBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public ExternalAuthenticationBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExternalAuthenticationBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ExternalAuthentication build() {
            ExternalAuthentication instance = new ExternalAuthentication();
            instance.setId(this.id);
            instance.setName(this.name);
            instance.setDescription(this.description);
            instance.setAuthType(this.authType);
            instance.setBusinessComponent(this.businessComponent);
            instance.setUsername(this.username);
            instance.setEncryptedPassword(this.encryptedPassword);
            instance.setRealm(this.realm);
            instance.setConsumerKey(this.consumerKey);
            instance.setConsumerSecret(this.consumerSecret);
            instance.setOauth1Token(this.oauth1Token);
            instance.setOauth1TokenSecret(this.oauth1TokenSecret);
            instance.setOauth1SignatureMethod(this.oauth1SignatureMethod);
            instance.setOauth1Realm(this.oauth1Realm);
            instance.setClientId(this.clientId);
            instance.setEncryptedClientSecret(this.encryptedClientSecret);
            instance.setTokenEndpoint(this.tokenEndpoint);
            instance.setAuthorizationEndpoint(this.authorizationEndpoint);
            instance.setRedirectUri(this.redirectUri);
            instance.setScopes(this.scopes);
            instance.setGrantType(this.grantType);
            instance.setEncryptedAccessToken(this.encryptedAccessToken);
            instance.setRefreshToken(this.refreshToken);
            instance.setTokenExpiresAt(this.tokenExpiresAt);
            instance.setEncryptedApiKey(this.encryptedApiKey);
            instance.setApiKeyHeader(this.apiKeyHeader);
            instance.setApiKeyPrefix(this.apiKeyPrefix);
            instance.setRateLimit(this.rateLimit);
            instance.setRateLimitWindowSeconds(this.rateLimitWindowSeconds);
            instance.setHmacAlgorithm(this.hmacAlgorithm);
            instance.setEncryptedHmacSecretKey(this.encryptedHmacSecretKey);
            instance.setHmacHeaderName(this.hmacHeaderName);
            instance.setHmacIncludeTimestamp(this.hmacIncludeTimestamp);
            instance.setHmacIncludeNonce(this.hmacIncludeNonce);
            instance.setCertificatePath(this.certificatePath);
            instance.setEncryptedCertificatePassword(this.encryptedCertificatePassword);
            instance.setCertificateType(this.certificateType);
            instance.setTrustStorePath(this.trustStorePath);
            instance.setEncryptedTrustStorePassword(this.encryptedTrustStorePassword);
            instance.setActive(this.isActive);
            instance.setLastUsedAt(this.lastUsedAt);
            instance.setUsageCount(this.usageCount);
            instance.setErrorCount(this.errorCount);
            instance.setCreatedBy(this.createdBy);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "ExternalAuthentication{" +
                "id=" + id + "name=" + name + "description=" + description + "authType=" + authType + "username=" + username + "..." +
                '}';
    }
}
