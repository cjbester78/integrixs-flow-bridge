package com.integrixs.backend.adapter;

import com.integrixs.adapters.core.AdapterException;
import com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum;
import com.integrixs.adapters.core.BaseAdapter;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.backend.service.ExternalAuthenticationService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.ExternalAuthentication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced HTTP adapter factory that integrates external authentication configurations
 * with HTTP/HTTPS - based adapters(HTTP, REST, SOAP, WebService, etc.)
 */
@Component
public class EnhancedHttpAdapterFactory {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedHttpAdapterFactory.class);

    @Autowired
    private ExternalAuthenticationService authenticationService;

    @Autowired
    private AdapterFactoryManager factoryManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create an adapter instance with external authentication configured
     */
    public Object createAdapter(CommunicationAdapter adapterConfig) throws AdapterException {
        try {
            // Parse the adapter configuration
            JsonNode configJson = objectMapper.readTree(adapterConfig.getConfiguration());
            Map<String, Object> config = objectMapper.convertValue(configJson, Map.class);

            // Check if this adapter uses HTTP/HTTPS
            if(isHttpBasedAdapter(adapterConfig.getType())) {
                // Apply external authentication if configured
                if(adapterConfig.getExternalAuthentication() != null) {
                    applyExternalAuthentication(config, adapterConfig.getExternalAuthentication());
                }
            }

            // Convert adapter type to the domain model adapter type
            com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum coreType =
                com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.valueOf(adapterConfig.getType().name());

            // Create the appropriate config object based on adapter type and mode
            Object typedConfig = createTypedConfig(coreType, adapterConfig.getMode(), config);

            // Create and initialize the adapter
            return factoryManager.createAndInitialize(coreType, adapterConfig.getMode(), typedConfig);

        } catch(Exception e) {
            logger.error("Failed to create adapter: {}", adapterConfig.getName(), e);
            throw new AdapterException(
                "Failed to create adapter with authentication for " + adapterConfig.getType() + " in " + adapterConfig.getMode() + " mode",
                e);
        }
    }

    /**
     * Check if adapter type uses HTTP/HTTPS protocol
     */
    private boolean isHttpBasedAdapter(com.integrixs.shared.enums.AdapterType type) {
        switch(type) {
            case HTTP:
            case REST:
            case SOAP:
            case ODATA:
                return true;
            default:
                return false;
        }
    }

    /**
     * Apply external authentication configuration to adapter config
     */
    private void applyExternalAuthentication(Map<String, Object> config, ExternalAuthentication auth) {
        logger.info("Applying external authentication ' {}' of type {}", auth.getName(), auth.getAuthType());

        // Remove any existing authentication config to avoid conflicts
        config.remove("authenticationType");
        config.remove("username");
        config.remove("password");
        config.remove("apiKey");
        config.remove("clientId");
        config.remove("clientSecret");
        config.remove("accessToken");

        // Apply authentication based on type
        switch(auth.getAuthType()) {
            case BASIC:
                config.put("authenticationType", "BASIC");
                config.put("basicUsername", auth.getUsername());
                config.put("basicPassword", auth.getEncryptedPassword());
                if(auth.getRealm() != null) {
                    config.put("authRealm", auth.getRealm());
                }
                break;

            case OAUTH2:
                config.put("authenticationType", "OAUTH2");
                config.put("clientId", auth.getClientId());
                config.put("clientSecret", auth.getEncryptedClientSecret());
                config.put("tokenEndpoint", auth.getTokenEndpoint());
                config.put("authorizationEndpoint", auth.getAuthorizationEndpoint());
                config.put("oauthAccessToken", auth.getEncryptedAccessToken());
                config.put("oauthRefreshToken", auth.getRefreshToken());
                if(auth.getScopes() != null) {
                    config.put("oauthScopes", auth.getScopes());
                }
                if(auth.getGrantType() != null) {
                    config.put("oauthGrantType", auth.getGrantType());
                }
                break;

            case API_KEY:
                config.put("authenticationType", "API_KEY");
                config.put("apiKey", auth.getEncryptedApiKey());
                if(auth.getApiKeyHeader() != null) {
                    config.put("apiKeyHeader", auth.getApiKeyHeader());
                }
                if(auth.getApiKeyPrefix() != null) {
                    config.put("apiKeyPrefix", auth.getApiKeyPrefix());
                }
                break;

            case OAUTH1:
                logger.warn("OAuth1 authentication not yet implemented for adapters");
                break;

            default:
                logger.warn("Unknown authentication type: {}", auth.getAuthType());
        }

        // Mark that external auth is being used
        config.put("externalAuthId", auth.getId().toString());
    }

    /**
     * Create typed configuration object based on adapter type and mode
     */
    private Object createTypedConfig(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum type,
                                   AdapterModeEnum mode, Map<String, Object> config) throws Exception {

        // Build the configuration class name
        String configClassName = String.format("com.integrixs.adapters.config.%s%sAdapterConfig",
            type.name().substring(0, 1).toUpperCase() + type.name().substring(1).toLowerCase(),
            mode.name().substring(0, 1).toUpperCase() + mode.name().substring(1).toLowerCase());

        try {
            Class<?> configClass = Class.forName(configClassName);
            return objectMapper.convertValue(config, configClass);
        } catch(ClassNotFoundException e) {
            // Fallback to generic config class if specific one doesn't exist
            logger.debug("Specific config class not found: {}, using generic map", configClassName);
            return config;
        }
    }

    /**
     * Create request context with authentication headers
     */
    public Map<String, String> createAuthenticatedHeaders(String externalAuthId) {
        Map<String, String> headers = new HashMap<>();

        if(externalAuthId != null) {
            authenticationService.applyAuthentication(externalAuthId, headers, new HashMap<>());
        }

        return headers;
    }
}
