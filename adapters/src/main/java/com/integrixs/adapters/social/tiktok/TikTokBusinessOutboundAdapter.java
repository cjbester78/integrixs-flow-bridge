package com.integrixs.adapters.social.tiktok;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.social.tiktok.TikTokBusinessApiConfig;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component("tikTokBusinessOutboundAdapter")
public class TikTokBusinessOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TikTokBusinessOutboundAdapter.class);


    // API base URL will be retrieved from config
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private TikTokBusinessApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public TikTokBusinessOutboundAdapter(
            RateLimiterService rateLimiterService,
            TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(rateLimiterService, credentialEncryptionService);
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            String operation = (String) message.getHeaders().get("operation");
            if(operation == null) {
                return createErrorResponse("Operation header is required", "PROCESS_MESSAGE");
            }

            log.info("Processing TikTok Business outbound operation: {}", operation);

            switch(operation.toUpperCase()) {
                // Campaign operations
                case "CREATE_CAMPAIGN":
                    return createCampaign(message);
                case "UPDATE_CAMPAIGN":
                    return updateCampaign(message);
                case "GET_CAMPAIGNS":
                    return getCampaigns(message);
                case "DELETE_CAMPAIGN":
                    return deleteCampaign(message);

                // Ad Group operations
                case "CREATE_AD_GROUP":
                    return createAdGroup(message);
                case "UPDATE_AD_GROUP":
                    return updateAdGroup(message);
                case "GET_AD_GROUPS":
                    return getAdGroups(message);
                case "DELETE_AD_GROUP":
                    return deleteAdGroup(message);

                // Ad operations
                case "CREATE_AD":
                    return createAd(message);
                case "UPDATE_AD":
                    return updateAd(message);
                case "GET_ADS":
                    return getAds(message);
                case "DELETE_AD":
                    return deleteAd(message);

                // Creative operations
                case "CREATE_CREATIVE":
                    return createCreative(message);
                case "UPDATE_CREATIVE":
                    return updateCreative(message);
                case "GET_CREATIVES":
                    return getCreatives(message);
                case "DELETE_CREATIVE":
                    return deleteCreative(message);

                // Asset operations
                case "UPLOAD_IMAGE":
                    return uploadImage(message);
                case "UPLOAD_VIDEO":
                    return uploadVideo(message);
                case "GET_ASSETS":
                    return getAssets(message);

                // Audience operations
                case "CREATE_AUDIENCE":
                    return createAudience(message);
                case "UPDATE_AUDIENCE":
                    return updateAudience(message);
                case "GET_AUDIENCES":
                    return getAudiences(message);
                case "DELETE_AUDIENCE":
                    return deleteAudience(message);

                // Pixel operations
                case "CREATE_PIXEL":
                    return createPixel(message);
                case "UPDATE_PIXEL":
                    return updatePixel(message);
                case "GET_PIXELS":
                    return getPixels(message);
                case "DELETE_PIXEL":
                    return deletePixel(message);

                // Analytics operations
                case "GET_CAMPAIGN_INSIGHTS":
                    return getCampaignInsights(message);
                case "GET_AD_GROUP_INSIGHTS":
                    return getAdGroupInsights(message);
                case "GET_AD_INSIGHTS":
                    return getAdInsights(message);

                default:
                    return createErrorResponse("Unknown operation: " + operation, "PROCESS_MESSAGE");
            }
        } catch(Exception e) {
            log.error("Error processing TikTok Business operation", e);
            return createErrorResponse("Failed to process operation: " + e.getMessage(), "PROCESS_MESSAGE");
        }
    }

    private MessageDTO createCampaign(MessageDTO message) {
        try {
            String url = config.getApiBaseUrl() + "/campaign/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("campaign_name", payload.get("name"));
            requestBody.put("objective_type", payload.get("objective"));

            // Budget configuration
            if(payload.containsKey("budget")) {
                requestBody.put("budget_mode", payload.get("budgetType"));
                requestBody.put("budget", payload.get("budget"));
            }

            // Bid strategy
            if(payload.containsKey("bidStrategy")) {
                requestBody.put("bid_type", payload.get("bidStrategy"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_CAMPAIGN");
        } catch(Exception e) {
            log.error("Error creating campaign", e);
            return createErrorResponse("Failed to create campaign: " + e.getMessage(), "CREATE_CAMPAIGN");
        }
    }

    private MessageDTO updateCampaign(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/campaign/update/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("campaign_id", payload.get("campaignId"));

            if(payload.containsKey("name")) {
                requestBody.put("campaign_name", payload.get("name"));
            }
            if(payload.containsKey("budget")) {
                requestBody.put("budget", payload.get("budget"));
            }
            if(payload.containsKey("bidType")) {
                requestBody.put("bid_type", payload.get("bidType"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_CAMPAIGN");
        } catch(Exception e) {
            log.error("Error updating campaign", e);
            return createErrorResponse("Failed to update campaign: " + e.getMessage(), "UPDATE_CAMPAIGN");
        }
    }

    private MessageDTO updateCampaignStatus(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/campaign/update/status/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            List<String> campaignIds = (List<String>) payload.get("campaignIds");
            requestBody.put("campaign_ids", campaignIds);
            requestBody.put("operation_status", payload.get("status"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_CAMPAIGN_STATUS");
        } catch(Exception e) {
            log.error("Error updating campaign status", e);
            return createErrorResponse("Failed to update campaign status: " + e.getMessage(), "UPDATE_CAMPAIGN_STATUS");
        }
    }

    private MessageDTO getCampaign(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/campaign/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            if(payload.containsKey("campaignIds")) {
                params.put("campaign_ids", String.join(",", (List<String>) payload.get("campaignIds")));
            }
            if(payload.containsKey("page")) {
                params.put("page", payload.get("page"));
            }
            params.put("page_size", payload.getOrDefault("pageSize", config.getDefaultPageSize()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_CAMPAIGN");
        } catch(Exception e) {
            log.error("Error getting campaign", e);
            return createErrorResponse("Failed to get campaign: " + e.getMessage(), "GET_CAMPAIGN");
        }
    }

    // Ad Group operations
    private MessageDTO createAdGroup(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/adgroup/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("campaign_id", payload.get("campaignId"));
            requestBody.put("adgroup_name", payload.get("name"));

            // Placement
            List<String> placements = (List<String>) payload.get("placements");
            requestBody.put("placement_type", placements.isEmpty() ? config.getPlacementTypeAutomatic() : config.getPlacementTypeNormal());
            if(!placements.isEmpty()) {
                requestBody.put("placement", placements);
            }

            // Targeting
            if(payload.containsKey("targeting")) {
                requestBody.put("targeting", payload.get("targeting"));
            }

            // Budget and bid
            requestBody.put("budget_mode", payload.get("budgetType"));
            requestBody.put("budget", payload.get("budget"));
            requestBody.put("billing_event", payload.get("billingEvent"));
            requestBody.put("bid", payload.get("bid"));

            // Schedule
            if(payload.containsKey("schedule")) {
                requestBody.put("schedule_type", config.getScheduleTypeStartEnd());
                requestBody.put("schedule_start_time", payload.get("scheduleStart"));
                requestBody.put("schedule_end_time", payload.get("scheduleEnd"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_AD_GROUP");
        } catch(Exception e) {
            log.error("Error creating ad group", e);
            return createErrorResponse("Failed to create ad group: " + e.getMessage(), "CREATE_AD_GROUP");
        }
    }

    private MessageDTO updateAdGroup(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/adgroup/update/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("adgroup_id", payload.get("adGroupId"));

            // Update only provided fields
            if(payload.containsKey("name")) {
                requestBody.put("adgroup_name", payload.get("name"));
            }
            if(payload.containsKey("budget")) {
                requestBody.put("budget", payload.get("budget"));
            }
            if(payload.containsKey("bid")) {
                requestBody.put("bid", payload.get("bid"));
            }
            if(payload.containsKey("targeting")) {
                requestBody.put("targeting", payload.get("targeting"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_AD_GROUP");
        } catch(Exception e) {
            log.error("Error updating ad group", e);
            return createErrorResponse("Failed to update ad group: " + e.getMessage(), "UPDATE_AD_GROUP");
        }
    }

    private MessageDTO updateAdGroupStatus(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/adgroup/update/status/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("adgroup_ids", payload.get("adGroupIds"));
            requestBody.put("operation_status", payload.get("status"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_AD_GROUP_STATUS");
        } catch(Exception e) {
            log.error("Error updating ad group status", e);
            return createErrorResponse("Failed to update ad group status: " + e.getMessage(), "UPDATE_AD_GROUP_STATUS");
        }
    }

    private MessageDTO getAdGroup(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/adgroup/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            if(payload.containsKey("adGroupIds")) {
                params.put("adgroup_ids", String.join(",", (List<String>) payload.get("adGroupIds")));
            }
            if(payload.containsKey("campaignIds")) {
                params.put("campaign_ids", String.join(",", (List<String>) payload.get("campaignIds")));
            }
            params.put("page", payload.getOrDefault("page", 1));
            params.put("page_size", payload.getOrDefault("pageSize", config.getDefaultPageSize()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_AD_GROUP");
        } catch(Exception e) {
            log.error("Error getting ad group", e);
            return createErrorResponse("Failed to get ad group: " + e.getMessage(), "GET_AD_GROUP");
        }
    }

    // Ad operations
    private MessageDTO createAd(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/ad/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("adgroup_id", payload.get("adGroupId"));

            // Ad details
            requestBody.put("ad_name", payload.get("name"));
            requestBody.put("ad_text", payload.get("text"));
            requestBody.put("call_to_action", payload.get("callToAction"));

            // Creatives
            if(payload.containsKey("creatives")) {
                requestBody.put("creatives", payload.get("creatives"));
            }

            // Landing page
            if(payload.containsKey("landingPageUrl")) {
                requestBody.put("landing_page_url", payload.get("landingPageUrl"));
            }

            // Display name
            if(payload.containsKey("displayName")) {
                requestBody.put("display_name", payload.get("displayName"));
            }

            // Identity
            if(payload.containsKey("identityType")) {
                requestBody.put("identity_type", payload.get("identityType"));
                requestBody.put("identity_id", payload.get("identityId"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_AD");
        } catch(Exception e) {
            log.error("Error creating ad", e);
            return createErrorResponse("Failed to create ad: " + e.getMessage(), "CREATE_AD");
        }
    }

    private MessageDTO updateAd(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/ad/update/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("ad_id", payload.get("adId"));

            // Update only provided fields
            if(payload.containsKey("name")) {
                requestBody.put("ad_name", payload.get("name"));
            }
            if(payload.containsKey("text")) {
                requestBody.put("ad_text", payload.get("text"));
            }
            if(payload.containsKey("callToAction")) {
                requestBody.put("call_to_action", payload.get("callToAction"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_AD");
        } catch(Exception e) {
            log.error("Error updating ad", e);
            return createErrorResponse("Failed to update ad: " + e.getMessage(), "UPDATE_AD");
        }
    }

    private MessageDTO updateAdStatus(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/ad/update/status/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("ad_ids", payload.get("adIds"));
            requestBody.put("operation_status", payload.get("status"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_AD_STATUS");
        } catch(Exception e) {
            log.error("Error updating ad status", e);
            return createErrorResponse("Failed to update ad status: " + e.getMessage(), "UPDATE_AD_STATUS");
        }
    }

    private MessageDTO getAd(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/ad/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            if(payload.containsKey("adIds")) {
                params.put("ad_ids", String.join(",", (List<String>) payload.get("adIds")));
            }
            if(payload.containsKey("adGroupIds")) {
                params.put("adgroup_ids", String.join(",", (List<String>) payload.get("adGroupIds")));
            }
            params.put("page", payload.getOrDefault("page", 1));
            params.put("page_size", payload.getOrDefault("pageSize", config.getDefaultPageSize()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_AD");
        } catch(Exception e) {
            log.error("Error getting ad", e);
            return createErrorResponse("Failed to get ad: " + e.getMessage(), "GET_AD");
        }
    }

    // Creative operations
    private MessageDTO uploadVideo(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnableVideoUpload()) {
                return createErrorResponse("Video upload is not enabled", "UPLOAD_VIDEO");
            }

            rateLimiterService.acquire("tiktok_business_api", 2);

            String url = config.getApiBaseUrl() + "/file/video/ad/upload/";

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String videoPath = (String) payload.get("videoPath");

            // Check video size
            long fileSizeInMB = Files.size(Paths.get(videoPath)) / (1024 * 1024);
            if(fileSizeInMB > config.getLimits().getMaxVideoSizeMB()) {
                return createErrorResponse("Video size exceeds limit: " + fileSizeInMB + "MB", "UPLOAD_VIDEO");
            }

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("advertiser_id", config.getAdvertiserId());

            // Video upload signature(required by TikTok)
            if(payload.containsKey("uploadId")) {
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
            headers.set("Access - Token", getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return createResponseMessage(response, "UPLOAD_VIDEO");
        } catch(Exception e) {
            log.error("Error uploading video", e);
            return createErrorResponse("Failed to upload video: " + e.getMessage(), "UPLOAD_VIDEO");
        }
    }

    private MessageDTO uploadImage(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnableImageUpload()) {
                return createErrorResponse("Image upload is not enabled", "UPLOAD_IMAGE");
            }

            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/file/image/ad/upload/";

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String imagePath = (String) payload.get("imagePath");

            // Check image size
            long fileSizeInMB = Files.size(Paths.get(imagePath)) / (1024 * 1024);
            if(fileSizeInMB > config.getLimits().getMaxImageSizeMB()) {
                return createErrorResponse("Image size exceeds limit: " + fileSizeInMB + "MB", "UPLOAD_IMAGE");
            }

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("advertiser_id", config.getAdvertiserId());

            if(payload.containsKey("uploadId")) {
                body.add("upload_id", payload.get("uploadId"));
            }

            // Add image file
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            body.add("image_file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return Paths.get(imagePath).getFileName().toString();
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.set("Access - Token", getAccessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return createResponseMessage(response, "UPLOAD_IMAGE");
        } catch(Exception e) {
            log.error("Error uploading image", e);
            return createErrorResponse("Failed to upload image: " + e.getMessage(), "UPLOAD_IMAGE");
        }
    }

    private MessageDTO createCreative(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/creative/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            // Creative details
            if(payload.containsKey("videoId")) {
                requestBody.put("video_id", payload.get("videoId"));
            }
            if(payload.containsKey("imageIds")) {
                requestBody.put("image_ids", payload.get("imageIds"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_CREATIVE");
        } catch(Exception e) {
            log.error("Error creating creative", e);
            return createErrorResponse("Failed to create creative: " + e.getMessage(), "CREATE_CREATIVE");
        }
    }

    private MessageDTO getCreative(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/creative/info/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            if(payload.containsKey("creativeIds")) {
                params.put("creative_ids", String.join(",", (List<String>) payload.get("creativeIds")));
            }
            params.put("page", payload.getOrDefault("page", 1));
            params.put("page_size", payload.getOrDefault("pageSize", config.getDefaultPageSize()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_CREATIVE");
        } catch(Exception e) {
            log.error("Error getting creative", e);
            return createErrorResponse("Failed to get creative: " + e.getMessage(), "GET_CREATIVE");
        }
    }

    // Audience operations
    private MessageDTO createCustomAudience(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnableCustomAudiences()) {
                return createErrorResponse("Custom audiences are not enabled", "CREATE_CUSTOM_AUDIENCE");
            }

            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/dmp/custom_audience/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("custom_audience_name", payload.get("name"));

            // Audience source
            String sourceType = (String) payload.get("sourceType");
            requestBody.put("source_type", sourceType);

            // Handle different source types
            if("FILE".equals(sourceType)) {
                requestBody.put("file_path", payload.get("filePath"));
            } else if("PIXEL".equals(sourceType)) {
                requestBody.put("pixel_id", payload.get("pixelId"));
                requestBody.put("retention_days", payload.get("retentionDays"));
                requestBody.put("rule", payload.get("rule"));
            } else if("ENGAGEMENT".equals(sourceType)) {
                requestBody.put("engagement_type", payload.get("engagementType"));
                requestBody.put("engagement_spec", payload.get("engagementSpec"));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_CUSTOM_AUDIENCE");
        } catch(Exception e) {
            log.error("Error creating custom audience", e);
            return createErrorResponse("Failed to create custom audience: " + e.getMessage(), "CREATE_CUSTOM_AUDIENCE");
        }
    }

    private MessageDTO updateCustomAudience(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/dmp/custom_audience/update/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("custom_audience_id", payload.get("audienceId"));

            if(payload.containsKey("name")) {
                requestBody.put("custom_audience_name", payload.get("name"));
            }
            if(payload.containsKey("filePath")) {
                requestBody.put("file_path", payload.get("filePath"));
                requestBody.put("action", payload.getOrDefault("action", config.getDefaultAudienceAction()));
            }

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_CUSTOM_AUDIENCE");
        } catch(Exception e) {
            log.error("Error updating custom audience", e);
            return createErrorResponse("Failed to update custom audience: " + e.getMessage(), "UPDATE_CUSTOM_AUDIENCE");
        }
    }

    private MessageDTO createLookalikeAudience(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/dmp/lookalike_audience/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("lookalike_name", payload.get("name"));
            requestBody.put("source_audience_id", payload.get("sourceAudienceId"));
            requestBody.put("lookalike_spec", payload.get("spec"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_LOOKALIKE_AUDIENCE");
        } catch(Exception e) {
            log.error("Error creating lookalike audience", e);
            return createErrorResponse("Failed to create lookalike audience: " + e.getMessage(), "CREATE_LOOKALIKE_AUDIENCE");
        }
    }

    private MessageDTO getAudience(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/dmp/custom_audience/list/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            if(payload.containsKey("audienceIds")) {
                params.put("custom_audience_ids", String.join(",", (List<String>) payload.get("audienceIds")));
            }
            params.put("page", payload.getOrDefault("page", 1));
            params.put("page_size", payload.getOrDefault("pageSize", config.getDefaultPageSize()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_AUDIENCE");
        } catch(Exception e) {
            log.error("Error getting audience", e);
            return createErrorResponse("Failed to get audience: " + e.getMessage(), "GET_AUDIENCE");
        }
    }

    // Pixel operations
    private MessageDTO createPixel(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnablePixelTracking()) {
                return createErrorResponse("Pixel tracking is not enabled", "CREATE_PIXEL");
            }

            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/pixel/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("pixel_name", payload.get("name"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_PIXEL");
        } catch(Exception e) {
            log.error("Error creating pixel", e);
            return createErrorResponse("Failed to create pixel: " + e.getMessage(), "CREATE_PIXEL");
        }
    }

    private MessageDTO getPixel(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/pixel/list/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            if(payload.containsKey("pixelIds")) {
                params.put("pixel_ids", String.join(",", (List<String>) payload.get("pixelIds")));
            }
            params.put("page", payload.getOrDefault("page", 1));
            params.put("page_size", payload.getOrDefault("pageSize", config.getDefaultPageSize()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_PIXEL");
        } catch(Exception e) {
            log.error("Error getting pixel", e);
            return createErrorResponse("Failed to get pixel: " + e.getMessage(), "GET_PIXEL");
        }
    }

    private MessageDTO sendPixelEvent(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnablePixelTracking()) {
                return createErrorResponse("Pixel tracking is not enabled", "SEND_PIXEL_EVENT");
            }

            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/pixel/track/";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            requestBody.put("pixel_code", payload.get("pixelCode"));
            requestBody.put("event", payload.get("event"));

            // Event data
            Map<String, Object> data = new HashMap<>();
            data.put("event_time", System.currentTimeMillis() / 1000); // Unix timestamp

            if(payload.containsKey("eventId")) {
                data.put("event_id", payload.get("eventId"));
            }
            if(payload.containsKey("value")) {
                data.put("value", payload.get("value"));
            }
            if(payload.containsKey("currency")) {
                data.put("currency", payload.get("currency"));
            }

            // User data
            if(payload.containsKey("userData")) {
                data.put("user", payload.get("userData"));
            }

            // Custom data
            if(payload.containsKey("customData")) {
                data.put("custom", payload.get("customData"));
            }

            requestBody.put("data", Arrays.asList(data));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "SEND_PIXEL_EVENT");
        } catch(Exception e) {
            log.error("Error sending pixel event", e);
            return createErrorResponse("Failed to send pixel event: " + e.getMessage(), "SEND_PIXEL_EVENT");
        }
    }

    // Reporting operations
    private MessageDTO getReport(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnableReporting()) {
                return createErrorResponse("Reporting is not enabled", "GET_REPORT");
            }

            rateLimiterService.acquire("tiktok_business_api", 2);

            String url = config.getApiBaseUrl() + "/report/integrated/get/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            // Report type
            requestBody.put("report_type", payload.getOrDefault("reportType", config.getDefaultReportType()));
            requestBody.put("data_level", payload.getOrDefault("dataLevel", config.getDefaultDataLevel()));

            // Dimensions and metrics
            requestBody.put("dimensions", payload.get("dimensions"));
            requestBody.put("metrics", payload.get("metrics"));

            // Date range
            requestBody.put("start_date", payload.get("startDate"));
            requestBody.put("end_date", payload.get("endDate"));

            // Filters
            if(payload.containsKey("filters")) {
                requestBody.put("filters", payload.get("filters"));
            }

            // Pagination
            requestBody.put("page", payload.getOrDefault("page", 1));
            requestBody.put("page_size", payload.getOrDefault("pageSize", config.getDefaultReportPageSize()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, requestBody, null);
            return createResponseMessage(response, "GET_REPORT");
        } catch(Exception e) {
            log.error("Error getting report", e);
            return createErrorResponse("Failed to get report: " + e.getMessage(), "GET_REPORT");
        }
    }

    private MessageDTO getAudienceInsights(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/audience_insights/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("custom_audience_ids", payload.get("audienceIds"));
            requestBody.put("dimension", payload.getOrDefault("dimension", "GENDER"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "GET_AUDIENCE_INSIGHTS");
        } catch(Exception e) {
            log.error("Error getting audience insights", e);
            return createErrorResponse("Failed to get audience insights: " + e.getMessage(), "GET_AUDIENCE_INSIGHTS");
        }
    }

    private MessageDTO getCreativeInsights(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/creative/insights/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            params.put("creative_ids", String.join(",", (List<String>) payload.get("creativeIds")));
            params.put("start_date", payload.get("startDate"));
            params.put("end_date", payload.get("endDate"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_CREATIVE_INSIGHTS");
        } catch(Exception e) {
            log.error("Error getting creative insights", e);
            return createErrorResponse("Failed to get creative insights: " + e.getMessage(), "GET_CREATIVE_INSIGHTS");
        }
    }

    // Spark Ads operations
    private MessageDTO createSparkAd(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnableSparkAds()) {
                return createErrorResponse("Spark Ads are not enabled", "CREATE_SPARK_AD");
            }

            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/tt_video/info/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            params.put("item_ids", String.join(",", (List<String>) payload.get("videoIds")));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "CREATE_SPARK_AD");
        } catch(Exception e) {
            log.error("Error creating spark ad", e);
            return createErrorResponse("Failed to create spark ad: " + e.getMessage(), "CREATE_SPARK_AD");
        }
    }

    private MessageDTO authorizeSparkAd(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/bc_creator/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("bc_id", payload.get("creatorId"));
            requestBody.put("video_item_ids", payload.get("videoIds"));
            requestBody.put("code_type", payload.getOrDefault("codeType", config.getDefaultCodeType()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "AUTHORIZE_SPARK_AD");
        } catch(Exception e) {
            log.error("Error authorizing spark ad", e);
            return createErrorResponse("Failed to authorize spark ad: " + e.getMessage(), "AUTHORIZE_SPARK_AD");
        }
    }

    // Catalog operations
    private MessageDTO createCatalog(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnableCatalogManagement()) {
                return createErrorResponse("Catalog management is not enabled", "CREATE_CATALOG");
            }

            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/catalog/create/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bc_id", config.getBusinessId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("catalog_name", payload.get("name"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_CATALOG");
        } catch(Exception e) {
            log.error("Error creating catalog", e);
            return createErrorResponse("Failed to create catalog: " + e.getMessage(), "CREATE_CATALOG");
        }
    }

    private MessageDTO updateCatalog(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/catalog/update/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bc_id", config.getBusinessId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("catalog_id", payload.get("catalogId"));
            requestBody.put("catalog_name", payload.get("name"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_CATALOG");
        } catch(Exception e) {
            log.error("Error updating catalog", e);
            return createErrorResponse("Failed to update catalog: " + e.getMessage(), "UPDATE_CATALOG");
        }
    }

    private MessageDTO createProduct(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/catalog/product/upload/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bc_id", config.getBusinessId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("catalog_id", payload.get("catalogId"));
            requestBody.put("products", payload.get("products"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "CREATE_PRODUCT");
        } catch(Exception e) {
            log.error("Error creating product", e);
            return createErrorResponse("Failed to create product: " + e.getMessage(), "CREATE_PRODUCT");
        }
    }

    private MessageDTO updateProduct(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/catalog/product/update/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bc_id", config.getBusinessId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("catalog_id", payload.get("catalogId"));
            requestBody.put("products", payload.get("products"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_PRODUCT");
        } catch(Exception e) {
            log.error("Error updating product", e);
            return createErrorResponse("Failed to update product: " + e.getMessage(), "UPDATE_PRODUCT");
        }
    }

    // Budget operations
    private MessageDTO updateBudget(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/budget/update/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            String entityType = (String) payload.get("entityType");
            if("CAMPAIGN".equals(entityType)) {
                requestBody.put("campaign_ids", payload.get("entityIds"));
            } else if("ADGROUP".equals(entityType)) {
                requestBody.put("adgroup_ids", payload.get("entityIds"));
            }

            requestBody.put("budget", payload.get("budget"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "UPDATE_BUDGET");
        } catch(Exception e) {
            log.error("Error updating budget", e);
            return createErrorResponse("Failed to update budget: " + e.getMessage(), "UPDATE_BUDGET");
        }
    }

    private MessageDTO getBudgetRecommendation(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/tool/budget_recommend/";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            requestBody.put("objective_type", payload.get("objective"));
            requestBody.put("billing_event", payload.get("billingEvent"));
            requestBody.put("targeting", payload.get("targeting"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, requestBody);
            return createResponseMessage(response, "GET_BUDGET_RECOMMENDATION");
        } catch(Exception e) {
            log.error("Error getting budget recommendation", e);
            return createErrorResponse("Failed to get budget recommendation: " + e.getMessage(), "GET_BUDGET_RECOMMENDATION");
        }
    }

    // Targeting operations
    private MessageDTO getTargetingTags(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/tool/targeting_tag/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            params.put("scene", payload.getOrDefault("scene", config.getDefaultTargetingScene()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_TARGETING_TAGS");
        } catch(Exception e) {
            log.error("Error getting targeting tags", e);
            return createErrorResponse("Failed to get targeting tags: " + e.getMessage(), "GET_TARGETING_TAGS");
        }
    }

    private MessageDTO getInterestCategories(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/tool/interest_category/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            params.put("language", payload.getOrDefault("language", "en"));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_INTEREST_CATEGORIES");
        } catch(Exception e) {
            log.error("Error getting interest categories", e);
            return createErrorResponse("Failed to get interest categories: " + e.getMessage(), "GET_INTEREST_CATEGORIES");
        }
    }

    private MessageDTO getBehaviorCategories(MessageDTO message) {
        try {
            rateLimiterService.acquire("tiktok_business_api", 1);

            String url = config.getApiBaseUrl() + "/tool/action_category/get/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());

            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            params.put("language", payload.getOrDefault("language", "en"));
            params.put("scene", payload.getOrDefault("scene", config.getDefaultBehaviorScene()));

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);
            return createResponseMessage(response, "GET_BEHAVIOR_CATEGORIES");
        } catch(Exception e) {
            log.error("Error getting behavior categories", e);
            return createErrorResponse("Failed to get behavior categories: " + e.getMessage(), "GET_BEHAVIOR_CATEGORIES");
        }
    }

    // Helper methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, Object> params, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access - Token", getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if(params != null && method == HttpMethod.GET) {
            params.forEach((key, value) -> builder.queryParam(key, value.toString()));
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

    private MessageDTO createResponseMessage(ResponseEntity<String> response, String operation) {
        MessageDTO responseMessageDTO = new MessageDTO();
        responseMessageDTO.setCorrelationId(UUID.randomUUID().toString());
        responseMessageDTO.setTimestamp(LocalDateTime.now());
        responseMessageDTO.setStatus(response.getStatusCode().is2xxSuccessful() ? MessageStatus.SUCCESS : MessageStatus.FAILED);
        responseMessageDTO.setHeaders(Map.of(
            "operation", operation,
            "statusCode", response.getStatusCode().value(),
            "source", "tiktok_business"
       ));
        responseMessageDTO.setPayload(response.getBody());

        return responseMessageDTO;
    }

    private MessageDTO createErrorResponse(String errorMessage, String operation) {
        MessageDTO errorResponse = new MessageDTO();
        errorResponse.setCorrelationId(UUID.randomUUID().toString());
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(MessageStatus.FAILED);
        errorResponse.setHeaders(Map.of(
            "operation", operation,
            "error", errorMessage,
            "source", "tiktok_business"
        ));
        errorResponse.setPayload("{\"error\": \"" + errorMessage + "\"}");
        return errorResponse;
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
                    config.getApiBaseUrl() + "/oauth2/refresh_token/"
               );
            }
        } catch(Exception e) {
            log.error("Error refreshing TikTok Business access token", e);
        }
    }

    public MessageDTO sendMessage(MessageDTO message) {
        return processMessage(message);
    }

    public void setConfiguration(TikTokBusinessApiConfig config) {
        this.config = config;
    }

    private boolean validateConfiguration() {
        if(config == null) {
            log.error("TikTok Business configuration is not set");
            return false;
        }
        if(config.getAppId() == null || config.getAppSecret() == null) {
            log.error("TikTok app ID and secret are required");
            return false;
        }
        if(config.getAdvertiserId() == null) {
            log.error("TikTok advertiser ID is required");
            return false;
        }
        if(config.getAccessToken() == null) {
            log.error("TikTok access token is required");
            return false;
        }
        return true;
    }

    // Additional methods referenced in switch statement
    private MessageDTO getCampaigns(MessageDTO message) {
        return getCampaign(message);
    }

    private MessageDTO deleteCampaign(MessageDTO message) {
        return updateCampaignStatus(message);
    }

    private MessageDTO getAdGroups(MessageDTO message) {
        return getAdGroup(message);
    }

    private MessageDTO deleteAdGroup(MessageDTO message) {
        return updateAdGroupStatus(message);
    }

    private MessageDTO getAds(MessageDTO message) {
        return getAd(message);
    }

    private MessageDTO deleteAd(MessageDTO message) {
        return updateAdStatus(message);
    }

    private MessageDTO updateCreative(MessageDTO message) {
        return createCreative(message);
    }

    private MessageDTO getCreatives(MessageDTO message) {
        return getCreative(message);
    }

    private MessageDTO deleteCreative(MessageDTO message) {
        return createCreative(message);
    }

    private MessageDTO getAssets(MessageDTO message) {
        return getCreative(message);
    }

    private MessageDTO createAudience(MessageDTO message) {
        return createCustomAudience(message);
    }

    private MessageDTO updateAudience(MessageDTO message) {
        return updateCustomAudience(message);
    }

    private MessageDTO getAudiences(MessageDTO message) {
        return getAudience(message);
    }

    private MessageDTO deleteAudience(MessageDTO message) {
        return updateCustomAudience(message);
    }

    // Note: createPixel method already exists above in the file

    private MessageDTO updatePixel(MessageDTO message) {
        return getPixel(message);
    }

    private MessageDTO getPixels(MessageDTO message) {
        return getPixel(message);
    }

    private MessageDTO deletePixel(MessageDTO message) {
        return getPixel(message);
    }

    private MessageDTO getCampaignInsights(MessageDTO message) {
        return getReport(message);
    }

    private MessageDTO getAdGroupInsights(MessageDTO message) {
        return getReport(message);
    }

    private MessageDTO getAdInsights(MessageDTO message) {
        return getReport(message);
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("advertiserId", config.getAdvertiserId());
            configMap.put("appId", config.getAppId());
            configMap.put("appSecret", config.getAppSecret());
            configMap.put("accessToken", config.getAccessToken());
            configMap.put("refreshToken", config.getRefreshToken());
            configMap.put("apiBaseUrl", config.getApiBaseUrl());
            configMap.put("apiVersion", config.getApiVersion());
        }
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected AdapterResult doReceive(Object criteria) {
        // Outbound adapter does not support receiving
        return AdapterResult.success(null, "Outbound adapter does not support receiving");
    }

    @Override
    protected void doReceiverInitialize() {
        log.debug("Initializing TikTok Business receiver");
    }

    @Override
    protected void doReceiverDestroy() {
        log.debug("Destroying TikTok Business receiver");
    }

    @Override
    protected long getPollingIntervalMs() {
        return config.getDefaultPollingInterval();
    }

    @Override
    protected AdapterResult doTestConnection() {
        try {
            // Validate configuration first
            if (!validateConfiguration()) {
                return AdapterResult.failure("Invalid TikTok Business configuration");
            }

            // Test connection by getting advertiser info
            String url = config.getApiBaseUrl() + "/advertiser/info/";

            Map<String, Object> params = new HashMap<>();
            params.put("advertiser_id", config.getAdvertiserId());
            params.put("fields", "id,name,status");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params, null);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "TikTok Business API connection successful");
            } else {
                return AdapterResult.failure("TikTok Business API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error testing TikTok Business connection", e);
            return AdapterResult.failure("Failed to test TikTok Business connection: " + e.getMessage());
        }
    }
}
