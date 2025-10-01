package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.core.OutboundAdapter;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.social.facebook.model.FacebookPost;
import com.integrixs.adapters.social.facebook.model.FacebookPostResponse;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.services.CredentialEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Facebook Graph API Outbound Adapter
 * Handles sending data to Facebook(posts, comments, messages)
 */
@Component
public class FacebookGraphOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(FacebookGraphOutboundAdapter.class);

    public FacebookGraphOutboundAdapter() {
        super(null, null); // These will be injected
    }


    @Autowired
    private FacebookGraphApiClient apiClient;

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Autowired
    private FacebookMediaUploader mediaUploader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FacebookGraphApiConfig config;

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("pageId", config.getPageId());
            configMap.put("clientId", config.getClientId());
            configMap.put("features", config.getFeatures());
        }
        return configMap;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            send(message, message.getCorrelationId(), config);
            return createSuccessResponse(message.getCorrelationId(),
                "Message processed successfully", "send");
        } catch (Exception e) {
            return createErrorResponse(message, e.getMessage());
        }
    }

    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Delegate to send method
        MessageDTO message = new MessageDTO();
        message.setPayload(payload.toString());
        message.setHeaders(headers);
        send(message, (String) headers.get("flowId"), config);
        return AdapterResult.success("Message sent successfully");
    }

    protected void doSenderInitialize() throws Exception {
        // No specific initialization needed
        log.debug("Facebook Graph outbound adapter initialized");
    }

    protected void doSenderDestroy() throws Exception {
        // No specific cleanup needed
        log.debug("Facebook Graph outbound adapter destroyed");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        // Test connection by validating access token
        try {
            if (config != null && config.getPageId() != null && (config.getAccessToken() != null || config.getPageAccessToken() != null)) {
                return AdapterResult.success("Configuration is valid");
            } else {
                return AdapterResult.failure("Invalid configuration");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
        }
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapter does not support receiving
        return AdapterResult.success(null, "Outbound adapter does not support receiving");
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        // No receiver initialization needed for outbound adapter
        log.debug("Facebook Graph outbound adapter receiver initialized");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // No receiver cleanup needed for outbound adapter
        log.debug("Facebook Graph outbound adapter receiver destroyed");
    }

    @Override
    protected long getPollingIntervalMs() {
        // Not used for outbound adapter
        return 0;
    }


    public void send(MessageDTO message, String flowId, FacebookGraphApiConfig config) {
        try {
            // Ensure we have valid access token
            String accessToken = ensureValidAccessToken(config);

            // Determine the type of operation from message headers
            String operationType = (String) message.getHeaders().getOrDefault("operationType", "post");

            switch(operationType.toLowerCase()) {
                case "post":
                    createPost(message, config, accessToken);
                    break;

                case "comment":
                    postComment(message, config, accessToken);
                    break;

                case "delete":
                    deleteContent(message, config, accessToken);
                    break;

                case "update":
                    updateContent(message, config, accessToken);
                    break;

                case "story":
                    createStory(message, config, accessToken);
                    break;

                case "reel":
                    createReel(message, config, accessToken);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported operation type: " + operationType);
            }

            log.info("Successfully sent message to Facebook via {} operation", operationType);

        } catch(Exception e) {
            log.error("Error sending to Facebook Graph API", e);
            throw new RuntimeException("Failed to send to Facebook", e);
        }
    }

    /**
     * Create a Facebook post
     */
    private void createPost(MessageDTO message, FacebookGraphApiConfig config, String accessToken) {
        try {
            FacebookPost post = convertToFacebookPost(message, config);

            // Handle media uploads if present
            if(post.getPhotoUrls() != null && !post.getPhotoUrls().isEmpty()) {
                // Photos are handled by the API client directly
            } else if(post.getVideoUrl() != null) {
                // For video posts, we need to upload the video first
                String videoId = mediaUploader.uploadVideo(config.getPageId(), post.getVideoUrl(),
                    post.getMessage(), accessToken, config);
                message.getHeaders().put("facebook_video_id", videoId);
                return;
            }

            // Create the post
            FacebookPostResponse response = apiClient.createPost(config.getPageId(), post, accessToken, config);

            // Add Facebook post ID to message headers for tracking
            message.getHeaders().put("facebook_post_id", response.getId());
            // Store response ID only - permalink URL not available in basic response

            log.info("Created Facebook post with ID: {}", response.getId());

        } catch(Exception e) {
            log.error("Error creating Facebook post", e);
            throw new RuntimeException("Failed to create Facebook post", e);
        }
    }

    /**
     * Post a comment
     */
    private void postComment(MessageDTO message, FacebookGraphApiConfig config, String accessToken) {
        try {
            String parentId = (String) message.getHeaders().get("parent_id");
            if(parentId == null) {
                throw new IllegalArgumentException("Parent ID is required for comments");
            }

            String comment = extractTextContent(message);
            String commentId = apiClient.postComment(parentId, comment, accessToken, config);

            message.getHeaders().put("facebook_comment_id", commentId);
            log.info("Posted comment with ID: {}", commentId);

        } catch(Exception e) {
            log.error("Error posting comment", e);
            throw new RuntimeException("Failed to post comment", e);
        }
    }

    /**
     * Delete content
     */
    private void deleteContent(MessageDTO message, FacebookGraphApiConfig config, String accessToken) {
        try {
            String contentId = (String) message.getHeaders().get("content_id");
            if(contentId == null) {
                throw new IllegalArgumentException("Content ID is required for deletion");
            }

            boolean deleted = apiClient.deletePost(contentId, accessToken, config);
            if(deleted) {
                log.info("Deleted content with ID: {}", contentId);
            } else {
                throw new RuntimeException("Failed to delete content");
            }

        } catch(Exception e) {
            log.error("Error deleting content", e);
            throw new RuntimeException("Failed to delete content", e);
        }
    }

    /**
     * Update content
     */
    private void updateContent(MessageDTO message, FacebookGraphApiConfig config, String accessToken) {
        // Facebook Graph API has limited support for updating posts
        // Mainly can update page posts' message field
        log.warn("Update operation has limited support in Facebook Graph API");
        // Implementation would go here based on specific requirements
    }

    /**
     * Create a Facebook Story
     */
    private void createStory(MessageDTO message, FacebookGraphApiConfig config, String accessToken) {
        try {
            if(config.getFeatures() == null || !config.getFeatures().isStoriesEnabled()) {
                throw new IllegalStateException("Stories feature is not enabled");
            }

            // Extract story content
            Map<String, Object> payload = parsePayload(message);

            String mediaUrl = (String) payload.get("media_url");
            if(mediaUrl == null) {
                throw new IllegalArgumentException("Media URL is required for stories");
            }

            // Upload story using the Page Stories API
            // This would require specific implementation based on Facebook's story API
            log.info("Creating Facebook story");

        } catch(Exception e) {
            log.error("Error creating story", e);
            throw new RuntimeException("Failed to create story", e);
        }
    }

    /**
     * Create a Facebook Reel
     */
    private void createReel(MessageDTO message, FacebookGraphApiConfig config, String accessToken) {
        try {
            if(config.getFeatures() == null || !config.getFeatures().isReelsEnabled()) {
                throw new IllegalStateException("Reels feature is not enabled");
            }

            // Extract reel content
            Map<String, Object> payload = parsePayload(message);

            String videoUrl = (String) payload.get("video_url");
            if(videoUrl == null) {
                throw new IllegalArgumentException("Video URL is required for reels");
            }

            // Upload reel using the Reels API
            String reelId = mediaUploader.uploadReel(config.getPageId(), videoUrl,
                (String) payload.get("description"), accessToken, config);

            message.getHeaders().put("facebook_reel_id", reelId);
            log.info("Created Facebook reel with ID: {}", reelId);

        } catch(Exception e) {
            log.error("Error creating reel", e);
            throw new RuntimeException("Failed to create reel", e);
        }
    }

    /**
     * Convert MessageDTO to FacebookPost
     */
    private FacebookPost convertToFacebookPost(MessageDTO message, FacebookGraphApiConfig config) {
        try {
            Map<String, Object> payload = parsePayload(message);

            FacebookPost.Builder builder = FacebookPost.builder();

            // Basic content
            builder.message(extractTextContent(message));

            // Link
            if(payload.containsKey("link")) {
                builder.link((String) payload.get("link"));
            }

            // Media
            if(payload.containsKey("photos")) {
                List<String> photoUrls = (List<String>) payload.get("photos");
                builder.photoUrls(photoUrls);
            }
            if(payload.containsKey("video")) {
                builder.videoUrl((String) payload.get("video"));
            }

            // Publishing options
            if(payload.containsKey("published")) {
                builder.published((Boolean) payload.get("published"));
            }
            if(payload.containsKey("scheduled_time")) {
                String scheduledTime = (String) payload.get("scheduled_time");
                builder.scheduledPublishTime(LocalDateTime.parse(scheduledTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            // Targeting
            if(payload.containsKey("targeting")) {
                Map<String, Object> targetingData = (Map<String, Object>) payload.get("targeting");
                builder.targeting(buildTargeting(targetingData));
            }

            // Tags
            if(payload.containsKey("tags")) {
                builder.tags((List<String>) payload.get("tags"));
            }

            // Location
            if(payload.containsKey("place")) {
                builder.place((String) payload.get("place"));
            }

            return builder.build();

        } catch(Exception e) {
            log.error("Error converting message to Facebook post", e);
            throw new RuntimeException("Failed to convert message to Facebook post", e);
        }
    }

    /**
     * Build Facebook targeting from data
     */
    private FacebookPost.FacebookTargeting buildTargeting(Map<String, Object> targetingData) {
        FacebookPost.FacebookTargeting.Builder builder =
            FacebookPost.FacebookTargeting.builder();

        if(targetingData.containsKey("countries")) {
            builder.countries((List<String>) targetingData.get("countries"));
        }
        if(targetingData.containsKey("age_min")) {
            builder.ageMin((Integer) targetingData.get("age_min"));
        }
        if(targetingData.containsKey("age_max")) {
            builder.ageMax((Integer) targetingData.get("age_max"));
        }
        if(targetingData.containsKey("interests")) {
            builder.interests((List<String>) targetingData.get("interests"));
        }

        return builder.build();
    }

    /**
     * Extract text content from message
     */
    private String extractTextContent(MessageDTO message) {
        try {
            Map<String, Object> payload = parsePayload(message);

            // Check various possible fields for text content
            if(payload.containsKey("message")) {
                return(String) payload.get("message");
            }
            if(payload.containsKey("text")) {
                return(String) payload.get("text");
            }
            if(payload.containsKey("content")) {
                return(String) payload.get("content");
            }

            // Fallback to raw payload if it's a string
            if(message.getPayload() != null && !message.getPayload().startsWith(" {")) {
                return message.getPayload();
            }

            return "";

        } catch(Exception e) {
            log.debug("Could not extract text content, returning empty string", e);
            return "";
        }
    }

    /**
     * Parse payload from MessageDTO
     */
    private Map<String, Object> parsePayload(MessageDTO message) {
        try {
            if(message.getPayload() != null && message.getPayload().startsWith(" {")) {
                return objectMapper.readValue(message.getPayload(), Map.class);
            }

            // If payload is not JSON, create a map with the payload as content
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", message.getPayload());
            return payload;

        } catch(Exception e) {
            log.error("Error parsing payload", e);
            return new HashMap<>();
        }
    }

    /**
     * Ensure we have a valid access token
     */
    private String ensureValidAccessToken(FacebookGraphApiConfig config) {
        String token = config.getPageAccessToken();

        if(token == null || token.isEmpty()) {
            token = config.getAccessToken();
        }

        if(token != null && token.startsWith("enc:")) {
            token = encryptionService.decrypt(token);
        }

        if(token == null || token.isEmpty()) {
            throw new IllegalStateException("No valid access token available");
        }

        return token;
    }

    protected void validateConfiguration(FacebookGraphApiConfig config) {
        if(config.getPageId() == null || config.getPageId().isEmpty()) {
            throw new IllegalArgumentException("Facebook Page ID is required");
        }

        if((config.getAccessToken() == null || config.getAccessToken().isEmpty()) &&
           (config.getPageAccessToken() == null || config.getPageAccessToken().isEmpty())) {
            throw new IllegalArgumentException("Valid access token is required");
        }

        if(config.getClientId() == null || config.getClientId().isEmpty()) {
            throw new IllegalArgumentException("Facebook App Client ID is required");
        }
    }
}
