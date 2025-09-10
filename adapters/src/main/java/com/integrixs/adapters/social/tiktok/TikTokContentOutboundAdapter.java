package com.integrixs.adapters.social.tiktok;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.tiktok.TikTokContentApiConfig.*;
import com.integrixs.core.api.channel.Message;
import com.integrixs.core.exception.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("tikTokContentOutboundAdapter")
public class TikTokContentOutboundAdapter extends AbstractSocialMediaOutboundAdapter<TikTokContentApiConfig> {
    
    private static final String TIKTOK_API_BASE = "https://open-api.tiktok.com";
    private static final String TIKTOK_API_VERSION = "/v1.3";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public TikTokContentOutboundAdapter(
            TikTokContentApiConfig config,
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
    protected Message processOutboundMessage(Message message) {
        try {
            String operation = (String) message.getHeaders().get("operation");
            if (operation == null) {
                throw new AdapterException("Operation header is required");
            }
            
            log.info("Processing TikTok Content outbound operation: {}", operation);
            
            // Refresh token if needed
            refreshAccessTokenIfNeeded();
            
            switch (operation.toUpperCase()) {
                // Video operations
                case "PUBLISH_VIDEO":
                    return publishVideo(message);
                case "DELETE_VIDEO":
                    return deleteVideo(message);
                case "UPDATE_VIDEO_CAPTION":
                    return updateVideoCaption(message);
                case "GET_VIDEO_INFO":
                    return getVideoInfo(message);
                case "GET_VIDEO_LIST":
                    return getVideoList(message);
                    
                // Comment operations
                case "POST_COMMENT":
                    return postComment(message);
                case "DELETE_COMMENT":
                    return deleteComment(message);
                case "GET_COMMENTS":
                    return getComments(message);
                case "REPLY_TO_COMMENT":
                    return replyToComment(message);
                    
                // User operations
                case "GET_USER_INFO":
                    return getUserInfo(message);
                case "UPDATE_PROFILE":
                    return updateProfile(message);
                case "GET_FOLLOWERS":
                    return getFollowers(message);
                case "GET_FOLLOWING":
                    return getFollowing(message);
                    
                // Analytics operations
                case "GET_VIDEO_ANALYTICS":
                    return getVideoAnalytics(message);
                case "GET_PROFILE_ANALYTICS":
                    return getProfileAnalytics(message);
                case "GET_CONTENT_INSIGHTS":
                    return getContentInsights(message);
                    
                // Trending operations
                case "GET_TRENDING_HASHTAGS":
                    return getTrendingHashtags(message);
                case "GET_TRENDING_SOUNDS":
                    return getTrendingSounds(message);
                case "GET_TRENDING_EFFECTS":
                    return getTrendingEffects(message);
                    
                // Music operations
                case "SEARCH_MUSIC":
                    return searchMusic(message);
                case "GET_MUSIC_INFO":
                    return getMusicInfo(message);
                case "ADD_MUSIC_TO_FAVORITES":
                    return addMusicToFavorites(message);
                    
                // Effect operations
                case "SEARCH_EFFECTS":
                    return searchEffects(message);
                case "GET_EFFECT_INFO":
                    return getEffectInfo(message);
                    
                // Live streaming operations
                case "START_LIVE_STREAM":
                    return startLiveStream(message);
                case "END_LIVE_STREAM":
                    return endLiveStream(message);
                case "GET_LIVE_STATS":
                    return getLiveStats(message);
                    
                // Collaboration operations
                case "CREATE_DUET":
                    return createDuet(message);
                case "CREATE_STITCH":
                    return createStitch(message);
                case "ENABLE_COLLABORATION":
                    return enableCollaboration(message);
                    
                // Discovery operations
                case "SEARCH_VIDEOS":
                    return searchVideos(message);
                case "SEARCH_USERS":
                    return searchUsers(message);
                case "DISCOVER_CONTENT":
                    return discoverContent(message);
                    
                // Challenge operations
                case "JOIN_CHALLENGE":
                    return joinChallenge(message);
                case "GET_CHALLENGE_INFO":
                    return getChallengeInfo(message);
                    
                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch (Exception e) {
            log.error("Error processing TikTok Content outbound message", e);
            throw new AdapterException("Failed to process outbound message", e);
        }
    }
    
    // Video operations
    private Message publishVideo(Message message) throws Exception {
        if (!config.getFeatures().isEnableVideoPublishing()) {
            throw new AdapterException("Video publishing is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 2);
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        // Step 1: Upload video file
        String videoPath = (String) payload.get("videoPath");
        
        // Check video constraints
        long fileSizeInMB = Files.size(Paths.get(videoPath)) / (1024 * 1024);
        if (fileSizeInMB > config.getLimits().getMaxVideoSizeMB()) {
            throw new AdapterException("Video size exceeds limit: " + fileSizeInMB + "MB");
        }
        
        // Step 1: Initialize upload
        String initUrl = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/upload/init/";
        Map<String, Object> initRequest = new HashMap<>();
        initRequest.put("video_size", Files.size(Paths.get(videoPath)));
        
        ResponseEntity<String> initResponse = makeApiCall(initUrl, HttpMethod.POST, null, initRequest);
        JsonNode initData = objectMapper.readTree(initResponse.getBody());
        String uploadUrl = initData.path("data").path("upload_url").asText();
        String publishId = initData.path("data").path("publish_id").asText();
        
        // Step 2: Upload video chunks
        byte[] videoBytes = Files.readAllBytes(Paths.get(videoPath));
        uploadVideoChunks(uploadUrl, videoBytes);
        
        // Step 3: Publish video
        String publishUrl = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/publish/";
        Map<String, Object> publishRequest = new HashMap<>();
        publishRequest.put("publish_id", publishId);
        
        // Video metadata
        publishRequest.put("caption", payload.getOrDefault("caption", ""));
        
        // Privacy settings
        publishRequest.put("privacy_level", payload.getOrDefault("privacy", VideoPrivacy.PUBLIC_TO_EVERYONE.name()));
        
        // Comments settings
        publishRequest.put("allow_comments", payload.getOrDefault("allowComments", true));
        publishRequest.put("allow_duet", payload.getOrDefault("allowDuet", true));
        publishRequest.put("allow_stitch", payload.getOrDefault("allowStitch", true));
        publishRequest.put("allow_download", payload.getOrDefault("allowDownload", true));
        
        // Hashtags
        if (payload.containsKey("hashtags")) {
            List<String> hashtags = (List<String>) payload.get("hashtags");
            if (hashtags.size() > config.getLimits().getMaxHashtagsPerPost()) {
                hashtags = hashtags.subList(0, config.getLimits().getMaxHashtagsPerPost());
            }
            publishRequest.put("hashtags", hashtags);
        }
        
        // Music
        if (payload.containsKey("musicId")) {
            publishRequest.put("music_id", payload.get("musicId"));
        }
        
        // Mentions
        if (payload.containsKey("mentions")) {
            List<String> mentions = (List<String>) payload.get("mentions");
            if (mentions.size() > config.getLimits().getMaxMentionsPerPost()) {
                mentions = mentions.subList(0, config.getLimits().getMaxMentionsPerPost());
            }
            publishRequest.put("mentions", mentions);
        }
        
        // Location
        if (payload.containsKey("location")) {
            publishRequest.put("location", payload.get("location"));
        }
        
        ResponseEntity<String> response = makeApiCall(publishUrl, HttpMethod.POST, null, publishRequest);
        return createResponseMessage(response, "PUBLISH_VIDEO");
    }
    
    private void uploadVideoChunks(String uploadUrl, byte[] videoBytes) throws Exception {
        int chunkSize = 5 * 1024 * 1024; // 5MB chunks
        int totalChunks = (int) Math.ceil(videoBytes.length / (double) chunkSize);
        
        for (int i = 0; i < totalChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, videoBytes.length);
            byte[] chunk = Arrays.copyOfRange(videoBytes, start, end);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("video", new ByteArrayResource(chunk) {
                @Override
                public String getFilename() {
                    return "video_chunk_" + i + ".mp4";
                }
            });
            body.add("chunk_number", i);
            body.add("total_chunks", totalChunks);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new AdapterException("Failed to upload video chunk " + i);
            }
        }
    }
    
    private Message deleteVideo(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/delete/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", payload.get("videoId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "DELETE_VIDEO");
    }
    
    private Message updateVideoCaption(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/update/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", payload.get("videoId"));
        requestBody.put("caption", payload.get("caption"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_VIDEO_CAPTION");
    }
    
    private Message getVideoInfo(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/info/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("video_id", payload.get("videoId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_VIDEO_INFO");
    }
    
    private Message getVideoList(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/list/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", payload.getOrDefault("userId", config.getUserId()));
        params.put("count", payload.getOrDefault("count", 20));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_VIDEO_LIST");
    }
    
    // Comment operations
    private Message postComment(Message message) throws Exception {
        if (!config.getFeatures().isEnableCommentManagement()) {
            throw new AdapterException("Comment management is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/comment/create/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", payload.get("videoId"));
        requestBody.put("text", payload.get("text"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "POST_COMMENT");
    }
    
    private Message deleteComment(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/comment/delete/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("comment_id", payload.get("commentId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "DELETE_COMMENT");
    }
    
    private Message getComments(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/comment/list/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("video_id", payload.get("videoId"));
        params.put("count", Math.min(payload.getOrDefault("count", 50), config.getLimits().getMaxCommentsToRetrieve()));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_COMMENTS");
    }
    
    private Message replyToComment(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/comment/reply/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("comment_id", payload.get("commentId"));
        requestBody.put("text", payload.get("text"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "REPLY_TO_COMMENT");
    }
    
    // User operations
    private Message getUserInfo(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/info/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", payload.getOrDefault("userId", config.getUserId()));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_USER_INFO");
    }
    
    private Message updateProfile(Message message) throws Exception {
        if (!config.getFeatures().isEnableUserProfile()) {
            throw new AdapterException("Profile management is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/update/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        
        if (payload.containsKey("displayName")) {
            requestBody.put("display_name", payload.get("displayName"));
        }
        if (payload.containsKey("bio")) {
            requestBody.put("bio", payload.get("bio"));
        }
        if (payload.containsKey("profileImageUrl")) {
            requestBody.put("profile_image_url", payload.get("profileImageUrl"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_PROFILE");
    }
    
    private Message getFollowers(Message message) throws Exception {
        if (!config.getFeatures().isEnableFollowerAnalytics()) {
            throw new AdapterException("Follower analytics is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/followers/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", payload.getOrDefault("userId", config.getUserId()));
        params.put("count", Math.min(payload.getOrDefault("count", 100), config.getLimits().getMaxFollowersToRetrieve()));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_FOLLOWERS");
    }
    
    private Message getFollowing(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/following/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", payload.getOrDefault("userId", config.getUserId()));
        params.put("count", payload.getOrDefault("count", 100));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_FOLLOWING");
    }
    
    // Analytics operations
    private Message getVideoAnalytics(Message message) throws Exception {
        if (!config.getFeatures().isEnableContentInsights()) {
            throw new AdapterException("Content insights is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/analytics/video/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("video_id", payload.get("videoId"));
        params.put("metrics", String.join(",", (List<String>) payload.getOrDefault("metrics", 
            Arrays.asList("views", "likes", "comments", "shares"))));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_VIDEO_ANALYTICS");
    }
    
    private Message getProfileAnalytics(Message message) throws Exception {
        if (!config.getFeatures().isEnableEngagementMetrics()) {
            throw new AdapterException("Engagement metrics is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/analytics/user/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", payload.getOrDefault("userId", config.getUserId()));
        params.put("period", payload.getOrDefault("period", TimePeriod.LAST_7_DAYS.name()));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_PROFILE_ANALYTICS");
    }
    
    private Message getContentInsights(Message message) throws Exception {
        if (!config.getFeatures().isEnableContentInsights()) {
            throw new AdapterException("Content insights is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 2);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/analytics/insights/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", payload.getOrDefault("userId", config.getUserId()));
        requestBody.put("period", payload.getOrDefault("period", TimePeriod.LAST_28_DAYS.name()));
        requestBody.put("metrics", payload.getOrDefault("metrics", 
            Arrays.asList("views", "completion_rate", "average_watch_time", "engagement_rate")));
        
        if (payload.containsKey("filters")) {
            requestBody.put("filters", payload.get("filters"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "GET_CONTENT_INSIGHTS");
    }
    
    // Trending operations
    private Message getTrendingHashtags(Message message) throws Exception {
        if (!config.getFeatures().isEnableHashtagAnalytics()) {
            throw new AdapterException("Hashtag analytics is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/hashtag/trending/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("count", payload.getOrDefault("count", 20));
        params.put("region", payload.getOrDefault("region", "US"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_TRENDING_HASHTAGS");
    }
    
    private Message getTrendingSounds(Message message) throws Exception {
        if (!config.getFeatures().isEnableMusicIntegration()) {
            throw new AdapterException("Music integration is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/music/trending/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("count", payload.getOrDefault("count", 20));
        params.put("category", payload.getOrDefault("category", SoundCategory.TRENDING.name()));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_TRENDING_SOUNDS");
    }
    
    private Message getTrendingEffects(Message message) throws Exception {
        if (!config.getFeatures().isEnableEffectsManagement()) {
            throw new AdapterException("Effects management is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/effect/trending/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("count", payload.getOrDefault("count", 20));
        params.put("type", payload.getOrDefault("type", EffectType.TRENDING.name()));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_TRENDING_EFFECTS");
    }
    
    // Music operations
    private Message searchMusic(Message message) throws Exception {
        if (!config.getFeatures().isEnableMusicIntegration()) {
            throw new AdapterException("Music integration is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/music/search/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("query", payload.get("query"));
        params.put("count", payload.getOrDefault("count", 20));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "SEARCH_MUSIC");
    }
    
    private Message getMusicInfo(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/music/info/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("music_id", payload.get("musicId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_MUSIC_INFO");
    }
    
    private Message addMusicToFavorites(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/music/favorite/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("music_id", payload.get("musicId"));
        requestBody.put("action", payload.getOrDefault("action", "add"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "ADD_MUSIC_TO_FAVORITES");
    }
    
    // Effect operations
    private Message searchEffects(Message message) throws Exception {
        if (!config.getFeatures().isEnableEffectsManagement()) {
            throw new AdapterException("Effects management is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/effect/search/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("query", payload.get("query"));
        params.put("count", payload.getOrDefault("count", 20));
        params.put("type", payload.getOrDefault("type", EffectType.TRENDING.name()));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "SEARCH_EFFECTS");
    }
    
    private Message getEffectInfo(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/effect/info/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("effect_id", payload.get("effectId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_EFFECT_INFO");
    }
    
    // Live streaming operations
    private Message startLiveStream(Message message) throws Exception {
        if (!config.getFeatures().isEnableLiveStreaming()) {
            throw new AdapterException("Live streaming is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 2);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/live/create/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", payload.get("title"));
        requestBody.put("cover_image", payload.getOrDefault("coverImage", ""));
        requestBody.put("scheduled_start", payload.getOrDefault("scheduledStart", null));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "START_LIVE_STREAM");
    }
    
    private Message endLiveStream(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/live/end/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("live_id", payload.get("liveId"));
        requestBody.put("save_replay", payload.getOrDefault("saveReplay", true));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "END_LIVE_STREAM");
    }
    
    private Message getLiveStats(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/live/stats/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("live_id", payload.get("liveId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_LIVE_STATS");
    }
    
    // Collaboration operations
    private Message createDuet(Message message) throws Exception {
        if (!config.getFeatures().isEnableDuetStitch()) {
            throw new AdapterException("Duet/Stitch is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 2);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/duet/create/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("original_video_id", payload.get("originalVideoId"));
        requestBody.put("duet_video_path", payload.get("duetVideoPath"));
        requestBody.put("layout", payload.getOrDefault("layout", "side_by_side"));
        requestBody.put("caption", payload.getOrDefault("caption", ""));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_DUET");
    }
    
    private Message createStitch(Message message) throws Exception {
        if (!config.getFeatures().isEnableDuetStitch()) {
            throw new AdapterException("Duet/Stitch is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 2);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/stitch/create/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("original_video_id", payload.get("originalVideoId"));
        requestBody.put("stitch_start_time", payload.get("startTime"));
        requestBody.put("stitch_end_time", payload.get("endTime"));
        requestBody.put("stitch_video_path", payload.get("stitchVideoPath"));
        requestBody.put("caption", payload.getOrDefault("caption", ""));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_STITCH");
    }
    
    private Message enableCollaboration(Message message) throws Exception {
        if (!config.getFeatures().isEnableCollaboration()) {
            throw new AdapterException("Collaboration is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/collaboration/settings/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", payload.get("videoId"));
        requestBody.put("allow_duet", payload.getOrDefault("allowDuet", true));
        requestBody.put("allow_stitch", payload.getOrDefault("allowStitch", true));
        requestBody.put("duet_privacy", payload.getOrDefault("duetPrivacy", "everyone"));
        requestBody.put("stitch_privacy", payload.getOrDefault("stitchPrivacy", "everyone"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "ENABLE_COLLABORATION");
    }
    
    // Discovery operations
    private Message searchVideos(Message message) throws Exception {
        if (!config.getFeatures().isEnableContentDiscovery()) {
            throw new AdapterException("Content discovery is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/search/video/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("query", payload.get("query"));
        params.put("count", Math.min(payload.getOrDefault("count", 20), config.getLimits().getMaxSearchResults()));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        if (payload.containsKey("filters")) {
            Map<String, Object> filters = (Map<String, Object>) payload.get("filters");
            if (filters.containsKey("publishTime")) {
                params.put("publish_time", filters.get("publishTime"));
            }
            if (filters.containsKey("duration")) {
                params.put("duration", filters.get("duration"));
            }
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "SEARCH_VIDEOS");
    }
    
    private Message searchUsers(Message message) throws Exception {
        if (!config.getFeatures().isEnableCreatorSearch()) {
            throw new AdapterException("Creator search is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/search/user/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("query", payload.get("query"));
        params.put("count", payload.getOrDefault("count", 20));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "SEARCH_USERS");
    }
    
    private Message discoverContent(Message message) throws Exception {
        if (!config.getFeatures().isEnableContentDiscovery()) {
            throw new AdapterException("Content discovery is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/discover/feed/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("type", payload.getOrDefault("feedType", DiscoveryFeedType.FOR_YOU.name()));
        params.put("count", payload.getOrDefault("count", 20));
        params.put("cursor", payload.getOrDefault("cursor", 0));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "DISCOVER_CONTENT");
    }
    
    // Challenge operations
    private Message joinChallenge(Message message) throws Exception {
        if (!config.getFeatures().isEnableChallengeParticipation()) {
            throw new AdapterException("Challenge participation is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/challenge/join/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("challenge_id", payload.get("challengeId"));
        requestBody.put("video_id", payload.get("videoId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "JOIN_CHALLENGE");
    }
    
    private Message getChallengeInfo(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);
        
        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/challenge/info/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("challenge_id", payload.get("challengeId"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_CHALLENGE_INFO");
    }
    
    // Helper methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, Object> params, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (params != null && method == HttpMethod.GET) {
            params.forEach((key, value) -> builder.queryParam(key, value.toString()));
        }
        
        HttpEntity<?> entity;
        if (body != null) {
            entity = new HttpEntity<>(body, headers);
        } else if (params != null && method != HttpMethod.GET) {
            entity = new HttpEntity<>(params, headers);
        } else {
            entity = new HttpEntity<>(headers);
        }
        
        return restTemplate.exchange(builder.toUriString(), method, entity, String.class);
    }
    
    private Message createResponseMessage(ResponseEntity<String> response, String operation) {
        Message responseMessage = new Message();
        responseMessage.setHeaders(Map.of(
            "operation", operation,
            "statusCode", response.getStatusCode().value(),
            "source", "tiktok_content"
        ));
        responseMessage.setPayload(response.getBody());
        
        return responseMessage;
    }
    
    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }
    
    private void refreshAccessTokenIfNeeded() {
        try {
            if (config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getClientKey(),
                    config.getClientSecret(),
                    config.getRefreshToken(),
                    TIKTOK_API_BASE + "/oauth/refresh_token/"
                );
            }
        } catch (Exception e) {
            log.error("Error refreshing TikTok Content access token", e);
        }
    }
}