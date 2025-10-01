package com.integrixs.adapters.social.whatsapp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.social.whatsapp.WhatsAppBusinessApiConfig.*;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.io.IOException;

@Component("whatsappBusinessOutboundAdapter")
public class WhatsAppBusinessOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(WhatsAppBusinessOutboundAdapter.class);


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private WhatsAppBusinessApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final OAuth2TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public WhatsAppBusinessOutboundAdapter(
            RateLimiterService rateLimiterService,
            OAuth2TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(rateLimiterService, credentialEncryptionService);
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            validateConfiguration();
        } catch (Exception e) {
            return createErrorResponse(message, "Configuration validation failed: " + e.getMessage());
        }

        String operation = (String) message.getHeaders().get("operation");
        if(operation == null) {
            return createErrorResponse(message, "Operation header is required");
        }

        log.debug("Processing WhatsApp Business operation: {}", operation);

        try {
            return switch(operation.toUpperCase()) {
                case "SEND_TEXT" -> sendTextMessage(message);
                case "SEND_IMAGE" -> sendImageMessage(message);
                case "SEND_VIDEO" -> sendVideoMessage(message);
                case "SEND_AUDIO" -> sendAudioMessage(message);
                case "SEND_DOCUMENT" -> sendDocumentMessage(message);
                case "SEND_LOCATION" -> sendLocationMessage(message);
                case "SEND_CONTACT" -> sendContactsMessage(message);
                case "SEND_TEMPLATE" -> sendTemplateMessage(message);
                case "SEND_INTERACTIVE" -> sendInteractiveMessage(message);
                case "SEND_REACTION" -> sendReactionMessage(message);
                case "MARK_AS_READ" -> markAsRead(message);
                case "CREATE_MESSAGE_TEMPLATE" -> createTemplate(message);
                case "DELETE_MESSAGE_TEMPLATE" -> deleteTemplate(message);
                case "GET_TEMPLATE_STATUS" -> getTemplates(message);
                case "UPDATE_BUSINESS_PROFILE" -> updateBusinessProfile(message);
                case "GET_BUSINESS_PROFILE" -> getBusinessProfile(message);
                case "CREATE_PRODUCT" -> createProduct(message);
                case "UPDATE_PRODUCT" -> updateProduct(message);
                case "DELETE_PRODUCT" -> deleteProduct(message);
                case "GET_PRODUCT" -> getProducts(message);
                case "CREATE_CATALOG" -> getCatalog(message);
                case "SEND_PRODUCT_MESSAGE" -> sendInteractiveMessage(message);
                case "GET_MEDIA_URL" -> getMediaUrl(message);
                case "UPLOAD_MEDIA" -> uploadMedia(message);
                case "DELETE_MEDIA" -> deleteMedia(message);
                case "GET_PHONE_NUMBERS" -> getPhoneNumber(message);
                case "VERIFY_CONTACT" -> verifyPhone(message);
                default -> throw new AdapterException("Unsupported operation: " + operation);
            };
        } catch(Exception e) {
            log.error("Error processing WhatsApp Business operation: {}", operation, e);
            MessageDTO errorResponse = new MessageDTO();
            errorResponse.setCorrelationId(message.getCorrelationId());
            errorResponse.setStatus(MessageStatus.FAILED);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "originalOperation", operation
           ));
            return errorResponse;
        }
    }

    private MessageDTO sendTextMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("recipient_type", "individual");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "text");

        ObjectNode text = objectMapper.createObjectNode();
        text.put("preview_url", payload.path("preview_url").asBoolean(false));
        text.put("body", payload.path("text").asText());
        requestBody.set("text", text);

        // Add context if replying
        if(payload.has("reply_to")) {
            ObjectNode context = objectMapper.createObjectNode();
            context.put("message_id", payload.path("reply_to").asText());
            requestBody.set("context", context);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "TEXT_SENT");
    }

    private MessageDTO sendImageMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("recipient_type", "individual");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "image");

        ObjectNode image = objectMapper.createObjectNode();
        if(payload.has("media_id")) {
            image.put("id", payload.path("media_id").asText());
        } else if(payload.has("link")) {
            image.put("link", payload.path("link").asText());
        }
        if(payload.has("caption")) {
            image.put("caption", payload.path("caption").asText());
        }
        requestBody.set("image", image);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "IMAGE_SENT");
    }

    private MessageDTO sendVideoMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "video");

        ObjectNode video = objectMapper.createObjectNode();
        if(payload.has("media_id")) {
            video.put("id", payload.path("media_id").asText());
        } else if(payload.has("link")) {
            video.put("link", payload.path("link").asText());
        }
        if(payload.has("caption")) {
            video.put("caption", payload.path("caption").asText());
        }
        requestBody.set("video", video);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "VIDEO_SENT");
    }

    private MessageDTO sendAudioMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "audio");

        ObjectNode audio = objectMapper.createObjectNode();
        if(payload.has("media_id")) {
            audio.put("id", payload.path("media_id").asText());
        } else if(payload.has("link")) {
            audio.put("link", payload.path("link").asText());
        }
        requestBody.set("audio", audio);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "AUDIO_SENT");
    }

    private MessageDTO sendDocumentMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "document");

        ObjectNode document = objectMapper.createObjectNode();
        if(payload.has("media_id")) {
            document.put("id", payload.path("media_id").asText());
        } else if(payload.has("link")) {
            document.put("link", payload.path("link").asText());
        }
        if(payload.has("caption")) {
            document.put("caption", payload.path("caption").asText());
        }
        if(payload.has("filename")) {
            document.put("filename", payload.path("filename").asText());
        }
        requestBody.set("document", document);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "DOCUMENT_SENT");
    }

    private MessageDTO sendLocationMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "location");

        ObjectNode location = objectMapper.createObjectNode();
        location.put("longitude", payload.path("longitude").asDouble());
        location.put("latitude", payload.path("latitude").asDouble());
        if(payload.has("name")) {
            location.put("name", payload.path("name").asText());
        }
        if(payload.has("address")) {
            location.put("address", payload.path("address").asText());
        }
        requestBody.set("location", location);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "LOCATION_SENT");
    }

    private MessageDTO sendContactsMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "contacts");

        ArrayNode contacts = objectMapper.createArrayNode();
        JsonNode contactsData = payload.path("contacts");
        if(contactsData.isArray()) {
            contacts = (ArrayNode) contactsData;
        }
        requestBody.set("contacts", contacts);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "CONTACTS_SENT");
    }

    private MessageDTO sendStickerMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "sticker");

        ObjectNode sticker = objectMapper.createObjectNode();
        if(payload.has("media_id")) {
            sticker.put("id", payload.path("media_id").asText());
        } else if(payload.has("link")) {
            sticker.put("link", payload.path("link").asText());
        }
        requestBody.set("sticker", sticker);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "STICKER_SENT");
    }

    private MessageDTO sendTemplateMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "template");

        ObjectNode template = objectMapper.createObjectNode();
        template.put("name", payload.path("template_name").asText());

        ObjectNode language = objectMapper.createObjectNode();
        language.put("code", payload.path("language_code").asText("en"));
        template.set("language", language);

        // Add components(header, body, footer, buttons)
        if(payload.has("components")) {
            template.set("components", payload.get("components"));
        }

        requestBody.set("template", template);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "TEMPLATE_SENT");
    }

    private MessageDTO sendInteractiveMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "interactive");

        ObjectNode interactive = objectMapper.createObjectNode();
        String interactiveType = payload.path("interactive_type").asText();
        interactive.put("type", interactiveType);

        // Add header if present
        if(payload.has("header")) {
            interactive.set("header", payload.get("header"));
        }

        // Add body
        ObjectNode body = objectMapper.createObjectNode();
        body.put("text", payload.path("body_text").asText());
        interactive.set("body", body);

        // Add footer if present
        if(payload.has("footer_text")) {
            ObjectNode footer = objectMapper.createObjectNode();
            footer.put("text", payload.path("footer_text").asText());
            interactive.set("footer", footer);
        }

        // Add action based on type
        ObjectNode action = objectMapper.createObjectNode();
        switch(interactiveType) {
            case "button":
                ArrayNode buttons = objectMapper.createArrayNode();
                JsonNode buttonData = payload.path("buttons");
                if(buttonData.isArray()) {
                    for(JsonNode button : buttonData) {
                        ObjectNode btn = objectMapper.createObjectNode();
                        btn.put("type", "reply");
                        ObjectNode reply = objectMapper.createObjectNode();
                        reply.put("id", button.path("id").asText());
                        reply.put("title", button.path("title").asText());
                        btn.set("reply", reply);
                        buttons.add(btn);
                    }
                }
                action.set("buttons", buttons);
                break;

            case "list":
                action.put("button", payload.path("button_text").asText("Menu"));
                ArrayNode sections = objectMapper.createArrayNode();
                JsonNode sectionData = payload.path("sections");
                if(sectionData.isArray()) {
                    sections = (ArrayNode) sectionData;
                }
                action.set("sections", sections);
                break;

            case "product":
                action.put("catalog_id", payload.path("catalog_id").asText());
                action.put("product_retailer_id", payload.path("product_id").asText());
                break;

            case "product_list":
                action.put("catalog_id", payload.path("catalog_id").asText());
                action.set("sections", payload.path("sections"));
                break;
        }

        interactive.set("action", action);
        requestBody.set("interactive", interactive);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "INTERACTIVE_SENT");
    }

    private MessageDTO sendReactionMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "reaction");

        ObjectNode reaction = objectMapper.createObjectNode();
        reaction.put("message_id", payload.path("message_id").asText());
        reaction.put("emoji", payload.path("emoji").asText());
        requestBody.set("reaction", reaction);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "REACTION_SENT");
    }

    private MessageDTO uploadMedia(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/media",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        byte[] mediaData = Base64.getDecoder().decode(payload.path("media_data").asText());
        String fileName = payload.path("file_name").asText("file");
        String mimeType = payload.path("mime_type").asText("application/octet - stream");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("messaging_product", "whatsapp");

        Resource mediaResource = new ByteArrayResource(mediaData) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        body.add("file", mediaResource);
        body.add("type", mimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "MEDIA_UPLOADED");
    }

    private MessageDTO getMediaUrl(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            mediaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "MEDIA_URL_RETRIEVED");
    }

    private MessageDTO deleteMedia(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            mediaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "MEDIA_DELETED");
    }

    private MessageDTO createTemplate(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/%s/message_templates",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getBusinessAccountId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "TEMPLATE_CREATED");
    }

    private MessageDTO updateTemplate(MessageDTO message) throws Exception {
        // WhatsApp doesn't support direct template updates - need to create new version
        return createTemplate(message);
    }

    private MessageDTO deleteTemplate(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String templateName = payload.path("template_name").asText();

        String url = String.format("%s/%s/%s/message_templates?name = %s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getBusinessAccountId(),
            URLEncoder.encode(templateName, StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "TEMPLATE_DELETED");
    }

    private MessageDTO getTemplates(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/message_templates",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getBusinessAccountId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "TEMPLATES_RETRIEVED");
    }

    private MessageDTO getBusinessProfile(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/whatsapp_business_profile",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        String fields = "about,address,description,email,profile_picture_url,vertical,websites";
        url += "?fields = " + fields;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "BUSINESS_PROFILE_RETRIEVED");
    }

    private MessageDTO updateBusinessProfile(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/whatsapp_business_profile",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "BUSINESS_PROFILE_UPDATED");
    }

    // Additional helper methods for other operations
    private MessageDTO getContacts(MessageDTO message) throws Exception {
        // WhatsApp Business API doesn't provide direct contact listing
        // This would typically be managed by the business's own CRM
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(message.getCorrelationId());
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "operation", "CONTACTS_NOT_AVAILABLE",
            "message", "Contact management should be handled by your CRM system"
       ));
        return response;
    }

    private MessageDTO createContact(MessageDTO message) throws Exception {
        return getContacts(message);
    }

    private MessageDTO updateContact(MessageDTO message) throws Exception {
        return getContacts(message);
    }

    private MessageDTO createGroup(MessageDTO message) throws Exception {
        // Groups are not directly manageable via Business API
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(message.getCorrelationId());
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "operation", "GROUPS_NOT_SUPPORTED",
            "message", "Group management is not available in WhatsApp Business API"
       ));
        return response;
    }

    private MessageDTO updateGroup(MessageDTO message) throws Exception {
        return createGroup(message);
    }

    private MessageDTO addGroupParticipants(MessageDTO message) throws Exception {
        return createGroup(message);
    }

    private MessageDTO removeGroupParticipants(MessageDTO message) throws Exception {
        return createGroup(message);
    }

    private MessageDTO getCatalog(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String catalogId = payload.path("catalog_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            catalogId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "CATALOG_RETRIEVED");
    }

    private MessageDTO createProduct(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String catalogId = payload.path("catalog_id").asText();

        String url = String.format("%s/%s/%s/products",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            catalogId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "PRODUCT_CREATED");
    }

    private MessageDTO updateProduct(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String productId = payload.path("product_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            productId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "PRODUCT_UPDATED");
    }

    private MessageDTO deleteProduct(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String productId = payload.path("product_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            productId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "PRODUCT_DELETED");
    }

    private MessageDTO createQRCode(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/message_qrdls",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "QR_CODE_CREATED");
    }

    private MessageDTO getQRCodes(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/message_qrdls",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "QR_CODES_RETRIEVED");
    }

    private MessageDTO updateQRCode(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String qrCodeId = payload.path("qr_code_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            qrCodeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "QR_CODE_UPDATED");
    }

    private MessageDTO deleteQRCode(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String qrCodeId = payload.path("qr_code_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            qrCodeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "QR_CODE_DELETED");
    }

    private MessageDTO createFlow(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s/flows",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getBusinessAccountId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "FLOW_CREATED");
    }

    private MessageDTO updateFlow(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String flowId = payload.path("flow_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            flowId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "FLOW_UPDATED");
    }

    private MessageDTO deleteFlow(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String flowId = payload.path("flow_id").asText();

        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            flowId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "FLOW_DELETED");
    }

    private MessageDTO publishFlow(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String flowId = payload.path("flow_id").asText();

        String url = String.format("%s/%s/%s/publish",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            flowId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("action", "PUBLISH");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "FLOW_PUBLISHED");
    }

    private MessageDTO createSuccessResponseMessage(String messageId, String responseBody, String operation) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setTimestamp(LocalDateTime.now());
        response.setHeaders(Map.of(
            "operation", operation,
            "source", "whatsapp"
       ));
        response.setPayload(responseBody);
        return response;
    }

    private String getAccessToken() {
        String encryptedToken = config.getSystemUserAccessToken() != null ?
            config.getSystemUserAccessToken() : config.getAccessToken();
        return credentialEncryptionService.decrypt(encryptedToken);
    }

    private void validateConfiguration() throws AdapterException {
        if(config == null) {
            throw new AdapterException("WhatsApp Business configuration is not set");
        }
        if(config.getPhoneNumberId() == null || config.getPhoneNumberId().isEmpty()) {
            throw new AdapterException("Phone number ID is required");
        }
        if(config.getSystemUserAccessToken() == null && config.getAccessToken() == null) {
            throw new AdapterException("Access token is required");
        }
    }

    public MessageDTO sendMessage(MessageDTO message) throws AdapterException {
        return processMessage(message);
    }

    public void setConfiguration(WhatsAppBusinessApiConfig config) {
        this.config = config;
    }

    // Add missing methods referenced in switch
    private MessageDTO markAsRead(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String messageId = payload.path("message_id").asText();

        String url = String.format("%s/%s/%s/messages",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("status", "read");
        requestBody.put("message_id", messageId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "MESSAGE_MARKED_READ");
    }

    private MessageDTO getProducts(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String catalogId = payload.path("catalog_id").asText();

        String url = String.format("%s/%s/%s/products",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            catalogId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "PRODUCTS_RETRIEVED");
    }

    private MessageDTO getPhoneNumber(MessageDTO message) throws Exception {
        String url = String.format("%s/%s/%s",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "PHONE_NUMBER_RETRIEVED");
    }

    private MessageDTO verifyPhone(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String verificationCode = payload.path("code").asText();

        String url = String.format("%s/%s/%s/register",
            config.getApiBaseUrl(),
            config.getApiVersion(),
            config.getPhoneNumberId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("pin", verificationCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponseMessage(message.getCorrelationId(), response.getBody(), "PHONE_VERIFIED");
    }

    // Implement abstract methods from AbstractSocialMediaOutboundAdapter
    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("baseUrl", config.getApiBaseUrl());
            configMap.put("apiVersion", config.getApiVersion());
            configMap.put("phoneNumberId", config.getPhoneNumberId());
            configMap.put("businessAccountId", config.getBusinessAccountId());
            configMap.put("features", config.getFeatures());
            configMap.put("limits", config.getLimits());
        }
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.WHATSAPP;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    // Implement abstract methods from AbstractOutboundAdapter
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            validateConfiguration();
            // Test API connection by getting business profile
            String url = String.format("%s/%s/%s/whatsapp_business_profile",
                config.getApiBaseUrl(),
                config.getApiVersion(),
                config.getPhoneNumberId());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success("Connection test successful");
            } else {
                return AdapterResult.failure("Connection test failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return AdapterResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    @Override
    protected void doInitialize() {
        log.info("Initializing WhatsApp Business Outbound Adapter");
        try {
            validateConfiguration();
        } catch (Exception e) {
            log.error("Failed to initialize adapter", e);
            throw new RuntimeException("Failed to initialize WhatsApp Business adapter", e);
        }
    }

    @Override
    protected void doDestroy() {
        log.info("Destroying WhatsApp Business Outbound Adapter");
    }

    // Implement missing abstract methods from AbstractOutboundAdapter
    @Override
    protected void doReceiverInitialize() throws Exception {
        log.info("Initializing WhatsApp Business receiver (not used for outbound adapters)");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.info("Destroying WhatsApp Business receiver (not used for outbound adapters)");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapters typically don't receive data, but this method is required
        log.warn("doReceive called on outbound adapter - not supported");
        return AdapterResult.failure("Receive operation not supported on outbound adapter");
    }

    @Override
    protected long getPollingIntervalMs() {
        // Not used for outbound adapters
        return 0;
    }
}
