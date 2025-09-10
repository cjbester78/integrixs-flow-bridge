package com.integrixs.adapters.social.instagram;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.instagram.InstagramGraphApiConfig.*;
import com.integrixs.core.api.channel.Message;
import com.integrixs.core.exception.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@Component("instagramGraphOutboundAdapter")
public class InstagramGraphOutboundAdapter extends AbstractSocialMediaOutboundAdapter<InstagramGraphApiConfig> {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public InstagramGraphOutboundAdapter(
            InstagramGraphApiConfig config,
            RateLimiterService rateLimiterService,
            OAuth2TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(config, rateLimiterService, tokenRefreshService, credentialEncryptionService);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Message processMessage(Message message) throws AdapterException {
        validateConfiguration();
        
        String operation = (String) message.getHeaders().get("operation");
        if (operation == null) {
            throw new AdapterException("Operation header is required");
        }
        
        log.debug("Processing Instagram operation: {}", operation);
        
        try {
            rateLimiterService.acquire("instagram_api", 1);
            
            Message response;
            switch (operation.toUpperCase()) {
                // Content publishing operations
                case "PUBLISH_IMAGE":
                    response = publishImage(message);
                    break;
                case "PUBLISH_VIDEO":
                    response = publishVideo(message);
                    break;
                case "PUBLISH_CAROUSEL":
                    response = publishCarousel(message);
                    break;
                case "PUBLISH_REEL":
                    response = publishReel(message);
                    break;
                case "PUBLISH_STORY":
                    response = publishStory(message);
                    break;
                case "PUBLISH_IGTV":
                    response = publishIGTV(message);
                    break;
                    
                // Comment operations
                case "POST_COMMENT":
                    response = postComment(message);
                    break;
                case "DELETE_COMMENT":
                    response = deleteComment(message);
                    break;
                case "HIDE_COMMENT":
                    response = hideComment(message);
                    break;
                case "REPLY_TO_COMMENT":
                    response = replyToComment(message);
                    break;
                    
                // Media operations
                case "UPDATE_MEDIA":
                    response = updateMedia(message);
                    break;
                case "DELETE_MEDIA":
                    response = deleteMedia(message);
                    break;
                case "GET_MEDIA_INSIGHTS":
                    response = getMediaInsights(message);
                    break;
                    
                // Account operations
                case "GET_ACCOUNT_INFO":
                    response = getAccountInfo(message);
                    break;
                case "GET_ACCOUNT_INSIGHTS":
                    response = getAccountInsights(message);
                    break;
                case "GET_AUDIENCE_INSIGHTS":
                    response = getAudienceInsights(message);
                    break;
                    
                // Hashtag operations
                case "GET_HASHTAG_ID":
                    response = getHashtagId(message);
                    break;
                case "GET_HASHTAG_MEDIA":
                    response = getHashtagMedia(message);
                    break;
                case "SEARCH_HASHTAGS":
                    response = searchHashtags(message);
                    break;
                    
                // Shopping operations
                case "TAG_PRODUCTS":
                    response = tagProducts(message);
                    break;
                case "GET_TAGGED_PRODUCTS":
                    response = getTaggedProducts(message);
                    break;
                    
                // Discovery operations
                case "GET_MEDIA_DISCOVERY":
                    response = getMediaDiscovery(message);
                    break;
                case "GET_USER_TAGS":
                    response = getUserTags(message);
                    break;
                    
                default:
                    throw new AdapterException("Unknown operation: " + operation);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing Instagram message", e);
            Message errorResponse = new Message();
            errorResponse.setMessageId(message.getMessageId());
            errorResponse.setStatus(MessageStatus.ERROR);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "originalOperation", operation
            ));
            return errorResponse;
        }
    }
    
    private Message publishImage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        // Step 1: Create media container
        String containerUrl = String.format("%s/%s/%s/media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("image_url", payload.path("image_url").asText());
        params.add("caption", payload.path("caption").asText(""));
        
        // Add location if provided
        if (payload.has("location_id")) {
            params.add("location_id", payload.path("location_id").asText());
        }
        
        // Add user tags if provided
        if (payload.has("user_tags")) {
            params.add("user_tags", payload.path("user_tags").toString());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);
        
        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();
        
        // Step 2: Publish the container
        String publishUrl = String.format("%s/%s/%s/media_publish", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);
        
        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);
        
        return createSuccessResponse(message.getMessageId(), publishResponse.getBody(), "IMAGE_PUBLISHED");
    }
    
    private Message publishVideo(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        // Step 1: Create video container
        String containerUrl = String.format("%s/%s/%s/media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("video_url", payload.path("video_url").asText());
        params.add("caption", payload.path("caption").asText(""));
        params.add("media_type", "VIDEO");
        
        if (payload.has("thumb_offset")) {
            params.add("thumb_offset", payload.path("thumb_offset").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);
        
        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();
        
        // Step 2: Check upload status (videos need time to process)
        Thread.sleep(5000); // Wait 5 seconds before checking
        
        String statusUrl = String.format("%s/%s/%s?fields=status_code&access_token=%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            containerId,
            getAccessToken());
        
        ResponseEntity<String> statusResponse = restTemplate.getForEntity(statusUrl, String.class);
        JsonNode statusData = objectMapper.readTree(statusResponse.getBody());
        
        // Wait for video to finish processing
        while (!"FINISHED".equals(statusData.path("status_code").asText())) {
            Thread.sleep(5000);
            statusResponse = restTemplate.getForEntity(statusUrl, String.class);
            statusData = objectMapper.readTree(statusResponse.getBody());
        }
        
        // Step 3: Publish the video
        String publishUrl = String.format("%s/%s/%s/media_publish", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);
        
        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);
        
        return createSuccessResponse(message.getMessageId(), publishResponse.getBody(), "VIDEO_PUBLISHED");
    }
    
    private Message publishCarousel(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ArrayNode items = (ArrayNode) payload.path("items");
        
        if (items.size() < 2 || items.size() > 10) {
            throw new AdapterException("Carousel must have between 2 and 10 items");
        }
        
        // Step 1: Create containers for each item
        List<String> containerIds = new ArrayList<>();
        
        for (JsonNode item : items) {
            String containerUrl = String.format("%s/%s/%s/media", 
                config.getBaseUrl(), 
                config.getApiVersion(), 
                config.getInstagramBusinessAccountId());
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("access_token", getAccessToken());
            params.add("is_carousel_item", "true");
            
            if (item.has("image_url")) {
                params.add("image_url", item.path("image_url").asText());
                params.add("media_type", "IMAGE");
            } else if (item.has("video_url")) {
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
        String carouselUrl = String.format("%s/%s/%s/media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
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
        String publishUrl = String.format("%s/%s/%s/media_publish", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", carouselId);
        
        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);
        
        return createSuccessResponse(message.getMessageId(), publishResponse.getBody(), "CAROUSEL_PUBLISHED");
    }
    
    private Message publishReel(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        // Reels are published similar to videos but with specific requirements
        String containerUrl = String.format("%s/%s/%s/media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("video_url", payload.path("video_url").asText());
        params.add("caption", payload.path("caption").asText(""));
        params.add("media_type", "REELS");
        params.add("share_to_feed", payload.path("share_to_feed").asText("true"));
        
        if (payload.has("cover_url")) {
            params.add("cover_url", payload.path("cover_url").asText());
        }
        
        if (payload.has("audio_name")) {
            params.add("audio_name", payload.path("audio_name").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);
        
        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();
        
        // Wait for processing
        Thread.sleep(10000);
        
        // Publish the reel
        String publishUrl = String.format("%s/%s/%s/media_publish", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);
        
        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);
        
        return createSuccessResponse(message.getMessageId(), publishResponse.getBody(), "REEL_PUBLISHED");
    }
    
    private Message publishStory(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String containerUrl = String.format("%s/%s/%s/media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("media_type", "STORIES");
        
        if (payload.has("image_url")) {
            params.add("image_url", payload.path("image_url").asText());
        } else if (payload.has("video_url")) {
            params.add("video_url", payload.path("video_url").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> containerResponse = restTemplate.postForEntity(containerUrl, request, String.class);
        
        JsonNode containerData = objectMapper.readTree(containerResponse.getBody());
        String containerId = containerData.path("id").asText();
        
        // Publish story
        String publishUrl = String.format("%s/%s/%s/media_publish", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
        MultiValueMap<String, String> publishParams = new LinkedMultiValueMap<>();
        publishParams.add("access_token", getAccessToken());
        publishParams.add("creation_id", containerId);
        
        HttpEntity<MultiValueMap<String, String>> publishRequest = new HttpEntity<>(publishParams, headers);
        ResponseEntity<String> publishResponse = restTemplate.postForEntity(publishUrl, publishRequest, String.class);
        
        return createSuccessResponse(message.getMessageId(), publishResponse.getBody(), "STORY_PUBLISHED");
    }
    
    private Message publishIGTV(Message message) throws Exception {
        // IGTV is being phased out in favor of Reels
        // Redirect to reel publishing
        return publishReel(message);
    }
    
    private Message postComment(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s/comments", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("message", payload.path("message").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "COMMENT_POSTED");
    }
    
    private Message deleteComment(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("comment_id").asText();
        
        String url = String.format("%s/%s/%s?access_token=%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            commentId,
            getAccessToken());
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "COMMENT_DELETED");
    }
    
    private Message hideComment(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("comment_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            commentId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("hide", payload.path("hide").asText("true"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "COMMENT_HIDDEN");
    }
    
    private Message replyToComment(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("comment_id").asText();
        
        String url = String.format("%s/%s/%s/replies", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            commentId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("message", payload.path("message").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "REPLY_POSTED");
    }
    
    private Message updateMedia(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        
        if (payload.has("comment_enabled")) {
            params.add("comment_enabled", payload.path("comment_enabled").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MEDIA_UPDATED");
    }
    
    private Message deleteMedia(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s?access_token=%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId,
            getAccessToken());
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MEDIA_DELETED");
    }
    
    private Message getMediaInsights(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s/insights", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MEDIA_INSIGHTS_RETRIEVED");
    }
    
    private Message getAccountInfo(Message message) throws Exception {
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "ACCOUNT_INFO_RETRIEVED");
    }
    
    private Message getAccountInsights(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/insights", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "ACCOUNT_INSIGHTS_RETRIEVED");
    }
    
    private Message getAudienceInsights(Message message) throws Exception {
        String url = String.format("%s/%s/%s/insights", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AUDIENCE_INSIGHTS_RETRIEVED");
    }
    
    private Message getHashtagId(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String hashtag = payload.path("hashtag").asText().replace("#", "");
        
        String url = String.format("%s/%s/ig_hashtag_search", 
            config.getBaseUrl(), 
            config.getApiVersion());
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("user_id", config.getInstagramBusinessAccountId());
        params.put("q", hashtag);
        
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) -> 
            urlBuilder.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append("&"));
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "HASHTAG_ID_RETRIEVED");
    }
    
    private Message getHashtagMedia(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String hashtagId = payload.path("hashtag_id").asText();
        
        // Get recent media for hashtag
        String recentUrl = String.format("%s/%s/%s/recent_media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            hashtagId);
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("user_id", config.getInstagramBusinessAccountId());
        params.put("fields", "id,media_type,media_url,permalink,caption,timestamp");
        
        StringBuilder urlBuilder = new StringBuilder(recentUrl);
        urlBuilder.append("?");
        params.forEach((key, value) -> 
            urlBuilder.append(key).append("=").append(value).append("&"));
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "HASHTAG_MEDIA_RETRIEVED");
    }
    
    private Message searchHashtags(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String query = payload.path("query").asText();
        
        String url = String.format("%s/%s/ig_hashtag_search", 
            config.getBaseUrl(), 
            config.getApiVersion());
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("user_id", config.getInstagramBusinessAccountId());
        params.put("q", query);
        
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) -> 
            urlBuilder.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append("&"));
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "HASHTAGS_SEARCHED");
    }
    
    private Message tagProducts(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s/product_tags", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("updated_tags", payload.path("product_tags").toString());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "PRODUCTS_TAGGED");
    }
    
    private Message getTaggedProducts(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s/product_tags", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TAGGED_PRODUCTS_RETRIEVED");
    }
    
    private Message getMediaDiscovery(Message message) throws Exception {
        String url = String.format("%s/%s/%s/media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MEDIA_DISCOVERY_RETRIEVED");
    }
    
    private Message getUserTags(Message message) throws Exception {
        String url = String.format("%s/%s/%s/tags", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_TAGS_RETRIEVED");
    }
    
    private Message createSuccessResponse(String messageId, String responseBody, String operation) {
        Message response = new Message();
        response.setMessageId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setTimestamp(Instant.now());
        response.setHeaders(Map.of(
            "operation", operation,
            "source", "instagram"
        ));
        response.setPayload(responseBody);
        return response;
    }
    
    private String getAccessToken() {
        String encryptedToken = config.getAccessToken();
        return credentialEncryptionService.decrypt(encryptedToken);
    }
    
    private void validateConfiguration() throws AdapterException {
        if (config == null) {
            throw new AdapterException("Instagram configuration is not set");
        }
        if (config.getInstagramBusinessAccountId() == null || config.getInstagramBusinessAccountId().isEmpty()) {
            throw new AdapterException("Instagram Business Account ID is required");
        }
        if (config.getAccessToken() == null || config.getAccessToken().isEmpty()) {
            throw new AdapterException("Access token is required");
        }
    }
}