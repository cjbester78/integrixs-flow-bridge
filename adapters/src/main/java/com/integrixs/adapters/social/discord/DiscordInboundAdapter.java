package com.integrixs.adapters.social.discord;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.discord.DiscordApiConfig.*;
import com.integrixs.adapters.social.base.EventType;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DiscordInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(DiscordInboundAdapter.class);


    // These will be read from config
    private String apiBaseUrl;
    private String gatewayUrl;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Autowired
    private DiscordApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketSession gatewaySession;
    private ScheduledExecutorService heartbeatExecutor;
    private long lastSequence = 0;
    private String sessionId;
    private boolean isConnected = false;

    // Cache for frequently accessed data
    private final Map<String, JsonNode> guildCache = new ConcurrentHashMap<>();
    private final Map<String, JsonNode> channelCache = new ConcurrentHashMap<>();
    private final Map<String, JsonNode> userCache = new ConcurrentHashMap<>();

    private String channelId;

    @Override
    public com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.REST;
    }

    public AdapterType getType() {
        return AdapterType.REST; // Using REST as Discord is not yet in AdapterType enum
    }

    public String getName() {
        return "Discord API Adapter";
    }

    public DiscordApiConfig getDiscordConfig() {
        return config;
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("botToken", config.getBotToken());
        configMap.put("applicationId", config.getApplicationId());
        configMap.put("guildId", config.getGuildId());
        configMap.put("clientId", config.getClientId());
        configMap.put("clientSecret", config.getClientSecret());
        configMap.put("publicKey", config.getPublicKey());
        configMap.put("apiVersion", "v10");
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList(
            "READY", "RESUMED", "GUILD_CREATE", "GUILD_UPDATE", "GUILD_DELETE",
            "CHANNEL_CREATE", "CHANNEL_UPDATE", "CHANNEL_DELETE",
            "MESSAGE_CREATE", "MESSAGE_UPDATE", "MESSAGE_DELETE",
            "GUILD_MEMBER_ADD", "GUILD_MEMBER_UPDATE", "GUILD_MEMBER_REMOVE",
            "PRESENCE_UPDATE", "VOICE_STATE_UPDATE", "INTERACTION_CREATE"
        );
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Inbound adapters typically don't send data, they receive it
        // This is a no-op implementation as Discord inbound adapter only polls/receives data
        log.debug("doSend called on DiscordInboundAdapter - this is an inbound adapter, ignoring send request");
        return AdapterResult.success("Discord inbound adapter does not support sending");
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // No sender initialization needed for inbound adapter
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // No sender cleanup needed for inbound adapter
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test Discord API connection by getting current user info
            JsonNode response = makeAuthenticatedRequest("/users/@me", HttpMethod.GET);
            if (response != null) {
                String username = response.path("username").asText();
                String discriminator = response.path("discriminator").asText();
                return AdapterResult.success("Connected to Discord as " + username + "#" + discriminator);
            } else {
                return AdapterResult.failure("Failed to connect to Discord API");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) {
        // Delegate to the doSend method
        try {
            return doSend(payload, headers);
        } catch (Exception e) {
            log.error("Error sending message", e);
            return AdapterResult.failure("Failed to send: " + e.getMessage());
        }
    }

    // Initialize Gateway connection(for real - time events)
    @PostConstruct
    public void initialize() {
        // Initialize URLs from config
        this.apiBaseUrl = config.getApiBaseUrl();
        this.gatewayUrl = config.getGatewayUrl();
        if(config.getBotToken() != null) {
            connectToGateway();
        }
    }

    // Guild polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.discord.polling.guilds:3600000}")
    public void pollGuilds() {
        // Check if guild polling is enabled
        if(config.getGuildId() == null) {
            return; // Skip if no guild configured
        }

        try {
            log.debug("Polling Discord guilds");
            JsonNode guilds = makeAuthenticatedRequest("/users/@me/guilds", HttpMethod.GET);

            if(guilds != null && guilds.isArray()) {
                processGuilds(guilds);
            }
        } catch(Exception e) {
            log.error("Error polling guilds", e);
        }
    }

    // Channel polling for specific guild
    @Scheduled(fixedDelayString = "${integrixs.adapters.discord.polling.channels:1800000}")
    public void pollChannels() {
        // Check if channel polling is enabled
        if(config.getGuildId() == null) {
            return; // Skip if no guild configured
        }

        try {
            log.debug("Polling Discord channels for guild: {}", config.getGuildId());
            String endpoint = String.format("/guilds/%s/channels", config.getGuildId());
            JsonNode channels = makeAuthenticatedRequest(endpoint, HttpMethod.GET);

            if(channels != null && channels.isArray()) {
                processChannels(channels);
            }
        } catch(Exception e) {
            log.error("Error polling channels", e);
        }
    }

    // MessageDTO polling for specific channels
    @Scheduled(fixedDelayString = "${integrixs.adapters.discord.polling.messages:300000}")
    public void pollMessages() {
        // Check if message polling is enabled
        if(channelId == null || channelId.isEmpty()) {
            return; // Skip if no channel specified
        }

        try {
            // Poll messages for cached channels
            channelCache.keySet().forEach(channelId -> {
                try {
                    String endpoint = String.format("/channels/%s/messages?limit=%d", channelId, config.getLimits().getMessageHistoryLimit());
                    JsonNode messages = makeAuthenticatedRequest(endpoint, HttpMethod.GET);

                    if(messages != null && messages.isArray()) {
                        processMessages(channelId, messages);
                    }
                } catch(Exception e) {
                    log.error("Error polling messages for channel: {}", channelId, e);
                }
            });
        } catch(Exception e) {
            log.error("Error polling messages", e);
        }
    }

    // Member polling for specific guild
    @Scheduled(fixedDelayString = "${integrixs.adapters.discord.polling.members:3600000}")
    public void pollMembers() {
        // Check if member polling is enabled
        if(config.getGuildId() == null) {
            return; // Skip if no guild configured
        }

        try {
            log.debug("Polling Discord members for guild: {}", config.getGuildId());
            String endpoint = String.format("/guilds/%s/members?limit=%d", config.getGuildId(), config.getGuildMemberLimit());
            JsonNode members = makeAuthenticatedRequest(endpoint, HttpMethod.GET);

            if(members != null && members.isArray()) {
                processMembers(members);
            }
        } catch(Exception e) {
            log.error("Error polling members", e);
        }
    }

    // Scheduled events polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.discord.polling.events:1800000}")
    public void pollScheduledEvents() {
        // Check if event polling is enabled
        if(config.getGuildId() == null) {
            return; // Skip if no guild configured
        }

        try {
            log.debug("Polling Discord scheduled events");
            String endpoint = String.format("/guilds/%s/scheduled - events", config.getGuildId());
            JsonNode events = makeAuthenticatedRequest(endpoint, HttpMethod.GET);

            if(events != null && events.isArray()) {
                processScheduledEvents(events);
            }
        } catch(Exception e) {
            log.error("Error polling scheduled events", e);
        }
    }

    // Voice states polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.discord.polling.voice:60000}")
    public void pollVoiceStates() {
        if(!config.isEnableVoiceSupport()) {
            return;
        }

        try {
            // Voice states are typically received via Gateway events
            // This is a backup polling mechanism for specific guild
            if(config.getGuildId() != null && gatewaySession == null) {
                log.debug("Polling voice states via REST API");
                // Voice states are part of guild data
                String endpoint = String.format("/guilds/%s?with_counts = true", config.getGuildId());
                JsonNode guild = makeAuthenticatedRequest(endpoint, HttpMethod.GET);

                if(guild != null && guild.has("voice_states")) {
                    processVoiceStates(guild.get("voice_states"));
                }
            }
        } catch(Exception e) {
            log.error("Error polling voice states", e);
        }
    }

    // Gateway connection management
    private void connectToGateway() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            gatewaySession = client.doHandshake(new DiscordWebSocketHandler(), gatewayUrl).get();
            log.info("Connected to Discord Gateway");
        } catch(Exception e) {
            log.error("Failed to connect to Discord Gateway", e);
        }
    }

    // WebSocket handler for Gateway events
    private class DiscordWebSocketHandler extends TextWebSocketHandler {
        @Override
        protected void handleTextMessage(WebSocketSession session, org.springframework.web.socket.TextMessage message) {
            try {
                JsonNode payload = objectMapper.readTree(message.getPayload());
                handleGatewayEvent(payload);
            } catch(Exception e) {
                log.error("Error handling gateway message", e);
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
            log.warn("Discord Gateway connection closed: {}", status);
            isConnected = false;
            // Attempt to reconnect after delay
            scheduleReconnect();
        }
    }

    private void handleGatewayEvent(JsonNode payload) {
        int opcode = payload.get("op").asInt();

        switch(opcode) {
            case 0: // Dispatch
                handleDispatchEvent(payload);
                break;
            case 1: // Heartbeat request
                sendHeartbeat();
                break;
            case 7: // Reconnect
                reconnectGateway();
                break;
            case 9: // Invalid session
                handleInvalidSession(payload);
                break;
            case 10: // Hello
                handleHello(payload);
                break;
            case 11: // Heartbeat ACK
                log.trace("Heartbeat acknowledged");
                break;
        }

        // Update sequence number
        if(payload.has("s") && !payload.get("s").isNull()) {
            lastSequence = payload.get("s").asLong();
        }
    }

    private void handleDispatchEvent(JsonNode payload) {
        String eventType = payload.get("t").asText();
        JsonNode data = payload.get("d");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_type", eventType);
        eventData.put("timestamp", Instant.now().toString());
        eventData.put("data", data);

        // Cache relevant data based on event type
        switch(eventType) {
            case "GUILD_CREATE":
            case "GUILD_UPDATE":
                guildCache.put(data.get("id").asText(), data);
                break;
            case "CHANNEL_CREATE":
            case "CHANNEL_UPDATE":
                channelCache.put(data.get("id").asText(), data);
                break;
            case "READY":
                sessionId = data.get("session_id").asText();
                isConnected = true;
                break;
        }

        // Create message for queue
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        try {
            message.setPayload(objectMapper.writeValueAsString(eventData));
        } catch (Exception e) {
            log.error("Error serializing event data", e);
            return;
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "discord_event");
        headers.put("source", "discord_gateway");
        headers.put("timestamp", LocalDateTime.now().toString());
        message.setHeaders(headers);

        // Process the Discord event
        processDiscordEvent(message);
        log.info("Discord event received: {}", eventType);
    }

    private void handleHello(JsonNode payload) {
        JsonNode data = payload.get("d");
        int heartbeatInterval = data.get("heartbeat_interval").asInt();

        // Start heartbeat
        startHeartbeat(heartbeatInterval);

        // Send identify or resume
        if(sessionId != null) {
            sendResume();
        } else {
            sendIdentify();
        }
    }

    private void processDiscordEvent(MessageDTO message) {
        // Store the event for processing by the integration flow
        if (eventQueue != null) {
            eventQueue.offer(message);
        }
    }

    private final BlockingQueue<MessageDTO> eventQueue = new LinkedBlockingQueue<>();

    private void sendIdentify() {
        try {
            ObjectNode identify = objectMapper.createObjectNode();
            identify.put("op", 2);

            ObjectNode data = objectMapper.createObjectNode();
            data.put("token", config.getBotToken());
            data.put("intents", calculateIntents());

            ObjectNode properties = objectMapper.createObjectNode();
            properties.put("$os", "linux");
            properties.put("$browser", "integrixs");
            properties.put("$device", "integrixs");
            data.set("properties", properties);

            identify.set("d", data);

            gatewaySession.sendMessage(new org.springframework.web.socket.TextMessage(
                objectMapper.writeValueAsString(identify)
           ));
        } catch(Exception e) {
            log.error("Error sending identify", e);
        }
    }

    private void sendResume() {
        try {
            ObjectNode resume = objectMapper.createObjectNode();
            resume.put("op", 6);

            ObjectNode data = objectMapper.createObjectNode();
            data.put("token", config.getBotToken());
            data.put("session_id", sessionId);
            data.put("seq", lastSequence);

            resume.set("d", data);

            gatewaySession.sendMessage(new org.springframework.web.socket.TextMessage(
                objectMapper.writeValueAsString(resume)
           ));
        } catch(Exception e) {
            log.error("Error sending resume", e);
        }
    }

    private void startHeartbeat(int interval) {
        if(heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }

        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, interval, interval, TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeat() {
        try {
            ObjectNode heartbeat = objectMapper.createObjectNode();
            heartbeat.put("op", 1);
            heartbeat.put("d", lastSequence > 0 ? lastSequence : null);

            gatewaySession.sendMessage(new org.springframework.web.socket.TextMessage(
                objectMapper.writeValueAsString(heartbeat)
           ));
        } catch(Exception e) {
            log.error("Error sending heartbeat", e);
        }
    }

    private int calculateIntents() {
        int intents = 0;
        // Add intents based on configuration
        // Enable all basic intents for Discord gateway from config
        Map<String, Integer> intentBits = config.getGatewayIntentBits();
        intents |= 1 << intentBits.get("GUILDS");
        intents |= 1 << intentBits.get("GUILD_MEMBERS");
        intents |= 1 << intentBits.get("GUILD_MESSAGES");
        intents |= 1 << intentBits.get("DIRECT_MESSAGES");
        intents |= 1 << intentBits.get("MESSAGE_CONTENT");
        intents |= 1 << intentBits.get("GUILD_VOICE_STATES");
        intents |= 1 << intentBits.get("GUILD_SCHEDULED_EVENTS");
        return intents;
    }

    // Processing methods
    private void processGuilds(JsonNode guilds) {
        guilds.forEach(guild -> {
            try {
                Map<String, Object> guildData = new HashMap<>();
                guildData.put("id", guild.get("id").asText());
                guildData.put("name", guild.get("name").asText());
                guildData.put("icon", guild.has("icon") ? guild.get("icon").asText() : null);
                guildData.put("owner", guild.get("owner").asBoolean());
                guildData.put("permissions", guild.get("permissions").asText());

                // Cache guild data
                guildCache.put(guild.get("id").asText(), guild);

                MessageDTO message = new MessageDTO();
                message.setCorrelationId(UUID.randomUUID().toString());
                try {
                    message.setPayload(objectMapper.writeValueAsString(guildData));
                } catch (Exception ex) {
                    log.error("Error serializing guild data", ex);
                    return;
                }
                Map<String, Object> headers = new HashMap<>();
                headers.put("type", "guild");
                headers.put("source", "discord");
                headers.put("timestamp", LocalDateTime.now().toString());
                message.setHeaders(headers);

                processDiscordEvent(message);
            } catch(Exception e) {
                log.error("Error processing guild", e);
            }
        });
    }

    private void processChannels(JsonNode channels) {
        channels.forEach(channel -> {
            try {
                Map<String, Object> channelData = new HashMap<>();
                channelData.put("id", channel.get("id").asText());
                channelData.put("type", channel.get("type").asInt());
                channelData.put("guild_id", channel.has("guild_id") ? channel.get("guild_id").asText() : null);
                channelData.put("name", channel.has("name") ? channel.get("name").asText() : null);
                channelData.put("position", channel.get("position").asInt());
                channelData.put("parent_id", channel.has("parent_id") ? channel.get("parent_id").asText() : null);

                // Cache channel data
                channelCache.put(channel.get("id").asText(), channel);

                MessageDTO message = new MessageDTO();
                message.setCorrelationId(UUID.randomUUID().toString());
                try {
                    message.setPayload(objectMapper.writeValueAsString(channelData));
                } catch (Exception ex) {
                    log.error("Error serializing channel data", ex);
                    return;
                }
                Map<String, Object> headers = new HashMap<>();
                headers.put("type", "channel");
                headers.put("source", "discord");
                headers.put("timestamp", LocalDateTime.now().toString());
                message.setHeaders(headers);

                processDiscordEvent(message);
            } catch(Exception e) {
                log.error("Error processing channel", e);
            }
        });
    }

    private void processMessages(String channelId, JsonNode messages) {
        messages.forEach(message -> {
            try {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", message.get("id").asText());
                messageData.put("channel_id", channelId);
                messageData.put("author", message.get("author"));
                messageData.put("content", message.get("content").asText());
                messageData.put("timestamp", message.get("timestamp").asText());
                messageData.put("edited_timestamp", message.has("edited_timestamp") ?
                    message.get("edited_timestamp").asText() : null);
                messageData.put("tts", message.get("tts").asBoolean());
                messageData.put("mention_everyone", message.get("mention_everyone").asBoolean());
                messageData.put("attachments", message.get("attachments"));
                messageData.put("embeds", message.get("embeds"));
                messageData.put("reactions", message.has("reactions") ? message.get("reactions") : null);
                messageData.put("pinned", message.get("pinned").asBoolean());
                messageData.put("type", message.get("type").asInt());

                MessageDTO msg = new MessageDTO();
                msg.setCorrelationId(UUID.randomUUID().toString());
                try {
                    msg.setPayload(objectMapper.writeValueAsString(messageData));
                } catch (Exception ex) {
                    log.error("Error serializing message data", ex);
                    return;
                }
                Map<String, Object> msgHeaders = new HashMap<>();
                msgHeaders.put("type", "message");
                msgHeaders.put("source", "discord");
                msgHeaders.put("timestamp", LocalDateTime.now().toString());
                msg.setHeaders(msgHeaders);

                processDiscordEvent(msg);
            } catch(Exception e) {
                log.error("Error processing message", e);
            }
        });
    }

    private void processMembers(JsonNode members) {
        members.forEach(member -> {
            try {
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("user", member.get("user"));
                memberData.put("nick", member.has("nick") ? member.get("nick").asText() : null);
                memberData.put("roles", member.get("roles"));
                memberData.put("joined_at", member.get("joined_at").asText());
                memberData.put("premium_since", member.has("premium_since") ?
                    member.get("premium_since").asText() : null);
                memberData.put("deaf", member.get("deaf").asBoolean());
                memberData.put("mute", member.get("mute").asBoolean());

                MessageDTO message = new MessageDTO();
                message.setCorrelationId(UUID.randomUUID().toString());
                try {
                    message.setPayload(objectMapper.writeValueAsString(memberData));
                } catch (Exception ex) {
                    log.error("Error serializing member data", ex);
                    return;
                }
                Map<String, Object> headers = new HashMap<>();
                headers.put("type", "member");
                headers.put("source", "discord");
                headers.put("timestamp", LocalDateTime.now().toString());
                message.setHeaders(headers);

                processDiscordEvent(message);
            } catch(Exception e) {
                log.error("Error processing member", e);
            }
        });
    }

    private void processScheduledEvents(JsonNode events) {
        events.forEach(event -> {
            try {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("id", event.get("id").asText());
                eventData.put("guild_id", event.get("guild_id").asText());
                eventData.put("channel_id", event.has("channel_id") ? event.get("channel_id").asText() : null);
                eventData.put("creator_id", event.has("creator_id") ? event.get("creator_id").asText() : null);
                eventData.put("name", event.get("name").asText());
                eventData.put("description", event.has("description") ? event.get("description").asText() : null);
                eventData.put("scheduled_start_time", event.get("scheduled_start_time").asText());
                eventData.put("scheduled_end_time", event.has("scheduled_end_time") ?
                    event.get("scheduled_end_time").asText() : null);
                eventData.put("privacy_level", event.get("privacy_level").asInt());
                eventData.put("status", event.get("status").asInt());
                eventData.put("entity_type", event.get("entity_type").asInt());
                eventData.put("entity_id", event.has("entity_id") ? event.get("entity_id").asText() : null);
                eventData.put("entity_metadata", event.has("entity_metadata") ? event.get("entity_metadata") : null);
                eventData.put("user_count", event.has("user_count") ? event.get("user_count").asInt() : 0);

                MessageDTO message = new MessageDTO();
                message.setCorrelationId(UUID.randomUUID().toString());
                try {
                    message.setPayload(objectMapper.writeValueAsString(eventData));
                } catch (Exception ex) {
                    log.error("Error serializing scheduled event data", ex);
                    return;
                }
                Map<String, Object> headers = new HashMap<>();
                headers.put("type", "scheduled_event");
                headers.put("source", "discord");
                headers.put("timestamp", LocalDateTime.now().toString());
                message.setHeaders(headers);

                processDiscordEvent(message);
            } catch(Exception e) {
                log.error("Error processing scheduled event", e);
            }
        });
    }

    private void processVoiceStates(JsonNode voiceStates) {
        voiceStates.forEach(state -> {
            try {
                Map<String, Object> voiceData = new HashMap<>();
                voiceData.put("guild_id", state.has("guild_id") ? state.get("guild_id").asText() : null);
                voiceData.put("channel_id", state.has("channel_id") ? state.get("channel_id").asText() : null);
                voiceData.put("user_id", state.get("user_id").asText());
                voiceData.put("member", state.has("member") ? state.get("member") : null);
                voiceData.put("session_id", state.get("session_id").asText());
                voiceData.put("deaf", state.get("deaf").asBoolean());
                voiceData.put("mute", state.get("mute").asBoolean());
                voiceData.put("self_deaf", state.get("self_deaf").asBoolean());
                voiceData.put("self_mute", state.get("self_mute").asBoolean());
                voiceData.put("self_stream", state.has("self_stream") ? state.get("self_stream").asBoolean() : false);
                voiceData.put("self_video", state.get("self_video").asBoolean());
                voiceData.put("suppress", state.get("suppress").asBoolean());

                MessageDTO message = new MessageDTO();
                message.setCorrelationId(UUID.randomUUID().toString());
                try {
                    message.setPayload(objectMapper.writeValueAsString(voiceData));
                } catch (Exception ex) {
                    log.error("Error serializing voice state data", ex);
                    return;
                }
                Map<String, Object> headers = new HashMap<>();
                headers.put("type", "voice_state");
                headers.put("source", "discord");
                headers.put("timestamp", LocalDateTime.now().toString());
                message.setHeaders(headers);

                processDiscordEvent(message);
            } catch(Exception e) {
                log.error("Error processing voice state", e);
            }
        });
    }

    // Interaction/Webhook verification
    public boolean verifyWebhookSignature(String signature, String timestamp, String body) {
        try {
            String message = timestamp + body;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                config.getPublicKey().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
           );
            mac.init(secretKey);

            byte[] signatureBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(signatureBytes);

            return calculatedSignature.equals(signature);
        } catch(NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for(byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Reconnection logic
    private void scheduleReconnect() {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            log.info("Attempting to reconnect to Discord Gateway");
            connectToGateway();
        }, 5, TimeUnit.SECONDS);
    }

    private void reconnectGateway() {
        try {
            if(gatewaySession != null && gatewaySession.isOpen()) {
                gatewaySession.close();
            }
            connectToGateway();
        } catch(Exception e) {
            log.error("Error reconnecting to gateway", e);
        }
    }

    private void handleInvalidSession(JsonNode payload) {
        boolean resumable = payload.get("d").asBoolean();
        if(resumable) {
            sendResume();
        } else {
            sessionId = null;
            sendIdentify();
        }
    }

    // Helper methods
    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method) {
        return makeAuthenticatedRequest(endpoint, method, null);
    }

    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bot " + config.getBotToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                apiBaseUrl + endpoint,
                method,
                entity,
                JsonNode.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Error making authenticated request to Discord API: {} {}", method, endpoint, e);
            return null;
        }
    }


    @Override
    public void destroy() {
        try {
            super.destroy();
        } catch(AdapterException e) {
            log.error("Error destroying adapter", e);
        }
        if(heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        if(gatewaySession != null && gatewaySession.isOpen()) {
            try {
                gatewaySession.close();
            } catch(Exception e) {
                log.error("Error closing gateway session", e);
            }
        }
    }
}
