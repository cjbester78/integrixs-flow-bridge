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

@Component("instagramGraphOutboundAdapter")
public class InstagramGraphOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(InstagramGraphOutboundAdapter.class);

    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private final RateLimiterService rateLimiterService;
    private final CredentialEncryptionService credentialEncryptionService;
    private InstagramGraphApiConfig config;
    
    @Autowired
    public InstagramGraphOutboundAdapter(
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public MessageDTO processMessage(MessageDTO message) throws AdapterException {
        if (config == null) {
            throw new AdapterException("Instagram configuration is not set");
        }
        
        String operation = (String) message.getHeaders().get("operation");
        if (operation == null) {
            throw new AdapterException("Operation header is required");
        }
        
        log.debug("Processing Instagram operation: {}", operation);
        
        try {
            switch (operation.toUpperCase()) {
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
                    throw new AdapterException("Unknown operation: " + operation);
            }
        } catch (Exception e) {
            log.error("Error processing Instagram operation: " + operation, e);
            MessageDTO errorResponse = new MessageDTO();
            errorResponse.setCorrelationId(message.getCorrelationId());
            errorResponse.setStatus(MessageStatus.FAILED);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "originalOperation", operation
            ));
            return errorResponse;
        }
    }
    
    private MessageDTO publishImage(MessageDTO message) throws Exception {
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
    
    private MessageDTO publishVideo(MessageDTO message) throws Exception {
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
    
    private MessageDTO publishCarousel(MessageDTO message) throws Exception {
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
    
    private MessageDTO publishReel(MessageDTO message) throws Exception {
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
    
    private MessageDTO publishStory(MessageDTO message) throws Exception {
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
    
    private MessageDTO publishIGTV(MessageDTO message) throws Exception {
        // IGTV is being phased out in favor of Reels
        // Redirect to reel publishing
        return publishReel(message);
    }
    
    private MessageDTO postComment(MessageDTO message) throws Exception {
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
    
    private MessageDTO deleteComment(MessageDTO message) throws Exception {
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
    
    private MessageDTO hideComment(MessageDTO message) throws Exception {
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
    
    private MessageDTO replyToComment(MessageDTO message) throws Exception {
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
    
    private MessageDTO updateMedia(MessageDTO message) throws Exception {
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
    
    private MessageDTO deleteMedia(MessageDTO message) throws Exception {
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
    
    private MessageDTO getMediaInsights(MessageDTO message) throws Exception {
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
    
    private MessageDTO getAccountInfo(MessageDTO message) throws Exception {
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
    
    private MessageDTO getAccountInsights(MessageDTO message) throws Exception {
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
    
    private MessageDTO getAudienceInsights(MessageDTO message) throws Exception {
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
    
    private MessageDTO getHashtagId(MessageDTO message) throws Exception {
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
    
    private MessageDTO getHashtagMedia(MessageDTO message) throws Exception {
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
    
    private MessageDTO searchHashtags(MessageDTO message) throws Exception {
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
    
    private MessageDTO tagProducts(MessageDTO message) throws Exception {
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
    
    private MessageDTO getTaggedProducts(MessageDTO message) throws Exception {
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
    
    private MessageDTO getMediaDiscovery(MessageDTO message) throws Exception {
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
    
    private MessageDTO getUserTags(MessageDTO message) throws Exception {
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
    
    private MessageDTO createSuccessResponse(String messageId, String responseBody, String operation) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setMessageTimestamp(Instant.now());
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
        
        String url = String.format("%s/%s/%s/comments", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "COMMENTS_RETRIEVED");
    }
    
    private MessageDTO getMentions(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/mentioned_media", 
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MENTIONS_RETRIEVED");
    }
    
    private MessageDTO getTaggedMedia(MessageDTO message) throws Exception {
        return getUserTags(message);
    }
    
    private MessageDTO getStories(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/stories", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getInstagramBusinessAccountId());
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "STORIES_RETRIEVED");
    }
    
    private MessageDTO schedulePost(MessageDTO message) throws Exception {
        // Instagram doesn't support scheduling through API yet
        // This would need to be implemented with a scheduling service
        throw new AdapterException("Post scheduling is not yet supported by Instagram API");
    }

}