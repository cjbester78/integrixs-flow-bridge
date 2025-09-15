package com.integrixs.adapters.social.telegram;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.telegram.TelegramBotApiConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
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
import java.util.*;

@Component
public class TelegramBotOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TelegramBotOutboundAdapter.class);


    private static final String API_URL_FORMAT = "%s/bot%s/%s";

    @Autowired
    private TelegramBotApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AdapterType getType() {
        return AdapterType.REST;
    }

    @Override
    public String getName() {
        return "Telegram Bot API Outbound Adapter";
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            String operation = message.getHeaders().get("operation");
            if(operation == null) {
                throw new AdapterException("Operation header is required");
            }

            log.info("Processing Telegram operation: {}", operation);

            switch(operation.toUpperCase()) {
                // Message operations
                case "SEND_MESSAGE":
                    return sendMessage(message);
                case "SEND_PHOTO":
                    return sendPhoto(message);
                case "SEND_AUDIO":
                    return sendAudio(message);
                case "SEND_DOCUMENT":
                    return sendDocument(message);
                case "SEND_VIDEO":
                    return sendVideo(message);
                case "SEND_ANIMATION":
                    return sendAnimation(message);
                case "SEND_VOICE":
                    return sendVoice(message);
                case "SEND_VIDEO_NOTE":
                    return sendVideoNote(message);
                case "SEND_MEDIA_GROUP":
                    return sendMediaGroup(message);
                case "SEND_LOCATION":
                    return sendLocation(message);
                case "SEND_VENUE":
                    return sendVenue(message);
                case "SEND_CONTACT":
                    return sendContact(message);
                case "SEND_POLL":
                    return sendPoll(message);
                case "SEND_DICE":
                    return sendDice(message);
                case "SEND_STICKER":
                    return sendSticker(message);

                // Chat actions
                case "SEND_CHAT_ACTION":
                    return sendChatAction(message);

                // Message editing
                case "EDIT_MESSAGE_TEXT":
                    return editMessageText(message);
                case "EDIT_MESSAGE_CAPTION":
                    return editMessageCaption(message);
                case "EDIT_MESSAGE_MEDIA":
                    return editMessageMedia(message);
                case "EDIT_MESSAGE_REPLY_MARKUP":
                    return editMessageReplyMarkup(message);

                // Message deletion
                case "DELETE_MESSAGE":
                    return deleteMessage(message);
                case "DELETE_MESSAGES":
                    return deleteMessages(message);

                // Chat management
                case "GET_CHAT":
                    return getChat(message);
                case "GET_CHAT_ADMINISTRATORS":
                    return getChatAdministrators(message);
                case "GET_CHAT_MEMBER_COUNT":
                    return getChatMemberCount(message);
                case "GET_CHAT_MEMBER":
                    return getChatMember(message);
                case "SET_CHAT_PERMISSIONS":
                    return setChatPermissions(message);
                case "SET_CHAT_TITLE":
                    return setChatTitle(message);
                case "SET_CHAT_DESCRIPTION":
                    return setChatDescription(message);
                case "PIN_CHAT_MESSAGE":
                    return pinChatMessage(message);
                case "UNPIN_CHAT_MESSAGE":
                    return unpinChatMessage(message);

                // Callback query
                case "ANSWER_CALLBACK_QUERY":
                    return answerCallbackQuery(message);

                // Inline mode
                case "ANSWER_INLINE_QUERY":
                    return answerInlineQuery(message);

                // User info
                case "GET_USER_PROFILE_PHOTOS":
                    return getUserProfilePhotos(message);

                // File operations
                case "GET_FILE":
                    return getFile(message);

                // Commands
                case "SET_MY_COMMANDS":
                    return setMyCommands(message);
                case "DELETE_MY_COMMANDS":
                    return deleteMyCommands(message);

                // Webhook management
                case "SET_WEBHOOK":
                    return setWebhook(message);
                case "DELETE_WEBHOOK":
                    return deleteWebhook(message);

                default:
                    throw new AdapterException("Unknown operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error processing Telegram operation", e);
            throw new AdapterException("Failed to process operation", e);
        }
    }

    // MessageDTO Operations
    private MessageDTO sendMessage(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            Map<String, Object> params = new HashMap<>();

            params.put("chat_id", content.get("chat_id").asText());
            params.put("text", content.get("text").asText());

            // Optional parameters
            if(content.has("parse_mode")) {
                params.put("parse_mode", content.get("parse_mode").asText());
            }
            if(content.has("disable_web_page_preview")) {
                params.put("disable_web_page_preview", content.get("disable_web_page_preview").asBoolean());
            }
            if(content.has("disable_notification")) {
                params.put("disable_notification", content.get("disable_notification").asBoolean());
            }
            if(content.has("reply_to_message_id")) {
                params.put("reply_to_message_id", content.get("reply_to_message_id").asLong());
            }
            if(content.has("reply_markup")) {
                params.put("reply_markup", content.get("reply_markup").toString());
            }
            if(content.has("entities")) {
                params.put("entities", content.get("entities"));
            }
            if(content.has("protect_content")) {
                params.put("protect_content", content.get("protect_content").asBoolean());
            }
            if(content.has("message_thread_id")) {
                params.put("message_thread_id", content.get("message_thread_id").asInt());
            }
            if(content.has("business_connection_id")) {
                params.put("business_connection_id", content.get("business_connection_id").asText());
            }

            JsonNode response = makeApiRequest("sendMessage", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending message", e);
            throw new AdapterException("Failed to send message", e);
        }
    }

    private MessageDTO sendPhoto(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            if(content.has("photo_data")) {
                // Photo upload with multipart
                return sendPhotoMultipart(content);
            } else {
                // Photo by file_id or URL
                Map<String, Object> params = new HashMap<>();
                params.put("chat_id", content.get("chat_id").asText());
                params.put("photo", content.get("photo").asText());

                addMediaOptionalParams(params, content);

                JsonNode response = makeApiRequest("sendPhoto", params);
                return createSuccessResponse(message, response);
            }
        } catch(Exception e) {
            log.error("Error sending photo", e);
            throw new AdapterException("Failed to send photo", e);
        }
    }

    private MessageDTO sendPhotoMultipart(JsonNode content) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add photo file
            String base64Data = content.get("photo_data").asText();
            byte[] photoBytes = Base64.getDecoder().decode(base64Data);
            String filename = content.has("filename") ?
                content.get("filename").asText() : "photo.jpg";

            body.add("photo", new ByteArrayResource(photoBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            // Add other parameters
            body.add("chat_id", content.get("chat_id").asText());

            if(content.has("caption")) {
                body.add("caption", content.get("caption").asText());
            }
            if(content.has("parse_mode")) {
                body.add("parse_mode", content.get("parse_mode").asText());
            }
            if(content.has("disable_notification")) {
                body.add("disable_notification", content.get("disable_notification").asText());
            }
            if(content.has("reply_to_message_id")) {
                body.add("reply_to_message_id", content.get("reply_to_message_id").asText());
            }
            if(content.has("reply_markup")) {
                body.add("reply_markup", content.get("reply_markup").toString());
            }

            return sendMultipartRequest("sendPhoto", body);
        } catch(Exception e) {
            log.error("Error sending photo multipart", e);
            throw new AdapterException("Failed to send photo", e);
        }
    }

    private MessageDTO sendVideo(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            if(content.has("video_data")) {
                // Video upload with multipart
                return sendVideoMultipart(content);
            } else {
                // Video by file_id or URL
                Map<String, Object> params = new HashMap<>();
                params.put("chat_id", content.get("chat_id").asText());
                params.put("video", content.get("video").asText());

                addMediaOptionalParams(params, content);

                if(content.has("duration")) {
                    params.put("duration", content.get("duration").asInt());
                }
                if(content.has("width")) {
                    params.put("width", content.get("width").asInt());
                }
                if(content.has("height")) {
                    params.put("height", content.get("height").asInt());
                }
                if(content.has("supports_streaming")) {
                    params.put("supports_streaming", content.get("supports_streaming").asBoolean());
                }

                JsonNode response = makeApiRequest("sendVideo", params);
                return createSuccessResponse(message, response);
            }
        } catch(Exception e) {
            log.error("Error sending video", e);
            throw new AdapterException("Failed to send video", e);
        }
    }

    private MessageDTO sendVideoMultipart(JsonNode content) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add video file
            String base64Data = content.get("video_data").asText();
            byte[] videoBytes = Base64.getDecoder().decode(base64Data);
            String filename = content.has("filename") ?
                content.get("filename").asText() : "video.mp4";

            body.add("video", new ByteArrayResource(videoBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            // Add other parameters
            body.add("chat_id", content.get("chat_id").asText());

            addMediaMultipartParams(body, content);

            if(content.has("duration")) {
                body.add("duration", content.get("duration").asText());
            }
            if(content.has("width")) {
                body.add("width", content.get("width").asText());
            }
            if(content.has("height")) {
                body.add("height", content.get("height").asText());
            }

            // Add thumbnail if provided
            if(content.has("thumbnail_data")) {
                String thumbBase64 = content.get("thumbnail_data").asText();
                byte[] thumbBytes = Base64.getDecoder().decode(thumbBase64);

                body.add("thumbnail", new ByteArrayResource(thumbBytes) {
                    @Override
                    public String getFilename() {
                        return "thumb.jpg";
                    }
                });
            }

            return sendMultipartRequest("sendVideo", body);
        } catch(Exception e) {
            log.error("Error sending video multipart", e);
            throw new AdapterException("Failed to send video", e);
        }
    }

    private MessageDTO sendDocument(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            if(content.has("document_data")) {
                // Document upload with multipart
                return sendDocumentMultipart(content);
            } else {
                // Document by file_id or URL
                Map<String, Object> params = new HashMap<>();
                params.put("chat_id", content.get("chat_id").asText());
                params.put("document", content.get("document").asText());

                addMediaOptionalParams(params, content);

                if(content.has("disable_content_type_detection")) {
                    params.put("disable_content_type_detection",
                        content.get("disable_content_type_detection").asBoolean());
                }

                JsonNode response = makeApiRequest("sendDocument", params);
                return createSuccessResponse(message, response);
            }
        } catch(Exception e) {
            log.error("Error sending document", e);
            throw new AdapterException("Failed to send document", e);
        }
    }

    private MessageDTO sendDocumentMultipart(JsonNode content) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Add document file
            String base64Data = content.get("document_data").asText();
            byte[] docBytes = Base64.getDecoder().decode(base64Data);
            String filename = content.get("filename").asText();

            body.add("document", new ByteArrayResource(docBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            // Add other parameters
            body.add("chat_id", content.get("chat_id").asText());

            addMediaMultipartParams(body, content);

            return sendMultipartRequest("sendDocument", body);
        } catch(Exception e) {
            log.error("Error sending document multipart", e);
            throw new AdapterException("Failed to send document", e);
        }
    }

    private MessageDTO sendAudio(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("audio", content.get("audio").asText());

            addMediaOptionalParams(params, content);

            if(content.has("duration")) {
                params.put("duration", content.get("duration").asInt());
            }
            if(content.has("performer")) {
                params.put("performer", content.get("performer").asText());
            }
            if(content.has("title")) {
                params.put("title", content.get("title").asText());
            }

            JsonNode response = makeApiRequest("sendAudio", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending audio", e);
            throw new AdapterException("Failed to send audio", e);
        }
    }

    private MessageDTO sendSticker(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("sticker", content.get("sticker").asText());

            if(content.has("emoji")) {
                params.put("emoji", content.get("emoji").asText());
            }
            if(content.has("disable_notification")) {
                params.put("disable_notification", content.get("disable_notification").asBoolean());
            }
            if(content.has("reply_to_message_id")) {
                params.put("reply_to_message_id", content.get("reply_to_message_id").asLong());
            }
            if(content.has("reply_markup")) {
                params.put("reply_markup", content.get("reply_markup").toString());
            }

            JsonNode response = makeApiRequest("sendSticker", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending sticker", e);
            throw new AdapterException("Failed to send sticker", e);
        }
    }

    private MessageDTO sendMediaGroup(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("media", content.get("media").toString());

            if(content.has("disable_notification")) {
                params.put("disable_notification", content.get("disable_notification").asBoolean());
            }
            if(content.has("reply_to_message_id")) {
                params.put("reply_to_message_id", content.get("reply_to_message_id").asLong());
            }

            JsonNode response = makeApiRequest("sendMediaGroup", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending media group", e);
            throw new AdapterException("Failed to send media group", e);
        }
    }

    // Location Operations
    private MessageDTO sendLocation(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("latitude", content.get("latitude").asDouble());
            params.put("longitude", content.get("longitude").asDouble());

            if(content.has("live_period")) {
                params.put("live_period", content.get("live_period").asInt());
            }
            if(content.has("heading")) {
                params.put("heading", content.get("heading").asInt());
            }
            if(content.has("proximity_alert_radius")) {
                params.put("proximity_alert_radius", content.get("proximity_alert_radius").asInt());
            }

            addOptionalMessageParams(params, content);

            JsonNode response = makeApiRequest("sendLocation", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending location", e);
            throw new AdapterException("Failed to send location", e);
        }
    }

    // Poll Operations
    private MessageDTO sendPoll(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("question", content.get("question").asText());

            // Convert options array
            ArrayNode optionsArray = (ArrayNode) content.get("options");
            List<String> options = new ArrayList<>();
            optionsArray.forEach(option -> options.add(option.asText()));
            params.put("options", options);

            if(content.has("is_anonymous")) {
                params.put("is_anonymous", content.get("is_anonymous").asBoolean());
            }
            if(content.has("type")) {
                params.put("type", content.get("type").asText());
            }
            if(content.has("allows_multiple_answers")) {
                params.put("allows_multiple_answers", content.get("allows_multiple_answers").asBoolean());
            }
            if(content.has("correct_option_id")) {
                params.put("correct_option_id", content.get("correct_option_id").asInt());
            }
            if(content.has("explanation")) {
                params.put("explanation", content.get("explanation").asText());
            }
            if(content.has("open_period")) {
                params.put("open_period", content.get("open_period").asInt());
            }
            if(content.has("close_date")) {
                params.put("close_date", content.get("close_date").asLong());
            }

            addOptionalMessageParams(params, content);

            JsonNode response = makeApiRequest("sendPoll", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending poll", e);
            throw new AdapterException("Failed to send poll", e);
        }
    }

    // MessageDTO Editing
    private MessageDTO editMessageText(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();

            if(content.has("inline_message_id")) {
                params.put("inline_message_id", content.get("inline_message_id").asText());
            } else {
                params.put("chat_id", content.get("chat_id").asText());
                params.put("message_id", content.get("message_id").asLong());
            }

            params.put("text", content.get("text").asText());

            if(content.has("parse_mode")) {
                params.put("parse_mode", content.get("parse_mode").asText());
            }
            if(content.has("disable_web_page_preview")) {
                params.put("disable_web_page_preview", content.get("disable_web_page_preview").asBoolean());
            }
            if(content.has("reply_markup")) {
                params.put("reply_markup", content.get("reply_markup").toString());
            }

            JsonNode response = makeApiRequest("editMessageText", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error editing message text", e);
            throw new AdapterException("Failed to edit message text", e);
        }
    }

    private MessageDTO deleteMessage(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("message_id", content.get("message_id").asLong());

            JsonNode response = makeApiRequest("deleteMessage", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error deleting message", e);
            throw new AdapterException("Failed to delete message", e);
        }
    }

    private MessageDTO deleteMessages(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("message_ids", content.get("message_ids"));

            JsonNode response = makeApiRequest("deleteMessages", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error deleting messages", e);
            throw new AdapterException("Failed to delete messages", e);
        }
    }

    // Inline Query Response
    private MessageDTO answerInlineQuery(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("inline_query_id", content.get("inline_query_id").asText());
            params.put("results", content.get("results").toString());

            if(content.has("cache_time")) {
                params.put("cache_time", content.get("cache_time").asInt());
            }
            if(content.has("is_personal")) {
                params.put("is_personal", content.get("is_personal").asBoolean());
            }
            if(content.has("next_offset")) {
                params.put("next_offset", content.get("next_offset").asText());
            }
            if(content.has("button")) {
                params.put("button", content.get("button").toString());
            }

            JsonNode response = makeApiRequest("answerInlineQuery", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error answering inline query", e);
            throw new AdapterException("Failed to answer inline query", e);
        }
    }

    // Callback Query Response
    private MessageDTO answerCallbackQuery(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("callback_query_id", content.get("callback_query_id").asText());

            if(content.has("text")) {
                params.put("text", content.get("text").asText());
            }
            if(content.has("show_alert")) {
                params.put("show_alert", content.get("show_alert").asBoolean());
            }
            if(content.has("url")) {
                params.put("url", content.get("url").asText());
            }
            if(content.has("cache_time")) {
                params.put("cache_time", content.get("cache_time").asInt());
            }

            JsonNode response = makeApiRequest("answerCallbackQuery", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error answering callback query", e);
            throw new AdapterException("Failed to answer callback query", e);
        }
    }

    // Chat Management
    private MessageDTO banChatMember(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("user_id", content.get("user_id").asLong());

            if(content.has("until_date")) {
                params.put("until_date", content.get("until_date").asLong());
            }
            if(content.has("revoke_messages")) {
                params.put("revoke_messages", content.get("revoke_messages").asBoolean());
            }

            JsonNode response = makeApiRequest("banChatMember", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error banning chat member", e);
            throw new AdapterException("Failed to ban chat member", e);
        }
    }

    private MessageDTO restrictChatMember(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("user_id", content.get("user_id").asLong());
            params.put("permissions", content.get("permissions").toString());

            if(content.has("use_independent_chat_permissions")) {
                params.put("use_independent_chat_permissions",
                    content.get("use_independent_chat_permissions").asBoolean());
            }
            if(content.has("until_date")) {
                params.put("until_date", content.get("until_date").asLong());
            }

            JsonNode response = makeApiRequest("restrictChatMember", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error restricting chat member", e);
            throw new AdapterException("Failed to restrict chat member", e);
        }
    }

    private MessageDTO promoteChatMember(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("user_id", content.get("user_id").asLong());

            // Admin rights
            if(content.has("is_anonymous")) {
                params.put("is_anonymous", content.get("is_anonymous").asBoolean());
            }
            if(content.has("can_manage_chat")) {
                params.put("can_manage_chat", content.get("can_manage_chat").asBoolean());
            }
            if(content.has("can_post_messages")) {
                params.put("can_post_messages", content.get("can_post_messages").asBoolean());
            }
            if(content.has("can_edit_messages")) {
                params.put("can_edit_messages", content.get("can_edit_messages").asBoolean());
            }
            if(content.has("can_delete_messages")) {
                params.put("can_delete_messages", content.get("can_delete_messages").asBoolean());
            }
            if(content.has("can_manage_video_chats")) {
                params.put("can_manage_video_chats", content.get("can_manage_video_chats").asBoolean());
            }
            if(content.has("can_restrict_members")) {
                params.put("can_restrict_members", content.get("can_restrict_members").asBoolean());
            }
            if(content.has("can_promote_members")) {
                params.put("can_promote_members", content.get("can_promote_members").asBoolean());
            }
            if(content.has("can_change_info")) {
                params.put("can_change_info", content.get("can_change_info").asBoolean());
            }
            if(content.has("can_invite_users")) {
                params.put("can_invite_users", content.get("can_invite_users").asBoolean());
            }
            if(content.has("can_pin_messages")) {
                params.put("can_pin_messages", content.get("can_pin_messages").asBoolean());
            }
            if(content.has("can_manage_topics")) {
                params.put("can_manage_topics", content.get("can_manage_topics").asBoolean());
            }

            JsonNode response = makeApiRequest("promoteChatMember", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error promoting chat member", e);
            throw new AdapterException("Failed to promote chat member", e);
        }
    }

    // Bot Commands
    private MessageDTO setMyCommands(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("commands", content.get("commands").toString());

            if(content.has("scope")) {
                params.put("scope", content.get("scope").toString());
            }
            if(content.has("language_code")) {
                params.put("language_code", content.get("language_code").asText());
            }

            JsonNode response = makeApiRequest("setMyCommands", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error setting bot commands", e);
            throw new AdapterException("Failed to set bot commands", e);
        }
    }

    // Chat Action
    private MessageDTO sendChatAction(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", content.get("chat_id").asText());
            params.put("action", content.get("action").asText());

            if(content.has("business_connection_id")) {
                params.put("business_connection_id", content.get("business_connection_id").asText());
            }
            if(content.has("message_thread_id")) {
                params.put("message_thread_id", content.get("message_thread_id").asInt());
            }

            JsonNode response = makeApiRequest("sendChatAction", params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error sending chat action", e);
            throw new AdapterException("Failed to send chat action", e);
        }
    }

    // Helper methods for additional operations
    private MessageDTO forwardMessage(MessageDTO message) {
        return genericForwardOperation(message, "forwardMessage");
    }

    private MessageDTO copyMessage(MessageDTO message) {
        return genericForwardOperation(message, "copyMessage");
    }

    private MessageDTO editMessageCaption(MessageDTO message) {
        return genericEditOperation(message, "editMessageCaption");
    }

    private MessageDTO editMessageMedia(MessageDTO message) {
        return genericEditOperation(message, "editMessageMedia");
    }

    private MessageDTO editMessageReplyMarkup(MessageDTO message) {
        return genericEditOperation(message, "editMessageReplyMarkup");
    }

    private MessageDTO sendAnimation(MessageDTO message) {
        return genericMediaOperation(message, "sendAnimation", "animation");
    }

    private MessageDTO sendVoice(MessageDTO message) {
        return genericMediaOperation(message, "sendVoice", "voice");
    }

    private MessageDTO sendVideoNote(MessageDTO message) {
        return genericMediaOperation(message, "sendVideoNote", "video_note");
    }

    private MessageDTO sendVenue(MessageDTO message) {
        return genericLocationOperation(message, "sendVenue");
    }

    private MessageDTO sendContact(MessageDTO message) {
        return genericContactOperation(message, "sendContact");
    }

    private MessageDTO stopPoll(MessageDTO message) {
        return genericStopOperation(message, "stopPoll");
    }

    private MessageDTO getChat(MessageDTO message) {
        return genericGetOperation(message, "getChat");
    }

    private MessageDTO getChatAdministrators(MessageDTO message) {
        return genericGetOperation(message, "getChatAdministrators");
    }

    private MessageDTO getChatMemberCount(MessageDTO message) {
        return genericGetOperation(message, "getChatMemberCount");
    }

    private MessageDTO getChatMember(MessageDTO message) {
        return genericGetMemberOperation(message, "getChatMember");
    }

    private MessageDTO setChatTitle(MessageDTO message) {
        return genericChatSetOperation(message, "setChatTitle", "title");
    }

    private MessageDTO setChatDescription(MessageDTO message) {
        return genericChatSetOperation(message, "setChatDescription", "description");
    }

    private MessageDTO pinChatMessage(MessageDTO message) {
        return genericChatMessageOperation(message, "pinChatMessage");
    }

    private MessageDTO unpinChatMessage(MessageDTO message) {
        return genericChatMessageOperation(message, "unpinChatMessage");
    }

    private MessageDTO leaveChat(MessageDTO message) {
        return genericChatOperation(message, "leaveChat");
    }

    private MessageDTO setChatPhoto(MessageDTO message) {
        return genericChatPhotoOperation(message, "setChatPhoto");
    }

    private MessageDTO deleteChatPhoto(MessageDTO message) {
        return genericChatOperation(message, "deleteChatPhoto");
    }

    private MessageDTO unbanChatMember(MessageDTO message) {
        return genericMemberOperation(message, "unbanChatMember");
    }

    private MessageDTO setChatAdministratorCustomTitle(MessageDTO message) {
        return genericAdminTitleOperation(message, "setChatAdministratorCustomTitle");
    }

    private MessageDTO deleteMyCommands(MessageDTO message) {
        return genericCommandOperation(message, "deleteMyCommands");
    }

    private MessageDTO getMyCommands(MessageDTO message) {
        return genericCommandOperation(message, "getMyCommands");
    }

    private MessageDTO answerWebAppQuery(MessageDTO message) {
        return genericAnswerOperation(message, "answerWebAppQuery");
    }

    private MessageDTO sendGame(MessageDTO message) {
        return genericGameOperation(message, "sendGame");
    }

    private MessageDTO setGameScore(MessageDTO message) {
        return genericGameScoreOperation(message, "setGameScore");
    }

    private MessageDTO getGameHighScores(MessageDTO message) {
        return genericGameScoreOperation(message, "getGameHighScores");
    }

    private MessageDTO sendInvoice(MessageDTO message) {
        return genericInvoiceOperation(message, "sendInvoice");
    }

    private MessageDTO answerShippingQuery(MessageDTO message) {
        return genericPaymentAnswerOperation(message, "answerShippingQuery");
    }

    private MessageDTO answerPreCheckoutQuery(MessageDTO message) {
        return genericPaymentAnswerOperation(message, "answerPreCheckoutQuery");
    }

    private MessageDTO createStickerSet(MessageDTO message) {
        return genericStickerOperation(message, "createNewStickerSet");
    }

    private MessageDTO addStickerToSet(MessageDTO message) {
        return genericStickerOperation(message, "addStickerToSet");
    }

    private MessageDTO setStickerPositionInSet(MessageDTO message) {
        return genericStickerOperation(message, "setStickerPositionInSet");
    }

    private MessageDTO deleteStickerFromSet(MessageDTO message) {
        return genericStickerOperation(message, "deleteStickerFromSet");
    }

    private MessageDTO createForumTopic(MessageDTO message) {
        return genericForumOperation(message, "createForumTopic");
    }

    private MessageDTO editForumTopic(MessageDTO message) {
        return genericForumOperation(message, "editForumTopic");
    }

    private MessageDTO closeForumTopic(MessageDTO message) {
        return genericForumOperation(message, "closeForumTopic");
    }

    private MessageDTO reopenForumTopic(MessageDTO message) {
        return genericForumOperation(message, "reopenForumTopic");
    }

    private MessageDTO deleteForumTopic(MessageDTO message) {
        return genericForumOperation(message, "deleteForumTopic");
    }

    private MessageDTO getFile(MessageDTO message) {
        return genericGetFileOperation(message, "getFile");
    }

    private MessageDTO editMessageTextInline(MessageDTO message) {
        return editMessageText(message);
    }

    private MessageDTO setWebhook(MessageDTO message) {
        return genericWebhookOperation(message, "setWebhook");
    }

    private MessageDTO deleteWebhook(MessageDTO message) {
        return genericWebhookOperation(message, "deleteWebhook");
    }

    private MessageDTO getWebhookInfo(MessageDTO message) {
        return genericWebhookOperation(message, "getWebhookInfo");
    }

    private MessageDTO editMessageLiveLocation(MessageDTO message) {
        return genericLocationEditOperation(message, "editMessageLiveLocation");
    }

    private MessageDTO stopMessageLiveLocation(MessageDTO message) {
        return genericLocationEditOperation(message, "stopMessageLiveLocation");
    }

    // Generic helper methods
    private MessageDTO genericForwardOperation(MessageDTO message, String method) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            Map<String, Object> params = objectMapper.convertValue(content, Map.class);

            JsonNode response = makeApiRequest(method, params);
            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error in {} operation", method, e);
            throw new AdapterException("Failed to execute " + method, e);
        }
    }

    private MessageDTO genericEditOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericMediaOperation(MessageDTO message, String method, String mediaField) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericLocationOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericContactOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericStopOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericGetOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericGetMemberOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericChatSetOperation(MessageDTO message, String method, String field) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericChatMessageOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericChatOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericChatPhotoOperation(MessageDTO message, String method) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("chat_id", content.get("chat_id").asText());

            String base64Data = content.get("photo_data").asText();
            byte[] photoBytes = Base64.getDecoder().decode(base64Data);

            body.add("photo", new ByteArrayResource(photoBytes) {
                @Override
                public String getFilename() {
                    return "photo.jpg";
                }
            });

            return sendMultipartRequest(method, body);
        } catch(Exception e) {
            log.error("Error in {} operation", method, e);
            throw new AdapterException("Failed to execute " + method, e);
        }
    }

    private MessageDTO genericMemberOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericAdminTitleOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericCommandOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericAnswerOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericGameOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericGameScoreOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericInvoiceOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericPaymentAnswerOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericStickerOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericForumOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericGetFileOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericWebhookOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    private MessageDTO genericLocationEditOperation(MessageDTO message, String method) {
        return genericForwardOperation(message, method);
    }

    // Helper methods for parameters
    private void addMediaOptionalParams(Map<String, Object> params, JsonNode content) {
        if(content.has("caption")) {
            params.put("caption", content.get("caption").asText());
        }
        if(content.has("parse_mode")) {
            params.put("parse_mode", content.get("parse_mode").asText());
        }
        if(content.has("caption_entities")) {
            params.put("caption_entities", content.get("caption_entities"));
        }
        if(content.has("has_spoiler")) {
            params.put("has_spoiler", content.get("has_spoiler").asBoolean());
        }
        addOptionalMessageParams(params, content);
    }

    private void addOptionalMessageParams(Map<String, Object> params, JsonNode content) {
        if(content.has("disable_notification")) {
            params.put("disable_notification", content.get("disable_notification").asBoolean());
        }
        if(content.has("protect_content")) {
            params.put("protect_content", content.get("protect_content").asBoolean());
        }
        if(content.has("reply_to_message_id")) {
            params.put("reply_to_message_id", content.get("reply_to_message_id").asLong());
        }
        if(content.has("allow_sending_without_reply")) {
            params.put("allow_sending_without_reply",
                content.get("allow_sending_without_reply").asBoolean());
        }
        if(content.has("reply_markup")) {
            params.put("reply_markup", content.get("reply_markup").toString());
        }
        if(content.has("message_thread_id")) {
            params.put("message_thread_id", content.get("message_thread_id").asInt());
        }
        if(content.has("business_connection_id")) {
            params.put("business_connection_id", content.get("business_connection_id").asText());
        }
    }

    private void addMediaMultipartParams(MultiValueMap<String, Object> body, JsonNode content) {
        if(content.has("caption")) {
            body.add("caption", content.get("caption").asText());
        }
        if(content.has("parse_mode")) {
            body.add("parse_mode", content.get("parse_mode").asText());
        }
        if(content.has("disable_notification")) {
            body.add("disable_notification", content.get("disable_notification").asText());
        }
        if(content.has("reply_to_message_id")) {
            body.add("reply_to_message_id", content.get("reply_to_message_id").asText());
        }
        if(content.has("reply_markup")) {
            body.add("reply_markup", content.get("reply_markup").toString());
        }
    }

    // API request methods
    private JsonNode makeApiRequest(String method, Map<String, Object> params) {
        try {
            String url = String.format(API_URL_FORMAT,
                config.getApiUrl(), config.getBotToken(), method);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = params != null ?
                new HttpEntity<>(params, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
           );

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if(responseJson.has("ok") && !responseJson.get("ok").asBoolean()) {
                String errorDescription = responseJson.has("description") ?
                    responseJson.get("description").asText() : "Unknown error";
                log.error("Telegram API error: {}", errorDescription);
                throw new AdapterException("Telegram API error: " + errorDescription);
            }

            return responseJson;
        } catch(HttpClientErrorException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AdapterException("Telegram API request failed", e);
        }
    }

    private MessageDTO sendMultipartRequest(String method, MultiValueMap<String, Object> body) {
        try {
            String url = String.format(API_URL_FORMAT,
                config.getApiUrl(), config.getBotToken(), method);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, String.class
           );

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if(responseJson.has("ok") && !responseJson.get("ok").asBoolean()) {
                String errorDescription = responseJson.has("description") ?
                    responseJson.get("description").asText() : "Unknown error";
                throw new AdapterException("Telegram API error: " + errorDescription);
            }

            return createSuccessResponse(null, responseJson);
        } catch(HttpClientErrorException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AdapterException("Telegram API request failed", e);
        } catch(Exception e) {
            throw new AdapterException("Failed to send multipart request", e);
        }
    }
}
