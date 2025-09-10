package com.integrixs.adapters.social.whatsapp;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.whatsapp.WhatsAppBusinessApiConfig.*;
import com.integrixs.core.api.channel.Message;
import com.integrixs.core.exception.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
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
import java.net.URLEncoder;
import java.io.IOException;

@Slf4j
@Component("whatsappBusinessOutboundAdapter")
public class WhatsAppBusinessOutboundAdapter extends AbstractSocialMediaOutboundAdapter<WhatsAppBusinessApiConfig> {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public WhatsAppBusinessOutboundAdapter(
            WhatsAppBusinessApiConfig config,
            RateLimiterService rateLimiterService,
            OAuth2TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(config, rateLimiterService, tokenRefreshService, credentialEncryptionService);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Message processMessage(Message message) throws AdapterException {
        validateConfiguration();
        
        String operation = (String) message.getHeaders().get("operation");
        if (operation == null) {
            throw new AdapterException("Operation header is required");
        }
        
        log.debug("Processing WhatsApp Business operation: {}", operation);
        
        try {
            rateLimiterService.acquire("whatsapp_api", 1);
            
            Message response;
            switch (operation.toUpperCase()) {
                // Messaging operations
                case "SEND_TEXT":
                    response = sendTextMessage(message);
                    break;
                case "SEND_IMAGE":
                    response = sendImageMessage(message);
                    break;
                case "SEND_VIDEO":
                    response = sendVideoMessage(message);
                    break;
                case "SEND_AUDIO":
                    response = sendAudioMessage(message);
                    break;
                case "SEND_DOCUMENT":
                    response = sendDocumentMessage(message);
                    break;
                case "SEND_LOCATION":
                    response = sendLocationMessage(message);
                    break;
                case "SEND_CONTACTS":
                    response = sendContactsMessage(message);
                    break;
                case "SEND_STICKER":
                    response = sendStickerMessage(message);
                    break;
                case "SEND_TEMPLATE":
                    response = sendTemplateMessage(message);
                    break;
                case "SEND_INTERACTIVE":
                    response = sendInteractiveMessage(message);
                    break;
                case "SEND_REACTION":
                    response = sendReactionMessage(message);
                    break;
                    
                // Template operations
                case "CREATE_TEMPLATE":
                    response = createTemplate(message);
                    break;
                case "UPDATE_TEMPLATE":
                    response = updateTemplate(message);
                    break;
                case "DELETE_TEMPLATE":
                    response = deleteTemplate(message);
                    break;
                case "GET_TEMPLATES":
                    response = getTemplates(message);
                    break;
                    
                // Media operations
                case "UPLOAD_MEDIA":
                    response = uploadMedia(message);
                    break;
                case "GET_MEDIA_URL":
                    response = getMediaUrl(message);
                    break;
                case "DELETE_MEDIA":
                    response = deleteMedia(message);
                    break;
                    
                // Business operations
                case "GET_BUSINESS_PROFILE":
                    response = getBusinessProfile(message);
                    break;
                case "UPDATE_BUSINESS_PROFILE":
                    response = updateBusinessProfile(message);
                    break;
                    
                // Contact operations
                case "GET_CONTACTS":
                    response = getContacts(message);
                    break;
                case "CREATE_CONTACT":
                    response = createContact(message);
                    break;
                case "UPDATE_CONTACT":
                    response = updateContact(message);
                    break;
                    
                // Group operations
                case "CREATE_GROUP":
                    response = createGroup(message);
                    break;
                case "UPDATE_GROUP":
                    response = updateGroup(message);
                    break;
                case "ADD_GROUP_PARTICIPANTS":
                    response = addGroupParticipants(message);
                    break;
                case "REMOVE_GROUP_PARTICIPANTS":
                    response = removeGroupParticipants(message);
                    break;
                    
                // Catalog operations
                case "GET_CATALOG":
                    response = getCatalog(message);
                    break;
                case "CREATE_PRODUCT":
                    response = createProduct(message);
                    break;
                case "UPDATE_PRODUCT":
                    response = updateProduct(message);
                    break;
                case "DELETE_PRODUCT":
                    response = deleteProduct(message);
                    break;
                    
                // QR code operations
                case "CREATE_QR_CODE":
                    response = createQRCode(message);
                    break;
                case "GET_QR_CODES":
                    response = getQRCodes(message);
                    break;
                case "UPDATE_QR_CODE":
                    response = updateQRCode(message);
                    break;
                case "DELETE_QR_CODE":
                    response = deleteQRCode(message);
                    break;
                    
                // Flow operations
                case "CREATE_FLOW":
                    response = createFlow(message);
                    break;
                case "UPDATE_FLOW":
                    response = updateFlow(message);
                    break;
                case "DELETE_FLOW":
                    response = deleteFlow(message);
                    break;
                case "PUBLISH_FLOW":
                    response = publishFlow(message);
                    break;
                    
                default:
                    throw new AdapterException("Unknown operation: " + operation);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing WhatsApp Business message", e);
            Message errorResponse = new Message();
            errorResponse.setMessageId(message.getMessageId());
            errorResponse.setStatus(MessageStatus.ERROR);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "originalOperation", operation
            ));
            return errorResponse;
        }
    }
    
    private Message sendTextMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
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
        if (payload.has("reply_to")) {
            ObjectNode context = objectMapper.createObjectNode();
            context.put("message_id", payload.path("reply_to").asText());
            requestBody.set("context", context);
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TEXT_SENT");
    }
    
    private Message sendImageMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("recipient_type", "individual");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "image");
        
        ObjectNode image = objectMapper.createObjectNode();
        if (payload.has("media_id")) {
            image.put("id", payload.path("media_id").asText());
        } else if (payload.has("link")) {
            image.put("link", payload.path("link").asText());
        }
        if (payload.has("caption")) {
            image.put("caption", payload.path("caption").asText());
        }
        requestBody.set("image", image);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "IMAGE_SENT");
    }
    
    private Message sendVideoMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "video");
        
        ObjectNode video = objectMapper.createObjectNode();
        if (payload.has("media_id")) {
            video.put("id", payload.path("media_id").asText());
        } else if (payload.has("link")) {
            video.put("link", payload.path("link").asText());
        }
        if (payload.has("caption")) {
            video.put("caption", payload.path("caption").asText());
        }
        requestBody.set("video", video);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "VIDEO_SENT");
    }
    
    private Message sendAudioMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "audio");
        
        ObjectNode audio = objectMapper.createObjectNode();
        if (payload.has("media_id")) {
            audio.put("id", payload.path("media_id").asText());
        } else if (payload.has("link")) {
            audio.put("link", payload.path("link").asText());
        }
        requestBody.set("audio", audio);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AUDIO_SENT");
    }
    
    private Message sendDocumentMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "document");
        
        ObjectNode document = objectMapper.createObjectNode();
        if (payload.has("media_id")) {
            document.put("id", payload.path("media_id").asText());
        } else if (payload.has("link")) {
            document.put("link", payload.path("link").asText());
        }
        if (payload.has("caption")) {
            document.put("caption", payload.path("caption").asText());
        }
        if (payload.has("filename")) {
            document.put("filename", payload.path("filename").asText());
        }
        requestBody.set("document", document);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "DOCUMENT_SENT");
    }
    
    private Message sendLocationMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "location");
        
        ObjectNode location = objectMapper.createObjectNode();
        location.put("longitude", payload.path("longitude").asDouble());
        location.put("latitude", payload.path("latitude").asDouble());
        if (payload.has("name")) {
            location.put("name", payload.path("name").asText());
        }
        if (payload.has("address")) {
            location.put("address", payload.path("address").asText());
        }
        requestBody.set("location", location);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LOCATION_SENT");
    }
    
    private Message sendContactsMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "contacts");
        
        ArrayNode contacts = objectMapper.createArrayNode();
        JsonNode contactsData = payload.path("contacts");
        if (contactsData.isArray()) {
            contacts = (ArrayNode) contactsData;
        }
        requestBody.set("contacts", contacts);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "CONTACTS_SENT");
    }
    
    private Message sendStickerMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", payload.path("to").asText());
        requestBody.put("type", "sticker");
        
        ObjectNode sticker = objectMapper.createObjectNode();
        if (payload.has("media_id")) {
            sticker.put("id", payload.path("media_id").asText());
        } else if (payload.has("link")) {
            sticker.put("link", payload.path("link").asText());
        }
        requestBody.set("sticker", sticker);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "STICKER_SENT");
    }
    
    private Message sendTemplateMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
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
        
        // Add components (header, body, footer, buttons)
        if (payload.has("components")) {
            template.set("components", payload.get("components"));
        }
        
        requestBody.set("template", template);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TEMPLATE_SENT");
    }
    
    private Message sendInteractiveMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
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
        if (payload.has("header")) {
            interactive.set("header", payload.get("header"));
        }
        
        // Add body
        ObjectNode body = objectMapper.createObjectNode();
        body.put("text", payload.path("body_text").asText());
        interactive.set("body", body);
        
        // Add footer if present
        if (payload.has("footer_text")) {
            ObjectNode footer = objectMapper.createObjectNode();
            footer.put("text", payload.path("footer_text").asText());
            interactive.set("footer", footer);
        }
        
        // Add action based on type
        ObjectNode action = objectMapper.createObjectNode();
        switch (interactiveType) {
            case "button":
                ArrayNode buttons = objectMapper.createArrayNode();
                JsonNode buttonData = payload.path("buttons");
                if (buttonData.isArray()) {
                    for (JsonNode button : buttonData) {
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
                if (sectionData.isArray()) {
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "INTERACTIVE_SENT");
    }
    
    private Message sendReactionMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/messages", 
            config.getBaseUrl(), 
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "REACTION_SENT");
    }
    
    private Message uploadMedia(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/media", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        byte[] mediaData = Base64.getDecoder().decode(payload.path("media_data").asText());
        String fileName = payload.path("file_name").asText("file");
        String mimeType = payload.path("mime_type").asText("application/octet-stream");
        
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
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MEDIA_UPLOADED");
    }
    
    private Message getMediaUrl(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MEDIA_URL_RETRIEVED");
    }
    
    private Message deleteMedia(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String mediaId = payload.path("media_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            mediaId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MEDIA_DELETED");
    }
    
    private Message createTemplate(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/%s/message_templates", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getBusinessAccountId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TEMPLATE_CREATED");
    }
    
    private Message updateTemplate(Message message) throws Exception {
        // WhatsApp doesn't support direct template updates - need to create new version
        return createTemplate(message);
    }
    
    private Message deleteTemplate(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String templateName = payload.path("template_name").asText();
        
        String url = String.format("%s/%s/%s/message_templates?name=%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getBusinessAccountId(),
            URLEncoder.encode(templateName, StandardCharsets.UTF_8));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TEMPLATE_DELETED");
    }
    
    private Message getTemplates(Message message) throws Exception {
        String url = String.format("%s/%s/%s/message_templates", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getBusinessAccountId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TEMPLATES_RETRIEVED");
    }
    
    private Message getBusinessProfile(Message message) throws Exception {
        String url = String.format("%s/%s/%s/whatsapp_business_profile", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        String fields = "about,address,description,email,profile_picture_url,vertical,websites";
        url += "?fields=" + fields;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "BUSINESS_PROFILE_RETRIEVED");
    }
    
    private Message updateBusinessProfile(Message message) throws Exception {
        String url = String.format("%s/%s/%s/whatsapp_business_profile", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "BUSINESS_PROFILE_UPDATED");
    }
    
    // Additional helper methods for other operations
    private Message getContacts(Message message) throws Exception {
        // WhatsApp Business API doesn't provide direct contact listing
        // This would typically be managed by the business's own CRM
        Message response = new Message();
        response.setMessageId(message.getMessageId());
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "operation", "CONTACTS_NOT_AVAILABLE",
            "message", "Contact management should be handled by your CRM system"
        ));
        return response;
    }
    
    private Message createContact(Message message) throws Exception {
        return getContacts(message);
    }
    
    private Message updateContact(Message message) throws Exception {
        return getContacts(message);
    }
    
    private Message createGroup(Message message) throws Exception {
        // Groups are not directly manageable via Business API
        Message response = new Message();
        response.setMessageId(message.getMessageId());
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "operation", "GROUPS_NOT_SUPPORTED",
            "message", "Group management is not available in WhatsApp Business API"
        ));
        return response;
    }
    
    private Message updateGroup(Message message) throws Exception {
        return createGroup(message);
    }
    
    private Message addGroupParticipants(Message message) throws Exception {
        return createGroup(message);
    }
    
    private Message removeGroupParticipants(Message message) throws Exception {
        return createGroup(message);
    }
    
    private Message getCatalog(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String catalogId = payload.path("catalog_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            catalogId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "CATALOG_RETRIEVED");
    }
    
    private Message createProduct(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String catalogId = payload.path("catalog_id").asText();
        
        String url = String.format("%s/%s/%s/products", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            catalogId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "PRODUCT_CREATED");
    }
    
    private Message updateProduct(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String productId = payload.path("product_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            productId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "PRODUCT_UPDATED");
    }
    
    private Message deleteProduct(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String productId = payload.path("product_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            productId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "PRODUCT_DELETED");
    }
    
    private Message createQRCode(Message message) throws Exception {
        String url = String.format("%s/%s/%s/message_qrdls", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "QR_CODE_CREATED");
    }
    
    private Message getQRCodes(Message message) throws Exception {
        String url = String.format("%s/%s/%s/message_qrdls", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getPhoneNumberId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "QR_CODES_RETRIEVED");
    }
    
    private Message updateQRCode(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String qrCodeId = payload.path("qr_code_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            qrCodeId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "QR_CODE_UPDATED");
    }
    
    private Message deleteQRCode(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String qrCodeId = payload.path("qr_code_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            qrCodeId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "QR_CODE_DELETED");
    }
    
    private Message createFlow(Message message) throws Exception {
        String url = String.format("%s/%s/%s/flows", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            config.getBusinessAccountId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "FLOW_CREATED");
    }
    
    private Message updateFlow(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String flowId = payload.path("flow_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            flowId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(message.getPayload(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "FLOW_UPDATED");
    }
    
    private Message deleteFlow(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String flowId = payload.path("flow_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            flowId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "FLOW_DELETED");
    }
    
    private Message publishFlow(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String flowId = payload.path("flow_id").asText();
        
        String url = String.format("%s/%s/%s/publish", 
            config.getBaseUrl(), 
            config.getApiVersion(), 
            flowId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ObjectNode body = objectMapper.createObjectNode();
        body.put("action", "PUBLISH");
        
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "FLOW_PUBLISHED");
    }
    
    private Message createSuccessResponse(String messageId, String responseBody, String operation) {
        Message response = new Message();
        response.setMessageId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setTimestamp(Instant.now());
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
        if (config == null) {
            throw new AdapterException("WhatsApp Business configuration is not set");
        }
        if (config.getPhoneNumberId() == null || config.getPhoneNumberId().isEmpty()) {
            throw new AdapterException("Phone number ID is required");
        }
        if (config.getSystemUserAccessToken() == null && config.getAccessToken() == null) {
            throw new AdapterException("Access token is required");
        }
    }
}