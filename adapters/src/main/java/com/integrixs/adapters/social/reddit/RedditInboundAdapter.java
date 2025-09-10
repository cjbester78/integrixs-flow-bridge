package com.integrixs.adapters.social.reddit;

import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.platform.events.EventType;
import com.integrixs.platform.models.Message;
import com.integrixs.shared.config.AdapterConfig;
import com.integrixs.shared.service.RateLimiterService;
import com.integrixs.shared.utils.CredentialEncryptionService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@ConditionalOnProperty(name = "integrixs.adapters.reddit.enabled", havingValue = "true", matchIfMissing = false)
public class RedditInboundAdapter extends AbstractSocialMediaInboundAdapter {

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
        super(rateLimiterService, credentialEncryptionService);
        this.config = config;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public AdapterConfig getAdapterConfig() {
        return config;
    }

    /**
     * Polls for new posts in monitored subreddits
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.posts-interval:300000}")
    public void pollSubredditPosts() {
        if (!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnableSubredditMonitoring()) {
            return;
        }

        try {
            log.debug("Polling Reddit posts from monitored subreddits");
            
            // Poll configured subreddits
            List<String> subreddits = config.getPollingConfig().getMonitoredSubreddits();
            if (subreddits != null && !subreddits.isEmpty()) {
                for (String subreddit : subreddits) {
                    monitoredSubreddits.add(subreddit);
                    pollSubredditNewPosts(subreddit);
                }
            }
            
            // Poll user's subscribed subreddits if enabled
            if (config.getPollingConfig().isPollSubscribedSubreddits()) {
                pollUserSubscriptions();
            }
        } catch (Exception e) {
            log.error("Error polling Reddit posts", e);
        }
    }

    /**
     * Polls for comments on tracked posts
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.comments-interval:600000}")
    public void pollComments() {
        if (!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnableCommentManagement()) {
            return;
        }

        try {
            log.debug("Polling Reddit comments");
            
            // Poll comments on user's posts
            if (config.getPollingConfig().isPollUserComments()) {
                pollUserPostComments();
            }
            
            // Poll replies to user's comments
            if (config.getPollingConfig().isPollCommentReplies()) {
                pollCommentReplies();
            }
        } catch (Exception e) {
            log.error("Error polling Reddit comments", e);
        }
    }

    /**
     * Polls for private messages and notifications
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.messages-interval:300000}")
    public void pollMessages() {
        if (!config.getPollingConfig().isEnabled() || !config.getFeatures().isEnablePrivateMessages()) {
            return;
        }

        try {
            log.debug("Polling Reddit messages");
            pollInbox();
            pollMentions();
        } catch (Exception e) {
            log.error("Error polling Reddit messages", e);
        }
    }

    /**
     * Polls for moderation queue items
     */
    @Scheduled(fixedDelayString = "${integrixs.adapters.reddit.polling.modqueue-interval:180000}")
    public void pollModQueue() {
        if (!config.getFeatures().isEnableModeration()) {
            return;
        }

        try {
            log.debug("Polling Reddit moderation queue");
            
            List<String> moderatedSubs = config.getPollingConfig().getModeratedSubreddits();
            if (moderatedSubs != null && !moderatedSubs.isEmpty()) {
                for (String subreddit : moderatedSubs) {
                    pollSubredditModQueue(subreddit);
                    pollSubredditReports(subreddit);
                }
            }
        } catch (Exception e) {
            log.error("Error polling Reddit mod queue", e);
        }
    }

    private void pollSubredditNewPosts(String subreddit) throws Exception {
        String lastSeen = lastSeenItems.get("posts_" + subreddit);
        
        String url = config.getApiUrl() + "/r/" + subreddit + "/new.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        params.put("raw_json", "1");
        
        if (lastSeen != null) {
            params.put("before", lastSeen);
        }
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> posts = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> post : posts) {
                processPost(post);
            }
            
            // Update last seen
            if (!posts.isEmpty()) {
                String firstPostName = (String) posts.get(0).get("data.name");
                lastSeenItems.put("posts_" + subreddit, firstPostName);
            }
        }
    }

    private void pollUserSubscriptions() throws Exception {
        String url = config.getApiUrl() + "/subreddits/mine/subscriber.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> subreddits = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> subreddit : subreddits) {
                Map<String, Object> subData = (Map<String, Object>) subreddit.get("data");
                String subName = (String) subData.get("display_name");
                
                if (!monitoredSubreddits.contains(subName)) {
                    monitoredSubreddits.add(subName);
                    // Poll new posts from this subreddit
                    pollSubredditNewPosts(subName);
                }
            }
        }
    }

    private void pollUserPostComments() throws Exception {
        String username = config.getUsername();
        if (!StringUtils.hasText(username)) {
            return;
        }
        
        String url = config.getApiUrl() + "/user/" + username + "/submitted.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "25");
        params.put("sort", "new");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> posts = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> post : posts) {
                Map<String, Object> postData = (Map<String, Object>) post.get("data");
                String postId = (String) postData.get("id");
                
                // Poll comments for this post
                pollPostComments(postId, (String) postData.get("subreddit"));
            }
        }
    }

    private void pollPostComments(String postId, String subreddit) throws Exception {
        String url = config.getApiUrl() + "/r/" + subreddit + "/comments/" + postId + ".json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "500");
        params.put("depth", "10");
        params.put("raw_json", "1");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        List<Map<String, Object>> responseArray = parseJsonArrayResponse(response);
        
        if (responseArray.size() > 1) {
            Map<String, Object> commentsData = responseArray.get(1);
            Map<String, Object> data = (Map<String, Object>) commentsData.get("data");
            
            if (data != null && data.containsKey("children")) {
                List<Map<String, Object>> comments = (List<Map<String, Object>>) data.get("children");
                processCommentTree(comments, postId);
            }
        }
    }

    private void processCommentTree(List<Map<String, Object>> comments, String parentId) {
        for (Map<String, Object> comment : comments) {
            String kind = (String) comment.get("kind");
            if ("t1".equals(kind)) { // t1 = comment
                processComment(comment, parentId);
                
                // Process replies recursively
                Map<String, Object> commentData = (Map<String, Object>) comment.get("data");
                Map<String, Object> replies = (Map<String, Object>) commentData.get("replies");
                
                if (replies != null && replies instanceof Map) {
                    Map<String, Object> repliesData = (Map<String, Object>) replies;
                    Map<String, Object> repliesDataInner = (Map<String, Object>) repliesData.get("data");
                    
                    if (repliesDataInner != null && repliesDataInner.containsKey("children")) {
                        List<Map<String, Object>> replyComments = (List<Map<String, Object>>) repliesDataInner.get("children");
                        String commentId = (String) commentData.get("id");
                        processCommentTree(replyComments, commentId);
                    }
                }
            }
        }
    }

    private void pollCommentReplies() throws Exception {
        String url = config.getApiUrl() + "/message/comments.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> message : messages) {
                processCommentReply(message);
            }
        }
    }

    private void pollInbox() throws Exception {
        String url = config.getApiUrl() + "/message/inbox.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> message : messages) {
                processPrivateMessage(message);
            }
        }
    }

    private void pollMentions() throws Exception {
        String url = config.getApiUrl() + "/message/mentions.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> mentions = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> mention : mentions) {
                processMention(mention);
            }
        }
    }

    private void pollSubredditModQueue(String subreddit) throws Exception {
        String url = config.getApiUrl() + "/r/" + subreddit + "/about/modqueue.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> item : items) {
                processModQueueItem(item);
            }
        }
    }

    private void pollSubredditReports(String subreddit) throws Exception {
        String url = config.getApiUrl() + "/r/" + subreddit + "/about/reports.json";
        Map<String, String> params = new HashMap<>();
        params.put("limit", "100");
        
        String response = executeApiCall(() -> makeGetRequest(url, params));
        Map<String, Object> responseData = parseJsonResponse(response);
        
        Map<String, Object> data = (Map<String, Object>) responseData.get("data");
        if (data != null && data.containsKey("children")) {
            List<Map<String, Object>> reports = (List<Map<String, Object>>) data.get("children");
            
            for (Map<String, Object> report : reports) {
                processReport(report);
            }
        }
    }

    private void processPost(Map<String, Object> post) {
        Map<String, Object> postData = (Map<String, Object>) post.get("data");
        String postId = (String) postData.get("id");
        
        if (!processedPosts.add(postId)) {
            return; // Already processed
        }
        
        RedditPost redditPost = RedditPost.builder()
                .id(postId)
                .name((String) postData.get("name"))
                .title((String) postData.get("title"))
                .author((String) postData.get("author"))
                .subreddit((String) postData.get("subreddit"))
                .selftext((String) postData.get("selftext"))
                .url((String) postData.get("url"))
                .permalink((String) postData.get("permalink"))
                .created(convertTimestamp(postData.get("created_utc")))
                .score(((Number) postData.get("score")).intValue())
                .upvoteRatio(((Number) postData.get("upvote_ratio")).doubleValue())
                .numComments(((Number) postData.get("num_comments")).intValue())
                .over18((Boolean) postData.get("over_18"))
                .spoiler((Boolean) postData.get("spoiler"))
                .locked((Boolean) postData.get("locked"))
                .stickied((Boolean) postData.get("stickied"))
                .distinguished((String) postData.get("distinguished"))
                .linkFlairText((String) postData.get("link_flair_text"))
                .postHint((String) postData.get("post_hint"))
                .preview(postData.get("preview"))
                .media(postData.get("media"))
                .mediaEmbed(postData.get("media_embed"))
                .gallery(postData.get("gallery_data"))
                .poll(postData.get("poll_data"))
                .crosspostParent((List<String>) postData.get("crosspost_parent_list"))
                .awards(extractAwards(postData))
                .build();
        
        publishMessage("reddit.post.created", redditPost);
    }

    private void processComment(Map<String, Object> comment, String parentId) {
        Map<String, Object> commentData = (Map<String, Object>) comment.get("data");
        String commentId = (String) commentData.get("id");
        
        if (!processedComments.add(commentId)) {
            return; // Already processed
        }
        
        RedditComment redditComment = RedditComment.builder()
                .id(commentId)
                .name((String) commentData.get("name"))
                .parentId(parentId)
                .linkId((String) commentData.get("link_id"))
                .author((String) commentData.get("author"))
                .body((String) commentData.get("body"))
                .created(convertTimestamp(commentData.get("created_utc")))
                .score(((Number) commentData.get("score")).intValue())
                .edited(commentData.get("edited"))
                .distinguished((String) commentData.get("distinguished"))
                .stickied((Boolean) commentData.get("stickied"))
                .scoreHidden((Boolean) commentData.get("score_hidden"))
                .locked((Boolean) commentData.get("locked"))
                .subreddit((String) commentData.get("subreddit"))
                .authorFlairText((String) commentData.get("author_flair_text"))
                .awards(extractAwards(commentData))
                .build();
        
        publishMessage("reddit.comment.created", redditComment);
    }

    private void processCommentReply(Map<String, Object> message) {
        Map<String, Object> messageData = (Map<String, Object>) message.get("data");
        
        RedditMessage reply = RedditMessage.builder()
                .id((String) messageData.get("id"))
                .type("comment_reply")
                .author((String) messageData.get("author"))
                .subject((String) messageData.get("subject"))
                .body((String) messageData.get("body"))
                .created(convertTimestamp(messageData.get("created_utc")))
                .context((String) messageData.get("context"))
                .subreddit((String) messageData.get("subreddit"))
                .wasComment((Boolean) messageData.get("was_comment"))
                .new_((Boolean) messageData.get("new"))
                .build();
        
        publishMessage("reddit.comment.reply", reply);
    }

    private void processPrivateMessage(Map<String, Object> message) {
        Map<String, Object> messageData = (Map<String, Object>) message.get("data");
        
        RedditMessage pm = RedditMessage.builder()
                .id((String) messageData.get("id"))
                .type("private_message")
                .author((String) messageData.get("author"))
                .dest((String) messageData.get("dest"))
                .subject((String) messageData.get("subject"))
                .body((String) messageData.get("body"))
                .created(convertTimestamp(messageData.get("created_utc")))
                .new_((Boolean) messageData.get("new"))
                .build();
        
        publishMessage("reddit.message.received", pm);
    }

    private void processMention(Map<String, Object> mention) {
        Map<String, Object> mentionData = (Map<String, Object>) mention.get("data");
        
        RedditMessage mentionMsg = RedditMessage.builder()
                .id((String) mentionData.get("id"))
                .type("username_mention")
                .author((String) mentionData.get("author"))
                .body((String) mentionData.get("body"))
                .context((String) mentionData.get("context"))
                .subreddit((String) mentionData.get("subreddit"))
                .created(convertTimestamp(mentionData.get("created_utc")))
                .new_((Boolean) mentionData.get("new"))
                .build();
        
        publishMessage("reddit.mention.received", mentionMsg);
    }

    private void processModQueueItem(Map<String, Object> item) {
        Map<String, Object> itemData = (Map<String, Object>) item.get("data");
        String kind = (String) item.get("kind");
        
        RedditModItem modItem = RedditModItem.builder()
                .id((String) itemData.get("id"))
                .kind(kind)
                .author((String) itemData.get("author"))
                .subreddit((String) itemData.get("subreddit"))
                .title(kind.equals("t3") ? (String) itemData.get("title") : null)
                .body(kind.equals("t1") ? (String) itemData.get("body") : null)
                .reports((List<List<Object>>) itemData.get("mod_reports"))
                .userReports((List<List<Object>>) itemData.get("user_reports"))
                .numReports(((Number) itemData.get("num_reports")).intValue())
                .approved((Boolean) itemData.get("approved"))
                .removed((Boolean) itemData.get("removed"))
                .spam((Boolean) itemData.get("spam"))
                .build();
        
        publishMessage("reddit.modqueue.item", modItem);
    }

    private void processReport(Map<String, Object> report) {
        publishMessage("reddit.report.received", report);
    }

    @Override
    public void processWebhookEvent(Map<String, Object> event) {
        // Reddit doesn't have a traditional webhook system
        // Instead it uses websockets for real-time events
        // This method would be used if implementing a custom webhook receiver
        
        String eventType = (String) event.get("type");
        
        switch (eventType) {
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
    protected String getAdapterType() {
        return "REDDIT";
    }

    @Override
    protected List<EventType> getSupportedEventTypes() {
        return Arrays.asList(
                EventType.SOCIAL_MEDIA_POST,
                EventType.SOCIAL_MEDIA_COMMENT,
                EventType.SOCIAL_MEDIA_MESSAGE,
                EventType.SOCIAL_MEDIA_MENTION,
                EventType.SOCIAL_MEDIA_MODERATION,
                EventType.SOCIAL_MEDIA_VOTE
        );
    }

    private LocalDateTime convertTimestamp(Object timestamp) {
        if (timestamp instanceof Number) {
            long epochSeconds = ((Number) timestamp).longValue();
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
        }
        return LocalDateTime.now();
    }

    private List<String> extractAwards(Map<String, Object> data) {
        List<Map<String, Object>> allAwardings = (List<Map<String, Object>>) data.get("all_awardings");
        if (allAwardings != null) {
            return allAwardings.stream()
                    .map(award -> (String) award.get("name"))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // Data classes for Reddit entities
    @Data
    @lombok.Builder
    public static class RedditPost {
        private String id;
        private String name;
        private String title;
        private String author;
        private String subreddit;
        private String selftext;
        private String url;
        private String permalink;
        private LocalDateTime created;
        private int score;
        private double upvoteRatio;
        private int numComments;
        private boolean over18;
        private boolean spoiler;
        private boolean locked;
        private boolean stickied;
        private String distinguished;
        private String linkFlairText;
        private String postHint;
        private Object preview;
        private Object media;
        private Object mediaEmbed;
        private Object gallery;
        private Object poll;
        private List<String> crosspostParent;
        private List<String> awards;
    }

    @Data
    @lombok.Builder
    public static class RedditComment {
        private String id;
        private String name;
        private String parentId;
        private String linkId;
        private String author;
        private String body;
        private LocalDateTime created;
        private int score;
        private Object edited;
        private String distinguished;
        private boolean stickied;
        private boolean scoreHidden;
        private boolean locked;
        private String subreddit;
        private String authorFlairText;
        private List<String> awards;
    }

    @Data
    @lombok.Builder
    public static class RedditMessage {
        private String id;
        private String type;
        private String author;
        private String dest;
        private String subject;
        private String body;
        private LocalDateTime created;
        private String context;
        private String subreddit;
        private Boolean wasComment;
        private Boolean new_;
    }

    @Data
    @lombok.Builder
    public static class RedditModItem {
        private String id;
        private String kind;
        private String author;
        private String subreddit;
        private String title;
        private String body;
        private List<List<Object>> reports;
        private List<List<Object>> userReports;
        private int numReports;
        private boolean approved;
        private boolean removed;
        private boolean spam;
    }
}