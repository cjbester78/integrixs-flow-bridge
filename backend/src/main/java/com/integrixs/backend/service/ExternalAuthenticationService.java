package com.integrixs.backend.service;

import com.integrixs.backend.annotation.*;
import com.integrixs.backend.security.CredentialEncryptionService;
import com.integrixs.data.model.ExternalAuthentication;
import com.integrixs.data.model.ExternalAuthentication.AuthType;
import com.integrixs.data.model.User;
import com.integrixs.data.model.BusinessComponent;
import com.integrixs.shared.dto.ExternalAuthenticationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Base64;

/**
 * Service for managing external authentication configurations.
 * These configurations can be used by any HTTP/HTTPS - based adapter.
 */
@Service
public class ExternalAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthenticationService.class);

    @Autowired
    private com.integrixs.data.sql.repository.ExternalAuthenticationSqlRepository repository;

    @Autowired
    private com.integrixs.data.sql.repository.BusinessComponentSqlRepository businessComponentRepository;

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Autowired
    private com.integrixs.data.sql.repository.UserSqlRepository userRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a new external authentication configuration
     */
    @AuditCreate
    public ExternalAuthenticationDTO createAuthentication(ExternalAuthenticationDTO dto) {
        logger.info("Creating external authentication: {} of type {}", dto.getName(), dto.getAuthType());

        // Validate unique name within business component
        BusinessComponent businessComponent = businessComponentRepository.findById(UUID.fromString(dto.getBusinessComponentId()))
                .orElseThrow(() -> new IllegalArgumentException("Business component not found: " + dto.getBusinessComponentId()));
        if(repository.existsByNameAndBusinessComponent(dto.getName(), businessComponent)) {
            throw new IllegalArgumentException("Authentication configuration with name '" + dto.getName() +
                    "' already exists in this business component");
        }

        ExternalAuthentication auth = new ExternalAuthentication();
        auth.setName(dto.getName());
        auth.setDescription(dto.getDescription());
        auth.setAuthType(AuthType.valueOf(dto.getAuthType()));
        auth.setBusinessComponent(businessComponent);

        // Encrypt sensitive fields based on auth type
        populateAndEncryptAuthFields(auth, dto);

        // Set audit fields
        auth.setCreatedBy(getCurrentUser());
        auth.setActive(true);

        ExternalAuthentication saved = repository.save(auth);

        // Log sensitive operation
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("authType", auth.getAuthType());
        auditDetails.put("businessComponent", dto.getBusinessComponentId());
        auditTrailService.logAction("ExternalAuthentication", saved.getId().toString(),
                com.integrixs.data.model.AuditTrail.AuditAction.CREATE, auditDetails);

        return toDTO(saved);
    }

    /**
     * Update an external authentication configuration
     */
    @AuditUpdate
    public ExternalAuthenticationDTO updateAuthentication(String id, ExternalAuthenticationDTO dto) {
        logger.info("Updating external authentication: {}", id);

        ExternalAuthentication auth = repository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + id));

        // Check for name uniqueness if name is being changed
        if(!auth.getName().equals(dto.getName()) &&
            repository.existsByNameAndBusinessComponent(dto.getName(), auth.getBusinessComponent())) {
            throw new IllegalArgumentException("Authentication configuration with name '" + dto.getName() +
                    "' already exists in this business component");
        }

        auth.setName(dto.getName());
        auth.setDescription(dto.getDescription());
        auth.setActive(dto.isActive());

        // Re - encrypt sensitive fields if they've changed
        populateAndEncryptAuthFields(auth, dto);

        ExternalAuthentication saved = repository.save(auth);

        // Log sensitive operation
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("fieldsUpdated", getUpdatedFields(auth, dto));
        auditTrailService.logAction("ExternalAuthentication", saved.getId().toString(),
                com.integrixs.data.model.AuditTrail.AuditAction.UPDATE, auditDetails);

        return toDTO(saved);
    }

    /**
     * Delete an external authentication configuration
     */
    @AuditDelete
    public void deleteAuthentication(String id) {
        logger.info("Deleting external authentication: {}", id);

        ExternalAuthentication auth = repository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + id));

        // Check if authentication is in use by any adapters
        // This would be implemented when we have the adapter - auth relationship

        repository.deleteById(auth.getId());

        // Log sensitive operation
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("authType", auth.getAuthType());
        auditDetails.put("name", auth.getName());
        auditTrailService.logAction("ExternalAuthentication", id,
                com.integrixs.data.model.AuditTrail.AuditAction.DELETE, auditDetails);
    }

    /**
     * Get all authentications for a business component
     */
    public List<ExternalAuthenticationDTO> getAuthenticationsByBusinessComponent(String businessComponentId) {
        var businessComponent = businessComponentRepository.findById(UUID.fromString(businessComponentId))
                .orElseThrow(() -> new IllegalArgumentException("Business component not found: " + businessComponentId));
        return repository.findByBusinessComponentAndIsActiveTrue(businessComponent)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get authentication by ID
     */
    public ExternalAuthenticationDTO getAuthenticationById(String id) {
        return repository.findById(UUID.fromString(id))
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + id));
    }

    /**
     * Apply authentication to an HTTP request builder
     * This method is called by adapters to configure authentication
     */
    public void applyAuthentication(String authId, Map<String, String> headers,
                                  Map<String, Object> requestContext) {
        if(authId == null) return;

        ExternalAuthentication auth = repository.findById(UUID.fromString(authId))
                .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + authId));

        if(!auth.isActive()) {
            throw new IllegalStateException("Authentication configuration is inactive: " + auth.getName());
        }

        try {
            switch(auth.getAuthType()) {
                case BASIC:
                    applyBasicAuth(auth, headers);
                    break;
                case OAUTH2:
                    applyOAuth2Auth(auth, headers);
                    break;
                case API_KEY:
                    applyApiKeyAuth(auth, headers);
                    break;
                case OAUTH1:
                    applyOAuth1Auth(auth, headers, requestContext);
                    break;
                case HMAC:
                    applyHmacAuth(auth, headers, requestContext);
                    break;
                case CERTIFICATE:
                    applyCertificateAuth(auth, requestContext);
                    break;
                case CUSTOM_HEADER:
                    applyCustomHeaderAuth(auth, headers);
                    break;
                default:
                    logger.warn("Unsupported authentication type: {}", auth.getAuthType());
            }

            // Update usage statistics
            repository.incrementUsageCount(auth.getId());

        } catch(Exception e) {
            logger.error("Failed to apply authentication: {}", auth.getName(), e);
            repository.incrementErrorCount(auth.getId());
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Test authentication configuration
     */
    public Map<String, Object> testAuthentication(String id) {
        ExternalAuthentication auth = repository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + id));

        Map<String, Object> result = new HashMap<>();
        result.put("authType", auth.getAuthType());
        result.put("name", auth.getName());

        try {
            // Basic validation based on auth type
            switch(auth.getAuthType()) {
                case BASIC:
                    if(auth.getUsername() == null || auth.getEncryptedPassword() == null) {
                        throw new IllegalStateException("Basic auth requires username and password");
                    }
                    result.put("status", "CONFIGURED");
                    result.put("message", "Basic authentication is configured");
                    break;

                case OAUTH2:
                    if(auth.getEncryptedAccessToken() != null && auth.getTokenExpiresAt() != null) {
                        if(auth.needsTokenRefresh()) {
                            result.put("status", "TOKEN_EXPIRED");
                            result.put("message", "OAuth2 token needs refresh");
                        } else {
                            result.put("status", "ACTIVE");
                            result.put("message", "OAuth2 token is valid until " + auth.getTokenExpiresAt());
                        }
                    } else {
                        result.put("status", "NEEDS_AUTHORIZATION");
                        result.put("message", "OAuth2 requires authorization");
                    }
                    break;

                case API_KEY:
                    if(auth.getEncryptedApiKey() == null) {
                        throw new IllegalStateException("API key authentication requires API key");
                    }
                    result.put("status", "CONFIGURED");
                    result.put("message", "API key authentication is configured");
                    break;

                case OAUTH1:
                    if(auth.getConsumerKey() == null || auth.getConsumerSecret() == null) {
                        throw new IllegalStateException("OAuth1 requires consumer key and secret");
                    }
                    result.put("status", "CONFIGURED");
                    result.put("message", "OAuth1 authentication is configured");
                    result.put("signatureMethod", auth.getOauth1SignatureMethod());
                    if(auth.getOauth1Realm() != null) {
                        result.put("realm", auth.getOauth1Realm());
                    }
                    break;

                default:
                    result.put("status", "UNSUPPORTED");
                    result.put("message", "Authentication type not yet supported");
            }

            result.put("success", true);

        } catch(Exception e) {
            result.put("success", false);
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }

        return result;
    }

    private void applyBasicAuth(ExternalAuthentication auth, Map<String, String> headers) {
        String password = encryptionService.decrypt(auth.getEncryptedPassword());
        String credentials = auth.getUsername() + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        headers.put("Authorization", "Basic " + encodedCredentials);

        if(auth.getRealm() != null) {
            headers.put("WWW - Authenticate", "Basic realm = \"" + auth.getRealm() + "\"");
        }
    }

    @Autowired
    private OAuth2TokenRefreshService oauth2TokenRefreshService;

    private void applyOAuth2Auth(ExternalAuthentication auth, Map<String, String> headers) {
        // Check if token needs refresh
        if(auth.needsTokenRefresh()) {
            if(auth.getRefreshToken() != null) {
                // Try to refresh the token
                logger.info("OAuth2 token needs refresh for authentication: {}", auth.getName());
                boolean refreshed = oauth2TokenRefreshService.refreshToken(auth);
                if(!refreshed) {
                    // If refresh fails with refresh token, try client credentials if configured
                    if("client_credentials".equals(auth.getGrantType()) ||
                        (auth.getClientId() != null && auth.getEncryptedClientSecret() != null)) {
                        logger.info("Attempting client credentials flow for authentication: {}", auth.getName());
                        Map<String, Object> result = oauth2TokenRefreshService.requestClientCredentialsToken(auth.getId().toString());
                        if(!Boolean.TRUE.equals(result.get("success"))) {
                            throw new RuntimeException("Failed to refresh OAuth2 token: " + result.get("message"));
                        }
                    } else {
                        throw new RuntimeException("OAuth2 token expired and refresh failed");
                    }
                }
            } else if("client_credentials".equals(auth.getGrantType()) ||
                      (auth.getClientId() != null && auth.getEncryptedClientSecret() != null)) {
                // For client credentials flow, request new token
                logger.info("Requesting new client credentials token for authentication: {}", auth.getName());
                Map<String, Object> result = oauth2TokenRefreshService.requestClientCredentialsToken(auth.getId().toString());
                if(!Boolean.TRUE.equals(result.get("success"))) {
                    throw new RuntimeException("Failed to obtain OAuth2 token: " + result.get("message"));
                }
            } else {
                throw new RuntimeException("OAuth2 token expired and no refresh mechanism available");
            }
        }

        // At this point we should have a valid token
        if(auth.getEncryptedAccessToken() == null) {
            throw new RuntimeException("No OAuth2 access token available");
        }

        String accessToken = encryptionService.decrypt(auth.getEncryptedAccessToken());
        headers.put("Authorization", "Bearer " + accessToken);
    }

    private void applyApiKeyAuth(ExternalAuthentication auth, Map<String, String> headers) {
        String apiKey = encryptionService.decrypt(auth.getEncryptedApiKey());
        String headerName = auth.getApiKeyHeader() != null ? auth.getApiKeyHeader() : "X - API - Key";

        if(auth.getApiKeyPrefix() != null) {
            apiKey = auth.getApiKeyPrefix() + " " + apiKey;
        }

        headers.put(headerName, apiKey);
    }

    private void applyOAuth1Auth(ExternalAuthentication auth, Map<String, String> headers,
                                Map<String, Object> requestContext) {
        try {
            // Get OAuth1 credentials
            String consumerKey = auth.getConsumerKey();
            String consumerSecret = encryptionService.decrypt(auth.getConsumerSecret());
            String token = auth.getOauth1Token();
            String tokenSecret = auth.getOauth1TokenSecret() != null ?
                encryptionService.decrypt(auth.getOauth1TokenSecret()) : "";

            // Get request details from context
            String httpMethod = (String) requestContext.getOrDefault("httpMethod", "GET");
            String requestUrl = (String) requestContext.get("requestUrl");
            if(requestUrl == null) {
                throw new IllegalArgumentException("Request URL is required for OAuth1 authentication");
            }

            // Parse query parameters if present
            Map<String, String> queryParams = new HashMap<>();
            String queryString = (String) requestContext.get("queryString");
            if(queryString != null && !queryString.isEmpty()) {
                parseQueryParameters(queryString, queryParams);
            }

            // Generate OAuth parameters
            Map<String, String> oauthParams = new TreeMap<>();
            oauthParams.put("oauth_consumer_key", consumerKey);
            oauthParams.put("oauth_nonce", generateNonce());
            oauthParams.put("oauth_signature_method", "HMAC - SHA1");
            oauthParams.put("oauth_timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            oauthParams.put("oauth_version", "1.0");

            if(token != null && !token.isEmpty()) {
                oauthParams.put("oauth_token", token);
            }

            // Additional parameters from auth config
            if(auth.getOauth1SignatureMethod() != null) {
                oauthParams.put("oauth_signature_method", auth.getOauth1SignatureMethod());
            }

            if(auth.getOauth1Realm() != null) {
                // Realm is included in Authorization header but not in signature
            }

            // Combine all parameters for signature base string
            Map<String, String> allParams = new TreeMap<>();
            allParams.putAll(queryParams);
            allParams.putAll(oauthParams);

            // Handle request body parameters for POST with form data
            if("POST".equalsIgnoreCase(httpMethod) && requestContext.containsKey("formParams")) {
                @SuppressWarnings("unchecked")
                Map<String, String> formParams = (Map<String, String>) requestContext.get("formParams");
                allParams.putAll(formParams);
            }

            // Build signature base string
            String signatureBaseString = buildSignatureBaseString(httpMethod, requestUrl, allParams);

            // Generate signature
            String signature = generateSignature(signatureBaseString, consumerSecret, tokenSecret,
                oauthParams.get("oauth_signature_method"));

            // Add signature to OAuth parameters
            oauthParams.put("oauth_signature", signature);

            // Build Authorization header
            StringBuilder authHeader = new StringBuilder("OAuth ");

            // Add realm if specified
            if(auth.getOauth1Realm() != null) {
                authHeader.append("realm = \"").append(percentEncode(auth.getOauth1Realm())).append("\", ");
            }

            // Add OAuth parameters
            boolean first = auth.getOauth1Realm() == null;
            for(Map.Entry<String, String> entry : oauthParams.entrySet()) {
                if(!first) {
                    authHeader.append(", ");
                }
                authHeader.append(entry.getKey()).append(" = \"")
                         .append(percentEncode(entry.getValue())).append("\"");
                first = false;
            }

            headers.put("Authorization", authHeader.toString());

            logger.debug("OAuth1 signature generated for {}: {}", auth.getName(), signatureBaseString);

        } catch(Exception e) {
            logger.error("Failed to apply OAuth1 authentication", e);
            throw new RuntimeException("OAuth1 authentication failed: " + e.getMessage(), e);
        }
    }

    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void parseQueryParameters(String queryString, Map<String, String> params) {
        if(queryString == null || queryString.isEmpty()) {
            return;
        }

        String[] pairs = queryString.split("&");
        for(String pair : pairs) {
            int idx = pair.indexOf(" = ");
            if(idx > 0) {
                try {
                    String key = java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = idx < pair.length() - 1 ?
                        java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
                    params.put(key, value);
                } catch(Exception e) {
                    logger.warn("Failed to parse query parameter: {}", pair);
                }
            }
        }
    }

    private String buildSignatureBaseString(String httpMethod, String requestUrl, Map<String, String> params) {
        // 1. Convert HTTP method to uppercase
        String method = httpMethod.toUpperCase();

        // 2. Normalize request URL(remove query string and fragment)
        String normalizedUrl = normalizeUrl(requestUrl);

        // 3. Normalize parameters
        String normalizedParams = normalizeParameters(params);

        // 4. Construct signature base string
        return method + "&" + percentEncode(normalizedUrl) + "&" + percentEncode(normalizedParams);
    }

    private String normalizeUrl(String url) {
        try {
            java.net.URL parsedUrl = new java.net.URL(url);

            // Reconstruct URL without query string or fragment
            String scheme = parsedUrl.getProtocol().toLowerCase();
            String host = parsedUrl.getHost().toLowerCase();
            int port = parsedUrl.getPort();
            String path = parsedUrl.getPath();

            // Normalize port
            if(port == parsedUrl.getDefaultPort() || port == -1) {
                return scheme + "://" + host + path;
            } else {
                return scheme + "://" + host + ":" + port + path;
            }
        } catch(Exception e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }

    private String normalizeParameters(Map<String, String> params) {
        // OAuth spec requires parameters to be sorted and concatenated
        List<String> paramPairs = new ArrayList<>();

        for(Map.Entry<String, String> entry : params.entrySet()) {
            // Skip oauth_signature
            if("oauth_signature".equals(entry.getKey())) {
                continue;
            }

            String encodedKey = percentEncode(entry.getKey());
            String encodedValue = percentEncode(entry.getValue());
            paramPairs.add(encodedKey + " = " + encodedValue);
        }

        // Sort parameters lexicographically
        Collections.sort(paramPairs);

        // Join with &
        return String.join("&", paramPairs);
    }

    private String percentEncode(String value) {
        if(value == null) {
            return "";
        }

        try {
            // OAuth1 percent encoding is slightly different from URLEncoder
            String encoded = java.net.URLEncoder.encode(value, "UTF-8");
            // OAuth1 requires these specific replacements
            return encoded
                .replace(" + ", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
        } catch(Exception e) {
            throw new RuntimeException("Failed to encode value: " + value, e);
        }
    }

    private String generateSignature(String signatureBaseString, String consumerSecret,
                                   String tokenSecret, String signatureMethod) {
        try {
            // Build signing key
            String signingKey = percentEncode(consumerSecret) + "&" + percentEncode(tokenSecret);

            // Generate signature based on method
            if("HMAC - SHA256".equals(signatureMethod)) {
                return hmacSha256(signatureBaseString, signingKey);
            } else if("HMAC - SHA1".equals(signatureMethod)) {
                return hmacSha1(signatureBaseString, signingKey);
            } else if("PLAINTEXT".equals(signatureMethod)) {
                // PLAINTEXT method just returns the signing key
                return signingKey;
            } else {
                throw new IllegalArgumentException("Unsupported signature method: " + signatureMethod);
            }
        } catch(Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private String hmacSha1(String data, String key) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
            key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA1");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    private String hmacSha256(String data, String key) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
            key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    private void applyHmacAuth(ExternalAuthentication auth, Map<String, String> headers,
                              Map<String, Object> requestContext) {
        try {
            String secretKey = encryptionService.decrypt(auth.getEncryptedHmacSecretKey());
            String algorithm = auth.getHmacAlgorithm() != null ? auth.getHmacAlgorithm() : "HmacSHA256";
            String headerName = auth.getHmacHeaderName() != null ? auth.getHmacHeaderName() : "X - HMAC - Signature";

            // Build string to sign
            StringBuilder dataToSign = new StringBuilder();

            // Add HTTP method if available
            if(requestContext.containsKey("httpMethod")) {
                dataToSign.append(requestContext.get("httpMethod")).append("\n");
            }

            // Add request URI if available
            if(requestContext.containsKey("requestUri")) {
                dataToSign.append(requestContext.get("requestUri")).append("\n");
            }

            // Add timestamp if configured
            if(Boolean.TRUE.equals(auth.getHmacIncludeTimestamp())) {
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                headers.put("X - Timestamp", timestamp);
                dataToSign.append(timestamp).append("\n");
            }

            // Add nonce if configured
            if(Boolean.TRUE.equals(auth.getHmacIncludeNonce())) {
                String nonce = UUID.randomUUID().toString();
                headers.put("X - Nonce", nonce);
                dataToSign.append(nonce).append("\n");
            }

            // Add request body if available
            if(requestContext.containsKey("requestBody")) {
                dataToSign.append(requestContext.get("requestBody"));
            }

            // Generate HMAC signature
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance(algorithm);
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), algorithm);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(dataToSign.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String hmacSignature = Base64.getEncoder().encodeToString(hmacBytes);

            headers.put(headerName, hmacSignature);

        } catch(Exception e) {
            logger.error("Failed to apply HMAC authentication", e);
            throw new RuntimeException("HMAC authentication failed: " + e.getMessage(), e);
        }
    }

    private void applyCertificateAuth(ExternalAuthentication auth, Map<String, Object> requestContext) {
        // Certificate authentication is typically handled at the HTTP client level
        // Store certificate configuration in request context for the adapter to use
        if(auth.getCertificatePath() != null) {
            requestContext.put("certificatePath", auth.getCertificatePath());
            if(auth.getEncryptedCertificatePassword() != null) {
                requestContext.put("certificatePassword",
                    encryptionService.decrypt(auth.getEncryptedCertificatePassword()));
            }
            if(auth.getCertificateType() != null) {
                requestContext.put("certificateType", auth.getCertificateType());
            }
        }

        if(auth.getTrustStorePath() != null) {
            requestContext.put("trustStorePath", auth.getTrustStorePath());
            if(auth.getEncryptedTrustStorePassword() != null) {
                requestContext.put("trustStorePassword",
                    encryptionService.decrypt(auth.getEncryptedTrustStorePassword()));
            }
        }

        // Set flag to indicate certificate auth is required
        requestContext.put("requiresCertificateAuth", true);
    }

    private void applyCustomHeaderAuth(ExternalAuthentication auth, Map<String, String> headers) {
        if(auth.getCustomHeaders() != null && !auth.getCustomHeaders().isEmpty()) {
            // Add all custom headers
            for(Map.Entry<String, String> entry : auth.getCustomHeaders().entrySet()) {
                String headerName = entry.getKey();
                String headerValue = entry.getValue();

                // Check if value contains a placeholder for encrypted data
                if(headerValue != null && headerValue.startsWith("${encrypted:") && headerValue.endsWith("}")) {
                    // Extract and decrypt the value
                    String encryptedValue = headerValue.substring(12, headerValue.length() - 1);
                    headerValue = encryptionService.decrypt(encryptedValue);
                }

                headers.put(headerName, headerValue);
            }
        } else {
            logger.warn("Custom header authentication configured but no headers defined");
        }
    }

    private void populateAndEncryptAuthFields(ExternalAuthentication auth, ExternalAuthenticationDTO dto) {
        switch(auth.getAuthType()) {
            case BASIC:
                auth.setUsername(dto.getUsername());
                if(dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                    auth.setEncryptedPassword(encryptionService.encrypt(dto.getPassword()));
                }
                auth.setRealm(dto.getRealm());
                break;

            case OAUTH2:
                auth.setClientId(dto.getClientId());
                if(dto.getClientSecret() != null && !dto.getClientSecret().isEmpty()) {
                    auth.setEncryptedClientSecret(encryptionService.encrypt(dto.getClientSecret()));
                }
                auth.setTokenEndpoint(dto.getTokenEndpoint());
                auth.setAuthorizationEndpoint(dto.getAuthorizationEndpoint());
                auth.setRedirectUri(dto.getRedirectUri());
                auth.setScopes(dto.getScopes());
                auth.setGrantType(dto.getGrantType());
                if(dto.getAccessToken() != null && !dto.getAccessToken().isEmpty()) {
                    auth.setEncryptedAccessToken(encryptionService.encrypt(dto.getAccessToken()));
                }
                auth.setRefreshToken(dto.getRefreshToken());
                auth.setTokenExpiresAt(dto.getTokenExpiresAt());
                break;

            case API_KEY:
                if(dto.getApiKey() != null && !dto.getApiKey().isEmpty()) {
                    auth.setEncryptedApiKey(encryptionService.encrypt(dto.getApiKey()));
                }
                auth.setApiKeyHeader(dto.getApiKeyHeader());
                auth.setApiKeyPrefix(dto.getApiKeyPrefix());
                auth.setRateLimit(dto.getRateLimit());
                auth.setRateLimitWindowSeconds(dto.getRateLimitWindowSeconds());
                break;

            case OAUTH1:
                auth.setConsumerKey(dto.getConsumerKey());
                if(dto.getConsumerSecret() != null && !dto.getConsumerSecret().isEmpty()) {
                    auth.setConsumerSecret(encryptionService.encrypt(dto.getConsumerSecret()));
                }
                auth.setOauth1Token(dto.getOauth1Token());
                if(dto.getOauth1TokenSecret() != null && !dto.getOauth1TokenSecret().isEmpty()) {
                    auth.setOauth1TokenSecret(encryptionService.encrypt(dto.getOauth1TokenSecret()));
                }
                auth.setOauth1SignatureMethod(dto.getOauth1SignatureMethod() != null ?
                    dto.getOauth1SignatureMethod() : "HMAC - SHA1");
                auth.setOauth1Realm(dto.getOauth1Realm());
                break;

            case HMAC:
                auth.setHmacAlgorithm(dto.getHmacAlgorithm());
                if(dto.getHmacSecretKey() != null && !dto.getHmacSecretKey().isEmpty()) {
                    auth.setEncryptedHmacSecretKey(encryptionService.encrypt(dto.getHmacSecretKey()));
                }
                auth.setHmacHeaderName(dto.getHmacHeaderName());
                auth.setHmacIncludeTimestamp(dto.isHmacIncludeTimestamp());
                auth.setHmacIncludeNonce(dto.isHmacIncludeNonce());
                break;

            case CERTIFICATE:
                auth.setCertificatePath(dto.getCertificatePath());
                if(dto.getCertificatePassword() != null && !dto.getCertificatePassword().isEmpty()) {
                    auth.setEncryptedCertificatePassword(encryptionService.encrypt(dto.getCertificatePassword()));
                }
                auth.setCertificateType(dto.getCertificateType());
                auth.setTrustStorePath(dto.getTrustStorePath());
                if(dto.getTrustStorePassword() != null && !dto.getTrustStorePassword().isEmpty()) {
                    auth.setEncryptedTrustStorePassword(encryptionService.encrypt(dto.getTrustStorePassword()));
                }
                break;

            case CUSTOM_HEADER:
                auth.setCustomHeaders(dto.getCustomHeaders());
                // Encrypt any sensitive values in custom headers
                if(dto.getCustomHeaders() != null) {
                    Map<String, String> processedHeaders = new HashMap<>();
                    for(Map.Entry<String, String> entry : dto.getCustomHeaders().entrySet()) {
                        String value = entry.getValue();
                        // Check if value is marked as sensitive
                        if(value != null && value.startsWith("${sensitive:") && value.endsWith("}")) {
                            String sensitiveValue = value.substring(12, value.length() - 1);
                            value = "${encrypted:" + encryptionService.encrypt(sensitiveValue) + "}";
                        }
                        processedHeaders.put(entry.getKey(), value);
                    }
                    auth.setCustomHeaders(processedHeaders);
                }
                break;
        }
    }

    private ExternalAuthenticationDTO toDTO(ExternalAuthentication auth) {
        ExternalAuthenticationDTO dto = new ExternalAuthenticationDTO();
        dto.setId(auth.getId().toString());
        dto.setName(auth.getName());
        dto.setDescription(auth.getDescription());
        dto.setAuthType(auth.getAuthType().name());
        dto.setBusinessComponentId(auth.getBusinessComponent().getId().toString());
        dto.setBusinessComponentName(auth.getBusinessComponent().getName());
        dto.setActive(auth.isActive());
        dto.setLastUsedAt(auth.getLastUsedAt());
        dto.setUsageCount(auth.getUsageCount());
        dto.setErrorCount(auth.getErrorCount());
        dto.setCreatedAt(auth.getCreatedAt());
        dto.setUpdatedAt(auth.getUpdatedAt());

        // Don't send decrypted credentials to client
        // Only send non - sensitive configuration
        switch(auth.getAuthType()) {
            case BASIC:
                dto.setUsername(auth.getUsername());
                dto.setRealm(auth.getRealm());
                dto.setHasPassword(auth.getEncryptedPassword() != null);
                break;

            case OAUTH2:
                dto.setClientId(auth.getClientId());
                dto.setTokenEndpoint(auth.getTokenEndpoint());
                dto.setAuthorizationEndpoint(auth.getAuthorizationEndpoint());
                dto.setRedirectUri(auth.getRedirectUri());
                dto.setScopes(auth.getScopes());
                dto.setGrantType(auth.getGrantType());
                dto.setTokenExpiresAt(auth.getTokenExpiresAt());
                dto.setHasClientSecret(auth.getEncryptedClientSecret() != null);
                dto.setHasAccessToken(auth.getEncryptedAccessToken() != null);
                dto.setHasRefreshToken(auth.getRefreshToken() != null);
                if(auth.getTokenExpiresAt() != null) {
                    dto.setTokenValid(!auth.needsTokenRefresh());
                }
                break;

            case API_KEY:
                dto.setApiKeyHeader(auth.getApiKeyHeader());
                dto.setApiKeyPrefix(auth.getApiKeyPrefix());
                dto.setRateLimit(auth.getRateLimit());
                dto.setRateLimitWindowSeconds(auth.getRateLimitWindowSeconds());
                dto.setHasApiKey(auth.getEncryptedApiKey() != null);
                break;

            case OAUTH1:
                dto.setConsumerKey(auth.getConsumerKey());
                dto.setOauth1Token(auth.getOauth1Token());
                dto.setOauth1SignatureMethod(auth.getOauth1SignatureMethod());
                dto.setOauth1Realm(auth.getOauth1Realm());
                dto.setHasConsumerSecret(auth.getConsumerSecret() != null);
                dto.setHasOauth1TokenSecret(auth.getOauth1TokenSecret() != null);
                break;

            case HMAC:
                dto.setHmacAlgorithm(auth.getHmacAlgorithm());
                dto.setHmacHeaderName(auth.getHmacHeaderName());
                dto.setHmacIncludeTimestamp(auth.getHmacIncludeTimestamp() != null ? auth.getHmacIncludeTimestamp() : false);
                dto.setHmacIncludeNonce(auth.getHmacIncludeNonce() != null ? auth.getHmacIncludeNonce() : false);
                dto.setHasHmacSecretKey(auth.getEncryptedHmacSecretKey() != null);
                break;

            case CERTIFICATE:
                dto.setCertificatePath(auth.getCertificatePath());
                dto.setCertificateType(auth.getCertificateType());
                dto.setTrustStorePath(auth.getTrustStorePath());
                dto.setHasCertificatePassword(auth.getEncryptedCertificatePassword() != null);
                dto.setHasTrustStorePassword(auth.getEncryptedTrustStorePassword() != null);
                break;

            case CUSTOM_HEADER:
                // Return custom headers but mask encrypted values
                if(auth.getCustomHeaders() != null) {
                    Map<String, String> maskedHeaders = new HashMap<>();
                    for(Map.Entry<String, String> entry : auth.getCustomHeaders().entrySet()) {
                        String value = entry.getValue();
                        if(value != null && value.startsWith("${encrypted:") && value.endsWith("}")) {
                            value = "***ENCRYPTED***";
                        }
                        maskedHeaders.put(entry.getKey(), value);
                    }
                    dto.setCustomHeaders(maskedHeaders);
                }
                break;
        }

        return dto;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    private List<String> getUpdatedFields(ExternalAuthentication auth, ExternalAuthenticationDTO dto) {
        List<String> updatedFields = new ArrayList<>();

        if(!auth.getName().equals(dto.getName())) updatedFields.add("name");
        if(!Objects.equals(auth.getDescription(), dto.getDescription())) updatedFields.add("description");

        // Check auth - specific fields based on type
        switch(auth.getAuthType()) {
            case BASIC:
                if(!Objects.equals(auth.getUsername(), dto.getUsername())) updatedFields.add("username");
                if(dto.getPassword() != null && !dto.getPassword().isEmpty()) updatedFields.add("password");
                break;
            case OAUTH2:
                if(!Objects.equals(auth.getClientId(), dto.getClientId())) updatedFields.add("clientId");
                if(dto.getClientSecret() != null && !dto.getClientSecret().isEmpty()) updatedFields.add("clientSecret");
                if(dto.getAccessToken() != null && !dto.getAccessToken().isEmpty()) updatedFields.add("accessToken");
                break;
            case API_KEY:
                if(dto.getApiKey() != null && !dto.getApiKey().isEmpty()) updatedFields.add("apiKey");
                break;
        }

        return updatedFields;
    }
}
