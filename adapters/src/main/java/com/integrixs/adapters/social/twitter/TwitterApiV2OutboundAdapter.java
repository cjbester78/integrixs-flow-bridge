package com.integrixs.adapters.social.twitter;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.twitter.TwitterApiV2Config.*;
import com.integrixs.core.api.channel.Message;
import com.integrixs.core.exception.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;

@Slf4j
@Component("twitterApiV2OutboundAdapter")
public class TwitterApiV2OutboundAdapter extends AbstractSocialMediaOutboundAdapter<TwitterApiV2Config> {
    
    private static final String TWITTER_API_BASE = "https://api.twitter.com/2";
    private static final String TWITTER_UPLOAD_BASE = "https://upload.twitter.com/1.1";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public TwitterApiV2OutboundAdapter(
            TwitterApiV2Config config,
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
    public Message processMessage(Message message) throws AdapterException {
        validateConfiguration();
        
        String operation = (String) message.getHeaders().get("operation");
        if (operation == null) {
            throw new AdapterException("Operation header is required");
        }
        
        log.debug("Processing Twitter operation: {}", operation);
        
        try {
            rateLimiterService.acquire("twitter_api", 1);
            
            Message response;
            switch (operation.toUpperCase()) {
                // Tweet operations
                case "CREATE_TWEET":
                    response = createTweet(message);
                    break;
                case "CREATE_THREAD":
                    response = createThread(message);
                    break;
                case "DELETE_TWEET":
                    response = deleteTweet(message);
                    break;
                case "LIKE_TWEET":
                    response = likeTweet(message);
                    break;
                case "UNLIKE_TWEET":
                    response = unlikeTweet(message);
                    break;
                case "RETWEET":
                    response = retweet(message);
                    break;
                case "UNRETWEET":
                    response = unretweet(message);
                    break;
                case "REPLY_TO_TWEET":
                    response = replyToTweet(message);
                    break;
                case "QUOTE_TWEET":
                    response = quoteTweet(message);
                    break;
                case "CREATE_POLL":
                    response = createPoll(message);
                    break;
                    
                // Media operations
                case "UPLOAD_MEDIA":
                    response = uploadMedia(message);
                    break;
                case "ADD_ALT_TEXT":
                    response = addAltText(message);
                    break;
                    
                // Timeline operations
                case "GET_HOME_TIMELINE":
                    response = getHomeTimeline(message);
                    break;
                case "GET_USER_TIMELINE":
                    response = getUserTimeline(message);
                    break;
                case "GET_MENTIONS":
                    response = getMentions(message);
                    break;
                case "SEARCH_TWEETS":
                    response = searchTweets(message);
                    break;
                    
                // User operations
                case "FOLLOW_USER":
                    response = followUser(message);
                    break;
                case "UNFOLLOW_USER":
                    response = unfollowUser(message);
                    break;
                case "GET_USER_INFO":
                    response = getUserInfo(message);
                    break;
                case "GET_FOLLOWERS":
                    response = getFollowers(message);
                    break;
                case "GET_FOLLOWING":
                    response = getFollowing(message);
                    break;
                case "BLOCK_USER":
                    response = blockUser(message);
                    break;
                case "UNBLOCK_USER":
                    response = unblockUser(message);
                    break;
                case "MUTE_USER":
                    response = muteUser(message);
                    break;
                case "UNMUTE_USER":
                    response = unmuteUser(message);
                    break;
                    
                // List operations
                case "CREATE_LIST":
                    response = createList(message);
                    break;
                case "UPDATE_LIST":
                    response = updateList(message);
                    break;
                case "DELETE_LIST":
                    response = deleteList(message);
                    break;
                case "ADD_LIST_MEMBER":
                    response = addListMember(message);
                    break;
                case "REMOVE_LIST_MEMBER":
                    response = removeListMember(message);
                    break;
                case "GET_LIST_TWEETS":
                    response = getListTweets(message);
                    break;
                    
                // DM operations
                case "SEND_DIRECT_MESSAGE":
                    response = sendDirectMessage(message);
                    break;
                case "GET_DIRECT_MESSAGES":
                    response = getDirectMessages(message);
                    break;
                    
                // Bookmark operations
                case "BOOKMARK_TWEET":
                    response = bookmarkTweet(message);
                    break;
                case "REMOVE_BOOKMARK":
                    response = removeBookmark(message);
                    break;
                case "GET_BOOKMARKS":
                    response = getBookmarks(message);
                    break;
                    
                // Spaces operations
                case "GET_SPACE":
                    response = getSpace(message);
                    break;
                case "SEARCH_SPACES":
                    response = searchSpaces(message);
                    break;
                    
                // Analytics operations
                case "GET_TWEET_METRICS":
                    response = getTweetMetrics(message);
                    break;
                case "GET_USER_METRICS":
                    response = getUserMetrics(message);
                    break;
                    
                default:
                    throw new AdapterException("Unknown operation: " + operation);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing Twitter message", e);
            Message errorResponse = new Message();
            errorResponse.setMessageId(message.getMessageId());
            errorResponse.setStatus(MessageStatus.ERROR);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "originalOperation", operation
            ));
            return errorResponse;
        }
    }
    
    private Message createTweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/tweets";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());
        
        // Add media if present
        if (payload.has("media_ids")) {
            ObjectNode media = objectMapper.createObjectNode();
            ArrayNode mediaIds = objectMapper.createArrayNode();
            JsonNode mediaIdsNode = payload.get("media_ids");
            if (mediaIdsNode.isArray()) {
                mediaIdsNode.forEach(id -> mediaIds.add(id.asText()));
            }
            media.set("media_ids", mediaIds);
            requestBody.set("media", media);
        }
        
        // Add poll if present
        if (payload.has("poll")) {
            requestBody.set("poll", payload.get("poll"));
        }
        
        // Add reply settings
        if (payload.has("reply_settings")) {
            requestBody.put("reply_settings", payload.path("reply_settings").asText());
        }
        
        // Add geo if present
        if (payload.has("geo")) {
            requestBody.set("geo", payload.get("geo"));
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TWEET_CREATED");
    }
    
    private Message createThread(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ArrayNode tweets = (ArrayNode) payload.path("tweets");
        
        if (tweets.size() == 0 || tweets.size() > config.getLimits().getMaxThreadLength()) {
            throw new AdapterException("Thread must have between 1 and " + 
                config.getLimits().getMaxThreadLength() + " tweets");
        }
        
        ArrayNode threadIds = objectMapper.createArrayNode();
        String previousTweetId = null;
        
        for (JsonNode tweetData : tweets) {
            ObjectNode tweetRequest = objectMapper.createObjectNode();
            tweetRequest.put("text", tweetData.path("text").asText());
            
            // Add reply to previous tweet in thread
            if (previousTweetId != null) {
                ObjectNode reply = objectMapper.createObjectNode();
                reply.put("in_reply_to_tweet_id", previousTweetId);
                tweetRequest.set("reply", reply);
            }
            
            // Add media if present
            if (tweetData.has("media_ids")) {
                tweetRequest.set("media", tweetData.get("media"));
            }
            
            String url = TWITTER_API_BASE + "/tweets";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getBearerToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(tweetRequest.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                previousTweetId = responseData.path("data").path("id").asText();
                threadIds.add(previousTweetId);
            } else {
                throw new AdapterException("Failed to create tweet in thread");
            }
        }
        
        ObjectNode threadResponse = objectMapper.createObjectNode();
        threadResponse.set("thread_ids", threadIds);
        
        return createSuccessResponse(message.getMessageId(), threadResponse.toString(), "THREAD_CREATED");
    }
    
    private Message replyToTweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/tweets";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());
        
        ObjectNode reply = objectMapper.createObjectNode();
        reply.put("in_reply_to_tweet_id", payload.path("in_reply_to_tweet_id").asText());
        requestBody.set("reply", reply);
        
        // Add media if present
        if (payload.has("media_ids")) {
            requestBody.set("media", payload.get("media"));
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "REPLY_CREATED");
    }
    
    private Message quoteTweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/tweets";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());
        requestBody.put("quote_tweet_id", payload.path("quote_tweet_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "QUOTE_TWEET_CREATED");
    }
    
    private Message createPoll(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/tweets";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());
        
        ObjectNode poll = objectMapper.createObjectNode();
        ArrayNode options = objectMapper.createArrayNode();
        JsonNode optionsNode = payload.path("poll_options");
        if (optionsNode.isArray()) {
            optionsNode.forEach(option -> options.add(option.asText()));
        }
        poll.set("options", options);
        poll.put("duration_minutes", payload.path("duration_minutes").asInt(1440)); // Default 24 hours
        requestBody.set("poll", poll);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "POLL_CREATED");
    }
    
    private Message deleteTweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String tweetId = payload.path("tweet_id").asText();
        
        String url = TWITTER_API_BASE + "/tweets/" + tweetId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TWEET_DELETED");
    }
    
    private Message likeTweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/likes";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("tweet_id", payload.path("tweet_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TWEET_LIKED");
    }
    
    private Message unlikeTweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        String tweetId = payload.path("tweet_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/likes/" + tweetId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TWEET_UNLIKED");
    }
    
    private Message retweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/retweets";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("tweet_id", payload.path("tweet_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "RETWEETED");
    }
    
    private Message unretweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        String tweetId = payload.path("tweet_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/retweets/" + tweetId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "UNRETWEETED");
    }
    
    private Message uploadMedia(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        byte[] mediaData = Base64.getDecoder().decode(payload.path("media_data").asText());
        String mediaType = payload.path("media_type").asText();
        String mediaCategory = getMediaCategory(mediaType);
        
        // Step 1: Initialize upload
        String initUrl = TWITTER_UPLOAD_BASE + "/media/upload.json";
        
        MultiValueMap<String, String> initParams = new LinkedMultiValueMap<>();
        initParams.add("command", "INIT");
        initParams.add("total_bytes", String.valueOf(mediaData.length));
        initParams.add("media_type", mediaType);
        initParams.add("media_category", mediaCategory);
        
        HttpHeaders initHeaders = new HttpHeaders();
        initHeaders.setBearerAuth(getBearerToken());
        initHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> initRequest = new HttpEntity<>(initParams, initHeaders);
        ResponseEntity<String> initResponse = restTemplate.postForEntity(initUrl, initRequest, String.class);
        
        JsonNode initData = objectMapper.readTree(initResponse.getBody());
        String mediaId = initData.path("media_id_string").asText();
        
        // Step 2: Upload chunks
        int chunkSize = 5 * 1024 * 1024; // 5MB chunks
        int totalChunks = (int) Math.ceil((double) mediaData.length / chunkSize);
        
        for (int i = 0; i < totalChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, mediaData.length);
            byte[] chunk = Arrays.copyOfRange(mediaData, start, end);
            
            MultiValueMap<String, Object> appendParams = new LinkedMultiValueMap<>();
            appendParams.add("command", "APPEND");
            appendParams.add("media_id", mediaId);
            appendParams.add("segment_index", String.valueOf(i));
            
            Resource mediaResource = new ByteArrayResource(chunk) {
                @Override
                public String getFilename() {
                    return "media";
                }
            };
            appendParams.add("media", mediaResource);
            
            HttpHeaders appendHeaders = new HttpHeaders();
            appendHeaders.setBearerAuth(getBearerToken());
            appendHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            HttpEntity<MultiValueMap<String, Object>> appendRequest = new HttpEntity<>(appendParams, appendHeaders);
            restTemplate.postForEntity(initUrl, appendRequest, String.class);
        }
        
        // Step 3: Finalize upload
        MultiValueMap<String, String> finalizeParams = new LinkedMultiValueMap<>();
        finalizeParams.add("command", "FINALIZE");
        finalizeParams.add("media_id", mediaId);
        
        HttpHeaders finalizeHeaders = new HttpHeaders();
        finalizeHeaders.setBearerAuth(getBearerToken());
        finalizeHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> finalizeRequest = new HttpEntity<>(finalizeParams, finalizeHeaders);
        ResponseEntity<String> finalizeResponse = restTemplate.postForEntity(initUrl, finalizeRequest, String.class);
        
        return createSuccessResponse(message.getMessageId(), finalizeResponse.getBody(), "MEDIA_UPLOADED");
    }
    
    private Message addAltText(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_UPLOAD_BASE + "/media/metadata/create.json";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("media_id", payload.path("media_id").asText());
        
        ObjectNode altText = objectMapper.createObjectNode();
        altText.put("text", payload.path("alt_text").asText());
        requestBody.set("alt_text", altText);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "ALT_TEXT_ADDED");
    }
    
    private Message getHomeTimeline(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/timelines/reverse_chronological";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics,entities,attachments");
        params.put("user.fields", "id,name,username,profile_image_url,verified");
        params.put("media.fields", "type,url,preview_image_url,duration_ms");
        params.put("expansions", "author_id,attachments.media_keys,referenced_tweets.id");
        
        if (payload.has("pagination_token")) {
            params.put("pagination_token", payload.path("pagination_token").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "HOME_TIMELINE_RETRIEVED");
    }
    
    private Message getUserTimeline(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/tweets";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,public_metrics,entities,attachments");
        params.put("media.fields", "type,url,preview_image_url");
        params.put("expansions", "attachments.media_keys");
        
        if (payload.has("exclude")) {
            params.put("exclude", payload.path("exclude").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_TIMELINE_RETRIEVED");
    }
    
    private Message getMentions(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/mentions";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,conversation_id,public_metrics");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("expansions", "author_id");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "MENTIONS_RETRIEVED");
    }
    
    private Message searchTweets(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/tweets/search/recent";
        
        Map<String, String> params = new HashMap<>();
        params.put("query", payload.path("query").asText());
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics,entities,attachments");
        params.put("user.fields", "id,name,username,profile_image_url,verified");
        params.put("media.fields", "type,url,preview_image_url");
        params.put("expansions", "author_id,attachments.media_keys");
        
        if (payload.has("start_time")) {
            params.put("start_time", payload.path("start_time").asText());
        }
        if (payload.has("end_time")) {
            params.put("end_time", payload.path("end_time").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TWEETS_SEARCHED");
    }
    
    private Message followUser(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + sourceUserId + "/following";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("target_user_id", payload.path("target_user_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_FOLLOWED");
    }
    
    private Message unfollowUser(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        String targetUserId = payload.path("target_user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + sourceUserId + "/following/" + targetUserId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_UNFOLLOWED");
    }
    
    private Message getUserInfo(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId;
        
        Map<String, String> params = new HashMap<>();
        params.put("user.fields", "created_at,description,entities,id,location,name,pinned_tweet_id," +
                                 "profile_image_url,protected,public_metrics,url,username,verified,verified_type");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_INFO_RETRIEVED");
    }
    
    private Message getFollowers(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/followers";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("1000"));
        params.put("user.fields", "id,name,username,profile_image_url,description,public_metrics");
        
        if (payload.has("pagination_token")) {
            params.put("pagination_token", payload.path("pagination_token").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "FOLLOWERS_RETRIEVED");
    }
    
    private Message getFollowing(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/following";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("1000"));
        params.put("user.fields", "id,name,username,profile_image_url,description,public_metrics");
        
        if (payload.has("pagination_token")) {
            params.put("pagination_token", payload.path("pagination_token").asText());
        }
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "FOLLOWING_RETRIEVED");
    }
    
    // Additional user operations
    private Message blockUser(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + sourceUserId + "/blocking";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("target_user_id", payload.path("target_user_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_BLOCKED");
    }
    
    private Message unblockUser(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        String targetUserId = payload.path("target_user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + sourceUserId + "/blocking/" + targetUserId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_UNBLOCKED");
    }
    
    private Message muteUser(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + sourceUserId + "/muting";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("target_user_id", payload.path("target_user_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_MUTED");
    }
    
    private Message unmuteUser(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        String targetUserId = payload.path("target_user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + sourceUserId + "/muting/" + targetUserId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_UNMUTED");
    }
    
    // List operations
    private Message createList(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/lists";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", payload.path("name").asText());
        if (payload.has("description")) {
            requestBody.put("description", payload.path("description").asText());
        }
        if (payload.has("private")) {
            requestBody.put("private", payload.path("private").asBoolean());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LIST_CREATED");
    }
    
    private Message updateList(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();
        
        String url = TWITTER_API_BASE + "/lists/" + listId;
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        if (payload.has("name")) {
            requestBody.put("name", payload.path("name").asText());
        }
        if (payload.has("description")) {
            requestBody.put("description", payload.path("description").asText());
        }
        if (payload.has("private")) {
            requestBody.put("private", payload.path("private").asBoolean());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LIST_UPDATED");
    }
    
    private Message deleteList(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();
        
        String url = TWITTER_API_BASE + "/lists/" + listId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LIST_DELETED");
    }
    
    private Message addListMember(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();
        
        String url = TWITTER_API_BASE + "/lists/" + listId + "/members";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("user_id", payload.path("user_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LIST_MEMBER_ADDED");
    }
    
    private Message removeListMember(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/lists/" + listId + "/members/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LIST_MEMBER_REMOVED");
    }
    
    private Message getListTweets(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();
        
        String url = TWITTER_API_BASE + "/lists/" + listId + "/tweets";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("expansions", "author_id");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "LIST_TWEETS_RETRIEVED");
    }
    
    // DM operations
    private Message sendDirectMessage(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/dm_conversations/with/" + 
                    payload.path("participant_id").asText() + "/messages";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());
        
        // Add media if present
        if (payload.has("attachments")) {
            requestBody.set("attachments", payload.get("attachments"));
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "DIRECT_MESSAGE_SENT");
    }
    
    private Message getDirectMessages(Message message) throws Exception {
        String url = TWITTER_API_BASE + "/dm_events";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", "100");
        params.put("dm_event.fields", "id,text,created_at,sender_id,attachments");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("media.fields", "type,url,preview_image_url");
        params.put("expansions", "sender_id,attachments.media_keys");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "DIRECT_MESSAGES_RETRIEVED");
    }
    
    // Bookmark operations
    private Message bookmarkTweet(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/bookmarks";
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("tweet_id", payload.path("tweet_id").asText());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TWEET_BOOKMARKED");
    }
    
    private Message removeBookmark(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        String tweetId = payload.path("tweet_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/bookmarks/" + tweetId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "BOOKMARK_REMOVED");
    }
    
    private Message getBookmarks(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId + "/bookmarks";
        
        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("expansions", "author_id");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "BOOKMARKS_RETRIEVED");
    }
    
    // Spaces operations
    private Message getSpace(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String spaceId = payload.path("space_id").asText();
        
        String url = TWITTER_API_BASE + "/spaces/" + spaceId;
        
        Map<String, String> params = new HashMap<>();
        params.put("space.fields", "host_ids,created_at,creator_id,id,lang,invited_user_ids," +
                                   "participant_count,speaker_ids,started_at,state,title,topic_ids");
        params.put("expansions", "host_ids,speaker_ids");
        params.put("user.fields", "id,name,username,profile_image_url");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "SPACE_RETRIEVED");
    }
    
    private Message searchSpaces(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        
        String url = TWITTER_API_BASE + "/spaces/search";
        
        Map<String, String> params = new HashMap<>();
        params.put("query", payload.path("query").asText());
        params.put("state", payload.path("state").asText("all"));
        params.put("max_results", payload.path("max_results").asText("100"));
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "SPACES_SEARCHED");
    }
    
    // Analytics operations
    private Message getTweetMetrics(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String tweetId = payload.path("tweet_id").asText();
        
        String url = TWITTER_API_BASE + "/tweets/" + tweetId;
        
        Map<String, String> params = new HashMap<>();
        params.put("tweet.fields", "public_metrics,organic_metrics,promoted_metrics");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "TWEET_METRICS_RETRIEVED");
    }
    
    private Message getUserMetrics(Message message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        
        String url = TWITTER_API_BASE + "/users/" + userId;
        
        Map<String, String> params = new HashMap<>();
        params.put("user.fields", "public_metrics");
        
        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);
        
        return createSuccessResponse(message.getMessageId(), response.getBody(), "USER_METRICS_RETRIEVED");
    }
    
    // Helper methods
    private String getMediaCategory(String mediaType) {
        if (mediaType.startsWith("image/")) {
            return "tweet_image";
        } else if (mediaType.startsWith("video/")) {
            return "tweet_video";
        } else if (mediaType.equals("image/gif")) {
            return "tweet_gif";
        }
        return "tweet_image";
    }
    
    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        StringBuilder urlBuilder = new StringBuilder(url);
        if (!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) -> {
                try {
                    urlBuilder.append(key).append("=")
                             .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()))
                             .append("&");
                } catch (Exception e) {
                    log.error("Error encoding parameter", e);
                }
            });
            urlBuilder.setLength(urlBuilder.length() - 1);
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);
    }
    
    private Message createSuccessResponse(String messageId, String responseBody, String operation) {
        Message response = new Message();
        response.setMessageId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setTimestamp(Instant.now());
        response.setHeaders(Map.of(
            "operation", operation,
            "source", "twitter"
        ));
        response.setPayload(responseBody);
        return response;
    }
    
    private String getBearerToken() {
        if (config.getBearerToken() != null) {
            return credentialEncryptionService.decrypt(config.getBearerToken());
        }
        // If no bearer token, use OAuth 2.0 access token
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }
    
    private void validateConfiguration() throws AdapterException {
        if (config == null) {
            throw new AdapterException("Twitter configuration is not set");
        }
        if (config.getBearerToken() == null && config.getAccessToken() == null) {
            throw new AdapterException("Bearer token or access token is required");
        }
    }
}