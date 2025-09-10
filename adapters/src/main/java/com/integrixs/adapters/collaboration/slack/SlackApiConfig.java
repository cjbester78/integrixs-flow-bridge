package com.integrixs.adapters.collaboration.slack;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.slack")
@EqualsAndHashCode(callSuper = true)
public class SlackApiConfig extends SocialMediaAdapterConfig {
    
    private String clientId;
    private String clientSecret;
    private String botToken;
    private String userToken;
    private String workspaceId;
    private String signingSecret; // For request verification
    private String webhookUrl;
    private String slashCommandPrefix = "/";
    private String apiUrl = "https://slack.com/api/"; // Allow custom URL for enterprise
    private SlackFeatures features = new SlackFeatures();
    private SlackLimits limits = new SlackLimits();
    private List<String> scopes; // OAuth scopes
    
    @Data
    public static class SlackFeatures {
        private boolean enableMessaging = true;
        private boolean enableChannels = true;
        private boolean enableDirectMessages = true;
        private boolean enableGroups = true;
        private boolean enableFiles = true;
        private boolean enableSnippets = true;
        private boolean enableSlashCommands = true;
        private boolean enableInteractiveComponents = true;
        private boolean enableWorkflows = true;
        private boolean enableAppHome = true;
        private boolean enableShortcuts = true;
        private boolean enableScheduledMessages = true;
        private boolean enableThreads = true;
        private boolean enableReactions = true;
        private boolean enablePins = true;
        private boolean enableBookmarks = true;
        private boolean enableReminders = true;
        private boolean enableSearch = true;
        private boolean enableCalls = true;
        private boolean enableHuddles = true;
        private boolean enableCanvas = true;
        private boolean enableEventSubscriptions = true;
        private boolean enableRealTimeMessaging = false; // RTM is deprecated
        private boolean enableSocketMode = true; // Preferred over RTM
        private boolean enableEnterpriseGrid = false;
        private boolean enableAdminApis = false;
        private boolean enableApprovalWorkflows = true;
        private boolean enableGuestAccess = true;
        private boolean enableSharedChannels = true;
        private boolean enableConnectChannels = true;
    }
    
    @Data
    public static class SlackLimits {
        private int maxMessageLength = 40000; // characters
        private int maxAttachmentsPerMessage = 20;
        private int maxBlocksPerMessage = 50;
        private int maxInteractiveComponentsPerMessage = 25;
        private int maxFileSizeMB = 1000; // 1GB
        private int maxSnippetSizeKB = 1000; // 1MB
        private int maxChannelsPerRequest = 1000;
        private int maxUsersPerRequest = 1000;
        private int rateLimitTier1PerMinute = 1; // Tier 1 methods
        private int rateLimitTier2PerMinute = 20; // Tier 2 methods
        private int rateLimitTier3PerMinute = 50; // Tier 3 methods
        private int rateLimitTier4PerMinute = 100; // Tier 4 methods
        private int rateLimitSpecialPerMinute = 1; // Special tier methods
        private int maxWorkflowSteps = 20;
        private int maxShortcutsPerApp = 10;
        private int maxSlashCommandsPerApp = 25;
        private int maxScheduledMessagesPerChannel = 120;
        private int maxPinsPerChannel = 100;
        private int maxBookmarksPerChannel = 50;
        private int burstLimitRequests = 10;
        private int websocketReconnectMaxAttempts = 5;
        private int websocketPingIntervalSeconds = 30;
    }
    
    // OAuth scopes
    public enum OAuthScope {
        // Messaging scopes
        CHANNELS_READ("channels:read"),
        CHANNELS_WRITE("channels:write"),
        CHANNELS_MANAGE("channels:manage"),
        CHANNELS_JOIN("channels:join"),
        CHANNELS_HISTORY("channels:history"),
        CHAT_WRITE("chat:write"),
        CHAT_WRITE_PUBLIC("chat:write:public"),
        CHAT_WRITE_CUSTOMIZE("chat:write:customize"),
        
        // Groups/Private channels
        GROUPS_READ("groups:read"),
        GROUPS_WRITE("groups:write"),
        GROUPS_HISTORY("groups:history"),
        
        // Direct messages
        IM_READ("im:read"),
        IM_WRITE("im:write"),
        IM_HISTORY("im:history"),
        MPIM_READ("mpim:read"),
        MPIM_WRITE("mpim:write"),
        MPIM_HISTORY("mpim:history"),
        
        // Users and workspace
        USERS_READ("users:read"),
        USERS_READ_EMAIL("users:read.email"),
        USERS_PROFILE_READ("users.profile:read"),
        USERS_PROFILE_WRITE("users.profile:write"),
        TEAM_READ("team:read"),
        
        // Files
        FILES_READ("files:read"),
        FILES_WRITE("files:write"),
        
        // Reactions and pins
        REACTIONS_READ("reactions:read"),
        REACTIONS_WRITE("reactions:write"),
        PINS_READ("pins:read"),
        PINS_WRITE("pins:write"),
        
        // App features
        COMMANDS("commands"),
        INCOMING_WEBHOOK("incoming-webhook"),
        WORKFLOW_STEPS_EXECUTE("workflow.steps:execute"),
        APP_MENTIONS_READ("app_mentions:read"),
        
        // Admin (Enterprise)
        ADMIN_USERS_READ("admin.users:read"),
        ADMIN_USERS_WRITE("admin.users:write"),
        ADMIN_CONVERSATIONS_READ("admin.conversations:read"),
        ADMIN_CONVERSATIONS_WRITE("admin.conversations:write"),
        ADMIN_TEAMS_READ("admin.teams:read"),
        ADMIN_TEAMS_WRITE("admin.teams:write"),
        ADMIN_INVITES_READ("admin.invites:read"),
        ADMIN_INVITES_WRITE("admin.invites:write"),
        
        // Other
        CALLS_READ("calls:read"),
        CALLS_WRITE("calls:write"),
        BOOKMARKS_READ("bookmarks:read"),
        BOOKMARKS_WRITE("bookmarks:write"),
        REMINDERS_READ("reminders:read"),
        REMINDERS_WRITE("reminders:write"),
        SEARCH_READ("search:read"),
        EMOJI_READ("emoji:read"),
        DND_READ("dnd:read"),
        DND_WRITE("dnd:write");
        
        private final String scope;
        
        OAuthScope(String scope) {
            this.scope = scope;
        }
        
        public String getScope() {
            return scope;
        }
    }
    
    // Event types for event subscriptions
    public enum EventType {
        // Message events
        MESSAGE_CHANNELS("message.channels"),
        MESSAGE_GROUPS("message.groups"),
        MESSAGE_IM("message.im"),
        MESSAGE_MPIM("message.mpim"),
        
        // App events
        APP_MENTION("app_mention"),
        APP_HOME_OPENED("app_home_opened"),
        APP_UNINSTALLED("app_uninstalled"),
        APP_REQUESTED("app_requested"),
        
        // Channel events
        CHANNEL_ARCHIVE("channel_archive"),
        CHANNEL_CREATED("channel_created"),
        CHANNEL_DELETED("channel_deleted"),
        CHANNEL_RENAME("channel_rename"),
        CHANNEL_UNARCHIVE("channel_unarchive"),
        CHANNEL_LEFT("channel_left"),
        CHANNEL_JOINED("channel_joined"),
        CHANNEL_SHARED("channel_shared"),
        CHANNEL_UNSHARED("channel_unshared"),
        
        // Member events
        MEMBER_JOINED_CHANNEL("member_joined_channel"),
        MEMBER_LEFT_CHANNEL("member_left_channel"),
        
        // User events
        USER_CHANGE("user_change"),
        USER_STATUS_CHANGED("user_status_changed"),
        TEAM_JOIN("team_join"),
        
        // File events
        FILE_CREATED("file_created"),
        FILE_DELETED("file_deleted"),
        FILE_SHARED("file_shared"),
        FILE_PUBLIC("file_public"),
        FILE_UNSHARED("file_unshared"),
        FILE_CHANGE("file_change"),
        FILE_COMMENT_ADDED("file_comment_added"),
        FILE_COMMENT_EDITED("file_comment_edited"),
        FILE_COMMENT_DELETED("file_comment_deleted"),
        
        // Reaction events
        REACTION_ADDED("reaction_added"),
        REACTION_REMOVED("reaction_removed"),
        
        // Pin events
        PIN_ADDED("pin_added"),
        PIN_REMOVED("pin_removed"),
        
        // Star events
        STAR_ADDED("star_added"),
        STAR_REMOVED("star_removed"),
        
        // Link events
        LINK_SHARED("link_shared"),
        
        // Workflow events
        WORKFLOW_STEP_EXECUTE("workflow_step_execute"),
        WORKFLOW_STEP_COMPLETED("workflow_step_completed"),
        WORKFLOW_STEP_FAILED("workflow_step_failed"),
        WORKFLOW_STEP_DELETED("workflow_step_deleted"),
        
        // Call events
        CALL_REJECTED("call_rejected"),
        
        // DND events
        DND_UPDATED("dnd_updated"),
        DND_UPDATED_USER("dnd_updated_user"),
        
        // Email domain events
        EMAIL_DOMAIN_CHANGED("email_domain_changed"),
        
        // Emoji events
        EMOJI_CHANGED("emoji_changed"),
        
        // Grid events (Enterprise)
        GRID_MIGRATION_COMPLETED("grid_migration_completed"),
        GRID_MIGRATION_FAILED("grid_migration_failed"),
        GRID_TEAM_JOINED("grid_team_joined"),
        GRID_TEAM_REMOVED("grid_team_removed"),
        
        // Invite events
        INVITE_REQUESTED("invite_requested"),
        
        // Shared channel events
        SHARED_CHANNEL_INVITE_RECEIVED("shared_channel_invite_received"),
        SHARED_CHANNEL_INVITE_ACCEPTED("shared_channel_invite_accepted"),
        SHARED_CHANNEL_INVITE_APPROVED("shared_channel_invite_approved"),
        SHARED_CHANNEL_INVITE_DECLINED("shared_channel_invite_declined"),
        
        // Subteam events
        SUBTEAM_CREATED("subteam_created"),
        SUBTEAM_UPDATED("subteam_updated"),
        SUBTEAM_SELF_ADDED("subteam_self_added"),
        SUBTEAM_SELF_REMOVED("subteam_self_removed"),
        
        // Token events
        TOKENS_REVOKED("tokens_revoked"),
        
        // URL verification
        URL_VERIFICATION("url_verification"),
        
        // Event callback wrapper
        EVENT_CALLBACK("event_callback")
    }
    
    // Interactive component types
    public enum InteractionType {
        BUTTON("button"),
        SELECT_MENU("select_menu"),
        STATIC_SELECT("static_select"),
        EXTERNAL_SELECT("external_select"),
        USERS_SELECT("users_select"),
        CONVERSATIONS_SELECT("conversations_select"),
        CHANNELS_SELECT("channels_select"),
        OVERFLOW("overflow"),
        DATEPICKER("datepicker"),
        TIMEPICKER("timepicker"),
        PLAIN_TEXT_INPUT("plain_text_input"),
        RADIO_BUTTONS("radio_buttons"),
        CHECKBOXES("checkboxes"),
        VIEW_SUBMISSION("view_submission"),
        VIEW_CLOSED("view_closed"),
        BLOCK_ACTIONS("block_actions"),
        MESSAGE_ACTION("message_action"),
        SHORTCUT("shortcut"),
        WORKFLOW_STEP_EDIT("workflow_step_edit"),
        WORKFLOW_STEP_SAVE("workflow_step_save"),
        WORKFLOW_STEP_EXECUTE("workflow_step_execute")
    }
    
    // Block types for rich messaging
    public enum BlockType {
        SECTION("section"),
        DIVIDER("divider"),
        IMAGE("image"),
        ACTIONS("actions"),
        CONTEXT("context"),
        INPUT("input"),
        FILE("file"),
        HEADER("header"),
        PLAIN_TEXT("plain_text"),
        MRKDWN("mrkdwn"),
        RICH_TEXT("rich_text"),
        VIDEO("video"),
        CALL("call")
    }
    
    // Conversation types
    public enum ConversationType {
        PUBLIC_CHANNEL("public_channel"),
        PRIVATE_CHANNEL("private_channel"),
        MPIM("mpim"), // Multi-party instant message
        IM("im"); // Direct message
        
        private final String type;
        
        ConversationType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Rate limit tiers
    public enum RateLimitTier {
        TIER_1(1, "Infrequently used methods"),
        TIER_2(2, "Less frequently used methods"),
        TIER_3(3, "Frequently used methods"),
        TIER_4(4, "Very frequently used methods"),
        SPECIAL(0, "Methods with special rate limits");
        
        private final int tier;
        private final String description;
        
        RateLimitTier(int tier, String description) {
            this.tier = tier;
            this.description = description;
        }
        
        public int getTier() {
            return tier;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // File types
    public enum FileType {
        AUTO("auto"),
        TEXT("text"),
        APPLESCRIPT("applescript"),
        BOXNOTE("boxnote"),
        C("c"),
        CSHARP("csharp"),
        CPP("cpp"),
        CSS("css"),
        CSV("csv"),
        CLOJURE("clojure"),
        COFFEESCRIPT("coffeescript"),
        CFM("cfm"),
        D("d"),
        DART("dart"),
        DIFF("diff"),
        DOCKERFILE("dockerfile"),
        ERLANG("erlang"),
        FSHARP("fsharp"),
        FORTRAN("fortran"),
        GO("go"),
        GROOVY("groovy"),
        HTML("html"),
        HANDLEBARS("handlebars"),
        HASKELL("haskell"),
        HAXE("haxe"),
        JAVA("java"),
        JAVASCRIPT("javascript"),
        JSON("json"),
        JULIA("julia"),
        KOTLIN("kotlin"),
        LATEX("latex"),
        LISP("lisp"),
        LUA("lua"),
        MARKDOWN("markdown"),
        MATLAB("matlab"),
        MUMPS("mumps"),
        OCAML("ocaml"),
        OBJC("objc"),
        PHP("php"),
        PASCAL("pascal"),
        PERL("perl"),
        PIG("pig"),
        POST("post"),
        POWERSHELL("powershell"),
        PUPPET("puppet"),
        PYTHON("python"),
        R("r"),
        RUBY("ruby"),
        RUST("rust"),
        SQL("sql"),
        SASS("sass"),
        SCALA("scala"),
        SCHEME("scheme"),
        SHELL("shell"),
        SMALLTALK("smalltalk"),
        SWIFT("swift"),
        TSV("tsv"),
        VB("vb"),
        VBSCRIPT("vbscript"),
        VELOCITY("velocity"),
        VERILOG("verilog"),
        XML("xml"),
        YAML("yaml");
        
        private final String type;
        
        FileType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Error codes
    public enum SlackErrorCode {
        // General errors
        NOT_AUTHED("not_authed", "No authentication token provided"),
        INVALID_AUTH("invalid_auth", "Some aspect of authentication cannot be validated"),
        ACCOUNT_INACTIVE("account_inactive", "Authentication token is for a deleted user or workspace"),
        TOKEN_REVOKED("token_revoked", "Authentication token is for a deleted user or workspace"),
        NO_PERMISSION("no_permission", "The workspace token used does not have the permissions necessary"),
        ORG_LOGIN_REQUIRED("org_login_required", "The workspace is undergoing an enterprise migration"),
        INVALID_ARG_NAME("invalid_arg_name", "The method was passed an argument whose name falls outside the bounds of accepted or expected values"),
        INVALID_ARRAY_ARG("invalid_array_arg", "The method was passed a PHP-style array argument"),
        INVALID_CHARSET("invalid_charset", "The method was called via a POST request, but the charset specified was invalid"),
        INVALID_FORM_DATA("invalid_form_data", "The method was called via a POST request with Content-Type application/x-www-form-urlencoded or multipart/form-data, but the form data was either missing or syntactically invalid"),
        INVALID_POST_TYPE("invalid_post_type", "The method was called via a POST request, but the specified Content-Type was invalid"),
        MISSING_POST_TYPE("missing_post_type", "The method was called via a POST request and included a data argument but did not include a Content-Type header"),
        REQUEST_TIMEOUT("request_timeout", "The method was called via a POST request, but the POST data was either missing or truncated"),
        UPGRADING_RTM_CLOSED("upgrading_rtm_closed", "The server is temporarily unavailable"),
        TEAM_ADDED_TO_ORG("team_added_to_org", "The workspace associated with your request is currently undergoing migration to an Enterprise Organization"),
        MISSING_SCOPE("missing_scope", "The token used is not granted the specific scope permissions required"),
        NOT_ALLOWED_TOKEN_TYPE("not_allowed_token_type", "The token type is not allowed for this method"),
        METHOD_DEPRECATED("method_deprecated", "The method has been deprecated"),
        DEPRECATED_ENDPOINT("deprecated_endpoint", "The endpoint has been deprecated"),
        TWO_FACTOR_SETUP_REQUIRED("two_factor_setup_required", "Two factor setup is required"),
        ENTERPRISE_IS_RESTRICTED("enterprise_is_restricted", "The method cannot be called from an Enterprise"),
        INVALID_ARGUMENTS("invalid_arguments", "The method was either called with invalid arguments or some detail about the arguments passed are invalid"),
        
        // Rate limiting
        RATE_LIMITED("rate_limited", "Too many requests have been made, see Retry-After header"),
        
        // Channel/conversation errors
        CHANNEL_NOT_FOUND("channel_not_found", "Value passed for channel was invalid"),
        NOT_IN_CHANNEL("not_in_channel", "Caller is not a member of the channel"),
        IS_ARCHIVED("is_archived", "Channel has been archived"),
        CANT_INVITE_SELF("cant_invite_self", "Authenticated user cannot invite themselves to a channel"),
        CANT_INVITE("cant_invite", "User cannot be invited to this channel"),
        CANT_KICK_SELF("cant_kick_self", "Authenticated user cannot kick themselves from a channel"),
        CANT_KICK_FROM_GENERAL("cant_kick_from_general", "User cannot be removed from #general"),
        CANT_KICK_FROM_LAST_CHANNEL("cant_kick_from_last_channel", "User cannot be removed from the last channel they're in"),
        RESTRICTED_ACTION("restricted_action", "A workspace preference prevents this action"),
        
        // User errors
        USER_NOT_FOUND("user_not_found", "Value passed for user was invalid"),
        USER_NOT_VISIBLE("user_not_visible", "The requested user is not visible to the calling user"),
        USER_IS_BOT("user_is_bot", "This method cannot be called by a bot user"),
        USER_IS_RESTRICTED("user_is_restricted", "This method cannot be called by a restricted user"),
        USER_IS_ULTRA_RESTRICTED("user_is_ultra_restricted", "This method cannot be called by a single channel guest"),
        USERS_LIST_NOT_SUPPLIED("users_list_not_supplied", "Missing users in request"),
        
        // Message errors
        MESSAGE_NOT_FOUND("message_not_found", "No message exists with the requested timestamp"),
        CANT_DELETE_MESSAGE("cant_delete_message", "Authenticated user does not have permission to delete this message"),
        CANT_UPDATE_MESSAGE("cant_update_message", "Authenticated user does not have permission to update this message"),
        CANT_BROADCAST_MESSAGE("cant_broadcast_message", "Authenticated user does not have permission to broadcast a message"),
        MSG_TOO_LONG("msg_too_long", "Message text is too long"),
        NO_TEXT("no_text", "No message text provided"),
        TOO_MANY_ATTACHMENTS("too_many_attachments", "Too many attachments were provided"),
        
        // File errors
        FILE_NOT_FOUND("file_not_found", "The file does not exist"),
        FILE_DELETED("file_deleted", "The file has been deleted"),
        CANT_DELETE_FILE("cant_delete_file", "Authenticated user does not have permission to delete this file"),
        CANT_EDIT_FILE("cant_edit_file", "Authenticated user does not have permission to edit this file"),
        
        // App/bot errors
        APP_NOT_INSTALLED("app_not_installed", "The app is not installed on the workspace"),
        BOT_NOT_IN_CHANNEL("bot_not_in_channel", "The bot is not a member of the channel"),
        
        // Workflow errors
        WORKFLOW_STEP_NOT_FOUND("workflow_step_not_found", "The workflow step could not be found"),
        
        // Enterprise Grid errors
        TEAM_NOT_FOUND("team_not_found", "Team not found"),
        TEAM_NOT_ON_ENTERPRISE("team_not_on_enterprise", "Team is not part of an Enterprise organization"),
        ORG_NOT_FOUND("org_not_found", "Organization not found"),
        ORG_LEVEL_EMAIL_DOMAIN_ALREADY_EXISTS("org_level_email_domain_already_exists", "An organization level email domain already exists"),
        INVALID_TEAM_ID("invalid_team_id", "The team id provided is invalid"),
        
        // Other errors
        UNKNOWN_ERROR("unknown_error", "An unknown error occurred"),
        INTERNAL_ERROR("internal_error", "The server encountered an error while processing your request");
        
        private final String code;
        private final String message;
        
        SlackErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
}