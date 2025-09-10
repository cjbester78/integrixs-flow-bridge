package com.integrixs.adapters.social.reddit;

import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.platform.models.Message;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.service.RateLimiterService;
import com.integrixs.shared.utils.CredentialEncryptionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Outbound adapter for Reddit API integration.
 * Handles post creation, commenting, voting, moderation, and user operations.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "integrixs.adapters.reddit.enabled", havingValue = "true", matchIfMissing = false)
public class RedditOutboundAdapter extends AbstractSocialMediaOutboundAdapter {

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
    public AdapterConfig getAdapterConfig() {
        return config;
    }

    @Override
    public Message processMessage(Message message) {
        String action = message.getHeader("action");
        
        try {
            // Ensure we have a valid access token
            ensureAccessToken();
            
            switch (action) {
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
                    throw new UnsupportedOperationException("Unknown action: " + action);
            }
        } catch (Exception e) {
            return createErrorResponse(message, e.getMessage());
        }
    }

    private void ensureAccessToken() throws Exception {
        if (accessToken == null || System.currentTimeMillis() > tokenExpiry) {
            refreshAccessToken();
        }
    }

    private void refreshAccessToken() throws Exception {
        String tokenUrl = "https://www.reddit.com/api/v1/access_token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(config.getClientId(), getDecryptedCredential("clientSecret"));
        headers.set("User-Agent", config.getUserAgent());
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        
        if (StringUtils.hasText(config.getOauth2Config().getRefreshToken())) {
            // Use refresh token if available
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", getDecryptedCredential("refreshToken"));
        } else {
            // Use password grant (script app)
            body.add("grant_type", "password");
            body.add("username", config.getUsername());
            body.add("password", getDecryptedCredential("password"));
        }
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> tokenData = response.getBody();
            accessToken = (String) tokenData.get("access_token");
            int expiresIn = ((Number) tokenData.get("expires_in")).intValue();
            tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // Refresh 1 minute early
        } else {
            throw new RuntimeException("Failed to obtain Reddit access token");
        }
    }

    private Message submitPost(Message message) throws Exception {
        Map<String, Object> postData = message.getPayloadAsMap();
        
        String subreddit = (String) postData.get("subreddit");
        String kind = (String) postData.get("kind"); // self or link
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("sr", subreddit);
        params.add("kind", kind);
        params.add("title", (String) postData.get("title"));
        
        if ("self".equals(kind)) {
            params.add("text", (String) postData.get("text"));
        } else {
            params.add("url", (String) postData.get("url"));
        }
        
        // Optional parameters
        addOptionalParam(params, postData, "flair_id");
        addOptionalParam(params, postData, "flair_text");
        addOptionalParam(params, postData, "nsfw", "false");
        addOptionalParam(params, postData, "spoiler", "false");
        addOptionalParam(params, postData, "sendreplies", "true");
        addOptionalParam(params, postData, "resubmit", "false");
        
        String url = config.getApiUrl() + "/api/submit";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message editPost(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        String text = message.getPayloadAsString();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("thing_id", thingId);
        params.add("text", text);
        
        String url = config.getApiUrl() + "/api/editusertext";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message deletePost(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + "/api/del";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message crosspost(Message message) throws Exception {
        Map<String, Object> crosspostData = message.getPayloadAsMap();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("sr", (String) crosspostData.get("subreddit"));
        params.add("kind", "crosspost");
        params.add("title", (String) crosspostData.get("title"));
        params.add("crosspost_fullname", (String) crosspostData.get("original_post_id"));
        
        addOptionalParam(params, crosspostData, "nsfw", "false");
        addOptionalParam(params, crosspostData, "spoiler", "false");
        
        String url = config.getApiUrl() + "/api/submit";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message markNSFW(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        boolean nsfw = Boolean.parseBoolean(message.getHeader("nsfw", "true"));
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + (nsfw ? "/api/marknsfw" : "/api/unmarknsfw");
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message markSpoiler(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        boolean spoiler = Boolean.parseBoolean(message.getHeader("spoiler", "true"));
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + (spoiler ? "/api/spoiler" : "/api/unspoiler");
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message setPostFlair(Message message) throws Exception {
        String link = message.getHeader("link");
        String flairId = message.getHeader("flairId");
        String flairText = message.getHeader("flairText");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("link", link);
        
        if (flairId != null) {
            params.add("flair_template_id", flairId);
        }
        if (flairText != null) {
            params.add("text", flairText);
        }
        
        String url = config.getApiUrl() + "/api/selectflair";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message postComment(Message message) throws Exception {
        String parentId = message.getHeader("parentId");
        String text = message.getPayloadAsString();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("thing_id", parentId);
        params.add("text", text);
        
        String url = config.getApiUrl() + "/api/comment";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message editComment(Message message) throws Exception {
        return editPost(message); // Same API endpoint
    }

    private Message deleteComment(Message message) throws Exception {
        return deletePost(message); // Same API endpoint
    }

    private Message vote(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        int direction = Integer.parseInt(message.getHeader("direction", "0")); // 1=upvote, -1=downvote, 0=unvote
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        params.add("dir", String.valueOf(direction));
        
        String url = config.getApiUrl() + "/api/vote";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message saveItem(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        String category = message.getHeader("category");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        if (category != null) {
            params.add("category", category);
        }
        
        String url = config.getApiUrl() + "/api/save";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message unsaveItem(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + "/api/unsave";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message hidePost(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + "/api/hide";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message unhidePost(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + "/api/unhide";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message giveAward(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        String awardId = message.getHeader("awardId");
        boolean isAnonymous = Boolean.parseBoolean(message.getHeader("anonymous", "false"));
        
        Map<String, Object> awardData = new HashMap<>();
        awardData.put("is_anonymous", isAnonymous);
        
        String url = config.getApiUrl() + "/api/v2/gold/gild/" + thingId + "/" + awardId;
        String response = executeApiCall(() -> makePostJsonRequest(url, awardData));
        
        return createSuccessResponse(message, response);
    }

    private Message followUser(Message message) throws Exception {
        String username = message.getHeader("username");
        
        Map<String, Object> followData = new HashMap<>();
        followData.put("action", "sub");
        followData.put("sr_name", "u_" + username);
        followData.put("skip_initial_defaults", true);
        
        String url = config.getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostJsonRequest(url, followData));
        
        return createSuccessResponse(message, response);
    }

    private Message unfollowUser(Message message) throws Exception {
        String username = message.getHeader("username");
        
        Map<String, Object> unfollowData = new HashMap<>();
        unfollowData.put("action", "unsub");
        unfollowData.put("sr_name", "u_" + username);
        
        String url = config.getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostJsonRequest(url, unfollowData));
        
        return createSuccessResponse(message, response);
    }

    private Message blockUser(Message message) throws Exception {
        String username = message.getHeader("username");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        
        String url = config.getApiUrl() + "/api/block_user";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message unblockUser(Message message) throws Exception {
        String username = message.getHeader("username");
        String userId = message.getHeader("userId");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("id", userId);
        params.add("type", "enemy");
        
        String url = config.getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message sendMessage(Message message) throws Exception {
        Map<String, Object> msgData = message.getPayloadAsMap();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("to", (String) msgData.get("to"));
        params.add("subject", (String) msgData.get("subject"));
        params.add("text", (String) msgData.get("text"));
        
        String url = config.getApiUrl() + "/api/compose";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message subscribe(Message message) throws Exception {
        String subreddit = message.getHeader("subreddit");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("action", "sub");
        params.add("sr_name", subreddit);
        
        String url = config.getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message unsubscribe(Message message) throws Exception {
        String subreddit = message.getHeader("subreddit");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("action", "unsub");
        params.add("sr_name", subreddit);
        
        String url = config.getApiUrl() + "/api/subscribe";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message createMultireddit(Message message) throws Exception {
        Map<String, Object> multiData = message.getPayloadAsMap();
        String multiName = (String) multiData.get("name");
        
        Map<String, Object> model = new HashMap<>();
        model.put("display_name", (String) multiData.get("display_name"));
        model.put("description_md", (String) multiData.get("description"));
        model.put("visibility", multiData.getOrDefault("visibility", "private"));
        model.put("subreddits", multiData.get("subreddits"));
        
        Map<String, Object> request = Map.of("model", model);
        
        String url = config.getApiUrl() + "/api/multi/user/" + config.getUsername() + "/m/" + multiName;
        String response = executeApiCall(() -> makePutJsonRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message updateMultireddit(Message message) throws Exception {
        return createMultireddit(message); // Same endpoint with PUT
    }

    private Message deleteMultireddit(Message message) throws Exception {
        String multiName = message.getHeader("multiName");
        
        String url = config.getApiUrl() + "/api/multi/user/" + config.getUsername() + "/m/" + multiName;
        String response = executeApiCall(() -> makeDeleteRequest(url));
        
        return createSuccessResponse(message, response);
    }

    // Moderation actions
    private Message approve(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + "/api/approve";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message remove(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        boolean spam = Boolean.parseBoolean(message.getHeader("spam", "false"));
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        params.add("spam", String.valueOf(spam));
        
        String url = config.getApiUrl() + "/api/remove";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message spam(Message message) throws Exception {
        message.setHeader("spam", "true");
        return remove(message);
    }

    private Message distinguish(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        String how = message.getHeader("how", "yes"); // yes, no, admin, special
        boolean sticky = Boolean.parseBoolean(message.getHeader("sticky", "false"));
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("id", thingId);
        params.add("how", how);
        params.add("sticky", String.valueOf(sticky));
        
        String url = config.getApiUrl() + "/api/distinguish";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message sticky(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        boolean state = Boolean.parseBoolean(message.getHeader("state", "true"));
        int num = Integer.parseInt(message.getHeader("num", "1")); // 1 or 2 for slot
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("id", thingId);
        params.add("num", String.valueOf(num));
        params.add("state", String.valueOf(state));
        
        String url = config.getApiUrl() + "/api/set_subreddit_sticky";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message lock(Message message) throws Exception {
        String thingId = message.getHeader("thingId");
        boolean lock = Boolean.parseBoolean(message.getHeader("lock", "true"));
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", thingId);
        
        String url = config.getApiUrl() + (lock ? "/api/lock" : "/api/unlock");
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message banUser(Message message) throws Exception {
        Map<String, Object> banData = message.getPayloadAsMap();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) banData.get("username"));
        params.add("r", (String) banData.get("subreddit"));
        
        addOptionalParam(params, banData, "ban_reason");
        addOptionalParam(params, banData, "ban_message");
        addOptionalParam(params, banData, "duration");
        addOptionalParam(params, banData, "note");
        
        String url = config.getApiUrl() + "/api/friend";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message unbanUser(Message message) throws Exception {
        String username = message.getHeader("username");
        String subreddit = message.getHeader("subreddit");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("r", subreddit);
        params.add("type", "banned");
        
        String url = config.getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message muteUser(Message message) throws Exception {
        Map<String, Object> muteData = message.getPayloadAsMap();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) muteData.get("username"));
        params.add("r", (String) muteData.get("subreddit"));
        params.add("type", "muted");
        
        String url = config.getApiUrl() + "/api/friend";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message unmuteUser(Message message) throws Exception {
        String username = message.getHeader("username");
        String subreddit = message.getHeader("subreddit");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("r", subreddit);
        params.add("type", "muted");
        
        String url = config.getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message inviteModerator(Message message) throws Exception {
        Map<String, Object> inviteData = message.getPayloadAsMap();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) inviteData.get("username"));
        params.add("r", (String) inviteData.get("subreddit"));
        params.add("type", "moderator_invite");
        params.add("permissions", (String) inviteData.getOrDefault("permissions", "+all"));
        
        String url = config.getApiUrl() + "/api/friend";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message removeModerator(Message message) throws Exception {
        String username = message.getHeader("username");
        String subreddit = message.getHeader("subreddit");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", username);
        params.add("r", subreddit);
        params.add("type", "moderator");
        
        String url = config.getApiUrl() + "/api/unfriend";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message createWikiPage(Message message) throws Exception {
        Map<String, Object> wikiData = message.getPayloadAsMap();
        String subreddit = (String) wikiData.get("subreddit");
        String page = (String) wikiData.get("page");
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("content", (String) wikiData.get("content"));
        params.add("reason", (String) wikiData.getOrDefault("reason", "Created via API"));
        
        String url = config.getApiUrl() + "/r/" + subreddit + "/api/wiki/edit";
        params.add("page", page);
        
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message editWikiPage(Message message) throws Exception {
        return createWikiPage(message); // Same endpoint
    }

    private Message searchPosts(Message message) throws Exception {
        String query = message.getHeader("query");
        String subreddit = message.getHeader("subreddit");
        String sort = message.getHeader("sort", "relevance");
        String timeFilter = message.getHeader("time", "all");
        
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("sort", sort);
        params.put("t", timeFilter);
        params.put("type", "link");
        params.put("limit", message.getHeader("limit", "25"));
        
        String url = config.getApiUrl();
        if (subreddit != null) {
            url += "/r/" + subreddit;
        }
        url += "/search.json";
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message searchSubreddits(Message message) throws Exception {
        String query = message.getHeader("query");
        
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("type", "sr");
        params.put("limit", message.getHeader("limit", "25"));
        
        String url = config.getApiUrl() + "/search.json";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message searchUsers(Message message) throws Exception {
        String query = message.getHeader("query");
        
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("type", "user");
        params.put("limit", message.getHeader("limit", "25"));
        
        String url = config.getApiUrl() + "/search.json";
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message setUserFlair(Message message) throws Exception {
        Map<String, Object> flairData = message.getPayloadAsMap();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_type", "json");
        params.add("name", (String) flairData.get("username"));
        params.add("r", (String) flairData.get("subreddit"));
        
        addOptionalParam(params, flairData, "flair_template_id");
        addOptionalParam(params, flairData, "text");
        addOptionalParam(params, flairData, "css_class");
        
        String url = config.getApiUrl() + "/api/flair";
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message createLinkFlair(Message message) throws Exception {
        Map<String, Object> flairData = message.getPayloadAsMap();
        String subreddit = (String) flairData.get("subreddit");
        
        Map<String, Object> request = new HashMap<>();
        request.put("text", flairData.get("text"));
        request.put("text_editable", flairData.getOrDefault("text_editable", true));
        request.put("background_color", flairData.get("background_color"));
        request.put("text_color", flairData.get("text_color"));
        
        String url = config.getApiUrl() + "/r/" + subreddit + "/api/flairtemplate_v2";
        String response = executeApiCall(() -> makePostJsonRequest(url, request));
        
        return createSuccessResponse(message, response);
    }

    private Message updateLinkFlair(Message message) throws Exception {
        return createLinkFlair(message); // Same endpoint
    }

    private Message deleteLinkFlair(Message message) throws Exception {
        String subreddit = message.getHeader("subreddit");
        String flairId = message.getHeader("flairId");
        
        String url = config.getApiUrl() + "/r/" + subreddit + "/api/deleteflairtemplate";
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("flair_template_id", flairId);
        
        String response = executeApiCall(() -> makePostRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message getUserKarma(Message message) throws Exception {
        String url = config.getApiUrl() + "/api/v1/me/karma";
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));
        
        return createSuccessResponse(message, response);
    }

    private Message getPostInsights(Message message) throws Exception {
        String postId = message.getHeader("postId");
        
        String url = config.getApiUrl() + "/api/info.json";
        Map<String, String> params = new HashMap<>();
        params.put("id", "t3_" + postId);
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        
        return createSuccessResponse(message, response);
    }

    private Message getSubredditStats(Message message) throws Exception {
        String subreddit = message.getHeader("subreddit");
        
        String url = config.getApiUrl() + "/r/" + subreddit + "/about.json";
        String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));
        
        return createSuccessResponse(message, response);
    }

    // Helper methods
    private void addOptionalParam(MultiValueMap<String, String> params, Map<String, Object> source, String key) {
        addOptionalParam(params, source, key, null);
    }

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
        if (!params.isEmpty()) {
            urlWithParams.append("?");
            params.forEach((key, value) -> {
                try {
                    urlWithParams.append(key).append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                            .append("&");
                } catch (Exception e) {
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
        headers.set("User-Agent", config.getUserAgent());
        return headers;
    }

    @Override
    protected String getAdapterType() {
        return "REDDIT";
    }
}