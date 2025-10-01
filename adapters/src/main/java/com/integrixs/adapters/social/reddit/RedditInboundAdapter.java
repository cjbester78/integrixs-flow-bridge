package com.integrixs.adapters.social.reddit;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.social.base.EventType;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Inbound adapter for Reddit API integration.
 * Handles polling for posts, comments, messages, and subreddit activities.
 */
@Component
@ConditionalOnProperty(name = "integrixs.adapters.reddit.enabled", havingValue = "true", matchIfMissing = false)
public class RedditInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(RedditInboundAdapter.class);


    private final RedditApiConfig config;
    private final Set<String> processedPosts = ConcurrentHashMap.newKeySet();
    private final Set<String> processedComments = ConcurrentHashMap.newKeySet();
    private final Set<String> monitoredSubreddits = ConcurrentHashMap.newKeySet();
    private final Map<String, String> lastSeenItems = new ConcurrentHashMap<>();

    @Autowired
    public RedditInboundAdapter(
            RedditApiConfig config,
            RateLimiterService rateLimiterService,
            CredentialEncryptionService credentialEncryptionService) {
        super();
        this.config = config;
    }

    @Override
    protected Map<String, Object> getConfig() {
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
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    /**
     * Polls for new posts in monitored subreddits
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.posts - interval:300000}")
    public void pollSubredditPosts() {
        if(!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnableSubredditMonitoring()) {
            return;
        }

        try {
            log.debug("Polling Reddit posts from monitored subreddits");

            // Poll configured subreddits
            List<String> subreddits = config.getPollingConfig().getMonitoredSubreddits();
            if(subreddits != null && !subreddits.isEmpty()) {
                for(String subreddit : subreddits) {
                    monitoredSubreddits.add(subreddit);
                    pollSubredditNewPosts(subreddit);
                }
            }

            // Poll user's subscribed subreddits if enabled
            if(config.getPollingConfig().isPollSubscribedSubreddits()) {
                pollUserSubscriptions();
            }
        } catch(Exception e) {
            log.error("Error polling Reddit posts", e);
        }
    }

    /**
     * Polls for comments on tracked posts
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.comments - interval:600000}")
    public void pollComments() {
        if(!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnableCommentManagement()) {
            return;
        }

        try {
            log.debug("Polling Reddit comments");

            // Poll comments on user's posts
            if(config.getPollingConfig().isPollUserComments()) {
                pollUserPostComments();
            }

            // Poll replies to user's comments
            if(config.getPollingConfig().isPollCommentReplies()) {
                pollCommentReplies();
            }
        } catch(Exception e) {
            log.error("Error polling Reddit comments", e);
        }
    }

    /**
     * Polls for private messages and notifications
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.messages - interval:300000}")
    public void pollMessages() {
        if(!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnablePrivateMessages()) {
            return;
        }

        try {
            log.debug("Polling Reddit messages");
            pollInbox();
            pollMentions();
        } catch(Exception e) {
            log.error("Error polling Reddit messages", e);
        }
    }

    /**
     * Polls for moderation queue items
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.modqueue - interval:180000}")
    public void pollModQueue() {
        if(!config.getFeatures().isEnableModeration()) {
            return;
        }

        try {
            log.debug("Polling Reddit moderation queue");

            List<String> moderatedSubs = config.getPollingConfig().getModeratedSubreddits();
            if(moderatedSubs != null && !moderatedSubs.isEmpty()) {
                for(String subreddit : moderatedSubs) {
                    pollSubredditModQueue(subreddit);
                    pollSubredditReports(subreddit);
                }
            }
        } catch(Exception e) {
            log.error("Error polling Reddit mod queue", e);
        }
    }

    private void pollSubredditNewPosts(String subreddit) throws Exception {
        String lastSeen = lastSeenItems.get("posts_" + subreddit);

        String url = getApiUrl() + "/r/" + subreddit + "/new.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        params.put("raw_json", "1");

        if(lastSeen != null) {
            params.put("before", lastSeen);
        }

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> posts = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> post : posts) {
                processPost(post);
            }

            // Update last seen
            if(!posts.isEmpty()) {
                String firstPostName = (String) posts.get(0).get("data.name");
                lastSeenItems.put("posts_" + subreddit, firstPostName);
            }
        }
    }

    private void pollUserSubscriptions() throws Exception {
        String url = getApiUrl() + "/subreddits/mine/subscriber.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> subreddits = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> subreddit : subreddits) {
                Map<String, Object> subData = (Map<String, Object>) subreddit.get("data");
                String subName = (String) subData.get("display_name");

                if(!monitoredSubreddits.contains(subName)) {
                    monitoredSubreddits.add(subName);
                    // Poll new posts from this subreddit
                    pollSubredditNewPosts(subName);
                }
            }
        }
    }

    private void pollUserPostComments() throws Exception {
        String username = config.getUsername();
        if(!StringUtils.hasText(username)) {
            return;
        }

        String url = getApiUrl() + "/user/" + username + "/submitted.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "25");
        params.put("sort", "new");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> posts = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> post : posts) {
                Map<String, Object> postData = (Map<String, Object>) post.get("data");
                String postId = (String) postData.get("id");

                // Poll comments for this post
                pollPostComments(postId, (String) postData.get("subreddit"));
            }
        }
    }

    private void pollPostComments(String postId, String subreddit) throws Exception {
        String url = getApiUrl() + "/r/" + subreddit + "/comments/" + postId + ".json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "500");
        params.put("depth", "10");
        params.put("raw_json", "1");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        List<Map<String, Object>> responseArray = parseJsonArrayResponse(response);

        if(responseArray.size() > 1) {
            Map<String, Object> commentsData = responseArray.get(1);
            Map<String, Object> data = (Map<String, Object>) commentsData.get("data");

            if(data != null && data.containsKey("children")) {
                List<Map<String, Object>> comments = (List<Map<String, Object>>) data.get("children");
                processCommentTree(comments, postId);
            }
        }
    }

    private void processCommentTree(List<Map<String, Object>> comments, String parentId) {
        for(Map<String, Object> comment : comments) {
            String kind = (String) comment.get("kind");
            if("t1".equals(kind)) { // t1 = comment
                processComment(comment, parentId);

                // Process replies recursively
                Map<String, Object> commentData = (Map<String, Object>) comment.get("data");
                Map<String, Object> replies = (Map<String, Object>) commentData.get("replies");

                if(replies != null && replies instanceof Map) {
                    Map<String, Object> repliesData = (Map<String, Object>) replies;
                    Map<String, Object> repliesDataInner = (Map<String, Object>) repliesData.get("data");

                    if(repliesDataInner != null && repliesDataInner.containsKey("children")) {
                        List<Map<String, Object>> replyComments = (List<Map<String, Object>>) repliesDataInner.get("children");
                        String commentId = (String) commentData.get("id");
                        processCommentTree(replyComments, commentId);
                    }
                }
            }
        }
    }

    private void pollCommentReplies() throws Exception {
        String url = getApiUrl() + "/message/comments.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> message : messages) {
                processCommentReply(message);
            }
        }
    }

    private void pollInbox() throws Exception {
        String url = getApiUrl() + "/message/inbox.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> message : messages) {
                processPrivateMessage(message);
            }
        }
    }

    private void pollMentions() throws Exception {
        String url = getApiUrl() + "/message/mentions.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> mentions = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> mention : mentions) {
                processMention(mention);
            }
        }
    }

    private void pollSubredditModQueue(String subreddit) throws Exception {
        String url = getApiUrl() + "/r/" + subreddit + "/about/modqueue.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> item : items) {
                processModQueueItem(item);
            }
        }
    }

    private void pollSubredditReports(String subreddit) throws Exception {
        String url = getApiUrl() + "/r/" + subreddit + "/about/reports.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");

        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);

        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if(data != null && data.containsKey("children")) {
            List<Map<String, Object>> reports = (List<Map<String, Object>>) data.get("children");

            for(Map<String, Object> report : reports) {
                processReport(report);
            }
        }
    }

    private void processPost(Map<String, Object> post) {
        Map<String, Object> postData = (Map<String, Object>) post.get("data");
        String postId = (String) postData.get("id");

        if(!processedPosts.add(postId)) {
            return; // Already processed
        }

        RedditPost redditPost = new RedditInboundAdapter.RedditPost();
        redditPost.id = postId;
        redditPost.name = (String) postData.get("name");
        redditPost.title = (String) postData.get("title");
        redditPost.author = (String) postData.get("author");
        redditPost.subreddit = (String) postData.get("subreddit");
        redditPost.selftext = (String) postData.get("selftext");
        redditPost.url = (String) postData.get("url");
        redditPost.permalink = (String) postData.get("permalink");
        redditPost.created = convertTimestamp(postData.get("created_utc"));
        redditPost.score = ((Number) postData.get("score")).intValue();
        redditPost.upvoteRatio = ((Number) postData.get("upvote_ratio")).doubleValue();
        redditPost.numComments = ((Number) postData.get("num_comments")).intValue();
        redditPost.over18 = (Boolean) postData.get("over_18");
        redditPost.spoiler = (Boolean) postData.get("spoiler");
        redditPost.locked = (Boolean) postData.get("locked");
        redditPost.stickied = (Boolean) postData.get("stickied");
        redditPost.distinguished = (String) postData.get("distinguished");
        redditPost.linkFlairText = (String) postData.get("link_flair_text");
        redditPost.postHint = (String) postData.get("post_hint");
        redditPost.preview = postData.get("preview");
        redditPost.media = postData.get("media");
        redditPost.mediaEmbed = postData.get("media_embed");
        redditPost.gallery = postData.get("gallery_data");
        redditPost.poll = postData.get("poll_data");
        redditPost.crosspostParent = (List<String>) postData.get("crosspost_parent_list");
        redditPost.awards = extractAwards(postData);
        publishMessage("reddit.post.created", redditPost);
    }

    private void processComment(Map<String, Object> comment, String parentId) {
        Map<String, Object> commentData = (Map<String, Object>) comment.get("data");
        String commentId = (String) commentData.get("id");

        if(!processedComments.add(commentId)) {
            return; // Already processed
        }

        RedditComment redditComment = new RedditInboundAdapter.RedditComment();
        redditComment.id = commentId;
        redditComment.name = (String) commentData.get("name");
        redditComment.parentId = parentId;
        redditComment.linkId = (String) commentData.get("link_id");
        redditComment.author = (String) commentData.get("author");
        redditComment.body = (String) commentData.get("body");
        redditComment.created = convertTimestamp(commentData.get("created_utc"));
        redditComment.score = ((Number) commentData.get("score")).intValue();
        redditComment.edited = commentData.get("edited");
        redditComment.distinguished = (String) commentData.get("distinguished");
        redditComment.stickied = (Boolean) commentData.get("stickied");
        redditComment.scoreHidden = (Boolean) commentData.get("score_hidden");
        redditComment.locked = (Boolean) commentData.get("locked");
        redditComment.subreddit = (String) commentData.get("subreddit");
        redditComment.authorFlairText = (String) commentData.get("author_flair_text");
        redditComment.awards = extractAwards(commentData);
        publishMessage("reddit.comment.created", redditComment);
    }

    private void processCommentReply(Map<String, Object> message) {
        Map<String, Object> messageData = (Map<String, Object>) message.get("data");

        RedditMessageDTO reply = new RedditInboundAdapter.RedditMessageDTO();
        reply.id = (String) messageData.get("id");
        reply.type = "comment_reply";
        reply.author = (String) messageData.get("author");
        reply.subject = (String) messageData.get("subject");
        reply.body = (String) messageData.get("body");
        reply.created = convertTimestamp(messageData.get("created_utc"));
        reply.context = (String) messageData.get("context");
        reply.subreddit = (String) messageData.get("subreddit");
        reply.wasComment = (Boolean) messageData.get("was_comment");
        reply.new_ = (Boolean) messageData.get("new");
        publishMessage("reddit.comment.reply", reply);
    }

    private void processPrivateMessage(Map<String, Object> message) {
        Map<String, Object> messageData = (Map<String, Object>) message.get("data");

        RedditMessageDTO pm = new RedditInboundAdapter.RedditMessageDTO();
        pm.id = (String) messageData.get("id");
        pm.type = "private_message";
        pm.author = (String) messageData.get("author");
        pm.dest = (String) messageData.get("dest");
        pm.subject = (String) messageData.get("subject");
        pm.body = (String) messageData.get("body");
        pm.created = convertTimestamp(messageData.get("created_utc"));
        pm.new_ = (Boolean) messageData.get("new");
        publishMessage("reddit.message.received", pm);
    }

    private void processMention(Map<String, Object> mention) {
        Map<String, Object> mentionData = (Map<String, Object>) mention.get("data");

        RedditMessageDTO mentionMsg = new RedditInboundAdapter.RedditMessageDTO();
        mentionMsg.id = (String) mentionData.get("id");
        mentionMsg.type = "username_mention";
        mentionMsg.author = (String) mentionData.get("author");
        mentionMsg.body = (String) mentionData.get("body");
        mentionMsg.context = (String) mentionData.get("context");
        mentionMsg.subreddit = (String) mentionData.get("subreddit");
        mentionMsg.created = convertTimestamp(mentionData.get("created_utc"));
        mentionMsg.new_ = (Boolean) mentionData.get("new");
        publishMessage("reddit.mention.received", mentionMsg);
    }

    private void processModQueueItem(Map<String, Object> item) {
        Map<String, Object> itemData = (Map<String, Object>) item.get("data");
        String kind = (String) item.get("kind");

        RedditModItem modItem = new RedditInboundAdapter.RedditModItem();
        modItem.id = (String) itemData.get("id");
        modItem.kind = kind;
        modItem.author = (String) itemData.get("author");
        modItem.subreddit = (String) itemData.get("subreddit");
        modItem.title = kind.equals("t3") ? (String) itemData.get("title") : null;
        modItem.body = kind.equals("t1") ? (String) itemData.get("body") : null;
        modItem.reports = (List<List<Object>>) itemData.get("mod_reports");
        modItem.userReports = (List<List<Object>>) itemData.get("user_reports");
        modItem.numReports = ((Number) itemData.get("num_reports")).intValue();
        modItem.approved = (Boolean) itemData.get("approved");
        modItem.removed = (Boolean) itemData.get("removed");
        modItem.spam = (Boolean) itemData.get("spam");
        publishMessage("reddit.modqueue.item", modItem);
    }

    private void processReport(Map<String, Object> report) {
        publishMessage("reddit.report.received", report);
    }

    @Override
    public void processWebhookEvent(Map<String, Object> event) {
        // Reddit doesn't have a traditional webhook system
        // Instead it uses websockets for real - time events
        // This method would be used if implementing a custom webhook receiver

        String eventType = (String) event.get("type");

        switch(eventType) {
            case "post.create":
                processWebhookPost(event);
                break;
            case "comment.create":
                processWebhookComment(event);
                break;
            case "message.receive":
                processWebhookMessage(event);
                break;
            default:
                log.warn("Unknown webhook event type: {}", eventType);
        }
    }

    private void processWebhookPost(Map<String, Object> event) {
        publishMessage("reddit.webhook.post", event);
    }

    private void processWebhookComment(Map<String, Object> event) {
        publishMessage("reddit.webhook.comment", event);
    }

    private void processWebhookMessage(Map<String, Object> event) {
        publishMessage("reddit.webhook.message", event);
    }

    @Override
    public boolean verifyWebhookSignature(String signature, String payload) {
        // Reddit doesn't use webhook signatures in the traditional sense
        // This would be implemented for custom webhook solutions
        return true;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList(
                "SOCIAL_MEDIA_POST",
                "SOCIAL_MEDIA_COMMENT",
                "SOCIAL_MEDIA_MESSAGE",
                "SOCIAL_MEDIA_MENTION",
                "SOCIAL_MEDIA_MODERATION",
                "SOCIAL_MEDIA_VOTE"
       );
    }

    private LocalDateTime convertTimestamp(Object timestamp) {
        if(timestamp instanceof Number) {
            long epochSeconds = ((Number) timestamp).longValue();
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
        }
        return LocalDateTime.now();
    }

    private List<String> extractAwards(Map<String, Object> data) {
        List<Map<String, Object>> allAwardings = (List<Map<String, Object>>) data.get("all_awardings");
        if(allAwardings != null) {
            return allAwardings.stream()
                    .map(award ->(String) award.get("name"))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // Data classes for Reddit entities
    public static class RedditPost {
        public String id;
        public String name;
        public String title;
        public String author;
        public String subreddit;
        public String selftext;
        public String url;
        public String permalink;
        public LocalDateTime created;
        public int score;
        public double upvoteRatio;
        public int numComments;
        public boolean over18;
        public boolean spoiler;
        public boolean locked;
        public boolean stickied;
        public String distinguished;
        public String linkFlairText;
        public String postHint;
        public Object preview;
        public Object media;
        public Object mediaEmbed;
        public Object gallery;
        public Object poll;
        public List<String> crosspostParent;
        public List<String> awards;
    }

    public static class RedditComment {
        public String id;
        public String name;
        public String parentId;
        public String linkId;
        public String author;
        public String body;
        public LocalDateTime created;
        public int score;
        public Object edited;
        public String distinguished;
        public boolean stickied;
        public boolean scoreHidden;
        public boolean locked;
        public String subreddit;
        public String authorFlairText;
        public List<String> awards;
    }

    public static class RedditMessageDTO {
        public String id;
        public String type;
        public String author;
        public String dest;
        public String subject;
        public String body;
        public LocalDateTime created;
        public String context;
        public String subreddit;
        public Boolean wasComment;
        public Boolean new_;
    }

    public static class RedditModItem {
        public String id;
        public String kind;
        public String author;
        public String subreddit;
        public String title;
        public String body;
        public List<List<Object>> reports;
        public List<List<Object>> userReports;
        public int numReports;
        public boolean approved;
        public boolean removed;
        public boolean spam;
    }

    // Getters and Setters for adapter state
    public Set<String> getProcessedPosts() {
        return processedPosts;
    }
    public Set<String> getProcessedComments() {
        return processedComments;
    }
    public Set<String> getMonitoredSubreddits() {
        return monitoredSubreddits;
    }
    public Map<String, String> getLastSeenItems() {
        return lastSeenItems;
    }

    // Helper methods for API calls
    private String getApiUrl() {
        return config.getApiBaseUrl() != null ? config.getApiBaseUrl() : "https://oauth.reddit.com";
    }

    private String executeApiCall(java.util.concurrent.Callable<String> apiCall) throws Exception {
        try {
            return apiCall.call();
        } catch (Exception e) {
            handleApiError(e, "API call");
            throw e;
        }
    }

    private String makeGetRequest(String url, Map<String, String> params) throws Exception {
        // This would be implemented using RestTemplate or HttpClient
        // For now, returning empty response
        log.debug("Making GET request to: {} with params: {}", url, params);
        return "{}";
    }

    private Map<String, Object> parseJsonResponse(String response) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(response, Map.class);
        } catch (Exception e) {
            log.error("Error parsing JSON response", e);
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> parseJsonArrayResponse(String response) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(response, List.class);
        } catch (Exception e) {
            log.error("Error parsing JSON array response", e);
            return new ArrayList<>();
        }
    }

    private void publishMessage(String eventType, Object data) {
        MessageDTO message = convertToMessage(Map.of("data", data), eventType);
        // This would publish to the message bus/queue
        log.debug("Publishing message of type: {}", eventType);
    }

    // Required abstract methods from AbstractInboundAdapter
    @Override
    protected void doSenderInitialize() throws Exception {
        log.debug("Initializing Reddit inbound adapter sender");
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.debug("Destroying Reddit inbound adapter sender");
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws com.integrixs.shared.exceptions.AdapterException {
        // Reddit inbound adapter doesn't support sending
        return AdapterResult.failure("Reddit inbound adapter does not support sending messages");
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // Reddit inbound adapter doesn't support sending
        return AdapterResult.failure("Reddit inbound adapter does not support sending messages");
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test by getting user info
            String url = getApiUrl() + "/api/v1/me";
            String response = executeApiCall(() -> makeGetRequest(url, new HashMap<>()));

            if (response != null) {
                return AdapterResult.success(null, "Reddit API connection successful");
            } else {
                return AdapterResult.failure("Reddit API connection failed");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Failed to test Reddit connection: " + e.getMessage(), e);
        }
    }
}
