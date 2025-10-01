package com.integrixs.adapters.social.tiktok;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.tiktok.TikTokBusinessApiConfig.*;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component("tikTokBusinessInboundAdapter")
public class TikTokBusinessInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TikTokBusinessInboundAdapter.class);


    private static final String TIKTOK_API_BASE = "https://business-api.tiktok.com/open_api/v1.3";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private final TikTokBusinessApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final OAuth2TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;
    private boolean isListening = false;

    @Autowired
    public TikTokBusinessInboundAdapter(
            TikTokBusinessApiConfig config,
            RateLimiterService rateLimiterService,
            OAuth2TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super();
        this.config = config;
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList("CAMPAIGN", "AD_GROUP", "AD", "CREATIVE", "REPORT", "CONVERSION", "PIXEL_EVENT", "AUDIENCE");
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("appId", config.getAppId());
        configMap.put("appSecret", config.getAppSecret());
        configMap.put("advertiserId", config.getAdvertiserId());
        configMap.put("businessId", config.getBusinessId());
        configMap.put("features", config.getFeatures());
        configMap.put("limits", config.getLimits());
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("TikTok Business configuration is invalid");
        }

        log.info("Starting TikTok Business inbound adapter for advertiser: {}", config.getAdvertiserId());

        // Refresh access token if needed
        refreshAccessTokenIfNeeded();

        isListening = true;

        // Initialize scheduled polling based on enabled features
        if(config.getFeatures().isEnableCampaignManagement()) {
            scheduleCampaignPolling();
        }
        if(config.getFeatures().isEnableReporting()) {
            scheduleReportPolling();
        }
        if(config.getFeatures().isEnableCreativeManagement()) {
            scheduleCreativePolling();
        }
        if(config.getFeatures().isEnableConversionTracking()) {
            scheduleConversionPolling();
        }
        if(config.getFeatures().isEnablePixelTracking()) {
            schedulePixelEventPolling();
        }
    }

    public void stopListening() {
        log.info("Stopping TikTok Business inbound adapter");
        isListening = false;
    }

    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.SUCCESS);

            JsonNode dataNode = objectMapper.readTree(data);

            switch(type) {
                case "CAMPAIGN":
                    message = processCampaignData(dataNode);
                    break;
                case "AD_GROUP":
                    message = processAdGroupData(dataNode);
                    break;
                case "AD":
                    message = processAdData(dataNode);
                    break;
                case "CREATIVE":
                    message = processCreativeData(dataNode);
                    break;
                case "REPORT":
                    message = processReportData(dataNode);
                    break;
                case "CONVERSION":
                    message = processConversionData(dataNode);
                    break;
                case "PIXEL_EVENT":
                    message = processPixelEventData(dataNode);
                    break;
                case "AUDIENCE":
                    message = processAudienceData(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "tiktok_business"));
            }

            return message;
        } catch(Exception e) {
            log.error("Error processing TikTok Business inbound data", e);
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setPayload("Error: " + e.getMessage());
            errorMessage.setHeaders(Map.of("error", "true", "errorMessage", e.getMessage()));
            return errorMessage;
        }
    }

    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        // TikTok Business API supports webhooks for conversion events
        try {
            MessageDTO message = new MessageDTO();
            message.setId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.SUCCESS);

            // Verify webhook signature
            if(!verifyWebhookSignature(webhookData)) {
                throw new AdapterException("Invalid webhook signature");
            }

            String eventType = (String) webhookData.get("event_type");
            Map<String, Object> headers = new HashMap<>();
            headers.put("type", "WEBHOOK_EVENT");
            headers.put("eventType", eventType);
            headers.put("source", "tiktok_business");

            // Process based on event type
            if("conversion".equals(eventType)) {
                headers.put("conversionEvent", webhookData.get("conversion_event"));
                headers.put("pixelId", webhookData.get("pixel_id"));
            } else if("ad_review".equals(eventType)) {
                headers.put("adId", webhookData.get("ad_id"));
                headers.put("reviewStatus", webhookData.get("review_status"));
            }

            message.setHeaders(headers);
            message.setPayload(objectMapper.writeValueAsString(webhookData));

            return message;
        } catch(Exception e) {
            log.error("Error processing TikTok Business webhook", e);
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setPayload("Error: " + e.getMessage());
            errorMessage.setHeaders(Map.of("error", "true", "errorMessage", e.getMessage()));
            return errorMessage;
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.business.campaignPollInterval:1800000}") // 30 minutes
    private void pollCampaigns() {
        if(!isListening || !config.getFeatures().isEnableCampaignManagement()) return;

        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = TIKTOK_API_BASE + "/campaign/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());
            params.put("page", 1);
            params.put("page_size", 100);

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CAMPAIGN");
                lastPollTime.put("campaigns", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling campaigns", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.business.reportPollInterval:7200000}") // 2 hours
    private void pollReports() {
        if(!isListening || !config.getFeatures().isEnableReporting()) return;

        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            // Account for reporting delay
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(7);

            String url = TIKTOK_API_BASE + "/report/integrated/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());
            params.put("report_type", ReportType.BASIC.name());
            params.put("dimensions", Arrays.asList("campaign_id", "day"));
            params.put("metrics", Arrays.asList("spend", "impressions", "clicks", "conversions"));
            params.put("start_date", startDate.toString());
            params.put("end_date", endDate.toString());
            params.put("page", 1);
            params.put("page_size", 1000);

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "REPORT");
                lastPollTime.put("reports", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling reports", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.business.creativePollInterval:3600000}") // 1 hour
    private void pollCreatives() {
        if(!isListening || !config.getFeatures().isEnableCreativeManagement()) return;

        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = TIKTOK_API_BASE + "/creative/info/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());
            params.put("page", 1);
            params.put("page_size", 100);

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CREATIVE");
                lastPollTime.put("creatives", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling creatives", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.business.conversionPollInterval:1800000}") // 30 minutes
    private void pollConversions() {
        if(!isListening || !config.getFeatures().isEnableConversionTracking()) return;

        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            LocalDateTime endTime = LocalDateTime.now().minusHours(config.getLimits().getReportingDelayHours());
            LocalDateTime startTime = endTime.minusHours(1);

            String url = TIKTOK_API_BASE + "/conversion/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());
            params.put("start_time", startTime.toEpochSecond(ZoneOffset.UTC));
            params.put("end_time", endTime.toEpochSecond(ZoneOffset.UTC));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CONVERSION");
                lastPollTime.put("conversions", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling conversions", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.tiktok.business.pixelPollInterval:900000}") // 15 minutes
    private void pollPixelEvents() {
        if(!isListening || !config.getFeatures().isEnablePixelTracking()) return;

        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = TIKTOK_API_BASE + "/pixel/stats/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());
            params.put("period", "LAST_7_DAYS");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "PIXEL_EVENT");
                lastPollTime.put("pixel_events", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling pixel events", e);
        }
    }

    // Process different data types
    private MessageDTO processCampaignData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CAMPAIGN_DATA");
        headers.put("source", "tiktok_business");
        headers.put("advertiserId", config.getAdvertiserId());

        if(data.has("data") && data.get("data").has("list")) {
            JsonNode campaigns = data.get("data").get("list");
            headers.put("campaignCount", campaigns.size());

            int activeCampaigns = 0;
            double totalSpend = 0;

            for(JsonNode campaign : campaigns) {
                if("ENABLE".equals(campaign.path("operation_status").asText())) {
                    activeCampaigns++;
                }
                totalSpend += campaign.path("budget_spend").asDouble(0);
            }

            headers.put("activeCampaigns", activeCampaigns);
            headers.put("totalSpend", totalSpend);
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAdGroupData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_GROUP_DATA");
        headers.put("source", "tiktok_business");
        headers.put("advertiserId", config.getAdvertiserId());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAdData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_DATA");
        headers.put("source", "tiktok_business");

        if(data.has("data") && data.get("data").has("list")) {
            JsonNode ads = data.get("data").get("list");
            headers.put("adCount", ads.size());

            Map<String, Integer> statusCounts = new HashMap<>();
            for(JsonNode ad : ads) {
                String status = ad.path("review_status").asText("UNKNOWN");
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }
            headers.put("reviewStatusCounts", statusCounts);
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processCreativeData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CREATIVE_DATA");
        headers.put("source", "tiktok_business");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processReportData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "REPORT_DATA");
        headers.put("source", "tiktok_business");
        headers.put("reportType", data.path("report_type").asText());

        if(data.has("data") && data.get("data").has("list")) {
            JsonNode reportData = data.get("data").get("list");

            double totalSpend = 0;
            long totalImpressions = 0;
            long totalClicks = 0;
            long totalConversions = 0;

            for(JsonNode row : reportData) {
                JsonNode metrics = row.get("metrics");
                if(metrics != null) {
                    totalSpend += metrics.path("spend").asDouble(0);
                    totalImpressions += metrics.path("impressions").asLong(0);
                    totalClicks += metrics.path("clicks").asLong(0);
                    totalConversions += metrics.path("conversions").asLong(0);
                }
            }

            headers.put("totalSpend", totalSpend);
            headers.put("totalImpressions", totalImpressions);
            headers.put("totalClicks", totalClicks);
            headers.put("totalConversions", totalConversions);
            headers.put("rowCount", reportData.size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processConversionData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CONVERSION_DATA");
        headers.put("source", "tiktok_business");

        if(data.has("data")) {
            headers.put("conversionCount", data.get("data").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processPixelEventData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "PIXEL_EVENT_DATA");
        headers.put("source", "tiktok_business");

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAudienceData(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AUDIENCE_DATA");
        headers.put("source", "tiktok_business");

        if(data.has("data") && data.get("data").has("list")) {
            headers.put("audienceCount", data.get("data").get("list").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    // Helper methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, Object> params, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access - Token", getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if(params != null && method == HttpMethod.GET) {
            params.forEach((key, value) -> {
                if(value instanceof List) {
                    builder.queryParam(key, String.join(",", (List<String>) value));
                } else {
                    builder.queryParam(key, value.toString());
                }
            });
        }

        HttpEntity<?> entity;
        if(body != null) {
            entity = new HttpEntity<>(body, headers);
        } else if(params != null && method != HttpMethod.GET) {
            entity = new HttpEntity<>(params, headers);
        } else {
            entity = new HttpEntity<>(headers);
        }

        return restTemplate.exchange(builder.toUriString(), method, entity, String.class);
    }

    private boolean verifyWebhookSignature(Map<String, Object> webhookData) {
        try {
            String signature = (String) webhookData.get("signature");
            String timestamp = (String) webhookData.get("timestamp");
            String nonce = (String) webhookData.get("nonce");

            if(signature == null || timestamp == null || nonce == null) {
                return false;
            }

            // Verify timestamp is within 5 minutes
            long webhookTime = Long.parseLong(timestamp);
            long currentTime = Instant.now().getEpochSecond();
            if(Math.abs(currentTime - webhookTime) > 300) {
                return false;
            }

            // Calculate signature
            String payload = objectMapper.writeValueAsString(webhookData.get("data"));
            String toSign = config.getAppSecret() + timestamp + nonce + payload;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(toSign.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(hash);

            return signature.equals(calculatedSignature);
        } catch(Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for(byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private void refreshAccessTokenIfNeeded() {
        try {
            if(config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getAppId(),
                    config.getAppSecret(),
                    config.getRefreshToken(),
                    TIKTOK_API_BASE + "/oauth2/refresh_token/"
               );
            }
        } catch(Exception e) {
            log.error("Error refreshing TikTok Business access token", e);
        }
    }

    // Scheduling initialization methods
    private void scheduleCampaignPolling() {
        log.info("Scheduled campaign polling for TikTok Business");
    }

    private void scheduleReportPolling() {
        log.info("Scheduled report polling for TikTok Business");
    }

    private void scheduleCreativePolling() {
        log.info("Scheduled creative polling for TikTok Business");
    }

    private void scheduleConversionPolling() {
        log.info("Scheduled conversion polling for TikTok Business");
    }

    private void schedulePixelEventPolling() {
        log.info("Scheduled pixel event polling for TikTok Business");
    }

    private boolean isConfigValid() {
        return config != null
            && config.getAppId() != null
            && config.getAppSecret() != null
            && config.getAdvertiserId() != null
            && config.getAccessToken() != null;
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        log.info("Initializing TikTok Business inbound adapter");
        // Initialization logic if needed
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.info("Destroying TikTok Business inbound adapter");
        // Cleanup logic if needed
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // This is an inbound adapter, so we don't typically send data
        // But we need to implement this method
        log.debug("Sending data through TikTok Business inbound adapter");
        return AdapterResult.success("Message processed successfully");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = TIKTOK_API_BASE + "/advertiser/info/";
            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_ids", Arrays.asList(config.getAdvertiserId()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success("Connection test successful");
            } else {
                return AdapterResult.failure("Connection test failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        try {
            return doSend(payload, headers);
        } catch (Exception e) {
            throw new AdapterException("Failed to send data", e);
        }
    }
}
