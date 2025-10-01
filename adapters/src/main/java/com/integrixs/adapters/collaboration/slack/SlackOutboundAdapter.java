package com.integrixs.adapters.collaboration.slack;
import com.integrixs.adapters.domain.model.AdapterConfiguration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.collaboration.slack.SlackApiConfig.*;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SlackOutboundAdapter extends AbstractSocialMediaOutboundAdapter {

    @Autowired
    public SlackOutboundAdapter(RateLimiterService rateLimiterService,
                                CredentialEncryptionService credentialEncryptionService) {
        super(rateLimiterService, credentialEncryptionService);
    }




    @Override


    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.SLACK;
    }




    @Override


    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> adapterConfig = new HashMap<>();
        adapterConfig.put("botToken", config.getBotToken());
        adapterConfig.put("appToken", config.getAppToken());
        adapterConfig.put("signingSecret", config.getSigningSecret());
        adapterConfig.put("features", Map.of(
            "enableMessaging", config.getFeatures().isEnableMessaging(),
            "enableFiles", config.getFeatures().isEnableFiles(),
            "enableChannels", config.getFeatures().isEnableChannels(),
            "enableReactions", config.getFeatures().isEnableReactions(),
            "enableSocketMode", config.getFeatures().isEnableSocketMode(),
            "enableAdminApis", config.getFeatures().isEnableAdminApis()
       ));
        adapterConfig.put("limits", Map.of(
            "maxMessagesPerRequest", config.getLimits().getMaxMessagesPerRequest(),
            "maxUsersPerRequest", config.getLimits().getMaxUsersPerRequest(),
            "maxChannelsPerRequest", config.getLimits().getMaxChannelsPerRequest(),
            "maxFileSizeMb", config.getLimits().getMaxFileSizeMb(),
            "websocketReconnectMaxAttempts", config.getLimits().getWebsocketReconnectMaxAttempts()
       ));
        return adapterConfig;
    }




    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    private static final Logger log = LoggerFactory.getLogger(SlackOutboundAdapter.class);



    @Autowired
    private SlackApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Rate limit tracking
    private final Map<String, Long> rateLimitResetTimes = new ConcurrentHashMap<>();




            @Override


            public MessageDTO processMessage(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String action = content.get("action").asText();

            log.info("Processing Slack action: {}", action);

            MessageDTO response = null;

            switch(action) {
                // Messaging
                case "send_message":
                    response = sendMessage(content);
                    break;
                case "update_message":
                    response = updateMessage(content);
                    break;
                case "delete_message":
                    response = deleteMessage(content);
                    break;
                case "send_ephemeral":
                    response = sendEphemeralMessage(content);
                    break;
                case "schedule_message":
                    response = scheduleMessage(content);
                    break;

                // Conversations/Channels
                case "create_channel":
                    response = createChannel(content);
                    break;
                case "archive_channel":
                    response = archiveChannel(content);
                    break;
                case "unarchive_channel":
                    response = unarchiveChannel(content);
                    break;
                case "rename_channel":
                    response = renameChannel(content);
                    break;
                case "set_channel_topic":
                    response = setChannelTopic(content);
                    break;
                case "set_channel_purpose":
                    response = setChannelPurpose(content);
                    break;
                case "invite_to_channel":
                    response = inviteToChannel(content);
                    break;
                case "kick_from_channel":
                    response = kickFromChannel(content);
                    break;
                case "join_channel":
                    response = joinChannel(content);
                    break;
                case "leave_channel":
                    response = leaveChannel(content);
                    break;

                // Files
                case "upload_file":
                    response = uploadFile(content);
                    break;
                case "delete_file":
                    response = deleteFile(content);
                    break;
                case "share_file":
                    response = shareFile(content);
                    break;
                case "create_snippet":
                    response = createSnippet(content);
                    break;

                // Reactions
                case "add_reaction":
                    response = addReaction(content);
                    break;
                case "remove_reaction":
                    response = removeReaction(content);
                    break;

                // Pins
                case "pin_message":
                    response = pinMessage(content);
                    break;
                case "unpin_message":
                    response = unpinMessage(content);
                    break;

                // Bookmarks
                case "add_bookmark":
                    response = addBookmark(content);
                    break;
                case "edit_bookmark":
                    response = editBookmark(content);
                    break;
                case "remove_bookmark":
                    response = removeBookmark(content);
                    break;

                // User interactions
                case "open_dialog":
                    response = openDialog(content);
                    break;
                case "open_modal":
                    response = openModal(content);
                    break;
                case "update_modal":
                    response = updateModal(content);
                    break;
                case "push_modal":
                    response = pushModal(content);
                    break;

                // User profile
                case "update_user_status":
                    response = updateUserStatus(content);
                    break;
                case "set_user_presence":
                    response = setUserPresence(content);
                    break;

                // Workflows
                case "trigger_workflow":
                    response = triggerWorkflow(content);
                    break;
                case "update_workflow_step":
                    response = updateWorkflowStep(content);
                    break;

                // Reminders
                case "add_reminder":
                    response = addReminder(content);
                    break;
                case "delete_reminder":
                    response = deleteReminder(content);
                    break;

                // Search
                case "search_messages":
                    response = searchMessages(content);
                    break;
                case "search_files":
                    response = searchFiles(content);
                    break;

                // App Home
                case "publish_app_home":
                    response = publishAppHome(content);
                    break;

                // Canvas
                case "create_canvas":
                    response = createCanvas(content);
                    break;
                case "update_canvas":
                    response = updateCanvas(content);
                    break;

                // Admin functions(Enterprise)
                case "invite_user":
                    response = inviteUser(content);
                    break;
                case "remove_user":
                    response = removeUser(content);
                    break;
                case "set_user_admin":
                    response = setUserAdmin(content);
                    break;

                default:
                    log.warn("Unknown action: {}", action);
                    response = createErrorResponse("Unknown action: " + action);
            }

            if(response != null) {
                publishResponse(response, message);
            }

            return response != null ? response : createSuccessResponse(message.getCorrelationId(), Map.of("status", "processed"));
        } catch(Exception e) {
            log.error("Error processing Slack message", e);
            publishErrorResponse(e, message);
            return createErrorResponse("Error processing message: " + e.getMessage());
        }
    }

    // Messaging methods
    private MessageDTO sendMessage(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());

        // Message content
        if(content.has("text")) {
            params.put("text", content.get("text").asText());
        }

        // Blocks for rich formatting
        if(content.has("blocks")) {
            params.put("blocks", content.get("blocks"));
        }

        // Attachments(legacy)
        if(content.has("attachments")) {
            params.put("attachments", content.get("attachments"));
        }

        // Threading
        if(content.has("thread_ts")) {
            params.put("thread_ts", content.get("thread_ts").asText());
        }

        // Additional options
        if(content.has("reply_broadcast")) {
            params.put("reply_broadcast", content.get("reply_broadcast").asBoolean());
        }
        if(content.has("unfurl_links")) {
            params.put("unfurl_links", content.get("unfurl_links").asBoolean());
        }
        if(content.has("unfurl_media")) {
            params.put("unfurl_media", content.get("unfurl_media").asBoolean());
        }
        if(content.has("link_names")) {
            params.put("link_names", content.get("link_names").asBoolean());
        }
        if(content.has("mrkdwn")) {
            params.put("mrkdwn", content.get("mrkdwn").asBoolean());
        }
        if(content.has("icon_emoji")) {
            params.put("icon_emoji", content.get("icon_emoji").asText());
        }
        if(content.has("icon_url")) {
            params.put("icon_url", content.get("icon_url").asText());
        }
        if(content.has("username")) {
            params.put("username", content.get("username").asText());
        }

        JsonNode response = makeApiRequest("chat.postMessage", params);
        return createSuccessResponse(response);
    }

    private MessageDTO updateMessage(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("ts", content.get("ts").asText());

        if(content.has("text")) {
            params.put("text", content.get("text").asText());
        }
        if(content.has("blocks")) {
            params.put("blocks", content.get("blocks"));
        }
        if(content.has("attachments")) {
            params.put("attachments", content.get("attachments"));
        }

        JsonNode response = makeApiRequest("chat.update", params);
        return createSuccessResponse(response);
    }

    private MessageDTO deleteMessage(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("ts", content.get("ts").asText());

        JsonNode response = makeApiRequest("chat.delete", params);
        return createSuccessResponse(response);
    }

    private MessageDTO sendEphemeralMessage(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("user", content.get("user").asText());
        params.put("text", content.get("text").asText());

        if(content.has("blocks")) {
            params.put("blocks", content.get("blocks"));
        }
        if(content.has("attachments")) {
            params.put("attachments", content.get("attachments"));
        }
        if(content.has("thread_ts")) {
            params.put("thread_ts", content.get("thread_ts").asText());
        }

        JsonNode response = makeApiRequest("chat.postEphemeral", params);
        return createSuccessResponse(response);
    }

    private MessageDTO scheduleMessage(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("text", content.get("text").asText());

        // Convert schedule time
        long postAt = content.get("post_at").asLong();
        params.put("post_at", postAt);

        if(content.has("blocks")) {
            params.put("blocks", content.get("blocks"));
        }
        if(content.has("thread_ts")) {
            params.put("thread_ts", content.get("thread_ts").asText());
        }

        JsonNode response = makeApiRequest("chat.scheduleMessage", params);
        return createSuccessResponse(response);
    }

    // Channel management methods
    private MessageDTO createChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("name", content.get("name").asText());

        if(content.has("is_private")) {
            params.put("is_private", content.get("is_private").asBoolean());
        }
        if(content.has("team_id")) {
            params.put("team_id", content.get("team_id").asText());
        }

        JsonNode response = makeApiRequest("conversations.create", params);
        return createSuccessResponse(response);
    }

    private MessageDTO archiveChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());

        JsonNode response = makeApiRequest("conversations.archive", params);
        return createSuccessResponse(response);
    }

    private MessageDTO unarchiveChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());

        JsonNode response = makeApiRequest("conversations.unarchive", params);
        return createSuccessResponse(response);
    }

    private MessageDTO renameChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("name", content.get("name").asText());

        JsonNode response = makeApiRequest("conversations.rename", params);
        return createSuccessResponse(response);
    }

    private MessageDTO setChannelTopic(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("topic", content.get("topic").asText());

        JsonNode response = makeApiRequest("conversations.setTopic", params);
        return createSuccessResponse(response);
    }

    private MessageDTO setChannelPurpose(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("purpose", content.get("purpose").asText());

        JsonNode response = makeApiRequest("conversations.setPurpose", params);
        return createSuccessResponse(response);
    }

    private MessageDTO inviteToChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());

        if(content.has("users")) {
            List<String> users = new ArrayList<>();
            content.get("users").forEach(user -> users.add(user.asText()));
            params.put("users", String.join(",", users));
        }

        JsonNode response = makeApiRequest("conversations.invite", params);
        return createSuccessResponse(response);
    }

    private MessageDTO kickFromChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("user", content.get("user").asText());

        JsonNode response = makeApiRequest("conversations.kick", params);
        return createSuccessResponse(response);
    }

    private MessageDTO joinChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());

        JsonNode response = makeApiRequest("conversations.join", params);
        return createSuccessResponse(response);
    }

    private MessageDTO leaveChannel(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());

        JsonNode response = makeApiRequest("conversations.leave", params);
        return createSuccessResponse(response);
    }

    // File handling methods
    private MessageDTO uploadFile(JsonNode content) throws Exception {
        String filename = content.get("filename").asText();
        String fileContent = content.get("content").asText();
        byte[] fileBytes = Base64.getDecoder().decode(fileContent);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();

        Resource fileResource = new ByteArrayResource(fileBytes) {


            @Override

            public String getFilename() {
                return filename;
            }
        };

        params.add("file", fileResource);
        params.add("filename", filename);

        if(content.has("channels")) {
            List<String> channels = new ArrayList<>();
            content.get("channels").forEach(ch -> channels.add(ch.asText()));
            params.add("channels", String.join(",", channels));
        }

        if(content.has("title")) {
            params.add("title", content.get("title").asText());
        }
        if(content.has("initial_comment")) {
            params.add("initial_comment", content.get("initial_comment").asText());
        }
        if(content.has("thread_ts")) {
            params.add("thread_ts", content.get("thread_ts").asText());
        }

        JsonNode response = makeMultipartApiRequest("files.upload", params);
        return createSuccessResponse(response);
    }

    private MessageDTO deleteFile(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("file", content.get("file").asText());

        JsonNode response = makeApiRequest("files.delete", params);
        return createSuccessResponse(response);
    }

    private MessageDTO shareFile(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("file", content.get("file").asText());
        params.put("channel", content.get("channel").asText());

        if(content.has("thread_ts")) {
            params.put("thread_ts", content.get("thread_ts").asText());
        }

        JsonNode response = makeApiRequest("files.sharedPublicURL", params);
        return createSuccessResponse(response);
    }

    private MessageDTO createSnippet(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("content", content.get("content").asText());

        if(content.has("filename")) {
            params.put("filename", content.get("filename").asText());
        }
        if(content.has("filetype")) {
            params.put("filetype", content.get("filetype").asText());
        }
        if(content.has("title")) {
            params.put("title", content.get("title").asText());
        }
        if(content.has("channels")) {
            List<String> channels = new ArrayList<>();
            content.get("channels").forEach(ch -> channels.add(ch.asText()));
            params.put("channels", String.join(",", channels));
        }

        JsonNode response = makeApiRequest("files.upload", params);
        return createSuccessResponse(response);
    }

    // Reaction methods
    private MessageDTO addReaction(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("name", content.get("name").asText());
        params.put("timestamp", content.get("timestamp").asText());

        JsonNode response = makeApiRequest("reactions.add", params);
        return createSuccessResponse(response);
    }

    private MessageDTO removeReaction(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("name", content.get("name").asText());
        params.put("timestamp", content.get("timestamp").asText());

        JsonNode response = makeApiRequest("reactions.remove", params);
        return createSuccessResponse(response);
    }

    // Pin methods
    private MessageDTO pinMessage(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("timestamp", content.get("timestamp").asText());

        JsonNode response = makeApiRequest("pins.add", params);
        return createSuccessResponse(response);
    }

    private MessageDTO unpinMessage(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", content.get("channel").asText());
        params.put("timestamp", content.get("timestamp").asText());

        JsonNode response = makeApiRequest("pins.remove", params);
        return createSuccessResponse(response);
    }

    // Bookmark methods
    private MessageDTO addBookmark(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("channel_id", content.get("channel_id").asText());
        params.put("title", content.get("title").asText());
        params.put("type", content.get("type").asText());

        if(content.has("link")) {
            params.put("link", content.get("link").asText());
        }
        if(content.has("emoji")) {
            params.put("emoji", content.get("emoji").asText());
        }
        if(content.has("entity_id")) {
            params.put("entity_id", content.get("entity_id").asText());
        }
        if(content.has("parent_id")) {
            params.put("parent_id", content.get("parent_id").asText());
        }

        JsonNode response = makeApiRequest("bookmarks.add", params);
        return createSuccessResponse(response);
    }

    private MessageDTO editBookmark(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("bookmark_id", content.get("bookmark_id").asText());
        params.put("channel_id", content.get("channel_id").asText());

        if(content.has("title")) {
            params.put("title", content.get("title").asText());
        }
        if(content.has("link")) {
            params.put("link", content.get("link").asText());
        }
        if(content.has("emoji")) {
            params.put("emoji", content.get("emoji").asText());
        }

        JsonNode response = makeApiRequest("bookmarks.edit", params);
        return createSuccessResponse(response);
    }

    private MessageDTO removeBookmark(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("bookmark_id", content.get("bookmark_id").asText());
        params.put("channel_id", content.get("channel_id").asText());

        JsonNode response = makeApiRequest("bookmarks.remove", params);
        return createSuccessResponse(response);
    }

    // Modal/Dialog methods
    private MessageDTO openDialog(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("trigger_id", content.get("trigger_id").asText());
        params.put("dialog", content.get("dialog"));

        JsonNode response = makeApiRequest("dialog.open", params);
        return createSuccessResponse(response);
    }

    private MessageDTO openModal(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("trigger_id", content.get("trigger_id").asText());
        params.put("view", content.get("view"));

        JsonNode response = makeApiRequest("views.open", params);
        return createSuccessResponse(response);
    }

    private MessageDTO updateModal(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("view_id", content.get("view_id").asText());
        params.put("view", content.get("view"));

        if(content.has("hash")) {
            params.put("hash", content.get("hash").asText());
        }

        JsonNode response = makeApiRequest("views.update", params);
        return createSuccessResponse(response);
    }

    private MessageDTO pushModal(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("trigger_id", content.get("trigger_id").asText());
        params.put("view", content.get("view"));

        JsonNode response = makeApiRequest("views.push", params);
        return createSuccessResponse(response);
    }

    // User profile methods
    private MessageDTO updateUserStatus(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> profile = new HashMap<>();
        profile.put("status_text", content.get("status_text").asText());
        profile.put("status_emoji", content.get("status_emoji").asText());

        if(content.has("status_expiration")) {
            profile.put("status_expiration", content.get("status_expiration").asLong());
        }

        params.put("profile", objectMapper.writeValueAsString(profile));

        JsonNode response = makeApiRequest("users.profile.set", params);
        return createSuccessResponse(response);
    }

    private MessageDTO setUserPresence(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("presence", content.get("presence").asText()); // auto or away

        JsonNode response = makeApiRequest("users.setPresence", params);
        return createSuccessResponse(response);
    }

    // Workflow methods
    private MessageDTO triggerWorkflow(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("workflow_id", content.get("workflow_id").asText());
        params.put("inputs", content.get("inputs"));

        if(content.has("team_id")) {
            params.put("team_id", content.get("team_id").asText());
        }

        JsonNode response = makeApiRequest("workflows.trigger", params);
        return createSuccessResponse(response);
    }

    private MessageDTO updateWorkflowStep(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("workflow_step_execute_id", content.get("workflow_step_execute_id").asText());

        if(content.has("outputs")) {
            params.put("outputs", content.get("outputs"));
        }

        String endpoint = content.has("error") ? "workflows.stepFailed" : "workflows.stepCompleted";

        if(content.has("error")) {
            params.put("error", content.get("error"));
        }

        JsonNode response = makeApiRequest(endpoint, params);
        return createSuccessResponse(response);
    }

    // Reminder methods
    private MessageDTO addReminder(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("text", content.get("text").asText());
        params.put("time", content.get("time").asText()); // Can be timestamp or natural language

        if(content.has("user")) {
            params.put("user", content.get("user").asText());
        }

        JsonNode response = makeApiRequest("reminders.add", params);
        return createSuccessResponse(response);
    }

    private MessageDTO deleteReminder(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("reminder", content.get("reminder").asText());

        JsonNode response = makeApiRequest("reminders.delete", params);
        return createSuccessResponse(response);
    }

    // Search methods
    private MessageDTO searchMessages(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("query", content.get("query").asText());

        if(content.has("count")) {
            params.put("count", content.get("count").asInt());
        }
        if(content.has("page")) {
            params.put("page", content.get("page").asInt());
        }
        if(content.has("sort")) {
            params.put("sort", content.get("sort").asText());
        }
        if(content.has("sort_dir")) {
            params.put("sort_dir", content.get("sort_dir").asText());
        }

        JsonNode response = makeApiRequest("search.messages", params);
        return createSuccessResponse(response);
    }

    private MessageDTO searchFiles(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("query", content.get("query").asText());

        if(content.has("count")) {
            params.put("count", content.get("count").asInt());
        }
        if(content.has("page")) {
            params.put("page", content.get("page").asInt());
        }
        if(content.has("sort")) {
            params.put("sort", content.get("sort").asText());
        }
        if(content.has("sort_dir")) {
            params.put("sort_dir", content.get("sort_dir").asText());
        }

        JsonNode response = makeApiRequest("search.files", params);
        return createSuccessResponse(response);
    }

    // App Home methods
    private MessageDTO publishAppHome(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", content.get("user_id").asText());
        params.put("view", content.get("view"));

        JsonNode response = makeApiRequest("views.publish", params);
        return createSuccessResponse(response);
    }

    // Canvas methods
    private MessageDTO createCanvas(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("title", content.get("title").asText());

        if(content.has("content")) {
            params.put("content", content.get("content"));
        }

        JsonNode response = makeApiRequest("canvases.create", params);
        return createSuccessResponse(response);
    }

    private MessageDTO updateCanvas(JsonNode content) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("canvas_id", content.get("canvas_id").asText());
        params.put("changes", content.get("changes"));

        JsonNode response = makeApiRequest("canvases.edit", params);
        return createSuccessResponse(response);
    }

    // Admin methods
    private MessageDTO inviteUser(JsonNode content) throws Exception {
        if(!config.getFeatures().isEnableAdminApis()) {
            throw new AdapterException("Admin APIs are not enabled");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("email", content.get("email").asText());
        params.put("team_id", content.get("team_id").asText());

        if(content.has("channels")) {
            List<String> channels = new ArrayList<>();
            content.get("channels").forEach(ch -> channels.add(ch.asText()));
            params.put("channels", String.join(",", channels));
        }
        if(content.has("real_name")) {
            params.put("real_name", content.get("real_name").asText());
        }
        if(content.has("resend")) {
            params.put("resend", content.get("resend").asBoolean());
        }

        JsonNode response = makeApiRequest("admin.invites.send", params);
        return createSuccessResponse(response);
    }

    private MessageDTO removeUser(JsonNode content) throws Exception {
        if(!config.getFeatures().isEnableAdminApis()) {
            throw new AdapterException("Admin APIs are not enabled");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", content.get("user_id").asText());
        params.put("team_id", content.get("team_id").asText());

        JsonNode response = makeApiRequest("admin.users.remove", params);
        return createSuccessResponse(response);
    }

    private MessageDTO setUserAdmin(JsonNode content) throws Exception {
        if(!config.getFeatures().isEnableAdminApis()) {
            throw new AdapterException("Admin APIs are not enabled");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", content.get("user_id").asText());
        params.put("team_id", content.get("team_id").asText());
        params.put("is_admin", content.get("is_admin").asBoolean());

        JsonNode response = makeApiRequest("admin.users.setAdmin", params);
        return createSuccessResponse(response);
    }

    // API request helpers
    private JsonNode makeApiRequest(String method, Map<String, Object> params) throws Exception {
        return makeApiRequestWithRetry(method, params, 0);
    }

    private JsonNode makeApiRequestWithRetry(String method, Map<String, Object> params, int attempt) throws Exception {
        try {
            // Check rate limit
            if(isRateLimited(method)) {
                long waitTime = rateLimitResetTimes.get(method) - System.currentTimeMillis();
                if(waitTime > 0) {
                    log.warn("Rate limited for method {}. Waiting {} ms", method, waitTime);
                    Thread.sleep(waitTime);
                }
                rateLimitResetTimes.remove(method);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getBotToken());

            String url = config.getApiUrl() + method;

            HttpEntity<Map<String, Object>> entity = params != null ?
                new HttpEntity<>(params, headers) : new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
           );

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if(!responseJson.get("ok").asBoolean()) {
                String error = responseJson.get("error").asText();

                // Handle retryable errors
                if(error.equals("rate_limited") && attempt < config.getMaxRetryAttempts()) {
                    int retryAfter = responseJson.path("retry_after").asInt(60);
                    log.warn("Rate limited. Retrying after {} seconds", retryAfter);
                    Thread.sleep(retryAfter * 1000);
                    return makeApiRequestWithRetry(method, params, attempt + 1);
                }

                throw new AdapterException("Slack API error: " + error);
            }

            return responseJson;
        } catch(HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && attempt < config.getMaxRetryAttempts()) {
                String retryAfter = e.getResponseHeaders().getFirst("Retry-After");
                int delay = retryAfter != null ? Integer.parseInt(retryAfter) : 60;

                log.warn("HTTP 429 Rate Limited. Retrying after {} seconds", delay);
                Thread.sleep(delay * 1000);
                return makeApiRequestWithRetry(method, params, attempt + 1);
            }

            throw new AdapterException("Slack API request failed: " + e.getMessage(), e);
        }
    }

    private JsonNode makeMultipartApiRequest(String method, MultiValueMap<String, Object> params) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(config.getBotToken());

            String url = config.getApiUrl() + method;

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
           );

            JsonNode responseJson = objectMapper.readTree(response.getBody());

            if(!responseJson.get("ok").asBoolean()) {
                throw new AdapterException("Slack API error: " + responseJson.get("error").asText());
            }

            return responseJson;
        } catch(HttpClientErrorException e) {
            throw new AdapterException("Slack API request failed: " + e.getMessage(), e);
        }
    }

    private boolean isRateLimited(String method) {
        Long resetTime = rateLimitResetTimes.get(method);
        return resetTime != null && System.currentTimeMillis() < resetTime;
    }

    private MessageDTO createSuccessResponse(JsonNode response) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", response);

        MessageDTO message = new MessageDTO();
        message.setType("slack_response");
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        try {
            message.setPayload(objectMapper.writeValueAsString(result));
        } catch(Exception e) {
            message.setPayload(result.toString());
        }
        message.setMessageTimestamp(Instant.now());
        return message;
    }

    private MessageDTO createSuccessResponse(String correlationId, Map<String, Object> data) {
        MessageDTO response = new MessageDTO();
        response.setType("slack_response");
        response.setCorrelationId(correlationId);
        try {
            response.setPayload(objectMapper.writeValueAsString(data));
        } catch(Exception e) {
            response.setPayload(data.toString());
        }
        response.setMessageTimestamp(Instant.now());
        return response;
    }

    private MessageDTO createErrorResponse(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", error);

        MessageDTO message = new MessageDTO();
        message.setType("slack_response");
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        try {
            message.setPayload(objectMapper.writeValueAsString(result));
        } catch(Exception e) {
            message.setPayload(result.toString());
        }
        message.setMessageTimestamp(Instant.now());
        return message;
    }

    private MessageDTO createMessageDTOWithValues(String type, String category, Map<String, Object> content, long timestamp) {
        MessageDTO message = new MessageDTO();
        message.setType(type);
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        try {
            message.setPayload(objectMapper.writeValueAsString(content));
        } catch(Exception e) {
            message.setPayload(content.toString());
        }
        message.setMessageTimestamp(Instant.ofEpochMilli(timestamp));
        return message;
    }


    protected void publishResponse(MessageDTO response, MessageDTO originalMessage) {
        // TODO: Implement response publishing logic
        log.debug("Publishing response for message {}", originalMessage.getCorrelationId());
    }

    protected void publishErrorResponse(Exception e, MessageDTO originalMessage) {
        // TODO: Implement error response publishing logic
        log.error("Error processing message {}: {}", originalMessage.getCorrelationId(), e.getMessage());
    }

    // Abstract methods from AbstractOutboundAdapter

    @Override
    protected void doReceiverInitialize() throws Exception {
        // No specific receiver initialization needed for Slack outbound adapter
        log.debug("Slack outbound adapter receiver initialized");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // No specific receiver cleanup needed for Slack outbound adapter
        log.debug("Slack outbound adapter receiver destroyed");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Slack outbound adapter doesn't implement receive functionality
        // It's primarily for sending messages/actions to Slack
        return AdapterResult.success(null, "Receive not supported for Slack outbound adapter");
    }

    @Override
    protected long getPollingIntervalMs() {
        // Slack outbound adapter doesn't use polling
        return 0;
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        // Test connection by calling auth.test
        try {
            Map<String, Object> params = new HashMap<>();
            JsonNode response = makeApiRequest("auth.test", params);

            if (response != null && response.has("ok") && response.get("ok").asBoolean()) {
                String team = response.path("team").asText();
                String user = response.path("user").asText();
                return AdapterResult.success(String.format("Connected to Slack workspace: %s as user: %s", team, user));
            } else {
                return AdapterResult.failure("Failed to connect to Slack API");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
        }
    }
}
