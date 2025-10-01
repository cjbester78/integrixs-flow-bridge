package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.UUID;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Outbound adapter for Facebook Messenger Platform integration.
 * Handles message sending, template management, and conversation control.
 */
@Component
@ConditionalOnProperty(name = "integrixs.adapters.facebook.messenger.enabled", havingValue = "true", matchIfMissing = false)
public class FacebookMessengerOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(FacebookMessengerOutboundAdapter.class);


    private final FacebookMessengerApiConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public FacebookMessengerOutboundAdapter(
            FacebookMessengerApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(rateLimiterService, credentialEncryptionService);
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("pageId", config.getPageId());
        configMap.put("apiUrl", config.getApiBaseUrl());
        configMap.put("apiVersion", config.getApiVersion());
        configMap.put("pageAccessToken", config.getPageAccessToken());
        configMap.put("appSecret", config.getAppSecret());
        return configMap;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        Map<String, Object> headers = message.getHeaders();
        String action = headers != null ? (String) headers.get("action") : null;

        try {
            switch(action) {
                case "SEND_TEXT_MESSAGE":
                    return sendTextMessage(message);
                case "SEND_IMAGE":
                    return sendImage(message);
                case "SEND_VIDEO":
                    return sendVideo(message);
                case "SEND_AUDIO":
                    return sendAudio(message);
                case "SEND_FILE":
                    return sendFile(message);
                case "SEND_TEMPLATE":
                    return sendTemplate(message);
                case "SEND_BUTTON_TEMPLATE":
                    return sendButtonTemplate(message);
                case "SEND_GENERIC_TEMPLATE":
                    return sendGenericTemplate(message);
                case "SEND_LIST_TEMPLATE":
                    return sendListTemplate(message);
                case "SEND_RECEIPT_TEMPLATE":
                    return sendReceiptTemplate(message);
                case "SEND_MEDIA_TEMPLATE":
                    return sendMediaTemplate(message);
                case "SEND_CAROUSEL":
                    return sendCarousel(message);
                case "SEND_QUICK_REPLIES":
                    return sendQuickReplies(message);
                case "SEND_LOCATION":
                    return sendLocation(message);
                case "SEND_TYPING_INDICATOR":
                    return sendTypingIndicator(message);
                case "MARK_SEEN":
                    return markSeen(message);
                case "SET_PERSISTENT_MENU":
                    return setPersistentMenu(message);
                case "GET_STARTED":
                    return setGetStarted(message);
                case "SET_GREETING":
                    return setGreeting(message);
                case "SET_ICE_BREAKERS":
                    return setIceBreakers(message);
                case "CREATE_PERSONA":
                    return createPersona(message);
                case "UPDATE_PERSONA":
                    return updatePersona(message);
                case "DELETE_PERSONA":
                    return deletePersona(message);
                case "PASS_THREAD_CONTROL":
                    return passThreadControl(message);
                case "TAKE_THREAD_CONTROL":
                    return takeThreadControl(message);
                case "REQUEST_THREAD_CONTROL":
                    return requestThreadControl(message);
                case "SEND_BROADCAST":
                    return sendBroadcast(message);
                case "CREATE_BROADCAST_MESSAGE":
                    return createBroadcastMessage(message);
                case "SEND_SPONSORED_MESSAGE":
                    return sendSponsoredMessage(message);
                case "SEND_PRIVATE_REPLY":
                    return sendPrivateReply(message);
                case "GET_USER_PROFILE":
                    return getUserProfile(message);
                case "UPLOAD_ATTACHMENT":
                    return uploadAttachment(message);
                default:
                    log.warn("Unknown action received: {}", action);
                    return message;
            }
        } catch(Exception e) {
            return createErrorResponse(message, e.getMessage());
        }
    }

    private MessageDTO sendTextMessage(MessageDTO message) throws Exception {
        Map<String, Object> headers = message.getHeaders();
        String recipientId = headers != null ? (String) headers.get("recipientId") : null;
        String text = message.getPayload();
        String messagingType = headers != null ? (String) headers.getOrDefault("messagingType", "RESPONSE") : "RESPONSE";
        String messageTag = headers != null ? (String) headers.get("messageTag") : null;
        String personaId = headers != null ? (String) headers.get("personaId") : null;

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", messagingType);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", text);

        if(personaId != null) {
            messageData.put("persona_id", personaId);
        }

        request.put("message", messageData);

        if(messageTag != null) {
            request.put("tag", messageTag);
        }

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendImage(MessageDTO message) throws Exception {
        return sendMediaMessage(message, "image");
    }

    private MessageDTO sendVideo(MessageDTO message) throws Exception {
        return sendMediaMessage(message, "video");
    }

    private MessageDTO sendAudio(MessageDTO message) throws Exception {
        return sendMediaMessage(message, "audio");
    }

    private MessageDTO sendFile(MessageDTO message) throws Exception {
        return sendMediaMessage(message, "file");
    }

    private MessageDTO sendMediaMessage(MessageDTO message, String type) throws Exception {
        Map<String, Object> headers = message.getHeaders();
        String recipientId = headers != null ? (String) headers.get("recipientId") : null;
        String url = headers != null ? (String) headers.get("mediaUrl") : null;
        boolean isReusable = Boolean.parseBoolean(headers != null ? (String) headers.getOrDefault("isReusable", "false") : "false");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", headers != null ? (String) headers.getOrDefault("messagingType", "RESPONSE") : "RESPONSE");

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("type", type);
        attachment.put("payload", Map.of(
                "url", url,
                "is_reusable", isReusable
       ));

        request.put("message", Map.of("attachment", attachment));

        String apiUrl = getApiUrl() + "/me/messages";
        String response = executeApiCall(() -> makePostRequest(apiUrl, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendTemplate(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        String templateType = getHeader(message, "templateType");
        Map<String, Object> templateData = getPayloadAsMap(message);

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("type", "template");

        Map<String, Object> payload = new HashMap<>(templateData);
        payload.put("template_type", templateType);
        attachment.put("payload", payload);

        request.put("message", Map.of("attachment", attachment));

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendButtonTemplate(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        String text = getHeader(message, "text");
        List<Map<String, Object>> buttons = (List<Map<String, Object>>) getPayloadAsMap(message).get("buttons");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("template_type", "button");
        payload.put("text", text);
        payload.put("buttons", buttons);

        Map<String, Object> attachment = Map.of(
                "type", "template",
                "payload", payload
       );

        request.put("message", Map.of("attachment", attachment));

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendGenericTemplate(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        List<Map<String, Object>> elements = (List<Map<String, Object>>) getPayloadAsMap(message).get("elements");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("template_type", "generic");
        payload.put("elements", elements);

        Map<String, Object> attachment = Map.of(
                "type", "template",
                "payload", payload
       );

        request.put("message", Map.of("attachment", attachment));

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendListTemplate(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        Map<String, Object> listData = getPayloadAsMap(message);

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("template_type", "list");
        payload.put("top_element_style", listData.get("topElementStyle"));
        payload.put("elements", listData.get("elements"));

        if(listData.containsKey("buttons")) {
            payload.put("buttons", listData.get("buttons"));
        }

        Map<String, Object> attachment = Map.of(
                "type", "template",
                "payload", payload
       );

        request.put("message", Map.of("attachment", attachment));

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendReceiptTemplate(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        Map<String, Object> receiptData = getPayloadAsMap(message);

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> payload = new HashMap<>(receiptData);
        payload.put("template_type", "receipt");

        Map<String, Object> attachment = Map.of(
                "type", "template",
                "payload", payload
       );

        request.put("message", Map.of("attachment", attachment));

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendMediaTemplate(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        String mediaType = getHeader(message, "mediaType");
        String attachmentId = getHeader(message, "attachmentId");
        String mediaUrl = getHeader(message, "mediaUrl");
        List<Map<String, Object>> buttons = (List<Map<String, Object>>) getPayloadAsMap(message).get("buttons");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> element = new HashMap<>();
        element.put("media_type", mediaType);

        if(attachmentId != null) {
            element.put("attachment_id", attachmentId);
        } else if(mediaUrl != null) {
            element.put("url", mediaUrl);
        }

        if(buttons != null) {
            element.put("buttons", buttons);
        }

        Map<String, Object> payload = Map.of(
                "template_type", "media",
                "elements", List.of(element)
       );

        Map<String, Object> attachment = Map.of(
                "type", "template",
                "payload", payload
       );

        request.put("message", Map.of("attachment", attachment));

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendCarousel(MessageDTO message) throws Exception {
        return sendGenericTemplate(message); // Carousel is implemented as generic template with multiple elements
    }

    private MessageDTO sendQuickReplies(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        String text = getHeader(message, "text");
        List<Map<String, Object>> quickReplies = (List<Map<String, Object>>) getPayloadAsMap(message).get("quickReplies");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", text);
        messageData.put("quick_replies", quickReplies);

        request.put("message", messageData);

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendLocation(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        double latitude = Double.parseDouble(getHeader(message, "latitude"));
        double longitude = Double.parseDouble(getHeader(message, "longitude"));

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("messaging_type", getHeader(message, "messagingType", "RESPONSE"));

        Map<String, Object> coordinates = Map.of(
                "lat", latitude,
                "long", longitude
       );

        Map<String, Object> payload = Map.of(
                "type", "location",
                "coordinates", coordinates
       );

        Map<String, Object> attachment = Map.of(
                "type", "location",
                "payload", payload
       );

        request.put("message", Map.of("attachment", attachment));

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendTypingIndicator(MessageDTO message) throws Exception {
        return sendSenderAction(message, "typing_on");
    }

    private MessageDTO markSeen(MessageDTO message) throws Exception {
        return sendSenderAction(message, "mark_seen");
    }

    private MessageDTO sendSenderAction(MessageDTO message, String action) throws Exception {
        String recipientId = getHeader(message, "recipientId");

        Map<String, Object> request = Map.of(
                "recipient", Map.of("id", recipientId),
                "sender_action", action
       );

        String url = config.getMessagesEndpoint();
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO setPersistentMenu(MessageDTO message) throws Exception {
        List<Map<String, Object>> menuItems = (List<Map<String, Object>>) getPayloadAsMap(message).get("menuItems");
        String locale = getHeader(message, "locale", "default");

        Map<String, Object> persistentMenu = Map.of(
                "locale", locale,
                "composer_input_disabled", Boolean.parseBoolean(getHeader(message, "composerInputDisabled", "false")),
                "call_to_actions", menuItems
       );

        Map<String, Object> request = Map.of("persistent_menu", List.of(persistentMenu));

        String url = getApiUrl() + "/me/messenger_profile";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO setGetStarted(MessageDTO message) throws Exception {
        String payload = message.getPayload();

        Map<String, Object> request = Map.of("get_started", Map.of("payload", payload));

        String url = getApiUrl() + "/me/messenger_profile";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO setGreeting(MessageDTO message) throws Exception {
        List<Map<String, Object>> greetings = (List<Map<String, Object>>) getPayloadAsMap(message).get("greetings");

        Map<String, Object> request = Map.of("greeting", greetings);

        String url = getApiUrl() + "/me/messenger_profile";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO setIceBreakers(MessageDTO message) throws Exception {
        List<Map<String, Object>> iceBreakers = (List<Map<String, Object>>) getPayloadAsMap(message).get("iceBreakers");

        Map<String, Object> request = Map.of("ice_breakers", iceBreakers);

        String url = getApiUrl() + "/me/messenger_profile";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO createPersona(MessageDTO message) throws Exception {
        Map<String, Object> personaData = getPayloadAsMap(message);

        String url = getApiUrl() + "/me/personas";
        String response = executeApiCall(() -> makePostRequest(url, personaData));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO updatePersona(MessageDTO message) throws Exception {
        String personaId = getHeader(message, "personaId");
        Map<String, Object> personaData = getPayloadAsMap(message);

        String url = getApiUrl() + "/" + personaId;
        String response = executeApiCall(() -> makePostRequest(url, personaData));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO deletePersona(MessageDTO message) throws Exception {
        String personaId = getHeader(message, "personaId");

        String url = getApiUrl() + "/" + personaId;
        String response = executeApiCall(() -> makeDeleteRequest(url));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO passThreadControl(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        String targetAppId = getHeader(message, "targetAppId");
        String metadata = getHeader(message, "metadata");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));
        request.put("target_app_id", targetAppId);

        if(metadata != null) {
            request.put("metadata", metadata);
        }

        String url = getApiUrl() + "/me/pass_thread_control";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO takeThreadControl(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        String metadata = getHeader(message, "metadata");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));

        if(metadata != null) {
            request.put("metadata", metadata);
        }

        String url = getApiUrl() + "/me/take_thread_control";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO requestThreadControl(MessageDTO message) throws Exception {
        String recipientId = getHeader(message, "recipientId");
        String metadata = getHeader(message, "metadata");

        Map<String, Object> request = new HashMap<>();
        request.put("recipient", Map.of("id", recipientId));

        if(metadata != null) {
            request.put("metadata", metadata);
        }

        String url = getApiUrl() + "/me/request_thread_control";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendBroadcast(MessageDTO message) throws Exception {
        String messageCreativeId = getHeader(message, "messageCreativeId");
        String customLabelId = getHeader(message, "customLabelId");
        String notificationType = getHeader(message, "notificationType", "REGULAR");
        String messagingType = getHeader(message, "messagingType", "MESSAGE_TAG");
        String tag = getHeader(message, "tag", "NON_PROMOTIONAL_SUBSCRIPTION");

        Map<String, Object> request = new HashMap<>();
        request.put("message_creative_id", messageCreativeId);
        request.put("notification_type", notificationType);
        request.put("messaging_type", messagingType);
        request.put("tag", tag);

        if(customLabelId != null) {
            request.put("custom_label_id", customLabelId);
        }

        String url = getApiUrl() + "/me/broadcast_messages";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO createBroadcastMessage(MessageDTO message) throws Exception {
        Map<String, Object> messageData = getPayloadAsMap(message);

        Map<String, Object> request = Map.of("messages", List.of(messageData));

        String url = getApiUrl() + "/me/message_creatives";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendSponsoredMessage(MessageDTO message) throws Exception {
        String adAccountId = getHeader(message, "adAccountId");
        String targetingSpec = getHeader(message, "targetingSpec");
        Map<String, Object> messageData = getPayloadAsMap(message);

        Map<String, Object> request = new HashMap<>();
        request.put("message", messageData);
        request.put("targeting", parseJsonString(targetingSpec));

        String url = getApiUrl() + "/act_" + adAccountId + "/sponsored_message_ads";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO sendPrivateReply(MessageDTO message) throws Exception {
        String commentId = getHeader(message, "commentId");
        String text = message.getPayload();

        Map<String, Object> request = Map.of("message", text);

        String url = getApiUrl() + "/" + commentId + "/private_replies";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO getUserProfile(MessageDTO message) throws Exception {
        String userId = getHeader(message, "userId");
        String fields = getHeader(message, "fields", "first_name,last_name,profile_pic");

        Map<String, String> params = new HashMap<>();
        params.put("fields", fields);
        params.put("access_token", getDecryptedCredential("pageAccessToken"));

        String url = getApiUrl() + "/" + userId;
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private MessageDTO uploadAttachment(MessageDTO message) throws Exception {
        String type = getHeader(message, "attachmentType");
        String url = getHeader(message, "attachmentUrl");
        boolean isReusable = Boolean.parseBoolean(getHeader(message, "isReusable", "true"));

        Map<String, Object> attachment = Map.of(
                "type", type,
                "payload", Map.of(
                        "url", url,
                        "is_reusable", isReusable
               )
       );

        Map<String, Object> request = Map.of("message", Map.of("attachment", attachment));

        String apiUrl = getApiUrl() + "/me/message_attachments";
        String response = executeApiCall(() -> makePostRequest(apiUrl, request));

        return createSuccessResponse(message.getCorrelationId(), response, "FACEBOOK_MESSENGER_OPERATION");
    }

    private String makePostRequest(String url, Map<String, Object> data) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getDecryptedCredential("pageAccessToken"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    private String makeGetRequest(String url, Map<String, String> params) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getDecryptedCredential("pageAccessToken"));

        StringBuilder urlWithParams = new StringBuilder(url);
        if(!params.isEmpty()) {
            urlWithParams.append("?");
            params.forEach((key, value) -> urlWithParams.append(key).append(" = ").append(value).append("&"));
            urlWithParams.deleteCharAt(urlWithParams.length() - 1);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(urlWithParams.toString(), HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    private String makeDeleteRequest(String url) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getDecryptedCredential("pageAccessToken"));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        return response.getBody();
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }


    private String getApiUrl() {
        return config.getMessagesEndpoint().replace("/me/messages", "");
    }

    private Map<String, Object> getPayloadAsMap(MessageDTO message) {
        try {
            String payload = message.getPayload();
            if (payload == null || payload.isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("Error parsing payload as map", e);
            return new HashMap<>();
        }
    }

    private String getHeader(MessageDTO message, String key) {
        Map<String, Object> headers = message.getHeaders();
        return headers != null ? (String) headers.get(key) : null;
    }

    private String getHeader(MessageDTO message, String key, String defaultValue) {
        Map<String, Object> headers = message.getHeaders();
        return headers != null ? (String) headers.getOrDefault(key, defaultValue) : defaultValue;
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapter does not support receiving
        return AdapterResult.success(null, "Outbound adapter does not support receiving");
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        log.debug("Initializing Facebook Messenger receiver");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.debug("Destroying Facebook Messenger receiver");
    }

    @Override
    protected long getPollingIntervalMs() {
        return config.getPollingInterval() != null ? config.getPollingInterval() : 60000L;
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = getApiUrl() + "/me";

            Map<String, String> params = new HashMap<>();
            params.put("access_token", config.getPageAccessToken());
            params.put("fields", "id,name");

            String response = makeGetRequest(url, params);

            if (response != null) {
                return AdapterResult.success(null, "Facebook Messenger API connection successful");
            } else {
                return AdapterResult.failure("Facebook Messenger API connection failed");
            }
        } catch (Exception e) {
            log.error("Error testing Facebook Messenger connection", e);
            return AdapterResult.failure("Failed to test Facebook Messenger connection: " + e.getMessage());
        }
    }
}
