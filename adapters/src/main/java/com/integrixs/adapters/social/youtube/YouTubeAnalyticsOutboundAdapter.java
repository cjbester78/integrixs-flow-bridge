package com.integrixs.adapters.social.youtube;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
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

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import com.opencsv.CSVWriter;

@Component("youTubeAnalyticsOutboundAdapter")
public class YouTubeAnalyticsOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(YouTubeAnalyticsOutboundAdapter.class);


    // API URLs are configured in application.yml
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private YouTubeAnalyticsApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public YouTubeAnalyticsOutboundAdapter(
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

    public MessageDTO sendMessage(MessageDTO message) throws AdapterException {
        try {
            validateConfiguration();

            String operation = message.getHeaders().getOrDefault("operation", "").toString();
            log.info("Processing YouTube Analytics operation: {}", operation);

            switch(operation.toUpperCase()) {
                // Basic Reports
                case "GET_CHANNEL_METRICS":
                    return getChannelMetrics(message);
                case "GET_VIDEO_METRICS":
                    return getVideoMetrics(message);
                case "GET_PLAYLIST_METRICS":
                    return getPlaylistMetrics(message);

                // Revenue Reports
                case "GET_REVENUE_REPORT":
                    return getRevenueReport(message);
                case "GET_AD_PERFORMANCE":
                    return getAdPerformance(message);
                case "GET_ESTIMATED_REVENUE":
                    return getEstimatedRevenue(message);

                // Engagement Reports
                case "GET_ENGAGEMENT_METRICS":
                    return getEngagementMetrics(message);
                case "GET_AUDIENCE_RETENTION":
                    return getAudienceRetention(message);
                case "GET_CARD_PERFORMANCE":
                    return getCardPerformance(message);
                case "GET_END_SCREEN_PERFORMANCE":
                    return getEndScreenPerformance(message);

                // Audience Reports
                case "GET_DEMOGRAPHICS":
                    return getDemographics(message);
                case "GET_GEOGRAPHY":
                    return getGeography(message);
                case "GET_DEVICE_REPORTS":
                    return getDeviceReports(message);
                case "GET_PLAYBACK_LOCATION":
                    return getPlaybackLocation(message);

                // Traffic Source Reports
                case "GET_TRAFFIC_SOURCES":
                    return getTrafficSources(message);
                case "GET_SEARCH_TERMS":
                    return getSearchTerms(message);
                case "GET_EXTERNAL_TRAFFIC":
                    return getExternalTraffic(message);
                case "GET_SUGGESTED_VIDEOS":
                    return getSuggestedVideos(message);

                // Custom Reports
                case "CREATE_CUSTOM_REPORT":
                    return createCustomReport(message);
                case "GET_REPORT_JOBS":
                    return getReportJobs(message);
                case "DOWNLOAD_REPORT":
                    return downloadReport(message);

                // Comparison Reports
                case "COMPARE_PERIODS":
                    return comparePeriods(message);
                case "COMPARE_VIDEOS":
                    return compareVideos(message);
                case "BENCHMARK_PERFORMANCE":
                    return benchmarkPerformance(message);

                // Real - time Reports
                case "GET_REALTIME_METRICS":
                    return getRealtimeMetrics(message);
                case "GET_LIVE_STREAM_METRICS":
                    return getLiveStreamMetrics(message);

                // Export Reports
                case "EXPORT_TO_CSV":
                    return exportToCsv(message);
                case "EXPORT_TO_JSON":
                    return exportToJson(message);
                case "SCHEDULE_REPORT":
                    return scheduleReport(message);

                // Group Reports
                case "CREATE_GROUP":
                    return createGroup(message);
                case "GET_GROUP_METRICS":
                    return getGroupMetrics(message);
                case "UPDATE_GROUP":
                    return updateGroup(message);
                case "DELETE_GROUP":
                    return deleteGroup(message);

                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error in YouTube Analytics outbound adapter", e);
            throw new AdapterException("Failed to process YouTube Analytics operation", e);
        }
    }

    // Basic Reports Methods
    private MessageDTO getChannelMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());
        List<Metric> metrics = parseMetrics(payload.get("metrics"));
        List<Dimension> dimensions = parseDimensions(payload.get("dimensions"));

        String url = buildReportUrl(
            startDate,
            endDate,
            "channel==" + config.getChannelId(),
            metrics,
            dimensions,
            payload.path("sort").asText(null)
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "CHANNEL_METRICS_RETRIEVED");
    }

    private MessageDTO getVideoMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String videoId = payload.path("videoId").asText();
        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());
        List<Metric> metrics = parseMetrics(payload.get("metrics"));

        String url = buildReportUrl(
            startDate,
            endDate,
            "video==" + videoId,
            metrics,
            null,
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "VIDEO_METRICS_RETRIEVED");
    }

    private MessageDTO getPlaylistMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String playlistId = payload.path("playlistId").asText();
        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());
        List<Metric> metrics = Arrays.asList(
            Metric.PLAYLIST_STARTS,
            Metric.VIEWS_PER_PLAYLIST_START,
            Metric.AVERAGE_TIME_IN_PLAYLIST
       );

        String url = buildReportUrl(
            startDate,
            endDate,
            "playlist==" + playlistId,
            metrics,
            Arrays.asList(Dimension.PLAYLIST),
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "PLAYLIST_METRICS_RETRIEVED");
    }

    // Revenue Reports Methods
    private MessageDTO getRevenueReport(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.ESTIMATED_REVENUE,
            Metric.ESTIMATED_AD_REVENUE,
            Metric.ESTIMATED_RED_PARTNER_REVENUE,
            Metric.GROSS_REVENUE,
            Metric.CPM,
            Metric.PLAYBACK_BASED_CPM
       );

        List<Dimension> dimensions = Arrays.asList(Dimension.DAY);
        if(payload.has("dimensions")) {
            dimensions = parseDimensions(payload.get("dimensions"));
        }

        String url = buildReportUrl(
            startDate,
            endDate,
            "channel==" + config.getChannelId(),
            metrics,
            dimensions,
            " - estimatedRevenue"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "REVENUE_REPORT_RETRIEVED");
    }

    private MessageDTO getAdPerformance(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.AD_IMPRESSIONS,
            Metric.MONETIZED_PLAYBACKS,
            Metric.ESTIMATED_AD_REVENUE,
            Metric.CPM
       );

        String filter = "channel==" + config.getChannelId();
        if(payload.has("adType")) {
            filter += ";adType==" + payload.get("adType").asText();
        }

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            Arrays.asList(Dimension.AD_TYPE, Dimension.DAY),
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "AD_PERFORMANCE_RETRIEVED");
    }

    private MessageDTO getEstimatedRevenue(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        TimePeriod period = TimePeriod.valueOf(payload.path("period").asText("LAST_30_DAYS"));
        LocalDate endDate = LocalDate.now().minusDays(2); // 48hr delay
        LocalDate startDate = calculateStartDate(period, endDate);

        List<Metric> metrics = Arrays.asList(
            Metric.ESTIMATED_REVENUE,
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED
       );

        String url = buildReportUrl(
            startDate,
            endDate,
            "channel==" + config.getChannelId(),
            metrics,
            Arrays.asList(Dimension.DAY),
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);

        // Calculate summary
        JsonNode data = objectMapper.readTree(response.getBody());
        double totalRevenue = 0;
        long totalViews = 0;

        if(data.has("rows")) {
            for(JsonNode row : data.get("rows")) {
                totalRevenue += row.get(1).asDouble(0);
                totalViews += row.get(2).asLong(0);
            }
        }

        ObjectNode summary = objectMapper.createObjectNode();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalViews", totalViews);
        summary.put("revenuePerView", totalViews > 0 ? totalRevenue / totalViews : 0);
        summary.set("details", data);

        MessageDTO result = createResponseMessage(response, "ESTIMATED_REVENUE_RETRIEVED");
        result.setPayload(summary.toString());
        return result;
    }

    // Engagement Reports Methods
    private MessageDTO getEngagementMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.LIKES,
            Metric.DISLIKES,
            Metric.COMMENTS,
            Metric.SHARES,
            Metric.SUBSCRIBERS_GAINED,
            Metric.SUBSCRIBERS_LOST,
            Metric.AVERAGE_VIEW_PERCENTAGE
       );

        String filter = "channel==" + config.getChannelId();
        if(payload.has("videoId")) {
            filter = "video==" + payload.get("videoId").asText();
        }

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            payload.has("dimensions") ? parseDimensions(payload.get("dimensions")) : null,
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "ENGAGEMENT_METRICS_RETRIEVED");
    }

    private MessageDTO getAudienceRetention(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String videoId = payload.path("videoId").asText();

        // Audience retention requires special handling
        // This is a simplified version
        List<Metric> metrics = Arrays.asList(
            Metric.AVERAGE_VIEW_DURATION,
            Metric.AVERAGE_VIEW_PERCENTAGE
       );

        String url = buildReportUrl(
            LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(2),
            "video==" + videoId,
            metrics,
            null,
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "AUDIENCE_RETENTION_RETRIEVED");
    }

    private MessageDTO getCardPerformance(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.CARD_IMPRESSIONS,
            Metric.CARD_CLICKS,
            Metric.CARD_CLICK_RATE,
            Metric.CARD_TEASER_IMPRESSIONS,
            Metric.CARD_TEASER_CLICKS,
            Metric.CARD_TEASER_CLICK_RATE
       );

        String filter = "channel==" + config.getChannelId();
        if(payload.has("videoId")) {
            filter = "video==" + payload.get("videoId").asText();
        }

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            Arrays.asList(Dimension.VIDEO),
            " - cardClicks"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "CARD_PERFORMANCE_RETRIEVED");
    }

    private MessageDTO getEndScreenPerformance(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.END_SCREEN_ELEMENT_IMPRESSIONS,
            Metric.END_SCREEN_ELEMENT_CLICKS,
            Metric.END_SCREEN_ELEMENT_CLICK_RATE
       );

        String filter = "channel==" + config.getChannelId();
        if(payload.has("videoId")) {
            filter = "video==" + payload.get("videoId").asText();
        }

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            Arrays.asList(Dimension.VIDEO),
            " - endScreenElementClicks"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "END_SCREEN_PERFORMANCE_RETRIEVED");
    }

    // Audience Reports Methods
    private MessageDTO getDemographics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED,
            Metric.AVERAGE_VIEW_DURATION
       );

        List<Dimension> dimensions = Arrays.asList(
            Dimension.AGE_GROUP,
            Dimension.GENDER
       );

        String filter = "channel==" + config.getChannelId();
        if(payload.has("videoId")) {
            filter = "video==" + payload.get("videoId").asText();
        }

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            dimensions,
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "DEMOGRAPHICS_RETRIEVED");
    }

    private MessageDTO getGeography(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED,
            Metric.AVERAGE_VIEW_DURATION
       );

        List<Dimension> dimensions = Arrays.asList(Dimension.COUNTRY);
        if(payload.path("includeProvinces").asBoolean(false)) {
            dimensions = Arrays.asList(Dimension.COUNTRY, Dimension.PROVINCE);
        }

        String filter = "channel==" + config.getChannelId();
        if(payload.has("country")) {
            filter += ";country==" + payload.get("country").asText();
        }

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            dimensions,
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "GEOGRAPHY_RETRIEVED");
    }

    private MessageDTO getDeviceReports(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED
       );

        List<Dimension> dimensions = Arrays.asList(
            Dimension.DEVICE_TYPE,
            Dimension.OPERATING_SYSTEM
       );

        String filter = "channel==" + config.getChannelId();

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            dimensions,
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "DEVICE_REPORTS_RETRIEVED");
    }

    private MessageDTO getPlaybackLocation(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED
       );

        List<Dimension> dimensions = Arrays.asList(
            Dimension.PLAYBACK_LOCATION_TYPE
       );

        if(payload.path("includeDetails").asBoolean(false)) {
            dimensions = Arrays.asList(
                Dimension.PLAYBACK_LOCATION_TYPE,
                Dimension.PLAYBACK_LOCATION_DETAIL
           );
        }

        String filter = "channel==" + config.getChannelId();

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            dimensions,
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "PLAYBACK_LOCATION_RETRIEVED");
    }

    // Traffic Source Reports Methods
    private MessageDTO getTrafficSources(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED
       );

        List<Dimension> dimensions = Arrays.asList(
            Dimension.TRAFFIC_SOURCE_TYPE
       );

        if(payload.path("includeDetails").asBoolean(false)) {
            dimensions = Arrays.asList(
                Dimension.TRAFFIC_SOURCE_TYPE,
                Dimension.TRAFFIC_SOURCE_DETAIL
           );
        }

        String filter = "channel==" + config.getChannelId();

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            dimensions,
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "TRAFFIC_SOURCES_RETRIEVED");
    }

    private MessageDTO getSearchTerms(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED
       );

        String filter = "channel==" + config.getChannelId() +
                       ";trafficSourceType==YT_SEARCH";

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            metrics,
            Arrays.asList(Dimension.SEARCH_TERM),
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "SEARCH_TERMS_RETRIEVED");
    }

    private MessageDTO getExternalTraffic(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        String filter = "channel==" + config.getChannelId() +
                       ";trafficSourceType==EXT_URL";

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            Arrays.asList(Metric.VIEWS, Metric.ESTIMATED_MINUTES_WATCHED),
            Arrays.asList(Dimension.TRAFFIC_SOURCE_DETAIL),
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "EXTERNAL_TRAFFIC_RETRIEVED");
    }

    private MessageDTO getSuggestedVideos(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String videoId = payload.path("videoId").asText();

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());

        String filter = "video==" + videoId +
                       ";trafficSourceType==RELATED_VIDEO";

        String url = buildReportUrl(
            startDate,
            endDate,
            filter,
            Arrays.asList(Metric.VIEWS),
            Arrays.asList(Dimension.TRAFFIC_SOURCE_DETAIL),
            " - views"
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "SUGGESTED_VIDEOS_RETRIEVED");
    }

    // Custom Reports Methods
    private MessageDTO createCustomReport(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Build custom report based on user specifications
        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());
        String filters = payload.path("filters").asText();
        List<Metric> metrics = parseMetrics(payload.get("metrics"));
        List<Dimension> dimensions = parseDimensions(payload.get("dimensions"));
        String sort = payload.path("sort").asText(null);

        String url = buildReportUrl(
            startDate,
            endDate,
            filters,
            metrics,
            dimensions,
            sort
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "CUSTOM_REPORT_CREATED");
    }

    private MessageDTO getReportJobs(MessageDTO message) throws Exception {
        // YouTube Reporting API for scheduled reports
        String url = config.getReportingApiBaseUrl() + "/v1/jobs?onBehalfOfContentOwner = " +
                    config.getChannelId();

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "REPORT_JOBS_RETRIEVED");
    }

    private MessageDTO downloadReport(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String reportUrl = payload.path("reportUrl").asText();

        // Download report file
        ResponseEntity<String> response = makeApiCall(reportUrl, HttpMethod.GET, null);
        return createResponseMessage(response, "REPORT_DOWNLOADED");
    }

    // Comparison Reports Methods
    private MessageDTO comparePeriods(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Period 1
        LocalDate period1Start = LocalDate.parse(payload.path("period1Start").asText());
        LocalDate period1End = LocalDate.parse(payload.path("period1End").asText());

        // Period 2
        LocalDate period2Start = LocalDate.parse(payload.path("period2Start").asText());
        LocalDate period2End = LocalDate.parse(payload.path("period2End").asText());

        List<Metric> metrics = parseMetrics(payload.get("metrics"));
        String filter = "channel==" + config.getChannelId();

        // Get data for both periods
        String url1 = buildReportUrl(period1Start, period1End, filter, metrics, null, null);
        String url2 = buildReportUrl(period2Start, period2End, filter, metrics, null, null);

        ResponseEntity<String> response1 = makeApiCall(url1, HttpMethod.GET, null);
        ResponseEntity<String> response2 = makeApiCall(url2, HttpMethod.GET, null);

        // Combine results for comparison
        ObjectNode comparison = objectMapper.createObjectNode();
        comparison.set("period1", objectMapper.readTree(response1.getBody()));
        comparison.set("period2", objectMapper.readTree(response2.getBody()));

        MessageDTO result = createResponseMessage(response1, "PERIODS_COMPARED");
        result.setPayload(comparison.toString());
        return result;
    }

    private MessageDTO compareVideos(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        List<String> videoIds = new ArrayList<>();

        for(JsonNode videoId : payload.get("videoIds")) {
            videoIds.add(videoId.asText());
        }

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());
        List<Metric> metrics = parseMetrics(payload.get("metrics"));

        // Get metrics for each video
        ObjectNode comparison = objectMapper.createObjectNode();

        for(String videoId : videoIds) {
            String url = buildReportUrl(
                startDate,
                endDate,
                "video==" + videoId,
                metrics,
                null,
                null
           );

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
            comparison.set(videoId, objectMapper.readTree(response.getBody()));
        }

        MessageDTO result = new MessageDTO();
        result.setPayload(comparison.toString());
        result.setHeaders(Map.of("operation", "VIDEOS_COMPARED"));

        return result;
    }

    private MessageDTO benchmarkPerformance(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Get channel averages for benchmarking
        LocalDate endDate = LocalDate.now().minusDays(2);
        LocalDate startDate = endDate.minusDays(90); // 3 - month average

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.AVERAGE_VIEW_DURATION,
            Metric.AVERAGE_VIEW_PERCENTAGE,
            Metric.SUBSCRIBERS_GAINED
       );

        String url = buildReportUrl(
            startDate,
            endDate,
            "channel==" + config.getChannelId(),
            metrics,
            Arrays.asList(Dimension.VIDEO),
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);

        // Calculate benchmarks
        JsonNode data = objectMapper.readTree(response.getBody());
        ObjectNode benchmarks = calculateBenchmarks(data);

        MessageDTO result = createResponseMessage(response, "PERFORMANCE_BENCHMARKED");
        result.setPayload(benchmarks.toString());
        return result;
    }

    // Real - time Reports Methods
    private MessageDTO getRealtimeMetrics(MessageDTO message) throws Exception {
        // Note: YouTube Analytics API has a 48 - hour delay
        // This gets the most recent available data
        LocalDate today = LocalDate.now();
        LocalDate recentDate = today.minusDays(2);

        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED,
            Metric.SUBSCRIBERS_GAINED
       );

        String url = buildReportUrl(
            recentDate,
            recentDate,
            "channel==" + config.getChannelId(),
            metrics,
            null,
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "REALTIME_METRICS_RETRIEVED");
    }

    private MessageDTO getLiveStreamMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String broadcastId = payload.path("broadcastId").asText();

        // Get metrics for live stream
        List<Metric> metrics = Arrays.asList(
            Metric.VIEWS,
            Metric.ESTIMATED_MINUTES_WATCHED,
            Metric.LIKES,
            Metric.COMMENTS
       );

        String url = buildReportUrl(
            LocalDate.now().minusDays(1),
            LocalDate.now(),
            "video==" + broadcastId + ";liveOrOnDemand==LIVE",
            metrics,
            null,
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "LIVE_STREAM_METRICS_RETRIEVED");
    }

    // Export Reports Methods
    private MessageDTO exportToCsv(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Get the report data first
        MessageDTO reportData = createCustomReport(message);
        JsonNode data = objectMapper.readTree(reportData.getPayload());

        // Convert to CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));

        // Write headers
        if(data.has("columnHeaders")) {
            List<String> headers = new ArrayList<>();
            for(JsonNode header : data.get("columnHeaders")) {
                headers.add(header.path("name").asText());
            }
            csvWriter.writeNext(headers.toArray(new String[0]));
        }

        // Write data rows
        if(data.has("rows")) {
            for(JsonNode row : data.get("rows")) {
                List<String> values = new ArrayList<>();
                for(JsonNode value : row) {
                    values.add(value.asText());
                }
                csvWriter.writeNext(values.toArray(new String[0]));
            }
        }

        csvWriter.close();

        MessageDTO result = new MessageDTO();
        result.setPayload(outputStream.toString());
        result.setHeaders(Map.of(
            "operation", "CSV_EXPORTED",
            "contentType", "text/csv",
            "filename", "youtube_analytics_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".csv"
       ));

        return result;
    }

    private MessageDTO exportToJson(MessageDTO message) throws Exception {
        // Get the report data
        MessageDTO reportData = createCustomReport(message);

        // Already in JSON format
        MessageDTO result = new MessageDTO();
        result.setPayload(reportData.getPayload());
        result.setHeaders(Map.of(
            "operation", "JSON_EXPORTED",
            "contentType", "application/json",
            "filename", "youtube_analytics_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".json"
       ));

        return result;
    }

    private MessageDTO scheduleReport(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Create a reporting job using YouTube Reporting API
        String url = config.getReportingApiBaseUrl() + "/v1/reportTypes";

        // First get available report types
        ResponseEntity<String> typesResponse = makeApiCall(url, HttpMethod.GET, null);

        // Create job for selected report type
        ObjectNode jobRequest = objectMapper.createObjectNode();
        jobRequest.put("reportTypeId", payload.path("reportTypeId").asText());
        jobRequest.put("name", payload.path("name").asText());

        String createJobUrl = config.getReportingApiBaseUrl() + "/v1/jobs";
        ResponseEntity<String> response = makeApiCall(createJobUrl, HttpMethod.POST, jobRequest.toString());

        return createResponseMessage(response, "REPORT_SCHEDULED");
    }

    // Group Reports Methods
    private MessageDTO createGroup(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getDataApiBaseUrl() + "/groups";

        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode snippet = requestBody.putObject("snippet");
        snippet.put("title", payload.path("title").asText());

        ArrayNode items = requestBody.putArray("items");
        for(JsonNode videoId : payload.get("videoIds")) {
            ObjectNode item = items.addObject();
            item.put("id", videoId.asText());
            item.put("kind", "youtube#video");
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "GROUP_CREATED");
    }

    private MessageDTO getGroupMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String groupId = payload.path("groupId").asText();

        LocalDate startDate = LocalDate.parse(payload.path("startDate").asText());
        LocalDate endDate = LocalDate.parse(payload.path("endDate").asText());
        List<Metric> metrics = parseMetrics(payload.get("metrics"));

        String url = buildReportUrl(
            startDate,
            endDate,
            "group==" + groupId,
            metrics,
            Arrays.asList(Dimension.GROUP),
            null
       );

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "GROUP_METRICS_RETRIEVED");
    }

    private MessageDTO updateGroup(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String groupId = payload.path("groupId").asText();

        String url = config.getDataApiBaseUrl() + "/groups?id = " + groupId;

        ObjectNode requestBody = objectMapper.createObjectNode();
        if(payload.has("title")) {
            ObjectNode snippet = requestBody.putObject("snippet");
            snippet.put("title", payload.get("title").asText());
        }

        if(payload.has("videoIds")) {
            ArrayNode items = requestBody.putArray("items");
            for(JsonNode videoId : payload.get("videoIds")) {
                ObjectNode item = items.addObject();
                item.put("id", videoId.asText());
                item.put("kind", "youtube#video");
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "GROUP_UPDATED");
    }

    private MessageDTO deleteGroup(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String groupId = payload.path("groupId").asText();

        String url = config.getDataApiBaseUrl() + "/groups?id = " + groupId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "GROUP_DELETED");
    }

    // Helper Methods
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
                    .map(m -> m.name().toLowerCase().replace("_", ""))
                    .toList())
           );
        }

        if(dimensions != null && !dimensions.isEmpty()) {
            url.append("&dimensions = ").append(
                String.join(",", dimensions.stream()
                    .map(d -> d.name().toLowerCase().replace("_", ""))
                    .toList())
           );
        }

        if(sort != null && !sort.isEmpty()) {
            url.append("&sort = ").append(sort);
        }

        url.append("&maxResults = ").append(config.getLimits().getMaxReportRows());

        return url.toString();
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        rateLimiterService.acquire("youtube_analytics_api", 1);

        return restTemplate.exchange(url, method, entity, String.class);
    }

    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private List<Metric> parseMetrics(JsonNode metricsNode) {
        List<Metric> metrics = new ArrayList<>();
        if(metricsNode != null && metricsNode.isArray()) {
            for(JsonNode metric : metricsNode) {
                try {
                    metrics.add(Metric.valueOf(metric.asText().toUpperCase()));
                } catch(IllegalArgumentException e) {
                    log.warn("Unknown metric: {}", metric.asText());
                }
            }
        }
        return metrics;
    }

    private List<Dimension> parseDimensions(JsonNode dimensionsNode) {
        List<Dimension> dimensions = new ArrayList<>();
        if(dimensionsNode != null && dimensionsNode.isArray()) {
            for(JsonNode dimension : dimensionsNode) {
                try {
                    dimensions.add(Dimension.valueOf(dimension.asText().toUpperCase()));
                } catch(IllegalArgumentException e) {
                    log.warn("Unknown dimension: {}", dimension.asText());
                }
            }
        }
        return dimensions;
    }

    private LocalDate calculateStartDate(TimePeriod period, LocalDate endDate) {
        switch(period) {
            case LAST_7_DAYS:
                return endDate.minusDays(7);
            case LAST_28_DAYS:
                return endDate.minusDays(28);
            case LAST_30_DAYS:
                return endDate.minusDays(30);
            case LAST_90_DAYS:
                return endDate.minusDays(90);
            case LAST_365_DAYS:
                return endDate.minusYears(1);
            case MONTH_TO_DATE:
                return endDate.withDayOfMonth(1);
            case QUARTER_TO_DATE:
                int quarter = (endDate.getMonthValue() - 1) / 3;
                return endDate.withMonth(quarter * 3 + 1).withDayOfMonth(1);
            case YEAR_TO_DATE:
                return endDate.withDayOfYear(1);
            case LIFETIME:
                return LocalDate.of(2005, 1, 1); // YouTube launch date
            default:
                return endDate.minusDays(30);
        }
    }

    private ObjectNode calculateBenchmarks(JsonNode data) {
        ObjectNode benchmarks = objectMapper.createObjectNode();

        if(data.has("rows") && data.get("rows").size() > 0) {
            double totalViews = 0;
            double totalDuration = 0;
            double totalViewPercentage = 0;
            int count = 0;

            for(JsonNode row : data.get("rows")) {
                totalViews += row.get(1).asDouble(0);
                totalDuration += row.get(2).asDouble(0);
                totalViewPercentage += row.get(3).asDouble(0);
                count++;
            }

            benchmarks.put("averageViews", totalViews / count);
            benchmarks.put("averageDuration", totalDuration / count);
            benchmarks.put("averageViewPercentage", totalViewPercentage / count);
        }

        return benchmarks;
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getClientId() == null || config.getClientSecret() == null) {
            throw new AdapterException("YouTube Analytics API credentials are not configured");
        }
        if(config.getAccessToken() == null) {
            throw new AdapterException("YouTube Analytics access token is not configured");
        }
    }

    private MessageDTO createResponseMessage(ResponseEntity<String> response, String operation) {
        MessageDTO responseMessage = new MessageDTO();
        responseMessage.setCorrelationId(UUID.randomUUID().toString());
        responseMessage.setTimestamp(LocalDateTime.now());
        responseMessage.setStatus(response.getStatusCode().is2xxSuccessful() ? MessageStatus.SUCCESS : MessageStatus.FAILED);
        responseMessage.setHeaders(Map.of(
            "operation", operation,
            "statusCode", response.getStatusCodeValue(),
            "source", "youtube_analytics"
       ));
        responseMessage.setPayload(response.getBody());
        return responseMessage;
    }

    public void setConfiguration(YouTubeAnalyticsApiConfig config) {
        this.config = config;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            return sendMessage(message);
        } catch (AdapterException e) {
            return createErrorResponse(message, e.getMessage());
        }
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
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
    protected void doReceiverInitialize() throws Exception {
        // No initialization needed for analytics adapter
        log.debug("Initializing YouTube Analytics outbound adapter");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // No cleanup needed for analytics adapter
        log.debug("Destroying YouTube Analytics outbound adapter");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Analytics adapter doesn't receive data - it's query-based
        log.debug("Outbound adapter does not receive data");
        return AdapterResult.success(null);
    }

    @Override
    protected long getPollingIntervalMs() {
        // Analytics adapter doesn't support polling
        return 0;
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = config.getApiBaseUrl() + "/v2/reports?ids=channel==" + config.getChannelId() +
                        "&startDate=" + LocalDate.now().minusDays(7) +
                        "&endDate=" + LocalDate.now().minusDays(1) +
                        "&metrics=views&maxResults=1";

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);

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
