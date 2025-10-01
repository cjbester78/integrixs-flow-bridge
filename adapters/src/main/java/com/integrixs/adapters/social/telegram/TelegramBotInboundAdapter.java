package com.integrixs.adapters.social.telegram;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.telegram.TelegramBotApiConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TelegramBotInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TelegramBotInboundAdapter.class);


    private static final String API_URL_FORMAT = "%s/bot%s/%s";

    @Autowired
    private TelegramBotApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // For long polling
    private AtomicLong lastUpdateId = new AtomicLong(0);

    // Cache for user and chat information
    private final Map<Long, JsonNode> userCache = new ConcurrentHashMap<>();
    private final Map<Long, JsonNode> chatCache = new ConcurrentHashMap<>();

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList(
            "MESSAGE", "EDITED_MESSAGE", "CHANNEL_POST", "EDITED_CHANNEL_POST",
            "INLINE_QUERY", "CHOSEN_INLINE_RESULT", "CALLBACK_QUERY", "SHIPPING_QUERY",
            "PRE_CHECKOUT_QUERY", "POLL", "POLL_ANSWER", "MY_CHAT_MEMBER",
            "CHAT_MEMBER", "CHAT_JOIN_REQUEST", "MESSAGE_REACTION", "MESSAGE_REACTION_COUNT",
            "CHAT_BOOST", "REMOVED_CHAT_BOOST", "BUSINESS_CONNECTION", "BUSINESS_MESSAGE",
            "EDITED_BUSINESS_MESSAGE", "DELETED_BUSINESS_MESSAGES"
        );
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("botToken", config.getBotToken());
        configMap.put("apiUrl", config.getApiUrl());
        configMap.put("webhookUrl", config.getWebhookUrl());
        configMap.put("webhookPath", config.getWebhookPath());
        configMap.put("webhookSecret", config.getWebhookSecret());
        configMap.put("defaultChatId", config.getDefaultChatId());
        configMap.put("features", config.getFeatures());
        configMap.put("limits", config.getLimits());
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    public void initialize() {
        try {
            super.initialize();
        } catch (Exception e) {
            log.error("Error initializing TelegramBotInboundAdapter", e);
        }

        // Set webhook if enabled and URL is provided
        if(config.getFeatures().isEnableWebhooks() && config.getWebhookUrl() != null) {
            setWebhook();
        } else if(config.getFeatures().isEnablePolling()) {
            // Delete webhook to enable polling
            deleteWebhook();
        }
    }

    // Polling for updates(when webhooks are not used)
    @Scheduled(fixedDelayString = "${integrixs.adapters.telegram.bot.polling.interval:1000}")
    public void pollUpdates() {
        if(!config.getFeatures().isEnablePolling() ||
            (config.getFeatures().isEnableWebhooks() && config.getWebhookUrl() != null)) {
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("offset", lastUpdateId.get() + 1);
            params.put("limit", 100);
            params.put("timeout", 30); // Long polling timeout

            // Specify which update types to receive
            List<String> allowedUpdates = new ArrayList<>();
            if(config.getFeatures().isEnableMessages()) {
                allowedUpdates.add("message");
                allowedUpdates.add("edited_message");
            }
            if(config.getFeatures().isEnableChannelPosts()) {
                allowedUpdates.add("channel_post");
                allowedUpdates.add("edited_channel_post");
            }
            if(config.getFeatures().isEnableInlineQueries()) {
                allowedUpdates.add("inline_query");
                allowedUpdates.add("chosen_inline_result");
            }
            if(config.getFeatures().isEnableCallbackQueries()) {
                allowedUpdates.add("callback_query");
            }
            if(config.getFeatures().isEnablePolls()) {
                allowedUpdates.add("poll");
                allowedUpdates.add("poll_answer");
            }
            params.put("allowed_updates", allowedUpdates);

            JsonNode response = makeApiRequest("getUpdates", params);

            if(response != null && response.has("result")) {
                JsonNode updates = response.get("result");
                processUpdates(updates);
            }
        } catch(Exception e) {
            log.error("Error polling updates", e);
        }
    }

    // Process updates from polling or webhook
    private void processUpdates(JsonNode updates) {
        if(updates.isArray()) {
            for(JsonNode update : updates) {
                try {
                    long updateId = update.get("update_id").asLong();
                    lastUpdateId.updateAndGet(current -> Math.max(current, updateId));

                    processUpdate(update);
                } catch(Exception e) {
                    log.error("Error processing update", e);
                }
            }
        }
    }

    private void processUpdate(JsonNode update) {
        String updateType = determineUpdateType(update);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("update_id", update.get("update_id").asLong());
        updateData.put("type", updateType);
        updateData.put("timestamp", Instant.now().toString());
        updateData.put("data", update);

        // Cache user and chat information
        cacheUserAndChatInfo(update);

        try {
            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "telegram_update",
                "category", updateType,
                "source", "telegram_bot"
            ));
            message.setPayload(objectMapper.writeValueAsString(updateData));

            send(message);
        } catch (Exception ex) {
            log.error("Error processing update", ex);
        }
    }

    private String determineUpdateType(JsonNode update) {
        if(update.has("message")) return "MESSAGE";
        if(update.has("edited_message")) return "EDITED_MESSAGE";
        if(update.has("channel_post")) return "CHANNEL_POST";
        if(update.has("edited_channel_post")) return "EDITED_CHANNEL_POST";
        if(update.has("inline_query")) return "INLINE_QUERY";
        if(update.has("chosen_inline_result")) return "CHOSEN_INLINE_RESULT";
        if(update.has("callback_query")) return "CALLBACK_QUERY";
        if(update.has("shipping_query")) return "SHIPPING_QUERY";
        if(update.has("pre_checkout_query")) return "PRE_CHECKOUT_QUERY";
        if(update.has("poll")) return "POLL";
        if(update.has("poll_answer")) return "POLL_ANSWER";
        if(update.has("my_chat_member")) return "MY_CHAT_MEMBER";
        if(update.has("chat_member")) return "CHAT_MEMBER";
        if(update.has("chat_join_request")) return "CHAT_JOIN_REQUEST";
        if(update.has("message_reaction")) return "MESSAGE_REACTION";
        if(update.has("message_reaction_count")) return "MESSAGE_REACTION_COUNT";
        if(update.has("chat_boost")) return "CHAT_BOOST";
        if(update.has("removed_chat_boost")) return "REMOVED_CHAT_BOOST";
        if(update.has("business_connection")) return "BUSINESS_CONNECTION";
        if(update.has("business_message")) return "BUSINESS_MESSAGE";
        if(update.has("edited_business_message")) return "EDITED_BUSINESS_MESSAGE";
        if(update.has("deleted_business_messages")) return "DELETED_BUSINESS_MESSAGES";
        return "UNKNOWN";
    }

    // Webhook handling
    public void handleWebhookEvent(JsonNode event) {
        try {
            processUpdate(event);
        } catch(Exception e) {
            log.error("Error handling webhook event", e);
        }
    }

    public boolean verifyWebhookSignature(String signature, String body) {
        if(config.getWebhookSecret() == null) {
            return true; // No secret configured, skip verification
        }

        try {
            // Telegram uses SHA-256 HMAC
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                config.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
           );
            sha256Hmac.init(secretKey);

            byte[] signatureBytes = sha256Hmac.doFinal(
                body.getBytes(StandardCharsets.UTF_8)
           );

            String calculatedSignature = Base64.getEncoder().encodeToString(signatureBytes);
            return calculatedSignature.equals(signature);
        } catch(NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    // Bot info polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.telegram.bot.polling.botInfo:3600000}")
    public void pollBotInfo() {
        try {
            log.debug("Polling Telegram bot info");
            JsonNode response = makeApiRequest("getMe", null);

            if(response != null && response.has("result")) {
                processBotInfo(response.get("result"));
            }
        } catch(Exception e) {
            log.error("Error polling bot info", e);
        }
    }

    // Chat info polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.telegram.bot.polling.chats:1800000}")
    public void pollChats() {
        if(!config.getFeatures().isEnableGroupManagement() || config.getDefaultChatId() == null) {
            return;
        }

        try {
            log.debug("Polling chat info for: {}", config.getDefaultChatId());
            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", config.getDefaultChatId());

            JsonNode response = makeApiRequest("getChat", params);
            if(response != null && response.has("result")) {
                processChatInfo(response.get("result"));
            }

            // Get chat administrators
            response = makeApiRequest("getChatAdministrators", params);
            if(response != null && response.has("result")) {
                processChatAdministrators(response.get("result"));
            }

            // Get chat member count
            response = makeApiRequest("getChatMemberCount", params);
            if(response != null && response.has("result")) {
                processChatMemberCount(response.get("result").asInt());
            }
        } catch(Exception e) {
            log.error("Error polling chat info", e);
        }
    }

    // Commands polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.telegram.bot.polling.commands:3600000}")
    public void pollCommands() {
        if(!config.getFeatures().isEnableCommands()) {
            return;
        }

        try {
            log.debug("Polling bot commands");
            JsonNode response = makeApiRequest("getMyCommands", null);

            if(response != null && response.has("result")) {
                processCommands(response.get("result"));
            }
        } catch(Exception e) {
            log.error("Error polling commands", e);
        }
    }

    // Sticker sets polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.telegram.bot.polling.stickers:7200000}")
    public void pollStickerSets() {
        if(!config.getFeatures().isEnableStickers()) {
            return;
        }

        try {
            log.debug("Polling bot sticker sets");
            // Get custom emoji stickers
            JsonNode response = makeApiRequest("getCustomEmojiStickers", null);

            if(response != null && response.has("result")) {
                processCustomEmojiStickers(response.get("result"));
            }
        } catch(Exception e) {
            log.error("Error polling sticker sets", e);
        }
    }

    // Processing methods
    private void processBotInfo(JsonNode botInfo) {
        try {
            Map<String, Object> botData = new HashMap<>();
            botData.put("id", botInfo.get("id").asLong());
            botData.put("is_bot", botInfo.get("is_bot").asBoolean());
            botData.put("first_name", botInfo.get("first_name").asText());
            botData.put("username", botInfo.get("username").asText());
            botData.put("can_join_groups", botInfo.get("can_join_groups").asBoolean());
            botData.put("can_read_all_group_messages",
                botInfo.get("can_read_all_group_messages").asBoolean());
            botData.put("supports_inline_queries",
                botInfo.get("supports_inline_queries").asBoolean());

            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "bot_info",
                "category", "bot_data",
                "source", "telegram_bot"
            ));
            message.setPayload(objectMapper.writeValueAsString(botData));

            send(message);
        } catch(Exception e) {
            log.error("Error processing bot info", e);
        }
    }

    private void processChatInfo(JsonNode chatInfo) {
        try {
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("id", chatInfo.get("id").asLong());
            chatData.put("type", chatInfo.get("type").asText());
            chatData.put("title", chatInfo.has("title") ? chatInfo.get("title").asText() : null);
            chatData.put("username", chatInfo.has("username") ?
                chatInfo.get("username").asText() : null);
            chatData.put("description", chatInfo.has("description") ?
                chatInfo.get("description").asText() : null);
            chatData.put("invite_link", chatInfo.has("invite_link") ?
                chatInfo.get("invite_link").asText() : null);

            // Cache chat info
            chatCache.put(chatInfo.get("id").asLong(), chatInfo);

            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "chat_info",
                "category", "chat_data",
                "source", "telegram_bot"
            ));
            message.setPayload(objectMapper.writeValueAsString(chatData));

            send(message);
        } catch(Exception e) {
            log.error("Error processing chat info", e);
        }
    }

    private void processChatAdministrators(JsonNode administrators) {
        try {
            List<Map<String, Object>> adminList = new ArrayList<>();

            for(JsonNode admin : administrators) {
                Map<String, Object> adminData = new HashMap<>();
                adminData.put("user", admin.get("user"));
                adminData.put("status", admin.get("status").asText());
                adminData.put("is_anonymous", admin.get("is_anonymous").asBoolean());

                if(admin.has("custom_title")) {
                    adminData.put("custom_title", admin.get("custom_title").asText());
                }

                // Cache user info
                JsonNode user = admin.get("user");
                userCache.put(user.get("id").asLong(), user);

                adminList.add(adminData);
            }

            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "chat_administrators",
                "category", "chat_data",
                "source", "telegram_bot"
            ));
            message.setPayload(objectMapper.writeValueAsString(adminList));

            send(message);
        } catch(Exception e) {
            log.error("Error processing chat administrators", e);
        }
    }

    private void processChatMemberCount(int count) {
        try {
            Map<String, Object> countData = new HashMap<>();
            countData.put("chat_id", config.getDefaultChatId());
            countData.put("member_count", count);

            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "chat_member_count",
                "category", "chat_data",
                "source", "telegram_bot"
            ));
            message.setPayload(objectMapper.writeValueAsString(countData));

            send(message);
        } catch(Exception e) {
            log.error("Error processing chat member count", e);
        }
    }

    private void processCommands(JsonNode commands) {
        try {
            List<Map<String, Object>> commandList = new ArrayList<>();

            for(JsonNode command : commands) {
                Map<String, Object> commandData = new HashMap<>();
                commandData.put("command", command.get("command").asText());
                commandData.put("description", command.get("description").asText());
                commandList.add(commandData);
            }

            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "bot_commands",
                "category", "bot_data",
                "source", "telegram_bot"
            ));
            message.setPayload(objectMapper.writeValueAsString(commandList));

            send(message);
        } catch(Exception e) {
            log.error("Error processing commands", e);
        }
    }

    private void processCustomEmojiStickers(JsonNode stickers) {
        try {
            List<Map<String, Object>> stickerList = new ArrayList<>();

            for(JsonNode sticker : stickers) {
                Map<String, Object> stickerData = new HashMap<>();
                stickerData.put("file_id", sticker.get("file_id").asText());
                stickerData.put("file_unique_id", sticker.get("file_unique_id").asText());
                stickerData.put("type", sticker.get("type").asText());
                stickerData.put("width", sticker.get("width").asInt());
                stickerData.put("height", sticker.get("height").asInt());
                stickerData.put("is_animated", sticker.get("is_animated").asBoolean());
                stickerData.put("is_video", sticker.get("is_video").asBoolean());

                if(sticker.has("emoji")) {
                    stickerData.put("emoji", sticker.get("emoji").asText());
                }
                if(sticker.has("set_name")) {
                    stickerData.put("set_name", sticker.get("set_name").asText());
                }

                stickerList.add(stickerData);
            }

            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "custom_emoji_stickers",
                "category", "sticker_data",
                "source", "telegram_bot"
            ));
            message.setPayload(objectMapper.writeValueAsString(stickerList));

            send(message);
        } catch(Exception e) {
            log.error("Error processing custom emoji stickers", e);
        }
    }

    private void cacheUserAndChatInfo(JsonNode update) {
        try {
            // Extract and cache user info
            JsonNode message = null;
            if(update.has("message")) message = update.get("message");
            else if(update.has("edited_message")) message = update.get("edited_message");
            else if(update.has("channel_post")) message = update.get("channel_post");
            else if(update.has("edited_channel_post")) message = update.get("edited_channel_post");

            if(message != null) {
                if(message.has("from")) {
                    JsonNode from = message.get("from");
                    userCache.put(from.get("id").asLong(), from);
                }
                if(message.has("chat")) {
                    JsonNode chat = message.get("chat");
                    chatCache.put(chat.get("id").asLong(), chat);
                }
            }

            // Handle callback queries
            if(update.has("callback_query")) {
                JsonNode callbackQuery = update.get("callback_query");
                if(callbackQuery.has("from")) {
                    JsonNode from = callbackQuery.get("from");
                    userCache.put(from.get("id").asLong(), from);
                }
            }

            // Handle inline queries
            if(update.has("inline_query")) {
                JsonNode inlineQuery = update.get("inline_query");
                if(inlineQuery.has("from")) {
                    JsonNode from = inlineQuery.get("from");
                    userCache.put(from.get("id").asLong(), from);
                }
            }
        } catch(Exception e) {
            log.error("Error caching user and chat info", e);
        }
    }

    // Webhook management
    private void setWebhook() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("url", config.getWebhookUrl() + config.getWebhookPath());

            if(config.getWebhookSecret() != null) {
                params.put("secret_token", config.getWebhookSecret());
            }

            // Specify allowed updates
            List<String> allowedUpdates = new ArrayList<>();
            if(config.getFeatures().isEnableMessages()) {
                allowedUpdates.add("message");
                allowedUpdates.add("edited_message");
            }
            if(config.getFeatures().isEnableChannelPosts()) {
                allowedUpdates.add("channel_post");
                allowedUpdates.add("edited_channel_post");
            }
            if(config.getFeatures().isEnableInlineQueries()) {
                allowedUpdates.add("inline_query");
                allowedUpdates.add("chosen_inline_result");
            }
            if(config.getFeatures().isEnableCallbackQueries()) {
                allowedUpdates.add("callback_query");
            }
            params.put("allowed_updates", allowedUpdates);
            params.put("max_connections", config.getLimits().getWebhookMaxConnections());
            params.put("drop_pending_updates", true);

            JsonNode response = makeApiRequest("setWebhook", params);

            if(response != null && response.has("result") && response.get("result").asBoolean()) {
                log.info("Webhook set successfully");
            } else {
                log.error("Failed to set webhook: {}", response);
            }
        } catch(Exception e) {
            log.error("Error setting webhook", e);
        }
    }

    private void deleteWebhook() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("drop_pending_updates", true);

            JsonNode response = makeApiRequest("deleteWebhook", params);

            if(response != null && response.has("result") && response.get("result").asBoolean()) {
                log.info("Webhook deleted successfully");
            }
        } catch(Exception e) {
            log.error("Error deleting webhook", e);
        }
    }

    // API request helper
    private JsonNode makeApiRequest(String method, Map<String, Object> params) {
        try {
            String url = String.format(API_URL_FORMAT, config.getApiUrl(), config.getBotToken(), method);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = params != null ?
                new HttpEntity<>(params, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
           );

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if(responseJson.has("ok") && !responseJson.get("ok").asBoolean()) {
                log.error("Telegram API error: {}", responseJson.get("description").asText());
                return null;
            }

            return responseJson;
        } catch(HttpClientErrorException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch(Exception e) {
            log.error("Failed to make API request", e);
            return null;
        }
    }

    @Override
    public void destroy() {
        try {
            super.destroy();
        } catch (Exception e) {
            log.error("Error destroying TelegramBotInboundAdapter", e);
        }
        // Clear caches
        userCache.clear();
        chatCache.clear();
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        log.info("Initializing Telegram Bot adapter");
        // Initialization logic if needed
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.info("Destroying Telegram Bot adapter");
        // Cleanup logic if needed
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // This is an inbound adapter, so we don't typically send data
        // But we need to implement this method
        log.debug("Sending data through Telegram Bot adapter");
        return AdapterResult.success("Message processed successfully");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            JsonNode response = makeApiRequest("getMe", null);
            if (response != null && response.has("result")) {
                return AdapterResult.success("Connection test successful");
            } else {
                return AdapterResult.failure("Connection test failed: No response");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        try {
            return doSend(payload, headers);
        } catch (Exception e) {
            throw new AdapterException("Failed to send data", e);
        }
    }
}
