package com.integrixs.adapters.social.discord;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.social.discord.DiscordApiConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.enums.MessageStatus;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class DiscordOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(DiscordOutboundAdapter.class);

    public DiscordOutboundAdapter() {
        super(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.REST);
    }


    // This will be read from config
    private String apiBaseUrl;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Autowired
    private DiscordApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() {
        this.apiBaseUrl = config.getApiBaseUrl();
    }

    @Override
    protected long getPollingIntervalMs() {
        return config.getPollingIntervalMs() != null ? config.getPollingIntervalMs() : 30000;
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        // No receiver initialization needed for outbound adapter
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // No receiver cleanup needed for outbound adapter
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapters typically don't receive data
        return AdapterResult.success(null, "Outbound adapter does not support receiving");
    }

    @Override
    public com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bot " + config.getBotToken());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiBaseUrl + "/users/@me",
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success("Discord connection successful");
            } else {
                return AdapterResult.failure("Discord connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Discord connection test failed: " + e.getMessage());
        }
    }

    public AdapterType getType() {
        return AdapterType.REST;
    }

    public String getName() {
        return "Discord API Outbound Adapter";
    }

    public MessageDTO processMessage(MessageDTO message) throws AdapterException {
        try {
            String operation = (String) message.getHeaders().get("operation");
            if(operation == null) {
                throw new AdapterException("Operation header is required");
            }

            log.info("Processing Discord operation: {}", operation);

            switch(operation.toLowerCase()) {
                case "send_message":
                    return sendMessage(message);
                case "send_embed":
                    return sendEmbed(message);
                case "create_channel":
                    return createChannel(message);
                case "delete_message":
                    return deleteMessage(message);
                case "edit_message":
                    return editMessage(message);
                case "create_webhook":
                    return createWebhook(message);
                case "send_webhook":
                    return sendWebhookMessage(message);
                case "add_reaction":
                    return addReaction(message);
                case "create_thread":
                    return createThread(message);
                case "send_dm":
                    return sendDirectMessage(message);
                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error processing Discord operation", e);
            throw new AdapterException("Failed to process Discord operation", e);
        }
    }

    // MessageDTO Operations
    private MessageDTO sendMessage(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();

            ObjectNode messageData = objectMapper.createObjectNode();

            // Basic message content
            if(content.has("content")) {
                messageData.put("content", content.get("content").asText());
            }

            // Embeds
            if(content.has("embeds")) {
                messageData.set("embeds", content.get("embeds"));
            }

            // Components(buttons, select menus)
            if(content.has("components")) {
                messageData.set("components", content.get("components"));
            }

            // File attachments
            if(content.has("files")) {
                // Handle file uploads separately
                return sendMessageWithFiles(channelId, messageData, content.get("files"));
            }

            // Mentions
            if(content.has("allowed_mentions")) {
                messageData.set("allowed_mentions", content.get("allowed_mentions"));
            }

            // MessageDTO reference(for replies)
            if(content.has("message_reference")) {
                messageData.set("message_reference", content.get("message_reference"));
            }

            // TTS
            if(content.has("tts")) {
                messageData.put("tts", content.get("tts").asBoolean());
            }

            // Sticker IDs
            if(content.has("sticker_ids")) {
                messageData.set("sticker_ids", content.get("sticker_ids"));
            }

            String endpoint = String.format("/channels/%s/messages", channelId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, messageData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending message", e);
            throw new AdapterException("Failed to send message", e);
        }
    }

    private MessageDTO sendEmbed(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();

            ObjectNode messageData = objectMapper.createObjectNode();

            // Create embed
            ArrayNode embeds = objectMapper.createArrayNode();
            ObjectNode embed = objectMapper.createObjectNode();

            if(content.has("title")) {
                embed.put("title", content.get("title").asText());
            }
            if(content.has("description")) {
                embed.put("description", content.get("description").asText());
            }
            if(content.has("url")) {
                embed.put("url", content.get("url").asText());
            }
            if(content.has("color")) {
                embed.put("color", content.get("color").asInt());
            }
            if(content.has("timestamp")) {
                embed.put("timestamp", content.get("timestamp").asText());
            }

            // Author
            if(content.has("author")) {
                embed.set("author", content.get("author"));
            }

            // Fields
            if(content.has("fields")) {
                embed.set("fields", content.get("fields"));
            }

            // Thumbnail
            if(content.has("thumbnail")) {
                embed.set("thumbnail", content.get("thumbnail"));
            }

            // Image
            if(content.has("image")) {
                embed.set("image", content.get("image"));
            }

            // Footer
            if(content.has("footer")) {
                embed.set("footer", content.get("footer"));
            }

            embeds.add(embed);
            messageData.set("embeds", embeds);

            String endpoint = String.format("/channels/%s/messages", channelId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, messageData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending embed", e);
            throw new AdapterException("Failed to send embed", e);
        }
    }

    private MessageDTO sendFile(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add file
            String base64Data = content.get("file_data").asText();
            byte[] fileBytes = Base64.getDecoder().decode(base64Data);
            String filename = content.get("filename").asText();

            body.add("files[0]", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            // Add payload JSON
            ObjectNode payload = objectMapper.createObjectNode();
            if(content.has("content")) {
                payload.put("content", content.get("content").asText());
            }

            body.add("payload_json", objectMapper.writeValueAsString(payload));

            String endpoint = String.format("/channels/%s/messages", channelId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bot " + config.getBotToken());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiBaseUrl + endpoint, HttpMethod.POST, requestEntity, String.class
           );

            return createSuccessResponse(message, objectMapper.readTree(response.getBody()));
        } catch(Exception e) {
            log.error("Error sending file", e);
            throw new AdapterException("Failed to send file", e);
        }
    }

    private MessageDTO sendMessageWithFiles(String channelId, ObjectNode messageData, JsonNode files) throws AdapterException {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add files
            int fileIndex = 0;
            for(JsonNode file : files) {
                String base64Data = file.get("data").asText();
                byte[] fileBytes = Base64.getDecoder().decode(base64Data);
                String filename = file.get("filename").asText();

                body.add("files[" + fileIndex + "]", new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                });
                fileIndex++;
            }

            // Add message payload
            body.add("payload_json", objectMapper.writeValueAsString(messageData));

            String endpoint = String.format("/channels/%s/messages", channelId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bot " + config.getBotToken());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiBaseUrl + endpoint, HttpMethod.POST, requestEntity, String.class
           );

            return createSuccessResponse(null, objectMapper.readTree(response.getBody()));
        } catch(Exception e) {
            log.error("Error sending message with files", e);
            throw new AdapterException("Failed to send message with files", e);
        }
    }

    private MessageDTO editMessage(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();
            String messageId = content.get("message_id").asText();

            ObjectNode updates = objectMapper.createObjectNode();
            if(content.has("content")) {
                updates.put("content", content.get("content").asText());
            }
            if(content.has("embeds")) {
                updates.set("embeds", content.get("embeds"));
            }
            if(content.has("components")) {
                updates.set("components", content.get("components"));
            }

            String endpoint = String.format("/channels/%s/messages/%s", channelId, messageId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.PATCH, updates);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error editing message", e);
            throw new AdapterException("Failed to edit message", e);
        }
    }

    private MessageDTO deleteMessage(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();
            String messageId = content.get("message_id").asText();

            String endpoint = String.format("/channels/%s/messages/%s", channelId, messageId);
            makeAuthenticatedRequest(endpoint, HttpMethod.DELETE, null);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("message_id", messageId));
        } catch(Exception e) {
            log.error("Error deleting message", e);
            throw new AdapterException("Failed to delete message", e);
        }
    }

    private MessageDTO bulkDeleteMessages(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();

            ObjectNode body = objectMapper.createObjectNode();
            body.set("messages", content.get("message_ids"));

            String endpoint = String.format("/channels/%s/messages/bulk - delete", channelId);
            makeAuthenticatedRequest(endpoint, HttpMethod.POST, body);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("count", content.get("message_ids").size()));
        } catch(Exception e) {
            log.error("Error bulk deleting messages", e);
            throw new AdapterException("Failed to bulk delete messages", e);
        }
    }

    // Channel Operations
    private MessageDTO createChannel(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();

            ObjectNode channelData = objectMapper.createObjectNode();
            channelData.put("name", content.get("name").asText());
            channelData.put("type", content.has("type") ? content.get("type").asInt() : 0);

            if(content.has("topic")) {
                channelData.put("topic", content.get("topic").asText());
            }
            if(content.has("bitrate")) {
                channelData.put("bitrate", content.get("bitrate").asInt());
            }
            if(content.has("user_limit")) {
                channelData.put("user_limit", content.get("user_limit").asInt());
            }
            if(content.has("position")) {
                channelData.put("position", content.get("position").asInt());
            }
            if(content.has("parent_id")) {
                channelData.put("parent_id", content.get("parent_id").asText());
            }
            if(content.has("nsfw")) {
                channelData.put("nsfw", content.get("nsfw").asBoolean());
            }

            String endpoint = String.format("/guilds/%s/channels", guildId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, channelData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating channel", e);
            throw new AdapterException("Failed to create channel", e);
        }
    }

    // Guild Operations
    private MessageDTO createRole(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();

            ObjectNode roleData = objectMapper.createObjectNode();
            if(content.has("name")) {
                roleData.put("name", content.get("name").asText());
            }
            if(content.has("permissions")) {
                roleData.put("permissions", content.get("permissions").asText());
            }
            if(content.has("color")) {
                roleData.put("color", content.get("color").asInt());
            }
            if(content.has("hoist")) {
                roleData.put("hoist", content.get("hoist").asBoolean());
            }
            if(content.has("mentionable")) {
                roleData.put("mentionable", content.get("mentionable").asBoolean());
            }

            String endpoint = String.format("/guilds/%s/roles", guildId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, roleData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating role", e);
            throw new AdapterException("Failed to create role", e);
        }
    }

    private MessageDTO modifyMember(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String userId = content.get("user_id").asText();

            ObjectNode memberData = objectMapper.createObjectNode();
            if(content.has("nick")) {
                memberData.put("nick", content.get("nick").asText());
            }
            if(content.has("roles")) {
                memberData.set("roles", content.get("roles"));
            }
            if(content.has("mute")) {
                memberData.put("mute", content.get("mute").asBoolean());
            }
            if(content.has("deaf")) {
                memberData.put("deaf", content.get("deaf").asBoolean());
            }
            if(content.has("channel_id")) {
                memberData.put("channel_id", content.get("channel_id").asText());
            }

            String endpoint = String.format("/guilds/%s/members/%s", guildId, userId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.PATCH, memberData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error modifying member", e);
            throw new AdapterException("Failed to modify member", e);
        }
    }

    private MessageDTO banMember(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String userId = content.get("user_id").asText();

            ObjectNode banData = objectMapper.createObjectNode();
            if(content.has("delete_message_seconds")) {
                banData.put("delete_message_seconds", content.get("delete_message_seconds").asInt());
            }

            String endpoint = String.format("/guilds/%s/bans/%s", guildId, userId);
            makeAuthenticatedRequest(endpoint, HttpMethod.PUT, banData);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("user_id", userId));
        } catch(Exception e) {
            log.error("Error banning member", e);
            throw new AdapterException("Failed to ban member", e);
        }
    }

    // Webhook Operations
    private MessageDTO createWebhook(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();

            ObjectNode webhookData = objectMapper.createObjectNode();
            webhookData.put("name", content.get("name").asText());
            if(content.has("avatar")) {
                webhookData.put("avatar", content.get("avatar").asText());
            }

            String endpoint = String.format("/channels/%s/webhooks", channelId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, webhookData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating webhook", e);
            throw new AdapterException("Failed to create webhook", e);
        }
    }

    private MessageDTO executeWebhook(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String webhookId = content.get("webhook_id").asText();
            String webhookToken = content.get("webhook_token").asText();

            ObjectNode webhookMessage = objectMapper.createObjectNode();
            if(content.has("content")) {
                webhookMessage.put("content", content.get("content").asText());
            }
            if(content.has("username")) {
                webhookMessage.put("username", content.get("username").asText());
            }
            if(content.has("avatar_url")) {
                webhookMessage.put("avatar_url", content.get("avatar_url").asText());
            }
            if(content.has("embeds")) {
                webhookMessage.set("embeds", content.get("embeds"));
            }

            String endpoint = String.format("/webhooks/%s/%s", webhookId, webhookToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Webhook execution doesn't need bot token

            HttpEntity<String> entity = new HttpEntity<>(webhookMessage.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiBaseUrl + endpoint, HttpMethod.POST, entity, String.class
           );

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("status", response.getStatusCodeValue()));
        } catch(Exception e) {
            log.error("Error executing webhook", e);
            throw new AdapterException("Failed to execute webhook", e);
        }
    }


    private MessageDTO sendWebhookMessage(MessageDTO message) throws AdapterException {
        return executeWebhook(message);
    }

    // Scheduled Event Operations
    private MessageDTO createScheduledEvent(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();

            ObjectNode eventData = objectMapper.createObjectNode();
            eventData.put("name", content.get("name").asText());
            eventData.put("privacy_level", content.has("privacy_level") ?
                content.get("privacy_level").asInt() : config.getDefaultPrivacyLevel()); // GUILD_ONLY
            eventData.put("scheduled_start_time", content.get("scheduled_start_time").asText());
            eventData.put("entity_type", content.get("entity_type").asInt());

            if(content.has("channel_id")) {
                eventData.put("channel_id", content.get("channel_id").asText());
            }
            if(content.has("entity_metadata")) {
                eventData.set("entity_metadata", content.get("entity_metadata"));
            }
            if(content.has("scheduled_end_time")) {
                eventData.put("scheduled_end_time", content.get("scheduled_end_time").asText());
            }
            if(content.has("description")) {
                eventData.put("description", content.get("description").asText());
            }

            String endpoint = String.format("/guilds/%s/scheduled - events", guildId);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, eventData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating scheduled event", e);
            throw new AdapterException("Failed to create scheduled event", e);
        }
    }

    // Interaction Response
    private MessageDTO sendInteractionResponse(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String interactionId = content.get("interaction_id").asText();
            String interactionToken = content.get("interaction_token").asText();

            ObjectNode response = objectMapper.createObjectNode();
            response.put("type", content.get("type").asInt()); // Response type

            if(content.has("data")) {
                response.set("data", content.get("data"));
            }

            String endpoint = String.format("/interactions/%s/%s/callback",
                interactionId, interactionToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Interaction responses don't need auth

            HttpEntity<String> entity = new HttpEntity<>(response.toString(), headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                apiBaseUrl + endpoint, HttpMethod.POST, entity, String.class
           );

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("status", resp.getStatusCodeValue()));
        } catch(Exception e) {
            log.error("Error sending interaction response", e);
            throw new AdapterException("Failed to send interaction response", e);
        }
    }

    // Application Commands
    private MessageDTO createApplicationCommand(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());

            ObjectNode commandData = objectMapper.createObjectNode();
            commandData.put("name", content.get("name").asText());
            commandData.put("description", content.get("description").asText());

            if(content.has("options")) {
                commandData.set("options", content.get("options"));
            }
            if(content.has("default_permission")) {
                commandData.put("default_permission", content.get("default_permission").asBoolean());
            }
            if(content.has("type")) {
                commandData.put("type", content.get("type").asInt());
            }

            String endpoint;
            if(content.has("guild_id")) {
                // Guild command
                endpoint = String.format("/applications/%s/guilds/%s/commands",
                    config.getApplicationId(), content.get("guild_id").asText());
            } else {
                // Global command
                endpoint = String.format("/applications/%s/commands", config.getApplicationId());
            }

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, commandData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating application command", e);
            throw new AdapterException("Failed to create application command", e);
        }
    }

    // Direct MessageDTO Operations
    private MessageDTO sendDirectMessage(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String userId = content.get("user_id").asText();

            // First, create or get DM channel
            ObjectNode dmRequest = objectMapper.createObjectNode();
            dmRequest.put("recipient_id", userId);

            String endpoint = "/users/@me/channels";
            JsonNode dmChannel = makeAuthenticatedRequest(endpoint, HttpMethod.POST, dmRequest);

            // Then send the message
            String channelId = dmChannel.get("id").asText();
            ((ObjectNode) content).put("channel_id", channelId);

            return sendMessage(message);
        } catch(Exception e) {
            log.error("Error sending direct message", e);
            throw new AdapterException("Failed to send direct message", e);
        }
    }

    // Helper methods for other operations
    private MessageDTO modifyChannel(MessageDTO message) throws AdapterException {
        return genericModify(message, "channel_id", "/channels/%s");
    }

    private MessageDTO deleteChannel(MessageDTO message) throws AdapterException {
        return genericDelete(message, "channel_id", "/channels/%s");
    }

    private MessageDTO createInvite(MessageDTO message) throws AdapterException {
        return genericCreate(message, "channel_id", "/channels/%s/invites");
    }

    private MessageDTO modifyPermissions(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();
            String overwriteId = content.get("overwrite_id").asText();

            ObjectNode permissions = objectMapper.createObjectNode();
            permissions.put("allow", content.get("allow").asText());
            permissions.put("deny", content.get("deny").asText());
            permissions.put("type", content.get("type").asInt());

            String endpoint = String.format("/channels/%s/permissions/%s", channelId, overwriteId);
            makeAuthenticatedRequest(endpoint, HttpMethod.PUT, permissions);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true));
        } catch(Exception e) {
            log.error("Error modifying permissions", e);
            throw new AdapterException("Failed to modify permissions", e);
        }
    }

    private MessageDTO modifyGuild(MessageDTO message) throws AdapterException {
        return genericModify(message, "guild_id", "/guilds/%s");
    }

    private MessageDTO modifyRole(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String roleId = content.get("role_id").asText();

            String endpoint = String.format("/guilds/%s/roles/%s", guildId, roleId);
            return genericModifyWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error modifying role", e);
            throw new AdapterException("Failed to modify role", e);
        }
    }

    private MessageDTO deleteRole(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String roleId = content.get("role_id").asText();

            String endpoint = String.format("/guilds/%s/roles/%s", guildId, roleId);
            return genericDeleteWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error deleting role", e);
            throw new AdapterException("Failed to delete role", e);
        }
    }

    private MessageDTO kickMember(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String userId = content.get("user_id").asText();

            String endpoint = String.format("/guilds/%s/members/%s", guildId, userId);
            return genericDeleteWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error kicking member", e);
            throw new AdapterException("Failed to kick member", e);
        }
    }

    private MessageDTO unbanMember(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String userId = content.get("user_id").asText();

            String endpoint = String.format("/guilds/%s/bans/%s", guildId, userId);
            return genericDeleteWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error unbanning member", e);
            throw new AdapterException("Failed to unban member", e);
        }
    }

    // Voice operations helpers
    private MessageDTO moveMember(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            ((ObjectNode) content).put("channel_id", content.get("voice_channel_id").asText());
            return modifyMember(message);
        } catch(Exception e) {
            log.error("Error moving member", e);
            throw new AdapterException("Failed to move member", e);
        }
    }

    // Additional operations
    private MessageDTO createThread(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();

            ObjectNode threadData = objectMapper.createObjectNode();
            threadData.put("name", content.get("name").asText());
            threadData.put("auto_archive_duration", content.has("auto_archive_duration") ?
                content.get("auto_archive_duration").asInt() : 60);

            String endpoint;
            if(content.has("message_id")) {
                // Thread from message
                endpoint = String.format("/channels/%s/messages/%s/threads",
                    channelId, content.get("message_id").asText());
            } else {
                // Thread without message
                endpoint = String.format("/channels/%s/threads", channelId);
                threadData.put("type", content.has("type") ? content.get("type").asInt() : 11);
            }

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, threadData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating thread", e);
            throw new AdapterException("Failed to create thread", e);
        }
    }

    private MessageDTO pinMessage(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();
            String messageId = content.get("message_id").asText();

            String endpoint = String.format("/channels/%s/pins/%s", channelId, messageId);
            makeAuthenticatedRequest(endpoint, HttpMethod.PUT, null);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("message_id", messageId));
        } catch(Exception e) {
            log.error("Error pinning message", e);
            throw new AdapterException("Failed to pin message", e);
        }
    }

    private MessageDTO addReaction(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();
            String messageId = content.get("message_id").asText();
            String emoji = content.get("emoji").asText();

            String endpoint = String.format("/channels/%s/messages/%s/reactions/%s/@me",
                channelId, messageId, emoji);
            makeAuthenticatedRequest(endpoint, HttpMethod.PUT, null);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("emoji", emoji));
        } catch(Exception e) {
            log.error("Error adding reaction", e);
            throw new AdapterException("Failed to add reaction", e);
        }
    }

    private MessageDTO removeReaction(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String channelId = content.get("channel_id").asText();
            String messageId = content.get("message_id").asText();
            String emoji = content.get("emoji").asText();
            String userId = content.has("user_id") ? content.get("user_id").asText() : "@me";

            String endpoint = String.format("/channels/%s/messages/%s/reactions/%s/%s",
                channelId, messageId, emoji, userId);
            makeAuthenticatedRequest(endpoint, HttpMethod.DELETE, null);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true)
                .put("emoji", emoji));
        } catch(Exception e) {
            log.error("Error removing reaction", e);
            throw new AdapterException("Failed to remove reaction", e);
        }
    }

    private MessageDTO modifyWebhook(MessageDTO message) throws AdapterException {
        return genericModify(message, "webhook_id", "/webhooks/%s");
    }

    private MessageDTO deleteWebhook(MessageDTO message) throws AdapterException {
        return genericDelete(message, "webhook_id", "/webhooks/%s");
    }

    private MessageDTO modifyScheduledEvent(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String eventId = content.get("event_id").asText();

            String endpoint = String.format("/guilds/%s/scheduled - events/%s", guildId, eventId);
            return genericModifyWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error modifying scheduled event", e);
            throw new AdapterException("Failed to modify scheduled event", e);
        }
    }

    private MessageDTO deleteScheduledEvent(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String guildId = content.get("guild_id").asText();
            String eventId = content.get("event_id").asText();

            String endpoint = String.format("/guilds/%s/scheduled - events/%s", guildId, eventId);
            return genericDeleteWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error deleting scheduled event", e);
            throw new AdapterException("Failed to delete scheduled event", e);
        }
    }

    private MessageDTO editApplicationCommand(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String commandId = content.get("command_id").asText();

            String endpoint;
            if(content.has("guild_id")) {
                endpoint = String.format("/applications/%s/guilds/%s/commands/%s",
                    config.getApplicationId(), content.get("guild_id").asText(), commandId);
            } else {
                endpoint = String.format("/applications/%s/commands/%s",
                    config.getApplicationId(), commandId);
            }

            return genericModifyWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error editing application command", e);
            throw new AdapterException("Failed to edit application command", e);
        }
    }

    private MessageDTO deleteApplicationCommand(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String commandId = content.get("command_id").asText();

            String endpoint;
            if(content.has("guild_id")) {
                endpoint = String.format("/applications/%s/guilds/%s/commands/%s",
                    config.getApplicationId(), content.get("guild_id").asText(), commandId);
            } else {
                endpoint = String.format("/applications/%s/commands/%s",
                    config.getApplicationId(), commandId);
            }

            return genericDeleteWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error deleting application command", e);
            throw new AdapterException("Failed to delete application command", e);
        }
    }

    // Stage operations
    private MessageDTO createStageInstance(MessageDTO message) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());

            ObjectNode stageData = objectMapper.createObjectNode();
            stageData.put("channel_id", content.get("channel_id").asText());
            stageData.put("topic", content.get("topic").asText());
            stageData.put("privacy_level", content.has("privacy_level") ?
                content.get("privacy_level").asInt() : config.getDefaultPrivacyLevel());

            JsonNode response = makeAuthenticatedRequest("/stage - instances", HttpMethod.POST, stageData);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating stage instance", e);
            throw new AdapterException("Failed to create stage instance", e);
        }
    }

    private MessageDTO modifyStageInstance(MessageDTO message) throws AdapterException {
        return genericModify(message, "channel_id", "/stage - instances/%s");
    }

    private MessageDTO deleteStageInstance(MessageDTO message) throws AdapterException {
        return genericDelete(message, "channel_id", "/stage - instances/%s");
    }

    // Generic helper methods
    private MessageDTO genericModify(MessageDTO message, String idField, String endpointFormat) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String id = content.get(idField).asText();
            String endpoint = String.format(endpointFormat, id);

            return genericModifyWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error in generic modify", e);
            throw new AdapterException("Generic modify failed", e);
        }
    }

    private MessageDTO genericModifyWithEndpoint(MessageDTO message, String endpoint) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());

            ObjectNode updates = objectMapper.createObjectNode();
            content.fieldNames().forEachRemaining(field -> {
                if(!field.endsWith("_id") && !field.equals("operation")) {
                    updates.set(field, content.get(field));
                }
            });

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.PATCH, updates);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error in generic modify", e);
            throw new AdapterException("Generic modify failed", e);
        }
    }

    private MessageDTO genericDelete(MessageDTO message, String idField, String endpointFormat) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String id = content.get(idField).asText();
            String endpoint = String.format(endpointFormat, id);

            return genericDeleteWithEndpoint(message, endpoint);
        } catch(Exception e) {
            log.error("Error in generic delete", e);
            throw new AdapterException("Generic delete failed", e);
        }
    }

    private MessageDTO genericDeleteWithEndpoint(MessageDTO message, String endpoint) throws AdapterException {
        try {
            makeAuthenticatedRequest(endpoint, HttpMethod.DELETE, null);

            return createSuccessResponse(message, objectMapper.createObjectNode()
                .put("success", true));
        } catch(Exception e) {
            log.error("Error in generic delete", e);
            throw new AdapterException("Generic delete failed", e);
        }
    }

    private MessageDTO genericCreate(MessageDTO message, String idField, String endpointFormat) throws AdapterException {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String id = content.get(idField).asText();
            String endpoint = String.format(endpointFormat, id);

            ObjectNode data = objectMapper.createObjectNode();
            content.fieldNames().forEachRemaining(field -> {
                if(!field.endsWith("_id") && !field.equals("operation")) {
                    data.set(field, content.get(field));
                }
            });

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.POST, data);

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error in generic create", e);
            throw new AdapterException("Generic create failed", e);
        }
    }

    // Helper method for authenticated requests
    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method, Object body) throws AdapterException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bot " + config.getBotToken());
            headers.set("Content - Type", "application/json");
            headers.set("User - Agent", config.getUserAgent());

            HttpEntity<?> entity = body != null ?
                new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiBaseUrl + endpoint, method, entity, String.class
           );

            if(response.getBody() != null && !response.getBody().isEmpty()) {
                return objectMapper.readTree(response.getBody());
            }
            return objectMapper.createObjectNode();
        } catch(HttpClientErrorException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AdapterException("Discord API request failed", e);
        } catch(JsonProcessingException e) {
            log.error("Error parsing JSON response", e);
            throw new AdapterException("Failed to parse Discord API response", e);
        }
    }

    private MessageDTO createSuccessResponse(MessageDTO originalMessage, JsonNode responseData) throws AdapterException {
        try {
            MessageDTO response = new MessageDTO();
            response.setId(UUID.randomUUID().toString());
            response.setType("discord_response");
            response.setPayload(objectMapper.writeValueAsString(responseData));
            response.setTimestamp(LocalDateTime.now());
            response.setStatus(MessageStatus.SUCCESS);

            // Copy headers from original message
            if(originalMessage != null) {
                response.setHeaders(new HashMap<>(originalMessage.getHeaders()));
            } else {
                response.setHeaders(new HashMap<>());
            }

            // Add Discord - specific metadata
            response.getHeaders().put("discord_api_version", config.getApiVersion());
            response.getHeaders().put("response_time", String.valueOf(System.currentTimeMillis()));

            return response;
        } catch(Exception e) {
            throw new AdapterException("Failed to create success response", e);
        }
    }
}
