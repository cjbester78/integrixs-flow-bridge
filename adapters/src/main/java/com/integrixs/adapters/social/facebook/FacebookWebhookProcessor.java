package com.integrixs.adapters.social.facebook;

import com.integrixs.shared.dto.FlowMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Processes Facebook webhook events
 */
@Slf4j
@Component
public class FacebookWebhookProcessor {
    
    // Store events by flow ID for processing
    private final Map<String, Queue<FlowMessage>> eventQueues = new ConcurrentHashMap<>();
    
    /**
     * Process incoming webhook event
     */
    public void processWebhookEvent(Map<String, Object> event, String flowId) {
        try {
            FlowMessage message = convertWebhookToFlowMessage(event);
            
            Queue<FlowMessage> queue = eventQueues.computeIfAbsent(flowId, k -> new ConcurrentLinkedQueue<>());
            queue.offer(message);
            
            log.debug("Queued webhook event for flow: {}", flowId);
            
        } catch (Exception e) {
            log.error("Error processing webhook event", e);
        }
    }
    
    /**
     * Get next event for a flow
     */
    public FlowMessage getNextEvent(String flowId) {
        Queue<FlowMessage> queue = eventQueues.get(flowId);
        if (queue != null) {
            return queue.poll();
        }
        return null;
    }
    
    /**
     * Verify webhook challenge
     */
    public boolean verifyWebhookChallenge(String mode, String token, String expectedToken) {
        return "subscribe".equals(mode) && expectedToken.equals(token);
    }
    
    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String signature, String payload, String appSecret) {
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        
        try {
            String expectedSignature = calculateHmacSha256(payload, appSecret);
            return signature.substring(7).equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    /**
     * Convert webhook event to FlowMessage
     */
    private FlowMessage convertWebhookToFlowMessage(Map<String, Object> event) {
        FlowMessage message = new FlowMessage();
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        
        Map<String, String> headers = new HashMap<>();
        headers.put("source", "facebook-webhook");
        
        // Extract webhook type
        String object = (String) event.get("object");
        headers.put("webhookObject", object);
        
        if ("page".equals(object)) {
            processPageWebhookEvent(event, message, headers);
        } else if ("instagram".equals(object)) {
            headers.put("platform", "instagram");
        } else if ("whatsapp_business_account".equals(object)) {
            headers.put("platform", "whatsapp");
        }
        
        message.setHeaders(headers);
        
        // Set the entire event as payload
        message.setPayload(com.fasterxml.jackson.databind.ObjectMapperBuilder.create().build()
            .writeValueAsString(event));
        
        return message;
    }
    
    /**
     * Process page-specific webhook event
     */
    private void processPageWebhookEvent(Map<String, Object> event, FlowMessage message, Map<String, String> headers) {
        try {
            java.util.List<Map<String, Object>> entries = (java.util.List<Map<String, Object>>) event.get("entry");
            if (entries != null && !entries.isEmpty()) {
                Map<String, Object> entry = entries.get(0);
                headers.put("pageId", String.valueOf(entry.get("id")));
                
                java.util.List<Map<String, Object>> changes = (java.util.List<Map<String, Object>>) entry.get("changes");
                if (changes != null && !changes.isEmpty()) {
                    Map<String, Object> change = changes.get(0);
                    String field = (String) change.get("field");
                    headers.put("changeType", field);
                    
                    // Set specific headers based on change type
                    Map<String, Object> value = (Map<String, Object>) change.get("value");
                    switch (field) {
                        case "feed":
                            headers.put("eventType", "post");
                            if (value.containsKey("post_id")) {
                                headers.put("postId", String.valueOf(value.get("post_id")));
                            }
                            break;
                            
                        case "comments":
                            headers.put("eventType", "comment");
                            if (value.containsKey("comment_id")) {
                                headers.put("commentId", String.valueOf(value.get("comment_id")));
                            }
                            break;
                            
                        case "messages":
                            headers.put("eventType", "message");
                            break;
                            
                        case "reactions":
                            headers.put("eventType", "reaction");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing page webhook event", e);
        }
    }
    
    /**
     * Calculate HMAC SHA256 signature
     */
    private String calculateHmacSha256(String data, String secret) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
            secret.getBytes("UTF-8"), "HmacSHA256"
        );
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes("UTF-8"));
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}