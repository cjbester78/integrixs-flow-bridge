package com.integrixs.adapters.social.instagram;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.instagram.InstagramGraphApiConfig;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;

@Component("instagramGraphOutboundAdapter")
public class InstagramGraphOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(InstagramGraphOutboundAdapter.class);

    @Value("${integrix.adapters.instagram.graph.media-endpoint:/media}")
    private String mediaEndpointPath;

    @Value("${integrix.adapters.instagram.graph.media-publish-endpoint:/media_publish}")
    private String mediaPublishEndpointPath;

    @Value("${integrix.adapters.instagram.graph.comments-endpoint:/comments}")
    private String commentsEndpointPath;

    @Value("${integrix.adapters.instagram.graph.replies-endpoint:/replies}")
    private String repliesEndpointPath;

    @Value("${integrix.adapters.instagram.graph.product-tags-endpoint:/product_tags}")
    private String productTagsEndpointPath;

    @Value("${integrix.adapters.instagram.graph.stories-endpoint:/stories}")
    private String storiesEndpointPath;

    @Value("${integrix.adapters.instagram.graph.tags-endpoint:/tags}")
    private String tagsEndpointPath;

    @Value("${integrix.adapters.instagram.graph.insights-endpoint:/insights}")
    private String insightsEndpointPath;

    @Value("${integrix.adapters.instagram.graph.hashtag-search-endpoint:/ig_hashtag_search}")
    private String hashtagSearchEndpointPath;

    @Value("${integrix.adapters.instagram.graph.recent-media-endpoint:/recent_media}")
    private String recentMediaEndpointPath;

    @Value("${integrix.adapters.instagram.graph.mentioned-media-endpoint:/mentioned_media}")
    private String mentionedMediaEndpointPath;

    @Value("${integrix.adapters.instagram.graph.video-processing-initial-delay:5000}")
    private long videoProcessingInitialDelay;

    @Value("${integrix.adapters.instagram.graph.video-processing-check-interval:5000}")
    private long videoProcessingCheckInterval;

    @Value("${integrix.adapters.instagram.graph.reel-processing-delay:10000}")
    private long reelProcessingDelay;

    @Value("${integrix.adapters.instagram.graph.default-polling-interval:60000}")
    private long defaultPollingInterval;

    @Value("${integrix.adapters.instagram.graph.carousel-min-items:2}")
    private int carouselMinItems;

    @Value("${integrix.adapters.instagram.graph.carousel-max-items:10}")
    private int carouselMaxItems;

    @Value("${integrix.adapters.instagram.graph.default-api-version:v18.0}")
    private String defaultApiVersion;

    @Value("${integrix.adapters.instagram.graph.default-api-base-url:https://graph.facebook.com}")
    private String defaultApiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private InstagramGraphApiConfig config;

    @Autowired
    public InstagramGraphOutboundAdapter(
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(rateLimiterService, credentialEncryptionService);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        if(config == null) {
            log.error("Instagram configuration is not set");
            return createErrorResponse(message, "Instagram configuration is not set");
        }

        String operation = (String) message.getHeaders().get("operation");
        if(operation == null) {
            log.error("Operation header is required");
            return createErrorResponse(message, "Operation header is required");
        }

        log.debug("Processing Instagram operation: {}", operation);

        try {
            switch(operation.toUpperCase()) {
                case "PUBLISH_IMAGE":
                    return publishImage(message);
                case "PUBLISH_VIDEO":
                    return publishVideo(message);
                case "PUBLISH_CAROUSEL":
                    return publishCarousel(message);
                case "PUBLISH_STORY":
                    return publishStory(message);
                case "GET_INSIGHTS":
                    return getInsights(message);
                case "GET_MEDIA":
                    return getMedia(message);
                case "GET_COMMENTS":
                    return getComments(message);
                case "REPLY_TO_COMMENT":
                    return replyToComment(message);
                case "DELETE_COMMENT":
                    return deleteComment(message);
                case "HIDE_COMMENT":
                    return hideComment(message);
                case "GET_MENTIONS":
                    return getMentions(message);
                case "GET_TAGGED_MEDIA":
                    return getTaggedMedia(message);
                case "GET_STORIES":
                    return getStories(message);
                case "SCHEDULE_POST":
                    return schedulePost(message);
                default:
                    log.error("Unknown operation: {}", operation);
                    return createErrorResponse(message, "Unknown operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error processing Instagram operation: " + operation, e);
            return createErrorResponse(message, e.getMessage());
        }
    }

    private MessageDTO publishImage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Step 1: Create media container
        String containerUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("image_url", payload.path("image_url").asText());
        params.add("caption", payload.path("caption").asText(""));

        // Add location if provided
        if(payload.has("location_id")) {
            params.add("location_id", payload.path("location_id").asText());
        }

        // Add user tags if provided
        if(payload.has("user_tags")) {
            params.add("user_tags", payload.path("user_tags").toString());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);

        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();

        // Step 2: Publish the container
        String publishUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaPublishEndpointPath);

        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);

        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);

        return createSuccessResponse(message.getCorrelationId(), publishResponse.getBody(), "IMAGE_PUBLISHED");
    }

    private MessageDTO publishVideo(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Step 1: Create video container
        String containerUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("video_url", payload.path("video_url").asText());
        params.add("caption", payload.path("caption").asText(""));
        params.add("media_type", "VIDEO");

        if(payload.has("thumb_offset")) {
            params.add("thumb_offset", payload.path("thumb_offset").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);

        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();

        // Step 2: Check upload status(videos need time to process)
        Thread.sleep(videoProcessingInitialDelay);

        String statusUrl = String.format("%s/%s?fields=status_code&access_token=%s",
            config.getBaseUrl(),
            containerId,
            getAccessToken());

        ResponseEntity<String> statusResponse = restTemplate.getForEntity(statusUrl, String.class);
        JsonNode statusData = objectMapper.readTree(statusResponse.getBody());

        // Wait for video to finish processing
        while(!"FINISHED".equals(statusData.path("status_code").asText())) {
            Thread.sleep(videoProcessingCheckInterval);
            statusResponse = restTemplate.getForEntity(statusUrl, String.class);
            statusData = objectMapper.readTree(statusResponse.getBody());
        }

        // Step 3: Publish the video
        String publishUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaPublishEndpointPath);

        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);

        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);

        return createSuccessResponse(message.getCorrelationId(), publishResponse.getBody(), "VIDEO_PUBLISHED");
    }

    private MessageDTO publishCarousel(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ArrayNode items = (ArrayNode) payload.path("items");

        if(items.size() < carouselMinItems || items.size() > carouselMaxItems) {
            log.error("Carousel must have between {} and {} items", carouselMinItems, carouselMaxItems);
            return createErrorResponse(message,
                String.format("Carousel must have between %d and %d items", carouselMinItems, carouselMaxItems));
        }

        // Step 1: Create containers for each item
        List<String> containerIds = new ArrayList<>();

        for(JsonNode item : items) {
            String containerUrl = String.format("%s/%s%s",
                config.getBaseUrl(),
                config.getInstagramBusinessAccountId(),
                mediaEndpointPath);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("access_token", getAccessToken());
            params.add("is_carousel_item", "true");

            if(item.has("image_url")) {
                params.add("image_url", item.path("image_url").asText());
                params.add("media_type", "IMAGE");
            } else if(item.has("video_url")) {
                params.add("video_url", item.path("video_url").asText());
                params.add("media_type", "VIDEO");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(containerUrl, request, String.class);

            JsonNode containerData = objectMapper.readTree(response.getBody());
            containerIds.add(containerData.path("id").asText());
        }

        // Step 2: Create carousel container
        String carouselUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        MultiValueMap<String, String> carouselParams = new LinkedMultiValueMap<>();
        carouselParams.add("access_token", getAccessToken());
        carouselParams.add("media_type", "CAROUSEL");
        carouselParams.add("caption", payload.path("caption").asText(""));
        carouselParams.add("children", String.join(",", containerIds));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> carouselRequest = new HttpEntity<>(carouselParams, headers);
        ResponseEntity<String> carouselResponse = restTemplate.postForEntity(carouselUrl, carouselRequest, String.class);

        JsonNode carouselData = objectMapper.readTree(carouselResponse.getBody());
        String carouselId = carouselData.path("id").asText();

        // Step 3: Publish carousel
        String publishUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaPublishEndpointPath);

        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", carouselId);

        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);

        return createSuccessResponse(message.getCorrelationId(), publishResponse.getBody(), "CAROUSEL_PUBLISHED");
    }

    private MessageDTO publishReel(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Reels are published similar to videos but with specific requirements
        String containerUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("video_url", payload.path("video_url").asText());
        params.add("caption", payload.path("caption").asText(""));
        params.add("media_type", "REELS");
        params.add("share_to_feed", payload.path("share_to_feed").asText("true"));

        if(payload.has("cover_url")) {
            params.add("cover_url", payload.path("cover_url").asText());
        }

        if(payload.has("audio_name")) {
            params.add("audio_name", payload.path("audio_name").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);

        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();

        // Wait for processing
        Thread.sleep(reelProcessingDelay);

        // Publish the reel
        String publishUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaPublishEndpointPath);

        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);

        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);

        return createSuccessResponse(message.getCorrelationId(), publishResponse.getBody(), "REEL_PUBLISHED");
    }

    private MessageDTO publishStory(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String containerUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("media_type", "STORIES");

        if(payload.has("image_url")) {
            params.add("image_url", payload.path("image_url").asText());
        } else if(payload.has("video_url")) {
            params.add("video_url", payload.path("video_url").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);

        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();

        // Publish story
        String publishUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaPublishEndpointPath);

        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);

        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);

        return createSuccessResponse(message.getCorrelationId(), publishResponse.getBody(), "STORY_PUBLISHED");
    }

    private MessageDTO publishIGTV(MessageDTO message) throws Exception {
        // IGTV is being phased out in favor of Reels
        // Redirect to reel publishing
        return publishReel(message);
    }

    private MessageDTO postComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            mediaId,
            commentsEndpointPath);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("message", payload.path("message").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "COMMENT_POSTED");
    }

    private MessageDTO deleteComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("comment_id").asText();

        String url = String.format("%s/%s?access_token=%s",
            config.getBaseUrl(),
            commentId,
            getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "COMMENT_DELETED");
    }

    private MessageDTO hideComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("comment_id").asText();

        String url = String.format("%s/%s",
            config.getBaseUrl(),
            commentId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("hide", payload.path("hide").asText("true"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "COMMENT_HIDDEN");
    }

    private MessageDTO replyToComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("comment_id").asText();

        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            commentId,
            repliesEndpointPath);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("message", payload.path("message").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "REPLY_POSTED");
    }

    private MessageDTO updateMedia(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s",
            config.getBaseUrl(),
            mediaId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());

        if(payload.has("comment_enabled")) {
            params.add("comment_enabled", payload.path("comment_enabled").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "MEDIA_UPDATED");
    }

    private MessageDTO deleteMedia(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s?access_token=%s",
            config.getBaseUrl(),
            mediaId,
            getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "MEDIA_DELETED");
    }

    private MessageDTO getMediaInsights(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            mediaId,
            insightsEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("metric", payload.path("metrics").asText("engagement,impressions,reach,saved"));

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "MEDIA_INSIGHTS_RETRIEVED");
    }

    private MessageDTO getAccountInfo(MessageDTO message) throws Exception {
        String url = String.format("%s/%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId());

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,username,name,biography,website,followers_count,follows_count," +
                            "media_count,profile_picture_url,is_verified");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "ACCOUNT_INFO_RETRIEVED");
    }

    private MessageDTO getAccountInsights(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            insightsEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("metric", payload.path("metrics").asText(
            "impressions,reach,profile_views,website_clicks,follower_count"));
        params.put("period", payload.path("period").asText("day"));

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "ACCOUNT_INSIGHTS_RETRIEVED");
    }

    private MessageDTO getAudienceInsights(MessageDTO message) throws Exception {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            insightsEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("metric", "audience_city,audience_country,audience_gender_age,audience_locale");
        params.put("period", "lifetime");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AUDIENCE_INSIGHTS_RETRIEVED");
    }

    private MessageDTO getHashtagId(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String hashtag = payload.path("hashtag").asText().replace("#", "");

        String url = String.format("%s%s",
            config.getBaseUrl(),
            hashtagSearchEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("user_id", config.getInstagramBusinessAccountId());
        params.put("q", hashtag);

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append(" = ").append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "HASHTAG_ID_RETRIEVED");
    }

    private MessageDTO getHashtagMedia(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String hashtagId = payload.path("hashtag_id").asText();

        // Get recent media for hashtag
        String recentUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            hashtagId,
            recentMediaEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("user_id", config.getInstagramBusinessAccountId());
        params.put("fields", "id,media_type,media_url,permalink,caption,timestamp");

        StringBuilder urlBuilder = new StringBuilder(recentUrl);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append(" = ").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "HASHTAG_MEDIA_RETRIEVED");
    }

    private MessageDTO searchHashtags(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String query = payload.path("query").asText();

        String url = String.format("%s%s",
            config.getBaseUrl(),
            hashtagSearchEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("user_id", config.getInstagramBusinessAccountId());
        params.put("q", query);

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append(" = ").append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "HASHTAGS_SEARCHED");
    }

    private MessageDTO tagProducts(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            mediaId,
            productTagsEndpointPath);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("updated_tags", payload.path("product_tags").toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "PRODUCTS_TAGGED");
    }

    private MessageDTO getTaggedProducts(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            mediaId,
            productTagsEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TAGGED_PRODUCTS_RETRIEVED");
    }

    private MessageDTO getMediaDiscovery(MessageDTO message) throws Exception {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,media_type,media_url,thumbnail_url,permalink,caption," +
                            "timestamp,like_count,comments_count");
        params.put("limit", "25");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "MEDIA_DISCOVERY_RETRIEVED");
    }

    private MessageDTO getUserTags(MessageDTO message) throws Exception {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            tagsEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,media_type,media_url,caption,permalink,timestamp");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_TAGS_RETRIEVED");
    }

    @Override
    protected MessageDTO createSuccessResponse(String correlationId, String payload, String operation) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(correlationId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setMessageTimestamp(Instant.now());
        response.setHeaders(Map.of(
            "operation", operation,
            "source", "instagram"
       ));
        response.setPayload(payload);
        return response;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("instagramBusinessAccountId", config.getInstagramBusinessAccountId());
            configMap.put("baseUrl", config.getBaseUrl());
            configMap.put("apiVersion", config.getApiVersion());
            configMap.put("accessToken", config.getAccessToken());
        }
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    private String getAccessToken() {
        return getDecryptedCredential("accessToken");
    }

    private boolean validateConfiguration() {
        if(config == null) {
            log.error("Instagram configuration is not set");
            return false;
        }
        if(config.getInstagramBusinessAccountId() == null || config.getInstagramBusinessAccountId().isEmpty()) {
            log.error("Instagram Business Account ID is required");
            return false;
        }
        if(config.getAccessToken() == null || config.getAccessToken().isEmpty()) {
            log.error("Access token is required");
            return false;
        }
        return true;
    }

    public void setConfiguration(InstagramGraphApiConfig config) {
        this.config = config;
    }

    private MessageDTO getInsights(MessageDTO message) throws Exception {
        return getMediaInsights(message);
    }

    private MessageDTO getMedia(MessageDTO message) throws Exception {
        return getMediaDiscovery(message);
    }

    private MessageDTO getComments(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            mediaId,
            commentsEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,text,username,timestamp,like_count");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "COMMENTS_RETRIEVED");
    }

    private MessageDTO getMentions(MessageDTO message) throws Exception {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mentionedMediaEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,media_type,media_url,caption,permalink,timestamp");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "MENTIONS_RETRIEVED");
    }

    private MessageDTO getTaggedMedia(MessageDTO message) throws Exception {
        return getUserTags(message);
    }

    private MessageDTO getStories(MessageDTO message) throws Exception {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            storiesEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,media_type,media_url,permalink,timestamp");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append("=").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "STORIES_RETRIEVED");
    }

    private MessageDTO schedulePost(MessageDTO message) throws Exception {
        // Instagram doesn't support scheduling through API yet
        // This would need to be implemented with a scheduling service
        log.error("Post scheduling is not yet supported by Instagram API");
        return createErrorResponse(message, "Post scheduling is not yet supported by Instagram API");
    }

    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Convert the payload to MessageDTO for processing
        MessageDTO message = new MessageDTO();
        message.setPayload(payload.toString());
        message.setHeaders(headers);
        message.setCorrelationId(UUID.randomUUID().toString());

        MessageDTO result = processMessage(message);
        return AdapterResult.success(result.getPayload(), "Message sent successfully");
    }

    protected void doSenderInitialize() throws Exception {
        log.debug("Initializing Instagram Graph sender");
        if (!validateConfiguration()) {
            throw new Exception("Invalid Instagram configuration");
        }
    }

    protected void doSenderDestroy() throws Exception {
        log.debug("Destroying Instagram Graph sender");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = String.format("%s/%s",
                config.getBaseUrl() != null ? config.getBaseUrl() : defaultApiBaseUrl,
                config.getInstagramBusinessAccountId());

            Map<String, String> params = new HashMap<>();
            params.put("access_token", getAccessToken());
            params.put("fields", "id,name");

            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?");
            params.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                urlBuilder.toString(), HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Instagram Graph API connection successful");
            } else {
                return AdapterResult.failure("Instagram Graph API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error testing Instagram Graph connection", e);
            return AdapterResult.failure("Failed to test Instagram Graph connection: " + e.getMessage());
        }
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapter does not support receiving
        return AdapterResult.success(null, "Outbound adapter does not support receiving");
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        log.debug("Initializing Instagram Graph receiver");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.debug("Destroying Instagram Graph receiver");
    }

    @Override
    protected long getPollingIntervalMs() {
        return defaultPollingInterval;
    }

}
