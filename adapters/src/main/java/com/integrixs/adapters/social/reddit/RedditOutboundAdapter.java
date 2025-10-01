package com.integrixs.adapters.social.reddit;
import com.integrixs.adapters.domain.model.AdapterConfiguration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.adapters.core.AdapterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Outbound adapter for Reddit API integration.
 * Handles post creation, commenting, voting, moderation, and user operations.
 */
@Component
@ConditionalOnProperty(name = "integrixs.adapters.reddit.enabled", havingValue = "true", matchIfMissing = false)
public class RedditOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(RedditOutboundAdapter.class);


    private final RedditApiConfig config;
    private final RestTemplate restTemplate;
    private String accessToken;
    private long tokenExpiry;

    @Autowired
    public RedditOutboundAdapter(
            RedditApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate) {
        super(rateLimiterService, credentialEncryptionService);
        this.config = config;
        this.restTemplate = restTemplate;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("clientId", config.getClientId());
            configMap.put("clientSecret", config.getClientSecret());
            configMap.put("userAgent", config.getUserAgent());
            configMap.put("username", config.getUsername());
            configMap.put("password", config.getPassword());
            configMap.put("enabled", config.isEnabled());
            configMap.put("apiBaseUrl", config.getApiBaseUrl());
        }
        return configMap;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        String action = getHeader(message, "action");

        try {
            // Ensure we have a valid access token
            ensureAccessToken();

            switch(action) {
                // Post Management
                case "SUBMIT_POST":
                    return submitPost(message);
                case "EDIT_POST":
                    return editPost(message);
                case "DELETE_POST":
                    return deletePost(message);
                case "CROSSPOST":
                    return crosspost(message);
                case "MARK_NSFW":
                    return markNSFW(message);
                case "MARK_SPOILER":
                    return markSpoiler(message);
                case "SET_POST_FLAIR":
                    return setPostFlair(message);

                // Comment Management
                case "POST_COMMENT":
                    return postComment(message);
                case "EDIT_COMMENT":
                    return editComment(message);
                case "DELETE_COMMENT":
                    return deleteComment(message);

                // Voting
                case "VOTE":
                    return vote(message);
                case "SAVE_ITEM":
                    return saveItem(message);
                case "UNSAVE_ITEM":
                    return unsaveItem(message);
                case "HIDE_POST":
                    return hidePost(message);
                case "UNHIDE_POST":
                    return unhidePost(message);

                // Awards
                case "GIVE_AWARD":
                    return giveAward(message);

                // User Management
                case "FOLLOW_USER":
                    return followUser(message);
                case "UNFOLLOW_USER":
                    return unfollowUser(message);
                case "BLOCK_USER":
                    return blockUser(message);
                case "UNBLOCK_USER":
                    return unblockUser(message);
                case "SEND_MESSAGE":
                    return sendMessage(message);

                // Subreddit Management
                case "SUBSCRIBE":
                    return subscribe(message);
                case "UNSUBSCRIBE":
                    return unsubscribe(message);
                case "CREATE_MULTIREDDIT":
                    return createMultireddit(message);
                case "UPDATE_MULTIREDDIT":
                    return updateMultireddit(message);
                case "DELETE_MULTIREDDIT":
                    return deleteMultireddit(message);

                // Moderation Actions
                case "APPROVE":
                    return approve(message);
                case "REMOVE":
                    return remove(message);
                case "SPAM":
                    return spam(message);
                case "DISTINGUISH":
                    return distinguish(message);
                case "STICKY":
                    return sticky(message);
                case "LOCK":
                    return lock(message);
                case "BAN_USER":
                    return banUser(message);
                case "UNBAN_USER":
                    return unbanUser(message);
                case "MUTE_USER":
                    return muteUser(message);
                case "UNMUTE_USER":
                    return unmuteUser(message);
                case "INVITE_MODERATOR":
                    return inviteModerator(message);
                case "REMOVE_MODERATOR":
                    return removeModerator(message);

                // Wiki Management
                case "CREATE_WIKI_PAGE":
                    return createWikiPage(message);
                case "EDIT_WIKI_PAGE":
                    return editWikiPage(message);

                // Search
                case "SEARCH_POSTS":
                    return searchPosts(message);
                case "SEARCH_SUBREDDITS":
                    return searchSubreddits(message);
                case "SEARCH_USERS":
                    return searchUsers(message);

                // Flair Management
                case "SET_USER_FLAIR":
                    return setUserFlair(message);
                case "CREATE_LINK_FLAIR":
                    return createLinkFlair(message);
                case "UPDATE_LINK_FLAIR":
                    return updateLinkFlair(message);
                case "DELETE_LINK_FLAIR":
                    return deleteLinkFlair(message);

                // Analytics
                case "GET_USER_KARMA":
                    return getUserKarma(message);
                case "GET_POST_INSIGHTS":
                    return getPostInsights(message);
                case "GET_SUBREDDIT_STATS":
                    return getSubredditStats(message);

                default:
                    log.warn("Unknown action received: {}", action);
                    return message;
            }
        } catch(Exception e) {
            return createErrorResponse(message, e.getMessage());
        }
    }

    private void ensureAccessToken() throws Exception {
        if(accessToken == null || System.currentTimeMillis() > tokenExpiry) {
            refreshAccessToken();
        }
    }

    private void refreshAccessToken() throws Exception {
        String tokenUrl = "https://www.reddit.com/api/v1/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(config.getClientId(), getDecryptedCredential("clientSecret"));
        headers.set("User - Agent", config.getUserAgent());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        if(StringUtils.hasText(config.getRefreshToken())) {
            // Use refresh token if available
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", getDecryptedCredential("refreshToken"));
        } else {
            // Use password grant(script app)
            body.add("grant_type", "password");
            body.add("username", config.getUsername());
            body.add("password", getDecryptedCredential("password"));
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> tokenData = response.getBody();
            accessToken = (String) tokenData.get("access_token");
            int expiresIn = ((Number) tokenData.get("expires_in")).intValue();
            tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // Refresh 1 minute early
        } else {
            throw new RuntimeException("Failed to obtain Reddit access token");
        }
    }

    private MessageDTO submitPost(MessageDTO message) throws Exception {
        Map<String, Object> postData = getPayloadAsMap(message);

        String subreddit = (String) postData.get("subreddit");
        String kind = (String) postData.get("kind"); // self or link

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("sr", subreddit);
        params.add("kind", kind);
        params.add("title", (String) postData.get("title"));

        if("self".equals(kind)) {
            params.add("text", (String) postData.get("text"));
        } else {
            params.add("url", (String) postData.get("url"));
        }

        // Optional parameters
        addOptionalParam(params, postData, "flair_id", null);
        addOptionalParam(params, postData, "flair_text", null);
        addOptionalParam(params, postData, "nsfw", "false");
        addOptionalParam(params, postData, "spoiler", "false");
        addOptionalParam(params, postData, "sendreplies", "true");
        addOptionalParam(params, postData, "resubmit", "false");

        String url = getApiUrl() + "/api/submit";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO editPost(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        String text = getPayloadAsString(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("thing_id", thingId);
        params.add("text", text);

        String url = getApiUrl() + "/api/editusertext";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO deletePost(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + "/api/del";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO crosspost(MessageDTO message) throws Exception {
        Map<String, Object> crosspostData = getPayloadAsMap(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("sr", (String) crosspostData.get("subreddit"));
        params.add("kind", "crosspost");
        params.add("title", (String) crosspostData.get("title"));
        params.add("crosspost_fullname", (String) crosspostData.get("original_post_id"));

        addOptionalParam(params, crosspostData, "nsfw", "false");
        addOptionalParam(params, crosspostData, "spoiler", "false");

        String url = getApiUrl() + "/api/submit";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO markNSFW(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        boolean nsfw = Boolean.parseBoolean(getHeader(message, "nsfw", "true"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + (nsfw ? "/api/marknsfw" : "/api/unmarknsfw");
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO markSpoiler(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        boolean spoiler = Boolean.parseBoolean(getHeader(message, "spoiler", "true"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + (spoiler ? "/api/spoiler" : "/api/unspoiler");
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO setPostFlair(MessageDTO message) throws Exception {
        String link = getHeader(message, "link");
        String flairId = getHeader(message, "flairId");
        String flairText = getHeader(message, "flairText");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("link", link);

        if(flairId != null) {
            params.add("flair_template_id", flairId);
        }
        if(flairText != null) {
            params.add("text", flairText);
        }

        String url = getApiUrl() + "/api/selectflair";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO postComment(MessageDTO message) throws Exception {
        String parentId = getHeader(message, "parentId");
        String text = getPayloadAsString(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("thing_id", parentId);
        params.add("text", text);

        String url = getApiUrl() + "/api/comment";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO editComment(MessageDTO message) throws Exception {
        return editPost(message); // Same API endpoint
    }

    private MessageDTO deleteComment(MessageDTO message) throws Exception {
        return deletePost(message); // Same API endpoint
    }

    private MessageDTO vote(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        int direction = Integer.parseInt(getHeader(message, "direction", "0")); // 1 = upvote, -1 = downvote, 0 = unvote

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        params.add("dir", String.valueOf(direction));

        String url = getApiUrl() + "/api/vote";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO saveItem(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        String category = getHeader(message, "category");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        if(category != null) {
            params.add("category", category);
        }

        String url = getApiUrl() + "/api/save";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO unsaveItem(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + "/api/unsave";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO hidePost(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + "/api/hide";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO unhidePost(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + "/api/unhide";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO giveAward(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        String awardId = getHeader(message, "awardId");
        boolean isAnonymous = Boolean.parseBoolean(getHeader(message, "anonymous", "false"));

        Map<String, Object> awardData = new HashMap<>();
        awardData.put("is_anonymous", isAnonymous);

        String url = getApiUrl() + "/api/v2/gold/gild/" + thingId + "/" + awardId;
        String response = executeApiCall(() -> makePostJsonRequest(url, awardData));

        return createSuccessResponse(message, response);
    }

    private MessageDTO followUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");

        Map<String, Object> followData = new HashMap<>();
        followData.put("action", "sub");
        followData.put("sr_name", "u_" + username);
        followData.put("skip_initial_defaults", true);

        String url = getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostJsonRequest(url, followData));

        return createSuccessResponse(message, response);
    }

    private MessageDTO unfollowUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");

        Map<String, Object> unfollowData = new HashMap<>();
        unfollowData.put("action", "unsub");
        unfollowData.put("sr_name", "u_" + username);

        String url = getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostJsonRequest(url, unfollowData));

        return createSuccessResponse(message, response);
    }

    private MessageDTO blockUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);

        String url = getApiUrl() + "/api/block_user";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO unblockUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");
        String userId = getHeader(message, "userId");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("id", userId);
        params.add("type", "enemy");

        String url = getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO sendMessage(MessageDTO message) throws Exception {
        Map<String, Object> msgData = getPayloadAsMap(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("to", (String) msgData.get("to"));
        params.add("subject", (String) msgData.get("subject"));
        params.add("text", (String) msgData.get("text"));

        String url = getApiUrl() + "/api/compose";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO subscribe(MessageDTO message) throws Exception {
        String subreddit = getHeader(message, "subreddit");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("action", "sub");
        params.add("sr_name", subreddit);

        String url = getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO unsubscribe(MessageDTO message) throws Exception {
        String subreddit = getHeader(message, "subreddit");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("action", "unsub");
        params.add("sr_name", subreddit);

        String url = getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO createMultireddit(MessageDTO message) throws Exception {
        Map<String, Object> multiData = getPayloadAsMap(message);
        String multiName = (String) multiData.get("name");

        Map<String, Object> model = new HashMap<>();
        model.put("display_name", (String) multiData.get("display_name"));
        model.put("description_md", (String) multiData.get("description"));
        model.put("visibility", multiData.getOrDefault("visibility", "private"));
        model.put("subreddits", multiData.get("subreddits"));

        Map<String, Object> request = Map.of("model", model);

        String url = getApiUrl() + "/api/multi/user/" + config.getUsername() + "/m/" + multiName;
        String response = executeApiCall(() -> makePutJsonRequest(url, request));

        return createSuccessResponse(message, response);
    }

    private MessageDTO updateMultireddit(MessageDTO message) throws Exception {
        return createMultireddit(message); // Same endpoint with PUT
    }

    private MessageDTO deleteMultireddit(MessageDTO message) throws Exception {
        String multiName = getHeader(message, "multiName");

        String url = getApiUrl() + "/api/multi/user/" + config.getUsername() + "/m/" + multiName;
        String response = executeApiCall(() -> makeDeleteRequest(url));

        return createSuccessResponse(message, response);
    }

    // Moderation actions
    private MessageDTO approve(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + "/api/approve";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO remove(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        boolean spam = Boolean.parseBoolean(getHeader(message, "spam", "false"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        params.add("spam", String.valueOf(spam));

        String url = getApiUrl() + "/api/remove";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO spam(MessageDTO message) throws Exception {
        Map<String, Object> headers = message.getHeaders() != null ? new HashMap<>(message.getHeaders()) : new HashMap<>();
        headers.put("spam", "true");
        message.setHeaders(headers);
        return remove(message);
    }

    private MessageDTO distinguish(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        String how = getHeader(message, "how", "yes"); // yes, no, admin, special
        boolean sticky = Boolean.parseBoolean(getHeader(message, "sticky", "false"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("id", thingId);
        params.add("how", how);
        params.add("sticky", String.valueOf(sticky));

        String url = getApiUrl() + "/api/distinguish";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO sticky(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        boolean state = Boolean.parseBoolean(getHeader(message, "state", "true"));
        int num = Integer.parseInt(getHeader(message, "num", "1")); // 1 or 2 for slot

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("id", thingId);
        params.add("num", String.valueOf(num));
        params.add("state", String.valueOf(state));

        String url = getApiUrl() + "/api/set_subreddit_sticky";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO lock(MessageDTO message) throws Exception {
        String thingId = getHeader(message, "thingId");
        boolean lock = Boolean.parseBoolean(getHeader(message, "lock", "true"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);

        String url = getApiUrl() + (lock ? "/api/lock" : "/api/unlock");
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO banUser(MessageDTO message) throws Exception {
        Map<String, Object> banData = getPayloadAsMap(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) banData.get("username"));
        params.add("r", (String) banData.get("subreddit"));

        addOptionalParam(params, banData, "ban_reason", null);
        addOptionalParam(params, banData, "ban_message", null);
        addOptionalParam(params, banData, "duration", null);
        addOptionalParam(params, banData, "note", null);

        String url = getApiUrl() + "/api/friend";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO unbanUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");
        String subreddit = getHeader(message, "subreddit");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("r", subreddit);
        params.add("type", "banned");

        String url = getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO muteUser(MessageDTO message) throws Exception {
        Map<String, Object> muteData = getPayloadAsMap(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) muteData.get("username"));
        params.add("r", (String) muteData.get("subreddit"));
        params.add("type", "muted");

        String url = getApiUrl() + "/api/friend";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO unmuteUser(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");
        String subreddit = getHeader(message, "subreddit");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("r", subreddit);
        params.add("type", "muted");

        String url = getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO inviteModerator(MessageDTO message) throws Exception {
        Map<String, Object> inviteData = getPayloadAsMap(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) inviteData.get("username"));
        params.add("r", (String) inviteData.get("subreddit"));
        params.add("type", "moderator_invite");
        params.add("permissions", (String) inviteData.getOrDefault("permissions", " + all"));

        String url = getApiUrl() + "/api/friend";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO removeModerator(MessageDTO message) throws Exception {
        String username = getHeader(message, "username");
        String subreddit = getHeader(message, "subreddit");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("r", subreddit);
        params.add("type", "moderator");

        String url = getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO createWikiPage(MessageDTO message) throws Exception {
        Map<String, Object> wikiData = getPayloadAsMap(message);
        String subreddit = (String) wikiData.get("subreddit");
        String page = (String) wikiData.get("page");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("content", (String) wikiData.get("content"));
        params.add("reason", (String) wikiData.getOrDefault("reason", "Created via API"));

        String url = getApiUrl() + "/r/" + subreddit + "/api/wiki/edit";
        params.add("page", page);

        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO editWikiPage(MessageDTO message) throws Exception {
        return createWikiPage(message); // Same endpoint
    }

    private MessageDTO searchPosts(MessageDTO message) throws Exception {
        String query = getHeader(message, "query");
        String subreddit = getHeader(message, "subreddit");
        String sort = getHeader(message, "sort", "relevance");
        String timeFilter = getHeader(message, "time", "all");

        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("sort", sort);
        params.put("t", timeFilter);
        params.put("type", "link");
        params.put("limit", getHeader(message, "limit", "25"));

        String baseUrl = getApiUrl();
        String finalUrl;
        if(subreddit != null) {
            finalUrl = baseUrl + "/r/" + subreddit + "/search.json";
        } else {
            finalUrl = baseUrl + "/search.json";
        }

        String response = executeApiCall(() -> makeGetRequest(finalUrl, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO searchSubreddits(MessageDTO message) throws Exception {
        String query = getHeader(message, "query");

        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("type", "sr");
        params.put("limit", getHeader(message, "limit", "25"));

        String url = getApiUrl() + "/search.json";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO searchUsers(MessageDTO message) throws Exception {
        String query = getHeader(message, "query");

        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("type", "user");
        params.put("limit", getHeader(message, "limit", "25"));

        String url = getApiUrl() + "/search.json";
        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO setUserFlair(MessageDTO message) throws Exception {
        Map<String, Object> flairData = getPayloadAsMap(message);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) flairData.get("username"));
        params.add("r", (String) flairData.get("subreddit"));

        addOptionalParam(params, flairData, "flair_template_id", null);
        addOptionalParam(params, flairData, "text", null);
        addOptionalParam(params, flairData, "css_class", null);

        String url = getApiUrl() + "/api/flair";
        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO createLinkFlair(MessageDTO message) throws Exception {
        Map<String, Object> flairData = getPayloadAsMap(message);
        String subreddit = (String) flairData.get("subreddit");

        Map<String, Object> request = new HashMap<>();
        request.put("text", flairData.get("text"));
        request.put("text_editable", flairData.getOrDefault("text_editable", true));
        request.put("background_color", flairData.get("background_color"));
        request.put("text_color", flairData.get("text_color"));

        String url = getApiUrl() + "/r/" + subreddit + "/api/flairtemplate_v2";
        String response = executeApiCall(() -> makePostJsonRequest(url, request));

        return createSuccessResponse(message, response);
    }

    private MessageDTO updateLinkFlair(MessageDTO message) throws Exception {
        return createLinkFlair(message); // Same endpoint
    }

    private MessageDTO deleteLinkFlair(MessageDTO message) throws Exception {
        String subreddit = getHeader(message, "subreddit");
        String flairId = getHeader(message, "flairId");

        String url = getApiUrl() + "/r/" + subreddit + "/api/deleteflairtemplate";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("flair_template_id", flairId);

        String response = executeApiCall(() -> makePostRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO getUserKarma(MessageDTO message) throws Exception {
        String url = getApiUrl() + "/api/v1/me/karma";
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));

        return createSuccessResponse(message, response);
    }

    private MessageDTO getPostInsights(MessageDTO message) throws Exception {
        String postId = getHeader(message, "postId");

        String url = getApiUrl() + "/api/info.json";
        Map<String, String> params = new HashMap<>();
        params.put("id", "t3_" + postId);

        String response = executeApiCall(() -> makeGetRequest(url, params));

        return createSuccessResponse(message, response);
    }

    private MessageDTO getSubredditStats(MessageDTO message) throws Exception {
        String subreddit = getHeader(message, "subreddit");

        String url = getApiUrl() + "/r/" + subreddit + "/about.json";
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));

        return createSuccessResponse(message, response);
    }

    // Helper methods
    private void addOptionalParam(MultiValueMap<String, String> params, Map<String, Object> source, String key, String defaultValue) {
        Object value = source.get(key);
        if (value != null) {
            params.add(key, value.toString());
        } else if (defaultValue != null) {
            params.add(key, defaultValue);
        }
    }


    private String makePostRequest(String url, MultiValueMap<String, String> params) throws Exception {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    private String makePostJsonRequest(String url, Map<String, Object> data) throws Exception {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    private String makePutJsonRequest(String url, Map<String, Object> data) throws Exception {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        return response.getBody();
    }

    private String makeGetRequest(String url, Map<String, String> params) throws Exception {
        HttpHeaders headers = createHeaders();

        StringBuilder urlWithParams = new StringBuilder(url);
        if(!params.isEmpty()) {
            urlWithParams.append("?");
            params.forEach((key, value) -> {
                try {
                    urlWithParams.append(key).append(" = ")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                            .append("&");
                } catch(Exception e) {
                    log.error("Error encoding parameter", e);
                }
            });
            urlWithParams.deleteCharAt(urlWithParams.length() - 1);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(urlWithParams.toString(), HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    private String makeDeleteRequest(String url) throws Exception {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        return response.getBody();
    }

    private HttpHeaders createHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("User - Agent", config.getUserAgent());
        return headers;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    // Helper methods for MessageDTO access
    private String getHeader(MessageDTO message, String key) {
        return message.getHeaders() != null ?
            (String) message.getHeaders().get(key) : null;
    }

    private String getHeader(MessageDTO message, String key, String defaultValue) {
        String value = getHeader(message, key);
        return value != null ? value : defaultValue;
    }

    private Map<String, Object> getPayloadAsMap(MessageDTO message) {
        try {
            if (message.getPayload() != null) {
                return new ObjectMapper().readValue(message.getPayload(), Map.class);
            }
        } catch (Exception e) {
            log.error("Error parsing payload as map", e);
        }
        return new HashMap<>();
    }

    private String getPayloadAsString(MessageDTO message) {
        return message.getPayload();
    }

    private String getApiUrl() {
        return config.getApiBaseUrl() != null ? config.getApiBaseUrl() : "https://oauth.reddit.com";
    }


    // Override createSuccessResponse to match expected signature
    private MessageDTO createSuccessResponse(MessageDTO originalMessage, String responseData) {
        return createSuccessResponse(originalMessage.getCorrelationId(), responseData, "SUCCESS");
    }

    // Required abstract methods from AbstractOutboundAdapter
    @Override
    protected void doReceiverInitialize() throws Exception {
        log.debug("Initializing Reddit outbound adapter receiver");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        log.debug("Destroying Reddit outbound adapter receiver");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Reddit outbound adapter doesn't support receiving
        return AdapterResult.failure("Reddit outbound adapter does not support receiving messages");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test by getting user info
            ensureAccessToken();
            String url = getApiUrl() + "/api/v1/me";
            String response = makeGetRequest(url, new HashMap<>());

            if (response != null) {
                return AdapterResult.success(null, "Reddit API connection successful");
            } else {
                return AdapterResult.failure("Reddit API connection failed");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test Reddit connection: " + e.getMessage(), e);
        }
    }

    @Override
    protected long getPollingIntervalMs() {
        // Reddit outbound adapter doesn't support polling
        return 0;
    }
}
