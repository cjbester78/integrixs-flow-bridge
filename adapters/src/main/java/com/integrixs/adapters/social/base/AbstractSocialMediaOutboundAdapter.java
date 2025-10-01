package com.integrixs.adapters.social.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.enums.MessageStatus;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Abstract base class for social media outbound adapters.
 * Provides common functionality for posting to social media platforms.
 */
public abstract class AbstractSocialMediaOutboundAdapter extends AbstractOutboundAdapter {

    /**
     * Get the adapter configuration
     */
    public abstract Map<String, Object> getAdapterConfig();

    /**
     * Get the adapter type
     */
    public abstract AdapterConfiguration.AdapterTypeEnum getAdapterType();
    private static final Logger log = LoggerFactory.getLogger(AbstractSocialMediaOutboundAdapter.class);


    protected final RateLimiterService rateLimiterService;
    protected final CredentialEncryptionService credentialEncryptionService;

    protected AbstractSocialMediaOutboundAdapter(RateLimiterService rateLimiterService,
                                                CredentialEncryptionService credentialEncryptionService) {
        super(AdapterConfiguration.AdapterTypeEnum.REST);
        this.rateLimiterService = rateLimiterService;
        this.credentialEncryptionService = credentialEncryptionService;
    }

    /**
     * Process a message to be sent to the social media platform
     */
    public abstract MessageDTO processMessage(MessageDTO message);

    /**
     * Get the adapter configuration
     */
    protected abstract SocialMediaAdapterConfig getConfig();

    /**
     * Execute an API call with rate limiting
     */
    protected <T> T executeApiCall(Callable<T> callable) throws Exception {
        String rateLimiterName = getAdapterType().name() + "_rate_limiter";
        return rateLimiterService.executeWithRateLimit(rateLimiterName, callable);
    }

    /**
     * Get decrypted credential
     */
    protected String getDecryptedCredential(String credentialKey) {
        Map<String, Object> config = getConfig().getConfiguration();
        String encryptedValue = (String) config.get(credentialKey);
        return credentialEncryptionService.decryptIfNeeded(encryptedValue);
    }

    /**
     * Create a success response
     */
    protected MessageDTO createSuccessResponse(String correlationId, String payload, String operation) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(correlationId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "operation", operation,
            "source", getAdapterType(),
            "timestamp", System.currentTimeMillis()
       ));
        response.setPayload(payload);
        return response;
    }

    /**
     * Create an error response
     */
    protected MessageDTO createErrorResponse(MessageDTO originalMessage, String error) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(originalMessage.getCorrelationId());
        response.setStatus(MessageStatus.FAILED);
        response.setHeaders(Map.of(
            "error", error,
            "source", getAdapterType(),
            "timestamp", System.currentTimeMillis()
       ));
        return response;
    }


    /**
     * Parse JSON string to object
     */
    protected Map<String, Object> parseJsonString(String json) {
        try {
            // Simple implementation - in real scenario would use Jackson ObjectMapper
            return Map.of("data", json);
        } catch(Exception e) {
            log.error("Error parsing JSON", e);
            return Map.of();
        }
    }
}
