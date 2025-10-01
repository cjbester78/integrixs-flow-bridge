package com.integrixs.adapters.collaboration.teams;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaOutboundAdapter;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import com.integrixs.adapters.collaboration.teams.MicrosoftTeamsApiConfig.*;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class MicrosoftTeamsOutboundAdapter extends AbstractSocialMediaOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(MicrosoftTeamsOutboundAdapter.class);

    @Autowired
    public MicrosoftTeamsOutboundAdapter(RateLimiterService rateLimiterService,
                                         CredentialEncryptionService credentialEncryptionService) {
        super(rateLimiterService, credentialEncryptionService);
    }

    private static final String LOGIN_URL = "https://login.microsoftonline.com/%s/oauth2/v2.0/token";
    private static final long TOKEN_REFRESH_BUFFER = 300000; // 5 minutes before expiry
    private static final int MAX_BATCH_REQUESTS = 20;

    @Autowired
    private MicrosoftTeamsApiConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Authentication
    private String accessToken;
    private long tokenExpiry;
    private final Object tokenLock = new Object();

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        Map<String, Object> adapterConfig = new HashMap<>();
        adapterConfig.put("tenantId", config.getTenantId());
        adapterConfig.put("clientId", config.getClientId());
        adapterConfig.put("features", Map.of(
            "enableMessaging", config.getFeatures().isEnableMessaging(),
            "enableChannels", config.getFeatures().isEnableChannels(),
            "enableMeetings", config.getFeatures().isEnableMeetings(),
            "enableFiles", config.getFeatures().isEnableFiles()
        ));
        adapterConfig.put("limits", Map.of(
            "maxFileSize", config.getLimits().getMaxFileSizeMb()
        ));
        return adapterConfig;
    }

    @Override
    protected SocialMediaAdapterConfig getConfig() {
        return config;
    }

    @Override
    public MessageDTO processMessage(MessageDTO message) {
        try {
            JsonNode content = objectMapper.readTree(message.getPayload());
            String action = content.get("action").asText();

            log.info("Processing Teams action: {}", action);

            MessageDTO response = null;

            switch(action) {
                // Messaging
                case "send_message":
                    response = sendMessage(content);
                    break;
                case "reply_message":
                    response = replyToMessage(content);
                    break;
                case "update_message":
                    response = updateMessage(content);
                    break;
                case "delete_message":
                    response = deleteMessage(content);
                    break;
                case "send_activity_notification":
                    response = sendActivityNotification(content);
                    break;

                // Chat management
                case "create_chat":
                    response = createChat(content);
                    break;
                case "get_chat":
                    response = getChat(content);
                    break;
                case "update_chat":
                    response = updateChat(content);
                    break;
                case "add_chat_members":
                    response = addChatMembers(content);
                    break;
                case "remove_chat_member":
                    response = removeChatMember(content);
                    break;

                // Teams management
                case "create_team":
                    response = createTeam(content);
                    break;
                case "update_team":
                    response = updateTeam(content);
                    break;
                case "archive_team":
                    response = archiveTeam(content);
                    break;
                case "unarchive_team":
                    response = unarchiveTeam(content);
                    break;
                case "add_team_member":
                    response = addTeamMember(content);
                    break;
                case "remove_team_member":
                    response = removeTeamMember(content);
                    break;

                // Channel management
                case "create_channel":
                    response = createChannel(content);
                    break;
                case "update_channel":
                    response = updateChannel(content);
                    break;
                case "delete_channel":
                    response = deleteChannel(content);
                    break;
                case "add_channel_member":
                    response = addChannelMember(content);
                    break;
                case "remove_channel_member":
                    response = removeChannelMember(content);
                    break;

                // Meetings
                case "create_meeting":
                    response = createOnlineMeeting(content);
                    break;
                case "update_meeting":
                    response = updateOnlineMeeting(content);
                    break;
                case "delete_meeting":
                    response = deleteOnlineMeeting(content);
                    break;
                case "get_meeting_attendance":
                    response = getMeetingAttendance(content);
                    break;

                // Files and tabs
                case "upload_file":
                    response = uploadFile(content);
                    break;
                case "share_file":
                    response = shareFile(content);
                    break;
                case "create_tab":
                    response = createChannelTab(content);
                    break;
                case "update_tab":
                    response = updateChannelTab(content);
                    break;
                case "delete_tab":
                    response = deleteChannelTab(content);
                    break;

                // Bot activities
                case "send_bot_message":
                    response = sendBotMessage(content);
                    break;
                case "update_bot_message":
                    response = updateBotMessage(content);
                    break;
                case "send_adaptive_card":
                    response = sendAdaptiveCard(content);
                    break;
                case "send_proactive_message":
                    response = sendProactiveMessage(content);
                    break;

                // Apps
                case "install_app":
                    response = installApp(content);
                    break;
                case "uninstall_app":
                    response = uninstallApp(content);
                    break;
                case "update_app":
                    response = updateApp(content);
                    break;

                // Batch operations
                case "batch_request":
                    response = executeBatchRequest(content);
                    break;

                // Search
                case "search_messages":
                    response = searchMessages(content);
                    break;
                case "search_teams":
                    response = searchTeams(content);
                    break;

                // Presence
                case "get_presence":
                    response = getUserPresence(content);
                    break;
                case "set_presence":
                    response = setUserPresence(content);
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
            log.error("Error processing Teams message", e);
            publishErrorResponse(e, message);
            return createErrorResponse("Error processing message: " + e.getMessage());
        }
    }

    // Token management
    private void refreshAccessToken() {
        try {
            synchronized(tokenLock) {
                log.info("Refreshing Microsoft Teams access token");

                String tokenUrl = String.format(LOGIN_URL, config.getTenantId());

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("client_id", config.getClientId());
                params.add("client_secret", config.getClientSecret());
                params.add("scope", "https://graph.microsoft.com/.default");
                params.add("grant_type", "client_credentials");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, JsonNode.class
               );

                JsonNode tokenResponse = response.getBody();
                accessToken = tokenResponse.get("access_token").asText();
                long expiresIn = tokenResponse.get("expires_in").asLong();
                tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000);

                log.info("Successfully refreshed access token");
            }
        } catch(Exception e) {
            log.error("Error refreshing access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    private String getValidAccessToken() {
        synchronized(tokenLock) {
            if(accessToken == null || System.currentTimeMillis() >(tokenExpiry - TOKEN_REFRESH_BUFFER)) {
                refreshAccessToken();
            }
            return accessToken;
        }
    }

    // Messaging methods
    private MessageDTO sendMessage(JsonNode content) throws Exception {
        String endpoint;
        if(content.has("teamId") && content.has("channelId")) {
            endpoint = String.format("/teams/%s/channels/%s/messages",
                content.get("teamId").asText(),
                content.get("channelId").asText());
        } else if(content.has("chatId")) {
            endpoint = String.format("/chats/%s/messages", content.get("chatId").asText());
        } else {
            throw new AdapterException("Either teamId/channelId or chatId must be specified");
        }

        Map<String, Object> message = new HashMap<>();

        // Message body
        Map<String, Object> body = new HashMap<>();
        body.put("content", content.get("text").asText());
        body.put("contentType", content.has("contentType") ?
            content.get("contentType").asText() : "text");
        message.put("body", body);

        // Importance
        if(content.has("importance")) {
            message.put("importance", content.get("importance").asText());
        }

        // Mentions
        if(content.has("mentions")) {
            message.put("mentions", processMentions(content.get("mentions")));
        }

        // Attachments
        if(content.has("attachments")) {
            message.put("attachments", content.get("attachments"));
        }

        // Hosted contents(inline images)
        if(content.has("hostedContents")) {
            message.put("hostedContents", content.get("hostedContents"));
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, message);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO replyToMessage(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s/messages/%s/replies",
            content.get("teamId").asText(),
            content.get("channelId").asText(),
            content.get("messageId").asText());

        Map<String, Object> reply = new HashMap<>();

        Map<String, Object> body = new HashMap<>();
        body.put("content", content.get("text").asText());
        body.put("contentType", content.has("contentType") ?
            content.get("contentType").asText() : "text");
        reply.put("body", body);

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, reply);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO updateMessage(JsonNode content) throws Exception {
        String endpoint;
        if(content.has("teamId") && content.has("channelId")) {
            if(content.has("replyId")) {
                endpoint = String.format("/teams/%s/channels/%s/messages/%s/replies/%s",
                    content.get("teamId").asText(),
                    content.get("channelId").asText(),
                    content.get("messageId").asText(),
                    content.get("replyId").asText());
            } else {
                endpoint = String.format("/teams/%s/channels/%s/messages/%s",
                    content.get("teamId").asText(),
                    content.get("channelId").asText(),
                    content.get("messageId").asText());
            }
        } else if(content.has("chatId")) {
            endpoint = String.format("/chats/%s/messages/%s",
                content.get("chatId").asText(),
                content.get("messageId").asText());
        } else {
            throw new AdapterException("Either teamId/channelId or chatId must be specified");
        }

        Map<String, Object> update = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        body.put("content", content.get("text").asText());
        update.put("body", body);

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.PATCH, update);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO deleteMessage(JsonNode content) throws Exception {
        String endpoint;
        if(content.has("teamId") && content.has("channelId")) {
            endpoint = String.format("/teams/%s/channels/%s/messages/%s",
                content.get("teamId").asText(),
                content.get("channelId").asText(),
                content.get("messageId").asText());
        } else if(content.has("chatId")) {
            endpoint = String.format("/chats/%s/messages/%s",
                content.get("chatId").asText(),
                content.get("messageId").asText());
        } else {
            throw new AdapterException("Either teamId/channelId or chatId must be specified");
        }

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("deleted", true));
    }

    private MessageDTO sendActivityNotification(JsonNode content) throws Exception {
        String endpoint = String.format("/users/%s/teamwork/sendActivityNotification",
            content.get("userId").asText());

        Map<String, Object> notification = new HashMap<>();
        notification.put("topic", content.get("topic"));
        notification.put("activityType", content.get("activityType").asText());
        notification.put("chainId", content.get("chainId").asLong());
        notification.put("previewText", content.get("previewText"));
        notification.put("templateParameters", content.get("templateParameters"));

        if(content.has("teamsAppId")) {
            notification.put("teamsAppId", content.get("teamsAppId").asText());
        }

        makeGraphApiRequest(endpoint, HttpMethod.POST, notification);
        return createSuccessResponse(objectMapper.createObjectNode().put("sent", true));
    }

    // Chat management methods
    private MessageDTO createChat(JsonNode content) throws Exception {
        Map<String, Object> chat = new HashMap<>();
        chat.put("chatType", content.has("chatType") ?
            content.get("chatType").asText() : "oneOnOne");

        if(content.has("topic")) {
            chat.put("topic", content.get("topic").asText());
        }

        // Members
        List<Map<String, Object>> members = new ArrayList<>();
        for(JsonNode member : content.get("members")) {
            Map<String, Object> memberData = new HashMap<>();
            memberData.put("@odata.type", "#microsoft.graph.aadUserConversationMember");
            memberData.put("roles", member.has("roles") ?
                objectMapper.convertValue(member.get("roles"), List.class) :
                Arrays.asList("owner"));
            memberData.put("user@odata.bind",
                String.format("https://graph.microsoft.com/v1.0/users('%s')",
                    member.get("userId").asText()));
            members.add(memberData);
        }
        chat.put("members", members);

        JsonNode response = makeGraphApiRequest("/chats", HttpMethod.POST, chat);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO getChat(JsonNode content) throws Exception {
        String endpoint = String.format("/chats/%s", content.get("chatId").asText());

        if(content.has("expand")) {
            endpoint += "?$expand = " + content.get("expand").asText();
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.GET, null);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO updateChat(JsonNode content) throws Exception {
        String endpoint = String.format("/chats/%s", content.get("chatId").asText());

        Map<String, Object> update = new HashMap<>();
        if(content.has("topic")) {
            update.put("topic", content.get("topic").asText());
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.PATCH, update);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO addChatMembers(JsonNode content) throws Exception {
        String chatId = content.get("chatId").asText();

        for(JsonNode member : content.get("members")) {
            String endpoint = String.format("/chats/%s/members", chatId);

            Map<String, Object> memberData = new HashMap<>();
            memberData.put("@odata.type", "#microsoft.graph.aadUserConversationMember");
            memberData.put("user@odata.bind",
                String.format("https://graph.microsoft.com/v1.0/users('%s')",
                    member.get("userId").asText()));
            memberData.put("roles", member.has("roles") ?
                objectMapper.convertValue(member.get("roles"), List.class) :
                new ArrayList<>());

            makeGraphApiRequest(endpoint, HttpMethod.POST, memberData);
        }

        return createSuccessResponse(objectMapper.createObjectNode().put("membersAdded", true));
    }

    private MessageDTO removeChatMember(JsonNode content) throws Exception {
        String endpoint = String.format("/chats/%s/members/%s",
            content.get("chatId").asText(),
            content.get("membershipId").asText());

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("memberRemoved", true));
    }

    // Team management methods
    private MessageDTO createTeam(JsonNode content) throws Exception {
        Map<String, Object> team = new HashMap<>();

        if(content.has("template")) {
            // Create from template
            team.put("template@odata.bind",
                "https://graph.microsoft.com/v1.0/teamsTemplates('" +
                content.get("template").asText() + "')");
        }

        team.put("displayName", content.get("displayName").asText());
        team.put("description", content.get("description").asText());

        if(content.has("visibility")) {
            team.put("visibility", content.get("visibility").asText());
        }

        // Team settings
        if(content.has("memberSettings") || content.has("messagingSettings") ||
            content.has("funSettings") || content.has("guestSettings")) {
            team.put("memberSettings", content.get("memberSettings"));
            team.put("messagingSettings", content.get("messagingSettings"));
            team.put("funSettings", content.get("funSettings"));
            team.put("guestSettings", content.get("guestSettings"));
        }

        // Members
        if(content.has("members")) {
            List<Map<String, Object>> members = new ArrayList<>();
            for(JsonNode member : content.get("members")) {
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("@odata.type", "#microsoft.graph.aadUserConversationMember");
                memberData.put("user@odata.bind",
                    String.format("https://graph.microsoft.com/v1.0/users('%s')",
                        member.get("userId").asText()));
                memberData.put("roles", member.has("roles") ?
                    objectMapper.convertValue(member.get("roles"), List.class) :
                    Arrays.asList("member"));
                members.add(memberData);
            }
            team.put("members", members);
        }

        // Create team(async operation)
        HttpHeaders headers = createAuthHeaders();
        headers.add("Content - Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(team, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            config.getGraphApiUrl() + "/teams",
            HttpMethod.POST,
            entity,
            String.class
       );

        // Get operation location from headers
        String location = response.getHeaders().getFirst("Location");
        if(location != null) {
            // Poll for completion
            JsonNode result = pollAsyncOperation(location);
            return createSuccessResponse(result);
        }

        return createSuccessResponse(objectMapper.createObjectNode().put("created", true));
    }

    private MessageDTO updateTeam(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s", content.get("teamId").asText());

        Map<String, Object> update = new HashMap<>();

        if(content.has("displayName")) {
            update.put("displayName", content.get("displayName").asText());
        }
        if(content.has("description")) {
            update.put("description", content.get("description").asText());
        }
        if(content.has("visibility")) {
            update.put("visibility", content.get("visibility").asText());
        }

        // Settings updates
        if(content.has("memberSettings")) {
            update.put("memberSettings", content.get("memberSettings"));
        }
        if(content.has("messagingSettings")) {
            update.put("messagingSettings", content.get("messagingSettings"));
        }
        if(content.has("funSettings")) {
            update.put("funSettings", content.get("funSettings"));
        }
        if(content.has("guestSettings")) {
            update.put("guestSettings", content.get("guestSettings"));
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.PATCH, update);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO archiveTeam(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/archive", content.get("teamId").asText());

        Map<String, Object> params = new HashMap<>();
        if(content.has("shouldSetSpoSiteReadOnlyForMembers")) {
            params.put("shouldSetSpoSiteReadOnlyForMembers",
                content.get("shouldSetSpoSiteReadOnlyForMembers").asBoolean());
        }

        makeGraphApiRequest(endpoint, HttpMethod.POST, params);
        return createSuccessResponse(objectMapper.createObjectNode().put("archived", true));
    }

    private MessageDTO unarchiveTeam(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/unarchive", content.get("teamId").asText());

        makeGraphApiRequest(endpoint, HttpMethod.POST, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("unarchived", true));
    }

    private MessageDTO addTeamMember(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/members", content.get("teamId").asText());

        Map<String, Object> member = new HashMap<>();
        member.put("@odata.type", "#microsoft.graph.aadUserConversationMember");
        member.put("user@odata.bind",
            String.format("https://graph.microsoft.com/v1.0/users('%s')",
                content.get("userId").asText()));
        member.put("roles", content.has("roles") ?
            objectMapper.convertValue(content.get("roles"), List.class) :
            Arrays.asList("member"));

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, member);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO removeTeamMember(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/members/%s",
            content.get("teamId").asText(),
            content.get("membershipId").asText());

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("removed", true));
    }

    // Channel management methods
    private MessageDTO createChannel(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels", content.get("teamId").asText());

        Map<String, Object> channel = new HashMap<>();
        channel.put("displayName", content.get("displayName").asText());

        if(content.has("description")) {
            channel.put("description", content.get("description").asText());
        }

        channel.put("membershipType", content.has("membershipType") ?
            content.get("membershipType").asText() : "standard");

        // For private channels, add members
        if("private".equals(channel.get("membershipType")) && content.has("members")) {
            List<Map<String, Object>> members = new ArrayList<>();
            for(JsonNode member : content.get("members")) {
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("@odata.type", "#microsoft.graph.aadUserConversationMember");
                memberData.put("user@odata.bind",
                    String.format("https://graph.microsoft.com/v1.0/users('%s')",
                        member.get("userId").asText()));
                memberData.put("roles", member.has("roles") ?
                    objectMapper.convertValue(member.get("roles"), List.class) :
                    Arrays.asList("member"));
                members.add(memberData);
            }
            channel.put("members", members);
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, channel);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO updateChannel(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s",
            content.get("teamId").asText(),
            content.get("channelId").asText());

        Map<String, Object> update = new HashMap<>();

        if(content.has("displayName")) {
            update.put("displayName", content.get("displayName").asText());
        }
        if(content.has("description")) {
            update.put("description", content.get("description").asText());
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.PATCH, update);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO deleteChannel(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s",
            content.get("teamId").asText(),
            content.get("channelId").asText());

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("deleted", true));
    }

    private MessageDTO addChannelMember(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s/members",
            content.get("teamId").asText(),
            content.get("channelId").asText());

        Map<String, Object> member = new HashMap<>();
        member.put("@odata.type", "#microsoft.graph.aadUserConversationMember");
        member.put("user@odata.bind",
            String.format("https://graph.microsoft.com/v1.0/users('%s')",
                content.get("userId").asText()));
        member.put("roles", content.has("roles") ?
            objectMapper.convertValue(content.get("roles"), List.class) :
            Arrays.asList("member"));

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, member);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO removeChannelMember(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s/members/%s",
            content.get("teamId").asText(),
            content.get("channelId").asText(),
            content.get("membershipId").asText());

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("removed", true));
    }

    // Meeting methods
    private MessageDTO createOnlineMeeting(JsonNode content) throws Exception {
        Map<String, Object> meeting = new HashMap<>();
        meeting.put("subject", content.get("subject").asText());

        // Start and end times
        meeting.put("startDateTime", content.get("startDateTime").asText());
        meeting.put("endDateTime", content.get("endDateTime").asText());

        // Participants
        if(content.has("participants")) {
            Map<String, Object> participants = new HashMap<>();

            if(content.get("participants").has("organizer")) {
                participants.put("organizer", createMeetingParticipant(
                    content.get("participants").get("organizer")));
            }

            if(content.get("participants").has("attendees")) {
                List<Map<String, Object>> attendees = new ArrayList<>();
                for(JsonNode attendee : content.get("participants").get("attendees")) {
                    attendees.add(createMeetingParticipant(attendee));
                }
                participants.put("attendees", attendees);
            }

            meeting.put("participants", participants);
        }

        // Settings
        if(content.has("allowedPresenters")) {
            meeting.put("allowedPresenters", content.get("allowedPresenters").asText());
        }
        if(content.has("isEntryExitAnnounced")) {
            meeting.put("isEntryExitAnnounced", content.get("isEntryExitAnnounced").asBoolean());
        }
        if(content.has("lobbyBypassSettings")) {
            meeting.put("lobbyBypassSettings", content.get("lobbyBypassSettings"));
        }

        JsonNode response = makeGraphApiRequest("/me/onlineMeetings", HttpMethod.POST, meeting);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO updateOnlineMeeting(JsonNode content) throws Exception {
        String endpoint = String.format("/me/onlineMeetings/%s",
            content.get("meetingId").asText());

        Map<String, Object> update = new HashMap<>();

        if(content.has("subject")) {
            update.put("subject", content.get("subject").asText());
        }
        if(content.has("startDateTime")) {
            update.put("startDateTime", content.get("startDateTime").asText());
        }
        if(content.has("endDateTime")) {
            update.put("endDateTime", content.get("endDateTime").asText());
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.PATCH, update);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO deleteOnlineMeeting(JsonNode content) throws Exception {
        String endpoint = String.format("/me/onlineMeetings/%s",
            content.get("meetingId").asText());

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("deleted", true));
    }

    private MessageDTO getMeetingAttendance(JsonNode content) throws Exception {
        String endpoint = String.format("/me/onlineMeetings/%s/attendanceReports",
            content.get("meetingId").asText());

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.GET, null);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    // File methods
    private MessageDTO uploadFile(JsonNode content) throws Exception {
        String driveEndpoint;

        if(content.has("teamId")) {
            // Upload to team's SharePoint
            driveEndpoint = String.format("/groups/%s/drive/root:/%s:/content",
                content.get("teamId").asText(),
                content.get("fileName").asText());
        } else if(content.has("userId")) {
            // Upload to user's OneDrive
            driveEndpoint = String.format("/users/%s/drive/root:/%s:/content",
                content.get("userId").asText(),
                content.get("fileName").asText());
        } else {
            throw new AdapterException("Either teamId or userId must be specified");
        }

        byte[] fileContent = Base64.getDecoder().decode(content.get("content").asText());

        HttpHeaders headers = createAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> entity = new HttpEntity<>(fileContent, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            config.getGraphApiUrl() + driveEndpoint,
            HttpMethod.PUT,
            entity,
            JsonNode.class
       );

        return createSuccessResponse(response.getBody());
    }

    private MessageDTO shareFile(JsonNode content) throws Exception {
        String driveItemId = content.get("driveItemId").asText();
        String endpoint = String.format("/drives/%s/items/%s/createLink",
            content.get("driveId").asText(),
            driveItemId);

        Map<String, Object> shareRequest = new HashMap<>();
        shareRequest.put("type", content.has("type") ?
            content.get("type").asText() : "view");
        shareRequest.put("scope", content.has("scope") ?
            content.get("scope").asText() : "organization");

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, shareRequest);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    // Tab methods
    private MessageDTO createChannelTab(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s/tabs",
            content.get("teamId").asText(),
            content.get("channelId").asText());

        Map<String, Object> tab = new HashMap<>();
        tab.put("displayName", content.get("displayName").asText());

        // TeamsApp binding
        tab.put("teamsApp@odata.bind",
            "https://graph.microsoft.com/v1.0/appCatalogs/teamsApps/" +
            content.get("teamsAppId").asText());

        // Configuration
        if(content.has("configuration")) {
            tab.put("configuration", content.get("configuration"));
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, tab);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO updateChannelTab(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s/tabs/%s",
            content.get("teamId").asText(),
            content.get("channelId").asText(),
            content.get("tabId").asText());

        Map<String, Object> update = new HashMap<>();

        if(content.has("displayName")) {
            update.put("displayName", content.get("displayName").asText());
        }
        if(content.has("configuration")) {
            update.put("configuration", content.get("configuration"));
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.PATCH, update);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO deleteChannelTab(JsonNode content) throws Exception {
        String endpoint = String.format("/teams/%s/channels/%s/tabs/%s",
            content.get("teamId").asText(),
            content.get("channelId").asText(),
            content.get("tabId").asText());

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("deleted", true));
    }

    // Bot methods
    private MessageDTO sendBotMessage(JsonNode content) throws Exception {
        // Bot Framework endpoint
        String conversationId = content.get("conversationId").asText();
        String endpoint = String.format("%s/v3/conversations/%s/activities",
            config.getBotFrameworkUrl(),
            conversationId);

        Map<String, Object> activity = new HashMap<>();
        activity.put("type", "message");
        activity.put("from", Map.of(
            "id", config.getBotId(),
            "name", config.getBotName()
       ));
        activity.put("conversation", Map.of("id", conversationId));
        activity.put("text", content.get("text").asText());

        if(content.has("attachments")) {
            activity.put("attachments", content.get("attachments"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(activity, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            endpoint, HttpMethod.POST, entity, JsonNode.class
       );

        return createSuccessResponse(response.getBody());
    }

    private MessageDTO updateBotMessage(JsonNode content) throws Exception {
        String conversationId = content.get("conversationId").asText();
        String activityId = content.get("activityId").asText();
        String endpoint = String.format("%s/v3/conversations/%s/activities/%s",
            config.getBotFrameworkUrl(),
            conversationId,
            activityId);

        Map<String, Object> activity = new HashMap<>();
        activity.put("type", "message");
        activity.put("text", content.get("text").asText());

        if(content.has("attachments")) {
            activity.put("attachments", content.get("attachments"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(activity, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            endpoint, HttpMethod.PUT, entity, JsonNode.class
       );

        return createSuccessResponse(response.getBody());
    }

    private MessageDTO sendAdaptiveCard(JsonNode content) throws Exception {
        Map<String, Object> activity = new HashMap<>();
        activity.put("type", "message");
        activity.put("from", Map.of(
            "id", config.getBotId(),
            "name", config.getBotName()
       ));
        activity.put("conversation", Map.of("id", content.get("conversationId").asText()));

        // Create adaptive card attachment
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("contentType", "application/vnd.microsoft.card.adaptive");
        attachment.put("content", content.get("card"));

        activity.put("attachments", Arrays.asList(attachment));

        return createSuccessResponse(sendBotActivity(activity));
    }

    private MessageDTO sendProactiveMessage(JsonNode content) throws Exception {
        // Create conversation reference
        Map<String, Object> conversationRef = new HashMap<>();
        conversationRef.put("user", Map.of("id", content.get("userId").asText()));
        conversationRef.put("bot", Map.of(
            "id", config.getBotId(),
            "name", config.getBotName()
       ));
        conversationRef.put("conversation", Map.of(
            "isGroup", false,
            "tenantId", config.getTenantId()
       ));
        conversationRef.put("channelId", "msteams");
        conversationRef.put("serviceUrl", config.getBotFrameworkUrl());

        // Continue conversation
        String endpoint = config.getBotFrameworkUrl() + "/v3/conversations";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> conversation = new HashMap<>();
        conversation.put("bot", conversationRef.get("bot"));
        conversation.put("members", Arrays.asList(conversationRef.get("user")));
        conversation.put("channelData", Map.of("tenant", Map.of("id", config.getTenantId())));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(conversation, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            endpoint, HttpMethod.POST, entity, JsonNode.class
       );

        String conversationId = response.getBody().get("id").asText();

        // Send message
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("conversationId", conversationId);
        messageContent.put("text", content.get("text").asText());

        return sendBotMessage(objectMapper.valueToTree(messageContent));
    }

    // App management methods
    private MessageDTO installApp(JsonNode content) throws Exception {
        String endpoint;

        if(content.has("teamId")) {
            endpoint = String.format("/teams/%s/installedApps", content.get("teamId").asText());
        } else if(content.has("userId")) {
            endpoint = String.format("/users/%s/teamwork/installedApps",
                content.get("userId").asText());
        } else {
            throw new AdapterException("Either teamId or userId must be specified");
        }

        Map<String, Object> installation = new HashMap<>();
        installation.put("teamsApp@odata.bind",
            "https://graph.microsoft.com/v1.0/appCatalogs/teamsApps/" +
            content.get("teamsAppId").asText());

        if(content.has("consentedPermissionSet")) {
            installation.put("consentedPermissionSet", content.get("consentedPermissionSet"));
        }

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.POST, installation);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO uninstallApp(JsonNode content) throws Exception {
        String endpoint;

        if(content.has("teamId")) {
            endpoint = String.format("/teams/%s/installedApps/%s",
                content.get("teamId").asText(),
                content.get("installationId").asText());
        } else if(content.has("userId")) {
            endpoint = String.format("/users/%s/teamwork/installedApps/%s",
                content.get("userId").asText(),
                content.get("installationId").asText());
        } else {
            throw new AdapterException("Either teamId or userId must be specified");
        }

        makeGraphApiRequest(endpoint, HttpMethod.DELETE, null);
        return createSuccessResponse(objectMapper.createObjectNode().put("uninstalled", true));
    }

    private MessageDTO updateApp(JsonNode content) throws Exception {
        String endpoint;

        if(content.has("teamId")) {
            endpoint = String.format("/teams/%s/installedApps/%s/upgrade",
                content.get("teamId").asText(),
                content.get("installationId").asText());
        } else if(content.has("userId")) {
            endpoint = String.format("/users/%s/teamwork/installedApps/%s/upgrade",
                content.get("userId").asText(),
                content.get("installationId").asText());
        } else {
            throw new AdapterException("Either teamId or userId must be specified");
        }

        Map<String, Object> upgrade = new HashMap<>();
        if(content.has("consentedPermissionSet")) {
            upgrade.put("consentedPermissionSet", content.get("consentedPermissionSet"));
        }

        makeGraphApiRequest(endpoint, HttpMethod.POST, upgrade);
        return createSuccessResponse(objectMapper.createObjectNode().put("upgraded", true));
    }

    // Batch operations
    private MessageDTO executeBatchRequest(JsonNode content) throws Exception {
        List<Map<String, Object>> requests = new ArrayList<>();
        int id = 1;

        for(JsonNode request : content.get("requests")) {
            Map<String, Object> batchRequest = new HashMap<>();
            batchRequest.put("id", String.valueOf(id++));
            batchRequest.put("method", request.get("method").asText());
            batchRequest.put("url", request.get("url").asText());

            if(request.has("headers")) {
                batchRequest.put("headers", request.get("headers"));
            }
            if(request.has("body")) {
                batchRequest.put("body", request.get("body"));
            }

            requests.add(batchRequest);

            if(requests.size() >= MAX_BATCH_REQUESTS) {
                log.warn("Batch request limit reached, only processing first {} requests",
                    MAX_BATCH_REQUESTS);
                break;
            }
        }

        Map<String, Object> batch = Map.of("requests", requests);

        JsonNode response = makeGraphApiRequest("/$batch", HttpMethod.POST, batch);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    // Search methods
    private MessageDTO searchMessages(JsonNode content) throws Exception {
        Map<String, Object> searchRequest = new HashMap<>();

        List<Map<String, Object>> requests = new ArrayList<>();
        Map<String, Object> request = new HashMap<>();
        request.put("entityTypes", Arrays.asList("chatMessage"));
        request.put("query", Map.of("queryString", content.get("query").asText()));

        if(content.has("from")) {
            request.put("from", content.get("from").asInt());
        }
        if(content.has("size")) {
            request.put("size", content.get("size").asInt());
        }

        requests.add(request);
        searchRequest.put("requests", requests);

        JsonNode response = makeGraphApiRequest("/search/query", HttpMethod.POST, searchRequest);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO searchTeams(JsonNode content) throws Exception {
        String filter = String.format("displayName eq '%s' or startswith(displayName, '%s')",
            content.get("query").asText(),
            content.get("query").asText());

        String endpoint = "/groups?$filter = " +
            "resourceProvisioningOptions/Any(x:x eq 'Team') and(" + filter + ")";

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.GET, null);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    // Presence methods
    private MessageDTO getUserPresence(JsonNode content) throws Exception {
        String endpoint = String.format("/users/%s/presence", content.get("userId").asText());

        JsonNode response = makeGraphApiRequest(endpoint, HttpMethod.GET, null);
        return createSuccessResponse(objectMapper.valueToTree(response));
    }

    private MessageDTO setUserPresence(JsonNode content) throws Exception {
        String endpoint = "/me/presence/setPresence";

        Map<String, Object> presence = new HashMap<>();
        presence.put("sessionId", config.getBotId());
        presence.put("availability", content.get("availability").asText());

        if(content.has("activity")) {
            presence.put("activity", content.get("activity").asText());
        }
        if(content.has("expirationDuration")) {
            presence.put("expirationDuration", content.get("expirationDuration").asText());
        }

        makeGraphApiRequest(endpoint, HttpMethod.POST, presence);
        return createSuccessResponse(objectMapper.createObjectNode().put("presenceSet", true));
    }

    // Helper methods
    private List<Map<String, Object>> processMentions(JsonNode mentions) {
        List<Map<String, Object>> mentionList = new ArrayList<>();

        int id = 0;
        for(JsonNode mention : mentions) {
            Map<String, Object> mentionData = new HashMap<>();
            mentionData.put("id", id++);
            mentionData.put("mentionText", mention.get("text").asText());

            Map<String, Object> mentioned = new HashMap<>();
            if(mention.has("userId")) {
                mentioned.put("user", Map.of(
                    "@odata.type", "#microsoft.graph.teamworkUserIdentity",
                    "id", mention.get("userId").asText(),
                    "displayName", mention.get("displayName").asText(),
                    "userIdentityType", "aadUser"
               ));
            } else if(mention.has("channelId")) {
                mentioned.put("conversation", Map.of(
                    "@odata.type", "#microsoft.graph.teamworkConversationIdentity",
                    "id", mention.get("channelId").asText(),
                    "displayName", mention.get("displayName").asText(),
                    "conversationIdentityType", "channel"
               ));
            }
            mentionData.put("mentioned", mentioned);

            mentionList.add(mentionData);
        }

        return mentionList;
    }

    private Map<String, Object> createMeetingParticipant(JsonNode participant) {
        Map<String, Object> participantData = new HashMap<>();

        Map<String, Object> identity = new HashMap<>();
        identity.put("@odata.type", "#microsoft.graph.identitySet");

        Map<String, Object> user = new HashMap<>();
        user.put("@odata.type", "#microsoft.graph.identity");
        user.put("id", participant.get("userId").asText());
        user.put("displayName", participant.get("displayName").asText());

        identity.put("user", user);
        participantData.put("identity", identity);

        if(participant.has("role")) {
            participantData.put("role", participant.get("role").asText());
        }

        return participantData;
    }

    private JsonNode pollAsyncOperation(String location) throws Exception {
        int maxAttempts = 30;
        int attempt = 0;

        while(attempt < maxAttempts) {
            Thread.sleep(2000); // Wait 2 seconds between polls

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                    location, HttpMethod.GET, entity, JsonNode.class
               );

                JsonNode result = response.getBody();
                String status = result.path("status").asText();

                if("succeeded".equals(status)) {
                    return result;
                } else if("failed".equals(status)) {
                    throw new AdapterException("Async operation failed: " +
                        result.path("error").toString());
                }
            } catch(HttpClientErrorException e) {
                if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    // Operation completed and location no longer exists
                    return objectMapper.createObjectNode().put("status", "completed");
                }
                throw e;
            }

            attempt++;
        }

        throw new AdapterException("Async operation timed out");
    }

    private JsonNode sendBotActivity(Map<String, Object> activity) throws Exception {
        String endpoint = config.getBotFrameworkUrl() + "/v3/conversations/" +
            activity.get("conversation") + "/activities";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(activity, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            endpoint, HttpMethod.POST, entity, JsonNode.class
       );

        return response.getBody();
    }

    // Microsoft Graph API request helper
    private JsonNode makeGraphApiRequest(String path, HttpMethod method, Object body)
            throws Exception {
        HttpHeaders headers = createAuthHeaders();

        HttpEntity<?> entity = body != null ?
            new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                config.getGraphApiUrl() + path,
                method,
                entity,
                JsonNode.class
           );

            return response.getBody();
        } catch(HttpClientErrorException e) {
            log.error("Graph API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Force token refresh
                synchronized(tokenLock) {
                    accessToken = null;
                }
                // Retry once
                headers = createAuthHeaders();
                entity = body != null ?
                    new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getGraphApiUrl() + path,
                    method,
                    entity,
                    JsonNode.class
               );

                return response.getBody();
            }

            throw new AdapterException("Graph API request failed: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", "application/json");
        return headers;
    }

    private MessageDTO createSuccessResponse(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);

        MessageDTO message = new MessageDTO();
        message.setType("teams_response");
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        try {
            message.setPayload(objectMapper.writeValueAsString(result));
        } catch(Exception e) {
            message.setPayload(result.toString());
        }
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    private MessageDTO createSuccessResponse(String correlationId, Map<String, Object> data) {
        MessageDTO response = new MessageDTO();
        response.setType("teams_response");
        response.setCorrelationId(correlationId);
        try {
            response.setPayload(objectMapper.writeValueAsString(data));
        } catch(Exception e) {
            response.setPayload(data.toString());
        }
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private MessageDTO createErrorResponse(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", error);

        MessageDTO message = new MessageDTO();
        message.setType("teams_response");
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        try {
            message.setPayload(objectMapper.writeValueAsString(result));
        } catch(Exception e) {
            message.setPayload(result.toString());
        }
        message.setTimestamp(LocalDateTime.now());
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
        // No specific receiver initialization needed for Teams outbound adapter
        log.debug("Teams outbound adapter receiver initialized");
    }

    @Override
    protected void doReceiverDestroy() throws Exception {
        // No specific receiver cleanup needed for Teams outbound adapter
        log.debug("Teams outbound adapter receiver destroyed");
    }

    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // Teams outbound adapter doesn't implement receive functionality
        // It's primarily for sending messages/actions to Teams
        return AdapterResult.success(null, "Receive not supported for Teams outbound adapter");
    }

    @Override
    protected long getPollingIntervalMs() {
        // Teams outbound adapter doesn't use polling
        return 0;
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        // Test connection by getting current user info
        try {
            String token = getValidAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://graph.microsoft.com/v1.0/me",
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Connected to Microsoft Teams successfully");
            } else {
                return AdapterResult.failure("Failed to connect to Microsoft Teams");
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
        }
    }
}
