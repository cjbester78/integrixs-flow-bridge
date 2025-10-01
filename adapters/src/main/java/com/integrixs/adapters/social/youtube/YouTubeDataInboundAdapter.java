package com.integrixs.adapters.social.youtube;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.youtube.YouTubeDataApiConfig.*;
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
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("youTubeDataInboundAdapter")
public class YouTubeDataInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(YouTubeDataInboundAdapter.class);


    // API URLs are configured in application.yml
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private final Map<String, String> lastEtag = new ConcurrentHashMap<>();

    @Autowired
    private YouTubeDataApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;
    private volatile boolean isListening = false;

    @Autowired
    public YouTubeDataInboundAdapter(
            RateLimiterService rateLimiterService,
            TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super();
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("YouTube Data API configuration is invalid");
        }

        log.info("Starting YouTube Data API inbound adapter for channel: {}", config.getChannelId());

        // Refresh access token if needed
        refreshAccessTokenIfNeeded();

        isListening = true;

        // Initialize polling based on enabled features
        scheduleChannelActivityPolling();
        scheduleVideoCommentsPolling();
        scheduleSubscriberUpdatesPolling();
        scheduleLiveStreamPolling();
        scheduleCommunityPostPolling();
        schedulePlaylistUpdatesPolling();
    }

    public void stopListening() {
        log.info("Stopping YouTube Data API inbound adapter");
        isListening = false;
    }

    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.NEW);

            JsonNode dataNode = objectMapper.readTree(data);

            switch(type) {
                case "CHANNEL_ACTIVITY":
                    message = processChannelActivity(dataNode);
                    break;
                case "VIDEO_COMMENT":
                    message = processVideoComment(dataNode);
                    break;
                case "SUBSCRIBER_UPDATE":
                    message = processSubscriberUpdate(dataNode);
                    break;
                case "LIVE_STREAM":
                    message = processLiveStream(dataNode);
                    break;
                case "COMMUNITY_POST":
                    message = processCommunityPost(dataNode);
                    break;
                case "PLAYLIST_UPDATE":
                    message = processPlaylistUpdate(dataNode);
                    break;
                case "VIDEO_STATISTICS":
                    message = processVideoStatistics(dataNode);
                    break;
                case "CHANNEL_STATISTICS":
                    message = processChannelStatistics(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "youtube_data"));
            }

            return message;
        } catch(Exception e) {
            log.error("Error processing YouTube Data inbound data", e);
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setCorrelationId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setPayload("Error: " + e.getMessage());
            errorMessage.setHeaders(Map.of("error", "true", "errorMessage", e.getMessage()));
            return errorMessage;
        }
    }

    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        try {
            // YouTube uses push notifications(webhooks) for real - time updates
            String topic = webhookData.getOrDefault("topic", "").toString();

            if(topic.contains("feeds/videos")) {
                // New video upload notification
                return processVideoUploadNotification(webhookData);
            } else if(topic.contains("channel")) {
                // Channel update notification
                return processChannelUpdateNotification(webhookData);
            }

            return null;
        } catch(Exception e) {
            log.error("Error processing YouTube webhook data", e);
            return null;
        }
    }

    // Scheduled polling methods
    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.data.activityPollingInterval:300000}") // 5 minutes
    private void pollChannelActivity() {
        if(!isListening || !config.getFeatures().isEnableChannelManagement()) return;

        try {
            rateLimiterService.acquire("youtube_data_api", 1);

            String url = config.getApiBaseUrl() + "/activities";
            Map<String, String> params = new HashMap<>();
            params.put("part", "snippet,contentDetails");
            params.put("channelId", config.getChannelId());
            params.put("maxResults", "50");

            LocalDateTime lastPoll = lastPollTime.get("activities");
            if(lastPoll != null) {
                params.put("publishedAfter", lastPoll.format(DateTimeFormatter.ISO_INSTANT));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CHANNEL_ACTIVITY");
                lastPollTime.put("activities", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling channel activity", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.data.commentsPollingInterval:600000}") // 10 minutes
    private void pollVideoComments() {
        if(!isListening || !config.getFeatures().isEnableCommentManagement()) return;

        try {
            rateLimiterService.acquire("youtube_data_api", 2);

            // First get recent videos
            String videosUrl = config.getApiBaseUrl() + "/search";
            Map<String, String> params = new HashMap<>();
            params.put("part", "id");
            params.put("channelId", config.getChannelId());
            params.put("type", "video");
            params.put("order", "date");
            params.put("maxResults", "10");

            ResponseEntity<String> videosResponse = makeApiCall(videosUrl, HttpMethod.GET, params);
            if(videosResponse.getStatusCode().is2xxSuccessful()) {
                JsonNode videos = objectMapper.readTree(videosResponse.getBody());

                // Poll comments for each recent video
                if(videos.has("items")) {
                    for(JsonNode video : videos.get("items")) {
                        String videoId = video.path("id").path("videoId").asText();
                        pollCommentsForVideo(videoId);
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error polling video comments", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.data.subscriberPollingInterval:3600000}") // 1 hour
    private void pollSubscriberUpdates() {
        if(!isListening || !config.getFeatures().isEnableSubscriberManagement()) return;

        try {
            rateLimiterService.acquire("youtube_data_api", 1);

            String url = config.getApiBaseUrl() + "/channels";
            Map<String, String> params = new HashMap<>();
            params.put("part", "statistics");
            params.put("id", config.getChannelId());

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CHANNEL_STATISTICS");
            }
        } catch(Exception e) {
            log.error("Error polling subscriber updates", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.data.liveStreamPollingInterval:60000}") // 1 minute
    private void pollLiveStreams() {
        if(!isListening || !config.getFeatures().isEnableLiveStreaming()) return;

        try {
            rateLimiterService.acquire("youtube_data_api", 1);

            String url = config.getApiBaseUrl() + "/liveBroadcasts";
            Map<String, String> params = new HashMap<>();
            params.put("part", "snippet,status,contentDetails");
            params.put("broadcastStatus", "active,upcoming");
            params.put("maxResults", "10");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "LIVE_STREAM");
            }
        } catch(Exception e) {
            log.error("Error polling live streams", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.data.communityPollingInterval:1800000}") // 30 minutes
    private void pollCommunityPosts() {
        if(!isListening || !config.getFeatures().isEnableCommunityPosts()) return;

        try {
            // Community posts are part of channel activities
            pollChannelActivity();
        } catch(Exception e) {
            log.error("Error polling community posts", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.data.playlistPollingInterval:1800000}") // 30 minutes
    private void pollPlaylistUpdates() {
        if(!isListening || !config.getFeatures().isEnablePlaylistManagement()) return;

        try {
            rateLimiterService.acquire("youtube_data_api", 1);

            String url = config.getApiBaseUrl() + "/playlists";
            Map<String, String> params = new HashMap<>();
            params.put("part", "snippet,contentDetails");
            params.put("channelId", config.getChannelId());
            params.put("maxResults", "50");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "PLAYLIST_UPDATE");
            }
        } catch(Exception e) {
            log.error("Error polling playlist updates", e);
        }
    }

    // Process different types of data
    private MessageDTO processChannelActivity(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CHANNEL_ACTIVITY");
        headers.put("source", "youtube_data");
        headers.put("channelId", config.getChannelId());

        if(data.has("items") && data.get("items").size() > 0) {
            JsonNode firstItem = data.get("items").get(0);
            String activityType = firstItem.path("snippet").path("type").asText();
            headers.put("activityType", activityType);

            if("upload".equals(activityType)) {
                headers.put("videoId", firstItem.path("contentDetails").path("upload").path("videoId").asText());
            }
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processVideoComment(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "VIDEO_COMMENT");
        headers.put("source", "youtube_data");

        if(data.has("items")) {
            headers.put("commentCount", data.get("items").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processSubscriberUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "SUBSCRIBER_UPDATE");
        headers.put("source", "youtube_data");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processLiveStream(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "LIVE_STREAM");
        headers.put("source", "youtube_data");

        if(data.has("items")) {
            int activeStreams = 0;
            int upcomingStreams = 0;

            for(JsonNode item : data.get("items")) {
                String status = item.path("status").path("lifeCycleStatus").asText();
                if("live".equals(status) || "liveStarting".equals(status)) {
                    activeStreams++;
                } else if("upcoming".equals(status)) {
                    upcomingStreams++;
                }
            }

            headers.put("activeStreams", activeStreams);
            headers.put("upcomingStreams", upcomingStreams);
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processCommunityPost(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "COMMUNITY_POST");
        headers.put("source", "youtube_data");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processPlaylistUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "PLAYLIST_UPDATE");
        headers.put("source", "youtube_data");

        if(data.has("items")) {
            headers.put("playlistCount", data.get("items").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processVideoStatistics(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "VIDEO_STATISTICS");
        headers.put("source", "youtube_data");

        if(data.has("items") && data.get("items").size() > 0) {
            JsonNode stats = data.get("items").get(0).path("statistics");
            headers.put("viewCount", stats.path("viewCount").asLong());
            headers.put("likeCount", stats.path("likeCount").asLong());
            headers.put("commentCount", stats.path("commentCount").asLong());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processChannelStatistics(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CHANNEL_STATISTICS");
        headers.put("source", "youtube_data");

        if(data.has("items") && data.get("items").size() > 0) {
            JsonNode stats = data.get("items").get(0).path("statistics");
            headers.put("subscriberCount", stats.path("subscriberCount").asLong());
            headers.put("viewCount", stats.path("viewCount").asLong());
            headers.put("videoCount", stats.path("videoCount").asLong());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    // Helper methods
    private void pollCommentsForVideo(String videoId) {
        try {
            String url = config.getApiBaseUrl() + "/commentThreads";
            Map<String, String> params = new HashMap<>();
            params.put("part", "snippet,replies");
            params.put("videoId", videoId);
            params.put("maxResults", "100");
            params.put("order", "time");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "VIDEO_COMMENT");
            }
        } catch(Exception e) {
            log.error("Error polling comments for video: " + videoId, e);
        }
    }

    private MessageDTO processVideoUploadNotification(Map<String, Object> webhookData) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "VIDEO_UPLOAD_NOTIFICATION");
        headers.put("source", "youtube_webhook");
        headers.put("videoId", webhookData.getOrDefault("videoId", ""));

        message.setHeaders(headers);
        message.setPayload(objectMapper.valueToTree(webhookData).toString());

        return message;
    }

    private MessageDTO processChannelUpdateNotification(Map<String, Object> webhookData) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CHANNEL_UPDATE_NOTIFICATION");
        headers.put("source", "youtube_webhook");

        message.setHeaders(headers);
        message.setPayload(objectMapper.valueToTree(webhookData).toString());

        return message;
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add If - None - Match header for etag support
        String etagKey = url + params.toString();
        if(lastEtag.containsKey(etagKey)) {
            headers.set("If - None - Match", lastEtag.get(etagKey));
        }

        StringBuilder urlBuilder = new StringBuilder(url);
        if(!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) ->
                urlBuilder.append(key).append(" = ").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);

        // Store etag for future requests
        if(response.getHeaders().getETag() != null) {
            lastEtag.put(etagKey, response.getHeaders().getETag());
        }

        return response;
    }

    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private void refreshAccessTokenIfNeeded() {
        try {
            if(config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getClientId(),
                    config.getClientSecret(),
                    config.getRefreshToken(),
                    "https://oauth2.googleapis.com/token"
               );
            }
        } catch(Exception e) {
            log.error("Error refreshing YouTube access token", e);
        }
    }

    private void scheduleChannelActivityPolling() {
        log.info("Scheduled channel activity polling for YouTube Data API");
    }

    private void scheduleVideoCommentsPolling() {
        log.info("Scheduled video comments polling for YouTube Data API");
    }

    private void scheduleSubscriberUpdatesPolling() {
        log.info("Scheduled subscriber updates polling for YouTube Data API");
    }

    private void scheduleLiveStreamPolling() {
        log.info("Scheduled live stream polling for YouTube Data API");
    }

    private void scheduleCommunityPostPolling() {
        log.info("Scheduled community post polling for YouTube Data API");
    }

    private void schedulePlaylistUpdatesPolling() {
        log.info("Scheduled playlist updates polling for YouTube Data API");
    }

    private boolean isConfigValid() {
        return config != null
            && config.getClientId() != null
            && config.getClientSecret() != null
            && config.getAccessToken() != null;
    }

    public void setConfiguration(YouTubeDataApiConfig config) {
        this.config = config;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return List.of(
            "CHANNEL_ACTIVITY",
            "VIDEO_COMMENT",
            "SUBSCRIBER_UPDATE",
            "LIVE_STREAM",
            "COMMUNITY_POST",
            "PLAYLIST_UPDATE",
            "VIDEO_STATISTICS",
            "CHANNEL_STATISTICS"
        );
    }

    @Override
    protected Map<String, Object> getConfig() {
        return getAdapterConfig();
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("channelId", config.getChannelId());
            configMap.put("clientId", config.getClientId());
            configMap.put("clientSecret", config.getClientSecret());
            configMap.put("accessToken", config.getAccessToken());
            configMap.put("refreshToken", config.getRefreshToken());
            configMap.put("apiBaseUrl", config.getApiBaseUrl());
            configMap.put("apiVersion", config.getApiVersion());
        }
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        // Inbound adapters typically don't send data
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // Initialize any sender-specific resources
        log.debug("Initializing YouTube Data inbound adapter");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // Clean up any sender-specific resources
        log.debug("Destroying YouTube Data inbound adapter");
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Inbound adapters typically don't send data
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = config.getApiBaseUrl() + "/channels";
            Map<String, String> params = new HashMap<>();
            params.put("part", "id");
            params.put("mine", "true");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "YouTube Data API connection successful");
            } else {
                return AdapterResult.failure("YouTube Data API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test YouTube Data connection: " + e.getMessage());
        }
    }
}
