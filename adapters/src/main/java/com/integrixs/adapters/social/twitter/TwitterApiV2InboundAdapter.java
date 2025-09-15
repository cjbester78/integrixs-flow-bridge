package com.integrixs.adapters.social.twitter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.twitter.TwitterApiV2Config;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("twitterApiV2InboundAdapter")
public class TwitterApiV2InboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TwitterApiV2InboundAdapter.class);


    private static final String TWITTER_API_BASE = "https://api.twitter.com/2";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private final Set<String> processedTweetIds = ConcurrentHashMap.newKeySet();
    private String authenticatedUserId;

    private TwitterApiV2Config config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;
    private volatile boolean isListening = false;

    @Autowired
    public TwitterApiV2InboundAdapter(
            RateLimiterService rateLimiterService,
            TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("Twitter configuration is invalid");
        }

        log.info("Starting Twitter API v2 inbound adapter");

        // Get authenticated user ID
        authenticatedUserId = getAuthenticatedUserId();
        log.info("Authenticated as user: {}", authenticatedUserId);

        isListening = true;

        // Initialize polling based on enabled features
        if(config.getFeatures().isEnableMentionMonitoring()) {
            scheduleMentionPolling();
        }

        if(config.getFeatures().isEnableTimelineRetrieval()) {
            scheduleTimelinePolling();
        }

        if(config.getFeatures().isEnableDirectMessages()) {
            scheduleDirectMessagePolling();
        }

        if(config.getFeatures().isEnableSpacesIntegration()) {
            scheduleSpacesPolling();
        }
    }

    @Override
    public void stopListening() {
        log.info("Stopping Twitter API v2 inbound adapter");
        isListening = false;
    }

    @Override
    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setMessageTimestamp(Instant.now());
            message.setStatus(MessageStatus.NEW);

            JsonNode dataNode = objectMapper.readTree(data);

            switch(type) {
                case "TWEET":
                    message = processTweet(dataNode);
                    break;
                case "MENTION":
                    message = processMention(dataNode);
                    break;
                case "DIRECT_MESSAGE":
                    message = processDirectMessage(dataNode);
                    break;
                case "FOLLOWER_UPDATE":
                    message = processFollowerUpdate(dataNode);
                    break;
                case "SPACE_UPDATE":
                    message = processSpaceUpdate(dataNode);
                    break;
                case "LIST_UPDATE":
                    message = processListUpdate(dataNode);
                    break;
                case "LIKE_EVENT":
                    message = processLikeEvent(dataNode);
                    break;
                case "RETWEET_EVENT":
                    message = processRetweetEvent(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "twitter"));
            }

            return message;
        } catch(Exception e) {
            log.error("Error processing Twitter inbound data", e);
            throw new AdapterException("Failed to process inbound data", e);
        }
    }

    @Override
    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        try {
            // Twitter v2 uses Account Activity API for webhooks
            String webhookType = (String) webhookData.get("for_user_id");

            if(webhookData.containsKey("tweet_create_events")) {
                List<Map<String, Object>> tweets = (List<Map<String, Object>>) webhookData.get("tweet_create_events");
                return processTweetCreateWebhook(tweets);
            } else if(webhookData.containsKey("favorite_events")) {
                List<Map<String, Object>> favorites = (List<Map<String, Object>>) webhookData.get("favorite_events");
                return processFavoriteWebhook(favorites);
            } else if(webhookData.containsKey("follow_events")) {
                List<Map<String, Object>> follows = (List<Map<String, Object>>) webhookData.get("follow_events");
                return processFollowWebhook(follows);
            } else if(webhookData.containsKey("direct_message_events")) {
                List<Map<String, Object>> dms = (List<Map<String, Object>>) webhookData.get("direct_message_events");
                return processDirectMessageWebhook(dms);
            }

            return null;
        } catch(Exception e) {
            log.error("Error processing Twitter webhook", e);
            throw new AdapterException("Failed to process webhook", 1);

            String url = TWITTER_API_BASE + "/users/" + authenticatedUserId + "/mentions";

            Map<String, String> params = new HashMap<>();
            params.put("max_results", "100");
            params.put("tweet.fields", "id,text,created_at,author_id,conversation_id,public_metrics,entities");
            params.put("user.fields", "id,name,username,profile_image_url,verified");
            params.put("expansions", "author_id,referenced_tweets.id");

            LocalDateTime lastPoll = lastPollTime.getOrDefault("mentions", LocalDateTime.now().minusHours(1));
            params.put("start_time", lastPoll.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "MENTION");
                lastPollTime.put("mentions", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling Twitter mentions", e);
        }
    }

    @Scheduled(fixedDelayString = "$ {integrixs.adapters.twitter.timelinePollingInterval:300000}") // 5 minutes
    private void pollHomeTimeline() {
        if(!isListening || !config.getFeatures().isEnableTimelineRetrieval()) return;

        try {
            rateLimiterService.acquire("twitter_api", 1);

            String url = TWITTER_API_BASE + "/users/" + authenticatedUserId + "/timelines/reverse_chronological";

            Map<String, String> params = new HashMap<>();
            params.put("max_results", "100");
            params.put("tweet.fields", "id,text,created_at,author_id,public_metrics,entities,attachments");
            params.put("user.fields", "id,name,username,profile_image_url,verified");
            params.put("media.fields", "type,url,preview_image_url,duration_ms");
            params.put("expansions", "author_id,attachments.media_keys");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                if(responseData.has("data")) {
                    ArrayNode tweets = (ArrayNode) responseData.get("data");
                    for(JsonNode tweet : tweets) {
                        String tweetId = tweet.get("id").asText();
                        if(!processedTweetIds.contains(tweetId)) {
                            processInboundData(tweet.toString(), "TWEET");
                            processedTweetIds.add(tweetId);
                        }
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error polling Twitter timeline", e);
        }
    }

    @Scheduled(fixedDelayString = "$ {integrixs.adapters.twitter.dmPollingInterval:120000}") // 2 minutes
    private void pollDirectMessages() {
        if(!isListening || !config.getFeatures().isEnableDirectMessages()) return;

        try {
            rateLimiterService.acquire("twitter_api", 1);

            String url = TWITTER_API_BASE + "/dm_events";

            Map<String, String> params = new HashMap<>();
            params.put("max_results", "100");
            params.put("dm_event.fields", "id,text,created_at,sender_id,attachments");
            params.put("user.fields", "id,name,username,profile_image_url");
            params.put("media.fields", "type,url,preview_image_url");
            params.put("expansions", "sender_id,attachments.media_keys");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "DIRECT_MESSAGE");
            }
        } catch(Exception e) {
            log.error("Error polling Twitter direct messages", e);
        }
    }

    @Scheduled(fixedDelayString = "$ {integrixs.adapters.twitter.spacesPollingInterval:300000}") // 5 minutes
    private void pollSpaces() {
        if(!isListening || !config.getFeatures().isEnableSpacesIntegration()) return;

        try {
            rateLimiterService.acquire("twitter_api", 1);

            // Search for live spaces from followed users
            String url = TWITTER_API_BASE + "/spaces/search";

            Map<String, String> params = new HashMap<>();
            params.put("state", "live");
            params.put("max_results", "100");
            params.put("space.fields", "id,state,title,created_at,started_at,participant_count,host_ids");
            params.put("user.fields", "id,name,username,profile_image_url");
            params.put("expansions", "host_ids,speaker_ids");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "SPACE_UPDATE");
            }
        } catch(Exception e) {
            log.error("Error polling Twitter Spaces", e);
        }
    }

    // Process different types of data
    private MessageDTO processTweet(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(data.path("id").asText());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "TWEET");
        headers.put("source", "twitter");
        headers.put("author_id", data.path("author_id").asText());
        headers.put("created_at", data.path("created_at").asText());

        if(data.has("public_metrics")) {
            JsonNode metrics = data.get("public_metrics");
            headers.put("retweet_count", metrics.path("retweet_count").asInt());
            headers.put("reply_count", metrics.path("reply_count").asInt());
            headers.put("like_count", metrics.path("like_count").asInt());
            headers.put("quote_count", metrics.path("quote_count").asInt());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processMention(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "MENTION");
        headers.put("source", "twitter");

        if(data.has("data") && data.get("data").isArray()) {
            headers.put("mention_count", data.get("data").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processDirectMessage(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "DIRECT_MESSAGE");
        headers.put("source", "twitter");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processFollowerUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "FOLLOWER_UPDATE");
        headers.put("source", "twitter");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processSpaceUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "SPACE_UPDATE");
        headers.put("source", "twitter");

        if(data.has("data") && data.get("data").isArray()) {
            headers.put("space_count", data.get("data").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processListUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "LIST_UPDATE");
        headers.put("source", "twitter");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processLikeEvent(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "LIKE_EVENT");
        headers.put("source", "twitter");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processRetweetEvent(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "RETWEET_EVENT");
        headers.put("source", "twitter");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    // Webhook processing methods
    private MessageDTO processTweetCreateWebhook(List<Map<String, Object>> tweets) {
        if(tweets.isEmpty()) return null;

        Map<String, Object> tweet = tweets.get(0);
        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("tweet_data", tweet);
        return processTweet(node);
    }

    private MessageDTO processFavoriteWebhook(List<Map<String, Object>> favorites) {
        if(favorites.isEmpty()) return null;

        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("favorite_data", favorites.get(0));
        return processLikeEvent(node);
    }

    private MessageDTO processFollowWebhook(List<Map<String, Object>> follows) {
        if(follows.isEmpty()) return null;

        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("follow_data", follows.get(0));
        return processFollowerUpdate(node);
    }

    private MessageDTO processDirectMessageWebhook(List<Map<String, Object>> dms) {
        if(dms.isEmpty()) return null;

        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("dm_data", dms.get(0));
        return processDirectMessage(node);
    }

    // Schedule methods
    private void scheduleMentionPolling() {
        log.info("Scheduled mention polling for Twitter");
    }

    private void scheduleTimelinePolling() {
        log.info("Scheduled timeline polling for Twitter");
    }

    private void scheduleDirectMessagePolling() {
        log.info("Scheduled direct message polling for Twitter");
    }

    private void scheduleSpacesPolling() {
        log.info("Scheduled Spaces polling for Twitter");
    }

    // Helper methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder urlBuilder = new StringBuilder(url);
        if(!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) ->
                urlBuilder.append(key).append(" = ").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);
    }

    private String getAuthenticatedUserId() {
        try {
            String url = TWITTER_API_BASE + "/users/me";

            Map<String, String> params = new HashMap<>();
            params.put("user.fields", "id");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode data = objectMapper.readTree(response.getBody());
                return data.path("data").path("id").asText();
            }
        } catch(Exception e) {
            log.error("Error getting authenticated user ID", e);
        }
        return null;
    }

    private String getBearerToken() {
        if(config.getBearerToken() != null) {
            return credentialEncryptionService.decrypt(config.getBearerToken());
        }
        // If no bearer token, use OAuth 2.0 access token
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private boolean isConfigValid() {
        return config != null
            && ((config.getBearerToken() != null) || (config.getAccessToken() != null))
            && config.getApiKey() != null
            && config.getApiKeySecret() != null;
    }

    // CRC validation for webhook setup
    public String validateWebhookCRC(String crcToken) {
        try {
            String consumerSecret = credentialEncryptionService.decrypt(config.getApiKeySecret());

            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secret = new javax.crypto.spec.SecretKeySpec(
                consumerSecret.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secret);

            byte[] hash = mac.doFinal(crcToken.getBytes("UTF-8"));
            String responseToken = "sha256 = " + Base64.getEncoder().encodeToString(hash);

            return responseToken;
        } catch(Exception e) {
            log.error("Error validating webhook CRC", e);
            return null;
        }
    }

    public void setConfiguration(TwitterApiV2Config config) {
        this.config = config;
    }

    // Twitter Ads API Enums
    private enum PlacementType {
        ALL_ON_TWITTER,
        TWITTER_TIMELINE,
        TWITTER_PROFILE,
        TWITTER_SEARCH
    }

    private enum Granularity {
        DAY,
        HOUR,
        TOTAL
    }
}
