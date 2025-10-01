package com.integrixs.adapters.social.youtube;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.youtube.YouTubeAnalyticsApiConfig.*;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
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
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("youTubeAnalyticsInboundAdapter")
public class YouTubeAnalyticsInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(YouTubeAnalyticsInboundAdapter.class);


    // API URLs are configured in application.yml
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> lastReportTime = new ConcurrentHashMap<>();

    @Autowired
    private YouTubeAnalyticsApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;
    private volatile boolean isListening = false;

    @Autowired
    public YouTubeAnalyticsInboundAdapter(
            RateLimiterService rateLimiterService,
            TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super();
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("YouTube Analytics configuration is invalid");
        }

        log.info("Starting YouTube Analytics inbound adapter for channel: {}", config.getChannelId());

        // Refresh access token if needed
        refreshAccessTokenIfNeeded();

        isListening = true;

        // Initialize scheduled reports based on enabled features
        scheduleChannelReports();
        scheduleVideoReports();
        scheduleRevenueReports();
        scheduleEngagementReports();
        scheduleAudienceReports();
        scheduleTrafficSourceReports();
        scheduleRealtimeReports();
    }

    public void stopListening() {
        log.info("Stopping YouTube Analytics inbound adapter");
        isListening = false;
    }

    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.NEW);

            JsonNode dataNode = objectMapper.readTree(data);

            switch(type) {
                case "CHANNEL_REPORT":
                    message = processChannelReport(dataNode);
                    break;
                case "VIDEO_REPORT":
                    message = processVideoReport(dataNode);
                    break;
                case "REVENUE_REPORT":
                    message = processRevenueReport(dataNode);
                    break;
                case "ENGAGEMENT_REPORT":
                    message = processEngagementReport(dataNode);
                    break;
                case "AUDIENCE_REPORT":
                    message = processAudienceReport(dataNode);
                    break;
                case "TRAFFIC_SOURCE_REPORT":
                    message = processTrafficSourceReport(dataNode);
                    break;
                case "REALTIME_REPORT":
                    message = processRealtimeReport(dataNode);
                    break;
                case "DEVICE_REPORT":
                    message = processDeviceReport(dataNode);
                    break;
                case "GEOGRAPHY_REPORT":
                    message = processGeographyReport(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "youtube_analytics"));
            }

            return message;
        } catch(Exception e) {
            log.error("Error processing YouTube Analytics inbound data", e);
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setCorrelationId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setPayload("Error: " + e.getMessage());
            errorMessage.setHeaders(Map.of("error", "true", "errorMessage", e.getMessage()));
            return errorMessage;
        }
    }

    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        // YouTube Analytics doesn't use webhooks - all data is pulled via API
        return null;
    }

    // Scheduled report polling methods
    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.analytics.channelReportInterval:3600000}") // 1 hour
    private void pollChannelReports() {
        if(!isListening || !config.getFeatures().isEnableChannelReports()) return;

        try {
            rateLimiterService.acquire("youtube_analytics_api", 1);

            LocalDate endDate = LocalDate.now().minusDays(2); // Data has 48hr delay
            LocalDate startDate = endDate.minusDays(7);

            String url = buildReportUrl(
                startDate,
                endDate,
                "channel==" + config.getChannelId(),
                Arrays.asList(Metric.VIEWS, Metric.ESTIMATED_MINUTES_WATCHED,
                    Metric.AVERAGE_VIEW_DURATION, Metric.SUBSCRIBERS_GAINED,
                    Metric.SUBSCRIBERS_LOST),
                Arrays.asList(Dimension.DAY),
                null
           );

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "CHANNEL_REPORT");
                lastReportTime.put("channel", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling channel reports", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.analytics.videoReportInterval:3600000}") // 1 hour
    private void pollVideoReports() {
        if(!isListening || !config.getFeatures().isEnableVideoReports()) return;

        try {
            rateLimiterService.acquire("youtube_analytics_api", 2);

            // First get top videos from past 7 days
            List<String> topVideoIds = getTopVideos(10);

            if(!topVideoIds.isEmpty()) {
                LocalDate endDate = LocalDate.now().minusDays(2);
                LocalDate startDate = endDate.minusDays(7);

                // Get analytics for top videos
                String filter = "video==" + String.join(",", topVideoIds);
                String url = buildReportUrl(
                    startDate,
                    endDate,
                    filter,
                    Arrays.asList(Metric.VIEWS, Metric.ESTIMATED_MINUTES_WATCHED,
                        Metric.LIKES, Metric.COMMENTS, Metric.SHARES),
                    Arrays.asList(Dimension.VIDEO),
                    " - views"
               );

                ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET);
                if(response.getStatusCode().is2xxSuccessful()) {
                    processInboundData(response.getBody(), "VIDEO_REPORT");
                    lastReportTime.put("video", LocalDateTime.now());
                }
            }
        } catch(Exception e) {
            log.error("Error polling video reports", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.analytics.revenueReportInterval:86400000}") // 24 hours
    private void pollRevenueReports() {
        if(!isListening || !config.getFeatures().isEnableRevenueReports()) return;

        try {
            rateLimiterService.acquire("youtube_analytics_api", 1);

            LocalDate endDate = LocalDate.now().minusDays(2);
            LocalDate startDate = endDate.minusMonths(1);

            String url = buildReportUrl(
                startDate,
                endDate,
                "channel==" + config.getChannelId(),
                Arrays.asList(Metric.ESTIMATED_REVENUE, Metric.ESTIMATED_AD_REVENUE,
                    Metric.ESTIMATED_RED_PARTNER_REVENUE, Metric.CPM,
                    Metric.AD_IMPRESSIONS, Metric.MONETIZED_PLAYBACKS),
                Arrays.asList(Dimension.DAY),
                null
           );

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "REVENUE_REPORT");
                lastReportTime.put("revenue", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling revenue reports", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.analytics.engagementReportInterval:7200000}") // 2 hours
    private void pollEngagementReports() {
        if(!isListening || !config.getFeatures().isEnableEngagementReports()) return;

        try {
            rateLimiterService.acquire("youtube_analytics_api", 1);

            LocalDate endDate = LocalDate.now().minusDays(2);
            LocalDate startDate = endDate.minusDays(28);

            String url = buildReportUrl(
                startDate,
                endDate,
                "channel==" + config.getChannelId(),
                Arrays.asList(Metric.LIKES, Metric.DISLIKES, Metric.COMMENTS,
                    Metric.SHARES, Metric.SUBSCRIBERS_GAINED, Metric.SUBSCRIBERS_LOST,
                    Metric.AVERAGE_VIEW_PERCENTAGE),
                Arrays.asList(Dimension.DAY),
                null
           );

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "ENGAGEMENT_REPORT");
                lastReportTime.put("engagement", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling engagement reports", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.analytics.audienceReportInterval:86400000}") // 24 hours
    private void pollAudienceReports() {
        if(!isListening || !config.getFeatures().isEnableAudienceReports()) return;

        try {
            rateLimiterService.acquire("youtube_analytics_api", 3);

            LocalDate endDate = LocalDate.now().minusDays(2);
            LocalDate startDate = endDate.minusDays(28);

            // Demographics report
            String demographicsUrl = buildReportUrl(
                startDate,
                endDate,
                "channel==" + config.getChannelId(),
                Arrays.asList(Metric.VIEWS, Metric.ESTIMATED_MINUTES_WATCHED),
                Arrays.asList(Dimension.AGE_GROUP, Dimension.GENDER),
                null
           );

            ResponseEntity<String> demographicsResponse = makeApiCall(demographicsUrl, HttpMethod.GET);
            if(demographicsResponse.getStatusCode().is2xxSuccessful()) {
                processInboundData(demographicsResponse.getBody(), "AUDIENCE_REPORT");
            }

            // Geography report
            String geographyUrl = buildReportUrl(
                startDate,
                endDate,
                "channel==" + config.getChannelId(),
                Arrays.asList(Metric.VIEWS, Metric.ESTIMATED_MINUTES_WATCHED),
                Arrays.asList(Dimension.COUNTRY),
                " - views"
           );

            ResponseEntity<String> geographyResponse = makeApiCall(geographyUrl, HttpMethod.GET);
            if(geographyResponse.getStatusCode().is2xxSuccessful()) {
                processInboundData(geographyResponse.getBody(), "GEOGRAPHY_REPORT");
            }

            lastReportTime.put("audience", LocalDateTime.now());
        } catch(Exception e) {
            log.error("Error polling audience reports", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.analytics.trafficSourceReportInterval:14400000}") // 4 hours
    private void pollTrafficSourceReports() {
        if(!isListening || !config.getFeatures().isEnableTrafficSourceReports()) return;

        try {
            rateLimiterService.acquire("youtube_analytics_api", 1);

            LocalDate endDate = LocalDate.now().minusDays(2);
            LocalDate startDate = endDate.minusDays(28);

            String url = buildReportUrl(
                startDate,
                endDate,
                "channel==" + config.getChannelId(),
                Arrays.asList(Metric.VIEWS, Metric.ESTIMATED_MINUTES_WATCHED),
                Arrays.asList(Dimension.TRAFFIC_SOURCE_TYPE),
                " - views"
           );

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "TRAFFIC_SOURCE_REPORT");
                lastReportTime.put("trafficSource", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling traffic source reports", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.youtube.analytics.realtimeReportInterval:300000}") // 5 minutes
    private void pollRealtimeReports() {
        if(!isListening || !config.getFeatures().isEnableRealtimeReports()) return;

        try {
            // Real - time reports would typically use a different endpoint
            // This is a simplified version using recent data
            rateLimiterService.acquire("youtube_analytics_api", 1);

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            String url = buildReportUrl(
                yesterday,
                today,
                "channel==" + config.getChannelId(),
                Arrays.asList(Metric.VIEWS, Metric.ESTIMATED_MINUTES_WATCHED),
                Arrays.asList(Dimension.DAY),
                null
           );

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET);
            if(response.getStatusCode().is2xxSuccessful()) {
                processInboundData(response.getBody(), "REALTIME_REPORT");
                lastReportTime.put("realtime", LocalDateTime.now());
            }
        } catch(Exception e) {
            log.error("Error polling realtime reports", e);
        }
    }

    // Process different report types
    private MessageDTO processChannelReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "CHANNEL_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("channelId", config.getChannelId());
        headers.put("reportType", ReportType.CHANNEL_BASIC.name());

        // Extract key metrics
        if(data.has("rows") && data.get("rows").size() > 0) {
            long totalViews = 0;
            long totalMinutesWatched = 0;
            long netSubscribers = 0;

            for(JsonNode row : data.get("rows")) {
                totalViews += row.get(1).asLong(0); // Views column
                totalMinutesWatched += row.get(2).asLong(0); // Minutes watched
                netSubscribers += row.get(4).asLong(0) - row.get(5).asLong(0); // Gained - Lost
            }

            headers.put("totalViews", totalViews);
            headers.put("totalMinutesWatched", totalMinutesWatched);
            headers.put("netSubscribers", netSubscribers);
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processVideoReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "VIDEO_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("reportType", ReportType.VIDEO_BASIC.name());

        if(data.has("rows")) {
            headers.put("videoCount", data.get("rows").size());
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processRevenueReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "REVENUE_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("reportType", ReportType.REVENUE_REPORTS.name());

        // Calculate total revenue
        if(data.has("rows") && data.get("rows").size() > 0) {
            double totalRevenue = 0;
            double totalAdRevenue = 0;

            for(JsonNode row : data.get("rows")) {
                totalRevenue += row.get(1).asDouble(0); // Estimated revenue
                totalAdRevenue += row.get(2).asDouble(0); // Ad revenue
            }

            headers.put("totalRevenue", totalRevenue);
            headers.put("totalAdRevenue", totalAdRevenue);
            headers.put("currency", "USD"); // Default currency
        }

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processEngagementReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "ENGAGEMENT_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("reportType", ReportType.ENGAGEMENT_REPORTS.name());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processAudienceReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "AUDIENCE_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("reportType", ReportType.CHANNEL_DEMOGRAPHICS.name());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processTrafficSourceReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "TRAFFIC_SOURCE_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("reportType", ReportType.CHANNEL_TRAFFIC_SOURCE.name());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processRealtimeReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "REALTIME_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("isRealtime", true);

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processDeviceReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "DEVICE_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("reportType", ReportType.CHANNEL_DEVICE_OS.name());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processGeographyReport(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "GEOGRAPHY_REPORT");
        headers.put("source", "youtube_analytics");
        headers.put("reportType", ReportType.CHANNEL_GEOGRAPHY.name());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    // Helper methods
    private String buildReportUrl(LocalDate startDate, LocalDate endDate, String filters,
                                  List<Metric> metrics, List<Dimension> dimensions, String sort) {
        StringBuilder url = new StringBuilder(config.getApiBaseUrl() + "/v2/reports?");

        url.append("startDate = ").append(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        url.append("&endDate = ").append(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        if(filters != null && !filters.isEmpty()) {
            url.append("&filters = ").append(filters);
        }

        if(metrics != null && !metrics.isEmpty()) {
            url.append("&metrics = ").append(
                String.join(",", metrics.stream()
                    .map(m -> m.name().toLowerCase())
                    .toList())
           );
        }

        if(dimensions != null && !dimensions.isEmpty()) {
            url.append("&dimensions = ").append(
                String.join(",", dimensions.stream()
                    .map(d -> d.name().toLowerCase())
                    .toList())
           );
        }

        if(sort != null && !sort.isEmpty()) {
            url.append("&sort = ").append(sort);
        }

        url.append("&maxResults = ").append(config.getLimits().getMaxReportRows());

        return url.toString();
    }

    private List<String> getTopVideos(int count) throws Exception {
        // Get channel's uploaded videos playlist
        String channelUrl = config.getDataApiBaseUrl() + "/channels?part = contentDetails&id = " + config.getChannelId();
        ResponseEntity<String> channelResponse = makeApiCall(channelUrl, HttpMethod.GET);

        if(!channelResponse.getStatusCode().is2xxSuccessful()) {
            return Collections.emptyList();
        }

        JsonNode channelData = objectMapper.readTree(channelResponse.getBody());
        String uploadsPlaylistId = channelData.path("items").get(0)
            .path("contentDetails").path("relatedPlaylists").path("uploads").asText();

        // Get recent videos from uploads playlist
        String videosUrl = config.getDataApiBaseUrl() + "/playlistItems?part = contentDetails" +
            "&playlistId = " + uploadsPlaylistId + "&maxResults = " + count;

        ResponseEntity<String> videosResponse = makeApiCall(videosUrl, HttpMethod.GET);

        if(!videosResponse.getStatusCode().is2xxSuccessful()) {
            return Collections.emptyList();
        }

        List<String> videoIds = new ArrayList<>();
        JsonNode videosData = objectMapper.readTree(videosResponse.getBody());

        for(JsonNode item : videosData.get("items")) {
            videoIds.add(item.path("contentDetails").path("videoId").asText());
        }

        return videoIds;
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, method, entity, String.class);
    }

    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private void refreshAccessTokenIfNeeded() {
        try {
            if(config.getRefreshToken() != null) {
                tokenRefreshService.refreshToken(
                    config.getClientId(),
                    config.getClientSecret(),
                    config.getRefreshToken(),
                    "https://oauth2.googleapis.com/token"
               );
            }
        } catch(Exception e) {
            log.error("Error refreshing YouTube Analytics access token", e);
        }
    }

    // Scheduling initialization methods
    private void scheduleChannelReports() {
        log.info("Scheduled channel reports polling for YouTube Analytics");
    }

    private void scheduleVideoReports() {
        log.info("Scheduled video reports polling for YouTube Analytics");
    }

    private void scheduleRevenueReports() {
        log.info("Scheduled revenue reports polling for YouTube Analytics");
    }

    private void scheduleEngagementReports() {
        log.info("Scheduled engagement reports polling for YouTube Analytics");
    }

    private void scheduleAudienceReports() {
        log.info("Scheduled audience reports polling for YouTube Analytics");
    }

    private void scheduleTrafficSourceReports() {
        log.info("Scheduled traffic source reports polling for YouTube Analytics");
    }

    private void scheduleRealtimeReports() {
        log.info("Scheduled realtime reports polling for YouTube Analytics");
    }

    private boolean isConfigValid() {
        return config != null
            && config.getClientId() != null
            && config.getClientSecret() != null
            && config.getAccessToken() != null;
    }

    public void setConfiguration(YouTubeAnalyticsApiConfig config) {
        this.config = config;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return List.of(
            "CHANNEL_REPORT",
            "VIDEO_REPORT",
            "REVENUE_REPORT",
            "ENGAGEMENT_REPORT",
            "AUDIENCE_REPORT",
            "TRAFFIC_SOURCE_REPORT",
            "REALTIME_REPORT",
            "DEVICE_REPORT",
            "GEOGRAPHY_REPORT"
        );
    }

    @Override
    protected Map<String, Object> getConfig() {
        return getAdapterConfig();
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("channelId", config.getChannelId());
            configMap.put("clientId", config.getClientId());
            configMap.put("clientSecret", config.getClientSecret());
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
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        // Inbound adapters typically don't send data
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // Initialize any sender-specific resources
        log.debug("Initializing YouTube Analytics inbound adapter");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // Clean up any sender-specific resources
        log.debug("Destroying YouTube Analytics inbound adapter");
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Inbound adapters typically don't send data
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = config.getApiBaseUrl() + "/v2/reports?ids=channel==" + config.getChannelId() +
                        "&startDate=" + LocalDate.now().minusDays(7) +
                        "&endDate=" + LocalDate.now().minusDays(1) +
                        "&metrics=views&maxResults=1";

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "YouTube Analytics API connection successful");
            } else {
                return AdapterResult.failure("YouTube Analytics API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test YouTube Analytics connection: " + e.getMessage());
        }
    }
}
