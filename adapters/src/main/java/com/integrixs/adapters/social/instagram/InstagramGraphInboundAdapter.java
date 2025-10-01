package com.integrixs.adapters.social.instagram;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.instagram.InstagramGraphApiConfig;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
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
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component("instagramGraphInboundAdapter")
public class InstagramGraphInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(InstagramGraphInboundAdapter.class);

    @Value("${integrix.adapters.instagram.graph.insights-polling-interval:3600000}")
    private long insightsPollingInterval;

    @Value("${integrix.adapters.instagram.graph.comment-polling-interval:300000}")
    private long commentPollingInterval;

    @Value("${integrix.adapters.instagram.graph.mention-polling-interval:300000}")
    private long mentionPollingInterval;

    @Value("${integrix.adapters.instagram.graph.media-polling-interval:600000}")
    private long mediaPollingInterval;

    @Value("${integrix.adapters.instagram.graph.media-endpoint:/media}")
    private String mediaEndpointPath;

    @Value("${integrix.adapters.instagram.graph.mentioned-comment-endpoint:/mentioned_comment}")
    private String mentionedCommentEndpointPath;

    @Value("${integrix.adapters.instagram.graph.mentioned-media-endpoint:/mentioned_media}")
    private String mentionedMediaEndpointPath;

    @Value("${integrix.adapters.instagram.graph.insights-endpoint:/insights}")
    private String insightsEndpointPath;


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private final Set<String> processedMediaIds = ConcurrentHashMap.newKeySet();

    private final RateLimiterService rateLimiterService;
    private final CredentialEncryptionService credentialEncryptionService;
    private InstagramGraphApiConfig config;
    private volatile boolean isListening = false;
    private final BlockingQueue<MessageDTO> eventQueue = new LinkedBlockingQueue<>();

    @Autowired
    public InstagramGraphInboundAdapter(
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void startListening() throws AdapterException {
        if(config == null || config.getInstagramBusinessAccountId() == null) {
            throw new AdapterException("Instagram configuration is invalid");
        }

        log.info("Starting Instagram Graph inbound adapter for account: {}",
                config.getInstagramBusinessAccountId());
        isListening = true;

        // Initialize polling based on enabled features
        if(config.isEnableInsights()) {
            scheduleInsightsPolling();
        }

        if(config.isEnableCommentFiltering()) {
            scheduleCommentPolling();
        }

        // Always enable mention monitoring
        scheduleMentionPolling();
    }

    public void stopListening() {
        log.info("Stopping Instagram Graph inbound adapter");
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
                case "WEBHOOK_EVENT":
                    return processWebhookEvent(dataNode);
                case "NEW_MEDIA":
                    return processNewMedia(dataNode);
                case "NEW_COMMENT":
                    return processNewComment(dataNode);
                case "NEW_MENTION":
                    return processNewMention(dataNode);
                case "INSIGHTS_UPDATE":
                    return processInsightsUpdate(dataNode);
                case "STORY_INSIGHT":
                    return processStoryInsight(dataNode);
                case "HASHTAG_ANALYTICS":
                    return processHashtagAnalytics(dataNode);
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "instagram"));
                    return message;
            }
        } catch(Exception e) {
            log.error("Error processing Instagram inbound data", e);
            // Return a failed message instead of throwing exception
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setCorrelationId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setStatus(MessageStatus.FAILED);
            errorMessage.setPayload(data);
            errorMessage.setHeaders(Map.of("type", type, "source", "instagram", "error", e.getMessage()));
            return errorMessage;
        }
    }

    public MessageDTO processWebhookData(Map<String, Object> webhookData) throws AdapterException {
        try {
            String object = (String) webhookData.get("object");
            if(!"instagram".equals(object)) {
                log.warn("Received webhook for non - Instagram object: {}", object);
                return null;
            }

            List<Map<String, Object>> entries = (List<Map<String, Object>>) webhookData.get("entry");
            if(entries == null || entries.isEmpty()) {
                return null;
            }

            for(Map<String, Object> entry : entries) {
                String entryId = (String) entry.get("id");
                List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");

                for(Map<String, Object> change : changes) {
                    String field = (String) change.get("field");
                    Map<String, Object> value = (Map<String, Object>) change.get("value");

                    switch(field) {
                        case "comments":
                            return processWebhookComment(value);
                        case "mentions":
                            return processWebhookMention(value);
                        case "live_videos":
                            return processWebhookLiveVideo(value);
                        case "stories":
                            return processWebhookStory(value);
                        default:
                            log.debug("Unhandled webhook field: {}", field);
                    }
                }
            }

            return null;
        } catch(Exception e) {
            log.error("Error processing Instagram webhook", e);
            throw new AdapterException("Failed to process webhook", e);
        }
    }

    private void pollNewMedia() {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,caption,media_type,media_url,thumbnail_url,permalink," +
                            "timestamp,like_count,comments_count,insights.metric(impressions,reach)");
        params.put("limit", String.valueOf(config.getMediaLimit()));

        LocalDateTime lastPoll = lastPollTime.getOrDefault("media", LocalDateTime.now().minusHours(1));

        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                if(responseData.has("data")) {
                    ArrayNode mediaItems = (ArrayNode) responseData.get("data");
                    for(JsonNode media : mediaItems) {
                        String mediaId = media.get("id").asText();
                        if(!processedMediaIds.contains(mediaId)) {
                            MessageDTO message = processInboundData(media.toString(), "NEW_MEDIA");
                            processInstagramEvent(message);
                            processedMediaIds.add(mediaId);
                        }
                    }
                }
                lastPollTime.put("media", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling Instagram media", e);
        }
    }

    private void pollNewComments() {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mentionedCommentEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,text,username,timestamp,media {id,media_type,permalink}");
        params.put("limit", String.valueOf(config.getCommentsLimit()));

        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                MessageDTO message = processInboundData(response.getBody(), "NEW_COMMENT");
                processInstagramEvent(message);
            }
        } catch(Exception e) {
            log.error("Error polling Instagram comments", e);
        }
    }

    private void pollMentions() {
        String url = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mentionedMediaEndpointPath);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,caption,media_type,media_url,owner,timestamp");
        params.put("limit", String.valueOf(config.getMentionsLimit()));

        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                MessageDTO message = processInboundData(response.getBody(), "NEW_MENTION");
                processInstagramEvent(message);
            }
        } catch(Exception e) {
            log.error("Error polling Instagram mentions", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrix.adapters.instagram.graph.insights-polling-interval:3600000}")
    private void pollInsights() {
        if(!isListening || !config.isEnableInsights()) return;

        try {
            rateLimiterService.acquire("instagram_api", 1);

            String url = String.format("%s/%s%s",
                config.getBaseUrl(),
                config.getInstagramBusinessAccountId(),
                insightsEndpointPath);

            // Account level insights
            Map<String, String> params = new HashMap<>();
            params.put("access_token", getAccessToken());
            params.put("metric", "impressions,reach,profile_views,website_clicks,follower_count");
            params.put("period", "day");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                MessageDTO message = processInboundData(response.getBody(), "INSIGHTS_UPDATE");
                processInstagramEvent(message);
            }

            // Media insights for recent posts
            pollMediaInsights();

        } catch(Exception e) {
            log.error("Error polling Instagram insights", e);
        }
    }

    private void pollMediaInsights() {
        // Get recent media first
        String mediaUrl = String.format("%s/%s%s",
            config.getBaseUrl(),
            config.getInstagramBusinessAccountId(),
            mediaEndpointPath);

        Map<String, String> mediaParams = new HashMap<>();
        mediaParams.put("access_token", getAccessToken());
        mediaParams.put("fields", "id");
        mediaParams.put("limit", String.valueOf(config.getInsightsMediaLimit()));

        try {
            ResponseEntity<String> mediaResponse = makeApiCall(mediaUrl, HttpMethod.GET, mediaParams);
            if(mediaResponse.getStatusCode().is2xxSuccessful()) {
                JsonNode mediaData = objectMapper.readTree(mediaResponse.getBody());
                if(mediaData.has("data")) {
                    ArrayNode mediaItems = (ArrayNode) mediaData.get("data");

                    for(JsonNode media : mediaItems) {
                        String mediaId = media.get("id").asText();

                        // Get insights for each media
                        String insightsUrl = String.format("%s/%s%s",
                            config.getBaseUrl(), mediaId,
                            insightsEndpointPath);

                        Map<String, String> insightsParams = new HashMap<>();
                        insightsParams.put("access_token", getAccessToken());
                        insightsParams.put("metric", "impressions,reach,engagement,saved,video_views");

                        ResponseEntity<String> insightsResponse = makeApiCall(insightsUrl, HttpMethod.GET, insightsParams);
                        if(insightsResponse.getStatusCode().is2xxSuccessful()) {
                            JsonNode insightsNode = objectMapper.readTree(insightsResponse.getBody());
                            ((ObjectNode) insightsNode).put("media_id", mediaId);
                            MessageDTO message = processInboundData(insightsNode.toString(), "MEDIA_INSIGHTS");
                            processInstagramEvent(message);
                        }
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error polling media insights", e);
        }
    }

    // Process different types of data
    private MessageDTO processWebhookEvent(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "WEBHOOK_EVENT");
        headers.put("source", "instagram");
        headers.put("event_type", data.path("field").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processNewMedia(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "NEW_MEDIA");
        headers.put("source", "instagram");
        headers.put("media_id", data.path("id").asText());
        headers.put("media_type", data.path("media_type").asText());
        headers.put("permalink", data.path("permalink").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processNewComment(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "NEW_COMMENT");
        headers.put("source", "instagram");

        if(data.has("data") && data.get("data").isArray()) {
            headers.put("comment_count", data.get("data").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processNewMention(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "NEW_MENTION");
        headers.put("source", "instagram");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processInsightsUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "INSIGHTS_UPDATE");
        headers.put("source", "instagram");
        headers.put("period", data.path("period").asText("day"));

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processStoryInsight(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "STORY_INSIGHT");
        headers.put("source", "instagram");
        headers.put("story_id", data.path("id").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processHashtagAnalytics(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "HASHTAG_ANALYTICS");
        headers.put("source", "instagram");
        headers.put("hashtag", data.path("name").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processWebhookComment(Map<String, Object> value) {
        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("comment_data", value);
        return processNewComment(node);
    }

    private MessageDTO processWebhookMention(Map<String, Object> value) {
        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("mention_data", value);
        return processNewMention(node);
    }

    private MessageDTO processWebhookLiveVideo(Map<String, Object> value) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);
        message.setHeaders(Map.of(
            "type", "LIVE_VIDEO_EVENT",
            "source", "instagram",
            "video_id", String.valueOf(value.get("id"))
       ));
        message.setPayload(objectMapper.valueToTree(value).toString());
        return message;
    }

    private MessageDTO processWebhookStory(Map<String, Object> value) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);
        message.setHeaders(Map.of(
            "type", "STORY_EVENT",
            "source", "instagram",
            "story_id", String.valueOf(value.get("id"))
       ));
        message.setPayload(objectMapper.valueToTree(value).toString());
        return message;
    }

    @Scheduled(fixedDelayString = "${integrix.adapters.instagram.graph.comment-polling-interval:300000}")
    private void scheduledCommentPolling() {
        if(!isListening || !config.isEnableCommentFiltering()) return;
        try {
            pollNewComments();
        } catch(Exception e) {
            log.error("Error in scheduled comment polling", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrix.adapters.instagram.graph.mention-polling-interval:300000}")
    private void scheduledMentionPolling() {
        if(!isListening) return;
        try {
            pollMentions();
        } catch(Exception e) {
            log.error("Error in scheduled mention polling", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrix.adapters.instagram.graph.media-polling-interval:600000}")
    private void scheduledMediaPolling() {
        if(!isListening) return;
        try {
            pollNewMedia();
        } catch(Exception e) {
            log.error("Error in scheduled media polling", e);
        }
    }

    private void scheduleInsightsPolling() {
        log.info("Scheduled insights polling for Instagram");
    }

    private void scheduleCommentPolling() {
        log.info("Scheduled comment polling for Instagram");
    }

    private void scheduleMentionPolling() {
        log.info("Scheduled mention polling for Instagram");
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder urlBuilder = new StringBuilder(url);
        if(!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);
    }

    private String getAccessToken() {
        String encryptedToken = config.getAccessToken();
        return credentialEncryptionService.decrypt(encryptedToken);
    }

    private void processInstagramEvent(MessageDTO message) {
        // Store the event for processing by the integration flow
        if (eventQueue != null && message != null) {
            eventQueue.offer(message);
        }
    }

    public MessageDTO pollEvent(long timeout) throws InterruptedException {
        return eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
    }

    private boolean isConfigValid() {
        return config != null
            && config.getInstagramBusinessAccountId() != null
            && config.getAccessToken() != null
            && config.getClientId() != null
            && config.getClientSecret() != null;
    }

    public void setConfiguration(InstagramGraphApiConfig config) {
        this.config = config;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return List.of(
            "WEBHOOK_EVENT",
            "NEW_MEDIA",
            "NEW_COMMENT",
            "NEW_MENTION",
            "INSIGHTS_UPDATE",
            "STORY_INSIGHT",
            "HASHTAG_ANALYTICS"
        );
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("instagramBusinessAccountId", config.getInstagramBusinessAccountId());
            configMap.put("accessToken", config.getAccessToken());
            configMap.put("clientId", config.getClientId());
            configMap.put("clientSecret", config.getClientSecret());
            configMap.put("baseUrl", config.getBaseUrl());
            configMap.put("apiVersion", config.getApiVersion());
            configMap.put("enableInsights", config.isEnableInsights());
            configMap.put("enableCommentFiltering", config.isEnableCommentFiltering());
        }
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        log.debug("Initializing Instagram Graph inbound adapter");
        // Initialization is handled in startListening method
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.debug("Destroying Instagram Graph inbound adapter");
        stopListening();
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Inbound adapter doesn't send data, it receives it
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        if (!isConfigValid()) {
            return AdapterResult.failure("Instagram configuration is invalid");
        }

        try {
            // Test connection by making a simple API call
            String url = String.format("%s/%s",
                config.getBaseUrl(),
                config.getInstagramBusinessAccountId());

            Map<String, String> params = new HashMap<>();
            params.put("access_token", getAccessToken());
            params.put("fields", "id,username");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Instagram Graph API connection successful");
            } else {
                return AdapterResult.failure("Instagram Graph API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error testing Instagram connection", e);
            return AdapterResult.failure("Failed to test Instagram connection: " + e.getMessage());
        }
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        return executeTimedOperation("send", () -> doSend(payload, headers));
    }
}
