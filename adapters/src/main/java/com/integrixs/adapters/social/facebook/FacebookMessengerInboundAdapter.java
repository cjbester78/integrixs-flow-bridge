package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.platform.events.EventType;
import com.integrixs.platform.models.Message;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.service.RateLimiterService;
import com.integrixs.shared.utils.CredentialEncryptionService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inbound adapter for Facebook Messenger Platform integration.
 * Handles webhook events, message polling, and conversation management.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "integrixs.adapters.facebook.messenger.enabled", havingValue = "true", matchIfMissing = false)
public class FacebookMessengerInboundAdapter extends AbstractSocialMediaInboundAdapter {

    private final FacebookMessengerApiConfig config;
    private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();
    private final Set<String> processedPostbacks = ConcurrentHashMap.newKeySet();
    
    @Autowired
    public FacebookMessengerInboundAdapter(
            FacebookMessengerApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService) {
        super(rateLimiterService, credentialEncryptionService);
        this.config = config;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public AdapterConfig getAdapterConfig() {
        return config;
    }

    /**
     * Polls for recent conversations and messages
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.facebook.messenger.polling.interval:60000}")
    public void pollMessages() {
        if (!config.getPollingConfig().isEnabled()) {
            return;
        }

        try {
            log.debug("Polling Facebook Messenger messages");
            pollConversations();
        } catch (Exception e) {
            log.error("Error polling Facebook Messenger messages", e);
        }
    }

    /**
     * Polls for message insights and analytics
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.facebook.messenger.polling.insights-interval:3600000}")
    public void pollInsights() {
        if (!config.getFeatures().isEnableMessageInsights()) {
            return;
        }

        try {
            log.debug("Polling Facebook Messenger insights");
            fetchInsights();
        } catch (Exception e) {
            log.error("Error polling Facebook Messenger insights", e);
        }
    }

    private void pollConversations() throws Exception {
        String url = config.getApiUrl() + "/" + config.getPageId() + "/conversations";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getDecryptedCredential("pageAccessToken"));
        params.put("fields", "id,participants,updated_time,messages{id,from,to,message,attachments,created_time}");
        params.put("limit", "25");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if (responseData.containsKey("data")) {
            List<Map<String, Object>> conversations = (List<Map<String, Object>>) responseData.get("data");
            for (Map<String, Object> conversation : conversations) {
                processConversation(conversation);
            }
        }

        // Handle pagination
        handlePagination(responseData, this::pollConversations);
    }

    private void processConversation(Map<String, Object> conversation) {
        String conversationId = (String) conversation.get("id");
        
        if (conversation.containsKey("messages")) {
            Map<String, Object> messagesData = (Map<String, Object>) conversation.get("messages");
            if (messagesData.containsKey("data")) {
                List<Map<String, Object>> messages = (List<Map<String, Object>>) messagesData.get("data");
                for (Map<String, Object> message : messages) {
                    processMessage(message, conversationId);
                }
            }
        }
    }

    private void processMessage(Map<String, Object> messageData, String conversationId) {
        String messageId = (String) messageData.get("id");
        
        if (!processedMessages.add(messageId)) {
            return; // Already processed
        }

        FacebookMessengerMessage message = FacebookMessengerMessage.builder()
                .messageId(messageId)
                .conversationId(conversationId)
                .from(extractUserInfo((Map<String, Object>) messageData.get("from")))
                .to(extractUserInfo((Map<String, Object>) messageData.get("to")))
                .text((String) messageData.get("message"))
                .attachments(extractAttachments((List<Map<String, Object>>) messageData.get("attachments")))
                .timestamp((String) messageData.get("created_time"))
                .build();

        publishMessage("messenger.message.received", message);
    }

    private void fetchInsights() throws Exception {
        String url = config.getApiUrl() + "/" + config.getPageId() + "/insights";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getDecryptedCredential("pageAccessToken"));
        params.put("metric", String.join(",", Arrays.asList(
                "page_messages_total_messaging_connections",
                "page_messages_new_conversations_unique",
                "page_messages_blocked_conversations_unique",
                "page_messages_reported_conversations_unique"
        )));
        params.put("period", "day");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if (responseData.containsKey("data")) {
            List<Map<String, Object>> insights = (List<Map<String, Object>>) responseData.get("data");
            processInsights(insights);
        }
    }

    private void processInsights(List<Map<String, Object>> insights) {
        for (Map<String, Object> insight : insights) {
            String metricName = (String) insight.get("name");
            List<Map<String, Object>> values = (List<Map<String, Object>>) insight.get("values");
            
            for (Map<String, Object> value : values) {
                FacebookMessengerInsight insightData = FacebookMessengerInsight.builder()
                        .metric(metricName)
                        .value(value.get("value"))
                        .endTime((String) value.get("end_time"))
                        .build();

                publishMessage("messenger.insights.received", insightData);
            }
        }
    }

    @Override
    public void processWebhookEvent(Map<String, Object> event) {
        String object = (String) event.get("object");
        
        if ("page".equals(object)) {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) event.get("entry");
            for (Map<String, Object> entry : entries) {
                processPageEntry(entry);
            }
        }
    }

    private void processPageEntry(Map<String, Object> entry) {
        String pageId = String.valueOf(entry.get("id"));
        Long timestamp = ((Number) entry.get("time")).longValue();
        
        if (entry.containsKey("messaging")) {
            List<Map<String, Object>> messagingEvents = (List<Map<String, Object>>) entry.get("messaging");
            for (Map<String, Object> messagingEvent : messagingEvents) {
                processMessagingEvent(messagingEvent, pageId, timestamp);
            }
        }
    }

    private void processMessagingEvent(Map<String, Object> event, String pageId, Long timestamp) {
        Map<String, Object> sender = (Map<String, Object>) event.get("sender");
        Map<String, Object> recipient = (Map<String, Object>) event.get("recipient");
        
        if (event.containsKey("message")) {
            processWebhookMessage(event, sender, recipient, timestamp);
        } else if (event.containsKey("postback")) {
            processWebhookPostback(event, sender, recipient, timestamp);
        } else if (event.containsKey("referral")) {
            processWebhookReferral(event, sender, recipient, timestamp);
        } else if (event.containsKey("optin")) {
            processWebhookOptin(event, sender, recipient, timestamp);
        } else if (event.containsKey("delivery")) {
            processWebhookDelivery(event, sender, recipient, timestamp);
        } else if (event.containsKey("read")) {
            processWebhookRead(event, sender, recipient, timestamp);
        } else if (event.containsKey("reaction")) {
            processWebhookReaction(event, sender, recipient, timestamp);
        }
    }

    private void processWebhookMessage(Map<String, Object> event, Map<String, Object> sender, 
                                       Map<String, Object> recipient, Long timestamp) {
        Map<String, Object> messageData = (Map<String, Object>) event.get("message");
        String messageId = (String) messageData.get("mid");
        
        if (!processedMessages.add(messageId)) {
            return; // Already processed
        }

        FacebookMessengerMessage message = FacebookMessengerMessage.builder()
                .messageId(messageId)
                .from(extractUserInfo(sender))
                .to(extractUserInfo(recipient))
                .text((String) messageData.get("text"))
                .attachments(extractAttachments((List<Map<String, Object>>) messageData.get("attachments")))
                .quickReplies(extractQuickReplies((List<Map<String, Object>>) messageData.get("quick_replies")))
                .replyTo((String) messageData.get("reply_to"))
                .isEcho(Boolean.TRUE.equals(messageData.get("is_echo")))
                .timestamp(String.valueOf(timestamp))
                .build();

        publishMessage("messenger.message.webhook", message);
    }

    private void processWebhookPostback(Map<String, Object> event, Map<String, Object> sender,
                                        Map<String, Object> recipient, Long timestamp) {
        Map<String, Object> postbackData = (Map<String, Object>) event.get("postback");
        String payload = (String) postbackData.get("payload");
        String postbackId = sender.get("id") + "_" + payload + "_" + timestamp;
        
        if (!processedPostbacks.add(postbackId)) {
            return; // Already processed
        }

        FacebookMessengerPostback postback = FacebookMessengerPostback.builder()
                .senderId((String) sender.get("id"))
                .recipientId((String) recipient.get("id"))
                .payload(payload)
                .title((String) postbackData.get("title"))
                .referral(extractReferral((Map<String, Object>) postbackData.get("referral")))
                .timestamp(String.valueOf(timestamp))
                .build();

        publishMessage("messenger.postback.received", postback);
    }

    private void processWebhookReferral(Map<String, Object> event, Map<String, Object> sender,
                                        Map<String, Object> recipient, Long timestamp) {
        Map<String, Object> referralData = (Map<String, Object>) event.get("referral");
        
        FacebookMessengerReferral referral = extractReferral(referralData);
        referral.setSenderId((String) sender.get("id"));
        referral.setRecipientId((String) recipient.get("id"));
        referral.setTimestamp(String.valueOf(timestamp));

        publishMessage("messenger.referral.received", referral);
    }

    private void processWebhookOptin(Map<String, Object> event, Map<String, Object> sender,
                                     Map<String, Object> recipient, Long timestamp) {
        Map<String, Object> optinData = (Map<String, Object>) event.get("optin");
        
        FacebookMessengerOptin optin = FacebookMessengerOptin.builder()
                .senderId((String) sender.get("id"))
                .recipientId((String) recipient.get("id"))
                .ref((String) optinData.get("ref"))
                .userRef((String) optinData.get("user_ref"))
                .timestamp(String.valueOf(timestamp))
                .build();

        publishMessage("messenger.optin.received", optin);
    }

    private void processWebhookDelivery(Map<String, Object> event, Map<String, Object> sender,
                                        Map<String, Object> recipient, Long timestamp) {
        Map<String, Object> deliveryData = (Map<String, Object>) event.get("delivery");
        
        FacebookMessengerDelivery delivery = FacebookMessengerDelivery.builder()
                .senderId((String) sender.get("id"))
                .recipientId((String) recipient.get("id"))
                .messageIds((List<String>) deliveryData.get("mids"))
                .watermark(((Number) deliveryData.get("watermark")).longValue())
                .timestamp(String.valueOf(timestamp))
                .build();

        publishMessage("messenger.delivery.received", delivery);
    }

    private void processWebhookRead(Map<String, Object> event, Map<String, Object> sender,
                                    Map<String, Object> recipient, Long timestamp) {
        Map<String, Object> readData = (Map<String, Object>) event.get("read");
        
        FacebookMessengerRead read = FacebookMessengerRead.builder()
                .senderId((String) sender.get("id"))
                .recipientId((String) recipient.get("id"))
                .watermark(((Number) readData.get("watermark")).longValue())
                .timestamp(String.valueOf(timestamp))
                .build();

        publishMessage("messenger.read.received", read);
    }

    private void processWebhookReaction(Map<String, Object> event, Map<String, Object> sender,
                                        Map<String, Object> recipient, Long timestamp) {
        Map<String, Object> reactionData = (Map<String, Object>) event.get("reaction");
        
        FacebookMessengerReaction reaction = FacebookMessengerReaction.builder()
                .senderId((String) sender.get("id"))
                .recipientId((String) recipient.get("id"))
                .messageId((String) reactionData.get("mid"))
                .action((String) reactionData.get("action"))
                .emoji((String) reactionData.get("emoji"))
                .reaction((String) reactionData.get("reaction"))
                .timestamp(String.valueOf(timestamp))
                .build();

        publishMessage("messenger.reaction.received", reaction);
    }

    @Override
    public boolean verifyWebhookSignature(String signature, String payload) {
        if (!StringUtils.hasText(signature) || !signature.startsWith("sha256=")) {
            return false;
        }

        try {
            String expectedSignature = signature.substring(7); // Remove "sha256=" prefix
            String appSecret = getDecryptedCredential("appSecret");
            
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            String calculatedSignature = bytesToHex(hash);
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    calculatedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    @Override
    protected String getAdapterType() {
        return "FACEBOOK_MESSENGER";
    }

    @Override
    protected List<EventType> getSupportedEventTypes() {
        return Arrays.asList(
                EventType.SOCIAL_MEDIA_MESSAGE,
                EventType.SOCIAL_MEDIA_POSTBACK,
                EventType.SOCIAL_MEDIA_REFERRAL,
                EventType.SOCIAL_MEDIA_OPTIN,
                EventType.SOCIAL_MEDIA_DELIVERY,
                EventType.SOCIAL_MEDIA_READ,
                EventType.SOCIAL_MEDIA_REACTION,
                EventType.SOCIAL_MEDIA_INSIGHTS
        );
    }

    private FacebookMessengerUser extractUserInfo(Map<String, Object> user) {
        if (user == null) {
            return null;
        }
        return FacebookMessengerUser.builder()
                .id((String) user.get("id"))
                .name((String) user.get("name"))
                .build();
    }

    private List<FacebookMessengerAttachment> extractAttachments(List<Map<String, Object>> attachmentsList) {
        if (attachmentsList == null || attachmentsList.isEmpty()) {
            return new ArrayList<>();
        }

        List<FacebookMessengerAttachment> attachments = new ArrayList<>();
        for (Map<String, Object> attachmentData : attachmentsList) {
            FacebookMessengerAttachment attachment = FacebookMessengerAttachment.builder()
                    .type((String) attachmentData.get("type"))
                    .payload(attachmentData.get("payload"))
                    .build();
            attachments.add(attachment);
        }
        return attachments;
    }

    private List<String> extractQuickReplies(List<Map<String, Object>> quickRepliesList) {
        if (quickRepliesList == null || quickRepliesList.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> quickReplies = new ArrayList<>();
        for (Map<String, Object> quickReply : quickRepliesList) {
            String payload = (String) quickReply.get("payload");
            if (payload != null) {
                quickReplies.add(payload);
            }
        }
        return quickReplies;
    }

    private FacebookMessengerReferral extractReferral(Map<String, Object> referralData) {
        if (referralData == null) {
            return null;
        }

        return FacebookMessengerReferral.builder()
                .ref((String) referralData.get("ref"))
                .source((String) referralData.get("source"))
                .type((String) referralData.get("type"))
                .adId((String) referralData.get("ad_id"))
                .build();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Data classes for Facebook Messenger entities
    @Data
    @lombok.Builder
    public static class FacebookMessengerMessage {
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
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerUser {
        private String id;
        private String name;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerAttachment {
        private String type;
        private Object payload;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerPostback {
        private String senderId;
        private String recipientId;
        private String payload;
        private String title;
        private FacebookMessengerReferral referral;
        private String timestamp;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerReferral {
        private String ref;
        private String source;
        private String type;
        private String adId;
        private String senderId;
        private String recipientId;
        private String timestamp;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerOptin {
        private String senderId;
        private String recipientId;
        private String ref;
        private String userRef;
        private String timestamp;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerDelivery {
        private String senderId;
        private String recipientId;
        private List<String> messageIds;
        private Long watermark;
        private String timestamp;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerRead {
        private String senderId;
        private String recipientId;
        private Long watermark;
        private String timestamp;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerReaction {
        private String senderId;
        private String recipientId;
        private String messageId;
        private String action;
        private String emoji;
        private String reaction;
        private String timestamp;
    }

    @Data
    @lombok.Builder
    public static class FacebookMessengerInsight {
        private String metric;
        private Object value;
        private String endTime;
    }
}