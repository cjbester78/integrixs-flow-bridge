package com.integrixs.backend.service;

import com.integrixs.backend.security.CredentialEncryptionService;
import com.integrixs.data.model.ExternalAuthentication;
import com.integrixs.data.repository.ExternalAuthenticationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling OAuth2 token refresh operations
 */
@Service
@Transactional
public class OAuth2TokenRefreshService {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenRefreshService.class);
    
    @Autowired
    private ExternalAuthenticationRepository authRepository;
    
    @Autowired
    private CredentialEncryptionService encryptionService;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Scheduled task to refresh expiring OAuth2 tokens
     * Runs every 5 minutes
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void refreshExpiringTokens() {
        logger.debug("Starting OAuth2 token refresh check");
        
        try {
            // Find tokens that will expire in the next 10 minutes
            LocalDateTime expiryThreshold = LocalDateTime.now().plusMinutes(10);
            List<ExternalAuthentication> expiringAuths = authRepository.findOAuth2TokensNeedingRefresh(expiryThreshold);
            
            logger.info("Found {} OAuth2 authentications needing token refresh", expiringAuths.size());
            
            for (ExternalAuthentication auth : expiringAuths) {
                try {
                    refreshToken(auth);
                } catch (Exception e) {
                    logger.error("Failed to refresh token for authentication: {}", auth.getName(), e);
                    authRepository.incrementErrorCount(auth.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error during scheduled token refresh", e);
        }
    }
    
    /**
     * Refresh OAuth2 token for a specific authentication
     */
    public boolean refreshToken(ExternalAuthentication auth) {
        if (auth.getAuthType() != ExternalAuthentication.AuthType.OAUTH2) {
            throw new IllegalArgumentException("Authentication is not OAuth2 type: " + auth.getName());
        }
        
        if (auth.getRefreshToken() == null || auth.getRefreshToken().isEmpty()) {
            logger.warn("No refresh token available for authentication: {}", auth.getName());
            return false;
        }
        
        logger.info("Refreshing OAuth2 token for authentication: {}", auth.getName());
        
        try {
            // Build token refresh request
            Map<String, String> formData = new HashMap<>();
            formData.put("grant_type", "refresh_token");
            formData.put("refresh_token", auth.getRefreshToken());
            
            // Add client credentials if available
            String clientId = auth.getClientId();
            String clientSecret = null;
            if (auth.getEncryptedClientSecret() != null) {
                clientSecret = encryptionService.decrypt(auth.getEncryptedClientSecret());
            }
            
            // Build request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(auth.getTokenEndpoint()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(30));
            
            // Add client authentication
            if (clientId != null && clientSecret != null) {
                // Option 1: Basic authentication
                String credentials = clientId + ":" + clientSecret;
                String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
                requestBuilder.header("Authorization", "Basic " + basicAuth);
            } else if (clientId != null) {
                // Option 2: Client ID in form data
                formData.put("client_id", clientId);
            }
            
            // Build form body
            String formBody = buildFormBody(formData);
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(formBody));
            
            HttpRequest request = requestBuilder.build();
            
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse response
                JsonNode responseJson = objectMapper.readTree(response.body());
                
                // Update authentication with new tokens
                String newAccessToken = responseJson.get("access_token").asText();
                auth.setEncryptedAccessToken(encryptionService.encrypt(newAccessToken));
                
                // Update refresh token if provided
                if (responseJson.has("refresh_token")) {
                    auth.setRefreshToken(responseJson.get("refresh_token").asText());
                }
                
                // Calculate expiration time
                if (responseJson.has("expires_in")) {
                    int expiresIn = responseJson.get("expires_in").asInt();
                    auth.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                } else {
                    // Default to 1 hour if not specified
                    auth.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
                }
                
                authRepository.save(auth);
                authRepository.incrementUsageCount(auth.getId());
                
                // Log successful refresh
                Map<String, Object> auditDetails = new HashMap<>();
                auditDetails.put("authName", auth.getName());
                auditDetails.put("tokenEndpoint", auth.getTokenEndpoint());
                auditDetails.put("newExpiry", auth.getTokenExpiresAt());
                auditTrailService.logAction("ExternalAuthentication", auth.getId().toString(),
                        com.integrixs.data.model.AuditTrail.AuditAction.UPDATE, auditDetails);
                
                logger.info("Successfully refreshed OAuth2 token for authentication: {}", auth.getName());
                return true;
                
            } else {
                logger.error("Token refresh failed with status {}: {}", response.statusCode(), response.body());
                
                // Log failed refresh
                Map<String, Object> auditDetails = new HashMap<>();
                auditDetails.put("authName", auth.getName());
                auditDetails.put("statusCode", response.statusCode());
                auditDetails.put("error", response.body());
                auditTrailService.logAction("ExternalAuthentication", auth.getId().toString(),
                        com.integrixs.data.model.AuditTrail.AuditAction.UPDATE, auditDetails);
                
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error refreshing OAuth2 token for authentication: {}", auth.getName(), e);
            return false;
        }
    }
    
    /**
     * Manually refresh token for a specific authentication
     */
    @Transactional
    public Map<String, Object> refreshTokenManually(String authId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ExternalAuthentication auth = authRepository.findById(java.util.UUID.fromString(authId))
                    .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + authId));
            
            if (auth.getAuthType() != ExternalAuthentication.AuthType.OAUTH2) {
                result.put("success", false);
                result.put("message", "Authentication is not OAuth2 type");
                return result;
            }
            
            boolean refreshed = refreshToken(auth);
            
            if (refreshed) {
                result.put("success", true);
                result.put("message", "Token refreshed successfully");
                result.put("expiresAt", auth.getTokenExpiresAt());
            } else {
                result.put("success", false);
                result.put("message", "Token refresh failed");
            }
            
        } catch (Exception e) {
            logger.error("Error manually refreshing token", e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Build URL-encoded form body
     */
    private String buildFormBody(Map<String, String> formData) {
        StringBuilder body = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (!first) {
                body.append("&");
            }
            first = false;
            
            body.append(java.net.URLEncoder.encode(entry.getKey(), java.nio.charset.StandardCharsets.UTF_8));
            body.append("=");
            body.append(java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.UTF_8));
        }
        
        return body.toString();
    }
    
    /**
     * Initiate OAuth2 authorization flow
     */
    public Map<String, String> initiateAuthorizationFlow(String authId, String redirectUri) {
        Map<String, String> result = new HashMap<>();
        
        try {
            ExternalAuthentication auth = authRepository.findById(java.util.UUID.fromString(authId))
                    .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + authId));
            
            if (auth.getAuthType() != ExternalAuthentication.AuthType.OAUTH2) {
                throw new IllegalArgumentException("Authentication is not OAuth2 type");
            }
            
            // Build authorization URL
            StringBuilder authUrl = new StringBuilder(auth.getAuthorizationEndpoint());
            authUrl.append("?response_type=code");
            authUrl.append("&client_id=").append(java.net.URLEncoder.encode(auth.getClientId(), "UTF-8"));
            
            if (redirectUri != null) {
                authUrl.append("&redirect_uri=").append(java.net.URLEncoder.encode(redirectUri, "UTF-8"));
            } else if (auth.getRedirectUri() != null) {
                authUrl.append("&redirect_uri=").append(java.net.URLEncoder.encode(auth.getRedirectUri(), "UTF-8"));
            }
            
            if (auth.getScopes() != null && !auth.getScopes().isEmpty()) {
                authUrl.append("&scope=").append(java.net.URLEncoder.encode(auth.getScopes(), "UTF-8"));
            }
            
            // Add state for security
            String state = java.util.UUID.randomUUID().toString();
            authUrl.append("&state=").append(state);
            
            result.put("authorizationUrl", authUrl.toString());
            result.put("state", state);
            
        } catch (Exception e) {
            logger.error("Error initiating authorization flow", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}