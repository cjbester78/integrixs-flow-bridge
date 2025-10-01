package com.integrixs.adapters.social.linkedin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.linkedin.LinkedInApiConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
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
import com.integrixs.adapters.core.AdapterResult;

@Component("linkedInInboundAdapter")
public class LinkedInInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(LinkedInInboundAdapter.class);


    private static final String LINKEDIN_API_BASE = "https://api.linkedin.com/v2";
    private static final String LINKEDIN_API_REST_BASE = "https://api.linkedin.com/rest";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private final Set<String> processedActivityIds = ConcurrentHashMap.newKeySet();

    private final LinkedInApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;
    private boolean isListening = false;

    @Autowired
    public LinkedInInboundAdapter(
            LinkedInApiConfig config,
            RateLimiterService rateLimiterService,
            TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super();
        this.config = config;
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("LinkedIn configuration is invalid");
        }

        log.info("Starting LinkedIn API inbound adapter");

        // Refresh access token if needed
        refreshAccessTokenIfNeeded();

        isListening = true;

        // Initialize polling based on enabled features
        // TODO: Add getters to LinkedInFeatures or make fields public
        if(config.getFeatures() != null) {
            scheduleFeedPolling();
            scheduleCommentPolling();
            scheduleMessagePolling();
            scheduleConnectionPolling();
            scheduleAnalyticsPolling();
            if(config.getOrganizationId() != null) {
                scheduleOrganizationActivityPolling();
            }
        }
    }

    public void stopListening() {
        log.info("Stopping LinkedIn API inbound adapter");
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
                case "FEED_POST":
                    message = processFeedPost(dataNode);
                    break;
                case "COMMENT":
                    message = processComment(dataNode);
                    break;
                case "REACTION":
                    message = processReaction(dataNode);
                    break;
                case "MESSAGE":
                    message = processMessage(dataNode);
                    break;
                case "CONNECTION":
                    message = processConnection(dataNode);
                    break;
                case "ANALYTICS":
                    message = processAnalytics(dataNode);
                    break;
                case "ORGANIZATION_ACTIVITY":
                    message = processOrganizationActivity(dataNode);
                    break;
                case "SHARE_STATISTICS":
                    message = processShareStatistics(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "linkedin"));
            }

            return message;
        } catch(Exception e) {
            log.error("Error processing LinkedIn inbound data", e);
            // Return a failed message instead of throwing exception
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setCorrelationId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setStatus(MessageStatus.FAILED);
            errorMessage.setPayload(data);
            errorMessage.setHeaders(Map.of("type", type, "source", "linkedin", "error", e.getMessage()));
            return errorMessage;
        }
    }

    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        try {
            // LinkedIn uses webhooks for certain events
            String eventType = (String) webhookData.get("eventType");

            if("ORGANIZATION_SOCIAL_ACTION".equals(eventType)) {
                return processOrganizationSocialAction(webhookData);
            } else if("SHARE".equals(eventType)) {
                return processShareWebhook(webhookData);
            }

            return null;
        } catch(Exception e) {
            log.error("Error processing LinkedIn webhook", e);
            // Return a failed message instead of throwing exception
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setCorrelationId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setStatus(MessageStatus.FAILED);
            try {
                errorMessage.setPayload(objectMapper.writeValueAsString(webhookData));
            } catch (Exception ex) {
                errorMessage.setPayload(webhookData.toString());
            }
            errorMessage.setHeaders(Map.of("source", "linkedin", "type", "webhook", "error", e.getMessage()));
            return errorMessage;
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.feedPollingInterval:300000}") // 5 minutes
    private void pollFeed() {
        if(!isListening) return;

        try {
            rateLimiterService.acquire("linkedin_api", 1);

            // Poll user's feed/shares
            String url = LINKEDIN_API_REST_BASE + "/posts";

            Map<String, String> params = new HashMap<>();
            params.put("q", "author");
            params.put("author", config.getMemberUrn());
            params.put("count", "50");

            LocalDateTime lastPoll = lastPollTime.getOrDefault("feed", LocalDateTime.now().minusHours(24));
            params.put("start", String.valueOf(lastPoll.toEpochSecond(ZoneOffset.UTC) * 1000));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                if(responseData.has("elements")) {
                    ArrayNode posts = (ArrayNode) responseData.get("elements");
                    for(JsonNode post : posts) {
                        String activityId = post.path("id").asText();
                        if(!processedActivityIds.contains(activityId)) {
                            processInboundData(post.toString(), "FEED_POST");
                            processedActivityIds.add(activityId);
                        }
                    }
                }
                lastPollTime.put("feed", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling LinkedIn feed", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.commentPollingInterval:600000}") // 10 minutes
    private void pollComments() {
        if(!isListening) return;

        try {
            rateLimiterService.acquire("linkedin_api", 1);

            // Poll comments on recent shares
            String url = LINKEDIN_API_REST_BASE + "/socialActions";

            Map<String, String> params = new HashMap<>();
            params.put("q", "organizationSocialActions");
            if(config.getOrganizationId() != null) {
                params.put("organizationId", config.getOrganizationId());
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "COMMENT");
            }
        } catch(Exception e) {
            log.error("Error polling LinkedIn comments", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.messagePollingInterval:120000}") // 2 minutes
    private void pollMessages() {
        if(!isListening) return;

        try {
            rateLimiterService.acquire("linkedin_api", 1);

            // Poll messaging conversations
            String url = LINKEDIN_API_REST_BASE + "/conversations";

            Map<String, String> params = new HashMap<>();
            params.put("q", "participants");
            params.put("participants", config.getMemberUrn());

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode conversations = objectMapper.readTree(response.getBody());
                if(conversations.has("elements")) {
                    for(JsonNode conversation : conversations.get("elements")) {
                        pollConversationMessages(conversation.path("entityUrn").asText());
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error polling LinkedIn messages", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.connectionPollingInterval:3600000}") // 1 hour
    private void pollConnections() {
        if(!isListening) return;

        try {
            rateLimiterService.acquire("linkedin_api", 1);

            // Poll connection invitations
            String url = LINKEDIN_API_REST_BASE + "/invitations";

            Map<String, String> params = new HashMap<>();
            params.put("q", "invitationType");
            params.put("invitationType", "CONNECTION");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CONNECTION");
            }
        } catch(Exception e) {
            log.error("Error polling LinkedIn connections", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.analyticsPollingInterval:1800000}") // 30 minutes
    private void pollAnalytics() {
        if(!isListening) return;

        try {
            rateLimiterService.acquire("linkedin_api", 1);

            // Poll share statistics
            String url = LINKEDIN_API_REST_BASE + "/organizationShareStatistics";

            Map<String, String> params = new HashMap<>();
            if(config.getOrganizationId() != null) {
                params.put("q", "organizationShareStatistics");
                params.put("organization", "urn:li:organization:" + config.getOrganizationId());

                LocalDateTime startTime = LocalDateTime.now().minusDays(7);
                LocalDateTime endTime = LocalDateTime.now();

                params.put("timeInterval.start", String.valueOf(startTime.toEpochSecond(ZoneOffset.UTC) * 1000));
                params.put("timeInterval.end", String.valueOf(endTime.toEpochSecond(ZoneOffset.UTC) * 1000));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "ANALYTICS");
            }
        } catch(Exception e) {
            log.error("Error polling LinkedIn analytics", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.organizationPollingInterval:600000}") // 10 minutes
    private void pollOrganizationActivity() {
        if(!isListening || config.getOrganizationId() == null) {
            return;
        }

        try {
            rateLimiterService.acquire("linkedin_api", 1);

            // Poll organization page followers
            String url = LINKEDIN_API_REST_BASE + "/organizationPageStatistics";

            Map<String, String> params = new HashMap<>();
            params.put("q", "organization");
            params.put("organization", "urn:li:organization:" + config.getOrganizationId());

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "ORGANIZATION_ACTIVITY");
            }
        } catch(Exception e) {
            log.error("Error polling LinkedIn organization activity", e);
        }
    }

    // Process different types of data
    private MessageDTO processFeedPost(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(data.path("id").asText());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "FEED_POST");
        headers.put("source", "linkedin");
        headers.put("author", data.path("author").asText());
        headers.put("created_at", data.path("created").path("time").asText());

        if(data.has("lifecycleState")) {
            headers.put("lifecycle_state", data.get("lifecycleState").asText());
        }

        if(data.has("visibility")) {
            headers.put("visibility", data.path("visibility").path("com.linkedin.ugc.MemberNetworkVisibility").asText());
        }

        // Extract metrics if available
        if(data.has("ugcPost")) {
            JsonNode metrics = data.path("ugcPost").path("socialDetail").path("totalShareStatistics");
            if(!metrics.isMissingNode()) {
                headers.put("like_count", metrics.path("likeCount").asInt());
                headers.put("comment_count", metrics.path("commentCount").asInt());
                headers.put("share_count", metrics.path("shareCount").asInt());
                headers.put("impression_count", metrics.path("impressionCount").asInt());
            }
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processComment(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "COMMENT");
        headers.put("source", "linkedin");

        if(data.has("elements") && data.get("elements").isArray()) {
            headers.put("comment_count", data.get("elements").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processReaction(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "REACTION");
        headers.put("source", "linkedin");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processMessage(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "MESSAGE");
        headers.put("source", "linkedin");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processConnection(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CONNECTION");
        headers.put("source", "linkedin");

        if(data.has("elements") && data.get("elements").isArray()) {
            headers.put("invitation_count", data.get("elements").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAnalytics(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "ANALYTICS");
        headers.put("source", "linkedin");

        // Aggregate metrics
        if(data.has("elements") && data.get("elements").isArray()) {
            long totalImpressions = 0;
            long totalClicks = 0;
            long totalEngagement = 0;

            for(JsonNode element : data.get("elements")) {
                JsonNode totalShareStatistics = element.path("totalShareStatistics");
                totalImpressions += totalShareStatistics.path("impressionCount").asLong(0);
                totalClicks += totalShareStatistics.path("clickCount").asLong(0);
                totalEngagement += totalShareStatistics.path("engagement").asLong(0);
            }

            headers.put("total_impressions", totalImpressions);
            headers.put("total_clicks", totalClicks);
            headers.put("total_engagement", totalEngagement);
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processOrganizationActivity(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "ORGANIZATION_ACTIVITY");
        headers.put("source", "linkedin");
        headers.put("organization_id", config.getOrganizationId());

        if(data.has("followerCount")) {
            headers.put("follower_count", data.get("followerCount").asInt());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processShareStatistics(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "SHARE_STATISTICS");
        headers.put("source", "linkedin");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    // Webhook processing
    private MessageDTO processOrganizationSocialAction(Map<String, Object> webhookData) {
        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("webhook_data", webhookData);
        return processOrganizationActivity(node);
    }

    private MessageDTO processShareWebhook(Map<String, Object> webhookData) {
        ObjectNode node = objectMapper.createObjectNode();
        node.putPOJO("webhook_data", webhookData);
        return processFeedPost(node);
    }

    // Helper methods
    private void pollConversationMessages(String conversationUrn) {
        try {
            String url = LINKEDIN_API_REST_BASE + "/messages";

            Map<String, String> params = new HashMap<>();
            params.put("q", "messagesInConversation");
            params.put("conversation", conversationUrn);
            params.put("count", "20");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "MESSAGE");
            }
        } catch(Exception e) {
            log.error("Error polling conversation messages", e);
        }
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("LinkedIn-Version", "202401");
        headers.set("X-Restli-Protocol-Version", "2.0.0");

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
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private void refreshAccessTokenIfNeeded() {
        try {
            // LinkedIn OAuth2 token refresh
            if(config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getClientId(),
                    config.getClientSecret(),
                    config.getRefreshToken(),
                    "https://www.linkedin.com/oauth/v2/accessToken"
               );
            }
        } catch(Exception e) {
            log.error("Error refreshing LinkedIn access token", e);
        }
    }

    private void scheduleFeedPolling() {
        log.info("Scheduled feed polling for LinkedIn");
    }

    private void scheduleCommentPolling() {
        log.info("Scheduled comment polling for LinkedIn");
    }

    private void scheduleMessagePolling() {
        log.info("Scheduled message polling for LinkedIn");
    }

    private void scheduleConnectionPolling() {
        log.info("Scheduled connection polling for LinkedIn");
    }

    private void scheduleAnalyticsPolling() {
        log.info("Scheduled analytics polling for LinkedIn");
    }

    private void scheduleOrganizationActivityPolling() {
        log.info("Scheduled organization activity polling for LinkedIn");
    }

    private boolean isConfigValid() {
        return config != null
            && config.getClientId() != null
            && config.getClientSecret() != null
            && config.getAccessToken() != null;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList(
            "FEED_POST", "COMMENT", "REACTION", "MESSAGE",
            "CONNECTION", "ANALYTICS", "ORGANIZATION_ACTIVITY", "SHARE_STATISTICS"
        );
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("clientId", config.getClientId());
        configMap.put("organizationId", config.getOrganizationId());
        configMap.put("memberUrn", config.getMemberUrn());
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        log.debug("Initializing LinkedIn sender");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.debug("Destroying LinkedIn sender");
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        try {
            String url = LINKEDIN_API_REST_BASE + "/posts";

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setBearerAuth(getAccessToken());
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("LinkedIn-Version", "202401");
            httpHeaders.set("X-Restli-Protocol-Version", "2.0.0");

            HttpEntity<Object> entity = new HttpEntity<>(payload, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if(response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(response.getBody(), "LinkedIn post created successfully");
            } else {
                return AdapterResult.failure("Failed to create LinkedIn post: " + response.getStatusCode());
            }
        } catch(Exception e) {
            log.error("Error sending to LinkedIn", e);
            return AdapterResult.failure("Failed to send to LinkedIn: " + e.getMessage(), e);
        }
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            String url = LINKEDIN_API_BASE + "/me";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            headers.set("LinkedIn-Version", "202401");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if(response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "LinkedIn API connection successful");
            } else {
                return AdapterResult.failure("LinkedIn API connection failed: " + response.getStatusCode());
            }
        } catch(Exception e) {
            log.error("Error testing LinkedIn connection", e);
            return AdapterResult.failure("Failed to test LinkedIn connection: " + e.getMessage(), e);
        }
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        validateReady();

        if(payload == null) {
            throw new AdapterException("Payload cannot be null");
        }

        return executeTimedOperation("send", () -> doSend(payload, headers));
    }
}
