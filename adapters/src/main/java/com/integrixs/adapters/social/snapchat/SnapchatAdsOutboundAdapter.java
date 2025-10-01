package com.integrixs.adapters.social.snapchat;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.snapchat.SnapchatAdsApiConfig.*;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class SnapchatAdsOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(SnapchatAdsOutboundAdapter.class);


    // API URLs are configured in application.yml
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Autowired
    private SnapchatAdsApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public SnapchatAdsOutboundAdapter(
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            SnapchatAdsApiConfig config) {
        super(rateLimiterService, credentialEncryptionService);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.config = config;
    }

    public AdapterType getType() {
        return AdapterType.REST;
    }

    public String getName() {
        return "Snapchat Ads API Outbound Adapter";
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            String operation = (String) message.getHeaders().get("operation");
            if(operation == null) {
                return createErrorResponse(message, "Operation header is required");
            }

            log.info("Processing Snapchat Ads operation: {}", operation);

            switch(operation.toUpperCase()) {
                // Campaign operations
                case "CREATE_CAMPAIGN":
                    return createCampaign(message);
                case "UPDATE_CAMPAIGN":
                    return updateCampaign(message);
                case "PAUSE_CAMPAIGN":
                    return pauseCampaign(message);
                case "RESUME_CAMPAIGN":
                    return resumeCampaign(message);
                case "DELETE_CAMPAIGN":
                    return deleteCampaign(message);

                // Ad Squad(Ad Set) operations
                case "CREATE_AD_SQUAD":
                    return createAdSquad(message);
                case "UPDATE_AD_SQUAD":
                    return updateAdSquad(message);
                case "PAUSE_AD_SQUAD":
                    return pauseAdSquad(message);
                case "RESUME_AD_SQUAD":
                    return resumeAdSquad(message);
                case "DELETE_AD_SQUAD":
                    return deleteAdSquad(message);

                // Creative operations
                case "CREATE_CREATIVE":
                    return createCreative(message);
                case "UPDATE_CREATIVE":
                    return updateCreative(message);
                case "DELETE_CREATIVE":
                    return deleteCreative(message);

                // Media operations
                case "UPLOAD_MEDIA":
                    return uploadMedia(message);

                // Audience operations
                case "CREATE_AUDIENCE":
                    return createAudience(message);
                case "CREATE_LOOKALIKE_AUDIENCE":
                    return createLookalikeAudience(message);
                case "UPDATE_AUDIENCE":
                    return updateAudience(message);
                case "DELETE_AUDIENCE":
                    return deleteAudience(message);

                // Pixel operations
                case "CREATE_PIXEL":
                    return createPixel(message);
                case "UPDATE_PIXEL":
                    return updatePixel(message);
                case "FIRE_PIXEL_EVENT":
                    return firePixelEvent(message);

                // Reporting operations
                case "GENERATE_REPORT":
                    return generateReport(message);
                case "GET_STATS":
                    return getStats(message);

                // Bulk operations
                case "BULK_CREATE":
                    return bulkCreate(message);
                case "BULK_UPDATE":
                    return bulkUpdate(message);
                case "BULK_DELETE":
                    return bulkDelete(message);

                // Advanced features
                case "CREATE_AR_LENS":
                    return createARLens(message);
                case "CREATE_FILTER":
                    return createFilter(message);
                case "CREATE_BRANDED_MOMENT":
                    return createBrandedMoment(message);

                // Catalog operations
                case "CREATE_CATALOG":
                    return createCatalog(message);
                case "UPDATE_CATALOG":
                    return updateCatalog(message);
                case "UPLOAD_PRODUCTS":
                    return uploadProducts(message);

                // Data export
                case "EXPORT_DATA":
                    return exportData(message);

                default:
                    return createErrorResponse(message, "Unknown operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error processing Snapchat Ads operation", e);
            return createErrorResponse(message, "Failed to process operation: " + e.getMessage());
        }
    }

    // Campaign Operations
    private MessageDTO createCampaign(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            ObjectNode campaign = objectMapper.createObjectNode();
            campaign.put("ad_account_id", config.getAdAccountId());
            campaign.put("name", content.get("name").asText());
            campaign.put("objective", content.get("objective").asText());
            campaign.put("status", content.has("status") ?
                content.get("status").asText() : "PAUSED");
            campaign.put("daily_budget_micro", content.get("daily_budget").asLong() * 1000000);
            campaign.put("start_time", content.get("start_time").asText());

            if(content.has("end_time")) {
                campaign.put("end_time", content.get("end_time").asText());
            }

            if(content.has("lifetime_budget")) {
                campaign.put("lifetime_budget_micro",
                    content.get("lifetime_budget").asLong() * 1000000);
            }

            String endpoint = String.format("%s/adaccounts/%s/campaigns",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, campaign
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating campaign", e);
            return createErrorResponse(message, "Failed to create campaign: " + e.getMessage());
        }
    }

    private MessageDTO updateCampaign(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            String campaignId = content.get("campaign_id").asText();

            ObjectNode updates = objectMapper.createObjectNode();
            if(content.has("name")) {
                updates.put("name", content.get("name").asText());
            }
            if(content.has("daily_budget")) {
                updates.put("daily_budget_micro", content.get("daily_budget").asLong() * 1000000);
            }
            if(content.has("end_time")) {
                updates.put("end_time", content.get("end_time").asText());
            }

            String endpoint = String.format("%s/campaigns/%s", config.getApiBaseUrl(), campaignId);

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.PUT, null, updates
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error updating campaign", e);
            return createErrorResponse(message, "Failed to update campaign: " + e.getMessage());
        }
    }

    // Ad Squad Operations
    private MessageDTO createAdSquad(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            ObjectNode adSquad = objectMapper.createObjectNode();
            adSquad.put("campaign_id", content.get("campaign_id").asText());
            adSquad.put("name", content.get("name").asText());
            adSquad.put("type", content.get("type").asText());
            adSquad.put("status", content.has("status") ?
                content.get("status").asText() : "PAUSED");
            adSquad.put("bid_strategy", content.get("bid_strategy").asText());
            adSquad.put("optimization_goal", content.get("optimization_goal").asText());
            adSquad.put("daily_budget_micro", content.get("daily_budget").asLong() * 1000000);

            if(content.has("bid_micro")) {
                adSquad.put("bid_micro", content.get("bid").asLong() * 1000000);
            }

            // Targeting
            if(content.has("targeting")) {
                adSquad.set("targeting", createTargetingSpec(content.get("targeting")));
            }

            // Schedule
            if(content.has("start_time")) {
                adSquad.put("start_time", content.get("start_time").asText());
            }
            if(content.has("end_time")) {
                adSquad.put("end_time", content.get("end_time").asText());
            }

            String endpoint = String.format("%s/adaccounts/%s/adsquads",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, adSquad
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating ad squad", e);
            return createErrorResponse(message, "Failed to create ad squad: " + e.getMessage());
        }
    }

    // Creative Operations
    private MessageDTO createCreative(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            ObjectNode creative = objectMapper.createObjectNode();
            creative.put("ad_squad_id", content.get("ad_squad_id").asText());
            creative.put("name", content.get("name").asText());
            creative.put("type", content.get("type").asText());

            // Creative properties
            ObjectNode properties = objectMapper.createObjectNode();

            if(content.has("headline")) {
                properties.put("headline", content.get("headline").asText());
            }
            if(content.has("brand_name")) {
                properties.put("brand_name", content.get("brand_name").asText());
            }
            if(content.has("call_to_action")) {
                properties.put("call_to_action", content.get("call_to_action").asText());
            }
            if(content.has("top_snap_media_id")) {
                properties.put("top_snap_media_id", content.get("top_snap_media_id").asText());
            }

            creative.set("properties", properties);

            String endpoint = String.format("%s/adaccounts/%s/creatives",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, creative
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating creative", e);
            return createErrorResponse(message, "Failed to create creative: " + e.getMessage());
        }
    }

    private MessageDTO uploadMedia(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            String mediaType = content.get("media_type").asText();

            // Validate media type and size
            if(!Arrays.asList("IMAGE", "VIDEO").contains(mediaType.toUpperCase())) {
                return createErrorResponse(message, "Invalid media type: " + mediaType);
            }

            String base64Data = content.get("media_data").asText();
            byte[] mediaBytes = Base64.getDecoder().decode(base64Data);

            // Check size limits
            if(mediaType.equalsIgnoreCase("VIDEO") &&
                mediaBytes.length > config.getLimits().getMaxVideoSizeMB() * 1024 * 1024) {
                return createErrorResponse(message, "Video size exceeds limit");
            }
            if(mediaType.equalsIgnoreCase("IMAGE") &&
                mediaBytes.length > config.getLimits().getMaxImageSizeMB() * 1024 * 1024) {
                return createErrorResponse(message, "Image size exceeds limit");
            }

            // Upload media
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + getAccessToken());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", new ByteArrayResource(mediaBytes) {
                @Override
                public String getFilename() {
                    return content.has("filename") ?
                        content.get("filename").asText() : "media." + mediaType.toLowerCase();
                }
            });
            body.add("type", mediaType);
            body.add("ad_account_id", config.getAdAccountId());

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                config.getMediaUploadUrl(), HttpMethod.POST, requestEntity, String.class
           );

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return createSuccessResponse(message, responseJson);
        } catch(Exception e) {
            log.error("Error uploading media", e);
            return createErrorResponse(message, "Failed to upload media: " + e.getMessage());
        }
    }

    // Audience Operations
    private MessageDTO createAudience(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            ObjectNode audience = objectMapper.createObjectNode();
            audience.put("ad_account_id", config.getAdAccountId());
            audience.put("name", content.get("name").asText());
            audience.put("type", content.get("type").asText());
            audience.put("description", content.has("description") ?
                content.get("description").asText() : "");

            // Audience data
            if(content.has("audience_data")) {
                audience.set("data", content.get("audience_data"));
            }

            // Match type for customer lists
            if(content.has("match_type")) {
                audience.put("match_type", content.get("match_type").asText());
            }

            String endpoint = String.format("%s/adaccounts/%s/segments",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, audience
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating audience", e);
            return createErrorResponse(message, "Failed to create audience: " + e.getMessage());
        }
    }

    private MessageDTO createLookalikeAudience(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            ObjectNode lookalike = objectMapper.createObjectNode();
            lookalike.put("ad_account_id", config.getAdAccountId());
            lookalike.put("name", content.get("name").asText());
            lookalike.put("type", "LOOKALIKE");
            lookalike.put("source_segment_id", content.get("source_audience_id").asText());
            lookalike.put("lookalike_spec", objectMapper.createObjectNode()
                .put("country", content.get("country").asText())
                .put("similarity_percent", content.get("similarity").asInt()));

            String endpoint = String.format("%s/adaccounts/%s/segments",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, lookalike
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating lookalike audience", e);
            return createErrorResponse(message, "Failed to create lookalike audience: " + e.getMessage());
        }
    }

    // Pixel Operations
    private MessageDTO createPixel(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            ObjectNode pixel = objectMapper.createObjectNode();
            pixel.put("ad_account_id", config.getAdAccountId());
            pixel.put("name", content.get("name").asText());
            pixel.put("description", content.has("description") ?
                content.get("description").asText() : "");

            String endpoint = String.format("%s/adaccounts/%s/pixels",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, pixel
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating pixel", e);
            return createErrorResponse(message, "Failed to create pixel: " + e.getMessage());
        }
    }

    private MessageDTO firePixelEvent(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            String pixelId = content.has("pixel_id") ?
                content.get("pixel_id").asText() : config.getPixelId();

            ObjectNode event = objectMapper.createObjectNode();
            event.put("event_type", content.get("event_type").asText());
            event.put("event_time", content.has("event_time") ?
                content.get("event_time").asText() : Instant.now().toString());
            event.put("user_agent", content.get("user_agent").asText());
            event.put("ip_address", content.get("ip_address").asText());

            // Event properties
            if(content.has("event_properties")) {
                event.set("event_properties", content.get("event_properties"));
            }

            // User data for matching
            if(content.has("user_data")) {
                event.set("user_data", content.get("user_data"));
            }

            String endpoint = String.format("%s/pixels/%s/events", config.getApiBaseUrl(), pixelId);

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, event
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error firing pixel event", e);
            return createErrorResponse(message, "Failed to fire pixel event: " + e.getMessage());
        }
    }

    // Reporting Operations
    private MessageDTO generateReport(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());

            Map<String, String> params = new HashMap<>();
            params.put("start_time", content.get("start_time").asText());
            params.put("end_time", content.get("end_time").asText());
            params.put("granularity", content.get("granularity").asText());
            params.put("report_dimension", content.get("dimension").asText());
            params.put("swipe_up_attribution_window",
                content.has("attribution_window") ?
                    content.get("attribution_window").asText() : "28_DAY");
            params.put("view_attribution_window", "1_DAY");

            // Metrics
            if(content.has("metrics")) {
                List<String> metrics = new ArrayList<>();
                content.get("metrics").forEach(metric ->
                    metrics.add(metric.asText())
               );
                params.put("metrics", String.join(",", metrics));
            }

            // Filters
            if(content.has("filters")) {
                JsonNode filters = content.get("filters");
                filters.fieldNames().forEachRemaining(field ->
                    params.put(field, filters.get(field).asText())
               );
            }

            String endpoint = String.format("%s/adaccounts/%s/stats",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.GET, params
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error generating report", e);
            return createErrorResponse(message, "Failed to generate report: " + e.getMessage());
        }
    }

    private MessageDTO getStats(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            String entityType = content.get("entity_type").asText();
            String entityId = content.get("entity_id").asText();

            Map<String, String> params = new HashMap<>();
            params.put("granularity", "DAY");
            params.put("start_time", content.has("start_time") ?
                content.get("start_time").asText() : getDefaultStartTime());
            params.put("end_time", content.has("end_time") ?
                content.get("end_time").asText() : getDefaultEndTime());

            String endpoint = String.format("%s/%s/%s/stats",
                config.getApiBaseUrl(), entityType.toLowerCase(), entityId);

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.GET, params
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error getting stats", e);
            return createErrorResponse(message, "Failed to get stats: " + e.getMessage());
        }
    }

    private MessageDTO bulkCreate(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            JsonNode items = content.get("items");

            List<JsonNode> results = new ArrayList<>();
            items.forEach(item -> {
                try {
                    MessageDTO itemMessage = new MessageDTO();
                    itemMessage.setHeaders(message.getHeaders());
                    itemMessage.setPayload(item.toString());
                    MessageDTO result = createEntity(itemMessage, content.get("entity_type").asText());
                    results.add(objectMapper.readTree(result.getContent()));
                } catch(Exception e) {
                    log.error("Error creating item", e);
                }
            });

            return createSuccessResponse(message, objectMapper.valueToTree(results));
        } catch(Exception e) {
            log.error("Error in bulk create", e);
            return createErrorResponse(message, "Failed to perform bulk create: " + e.getMessage());
        }
    }

    private MessageDTO createEntity(MessageDTO message, String entityType) {
        switch(entityType.toUpperCase()) {
            case "CAMPAIGN":
                return createCampaign(message);
            case "AD_SQUAD":
                return createAdSquad(message);
            case "CREATIVE":
                return createCreative(message);
            case "AUDIENCE":
                return createAudience(message);
            default:
                return createErrorResponse(message, "Unknown entity type: " + entityType);
        }
    }

    // Advanced Features
    private MessageDTO createARLens(MessageDTO message) {
        try {
            if(!config.getFeatures().isEnableARLenses()) {
                return createErrorResponse(message, "AR Lenses feature is not enabled");
            }

            JsonNode content = objectMapper.readTree(message.getContent());

            ObjectNode lens = objectMapper.createObjectNode();
            lens.put("name", content.get("name").asText());
            lens.put("lens_id", content.get("lens_id").asText());
            lens.put("ad_account_id", config.getAdAccountId());

            String endpoint = String.format("%s/adaccounts/%s/lenses",
                config.getApiBaseUrl(), config.getAdAccountId());

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.POST, null, lens
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error creating AR lens", e);
            return createErrorResponse(message, "Failed to create AR lens: " + e.getMessage());
        }
    }

    // Helper Methods
    private ObjectNode createTargetingSpec(JsonNode targetingData) {
        ObjectNode targeting = objectMapper.createObjectNode();

        // Demographics
        if(targetingData.has("demographics")) {
            ObjectNode demographics = objectMapper.createObjectNode();
            JsonNode demo = targetingData.get("demographics");

            if(demo.has("age_min")) {
                demographics.put("age_min", demo.get("age_min").asInt());
            }
            if(demo.has("age_max")) {
                demographics.put("age_max", demo.get("age_max").asInt());
            }
            if(demo.has("gender")) {
                demographics.put("gender", demo.get("gender").asText());
            }

            targeting.set("demographics", demographics);
        }

        // Location
        if(targetingData.has("locations")) {
            targeting.set("geo_locations", targetingData.get("locations"));
        }

        // Interests
        if(targetingData.has("interests")) {
            targeting.set("interests", targetingData.get("interests"));
        }

        // Custom audiences
        if(targetingData.has("custom_audiences")) {
            targeting.set("custom_audiences", targetingData.get("custom_audiences"));
        }

        return targeting;
    }

    private String getDefaultStartTime() {
        return Instant.now().minus(7, TimeUnit.DAYS.toChronoUnit()).toString();
    }

    private String getDefaultEndTime() {
        return Instant.now().toString();
    }

    private MessageDTO pauseCampaign(MessageDTO message) {
        return updateEntityStatus(message, "campaigns", "PAUSED");
    }

    private MessageDTO resumeCampaign(MessageDTO message) {
        return updateEntityStatus(message, "campaigns", "ACTIVE");
    }

    private MessageDTO pauseAdSquad(MessageDTO message) {
        return updateEntityStatus(message, "adsquads", "PAUSED");
    }

    private MessageDTO resumeAdSquad(MessageDTO message) {
        return updateEntityStatus(message, "adsquads", "ACTIVE");
    }

    private MessageDTO updateEntityStatus(MessageDTO message, String entityType, String status) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            String entityId = content.get(entityType.substring(0, entityType.length() - 1) + "_id").asText();

            ObjectNode update = objectMapper.createObjectNode();
            update.put("status", status);

            String endpoint = String.format("%s/%s/%s", config.getApiBaseUrl(), entityType, entityId);

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.PUT, null, update
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error updating entity status", e);
            return createErrorResponse(message, "Failed to update entity status: " + e.getMessage());
        }
    }

    private MessageDTO deleteCampaign(MessageDTO message) {
        return deleteEntity(message, "campaigns", "campaign_id");
    }

    private MessageDTO deleteAdSquad(MessageDTO message) {
        return deleteEntity(message, "adsquads", "ad_squad_id");
    }

    private MessageDTO deleteCreative(MessageDTO message) {
        return deleteEntity(message, "creatives", "creative_id");
    }

    private MessageDTO deleteAudience(MessageDTO message) {
        return deleteEntity(message, "segments", "audience_id");
    }

    private MessageDTO deleteEntity(MessageDTO message, String entityType, String idField) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            String entityId = content.get(idField).asText();

            String endpoint = String.format("%s/%s/%s", config.getApiBaseUrl(), entityType, entityId);

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.DELETE, null
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error deleting entity", e);
            return createErrorResponse(message, "Failed to delete entity: " + e.getMessage());
        }
    }

    private MessageDTO updateAdSquad(MessageDTO message) {
        return updateEntity(message, "adsquads", "ad_squad_id");
    }

    private MessageDTO updateCreative(MessageDTO message) {
        return updateEntity(message, "creatives", "creative_id");
    }

    private MessageDTO updateAudience(MessageDTO message) {
        return updateEntity(message, "segments", "audience_id");
    }

    private MessageDTO updatePixel(MessageDTO message) {
        return updateEntity(message, "pixels", "pixel_id");
    }

    private MessageDTO updateEntity(MessageDTO message, String entityType, String idField) {
        try {
            JsonNode content = objectMapper.readTree(message.getContent());
            String entityId = content.get(idField).asText();

            ObjectNode updates = objectMapper.createObjectNode();
            content.fieldNames().forEachRemaining(field -> {
                if(!field.equals(idField) && !field.equals("operation")) {
                    updates.set(field, content.get(field));
                }
            });

            String endpoint = String.format("%s/%s/%s", config.getApiBaseUrl(), entityType, entityId);

            JsonNode response = makeAuthenticatedRequest(
                endpoint, HttpMethod.PUT, null, updates
           );

            return createSuccessResponse(message, response);
        } catch(Exception e) {
            log.error("Error updating entity", e);
            return createErrorResponse(message, "Failed to update entity: " + e.getMessage());
        }
    }

    private MessageDTO createFilter(MessageDTO message) {
        // Similar to createARLens but for filters
        return createARLens(message); // Placeholder implementation
    }

    private MessageDTO createBrandedMoment(MessageDTO message) {
        // Similar to createARLens but for branded moments
        return createARLens(message); // Placeholder implementation
    }

    private MessageDTO exportData(MessageDTO message) {
        // Export data to CSV or other formats
        return generateReport(message); // Placeholder implementation
    }

    private MessageDTO createCatalog(MessageDTO message) {
        // Create product catalog
        return createAudience(message); // Placeholder implementation
    }

    private MessageDTO updateCatalog(MessageDTO message) {
        // Update product catalog
        return updateAudience(message); // Placeholder implementation
    }

    private MessageDTO uploadProducts(MessageDTO message) {
        // Upload products to catalog
        return bulkCreate(message); // Placeholder implementation
    }

    private MessageDTO bulkUpdate(MessageDTO message) {
        // Similar to bulkCreate but for updates
        return bulkCreate(message); // Placeholder implementation
    }

    private MessageDTO bulkDelete(MessageDTO message) {
        // Similar to bulkCreate but for deletes
        return bulkCreate(message); // Placeholder implementation
    }

    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method,
                                            Map<String, String> queryParams) {
        return makeAuthenticatedRequest(endpoint, method, queryParams, null);
    }

    private JsonNode makeAuthenticatedRequest(String endpoint, HttpMethod method,
                                            Map<String, String> queryParams, Object body) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            if(body != null) {
                headers.set("Content - Type", "application/json");
            }

            String url = endpoint;
            if(queryParams != null && !queryParams.isEmpty()) {
                StringBuilder queryString = new StringBuilder("?");
                queryParams.forEach((key, value) ->
                    queryString.append(key).append(" = ").append(value).append("&")
               );
                url += queryString.substring(0, queryString.length() - 1);
            }

            HttpEntity<?> entity = body != null ?
                new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, method, entity, String.class
           );

            return objectMapper.readTree(response.getBody());
        } catch(HttpClientErrorException e) {
            log.error("HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch(Exception e) {
            log.error("Error making authenticated request", e);
            return null;
        }
    }

    private String getAccessToken() {
        // In a real implementation, this would handle OAuth2 token refresh
        return config.getAccessToken();
    }

    private MessageDTO createSuccessResponse(MessageDTO originalMessage, JsonNode responseData) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(originalMessage.getCorrelationId());
        response.setHeaders(new HashMap<>(originalMessage.getHeaders()));
        response.getHeaders().put("status", "success");
        response.setPayload(responseData.toString());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    @Override
    protected SnapchatAdsApiConfig getConfig() {
        return config;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("adAccountId", config.getAdAccountId());
            configMap.put("organizationId", config.getOrganizationId());
            configMap.put("pixelId", config.getPixelId());
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
    protected long getPollingIntervalMs() {
        // Outbound adapters don't typically poll, but return 0 to indicate no polling
        return 0;
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        // Initialize any receiver-specific resources
        log.debug("Initializing Snapchat Ads outbound adapter");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // Clean up any receiver-specific resources
        log.debug("Destroying Snapchat Ads outbound adapter");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapters typically don't receive data
        log.debug("Outbound adapter does not receive data");
        return AdapterResult.success(null);
    }


    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by making a simple API call
            String url = config.getApiBaseUrl() + "/me";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getAccessToken());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Snapchat Ads API connection successful");
            } else {
                return AdapterResult.failure("Snapchat Ads API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test Snapchat Ads connection: " + e.getMessage());
        }
    }
}
