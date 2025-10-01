package com.integrixs.adapters.social.pinterest;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
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
@Component
@ConditionalOnProperty(name = "integrixs.adapters.pinterest.enabled", havingValue = "true", matchIfMissing = false)
public class PinterestOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(PinterestOutboundAdapter.class);


    private final PinterestApiConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("appId", config.getAppId());
            configMap.put("appSecret", config.getAppSecret());
            configMap.put("accessToken", config.getAccessToken());
            configMap.put("apiVersion", config.getApiVersion());
            configMap.put("enabled", config.isEnabled());
            configMap.put("apiBaseUrl", config.getApiBaseUrl());
            configMap.put("advertiserId", config.getAdvertiserId());
        }
        return configMap;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        String action = getHeader(message, "action");

        try {
            switch(action) {
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
                    log.warn("Unknown action received: {}", action);
                    return message;
            }
        } catch(Exception e) {
            return createErrorResponse(message, e.getMessage());
        }
    }

    private MessageDTO createPin(MessageDTO message) throws Exception {
        Map<String, Object> pinData = getPayloadAsMap(message);

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

        String url = getApiUrl() + "/v5/pins";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "CREATE_PIN");
    }

    private MessageDTO updatePin(MessageDTO message) throws Exception {
        String pinId = getHeader(message, "pinId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/pins/" + pinId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "UPDATE_PIN");
    }

    private MessageDTO deletePin(MessageDTO message) throws Exception {
        String pinId = getHeader(message, "pinId");

        String url = getApiUrl() + "/v5/pins/" + pinId;
        String response = executeApiCall(() -> makeDeleteRequest(url));

        return createSuccessResponse(message.getCorrelationId(), response, "DELETE_PIN");
    }

    private MessageDTO getPin(MessageDTO message) throws Exception {
        String pinId = getHeader(message, "pinId");

        String url = getApiUrl() + "/v5/pins/" + pinId;
        Map<String, String> params = new HashMap<>();
        params.put("pin_metrics", "true");
        params.put("ad_account_id", config.getAdvertiserId());

        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "GET_PIN");
    }

    private MessageDTO savePin(MessageDTO message) throws Exception {
        String pinId = getHeader(message, "pinId");
        String boardId = getHeader(message, "boardId");
        String boardSectionId = getHeader(message, "boardSectionId");

        Map<String, Object> request = new HashMap<>();
        request.put("board_id", boardId);
        if(boardSectionId != null) {
            request.put("board_section_id", boardSectionId);
        }

        String url = getApiUrl() + "/v5/pins/" + pinId + "/save";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "SAVE_PIN");
    }

    private MessageDTO createBoard(MessageDTO message) throws Exception {
        Map<String, Object> boardData = getPayloadAsMap(message);

        validateRequiredFields(boardData, Arrays.asList("name"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", boardData.get("name"));

        addOptionalField(request, boardData, "description");
        addOptionalField(request, boardData, "privacy");

        String url = getApiUrl() + "/v5/boards";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "CREATE_BOARD");
    }

    private MessageDTO updateBoard(MessageDTO message) throws Exception {
        String boardId = getHeader(message, "boardId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/boards/" + boardId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO deleteBoard(MessageDTO message) throws Exception {
        String boardId = getHeader(message, "boardId");

        String url = getApiUrl() + "/v5/boards/" + boardId;
        String response = executeApiCall(() -> makeDeleteRequest(url));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getBoard(MessageDTO message) throws Exception {
        String boardId = getHeader(message, "boardId");

        String url = getApiUrl() + "/v5/boards/" + boardId;
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO createBoardSection(MessageDTO message) throws Exception {
        String boardId = getHeader(message, "boardId");
        Map<String, Object> sectionData = getPayloadAsMap(message);

        validateRequiredFields(sectionData, Arrays.asList("name"));

        String url = getApiUrl() + "/v5/boards/" + boardId + "/sections";
        String response = executeApiCall(() -> makePostRequest(url, sectionData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO updateBoardSection(MessageDTO message) throws Exception {
        String boardId = getHeader(message, "boardId");
        String sectionId = getHeader(message, "sectionId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/boards/" + boardId + "/sections/" + sectionId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO deleteBoardSection(MessageDTO message) throws Exception {
        String boardId = getHeader(message, "boardId");
        String sectionId = getHeader(message, "sectionId");

        String url = getApiUrl() + "/v5/boards/" + boardId + "/sections/" + sectionId;
        String response = executeApiCall(() -> makeDeleteRequest(url));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO createProductPin(MessageDTO message) throws Exception {
        Map<String, Object> productPinData = getPayloadAsMap(message);

        validateRequiredFields(productPinData, Arrays.asList("board_id", "media_source", "product_tags"));

        // Create pin with product tags
        String url = getApiUrl() + "/v5/pins";
        String response = executeApiCall(() -> makePostRequest(url, productPinData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO tagProducts(MessageDTO message) throws Exception {
        String pinId = getHeader(message, "pinId");
        List<Map<String, Object>> productTags = (List<Map<String, Object>>) getPayloadAsMap(message).get("product_tags");

        Map<String, Object> request = Map.of("product_tags", productTags);

        String url = getApiUrl() + "/v5/pins/" + pinId;
        String response = executeApiCall(() -> makePatchRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO createCatalog(MessageDTO message) throws Exception {
        Map<String, Object> catalogData = getPayloadAsMap(message);

        validateRequiredFields(catalogData, Arrays.asList("name", "format"));

        String url = getApiUrl() + "/v5/catalogs";
        String response = executeApiCall(() -> makePostRequest(url, catalogData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO updateCatalog(MessageDTO message) throws Exception {
        String catalogId = getHeader(message, "catalogId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/catalogs/" + catalogId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO uploadProductFeed(MessageDTO message) throws Exception {
        String catalogId = getHeader(message, "catalogId");
        Map<String, Object> feedData = getPayloadAsMap(message);

        validateRequiredFields(feedData, Arrays.asList("format", "location"));

        String url = getApiUrl() + "/v5/catalogs/" + catalogId + "/feeds";
        String response = executeApiCall(() -> makePostRequest(url, feedData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getUserAnalytics(MessageDTO message) throws Exception {
        Map<String, String> params = buildAnalyticsParams(message);

        String url = getApiUrl() + "/v5/user_account/analytics";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getPinAnalytics(MessageDTO message) throws Exception {
        String pinId = getHeader(message, "pinId");
        Map<String, String> params = buildAnalyticsParams(message);

        String url = getApiUrl() + "/v5/pins/" + pinId + "/analytics";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getBoardAnalytics(MessageDTO message) throws Exception {
        String boardId = getHeader(message, "boardId");
        Map<String, String> params = buildAnalyticsParams(message);

        String url = getApiUrl() + "/v5/boards/" + boardId + "/analytics";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO generateReport(MessageDTO message) throws Exception {
        String reportType = getHeader(message, "reportType");
        Map<String, Object> reportParams = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/reports";
        String response = executeApiCall(() -> makePostRequest(url, reportParams));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO createCampaign(MessageDTO message) throws Exception {
        Map<String, Object> campaignData = getPayloadAsMap(message);

        validateRequiredFields(campaignData, Arrays.asList("name", "objective_type", "daily_spend_cap"));

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/campaigns";
        String response = executeApiCall(() -> makePostRequest(url, campaignData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO updateCampaign(MessageDTO message) throws Exception {
        String campaignId = getHeader(message, "campaignId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/campaigns/" + campaignId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO createAdGroup(MessageDTO message) throws Exception {
        Map<String, Object> adGroupData = getPayloadAsMap(message);

        validateRequiredFields(adGroupData, Arrays.asList("campaign_id", "name", "budget_in_micro_currency", "bid_in_micro_currency"));

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ad_groups";
        String response = executeApiCall(() -> makePostRequest(url, adGroupData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO updateAdGroup(MessageDTO message) throws Exception {
        String adGroupId = getHeader(message, "adGroupId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ad_groups/" + adGroupId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO createAd(MessageDTO message) throws Exception {
        Map<String, Object> adData = getPayloadAsMap(message);

        validateRequiredFields(adData, Arrays.asList("ad_group_id", "creative_type", "pin_id"));

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ads";
        String response = executeApiCall(() -> makePostRequest(url, adData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO updateAd(MessageDTO message) throws Exception {
        String adId = getHeader(message, "adId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ads/" + adId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO createAudience(MessageDTO message) throws Exception {
        Map<String, Object> audienceData = getPayloadAsMap(message);

        validateRequiredFields(audienceData, Arrays.asList("name", "rule"));

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/audiences";
        String response = executeApiCall(() -> makePostRequest(url, audienceData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO updateAudience(MessageDTO message) throws Exception {
        String audienceId = getHeader(message, "audienceId");
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/audiences/" + audienceId;
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO bulkCreatePins(MessageDTO message) throws Exception {
        List<Map<String, Object>> pins = (List<Map<String, Object>>) getPayloadAsMap(message).get("pins");

        if(pins.size() > config.getLimits().getMaxBulkPinsPerRequest()) {
            throw new IllegalArgumentException("Too many pins. Maximum allowed: " + config.getLimits().getMaxBulkPinsPerRequest());
        }

        Map<String, Object> request = Map.of("pins", pins);

        String url = getApiUrl() + "/v5/pins/bulk";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO bulkDeletePins(MessageDTO message) throws Exception {
        List<String> pinIds = (List<String>) getPayloadAsMap(message).get("pin_ids");

        Map<String, Object> request = Map.of("pin_ids", pinIds);

        String url = getApiUrl() + "/v5/pins/bulk/delete";
        String response = executeApiCall(() -> makePostRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO searchPins(MessageDTO message) throws Exception {
        String query = getHeader(message, "query");
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("page_size", getHeader(message, "pageSize", "25"));

        addOptionalParam(params, message, "bookmark");

        String url = getApiUrl() + "/v5/search/pins";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO searchBoards(MessageDTO message) throws Exception {
        String query = getHeader(message, "query");
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("page_size", getHeader(message, "pageSize", "25"));

        String url = getApiUrl() + "/v5/search/boards";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO searchUsers(MessageDTO message) throws Exception {
        String query = getHeader(message, "query");
        Map<String, String> params = new HashMap<>();
        params.put("query", query);

        String url = getApiUrl() + "/v5/search/user_account";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getTrending(MessageDTO message) throws Exception {
        String trendingType = getHeader(message, "trendingType", "searches");
        String region = getHeader(message, "region", "US");

        String url = getApiUrl() + "/v5/trends/" + trendingType + "/" + region;
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getUserInfo(MessageDTO message) throws Exception {
        String url = getApiUrl() + "/v5/user_account";
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO updateUserInfo(MessageDTO message) throws Exception {
        Map<String, Object> updateData = getPayloadAsMap(message);

        String url = getApiUrl() + "/v5/user_account";
        String response = executeApiCall(() -> makePatchRequest(url, updateData));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO followUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");

        Map<String, Object> request = Map.of("auto_follow", false);

        String url = getApiUrl() + "/v5/user_account/following/" + username;
        String response = executeApiCall(() -> makePutRequest(url, request));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO unfollowUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");

        String url = getApiUrl() + "/v5/user_account/following/" + username;
        String response = executeApiCall(() -> makeDeleteRequest(url));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getFollowers(MessageDTO message) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("page_size", getHeader(message, "pageSize", "25"));
        addOptionalParam(params, message, "bookmark");

        String url = getApiUrl() + "/v5/user_account/followers";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO getFollowing(MessageDTO message) throws Exception {
        String followingType = getHeader(message, "followingType", "USERS");
        Map<String, String> params = new HashMap<>();
        params.put("feed_type", followingType);
        params.put("page_size", getHeader(message, "pageSize", "25"));
        addOptionalParam(params, message, "bookmark");

        String url = getApiUrl() + "/v5/user_account/following";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message.getCorrelationId(), response, "OPERATION");
    }

    private MessageDTO uploadImage(MessageDTO message) throws Exception {
        byte[] imageData = getPayloadAsBytes(message);
        String mediaType = getHeader(message, "mediaType", "image/jpeg");

        // Validate image size
        if(imageData.length > config.getLimits().getMaxImageSizeMB() * 1024 * 1024) {
            throw new IllegalArgumentException("Image size exceeds maximum allowed: " + config.getLimits().getMaxImageSizeMB() + "MB");
        }

        // Register media upload
        Map<String, Object> registerRequest = Map.of("media_type", mediaType);
        String registerUrl = getApiUrl() + "/v5/media";
        String registerResponse = executeApiCall(() -> makePostRequest(registerUrl, registerRequest));
        Map<String, Object> uploadData = parseJsonResponse(registerResponse);

        // Upload to provided URL
        String uploadUrl = (String) uploadData.get("upload_url");
        String uploadId = (String) uploadData.get("media_id");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        HttpEntity<byte[]> entity = new HttpEntity<>(imageData, headers);

        ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.PUT, entity, String.class);

        if(!uploadResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload image: " + uploadResponse.getStatusCode());
        }

        return createSuccessResponse(message, Map.of("media_id", uploadId));
    }

    private MessageDTO uploadVideo(MessageDTO message) throws Exception {
        byte[] videoData = getPayloadAsBytes(message);
        String mediaType = getHeader(message, "mediaType", "video/mp4");

        // Validate video size
        if(videoData.length > config.getLimits().getMaxVideoSizeMB() * 1024 * 1024) {
            throw new IllegalArgumentException("Video size exceeds maximum allowed: " + config.getLimits().getMaxVideoSizeMB() + "MB");
        }

        // Similar upload process as image
        Map<String, Object> registerRequest = Map.of("media_type", mediaType);
        String registerUrl = getApiUrl() + "/v5/media";
        String registerResponse = executeApiCall(() -> makePostRequest(registerUrl, registerRequest));
        Map<String, Object> uploadData = parseJsonResponse(registerResponse);

        String uploadUrl = (String) uploadData.get("upload_url");
        String uploadId = (String) uploadData.get("media_id");

        // Upload video
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        HttpEntity<byte[]> entity = new HttpEntity<>(videoData, headers);

        ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.PUT, entity, String.class);

        if(!uploadResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload video: " + uploadResponse.getStatusCode());
        }

        return createSuccessResponse(message, Map.of("media_id", uploadId));
    }

    // Helper methods
    private Map<String, String> buildAnalyticsParams(MessageDTO message) {
        Map<String, String> params = new HashMap<>();
        params.put("start_date", getHeader(message, "startDate"));
        params.put("end_date", getHeader(message, "endDate"));

        String metrics = getHeader(message, "metrics");
        if(metrics != null) {
            params.put("metric_types", metrics);
        }

        String granularity = getHeader(message, "granularity");
        if(granularity != null) {
            params.put("granularity", granularity);
        }

        return params;
    }

    private void validateRequiredFields(Map<String, Object> data, List<String> requiredFields) {
        for(String field : requiredFields) {
            if(!data.containsKey(field) || data.get(field) == null) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }
    }

    private void addOptionalField(Map<String, Object> target, Map<String, Object> source, String field) {
        if(source.containsKey(field) && source.get(field) != null) {
            target.put(field, source.get(field));
        }
    }

    private void addOptionalParam(Map<String, String> params, MessageDTO message, String param) {
        String value = getHeader(message, param);
        if(value != null) {
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
        if(!params.isEmpty()) {
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
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    // Required abstract methods from AbstractOutboundAdapter
    @Override
    protected void doReceiverInitialize() throws Exception {
        log.debug("Initializing Pinterest outbound adapter receiver");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.debug("Destroying Pinterest outbound adapter receiver");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Pinterest outbound adapter doesn't support receiving
        return AdapterResult.failure("Pinterest outbound adapter does not support receiving messages");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test by getting user info
            String url = getApiUrl() + "/v5/user_account";
            String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));

            if (response != null) {
                return AdapterResult.success(null, "Pinterest API connection successful");
            } else {
                return AdapterResult.failure("Pinterest API connection failed");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test Pinterest connection: " + e.getMessage(), e);
        }
    }

    @Override
    protected long getPollingIntervalMs() {
        // Pinterest outbound adapter doesn't support polling
        return 0;
    }

    // Helper methods for MessageDTO access
    private String getHeader(MessageDTO message, String key) {
        return message.getHeaders() != null ?
            (String) message.getHeaders().get(key) : null;
    }

    private String getHeader(MessageDTO message, String key, String defaultValue) {
        String value = getHeader(message, key);
        return value != null ? value : defaultValue;
    }

    private Map<String, Object> getPayloadAsMap(MessageDTO message) {
        try {
            if (message.getPayload() != null) {
                return objectMapper.readValue(message.getPayload(), Map.class);
            }
        } catch (Exception e) {
            log.error("Error parsing payload as map", e);
        }
        return new HashMap<>();
    }

    private byte[] getPayloadAsBytes(MessageDTO message) {
        if (message.getPayload() != null) {
            return message.getPayload().getBytes();
        }
        return new byte[0];
    }

    private String getApiUrl() {
        return config.getApiBaseUrl() != null ? config.getApiBaseUrl() : "https://api.pinterest.com";
    }

    private Map<String, Object> parseJsonResponse(String response) {
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.error("Error parsing JSON response", e);
            return new HashMap<>();
        }
    }

    private MessageDTO createSuccessResponse(MessageDTO message, Map<String, Object> data) {
        try {
            String payload = objectMapper.writeValueAsString(data);
            return createSuccessResponse(message.getCorrelationId(), payload, "RESPONSE");
        } catch (Exception e) {
            return createErrorResponse(message, "Failed to serialize response: " + e.getMessage());
        }
    }
}
