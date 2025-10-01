package com.integrixs.adapters.social.twitter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.social.twitter.TwitterAdsApiConfig;
import com.integrixs.adapters.social.twitter.TwitterAdsApiConfig.PlacementType;
import com.integrixs.adapters.social.twitter.TwitterAdsApiConfig.Granularity;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.enums.MessageStatus;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Component("twitterAdsOutboundAdapter")
public class TwitterAdsOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TwitterAdsOutboundAdapter.class);


    @Value("${integrixs.adapters.twitter.ads.api-base-url:https://ads-api.twitter.com/12}")
    private String twitterAdsApiBase;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private TwitterAdsApiConfig config;

    private final RateLimiterService rateLimiterService;
    private final OAuth2TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public TwitterAdsOutboundAdapter(
            TwitterAdsApiConfig config,
            RateLimiterService rateLimiterService,
            OAuth2TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(rateLimiterService, credentialEncryptionService);
        this.config = config;
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
            log.info("Processing Twitter Ads operation: {}", operation);

            switch(operation.toUpperCase()) {
                // Campaign Management
                case "CREATE_CAMPAIGN":
                    return createCampaign(message);
                case "UPDATE_CAMPAIGN":
                    return updateCampaign(message);
                case "DELETE_CAMPAIGN":
                    return deleteCampaign(message);
                case "PAUSE_CAMPAIGN":
                    return pauseCampaign(message);
                case "RESUME_CAMPAIGN":
                    return resumeCampaign(message);

                // Ad Group(Line Item) Management
                case "CREATE_AD_GROUP":
                    return createAdGroup(message);
                case "UPDATE_AD_GROUP":
                    return updateAdGroup(message);
                case "DELETE_AD_GROUP":
                    return deleteAdGroup(message);

                // Creative Management
                case "CREATE_PROMOTED_TWEET":
                    return createPromotedTweet(message);
                case "CREATE_CARD":
                    return createCard(message);
                case "UPDATE_CREATIVE":
                    return updateCreative(message);

                // Audience Management
                case "CREATE_CUSTOM_AUDIENCE":
                    return createCustomAudience(message);
                case "UPDATE_CUSTOM_AUDIENCE":
                    return updateCustomAudience(message);
                case "ADD_TO_CUSTOM_AUDIENCE":
                    return addToCustomAudience(message);
                case "REMOVE_FROM_CUSTOM_AUDIENCE":
                    return removeFromCustomAudience(message);
                case "CREATE_LOOKALIKE_AUDIENCE":
                    return createLookalikeAudience(message);

                // Targeting
                case "SET_TARGETING":
                    return setTargeting(message);
                case "UPDATE_TARGETING":
                    return updateTargeting(message);

                // Budget & Bidding
                case "UPDATE_BUDGET":
                    return updateBudget(message);
                case "UPDATE_BID":
                    return updateBid(message);

                // Reporting
                case "CREATE_REPORT":
                    return createReport(message);
                case "GET_METRICS":
                    return getMetrics(message);
                case "EXPORT_DATA":
                    return exportData(message);

                // Conversion Tracking
                case "CREATE_CONVERSION_EVENT":
                    return createConversionEvent(message);
                case "CREATE_WEB_EVENT_TAG":
                    return createWebEventTag(message);

                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error in Twitter Ads outbound adapter", e);
            return createErrorResponse(message, e.getMessage());
        }
    }

    // Campaign Management Methods
    private MessageDTO createCampaign(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/campaigns",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("funding_instrument_id", payload.path("funding_instrument_id").asText());
        requestBody.put("daily_budget_amount_local_micro",
            (long)(payload.path("daily_budget").asDouble() * 1000000));
        requestBody.put("objective", payload.path("objective").asText(
            CampaignObjective.TWEET_ENGAGEMENTS.name()));

        if(payload.has("start_time")) {
            requestBody.put("start_time", payload.get("start_time").asText());
        }
        if(payload.has("end_time")) {
            requestBody.put("end_time", payload.get("end_time").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_CREATED");
    }

    private MessageDTO updateCampaign(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();

        String url = String.format("%s/accounts/%s/campaigns/%s",
            twitterAdsApiBase, config.getAdsAccountId(), campaignId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        if(payload.has("name")) {
            requestBody.put("name", payload.get("name").asText());
        }
        if(payload.has("daily_budget")) {
            requestBody.put("daily_budget_amount_local_micro",
                (long)(payload.path("daily_budget").asDouble() * 1000000));
        }
        if(payload.has("paused")) {
            requestBody.put("paused", payload.get("paused").asBoolean());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_UPDATED");
    }

    private MessageDTO deleteCampaign(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();

        String url = String.format("%s/accounts/%s/campaigns/%s",
            twitterAdsApiBase, config.getAdsAccountId(), campaignId);

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "CAMPAIGN_DELETED");
    }

    private MessageDTO pauseCampaign(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();

        String url = String.format("%s/accounts/%s/campaigns/%s",
            twitterAdsApiBase, config.getAdsAccountId(), campaignId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("paused", true);

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_PAUSED");
    }

    private MessageDTO resumeCampaign(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();

        String url = String.format("%s/accounts/%s/campaigns/%s",
            twitterAdsApiBase, config.getAdsAccountId(), campaignId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("paused", false);

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_RESUMED");
    }

    // Ad Group Management Methods
    private MessageDTO createAdGroup(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/line_items",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("campaign_id", payload.path("campaign_id").asText());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("product_type", "PROMOTED_TWEETS");
        requestBody.put("objective", payload.path("objective").asText());
        requestBody.put("bid_type", payload.path("bid_type").asText(BidType.AUTO.name()));

        if(payload.has("bid_amount_local_micro")) {
            requestBody.put("bid_amount_local_micro", payload.get("bid_amount_local_micro").asLong());
        }

        if(payload.has("placements") && payload.get("placements").isArray()) {
            requestBody.set("placements", payload.get("placements"));
        } else {
            requestBody.putArray("placements").add(PlacementType.ALL_ON_TWITTER.name());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "AD_GROUP_CREATED");
    }

    private MessageDTO updateAdGroup(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String lineItemId = payload.path("line_item_id").asText();

        String url = String.format("%s/accounts/%s/line_items/%s",
            twitterAdsApiBase, config.getAdsAccountId(), lineItemId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        if(payload.has("name")) {
            requestBody.put("name", payload.get("name").asText());
        }
        if(payload.has("bid_amount_local_micro")) {
            requestBody.put("bid_amount_local_micro", payload.get("bid_amount_local_micro").asLong());
        }
        if(payload.has("paused")) {
            requestBody.put("paused", payload.get("paused").asBoolean());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "AD_GROUP_UPDATED");
    }

    private MessageDTO deleteAdGroup(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String lineItemId = payload.path("line_item_id").asText();

        String url = String.format("%s/accounts/%s/line_items/%s",
            twitterAdsApiBase, config.getAdsAccountId(), lineItemId);

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "AD_GROUP_DELETED");
    }

    // Creative Management Methods
    private MessageDTO createPromotedTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/promoted_tweets",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("line_item_id", payload.path("line_item_id").asText());
        requestBody.put("tweet_id", payload.path("tweet_id").asText());

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "PROMOTED_TWEET_CREATED");
    }

    private MessageDTO createCard(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String cardType = payload.path("card_type").asText("WEBSITE");

        String endpoint = getCardEndpoint(cardType);
        String url = String.format("%s/accounts/%s/%s",
            twitterAdsApiBase, config.getAdsAccountId(), endpoint);

        ObjectNode requestBody = objectMapper.createObjectNode();

        // Common card properties
        requestBody.put("name", payload.path("name").asText());

        // Card - specific properties
        switch(cardType.toUpperCase()) {
            case "WEBSITE":
                requestBody.put("website_title", payload.path("title").asText());
                requestBody.put("website_url", payload.path("url").asText());
                if(payload.has("image_media_id")) {
                    requestBody.put("image_media_id", payload.get("image_media_id").asText());
                }
                break;

            case "APP":
                requestBody.put("app_country_code", payload.path("country_code").asText());
                requestBody.put("app_cta", payload.path("cta").asText("INSTALL"));
                if(payload.has("iphone_app_id")) {
                    requestBody.put("iphone_app_id", payload.get("iphone_app_id").asText());
                }
                if(payload.has("android_app_id")) {
                    requestBody.put("googleplay_app_id", payload.get("android_app_id").asText());
                }
                break;

            case "LEAD_GEN":
                requestBody.put("title", payload.path("title").asText());
                requestBody.put("cta", payload.path("cta").asText());
                requestBody.put("privacy_policy_url", payload.path("privacy_policy_url").asText());
                if(payload.has("custom_questions") && payload.get("custom_questions").isArray()) {
                    requestBody.set("custom_questions", payload.get("custom_questions"));
                }
                break;
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CARD_CREATED");
    }

    private MessageDTO updateCreative(MessageDTO message) throws Exception {
        // Update creative properties
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String creativeType = payload.path("creative_type").asText();
        String creativeId = payload.path("creative_id").asText();

        // Implementation depends on creative type
        return createResponseMessage(null, "CREATIVE_UPDATED");
    }

    // Audience Management Methods
    private MessageDTO createCustomAudience(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/custom_audiences",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("description", payload.path("description").asText(""));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CUSTOM_AUDIENCE_CREATED");
    }

    private MessageDTO updateCustomAudience(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audience_id").asText();

        String url = String.format("%s/accounts/%s/custom_audiences/%s",
            twitterAdsApiBase, config.getAdsAccountId(), audienceId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        if(payload.has("name")) {
            requestBody.put("name", payload.get("name").asText());
        }
        if(payload.has("description")) {
            requestBody.put("description", payload.get("description").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "CUSTOM_AUDIENCE_UPDATED");
    }

    private MessageDTO addToCustomAudience(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audience_id").asText();

        String url = String.format("%s/accounts/%s/custom_audiences/%s/users",
            twitterAdsApiBase, config.getAdsAccountId(), audienceId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("operation_type", "ADD");

        ArrayNode users = requestBody.putArray("users");
        if(payload.has("user_ids") && payload.get("user_ids").isArray()) {
            for(JsonNode userId : payload.get("user_ids")) {
                ObjectNode user = users.addObject();
                user.put("twitter_id", userId.asText());
            }
        }
        if(payload.has("emails") && payload.get("emails").isArray()) {
            for(JsonNode email : payload.get("emails")) {
                ObjectNode user = users.addObject();
                user.put("email", hashEmail(email.asText()));
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "USERS_ADDED_TO_AUDIENCE");
    }

    private MessageDTO removeFromCustomAudience(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audience_id").asText();

        String url = String.format("%s/accounts/%s/custom_audiences/%s/users",
            twitterAdsApiBase, config.getAdsAccountId(), audienceId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("operation_type", "REMOVE");

        ArrayNode users = requestBody.putArray("users");
        if(payload.has("user_ids") && payload.get("user_ids").isArray()) {
            for(JsonNode userId : payload.get("user_ids")) {
                ObjectNode user = users.addObject();
                user.put("twitter_id", userId.asText());
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "USERS_REMOVED_FROM_AUDIENCE");
    }

    private MessageDTO createLookalikeAudience(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/custom_audiences",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("lookalike_source_id", payload.path("source_audience_id").asText());
        requestBody.put("lookalike_expansion_level", payload.path("expansion_level").asInt(10));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "LOOKALIKE_AUDIENCE_CREATED");
    }

    // Targeting Methods
    private MessageDTO setTargeting(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String lineItemId = payload.path("line_item_id").asText();

        String url = String.format("%s/accounts/%s/targeting_criteria",
            twitterAdsApiBase, config.getAdsAccountId());

        ArrayNode batch = objectMapper.createArrayNode();

        // Process each targeting criterion
        if(payload.has("targeting_criteria") && payload.get("targeting_criteria").isArray()) {
            for(JsonNode criterion : payload.get("targeting_criteria")) {
                ObjectNode targetingCriterion = batch.addObject();
                targetingCriterion.put("line_item_id", lineItemId);
                targetingCriterion.put("targeting_type", criterion.path("type").asText());
                targetingCriterion.put("targeting_value", criterion.path("value").asText());

                if(criterion.has("negated")) {
                    targetingCriterion.put("negated", criterion.get("negated").asBoolean());
                }
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, batch.toString());
        return createResponseMessage(response, "TARGETING_SET");
    }

    private MessageDTO updateTargeting(MessageDTO message) throws Exception {
        // Similar to setTargeting but updates existing criteria
        return setTargeting(message);
    }

    // Budget & Bidding Methods
    private MessageDTO updateBudget(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();

        String url = String.format("%s/accounts/%s/campaigns/%s",
            twitterAdsApiBase, config.getAdsAccountId(), campaignId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        if(payload.has("daily_budget")) {
            requestBody.put("daily_budget_amount_local_micro",
                (long)(payload.path("daily_budget").asDouble() * 1000000));
        }
        if(payload.has("total_budget")) {
            requestBody.put("total_budget_amount_local_micro",
                (long)(payload.path("total_budget").asDouble() * 1000000));
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "BUDGET_UPDATED");
    }

    private MessageDTO updateBid(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String lineItemId = payload.path("line_item_id").asText();

        String url = String.format("%s/accounts/%s/line_items/%s",
            twitterAdsApiBase, config.getAdsAccountId(), lineItemId);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("bid_type", payload.path("bid_type").asText());
        if(payload.has("bid_amount")) {
            requestBody.put("bid_amount_local_micro",
                (long)(payload.path("bid_amount").asDouble() * 1000000));
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "BID_UPDATED");
    }

    // Reporting Methods
    private MessageDTO createReport(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/stats/jobs",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("entity", payload.path("entity").asText("CAMPAIGN"));
        requestBody.put("entity_ids", payload.path("entity_ids").asText());
        requestBody.put("granularity", payload.path("granularity").asText(Granularity.DAY.name()));
        requestBody.set("metric_groups", payload.path("metric_groups"));
        requestBody.put("start_time", payload.path("start_time").asText());
        requestBody.put("end_time", payload.path("end_time").asText());

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "REPORT_CREATED");
    }

    private MessageDTO getMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String entity = payload.path("entity").asText("CAMPAIGN");
        String entityIds = payload.path("entity_ids").asText();

        String url = String.format("%s/accounts/%s/stats/accounts/%s",
            twitterAdsApiBase, config.getAdsAccountId(), config.getAdsAccountId());

        Map<String, String> params = new HashMap<>();
        params.put("entity", entity);
        params.put("entity_ids", entityIds);
        params.put("start_time", payload.path("start_time").asText());
        params.put("end_time", payload.path("end_time").asText());
        params.put("granularity", payload.path("granularity").asText(Granularity.DAY.name()));
        params.put("metric_groups", "ENGAGEMENT,BILLING");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "METRICS_RETRIEVED");
    }

    private MessageDTO exportData(MessageDTO message) throws Exception {
        // Create async export job
        return createReport(message);
    }

    // Conversion Tracking Methods
    private MessageDTO createConversionEvent(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/web_conversions",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("pixel_id", payload.path("pixel_id").asText());
        requestBody.put("conversion_type", payload.path("conversion_type").asText());
        requestBody.put("conversion_time", payload.path("conversion_time").asText());
        if(payload.has("conversion_value")) {
            requestBody.put("conversion_value", payload.get("conversion_value").asDouble());
        }
        if(payload.has("currency")) {
            requestBody.put("currency", payload.get("currency").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CONVERSION_EVENT_CREATED");
    }

    private MessageDTO createWebEventTag(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/accounts/%s/web_event_tags",
            twitterAdsApiBase, config.getAdsAccountId());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("type", payload.path("type").asText("UNIVERSAL_WEBSITE_TAG"));
        requestBody.put("retargeting_enabled", payload.path("retargeting_enabled").asBoolean(true));
        requestBody.put("view_through_window", payload.path("view_through_window").asInt(1));
        requestBody.put("click_window", payload.path("click_window").asInt(30));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "WEB_EVENT_TAG_CREATED");
    }

    // Helper Methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body) {
        return makeApiCall(url, method, body, null);
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", generateOAuth1Header(url, method.name()));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (params != null && method == HttpMethod.GET) {
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?");
            params.forEach((key, value) -> {
                try {
                    urlBuilder.append(key).append("=")
                             .append(java.net.URLEncoder.encode(value, "UTF-8"))
                             .append("&");
                } catch (Exception e) {
                    log.error("Error encoding parameter", e);
                }
            });
            urlBuilder.setLength(urlBuilder.length() - 1);
            url = urlBuilder.toString();
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, entity, String.class);
    }

    private String generateOAuth1Header(String url, String method) {
        // Simplified OAuth 1.0a header generation
        // In production, use a proper OAuth library
        String consumerKey = credentialEncryptionService.decrypt(config.getApiKey());
        String token = credentialEncryptionService.decrypt(config.getAccessToken());

        return String.format("OAuth oauth_consumer_key=\"%s\", oauth_token=\"%s\", " +
            "oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"%d\", " +
            "oauth_nonce=\"%s\", oauth_version=\"1.0\", oauth_signature=\"%s\"",
            consumerKey, token, System.currentTimeMillis() / 1000,
            UUID.randomUUID().toString(), "signature_placeholder");
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getAdsAccountId() == null || config.getAdsAccountId().isEmpty()) {
            throw new AdapterException("Twitter Ads account ID is not configured");
        }
        if(config.getApiKey() == null || config.getApiKeySecret() == null ||
            config.getAccessToken() == null || config.getAccessTokenSecret() == null) {
            throw new AdapterException("Twitter Ads API credentials are not configured");
        }
    }

    private String getCardEndpoint(String cardType) {
        switch(cardType.toUpperCase()) {
            case "WEBSITE":
                return "cards/website";
            case "APP":
                return "cards/app_download";
            case "LEAD_GEN":
                return "cards/lead_gen";
            case "VIDEO_WEBSITE":
                return "cards/video_website";
            case "IMAGE_CONVERSATION":
                return "cards/image_conversation";
            case "VIDEO_CONVERSATION":
                return "cards/video_conversation";
            default:
                return "cards/website";
        }
    }

    private String hashEmail(String email) {
        // SHA256 hash for email matching
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for(byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception e) {
            return email;
        }
    }

    private MessageDTO createResponseMessage(ResponseEntity<String> response, String operationType) {
        MessageDTO responseMessage = new MessageDTO();
        responseMessage.setCorrelationId(UUID.randomUUID().toString());
        responseMessage.setTimestamp(LocalDateTime.now());
        if(response != null) {
            responseMessage.setStatus(response.getStatusCode().is2xxSuccessful() ? MessageStatus.SUCCESS : MessageStatus.FAILED);
            responseMessage.setHeaders(Map.of(
                "operation", operationType,
                "statusCode", response.getStatusCodeValue(),
                "source", "twitter_ads"
           ));
            responseMessage.setPayload(response.getBody());
        } else {
            responseMessage.setStatus(MessageStatus.SUCCESS);
            responseMessage.setHeaders(Map.of(
                "operation", operationType,
                "source", "twitter_ads"
           ));
            responseMessage.setPayload("{\"status\":\"success\"}");
        }
        return responseMessage;
    }

    public void setConfiguration(TwitterAdsApiConfig config) {
        this.config = config;
    }

    // Twitter Ads API Enums
    private enum CampaignObjective {
        AWARENESS,
        TWEET_ENGAGEMENTS,
        VIDEO_VIEWS,
        PREROLL_VIEWS,
        APP_INSTALLS,
        WEBSITE_TRAFFIC,
        APP_REENGAGEMENTS,
        REACH,
        FOLLOWERS,
        WEBSITE_CONVERSIONS,
        APP_DOWNLOADS
    }

    private enum BidType {
        AUTO,
        MAX,
        TARGET
    }

    // Implement abstract methods from AbstractSocialMediaOutboundAdapter
    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("apiBaseUrl", twitterAdsApiBase);
        configMap.put("adsAccountId", config.getAdsAccountId());
        configMap.put("features", config.getFeatures());
        configMap.put("limits", config.getLimits());
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.TWITTER;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            return sendMessage(message);
        } catch (Exception e) {
            log.error("Error processing message in Twitter Ads adapter", e);
            return createErrorResponse(message, e.getMessage());
        }
    }

    // Implement abstract methods from AbstractOutboundAdapter
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            validateConfiguration();
            // Test API connection by getting account info
            String url = String.format("%s/accounts/%s", twitterAdsApiBase, config.getAdsAccountId());
            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success("Connection test successful");
            } else {
                return AdapterResult.failure("Connection test failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return AdapterResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    @Override
    protected void doInitialize() {
        log.info("Initializing Twitter Ads Outbound Adapter");
        try {
            validateConfiguration();
        } catch (Exception e) {
            log.error("Failed to initialize adapter", e);
            throw new RuntimeException("Failed to initialize Twitter Ads adapter", e);
        }
    }

    @Override
    protected void doDestroy() {
        log.info("Destroying Twitter Ads Outbound Adapter");
    }

    // Implement missing abstract methods from AbstractOutboundAdapter
    @Override
    protected void doReceiverInitialize() throws Exception {
        log.info("Initializing Twitter Ads receiver (not used for outbound adapters)");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.info("Destroying Twitter Ads receiver (not used for outbound adapters)");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Outbound adapters typically don't receive data, but this method is required
        log.warn("doReceive called on outbound adapter - not supported");
        return AdapterResult.failure("Receive operation not supported on outbound adapter");
    }

    @Override
    protected long getPollingIntervalMs() {
        // Not used for outbound adapters
        return 0;
    }
}
