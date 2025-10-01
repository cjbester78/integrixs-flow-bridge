package com.integrixs.adapters.collaboration.teams;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.collaboration.teams.MicrosoftTeamsApiConfig.*;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MicrosoftTeamsInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(MicrosoftTeamsInboundAdapter.class);

    @Override
    public com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.TEAMS;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList("message", "messageReaction", "channel", "team", "meeting", "call");
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("tenantId", config.getTenantId());
        configMap.put("clientId", config.getClientId());
        configMap.put("features", config.getFeatures());
        configMap.put("limits", config.getLimits());
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Teams doesn't use traditional outbound sending from inbound adapter
        log.debug("doSend called with payload: {}", payload);
        return AdapterResult.success("Operation not supported for Teams inbound adapter");
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // No specific sender initialization needed for Teams inbound adapter
        log.debug("Teams inbound adapter sender initialized");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // No specific sender cleanup needed for Teams inbound adapter
        log.debug("Teams inbound adapter sender destroyed");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        // Test connection by checking if we can obtain access token
        try {
            // For now, just check if config is valid
            if (config != null && config.getTenantId() != null && config.getClientId() != null) {
                return AdapterResult.success("Configuration is valid");
            } else {
                return AdapterResult.failure("Invalid configuration");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        // Delegate to doSend
        try {
            return doSend(payload, headers);
        } catch (Exception e) {
            throw new AdapterException("Failed to send message", e);
        }
    }


    @Value("${integrix.adapters.teams.authorization-url-base:https://login.microsoftonline.com}")
    private String authorizationUrlBase;

    private static final String SUBSCRIPTION_PATH = "/subscriptions";

    @Value("${integrix.adapters.teams.token-refresh-buffer:300000}")
    private long TOKEN_REFRESH_BUFFER; // 5 minutes before expiry

    @Autowired
    private MicrosoftTeamsApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Authentication
    private String accessToken;
    private long tokenExpiry;
    private final Object tokenLock = new Object();

    // Change notification subscriptions
    private final Map<String, SubscriptionInfo> activeSubscriptions = new ConcurrentHashMap<>();

    // Webhook validation
    private final Set<String> processedNotifications = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Cache for teams and channels
    private final Map<String, JsonNode> teamCache = new ConcurrentHashMap<>();
    private final Map<String, JsonNode> channelCache = new ConcurrentHashMap<>();
    private final Map<String, JsonNode> userCache = new ConcurrentHashMap<>();

    // Bot Framework integration
    private final AtomicBoolean botConnected = new AtomicBoolean(false);

    public AdapterType getType() {
        return AdapterType.TEAMS;
    }

    public String getName() {
        return "Microsoft Teams API Adapter";
    }

    @Override
    public void initialize() {
        try {
            super.initialize();
        } catch (AdapterException e) {
            log.error("Failed to initialize MicrosoftTeamsInboundAdapter", e);
            throw new RuntimeException("Failed to initialize adapter", e);
        }

        // Get initial access token
        try {
            refreshAccessToken();
        } catch (AdapterException e) {
            log.error("Failed to refresh access token during initialization", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }

        // Setup webhook subscriptions if enabled
        if(config.getFeatures().isEnableWebhooks()) {
            setupChangeNotifications();
        }

        // Schedule token refresh
        scheduler.scheduleAtFixedRate(this::checkAndRefreshToken, 60, 60, TimeUnit.SECONDS);

        // Schedule subscription renewal
        scheduler.scheduleAtFixedRate(this::renewSubscriptions, 3600, 3600, TimeUnit.SECONDS);

        // Clean up old notifications periodically
        scheduler.scheduleAtFixedRate(this::cleanupProcessedNotifications, 300, 300, TimeUnit.SECONDS);
    }

    // OAuth2 token management
    private void refreshAccessToken() throws AdapterException {
        try {
            synchronized(tokenLock) {
                log.info("Refreshing Microsoft Teams access token");

                String tokenUrl = String.format("%s/%s/oauth2/v2.0/token", authorizationUrlBase, config.getTenantId());

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("client_id", config.getClientId());
                params.add("client_secret", config.getClientSecret());
                params.add("scope", "https://graph.microsoft.com/.default");
                params.add("grant_type", "client_credentials");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, JsonNode.class
               );

                JsonNode tokenResponse = response.getBody();
                accessToken = tokenResponse.get("access_token").asText();
                long expiresIn = tokenResponse.get("expires_in").asLong();
                tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000);

                log.info("Successfully refreshed access token, expires in {} seconds", expiresIn);
            }
        } catch(Exception e) {
            log.error("Error refreshing access token", e);
            throw new AdapterException("Failed to refresh Microsoft Teams access token: " + e.getMessage());
        }
    }

    private void checkAndRefreshToken() {
        synchronized(tokenLock) {
            if(accessToken == null || System.currentTimeMillis() >(tokenExpiry - TOKEN_REFRESH_BUFFER)) {
                try {
                    refreshAccessToken();
                } catch (AdapterException e) {
                    log.error("Failed to refresh access token", e);
                    // Don't rethrow to avoid breaking scheduled tasks
                }
            }
        }
    }

    private String getValidAccessToken() {
        synchronized(tokenLock) {
            checkAndRefreshToken();
            return accessToken;
        }
    }

    // Change notification setup
    private void setupChangeNotifications() {
        try {
            log.info("Setting up Teams change notifications");

            // Subscribe to team and channel changes
            if(config.getFeatures().isEnableTeams()) {
                createSubscription("/teams", "created,updated,deleted");
            }

            // Subscribe to chat messages
            if(config.getFeatures().isEnableMessaging()) {
                createSubscription("/chats/getAllMessages", "created,updated,deleted");
            }

            // Subscribe to channel messages
            if(config.getFeatures().isEnableChannels()) {
                createSubscription("/teams/getAllMessages", "created,updated,deleted");
            }

            // Subscribe to meetings
            if(config.getFeatures().isEnableMeetings()) {
                createSubscription("/communications/onlineMeetings", "created,updated");
            }
        } catch(Exception e) {
            log.error("Error setting up change notifications", e);
        }
    }

    private void createSubscription(String resource, String changeTypes) {
        try {
            Map<String, Object> subscription = new HashMap<>();
            subscription.put("changeType", changeTypes);
            subscription.put("notificationUrl", config.getNotificationUrl());
            subscription.put("resource", resource);
            subscription.put("expirationDateTime",
                ZonedDateTime.now().plusHours(config.getLimits().getWebhookExpiryHours())
                    .format(DateTimeFormatter.ISO_INSTANT));
            subscription.put("clientState", UUID.randomUUID().toString());

            JsonNode response = makeGraphApiRequest(SUBSCRIPTION_PATH, HttpMethod.POST, subscription);

            if(response != null && response.has("id")) {
                String subscriptionId = response.get("id").asText();
                activeSubscriptions.put(subscriptionId, new SubscriptionInfo(
                    subscriptionId, resource, changeTypes,
                    response.get("expirationDateTime").asText(),
                    response.get("clientState").asText()
               ));
                log.info("Created subscription {} for resource {}", subscriptionId, resource);
            }
        } catch(Exception e) {
            log.error("Error creating subscription for {}", resource, e);
        }
    }

    private void renewSubscriptions() {
        for(SubscriptionInfo subscription : activeSubscriptions.values()) {
            try {
                Map<String, Object> update = new HashMap<>();
                update.put("expirationDateTime",
                    ZonedDateTime.now().plusHours(config.getLimits().getWebhookExpiryHours())
                        .format(DateTimeFormatter.ISO_INSTANT));

                makeGraphApiRequest(SUBSCRIPTION_PATH + "/" + subscription.id,
                    HttpMethod.PATCH, update);

                log.info("Renewed subscription {}", subscription.id);
            } catch(Exception e) {
                log.error("Error renewing subscription {}", subscription.id, e);
            }
        }
    }

    // Webhook handling
    public void handleWebhookEvent(JsonNode event) {
        try {
            // Handle validation token for new subscriptions
            if(event.has("validationCode")) {
                // This should be handled by the controller
                return;
            }

            // Process change notifications
            if(event.has("value") && event.get("value").isArray()) {
                for(JsonNode notification : event.get("value")) {
                    processChangeNotification(notification);
                }
            }
        } catch(Exception e) {
            log.error("Error handling webhook event", e);
        }
    }

    private void processChangeNotification(JsonNode notification) {
        try {
            String subscriptionId = notification.get("subscriptionId").asText();
            String changeType = notification.get("changeType").asText();
            String resource = notification.get("resource").asText();
            String clientState = notification.get("clientState").asText();

            // Verify subscription
            SubscriptionInfo subscription = activeSubscriptions.get(subscriptionId);
            if(subscription == null || !subscription.clientState.equals(clientState)) {
                log.warn("Invalid subscription or client state");
                return;
            }

            // Deduplication
            String notificationId = notification.get("id").asText();
            if(!processedNotifications.add(notificationId)) {
                log.debug("Duplicate notification: {}", notificationId);
                return;
            }

            // Extract resource data
            JsonNode resourceData = notification.get("resourceData");

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("subscriptionId", subscriptionId);
            eventData.put("changeType", changeType);
            eventData.put("resource", resource);
            eventData.put("resourceData", resourceData);
            eventData.put("timestamp", notification.get("subscriptionExpirationDateTime").asText());

            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setType("teams_change_notification");
            message.setSource("microsoft_teams");
            message.setTarget(getQueueName());
            message.setPayload(objectMapper.writeValueAsString(eventData));
            message.setTimestamp(LocalDateTime.now());

            publishToQueue(message);
        } catch(Exception e) {
            log.error("Error processing change notification", e);
        }
    }

    private String getNotificationCategory(String resource) {
        if(resource.contains("/messages")) return "message";
        if(resource.contains("/teams")) return "team";
        if(resource.contains("/channels")) return "channel";
        if(resource.contains("/chats")) return "chat";
        if(resource.contains("/onlineMeetings")) return "meeting";
        if(resource.contains("/callRecords")) return "call";
        return "unknown";
    }

    // Bot activity handling
    public void handleBotActivity(JsonNode activity) {
        try {
            String activityType = activity.get("type").asText();

            Map<String, Object> activityData = new HashMap<>();
            activityData.put("type", activityType);
            activityData.put("id", activity.get("id").asText());
            activityData.put("timestamp", activity.get("timestamp").asText());
            activityData.put("from", activity.get("from"));
            activityData.put("conversation", activity.get("conversation"));
            activityData.put("channelId", activity.get("channelId").asText());

            // Handle specific activity types
            switch(activityType) {
                case "message":
                    handleMessageActivity(activity, activityData);
                    break;
                case "conversationUpdate":
                    handleConversationUpdate(activity, activityData);
                    break;
                case "invoke":
                    handleInvokeActivity(activity, activityData);
                    break;
                case "messageReaction":
                    handleMessageReaction(activity, activityData);
                    break;
                default:
                    activityData.put("rawActivity", activity);
            }

            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setType("teams_bot_activity");
            message.setSource("microsoft_teams_bot");
            message.setTarget(getQueueName());
            message.setPayload(objectMapper.writeValueAsString(activityData));
            message.setTimestamp(LocalDateTime.now());

            publishToQueue(message);
        } catch(Exception e) {
            log.error("Error handling bot activity", e);
        }
    }

    private void handleMessageActivity(JsonNode activity, Map<String, Object> activityData) {
        activityData.put("text", activity.path("text").asText());
        activityData.put("textFormat", activity.path("textFormat").asText());
        activityData.put("attachments", activity.path("attachments"));
        activityData.put("entities", activity.path("entities"));
        activityData.put("mentions", extractMentions(activity));
        activityData.put("replyToId", activity.path("replyToId").asText());
    }

    private void handleConversationUpdate(JsonNode activity, Map<String, Object> activityData) {
        activityData.put("membersAdded", activity.path("membersAdded"));
        activityData.put("membersRemoved", activity.path("membersRemoved"));
        activityData.put("topicName", activity.path("topicName").asText());
    }

    private void handleInvokeActivity(JsonNode activity, Map<String, Object> activityData) {
        activityData.put("name", activity.path("name").asText());
        activityData.put("value", activity.path("value"));
    }

    private void handleMessageReaction(JsonNode activity, Map<String, Object> activityData) {
        activityData.put("reactionsAdded", activity.path("reactionsAdded"));
        activityData.put("reactionsRemoved", activity.path("reactionsRemoved"));
    }

    private List<Map<String, Object>> extractMentions(JsonNode activity) {
        List<Map<String, Object>> mentions = new ArrayList<>();
        JsonNode entities = activity.path("entities");

        if(entities.isArray()) {
            for(JsonNode entity : entities) {
                if("mention".equals(entity.path("type").asText())) {
                    Map<String, Object> mention = new HashMap<>();
                    mention.put("mentioned", entity.path("mentioned"));
                    mention.put("text", entity.path("text").asText());
                    mentions.add(mention);
                }
            }
        }

        return mentions;
    }

    // Polling methods
    @Scheduled(fixedDelayString = "${integrixs.adapters.teams.polling.teams:3600000}")
    public void pollTeams() {
        if(!config.getFeatures().isEnableTeams()) {
            return;
        }

        try {
            log.debug("Polling Microsoft Teams");

            JsonNode response = makeGraphApiRequest("/groups?$filter = resourceProvisioningOptions/Any(x:x eq 'Team')",
                HttpMethod.GET, null);

            if(response != null && response.has("value")) {
                processTeams(response.get("value"));

                // Handle pagination
                String nextLink = response.path("@odata.nextLink").asText();
                while(!nextLink.isEmpty()) {
                    response = makeGraphApiRequestUrl(nextLink, HttpMethod.GET, null);
                    if(response != null && response.has("value")) {
                        processTeams(response.get("value"));
                        nextLink = response.path("@odata.nextLink").asText();
                    } else {
                        break;
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error polling teams", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.teams.polling.channels:1800000}")
    public void pollChannels() {
        if(!config.getFeatures().isEnableChannels()) {
            return;
        }

        try {
            log.debug("Polling Teams channels");

            // Poll channels for each cached team
            for(String teamId : teamCache.keySet()) {
                try {
                    JsonNode response = makeGraphApiRequest(
                        String.format("/teams/%s/channels", teamId),
                        HttpMethod.GET, null
                   );

                    if(response != null && response.has("value")) {
                        processChannels(teamId, response.get("value"));
                    }
                } catch(Exception e) {
                    log.error("Error polling channels for team {}", teamId, e);
                }
            }
        } catch(Exception e) {
            log.error("Error polling channels", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.teams.polling.users:3600000}")
    public void pollUsers() {
        try {
            log.debug("Polling Teams users");

            JsonNode response = makeGraphApiRequest("/users", HttpMethod.GET, null);

            if(response != null && response.has("value")) {
                processUsers(response.get("value"));

                // Handle pagination
                String nextLink = response.path("@odata.nextLink").asText();
                while(!nextLink.isEmpty()) {
                    response = makeGraphApiRequestUrl(nextLink, HttpMethod.GET, null);
                    if(response != null && response.has("value")) {
                        processUsers(response.get("value"));
                        nextLink = response.path("@odata.nextLink").asText();
                    } else {
                        break;
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error polling users", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.teams.polling.meetings:900000}")
    public void pollMeetings() {
        if(!config.getFeatures().isEnableMeetings()) {
            return;
        }

        try {
            log.debug("Polling Teams meetings");

            // Get meetings for the authenticated user
            JsonNode response = makeGraphApiRequest("/me/onlineMeetings", HttpMethod.GET, null);

            if(response != null && response.has("value")) {
                processMeetings(response.get("value"));
            }
        } catch(Exception e) {
            log.error("Error polling meetings", e);
        }
    }

    // Processing methods
    private void processTeams(JsonNode teams) {
        try {
            List<Map<String, Object>> teamList = new ArrayList<>();

            for(JsonNode team : teams) {
                String teamId = team.get("id").asText();
                teamCache.put(teamId, team);

                Map<String, Object> teamData = new HashMap<>();
                teamData.put("id", teamId);
                teamData.put("displayName", team.get("displayName").asText());
                teamData.put("description", team.path("description").asText());
                teamData.put("visibility", team.path("visibility").asText());
                teamData.put("createdDateTime", team.path("createdDateTime").asText());

                teamList.add(teamData);
            }

            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setType("teams_list");
            message.setSource("microsoft_teams");
            message.setTarget(getQueueName());
            message.setPayload(objectMapper.writeValueAsString(teamList));
            message.setTimestamp(LocalDateTime.now());

            publishToQueue(message);
        } catch(Exception e) {
            log.error("Error processing teams", e);
        }
    }

    private void processChannels(String teamId, JsonNode channels) {
        try {
            List<Map<String, Object>> channelList = new ArrayList<>();

            for(JsonNode channel : channels) {
                String channelId = channel.get("id").asText();
                channelCache.put(teamId + ":" + channelId, channel);

                Map<String, Object> channelData = new HashMap<>();
                channelData.put("id", channelId);
                channelData.put("teamId", teamId);
                channelData.put("displayName", channel.get("displayName").asText());
                channelData.put("description", channel.path("description").asText());
                channelData.put("email", channel.path("email").asText());
                channelData.put("membershipType", channel.path("membershipType").asText());
                channelData.put("isFavoriteByDefault", channel.path("isFavoriteByDefault").asBoolean());

                channelList.add(channelData);
            }

            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setType("channels_list");
            message.setSource("microsoft_teams");
            message.setTarget(getQueueName());
            message.setPayload(objectMapper.writeValueAsString(channelList));
            message.setTimestamp(LocalDateTime.now());

            publishToQueue(message);
        } catch(Exception e) {
            log.error("Error processing channels", e);
        }
    }

    private void processUsers(JsonNode users) {
        try {
            List<Map<String, Object>> userList = new ArrayList<>();

            for(JsonNode user : users) {
                String userId = user.get("id").asText();
                userCache.put(userId, user);

                Map<String, Object> userData = new HashMap<>();
                userData.put("id", userId);
                userData.put("displayName", user.get("displayName").asText());
                userData.put("userPrincipalName", user.path("userPrincipalName").asText());
                userData.put("mail", user.path("mail").asText());
                userData.put("jobTitle", user.path("jobTitle").asText());
                userData.put("department", user.path("department").asText());
                userData.put("officeLocation", user.path("officeLocation").asText());
                userData.put("accountEnabled", user.path("accountEnabled").asBoolean());

                userList.add(userData);
            }

            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setType("users_list");
            message.setSource("microsoft_teams");
            message.setTarget(getQueueName());
            message.setPayload(objectMapper.writeValueAsString(userList));
            message.setTimestamp(LocalDateTime.now());

            publishToQueue(message);
        } catch(Exception e) {
            log.error("Error processing users", e);
        }
    }

    private void processMeetings(JsonNode meetings) {
        try {
            List<Map<String, Object>> meetingList = new ArrayList<>();

            for(JsonNode meeting : meetings) {
                Map<String, Object> meetingData = new HashMap<>();
                meetingData.put("id", meeting.get("id").asText());
                meetingData.put("subject", meeting.get("subject").asText());
                meetingData.put("startDateTime", meeting.get("startDateTime").asText());
                meetingData.put("endDateTime", meeting.get("endDateTime").asText());
                meetingData.put("joinUrl", meeting.path("joinUrl").asText());
                meetingData.put("joinWebUrl", meeting.path("joinWebUrl").asText());
                meetingData.put("videoTeleconferenceId", meeting.path("videoTeleconferenceId").asText());
                meetingData.put("isEntryExitAnnounced", meeting.path("isEntryExitAnnounced").asBoolean());
                meetingData.put("allowedPresenters", meeting.path("allowedPresenters").asText());

                meetingList.add(meetingData);
            }

            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setType("meetings_list");
            message.setSource("microsoft_teams");
            message.setTarget(getQueueName());
            message.setPayload(objectMapper.writeValueAsString(meetingList));
            message.setTimestamp(LocalDateTime.now());

            publishToQueue(message);
        } catch(Exception e) {
            log.error("Error processing meetings", e);
        }
    }

    // Helper methods

    private String getQueueName() {
        return "teams.inbound.queue";
    }

    private void publishToQueue(MessageDTO message) {
        // TODO: Implement actual queue publishing
        log.debug("Publishing message to queue: {}", message);
    }
    private void cleanupProcessedNotifications() {
        // Remove notifications older than 1 hour
        long cutoff = System.currentTimeMillis() - 3600000;
        processedNotifications.removeIf(id -> {
            // Simple cleanup based on size(notifications don't have timestamps in the ID)
            return processedNotifications.size() > 10000;
        });
    }

    // Microsoft Graph API request helper
    private JsonNode makeGraphApiRequest(String path, HttpMethod method, Object body) throws AdapterException {
        return makeGraphApiRequestUrl(config.getGraphApiUrl() + path, method, body);
    }

    private JsonNode makeGraphApiRequestUrl(String url, HttpMethod method, Object body) throws AdapterException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getValidAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity = body != null ?
                new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, method, entity, String.class
           );

            return objectMapper.readTree(response.getBody());
        } catch(JsonProcessingException e) {
            log.error("Error parsing JSON response", e);
            throw new AdapterException("Failed to parse API response: " + e.getMessage());
        } catch(HttpClientErrorException e) {
            log.error("Graph API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Force token refresh
                synchronized(tokenLock) {
                    accessToken = null;
                }
            }

            throw new AdapterException("Graph API request failed: " + e.getMessage());
        }
    }

    // Bot Framework signature verification
    public static boolean verifyBotFrameworkSignature(String authHeader) {
        // Bot Framework uses JWT tokens for authentication
        // This would need proper JWT validation in production
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    @Override
    public void destroy() {
        try {
            super.destroy();
        } catch (AdapterException e) {
            log.error("Failed to destroy MicrosoftTeamsInboundAdapter", e);
            // Don't rethrow as destroy should not fail
        }

        // Cancel all subscriptions
        for(String subscriptionId : activeSubscriptions.keySet()) {
            try {
                makeGraphApiRequest(SUBSCRIPTION_PATH + "/" + subscriptionId,
                    HttpMethod.DELETE, null);
            } catch(Exception e) {
                log.error("Error deleting subscription {}", subscriptionId, e);
            }
        }

        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if(!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch(InterruptedException e) {
            scheduler.shutdownNow();
        }

        // Clear caches
        teamCache.clear();
        channelCache.clear();
        userCache.clear();
        activeSubscriptions.clear();
        processedNotifications.clear();
    }

    // Inner class for subscription tracking
    private static class SubscriptionInfo {
        final String id;
        final String resource;
        final String changeTypes;
        final String expirationDateTime;
        final String clientState;

        SubscriptionInfo(String id, String resource, String changeTypes,
                        String expirationDateTime, String clientState) {
            this.id = id;
            this.resource = resource;
            this.changeTypes = changeTypes;
            this.expirationDateTime = expirationDateTime;
            this.clientState = clientState;
        }
    }
}
