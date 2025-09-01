package com.integrixs.backend.service;

import com.integrixs.backend.annotation.*;
import com.integrixs.backend.security.CredentialEncryptionService;
import com.integrixs.data.repository.UserRepository;
import com.integrixs.data.model.ExternalAuthentication;
import com.integrixs.data.model.ExternalAuthentication.AuthType;
import com.integrixs.data.model.User;
import com.integrixs.data.repository.ExternalAuthenticationRepository;
import com.integrixs.data.repository.BusinessComponentRepository;
import com.integrixs.shared.dto.ExternalAuthenticationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing external authentication configurations.
 * These configurations can be used by any HTTP/HTTPS-based adapter.
 */
@Service
@Transactional
public class ExternalAuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthenticationService.class);
    
    @Autowired
    private ExternalAuthenticationRepository repository;
    
    @Autowired
    private BusinessComponentRepository businessComponentRepository;
    
    @Autowired
    private CredentialEncryptionService encryptionService;
    
    @Autowired
    private com.integrixs.data.repository.UserRepository userRepository;
    
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
        if (repository.existsByNameAndBusinessComponent(dto.getName(), 
                businessComponentRepository.getReferenceById(UUID.fromString(dto.getBusinessComponentId())))) {
            throw new IllegalArgumentException("Authentication configuration with name '" + dto.getName() + 
                    "' already exists in this business component");
        }
        
        ExternalAuthentication auth = new ExternalAuthentication();
        auth.setName(dto.getName());
        auth.setDescription(dto.getDescription());
        auth.setAuthType(AuthType.valueOf(dto.getAuthType()));
        auth.setBusinessComponent(businessComponentRepository.getReferenceById(
                UUID.fromString(dto.getBusinessComponentId())));
        
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
        if (!auth.getName().equals(dto.getName()) && 
            repository.existsByNameAndBusinessComponent(dto.getName(), auth.getBusinessComponent())) {
            throw new IllegalArgumentException("Authentication configuration with name '" + dto.getName() + 
                    "' already exists in this business component");
        }
        
        auth.setName(dto.getName());
        auth.setDescription(dto.getDescription());
        auth.setActive(dto.isActive());
        
        // Re-encrypt sensitive fields if they've changed
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
        // This would be implemented when we have the adapter-auth relationship
        
        repository.delete(auth);
        
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
    @Transactional(readOnly = true)
    public List<ExternalAuthenticationDTO> getAuthenticationsByBusinessComponent(String businessComponentId) {
        var businessComponent = businessComponentRepository.getReferenceById(UUID.fromString(businessComponentId));
        return repository.findByBusinessComponentAndIsActiveTrue(businessComponent)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get authentication by ID
     */
    @Transactional(readOnly = true)
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
        if (authId == null) return;
        
        ExternalAuthentication auth = repository.findById(UUID.fromString(authId))
                .orElseThrow(() -> new IllegalArgumentException("Authentication not found: " + authId));
        
        if (!auth.isActive()) {
            throw new IllegalStateException("Authentication configuration is inactive: " + auth.getName());
        }
        
        try {
            switch (auth.getAuthType()) {
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
                default:
                    logger.warn("Unsupported authentication type: {}", auth.getAuthType());
            }
            
            // Update usage statistics
            repository.incrementUsageCount(auth.getId());
            
        } catch (Exception e) {
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
            switch (auth.getAuthType()) {
                case BASIC:
                    if (auth.getUsername() == null || auth.getEncryptedPassword() == null) {
                        throw new IllegalStateException("Basic auth requires username and password");
                    }
                    result.put("status", "CONFIGURED");
                    result.put("message", "Basic authentication is configured");
                    break;
                    
                case OAUTH2:
                    if (auth.getEncryptedAccessToken() != null && auth.getTokenExpiresAt() != null) {
                        if (auth.needsTokenRefresh()) {
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
                    if (auth.getEncryptedApiKey() == null) {
                        throw new IllegalStateException("API key authentication requires API key");
                    }
                    result.put("status", "CONFIGURED");
                    result.put("message", "API key authentication is configured");
                    break;
                    
                default:
                    result.put("status", "UNSUPPORTED");
                    result.put("message", "Authentication type not yet supported");
            }
            
            result.put("success", true);
            
        } catch (Exception e) {
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
        
        if (auth.getRealm() != null) {
            headers.put("WWW-Authenticate", "Basic realm=\"" + auth.getRealm() + "\"");
        }
    }
    
    private void applyOAuth2Auth(ExternalAuthentication auth, Map<String, String> headers) {
        // Check if token needs refresh
        if (auth.needsTokenRefresh() && auth.getRefreshToken() != null) {
            // TODO: Implement OAuth2 token refresh
            logger.warn("OAuth2 token refresh needed but not implemented yet");
        }
        
        String accessToken = encryptionService.decrypt(auth.getEncryptedAccessToken());
        headers.put("Authorization", "Bearer " + accessToken);
    }
    
    private void applyApiKeyAuth(ExternalAuthentication auth, Map<String, String> headers) {
        String apiKey = encryptionService.decrypt(auth.getEncryptedApiKey());
        String headerName = auth.getApiKeyHeader() != null ? auth.getApiKeyHeader() : "X-API-Key";
        
        if (auth.getApiKeyPrefix() != null) {
            apiKey = auth.getApiKeyPrefix() + " " + apiKey;
        }
        
        headers.put(headerName, apiKey);
    }
    
    private void applyOAuth1Auth(ExternalAuthentication auth, Map<String, String> headers, 
                                Map<String, Object> requestContext) {
        // TODO: Implement OAuth1 signature generation
        logger.warn("OAuth1 authentication not implemented yet");
    }
    
    private void populateAndEncryptAuthFields(ExternalAuthentication auth, ExternalAuthenticationDTO dto) {
        switch (auth.getAuthType()) {
            case BASIC:
                auth.setUsername(dto.getUsername());
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                    auth.setEncryptedPassword(encryptionService.encrypt(dto.getPassword()));
                }
                auth.setRealm(dto.getRealm());
                break;
                
            case OAUTH2:
                auth.setClientId(dto.getClientId());
                if (dto.getClientSecret() != null && !dto.getClientSecret().isEmpty()) {
                    auth.setEncryptedClientSecret(encryptionService.encrypt(dto.getClientSecret()));
                }
                auth.setTokenEndpoint(dto.getTokenEndpoint());
                auth.setAuthorizationEndpoint(dto.getAuthorizationEndpoint());
                auth.setRedirectUri(dto.getRedirectUri());
                auth.setScopes(dto.getScopes());
                auth.setGrantType(dto.getGrantType());
                if (dto.getAccessToken() != null && !dto.getAccessToken().isEmpty()) {
                    auth.setEncryptedAccessToken(encryptionService.encrypt(dto.getAccessToken()));
                }
                auth.setRefreshToken(dto.getRefreshToken());
                auth.setTokenExpiresAt(dto.getTokenExpiresAt());
                break;
                
            case API_KEY:
                if (dto.getApiKey() != null && !dto.getApiKey().isEmpty()) {
                    auth.setEncryptedApiKey(encryptionService.encrypt(dto.getApiKey()));
                }
                auth.setApiKeyHeader(dto.getApiKeyHeader());
                auth.setApiKeyPrefix(dto.getApiKeyPrefix());
                auth.setRateLimit(dto.getRateLimit());
                auth.setRateLimitWindowSeconds(dto.getRateLimitWindowSeconds());
                break;
                
            case OAUTH1:
                auth.setConsumerKey(dto.getConsumerKey());
                if (dto.getConsumerSecret() != null && !dto.getConsumerSecret().isEmpty()) {
                    auth.setConsumerSecret(encryptionService.encrypt(dto.getConsumerSecret()));
                }
                auth.setOauth1Token(dto.getOauth1Token());
                if (dto.getOauth1TokenSecret() != null && !dto.getOauth1TokenSecret().isEmpty()) {
                    auth.setOauth1TokenSecret(encryptionService.encrypt(dto.getOauth1TokenSecret()));
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
        // Only send non-sensitive configuration
        switch (auth.getAuthType()) {
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
                if (auth.getTokenExpiresAt() != null) {
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
                dto.setHasConsumerSecret(auth.getConsumerSecret() != null);
                dto.setHasOauth1TokenSecret(auth.getOauth1TokenSecret() != null);
                break;
        }
        
        return dto;
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return userRepository.findByUsername(auth.getName());
        }
        return null;
    }
    
    private List<String> getUpdatedFields(ExternalAuthentication auth, ExternalAuthenticationDTO dto) {
        List<String> updatedFields = new ArrayList<>();
        
        if (!auth.getName().equals(dto.getName())) updatedFields.add("name");
        if (!Objects.equals(auth.getDescription(), dto.getDescription())) updatedFields.add("description");
        
        // Check auth-specific fields based on type
        switch (auth.getAuthType()) {
            case BASIC:
                if (!Objects.equals(auth.getUsername(), dto.getUsername())) updatedFields.add("username");
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) updatedFields.add("password");
                break;
            case OAUTH2:
                if (!Objects.equals(auth.getClientId(), dto.getClientId())) updatedFields.add("clientId");
                if (dto.getClientSecret() != null && !dto.getClientSecret().isEmpty()) updatedFields.add("clientSecret");
                if (dto.getAccessToken() != null && !dto.getAccessToken().isEmpty()) updatedFields.add("accessToken");
                break;
            case API_KEY:
                if (dto.getApiKey() != null && !dto.getApiKey().isEmpty()) updatedFields.add("apiKey");
                break;
        }
        
        return updatedFields;
    }
}