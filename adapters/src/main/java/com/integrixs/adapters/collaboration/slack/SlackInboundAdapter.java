package com.integrixs.adapters.collaboration.slack;
import com.integrixs.adapters.domain.model.AdapterConfiguration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.collaboration.slack.SlackApiConfig.*;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SlackInboundAdapter extends AbstractSocialMediaInboundAdapter {




    @Override


    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.SLACK;
    }




    @Override


    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> adapterConfig = new HashMap<>();
        adapterConfig.put("botToken", config.getBotToken());
        adapterConfig.put("appToken", config.getAppToken());
        adapterConfig.put("signingSecret", config.getSigningSecret());
        adapterConfig.put("features", Map.of(
            "enableMessaging", config.getFeatures().isEnableMessaging(),
            "enableFiles", config.getFeatures().isEnableFiles(),
            "enableChannels", config.getFeatures().isEnableChannels(),
            "enableReactions", config.getFeatures().isEnableReactions(),
            "enableSocketMode", config.getFeatures().isEnableSocketMode()
       ));
        adapterConfig.put("limits", Map.of(
            "maxMessagesPerRequest", config.getLimits().getMaxMessagesPerRequest(),
            "maxUsersPerRequest", config.getLimits().getMaxUsersPerRequest(),
            "maxChannelsPerRequest", config.getLimits().getMaxChannelsPerRequest(),
            "maxFileSizeMb", config.getLimits().getMaxFileSizeMb(),
            "websocketReconnectMaxAttempts", config.getLimits().getWebsocketReconnectMaxAttempts()
       ));
        return adapterConfig;
    }




    @Override


    protected Map<String, Object> getConfig() {
        return getAdapterConfig();
    }




    @Override


    protected List<String> getSupportedEventTypes() {
        return Arrays.asList(
            SlackApiConfig.EventType.MESSAGE_CHANNELS.getEventName(),
            SlackApiConfig.EventType.MESSAGE_GROUPS.getEventName(),
            SlackApiConfig.EventType.MESSAGE_IM.getEventName(),
            SlackApiConfig.EventType.MESSAGE_MPIM.getEventName(),
            SlackApiConfig.EventType.APP_MENTION.getEventName(),
            SlackApiConfig.EventType.APP_HOME_OPENED.getEventName(),
            SlackApiConfig.EventType.APP_UNINSTALLED.getEventName(),
            SlackApiConfig.EventType.CHANNEL_ARCHIVE.getEventName(),
            SlackApiConfig.EventType.CHANNEL_CREATED.getEventName(),
            SlackApiConfig.EventType.CHANNEL_DELETED.getEventName(),
            SlackApiConfig.EventType.CHANNEL_RENAME.getEventName(),
            SlackApiConfig.EventType.CHANNEL_UNARCHIVE.getEventName(),
            SlackApiConfig.EventType.USER_CHANGE.getEventName(),
            SlackApiConfig.EventType.TEAM_JOIN.getEventName(),
            SlackApiConfig.EventType.FILE_CREATED.getEventName(),
            SlackApiConfig.EventType.FILE_SHARED.getEventName(),
            SlackApiConfig.EventType.FILE_DELETED.getEventName(),
            SlackApiConfig.EventType.FILE_CHANGE.getEventName(),
            SlackApiConfig.EventType.FILE_COMMENT_ADDED.getEventName(),
            SlackApiConfig.EventType.FILE_COMMENT_DELETED.getEventName(),
            SlackApiConfig.EventType.REACTION_ADDED.getEventName(),
            SlackApiConfig.EventType.REACTION_REMOVED.getEventName()
       );
    }




    @Override


    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Slack doesn't use traditional outbound sending from inbound adapter
        // Messages are published through webhooks or Socket Mode
        log.debug("doSend called with payload: {}", payload);
        return AdapterResult.success("Operation not supported for Slack inbound adapter");
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // No specific sender initialization needed for Slack inbound adapter
        log.debug("Slack inbound adapter sender initialized");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // No specific sender cleanup needed for Slack inbound adapter
        log.debug("Slack inbound adapter sender destroyed");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        // Test connection by calling auth.test
        try {
            // For now, just check if config is valid
            if (config != null && config.getBotToken() != null) {
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

    private static final Logger log = LoggerFactory.getLogger(SlackInboundAdapter.class);


    @Value("${integrixs.adapters.slack.api-url:https://slack.com/api/}")
    private String API_URL;

    @Value("${integrixs.adapters.slack.socket-mode-reconnect-delay:5000}")
    private int SOCKET_MODE_RECONNECT_DELAY;

    @Value("${integrixs.adapters.slack.ping-interval:30000}")
    private int PING_INTERVAL;

    @Autowired
    private SlackApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Socket Mode connection
    private WebSocketSession socketSession;
    private final AtomicBoolean socketConnected = new AtomicBoolean(false);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private ScheduledFuture<?> pingTask;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Rate limiting tracking
    private final Map<String, AtomicLong> rateLimitTracker = new ConcurrentHashMap<>();
    private final Map<String, Long> rateLimitResetTimes = new ConcurrentHashMap<>();

    // Event deduplication
    private final Map<String, Long> processedEvents = new ConcurrentHashMap<>();
    private final long EVENT_EXPIRY = 300000; // 5 minutes

    // Cache for workspace and user info
    private final Map<String, JsonNode> userCache = new ConcurrentHashMap<>();
    private final Map<String, JsonNode> channelCache = new ConcurrentHashMap<>();
    private final Map<String, JsonNode> teamCache = new ConcurrentHashMap<>();




            @Override


            public void initialize() {
        try {
            super.initialize();
        } catch (AdapterException e) {
            log.error("Failed to initialize SlackInboundAdapter", e);
            throw new RuntimeException("Failed to initialize adapter", e);
        }

        // Start Socket Mode if enabled
        if(config.getFeatures().isEnableSocketMode()) {
            connectSocketMode();
        }

        // Clean up old events periodically
        scheduler.scheduleAtFixedRate(this::cleanupProcessedEvents, 60000, 60000, TimeUnit.MILLISECONDS);
    }

    // Socket Mode connection for real - time events
    private void connectSocketMode() {
        if(!config.getFeatures().isEnableSocketMode()) {
            return;
        }

        try {
            log.info("Connecting to Slack Socket Mode...");

            // Get WebSocket URL
            JsonNode response = makeApiRequest("apps.connections.open", null);
            if(response != null && response.has("url")) {
                String wsUrl = response.get("url").asText();

                StandardWebSocketClient client = new StandardWebSocketClient();
                socketSession = client.doHandshake(new SlackWebSocketHandler(), wsUrl).get();

                socketConnected.set(true);
                reconnectAttempts.set(0);

                // Start ping task
                startPingTask();

                log.info("Successfully connected to Slack Socket Mode");
            } else {
                log.error("Failed to get Socket Mode URL");
            }
        } catch(Exception e) {
            log.error("Error connecting to Socket Mode", e);
            scheduleReconnect();
        }
    }

    private class SlackWebSocketHandler implements WebSocketHandler {


        @Override

        public void afterConnectionEstablished(WebSocketSession session) {
            log.info("Socket Mode connection established");
            socketConnected.set(true);
        }




        @Override


        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
            try {
                String payload = message.getPayload().toString();
                JsonNode event = objectMapper.readTree(payload);

                // Handle different Socket Mode message types
                String type = event.path("type").asText();

                switch(type) {
                    case "hello":
                        log.info("Received Socket Mode hello");
                        break;

                    case "disconnect":
                        log.warn("Server requested disconnect: {}", event.path("reason").asText());
                        scheduleReconnect();
                        break;

                    case "slash_commands":
                        handleSlashCommand(event);
                        acknowledgeSocketMessage(event);
                        break;

                    case "events_api":
                        handleEventCallback(event.path("payload"));
                        acknowledgeSocketMessage(event);
                        break;

                    case "interactive":
                        handleInteractiveEvent(event.path("payload"));
                        acknowledgeSocketMessage(event);
                        break;

                    case "shortcut":
                        handleShortcut(event.path("payload"));
                        acknowledgeSocketMessage(event);
                        break;

                    case "workflow_step_execute":
                        handleWorkflowStepExecute(event.path("payload"));
                        acknowledgeSocketMessage(event);
                        break;

                    default:
                        log.warn("Unknown Socket Mode message type: {}", type);
                        acknowledgeSocketMessage(event);
                }
            } catch(Exception e) {
                log.error("Error handling Socket Mode message", e);
            }
        }




        @Override


        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("Socket Mode transport error", exception);
            socketConnected.set(false);
            scheduleReconnect();
        }




        @Override


        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.info("Socket Mode connection closed: {}", status);
            socketConnected.set(false);
            stopPingTask();
            scheduleReconnect();
        }




        public boolean supportsPartialMessages() {
            return false;
        }


        private void handleSlashCommand(JsonNode payload) {
            // Delegate to outer class method
            SlackInboundAdapter.this.handleSlashCommand(payload);
        }
    }

    private void acknowledgeSocketMessage(JsonNode event) {
        try {
            if(event.has("envelope_id") && socketSession != null && socketSession.isOpen()) {
                ObjectNode ack = objectMapper.createObjectNode();
                ack.put("envelope_id", event.get("envelope_id").asText());

                TextMessage ackMessage = new TextMessage(ack.toString());
                socketSession.sendMessage(ackMessage);
            }
        } catch(Exception e) {
            log.error("Error acknowledging Socket Mode message", e);
        }
    }

    private void startPingTask() {
        if(pingTask != null) {
            pingTask.cancel(false);
        }

        pingTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if(socketSession != null && socketSession.isOpen()) {
                    socketSession.sendMessage(new PingMessage());
                }
            } catch(Exception e) {
                log.error("Error sending ping", e);
            }
        }, PING_INTERVAL, PING_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void stopPingTask() {
        if(pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
    }

    private void scheduleReconnect() {
        if(reconnectAttempts.get() >= config.getLimits().getWebsocketReconnectMaxAttempts()) {
            log.error("Max reconnection attempts reached. Stopping Socket Mode.");
            return;
        }

        int delay = SOCKET_MODE_RECONNECT_DELAY * (int) Math.pow(2, reconnectAttempts.getAndIncrement());
        log.info("Scheduling Socket Mode reconnect in {} ms", delay);

        scheduler.schedule(() -> {
            if(!socketConnected.get()) {
                connectSocketMode();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    // Event handling methods
    private void handleSlashCommand(JsonNode command) {
        try {
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("command", command.path("command").asText());
            commandData.put("text", command.path("text").asText());
            commandData.put("response_url", command.path("response_url").asText());
            commandData.put("trigger_id", command.path("trigger_id").asText());
            commandData.put("user_id", command.path("user_id").asText());
            commandData.put("user_name", command.path("user_name").asText());
            commandData.put("team_id", command.path("team_id").asText());
            commandData.put("team_domain", command.path("team_domain").asText());
            commandData.put("channel_id", command.path("channel_id").asText());
            commandData.put("channel_name", command.path("channel_name").asText());

            MessageDTO message = new MessageDTO();
            message.setType("slash_command");
            message.setSource("slack");
            message.setTarget("slack-inbound");
            message.setPayload(objectMapper.writeValueAsString(commandData));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error handling slash command", e);
        }
    }

    public void handleWebhookEvent(JsonNode event) {
        try {
            String type = event.path("type").asText();

            // URL verification
            if("url_verification".equals(type)) {
                // This should be handled by the controller
                return;
            }

            // Verify request signature
            if(!verifySlackSignature(event)) {
                log.error("Invalid Slack signature");
                return;
            }

            // Handle event callback
            if("event_callback".equals(type)) {
                handleEventCallback(event);
            }
        } catch(Exception e) {
            log.error("Error handling webhook event", e);
        }
    }

    protected void publishMessage(String eventType, Object data) {
        MessageDTO message = convertToMessage(Map.of("data", data), eventType);
        // TODO: Implement actual message publishing logic
        log.debug("Publishing message: type = {}, data = {}", eventType, data);
    }


    private void handleEventCallback(JsonNode eventWrapper) {
        try {
            JsonNode event = eventWrapper.path("event");
            String eventId = eventWrapper.path("event_id").asText();
            String eventType = event.path("type").asText();

            // Deduplication
            if(isDuplicateEvent(eventId)) {
                log.debug("Ignoring duplicate event: {}", eventId);
                return;
            }

            // Cache user/channel info if present
            cacheEventInfo(event);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("event_id", eventId);
            eventData.put("event_type", eventType);
            eventData.put("event_time", eventWrapper.path("event_time").asLong());
            eventData.put("team_id", eventWrapper.path("team_id").asText());
            eventData.put("event", event);

            MessageDTO message = new MessageDTO();
            message.setType("slash_command");
            message.setSource("slack");
            message.setTarget("slack - inbound");
            message.setPayload(objectMapper.writeValueAsString(eventData));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error handling slash command", e);
        }
    }

    private void handleInteractiveEvent(JsonNode interaction) {
        try {
            String type = interaction.path("type").asText();

            Map<String, Object> interactionData = new HashMap<>();
            interactionData.put("type", type);
            interactionData.put("user", interaction.path("user"));
            interactionData.put("team", interaction.path("team"));
            interactionData.put("channel", interaction.path("channel"));
            interactionData.put("trigger_id", interaction.path("trigger_id").asText());
            interactionData.put("response_url", interaction.path("response_url").asText());

            // Add type - specific data
            switch(type) {
                case "block_actions":
                    interactionData.put("actions", interaction.path("actions"));
                    interactionData.put("container", interaction.path("container"));
                    interactionData.put("state", interaction.path("state"));
                    break;

                case "view_submission":
                case "view_closed":
                    interactionData.put("view", interaction.path("view"));
                    break;

                case "shortcut":
                case "message_action":
                    interactionData.put("callback_id", interaction.path("callback_id").asText());
                    if(interaction.has("message")) {
                        interactionData.put("message", interaction.path("message"));
                    }
                    break;
            }

            MessageDTO message = new MessageDTO();
            message.setType("interactive_component");
            message.setSource("slack");
            message.setTarget("slack - inbound");
            message.setPayload(objectMapper.writeValueAsString(interactionData));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error handling interactive event", e);
        }
    }

    private void handleShortcut(JsonNode shortcut) {
        try {
            Map<String, Object> shortcutData = new HashMap<>();
            shortcutData.put("type", shortcut.path("type").asText());
            shortcutData.put("callback_id", shortcut.path("callback_id").asText());
            shortcutData.put("trigger_id", shortcut.path("trigger_id").asText());
            shortcutData.put("user", shortcut.path("user"));
            shortcutData.put("team", shortcut.path("team"));

            MessageDTO message = new MessageDTO();
            message.setType("shortcut");
            message.setSource("slack");
            message.setTarget("slack - inbound");
            message.setPayload(objectMapper.writeValueAsString(shortcutData));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error handling shortcut", e);
        }
    }

    private void handleWorkflowStepExecute(JsonNode workflow) {
        try {
            Map<String, Object> workflowData = new HashMap<>();
            workflowData.put("workflow_step", workflow.path("workflow_step"));
            workflowData.put("event", workflow.path("event"));

            MessageDTO message = new MessageDTO();
            message.setType("workflow_step_execute");
            message.setSource("slack");
            message.setTarget("slack - inbound");
            message.setPayload(objectMapper.writeValueAsString(workflowData));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error handling workflow step execute", e);
        }
    }

    // Polling methods for workspace data
    @Scheduled(fixedDelayString = "${integrixs.adapters.slack.polling.users:3600000}")
    public void pollUsers() {
        if(!config.getFeatures().isEnableMessaging()) {
            return;
        }

        try {
            log.debug("Polling Slack users");
            String cursor = null;

            do {
                Map<String, Object> params = new HashMap<>();
                params.put("limit", config.getLimits().getMaxUsersPerRequest());
                if(cursor != null) {
                    params.put("cursor", cursor);
                }

                JsonNode response = makeApiRequest("users.list", params);
                if(response != null && response.get("ok").asBoolean()) {
                    JsonNode users = response.get("members");
                    processUsers(users);

                    cursor = response.path("response_metadata").path("next_cursor").asText(null);
                }
            } while(cursor != null && !cursor.isEmpty());
        } catch(Exception e) {
            log.error("Error polling users", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.slack.polling.channels:1800000}")
    public void pollChannels() {
        if(!config.getFeatures().isEnableChannels()) {
            return;
        }

        try {
            log.debug("Polling Slack channels");
            String cursor = null;

            do {
                Map<String, Object> params = new HashMap<>();
                params.put("types", "public_channel,private_channel");
                params.put("limit", config.getLimits().getMaxChannelsPerRequest());
                params.put("exclude_archived", true);
                if(cursor != null) {
                    params.put("cursor", cursor);
                }

                JsonNode response = makeApiRequest("conversations.list", params);
                if(response != null && response.get("ok").asBoolean()) {
                    JsonNode channels = response.get("channels");
                    processChannels(channels);

                    cursor = response.path("response_metadata").path("next_cursor").asText(null);
                }
            } while(cursor != null && !cursor.isEmpty());
        } catch(Exception e) {
            log.error("Error polling channels", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.slack.polling.team:86400000}")
    public void pollTeamInfo() {
        try {
            log.debug("Polling Slack team info");
            JsonNode response = makeApiRequest("team.info", null);

            if(response != null && response.get("ok").asBoolean()) {
                processTeamInfo(response.get("team"));
            }
        } catch(Exception e) {
            log.error("Error polling team info", e);
        }
    }

    // Processing methods
    private void processUsers(JsonNode users) {
        try {
            List<Map<String, Object>> userList = new ArrayList<>();

            for(JsonNode user : users) {
                String userId = user.get("id").asText();
                userCache.put(userId, user);

                Map<String, Object> userData = new HashMap<>();
                userData.put("id", userId);
                userData.put("name", user.path("name").asText());
                userData.put("real_name", user.path("real_name").asText());
                userData.put("display_name", user.path("profile").path("display_name").asText());
                userData.put("is_bot", user.path("is_bot").asBoolean());
                userData.put("is_admin", user.path("is_admin").asBoolean());
                userData.put("is_owner", user.path("is_owner").asBoolean());
                userData.put("is_restricted", user.path("is_restricted").asBoolean());
                userData.put("is_ultra_restricted", user.path("is_ultra_restricted").asBoolean());
                userData.put("has_2fa", user.path("has_2fa").asBoolean());
                userData.put("email", user.path("profile").path("email").asText());

                userList.add(userData);
            }

            MessageDTO message = new MessageDTO();
            message.setType("users_list");
            message.setSource("slack");
            message.setTarget("slack - inbound");
            message.setPayload(objectMapper.writeValueAsString(userList));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error processing users", e);
        }
    }

    private void processChannels(JsonNode channels) {
        try {
            List<Map<String, Object>> channelList = new ArrayList<>();

            for(JsonNode channel : channels) {
                String channelId = channel.get("id").asText();
                channelCache.put(channelId, channel);

                Map<String, Object> channelData = new HashMap<>();
                channelData.put("id", channelId);
                channelData.put("name", channel.path("name").asText());
                channelData.put("is_channel", channel.path("is_channel").asBoolean());
                channelData.put("is_group", channel.path("is_group").asBoolean());
                channelData.put("is_im", channel.path("is_im").asBoolean());
                channelData.put("is_mpim", channel.path("is_mpim").asBoolean());
                channelData.put("is_private", channel.path("is_private").asBoolean());
                channelData.put("is_archived", channel.path("is_archived").asBoolean());
                channelData.put("is_general", channel.path("is_general").asBoolean());
                channelData.put("is_shared", channel.path("is_shared").asBoolean());
                channelData.put("is_org_shared", channel.path("is_org_shared").asBoolean());
                channelData.put("is_pending_ext_shared", channel.path("is_pending_ext_shared").asBoolean());
                channelData.put("num_members", channel.path("num_members").asInt());
                channelData.put("topic", channel.path("topic").path("value").asText());
                channelData.put("purpose", channel.path("purpose").path("value").asText());

                channelList.add(channelData);
            }

            MessageDTO message = new MessageDTO();
            message.setType("channels_list");
            message.setSource("slack");
            message.setTarget("slack - inbound");
            message.setPayload(objectMapper.writeValueAsString(channelList));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error processing channels", e);
        }
    }

    private void processTeamInfo(JsonNode team) {
        try {
            String teamId = team.get("id").asText();
            teamCache.put(teamId, team);

            Map<String, Object> teamData = new HashMap<>();
            teamData.put("id", teamId);
            teamData.put("name", team.path("name").asText());
            teamData.put("domain", team.path("domain").asText());
            teamData.put("email_domain", team.path("email_domain").asText());
            teamData.put("icon", team.path("icon"));
            teamData.put("enterprise_id", team.path("enterprise_id").asText());
            teamData.put("enterprise_name", team.path("enterprise_name").asText());

            MessageDTO message = new MessageDTO();
            message.setType("team_info");
            message.setSource("slack");
            message.setTarget("slack - inbound");
            message.setPayload(objectMapper.writeValueAsString(teamData));
            message.setMessageTimestamp(Instant.now());

            // Use the base class publishMessage method
            publishMessage(message.getType(), message);
        } catch(Exception e) {
            log.error("Error processing team info", e);
        }
    }

    // Helper methods
    private boolean isDuplicateEvent(String eventId) {
        Long timestamp = processedEvents.putIfAbsent(eventId, System.currentTimeMillis());
        return timestamp != null;
    }

    private void cleanupProcessedEvents() {
        long cutoff = System.currentTimeMillis() - EVENT_EXPIRY;
        processedEvents.entrySet().removeIf(entry -> entry.getValue() < cutoff);
    }

    private void cacheEventInfo(JsonNode event) {
        try {
            // Cache user info
            if(event.has("user")) {
                String userId = event.get("user").asText();
                if(!userCache.containsKey(userId)) {
                    // Fetch user info if not cached
                    Map<String, Object> params = new HashMap<>();
                    params.put("user", userId);
                    JsonNode response = makeApiRequest("users.info", params);
                    if(response != null && response.get("ok").asBoolean()) {
                        userCache.put(userId, response.get("user"));
                    }
                }
            }

            // Cache channel info
            if(event.has("channel")) {
                String channelId = event.get("channel").asText();
                if(!channelCache.containsKey(channelId)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("channel", channelId);
                    JsonNode response = makeApiRequest("conversations.info", params);
                    if(response != null && response.get("ok").asBoolean()) {
                        channelCache.put(channelId, response.get("channel"));
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error caching event info", e);
        }
    }

    public boolean verifySlackSignature(JsonNode body) {
        // This should be implemented in the controller/filter that receives the webhook
        // Signature verification requires access to raw request body and headers
        return true;
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for(byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // API request helper with rate limiting
    private JsonNode makeApiRequest(String method, Map<String, Object> params) throws AdapterException {
        try {
            // Check rate limit
            if(isRateLimited(method)) {
                log.warn("Rate limited for method: {}", method);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getBotToken());

            String url = API_URL + method;

            HttpEntity<Map<String, Object>> entity = params != null ?
                new HttpEntity<>(params, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
           );

            // Update rate limit tracking
            updateRateLimit(method, response.getHeaders());

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if(!responseJson.get("ok").asBoolean()) {
                log.error("Slack API error for {}: {}", method, responseJson.get("error").asText());
                return null;
            }

            return responseJson;
        } catch(HttpClientErrorException e) {
            log.error("HTTP error calling {}: {} - {}", method, e.getStatusCode(), e.getResponseBodyAsString());

            // Handle rate limiting
            if(e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                String retryAfter = e.getResponseHeaders().getFirst("Retry - After");
                if(retryAfter != null) {
                    long resetTime = System.currentTimeMillis() + (Long.parseLong(retryAfter) * 1000);
                    rateLimitResetTimes.put(method, resetTime);
                }
            }

            throw new AdapterException("Slack API request failed", e);
        } catch(Exception e) {
            log.error("Error making API request to {}", method, e);
            throw new AdapterException("Failed to make API request", e);
        }
    }

    private boolean isRateLimited(String method) {
        Long resetTime = rateLimitResetTimes.get(method);
        if(resetTime != null && System.currentTimeMillis() < resetTime) {
            return true;
        }
        rateLimitResetTimes.remove(method);
        return false;
    }

    private void updateRateLimit(String method, HttpHeaders headers) {
        // Slack doesn't provide rate limit headers consistently
        // We track calls internally based on documented limits
        String tier = getRateLimitTier(method);
        AtomicLong counter = rateLimitTracker.computeIfAbsent(tier, k -> new AtomicLong(0));
        counter.incrementAndGet();
    }

    private String getRateLimitTier(String method) {
        // Categorize methods by rate limit tier
        // This is a simplified example - in production, this would be more comprehensive
        if(method.contains("chat.post") || method.contains("conversations.create")) {
            return "tier1";
        } else if(method.contains("users.") || method.contains("conversations.list")) {
            return "tier3";
        } else if(method.contains("search.") || method.contains("files.upload")) {
            return "tier2";
        } else {
            return "tier4";
        }
    }




    @Override


    public void destroy() {
        try {
            super.destroy();
        } catch (AdapterException e) {
            log.error("Error during SlackInboundAdapter destroy", e);
        }

        // Close Socket Mode connection
        if(socketSession != null && socketSession.isOpen()) {
            try {
                socketSession.close();
            } catch(Exception e) {
                log.error("Error closing Socket Mode connection", e);
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
        userCache.clear();
        channelCache.clear();
        teamCache.clear();
        processedEvents.clear();
        rateLimitTracker.clear();
        rateLimitResetTimes.clear();
    }
}
