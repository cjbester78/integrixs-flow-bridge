package com.integrixs.adapters.social.base;

import com.integrixs.adapters.core.AbstractAdapter;
import com.integrixs.adapters.social.base.SocialMediaContent;
import com.integrixs.adapters.social.base.SocialMediaResponse;
import com.integrixs.adapters.social.base.SocialMediaAnalytics;
import com.integrixs.adapters.social.auth.OAuth2Token;
import com.integrixs.shared.dto.MessageDTO;

import java.util.List;
import java.util.Map;

/**
 * Base interface for all social media adapters in Integrixs Flow Bridge
 */
public abstract class SocialMediaAdapter<T extends SocialMediaAdapterConfig> extends AbstractAdapter {

    protected SocialMediaAdapter(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum adapterType) {
        super(adapterType);
    }

    // Authentication Methods
    /**
     * Generates OAuth2 authorization URL for user authentication
     */
    public abstract String generateAuthorizationUrl(String state, Map<String, String> additionalParams);

    /**
     * Exchanges authorization code for access token
     */
    public abstract OAuth2Token exchangeCodeForToken(String code, String state);

    /**
     * Refreshes an expired access token
     */
    public abstract OAuth2Token refreshAccessToken(OAuth2Token expiredToken);

    /**
     * Validates if current credentials are valid
     */
    public abstract boolean validateCredentials(T config);

    // Core Social Media Operations
    /**
     * Publishes content to the social media platform
     */
    public abstract SocialMediaResponse publish(SocialMediaContent content, T config);

    /**
     * Retrieves posts/content from the platform
     */
    public abstract List<SocialMediaContent> getContent(String contentId, T config);

    /**
     * Deletes content from the platform
     */
    public abstract boolean deleteContent(String contentId, T config);

    /**
     * Updates existing content
     */
    public abstract SocialMediaResponse updateContent(String contentId, SocialMediaContent content, T config);

    // Analytics Operations
    /**
     * Retrieves analytics/insights for content or account
     */
    public abstract SocialMediaAnalytics getAnalytics(String entityId, String metricType, T config);

    // Engagement Operations
    /**
     * Retrieves comments for a piece of content
     */
    public abstract List<SocialMediaContent> getComments(String contentId, T config);

    /**
     * Posts a comment/reply
     */
    public abstract SocialMediaResponse postComment(String parentId, String comment, T config);

    // Webhook Support
    /**
     * Registers webhook endpoint with the platform
     */
    public abstract boolean registerWebhook(String webhookUrl, String verifyToken, T config);

    /**
     * Processes incoming webhook events
     */
    public abstract MessageDTO processWebhookEvent(Map<String, Object> event, T config);

    /**
     * Verifies webhook challenge/subscription
     */
    public abstract String verifyWebhookChallenge(Map<String, String> params, T config);

    // Utility Methods
    /**
     * Gets platform - specific API limits
     */
    public abstract Map<String, Integer> getApiLimits();

    /**
     * Converts MessageDTO to platform - specific content format
     */
    protected abstract SocialMediaContent convertToSocialContent(MessageDTO message, T config);

    /**
     * Converts platform response to MessageDTO
     */
    protected abstract MessageDTO convertToFlowMessage(SocialMediaContent content, T config);
}
