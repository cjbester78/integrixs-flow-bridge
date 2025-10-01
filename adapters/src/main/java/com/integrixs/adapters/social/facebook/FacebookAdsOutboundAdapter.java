package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.social.facebook.FacebookAdsApiConfig;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import com.integrixs.shared.enums.MessageStatus;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("facebookAdsOutboundAdapter")
public class FacebookAdsOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(FacebookAdsOutboundAdapter.class);


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public FacebookAdsOutboundAdapter(
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(AdapterConfiguration.AdapterTypeEnum.REST);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AdapterType getType() {
        return AdapterType.REST;
    }

    public void send(MessageDTO message, String flowId, Map<String, Object> configuration) {
        String operation = (String) message.getHeaders().get("operation");
        if(operation == null) {
            throw new RuntimeException("Operation header is required");
        }

        log.debug("Processing Facebook Ads operation: {}", operation);

        try {
            // Rate limiting would be handled by the service layer

            MessageDTO response = new MessageDTO();
            switch(operation.toUpperCase()) {
                // Campaign operations
                case "CREATE_CAMPAIGN":
                    response = createCampaign(message, flowId, configuration);
                    break;
                case "UPDATE_CAMPAIGN":
                    response = updateCampaign(message, flowId, configuration);
                    break;
                case "DELETE_CAMPAIGN":
                    response = deleteCampaign(message, flowId, configuration);
                    break;
                case "PAUSE_CAMPAIGN":
                    response = pauseCampaign(message, flowId, configuration);
                    break;
                case "RESUME_CAMPAIGN":
                    response = resumeCampaign(message, flowId, configuration);
                    break;

                // Ad Set operations
                case "CREATE_AD_SET":
                    response = createAdSet(message, flowId, configuration);
                    break;
                case "UPDATE_AD_SET":
                    response = updateAdSet(message, flowId, configuration);
                    break;
                case "DELETE_AD_SET":
                    response = deleteAdSet(message, flowId, configuration);
                    break;

                // Ad operations
                case "CREATE_AD":
                    response = createAd(message, flowId, configuration);
                    break;
                case "UPDATE_AD":
                    response = updateAd(message, flowId, configuration);
                    break;
                case "DELETE_AD":
                    response = deleteAd(message, flowId, configuration);
                    break;

                // Creative operations
                case "CREATE_CREATIVE":
                    response = createCreative(message, flowId, configuration);
                    break;
                case "UPLOAD_IMAGE":
                    response = uploadImage(message, flowId, configuration);
                    break;
                case "UPLOAD_VIDEO":
                    response = uploadVideo(message, flowId, configuration);
                    break;

                // Audience operations
                case "CREATE_CUSTOM_AUDIENCE":
                    response = createCustomAudience(message, flowId, configuration);
                    break;
                case "UPDATE_CUSTOM_AUDIENCE":
                    response = updateCustomAudience(message, flowId, configuration);
                    break;
                case "CREATE_LOOKALIKE_AUDIENCE":
                    response = createLookalikeAudience(message, flowId, configuration);
                    break;

                // Budget operations
                case "UPDATE_BUDGET":
                    response = updateBudget(message, flowId, configuration);
                    break;
                case "SET_BID":
                    response = setBid(message, flowId, configuration);
                    break;

                // Analytics operations
                case "GET_INSIGHTS":
                    response = getInsights(message, flowId, configuration);
                    break;
                case "GET_PERFORMANCE":
                    response = getPerformance(message, flowId, configuration);
                    break;

                // Lead operations
                case "GET_LEADS":
                    response = getLeads(message, flowId, configuration);
                    break;
                case "DOWNLOAD_LEADS":
                    response = downloadLeads(message, flowId, configuration);
                    break;

                default:
                    throw new AdapterException("Unknown operation: " + operation);
            }

            if(response == null) {
                throw new RuntimeException("Unknown operation: " + operation);
            }

        } catch(Exception e) {
            log.error("Error processing Facebook Ads operation: " + operation, e);
            throw new RuntimeException("Failed to process Facebook Ads operation", e);
        }
    }

    private MessageDTO createCampaign(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String baseUrl = (String) configuration.get("baseUrl");
        String apiVersion = (String) configuration.get("apiVersion");
        String adAccountId = (String) configuration.get("adAccountId");
        String url = String.format("%s/%s/act_%s/campaigns", baseUrl, apiVersion, adAccountId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("name", payload.path("name").asText());
        params.add("objective", payload.path("objective").asText("CONVERSIONS"));
        params.add("status", payload.path("status").asText("PAUSED"));

        if(payload.has("spend_cap")) {
            params.add("spend_cap", payload.path("spend_cap").asText());
        }
        if(payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }
        if(payload.has("lifetime_budget")) {
            params.add("lifetime_budget", payload.path("lifetime_budget").asText());
        }
        if(payload.has("start_time")) {
            params.add("start_time", payload.path("start_time").asText());
        }
        if(payload.has("end_time")) {
            params.add("end_time", payload.path("end_time").asText());
        }
        if(payload.has("special_ad_categories")) {
            ArrayNode categories = (ArrayNode) payload.path("special_ad_categories");
            List<String> catList = new ArrayList<>();
            categories.forEach(cat -> catList.add(cat.asText()));
            params.add("special_ad_categories", catList.toString());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "CAMPAIGN_CREATED");
    }

    private MessageDTO updateCampaign(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();

        String baseUrl = (String) configuration.get("baseUrl");
        String apiVersion = (String) configuration.get("apiVersion");
        String url = String.format("%s/%s/%s", baseUrl, apiVersion, campaignId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));

        if(payload.has("name")) {
            params.add("name", payload.path("name").asText());
        }
        if(payload.has("status")) {
            params.add("status", payload.path("status").asText());
        }
        if(payload.has("spend_cap")) {
            params.add("spend_cap", payload.path("spend_cap").asText());
        }
        if(payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "CAMPAIGN_UPDATED");
    }

    private MessageDTO createAdSet(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String baseUrl = (String) configuration.get("baseUrl");
        String apiVersion = (String) configuration.get("apiVersion");
        String adAccountId = (String) configuration.get("adAccountId");
        String url = String.format("%s/%s/act_%s/adsets", baseUrl, apiVersion, adAccountId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("name", payload.path("name").asText());
        params.add("campaign_id", payload.path("campaign_id").asText());
        params.add("billing_event", payload.path("billing_event").asText("IMPRESSIONS"));
        params.add("optimization_goal", payload.path("optimization_goal").asText("CONVERSIONS"));
        params.add("status", payload.path("status").asText("PAUSED"));

        // Budget
        if(payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }
        if(payload.has("lifetime_budget")) {
            params.add("lifetime_budget", payload.path("lifetime_budget").asText());
        }

        // Schedule
        if(payload.has("start_time")) {
            params.add("start_time", payload.path("start_time").asText());
        }
        if(payload.has("end_time")) {
            params.add("end_time", payload.path("end_time").asText());
        }

        // Targeting
        if(payload.has("targeting")) {
            params.add("targeting", payload.path("targeting").toString());
        }

        // Bid
        if(payload.has("bid_amount")) {
            params.add("bid_amount", payload.path("bid_amount").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AD_SET_CREATED");
    }

    private MessageDTO createAd(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/act_%s/ads",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), (String) configuration.get("adAccountId"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("name", payload.path("name").asText());
        params.add("adset_id", payload.path("adset_id").asText());
        params.add("creative", payload.path("creative").toString());
        params.add("status", payload.path("status").asText("PAUSED"));

        if(payload.has("tracking_specs")) {
            params.add("tracking_specs", payload.path("tracking_specs").toString());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AD_CREATED");
    }

    private MessageDTO createCreative(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/act_%s/adcreatives",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), (String) configuration.get("adAccountId"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("name", payload.path("name").asText());

        // Object story spec
        if(payload.has("object_story_spec")) {
            params.add("object_story_spec", payload.path("object_story_spec").toString());
        }

        // Asset feed spec
        if(payload.has("asset_feed_spec")) {
            params.add("asset_feed_spec", payload.path("asset_feed_spec").toString());
        }

        // Image or video
        if(payload.has("image_hash")) {
            params.add("image_hash", payload.path("image_hash").asText());
        }
        if(payload.has("video_id")) {
            params.add("video_id", payload.path("video_id").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "CREATIVE_CREATED");
    }

    private MessageDTO createCustomAudience(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/act_%s/customaudiences",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), (String) configuration.get("adAccountId"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("name", payload.path("name").asText());
        params.add("subtype", payload.path("subtype").asText("CUSTOM"));
        params.add("description", payload.path("description").asText(""));

        if(payload.has("customer_file_source")) {
            params.add("customer_file_source", payload.path("customer_file_source").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "CUSTOM_AUDIENCE_CREATED");
    }

    private MessageDTO createLookalikeAudience(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = String.format("%s/%s/act_%s/customaudiences",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), (String) configuration.get("adAccountId"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("name", payload.path("name").asText());
        params.add("subtype", "LOOKALIKE");
        params.add("origin_audience_id", payload.path("source_audience_id").asText());
        params.add("lookalike_spec", payload.path("lookalike_spec").toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LOOKALIKE_AUDIENCE_CREATED");
    }

    private MessageDTO getInsights(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String entityType = payload.path("entity_type").asText("campaign");
        String entityId = payload.path("entity_id").asText();

        String url = String.format("%s/%s/%s/insights",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), entityId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken(configuration));
        params.put("fields", payload.path("fields").asText(
            "impressions,clicks,spend,reach,frequency,ctr,cpc,cpm,conversions"));
        params.put("date_preset", payload.path("date_preset").asText("last_7d"));

        if(payload.has("breakdowns")) {
            params.put("breakdowns", payload.path("breakdowns").asText());
        }

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append(" = ").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "INSIGHTS_RETRIEVED");
    }

    private MessageDTO uploadImage(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        byte[] imageData = Base64.getDecoder().decode(payload.path("image_data").asText());
        String imageName = payload.path("image_name").asText("image.jpg");

        String url = String.format("%s/%s/act_%s/adimages",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), (String) configuration.get("adAccountId"));

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));

        Resource imageResource = new ByteArrayResource(imageData) {
            @Override
            public String getFilename() {
                return imageName;
            }
        };
        params.add("bytes", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "IMAGE_UPLOADED");
    }

    private MessageDTO uploadVideo(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // For video uploads, we initiate an upload session
        String url = String.format("%s/%s/act_%s/advideos",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), (String) configuration.get("adAccountId"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("upload_phase", "start");
        params.add("file_size", payload.path("file_size").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "VIDEO_UPLOAD_INITIATED");
    }

    // Helper methods
    private MessageDTO deleteCampaign(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaign_id").asText();

        String url = String.format("%s/%s/%s",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), campaignId);

        Map<String, String> params = Map.of("access_token", getAccessToken(configuration));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url + "?access_token = " + getAccessToken(configuration),
            HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "CAMPAIGN_DELETED");
    }

    private MessageDTO pauseCampaign(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ((ObjectNode) payload).put("status", "PAUSED");

        MessageDTO modifiedMessage = new MessageDTO();
        modifiedMessage.setCorrelationId(message.getCorrelationId());
        modifiedMessage.setHeaders(message.getHeaders());
        modifiedMessage.setPayload(payload.toString());

        return updateCampaign(modifiedMessage, flowId, configuration);
    }

    private MessageDTO resumeCampaign(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ((ObjectNode) payload).put("status", "ACTIVE");

        MessageDTO modifiedMessage = new MessageDTO();
        modifiedMessage.setCorrelationId(message.getCorrelationId());
        modifiedMessage.setHeaders(message.getHeaders());
        modifiedMessage.setPayload(payload.toString());

        return updateCampaign(modifiedMessage, flowId, configuration);
    }

    private MessageDTO updateAdSet(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adSetId = payload.path("adset_id").asText();

        String url = String.format("%s/%s/%s",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), adSetId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));

        // Update allowed fields
        if(payload.has("name")) params.add("name", payload.path("name").asText());
        if(payload.has("status")) params.add("status", payload.path("status").asText());
        if(payload.has("daily_budget")) params.add("daily_budget", payload.path("daily_budget").asText());
        if(payload.has("bid_amount")) params.add("bid_amount", payload.path("bid_amount").asText());
        if(payload.has("targeting")) params.add("targeting", payload.path("targeting").toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AD_SET_UPDATED");
    }

    private MessageDTO deleteAdSet(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adSetId = payload.path("adset_id").asText();

        String url = String.format("%s/%s/%s?access_token = %s",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), adSetId, getAccessToken(configuration));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AD_SET_DELETED");
    }

    private MessageDTO updateAd(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adId = payload.path("ad_id").asText();

        String url = String.format("%s/%s/%s",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), adId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));

        if(payload.has("name")) params.add("name", payload.path("name").asText());
        if(payload.has("status")) params.add("status", payload.path("status").asText());
        if(payload.has("creative")) params.add("creative", payload.path("creative").toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AD_UPDATED");
    }

    private MessageDTO deleteAd(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adId = payload.path("ad_id").asText();

        String url = String.format("%s/%s/%s?access_token = %s",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), adId, getAccessToken(configuration));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AD_DELETED");
    }

    private MessageDTO updateCustomAudience(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audience_id").asText();

        String url = String.format("%s/%s/%s/users",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), audienceId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("payload", payload.path("users").toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "AUDIENCE_UPDATED");
    }

    private MessageDTO updateBudget(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String entityType = payload.path("entity_type").asText();
        String entityId = payload.path("entity_id").asText();

        String url = String.format("%s/%s/%s",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), entityId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));

        if(payload.has("daily_budget")) {
            params.add("daily_budget", payload.path("daily_budget").asText());
        }
        if(payload.has("lifetime_budget")) {
            params.add("lifetime_budget", payload.path("lifetime_budget").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "BUDGET_UPDATED");
    }

    private MessageDTO setBid(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String adSetId = payload.path("adset_id").asText();

        String url = String.format("%s/%s/%s",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), adSetId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", getAccessToken(configuration));
        params.add("bid_amount", payload.path("bid_amount").asText());

        if(payload.has("bid_strategy")) {
            params.add("bid_strategy", payload.path("bid_strategy").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "BID_UPDATED");
    }

    private MessageDTO getPerformance(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        // Similar to getInsights but focused on performance metrics
        return getInsights(message, flowId, configuration);
    }

    private MessageDTO getLeads(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String formId = payload.path("form_id").asText();

        String url = String.format("%s/%s/%s/leads",
            (String) configuration.get("baseUrl"), (String) configuration.get("apiVersion"), formId);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", getAccessToken(configuration));
        params.put("fields", "created_time,id,ad_id,form_id,field_data");

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        params.forEach((key, value) ->
            urlBuilder.append(key).append(" = ").append(value).append("&"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            urlBuilder.toString(), HttpMethod.GET, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LEADS_RETRIEVED");
    }

    private MessageDTO downloadLeads(MessageDTO message, String flowId, Map<String, Object> configuration) throws Exception {
        // This would typically return CSV data
        MessageDTO leadsMessage = getLeads(message, flowId, configuration);

        // Convert JSON to CSV format
        JsonNode leadsData = objectMapper.readTree(leadsMessage.getPayload());
        StringBuilder csv = new StringBuilder();

        // Build CSV from leads data
        if(leadsData.has("data") && leadsData.get("data").isArray()) {
            csv.append("Lead ID,Created Time,Ad ID,Form ID,Field Data\n");
            for(JsonNode lead : leadsData.get("data")) {
                csv.append(lead.path("id").asText()).append(",");
                csv.append(lead.path("created_time").asText()).append(",");
                csv.append(lead.path("ad_id").asText()).append(",");
                csv.append(lead.path("form_id").asText()).append(",");
                csv.append(lead.path("field_data").toString()).append("\n");
            }
        }

        MessageDTO response = new MessageDTO();
        response.setCorrelationId(message.getCorrelationId());
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "content - type", "text/csv",
            "operation", "LEADS_DOWNLOADED"
       ));
        response.setPayload(csv.toString());

        return response;
    }

    private MessageDTO createSuccessResponse(String messageId, String responseBody, String operation) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setTimestamp(LocalDateTime.now());
        response.setHeaders(Map.of(
            "operation", operation,
            "source", "facebook_ads"
       ));
        response.setPayload(responseBody);
        return response;
    }

    private String getAccessToken(Map<String, Object> configuration) {
        String encryptedToken = (String) configuration.get("accessToken");
        // In a real implementation, you would decrypt the token
        // For now, return it as - is
        return encryptedToken;
    }

    private void validateConfiguration(Map<String, Object> configuration) throws AdapterException {
        if(configuration == null) {
            throw new AdapterException("Facebook Ads configuration is not set");
        }
        if(configuration.get("adAccountId") == null || configuration.get("adAccountId").toString().isEmpty()) {
            throw new AdapterException("Ad Account ID is required");
        }
        if(configuration.get("accessToken") == null || configuration.get("accessToken").toString().isEmpty()) {
            throw new AdapterException("Access token is required");
        }
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        log.debug("Initializing Facebook Ads receiver");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.debug("Destroying Facebook Ads receiver");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Facebook Ads doesn't support inbound receiving
        return AdapterResult.failure("Facebook Ads adapter does not support receiving messages");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection by getting ad account info
            Map<String, Object> configuration = new HashMap<>();
            // Configuration would be provided by the framework

            String baseUrl = (String) configuration.getOrDefault("baseUrl", "https://graph.facebook.com");
            String apiVersion = (String) configuration.getOrDefault("apiVersion", "v18.0");
            String adAccountId = (String) configuration.get("adAccountId");

            if (adAccountId == null) {
                return AdapterResult.failure("Ad Account ID not configured");
            }

            String url = String.format("%s/%s/act_%s", baseUrl, apiVersion, adAccountId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getAccessToken(configuration));

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Facebook Ads API connection successful");
            } else {
                return AdapterResult.failure("Facebook Ads API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error testing Facebook Ads connection", e);
            return AdapterResult.failure("Failed to test Facebook Ads connection: " + e.getMessage(), e);
        }
    }

    @Override
    public long getPollingIntervalMs() {
        // Facebook Ads doesn't support polling
        return 0;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        // Return default configuration
        configMap.put("baseUrl", "https://graph.facebook.com");
        configMap.put("apiVersion", "v18.0");
        return configMap;
    }
}
