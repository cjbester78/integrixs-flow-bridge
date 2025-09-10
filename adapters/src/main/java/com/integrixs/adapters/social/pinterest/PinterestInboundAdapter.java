package com.integrixs.adapters.social.pinterest;

import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.platform.events.EventType;
import com.integrixs.platform.models.Message;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.service.RateLimiterService;
import com.integrixs.shared.utils.CredentialEncryptionService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@ConditionalOnProperty(name = "integrixs.adapters.pinterest.enabled", havingValue = "true", matchIfMissing = false)
public class PinterestInboundAdapter extends AbstractSocialMediaInboundAdapter {

    private final PinterestApiConfig config;
    private final Set<String> processedPins = ConcurrentHashMap.newKeySet();
    private final Set<String> processedBoards = ConcurrentHashMap.newKeySet();
    private final Map<String, LocalDateTime> lastPolledTimes = new ConcurrentHashMap<>();
    
    @Autowired
    public PinterestInboundAdapter(
            PinterestApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService) {
        super(rateLimiterService, credentialEncryptionService);
        this.config = config;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public AdapterConfig getAdapterConfig() {
        return config;
    }

    /**
     * Polls for user's pins
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.pins-interval:300000}")
    public void pollPins() {
        if (!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnablePinManagement()) {
            return;
        }

        try {
            log.debug("Polling Pinterest pins");
            String userId = config.getOauth2Config().getUserId();
            fetchUserPins(userId);
        } catch (Exception e) {
            log.error("Error polling Pinterest pins", e);
        }
    }

    /**
     * Polls for user's boards
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.boards-interval:600000}")
    public void pollBoards() {
        if (!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnableBoardManagement()) {
            return;
        }

        try {
            log.debug("Polling Pinterest boards");
            String userId = config.getOauth2Config().getUserId();
            fetchUserBoards(userId);
        } catch (Exception e) {
            log.error("Error polling Pinterest boards", e);
        }
    }

    /**
     * Polls for analytics data
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.analytics-interval:3600000}")
    public void pollAnalytics() {
        if (!config.getFeatures().isEnableAnalytics()) {
            return;
        }

        try {
            log.debug("Polling Pinterest analytics");
            fetchAnalytics();
        } catch (Exception e) {
            log.error("Error polling Pinterest analytics", e);
        }
    }

    /**
     * Polls for ad account data (campaigns, ad groups, ads)
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.pinterest.polling.ads-interval:900000}")
    public void pollAds() {
        if (!config.getFeatures().isEnableAds() || !StringUtils.hasText(config.getAdvertiserId())) {
            return;
        }

        try {
            log.debug("Polling Pinterest ads data");
            fetchCampaigns();
            fetchAdGroups();
            fetchAds();
        } catch (Exception e) {
            log.error("Error polling Pinterest ads", e);
        }
    }

    private void fetchUserPins(String userId) throws Exception {
        LocalDateTime lastPolled = lastPolledTimes.getOrDefault("pins", LocalDateTime.now().minusDays(7));
        
        String url = config.getApiUrl() + "/v5/pins";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        params.put("bookmark", "");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        if (responseData.containsKey("items")) {
            List<Map<String, Object>> pins = (List<Map<String, Object>>) responseData.get("items");
            for (Map<String, Object> pin : pins) {
                processPinData(pin);
            }
        }
        
        lastPolledTimes.put("pins", LocalDateTime.now());
        
        // Handle pagination
        if (responseData.containsKey("bookmark") && responseData.get("bookmark") != null) {
            handlePagination(responseData, () -> fetchUserPins(userId));
        }
    }

    private void fetchUserBoards(String userId) throws Exception {
        String url = config.getApiUrl() + "/v5/boards";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        if (responseData.containsKey("items")) {
            List<Map<String, Object>> boards = (List<Map<String, Object>>) responseData.get("items");
            for (Map<String, Object> board : boards) {
                processBoardData(board);
                
                // Fetch pins for each board
                if (config.getPollingConfig().isPollBoardPins()) {
                    String boardId = (String) board.get("id");
                    fetchBoardPins(boardId);
                }
            }
        }
        
        // Handle pagination
        if (responseData.containsKey("bookmark") && responseData.get("bookmark") != null) {
            handlePagination(responseData, () -> fetchUserBoards(userId));
        }
    }

    private void fetchBoardPins(String boardId) throws Exception {
        String url = config.getApiUrl() + "/v5/boards/" + boardId + "/pins";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        if (responseData.containsKey("items")) {
            List<Map<String, Object>> pins = (List<Map<String, Object>>) responseData.get("items");
            for (Map<String, Object> pin : pins) {
                pin.put("board_id", boardId); // Add board context
                processPinData(pin);
            }
        }
    }

    private void fetchAnalytics() throws Exception {
        // User analytics
        if (config.getPollingConfig().isPollUserAnalytics()) {
            fetchUserAnalytics();
        }
        
        // Pin analytics
        if (config.getPollingConfig().isPollPinAnalytics()) {
            fetchPinAnalytics();
        }
        
        // Board analytics
        if (config.getPollingConfig().isPollBoardAnalytics()) {
            fetchBoardAnalytics();
        }
    }

    private void fetchUserAnalytics() throws Exception {
        String url = config.getApiUrl() + "/v5/user_account/analytics";
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
        
        for (String pinId : recentPinIds) {
            String url = config.getApiUrl() + "/v5/pins/" + pinId + "/analytics";
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
            } catch (Exception e) {
                log.warn("Failed to fetch analytics for pin: " + pinId, e);
            }
        }
    }

    private void fetchBoardAnalytics() throws Exception {
        Set<String> boardIds = new HashSet<>(processedBoards);
        
        for (String boardId : boardIds) {
            String url = config.getApiUrl() + "/v5/boards/" + boardId + "/analytics";
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
            } catch (Exception e) {
                log.warn("Failed to fetch analytics for board: " + boardId, e);
            }
        }
    }

    private void fetchCampaigns() throws Exception {
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/campaigns";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        if (responseData.containsKey("items")) {
            List<Map<String, Object>> campaigns = (List<Map<String, Object>>) responseData.get("items");
            for (Map<String, Object> campaign : campaigns) {
                processCampaignData(campaign);
            }
        }
    }

    private void fetchAdGroups() throws Exception {
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ad_groups";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        if (responseData.containsKey("items")) {
            List<Map<String, Object>> adGroups = (List<Map<String, Object>>) responseData.get("items");
            for (Map<String, Object> adGroup : adGroups) {
                processAdGroupData(adGroup);
            }
        }
    }

    private void fetchAds() throws Exception {
        String url = config.getApiUrl() + "/v5/ad_accounts/" + config.getAdvertiserId() + "/ads";
        Map<String, String> params = new HashMap<>();
        params.put("page_size", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        if (responseData.containsKey("items")) {
            List<Map<String, Object>> ads = (List<Map<String, Object>>) responseData.get("items");
            for (Map<String, Object> ad : ads) {
                processAdData(ad);
            }
        }
    }

    private void processPinData(Map<String, Object> pinData) {
        String pinId = (String) pinData.get("id");
        
        if (!processedPins.add(pinId)) {
            return; // Already processed
        }
        
        PinterestPin pin = PinterestPin.builder()
                .id(pinId)
                .createdAt((String) pinData.get("created_at"))
                .title((String) pinData.get("title"))
                .description((String) pinData.get("description"))
                .link((String) pinData.get("link"))
                .altText((String) pinData.get("alt_text"))
                .boardId((String) pinData.get("board_id"))
                .boardSectionId((String) pinData.get("board_section_id"))
                .mediaType(extractMediaType(pinData))
                .media(pinData.get("media"))
                .pinMetrics(pinData.get("pin_metrics"))
                .dominantColor((String) pinData.get("dominant_color"))
                .productTags((List<Map<String, Object>>) pinData.get("product_tags"))
                .build();
        
        publishMessage("pinterest.pin.created", pin);
    }

    private void processBoardData(Map<String, Object> boardData) {
        String boardId = (String) boardData.get("id");
        
        if (!processedBoards.add(boardId)) {
            return; // Already processed
        }
        
        PinterestBoard board = PinterestBoard.builder()
                .id(boardId)
                .name((String) boardData.get("name"))
                .description((String) boardData.get("description"))
                .privacy((String) boardData.get("privacy"))
                .createdAt((String) boardData.get("created_at"))
                .boardPinsModifiedAt((String) boardData.get("board_pins_modified_at"))
                .followerCount(((Number) boardData.get("follower_count")).intValue())
                .collaboratorCount(((Number) boardData.get("collaborator_count")).intValue())
                .pinCount(((Number) boardData.get("pin_count")).intValue())
                .build();
        
        publishMessage("pinterest.board.created", board);
    }

    private void processAnalyticsData(String type, Map<String, Object> analytics) {
        PinterestAnalytics analyticsData = PinterestAnalytics.builder()
                .type(type)
                .data(analytics)
                .timestamp(LocalDateTime.now().toString())
                .build();
        
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
        
        switch (eventType) {
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
        if (!StringUtils.hasText(signature)) {
            return false;
        }

        try {
            String appSecret = getDecryptedCredential("appSecret");
            
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            String calculatedSignature = Base64.getEncoder().encodeToString(hash);
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    @Override
    protected String getAdapterType() {
        return "PINTEREST";
    }

    @Override
    protected List<EventType> getSupportedEventTypes() {
        return Arrays.asList(
                EventType.SOCIAL_MEDIA_POST,
                EventType.SOCIAL_MEDIA_BOARD,
                EventType.SOCIAL_MEDIA_ANALYTICS,
                EventType.SOCIAL_MEDIA_CAMPAIGN,
                EventType.SOCIAL_MEDIA_AD,
                EventType.SOCIAL_MEDIA_FOLLOW
        );
    }

    private String extractMediaType(Map<String, Object> pinData) {
        Map<String, Object> media = (Map<String, Object>) pinData.get("media");
        if (media != null && media.containsKey("media_type")) {
            return (String) media.get("media_type");
        }
        return "image";
    }

    // Data classes for Pinterest entities
    @Data
    @lombok.Builder
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

    @Data
    @lombok.Builder
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

    @Data
    @lombok.Builder
    public static class PinterestAnalytics {
        private String type;
        private Map<String, Object> data;
        private String timestamp;
    }
}