package com.integrixs.adapters.social.tiktok;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.tiktok.TikTokContentApiConfig;
import com.integrixs.adapters.social.tiktok.TikTokContentApiConfig.*;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.TokenRefreshService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component("tikTokContentOutboundAdapter")
public class TikTokContentOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TikTokContentOutboundAdapter.class);


    private static final String TIKTOK_API_BASE = "https://open-api.tiktok.com";
    private static final String TIKTOK_API_VERSION = "/v1.3";
    private static final int VIDEO_CHUNK_SIZE_MB = 5;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private TikTokContentApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public TikTokContentOutboundAdapter(
            RateLimiterService rateLimiterService,
            TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            TikTokContentApiConfig config) {
        super(rateLimiterService, credentialEncryptionService);
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    protected MessageDTO processOutboundMessage(MessageDTO message) {
        try {
            String operation = (String) message.getHeaders().get("operation");
            if(operation == null) {
                throw new AdapterException("Operation header is required");
            }

            log.info("Processing TikTok Content outbound operation: {}", operation);

            return switch(operation.toUpperCase()) {
                case "PUBLISH_VIDEO" -> publishVideo(message);
                case "DELETE_VIDEO" -> deleteVideo(message);
                case "UPDATE_VIDEO_CAPTION" -> updateVideoCaption(message);
                case "GET_VIDEO_INFO" -> getVideoInfo(message);
                case "GET_VIDEO_LIST" -> getVideoList(message);
                case "POST_COMMENT" -> postComment(message);
                case "DELETE_COMMENT" -> deleteComment(message);
                case "GET_COMMENTS" -> getComments(message);
                case "REPLY_TO_COMMENT" -> replyToComment(message);
                case "GET_USER_INFO" -> getUserInfo(message);
                case "UPDATE_PROFILE" -> updateProfile(message);
                case "GET_FOLLOWERS" -> getFollowers(message);
                case "GET_FOLLOWING" -> getFollowing(message);
                case "GET_VIDEO_ANALYTICS" -> getVideoAnalytics(message);
                case "GET_PROFILE_ANALYTICS" -> getProfileAnalytics(message);
                case "GET_CONTENT_INSIGHTS" -> getContentInsights(message);
                case "GET_TRENDING_HASHTAGS" -> getTrendingHashtags(message);
                case "GET_TRENDING_SOUNDS" -> getTrendingSounds(message);
                case "GET_TRENDING_EFFECTS" -> getTrendingEffects(message);
                case "SEARCH_MUSIC" -> searchMusic(message);
                case "GET_MUSIC_INFO" -> getMusicInfo(message);
                case "ADD_MUSIC_TO_FAVORITES" -> addMusicToFavorites(message);
                case "SEARCH_EFFECTS" -> searchEffects(message);
                case "GET_EFFECT_INFO" -> getEffectInfo(message);
                case "START_LIVE_STREAM" -> startLiveStream(message);
                case "END_LIVE_STREAM" -> endLiveStream(message);
                case "GET_LIVE_STATS" -> getLiveStats(message);
                case "CREATE_DUET" -> createDuet(message);
                case "CREATE_STITCH" -> createStitch(message);
                case "ENABLE_COLLABORATION" -> enableCollaboration(message);
                case "SEARCH_VIDEOS" -> searchVideos(message);
                case "SEARCH_USERS" -> searchUsers(message);
                case "DISCOVER_CONTENT" -> discoverContent(message);
                case "GET_CHALLENGE_INFO" -> getChallengeInfo(message);
                case "JOIN_CHALLENGE" -> joinChallenge(message);
                default -> throw new AdapterException("Unsupported operation: " + operation);
            };
        } catch(Exception e) {
            log.error("Error processing TikTok Content outbound operation", e);
            MessageDTO errorResponse = new MessageDTO();
            errorResponse.setCorrelationId(message.getCorrelationId());
            errorResponse.setStatus(MessageStatus.FAILED);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "operation", message.getHeaders().getOrDefault("operation", "unknown")
           ));
            return errorResponse;
        }
    }

    private MessageDTO publishVideo(MessageDTO message) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

        // Step 1: Upload video file
        String videoPath = (String) payload.get("videoPath");

        // Check video constraints
        long fileSizeInMB = Files.size(Paths.get(videoPath)) / (1024 * 1024);
        if(fileSizeInMB > config.getLimits().getMaxVideoSizeMB()) {
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
        if(payload.containsKey("hashtags")) {
            List<String> hashtags = (List<String>) payload.get("hashtags");
            if(hashtags.size() > config.getLimits().getMaxHashtagsPerPost()) {
                hashtags = hashtags.subList(0, config.getLimits().getMaxHashtagsPerPost());
            }
            publishRequest.put("hashtags", hashtags);
        }

        // Music
        if(payload.containsKey("musicId")) {
            publishRequest.put("music_id", payload.get("musicId"));
        }

        // Mentions
        if(payload.containsKey("mentions")) {
            List<String> mentions = (List<String>) payload.get("mentions");
            if(mentions.size() > config.getLimits().getMaxMentionsPerPost()) {
                mentions = mentions.subList(0, config.getLimits().getMaxMentionsPerPost());
            }
            publishRequest.put("mentions", mentions);
        }

        // Location
        if(payload.containsKey("location")) {
            publishRequest.put("location", payload.get("location"));
        }

        ResponseEntity<String> response = makeApiCall(publishUrl, HttpMethod.POST, null, publishRequest);
        return createResponseMessage(response, "PUBLISH_VIDEO");
    }

    private void uploadVideoChunks(String uploadUrl, byte[] videoBytes) throws Exception {
        int chunkSize = VIDEO_CHUNK_SIZE_MB * 1024 * 1024;
        int totalChunks = (int) Math.ceil(videoBytes.length / (double) chunkSize);

        for(int i = 0; i < totalChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, videoBytes.length);
            byte[] chunk = Arrays.copyOfRange(videoBytes, start, end);
            final int chunkNumber = i;

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("video", new ByteArrayResource(chunk) {
                @Override
                public String getFilename() {
                    return "video_chunk_" + chunkNumber + ".mp4";
                }
            });
            body.add("chunk_number", i);
            body.add("total_chunks", totalChunks);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

            if(!response.getStatusCode().is2xxSuccessful()) {
                throw new AdapterException("Failed to upload video chunk " + i);
            }
        }
    }

    private MessageDTO deleteVideo(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/delete/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", payload.get("videoId"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "DELETE_VIDEO");
    }

    private MessageDTO updateVideoCaption(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/update/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("video_id", payload.get("videoId"));
        requestBody.put("caption", payload.get("caption"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_VIDEO_CAPTION");
    }

    private MessageDTO getVideoInfo(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/info/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("video_id", payload.get("videoId"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_VIDEO_INFO");
    }

    private MessageDTO getVideoList(MessageDTO message) throws Exception {
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
    private MessageDTO postComment(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableCommentManagement()) {
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

    private MessageDTO deleteComment(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/comment/delete/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("comment_id", payload.get("commentId"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "DELETE_COMMENT");
    }

    private MessageDTO getComments(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/comment/list/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("video_id", payload.get("videoId"));
        params.put("count", Math.min((Integer) payload.getOrDefault("count", 50), config.getLimits().getMaxCommentsToRetrieve()));
        params.put("cursor", payload.getOrDefault("cursor", 0));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_COMMENTS");
    }

    private MessageDTO replyToComment(MessageDTO message) throws Exception {
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
    private MessageDTO getUserInfo(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/info/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", payload.getOrDefault("userId", config.getUserId()));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_USER_INFO");
    }

    private MessageDTO updateProfile(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableUserProfile()) {
            throw new AdapterException("Profile management is not enabled");
        }

        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/update/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();

        if(payload.containsKey("displayName")) {
            requestBody.put("display_name", payload.get("displayName"));
        }
        if(payload.containsKey("bio")) {
            requestBody.put("bio", payload.get("bio"));
        }
        if(payload.containsKey("profileImageUrl")) {
            requestBody.put("profile_image_url", payload.get("profileImageUrl"));
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_PROFILE");
    }

    private MessageDTO getFollowers(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableFollowerAnalytics()) {
            throw new AdapterException("Follower analytics is not enabled");
        }

        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/followers/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", payload.getOrDefault("userId", config.getUserId()));
        params.put("count", Math.min((Integer) payload.getOrDefault("count", 100), config.getLimits().getMaxFollowersToRetrieve()));
        params.put("cursor", payload.getOrDefault("cursor", 0));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_FOLLOWERS");
    }

    private MessageDTO getFollowing(MessageDTO message) throws Exception {
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
    private MessageDTO getVideoAnalytics(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableContentInsights()) {
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

    private MessageDTO getProfileAnalytics(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableEngagementMetrics()) {
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

    private MessageDTO getContentInsights(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableContentInsights()) {
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

        if(payload.containsKey("filters")) {
            requestBody.put("filters", payload.get("filters"));
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "GET_CONTENT_INSIGHTS");
    }

    // Trending operations
    private MessageDTO getTrendingHashtags(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableHashtagAnalytics()) {
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

    private MessageDTO getTrendingSounds(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableMusicIntegration()) {
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

    private MessageDTO getTrendingEffects(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableEffectsManagement()) {
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
    private MessageDTO searchMusic(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableMusicIntegration()) {
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

    private MessageDTO getMusicInfo(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/music/info/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("music_id", payload.get("musicId"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_MUSIC_INFO");
    }

    private MessageDTO addMusicToFavorites(MessageDTO message) throws Exception {
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
    private MessageDTO searchEffects(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableEffectsManagement()) {
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

    private MessageDTO getEffectInfo(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/effect/info/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("effect_id", payload.get("effectId"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_EFFECT_INFO");
    }

    // Live streaming operations
    private MessageDTO startLiveStream(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableLiveStreaming()) {
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

    private MessageDTO endLiveStream(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/live/end/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("live_id", payload.get("liveId"));
        requestBody.put("save_replay", payload.getOrDefault("saveReplay", true));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "END_LIVE_STREAM");
    }

    private MessageDTO getLiveStats(MessageDTO message) throws Exception {
        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/live/stats/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("live_id", payload.get("liveId"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_LIVE_STATS");
    }

    // Collaboration operations
    private MessageDTO createDuet(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableDuetStitch()) {
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

    private MessageDTO createStitch(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableDuetStitch()) {
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

    private MessageDTO enableCollaboration(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableCollaboration()) {
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
    private MessageDTO searchVideos(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableContentDiscovery()) {
            throw new AdapterException("Content discovery is not enabled");
        }

        rateLimiterService.acquire("tiktok_content_api", 1);

        String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/search/video/";

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, Object> params = new HashMap<>();
        params.put("query", payload.get("query"));
        params.put("count", Math.min((Integer) payload.getOrDefault("count", 20), config.getLimits().getMaxSearchResults()));
        params.put("cursor", payload.getOrDefault("cursor", 0));

        if(payload.containsKey("filters")) {
            Map<String, Object> filters = (Map<String, Object>) payload.get("filters");
            if(filters.containsKey("publishTime")) {
                params.put("publish_time", filters.get("publishTime"));
            }
            if(filters.containsKey("duration")) {
                params.put("duration", filters.get("duration"));
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "SEARCH_VIDEOS");
    }

    private MessageDTO searchUsers(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableCreatorSearch()) {
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

    private MessageDTO discoverContent(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableContentDiscovery()) {
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
    private MessageDTO joinChallenge(MessageDTO message) throws Exception {
        if(!config.getFeatures().isEnableChallengeParticipation()) {
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

    private MessageDTO getChallengeInfo(MessageDTO message) throws Exception {
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
        if(params != null && method == HttpMethod.GET) {
            params.forEach((key, value) -> builder.queryParam(key, value.toString()));
        }

        HttpEntity<?> entity;
        if(body != null) {
            entity = new HttpEntity<>(body, headers);
        } else if(params != null && method != HttpMethod.GET) {
            entity = new HttpEntity<>(params, headers);
        } else {
            entity = new HttpEntity<>(headers);
        }

        return restTemplate.exchange(builder.toUriString(), method, entity, String.class);
    }

    private MessageDTO createResponseMessage(ResponseEntity<String> response, String operation) {
        MessageDTO responseMessageDTO = new MessageDTO();
        responseMessageDTO.setCorrelationId(UUID.randomUUID().toString());
        responseMessageDTO.setTimestamp(LocalDateTime.now());
        responseMessageDTO.setStatus(response.getStatusCode().is2xxSuccessful() ? MessageStatus.SUCCESS : MessageStatus.FAILED);
        responseMessageDTO.setHeaders(Map.of(
            "operation", operation,
            "statusCode", response.getStatusCode().value(),
            "source", "tiktok_content"
       ));
        responseMessageDTO.setPayload(response.getBody());

        return responseMessageDTO;
    }

    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private void refreshAccessTokenIfNeeded() {
        try {
            if(config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getClientKey(),
                    config.getClientSecret(),
                    config.getRefreshToken(),
                    TIKTOK_API_BASE + "/oauth/refresh_token/"
               );
            }
        } catch(Exception e) {
            log.error("Error refreshing TikTok Content access token", e);
        }
    }

    public MessageDTO sendMessage(MessageDTO message) throws AdapterException {
        return processOutboundMessage(message);
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("clientKey", config.getClientKey());
        configMap.put("clientSecret", config.getClientSecret());
        configMap.put("userId", config.getUserId());
        configMap.put("username", config.getUsername());
        configMap.put("features", config.getFeatures());
        configMap.put("limits", config.getLimits());
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        return processOutboundMessage(message);
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    protected long getPollingIntervalMs() {
        // Outbound adapter doesn't poll, return 0
        return 0;
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        // Outbound adapter doesn't need receiver initialization
        log.debug("Initializing TikTok Content outbound adapter");
        validateConfiguration();
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // Outbound adapter cleanup if needed
        log.debug("Destroying TikTok Content outbound adapter");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapter doesn't receive messages
        log.debug("Outbound adapter does not receive data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/info/";
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", config.getUserId());

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success("Connection test successful");
            } else {
                return AdapterResult.failure("Connection test failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    public void setConfiguration(TikTokContentApiConfig config) {
        this.config = config;
    }

    private void validateConfiguration() throws AdapterException {
        if(config == null) {
            throw new AdapterException("TikTok Content configuration is not set");
        }
        if(config.getClientKey() == null || config.getClientSecret() == null) {
            throw new AdapterException("TikTok client key and secret are required");
        }
        if(config.getAccessToken() == null) {
            throw new AdapterException("TikTok access token is required");
        }
    }

    // TikTok Content API Enums
    private enum VideoPrivacy {
        PUBLIC_TO_EVERYONE,
        FRIENDS,
        SELF_ONLY
    }

    private enum DiscoveryFeedType {
        FOR_YOU,
        FOLLOWING,
        NEARBY
    }
}
