package com.integrixs.adapters.social.tiktok;

import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.tiktok.TikTokContentApiConfig.*;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Component("tikTokContentInboundAdapter")
public class TikTokContentInboundAdapter extends AbstractSocialMediaInboundAdapter<TikTokContentApiConfig> {
    
    private static final String TIKTOK_API_BASE = "https://open-api.tiktok.com";
    private static final String TIKTOK_API_VERSION = "/v1.3";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private final Map<String, String> lastVideoId = new ConcurrentHashMap<>();
    
    @Autowired
    public TikTokContentInboundAdapter(
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
    public void startListening() throws AdapterException {
        if (!isConfigValid()) {
            throw new AdapterException("TikTok Content configuration is invalid");
        }
        
        log.info("Starting TikTok Content inbound adapter for user: {}", config.getUsername());
        
        // Refresh access token if needed
        refreshAccessTokenIfNeeded();
        
        isListening = true;
        
        // Initialize scheduled polling based on enabled features
        if (config.getFeatures().isEnableVideoRetrieval()) {
            scheduleVideoPolling();
        }
        if (config.getFeatures().isEnableCommentManagement()) {
            scheduleCommentPolling();
        }
        if (config.getFeatures().isEnableEngagementMetrics()) {
            scheduleEngagementPolling();
        }
        if (config.getFeatures().isEnableTrendingContent()) {
            scheduleTrendingPolling();
        }
        if (config.getFeatures().isEnableFollowerAnalytics()) {
            scheduleFollowerPolling();
        }
        if (config.getFeatures().isEnableHashtagAnalytics()) {
            scheduleHashtagPolling();
        }
    }
    
    @Override
    public void stopListening() {
        log.info("Stopping TikTok Content inbound adapter");
        isListening = false;
    }
    
    @Override
    protected Message processInboundData(String data, String type) {
        try {
            Message message = new Message();
            message.setMessageId(UUID.randomUUID().toString());
            message.setTimestamp(Instant.now());
            message.setStatus(MessageStatus.RECEIVED);
            
            JsonNode dataNode = objectMapper.readTree(data);
            
            switch (type) {
                case "VIDEO":
                    message = processVideoData(dataNode);
                    break;
                case "COMMENT":
                    message = processCommentData(dataNode);
                    break;
                case "ENGAGEMENT":
                    message = processEngagementData(dataNode);
                    break;
                case "TRENDING":
                    message = processTrendingData(dataNode);
                    break;
                case "FOLLOWER":
                    message = processFollowerData(dataNode);
                    break;
                case "HASHTAG":
                    message = processHashtagData(dataNode);
                    break;
                case "ANALYTICS":
                    message = processAnalyticsData(dataNode);
                    break;
                case "NOTIFICATION":
                    message = processNotificationData(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "tiktok_content"));
            }
            
            return message;
        } catch (Exception e) {
            log.error("Error processing TikTok Content inbound data", e);
            throw new AdapterException("Failed to process inbound data", e);
        }
    }
    
    @Override
    public Message processWebhookData(Map<String, Object> webhookData) {
        // TikTok Content API supports webhooks for certain events
        try {
            Message message = new Message();
            message.setMessageId(UUID.randomUUID().toString());
            message.setTimestamp(Instant.now());
            message.setStatus(MessageStatus.RECEIVED);
            
            // Verify webhook signature
            if (!verifyWebhookSignature(webhookData)) {
                throw new AdapterException("Invalid webhook signature");
            }
            
            String eventType = (String) webhookData.get("event_type");
            Map<String, Object> headers = new HashMap<>();
            headers.put("type", "WEBHOOK_EVENT");
            headers.put("eventType", eventType);
            headers.put("source", "tiktok_content");
            
            // Process based on event type
            switch (eventType) {
                case "video.publish":
                    headers.put("videoId", webhookData.get("video_id"));
                    headers.put("status", webhookData.get("publish_status"));
                    break;
                case "comment.create":
                    headers.put("videoId", webhookData.get("video_id"));
                    headers.put("commentId", webhookData.get("comment_id"));
                    headers.put("userId", webhookData.get("user_id"));
                    break;
                case "user.follow":
                    headers.put("followerId", webhookData.get("follower_id"));
                    headers.put("followerUsername", webhookData.get("follower_username"));
                    break;
                case "live.start":
                    headers.put("liveId", webhookData.get("live_id"));
                    headers.put("roomId", webhookData.get("room_id"));
                    break;
            }
            
            message.setHeaders(headers);
            message.setPayload(objectMapper.writeValueAsString(webhookData));
            
            return message;
        } catch (Exception e) {
            log.error("Error processing TikTok Content webhook", e);
            throw new AdapterException("Failed to process webhook data", e);
        }
    }
    
    // Scheduled polling methods
    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.content.videoPollInterval:3600000}") // 1 hour
    private void pollVideos() {
        if (!isListening || !config.getFeatures().isEnableVideoRetrieval()) return;
        
        try {
            rateLimiterService.acquire("tiktok_content_api", 1);
            
            String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/video/list/";
            
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", config.getUserId());
            params.put("count", Math.min(20, config.getLimits().getMaxVideosPerDay()));
            params.put("cursor", 0);
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                processInboundData(response.getBody(), "VIDEO");
                
                // Store latest video ID for incremental polling
                if (responseData.has("data") && responseData.get("data").has("videos")) {
                    JsonNode videos = responseData.get("data").get("videos");
                    if (videos.size() > 0) {
                        lastVideoId.put("latest", videos.get(0).path("id").asText());
                    }
                }
                
                lastPollTime.put("videos", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling videos", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.content.commentPollInterval:1800000}") // 30 minutes
    private void pollComments() {
        if (!isListening || !config.getFeatures().isEnableCommentManagement()) return;
        
        try {
            String latestVideo = lastVideoId.get("latest");
            if (latestVideo == null) return;
            
            rateLimiterService.acquire("tiktok_content_api", 1);
            
            String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/comment/list/";
            
            Map<String, Object> params = new HashMap<>();
            params.put("video_id", latestVideo);
            params.put("count", Math.min(50, config.getLimits().getMaxCommentsToRetrieve()));
            params.put("cursor", 0);
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "COMMENT");
                lastPollTime.put("comments", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling comments", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.content.engagementPollInterval:900000}") // 15 minutes
    private void pollEngagement() {
        if (!isListening || !config.getFeatures().isEnableEngagementMetrics()) return;
        
        try {
            rateLimiterService.acquire("tiktok_content_api", 1);
            
            String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/stats/";
            
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", config.getUserId());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "ENGAGEMENT");
                lastPollTime.put("engagement", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling engagement metrics", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.content.trendingPollInterval:7200000}") // 2 hours
    private void pollTrending() {
        if (!isListening || !config.getFeatures().isEnableTrendingContent()) return;
        
        try {
            rateLimiterService.acquire("tiktok_content_api", 1);
            
            String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/trending/feed/";
            
            Map<String, Object> params = new HashMap<>();
            params.put("count", Math.min(50, config.getLimits().getMaxTrendingVideos()));
            params.put("category", "all");
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "TRENDING");
                lastPollTime.put("trending", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling trending content", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.content.followerPollInterval:21600000}") // 6 hours
    private void pollFollowers() {
        if (!isListening || !config.getFeatures().isEnableFollowerAnalytics()) return;
        
        try {
            rateLimiterService.acquire("tiktok_content_api", 1);
            
            String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/user/followers/";
            
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", config.getUserId());
            params.put("count", Math.min(100, config.getLimits().getMaxFollowersToRetrieve()));
            params.put("cursor", 0);
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "FOLLOWER");
                lastPollTime.put("followers", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling followers", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.content.hashtagPollInterval:14400000}") // 4 hours
    private void pollHashtags() {
        if (!isListening || !config.getFeatures().isEnableHashtagAnalytics()) return;
        
        try {
            rateLimiterService.acquire("tiktok_content_api", 1);
            
            String url = TIKTOK_API_BASE + TIKTOK_API_VERSION + "/hashtag/trending/";
            
            Map<String, Object> params = new HashMap<>();
            params.put("count", 20);
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "HASHTAG");
                lastPollTime.put("hashtags", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling hashtags", e);
        }
    }
    
    // Process different data types
    private Message processVideoData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "VIDEO_DATA");
        headers.put("source", "tiktok_content");
        headers.put("userId", config.getUserId());
        
        if (data.has("data") && data.get("data").has("videos")) {
            JsonNode videos = data.get("data").get("videos");
            headers.put("videoCount", videos.size());
            
            long totalViews = 0;
            long totalLikes = 0;
            long totalComments = 0;
            long totalShares = 0;
            
            for (JsonNode video : videos) {
                JsonNode stats = video.get("statistics");
                if (stats != null) {
                    totalViews += stats.path("play_count").asLong(0);
                    totalLikes += stats.path("digg_count").asLong(0);
                    totalComments += stats.path("comment_count").asLong(0);
                    totalShares += stats.path("share_count").asLong(0);
                }
            }
            
            headers.put("totalViews", totalViews);
            headers.put("totalLikes", totalLikes);
            headers.put("totalComments", totalComments);
            headers.put("totalShares", totalShares);
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processCommentData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "COMMENT_DATA");
        headers.put("source", "tiktok_content");
        
        if (data.has("data") && data.get("data").has("comments")) {
            JsonNode comments = data.get("data").get("comments");
            headers.put("commentCount", comments.size());
            headers.put("videoId", data.path("video_id").asText());
            
            // Count comment types
            int positiveComments = 0;
            int questionsCount = 0;
            
            for (JsonNode comment : comments) {
                String text = comment.path("text").asText("");
                if (text.contains("?")) {
                    questionsCount++;
                }
                // Simple positive sentiment check
                if (text.contains("love") || text.contains("great") || text.contains("amazing")) {
                    positiveComments++;
                }
            }
            
            headers.put("questionsCount", questionsCount);
            headers.put("positiveComments", positiveComments);
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processEngagementData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "ENGAGEMENT_DATA");
        headers.put("source", "tiktok_content");
        
        if (data.has("data") && data.get("data").has("stats")) {
            JsonNode stats = data.get("data").get("stats");
            headers.put("followerCount", stats.path("follower_count").asLong(0));
            headers.put("followingCount", stats.path("following_count").asLong(0));
            headers.put("heartCount", stats.path("heart_count").asLong(0));
            headers.put("videoCount", stats.path("video_count").asLong(0));
            
            // Calculate engagement rate
            long followers = stats.path("follower_count").asLong(1);
            long hearts = stats.path("heart_count").asLong(0);
            long videos = stats.path("video_count").asLong(1);
            double engagementRate = (hearts / (double)(followers * videos)) * 100;
            headers.put("engagementRate", engagementRate);
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processTrendingData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "TRENDING_DATA");
        headers.put("source", "tiktok_content");
        
        if (data.has("data") && data.get("data").has("items")) {
            headers.put("trendingCount", data.get("data").get("items").size());
            headers.put("category", data.path("category").asText("all"));
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processFollowerData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "FOLLOWER_DATA");
        headers.put("source", "tiktok_content");
        
        if (data.has("data") && data.get("data").has("followers")) {
            JsonNode followers = data.get("data").get("followers");
            headers.put("newFollowerCount", followers.size());
            
            // Analyze follower quality
            int verifiedFollowers = 0;
            int activeFollowers = 0;
            
            for (JsonNode follower : followers) {
                if (follower.path("verified").asBoolean()) {
                    verifiedFollowers++;
                }
                if (follower.path("video_count").asInt(0) > 10) {
                    activeFollowers++;
                }
            }
            
            headers.put("verifiedFollowers", verifiedFollowers);
            headers.put("activeFollowers", activeFollowers);
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processHashtagData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "HASHTAG_DATA");
        headers.put("source", "tiktok_content");
        
        if (data.has("data") && data.get("data").has("hashtags")) {
            JsonNode hashtags = data.get("data").get("hashtags");
            headers.put("trendingHashtagCount", hashtags.size());
            
            List<String> topHashtags = new ArrayList<>();
            for (JsonNode hashtag : hashtags) {
                if (topHashtags.size() < 5) {
                    topHashtags.add("#" + hashtag.path("name").asText());
                }
            }
            headers.put("topHashtags", topHashtags);
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processAnalyticsData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "ANALYTICS_DATA");
        headers.put("source", "tiktok_content");
        headers.put("period", data.path("period").asText());
        
        if (data.has("data") && data.get("data").has("metrics")) {
            JsonNode metrics = data.get("data").get("metrics");
            
            headers.put("totalViews", metrics.path("views").asLong(0));
            headers.put("totalLikes", metrics.path("likes").asLong(0));
            headers.put("totalComments", metrics.path("comments").asLong(0));
            headers.put("totalShares", metrics.path("shares").asLong(0));
            headers.put("averageWatchTime", metrics.path("average_watch_time").asDouble(0));
            headers.put("completionRate", metrics.path("completion_rate").asDouble(0));
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processNotificationData(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "NOTIFICATION_DATA");
        headers.put("source", "tiktok_content");
        headers.put("notificationType", data.path("notification_type").asText());
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
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
    
    private boolean verifyWebhookSignature(Map<String, Object> webhookData) {
        try {
            String signature = (String) webhookData.get("x-tiktok-signature");
            String timestamp = (String) webhookData.get("x-tiktok-timestamp");
            
            if (signature == null || timestamp == null) {
                return false;
            }
            
            // Verify timestamp is within 5 minutes
            long webhookTime = Long.parseLong(timestamp);
            long currentTime = Instant.now().getEpochSecond();
            if (Math.abs(currentTime - webhookTime) > 300) {
                return false;
            }
            
            // Calculate signature
            String payload = objectMapper.writeValueAsString(webhookData.get("body"));
            String toSign = timestamp + payload;
            
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                config.getClientSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            
            byte[] signatureBytes = hmac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(signatureBytes);
            
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
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
    
    // Scheduling initialization methods
    private void scheduleVideoPolling() {
        log.info("Scheduled video polling for TikTok Content");
    }
    
    private void scheduleCommentPolling() {
        log.info("Scheduled comment polling for TikTok Content");
    }
    
    private void scheduleEngagementPolling() {
        log.info("Scheduled engagement polling for TikTok Content");
    }
    
    private void scheduleTrendingPolling() {
        log.info("Scheduled trending content polling for TikTok Content");
    }
    
    private void scheduleFollowerPolling() {
        log.info("Scheduled follower polling for TikTok Content");
    }
    
    private void scheduleHashtagPolling() {
        log.info("Scheduled hashtag polling for TikTok Content");
    }
    
    private boolean isConfigValid() {
        return config != null 
            && config.getClientKey() != null
            && config.getClientSecret() != null
            && config.getUserId() != null
            && config.getAccessToken() != null;
    }
}