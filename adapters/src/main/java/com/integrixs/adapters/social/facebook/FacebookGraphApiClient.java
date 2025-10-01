package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.adapters.social.facebook.model.FacebookPost;
import com.integrixs.adapters.social.facebook.model.FacebookPostResponse;
import com.integrixs.adapters.social.facebook.model.FacebookInsights;
import com.integrixs.shared.services.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Facebook Graph API client with built - in rate limiting and retry logic
 */
@Component
public class FacebookGraphApiClient {
    private static final Logger log = LoggerFactory.getLogger(FacebookGraphApiClient.class);


    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Post content to Facebook page
     */
    public FacebookPostResponse createPost(String pageId, FacebookPost post, String accessToken, FacebookGraphApiConfig config) {
        String endpoint = String.format("%s/feed", pageId);
        String url = config.getApiUrl(endpoint);

        // Check rate limit
        checkRateLimit(pageId, config);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            // Basic post parameters
            if(post.getMessage() != null) {
                params.add("message", post.getMessage());
            }
            if(post.getLink() != null) {
                params.add("link", post.getLink());
            }
            if(post.isPublished()) {
                params.add("published", "true");
            } else {
                params.add("published", "false");
            }

            // Schedule post if specified
            if(post.getScheduledPublishTime() != null) {
                long unixTime = post.getScheduledPublishTime().toEpochSecond(java.time.ZoneOffset.UTC);
                params.add("scheduled_publish_time", String.valueOf(unixTime));
                params.add("published", "false");
            }

            // Add media if present
            if(post.getPhotoUrls() != null && !post.getPhotoUrls().isEmpty()) {
                // For multiple photos, use different endpoint
                if(post.getPhotoUrls().size() > 1) {
                    return createMultiPhotoPost(pageId, post, accessToken, config);
                } else {
                    params.add("url", post.getPhotoUrls().get(0));
                }
            }

            // Targeting if specified
            if(post.getTargeting() != null) {
                params.add("targeting", objectMapper.writeValueAsString(post.getTargeting()));
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String postId = response.getBody().get("id").asText();

                FacebookPostResponse postResponse = new FacebookPostResponse();
                postResponse.setId(postId);
                postResponse.setSuccess(true);
                postResponse.setMessage("Post created successfully");

                // Fetch post details to get permalink
                JsonNode postDetails = getPost(postId, accessToken, config);
                if(postDetails != null && postDetails.has("permalink_url")) {
                    postResponse.setPermalinkUrl(postDetails.get("permalink_url").asText());
                }

                return postResponse;
            }

            throw new FacebookApiException("Failed to create post: " + response.getStatusCode());

        } catch(HttpClientErrorException e) {
            log.error("Facebook API error: {}", e.getResponseBodyAsString());
            throw new FacebookApiException("Facebook API error: " + parseErrorMessage(e.getResponseBodyAsString()), e);
        } catch(Exception e) {
            log.error("Error creating Facebook post", e);
            throw new FacebookApiException("Failed to create Facebook post", e);
        }
    }

    /**
     * Create a post with multiple photos
     */
    private FacebookPostResponse createMultiPhotoPost(String pageId, FacebookPost post, String accessToken, FacebookGraphApiConfig config) {
        try {
            // First, upload each photo and get their IDs
            Map<String, String> photoIds = new HashMap<>();

            for(String photoUrl : post.getPhotoUrls()) {
                String photoId = uploadPhoto(pageId, photoUrl, false, accessToken, config);
                photoIds.put(photoUrl, photoId);
            }

            // Then create the multi - photo post
            String endpoint = String.format("%s/feed", pageId);
            String url = config.getApiUrl(endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            if(post.getMessage() != null) {
                params.add("message", post.getMessage());
            }

            // Add attached_media parameter with photo IDs
            int index = 0;
            for(String photoId : photoIds.values()) {
                params.add(String.format("attached_media[%d]", index),
                    String.format(" {\"media_fbid\":\"%s\"}", photoId));
                index++;
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FacebookPostResponse postResponse = new FacebookPostResponse();
                postResponse.setId(response.getBody().get("id").asText());
                postResponse.setSuccess(true);
                postResponse.setMessage("Multi - photo post created successfully");
                postResponse.setPhotoIds(photoIds);
                return postResponse;
            }

            throw new FacebookApiException("Failed to create multi - photo post");

        } catch(Exception e) {
            log.error("Error creating multi - photo post", e);
            throw new FacebookApiException("Failed to create multi - photo post", e);
        }
    }

    /**
     * Upload a photo to Facebook
     */
    public String uploadPhoto(String pageId, String photoUrl, boolean published, String accessToken, FacebookGraphApiConfig config) {
        String endpoint = String.format("%s/photos", pageId);
        String url = config.getApiUrl(endpoint);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("url", photoUrl);
            params.add("published", String.valueOf(published));

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("id").asText();
            }

            throw new FacebookApiException("Failed to upload photo");

        } catch(Exception e) {
            log.error("Error uploading photo to Facebook", e);
            throw new FacebookApiException("Failed to upload photo", e);
        }
    }

    /**
     * Get post details
     */
    public JsonNode getPost(String postId, String accessToken, FacebookGraphApiConfig config) {
        String endpoint = postId;
        String url = config.getApiUrl(endpoint);

        // Check rate limit
        checkRateLimit(postId, config);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("fields", "id,message,created_time,permalink_url,shares,reactions.summary(true),comments.summary(true)")
                .queryParam("access_token", accessToken);

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                builder.toUriString(),
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }

            return null;

        } catch(Exception e) {
            log.error("Error fetching post details", e);
            throw new FacebookApiException("Failed to fetch post details", e);
        }
    }

    /**
     * Get page insights
     */
    public FacebookInsights getPageInsights(String pageId, String metric, LocalDateTime since, LocalDateTime until,
                                          String accessToken, FacebookGraphApiConfig config) {
        String endpoint = String.format("%s/insights", pageId);
        String url = config.getApiUrl(endpoint);

        // Check rate limit
        checkRateLimit(pageId + ":insights", config);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("metric", metric)
                .queryParam("period", "day")
                .queryParam("access_token", accessToken);

            if(since != null) {
                builder.queryParam("since", since.toEpochSecond(java.time.ZoneOffset.UTC));
            }
            if(until != null) {
                builder.queryParam("until", until.toEpochSecond(java.time.ZoneOffset.UTC));
            }

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                builder.toUriString(),
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseInsights(response.getBody());
            }

            throw new FacebookApiException("Failed to fetch insights");

        } catch(Exception e) {
            log.error("Error fetching page insights", e);
            throw new FacebookApiException("Failed to fetch page insights", e);
        }
    }

    /**
     * Delete a post
     */
    public boolean deletePost(String postId, String accessToken, FacebookGraphApiConfig config) {
        String url = config.getApiUrl(postId);

        // Check rate limit
        checkRateLimit(postId, config);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url + "?access_token = " + accessToken,
                HttpMethod.DELETE,
                request,
                JsonNode.class
           );

            return response.getStatusCode().is2xxSuccessful();

        } catch(Exception e) {
            log.error("Error deleting post", e);
            throw new FacebookApiException("Failed to delete post", e);
        }
    }

    /**
     * Get comments for a post
     */
    public JsonNode getPostComments(String postId, String accessToken, FacebookGraphApiConfig config) {
        String endpoint = String.format("%s/comments", postId);
        String url = config.getApiUrl(endpoint);

        // Check rate limit
        checkRateLimit(postId + ":comments", config);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("fields", "id,message,from,created_time,like_count,comment_count")
                .queryParam("access_token", accessToken)
                .queryParam("limit", 100);

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                builder.toUriString(),
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }

            return null;

        } catch(Exception e) {
            log.error("Error fetching comments", e);
            throw new FacebookApiException("Failed to fetch comments", e);
        }
    }

    /**
     * Post a comment reply
     */
    public String postComment(String objectId, String message, String accessToken, FacebookGraphApiConfig config) {
        String endpoint = String.format("%s/comments", objectId);
        String url = config.getApiUrl(endpoint);

        // Check rate limit
        checkRateLimit(objectId + ":comment", config);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(accessToken);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("message", message);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
           );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("id").asText();
            }

            throw new FacebookApiException("Failed to post comment");

        } catch(Exception e) {
            log.error("Error posting comment", e);
            throw new FacebookApiException("Failed to post comment", e);
        }
    }

    /**
     * Check rate limit before making API call
     */
    private void checkRateLimit(String key, FacebookGraphApiConfig config) {
        String rateLimitKey = "facebook:" + config.getPageId() + ":" + key;

        // Use the shared - lib RateLimiterService
        RateLimiterService.RateLimitResult result = rateLimiterService.getRateLimitStatus(rateLimitKey);

        if(!result.isAllowed()) {
            long waitSeconds = result.getWaitTimeMs() / 1000;
            throw new FacebookApiException(
                String.format("Rate limit exceeded. Retry after %d seconds", waitSeconds)
           );
        }
    }

    /**
     * Parse error message from Facebook API response
     */
    private String parseErrorMessage(String errorBody) {
        try {
            JsonNode errorNode = objectMapper.readTree(errorBody);
            if(errorNode.has("error") && errorNode.get("error").has("message")) {
                return errorNode.get("error").get("message").asText();
            }
        } catch(Exception e) {
            log.debug("Failed to parse error message", e);
        }
        return errorBody;
    }

    /**
     * Parse insights response
     */
    private FacebookInsights parseInsights(JsonNode response) {
        FacebookInsights insights = new FacebookInsights();
        // Implementation to parse insights data
        // This would extract metrics from the Facebook API response format
        return insights;
    }
}
