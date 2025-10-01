package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.shared.dto.MessageDTO;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.enums.MessageStatus;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.core.AdapterResult;

/**
 * Inbound adapter for Facebook Messenger Platform integration.
 * Handles webhook events, message polling, and conversation management.
 */
@Component
@ConditionalOnProperty(name = "integrixs.adapters.facebook.messenger.enabled", havingValue = "true", matchIfMissing = false)
public class FacebookMessengerInboundAdapter extends AbstractInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(FacebookMessengerInboundAdapter.class);


    private Map<String, Object> configuration = new HashMap<>();
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();
    private final Set<String> processedPostbacks = ConcurrentHashMap.newKeySet();

    @Autowired
    public FacebookMessengerInboundAdapter(RestTemplate restTemplate, ObjectMapper objectMapper,
                                         ApplicationEventPublisher eventPublisher) {
        super(AdapterConfiguration.AdapterTypeEnum.REST);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    protected Map<String, Object> getConfig() {
        return configuration;
    }

    public Map<String, Object> getAdapterConfig() {
        return configuration;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    /**
     * Polls for recent conversations and messages
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.facebook.messenger.polling.interval:60000}")
    public void pollMessages() {
        if(!Boolean.TRUE.equals(configuration.get("pollingEnabled"))) {
            return;
        }

        try {
            log.debug("Polling Facebook Messenger messages");
            pollConversations();
        } catch(Exception e) {
            log.error("Error polling Facebook Messenger messages", e);
        }
    }

    /**
     * Polls for message insights and analytics
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.facebook.messenger.polling.insights - interval:3600000}")
    public void pollInsights() {
        if(!Boolean.TRUE.equals(configuration.get("enableMessageInsights"))) {
            return;
        }

        try {
            log.debug("Polling Facebook Messenger insights");
            fetchInsights();
        } catch(Exception e) {
            log.error("Error polling Facebook Messenger insights", e);
        }
    }

    private void pollConversations() throws Exception {
        String url = (String) configuration.get("apiUrl") + "/" + (String) configuration.get("pageId") + "/conversations";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", (String) configuration.get("pageAccessToken"));
        params.put("fields", "id,participants,updated_time,messages {id,from,to,message,attachments,created_time}");
        params.put("limit", "25");

        String response = makeGetRequest(url, params);
        Map<String, Object> responseData = parseJsonResponse(response);

        List<Map<String, Object>> conversations = (List<Map<String, Object>>) responseData.get("data");
        if(conversations != null) {
            for(Map<String, Object> conversation : conversations) {
                processConversation(conversation);
            }
        }

        // Handle pagination
        handlePagination(responseData);
    }

    private void processConversation(Map<String, Object> conversation) {
        Map<String, Object> messages = (Map<String, Object>) conversation.get("messages");
        if(messages != null) {
            List<Map<String, Object>> messageList = (List<Map<String, Object>>) messages.get("data");
            if(messageList != null) {
                for(Map<String, Object> message : messageList) {
                    String messageId = (String) message.get("id");
                    if(!processedMessages.contains(messageId)) {
                        processMessage(message, (String) conversation.get("id"));
                        processedMessages.add(messageId);
                    }
                }
            }
        }
    }

    private void processMessage(Map<String, Object> messageData, String conversationId) {
        String messageId = (String) messageData.get("id");
        if(processedMessages.contains(messageId)) {
            log.debug("Message already processed: {}", messageId);
            return;
        }

        FacebookMessengerMessageDTO message = new FacebookMessengerMessageDTO();
        message.setMessageId(messageId);
        message.setConversationId(conversationId);
        message.setFrom(extractUserInfo((Map<String, Object>) messageData.get("from")));
        message.setTo(extractUserInfo((Map<String, Object>) messageData.get("to")));
        message.setText((String) messageData.get("message"));
        message.setAttachments(extractAttachments((List<Map<String, Object>>) messageData.get("attachments")));
        message.setTimestamp((String) messageData.get("created_time"));

        publishMessage("messenger_message", message);
    }

    private void fetchInsights() throws Exception {
        String url = (String) configuration.get("apiUrl") + "/" + (String) configuration.get("pageId") + "/insights";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", (String) configuration.get("pageAccessToken"));
        params.put("metric", "page_messages_total_messaging_connections,page_messages_new_conversations_unique");
        params.put("period", "day");

        String response = makeGetRequest(url, params);
        Map<String, Object> responseData = parseJsonResponse(response);

        List<Map<String, Object>> insights = (List<Map<String, Object>>) responseData.get("data");
        if(insights != null) {
            processInsights(insights);
        }
    }

    private void processInsights(List<Map<String, Object>> insights) {
        for(Map<String, Object> insight : insights) {
            FacebookMessengerInsight insightData = new FacebookMessengerInsight();
            insightData.setMetric((String) insight.get("name"));

            List<Map<String, Object>> values = (List<Map<String, Object>>) insight.get("values");
            if(values != null && !values.isEmpty()) {
                Map<String, Object> latestValue = values.get(values.size() - 1);
                insightData.setValue(latestValue.get("value"));
                insightData.setEndTime((String) latestValue.get("end_time"));

                publishMessage("messenger_insight", insightData);
            }
        }
    }

    public void processWebhookEvent(Map<String, Object> event) {
        List<Map<String, Object>> entries = (List<Map<String, Object>>) event.get("entry");
        if(entries != null) {
            for(Map<String, Object> entry : entries) {
                try {
                    processPageEntry(entry);
                } catch(Exception e) {
                    log.error("Error processing webhook entry", e);
                }
            }
        }
    }

    private void processPageEntry(Map<String, Object> entry) {
        String pageId = (String) entry.get("id");
        Long timestamp = (Long) entry.get("time");

        List<Map<String, Object>> messaging = (List<Map<String, Object>>) entry.get("messaging");
        if(messaging != null) {
            for(Map<String, Object> event : messaging) {
                try {
                    processMessagingEvent(event, pageId, timestamp);
                } catch(Exception e) {
                    log.error("Error processing messaging event", e);
                }
            }
        }
    }

    private void processMessagingEvent(Map<String, Object> event, String pageId, Long timestamp) {
        Map<String, Object> sender = (Map<String, Object>) event.get("sender");
        Map<String, Object> recipient = (Map<String, Object>) event.get("recipient");

        if(event.containsKey("message")) {
            processWebhookMessage(event, sender, recipient, pageId, timestamp);
        } else if(event.containsKey("postback")) {
            processWebhookPostback(event, sender, recipient, pageId, timestamp);
        } else if(event.containsKey("referral")) {
            processWebhookReferral(event, sender, recipient, pageId, timestamp);
        } else if(event.containsKey("optin")) {
            processWebhookOptin(event, sender, recipient, pageId, timestamp);
        } else if(event.containsKey("delivery")) {
            processWebhookDelivery(event, sender, recipient, pageId, timestamp);
        } else if(event.containsKey("read")) {
            processWebhookRead(event, sender, recipient, pageId, timestamp);
        } else if(event.containsKey("reaction")) {
            processWebhookReaction(event, sender, recipient, pageId, timestamp);
        }
    }

    private void processWebhookMessage(Map<String, Object> event, Map<String, Object> sender,
                                     Map<String, Object> recipient, String pageId, Long timestamp) {
        Map<String, Object> messageData = (Map<String, Object>) event.get("message");
        String messageId = (String) messageData.get("mid");

        if(processedMessages.contains(messageId)) {
            log.debug("Message already processed: {}", messageId);
            return;
        }

        FacebookMessengerMessageDTO message = new FacebookMessengerMessageDTO();
        message.setMessageId(messageId);
        message.setFrom(extractUserInfo(sender));
        message.setTo(extractUserInfo(recipient));
        message.setText((String) messageData.get("text"));
        message.setAttachments(extractAttachments((List<Map<String, Object>>) messageData.get("attachments")));
        message.setQuickReplies(extractQuickReplies((List<Map<String, Object>>) messageData.get("quick_replies")));
        message.setReplyTo((String) messageData.get("reply_to"));
        message.setIsEcho(Boolean.TRUE.equals(messageData.get("is_echo")));
        message.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger_message", message);
        processedMessages.add(messageId);
    }

    private void processWebhookPostback(Map<String, Object> event, Map<String, Object> sender,
                                      Map<String, Object> recipient, String pageId, Long timestamp) {
        Map<String, Object> postbackData = (Map<String, Object>) event.get("postback");
        String payload = (String) postbackData.get("payload");

        String postbackId = sender.get("id") + "_" + payload + "_" + timestamp;
        if(processedPostbacks.contains(postbackId)) {
            log.debug("Postback already processed: {}", postbackId);
            return;
        }

        FacebookMessengerPostback postback = new FacebookMessengerPostback();
        postback.setSenderId((String) sender.get("id"));
        postback.setRecipientId((String) recipient.get("id"));
        postback.setPayload(payload);
        postback.setTitle((String) postbackData.get("title"));
        postback.setReferral(extractReferral((Map<String, Object>) postbackData.get("referral")));
        postback.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger_postback", postback);
        processedPostbacks.add(postbackId);
    }

    private void processWebhookReferral(Map<String, Object> event, Map<String, Object> sender,
                                      Map<String, Object> recipient, String pageId, Long timestamp) {
        Map<String, Object> referralData = (Map<String, Object>) event.get("referral");
        FacebookMessengerReferral referral = extractReferral(referralData);
        referral.setSenderId((String) sender.get("id"));
        referral.setRecipientId((String) recipient.get("id"));
        referral.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger_referral", referral);
    }

    private void processWebhookOptin(Map<String, Object> event, Map<String, Object> sender,
                                   Map<String, Object> recipient, String pageId, Long timestamp) {
        Map<String, Object> optinData = (Map<String, Object>) event.get("optin");

        FacebookMessengerOptin optin = new FacebookMessengerOptin();
        optin.setSenderId((String) sender.get("id"));
        optin.setRecipientId((String) recipient.get("id"));
        optin.setRef((String) optinData.get("ref"));
        optin.setUserRef((String) optinData.get("user_ref"));
        optin.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger_optin", optin);
    }

    private void processWebhookDelivery(Map<String, Object> event, Map<String, Object> sender,
                                      Map<String, Object> recipient, String pageId, Long timestamp) {
        Map<String, Object> deliveryData = (Map<String, Object>) event.get("delivery");

        FacebookMessengerDelivery delivery = new FacebookMessengerDelivery();
        delivery.setSenderId((String) sender.get("id"));
        delivery.setRecipientId((String) recipient.get("id"));
        delivery.setMessageIds((List<String>) deliveryData.get("mids"));
        delivery.setWatermark((Long) deliveryData.get("watermark"));
        delivery.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger_delivery", delivery);
    }

    private void processWebhookRead(Map<String, Object> event, Map<String, Object> sender,
                                  Map<String, Object> recipient, String pageId, Long timestamp) {
        Map<String, Object> readData = (Map<String, Object>) event.get("read");

        FacebookMessengerRead read = new FacebookMessengerRead();
        read.setSenderId((String) sender.get("id"));
        read.setRecipientId((String) recipient.get("id"));
        read.setWatermark((Long) readData.get("watermark"));
        read.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger_read", read);
    }

    private void processWebhookReaction(Map<String, Object> event, Map<String, Object> sender,
                                      Map<String, Object> recipient, String pageId, Long timestamp) {
        Map<String, Object> reactionData = (Map<String, Object>) event.get("reaction");

        FacebookMessengerReaction reaction = new FacebookMessengerReaction();
        reaction.setSenderId((String) sender.get("id"));
        reaction.setRecipientId((String) recipient.get("id"));
        reaction.setMessageId((String) reactionData.get("mid"));
        reaction.setAction((String) reactionData.get("action"));
        reaction.setEmoji((String) reactionData.get("emoji"));
        reaction.setReaction((String) reactionData.get("reaction"));
        reaction.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger_reaction", reaction);
    }

    @Override
    protected AdapterResult doSend(Object data, Map<String, Object> headers) throws Exception {
        String recipientId = (String) headers.get("recipientId");
        if(recipientId == null) {
            throw new AdapterException("Recipient ID is required for sending messages");
        }

        try {
            String url = (String) configuration.get("apiUrl") + "/me/messages";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("Authorization", "Bearer " + configuration.get("pageAccessToken"));

            Map<String, Object> request = new HashMap<>();
            request.put("recipient", Map.of("id", recipientId));
            request.put("message", data);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, httpHeaders);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setPayload(response.getBody());
            messageDTO.setStatus(MessageStatus.SUCCESS);
            messageDTO.setCorrelationId(UUID.randomUUID().toString());

            return AdapterResult.success(messageDTO, "Message sent successfully");
        } catch(Exception e) {
            return AdapterResult.failure("Failed to send message: " + e.getMessage(), e);
        }
    }

    public String verifyWebhookSubscription(String mode, String challenge, String verifyToken) {
        String expectedToken = (String) configuration.get("webhookVerifyToken");
        if("subscribe".equals(mode) && expectedToken.equals(verifyToken)) {
            return challenge;
        }
        return null;
    }

    public boolean validateWebhookSignature(String signature, String payload) {
        try {
            String appSecret = (String) configuration.get("appSecret");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((payload + appSecret).getBytes(StandardCharsets.UTF_8));
            String expectedSignature = "sha256=" + bytesToHex(hash);
            return expectedSignature.equals(signature);
        } catch(Exception e) {
            log.error("Error validating webhook signature", e);
            return false;
        }
    }

    private FacebookMessengerUser extractUserInfo(Map<String, Object> user) {
        if(user == null) {
            return null;
        }
        FacebookMessengerUser userInfo = new FacebookMessengerUser();
        userInfo.setId((String) user.get("id"));
        userInfo.setName((String) user.get("name"));
        return userInfo;
    }

    private List<FacebookMessengerAttachment> extractAttachments(List<Map<String, Object>> attachmentsList) {
        if(attachmentsList == null || attachmentsList.isEmpty()) {
            return null;
        }

        List<FacebookMessengerAttachment> attachments = new ArrayList<>();
        for(Map<String, Object> attachmentData : attachmentsList) {
            FacebookMessengerAttachment attachment = new FacebookMessengerAttachment();
            attachment.setType((String) attachmentData.get("type"));
            attachment.setPayload(attachmentData.get("payload"));
            attachments.add(attachment);
        }
        return attachments;
    }

    private List<String> extractQuickReplies(List<Map<String, Object>> quickRepliesList) {
        if(quickRepliesList == null || quickRepliesList.isEmpty()) {
            return null;
        }

        List<String> quickReplies = new ArrayList<>();
        for(Map<String, Object> reply : quickRepliesList) {
            String title = (String) reply.get("title");
            if(title != null) {
                quickReplies.add(title);
            }
        }
        return quickReplies;
    }

    private FacebookMessengerReferral extractReferral(Map<String, Object> referralData) {
        if(referralData == null) {
            return null;
        }

        FacebookMessengerReferral referral = new FacebookMessengerReferral();
        referral.setRef((String) referralData.get("ref"));
        referral.setSource((String) referralData.get("source"));
        referral.setType((String) referralData.get("type"));
        referral.setAdId((String) referralData.get("ad_id"));
        return referral;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for(byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String makeGetRequest(String url, Map<String, String> params) {
        try {
            StringBuilder urlBuilder = new StringBuilder(url);
            if(params != null && !params.isEmpty()) {
                urlBuilder.append("?");
                for(Map.Entry<String, String> entry : params.entrySet()) {
                    urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                urlBuilder.deleteCharAt(urlBuilder.length() - 1);
            }

            ResponseEntity<String> response = restTemplate.getForEntity(
                urlBuilder.toString(), String.class);
            return response.getBody();
        } catch(Exception e) {
            log.error("Error making GET request to: " + url, e);
            throw new RuntimeException("Failed to make GET request", e);
        }
    }

    private Map<String, Object> parseJsonResponse(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch(Exception e) {
            log.error("Error parsing JSON response", e);
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    private void handlePagination(Map<String, Object> responseData) {
        Map<String, Object> paging = (Map<String, Object>) responseData.get("paging");
        if(paging != null) {
            String nextUrl = (String) paging.get("next");
            if(nextUrl != null) {
                // Process next page if needed
                log.debug("Next page available: {}", nextUrl);
            }
        }
    }

    private void publishMessage(String eventType, Object message) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", eventType);
            event.put("source", "facebook_messenger");
            event.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            event.put("data", message);

            // Publish event
            log.info("Publishing {} event", eventType);
        } catch(Exception e) {
            log.error("Error publishing message", e);
        }
    }

    // Getters and Setters for main class fields only
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        log.debug("Initializing Facebook Messenger sender");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.debug("Destroying Facebook Messenger sender");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            String url = (String) configuration.get("apiUrl") + "/" + (String) configuration.get("pageId");
            Map<String, String> params = new HashMap<>();
            params.put("access_token", (String) configuration.get("pageAccessToken"));
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

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        validateReady();

        if(payload == null) {
            throw new AdapterException("Payload cannot be null");
        }

        return executeTimedOperation("send", () -> doSend(payload, headers));
    }

    // Data classes for Facebook Messenger entities
    public static class FacebookMessengerMessageDTO {
        private String messageId;
        private String conversationId;
        private FacebookMessengerUser from;
        private FacebookMessengerUser to;
        private String text;
        private List<FacebookMessengerAttachment> attachments;
        private List<String> quickReplies;
        private String replyTo;
        private boolean isEcho;
        private String timestamp;

        // Getters and Setters
        public String getMessageId() {
            return messageId;
        }
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        public String getConversationId() {
            return conversationId;
        }
        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }
        public FacebookMessengerUser getFrom() {
            return from;
        }
        public void setFrom(FacebookMessengerUser from) {
            this.from = from;
        }
        public FacebookMessengerUser getTo() {
            return to;
        }
        public void setTo(FacebookMessengerUser to) {
            this.to = to;
        }
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public List<FacebookMessengerAttachment> getAttachments() {
            return attachments;
        }
        public void setAttachments(List<FacebookMessengerAttachment> attachments) {
            this.attachments = attachments;
        }
        public List<String> getQuickReplies() {
            return quickReplies;
        }
        public void setQuickReplies(List<String> quickReplies) {
            this.quickReplies = quickReplies;
        }
        public String getReplyTo() {
            return replyTo;
        }
        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }
        public boolean isIsEcho() {
            return isEcho;
        }
        public void setIsEcho(boolean isEcho) {
            this.isEcho = isEcho;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class FacebookMessengerUser {
        private String id;
        private String name;

        // Getters and Setters
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class FacebookMessengerAttachment {
        private String type;
        private Object payload;

        // Getters and Setters
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public Object getPayload() {
            return payload;
        }
        public void setPayload(Object payload) {
            this.payload = payload;
        }
    }

    public static class FacebookMessengerPostback {
        private String senderId;
        private String recipientId;
        private String payload;
        private String title;
        private FacebookMessengerReferral referral;
        private String timestamp;

        // Getters and Setters
        public String getSenderId() {
            return senderId;
        }
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        public String getRecipientId() {
            return recipientId;
        }
        public void setRecipientId(String recipientId) {
            this.recipientId = recipientId;
        }
        public String getPayload() {
            return payload;
        }
        public void setPayload(String payload) {
            this.payload = payload;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public FacebookMessengerReferral getReferral() {
            return referral;
        }
        public void setReferral(FacebookMessengerReferral referral) {
            this.referral = referral;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class FacebookMessengerReferral {
        private String ref;
        private String source;
        private String type;
        private String adId;
        private String senderId;
        private String recipientId;
        private String timestamp;

        // Getters and Setters
        public String getRef() {
            return ref;
        }
        public void setRef(String ref) {
            this.ref = ref;
        }
        public String getSource() {
            return source;
        }
        public void setSource(String source) {
            this.source = source;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getAdId() {
            return adId;
        }
        public void setAdId(String adId) {
            this.adId = adId;
        }
        public String getSenderId() {
            return senderId;
        }
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        public String getRecipientId() {
            return recipientId;
        }
        public void setRecipientId(String recipientId) {
            this.recipientId = recipientId;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class FacebookMessengerOptin {
        private String senderId;
        private String recipientId;
        private String ref;
        private String userRef;
        private String timestamp;

        // Getters and Setters
        public String getSenderId() {
            return senderId;
        }
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        public String getRecipientId() {
            return recipientId;
        }
        public void setRecipientId(String recipientId) {
            this.recipientId = recipientId;
        }
        public String getRef() {
            return ref;
        }
        public void setRef(String ref) {
            this.ref = ref;
        }
        public String getUserRef() {
            return userRef;
        }
        public void setUserRef(String userRef) {
            this.userRef = userRef;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class FacebookMessengerDelivery {
        private String senderId;
        private String recipientId;
        private List<String> messageIds;
        private Long watermark;
        private String timestamp;

        // Getters and Setters
        public String getSenderId() {
            return senderId;
        }
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        public String getRecipientId() {
            return recipientId;
        }
        public void setRecipientId(String recipientId) {
            this.recipientId = recipientId;
        }
        public List<String> getMessageIds() {
            return messageIds;
        }
        public void setMessageIds(List<String> messageIds) {
            this.messageIds = messageIds;
        }
        public Long getWatermark() {
            return watermark;
        }
        public void setWatermark(Long watermark) {
            this.watermark = watermark;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class FacebookMessengerRead {
        private String senderId;
        private String recipientId;
        private Long watermark;
        private String timestamp;

        // Getters and Setters
        public String getSenderId() {
            return senderId;
        }
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        public String getRecipientId() {
            return recipientId;
        }
        public void setRecipientId(String recipientId) {
            this.recipientId = recipientId;
        }
        public Long getWatermark() {
            return watermark;
        }
        public void setWatermark(Long watermark) {
            this.watermark = watermark;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class FacebookMessengerReaction {
        private String senderId;
        private String recipientId;
        private String messageId;
        private String action;
        private String emoji;
        private String reaction;
        private String timestamp;

        // Getters and Setters
        public String getSenderId() {
            return senderId;
        }
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        public String getRecipientId() {
            return recipientId;
        }
        public void setRecipientId(String recipientId) {
            this.recipientId = recipientId;
        }
        public String getMessageId() {
            return messageId;
        }
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        public String getAction() {
            return action;
        }
        public void setAction(String action) {
            this.action = action;
        }
        public String getEmoji() {
            return emoji;
        }
        public void setEmoji(String emoji) {
            this.emoji = emoji;
        }
        public String getReaction() {
            return reaction;
        }
        public void setReaction(String reaction) {
            this.reaction = reaction;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class FacebookMessengerInsight {
        private String metric;
        private Object value;
        private String endTime;

        // Getters and Setters
        public String getMetric() {
            return metric;
        }
        public void setMetric(String metric) {
            this.metric = metric;
        }
        public Object getValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }
        public String getEndTime() {
            return endTime;
        }
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }
}