package com.integrixs.adapters.social.tiktok;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.tiktok.TikTokBusinessApiConfig.*;
import com.integrixs.core.api.channel.Message;
import com.integrixs.core.exception.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component("tikTokBusinessOutboundAdapter")
public class TikTokBusinessOutboundAdapter extends AbstractSocialMediaOutboundAdapter<TikTokBusinessApiConfig> {
    
    private static final String TIKTOK_API_BASE = "https://business-api.tiktok.com/open_api/v1.3";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public TikTokBusinessOutboundAdapter(
            TikTokBusinessApiConfig config,
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
    protected Message processOutboundMessage(Message message) {
        try {
            String operation = (String) message.getHeaders().get("operation");
            if (operation == null) {
                throw new AdapterException("Operation header is required");
            }
            
            log.info("Processing TikTok Business outbound operation: {}", operation);
            
            // Refresh token if needed
            refreshAccessTokenIfNeeded();
            
            switch (operation.toUpperCase()) {
                // Campaign operations
                case "CREATE_CAMPAIGN":
                    return createCampaign(message);
                case "UPDATE_CAMPAIGN":
                    return updateCampaign(message);
                case "UPDATE_CAMPAIGN_STATUS":
                    return updateCampaignStatus(message);
                case "GET_CAMPAIGN":
                    return getCampaign(message);
                    
                // Ad Group operations
                case "CREATE_AD_GROUP":
                    return createAdGroup(message);
                case "UPDATE_AD_GROUP":
                    return updateAdGroup(message);
                case "UPDATE_AD_GROUP_STATUS":
                    return updateAdGroupStatus(message);
                case "GET_AD_GROUP":
                    return getAdGroup(message);
                    
                // Ad operations
                case "CREATE_AD":
                    return createAd(message);
                case "UPDATE_AD":
                    return updateAd(message);
                case "UPDATE_AD_STATUS":
                    return updateAdStatus(message);
                case "GET_AD":
                    return getAd(message);
                    
                // Creative operations
                case "UPLOAD_VIDEO":
                    return uploadVideo(message);
                case "UPLOAD_IMAGE":
                    return uploadImage(message);
                case "CREATE_CREATIVE":
                    return createCreative(message);
                case "GET_CREATIVE":
                    return getCreative(message);
                    
                // Audience operations
                case "CREATE_CUSTOM_AUDIENCE":
                    return createCustomAudience(message);
                case "UPDATE_CUSTOM_AUDIENCE":
                    return updateCustomAudience(message);
                case "CREATE_LOOKALIKE_AUDIENCE":
                    return createLookalikeAudience(message);
                case "GET_AUDIENCE":
                    return getAudience(message);
                    
                // Pixel operations
                case "CREATE_PIXEL":
                    return createPixel(message);
                case "GET_PIXEL":
                    return getPixel(message);
                case "SEND_PIXEL_EVENT":
                    return sendPixelEvent(message);
                    
                // Reporting operations
                case "GET_REPORT":
                    return getReport(message);
                case "GET_AUDIENCE_INSIGHTS":
                    return getAudienceInsights(message);
                case "GET_CREATIVE_INSIGHTS":
                    return getCreativeInsights(message);
                    
                // Spark Ads operations
                case "CREATE_SPARK_AD":
                    return createSparkAd(message);
                case "AUTHORIZE_SPARK_AD":
                    return authorizeSparkAd(message);
                    
                // Catalog operations
                case "CREATE_CATALOG":
                    return createCatalog(message);
                case "UPDATE_CATALOG":
                    return updateCatalog(message);
                case "CREATE_PRODUCT":
                    return createProduct(message);
                case "UPDATE_PRODUCT":
                    return updateProduct(message);
                    
                // Budget operations
                case "UPDATE_BUDGET":
                    return updateBudget(message);
                case "GET_BUDGET_RECOMMENDATION":
                    return getBudgetRecommendation(message);
                    
                // Targeting operations
                case "GET_TARGETING_TAGS":
                    return getTargetingTags(message);
                case "GET_INTEREST_CATEGORIES":
                    return getInterestCategories(message);
                case "GET_BEHAVIOR_CATEGORIES":
                    return getBehaviorCategories(message);
                    
                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch (Exception e) {
            log.error("Error processing TikTok Business outbound message", e);
            throw new AdapterException("Failed to process outbound message", e);
        }
    }
    
    // Campaign operations
    private Message createCampaign(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/campaign/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("campaign_name", payload.get("name"));
        requestBody.put("objective_type", payload.get("objective"));
        
        // Budget configuration
        if (payload.containsKey("budget")) {
            requestBody.put("budget_mode", payload.get("budgetType"));
            requestBody.put("budget", payload.get("budget"));
        }
        
        // Bid strategy
        if (payload.containsKey("bidStrategy")) {
            requestBody.put("bid_type", payload.get("bidStrategy"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_CAMPAIGN");
    }
    
    private Message updateCampaign(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/campaign/update/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("campaign_id", payload.get("campaignId"));
        
        if (payload.containsKey("name")) {
            requestBody.put("campaign_name", payload.get("name"));
        }
        if (payload.containsKey("budget")) {
            requestBody.put("budget", payload.get("budget"));
        }
        if (payload.containsKey("bidType")) {
            requestBody.put("bid_type", payload.get("bidType"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_CAMPAIGN");
    }
    
    private Message updateCampaignStatus(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/campaign/update/status/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        List<String> campaignIds = (List<String>) payload.get("campaignIds");
        requestBody.put("campaign_ids", campaignIds);
        requestBody.put("operation_status", payload.get("status"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_CAMPAIGN_STATUS");
    }
    
    private Message getCampaign(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/campaign/get/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        if (payload.containsKey("campaignIds")) {
            params.put("campaign_ids", String.join(",", (List<String>) payload.get("campaignIds")));
        }
        if (payload.containsKey("page")) {
            params.put("page", payload.get("page"));
        }
        params.put("page_size", payload.getOrDefault("pageSize", 100));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_CAMPAIGN");
    }
    
    // Ad Group operations
    private Message createAdGroup(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/adgroup/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("campaign_id", payload.get("campaignId"));
        requestBody.put("adgroup_name", payload.get("name"));
        
        // Placement
        List<String> placements = (List<String>) payload.get("placements");
        requestBody.put("placement_type", placements.isEmpty() ? "PLACEMENT_TYPE_AUTOMATIC" : "PLACEMENT_TYPE_NORMAL");
        if (!placements.isEmpty()) {
            requestBody.put("placement", placements);
        }
        
        // Targeting
        if (payload.containsKey("targeting")) {
            requestBody.put("targeting", payload.get("targeting"));
        }
        
        // Budget and bid
        requestBody.put("budget_mode", payload.get("budgetType"));
        requestBody.put("budget", payload.get("budget"));
        requestBody.put("billing_event", payload.get("billingEvent"));
        requestBody.put("bid", payload.get("bid"));
        
        // Schedule
        if (payload.containsKey("schedule")) {
            requestBody.put("schedule_type", "SCHEDULE_START_END");
            requestBody.put("schedule_start_time", payload.get("scheduleStart"));
            requestBody.put("schedule_end_time", payload.get("scheduleEnd"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_AD_GROUP");
    }
    
    private Message updateAdGroup(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/adgroup/update/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("adgroup_id", payload.get("adGroupId"));
        
        // Update only provided fields
        if (payload.containsKey("name")) {
            requestBody.put("adgroup_name", payload.get("name"));
        }
        if (payload.containsKey("budget")) {
            requestBody.put("budget", payload.get("budget"));
        }
        if (payload.containsKey("bid")) {
            requestBody.put("bid", payload.get("bid"));
        }
        if (payload.containsKey("targeting")) {
            requestBody.put("targeting", payload.get("targeting"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_AD_GROUP");
    }
    
    private Message updateAdGroupStatus(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/adgroup/update/status/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("adgroup_ids", payload.get("adGroupIds"));
        requestBody.put("operation_status", payload.get("status"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_AD_GROUP_STATUS");
    }
    
    private Message getAdGroup(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/adgroup/get/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        if (payload.containsKey("adGroupIds")) {
            params.put("adgroup_ids", String.join(",", (List<String>) payload.get("adGroupIds")));
        }
        if (payload.containsKey("campaignIds")) {
            params.put("campaign_ids", String.join(",", (List<String>) payload.get("campaignIds")));
        }
        params.put("page", payload.getOrDefault("page", 1));
        params.put("page_size", payload.getOrDefault("pageSize", 100));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_AD_GROUP");
    }
    
    // Ad operations
    private Message createAd(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/ad/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("adgroup_id", payload.get("adGroupId"));
        
        // Ad details
        requestBody.put("ad_name", payload.get("name"));
        requestBody.put("ad_text", payload.get("text"));
        requestBody.put("call_to_action", payload.get("callToAction"));
        
        // Creatives
        if (payload.containsKey("creatives")) {
            requestBody.put("creatives", payload.get("creatives"));
        }
        
        // Landing page
        if (payload.containsKey("landingPageUrl")) {
            requestBody.put("landing_page_url", payload.get("landingPageUrl"));
        }
        
        // Display name
        if (payload.containsKey("displayName")) {
            requestBody.put("display_name", payload.get("displayName"));
        }
        
        // Identity
        if (payload.containsKey("identityType")) {
            requestBody.put("identity_type", payload.get("identityType"));
            requestBody.put("identity_id", payload.get("identityId"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_AD");
    }
    
    private Message updateAd(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/ad/update/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("ad_id", payload.get("adId"));
        
        // Update only provided fields
        if (payload.containsKey("name")) {
            requestBody.put("ad_name", payload.get("name"));
        }
        if (payload.containsKey("text")) {
            requestBody.put("ad_text", payload.get("text"));
        }
        if (payload.containsKey("callToAction")) {
            requestBody.put("call_to_action", payload.get("callToAction"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_AD");
    }
    
    private Message updateAdStatus(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/ad/update/status/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("ad_ids", payload.get("adIds"));
        requestBody.put("operation_status", payload.get("status"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_AD_STATUS");
    }
    
    private Message getAd(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/ad/get/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        if (payload.containsKey("adIds")) {
            params.put("ad_ids", String.join(",", (List<String>) payload.get("adIds")));
        }
        if (payload.containsKey("adGroupIds")) {
            params.put("adgroup_ids", String.join(",", (List<String>) payload.get("adGroupIds")));
        }
        params.put("page", payload.getOrDefault("page", 1));
        params.put("page_size", payload.getOrDefault("pageSize", 100));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_AD");
    }
    
    // Creative operations
    private Message uploadVideo(Message message) throws Exception {
        if (!config.getFeatures().isEnableVideoUpload()) {
            throw new AdapterException("Video upload is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 2);
        
        String url = TIKTOK_API_BASE + "/file/video/ad/upload/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String videoPath = (String) payload.get("videoPath");
        
        // Check video size
        long fileSizeInMB = Files.size(Paths.get(videoPath)) / (1024 * 1024);
        if (fileSizeInMB > config.getLimits().getMaxVideoSizeMB()) {
            throw new AdapterException("Video size exceeds limit: " + fileSizeInMB + "MB");
        }
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("advertiser_id", config.getAdvertiserId());
        
        // Video upload signature (required by TikTok)
        if (payload.containsKey("uploadId")) {
            body.add("upload_id", payload.get("uploadId"));
        }
        
        // Add video file
        byte[] videoBytes = Files.readAllBytes(Paths.get(videoPath));
        body.add("video_file", new ByteArrayResource(videoBytes) {
            @Override
            public String getFilename() {
                return payload.getOrDefault("fileName", "video.mp4").toString();
            }
        });
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Token", getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        
        return createResponseMessage(response, "UPLOAD_VIDEO");
    }
    
    private Message uploadImage(Message message) throws Exception {
        if (!config.getFeatures().isEnableImageUpload()) {
            throw new AdapterException("Image upload is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/file/image/ad/upload/";
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String imagePath = (String) payload.get("imagePath");
        
        // Check image size
        long fileSizeInMB = Files.size(Paths.get(imagePath)) / (1024 * 1024);
        if (fileSizeInMB > config.getLimits().getMaxImageSizeMB()) {
            throw new AdapterException("Image size exceeds limit: " + fileSizeInMB + "MB");
        }
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("advertiser_id", config.getAdvertiserId());
        
        if (payload.containsKey("uploadId")) {
            body.add("upload_id", payload.get("uploadId"));
        }
        
        // Add image file
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        body.add("image_file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return payload.getOrDefault("fileName", "image.jpg").toString();
            }
        });
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Token", getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        
        return createResponseMessage(response, "UPLOAD_IMAGE");
    }
    
    private Message createCreative(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/creative/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        // Creative details
        if (payload.containsKey("videoId")) {
            requestBody.put("video_id", payload.get("videoId"));
        }
        if (payload.containsKey("imageIds")) {
            requestBody.put("image_ids", payload.get("imageIds"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_CREATIVE");
    }
    
    private Message getCreative(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/creative/info/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        if (payload.containsKey("creativeIds")) {
            params.put("creative_ids", String.join(",", (List<String>) payload.get("creativeIds")));
        }
        params.put("page", payload.getOrDefault("page", 1));
        params.put("page_size", payload.getOrDefault("pageSize", 100));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_CREATIVE");
    }
    
    // Audience operations
    private Message createCustomAudience(Message message) throws Exception {
        if (!config.getFeatures().isEnableCustomAudiences()) {
            throw new AdapterException("Custom audiences are not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/dmp/custom_audience/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("custom_audience_name", payload.get("name"));
        
        // Audience source
        String sourceType = (String) payload.get("sourceType");
        requestBody.put("source_type", sourceType);
        
        // Handle different source types
        if ("FILE".equals(sourceType)) {
            requestBody.put("file_path", payload.get("filePath"));
        } else if ("PIXEL".equals(sourceType)) {
            requestBody.put("pixel_id", payload.get("pixelId"));
            requestBody.put("retention_days", payload.get("retentionDays"));
            requestBody.put("rule", payload.get("rule"));
        } else if ("ENGAGEMENT".equals(sourceType)) {
            requestBody.put("engagement_type", payload.get("engagementType"));
            requestBody.put("engagement_spec", payload.get("engagementSpec"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_CUSTOM_AUDIENCE");
    }
    
    private Message updateCustomAudience(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/dmp/custom_audience/update/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("custom_audience_id", payload.get("audienceId"));
        
        if (payload.containsKey("name")) {
            requestBody.put("custom_audience_name", payload.get("name"));
        }
        if (payload.containsKey("filePath")) {
            requestBody.put("file_path", payload.get("filePath"));
            requestBody.put("action", payload.getOrDefault("action", "APPEND"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_CUSTOM_AUDIENCE");
    }
    
    private Message createLookalikeAudience(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/dmp/lookalike_audience/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("lookalike_name", payload.get("name"));
        requestBody.put("source_audience_id", payload.get("sourceAudienceId"));
        requestBody.put("lookalike_spec", payload.get("spec"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_LOOKALIKE_AUDIENCE");
    }
    
    private Message getAudience(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/dmp/custom_audience/list/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        if (payload.containsKey("audienceIds")) {
            params.put("custom_audience_ids", String.join(",", (List<String>) payload.get("audienceIds")));
        }
        params.put("page", payload.getOrDefault("page", 1));
        params.put("page_size", payload.getOrDefault("pageSize", 100));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_AUDIENCE");
    }
    
    // Pixel operations
    private Message createPixel(Message message) throws Exception {
        if (!config.getFeatures().isEnablePixelTracking()) {
            throw new AdapterException("Pixel tracking is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/pixel/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("pixel_name", payload.get("name"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_PIXEL");
    }
    
    private Message getPixel(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/pixel/list/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        if (payload.containsKey("pixelIds")) {
            params.put("pixel_ids", String.join(",", (List<String>) payload.get("pixelIds")));
        }
        params.put("page", payload.getOrDefault("page", 1));
        params.put("page_size", payload.getOrDefault("pageSize", 100));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_PIXEL");
    }
    
    private Message sendPixelEvent(Message message) throws Exception {
        if (!config.getFeatures().isEnablePixelTracking()) {
            throw new AdapterException("Pixel tracking is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/pixel/track/";
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        requestBody.put("pixel_code", payload.get("pixelCode"));
        requestBody.put("event", payload.get("event"));
        
        // Event data
        Map<String, Object> data = new HashMap<>();
        data.put("event_time", System.currentTimeMillis() / 1000); // Unix timestamp
        
        if (payload.containsKey("eventId")) {
            data.put("event_id", payload.get("eventId"));
        }
        if (payload.containsKey("value")) {
            data.put("value", payload.get("value"));
        }
        if (payload.containsKey("currency")) {
            data.put("currency", payload.get("currency"));
        }
        
        // User data
        if (payload.containsKey("userData")) {
            data.put("user", payload.get("userData"));
        }
        
        // Custom data
        if (payload.containsKey("customData")) {
            data.put("custom", payload.get("customData"));
        }
        
        requestBody.put("data", Arrays.asList(data));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "SEND_PIXEL_EVENT");
    }
    
    // Reporting operations
    private Message getReport(Message message) throws Exception {
        if (!config.getFeatures().isEnableReporting()) {
            throw new AdapterException("Reporting is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 2);
        
        String url = TIKTOK_API_BASE + "/report/integrated/get/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        // Report type
        requestBody.put("report_type", payload.getOrDefault("reportType", ReportType.BASIC.name()));
        requestBody.put("data_level", payload.getOrDefault("dataLevel", "AUCTION_CAMPAIGN"));
        
        // Dimensions and metrics
        requestBody.put("dimensions", payload.get("dimensions"));
        requestBody.put("metrics", payload.get("metrics"));
        
        // Date range
        requestBody.put("start_date", payload.get("startDate"));
        requestBody.put("end_date", payload.get("endDate"));
        
        // Filters
        if (payload.containsKey("filters")) {
            requestBody.put("filters", payload.get("filters"));
        }
        
        // Pagination
        requestBody.put("page", payload.getOrDefault("page", 1));
        requestBody.put("page_size", payload.getOrDefault("pageSize", 1000));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, requestBody, null);
        return createResponseMessage(response, "GET_REPORT");
    }
    
    private Message getAudienceInsights(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/audience_insights/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("custom_audience_ids", payload.get("audienceIds"));
        requestBody.put("dimension", payload.getOrDefault("dimension", "GENDER"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "GET_AUDIENCE_INSIGHTS");
    }
    
    private Message getCreativeInsights(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/creative/insights/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        params.put("creative_ids", String.join(",", (List<String>) payload.get("creativeIds")));
        params.put("start_date", payload.get("startDate"));
        params.put("end_date", payload.get("endDate"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_CREATIVE_INSIGHTS");
    }
    
    // Spark Ads operations
    private Message createSparkAd(Message message) throws Exception {
        if (!config.getFeatures().isEnableSparkAds()) {
            throw new AdapterException("Spark Ads are not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/tt_video/info/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        params.put("item_ids", String.join(",", (List<String>) payload.get("videoIds")));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "CREATE_SPARK_AD");
    }
    
    private Message authorizeSparkAd(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/bc_creator/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("bc_id", payload.get("creatorId"));
        requestBody.put("video_item_ids", payload.get("videoIds"));
        requestBody.put("code_type", payload.getOrDefault("codeType", "PUBLIC_AUTH"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "AUTHORIZE_SPARK_AD");
    }
    
    // Catalog operations
    private Message createCatalog(Message message) throws Exception {
        if (!config.getFeatures().isEnableCatalogManagement()) {
            throw new AdapterException("Catalog management is not enabled");
        }
        
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/catalog/create/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bc_id", config.getBusinessId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("catalog_name", payload.get("name"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_CATALOG");
    }
    
    private Message updateCatalog(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/catalog/update/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bc_id", config.getBusinessId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("catalog_id", payload.get("catalogId"));
        requestBody.put("catalog_name", payload.get("name"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_CATALOG");
    }
    
    private Message createProduct(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/catalog/product/upload/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bc_id", config.getBusinessId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("catalog_id", payload.get("catalogId"));
        requestBody.put("products", payload.get("products"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "CREATE_PRODUCT");
    }
    
    private Message updateProduct(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/catalog/product/update/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("bc_id", config.getBusinessId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("catalog_id", payload.get("catalogId"));
        requestBody.put("products", payload.get("products"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_PRODUCT");
    }
    
    // Budget operations
    private Message updateBudget(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/budget/update/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        
        String entityType = (String) payload.get("entityType");
        if ("CAMPAIGN".equals(entityType)) {
            requestBody.put("campaign_ids", payload.get("entityIds"));
        } else if ("ADGROUP".equals(entityType)) {
            requestBody.put("adgroup_ids", payload.get("entityIds"));
        }
        
        requestBody.put("budget", payload.get("budget"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "UPDATE_BUDGET");
    }
    
    private Message getBudgetRecommendation(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/tool/budget_recommend/";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        requestBody.put("objective_type", payload.get("objective"));
        requestBody.put("billing_event", payload.get("billingEvent"));
        requestBody.put("targeting", payload.get("targeting"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
        return createResponseMessage(response, "GET_BUDGET_RECOMMENDATION");
    }
    
    // Targeting operations
    private Message getTargetingTags(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/tool/targeting_tag/get/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        params.put("scene", payload.getOrDefault("scene", "GENERAL_INTEREST"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_TARGETING_TAGS");
    }
    
    private Message getInterestCategories(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/tool/interest_category/get/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        params.put("language", payload.getOrDefault("language", "en"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_INTEREST_CATEGORIES");
    }
    
    private Message getBehaviorCategories(Message message) throws Exception {
        rateLimiterService.acquire("tiktok_business_api", 1);
        
        String url = TIKTOK_API_BASE + "/tool/action_category/get/";
        
        Map<String, Object> params = new HashMap<>();
        params.put("advertiser_id", config.getAdvertiserId());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        params.put("language", payload.getOrDefault("language", "en"));
        params.put("scene", payload.getOrDefault("scene", "ACTION_CATEGORY"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
        return createResponseMessage(response, "GET_BEHAVIOR_CATEGORIES");
    }
    
    // Helper methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, Object> params, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Token", getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (params != null && method == HttpMethod.GET) {
            params.forEach((key, value) -> builder.queryParam(key, value.toString()));
        }
        
        HttpEntity<?> entity;
        if (body != null) {
            entity = new HttpEntity<>(body, headers);
        } else if (params != null && method != HttpMethod.GET) {
            entity = new HttpEntity<>(params, headers);
        } else {
            entity = new HttpEntity<>(headers);
        }
        
        return restTemplate.exchange(builder.toUriString(), method, entity, String.class);
    }
    
    private Message createResponseMessage(ResponseEntity<String> response, String operation) {
        Message responseMessage = new Message();
        responseMessage.setHeaders(Map.of(
            "operation", operation,
            "statusCode", response.getStatusCode().value(),
            "source", "tiktok_business"
        ));
        responseMessage.setPayload(response.getBody());
        
        return responseMessage;
    }
    
    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }
    
    private void refreshAccessTokenIfNeeded() {
        try {
            if (config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getAppId(),
                    config.getAppSecret(),
                    config.getRefreshToken(),
                    TIKTOK_API_BASE + "/oauth2/refresh_token/"
                );
            }
        } catch (Exception e) {
            log.error("Error refreshing TikTok Business access token", e);
        }
    }
}