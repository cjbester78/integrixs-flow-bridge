package com.integrixs.adapters.social.twitter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
// Import specific enums to avoid wildcard import conflicts
import com.integrixs.adapters.social.twitter.TwitterApiV2Config.TweetType;
import com.integrixs.adapters.social.twitter.TwitterApiV2Config.TimelineType;
import com.integrixs.adapters.social.twitter.TwitterApiV2Config.Expansion;
import com.integrixs.adapters.social.twitter.TwitterApiV2Config.TweetField;
import com.integrixs.adapters.social.twitter.TwitterApiV2Config.UserField;
import com.integrixs.adapters.social.twitter.TwitterApiV2Config.MediaField;
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
import java.time.LocalDateTime;

@Component("twitterApiV2OutboundAdapter")
public class TwitterApiV2OutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(TwitterApiV2OutboundAdapter.class);


    @Value("${integrixs.adapters.twitter.api.api-base-url:https://api.twitter.com/2}")
    private String twitterApiBase;

    @Value("${integrixs.adapters.twitter.api.upload-base-url:https://upload.twitter.com/1.1}")
    private String twitterUploadBase;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    private TwitterApiV2Config config;

    private final RateLimiterService rateLimiterService;
    private final OAuth2TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;

    @Autowired
    public TwitterApiV2OutboundAdapter(
            TwitterApiV2Config config,
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

    public MessageDTO processMessage(MessageDTO message) {
        try {
            validateConfiguration();
        } catch (AdapterException e) {
            return createErrorResponse(message, e.getMessage());
        }

        String operation = (String) message.getHeaders().get("operation");
        if(operation == null) {
            return createErrorResponse(message, "Operation header is required");
        }

        log.debug("Processing Twitter operation: {}", operation);

        try {
            return switch(operation.toUpperCase()) {
                case "CREATE_TWEET" -> createTweet(message);
                case "REPLY_TO_TWEET" -> replyToTweet(message);
                case "RETWEET" -> retweet(message);
                case "QUOTE_TWEET" -> quoteTweet(message);
                case "LIKE_TWEET" -> likeTweet(message);
                case "UNLIKE_TWEET" -> unlikeTweet(message);
                case "DELETE_TWEET" -> deleteTweet(message);
                case "CREATE_THREAD" -> createThread(message);
                case "CREATE_POLL" -> createPoll(message);
                case "FOLLOW_USER" -> followUser(message);
                case "UNFOLLOW_USER" -> unfollowUser(message);
                case "GET_TWEET" -> getTweet(message);
                case "GET_USER" -> getUser(message);
                case "SEARCH_TWEETS" -> searchTweets(message);
                case "GET_TIMELINE" -> getTimeline(message);
                case "GET_MENTIONS" -> getMentions(message);
                case "GET_FOLLOWERS" -> getFollowers(message);
                case "GET_FOLLOWING" -> getFollowing(message);
                case "UPDATE_PROFILE" -> updateProfile(message);
                case "UPLOAD_MEDIA" -> uploadMedia(message);
                default -> throw new AdapterException("Unsupported operation: " + operation);
            };
        } catch(Exception e) {
            log.error("Error processing Twitter operation: {}", operation, e);
            MessageDTO errorResponse = new MessageDTO();
            errorResponse.setCorrelationId(message.getCorrelationId());
            errorResponse.setStatus(MessageStatus.FAILED);
            errorResponse.setHeaders(Map.of(
                "error", e.getMessage(),
                "originalOperation", operation
           ));
            return errorResponse;
        }
    }

    private MessageDTO createTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/tweets";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());

        // Add media if present
        if(payload.has("media_ids")) {
            ObjectNode media = objectMapper.createObjectNode();
            ArrayNode mediaIds = objectMapper.createArrayNode();
            JsonNode mediaIdsNode = payload.get("media_ids");
            if(mediaIdsNode.isArray()) {
                mediaIdsNode.forEach(id -> mediaIds.add(id.asText()));
            }
            media.set("media_ids", mediaIds);
            requestBody.set("media", media);
        }

        // Add poll if present
        if(payload.has("poll")) {
            requestBody.set("poll", payload.get("poll"));
        }

        // Add reply settings
        if(payload.has("reply_settings")) {
            requestBody.put("reply_settings", payload.path("reply_settings").asText());
        }

        // Add geo if present
        if(payload.has("geo")) {
            requestBody.set("geo", payload.get("geo"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEET_CREATED");
    }

    private MessageDTO createThread(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        ArrayNode tweets = (ArrayNode) payload.path("tweets");

        if(tweets.size() == 0 || tweets.size() > config.getLimits().getMaxThreadLength()) {
            throw new AdapterException("Thread must have between 1 and " +
                config.getLimits().getMaxThreadLength() + " tweets");
        }

        ArrayNode threadIds = objectMapper.createArrayNode();
        String previousTweetId = null;

        for(JsonNode tweetData : tweets) {
            ObjectNode tweetRequest = objectMapper.createObjectNode();
            tweetRequest.put("text", tweetData.path("text").asText());

            // Add reply relationship if this is not the first tweet
            if(previousTweetId != null) {
                ObjectNode reply = objectMapper.createObjectNode();
                reply.put("in_reply_to_tweet_id", previousTweetId);
                tweetRequest.set("reply", reply);
            }

            // Add media if present
            if(tweetData.has("media_ids")) {
                ObjectNode media = objectMapper.createObjectNode();
                media.set("media_ids", tweetData.get("media_ids"));
                tweetRequest.set("media", media);
            }

            String url = twitterApiBase + "/tweets";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getBearerToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(tweetRequest.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseData = objectMapper.readTree(response.getBody());
                previousTweetId = responseData.path("data").path("id").asText();
                threadIds.add(previousTweetId);
            } else {
                throw new AdapterException("Failed to create tweet in thread");
            }
        }

        ObjectNode threadResponse = objectMapper.createObjectNode();
        threadResponse.set("thread_ids", threadIds);

        return createSuccessResponse(message.getCorrelationId(), threadResponse.toString(), "THREAD_CREATED");
    }

    private MessageDTO replyToTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/tweets";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());

        ObjectNode reply = objectMapper.createObjectNode();
        reply.put("in_reply_to_tweet_id", payload.path("in_reply_to_tweet_id").asText());
        requestBody.set("reply", reply);

        // Add media if present
        if(payload.has("media_ids")) {
            requestBody.set("media", payload.get("media"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "REPLY_CREATED");
    }

    private MessageDTO quoteTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/tweets";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());
        requestBody.put("quote_tweet_id", payload.path("quote_tweet_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "QUOTE_TWEET_CREATED");
    }

    private MessageDTO createPoll(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/tweets";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());

        ObjectNode poll = objectMapper.createObjectNode();
        ArrayNode options = objectMapper.createArrayNode();
        JsonNode optionsNode = payload.path("poll_options");
        if(optionsNode.isArray()) {
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

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "POLL_CREATED");
    }

    private MessageDTO deleteTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String tweetId = payload.path("tweet_id").asText();

        String url = twitterApiBase + "/tweets/" + tweetId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEET_DELETED");
    }

    private MessageDTO likeTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/likes";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("tweet_id", payload.path("tweet_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEET_LIKED");
    }

    private MessageDTO unlikeTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        String tweetId = payload.path("tweet_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/likes/" + tweetId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEET_UNLIKED");
    }

    private MessageDTO retweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/retweets";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("tweet_id", payload.path("tweet_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "RETWEETED");
    }

    private MessageDTO unretweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        String tweetId = payload.path("tweet_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/retweets/" + tweetId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "UNRETWEETED");
    }

    private MessageDTO uploadMedia(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        byte[] mediaData = Base64.getDecoder().decode(payload.path("media_data").asText());
        String mediaType = payload.path("media_type").asText();
        String mediaCategory = getMediaCategory(mediaType);

        // Step 1: Initialize upload
        String initUrl = twitterUploadBase + "/media/upload.json";

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

        for(int i = 0; i < totalChunks; i++) {
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

        return createSuccessResponse(message.getCorrelationId(), finalizeResponse.getBody(), "MEDIA_UPLOADED");
    }

    private MessageDTO addAltText(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterUploadBase + "/media/metadata/create.json";

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

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "ALT_TEXT_ADDED");
    }

    private MessageDTO getHomeTimeline(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/timelines/reverse_chronological";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics,entities,attachments");
        params.put("user.fields", "id,name,username,profile_image_url,verified");
        params.put("media.fields", "type,url,preview_image_url,duration_ms");
        params.put("expansions", "author_id,attachments.media_keys,referenced_tweets.id");

        if(payload.has("pagination_token")) {
            params.put("pagination_token", payload.path("pagination_token").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "HOME_TIMELINE_RETRIEVED");
    }

    private MessageDTO getUserTimeline(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/tweets";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,public_metrics,entities,attachments");
        params.put("media.fields", "type,url,preview_image_url");
        params.put("expansions", "attachments.media_keys");

        if(payload.has("exclude")) {
            params.put("exclude", payload.path("exclude").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_TIMELINE_RETRIEVED");
    }

    private MessageDTO getMentions(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/mentions";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,conversation_id,public_metrics");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("expansions", "author_id");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "MENTIONS_RETRIEVED");
    }

    private MessageDTO searchTweets(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/tweets/search/recent";

        Map<String, String> params = new HashMap<>();
        params.put("query", payload.path("query").asText());
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics,entities,attachments");
        params.put("user.fields", "id,name,username,profile_image_url,verified");
        params.put("media.fields", "type,url,preview_image_url");
        params.put("expansions", "author_id,attachments.media_keys");

        if(payload.has("start_time")) {
            params.put("start_time", payload.path("start_time").asText());
        }
        if(payload.has("end_time")) {
            params.put("end_time", payload.path("end_time").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEETS_SEARCHED");
    }

    private MessageDTO followUser(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();

        String url = twitterApiBase + "/users/" + sourceUserId + "/following";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("target_user_id", payload.path("target_user_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_FOLLOWED");
    }

    private MessageDTO unfollowUser(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        String targetUserId = payload.path("target_user_id").asText();

        String url = twitterApiBase + "/users/" + sourceUserId + "/following/" + targetUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_UNFOLLOWED");
    }

    private MessageDTO getUserInfo(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId;

        Map<String, String> params = new HashMap<>();
        params.put("user.fields", "created_at,description,entities,id,location,name,pinned_tweet_id," +
                                 "profile_image_url,protected,public_metrics,url,username,verified,verified_type");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_INFO_RETRIEVED");
    }

    private MessageDTO getFollowers(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/followers";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("1000"));
        params.put("user.fields", "id,name,username,profile_image_url,description,public_metrics");

        if(payload.has("pagination_token")) {
            params.put("pagination_token", payload.path("pagination_token").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "FOLLOWERS_RETRIEVED");
    }

    private MessageDTO getFollowing(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/following";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("1000"));
        params.put("user.fields", "id,name,username,profile_image_url,description,public_metrics");

        if(payload.has("pagination_token")) {
            params.put("pagination_token", payload.path("pagination_token").asText());
        }

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "FOLLOWING_RETRIEVED");
    }

    // Additional user operations
    private MessageDTO blockUser(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();

        String url = twitterApiBase + "/users/" + sourceUserId + "/blocking";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("target_user_id", payload.path("target_user_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_BLOCKED");
    }

    private MessageDTO unblockUser(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        String targetUserId = payload.path("target_user_id").asText();

        String url = twitterApiBase + "/users/" + sourceUserId + "/blocking/" + targetUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_UNBLOCKED");
    }

    private MessageDTO muteUser(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();

        String url = twitterApiBase + "/users/" + sourceUserId + "/muting";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("target_user_id", payload.path("target_user_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_MUTED");
    }

    private MessageDTO unmuteUser(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String sourceUserId = payload.path("source_user_id").asText();
        String targetUserId = payload.path("target_user_id").asText();

        String url = twitterApiBase + "/users/" + sourceUserId + "/muting/" + targetUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_UNMUTED");
    }

    // List operations
    private MessageDTO createList(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/lists";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("name", payload.path("name").asText());
        if(payload.has("description")) {
            requestBody.put("description", payload.path("description").asText());
        }
        if(payload.has("private")) {
            requestBody.put("private", payload.path("private").asBoolean());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LIST_CREATED");
    }

    private MessageDTO updateList(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();

        String url = twitterApiBase + "/lists/" + listId;

        ObjectNode requestBody = objectMapper.createObjectNode();
        if(payload.has("name")) {
            requestBody.put("name", payload.path("name").asText());
        }
        if(payload.has("description")) {
            requestBody.put("description", payload.path("description").asText());
        }
        if(payload.has("private")) {
            requestBody.put("private", payload.path("private").asBoolean());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LIST_UPDATED");
    }

    private MessageDTO deleteList(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();

        String url = twitterApiBase + "/lists/" + listId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LIST_DELETED");
    }

    private MessageDTO addListMember(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();

        String url = twitterApiBase + "/lists/" + listId + "/members";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("user_id", payload.path("user_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LIST_MEMBER_ADDED");
    }

    private MessageDTO removeListMember(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/lists/" + listId + "/members/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LIST_MEMBER_REMOVED");
    }

    private MessageDTO getListTweets(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String listId = payload.path("list_id").asText();

        String url = twitterApiBase + "/lists/" + listId + "/tweets";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("expansions", "author_id");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "LIST_TWEETS_RETRIEVED");
    }

    // DM operations
    private MessageDTO sendDirectMessage(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/dm_conversations/with/" +
                    payload.path("participant_id").asText() + "/messages";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("text", payload.path("text").asText());

        // Add media if present
        if(payload.has("attachments")) {
            requestBody.set("attachments", payload.get("attachments"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "DIRECT_MESSAGE_SENT");
    }

    private MessageDTO getDirectMessages(MessageDTO message) throws Exception {
        String url = twitterApiBase + "/dm_events";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", "100");
        params.put("dm_event.fields", "id,text,created_at,sender_id,attachments");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("media.fields", "type,url,preview_image_url");
        params.put("expansions", "sender_id,attachments.media_keys");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "DIRECT_MESSAGES_RETRIEVED");
    }

    // Bookmark operations
    private MessageDTO bookmarkTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/bookmarks";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("tweet_id", payload.path("tweet_id").asText());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEET_BOOKMARKED");
    }

    private MessageDTO removeBookmark(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();
        String tweetId = payload.path("tweet_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/bookmarks/" + tweetId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "BOOKMARK_REMOVED");
    }

    private MessageDTO getBookmarks(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId + "/bookmarks";

        Map<String, String> params = new HashMap<>();
        params.put("max_results", payload.path("max_results").asText("100"));
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics");
        params.put("user.fields", "id,name,username,profile_image_url");
        params.put("expansions", "author_id");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "BOOKMARKS_RETRIEVED");
    }

    // Spaces operations
    private MessageDTO getSpace(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String spaceId = payload.path("space_id").asText();

        String url = twitterApiBase + "/spaces/" + spaceId;

        Map<String, String> params = new HashMap<>();
        params.put("space.fields", "host_ids,created_at,creator_id,id,lang,invited_user_ids," +
                                   "participant_count,speaker_ids,started_at,state,title,topic_ids");
        params.put("expansions", "host_ids,speaker_ids");
        params.put("user.fields", "id,name,username,profile_image_url");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "SPACE_RETRIEVED");
    }

    private MessageDTO searchSpaces(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/spaces/search";

        Map<String, String> params = new HashMap<>();
        params.put("query", payload.path("query").asText());
        params.put("state", payload.path("state").asText("all"));
        params.put("max_results", payload.path("max_results").asText("100"));

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "SPACES_SEARCHED");
    }

    // Analytics operations
    private MessageDTO getTweetMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String tweetId = payload.path("tweet_id").asText();

        String url = twitterApiBase + "/tweets/" + tweetId;

        Map<String, String> params = new HashMap<>();
        params.put("tweet.fields", "public_metrics,organic_metrics,promoted_metrics");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEET_METRICS_RETRIEVED");
    }

    private MessageDTO getUserMetrics(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.path("user_id").asText();

        String url = twitterApiBase + "/users/" + userId;

        Map<String, String> params = new HashMap<>();
        params.put("user.fields", "public_metrics");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_METRICS_RETRIEVED");
    }

    // Helper methods
    private String getMediaCategory(String mediaType) {
        if(mediaType.startsWith("image/")) {
            return "tweet_image";
        } else if(mediaType.startsWith("video/")) {
            return "tweet_video";
        } else if(mediaType.equals("image/gif")) {
            return "tweet_gif";
        }
        return "tweet_image";
    }

    private ResponseEntity<String> makeApiCall(String url, HttpMethod method, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder urlBuilder = new StringBuilder(url);
        if(!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) -> {
                try {
                    urlBuilder.append(key).append(" = ")
                             .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()))
                             .append("&");
                } catch(Exception e) {
                    log.error("Error encoding parameter", e);
                }
            });
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(urlBuilder.toString(), method, entity, String.class);
    }

    protected MessageDTO createSuccessResponse(String messageId, String responseBody, String operation) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setTimestamp(java.time.LocalDateTime.now());
        response.setHeaders(Map.of(
            "operation", operation,
            "source", "twitter"
       ));
        response.setPayload(responseBody);
        return response;
    }

    protected MessageDTO createErrorResponse(MessageDTO originalMessage, String error) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(originalMessage.getCorrelationId());
        response.setStatus(MessageStatus.FAILED);
        response.setTimestamp(java.time.LocalDateTime.now());
        response.setHeaders(Map.of(
            "error", error,
            "source", "twitter"
       ));
        return response;
    }

    private String getBearerToken() {
        if(config.getBearerToken() != null) {
            return credentialEncryptionService.decrypt(config.getBearerToken());
        }
        // If no bearer token, use OAuth 2.0 access token
        return credentialEncryptionService.decrypt(config.getAccessToken());
    }

    private MessageDTO getTweet(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String tweetId = payload.path("tweet_id").asText();

        String url = twitterApiBase + "/tweets/" + tweetId;

        Map<String, String> params = new HashMap<>();
        params.put("tweet.fields", "id,text,created_at,author_id,public_metrics,entities,attachments");
        params.put("user.fields", "id,name,username,profile_image_url,verified");
        params.put("media.fields", "type,url,preview_image_url");
        params.put("expansions", "author_id,attachments.media_keys");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "TWEET_RETRIEVED");
    }

    private MessageDTO getUser(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String userId = payload.has("user_id") ? payload.path("user_id").asText() : null;
        String username = payload.has("username") ? payload.path("username").asText() : null;

        String url;
        if(userId != null) {
            url = twitterApiBase + "/users/" + userId;
        } else if(username != null) {
            url = twitterApiBase + "/users/by/username/" + username;
        } else {
            throw new AdapterException("Either user_id or username is required");
        }

        Map<String, String> params = new HashMap<>();
        params.put("user.fields", "id,name,username,created_at,description,profile_image_url,verified,public_metrics");

        ResponseEntity<String> response = makeApiCall(url, HttpMethod.GET, params);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "USER_RETRIEVED");
    }

    private MessageDTO getTimeline(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String timelineType = payload.path("timeline_type").asText("home");

        return switch(timelineType.toLowerCase()) {
            case "home" -> getHomeTimeline(message);
            case "user" -> getUserTimeline(message);
            case "mentions" -> getMentions(message);
            default -> throw new AdapterException("Unknown timeline type: " + timelineType);
        };
    }

    private MessageDTO updateProfile(MessageDTO message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());

        String url = twitterApiBase + "/users/me";

        ObjectNode requestBody = objectMapper.createObjectNode();

        if(payload.has("name")) {
            requestBody.put("name", payload.path("name").asText());
        }
        if(payload.has("description")) {
            requestBody.put("description", payload.path("description").asText());
        }
        if(payload.has("url")) {
            requestBody.put("url", payload.path("url").asText());
        }
        if(payload.has("location")) {
            requestBody.put("location", payload.path("location").asText());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

        return createSuccessResponse(message.getCorrelationId(), response.getBody(), "PROFILE_UPDATED");
    }

    private void validateConfiguration() throws AdapterException {
        if(config == null) {
            throw new AdapterException("Twitter configuration is not set");
        }
        if(config.getBearerToken() == null && config.getAccessToken() == null) {
            throw new AdapterException("Bearer token or access token is required");
        }
    }

    public MessageDTO sendMessage(MessageDTO message) throws AdapterException {
        return processMessage(message);
    }

    public void setConfiguration(TwitterApiV2Config config) {
        this.config = config;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("apiKey", config.getApiKey());
        configMap.put("bearerToken", config.getBearerToken() != null);
        configMap.put("enabled", config.isEnabled());
        return configMap;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    protected long getPollingIntervalMs() {
        // Outbound adapter doesn't poll, but return a default value
        return 60000; // 1 minute
    }

    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Convert payload to MessageDTO if needed
        MessageDTO message;
        if (payload instanceof MessageDTO) {
            message = (MessageDTO) payload;
        } else {
            message = new MessageDTO();
            message.setPayload(objectMapper.writeValueAsString(payload));
            message.setHeaders(headers);
        }

        MessageDTO result = processMessage(message);
        return AdapterResult.success(result.getPayload(), result.getHeaders().toString());
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            String url = twitterApiBase + "/users/me";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getBearerToken());
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Twitter API v2 connection successful");
            } else {
                return AdapterResult.failure("Twitter API v2 connection failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return AdapterResult.failure("Twitter API v2 connection test failed: " + e.getMessage(), e);
        }
    }

    // Implementation of abstract methods from AbstractOutboundAdapter
    @Override
    protected void doReceiverInitialize() throws Exception {
        log.debug("Initializing Twitter API v2 receiver");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.debug("Destroying Twitter API v2 receiver");
    }

    @Override
    protected AdapterResult doReceive(Object request) throws Exception {
        // This is an outbound adapter, so receive is not supported
        return AdapterResult.failure("Receive operation not supported for outbound adapter");
    }
}
