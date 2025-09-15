package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.social.facebook.FacebookAdsApiConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
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
    private Map<String, Object> configuration;

    @Autowired
    public FacebookAdsInboundAdapter(
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("Facebook Ads configuration is invalid");
        }

        log.info("Starting Facebook Ads inbound adapter for account: {}", (String) configuration.get("adAccountId"));
        // Start listening

        // Initialize polling for different data types
        if(Boolean.TRUE.equals(configuration.get("enablePerformanceTracking"))) {
            schedulePerformanceDataPolling();
        }

        if(Boolean.TRUE.equals(configuration.get("enableAutomatedRules"))) {
            scheduleRuleTriggersPolling();
        }
    }

    @Override
    public void stopListening() {
        log.info("Stopping Facebook Ads inbound adapter");
        // Stop listening
    }

    @Override
    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());

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
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), accountId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,objective,spend_cap,daily_budget,lifetime_budget," +
                            "start_time,stop_time,created_time,insights {impressions,clicks," +
                            "spend,reach,frequency,ctr,cpc,cpm,cpp}");
        params.put("limit", "100");

        LocalDateTime lastPoll = lastPollTime.getOrDefault("campaigns", LocalDateTime.now().minusHours(24));
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
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), accountId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,campaign_id,targeting,bid_amount,billing_event," +
                            "optimization_goal,daily_budget,lifetime_budget,start_time,end_time," +
                            "insights {impressions,clicks,spend,reach,frequency,conversions}");
        params.put("limit", "100");

        LocalDateTime lastPoll = lastPollTime.getOrDefault("adsets", LocalDateTime.now().minusHours(24));
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
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), accountId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,adset_id,creative,bid_type,bid_info," +
                            "insights {impressions,clicks,spend,reach,frequency,actions," +
                            "cost_per_action_type,video_views,video_p25_watched_actions}");
        params.put("limit", "100");

        LocalDateTime lastPoll = lastPollTime.getOrDefault("ads", LocalDateTime.now().minusHours(24));
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

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CAMPAIGN_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", (String) configuration.get("adAccountId"));

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

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_SET_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", (String) configuration.get("adAccountId"));

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

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", (String) configuration.get("adAccountId"));

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
                urlBuilder.append(key).append(" = ").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);
    }

    private String getAccessToken() {
        String encryptedToken = (String) configuration.get("accessToken");
        return credentialEncryptionService.decrypt(encryptedToken);
    }

    private boolean isConfigValid() {
        return configuration != null
            && (String) configuration.get("adAccountId") != null
            && (String) configuration.get("accessToken") != null
            && (String) configuration.get("appId") != null
            && (String) configuration.get("appSecret") != null;
    }
}
