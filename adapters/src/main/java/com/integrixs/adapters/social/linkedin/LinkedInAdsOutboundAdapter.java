package com.integrixs.adapters.social.linkedin;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.linkedin.LinkedInAdsApiConfig.*;
import com.integrixs.core.api.channel.Message;
import com.integrixs.core.exception.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component("linkedInAdsOutboundAdapter")
public class LinkedInAdsOutboundAdapter extends AbstractSocialMediaOutboundAdapter<LinkedInAdsApiConfig> {
    
    private static final String LINKEDIN_ADS_API_BASE = "https://api.linkedin.com/v2";
    private static final String LINKEDIN_ADS_REST_BASE = "https://api.linkedin.com/rest";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public LinkedInAdsOutboundAdapter(
            LinkedInAdsApiConfig config,
            RateLimiterService rateLimiterService,
            OAuth2TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super(config, rateLimiterService, tokenRefreshService, credentialEncryptionService);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Message sendMessage(Message message) throws AdapterException {
        try {
            validateConfiguration();
            
            String operation = message.getHeaders().getOrDefault("operation", "").toString();
            log.info("Processing LinkedIn Ads operation: {}", operation);
            
            switch (operation.toUpperCase()) {
                // Campaign Management
                case "CREATE_CAMPAIGN":
                    return createCampaign(message);
                case "UPDATE_CAMPAIGN":
                    return updateCampaign(message);
                case "DELETE_CAMPAIGN":
                    return deleteCampaign(message);
                case "PAUSE_CAMPAIGN":
                    return pauseCampaign(message);
                case "ACTIVATE_CAMPAIGN":
                    return activateCampaign(message);
                
                // Campaign Group Management
                case "CREATE_CAMPAIGN_GROUP":
                    return createCampaignGroup(message);
                case "UPDATE_CAMPAIGN_GROUP":
                    return updateCampaignGroup(message);
                case "DELETE_CAMPAIGN_GROUP":
                    return deleteCampaignGroup(message);
                
                // Creative Management
                case "CREATE_CREATIVE":
                    return createCreative(message);
                case "UPDATE_CREATIVE":
                    return updateCreative(message);
                case "DELETE_CREATIVE":
                    return deleteCreative(message);
                case "BOOST_POST":
                    return boostPost(message);
                
                // Audience Management
                case "CREATE_MATCHED_AUDIENCE":
                    return createMatchedAudience(message);
                case "UPDATE_MATCHED_AUDIENCE":
                    return updateMatchedAudience(message);
                case "ADD_TO_AUDIENCE":
                    return addToAudience(message);
                case "REMOVE_FROM_AUDIENCE":
                    return removeFromAudience(message);
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
                case "GET_ANALYTICS":
                    return getAnalytics(message);
                case "EXPORT_LEADS":
                    return exportLeads(message);
                
                // Conversion Tracking
                case "CREATE_CONVERSION":
                    return createConversion(message);
                case "TRACK_CONVERSION":
                    return trackConversion(message);
                
                // Lead Gen Forms
                case "CREATE_LEAD_GEN_FORM":
                    return createLeadGenForm(message);
                case "UPDATE_LEAD_GEN_FORM":
                    return updateLeadGenForm(message);
                case "GET_FORM_RESPONSES":
                    return getFormResponses(message);
                
                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch (Exception e) {
            log.error("Error in LinkedIn Ads outbound adapter", e);
            throw new AdapterException("Failed to process outbound message", e);
        }
    }
    
    // Campaign Management Methods
    private Message createCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
        requestBody.put("campaignGroup", payload.path("campaignGroupId").asText());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("type", payload.path("type").asText(CampaignType.SPONSORED_CONTENT.name()));
        requestBody.put("status", CampaignStatus.DRAFT.name());
        
        // Objective
        requestBody.put("objective", payload.path("objective").asText(
            CampaignObjective.WEBSITE_VISITS.name()));
        
        // Budget
        if (payload.has("dailyBudget")) {
            ObjectNode dailyBudget = requestBody.putObject("dailyBudget");
            dailyBudget.put("amount", payload.path("dailyBudget").asText());
            dailyBudget.put("currencyCode", payload.path("currency").asText("USD"));
        }
        
        if (payload.has("totalBudget")) {
            ObjectNode totalBudget = requestBody.putObject("totalBudget");
            totalBudget.put("amount", payload.path("totalBudget").asText());
            totalBudget.put("currencyCode", payload.path("currency").asText("USD"));
        }
        
        // Schedule
        if (payload.has("startDate")) {
            requestBody.put("runSchedule.start", parseDate(payload.get("startDate").asText()));
        }
        if (payload.has("endDate")) {
            requestBody.put("runSchedule.end", parseDate(payload.get("endDate").asText()));
        }
        
        // Bid
        requestBody.put("bidType", payload.path("bidType").asText(BidType.CPC.name()));
        if (payload.has("bidAmount")) {
            ObjectNode bidAmount = requestBody.putObject("bidAmount");
            bidAmount.put("amount", payload.path("bidAmount").asText());
            bidAmount.put("currencyCode", "USD");
        }
        
        // Optimization
        requestBody.put("optimizationTarget", payload.path("optimizationTarget").asText(
            OptimizationTarget.CLICKS.name()));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_CREATED");
    }
    
    private Message updateCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaignId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns/" + campaignId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        if (payload.has("name")) {
            requestBody.put("patch.$set.name", payload.get("name").asText());
        }
        if (payload.has("status")) {
            requestBody.put("patch.$set.status", payload.get("status").asText());
        }
        if (payload.has("dailyBudget")) {
            requestBody.put("patch.$set.dailyBudget.amount", payload.get("dailyBudget").asText());
        }
        if (payload.has("totalBudget")) {
            requestBody.put("patch.$set.totalBudget.amount", payload.get("totalBudget").asText());
        }
        if (payload.has("bidAmount")) {
            requestBody.put("patch.$set.bidAmount.amount", payload.get("bidAmount").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_UPDATED");
    }
    
    private Message deleteCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaignId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns/" + campaignId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("patch.$set.status", CampaignStatus.CANCELED.name());
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_DELETED");
    }
    
    private Message pauseCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaignId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns/" + campaignId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("patch.$set.status", CampaignStatus.PAUSED.name());
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_PAUSED");
    }
    
    private Message activateCampaign(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaignId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns/" + campaignId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("patch.$set.status", CampaignStatus.ACTIVE.name());
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_ACTIVATED");
    }
    
    // Campaign Group Management
    private Message createCampaignGroup(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaignGroups";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("status", "DRAFT");
        
        if (payload.has("totalBudget")) {
            ObjectNode totalBudget = requestBody.putObject("totalBudget");
            totalBudget.put("amount", payload.path("totalBudget").asText());
            totalBudget.put("currencyCode", "USD");
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_GROUP_CREATED");
    }
    
    private Message updateCampaignGroup(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String groupId = payload.path("campaignGroupId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaignGroups/" + groupId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        if (payload.has("name")) {
            requestBody.put("patch.$set.name", payload.get("name").asText());
        }
        if (payload.has("status")) {
            requestBody.put("patch.$set.status", payload.get("status").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_GROUP_UPDATED");
    }
    
    private Message deleteCampaignGroup(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String groupId = payload.path("campaignGroupId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaignGroups/" + groupId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("patch.$set.status", "CANCELED");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CAMPAIGN_GROUP_DELETED");
    }
    
    // Creative Management Methods
    private Message createCreative(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCreatives";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("campaign", payload.path("campaignId").asText());
        requestBody.put("type", payload.path("type").asText(AdFormat.SINGLE_IMAGE.name()));
        
        // Add reference to existing share/ugcPost
        if (payload.has("shareUrn")) {
            ObjectNode reference = requestBody.putObject("reference");
            reference.put("share", payload.get("shareUrn").asText());
        } else if (payload.has("ugcPostUrn")) {
            ObjectNode reference = requestBody.putObject("reference");
            reference.put("ugcPost", payload.get("ugcPostUrn").asText());
        }
        
        // Add inline content for new creative
        if (payload.has("content")) {
            JsonNode content = payload.get("content");
            
            if (content.has("title") || content.has("description")) {
                ObjectNode data = requestBody.putObject("data");
                ObjectNode shareContent = data.putObject("com.linkedin.ads.SponsoredInlineContentShareContent");
                
                if (content.has("title")) {
                    shareContent.put("title", content.get("title").asText());
                }
                if (content.has("description")) {
                    shareContent.put("description", content.get("description").asText());
                }
                if (content.has("landingPageUrl")) {
                    shareContent.put("landingPageUrl", content.get("landingPageUrl").asText());
                }
                if (content.has("callToAction")) {
                    shareContent.put("callToAction.labelType", content.get("callToAction").asText());
                }
            }
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CREATIVE_CREATED");
    }
    
    private Message updateCreative(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String creativeId = payload.path("creativeId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCreatives/" + creativeId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        if (payload.has("status")) {
            requestBody.put("patch.$set.status", payload.get("status").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CREATIVE_UPDATED");
    }
    
    private Message deleteCreative(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String creativeId = payload.path("creativeId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCreatives/" + creativeId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("patch.$set.status", CreativeStatus.CANCELED.name());
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CREATIVE_DELETED");
    }
    
    private Message boostPost(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        // First create a campaign for the boosted post
        ObjectNode campaignPayload = objectMapper.createObjectNode();
        campaignPayload.put("name", "Boost: " + payload.path("postTitle").asText());
        campaignPayload.put("objective", CampaignObjective.ENGAGEMENT.name());
        campaignPayload.put("type", CampaignType.SPONSORED_CONTENT.name());
        campaignPayload.put("dailyBudget", payload.path("dailyBudget").asText());
        campaignPayload.put("bidType", BidType.CPM.name());
        
        Message campaignMessage = new Message();
        campaignMessage.setPayload(campaignPayload.toString());
        Message campaignResponse = createCampaign(campaignMessage);
        
        // Extract campaign ID from response
        JsonNode campaignData = objectMapper.readTree(campaignResponse.getPayload());
        String campaignId = campaignData.path("id").asText();
        
        // Create creative with the post
        ObjectNode creativePayload = objectMapper.createObjectNode();
        creativePayload.put("campaignId", campaignId);
        creativePayload.put("shareUrn", payload.path("shareUrn").asText());
        
        Message creativeMessage = new Message();
        creativeMessage.setPayload(creativePayload.toString());
        
        return createCreative(creativeMessage);
    }
    
    // Audience Management Methods
    private Message createMatchedAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/dmpSegments";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("description", payload.path("description").asText(""));
        
        if (payload.has("type")) {
            requestBody.put("segmentType", payload.get("type").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "MATCHED_AUDIENCE_CREATED");
    }
    
    private Message updateMatchedAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audienceId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/dmpSegments/" + audienceId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        if (payload.has("name")) {
            requestBody.put("patch.$set.name", payload.get("name").asText());
        }
        if (payload.has("description")) {
            requestBody.put("patch.$set.description", payload.get("description").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "MATCHED_AUDIENCE_UPDATED");
    }
    
    private Message addToAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audienceId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/dmpSegments/" + audienceId + "/users";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode users = requestBody.putArray("elements");
        
        if (payload.has("emails") && payload.get("emails").isArray()) {
            for (JsonNode email : payload.get("emails")) {
                ObjectNode user = users.addObject();
                user.put("hash", hashValue(email.asText()));
                user.put("type", "SHA256_EMAIL");
            }
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "USERS_ADDED_TO_AUDIENCE");
    }
    
    private Message removeFromAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String audienceId = payload.path("audienceId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/dmpSegments/" + audienceId + "/users";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("action", "REMOVE");
        ArrayNode users = requestBody.putArray("elements");
        
        if (payload.has("emails") && payload.get("emails").isArray()) {
            for (JsonNode email : payload.get("emails")) {
                ObjectNode user = users.addObject();
                user.put("hash", hashValue(email.asText()));
                user.put("type", "SHA256_EMAIL");
            }
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, requestBody.toString());
        return createResponseMessage(response, "USERS_REMOVED_FROM_AUDIENCE");
    }
    
    private Message createLookalikeAudience(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/adTargetingSegments";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("seedSegment", payload.path("seedAudienceId").asText());
        requestBody.put("targetCountries", payload.path("targetCountries"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "LOOKALIKE_AUDIENCE_CREATED");
    }
    
    // Targeting Methods
    private Message setTargeting(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaignId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adTargetingFacets";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("campaign", campaignId);
        
        // Process targeting criteria
        if (payload.has("targetingCriteria") && payload.get("targetingCriteria").isObject()) {
            JsonNode criteria = payload.get("targetingCriteria");
            
            // Location targeting
            if (criteria.has("locations")) {
                ArrayNode locations = requestBody.putArray("facets.urn:li:adTargetingFacet:locations");
                for (JsonNode location : criteria.get("locations")) {
                    locations.add("urn:li:geo:" + location.asText());
                }
            }
            
            // Company targeting
            if (criteria.has("companies")) {
                ArrayNode companies = requestBody.putArray("facets.urn:li:adTargetingFacet:companies");
                for (JsonNode company : criteria.get("companies")) {
                    companies.add("urn:li:company:" + company.asText());
                }
            }
            
            // Job title targeting
            if (criteria.has("jobTitles")) {
                ArrayNode jobTitles = requestBody.putArray("facets.urn:li:adTargetingFacet:jobTitles");
                for (JsonNode title : criteria.get("jobTitles")) {
                    jobTitles.add("urn:li:title:" + title.asText());
                }
            }
            
            // Skills targeting
            if (criteria.has("skills")) {
                ArrayNode skills = requestBody.putArray("facets.urn:li:adTargetingFacet:skills");
                for (JsonNode skill : criteria.get("skills")) {
                    skills.add("urn:li:skill:" + skill.asText());
                }
            }
            
            // Matched audiences
            if (criteria.has("matchedAudiences")) {
                ArrayNode audiences = requestBody.putArray("facets.urn:li:adTargetingFacet:matchedAudiences");
                for (JsonNode audience : criteria.get("matchedAudiences")) {
                    audiences.add(audience.asText());
                }
            }
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "TARGETING_SET");
    }
    
    private Message updateTargeting(Message message) throws Exception {
        // Similar to setTargeting but updates existing
        return setTargeting(message);
    }
    
    // Budget & Bidding Methods
    private Message updateBudget(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaignId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns/" + campaignId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        if (payload.has("dailyBudget")) {
            requestBody.put("patch.$set.dailyBudget.amount", payload.get("dailyBudget").asText());
            requestBody.put("patch.$set.dailyBudget.currencyCode", "USD");
        }
        
        if (payload.has("totalBudget")) {
            requestBody.put("patch.$set.totalBudget.amount", payload.get("totalBudget").asText());
            requestBody.put("patch.$set.totalBudget.currencyCode", "USD");
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "BUDGET_UPDATED");
    }
    
    private Message updateBid(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String campaignId = payload.path("campaignId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adCampaigns/" + campaignId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        if (payload.has("bidType")) {
            requestBody.put("patch.$set.bidType", payload.get("bidType").asText());
        }
        
        if (payload.has("bidAmount")) {
            requestBody.put("patch.$set.bidAmount.amount", payload.get("bidAmount").asText());
            requestBody.put("patch.$set.bidAmount.currencyCode", "USD");
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "BID_UPDATED");
    }
    
    // Reporting Methods
    private Message createReport(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/adAnalytics";
        
        Map<String, String> params = new HashMap<>();
        params.put("q", "analytics");
        params.put("pivot", payload.path("pivot").asText("CAMPAIGN"));
        params.put("dateRange.start.year", payload.path("startDate.year").asText());
        params.put("dateRange.start.month", payload.path("startDate.month").asText());
        params.put("dateRange.start.day", payload.path("startDate.day").asText());
        params.put("dateRange.end.year", payload.path("endDate.year").asText());
        params.put("dateRange.end.month", payload.path("endDate.month").asText());
        params.put("dateRange.end.day", payload.path("endDate.day").asText());
        params.put("timeGranularity", payload.path("granularity").asText(TimeGranularity.DAILY.name()));
        
        // Add campaigns filter
        if (payload.has("campaignIds") && payload.get("campaignIds").isArray()) {
            StringBuilder campaigns = new StringBuilder();
            for (JsonNode campaignId : payload.get("campaignIds")) {
                campaigns.append("urn:li:sponsoredCampaign:").append(campaignId.asText()).append(",");
            }
            params.put("campaigns", campaigns.toString());
        }
        
        // Add metrics
        if (payload.has("metrics") && payload.get("metrics").isArray()) {
            params.put("fields", payload.get("metrics").toString());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "REPORT_CREATED");
    }
    
    private Message getAnalytics(Message message) throws Exception {
        return createReport(message);
    }
    
    private Message exportLeads(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/leadGenerationFormResponses";
        
        Map<String, String> params = new HashMap<>();
        params.put("q", "form");
        params.put("form", payload.path("formId").asText());
        
        if (payload.has("startTime")) {
            params.put("submittedAfter", payload.get("startTime").asText());
        }
        if (payload.has("endTime")) {
            params.put("submittedBefore", payload.get("endTime").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "LEADS_EXPORTED");
    }
    
    // Conversion Tracking Methods
    private Message createConversion(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/conversions";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("type", payload.path("type").asText(ConversionType.LEAD.name()));
        requestBody.put("postClickAttributionWindowSize", payload.path("postClickWindow").asInt(30));
        requestBody.put("viewThroughAttributionWindowSize", payload.path("viewThroughWindow").asInt(7));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CONVERSION_CREATED");
    }
    
    private Message trackConversion(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/conversionEvents";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("conversion", payload.path("conversionId").asText());
        requestBody.put("conversionHappenedAt", payload.path("timestamp").asLong());
        
        if (payload.has("value")) {
            ObjectNode amount = requestBody.putObject("amount");
            amount.put("value", payload.get("value").asText());
            amount.put("currencyCode", payload.path("currency").asText("USD"));
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CONVERSION_TRACKED");
    }
    
    // Lead Gen Form Methods
    private Message createLeadGenForm(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = LINKEDIN_ADS_REST_BASE + "/adForms";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("account", "urn:li:sponsoredAccount:" + config.getAdAccountId());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("privacyPolicy.url", payload.path("privacyPolicyUrl").asText());
        
        // Questions
        if (payload.has("questions") && payload.get("questions").isArray()) {
            ArrayNode questions = requestBody.putArray("questions");
            for (JsonNode question : payload.get("questions")) {
                ObjectNode q = questions.addObject();
                q.put("fieldType", question.path("type").asText());
                q.put("required", question.path("required").asBoolean(false));
                if (question.has("customQuestion")) {
                    q.put("customQuestion", question.get("customQuestion").asText());
                }
            }
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "LEAD_GEN_FORM_CREATED");
    }
    
    private Message updateLeadGenForm(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String formId = payload.path("formId").asText();
        
        String url = LINKEDIN_ADS_REST_BASE + "/adForms/" + formId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        if (payload.has("name")) {
            requestBody.put("patch.$set.name", payload.get("name").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "LEAD_GEN_FORM_UPDATED");
    }
    
    private Message getFormResponses(Message message) throws Exception {
        return exportLeads(message);
    }
    
    // Helper Methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body) {
        return makeApiCall(url, method, body, null);
    }
    
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("LinkedIn-Version", "202401");
        headers.set("X-Restli-Protocol-Version", "2.0.0");
        
        StringBuilder urlBuilder = new StringBuilder(url);
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) -> 
                urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        
        rateLimiterService.acquire("linkedin_ads_api", 1);
        
        return restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);
    }
    
    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }
    
    private String hashValue(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.toLowerCase().getBytes("UTF-8"));
            return javax.xml.bind.DatatypeConverter.printHexBinary(hash).toLowerCase();
        } catch (Exception e) {
            return value;
        }
    }
    
    private String parseDate(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    private void validateConfiguration() throws AdapterException {
        if (config.getAdAccountId() == null || config.getAdAccountId().isEmpty()) {
            throw new AdapterException("LinkedIn Ads account ID is not configured");
        }
        if (config.getClientId() == null || config.getClientSecret() == null ||
            config.getAccessToken() == null) {
            throw new AdapterException("LinkedIn Ads API credentials are not configured");
        }
    }
}