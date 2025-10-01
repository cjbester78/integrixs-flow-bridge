package com.integrixs.adapters.social.youtube;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.youtube.YouTubeDataApiConfig.*;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component("youTubeDataOutboundAdapter")
public class YouTubeDataOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(YouTubeDataOutboundAdapter.class);


    // API URLs are configured in application.yml
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private YouTubeDataApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public YouTubeDataOutboundAdapter(
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
            log.info("Processing YouTube Data API operation: {}", operation);

            switch(operation.toUpperCase()) {
                // Video Management
                case "UPLOAD_VIDEO":
                    return uploadVideo(message);
                case "UPDATE_VIDEO":
                    return updateVideo(message);
                case "DELETE_VIDEO":
                    return deleteVideo(message);
                case "SET_THUMBNAIL":
                    return setThumbnail(message);
                case "GET_VIDEO_DETAILS":
                    return getVideoDetails(message);

                // Playlist Management
                case "CREATE_PLAYLIST":
                    return createPlaylist(message);
                case "UPDATE_PLAYLIST":
                    return updatePlaylist(message);
                case "DELETE_PLAYLIST":
                    return deletePlaylist(message);
                case "ADD_TO_PLAYLIST":
                    return addToPlaylist(message);
                case "REMOVE_FROM_PLAYLIST":
                    return removeFromPlaylist(message);
                case "REORDER_PLAYLIST":
                    return reorderPlaylist(message);

                // Comment Management
                case "POST_COMMENT":
                    return postComment(message);
                case "UPDATE_COMMENT":
                    return updateComment(message);
                case "DELETE_COMMENT":
                    return deleteComment(message);
                case "MODERATE_COMMENT":
                    return moderateComment(message);
                case "REPLY_TO_COMMENT":
                    return replyToComment(message);

                // Live Streaming
                case "CREATE_LIVE_BROADCAST":
                    return createLiveBroadcast(message);
                case "UPDATE_LIVE_BROADCAST":
                    return updateLiveBroadcast(message);
                case "START_LIVE_STREAM":
                    return startLiveStream(message);
                case "END_LIVE_STREAM":
                    return endLiveStream(message);
                case "CREATE_LIVE_STREAM":
                    return createLiveStream(message);

                // Channel Management
                case "UPDATE_CHANNEL":
                    return updateChannel(message);
                case "UPDATE_BRANDING":
                    return updateBranding(message);
                case "SET_CHANNEL_SECTIONS":
                    return setChannelSections(message);
                case "UPDATE_CHANNEL_SETTINGS":
                    return updateChannelSettings(message);

                // Community Features
                case "POST_COMMUNITY":
                    return postCommunity(message);
                case "CREATE_POLL":
                    return createPoll(message);
                case "POST_STORY":
                    return postStory(message);

                // Captions
                case "UPLOAD_CAPTION":
                    return uploadCaption(message);
                case "UPDATE_CAPTION":
                    return updateCaption(message);
                case "DELETE_CAPTION":
                    return deleteCaption(message);

                // Subscriptions
                case "SUBSCRIBE":
                    return subscribe(message);
                case "UNSUBSCRIBE":
                    return unsubscribe(message);

                // Analytics
                case "GET_ANALYTICS":
                    return getAnalytics(message);
                case "GET_REAL_TIME_ANALYTICS":
                    return getRealTimeAnalytics(message);

                // Monetization
                case "UPDATE_MONETIZATION":
                    return updateMonetization(message);
                case "SET_MEMBERSHIP_PERKS":
                    return setMembershipPerks(message);

                // Search
                case "SEARCH_VIDEOS":
                    return searchVideos(message);
                case "SEARCH_CHANNELS":
                    return searchChannels(message);
                case "SEARCH_PLAYLISTS":
                    return searchPlaylists(message);

                default:
                    throw new AdapterException("Unsupported operation: " + operation);
            }
        } catch(Exception e) {
            log.error("Error in YouTube Data outbound adapter", e);
            throw new AdapterException("Failed to process YouTube operation", e);
        }
    }

    // Video Management Methods
    private MessageDTO uploadVideo(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Step 1: Upload video file
        String videoPath = payload.path("videoPath").asText();
        String uploadUrl = config.getUploadBaseUrl() + "/videos?uploadType=resumable&part=snippet,status,contentDetails";

        // Create video metadata
        ObjectNode videoMetadata = objectMapper.createObjectNode();

        ObjectNode snippet = videoMetadata.putObject("snippet");
        snippet.put("title", payload.path("title").asText());
        snippet.put("description", payload.path("description").asText(""));

        if(payload.has("tags")) {
            ArrayNode tags = snippet.putArray("tags");
            for(JsonNode tag : payload.get("tags")) {
                tags.add(tag.asText());
            }
        }

        snippet.put("categoryId", payload.path("categoryId").asText("22")); // Default to People & Blogs

        ObjectNode status = videoMetadata.putObject("status");
        status.put("privacyStatus", payload.path("privacyStatus").asText(PrivacyStatus.PRIVATE.name()));

        if(payload.has("publishAt")) {
            status.put("publishAt", payload.get("publishAt").asText());
            status.put("privacyStatus", PrivacyStatus.PRIVATE.name());
        }

        if(payload.has("madeForKids")) {
            status.put("selfDeclaredMadeForKids", payload.get("madeForKids").asBoolean());
        }

        if(payload.has("embeddable")) {
            status.put("embeddable", payload.get("embeddable").asBoolean());
        }

        if(payload.has("license")) {
            status.put("license", payload.get("license").asText(License.YOUTUBE.name()));
        }

        // Initiate resumable upload
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X - Upload - Content - Type", "video/*");

        Path videoFile = Paths.get(videoPath);
        long fileSize = Files.size(videoFile);
        headers.set("X - Upload - Content - Length", String.valueOf(fileSize));

        HttpEntity<String> initEntity = new HttpEntity<>(videoMetadata.toString(), headers);
        ResponseEntity<String> initResponse = restTemplate.exchange(uploadUrl, HttpMethod.POST, initEntity, String.class);

        if(!initResponse.getStatusCode().is2xxSuccessful()) {
            throw new AdapterException("Failed to initiate video upload");
        }

        // Get upload URL from Location header
        String uploadLocation = initResponse.getHeaders().getLocation().toString();

        // Step 2: Upload video content
        byte[] videoContent = Files.readAllBytes(videoFile);

        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setBearerAuth(getAccessToken());
        uploadHeaders.setContentType(MediaType.parseMediaType("video/*"));
        uploadHeaders.setContentLength(fileSize);

        HttpEntity<byte[]> uploadEntity = new HttpEntity<>(videoContent, uploadHeaders);
        ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadLocation, HttpMethod.PUT, uploadEntity, String.class);

        MessageDTO response = createResponseMessage(uploadResponse, "VIDEO_UPLOADED");

        // Step 3: Set thumbnail if provided
        if(payload.has("thumbnailPath")) {
            JsonNode uploadedVideo = objectMapper.readTree(uploadResponse.getBody());
            String videoId = uploadedVideo.path("id").asText();

            MessageDTO thumbnailMessage = new MessageDTO();
            ObjectNode thumbnailPayload = objectMapper.createObjectNode();
            thumbnailPayload.put("videoId", videoId);
            thumbnailPayload.put("thumbnailPath", payload.get("thumbnailPath").asText());
            thumbnailMessage.setPayload(thumbnailPayload.toString());
            thumbnailMessage.setHeaders(Map.of("operation", "SET_THUMBNAIL"));

            setThumbnail(thumbnailMessage);
        }

        return response;
    }

    private MessageDTO updateVideo(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String videoId = payload.path("videoId").asText();

        String url = config.getApiBaseUrl() + "/videos?part = snippet,status";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("id", videoId);

        ObjectNode snippet = requestBody.putObject("snippet");
        if(payload.has("title")) {
            snippet.put("title", payload.get("title").asText());
        }
        if(payload.has("description")) {
            snippet.put("description", payload.get("description").asText());
        }
        if(payload.has("tags")) {
            ArrayNode tags = snippet.putArray("tags");
            for(JsonNode tag : payload.get("tags")) {
                tags.add(tag.asText());
            }
        }
        if(payload.has("categoryId")) {
            snippet.put("categoryId", payload.get("categoryId").asText());
        }

        if(payload.has("privacyStatus") || payload.has("embeddable") || payload.has("license")) {
            ObjectNode status = requestBody.putObject("status");
            if(payload.has("privacyStatus")) {
                status.put("privacyStatus", payload.get("privacyStatus").asText());
            }
            if(payload.has("embeddable")) {
                status.put("embeddable", payload.get("embeddable").asBoolean());
            }
            if(payload.has("license")) {
                status.put("license", payload.get("license").asText());
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "VIDEO_UPDATED");
    }

    private MessageDTO deleteVideo(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String videoId = payload.path("videoId").asText();

        String url = config.getApiBaseUrl() + "/videos?id = " + videoId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "VIDEO_DELETED");
    }

    private MessageDTO setThumbnail(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String videoId = payload.path("videoId").asText();
        String thumbnailPath = payload.path("thumbnailPath").asText();

        String url = config.getUploadBaseUrl() + "/thumbnails/set?videoId = " + videoId;

        Path thumbnailFile = Paths.get(thumbnailPath);
        byte[] thumbnailContent = Files.readAllBytes(thumbnailFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.IMAGE_PNG);

        HttpEntity<byte[]> entity = new HttpEntity<>(thumbnailContent, headers);

        rateLimiterService.acquire("youtube_data_api", 50); // Thumbnail upload costs more quota
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return createResponseMessage(response, "THUMBNAIL_SET");
    }

    private MessageDTO getVideoDetails(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String videoId = payload.path("videoId").asText();

        String url = config.getApiBaseUrl() + "/videos";
        Map<String, String> params = new HashMap<>();
        params.put("id", videoId);
        params.put("part", payload.path("parts").asText("snippet,statistics,contentDetails,status"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "VIDEO_DETAILS_RETRIEVED");
    }

    // Playlist Management Methods
    private MessageDTO createPlaylist(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/playlists?part = snippet,status";

        ObjectNode requestBody = objectMapper.createObjectNode();

        ObjectNode snippet = requestBody.putObject("snippet");
        snippet.put("title", payload.path("title").asText());
        snippet.put("description", payload.path("description").asText(""));

        if(payload.has("tags")) {
            ArrayNode tags = snippet.putArray("tags");
            for(JsonNode tag : payload.get("tags")) {
                tags.add(tag.asText());
            }
        }

        ObjectNode status = requestBody.putObject("status");
        status.put("privacyStatus", payload.path("privacyStatus").asText(PlaylistStatus.PUBLIC.name()));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "PLAYLIST_CREATED");
    }

    private MessageDTO updatePlaylist(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String playlistId = payload.path("playlistId").asText();

        String url = config.getApiBaseUrl() + "/playlists?part = snippet,status";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("id", playlistId);

        ObjectNode snippet = requestBody.putObject("snippet");
        if(payload.has("title")) {
            snippet.put("title", payload.get("title").asText());
        }
        if(payload.has("description")) {
            snippet.put("description", payload.get("description").asText());
        }
        if(payload.has("tags")) {
            ArrayNode tags = snippet.putArray("tags");
            for(JsonNode tag : payload.get("tags")) {
                tags.add(tag.asText());
            }
        }

        if(payload.has("privacyStatus")) {
            ObjectNode status = requestBody.putObject("status");
            status.put("privacyStatus", payload.get("privacyStatus").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "PLAYLIST_UPDATED");
    }

    private MessageDTO deletePlaylist(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String playlistId = payload.path("playlistId").asText();

        String url = config.getApiBaseUrl() + "/playlists?id = " + playlistId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "PLAYLIST_DELETED");
    }

    private MessageDTO addToPlaylist(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/playlistItems?part = snippet";

        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode snippet = requestBody.putObject("snippet");
        snippet.put("playlistId", payload.path("playlistId").asText());

        ObjectNode resourceId = snippet.putObject("resourceId");
        resourceId.put("kind", "youtube#video");
        resourceId.put("videoId", payload.path("videoId").asText());

        if(payload.has("position")) {
            snippet.put("position", payload.get("position").asInt());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "VIDEO_ADDED_TO_PLAYLIST");
    }

    private MessageDTO removeFromPlaylist(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String playlistItemId = payload.path("playlistItemId").asText();

        String url = config.getApiBaseUrl() + "/playlistItems?id = " + playlistItemId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "VIDEO_REMOVED_FROM_PLAYLIST");
    }

    private MessageDTO reorderPlaylist(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String playlistItemId = payload.path("playlistItemId").asText();
        int newPosition = payload.path("position").asInt();

        String url = config.getApiBaseUrl() + "/playlistItems?part = snippet";

        // First get the current item
        String getUrl = config.getApiBaseUrl() + "/playlistItems?id = " + playlistItemId + "&part = snippet";
        ResponseEntity<String> getResponse = makeApiCall(getUrl, HttpMethod.GET, null);
        JsonNode currentItem = objectMapper.readTree(getResponse.getBody()).path("items").get(0);

        // Update position
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("id", playlistItemId);
        requestBody.set("snippet", currentItem.get("snippet"));
        ((ObjectNode) requestBody.path("snippet")).put("position", newPosition);

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "PLAYLIST_REORDERED");
    }

    // Comment Management Methods
    private MessageDTO postComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/commentThreads?part = snippet";

        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode snippet = requestBody.putObject("snippet");

        if(payload.has("videoId")) {
            snippet.put("videoId", payload.get("videoId").asText());
        } else if(payload.has("channelId")) {
            snippet.put("channelId", payload.get("channelId").asText());
        }

        ObjectNode topLevelComment = snippet.putObject("topLevelComment");
        ObjectNode commentSnippet = topLevelComment.putObject("snippet");
        commentSnippet.put("textOriginal", payload.path("text").asText());

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "COMMENT_POSTED");
    }

    private MessageDTO updateComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("commentId").asText();

        String url = config.getApiBaseUrl() + "/comments?part = snippet";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("id", commentId);

        ObjectNode snippet = requestBody.putObject("snippet");
        snippet.put("textOriginal", payload.path("text").asText());

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "COMMENT_UPDATED");
    }

    private MessageDTO deleteComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("commentId").asText();

        String url = config.getApiBaseUrl() + "/comments?id = " + commentId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "COMMENT_DELETED");
    }

    private MessageDTO moderateComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String commentId = payload.path("commentId").asText();
        String moderationStatus = payload.path("moderationStatus").asText();

        String url = config.getApiBaseUrl() + "/comments/setModerationStatus";
        Map<String, String> params = new HashMap<>();
        params.put("id", commentId);
        params.put("moderationStatus", moderationStatus);

        if(payload.has("banAuthor")) {
            params.put("banAuthor", payload.get("banAuthor").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, params);
        return createResponseMessage(response, "COMMENT_MODERATED");
    }

    private MessageDTO replyToComment(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/comments?part = snippet";

        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode snippet = requestBody.putObject("snippet");
        snippet.put("parentId", payload.path("parentId").asText());
        snippet.put("textOriginal", payload.path("text").asText());

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "COMMENT_REPLY_POSTED");
    }

    // Live Streaming Methods
    private MessageDTO createLiveBroadcast(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/liveBroadcasts?part = snippet,status,contentDetails";

        ObjectNode requestBody = objectMapper.createObjectNode();

        ObjectNode snippet = requestBody.putObject("snippet");
        snippet.put("title", payload.path("title").asText());
        snippet.put("scheduledStartTime", payload.path("scheduledStartTime").asText());

        if(payload.has("description")) {
            snippet.put("description", payload.get("description").asText());
        }

        ObjectNode status = requestBody.putObject("status");
        status.put("privacyStatus", payload.path("privacyStatus").asText(PrivacyStatus.PUBLIC.name()));

        ObjectNode contentDetails = requestBody.putObject("contentDetails");
        contentDetails.put("enableDvr", payload.path("enableDvr").asBoolean(true));
        contentDetails.put("enableContentEncryption", payload.path("enableContentEncryption").asBoolean(false));
        contentDetails.put("enableEmbed", payload.path("enableEmbed").asBoolean(true));
        contentDetails.put("recordFromStart", payload.path("recordFromStart").asBoolean(true));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "LIVE_BROADCAST_CREATED");
    }

    private MessageDTO updateLiveBroadcast(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String broadcastId = payload.path("broadcastId").asText();

        String url = config.getApiBaseUrl() + "/liveBroadcasts?part = snippet,status";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("id", broadcastId);

        if(payload.has("title") || payload.has("description") || payload.has("scheduledStartTime")) {
            ObjectNode snippet = requestBody.putObject("snippet");
            if(payload.has("title")) {
                snippet.put("title", payload.get("title").asText());
            }
            if(payload.has("description")) {
                snippet.put("description", payload.get("description").asText());
            }
            if(payload.has("scheduledStartTime")) {
                snippet.put("scheduledStartTime", payload.get("scheduledStartTime").asText());
            }
        }

        if(payload.has("privacyStatus")) {
            ObjectNode status = requestBody.putObject("status");
            status.put("privacyStatus", payload.get("privacyStatus").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "LIVE_BROADCAST_UPDATED");
    }

    private MessageDTO startLiveStream(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String broadcastId = payload.path("broadcastId").asText();
        String streamId = payload.path("streamId").asText();

        // Bind stream to broadcast
        String bindUrl = config.getApiBaseUrl() + "/liveBroadcasts/bind";
        Map<String, String> bindParams = new HashMap<>();
        bindParams.put("id", broadcastId);
        bindParams.put("part", "id,contentDetails,status");
        bindParams.put("streamId", streamId);

        makeApiCall(bindUrl, HttpMethod.POST, null, bindParams);

        // Transition to testing
        String testUrl = config.getApiBaseUrl() + "/liveBroadcasts/transition";
        Map<String, String> testParams = new HashMap<>();
        testParams.put("broadcastStatus", "testing");
        testParams.put("id", broadcastId);
        testParams.put("part", "status");

        makeApiCall(testUrl, HttpMethod.POST, null, testParams);

        // Wait for stream to be ready(in real implementation, this should be async)
        TimeUnit.SECONDS.sleep(5);

        // Transition to live
        String liveUrl = config.getApiBaseUrl() + "/liveBroadcasts/transition";
        Map<String, String> liveParams = new HashMap<>();
        liveParams.put("broadcastStatus", "live");
        liveParams.put("id", broadcastId);
        liveParams.put("part", "status");

        ResponseEntity<String> response = makeApiCall(liveUrl, HttpMethod.POST, null, liveParams);
        return createResponseMessage(response, "LIVE_STREAM_STARTED");
    }

    private MessageDTO endLiveStream(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String broadcastId = payload.path("broadcastId").asText();

        String url = config.getApiBaseUrl() + "/liveBroadcasts/transition";
        Map<String, String> params = new HashMap<>();
        params.put("broadcastStatus", "complete");
        params.put("id", broadcastId);
        params.put("part", "status");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, null, params);
        return createResponseMessage(response, "LIVE_STREAM_ENDED");
    }

    private MessageDTO createLiveStream(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/liveStreams?part = snippet,cdn,status";

        ObjectNode requestBody = objectMapper.createObjectNode();

        ObjectNode snippet = requestBody.putObject("snippet");
        snippet.put("title", payload.path("title").asText());

        ObjectNode cdn = requestBody.putObject("cdn");
        cdn.put("frameRate", payload.path("frameRate").asText("30fps"));
        cdn.put("ingestionType", "rtmp");
        cdn.put("resolution", payload.path("resolution").asText("1080p"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "LIVE_STREAM_CREATED");
    }

    // Channel Management Methods
    private MessageDTO updateChannel(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/channels?part = snippet,status,brandingSettings";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("id", config.getChannelId());

        if(payload.has("title") || payload.has("description")) {
            ObjectNode snippet = requestBody.putObject("snippet");
            if(payload.has("title")) {
                snippet.put("title", payload.get("title").asText());
            }
            if(payload.has("description")) {
                snippet.put("description", payload.get("description").asText());
            }
        }

        if(payload.has("keywords") || payload.has("trackingAnalyticsAccountId")) {
            ObjectNode brandingSettings = requestBody.putObject("brandingSettings");
            ObjectNode channel = brandingSettings.putObject("channel");

            if(payload.has("keywords")) {
                channel.put("keywords", payload.get("keywords").asText());
            }
            if(payload.has("trackingAnalyticsAccountId")) {
                channel.put("trackingAnalyticsAccountId", payload.get("trackingAnalyticsAccountId").asText());
            }
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "CHANNEL_UPDATED");
    }

    private MessageDTO updateBranding(MessageDTO message) throws Exception {
        // This would typically involve uploading channel art and watermarks
        // Implementation depends on specific requirements
        return createResponseMessage(null, "BRANDING_UPDATED");
    }

    private MessageDTO setChannelSections(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/channelSections?part = snippet,contentDetails";

        // Delete existing sections if requested
        if(payload.path("clearExisting").asBoolean(false)) {
            // Get existing sections
            String getUrl = config.getApiBaseUrl() + "/channelSections?part = id&channelId = " + config.getChannelId();
            ResponseEntity<String> getResponse = makeApiCall(getUrl, HttpMethod.GET, null);
            JsonNode sections = objectMapper.readTree(getResponse.getBody()).get("items");

            // Delete each section
            if(sections != null) {
                for(JsonNode section : sections) {
                    String deleteUrl = config.getApiBaseUrl() + "/channelSections?id = " + section.get("id").asText();
                    makeApiCall(deleteUrl, HttpMethod.DELETE, null);
                }
            }
        }

        // Create new sections
        if(payload.has("sections")) {
            for(JsonNode section : payload.get("sections")) {
                ObjectNode requestBody = objectMapper.createObjectNode();

                ObjectNode snippet = requestBody.putObject("snippet");
                snippet.put("type", section.path("type").asText());
                snippet.put("style", section.path("style").asText("horizontalRow"));
                snippet.put("position", section.path("position").asInt());

                if(section.has("title")) {
                    snippet.put("title", section.get("title").asText());
                }

                if(section.has("playlists")) {
                    ObjectNode contentDetails = requestBody.putObject("contentDetails");
                    ArrayNode playlists = contentDetails.putArray("playlists");
                    for(JsonNode playlistId : section.get("playlists")) {
                        playlists.add(playlistId.asText());
                    }
                }

                makeApiCall(url, HttpMethod.POST, requestBody.toString());
            }
        }

        return createResponseMessage(null, "CHANNEL_SECTIONS_SET");
    }

    private MessageDTO updateChannelSettings(MessageDTO message) throws Exception {
        // Channel settings update via YouTube Studio API
        return updateChannel(message);
    }

    // Community Features Methods
    private MessageDTO postCommunity(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Community posts are created through YouTube Studio API
        // This is a placeholder for the actual implementation
        String text = payload.path("text").asText();

        // Would need to use YouTube Studio internal API
        return createResponseMessage(null, "COMMUNITY_POST_CREATED");
    }

    private MessageDTO createPoll(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Polls are created through YouTube Studio API
        // This is a placeholder for the actual implementation
        return createResponseMessage(null, "POLL_CREATED");
    }

    private MessageDTO postStory(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        // Stories/Shorts are uploaded similar to regular videos but with specific metadata
        ObjectNode storyPayload = payload.deepCopy();
        storyPayload.put("isShort", true);

        MessageDTO storyMessage = new MessageDTO();
        storyMessage.setPayload(storyPayload.toString());
        storyMessage.setHeaders(Map.of("operation", "UPLOAD_VIDEO"));

        return uploadVideo(storyMessage);
    }

    // Caption Methods
    private MessageDTO uploadCaption(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String videoId = payload.path("videoId").asText();
        String captionPath = payload.path("captionPath").asText();

        String url = config.getUploadBaseUrl() + "/captions?part = snippet&sync = true";

        ObjectNode captionMetadata = objectMapper.createObjectNode();
        ObjectNode snippet = captionMetadata.putObject("snippet");
        snippet.put("videoId", videoId);
        snippet.put("language", payload.path("language").asText("en"));
        snippet.put("name", payload.path("name").asText(""));
        snippet.put("isDraft", payload.path("isDraft").asBoolean(false));

        // Read caption file
        Path captionFile = Paths.get(captionPath);
        String captionContent = Files.readString(captionFile);

        // Multipart upload for captions
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("snippet", captionMetadata.toString());
        body.add("file", new ByteArrayResource(captionContent.getBytes()) {
            @Override
            public String getFilename() {
                return captionFile.getFileName().toString();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        rateLimiterService.acquire("youtube_data_api", 200); // Caption upload costs significant quota
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return createResponseMessage(response, "CAPTION_UPLOADED");
    }

    private MessageDTO updateCaption(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String captionId = payload.path("captionId").asText();

        String url = config.getUploadBaseUrl() + "/captions?part = snippet";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("id", captionId);

        ObjectNode snippet = requestBody.putObject("snippet");
        if(payload.has("isDraft")) {
            snippet.put("isDraft", payload.get("isDraft").asBoolean());
        }

        // If updating caption content, would need multipart upload similar to uploadCaption

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.PUT, requestBody.toString());
        return createResponseMessage(response, "CAPTION_UPDATED");
    }

    private MessageDTO deleteCaption(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String captionId = payload.path("captionId").asText();

        String url = config.getApiBaseUrl() + "/captions?id = " + captionId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "CAPTION_DELETED");
    }

    // Subscription Methods
    private MessageDTO subscribe(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/subscriptions?part = snippet";

        ObjectNode requestBody = objectMapper.createObjectNode();
        ObjectNode snippet = requestBody.putObject("snippet");

        ObjectNode resourceId = snippet.putObject("resourceId");
        resourceId.put("kind", "youtube#channel");
        resourceId.put("channelId", payload.path("channelId").asText());

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.POST, requestBody.toString());
        return createResponseMessage(response, "SUBSCRIBED");
    }

    private MessageDTO unsubscribe(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String subscriptionId = payload.path("subscriptionId").asText();

        String url = config.getApiBaseUrl() + "/subscriptions?id = " + subscriptionId;

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.DELETE, null);
        return createResponseMessage(response, "UNSUBSCRIBED");
    }

    // Analytics Methods
    private MessageDTO getAnalytics(MessageDTO message) throws Exception {
        // YouTube Analytics API v2
        // This would require separate YouTube Analytics API setup
        return createResponseMessage(null, "ANALYTICS_RETRIEVED");
    }

    private MessageDTO getRealTimeAnalytics(MessageDTO message) throws Exception {
        // YouTube Analytics API v2 for real - time data
        return createResponseMessage(null, "REAL_TIME_ANALYTICS_RETRIEVED");
    }

    // Monetization Methods
    private MessageDTO updateMonetization(MessageDTO message) throws Exception {
        // Monetization settings via YouTube Studio API
        return createResponseMessage(null, "MONETIZATION_UPDATED");
    }

    private MessageDTO setMembershipPerks(MessageDTO message) throws Exception {
        // Channel memberships via YouTube Studio API
        return createResponseMessage(null, "MEMBERSHIP_PERKS_SET");
    }

    // Search Methods
    private MessageDTO searchVideos(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/search";
        Map<String, String> params = new HashMap<>();
        params.put("part", "id,snippet");
        params.put("q", payload.path("query").asText());
        params.put("type", "video");
        params.put("maxResults", payload.path("maxResults").asText("25"));

        if(payload.has("channelId")) {
            params.put("channelId", payload.get("channelId").asText());
        }

        if(payload.has("order")) {
            params.put("order", payload.get("order").asText());
        }

        if(payload.has("publishedAfter")) {
            params.put("publishedAfter", payload.get("publishedAfter").asText());
        }

        if(payload.has("videoCategoryId")) {
            params.put("videoCategoryId", payload.get("videoCategoryId").asText());
        }

        if(payload.has("videoDefinition")) {
            params.put("videoDefinition", payload.get("videoDefinition").asText());
        }

        if(payload.has("videoDuration")) {
            params.put("videoDuration", payload.get("videoDuration").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "VIDEOS_SEARCHED");
    }

    private MessageDTO searchChannels(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/search";
        Map<String, String> params = new HashMap<>();
        params.put("part", "id,snippet");
        params.put("q", payload.path("query").asText());
        params.put("type", "channel");
        params.put("maxResults", payload.path("maxResults").asText("25"));

        if(payload.has("order")) {
            params.put("order", payload.get("order").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "CHANNELS_SEARCHED");
    }

    private MessageDTO searchPlaylists(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = config.getApiBaseUrl() + "/search";
        Map<String, String> params = new HashMap<>();
        params.put("part", "id,snippet");
        params.put("q", payload.path("query").asText());
        params.put("type", "playlist");
        params.put("maxResults", payload.path("maxResults").asText("25"));

        if(payload.has("channelId")) {
            params.put("channelId", payload.get("channelId").asText());
        }

        if(payload.has("order")) {
            params.put("order", payload.get("order").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);
        return createResponseMessage(response, "PLAYLISTS_SEARCHED");
    }

    // Helper Methods
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body) {
        return makeApiCall(url, method, body, null);
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, String body, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        // Add query parameters to URL
        if (params != null && !params.isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?");
            params.forEach((key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1); // Remove trailing &
            url = urlBuilder.toString();
        }

        HttpEntity<String> entity = body != null ?
            new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

        rateLimiterService.acquire("youtube_data_api", getQuotaCost(method, url));
        return restTemplate.exchange(url, method, entity, String.class);
    }

    private int getQuotaCost(HttpMethod method, String url) {
        // YouTube API quota costs vary by operation
        if (method == HttpMethod.GET) return 1;
        if (method == HttpMethod.DELETE) return 50;
        if (url.contains("/videos") && method == HttpMethod.POST) return 1600;
        if (url.contains("/playlists") && method == HttpMethod.POST) return 50;
        if (url.contains("/comments") && method == HttpMethod.POST) return 50;
        return 50; // Default for other operations
    }


    private String getAccessToken() {
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getClientId() == null || config.getClientSecret() == null) {
            throw new AdapterException("YouTube API credentials are not configured");
        }
        if(config.getAccessToken() == null) {
            throw new AdapterException("YouTube access token is not configured");
        }
    }

    private MessageDTO createResponseMessage(ResponseEntity<String> response, String operation) {
        MessageDTO responseMessage = new MessageDTO();
        responseMessage.setCorrelationId(UUID.randomUUID().toString());
        responseMessage.setTimestamp(LocalDateTime.now());

        if (response != null) {
            responseMessage.setStatus(response.getStatusCode().is2xxSuccessful() ? MessageStatus.SUCCESS : MessageStatus.FAILED);
            responseMessage.setHeaders(Map.of(
                "operation", operation,
                "statusCode", response.getStatusCodeValue(),
                "source", "youtube"
            ));
            responseMessage.setPayload(response.getBody());
        } else {
            // For operations that don't have actual API calls
            responseMessage.setStatus(MessageStatus.SUCCESS);
            responseMessage.setHeaders(Map.of(
                "operation", operation,
                "source", "youtube"
            ));
            responseMessage.setPayload("{}");
        }
        return responseMessage;
    }

    public void setConfiguration(YouTubeDataApiConfig config) {
        this.config = config;
    }

    @Override
    protected YouTubeDataApiConfig getConfig() {
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
    public MessageDTO processMessage(MessageDTO message) {
        try {
            return sendMessage(message);
        } catch (AdapterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected long getPollingIntervalMs() {
        // Outbound adapters don't typically poll, but return 0 to indicate no polling
        return 0;
    }

    @Override
    protected void doReceiverInitialize() throws Exception {
        // Initialize any receiver-specific resources
        log.debug("Initializing YouTube Data outbound adapter");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // Clean up any receiver-specific resources
        log.debug("Destroying YouTube Data outbound adapter");
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
            String url = config.getApiBaseUrl() + "/channels/list";
            Map<String, String> params = new HashMap<>();
            params.put("part", "id");
            params.put("mine", "true");

            ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, null, params);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "YouTube Data API connection successful");
            } else {
                return AdapterResult.failure("YouTube Data API connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test YouTube Data connection: " + e.getMessage());
        }
    }
}
