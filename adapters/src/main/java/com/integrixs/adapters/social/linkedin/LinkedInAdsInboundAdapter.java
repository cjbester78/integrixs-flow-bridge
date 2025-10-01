package com.integrixs.adapters.social.linkedin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.linkedin.LinkedInAdsApiConfig;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.services.TokenRefreshService;
import com.integrixs.shared.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;

@Component("linkedInAdsInboundAdapter")
public class LinkedInAdsInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(LinkedInAdsInboundAdapter.class);

    @Value("${integrix.adapters.linkedin.ads.rest-path:/rest}")
    private String restPath;

    @Value("${integrix.adapters.linkedin.ads.ad-analytics-endpoint:/adAnalytics}")
    private String adAnalyticsEndpoint;

    @Value("${integrix.adapters.linkedin.ads.ad-campaign-groups-endpoint:/adCampaignGroups}")
    private String adCampaignGroupsEndpoint;

    @Value("${integrix.adapters.linkedin.ads.ad-creatives-endpoint:/adCreatives}")
    private String adCreativesEndpoint;

    @Value("${integrix.adapters.linkedin.ads.dmp-segments-endpoint:/dmpSegments}")
    private String dmpSegmentsEndpoint;

    @Value("${integrix.adapters.linkedin.ads.conversions-endpoint:/conversions}")
    private String conversionsEndpoint;

    @Value("${integrix.adapters.linkedin.ads.ad-campaigns-endpoint:/adCampaigns}")
    private String adCampaignsEndpoint;

    @Value("${integrix.adapters.linkedin.ads.lead-gen-form-responses-endpoint:/leadGenerationFormResponses}")
    private String leadGenFormResponsesEndpoint;

    @Value("${integrix.adapters.linkedin.ads.ad-accounts-endpoint:/adAccounts}")
    private String adAccountsEndpoint;

    // API base URLs are configured in application.yml
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();

    private final RateLimiterService rateLimiterService;
    private final CredentialEncryptionService credentialEncryptionService;
    private final TokenRefreshService tokenRefreshService;
    private LinkedInAdsApiConfig config;
    private volatile boolean isListening = false;

    @Autowired
    public LinkedInAdsInboundAdapter(
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            TokenRefreshService tokenRefreshService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.tokenRefreshService = tokenRefreshService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void startListening() throws AdapterException {
        if(config == null || config.getAdAccountId() == null) {
            throw new AdapterException("LinkedIn Ads configuration is invalid");
        }

        log.info("Starting LinkedIn Ads API inbound adapter for account: {}", config.getAdAccountId());
        isListening = true;

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

    public void stopListening() {
        log.info("Stopping LinkedIn Ads API inbound adapter");
        isListening = false;
    }

    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.NEW);

            JsonNode dataNode = objectMapper.readTree(data);

            switch(type) {
                case "CAMPAIGN_PERFORMANCE":
                    return processCampaignPerformance(dataNode);
                case "AD_PERFORMANCE":
                    return processAdPerformance(dataNode);
                case "CREATIVE_PERFORMANCE":
                    return processCreativePerformance(dataNode);
                case "AUDIENCE_INSIGHTS":
                    return processAudienceInsights(dataNode);
                case "CONVERSION_EVENT":
                    return processConversionEvent(dataNode);
                case "BUDGET_ALERT":
                    return processBudgetAlert(dataNode);
                case "LEAD_GEN_FORM":
                    return processLeadGenForm(dataNode);
                case "APPROVAL_STATUS":
                    return processApprovalStatus(dataNode);
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "linkedin_ads"));
                    return message;
            }
        } catch(Exception e) {
            log.error("Error processing LinkedIn Ads inbound data", e);
            // Return a failed message instead of throwing exception
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setCorrelationId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setStatus(MessageStatus.FAILED);
            errorMessage.setPayload(data);
            errorMessage.setHeaders(Map.of("type", type, "source", "linkedin_ads", "error", e.getMessage()));
            return errorMessage;
        }
    }

    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        // LinkedIn Ads primarily uses polling, not webhooks
        return null;
    }

    // Scheduled polling methods
    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.polling-intervals.campaign:300000}") // 5 minutes
    private void pollCampaignPerformance() {
        if(!isListening || !config.isEnableAnalytics()) return;

        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);

            String url = config.getApiBaseUrl() + restPath + adAnalyticsEndpoint;

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
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CAMPAIGN_PERFORMANCE");
                lastPollTime.put("campaigns", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling campaign performance", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.polling-intervals.ad-group:600000}") // 10 minutes
    private void pollAdGroupPerformance() {
        if(!isListening || !config.isEnableAnalytics()) return;

        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);

            String url = config.getApiBaseUrl() + restPath + adCampaignGroupsEndpoint;

            Map<String, String> params = new HashMap<>();
            params.put("q", "search");
            params.put("search.account.values[0]", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("search.status.values[0]", "ACTIVE");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode groups = objectMapper.readTree(response.getBody());

                // Get analytics for each ad group
                if(groups.has("elements")) {
                    for(JsonNode group : groups.get("elements")) {
                        pollAdGroupAnalytics(group.get("id").asText());
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error polling ad group performance", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.polling-intervals.creative:900000}") // 15 minutes
    private void pollCreativePerformance() {
        if(!isListening || !config.isEnableAnalytics()) return;

        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);

            String url = config.getApiBaseUrl() + restPath + adCreativesEndpoint;

            Map<String, String> params = new HashMap<>();
            params.put("q", "search");
            params.put("search.account.values[0]", "urn:li:sponsoredAccount:" + config.getAdAccountId());

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CREATIVE_PERFORMANCE");
            }
        } catch(Exception e) {
            log.error("Error polling creative performance", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.polling-intervals.audience:3600000}") // 1 hour
    private void pollAudienceInsights() {
        if(!isListening || !config.isEnableAudienceTargeting()) return;

        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);

            // Poll matched audiences
            String url = config.getApiBaseUrl() + restPath + dmpSegmentsEndpoint;

            Map<String, String> params = new HashMap<>();
            params.put("q", "account");
            params.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AUDIENCE_INSIGHTS");
            }
        } catch(Exception e) {
            log.error("Error polling audience insights", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.polling-intervals.conversion:1800000}") // 30 minutes
    private void pollConversionEvents() {
        if(!isListening || !config.isEnableConversionTracking()) return;

        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);

            String url = config.getApiBaseUrl() + restPath + conversionsEndpoint;

            Map<String, String> params = new HashMap<>();
            params.put("q", "account");
            params.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("start", String.valueOf(lastPollTime.getOrDefault("conversions",
                LocalDateTime.now().minusDays(1)).toEpochSecond(ZoneOffset.UTC) * 1000));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CONVERSION_EVENT");
                lastPollTime.put("conversions", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling conversion events", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.polling-intervals.budget-check:600000}") // 10 minutes
    private void checkBudgetAlerts() {
        if(!isListening || !config.isEnableBudgetManagement()) return;

        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);

            // Check campaign budgets
            String url = config.getApiBaseUrl() + restPath + adCampaignsEndpoint;

            Map<String, String> params = new HashMap<>();
            params.put("q", "search");
            params.put("search.account.values[0]", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("search.status.values[0]", "ACTIVE");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode campaigns = objectMapper.readTree(response.getBody());
                checkBudgetThresholds(campaigns);
            }
        } catch(Exception e) {
            log.error("Error checking budget alerts", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.linkedin.ads.polling-intervals.lead-gen:300000}") // 5 minutes
    private void pollLeadGenForms() {
        if(!isListening || !config.isEnableLeadGenForms()) return;

        try {
            rateLimiterService.acquire("linkedin_ads_api", 1);

            String url = config.getApiBaseUrl() + restPath + leadGenFormResponsesEndpoint;

            Map<String, String> params = new HashMap<>();
            params.put("q", "account");
            params.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
            params.put("submittedAfter", String.valueOf(
                lastPollTime.getOrDefault("leadgen", LocalDateTime.now().minusHours(1))
                    .toEpochSecond(ZoneOffset.UTC) * 1000));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "LEAD_GEN_FORM");
                lastPollTime.put("leadgen", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling lead gen forms", e);
        }
    }

    // Process different types of data
    private MessageDTO processCampaignPerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CAMPAIGN_PERFORMANCE");
        headers.put("source", "linkedin_ads");
        headers.put("account_id", config.getAdAccountId());

        if(data.has("elements") && data.get("elements").isArray()) {
            ArrayNode campaigns = (ArrayNode) data.get("elements");

            // Calculate aggregate metrics
            double totalSpend = 0;
            long totalImpressions = 0;
            long totalClicks = 0;
            long totalConversions = 0;

            for(JsonNode campaign : campaigns) {
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

    private MessageDTO processAdPerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_PERFORMANCE");
        headers.put("source", "linkedin_ads");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processCreativePerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CREATIVE_PERFORMANCE");
        headers.put("source", "linkedin_ads");

        if(data.has("elements")) {
            headers.put("creative_count", data.get("elements").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAudienceInsights(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AUDIENCE_INSIGHTS");
        headers.put("source", "linkedin_ads");

        if(data.has("elements")) {
            headers.put("audience_count", data.get("elements").size());

            // Calculate total audience size
            long totalAudienceSize = 0;
            for(JsonNode audience : data.get("elements")) {
                totalAudienceSize += audience.path("size").asLong(0);
            }
            headers.put("total_audience_size", totalAudienceSize);
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processConversionEvent(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CONVERSION_EVENT");
        headers.put("source", "linkedin_ads");

        if(data.has("elements")) {
            headers.put("conversion_count", data.get("elements").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processBudgetAlert(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "BUDGET_ALERT");
        headers.put("source", "linkedin_ads");
        headers.put("alert_level", config.getDefaultAlertLevel());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processLeadGenForm(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "LEAD_GEN_FORM");
        headers.put("source", "linkedin_ads");

        if(data.has("elements")) {
            headers.put("lead_count", data.get("elements").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processApprovalStatus(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

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
        headers.set("LinkedIn-Version", config.getLinkedInApiVersion());
        headers.set("X-Restli-Protocol-Version", config.getRestliProtocolVersion());

        StringBuilder urlBuilder = new StringBuilder(url);
        if(!params.isEmpty()) {
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
            if(config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getClientId(),
                    config.getClientSecret(),
                    config.getRefreshToken(),
                    config.getTokenUrl()
               );
            }
        } catch(Exception e) {
            log.error("Error refreshing LinkedIn access token", e);
        }
    }

    private void pollAdGroupAnalytics(String adGroupId) {
        try {
            String url = config.getApiBaseUrl() + restPath + adAnalyticsEndpoint;

            Map<String, String> params = new HashMap<>();
            params.put("q", "analytics");
            params.put("pivot", "CREATIVE");
            params.put("campaigns", adGroupId);
            params.put("fields", "impressions,clicks,costInLocalCurrency");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AD_PERFORMANCE");
            }
        } catch(Exception e) {
            log.error("Error polling ad group analytics", e);
        }
    }

    private void checkBudgetThresholds(JsonNode campaigns) {
        if(campaigns.has("elements")) {
            for(JsonNode campaign : campaigns.get("elements")) {
                double dailyBudget = campaign.path("dailyBudget").path("amount").asDouble(0);
                double totalBudget = campaign.path("totalBudget").path("amount").asDouble(0);

                // Get spend for today
                // This would require another API call to get today's spend
                // For now, we'll create an alert structure
                if(dailyBudget > 0 || totalBudget > 0) {
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

    public void setConfiguration(LinkedInAdsApiConfig config) {
        this.config = config;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return List.of(
            "CAMPAIGN_PERFORMANCE",
            "AD_PERFORMANCE",
            "CREATIVE_PERFORMANCE",
            "AUDIENCE_INSIGHTS",
            "CONVERSION_EVENT",
            "BUDGET_ALERT",
            "LEAD_GEN_FORM",
            "APPROVAL_STATUS"
        );
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("adAccountId", config.getAdAccountId());
            configMap.put("organizationId", config.getOrganizationId());
            configMap.put("clientId", config.getClientId());
            configMap.put("clientSecret", config.getClientSecret());
            configMap.put("accessToken", config.getAccessToken());
            configMap.put("refreshToken", config.getRefreshToken());
            configMap.put("enableAnalytics", config.isEnableAnalytics());
            configMap.put("enableAudienceTargeting", config.isEnableAudienceTargeting());
            configMap.put("enableConversionTracking", config.isEnableConversionTracking());
            configMap.put("enableBudgetManagement", config.isEnableBudgetManagement());
            configMap.put("enableLeadGenForms", config.isEnableLeadGenForms());
        }
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        log.debug("Initializing LinkedIn Ads inbound adapter");
        // Initialization is handled in startListening method
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.debug("Destroying LinkedIn Ads inbound adapter");
        stopListening();
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Inbound adapter doesn't send data, it receives it
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        if (!isConfigValid()) {
            return AdapterResult.failure("LinkedIn Ads configuration is invalid");
        }

        try {
            // Test connection by making a simple API call
            String url = config.getApiBaseUrl() + restPath + adAccountsEndpoint + "/" + config.getAdAccountId();

            Map<String, String> params = new HashMap<>();
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "LinkedIn Ads API connection successful");
            } else {
                return AdapterResult.failure("LinkedIn Ads API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error testing LinkedIn Ads connection", e);
            return AdapterResult.failure("Failed to test LinkedIn Ads connection: " + e.getMessage());
        }
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        return executeTimedOperation("send", () -> doSend(payload, headers));
    }
}
