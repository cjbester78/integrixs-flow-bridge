package com.integrixs.adapters.social.pinterest;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.platform.models.Message;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.service.RateLimiterService;
import com.integrixs.shared.utils.CredentialEncryptionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Outbound adapter for Pinterest API integration.
 * Handles pin creation, board management, advertising, and analytics operations.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "integrixs.adapters.pinterest.enabled", havingValue = "true", matchIfMissing = false)
public class PinterestOutboundAdapter extends AbstractSocialMediaOutboundAdapter {

    private final PinterestApiConfig config;
    private final RestTemplate restTemplate;

    @Autowired
    public PinterestOutboundAdapter(
            PinterestApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate) {
        super(rateLimiterService, credentialEncryptionService);
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public AdapterConfig getAdapterConfig() {
        return config;
    }

    @Override
    public Message processMessage(Message message) {
        String action = message.getHeader("action");
        
        try {
            switch (action) {
                // Pin Management
                case "CREATE_PIN":
                    return createPin(message);
                case "UPDATE_PIN":
                    return updatePin(message);
                case "DELETE_PIN":
                    return deletePin(message);
                case "GET_PIN":
                    return getPin(message);
                case "SAVE_PIN":
                    return savePin(message);
                    
                // Board Management
                case "CREATE_BOARD":
                    return createBoard(message);
                case "UPDATE_BOARD":
                    return updateBoard(message);
                case "DELETE_BOARD":
                    return deleteBoard(message);
                case "GET_BOARD":
                    return getBoard(message);
                case "CREATE_BOARD_SECTION":
                    return createBoardSection(message);
                case "UPDATE_BOARD_SECTION":
                    return updateBoardSection(message);
                case "DELETE_BOARD_SECTION":
                    return deleteBoardSection(message);
                    
                // Shopping & Products
                case "CREATE_PRODUCT_PIN":
                    return createProductPin(message);
                case "TAG_PRODUCTS":
                    return tagProducts(message);
                case "CREATE_CATALOG":
                    return createCatalog(message);
                case "UPDATE_CATALOG":
                    return updateCatalog(message);
                case "UPLOAD_PRODUCT_FEED":
                    return uploadProductFeed(message);
                    
                // Analytics
                case "GET_USER_ANALYTICS":
                    return getUserAnalytics(message);
                case "GET_PIN_ANALYTICS":
                    return getPinAnalytics(message);
                case "GET_BOARD_ANALYTICS":
                    return getBoardAnalytics(message);
                case "GENERATE_REPORT":
                    return generateReport(message);
                    
                // Advertising
                case "CREATE_CAMPAIGN":
                    return createCampaign(message);
                case "UPDATE_CAMPAIGN":
                    return updateCampaign(message);
                case "CREATE_AD_GROUP":
                    return createAdGroup(message);
                case "UPDATE_AD_GROUP":
                    return updateAdGroup(message);
                case "CREATE_AD":
                    return createAd(message);
                case "UPDATE_AD":
                    return updateAd(message);
                case "CREATE_AUDIENCE":
                    return createAudience(message);
                case "UPDATE_AUDIENCE":
                    return updateAudience(message);
                    
                // Bulk Operations
                case "BULK_CREATE_PINS":
                    return bulkCreatePins(message);
                case "BULK_DELETE_PINS":
                    return bulkDeletePins(message);
                    
                // Search & Discovery
                case "SEARCH_PINS":
                    return searchPins(message);
                case "SEARCH_BOARDS":
                    return searchBoards(message);
                case "SEARCH_USERS":
                    return searchUsers(message);
                case "GET_TRENDING":
                    return getTrending(message);
                    
                // User Operations
                case "GET_USER_INFO":
                    return getUserInfo(message);
                case "UPDATE_USER_INFO":
                    return updateUserInfo(message);
                case "FOLLOW_USER":
                    return followUser(message);
                case "UNFOLLOW_USER":
                    return unfollowUser(message);
                case "GET_FOLLOWERS":
                    return getFollowers(message);
                case "GET_FOLLOWING":
                    return getFollowing(message);
                    
                // Media Upload
                case "UPLOAD_IMAGE":
                    return uploadImage(message);
                case "UPLOAD_VIDEO":
                    return uploadVideo(message);
                    
                default:
                    throw new UnsupportedOperationException("Unknown action: " + action);
            }
        } catch (Exception e) {
            return createErrorResponse(message, e.getMessage());
        }
    }

    private Message createPin(Message message) throws Exception {
        Map<String, Object> pinData = message.getPayloadAsMap();
        
        // Validate required fields
        validateRequiredFields(pinData, Arrays.asList("board_id", "media_source"));
        
        Map<String, Object> request = new HashMap<>();
        request.put("board_id", pinData.get("board_id"));
        request.put("media_source", pinData.get("media_source"));
        
        // Optional fields
        addOptionalField(request, pinData, "title");
        addOptionalField(request, pinData, "description");
        addOptionalField(request, pinData, "link");
        addOptionalField(request, pinData, "alt_text");
        addOptionalField(request, pinData, "board_section_id");
        addOptionalField(request, pinData, "dominant_color");
        
        String url = config.getApiUrl() + "/v5/pins";
        String response = executeApiCall(() -> makePostRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message updatePin(Message message) throws Exception {
        String pinId = message.getHeader("pinId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/pins/" + pinId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message deletePin(Message message) throws Exception {
        String pinId = message.getHeader("pinId");
        
        String url = config.getApiUrl() + "/v5/pins/" + pinId;
        String response = executeApiCall(() -> makeDeleteRequest(url));
        
        return createSuccessResponse(message, response);
    }

    private Message getPin(Message message) throws Exception {
        String pinId = message.getHeader("pinId");
        
        String url = config.getApiUrl() + "/v5/pins/" + pinId;
        Map<String, String> params = new HashMap<>();
        params.put("pin_metrics", "true");
        params.put("ad_account_id", config.getAdvertiserId());
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message savePin(Message message) throws Exception {
        String pinId = message.getHeader("pinId");
        String boardId = message.getHeader("boardId");
        String boardSectionId = message.getHeader("boardSectionId");
        
        Map<String, Object> request = new HashMap<>();
        request.put("board_id", boardId);
        if (boardSectionId != null) {
            request.put("board_section_id", boardSectionId);
        }
        
        String url = config.getApiUrl() + "/v5/pins/" + pinId + "/save";
        String response = executeApiCall(() -> makePostRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message createBoard(Message message) throws Exception {
        Map<String, Object> boardData = message.getPayloadAsMap();
        
        validateRequiredFields(boardData, Arrays.asList("name"));
        
        Map<String, Object> request = new HashMap<>();
        request.put("name", boardData.get("name"));
        
        addOptionalField(request, boardData, "description");
        addOptionalField(request, boardData, "privacy");
        
        String url = config.getApiUrl() + "/v5/boards";
        String response = executeApiCall(() -> makePostRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message updateBoard(Message message) throws Exception {
        String boardId = message.getHeader("boardId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/boards/" + boardId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message deleteBoard(Message message) throws Exception {
        String boardId = message.getHeader("boardId");
        
        String url = config.getApiUrl() + "/v5/boards/" + boardId;
        String response = executeApiCall(() -> makeDeleteRequest(url));
        
        return createSuccessResponse(message, response);
    }

    private Message getBoard(Message message) throws Exception {
        String boardId = message.getHeader("boardId");
        
        String url = config.getApiUrl() + "/v5/boards/" + boardId;
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));
        
        return createSuccessResponse(message, response);
    }

    private Message createBoardSection(Message message) throws Exception {
        String boardId = message.getHeader("boardId");
        Map<String, Object> sectionData = message.getPayloadAsMap();
        
        validateRequiredFields(sectionData, Arrays.asList("name"));
        
        String url = config.getApiUrl() + "/v5/boards/" + boardId + "/sections";
        String response = executeApiCall(() -> makePostRequest(url, sectionData));
        
        return createSuccessResponse(message, response);
    }

    private Message updateBoardSection(Message message) throws Exception {
        String boardId = message.getHeader("boardId");
        String sectionId = message.getHeader("sectionId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/boards/" + boardId + "/sections/" + sectionId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message deleteBoardSection(Message message) throws Exception {
        String boardId = message.getHeader("boardId");
        String sectionId = message.getHeader("sectionId");
        
        String url = config.getApiUrl() + "/v5/boards/" + boardId + "/sections/" + sectionId;
        String response = executeApiCall(() -> makeDeleteRequest(url));
        
        return createSuccessResponse(message, response);
    }

    private Message createProductPin(Message message) throws Exception {
        Map<String, Object> productPinData = message.getPayloadAsMap();
        
        validateRequiredFields(productPinData, Arrays.asList("board_id", "media_source", "product_tags"));
        
        // Create pin with product tags
        String url = config.getApiUrl() + "/v5/pins";
        String response = executeApiCall(() -> makePostRequest(url, productPinData));
        
        return createSuccessResponse(message, response);
    }

    private Message tagProducts(Message message) throws Exception {
        String pinId = message.getHeader("pinId");
        List<Map<String, Object>> productTags = (List<Map<String, Object>>) message.getPayloadAsMap().get("product_tags");
        
        Map<String, Object> request = Map.of("product_tags", productTags);
        
        String url = config.getApiUrl() + "/v5/pins/" + pinId;
        String response = executeApiCall(() -> makePatchRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message createCatalog(Message message) throws Exception {
        Map<String, Object> catalogData = message.getPayloadAsMap();
        
        validateRequiredFields(catalogData, Arrays.asList("name", "format"));
        
        String url = config.getApiUrl() + "/v5/catalogs";
        String response = executeApiCall(() -> makePostRequest(url, catalogData));
        
        return createSuccessResponse(message, response);
    }

    private Message updateCatalog(Message message) throws Exception {
        String catalogId = message.getHeader("catalogId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/catalogs/" + catalogId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message uploadProductFeed(Message message) throws Exception {
        String catalogId = message.getHeader("catalogId");
        Map<String, Object> feedData = message.getPayloadAsMap();
        
        validateRequiredFields(feedData, Arrays.asList("format", "location"));
        
        String url = config.getApiUrl() + "/v5/catalogs/" + catalogId + "/feeds";
        String response = executeApiCall(() -> makePostRequest(url, feedData));
        
        return createSuccessResponse(message, response);
    }

    private Message getUserAnalytics(Message message) throws Exception {
        Map<String, String> params = buildAnalyticsParams(message);
        
        String url = config.getApiUrl() + "/v5/user_account/analytics";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message getPinAnalytics(Message message) throws Exception {
        String pinId = message.getHeader("pinId");
        Map<String, String> params = buildAnalyticsParams(message);
        
        String url = config.getApiUrl() + "/v5/pins/" + pinId + "/analytics";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message getBoardAnalytics(Message message) throws Exception {
        String boardId = message.getHeader("boardId");
        Map<String, String> params = buildAnalyticsParams(message);
        
        String url = config.getApiUrl() + "/v5/boards/" + boardId + "/analytics";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message generateReport(Message message) throws Exception {
        String reportType = message.getHeader("reportType");
        Map<String, Object> reportParams = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/reports";
        String response = executeApiCall(() -> makePostRequest(url, reportParams));
        
        return createSuccessResponse(message, response);
    }

    private Message createCampaign(Message message) throws Exception {
        Map<String, Object> campaignData = message.getPayloadAsMap();
        
        validateRequiredFields(campaignData, Arrays.asList("name", "objective_type", "daily_spend_cap"));
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/campaigns";
        String response = executeApiCall(() -> makePostRequest(url, campaignData));
        
        return createSuccessResponse(message, response);
    }

    private Message updateCampaign(Message message) throws Exception {
        String campaignId = message.getHeader("campaignId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/campaigns/" + campaignId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message createAdGroup(Message message) throws Exception {
        Map<String, Object> adGroupData = message.getPayloadAsMap();
        
        validateRequiredFields(adGroupData, Arrays.asList("campaign_id", "name", "budget_in_micro_currency", "bid_in_micro_currency"));
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ad_groups";
        String response = executeApiCall(() -> makePostRequest(url, adGroupData));
        
        return createSuccessResponse(message, response);
    }

    private Message updateAdGroup(Message message) throws Exception {
        String adGroupId = message.getHeader("adGroupId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ad_groups/" + adGroupId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message createAd(Message message) throws Exception {
        Map<String, Object> adData = message.getPayloadAsMap();
        
        validateRequiredFields(adData, Arrays.asList("ad_group_id", "creative_type", "pin_id"));
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ads";
        String response = executeApiCall(() -> makePostRequest(url, adData));
        
        return createSuccessResponse(message, response);
    }

    private Message updateAd(Message message) throws Exception {
        String adId = message.getHeader("adId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ads/" + adId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message createAudience(Message message) throws Exception {
        Map<String, Object> audienceData = message.getPayloadAsMap();
        
        validateRequiredFields(audienceData, Arrays.asList("name", "rule"));
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/audiences";
        String response = executeApiCall(() -> makePostRequest(url, audienceData));
        
        return createSuccessResponse(message, response);
    }

    private Message updateAudience(Message message) throws Exception {
        String audienceId = message.getHeader("audienceId");
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/audiences/" + audienceId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message bulkCreatePins(Message message) throws Exception {
        List<Map<String, Object>> pins = (List<Map<String, Object>>) message.getPayloadAsMap().get("pins");
        
        if (pins.size() > config.getLimits().getMaxBulkPinsPerRequest()) {
            throw new IllegalArgumentException("Too many pins. Maximum allowed: " + config.getLimits().getMaxBulkPinsPerRequest());
        }
        
        Map<String, Object> request = Map.of("pins", pins);
        
        String url = config.getApiUrl() + "/v5/pins/bulk";
        String response = executeApiCall(() -> makePostRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message bulkDeletePins(Message message) throws Exception {
        List<String> pinIds = (List<String>) message.getPayloadAsMap().get("pin_ids");
        
        Map<String, Object> request = Map.of("pin_ids", pinIds);
        
        String url = config.getApiUrl() + "/v5/pins/bulk/delete";
        String response = executeApiCall(() -> makePostRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message searchPins(Message message) throws Exception {
        String query = message.getHeader("query");
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("page_size", message.getHeader("pageSize", "25"));
        
        addOptionalParam(params, message, "bookmark");
        
        String url = config.getApiUrl() + "/v5/search/pins";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message searchBoards(Message message) throws Exception {
        String query = message.getHeader("query");
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("page_size", message.getHeader("pageSize", "25"));
        
        String url = config.getApiUrl() + "/v5/search/boards";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message searchUsers(Message message) throws Exception {
        String query = message.getHeader("query");
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        
        String url = config.getApiUrl() + "/v5/search/user_account";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message getTrending(Message message) throws Exception {
        String trendingType = message.getHeader("trendingType", "searches");
        String region = message.getHeader("region", "US");
        
        String url = config.getApiUrl() + "/v5/trends/" + trendingType + "/" + region;
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));
        
        return createSuccessResponse(message, response);
    }

    private Message getUserInfo(Message message) throws Exception {
        String url = config.getApiUrl() + "/v5/user_account";
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));
        
        return createSuccessResponse(message, response);
    }

    private Message updateUserInfo(Message message) throws Exception {
        Map<String, Object> updateData = message.getPayloadAsMap();
        
        String url = config.getApiUrl() + "/v5/user_account";
        String response = executeApiCall(() -> makePatchRequest(url, updateData));
        
        return createSuccessResponse(message, response);
    }

    private Message followUser(Message message) throws Exception {
        String username = message.getHeader("username");
        
        Map<String, Object> request = Map.of("auto_follow", false);
        
        String url = config.getApiUrl() + "/v5/user_account/following/" + username;
        String response = executeApiCall(() -> makePutRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message unfollowUser(Message message) throws Exception {
        String username = message.getHeader("username");
        
        String url = config.getApiUrl() + "/v5/user_account/following/" + username;
        String response = executeApiCall(() -> makeDeleteRequest(url));
        
        return createSuccessResponse(message, response);
    }

    private Message getFollowers(Message message) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("page_size", message.getHeader("pageSize", "25"));
        addOptionalParam(params, message, "bookmark");
        
        String url = config.getApiUrl() + "/v5/user_account/followers";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message getFollowing(Message message) throws Exception {
        String followingType = message.getHeader("followingType", "USERS");
        Map<String, String> params = new HashMap<>();
        params.put("feed_type", followingType);
        params.put("page_size", message.getHeader("pageSize", "25"));
        addOptionalParam(params, message, "bookmark");
        
        String url = config.getApiUrl() + "/v5/user_account/following";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message uploadImage(Message message) throws Exception {
        byte[] imageData = message.getPayloadAsBytes();
        String mediaType = message.getHeader("mediaType", "image/jpeg");
        
        // Validate image size
        if (imageData.length > config.getLimits().getMaxImageSizeMB() * 1024 * 1024) {
            throw new IllegalArgumentException("Image size exceeds maximum allowed: " + config.getLimits().getMaxImageSizeMB() + "MB");
        }
        
        // Register media upload
        Map<String, Object> registerRequest = Map.of("media_type", mediaType);
        String registerUrl = config.getApiUrl() + "/v5/media";
        String registerResponse = executeApiCall(() -> makePostRequest(registerUrl, registerRequest));
        Map<String, Object> uploadData = parseJsonResponse(registerResponse);
        
        // Upload to provided URL
        String uploadUrl = (String) uploadData.get("upload_url");
        String uploadId = (String) uploadData.get("media_id");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        HttpEntity<byte[]> entity = new HttpEntity<>(imageData, headers);
        
        ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.PUT, entity, String.class);
        
        if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload image: " + uploadResponse.getStatusCode());
        }
        
        return createSuccessResponse(message, Map.of("media_id", uploadId));
    }

    private Message uploadVideo(Message message) throws Exception {
        byte[] videoData = message.getPayloadAsBytes();
        String mediaType = message.getHeader("mediaType", "video/mp4");
        
        // Validate video size
        if (videoData.length > config.getLimits().getMaxVideoSizeMB() * 1024 * 1024) {
            throw new IllegalArgumentException("Video size exceeds maximum allowed: " + config.getLimits().getMaxVideoSizeMB() + "MB");
        }
        
        // Similar upload process as image
        Map<String, Object> registerRequest = Map.of("media_type", mediaType);
        String registerUrl = config.getApiUrl() + "/v5/media";
        String registerResponse = executeApiCall(() -> makePostRequest(registerUrl, registerRequest));
        Map<String, Object> uploadData = parseJsonResponse(registerResponse);
        
        String uploadUrl = (String) uploadData.get("upload_url");
        String uploadId = (String) uploadData.get("media_id");
        
        // Upload video
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        HttpEntity<byte[]> entity = new HttpEntity<>(videoData, headers);
        
        ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.PUT, entity, String.class);
        
        if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload video: " + uploadResponse.getStatusCode());
        }
        
        return createSuccessResponse(message, Map.of("media_id", uploadId));
    }

    // Helper methods
    private Map<String, String> buildAnalyticsParams(Message message) {
        Map<String, String> params = new HashMap<>();
        params.put("start_date", message.getHeader("startDate"));
        params.put("end_date", message.getHeader("endDate"));
        
        String metrics = message.getHeader("metrics");
        if (metrics != null) {
            params.put("metric_types", metrics);
        }
        
        String granularity = message.getHeader("granularity");
        if (granularity != null) {
            params.put("granularity", granularity);
        }
        
        return params;
    }

    private void validateRequiredFields(Map<String, Object> data, List<String> requiredFields) {
        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }
    }

    private void addOptionalField(Map<String, Object> target, Map<String, Object> source, String field) {
        if (source.containsKey(field) && source.get(field) != null) {
            target.put(field, source.get(field));
        }
    }

    private void addOptionalParam(Map<String, String> params, Message message, String param) {
        String value = message.getHeader(param);
        if (value != null) {
            params.put(param, value);
        }
    }

    private String makePostRequest(String url, Map<String, Object> data) throws Exception {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String makePatchRequest(String url, Map<String, Object> data) throws Exception {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
        return response.getBody();
    }

    private String makePutRequest(String url, Map<String, Object> data) throws Exception {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        return response.getBody();
    }

    private String makeGetRequest(String url, Map<String, String> params) throws Exception {
        HttpHeaders headers = createHeaders();
        
        StringBuilder urlWithParams = new StringBuilder(url);
        if (!params.isEmpty()) {
            urlWithParams.append("?");
            params.forEach((key, value) -> urlWithParams.append(key).append("=").append(value).append("&"));
            urlWithParams.deleteCharAt(urlWithParams.length() - 1);
        }
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(urlWithParams.toString(), HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    private String makeDeleteRequest(String url) throws Exception {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        return response.getBody();
    }

    private HttpHeaders createHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getDecryptedCredential("accessToken"));
        return headers;
    }

    @Override
    protected String getAdapterType() {
        return "PINTEREST";
    }
}