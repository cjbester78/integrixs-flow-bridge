package com.integrixs.adapters.social.linkedin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.social.linkedin.LinkedInApiConfig;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

@Component("linkedInOutboundAdapter")
public class LinkedInOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(LinkedInOutboundAdapter.class);


    @Value("${integrix.adapters.linkedin.api-base-url:https://api.linkedin.com/v2}")
    private String LINKEDIN_API_BASE;

    @Value("${integrix.adapters.linkedin.rest-api-base-url:https://api.linkedin.com/rest}")
    private String LINKEDIN_API_REST_BASE;

    @Value("${integrix.adapters.linkedin.media-upload-url:https://api.linkedin.com/mediaUpload}")
    private String LINKEDIN_MEDIA_UPLOAD;

    @Value("${integrix.adapters.linkedin.ugc-api-base-url:https://api.linkedin.com/v2}")
    private String LINKEDIN_API_UGC;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private LinkedInApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public LinkedInOutboundAdapter(
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
            log.info("Processing LinkedIn operation: {}", operation);

            switch(operation.toUpperCase()) {
                // Content sharing
                case "CREATE_POST":
                    return createPost(message);
                case "CREATE_ARTICLE":
                    return createArticle(message);
                case "SHARE_IMAGE":
                    return shareImage(message);
                case "SHARE_VIDEO":
                    return shareVideo(message);
                case "SHARE_DOCUMENT":
                    return shareDocument(message);
                case "UPDATE_POST":
                    return updatePost(message);
                case "DELETE_POST":
                    return deletePost(message);

                // Engagement
                case "CREATE_COMMENT":
                    return createComment(message);
                case "DELETE_COMMENT":
                    return deleteComment(message);
                case "ADD_REACTION":
                    return addReaction(message);
                case "REMOVE_REACTION":
                    return removeReaction(message);

                // Profile & Company
                case "GET_PROFILE":
                    return getProfile(message);
                case "UPDATE_PROFILE":
                    return updateProfile(message);
                case "GET_COMPANY_INFO":
                    return getCompanyInfo(message);
                case "UPDATE_COMPANY_PAGE":
                    return updateCompanyPage(message);

                // Connections & Messaging
                case "SEND_CONNECTION_REQUEST":
                    return sendConnectionRequest(message);
                case "ACCEPT_CONNECTION":
                    return acceptConnection(message);
                case "SEND_MESSAGE":
                    return sendDirectMessage(message);

                // Analytics
                case "GET_POST_ANALYTICS":
                    return getPostAnalytics(message);
                case "GET_FOLLOWER_STATISTICS":
                    return getFollowerStatistics(message);
                case "GET_SHARE_STATISTICS":
                    return getShareStatistics(message);

                // Events
                case "CREATE_EVENT":
                    return createEvent(message);
                case "UPDATE_EVENT":
                    return updateEvent(message);

                // Hashtag tracking
                case "FOLLOW_HASHTAG":
                    return followHashtag(message);
                case "UNFOLLOW_HASHTAG":
                    return unfollowHashtag(message);

                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error in LinkedIn outbound adapter", e);
            MessageDTO errorResponse = new MessageDTO();
            errorResponse.setCorrelationId(message.getCorrelationId());
            errorResponse.setStatus(MessageStatus.FAILED);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "operation", message.getHeaders().getOrDefault("operation", "unknown")
           ));
            return errorResponse;
        }
    }

    // Content methods
    private MessageDTO createPost(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_UGC + "/ugcPosts";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("author", config.getMemberUrn());

        ObjectNode commentary = objectMapper.createObjectNode();
        commentary.put("text", payload.path("text").asText());
        requestBody.set("commentary", commentary);

        // Visibility
        ObjectNode visibility = objectMapper.createObjectNode();
        String visibilityValue = payload.path("visibility").asText(Visibility.CONNECTIONS.name());
        visibility.put("com.linkedin.ugc.MemberNetworkVisibility", visibilityValue);
        requestBody.set("visibility", visibility);

        // Distribution
        ObjectNode distribution = objectMapper.createObjectNode();
        distribution.put("feedDistribution", payload.path("distribution").asText(Distribution.MAIN_FEED.name()));
        requestBody.set("distribution", distribution);

        // Lifecycle state
        requestBody.put("lifecycleState", "PUBLISHED");

        // Media attachments
        if(payload.has("media") && payload.get("media").isArray()) {
            ArrayNode content = requestBody.putArray("content");
            for(JsonNode mediaItem : payload.get("media")) {
                ObjectNode media = content.addObject();
                media.put("entity", mediaItem.path("entity").asText());
                if(mediaItem.has("title")) {
                    ObjectNode mediaTitle = media.putObject("title");
                    mediaTitle.put("text", mediaItem.get("title").asText());
                }
                if(mediaItem.has("description")) {
                    ObjectNode mediaDesc = media.putObject("description");
                    mediaDesc.put("text", mediaItem.get("description").asText());
                }
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "POST_CREATED");
    }

    private MessageDTO createArticle(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/articles";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("author", config.getMemberUrn());
        requestBody.put("title", payload.path("title").asText());
        requestBody.put("content", payload.path("content").asText());

        if(payload.has("coverImage")) {
            requestBody.put("coverImage", payload.get("coverImage").asText());
        }

        if(payload.has("publishedAt")) {
            requestBody.put("publishedAt", payload.get("publishedAt").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "ARTICLE_CREATED");
    }

    private MessageDTO shareImage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // First, register the image upload
        String registerUrl = LINKEDIN_API_REST_BASE + "/images";

        ObjectNode registerBody = objectMapper.createObjectNode();
        registerBody.put("initializeUploadRequest", objectMapper.createObjectNode()
            .put("owner", config.getMemberUrn()));

        ResponseEntity<String> registerResponse = makeApiCall(registerUrl, HttpMethod.POST, registerBody.toString());
        JsonNode uploadInfo = objectMapper.readTree(registerResponse.getBody());

        String uploadUrl = uploadInfo.path("value").path("uploadUrl").asText();
        String imageUrn = uploadInfo.path("value").path("image").asText();

        // Upload the image
        if(payload.has("imageData")) {
            byte[] imageData = Base64.getDecoder().decode(payload.get("imageData").asText());
            uploadMedia(uploadUrl, imageData, "image/jpeg");
        } else if(payload.has("imagePath")) {
            byte[] imageData = Files.readAllBytes(Paths.get(payload.get("imagePath").asText()));
            uploadMedia(uploadUrl, imageData, detectContentType(payload.get("imagePath").asText()));
        }

        // Create post with image
        ObjectNode postPayload = objectMapper.createObjectNode();
        postPayload.put("text", payload.path("caption").asText());

        ArrayNode media = postPayload.putArray("media");
        ObjectNode mediaItem = media.addObject();
        mediaItem.put("entity", imageUrn);
        if(payload.has("altText")) {
            mediaItem.put("altText", payload.get("altText").asText());
        }

        MessageDTO postMessageDTO = new MessageDTO();
        postMessageDTO.setPayload(postPayload.toString());
        postMessageDTO.setHeaders(Map.of("operation", "CREATE_POST"));

        return createPost(postMessageDTO);
    }

    private MessageDTO shareVideo(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Register video upload
        String registerUrl = LINKEDIN_API_REST_BASE + "/videos";

        ObjectNode registerBody = objectMapper.createObjectNode();
        ObjectNode initRequest = registerBody.putObject("initializeUploadRequest");
        initRequest.put("owner", config.getMemberUrn());
        initRequest.put("fileSizeBytes", payload.path("fileSize").asLong());
        initRequest.put("uploadCaptions", false);

        ResponseEntity<String> registerResponse = makeApiCall(registerUrl, HttpMethod.POST, registerBody.toString());
        JsonNode uploadInfo = objectMapper.readTree(registerResponse.getBody());

        String videoUrn = uploadInfo.path("value").path("video").asText();
        JsonNode uploadInstructions = uploadInfo.path("value").path("uploadInstructions");

        // Upload video in parts if needed
        if(uploadInstructions.isArray()) {
            for(JsonNode instruction : uploadInstructions) {
                String uploadUrl = instruction.path("uploadUrl").asText();
                // Upload video chunk logic here
            }
        }

        // Finalize upload
        String finalizeUrl = LINKEDIN_API_REST_BASE + "/videos/" + videoUrn + "/uploadFinalized";
        makeApiCall(finalizeUrl, HttpMethod.POST, " {}");

        // Create post with video
        ObjectNode postPayload = objectMapper.createObjectNode();
        postPayload.put("text", payload.path("caption").asText());

        ArrayNode media = postPayload.putArray("media");
        ObjectNode mediaItem = media.addObject();
        mediaItem.put("entity", videoUrn);
        if(payload.has("title")) {
            ObjectNode title = mediaItem.putObject("title");
            title.put("text", payload.get("title").asText());
        }

        MessageDTO postMessageDTO = new MessageDTO();
        postMessageDTO.setPayload(postPayload.toString());
        postMessageDTO.setHeaders(Map.of("operation", "CREATE_POST"));

        return createPost(postMessageDTO);
    }

    private MessageDTO shareDocument(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Register document upload
        String registerUrl = LINKEDIN_API_REST_BASE + "/documents";

        ObjectNode registerBody = objectMapper.createObjectNode();
        ObjectNode initRequest = registerBody.putObject("initializeUploadRequest");
        initRequest.put("owner", config.getMemberUrn());

        ResponseEntity<String> registerResponse = makeApiCall(registerUrl, HttpMethod.POST, registerBody.toString());
        JsonNode uploadInfo = objectMapper.readTree(registerResponse.getBody());

        String documentUrn = uploadInfo.path("value").path("document").asText();
        String uploadUrl = uploadInfo.path("value").path("uploadUrl").asText();

        // Upload document
        if(payload.has("documentPath")) {
            byte[] documentData = Files.readAllBytes(Paths.get(payload.get("documentPath").asText()));
            uploadMedia(uploadUrl, documentData, "application/pdf");
        }

        // Create post with document
        ObjectNode postPayload = objectMapper.createObjectNode();
        postPayload.put("text", payload.path("caption").asText());

        ArrayNode media = postPayload.putArray("media");
        ObjectNode mediaItem = media.addObject();
        mediaItem.put("entity", documentUrn);

        MessageDTO postMessageDTO = new MessageDTO();
        postMessageDTO.setPayload(postPayload.toString());
        postMessageDTO.setHeaders(Map.of("operation", "CREATE_POST"));

        return createPost(postMessageDTO);
    }

    private MessageDTO updatePost(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String postUrn = payload.path("postUrn").asText();

        String url = LINKEDIN_API_REST_BASE + "/posts/" + postUrn;

        ObjectNode requestBody = objectMapper.createObjectNode();

        if(payload.has("commentary")) {
            ObjectNode commentary = requestBody.putObject("commentary");
            commentary.put("text", payload.get("commentary").asText());
        }

        if(payload.has("visibility")) {
            ObjectNode visibility = requestBody.putObject("visibility");
            visibility.put("com.linkedin.ugc.MemberNetworkVisibility", payload.get("visibility").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PATCH, requestBody.toString());
        return createResponseMessage(response, "POST_UPDATED");
    }

    private MessageDTO deletePost(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String postUrn = payload.path("postUrn").asText();

        String url = LINKEDIN_API_REST_BASE + "/posts/" + postUrn;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "POST_DELETED");
    }

    // Engagement Methods
    private MessageDTO createComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/socialActions/" +
                     payload.path("postUrn").asText() + "/comments";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("actor", config.getMemberUrn());
        ObjectNode commentText = requestBody.putObject("message");
        commentText.put("text", payload.path("text").asText());

        if(payload.has("parentCommentUrn")) {
            requestBody.put("parentComment", payload.get("parentCommentUrn").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "COMMENT_CREATED");
    }

    private MessageDTO deleteComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentUrn = payload.path("commentUrn").asText();

        String url = LINKEDIN_API_REST_BASE + "/socialActions/comments/" + commentUrn;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "COMMENT_DELETED");
    }

    private MessageDTO addReaction(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/reactions";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("actor", config.getMemberUrn());
        requestBody.put("entity", payload.path("entityUrn").asText());
        requestBody.put("reactionType", payload.path("reactionType").asText(ReactionType.LIKE.name()));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "REACTION_ADDED");
    }

    private MessageDTO removeReaction(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String entityUrn = payload.path("entityUrn").asText();
        String url = LINKEDIN_API_REST_BASE + "/reactions/(actor:" + config.getMemberUrn() +
                     ",entity:" + entityUrn + ")";

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "REACTION_REMOVED");
    }

    // Profile & Company Methods
    private MessageDTO getProfile(MessageDTO message) throws Exception {
        String profileId = message.getHeaders()
            .getOrDefault("profileId", config.getMemberUrn()).toString();

        String url = LINKEDIN_API_REST_BASE + "/people/" + profileId;

        Map<String, String> params = new HashMap<>();
        params.put("projection", "(id,firstName,lastName,headline,vanityName,profilePicture)");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "PROFILE_RETRIEVED");
    }

    private MessageDTO updateProfile(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/people/" + config.getMemberUrn();

        ObjectNode requestBody = objectMapper.createObjectNode();

        if(payload.has("headline")) {
            requestBody.put("headline", payload.get("headline").asText());
        }

        if(payload.has("summary")) {
            requestBody.put("summary", payload.get("summary").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PATCH, requestBody.toString());
        return createResponseMessage(response, "PROFILE_UPDATED");
    }

    private MessageDTO getCompanyInfo(MessageDTO message) throws Exception {
        String organizationId = message.getHeaders()
            .getOrDefault("organizationId", config.getOrganizationId()).toString();

        String url = LINKEDIN_API_REST_BASE + "/organizations/" + organizationId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "COMPANY_INFO_RETRIEVED");
    }

    private MessageDTO updateCompanyPage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String organizationId = config.getOrganizationId();

        String url = LINKEDIN_API_REST_BASE + "/organizations/" + organizationId;

        ObjectNode requestBody = objectMapper.createObjectNode();

        if(payload.has("description")) {
            requestBody.put("description", payload.get("description").asText());
        }

        if(payload.has("specialties")) {
            requestBody.set("specialties", payload.get("specialties"));
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PATCH, requestBody.toString());
        return createResponseMessage(response, "COMPANY_PAGE_UPDATED");
    }

    // Connection & Messaging Methods
    private MessageDTO sendConnectionRequest(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/invitations";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("invitee", payload.path("inviteeUrn").asText());
        requestBody.put("invitationType", "CONNECTION");

        if(payload.has("message")) {
            ObjectNode invitationMessageDTO = requestBody.putObject("message");
            invitationMessageDTO.put("text", payload.get("message").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "CONNECTION_REQUEST_SENT");
    }

    private MessageDTO acceptConnection(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String invitationUrn = payload.path("invitationUrn").asText();

        String url = LINKEDIN_API_REST_BASE + "/invitations/" + invitationUrn + "/accept";

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, " {}");
        return createResponseMessage(response, "CONNECTION_ACCEPTED");
    }

    private MessageDTO sendDirectMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/messages";

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode recipients = requestBody.putArray("recipients");

        if(payload.has("recipientUrns") && payload.get("recipientUrns").isArray()) {
            for(JsonNode recipient : payload.get("recipientUrns")) {
                recipients.add(recipient.asText());
            }
        }

        ObjectNode messageBody = requestBody.putObject("body");
        messageBody.put("text", payload.path("text").asText());

        if(payload.has("subject")) {
            requestBody.put("subject", payload.get("subject").asText());
        }

        if(payload.has("attachments") && payload.get("attachments").isArray()) {
            ArrayNode attachments = requestBody.putArray("attachments");
            for(JsonNode attachment : payload.get("attachments")) {
                attachments.add(attachment);
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "MESSAGE_SENT");
    }

    // Analytics Methods
    private MessageDTO getPostAnalytics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String postUrn = payload.path("postUrn").asText();

        String url = LINKEDIN_API_REST_BASE + "/socialActions/" + postUrn + "/statistics";

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null);
        return createResponseMessage(response, "POST_ANALYTICS_RETRIEVED");
    }

    private MessageDTO getFollowerStatistics(MessageDTO message) throws Exception {
        String organizationId = config.getOrganizationId();

        String url = LINKEDIN_API_REST_BASE + "/organizationPageStatistics";

        Map<String, String> params = new HashMap<>();
        params.put("q", "organization");
        params.put("organization", "urn:li:organization:" + organizationId);
        params.put("timeInterval", "MONTH");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "FOLLOWER_STATISTICS_RETRIEVED");
    }

    private MessageDTO getShareStatistics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/organizationShareStatistics";

        Map<String, String> params = new HashMap<>();
        params.put("q", "organizationShareStatistics");

        if(config.getOrganizationId() != null) {
            params.put("organization", "urn:li:organization:" + config.getOrganizationId());
        } else {
            params.put("author", config.getMemberUrn());
        }

        if(payload.has("startTime")) {
            params.put("timeInterval.start", payload.get("startTime").asText());
        }
        if(payload.has("endTime")) {
            params.put("timeInterval.end", payload.get("endTime").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "SHARE_STATISTICS_RETRIEVED");
    }

    // Event Methods
    private MessageDTO createEvent(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = LINKEDIN_API_REST_BASE + "/events";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("organizer", config.getMemberUrn());
        requestBody.put("name", payload.path("name").asText());
        requestBody.put("description", payload.path("description").asText());
        requestBody.put("startAt", payload.path("startAt").asText());
        requestBody.put("endAt", payload.path("endAt").asText());

        if(payload.has("location")) {
            requestBody.set("location", payload.get("location"));
        }

        if(payload.has("isOnline")) {
            requestBody.put("isOnline", payload.get("isOnline").asBoolean());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "EVENT_CREATED");
    }

    private MessageDTO updateEvent(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String eventUrn = payload.path("eventUrn").asText();

        String url = LINKEDIN_API_REST_BASE + "/events/" + eventUrn;

        ObjectNode requestBody = objectMapper.createObjectNode();

        if(payload.has("name")) {
            requestBody.put("name", payload.get("name").asText());
        }
        if(payload.has("description")) {
            requestBody.put("description", payload.get("description").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PATCH, requestBody.toString());
        return createResponseMessage(response, "EVENT_UPDATED");
    }

    // Hashtag Methods
    private MessageDTO followHashtag(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String hashtag = payload.path("hashtag").asText();

        String url = LINKEDIN_API_REST_BASE + "/follows";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("follower", config.getMemberUrn());
        requestBody.put("followedEntity", "urn:li:hashtag:" + hashtag);

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "HASHTAG_FOLLOWED");
    }

    private MessageDTO unfollowHashtag(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String hashtag = payload.path("hashtag").asText();

        String url = LINKEDIN_API_REST_BASE + "/follows/(follower:" + config.getMemberUrn() +
                     ",followedEntity:urn:li:hashtag:" + hashtag + ")";

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "HASHTAG_UNFOLLOWED");
    }

    // Helper Methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body) {
        return makeApiCall(url, method, body, null);
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body, Map<String, String> params) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            if(params != null) {
                params.forEach(builder::queryParam);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(builder.toUriString(), method, entity, String.class);
        } catch(HttpClientErrorException e) {
            log.error("LinkedIn API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Return error response with status code and error details
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "LinkedIn API request failed");
            errorResponse.put("status", e.getStatusCode().value());
            errorResponse.put("message", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse.toString());
        } catch(Exception e) {
            log.error("Failed to make API call", e);
            // Return internal server error response
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Failed to make API call");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse.toString());
        }
    }


    private void uploadMedia(String uploadUrl, byte[] mediaData, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setBearerAuth(getAccessToken());

        HttpEntity<byte[]> entity = new HttpEntity<>(mediaData, headers);
        restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);
    }

    private String detectContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if(lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if(lowerName.endsWith(".png")) return "image/png";
        if(lowerName.endsWith(".gif")) return "image/gif";
        if(lowerName.endsWith(".mp4")) return "video/mp4";
        if(lowerName.endsWith(".pdf")) return "application/pdf";
        return "application/octet - stream";
    }

    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getClientId() == null || config.getClientSecret() == null) {
            throw new AdapterException("LinkedIn OAuth credentials are not configured");
        }
        if(config.getAccessToken() == null) {
            throw new AdapterException("LinkedIn access token is not configured");
        }
        if(config.getMemberUrn() == null && config.getOrganizationId() == null) {
            throw new AdapterException("Neither member URN nor organization ID is configured");
        }
    }

    public void setConfiguration(LinkedInApiConfig config) {
        this.config = config;
    }

    // LinkedIn API Enums
    private enum Visibility {
        CONNECTIONS,
        PUBLIC,
        LOGGED_IN
    }

    private enum Distribution {
        MAIN_FEED,
        NONE
    }

    private enum ReactionType {
        LIKE,
        CELEBRATE,
        SUPPORT,
        LOVE,
        INSIGHTFUL,
        CURIOUS
    }

    private MessageDTO createResponseMessage(ResponseEntity<String> response, String operationType) {
        MessageDTO responseMessage = new MessageDTO();
        responseMessage.setCorrelationId(UUID.randomUUID().toString());
        responseMessage.setTimestamp(java.time.LocalDateTime.now());
        responseMessage.setStatus(response.getStatusCode().is2xxSuccessful() ? MessageStatus.SUCCESS : MessageStatus.FAILED);

        Map<String, Object> headers = new HashMap<>();
        headers.put("operation", operationType);
        headers.put("statusCode", response.getStatusCodeValue());
        headers.put("source", "linkedin");
        responseMessage.setHeaders(headers);

        responseMessage.setPayload(response.getBody());
        return responseMessage;
    }

    // Required abstract methods from AbstractSocialMediaOutboundAdapter
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
        configMap.put("apiBaseUrl", LINKEDIN_API_BASE);
        configMap.put("apiRestBaseUrl", LINKEDIN_API_REST_BASE);
        configMap.put("mediaUploadUrl", LINKEDIN_MEDIA_UPLOAD);
        configMap.put("clientId", config != null ? config.getClientId() : null);
        configMap.put("organizationId", config != null ? config.getOrganizationId() : null);
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    // Required abstract methods from AbstractOutboundAdapter
    @Override
    protected void doReceiverInitialize() throws Exception {
        log.debug("Initializing LinkedIn outbound adapter receiver");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.debug("Destroying LinkedIn outbound adapter receiver");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // LinkedIn outbound adapter doesn't support receiving
        return AdapterResult.failure("LinkedIn outbound adapter does not support receiving messages");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            validateConfiguration();
            // Test by getting the profile
            MessageDTO testMessage = new MessageDTO();
            testMessage.setHeaders(Map.of("operation", "GET_PROFILE"));
            MessageDTO response = getProfile(testMessage);

            if (response.getStatus() == MessageStatus.SUCCESS) {
                return AdapterResult.success(null, "LinkedIn API connection successful");
            } else {
                return AdapterResult.failure("LinkedIn API connection failed");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test LinkedIn connection: " + e.getMessage(), e);
        }
    }

    @Override
    protected long getPollingIntervalMs() {
        // LinkedIn outbound adapter doesn't support polling
        return 0;
    }
}
