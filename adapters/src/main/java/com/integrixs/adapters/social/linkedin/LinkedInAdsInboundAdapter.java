package com.integrixs.adapters.social.linkedin;

import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.linkedin.LinkedInAdsApiConfig.*;
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
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component("linkedInAdsInboundAdapter")
public class LinkedInAdsInboundAdapter extends AbstractSocialMediaInboundAdapter<LinkedInAdsApiConfig> {
    
    private static final String LINKEDIN_ADS_API_BASE = "https://api.linkedin.com/v2";
    private static final String LINKEDIN_ADS_REST_BASE = "https://api.linkedin.com/rest";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    
    @Autowired
    public LinkedInAdsInboundAdapter(
            LinkedInAdsApiConfig config,
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
    public void startListening() throws AdapterException {
        if (!isConfigValid()) {
            throw new AdapterException("LinkedIn Ads configuration is invalid");
        }
        
        log.info("Starting LinkedIn Ads API inbound adapter for account: {}", config.getAdAccountId());
        
        // Refresh access token if needed
        refreshAccessTokenIfNeeded();
        
        isListening = true;
        
        // Initialize polling based on enabled features
        scheduleCampaignPerformancePolling();
        scheduleAdGroupPerformancePolling();
        scheduleCreativePerformancePolling();
        scheduleAudienceInsightsPolling();
        scheduleConversionTrackingPolling();
        scheduleBudgetAlertsPolling();
        scheduleLeadGenFormsPolling();
    }
    
    @Override
    public void stopListening() {
        log.info("Stopping LinkedIn Ads API inbound adapter");
        isListening = false;
    }
    
    @Override
    protected Message processInboundData(String data, String type) {
        try {
            Message message = new Message();
            message.setMessageId(UUID.randomUUID().toString());
            message.setTimestamp(Instant.now());
            message.setStatus(MessageStatus.RECEIVED);
            
            JsonNode dataNode = objectMapper.readTree(data);
            
            switch (type) {
                case "CAMPAIGN_PERFORMANCE":
                    message = processCampaignPerformance(dataNode);
                    break;
                case "AD_PERFORMANCE":
                    message = processAdPerformance(dataNode);
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
                case "LEAD_GEN_FORM":
                    message = processLeadGenForm(dataNode);
                    break;
                case "APPROVAL_STATUS":
                    message = processApprovalStatus(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "linkedin_ads"));
            }
            
            return message;
        } catch (Exception e) {
            log.error("Error processing LinkedIn Ads inbound data", e);
            throw new AdapterException("Failed to process inbound data", e);
        }
    }
    
    @Override
    public Message processWebhookData(Map<String, Object> webhookData) {
        // LinkedIn Ads primarily uses polling, not webhooks
        return null;
    }
    
    // Scheduled polling methods
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.campaignPollingInterval:300000}") // 5 minutes
    private void pollCampaignPerformance() {
        if (!isListening || !config.getFeatures().isEnableAnalytics()) return;
        
        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);
            
            String url = LINKEDIN_ADS_REST_BASE + "/adAnalytics";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "analytics");
            params.put("pivot", "CAMPAIGN");
            params.put("accounts", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(7);
            
            params.put("dateRange.start.year", String.valueOf(startDate.getYear()));
            params.put("dateRange.start.month", String.valueOf(startDate.getMonthValue()));
            params.put("dateRange.start.day", String.valueOf(startDate.getDayOfMonth()));
            params.put("dateRange.end.year", String.valueOf(endDate.getYear()));
            params.put("dateRange.end.month", String.valueOf(endDate.getMonthValue()));
            params.put("dateRange.end.day", String.valueOf(endDate.getDayOfMonth()));
            
            params.put("fields", "impressions,clicks,costInLocalCurrency,conversions,leads");
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CAMPAIGN_PERFORMANCE");
                lastPollTime.put("campaigns", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling campaign performance", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.adGroupPollingInterval:600000}") // 10 minutes
    private void pollAdGroupPerformance() {
        if (!isListening || !config.getFeatures().isEnableAnalytics()) return;
        
        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);
            
            String url = LINKEDIN_ADS_REST_BASE + "/adCampaignGroups";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "search");
            params.put("search.account.values[0]", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("search.status.values[0]", "ACTIVE");
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode groups = objectMapper.readTree(response.getBody());
                
                // Get analytics for each ad group
                if (groups.has("elements")) {
                    for (JsonNode group : groups.get("elements")) {
                        pollAdGroupAnalytics(group.get("id").asText());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error polling ad group performance", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.creativePollingInterval:900000}") // 15 minutes
    private void pollCreativePerformance() {
        if (!isListening || !config.getFeatures().isEnableAnalytics()) return;
        
        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);
            
            String url = LINKEDIN_ADS_REST_BASE + "/adCreatives";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "search");
            params.put("search.account.values[0]", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CREATIVE_PERFORMANCE");
            }
        } catch (Exception e) {
            log.error("Error polling creative performance", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.audiencePollingInterval:3600000}") // 1 hour
    private void pollAudienceInsights() {
        if (!isListening || !config.getFeatures().isEnableAudienceTargeting()) return;
        
        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);
            
            // Poll matched audiences
            String url = LINKEDIN_ADS_REST_BASE + "/dmpSegments";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "account");
            params.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AUDIENCE_INSIGHTS");
            }
        } catch (Exception e) {
            log.error("Error polling audience insights", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.conversionPollingInterval:1800000}") // 30 minutes
    private void pollConversionEvents() {
        if (!isListening || !config.getFeatures().isEnableConversionTracking()) return;
        
        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);
            
            String url = LINKEDIN_ADS_REST_BASE + "/conversions";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "account");
            params.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("start", String.valueOf(lastPollTime.getOrDefault("conversions", 
                LocalDateTime.now().minusDays(1)).toEpochSecond(ZoneOffset.UTC) * 1000));
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CONVERSION_EVENT");
                lastPollTime.put("conversions", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling conversion events", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.budgetCheckInterval:600000}") // 10 minutes
    private void checkBudgetAlerts() {
        if (!isListening || !config.getFeatures().isEnableBudgetManagement()) return;
        
        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);
            
            // Check campaign budgets
            String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "search");
            params.put("search.account.values[0]", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("search.status.values[0]", "ACTIVE");
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode campaigns = objectMapper.readTree(response.getBody());
                checkBudgetThresholds(campaigns);
            }
        } catch (Exception e) {
            log.error("Error checking budget alerts", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.leadGenPollingInterval:300000}") // 5 minutes
    private void pollLeadGenForms() {
        if (!isListening || !config.getFeatures().isEnableLeadGenForms()) return;
        
        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);
            
            String url = LINKEDIN_ADS_REST_BASE + "/leadGenerationFormResponses";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "account");
            params.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("submittedAfter", String.valueOf(
                lastPollTime.getOrDefault("leadgen", LocalDateTime.now().minusHours(1))
                    .toEpochSecond(ZoneOffset.UTC) * 1000));
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "LEAD_GEN_FORM");
                lastPollTime.put("leadgen", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling lead gen forms", e);
        }
    }
    
    // Process different types of data
    private Message processCampaignPerformance(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CAMPAIGN_PERFORMANCE");
        headers.put("source", "linkedin_ads");
        headers.put("account_id", config.getAdAccountId());
        
        if (data.has("elements") && data.get("elements").isArray()) {
            ArrayNode campaigns = (ArrayNode) data.get("elements");
            
            // Calculate aggregate metrics
            double totalSpend = 0;
            long totalImpressions = 0;
            long totalClicks = 0;
            long totalConversions = 0;
            
            for (JsonNode campaign : campaigns) {
                totalSpend += campaign.path("costInLocalCurrency").asDouble(0);
                totalImpressions += campaign.path("impressions").asLong(0);
                totalClicks += campaign.path("clicks").asLong(0);
                totalConversions += campaign.path("conversions").asLong(0);
            }
            
            headers.put("total_spend", totalSpend);
            headers.put("total_impressions", totalImpressions);
            headers.put("total_clicks", totalClicks);
            headers.put("total_conversions", totalConversions);
            headers.put("campaign_count", campaigns.size());
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processAdPerformance(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_PERFORMANCE");
        headers.put("source", "linkedin_ads");
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processCreativePerformance(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CREATIVE_PERFORMANCE");
        headers.put("source", "linkedin_ads");
        
        if (data.has("elements")) {
            headers.put("creative_count", data.get("elements").size());
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processAudienceInsights(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AUDIENCE_INSIGHTS");
        headers.put("source", "linkedin_ads");
        
        if (data.has("elements")) {
            headers.put("audience_count", data.get("elements").size());
            
            // Calculate total audience size
            long totalAudienceSize = 0;
            for (JsonNode audience : data.get("elements")) {
                totalAudienceSize += audience.path("size").asLong(0);
            }
            headers.put("total_audience_size", totalAudienceSize);
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processConversionEvent(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CONVERSION_EVENT");
        headers.put("source", "linkedin_ads");
        
        if (data.has("elements")) {
            headers.put("conversion_count", data.get("elements").size());
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processBudgetAlert(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "BUDGET_ALERT");
        headers.put("source", "linkedin_ads");
        headers.put("alert_level", "WARNING");
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processLeadGenForm(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "LEAD_GEN_FORM");
        headers.put("source", "linkedin_ads");
        
        if (data.has("elements")) {
            headers.put("lead_count", data.get("elements").size());
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processApprovalStatus(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "APPROVAL_STATUS");
        headers.put("source", "linkedin_ads");
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    // Helper methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("LinkedIn-Version", "202401");
        headers.set("X-Restli-Protocol-Version", "2.0.0");
        
        StringBuilder urlBuilder = new StringBuilder(url);
        if (!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) -> 
                urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);
    }
    
    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }
    
    private void refreshAccessTokenIfNeeded() {
        try {
            if (config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getClientId(),
                    config.getClientSecret(),
                    config.getRefreshToken(),
                    "https://www.linkedin.com/oauth/v2/accessToken"
                );
            }
        } catch (Exception e) {
            log.error("Error refreshing LinkedIn access token", e);
        }
    }
    
    private void pollAdGroupAnalytics(String adGroupId) {
        try {
            String url = LINKEDIN_ADS_REST_BASE + "/adAnalytics";
            
            Map<String, String> params = new HashMap<>();
            params.put("q", "analytics");
            params.put("pivot", "CREATIVE");
            params.put("campaigns", adGroupId);
            params.put("fields", "impressions,clicks,costInLocalCurrency");
            
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AD_PERFORMANCE");
            }
        } catch (Exception e) {
            log.error("Error polling ad group analytics", e);
        }
    }
    
    private void checkBudgetThresholds(JsonNode campaigns) {
        if (campaigns.has("elements")) {
            for (JsonNode campaign : campaigns.get("elements")) {
                double dailyBudget = campaign.path("dailyBudget").path("amount").asDouble(0);
                double totalBudget = campaign.path("totalBudget").path("amount").asDouble(0);
                
                // Get spend for today
                // This would require another API call to get today's spend
                // For now, we'll create an alert structure
                if (dailyBudget > 0 || totalBudget > 0) {
                    ObjectNode alert = objectMapper.createObjectNode();
                    alert.put("campaignId", campaign.get("id").asText());
                    alert.put("campaignName", campaign.path("name").asText());
                    alert.put("dailyBudget", dailyBudget);
                    alert.put("totalBudget", totalBudget);
                    alert.put("alertType", "BUDGET_CHECK");
                    
                    processInboundData(alert.toString(), "BUDGET_ALERT");
                }
            }
        }
    }
    
    private void scheduleCampaignPerformancePolling() {
        log.info("Scheduled campaign performance polling for LinkedIn Ads");
    }
    
    private void scheduleAdGroupPerformancePolling() {
        log.info("Scheduled ad group performance polling for LinkedIn Ads");
    }
    
    private void scheduleCreativePerformancePolling() {
        log.info("Scheduled creative performance polling for LinkedIn Ads");
    }
    
    private void scheduleAudienceInsightsPolling() {
        log.info("Scheduled audience insights polling for LinkedIn Ads");
    }
    
    private void scheduleConversionTrackingPolling() {
        log.info("Scheduled conversion tracking polling for LinkedIn Ads");
    }
    
    private void scheduleBudgetAlertsPolling() {
        log.info("Scheduled budget alerts polling for LinkedIn Ads");
    }
    
    private void scheduleLeadGenFormsPolling() {
        log.info("Scheduled lead gen forms polling for LinkedIn Ads");
    }
    
    private boolean isConfigValid() {
        return config != null 
            && config.getClientId() != null
            && config.getClientSecret() != null
            && config.getAccessToken() != null
            && config.getAdAccountId() != null;
    }
}