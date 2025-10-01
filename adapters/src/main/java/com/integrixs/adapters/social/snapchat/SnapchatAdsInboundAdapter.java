package com.integrixs.adapters.social.snapchat;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.snapchat.SnapchatAdsApiConfig.*;
import com.integrixs.adapters.social.snapchat.SnapchatAdsApiConfig.WebhookEvent;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class SnapchatAdsInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(SnapchatAdsInboundAdapter.class);


    private static final String API_BASE_URL = "https://adsapi.snapchat.com/v1";
    private static final String AUTH_URL = "https://accounts.snapchat.com/login/oauth2/access_token";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Autowired
    private SnapchatAdsApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList("campaign", "ad_squad", "creative", "audience", "pixel", "report");
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("clientId", config.getClientId());
        configMap.put("clientSecret", config.getClientSecret());
        configMap.put("adAccountId", config.getAdAccountId());
        configMap.put("accessToken", config.getAccessToken());
        configMap.put("pixelId", config.getPixelId());
        configMap.put("features", config.getFeatures());
        configMap.put("limits", config.getLimits());
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        log.info("Initializing Snapchat Ads adapter");
        // Initialization logic if needed
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.info("Destroying Snapchat Ads adapter");
        // Cleanup logic if needed
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // This is an inbound adapter, so we don't typically send data
        // But we need to implement this method
        log.debug("Sending data through Snapchat Ads adapter");
        return AdapterResult.success("Message processed successfully");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String endpoint = String.format("%s/me", API_BASE_URL);
            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET);
            if (response != null) {
                return AdapterResult.success("Connection test successful");
            } else {
                return AdapterResult.failure("Connection test failed: No response");
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

    // Campaign Management Polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.snapchat.ads.polling.campaigns:300000}")
    public void pollCampaigns() {
        if(!config.getFeatures().isEnableCampaignManagement()) {
            return;
        }

        try {
            log.debug("Polling Snapchat campaigns");
            String endpoint = String.format("%s/adaccounts/%s/campaigns",
                API_BASE_URL, config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET);
            if(response != null && response.has("campaigns")) {
                processCampaigns(response.get("campaigns"));
            }
        } catch(Exception e) {
            log.error("Error polling campaigns", e);
        }
    }

    // Ad Squad Polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.snapchat.ads.polling.adsquads:300000}")
    public void pollAdSquads() {
        if(!config.getFeatures().isEnableAdManagement()) {
            return;
        }

        try {
            log.debug("Polling Snapchat ad squads");
            String endpoint = String.format("%s/adaccounts/%s/adsquads",
                API_BASE_URL, config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET);
            if(response != null && response.has("adsquads")) {
                processAdSquads(response.get("adsquads"));
            }
        } catch(Exception e) {
            log.error("Error polling ad squads", e);
        }
    }

    // Creative Polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.snapchat.ads.polling.creatives:600000}")
    public void pollCreatives() {
        if(!config.getFeatures().isEnableCreativeManagement()) {
            return;
        }

        try {
            log.debug("Polling Snapchat creatives");
            String endpoint = String.format("%s/adaccounts/%s/creatives",
                API_BASE_URL, config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET);
            if(response != null && response.has("creatives")) {
                processCreatives(response.get("creatives"));
            }
        } catch(Exception e) {
            log.error("Error polling creatives", e);
        }
    }

    // Audience Polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.snapchat.ads.polling.audiences:3600000}")
    public void pollAudiences() {
        if(!config.getFeatures().isEnableAudienceManagement()) {
            return;
        }

        try {
            log.debug("Polling Snapchat audiences");
            String endpoint = String.format("%s/adaccounts/%s/segments",
                API_BASE_URL, config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET);
            if(response != null && response.has("segments")) {
                processAudiences(response.get("segments"));
            }
        } catch(Exception e) {
            log.error("Error polling audiences", e);
        }
    }

    // Pixel Events Polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.snapchat.ads.polling.pixels:900000}")
    public void pollPixelEvents() {
        if(!config.getFeatures().isEnablePixelTracking() || config.getPixelId() == null) {
            return;
        }

        try {
            log.debug("Polling Snapchat pixel events");
            String endpoint = String.format("%s/adaccounts/%s/pixels/%s/stats",
                API_BASE_URL, config.getAdAccountId(), config.getPixelId());

            Map<String, String> params = new HashMap<>();
            params.put("start_time", getStartTime());
            params.put("end_time", getEndTime());
            params.put("granularity", "HOUR");

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET, params);
            if(response != null && response.has("timeseries_stats")) {
                processPixelStats(response.get("timeseries_stats"));
            }
        } catch(Exception e) {
            log.error("Error polling pixel events", e);
        }
    }

    // Reporting and Analytics Polling
    @Scheduled(fixedDelayString = "${integrixs.adapters.snapchat.ads.polling.reports:1800000}")
    public void pollReports() {
        if(!config.getFeatures().isEnableReporting()) {
            return;
        }

        try {
            log.debug("Polling Snapchat ads reporting");

            // Poll different report levels
            pollCampaignReports();
            pollAdSquadReports();
            pollCreativeReports();

        } catch(Exception e) {
            log.error("Error polling reports", e);
        }
    }

    private void pollCampaignReports() {
        try {
            String endpoint = String.format("%s/adaccounts/%s/stats",
                API_BASE_URL, config.getAdAccountId());

            Map<String, String> params = new HashMap<>();
            params.put("granularity", "DAY");
            params.put("start_time", getStartTime());
            params.put("end_time", getEndTime());
            params.put("report_dimension", "campaign");
            params.put("metrics", "impressions,spend,swipe_ups,video_views");

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET, params);
            if(response != null && response.has("timeseries_stats")) {
                processCampaignReports(response.get("timeseries_stats"));
            }
        } catch(Exception e) {
            log.error("Error polling campaign reports", e);
        }
    }

    private void pollAdSquadReports() {
        try {
            String endpoint = String.format("%s/adaccounts/%s/stats",
                API_BASE_URL, config.getAdAccountId());

            Map<String, String> params = new HashMap<>();
            params.put("granularity", "HOUR");
            params.put("start_time", getStartTime());
            params.put("end_time", getEndTime());
            params.put("report_dimension", "ad_squad");
            params.put("metrics", "impressions,spend,swipe_ups,reach,frequency");

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET, params);
            if(response != null && response.has("timeseries_stats")) {
                processAdSquadReports(response.get("timeseries_stats"));
            }
        } catch(Exception e) {
            log.error("Error polling ad squad reports", e);
        }
    }

    private void pollCreativeReports() {
        try {
            String endpoint = String.format("%s/adaccounts/%s/stats",
                API_BASE_URL, config.getAdAccountId());

            Map<String, String> params = new HashMap<>();
            params.put("granularity", "DAY");
            params.put("start_time", getStartTime());
            params.put("end_time", getEndTime());
            params.put("report_dimension", "creative");
            params.put("metrics", "impressions,video_views,quartile_1,quartile_2,quartile_3");

            JsonNode response = makeAuthenticatedRequest(endpoint, HttpMethod.GET, params);
            if(response != null && response.has("timeseries_stats")) {
                processCreativeReports(response.get("timeseries_stats"));
            }
        } catch(Exception e) {
            log.error("Error polling creative reports", e);
        }
    }

    // Webhook Processing
    public void handleWebhookEvent(JsonNode event) {
        if(event == null || !event.has("event_type")) {
            log.warn("Invalid webhook event received");
            return;
        }

        String eventType = event.get("event_type").asText();
        WebhookEvent webhookEventType = WebhookEvent.valueOf(eventType.toUpperCase());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_type", eventType);
        eventData.put("timestamp", Instant.now().toString());
        eventData.put("data", event);

        try {
            MessageDTO message = new MessageDTO();
            message.setHeaders(Map.of(
                "type", "webhook_event",
                "category", webhookEventType.name(),
                "source", "snapchat_ads_webhook"
            ));
            message.setPayload(objectMapper.writeValueAsString(eventData));

            // Process the webhook event
            send(message);
        } catch (Exception ex) {
            log.error("Error processing webhook event", ex);
        }
    }

    public boolean verifyWebhookSignature(String signature, String timestamp, String body) {
        try {
            String signatureBase = timestamp + "|" + body;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                config.getClientSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
           );
            sha256Hmac.init(secretKey);

            byte[] signatureBytes = sha256Hmac.doFinal(
                signatureBase.getBytes(StandardCharsets.UTF_8)
           );

            String calculatedSignature = Base64.getEncoder().encodeToString(signatureBytes);
            return calculatedSignature.equals(signature);
        } catch(NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    // Processing Methods
    private void processCampaigns(JsonNode campaigns) {
        campaigns.forEach(campaign -> {
            try {
                Map<String, Object> campaignData = new HashMap<>();
                campaignData.put("id", campaign.get("id").asText());
                campaignData.put("name", campaign.get("name").asText());
                campaignData.put("status", campaign.get("status").asText());
                campaignData.put("objective", campaign.get("objective").asText());
                campaignData.put("daily_budget_micro", campaign.get("daily_budget_micro").asLong());
                campaignData.put("start_time", campaign.get("start_time").asText());

                if(campaign.has("end_time")) {
                    campaignData.put("end_time", campaign.get("end_time").asText());
                }

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "campaign",
                    "category", "campaign_data",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(campaignData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing campaign", e);
            }
        });
    }

    private void processAdSquads(JsonNode adSquads) {
        adSquads.forEach(adSquad -> {
            try {
                Map<String, Object> adSquadData = new HashMap<>();
                adSquadData.put("id", adSquad.get("id").asText());
                adSquadData.put("name", adSquad.get("name").asText());
                adSquadData.put("campaign_id", adSquad.get("campaign_id").asText());
                adSquadData.put("status", adSquad.get("status").asText());
                adSquadData.put("type", adSquad.get("type").asText());
                adSquadData.put("bid_micro", adSquad.get("bid_micro").asLong());
                adSquadData.put("daily_budget_micro", adSquad.get("daily_budget_micro").asLong());
                adSquadData.put("optimization_goal", adSquad.get("optimization_goal").asText());

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "ad_squad",
                    "category", "ad_squad_data",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(adSquadData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing ad squad", e);
            }
        });
    }

    private void processCreatives(JsonNode creatives) {
        creatives.forEach(creative -> {
            try {
                Map<String, Object> creativeData = new HashMap<>();
                creativeData.put("id", creative.get("id").asText());
                creativeData.put("name", creative.get("name").asText());
                creativeData.put("ad_squad_id", creative.get("ad_squad_id").asText());
                creativeData.put("type", creative.get("type").asText());
                creativeData.put("status", creative.get("status").asText());
                creativeData.put("review_status", creative.get("review_status").asText());

                if(creative.has("media_id")) {
                    creativeData.put("media_id", creative.get("media_id").asText());
                }

                if(creative.has("headline")) {
                    creativeData.put("headline", creative.get("headline").asText());
                }

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "creative",
                    "category", "creative_data",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(creativeData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing creative", e);
            }
        });
    }

    private void processAudiences(JsonNode audiences) {
        audiences.forEach(audience -> {
            try {
                Map<String, Object> audienceData = new HashMap<>();
                audienceData.put("id", audience.get("id").asText());
                audienceData.put("name", audience.get("name").asText());
                audienceData.put("type", audience.get("type").asText());
                audienceData.put("status", audience.get("status").asText());
                audienceData.put("target_size", audience.get("targetable_audience_size").asLong());
                audienceData.put("upload_size", audience.get("upload_audience_size").asLong());
                audienceData.put("match_rate", audience.get("audience_match_rate").asDouble());

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "audience",
                    "category", "audience_data",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(audienceData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing audience", e);
            }
        });
    }

    private void processPixelStats(JsonNode pixelStats) {
        pixelStats.forEach(stat -> {
            try {
                Map<String, Object> pixelData = new HashMap<>();
                pixelData.put("start_time", stat.get("start_time").asText());
                pixelData.put("end_time", stat.get("end_time").asText());

                JsonNode stats = stat.get("stats");
                stats.fieldNames().forEachRemaining(eventType -> {
                    pixelData.put(eventType, stats.get(eventType).asLong());
                });

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "pixel_stats",
                    "category", "pixel_data",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(pixelData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing pixel stats", e);
            }
        });
    }

    private void processCampaignReports(JsonNode reports) {
        reports.forEach(report -> {
            try {
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("report_type", "campaign");
                reportData.put("start_time", report.get("start_time").asText());
                reportData.put("end_time", report.get("end_time").asText());
                reportData.put("dimension_breakdown", report.get("dimension_stats"));

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "report",
                    "category", "campaign_report",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(reportData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing campaign report", e);
            }
        });
    }

    private void processAdSquadReports(JsonNode reports) {
        reports.forEach(report -> {
            try {
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("report_type", "ad_squad");
                reportData.put("start_time", report.get("start_time").asText());
                reportData.put("end_time", report.get("end_time").asText());
                reportData.put("dimension_breakdown", report.get("dimension_stats"));

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "report",
                    "category", "ad_squad_report",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(reportData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing ad squad report", e);
            }
        });
    }

    private void processCreativeReports(JsonNode reports) {
        reports.forEach(report -> {
            try {
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("report_type", "creative");
                reportData.put("start_time", report.get("start_time").asText());
                reportData.put("end_time", report.get("end_time").asText());
                reportData.put("dimension_breakdown", report.get("dimension_stats"));

                MessageDTO message = new MessageDTO();
                message.setHeaders(Map.of(
                    "type", "report",
                    "category", "creative_report",
                    "source", "snapchat_ads"
                ));
                message.setPayload(objectMapper.writeValueAsString(reportData));

                send(message);
            } catch(Exception e) {
                log.error("Error processing creative report", e);
            }
        });
    }

    // Helper methods
    private String getStartTime() {
        return LocalDateTime.now(ZoneOffset.UTC)
            .minusDays(7)
            .format(ISO_FORMATTER);
    }

    private String getEndTime() {
        return LocalDateTime.now(ZoneOffset.UTC)
            .format(ISO_FORMATTER);
    }

    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method) {
        return makeAuthenticatedRequest(endpoint, method, null, null);
    }

    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method,
                                            Map<String, String> queryParams) {
        return makeAuthenticatedRequest(endpoint, method, queryParams, null);
    }


    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method,
                                            Map<String, String> queryParams, Object body) {
        try {
            String accessToken = config.getAccessToken(); // Using config directly for now

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            if(body != null) {
                headers.set("Content - Type", "application/json");
            }

            String url = endpoint;
            if(queryParams != null && !queryParams.isEmpty()) {
                StringBuilder queryString = new StringBuilder("?");
                queryParams.forEach((key, value) ->
                    queryString.append(key).append(" = ").append(value).append("&")
               );
                url += queryString.substring(0, queryString.length() - 1);
            }

            HttpEntity<?> entity = body != null ?
                new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, method, entity, String.class
           );

            return objectMapper.readTree(response.getBody());
        } catch(HttpClientErrorException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch(Exception e) {
            log.error("Error parsing response", e);
            return null;
        }
    }
}
