package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller to handle Facebook webhook events
 */
@RestController
@RequestMapping("/webhooks/facebook")
public class FacebookWebhookController {
    private static final Logger log = LoggerFactory.getLogger(FacebookWebhookController.class);


    @Autowired
    private FacebookWebhookProcessor webhookProcessor;

    @Autowired
    private FacebookGraphInboundAdapter inboundAdapter;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${integrixs.adapters.facebook.webhook.verify - token:default - verify - token}")
    private String verifyToken;

    @Value("${integrixs.adapters.facebook.app - secret:}")
    private String appSecret;

    /**
     * Webhook verification endpoint
     * Facebook will call this to verify the webhook URL
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        log.info("Received webhook verification request - mode: {}, token: {}", mode, token);

        if(webhookProcessor.verifyWebhookChallenge(mode, token, verifyToken)) {
            log.info("Webhook verification successful");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Webhook verification failed - invalid token");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid verification token");
        }
    }

    /**
     * Webhook event receiver endpoint
     * Facebook will POST events to this endpoint
     */
    @PostMapping
    public ResponseEntity<String> handleWebhookEvent(
            @RequestBody String payload,
            @RequestHeader(value = "X - Hub - Signature-256", required = false) String signature) {

        try {
            log.debug("Received webhook event with signature: {}", signature);

            // Verify signature if app secret is configured
            if(appSecret != null && !appSecret.isEmpty()) {
                if(!webhookProcessor.verifyWebhookSignature(signature, payload, appSecret)) {
                    log.warn("Invalid webhook signature");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
                }
            }

            // Parse the webhook payload
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            log.info("Processing webhook event - object: {}", event.get("object"));

            // Process the event
            String object = (String) event.get("object");

            // Route to appropriate flow based on webhook configuration
            String flowId = determineFlowId(object, event);

            if(flowId != null) {
                // Queue the event for processing by the inbound adapter
                webhookProcessor.processWebhookEvent(event, flowId);

                // If adapter is configured for immediate processing
                if(shouldProcessImmediately(flowId)) {
                    inboundAdapter.processWebhookEvent(event);
                }
            }

            // Facebook expects 200 OK response quickly
            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch(Exception e) {
            log.error("Error processing webhook event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing event");
        }
    }

    /**
     * Health check endpoint for webhook
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "webhook", "facebook",
            "verifyTokenConfigured", !verifyToken.equals("default - verify - token"),
            "appSecretConfigured", appSecret != null && !appSecret.isEmpty()
       );

        return ResponseEntity.ok(health);
    }

    /**
     * Determine which flow should process this webhook event
     */
    private String determineFlowId(String object, Map<String, Object> event) {
        // This would be implemented based on your flow configuration
        // For now, return a default flow ID

        // You could map different event types to different flows
        switch(object) {
            case "page":
                return "facebook - page - events";
            case "instagram":
                return "instagram - events";
            case "whatsapp_business_account":
                return "whatsapp - events";
            default:
                return "facebook - default";
        }
    }

    /**
     * Check if event should be processed immediately
     */
    private boolean shouldProcessImmediately(String flowId) {
        // This could be configured per flow
        // Some flows might want batch processing, others immediate
        return true;
    }

    /**
     * Endpoint to manually trigger webhook processing
     * Useful for testing
     */
    @PostMapping("/test")
    public ResponseEntity<String> testWebhook(@RequestBody Map<String, Object> testEvent) {
        try {
            log.info("Processing test webhook event");

            String flowId = "test - flow";
            webhookProcessor.processWebhookEvent(testEvent, flowId);

            return ResponseEntity.ok("Test event processed");

        } catch(Exception e) {
            log.error("Error processing test event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing test event");
        }
    }
}
