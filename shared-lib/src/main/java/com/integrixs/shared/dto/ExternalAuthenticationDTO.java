package com.integrixs.shared.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO for external authentication configurations
 */
public class ExternalAuthenticationDTO {

    private String id;
    private String name;
    private String description;
    private String authType;
    private String businessComponentId;
    private String businessComponentName;
    private String username;
    private String password;
    private String realm;
    private boolean hasPassword;
    private String clientId;
    private String clientSecret;
    private String tokenEndpoint;
    private String authorizationEndpoint;
    private String redirectUri;
    private String scopes;
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    private boolean hasClientSecret;
    private boolean hasAccessToken;
    private boolean hasRefreshToken;
    private boolean tokenValid;
    private String apiKey;
    private String apiKeyHeader;
    private String apiKeyPrefix;
    private Integer rateLimit;
    private Integer rateLimitWindowSeconds;
    private boolean hasApiKey;
    private String consumerKey;
    private String consumerSecret;
    private String oauth1Token;
    private String oauth1TokenSecret;
    private String oauth1SignatureMethod;
    private String oauth1Realm;
    private boolean hasConsumerSecret;
    private boolean hasOauth1TokenSecret;
    private String hmacAlgorithm;
    private String hmacSecretKey;
    private String hmacHeaderName;
    private boolean hmacIncludeTimestamp;
    private boolean hmacIncludeNonce;
    private boolean hasHmacSecretKey;
    private String certificatePath;
    private String certificatePassword;
    private String certificateType;
    private String trustStorePath;
    private String trustStorePassword;
    private boolean hasCertificatePassword;
    private boolean hasTrustStorePassword;
    private Map<String, String> customHeaders;
    private boolean active;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private Long errorCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ExternalAuthenticationDTO() {
        this.customHeaders = new HashMap<>();
    }

    // All args constructor
    public ExternalAuthenticationDTO(String id, String name, String description, String authType, String businessComponentId, String businessComponentName, String username, String password, String realm, boolean hasPassword, String clientId, String clientSecret, String tokenEndpoint, String authorizationEndpoint, String redirectUri, String scopes, String grantType, String accessToken, String refreshToken, LocalDateTime tokenExpiresAt, boolean hasClientSecret, boolean hasAccessToken, boolean hasRefreshToken, boolean tokenValid, String apiKey, String apiKeyHeader, String apiKeyPrefix, Integer rateLimit, Integer rateLimitWindowSeconds, boolean hasApiKey, String consumerKey, String consumerSecret, String oauth1Token, String oauth1TokenSecret, String oauth1SignatureMethod, String oauth1Realm, boolean hasConsumerSecret, boolean hasOauth1TokenSecret, String hmacAlgorithm, String hmacSecretKey, String hmacHeaderName, boolean hmacIncludeTimestamp, boolean hmacIncludeNonce, boolean hasHmacSecretKey, String certificatePath, String certificatePassword, String certificateType, String trustStorePath, String trustStorePassword, boolean hasCertificatePassword, boolean hasTrustStorePassword, Map<String, String> customHeaders, boolean active, LocalDateTime lastUsedAt, Long usageCount, Long errorCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.authType = authType;
        this.businessComponentId = businessComponentId;
        this.businessComponentName = businessComponentName;
        this.username = username;
        this.password = password;
        this.realm = realm;
        this.hasPassword = hasPassword;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpoint = tokenEndpoint;
        this.authorizationEndpoint = authorizationEndpoint;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.hasClientSecret = hasClientSecret;
        this.hasAccessToken = hasAccessToken;
        this.hasRefreshToken = hasRefreshToken;
        this.tokenValid = tokenValid;
        this.apiKey = apiKey;
        this.apiKeyHeader = apiKeyHeader;
        this.apiKeyPrefix = apiKeyPrefix;
        this.rateLimit = rateLimit;
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
        this.hasApiKey = hasApiKey;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.oauth1Token = oauth1Token;
        this.oauth1TokenSecret = oauth1TokenSecret;
        this.oauth1SignatureMethod = oauth1SignatureMethod;
        this.oauth1Realm = oauth1Realm;
        this.hasConsumerSecret = hasConsumerSecret;
        this.hasOauth1TokenSecret = hasOauth1TokenSecret;
        this.hmacAlgorithm = hmacAlgorithm;
        this.hmacSecretKey = hmacSecretKey;
        this.hmacHeaderName = hmacHeaderName;
        this.hmacIncludeTimestamp = hmacIncludeTimestamp;
        this.hmacIncludeNonce = hmacIncludeNonce;
        this.hasHmacSecretKey = hasHmacSecretKey;
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword;
        this.certificateType = certificateType;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.hasCertificatePassword = hasCertificatePassword;
        this.hasTrustStorePassword = hasTrustStorePassword;
        this.customHeaders = customHeaders != null ? customHeaders : new HashMap<>();
        this.active = active;
        this.lastUsedAt = lastUsedAt;
        this.usageCount = usageCount;
        this.errorCount = errorCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAuthType() { return authType; }
    public String getBusinessComponentId() { return businessComponentId; }
    public String getBusinessComponentName() { return businessComponentName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRealm() { return realm; }
    public boolean isHasPassword() { return hasPassword; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getTokenEndpoint() { return tokenEndpoint; }
    public String getAuthorizationEndpoint() { return authorizationEndpoint; }
    public String getRedirectUri() { return redirectUri; }
    public String getScopes() { return scopes; }
    public String getGrantType() { return grantType; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public boolean isHasClientSecret() { return hasClientSecret; }
    public boolean isHasAccessToken() { return hasAccessToken; }
    public boolean isHasRefreshToken() { return hasRefreshToken; }
    public boolean isTokenValid() { return tokenValid; }
    public String getApiKey() { return apiKey; }
    public String getApiKeyHeader() { return apiKeyHeader; }
    public String getApiKeyPrefix() { return apiKeyPrefix; }
    public Integer getRateLimit() { return rateLimit; }
    public Integer getRateLimitWindowSeconds() { return rateLimitWindowSeconds; }
    public boolean isHasApiKey() { return hasApiKey; }
    public String getConsumerKey() { return consumerKey; }
    public String getConsumerSecret() { return consumerSecret; }
    public String getOauth1Token() { return oauth1Token; }
    public String getOauth1TokenSecret() { return oauth1TokenSecret; }
    public String getOauth1SignatureMethod() { return oauth1SignatureMethod; }
    public String getOauth1Realm() { return oauth1Realm; }
    public boolean isHasConsumerSecret() { return hasConsumerSecret; }
    public boolean isHasOauth1TokenSecret() { return hasOauth1TokenSecret; }
    public String getHmacAlgorithm() { return hmacAlgorithm; }
    public String getHmacSecretKey() { return hmacSecretKey; }
    public String getHmacHeaderName() { return hmacHeaderName; }
    public boolean isHmacIncludeTimestamp() { return hmacIncludeTimestamp; }
    public boolean isHmacIncludeNonce() { return hmacIncludeNonce; }
    public boolean isHasHmacSecretKey() { return hasHmacSecretKey; }
    public String getCertificatePath() { return certificatePath; }
    public String getCertificatePassword() { return certificatePassword; }
    public String getCertificateType() { return certificateType; }
    public String getTrustStorePath() { return trustStorePath; }
    public String getTrustStorePassword() { return trustStorePassword; }
    public boolean isHasCertificatePassword() { return hasCertificatePassword; }
    public boolean isHasTrustStorePassword() { return hasTrustStorePassword; }
    public Map<String, String> getCustomHeaders() { return customHeaders; }
    public boolean isActive() { return active; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public Long getUsageCount() { return usageCount; }
    public Long getErrorCount() { return errorCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setAuthType(String authType) { this.authType = authType; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    public void setBusinessComponentName(String businessComponentName) { this.businessComponentName = businessComponentName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRealm(String realm) { this.realm = realm; }
    public void setHasPassword(boolean hasPassword) { this.hasPassword = hasPassword; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public void setTokenEndpoint(String tokenEndpoint) { this.tokenEndpoint = tokenEndpoint; }
    public void setAuthorizationEndpoint(String authorizationEndpoint) { this.authorizationEndpoint = authorizationEndpoint; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    public void setScopes(String scopes) { this.scopes = scopes; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    public void setHasClientSecret(boolean hasClientSecret) { this.hasClientSecret = hasClientSecret; }
    public void setHasAccessToken(boolean hasAccessToken) { this.hasAccessToken = hasAccessToken; }
    public void setHasRefreshToken(boolean hasRefreshToken) { this.hasRefreshToken = hasRefreshToken; }
    public void setTokenValid(boolean tokenValid) { this.tokenValid = tokenValid; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setApiKeyHeader(String apiKeyHeader) { this.apiKeyHeader = apiKeyHeader; }
    public void setApiKeyPrefix(String apiKeyPrefix) { this.apiKeyPrefix = apiKeyPrefix; }
    public void setRateLimit(Integer rateLimit) { this.rateLimit = rateLimit; }
    public void setRateLimitWindowSeconds(Integer rateLimitWindowSeconds) { this.rateLimitWindowSeconds = rateLimitWindowSeconds; }
    public void setHasApiKey(boolean hasApiKey) { this.hasApiKey = hasApiKey; }
    public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }
    public void setConsumerSecret(String consumerSecret) { this.consumerSecret = consumerSecret; }
    public void setOauth1Token(String oauth1Token) { this.oauth1Token = oauth1Token; }
    public void setOauth1TokenSecret(String oauth1TokenSecret) { this.oauth1TokenSecret = oauth1TokenSecret; }
    public void setOauth1SignatureMethod(String oauth1SignatureMethod) { this.oauth1SignatureMethod = oauth1SignatureMethod; }
    public void setOauth1Realm(String oauth1Realm) { this.oauth1Realm = oauth1Realm; }
    public void setHasConsumerSecret(boolean hasConsumerSecret) { this.hasConsumerSecret = hasConsumerSecret; }
    public void setHasOauth1TokenSecret(boolean hasOauth1TokenSecret) { this.hasOauth1TokenSecret = hasOauth1TokenSecret; }
    public void setHmacAlgorithm(String hmacAlgorithm) { this.hmacAlgorithm = hmacAlgorithm; }
    public void setHmacSecretKey(String hmacSecretKey) { this.hmacSecretKey = hmacSecretKey; }
    public void setHmacHeaderName(String hmacHeaderName) { this.hmacHeaderName = hmacHeaderName; }
    public void setHmacIncludeTimestamp(boolean hmacIncludeTimestamp) { this.hmacIncludeTimestamp = hmacIncludeTimestamp; }
    public void setHmacIncludeNonce(boolean hmacIncludeNonce) { this.hmacIncludeNonce = hmacIncludeNonce; }
    public void setHasHmacSecretKey(boolean hasHmacSecretKey) { this.hasHmacSecretKey = hasHmacSecretKey; }
    public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }
    public void setCertificatePassword(String certificatePassword) { this.certificatePassword = certificatePassword; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
    public void setTrustStorePath(String trustStorePath) { this.trustStorePath = trustStorePath; }
    public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
    public void setHasCertificatePassword(boolean hasCertificatePassword) { this.hasCertificatePassword = hasCertificatePassword; }
    public void setHasTrustStorePassword(boolean hasTrustStorePassword) { this.hasTrustStorePassword = hasTrustStorePassword; }
    public void setCustomHeaders(Map<String, String> customHeaders) { this.customHeaders = customHeaders; }
    public void setActive(boolean active) { this.active = active; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    public void setErrorCount(Long errorCount) { this.errorCount = errorCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static ExternalAuthenticationDTOBuilder builder() {
        return new ExternalAuthenticationDTOBuilder();
    }

    public static class ExternalAuthenticationDTOBuilder {
        private String id;
        private String name;
        private String description;
        private String authType;
        private String businessComponentId;
        private String businessComponentName;
        private String username;
        private String password;
        private String realm;
        private boolean hasPassword;
        private String clientId;
        private String clientSecret;
        private String tokenEndpoint;
        private String authorizationEndpoint;
        private String redirectUri;
        private String scopes;
        private String grantType;
        private String accessToken;
        private String refreshToken;
        private LocalDateTime tokenExpiresAt;
        private boolean hasClientSecret;
        private boolean hasAccessToken;
        private boolean hasRefreshToken;
        private boolean tokenValid;
        private String apiKey;
        private String apiKeyHeader;
        private String apiKeyPrefix;
        private Integer rateLimit;
        private Integer rateLimitWindowSeconds;
        private boolean hasApiKey;
        private String consumerKey;
        private String consumerSecret;
        private String oauth1Token;
        private String oauth1TokenSecret;
        private String oauth1SignatureMethod;
        private String oauth1Realm;
        private boolean hasConsumerSecret;
        private boolean hasOauth1TokenSecret;
        private String hmacAlgorithm;
        private String hmacSecretKey;
        private String hmacHeaderName;
        private boolean hmacIncludeTimestamp;
        private boolean hmacIncludeNonce;
        private boolean hasHmacSecretKey;
        private String certificatePath;
        private String certificatePassword;
        private String certificateType;
        private String trustStorePath;
        private String trustStorePassword;
        private boolean hasCertificatePassword;
        private boolean hasTrustStorePassword;
        private Map<String, String> customHeaders = new HashMap<>();
        private boolean active;
        private LocalDateTime lastUsedAt;
        private Long usageCount;
        private Long errorCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ExternalAuthenticationDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ExternalAuthenticationDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ExternalAuthenticationDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ExternalAuthenticationDTOBuilder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public ExternalAuthenticationDTOBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public ExternalAuthenticationDTOBuilder businessComponentName(String businessComponentName) {
            this.businessComponentName = businessComponentName;
            return this;
        }

        public ExternalAuthenticationDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public ExternalAuthenticationDTOBuilder password(String password) {
            this.password = password;
            return this;
        }

        public ExternalAuthenticationDTOBuilder realm(String realm) {
            this.realm = realm;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasPassword(boolean hasPassword) {
            this.hasPassword = hasPassword;
            return this;
        }

        public ExternalAuthenticationDTOBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ExternalAuthenticationDTOBuilder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public ExternalAuthenticationDTOBuilder tokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        public ExternalAuthenticationDTOBuilder authorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
            return this;
        }

        public ExternalAuthenticationDTOBuilder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public ExternalAuthenticationDTOBuilder scopes(String scopes) {
            this.scopes = scopes;
            return this;
        }

        public ExternalAuthenticationDTOBuilder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public ExternalAuthenticationDTOBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public ExternalAuthenticationDTOBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public ExternalAuthenticationDTOBuilder tokenExpiresAt(LocalDateTime tokenExpiresAt) {
            this.tokenExpiresAt = tokenExpiresAt;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasClientSecret(boolean hasClientSecret) {
            this.hasClientSecret = hasClientSecret;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasAccessToken(boolean hasAccessToken) {
            this.hasAccessToken = hasAccessToken;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasRefreshToken(boolean hasRefreshToken) {
            this.hasRefreshToken = hasRefreshToken;
            return this;
        }

        public ExternalAuthenticationDTOBuilder tokenValid(boolean tokenValid) {
            this.tokenValid = tokenValid;
            return this;
        }

        public ExternalAuthenticationDTOBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public ExternalAuthenticationDTOBuilder apiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
            return this;
        }

        public ExternalAuthenticationDTOBuilder apiKeyPrefix(String apiKeyPrefix) {
            this.apiKeyPrefix = apiKeyPrefix;
            return this;
        }

        public ExternalAuthenticationDTOBuilder rateLimit(Integer rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }

        public ExternalAuthenticationDTOBuilder rateLimitWindowSeconds(Integer rateLimitWindowSeconds) {
            this.rateLimitWindowSeconds = rateLimitWindowSeconds;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasApiKey(boolean hasApiKey) {
            this.hasApiKey = hasApiKey;
            return this;
        }

        public ExternalAuthenticationDTOBuilder consumerKey(String consumerKey) {
            this.consumerKey = consumerKey;
            return this;
        }

        public ExternalAuthenticationDTOBuilder consumerSecret(String consumerSecret) {
            this.consumerSecret = consumerSecret;
            return this;
        }

        public ExternalAuthenticationDTOBuilder oauth1Token(String oauth1Token) {
            this.oauth1Token = oauth1Token;
            return this;
        }

        public ExternalAuthenticationDTOBuilder oauth1TokenSecret(String oauth1TokenSecret) {
            this.oauth1TokenSecret = oauth1TokenSecret;
            return this;
        }

        public ExternalAuthenticationDTOBuilder oauth1SignatureMethod(String oauth1SignatureMethod) {
            this.oauth1SignatureMethod = oauth1SignatureMethod;
            return this;
        }

        public ExternalAuthenticationDTOBuilder oauth1Realm(String oauth1Realm) {
            this.oauth1Realm = oauth1Realm;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasConsumerSecret(boolean hasConsumerSecret) {
            this.hasConsumerSecret = hasConsumerSecret;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasOauth1TokenSecret(boolean hasOauth1TokenSecret) {
            this.hasOauth1TokenSecret = hasOauth1TokenSecret;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hmacAlgorithm(String hmacAlgorithm) {
            this.hmacAlgorithm = hmacAlgorithm;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hmacSecretKey(String hmacSecretKey) {
            this.hmacSecretKey = hmacSecretKey;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hmacHeaderName(String hmacHeaderName) {
            this.hmacHeaderName = hmacHeaderName;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hmacIncludeTimestamp(boolean hmacIncludeTimestamp) {
            this.hmacIncludeTimestamp = hmacIncludeTimestamp;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hmacIncludeNonce(boolean hmacIncludeNonce) {
            this.hmacIncludeNonce = hmacIncludeNonce;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasHmacSecretKey(boolean hasHmacSecretKey) {
            this.hasHmacSecretKey = hasHmacSecretKey;
            return this;
        }

        public ExternalAuthenticationDTOBuilder certificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
            return this;
        }

        public ExternalAuthenticationDTOBuilder certificatePassword(String certificatePassword) {
            this.certificatePassword = certificatePassword;
            return this;
        }

        public ExternalAuthenticationDTOBuilder certificateType(String certificateType) {
            this.certificateType = certificateType;
            return this;
        }

        public ExternalAuthenticationDTOBuilder trustStorePath(String trustStorePath) {
            this.trustStorePath = trustStorePath;
            return this;
        }

        public ExternalAuthenticationDTOBuilder trustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasCertificatePassword(boolean hasCertificatePassword) {
            this.hasCertificatePassword = hasCertificatePassword;
            return this;
        }

        public ExternalAuthenticationDTOBuilder hasTrustStorePassword(boolean hasTrustStorePassword) {
            this.hasTrustStorePassword = hasTrustStorePassword;
            return this;
        }

        public ExternalAuthenticationDTOBuilder customHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public ExternalAuthenticationDTOBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public ExternalAuthenticationDTOBuilder lastUsedAt(LocalDateTime lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public ExternalAuthenticationDTOBuilder usageCount(Long usageCount) {
            this.usageCount = usageCount;
            return this;
        }

        public ExternalAuthenticationDTOBuilder errorCount(Long errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public ExternalAuthenticationDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExternalAuthenticationDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ExternalAuthenticationDTO build() {
            return new ExternalAuthenticationDTO(id, name, description, authType, businessComponentId, businessComponentName, username, password, realm, hasPassword, clientId, clientSecret, tokenEndpoint, authorizationEndpoint, redirectUri, scopes, grantType, accessToken, refreshToken, tokenExpiresAt, hasClientSecret, hasAccessToken, hasRefreshToken, tokenValid, apiKey, apiKeyHeader, apiKeyPrefix, rateLimit, rateLimitWindowSeconds, hasApiKey, consumerKey, consumerSecret, oauth1Token, oauth1TokenSecret, oauth1SignatureMethod, oauth1Realm, hasConsumerSecret, hasOauth1TokenSecret, hmacAlgorithm, hmacSecretKey, hmacHeaderName, hmacIncludeTimestamp, hmacIncludeNonce, hasHmacSecretKey, certificatePath, certificatePassword, certificateType, trustStorePath, trustStorePassword, hasCertificatePassword, hasTrustStorePassword, customHeaders, active, lastUsedAt, usageCount, errorCount, createdAt, updatedAt);
        }
    }
}
