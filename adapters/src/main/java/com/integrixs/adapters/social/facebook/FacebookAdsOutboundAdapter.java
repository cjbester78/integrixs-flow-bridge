package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component("facebookAdsOutboundAdapter")
public class FacebookAdsOutboundAdapter extends AbstractSocialMediaOutboundAdapter<FacebookAdsApiConfig> {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public FacebookAdsOutboundAdapter(
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
    public Message processMessage(Message message) throws AdapterException {
        validateConfiguration();
        
        String operation = (String) message.getHeaders().get("operation");
        if (operation == null) {
            throw new AdapterException("Operation header is required");
        }
        
        log.debug("Processing Facebook Ads operation: {}", operation);
        
        try {
            rateLimiterService.acquire("facebook_ads_api", 1);
            
            Message response;
            switch (operation.toUpperCase()) {
                // Campaign operations
                case "CREATE_CAMPAIGN":
                    response = createCampaign(message);
                    break;
                case "UPDATE_CAMPAIGN":
                    response = updateCampaign(message);
                    break;
                case "DELETE_CAMPAIGN":
                    response = deleteCampaign(message);
                    break;
                case "PAUSE_CAMPAIGN":
                    response = pauseCampaign(message);
                    break;
                case "RESUME_CAMPAIGN":
                    response = resumeCampaign(message);
                    break;
                    
                // Ad Set operations
                case "CREATE_AD_SET":
                    response = createAdSet(message);
                    break;
                case "UPDATE_AD_SET":
                    response = updateAdSet(message);
                    break;
                case "DELETE_AD_SET":
                    response = deleteAdSet(message);
                    break;
                    
                // Ad operations
                case "CREATE_AD":
                    response = createAd(message);
                    break;
                case "UPDATE_AD":
                    response = updateAd(message);
                    break;
                case "DELETE_AD":
                    response = deleteAd(message);
                    break;
                    
                // Creative operations
                case "CREATE_CREATIVE":
                    response = createCreative(message);
                    break;
                case "UPLOAD_IMAGE":
                    response = uploadImage(message);
                    break;
                case "UPLOAD_VIDEO":
                    response = uploadVideo(message);
                    break;
                    
                // Audience operations
                case "CREATE_CUSTOM_AUDIENCE":
                    response = createCustomAudience(message);
                    break;
                case "UPDATE_CUSTOM_AUDIENCE":
                    response = updateCustomAudience(message);
                    break;
                case "CREATE_LOOKALIKE_AUDIENCE":
                    response = createLookalikeAudience(message);
                    break;
                    
                // Budget operations
                case "UPDATE_BUDGET":
                    response = updateBudget(message);
                    break;
                case "SET_BID":
                    response = setBid(message);
                    break;
                    
                // Analytics operations
                case "GET_INSIGHTS":
                    response = getInsights(message);
                    break;
                case "GET_PERFORMANCE":
                    response = getPerformance(message);
                    break;
                    
                // Lead operations
                case "GET_LEADS":
                    response = getLeads(message);
                    break;
                case "DOWNLOAD_LEADS":
                    response = downloadLeads(message);
                    break;
                    
                default:
                    throw new AdapterException("Unknown operation: " + operation);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing Facebook Ads message", e);
            Message errorResponse = new Message();
            errorResponse.setMessageId(message.getMessageId());
            errorResponse.setStatus(MessageStatus.ERROR);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "originalOperation", operation
            ));
            return errorResponse;
        }
    }
    
    private Message createCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/act_%s/campaigns", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("name", payload.path("name").asText());
        params.add("objective", payload.path("objective").asText("CONVERSIONS"));
        params.add("status", payload.path("status").asText("PAUSED"));
        
        if (payload.has("spend_cap")) {
            params.add("spend_cap", payload.path("spend_cap").asText());
        }
        if (payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }
        if (payload.has("lifetime_budget")) {
            params.add("lifetime_budget", payload.path("lifetime_budget").asText());
        }
        if (payload.has("start_time")) {
            params.add("start_time", payload.path("start_time").asText());
        }
        if (payload.has("end_time")) {
            params.add("end_time", payload.path("end_time").asText());
        }
        if (payload.has("special_ad_categories")) {
            ArrayNode categories = (ArrayNode) payload.path("special_ad_categories");
            List<String> catList = new ArrayList<>();
            categories.forEach(cat -> catList.add(cat.asText()));
            params.add("special_ad_categories", catList.toString());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "CAMPAIGN_CREATED");
    }
    
    private Message updateCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), config.getApiVersion(), campaignId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        
        if (payload.has("name")) {
            params.add("name", payload.path("name").asText());
        }
        if (payload.has("status")) {
            params.add("status", payload.path("status").asText());
        }
        if (payload.has("spend_cap")) {
            params.add("spend_cap", payload.path("spend_cap").asText());
        }
        if (payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "CAMPAIGN_UPDATED");
    }
    
    private Message createAdSet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/act_%s/adsets", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("name", payload.path("name").asText());
        params.add("campaign_id", payload.path("campaign_id").asText());
        params.add("billing_event", payload.path("billing_event").asText("IMPRESSIONS"));
        params.add("optimization_goal", payload.path("optimization_goal").asText("CONVERSIONS"));
        params.add("status", payload.path("status").asText("PAUSED"));
        
        // Budget
        if (payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }
        if (payload.has("lifetime_budget")) {
            params.add("lifetime_budget", payload.path("lifetime_budget").asText());
        }
        
        // Schedule
        if (payload.has("start_time")) {
            params.add("start_time", payload.path("start_time").asText());
        }
        if (payload.has("end_time")) {
            params.add("end_time", payload.path("end_time").asText());
        }
        
        // Targeting
        if (payload.has("targeting")) {
            params.add("targeting", payload.path("targeting").toString());
        }
        
        // Bid
        if (payload.has("bid_amount")) {
            params.add("bid_amount", payload.path("bid_amount").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AD_SET_CREATED");
    }
    
    private Message createAd(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/act_%s/ads", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("name", payload.path("name").asText());
        params.add("adset_id", payload.path("adset_id").asText());
        params.add("creative", payload.path("creative").toString());
        params.add("status", payload.path("status").asText("PAUSED"));
        
        if (payload.has("tracking_specs")) {
            params.add("tracking_specs", payload.path("tracking_specs").toString());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AD_CREATED");
    }
    
    private Message createCreative(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/act_%s/adcreatives", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("name", payload.path("name").asText());
        
        // Object story spec
        if (payload.has("object_story_spec")) {
            params.add("object_story_spec", payload.path("object_story_spec").toString());
        }
        
        // Asset feed spec
        if (payload.has("asset_feed_spec")) {
            params.add("asset_feed_spec", payload.path("asset_feed_spec").toString());
        }
        
        // Image or video
        if (payload.has("image_hash")) {
            params.add("image_hash", payload.path("image_hash").asText());
        }
        if (payload.has("video_id")) {
            params.add("video_id", payload.path("video_id").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "CREATIVE_CREATED");
    }
    
    private Message createCustomAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/act_%s/customaudiences", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("name", payload.path("name").asText());
        params.add("subtype", payload.path("subtype").asText("CUSTOM"));
        params.add("description", payload.path("description").asText(""));
        
        if (payload.has("customer_file_source")) {
            params.add("customer_file_source", payload.path("customer_file_source").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "CUSTOM_AUDIENCE_CREATED");
    }
    
    private Message createLookalikeAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = String.format("%s/%s/act_%s/customaudiences", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("name", payload.path("name").asText());
        params.add("subtype", "LOOKALIKE");
        params.add("origin_audience_id", payload.path("source_audience_id").asText());
        params.add("lookalike_spec", payload.path("lookalike_spec").toString());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LOOKALIKE_AUDIENCE_CREATED");
    }
    
    private Message getInsights(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String entityType = payload.path("entity_type").asText("campaign");
        String entityId = payload.path("entity_id").asText();
        
        String url = String.format("%s/%s/%s/insights", 
            config.getBaseUrl(), config.getApiVersion(), entityId);
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", payload.path("fields").asText(
            "impressions,clicks,spend,reach,frequency,ctr,cpc,cpm,conversions"));
        params.put("date_preset", payload.path("date_preset").asText("last_7d"));
        
        if (payload.has("breakdowns")) {
            params.put("breakdowns", payload.path("breakdowns").asText());
        }
        
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) -> 
            urlBuilder.append(key).append("=").append(value).append("&"));
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "INSIGHTS_RETRIEVED");
    }
    
    private Message uploadImage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        byte[] imageData = Base64.getDecoder().decode(payload.path("image_data").asText());
        String imageName = payload.path("image_name").asText("image.jpg");
        
        String url = String.format("%s/%s/act_%s/adimages", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        
        Resource imageResource = new ByteArrayResource(imageData) {
            @Override
            public String getFilename() {
                return imageName;
            }
        };
        params.add("bytes", imageResource);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "IMAGE_UPLOADED");
    }
    
    private Message uploadVideo(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        // For video uploads, we initiate an upload session
        String url = String.format("%s/%s/act_%s/advideos", 
            config.getBaseUrl(), config.getApiVersion(), config.getAdAccountId());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("upload_phase", "start");
        params.add("file_size", payload.path("file_size").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "VIDEO_UPLOAD_INITIATED");
    }
    
    // Helper methods
    private Message deleteCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), config.getApiVersion(), campaignId);
        
        Map<String, String> params = Map.of("access_token", getAccessToken());
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url + "?access_token=" + getAccessToken(), 
            HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "CAMPAIGN_DELETED");
    }
    
    private Message pauseCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ((ObjectNode) payload).put("status", "PAUSED");
        
        Message modifiedMessage = new Message();
        modifiedMessage.setMessageId(message.getMessageId());
        modifiedMessage.setHeaders(message.getHeaders());
        modifiedMessage.setPayload(payload.toString());
        
        return updateCampaign(modifiedMessage);
    }
    
    private Message resumeCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ((ObjectNode) payload).put("status", "ACTIVE");
        
        Message modifiedMessage = new Message();
        modifiedMessage.setMessageId(message.getMessageId());
        modifiedMessage.setHeaders(message.getHeaders());
        modifiedMessage.setPayload(payload.toString());
        
        return updateCampaign(modifiedMessage);
    }
    
    private Message updateAdSet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adSetId = payload.path("adset_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), config.getApiVersion(), adSetId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        
        // Update allowed fields
        if (payload.has("name")) params.add("name", payload.path("name").asText());
        if (payload.has("status")) params.add("status", payload.path("status").asText());
        if (payload.has("daily_budget")) params.add("daily_budget", payload.path("daily_budget").asText());
        if (payload.has("bid_amount")) params.add("bid_amount", payload.path("bid_amount").asText());
        if (payload.has("targeting")) params.add("targeting", payload.path("targeting").toString());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AD_SET_UPDATED");
    }
    
    private Message deleteAdSet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adSetId = payload.path("adset_id").asText();
        
        String url = String.format("%s/%s/%s?access_token=%s", 
            config.getBaseUrl(), config.getApiVersion(), adSetId, getAccessToken());
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AD_SET_DELETED");
    }
    
    private Message updateAd(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adId = payload.path("ad_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), config.getApiVersion(), adId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        
        if (payload.has("name")) params.add("name", payload.path("name").asText());
        if (payload.has("status")) params.add("status", payload.path("status").asText());
        if (payload.has("creative")) params.add("creative", payload.path("creative").toString());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AD_UPDATED");
    }
    
    private Message deleteAd(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adId = payload.path("ad_id").asText();
        
        String url = String.format("%s/%s/%s?access_token=%s", 
            config.getBaseUrl(), config.getApiVersion(), adId, getAccessToken());
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AD_DELETED");
    }
    
    private Message updateCustomAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audience_id").asText();
        
        String url = String.format("%s/%s/%s/users", 
            config.getBaseUrl(), config.getApiVersion(), audienceId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("payload", payload.path("users").toString());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "AUDIENCE_UPDATED");
    }
    
    private Message updateBudget(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String entityType = payload.path("entity_type").asText();
        String entityId = payload.path("entity_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), config.getApiVersion(), entityId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        
        if (payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }
        if (payload.has("lifetime_budget")) {
            params.add("lifetime_budget", payload.path("lifetime_budget").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "BUDGET_UPDATED");
    }
    
    private Message setBid(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adSetId = payload.path("adset_id").asText();
        
        String url = String.format("%s/%s/%s", 
            config.getBaseUrl(), config.getApiVersion(), adSetId);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken());
        params.add("bid_amount", payload.path("bid_amount").asText());
        
        if (payload.has("bid_strategy")) {
            params.add("bid_strategy", payload.path("bid_strategy").asText());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "BID_UPDATED");
    }
    
    private Message getPerformance(Message message) throws Exception {
        // Similar to getInsights but focused on performance metrics
        return getInsights(message);
    }
    
    private Message getLeads(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String formId = payload.path("form_id").asText();
        
        String url = String.format("%s/%s/%s/leads", 
            config.getBaseUrl(), config.getApiVersion(), formId);
        
        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken());
        params.put("fields", "created_time,id,ad_id,form_id,field_data");
        
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) -> 
            urlBuilder.append(key).append("=").append(value).append("&"));
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LEADS_RETRIEVED");
    }
    
    private Message downloadLeads(Message message) throws Exception {
        // This would typically return CSV data
        Message leadsMessage = getLeads(message);
        
        // Convert JSON to CSV format
        JsonNode leadsData = objectMapper.readTree(leadsMessage.getPayload());
        StringBuilder csv = new StringBuilder();
        
        // Build CSV from leads data
        if (leadsData.has("data") && leadsData.get("data").isArray()) {
            csv.append("Lead ID,Created Time,Ad ID,Form ID,Field Data\n");
            for (JsonNode lead : leadsData.get("data")) {
                csv.append(lead.path("id").asText()).append(",");
                csv.append(lead.path("created_time").asText()).append(",");
                csv.append(lead.path("ad_id").asText()).append(",");
                csv.append(lead.path("form_id").asText()).append(",");
                csv.append(lead.path("field_data").toString()).append("\n");
            }
        }
        
        Message response = new Message();
        response.setMessageId(message.getMessageId());
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "content-type", "text/csv",
            "operation", "LEADS_DOWNLOADED"
        ));
        response.setPayload(csv.toString());
        
        return response;
    }
    
    private Message createSuccessResponse(String messageId, String responseBody, String operation) {
        Message response = new Message();
        response.setMessageId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setTimestamp(Instant.now());
        response.setHeaders(Map.of(
            "operation", operation,
            "source", "facebook_ads"
        ));
        response.setPayload(responseBody);
        return response;
    }
    
    private String getAccessToken() {
        String encryptedToken = config.getAccessToken();
        return credentialEncryptionService.decrypt(encryptedToken);
    }
    
    private void validateConfiguration() throws AdapterException {
        if (config == null) {
            throw new AdapterException("Facebook Ads configuration is not set");
        }
        if (config.getAdAccountId() == null || config.getAdAccountId().isEmpty()) {
            throw new AdapterException("Ad Account ID is required");
        }
        if (config.getAccessToken() == null || config.getAccessToken().isEmpty()) {
            throw new AdapterException("Access token is required");
        }
    }
}