package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.facebook.FacebookAdsApiConfig.*;
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

@Slf4j
@Component("facebookAdsInboundAdapter")
public class FacebookAdsInboundAdapter extends AbstractSocialMediaInboundAdapter<FacebookAdsApiConfig> {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastPollTime = new ConcurrentHashMap<>();
    
    @Autowired
    public FacebookAdsInboundAdapter(
            FacebookAdsApiConfig config,
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
            throw new AdapterException("Facebook Ads configuration is invalid");
        }
        
        log.info("Starting Facebook Ads inbound adapter for account: {}", config.getAdAccountId());
        isListening = true;
        
        // Initialize polling for different data types
        if (config.getFeatures().isEnablePerformanceTracking()) {
            schedulePerformanceDataPolling();
        }
        
        if (config.getFeatures().isEnableAutomatedRules()) {
            scheduleRuleTriggersPolling();
        }
    }
    
    @Override
    public void stopListening() {
        log.info("Stopping Facebook Ads inbound adapter");
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
        } catch (Exception e) {
            log.error("Error processing Facebook Ads inbound data", e);
            throw new AdapterException("Failed to process inbound data", e);
        }
    }
    
    @Scheduled(fixedDelayString = "${integrixs.adapters.facebook.ads.pollingInterval:300000}") // 5 minutes
    private void pollPerformanceData() {
        if (!isListening) return;
        
        try {
            rateLimiterService.acquire("facebook_ads_api", 1);
            
            // Poll campaign performance
            pollCampaignPerformance();
            
            // Poll ad set performance
            pollAdSetPerformance();
            
            // Poll ad performance
            pollAdPerformance();
            
        } catch (Exception e) {
            log.error("Error polling Facebook Ads performance data", e);
        }
    }
    
    private void pollCampaignPerformance() {
        String accountId = config.getAdAccountId();
        String url = String.format("%s/%s/act_%s/campaigns", 
            config.getBaseUrl(), config.getApiVersion(), accountId);
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,objective,spend_cap,daily_budget,lifetime_budget," +
                            "start_time,stop_time,created_time,insights{impressions,clicks," +
                            "spend,reach,frequency,ctr,cpc,cpm,cpp}");
        params.put("limit", "100");
        
        LocalDateTime lastPoll = lastPollTime.getOrDefault("campaigns", LocalDateTime.now().minusHours(24));
        params.put("filtering", String.format("[{field:'updated_time',operator:'GREATER_THAN',value:'%s'}]", 
                                            lastPoll.toString()));
        
        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CAMPAIGN_PERFORMANCE");
                lastPollTime.put("campaigns", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling campaign performance", e);
        }
    }
    
    private void pollAdSetPerformance() {
        String accountId = config.getAdAccountId();
        String url = String.format("%s/%s/act_%s/adsets", 
            config.getBaseUrl(), config.getApiVersion(), accountId);
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,campaign_id,targeting,bid_amount,billing_event," +
                            "optimization_goal,daily_budget,lifetime_budget,start_time,end_time," +
                            "insights{impressions,clicks,spend,reach,frequency,conversions}");
        params.put("limit", "100");
        
        LocalDateTime lastPoll = lastPollTime.getOrDefault("adsets", LocalDateTime.now().minusHours(24));
        params.put("filtering", String.format("[{field:'updated_time',operator:'GREATER_THAN',value:'%s'}]", 
                                            lastPoll.toString()));
        
        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AD_SET_PERFORMANCE");
                lastPollTime.put("adsets", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling ad set performance", e);
        }
    }
    
    private void pollAdPerformance() {
        String accountId = config.getAdAccountId();
        String url = String.format("%s/%s/act_%s/ads", 
            config.getBaseUrl(), config.getApiVersion(), accountId);
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "id,name,status,adset_id,creative,bid_type,bid_info," +
                            "insights{impressions,clicks,spend,reach,frequency,actions," +
                            "cost_per_action_type,video_views,video_p25_watched_actions}");
        params.put("limit", "100");
        
        LocalDateTime lastPoll = lastPollTime.getOrDefault("ads", LocalDateTime.now().minusHours(24));
        params.put("filtering", String.format("[{field:'updated_time',operator:'GREATER_THAN',value:'%s'}]", 
                                            lastPoll.toString()));
        
        try {
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
            if (response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "AD_PERFORMANCE");
                lastPollTime.put("ads", LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("Error polling ad performance", e);
        }
    }
    
    private void schedulePerformanceDataPolling() {
        log.info("Scheduled performance data polling for Facebook Ads");
    }
    
    private void scheduleRuleTriggersPolling() {
        log.info("Scheduled rule triggers polling for Facebook Ads");
    }
    
    private Message processCampaignPerformance(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CAMPAIGN_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", config.getAdAccountId());
        
        if (data.has("data") && data.get("data").isArray()) {
            headers.put("campaign_count", data.get("data").size());
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processAdSetPerformance(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AD_SET_PERFORMANCE");
        headers.put("source", "facebook_ads");
        headers.put("account_id", config.getAdAccountId());
        
        if (data.has("data") && data.get("data").isArray()) {
            headers.put("adset_count", data.get("data").size());
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
        headers.put("source", "facebook_ads");
        headers.put("account_id", config.getAdAccountId());
        
        if (data.has("data") && data.get("data").isArray()) {
            headers.put("ad_count", data.get("data").size());
        }
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processRuleTrigger(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "RULE_TRIGGER");
        headers.put("source", "facebook_ads");
        headers.put("rule_id", data.path("rule_id").asText());
        headers.put("rule_name", data.path("rule_name").asText());
        
        message.setHeaders(headers);
        message.setPayload(data.toString());
        
        return message;
    }
    
    private Message processLeadFormSubmission(JsonNode data) {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setTimestamp(Instant.now());
        message.setStatus(MessageStatus.RECEIVED);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "LEAD_FORM_SUBMISSION");
        headers.put("source", "facebook_ads");
        headers.put("form_id", data.path("form_id").asText());
        headers.put("lead_id", data.path("lead_id").asText());
        
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
        String encryptedToken = config.getAccessToken();
        return credentialEncryptionService.decrypt(encryptedToken);
    }
    
    private boolean isConfigValid() {
        return config != null 
            && config.getAdAccountId() != null 
            && config.getAccessToken() != null
            && config.getAppId() != null
            && config.getAppSecret() != null;
    }
}