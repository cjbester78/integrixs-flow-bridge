#!/bin/bash

# Fix all Teams config methods to use features and limits objects

file="/Users/cjbester/git/Integrixs-Flow-Bridge/adapters/src/main/java/com/integrixs/adapters/collaboration/teams/MicrosoftTeamsApiConfig.java"

# Fix all enable* methods to use features.enable*
sed -i '' 's/return enableFiles;/return features.enableFiles;/g' "$file"
sed -i '' 's/this\.enableFiles = /features.enableFiles = /g' "$file"

sed -i '' 's/return enableTabs;/return features.enableTabs;/g' "$file"
sed -i '' 's/this\.enableTabs = /features.enableTabs = /g' "$file"

sed -i '' 's/return enableBots;/return features.enableBots;/g' "$file"
sed -i '' 's/this\.enableBots = /features.enableBots = /g' "$file"

sed -i '' 's/return enableCards;/return features.enableCards;/g' "$file"
sed -i '' 's/this\.enableCards = /features.enableCards = /g' "$file"

sed -i '' 's/return enableConnectors;/return features.enableConnectors;/g' "$file"
sed -i '' 's/this\.enableConnectors = /features.enableConnectors = /g' "$file"

sed -i '' 's/return enableWebhooks;/return features.enableWebhooks;/g' "$file"
sed -i '' 's/this\.enableWebhooks = /features.enableWebhooks = /g' "$file"

sed -i '' 's/return enableProactiveMessaging;/return features.enableProactiveMessaging;/g' "$file"
sed -i '' 's/this\.enableProactiveMessaging = /features.enableProactiveMessaging = /g' "$file"

sed -i '' 's/return enableAdaptiveCards;/return features.enableAdaptiveCards;/g' "$file"
sed -i '' 's/this\.enableAdaptiveCards = /features.enableAdaptiveCards = /g' "$file"

sed -i '' 's/return enableMessageExtensions;/return features.enableMessageExtensions;/g' "$file"
sed -i '' 's/this\.enableMessageExtensions = /features.enableMessageExtensions = /g' "$file"

sed -i '' 's/return enableActivityFeed;/return features.enableActivityFeed;/g' "$file"
sed -i '' 's/this\.enableActivityFeed = /features.enableActivityFeed = /g' "$file"

sed -i '' 's/return enableShifts;/return features.enableShifts;/g' "$file"
sed -i '' 's/this\.enableShifts = /features.enableShifts = /g' "$file"

sed -i '' 's/return enablePlanner;/return features.enablePlanner;/g' "$file"
sed -i '' 's/this\.enablePlanner = /features.enablePlanner = /g' "$file"

sed -i '' 's/return enableOneNote;/return features.enableOneNote;/g' "$file"
sed -i '' 's/this\.enableOneNote = /features.enableOneNote = /g' "$file"

sed -i '' 's/return enableSharePoint;/return features.enableSharePoint;/g' "$file"
sed -i '' 's/this\.enableSharePoint = /features.enableSharePoint = /g' "$file"

sed -i '' 's/return enablePowerApps;/return features.enablePowerApps;/g' "$file"
sed -i '' 's/this\.enablePowerApps = /features.enablePowerApps = /g' "$file"

sed -i '' 's/return enablePowerAutomate;/return features.enablePowerAutomate;/g' "$file"
sed -i '' 's/this\.enablePowerAutomate = /features.enablePowerAutomate = /g' "$file"

sed -i '' 's/return enableYammer;/return features.enableYammer;/g' "$file"
sed -i '' 's/this\.enableYammer = /features.enableYammer = /g' "$file"

sed -i '' 's/return enableStream;/return features.enableStream;/g' "$file"
sed -i '' 's/this\.enableStream = /features.enableStream = /g' "$file"

sed -i '' 's/return enableForms;/return features.enableForms;/g' "$file"
sed -i '' 's/this\.enableForms = /features.enableForms = /g' "$file"

sed -i '' 's/return enableWhiteboard;/return features.enableWhiteboard;/g' "$file"
sed -i '' 's/this\.enableWhiteboard = /features.enableWhiteboard = /g' "$file"

sed -i '' 's/return enableLists;/return features.enableLists;/g' "$file"
sed -i '' 's/this\.enableLists = /features.enableLists = /g' "$file"

sed -i '' 's/return enableApprovals;/return features.enableApprovals;/g' "$file"
sed -i '' 's/this\.enableApprovals = /features.enableApprovals = /g' "$file"

sed -i '' 's/return enableBookings;/return features.enableBookings;/g' "$file"
sed -i '' 's/this\.enableBookings = /features.enableBookings = /g' "$file"

sed -i '' 's/return enablePolls;/return features.enablePolls;/g' "$file"
sed -i '' 's/this\.enablePolls = /features.enablePolls = /g' "$file"

sed -i '' 's/return enablePraise;/return features.enablePraise;/g' "$file"
sed -i '' 's/this\.enablePraise = /features.enablePraise = /g' "$file"

# Fix all limits methods to use limits.*
sed -i '' 's/return maxMessageLength;/return limits.maxMessageLength;/g' "$file"
sed -i '' 's/this\.maxMessageLength = /limits.maxMessageLength = /g' "$file"

sed -i '' 's/return maxCardSize;/return limits.maxCardSize;/g' "$file"
sed -i '' 's/this\.maxCardSize = /limits.maxCardSize = /g' "$file"

sed -i '' 's/return maxAttachmentsPerMessage;/return limits.maxAttachmentsPerMessage;/g' "$file"
sed -i '' 's/this\.maxAttachmentsPerMessage = /limits.maxAttachmentsPerMessage = /g' "$file"

sed -i '' 's/return maxTabsPerChannel;/return limits.maxTabsPerChannel;/g' "$file"
sed -i '' 's/this\.maxTabsPerChannel = /limits.maxTabsPerChannel = /g' "$file"

sed -i '' 's/return maxPrivateChannels;/return limits.maxPrivateChannels;/g' "$file"
sed -i '' 's/this\.maxPrivateChannels = /limits.maxPrivateChannels = /g' "$file"

sed -i '' 's/return maxChannelsPerTeam;/return limits.maxChannelsPerTeam;/g' "$file"
sed -i '' 's/this\.maxChannelsPerTeam = /limits.maxChannelsPerTeam = /g' "$file"

sed -i '' 's/return maxMembersPerTeam;/return limits.maxMembersPerTeam;/g' "$file"
sed -i '' 's/this\.maxMembersPerTeam = /limits.maxMembersPerTeam = /g' "$file"

sed -i '' 's/return maxTeamsPerUser;/return limits.maxTeamsPerUser;/g' "$file"
sed -i '' 's/this\.maxTeamsPerUser = /limits.maxTeamsPerUser = /g' "$file"

sed -i '' 's/return maxMeetingDuration;/return limits.maxMeetingDuration;/g' "$file"
sed -i '' 's/this\.maxMeetingDuration = /limits.maxMeetingDuration = /g' "$file"

sed -i '' 's/return maxMeetingParticipants;/return limits.maxMeetingParticipants;/g' "$file"
sed -i '' 's/this\.maxMeetingParticipants = /limits.maxMeetingParticipants = /g' "$file"

sed -i '' 's/return maxFileSizeMB;/return limits.maxFileSizeMB;/g' "$file"
sed -i '' 's/this\.maxFileSizeMB = /limits.maxFileSizeMB = /g' "$file"

sed -i '' 's/return maxBotMessagesPerSecond;/return limits.maxBotMessagesPerSecond;/g' "$file"
sed -i '' 's/this\.maxBotMessagesPerSecond = /limits.maxBotMessagesPerSecond = /g' "$file"

sed -i '' 's/return maxBotMessagesPerMinute;/return limits.maxBotMessagesPerMinute;/g' "$file"
sed -i '' 's/this\.maxBotMessagesPerMinute = /limits.maxBotMessagesPerMinute = /g' "$file"

sed -i '' 's/return maxBotConversationsPerSecond;/return limits.maxBotConversationsPerSecond;/g' "$file"
sed -i '' 's/this\.maxBotConversationsPerSecond = /limits.maxBotConversationsPerSecond = /g' "$file"

sed -i '' 's/return maxCardActions;/return limits.maxCardActions;/g' "$file"
sed -i '' 's/this\.maxCardActions = /limits.maxCardActions = /g' "$file"

sed -i '' 's/return maxSuggestedActions;/return limits.maxSuggestedActions;/g' "$file"
sed -i '' 's/this\.maxSuggestedActions = /limits.maxSuggestedActions = /g' "$file"

sed -i '' 's/return maxMessageExtensionResults;/return limits.maxMessageExtensionResults;/g' "$file"
sed -i '' 's/this\.maxMessageExtensionResults = /limits.maxMessageExtensionResults = /g' "$file"

sed -i '' 's/return rateLimitPerSecond;/return limits.rateLimitPerSecond;/g' "$file"
sed -i '' 's/this\.rateLimitPerSecond = /limits.rateLimitPerSecond = /g' "$file"

sed -i '' 's/return rateLimitPerMinute;/return limits.rateLimitPerMinute;/g' "$file"
sed -i '' 's/this\.rateLimitPerMinute = /limits.rateLimitPerMinute = /g' "$file"

sed -i '' 's/return burstLimit;/return limits.burstLimit;/g' "$file"
sed -i '' 's/this\.burstLimit = /limits.burstLimit = /g' "$file"

sed -i '' 's/return maxBatchRequests;/return limits.maxBatchRequests;/g' "$file"
sed -i '' 's/this\.maxBatchRequests = /limits.maxBatchRequests = /g' "$file"

sed -i '' 's/return webhookExpiryHours;/return limits.webhookExpiryHours;/g' "$file"
sed -i '' 's/this\.webhookExpiryHours = /limits.webhookExpiryHours = /g' "$file"

echo "Fixed Teams config methods"