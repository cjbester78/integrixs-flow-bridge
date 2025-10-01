package com.integrixs.adapters.social.pinterest;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.social.base.EventType;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inbound adapter for Pinterest API integration.
 * Handles polling for pins, boards, analytics, and webhook events.
 */
@Component
@ConditionalOnProperty(name = "integrixs.adapters.pinterest.enabled", havingValue = "true", matchIfMissing = false)
public class PinterestInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(PinterestInboundAdapter.class);


    private final PinterestApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final CredentialEncryptionService credentialEncryptionService;
    private final Set<String> processedPins = ConcurrentHashMap.newKeySet();
    private final Set<String> processedBoards = ConcurrentHashMap.newKeySet();
    private final Map<String, LocalDateTime> lastPolledTimes = new ConcurrentHashMap<>();

    @Autowired
    public PinterestInboundAdapter(
            PinterestApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService) {
        super();
        this.config = config;
        this.rateLimiterService = rateLimiterService;
        this.credentialEncryptionService = credentialEncryptionService;
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("appId", config.getAppId());
            configMap.put("appSecret", config.getAppSecret());
            configMap.put("accessToken", config.getAccessToken());
            configMap.put("apiVersion", config.getApiVersion());
            configMap.put("enabled", config.isEnabled());
        }
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    /**
     * Polls for user's pins
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.pins - interval:300000}")
    public void pollPins() {
        if(!config.isEnabled() || !config.getFeatures().isEnablePinManagement()) {
            return;
        }

        try {
            log.debug("Polling Pinterest pins");
            String userId = config.getClientId(); // Using clientId as userId
            fetchUserPins(userId);
        } catch(Exception e) {
            log.error("Error polling Pinterest pins", e);
        }
    }

    /**
     * Polls for user's boards
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.boards - interval:600000}")
    public void pollBoards() {
        if(!config.isEnabled() || !config.getFeatures().isEnableBoardManagement()) {
            return;
        }

        try {
            log.debug("Polling Pinterest boards");
            String userId = config.getClientId(); // Using clientId as userId
            fetchUserBoards(userId);
        } catch(Exception e) {
            log.error("Error polling Pinterest boards", e);
        }
    }

    /**
     * Polls for analytics data
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.analytics - interval:3600000}")
    public void pollAnalytics() {
        if(!config.getFeatures().isEnableAnalytics()) {
            return;
        }

        try {
            log.debug("Polling Pinterest analytics");
            fetchAnalytics();
        } catch(Exception e) {
            log.error("Error polling Pinterest analytics", e);
        }
    }

    /**
     * Polls for ad account data(campaigns, ad groups, ads)
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.ads - interval:900000}")
    public void pollAds() {
        if(!config.getFeatures().isEnableAds() || !StringUtils.hasText(config.getAdvertiserId())) {
            return;
        }

        try {
            log.debug("Polling Pinterest ads data");
            fetchCampaigns();
            fetchAdGroups();
            fetchAds();
        } catch(Exception e) {
            log.error("Error polling Pinterest ads", e);
        }
    }

    private void fetchUserPins(String userId) throws Exception {
        LocalDateTime lastPolled = lastPolledTimes.getOrDefault("pins", LocalDateTime.now().minusDays(7));

        String url = getApiUrl() + "/v5/pins";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        params.put("bookmark", "");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if(responseData.containsKey("items")) {
            List<Map<String, Object>> pins = (List<Map<String, Object>>) responseData.get("items");
            for(Map<String, Object> pin : pins) {
                processPinData(pin);
            }
        }

        lastPolledTimes.put("pins", LocalDateTime.now());

        // Handle pagination
        if(responseData.containsKey("bookmark") && responseData.get("bookmark") != null) {
            handlePagination(responseData, () -> {
                try {
                    fetchUserPins(userId);
                } catch (Exception e) {
                    log.error("Error fetching user pins during pagination", e);
                }
            });
        }
    }

    private void fetchUserBoards(String userId) throws Exception {
        String url = getApiUrl() + "/v5/boards";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if(responseData.containsKey("items")) {
            List<Map<String, Object>> boards = (List<Map<String, Object>>) responseData.get("items");
            for(Map<String, Object> board : boards) {
                processBoardData(board);

                // Fetch pins for each board
                if(config.getFeatures().isEnablePinManagement()) {
                    String boardId = (String) board.get("id");
                    fetchBoardPins(boardId);
                }
            }
        }

        // Handle pagination
        if(responseData.containsKey("bookmark") && responseData.get("bookmark") != null) {
            handlePagination(responseData, () -> {
                try {
                    fetchUserBoards(userId);
                } catch (Exception e) {
                    log.error("Error fetching user boards during pagination", e);
                }
            });
        }
    }

    private void fetchBoardPins(String boardId) throws Exception {
        String url = getApiUrl() + "/v5/boards/" + boardId + "/pins";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if(responseData.containsKey("items")) {
            List<Map<String, Object>> pins = (List<Map<String, Object>>) responseData.get("items");
            for(Map<String, Object> pin : pins) {
                pin.put("board_id", boardId); // Add board context
                processPinData(pin);
            }
        }
    }

    private void fetchAnalytics() throws Exception {
        // User analytics
        if(config.getFeatures().isEnableAnalytics()) {
            fetchUserAnalytics();
        }

        // Pin analytics
        if(config.getFeatures().isEnableAnalytics()) {
            fetchPinAnalytics();
        }

        // Board analytics
        if(config.getFeatures().isEnableAnalytics()) {
            fetchBoardAnalytics();
        }
    }

    private void fetchUserAnalytics() throws Exception {
        String url = getApiUrl() + "/v5/user_account/analytics";
        Map<String, String> params = new HashMap<>();
        params.put("start_date", LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE));
        params.put("end_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        params.put("metric_types", String.join(",", Arrays.asList(
                "IMPRESSION", "ENGAGEMENT", "ENGAGEMENT_RATE", "SAVE", "SAVE_RATE",
                "PIN_CLICK", "OUTBOUND_CLICK", "TOTAL_COMMENTS", "TOTAL_REACTIONS"
       )));

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> analytics = parseJsonResponse(response);

        processAnalyticsData("user", analytics);
    }

    private void fetchPinAnalytics() throws Exception {
        // Get recent pins first
        Set<String> recentPinIds = new HashSet<>(processedPins);

        for(String pinId : recentPinIds) {
            String url = getApiUrl() + "/v5/pins/" + pinId + "/analytics";
            Map<String, String> params = new HashMap<>();
            params.put("start_date", LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
            params.put("end_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            params.put("metric_types", String.join(",", Arrays.asList(
                    "IMPRESSION", "SAVE", "PIN_CLICK", "OUTBOUND_CLICK", "SAVE_RATE"
           )));

            try {
                String response = executeApiCall(() -> makeGetRequest(url, params));
                Map<String, Object> analytics = parseJsonResponse(response);
                analytics.put("pin_id", pinId);

                processAnalyticsData("pin", analytics);
            } catch(Exception e) {
                log.warn("Failed to fetch analytics for pin: " + pinId, e);
            }
        }
    }

    private void fetchBoardAnalytics() throws Exception {
        Set<String> boardIds = new HashSet<>(processedBoards);

        for(String boardId : boardIds) {
            String url = getApiUrl() + "/v5/boards/" + boardId + "/analytics";
            Map<String, String> params = new HashMap<>();
            params.put("start_date", LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
            params.put("end_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            params.put("metric_types", String.join(",", Arrays.asList(
                    "IMPRESSION", "ENGAGEMENT", "PIN_CLICK", "SAVE", "OUTBOUND_CLICK"
           )));

            try {
                String response = executeApiCall(() -> makeGetRequest(url, params));
                Map<String, Object> analytics = parseJsonResponse(response);
                analytics.put("board_id", boardId);

                processAnalyticsData("board", analytics);
            } catch(Exception e) {
                log.warn("Failed to fetch analytics for board: " + boardId, e);
            }
        }
    }

    private void fetchCampaigns() throws Exception {
        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/campaigns";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if(responseData.containsKey("items")) {
            List<Map<String, Object>> campaigns = (List<Map<String, Object>>) responseData.get("items");
            for(Map<String, Object> campaign : campaigns) {
                processCampaignData(campaign);
            }
        }
    }

    private void fetchAdGroups() throws Exception {
        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ad_groups";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if(responseData.containsKey("items")) {
            List<Map<String, Object>> adGroups = (List<Map<String, Object>>) responseData.get("items");
            for(Map<String, Object> adGroup : adGroups) {
                processAdGroupData(adGroup);
            }
        }
    }

    private void fetchAds() throws Exception {
        String url = getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ads";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        if(responseData.containsKey("items")) {
            List<Map<String, Object>> ads = (List<Map<String, Object>>) responseData.get("items");
            for(Map<String, Object> ad : ads) {
                processAdData(ad);
            }
        }
    }

    private void processPinData(Map<String, Object> pinData) {
        String pinId = (String) pinData.get("id");

        if(!processedPins.add(pinId)) {
            return; // Already processed
        }

        PinterestPin pin = new PinterestInboundAdapter.PinterestPin();
        pin.id = pinId;
        pin.createdAt = (String) pinData.get("created_at");
        pin.title = (String) pinData.get("title");
        pin.description = (String) pinData.get("description");
        pin.link = (String) pinData.get("link");
        pin.altText = (String) pinData.get("alt_text");
        pin.boardId = (String) pinData.get("board_id");
        pin.boardSectionId = (String) pinData.get("board_section_id");
        pin.mediaType = extractMediaType(pinData);
        pin.media = pinData.get("media");
        pin.pinMetrics = pinData.get("pin_metrics");
        pin.dominantColor = (String) pinData.get("dominant_color");
        pin.productTags = (List<Map<String, Object>>) pinData.get("product_tags");
        publishMessage("pinterest.pin.created", pin);
    }

    private void processBoardData(Map<String, Object> boardData) {
        String boardId = (String) boardData.get("id");

        if(!processedBoards.add(boardId)) {
            return; // Already processed
        }

        PinterestBoard board = new PinterestInboundAdapter.PinterestBoard();
        board.id = boardId;
        board.name = (String) boardData.get("name");
        board.description = (String) boardData.get("description");
        board.privacy = (String) boardData.get("privacy");
        board.createdAt = (String) boardData.get("created_at");
        board.boardPinsModifiedAt = (String) boardData.get("board_pins_modified_at");
        board.followerCount = ((Number) boardData.get("follower_count")).intValue();
        board.collaboratorCount = ((Number) boardData.get("collaborator_count")).intValue();
        board.pinCount = ((Number) boardData.get("pin_count")).intValue();
        publishMessage("pinterest.board.created", board);
    }

    private void processAnalyticsData(String type, Map<String, Object> analytics) {
        PinterestAnalytics analyticsData = new PinterestInboundAdapter.PinterestAnalytics();
        analyticsData.type = type;
        analyticsData.data = analytics;
        analyticsData.timestamp = LocalDateTime.now().toString();
        publishMessage("pinterest.analytics." + type, analyticsData);
    }

    private void processCampaignData(Map<String, Object> campaign) {
        publishMessage("pinterest.campaign.updated", campaign);
    }

    private void processAdGroupData(Map<String, Object> adGroup) {
        publishMessage("pinterest.adgroup.updated", adGroup);
    }

    private void processAdData(Map<String, Object> ad) {
        publishMessage("pinterest.ad.updated", ad);
    }

    @Override
    public void processWebhookEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event_type");

        switch(eventType) {
            case "PIN_CREATE":
                processWebhookPinCreate(event);
                break;
            case "PIN_UPDATE":
                processWebhookPinUpdate(event);
                break;
            case "PIN_DELETE":
                processWebhookPinDelete(event);
                break;
            case "BOARD_CREATE":
                processWebhookBoardCreate(event);
                break;
            case "BOARD_UPDATE":
                processWebhookBoardUpdate(event);
                break;
            case "BOARD_DELETE":
                processWebhookBoardDelete(event);
                break;
            case "USER_FOLLOW":
                processWebhookUserFollow(event);
                break;
            case "SAVE_PIN":
                processWebhookSavePin(event);
                break;
            default:
                log.warn("Unknown webhook event type: {}", eventType);
        }
    }

    private void processWebhookPinCreate(Map<String, Object> event) {
        Map<String, Object> pinData = (Map<String, Object>) event.get("pin");
        processPinData(pinData);
    }

    private void processWebhookPinUpdate(Map<String, Object> event) {
        Map<String, Object> pinData = (Map<String, Object>) event.get("pin");
        String pinId = (String) pinData.get("id");

        publishMessage("pinterest.pin.updated", pinData);
    }

    private void processWebhookPinDelete(Map<String, Object> event) {
        String pinId = (String) event.get("pin_id");
        processedPins.remove(pinId);

        publishMessage("pinterest.pin.deleted", Map.of("pin_id", pinId));
    }

    private void processWebhookBoardCreate(Map<String, Object> event) {
        Map<String, Object> boardData = (Map<String, Object>) event.get("board");
        processBoardData(boardData);
    }

    private void processWebhookBoardUpdate(Map<String, Object> event) {
        Map<String, Object> boardData = (Map<String, Object>) event.get("board");
        publishMessage("pinterest.board.updated", boardData);
    }

    private void processWebhookBoardDelete(Map<String, Object> event) {
        String boardId = (String) event.get("board_id");
        processedBoards.remove(boardId);

        publishMessage("pinterest.board.deleted", Map.of("board_id", boardId));
    }

    private void processWebhookUserFollow(Map<String, Object> event) {
        publishMessage("pinterest.user.followed", event);
    }

    private void processWebhookSavePin(Map<String, Object> event) {
        publishMessage("pinterest.pin.saved", event);
    }

    @Override
    public boolean verifyWebhookSignature(String signature, String payload) {
        if(!StringUtils.hasText(signature)) {
            return false;
        }

        try {
            String appSecret = getDecryptedCredential("appSecret");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            String calculatedSignature = Base64.getEncoder().encodeToString(hash);
            return signature.equals(calculatedSignature);
        } catch(Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList(
                "SOCIAL_MEDIA_POST",
                "SOCIAL_MEDIA_BOARD",
                "SOCIAL_MEDIA_ANALYTICS",
                "SOCIAL_MEDIA_CAMPAIGN",
                "SOCIAL_MEDIA_AD",
                "SOCIAL_MEDIA_FOLLOW"
       );
    }

    private String extractMediaType(Map<String, Object> pinData) {
        Map<String, Object> media = (Map<String, Object>) pinData.get("media");
        if(media != null && media.containsKey("media_type")) {
            return(String) media.get("media_type");
        }
        return "image";
    }

    // Data classes for Pinterest entities
        public static class PinterestPin {
        private String id;
        private String createdAt;
        private String title;
        private String description;
        private String link;
        private String altText;
        private String boardId;
        private String boardSectionId;
        private String mediaType;
        private Object media;
        private Object pinMetrics;
        private String dominantColor;
        private List<Map<String, Object>> productTags;
    }

        public static class PinterestBoard {
        private String id;
        private String name;
        private String description;
        private String privacy;
        private String createdAt;
        private String boardPinsModifiedAt;
        private int followerCount;
        private int collaboratorCount;
        private int pinCount;
    }

        public static class PinterestAnalytics {
        private String type;
        private Map<String, Object> data;
        private String timestamp;
    }
    // Required abstract methods from AbstractInboundAdapter
    @Override
    protected void doSenderInitialize() throws Exception {
        log.debug("Initializing Pinterest inbound adapter");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.debug("Destroying Pinterest inbound adapter");
    }

    protected AdapterResult doSend(Object data, Map<String, Object> configuration) throws Exception {
        // Pinterest inbound adapter doesn't support sending
        return AdapterResult.failure("Pinterest inbound adapter does not support sending messages");
    }

    @Override
    public AdapterResult send(Object data, Map<String, Object> configuration) throws AdapterException {
        try {
            return doSend(data, configuration);
        } catch (Exception e) {
            throw new AdapterException("Failed to send data", e);
        }
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test by fetching user info
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

    public long getPollingIntervalMs() {
        // Default polling interval: 5 minutes
        return 300000;
    }

    // Helper methods to access config values
    private String getApiUrl() {
        return config.getApiBaseUrl() != null ? config.getApiBaseUrl() : "https://api.pinterest.com";
    }

    private String getDecryptedCredential(String key) {
        Map<String, Object> configMap = getConfig();
        String value = (String) configMap.get(key);
        return credentialEncryptionService.decryptIfNeeded(value);
    }

    private <T> T executeApiCall(java.util.concurrent.Callable<T> callable) throws Exception {
        String rateLimiterName = "pinterest_rate_limiter";
        return rateLimiterService.executeWithRateLimit(rateLimiterName, callable);
    }

    private String makeGetRequest(String url, Map<String, String> params) throws Exception {
        // This would be implemented with RestTemplate or similar
        // For now, return a placeholder
        log.debug("Making GET request to: {} with params: {}", url, params);
        return "{}";
    }

    private Map<String, Object> parseJsonResponse(String response) {
        try {
            // Simple implementation - in real scenario would use Jackson ObjectMapper
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Error parsing JSON response", e);
            return new HashMap<>();
        }
    }

    private void handlePagination(Map<String, Object> responseData, Runnable fetchAction) {
        // Handle pagination logic
        log.debug("Handling pagination for response data");
    }

    private void publishMessage(String eventType, Object data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        message.setHeaders(Map.of(
            "eventType", eventType,
            "source", "pinterest",
            "timestamp", System.currentTimeMillis()
        ));
        message.setPayload(data.toString());
        message.setStatus(com.integrixs.shared.enums.MessageStatus.SUCCESS);

        // Publish through event system
        log.debug("Publishing event: {} with data: {}", eventType, data);
    }
}
