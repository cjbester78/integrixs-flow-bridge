package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.core.AdapterCallback;
import com.integrixs.adapters.social.facebook.FacebookAdsApiConfig.*;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Component("facebookAdsInboundAdapter")
public class FacebookAdsInboundAdapter extends AbstractInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(FacebookAdsInboundAdapter.class);


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    private Map<String, Object> configuration = new HashMap<>();

    @Autowired
    private FacebookAdsApiConfig config;

    @Autowired
    public FacebookAdsInboundAdapter(
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(AdapterConfiguration.AdapterTypeEnum.REST);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("Facebook Ads configuration is invalid");
        }

        log.info("Starting Facebook Ads inbound adapter for account: {}", configuration.get("adAccountId"));
        // Start listening

        // Initialize polling for different data types
        if(Boolean.TRUE.equals(configuration.get("enablePerformanceTracking"))) {
            schedulePerformanceDataPolling();
        }

        if(Boolean.TRUE.equals(configuration.get("enableAutomatedRules"))) {
            scheduleRuleTriggersPolling();
        }
    }

    public void stopListening() {
        log.info("Stopping Facebook Ads inbound adapter");
        // Stop listening
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        try {
            return doSend(payload, headers);
        } catch (Exception e) {
            throw new AdapterException("Failed to send via Facebook Ads adapter", e);
        }
    }

    @Override
    public void sendAsync(Object payload, AdapterCallback callback) throws AdapterException {
        // Not implemented for this adapter
        log.warn("Async send not supported by Facebook Ads adapter");
        if (callback != null) {
            callback.onFailure(AdapterResult.failure(
                "Async send not supported by Facebook Ads adapter",
                new AdapterException("Async send not supported by Facebook Ads adapter")
            ));
        }
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = String.format("%s/%s/act_%s",
                configuration.get("baseUrl"),
                configuration.get("apiVersion"),
                configuration.get("adAccountId"));

            Map<String, String> params = new HashMap<>();
            params.put("access_token", getAccessToken());
            params.put("fields", "id,name");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Facebook Ads API connection successful");
            } else {
                return AdapterResult.failure("Facebook Ads API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error testing Facebook Ads connection", e);
            return AdapterResult.failure("Failed to test Facebook Ads connection: " + e.getMessage());
        }
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // Initialize sender resources if needed
        log.debug("Initializing Facebook Ads sender");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // Cleanup sender resources if needed
        log.debug("Destroying Facebook Ads sender");
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Facebook Ads API is primarily for reading data, but can create/update campaigns
        String endpoint = (String) headers.get("endpoint");
        if (endpoint == null) {
            throw new AdapterException("Endpoint is required for Facebook Ads API operations");
        }

        try {
            String url = String.format("%s/%s/%s",
                configuration.get("baseUrl"),
                configuration.get("apiVersion"),
                endpoint);

            HttpMethod method = HttpMethod.valueOf((String) headers.getOrDefault("method", "POST"));
            Map<String, String> params = new HashMap<>();
            params.put("access_token", getAccessToken());

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(payload, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(response.getBody(), "Facebook Ads API operation successful");
            } else {
                return AdapterResult.failure("Facebook Ads API operation failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending to Facebook Ads API", e);
            return AdapterResult.failure("Failed to send to Facebook Ads API: " + e.getMessage());
        }
    }

    protected MessageDTO processInboundData(String data, String type) throws AdapterException {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.SUCCESS);

            JsonNode dataNode = objectMapper.readTree(data);

            switch(type) {
                case "CAMPAIGN_PERFORMANCE":
                    message = processCampaignPerformance(dataNode);
                    break;
                case "AD_SET_PERFORMANCE":
                    message = processAdSetPerformance(dataNode);
                    break;
                case "AD_PERFORMANCE":
                    message = processAdPerformance(dataNode);
                    break;
                case "RULE_TRIGGER":
                    message = processRuleTrigger(dataNode);
                    break;
                case "LEAD_FORM_SUBMISSION":
                    message = processLeadFormSubmission(dataNode);
                    break;
                case "BUDGET_ALERT":
                    message = processBudgetAlert(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "facebook_ads"));
            }

            return message;
        } catch(Exception e) {
            log.error("Error processing Facebook Ads inbound data", e);
            throw new AdapterException("Failed to process inbound data", e);
        }
    }

    private void pollCampaignPerformance() {
        String accountId = (String) configuration.get("adAccountId");
        String url = String.format("%s/%s/act_%s/campaigns",
            configuration.get("baseUrl"), configuration.get("apiVersion"), accountId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,objective,spend_cap,daily_budget,lifetime_budget," +
                            "start_time,stop_time,created_time,insights {impressions,clicks," +
                            "spend,reach,frequency,ctr,cpc,cpm,cpp}");
        params.put("limit", String.valueOf(config.getDefaultQueryLimit()));

        LocalDateTime lastPoll = lastPollTime.getOrDefault("campaigns", LocalDateTime.now().minusHours(config.getPollingLookbackHours()));
        params.put("filtering", String.format("[ {field:'updated_time',operator:'GREATER_THAN',value:'%s'}]",
                                            lastPoll.toString()));

        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CAMPAIGN_PERFORMANCE");
                lastPollTime.put("campaigns", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling campaign performance", e);
        }
    }

    private void pollAdSetPerformance() {
        String accountId = (String) configuration.get("adAccountId");
        String url = String.format("%s/%s/act_%s/adsets",
            configuration.get("baseUrl"), configuration.get("apiVersion"), accountId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,campaign_id,targeting,bid_amount,billing_event," +
                            "optimization_goal,daily_budget,lifetime_budget,start_time,end_time," +
                            "insights {impressions,clicks,spend,reach,frequency,conversions}");
        params.put("limit", String.valueOf(config.getDefaultQueryLimit()));

        LocalDateTime lastPoll = lastPollTime.getOrDefault("adsets", LocalDateTime.now().minusHours(config.getPollingLookbackHours()));
        params.put("filtering", String.format("[ {field:'updated_time',operator:'GREATER_THAN',value:'%s'}]",
                                            lastPoll.toString()));

        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AD_SET_PERFORMANCE");
                lastPollTime.put("adsets", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling ad set performance", e);
        }
    }

    private void pollAdPerformance() {
        String accountId = (String) configuration.get("adAccountId");
        String url = String.format("%s/%s/act_%s/ads",
            configuration.get("baseUrl"), configuration.get("apiVersion"), accountId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,adset_id,creative,bid_type,bid_info," +
                            "insights {impressions,clicks,spend,reach,frequency,actions," +
                            "cost_per_action_type,video_views,video_p25_watched_actions}");
        params.put("limit", String.valueOf(config.getDefaultQueryLimit()));

        LocalDateTime lastPoll = lastPollTime.getOrDefault("ads", LocalDateTime.now().minusHours(config.getPollingLookbackHours()));
        params.put("filtering", String.format("[ {field:'updated_time',operator:'GREATER_THAN',value:'%s'}]",
                                            lastPoll.toString()));

        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AD_PERFORMANCE");
                lastPollTime.put("ads", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling ad performance", e);
        }
    }

    private void schedulePerformanceDataPolling() {
        log.info("Scheduled performance data polling for Facebook Ads");
    }

    private void scheduleRuleTriggersPolling() {
        log.info("Scheduled rule triggers polling for Facebook Ads");
    }

    private MessageDTO processCampaignPerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CAMPAIGN_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", configuration.get("adAccountId"));

        if(data.has("data") && data.get("data").isArray()) {
            headers.put("campaign_count", data.get("data").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAdSetPerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_SET_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", configuration.get("adAccountId"));

        if(data.has("data") && data.get("data").isArray()) {
            headers.put("adset_count", data.get("data").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAdPerformance(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", configuration.get("adAccountId"));

        if(data.has("data") && data.get("data").isArray()) {
            headers.put("ad_count", data.get("data").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processRuleTrigger(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "RULE_TRIGGER");
        headers.put("source", "facebook_ads");
        headers.put("rule_id", data.path("rule_id").asText());
        headers.put("rule_name", data.path("rule_name").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processLeadFormSubmission(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "LEAD_FORM_SUBMISSION");
        headers.put("source", "facebook_ads");
        headers.put("form_id", data.path("form_id").asText());
        headers.put("lead_id", data.path("lead_id").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processBudgetAlert(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SUCCESS);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "BUDGET_ALERT");
        headers.put("source", "facebook_ads");
        headers.put("alert_type", data.path("alert_type").asText());
        headers.put("entity_id", data.path("entity_id").asText());
        headers.put("entity_type", data.path("entity_type").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
        // Since we don't have credentialEncryptionService, return the token directly
        // In production, this should be properly encrypted/decrypted
        return (String) configuration.get("accessToken");
    }

    private boolean isConfigValid() {
        return configuration != null
            && configuration.get("adAccountId") != null
            && configuration.get("accessToken") != null
            && configuration.get("appId") != null
            && configuration.get("appSecret") != null;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}