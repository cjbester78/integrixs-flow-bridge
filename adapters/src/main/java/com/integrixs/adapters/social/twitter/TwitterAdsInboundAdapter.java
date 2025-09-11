package com.integrixs.adapters.social.twitter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.twitter.TwitterAdsApiConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.TokenRefreshService;
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
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("twitterAdsInboundAdapter")
public class TwitterAdsInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TwitterAdsInboundAdapter.class);

    
    private static final String TWITTER_ADS_API_BASE = "https://ads-api.twitter.com/12";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private final TwitterAdsApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final CredentialEncryptionService credentialEncryptionService;
    
    @Autowired
    public TwitterAdsInboundAdapter(
            TwitterAdsApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super();
        this.config = config;
        this.rateLimiterService = rateLimiterService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void startListening() throws AdapterException {
        if (!isConfigValid()) {
            throw new AdapterException("Twitter Ads configuration is invalid");
        }
        
        log.info("Starting Twitter Ads API inbound adapter for account: {}", config.getAdsAccountId());
        isListening = true;
        
        // Schedule polling for different metrics
        scheduleCampaignPerformancePolling();
        scheduleAdGroupPerformancePolling();
        scheduleCreativePerformancePolling();
        scheduleAudienceInsightsPolling();
        scheduleConversionTrackingPolling();
        scheduleBudgetAlertsPolling();
    }
    
    @Override
    public void stopListening() {
        log.info("Stopping Twitter Ads API inbound adapter");
        isListening = false;
    }
    
    @Override
    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setMessageId(UUID.randomUUID().toString());
            message.setTimestamp(Instant.now());
            message.setStatus(MessageStatus.RECEIVED);
            
            JsonNode dataNode = objectMapper.readTree(data);
            
            switch (type) {
                case "CAMPAIGN_PERFORMANCE":
                    message = processCampaignPerformance(dataNode);
                    break;
                case "AD_GROUP_PERFORMANCE":
                    message = processAdGroupPerformance(dataNode);
                    break;
                case "CREATIVE_PERFORMANCE":
                    message = processCreativePerformance(dataNode);
                    break;
                case "AUDIENCE_INSIGHTS":
                    message = processAudienceInsights(dataNode);
                    break;
                case "CONVERSION_EVENT":
                    message = processConversionEvent(dataNode);
                    break;
                case "BUDGET_ALERT":
                    message = processBudgetAlert(dataNode);
                    break;
                case "APPROVAL_STATUS":
                    message = processApprovalStatus(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "twitter_ads"));
            }
            
            return message;
        } catch (Exception e) {
            log.error("Error processing Twitter Ads inbound data", e);
            throw new AdapterException("Failed to process inbound data", e);
        }
    }
    
    @Override
    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        // Twitter Ads API doesn't typically use webhooks, mostly polling
        // This is here for future extensibility
        return null;
    }
    
    // Scheduled polling methods
    @Scheduled(fixedDelayString = "${integrixs.adapters.twitter.ads.campaignPollingInterval:300000}") // 5 minutes
    private void pollCampaignPerformance() {
        if (!isListening || !config.getFeatures().isEnableAnalytics()) return;
        
        try {
            rateLimiterService.acquire("twitter_ads_api", 1);
            
            String url = String.format("%s/accounts/%s/stats/jobs", 
                TWITTER_ADS_API_BASE, config.getAdsAccountId());
            
            // Create stats job for campaigns
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("entity", "CAMPAIGN");
            requestBody.put("entity_ids", getAllCampaignIds());
            requestBody.put("granularity", Granularity.DAY.name());
            requestBody.putArray("metric_groups").add("ENGAGEMENT").add("BILLING").add("VIDEO");
            requestBody.put("start_time", getStartTime());
            requestBody.put("end_time", getEndTime());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
            if (response.getStatusCode().is2xxSuccessful()) {
                // Process async job results
                String jobId = extractJobId(response.getBody());
                pollJobResults(jobId, "CAMPAIGN_PERFORMANCE");
            }
        } catch (Exception e) {
            log.error("Error polling campaign performance", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.twitter.ads.adGroupPollingInterval:600000}") // 10 minutes
    private void pollAdGroupPerformance() {
        if (!isListening || !config.getFeatures().isEnableAnalytics()) return;
        
        try {
            rateLimiterService.acquire("twitter_ads_api", 1);
            
            String url = String.format("%s/accounts/%s/line_items", 
                TWITTER_ADS_API_BASE, config.getAdsAccountId());
            
            Map<String, String> params = new HashMap<>();
            params.put("count", "200");
            params.put("with_deleted", "false");
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AD_GROUP_PERFORMANCE");
            }
        } catch (Exception e) {
            log.error("Error polling ad group performance", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.twitter.ads.creativePollingInterval:900000}") // 15 minutes
    private void pollCreativePerformance() {
        if (!isListening || !config.getFeatures().isEnableAnalytics()) return;
        
        try {
            rateLimiterService.acquire("twitter_ads_api", 1);
            
            // Poll promoted tweets performance
            String url = String.format("%s/accounts/%s/promoted_tweets", 
                TWITTER_ADS_API_BASE, config.getAdsAccountId());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CREATIVE_PERFORMANCE");
            }
        } catch (Exception e) {
            log.error("Error polling creative performance", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.twitter.ads.audiencePollingInterval:3600000}") // 1 hour
    private void pollAudienceInsights() {
        if (!isListening || !config.getFeatures().isEnableAudienceTargeting()) return;
        
        try {
            rateLimiterService.acquire("twitter_ads_api", 1);
            
            // Poll custom audiences
            String url = String.format("%s/accounts/%s/custom_audiences", 
                TWITTER_ADS_API_BASE, config.getAdsAccountId());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AUDIENCE_INSIGHTS");
            }
        } catch (Exception e) {
            log.error("Error polling audience insights", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.twitter.ads.conversionPollingInterval:1800000}") // 30 minutes
    private void pollConversionEvents() {
        if (!isListening || !config.getFeatures().isEnableConversionTracking()) return;
        
        try {
            rateLimiterService.acquire("twitter_ads_api", 1);
            
            // Poll web event tags
            String url = String.format("%s/accounts/%s/web_event_tags", 
                TWITTER_ADS_API_BASE, config.getAdsAccountId());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode tags = objectMapper.readTree(response.getBody());
                
                // For each tag, get conversion events
                if (tags.has("data") && tags.get("data").isArray()) {
                    for (JsonNode tag : tags.get("data")) {
                        pollConversionEventsForTag(tag.get("id").asText());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error polling conversion events", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.twitter.ads.budgetCheckInterval:600000}") // 10 minutes
    private void checkBudgetAlerts() {
        if (!isListening || !config.getFeatures().isEnableBudgetManagement()) return;
        
        try {
            rateLimiterService.acquire("twitter_ads_api", 1);
            
            // Check funding instruments
            String url = String.format("%s/accounts/%s/funding_instruments", 
                TWITTER_ADS_API_BASE, config.getAdsAccountId());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode instruments = objectMapper.readTree(response.getBody());
                checkBudgetThresholds(instruments);
            }
        } catch (Exception e) {
            log.error("Error checking budget alerts", e);
        }
    }
    
    // Process different types of data
    private MessageDTO processCampaignPerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CAMPAIGN_PERFORMANCE");
        headers.put("source", "twitter_ads");
        headers.put("account_id", config.getAdsAccountId());
        
        if (data.has("data") && data.get("data").isArray()) {
            ArrayNode campaigns = (ArrayNode) data.get("data");
            headers.put("campaign_count", campaigns.size());
            
            // Calculate aggregate metrics
            double totalSpend = 0;
            long totalImpressions = 0;
            long totalEngagements = 0;
            
            for (JsonNode campaign : campaigns) {
                if (campaign.has("metrics")) {
                    JsonNode metrics = campaign.get("metrics");
                    totalSpend += metrics.path("billed_charge_local_micro").asDouble(0) / 1000000.0;
                    totalImpressions += metrics.path("impressions").asLong(0);
                    totalEngagements += metrics.path("engagements").asLong(0);
                }
            }
            
            headers.put("total_spend", totalSpend);
            headers.put("total_impressions", totalImpressions);
            headers.put("total_engagements", totalEngagements);
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private MessageDTO processAdGroupPerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_GROUP_PERFORMANCE");
        headers.put("source", "twitter_ads");
        headers.put("account_id", config.getAdsAccountId());
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private MessageDTO processCreativePerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CREATIVE_PERFORMANCE");
        headers.put("source", "twitter_ads");
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private MessageDTO processAudienceInsights(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AUDIENCE_INSIGHTS");
        headers.put("source", "twitter_ads");
        
        if (data.has("data") && data.get("data").isArray()) {
            headers.put("audience_count", data.get("data").size());
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private MessageDTO processConversionEvent(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CONVERSION_EVENT");
        headers.put("source", "twitter_ads");
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private MessageDTO processBudgetAlert(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "BUDGET_ALERT");
        headers.put("source", "twitter_ads");
        headers.put("alert_level", "WARNING");
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private MessageDTO processApprovalStatus(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "APPROVAL_STATUS");
        headers.put("source", "twitter_ads");
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    // Helper methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body) {
        return makeApiCall(url, method, body, null);
    }
    
    
    private String generateOAuth1Header(String url, String method) {
        // Simplified OAuth 1.0a header generation
        // In production, use a proper OAuth library
        String consumerKey = credentialEncryptionService.decrypt(config.getApiKey());
        String token = credentialEncryptionService.decrypt(config.getAccessToken());
        
        return String.format("OAuth oauth_consumer_key=\"%s\", oauth_token=\"%s\", " +
            "oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"%d\", " +
            "oauth_nonce=\"%s\", oauth_version=\"1.0\", oauth_signature=\"%s\"",
            consumerKey, token, System.currentTimeMillis() / 1000,
            UUID.randomUUID().toString(), "signature_placeholder");
    }
    
    private void scheduleCampaignPerformancePolling() {
        log.info("Scheduled campaign performance polling for Twitter Ads");
    }
    
    private void scheduleAdGroupPerformancePolling() {
        log.info("Scheduled ad group performance polling for Twitter Ads");
    }
    
    private void scheduleCreativePerformancePolling() {
        log.info("Scheduled creative performance polling for Twitter Ads");
    }
    
    private void scheduleAudienceInsightsPolling() {
        log.info("Scheduled audience insights polling for Twitter Ads");
    }
    
    private void scheduleConversionTrackingPolling() {
        log.info("Scheduled conversion tracking polling for Twitter Ads");
    }
    
    private void scheduleBudgetAlertsPolling() {
        log.info("Scheduled budget alerts polling for Twitter Ads");
    }
    
    private boolean isConfigValid() {
        return config != null 
            && config.getAdsAccountId() != null
            && config.getApiKey() != null
            && config.getApiKeySecret() != null
            && config.getAccessToken() != null
            && config.getAccessTokenSecret() != null;
    }
    
    private String getAllCampaignIds() {
        // In production, fetch actual campaign IDs
        return "";
    }
    
    private String getStartTime() {
        return LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_INSTANT);
    }
    
    private String getEndTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
    }
    
    private String extractJobId(String response) {
        try {
            JsonNode node = objectMapper.readTree(response);
            return node.path("data").path("id").asText();
        } catch (Exception e) {
            return null;
        }
    }
    
    private void pollJobResults(String jobId, String type) {
        // Implement async job polling
    }
    
    private void pollConversionEventsForTag(String tagId) {
        // Implement conversion event polling for specific tag
    }
    
    private void checkBudgetThresholds(JsonNode instruments) {
        // Check budget thresholds and generate alerts
    }
}