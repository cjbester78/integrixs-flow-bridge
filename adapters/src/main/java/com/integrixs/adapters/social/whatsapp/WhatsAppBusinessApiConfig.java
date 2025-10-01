package com.integrixs.adapters.social.whatsapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.whatsapp.business")
public class WhatsAppBusinessApiConfig extends SocialMediaAdapterConfig {

    private String phoneNumberId;
    private String businessAccountId;
    private String verifyToken;
    private String systemUserAccessToken;
    private WhatsAppFeatures features = new WhatsAppFeatures();
    private WhatsAppSettings settings = new WhatsAppSettings();
    private Long statusPollingInterval = 60000L; // 1 minute default

    @Override
    public String getPlatformName() {
        return "whatsapp";
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://www.facebook.com/v18.0/dialog/oauth";
    }

    @Override
    public String getTokenUrl() {
        return "https://graph.facebook.com/v18.0/oauth/access_token";
    }

    public static class WhatsAppFeatures {
        private boolean enableTextMessaging = true;
        private boolean enableMediaMessaging = true;
        private boolean enableTemplateMessaging = true;
        private boolean enableInteractiveMessages = true;
        private boolean enableStatusUpdates = true;
        private boolean enableGroupMessaging = true;
        private boolean enableContactManagement = true;
        private boolean enableBusinessProfile = true;
        private boolean enableCatalogs = true;
        private boolean enableQRCodes = true;
        private boolean enableLabels = true;
        private boolean enableFlows = true;

        // Getters and setters
        public boolean isEnableTextMessaging() {
            return enableTextMessaging;
        }
        public void setEnableTextMessaging(boolean enableTextMessaging) {
            this.enableTextMessaging = enableTextMessaging;
        }
        public boolean isEnableMediaMessaging() {
            return enableMediaMessaging;
        }
        public void setEnableMediaMessaging(boolean enableMediaMessaging) {
            this.enableMediaMessaging = enableMediaMessaging;
        }
        public boolean isEnableTemplateMessaging() {
            return enableTemplateMessaging;
        }
        public void setEnableTemplateMessaging(boolean enableTemplateMessaging) {
            this.enableTemplateMessaging = enableTemplateMessaging;
        }
        public boolean isEnableInteractiveMessages() {
            return enableInteractiveMessages;
        }
        public void setEnableInteractiveMessages(boolean enableInteractiveMessages) {
            this.enableInteractiveMessages = enableInteractiveMessages;
        }
        public boolean isEnableStatusUpdates() {
            return enableStatusUpdates;
        }
        public void setEnableStatusUpdates(boolean enableStatusUpdates) {
            this.enableStatusUpdates = enableStatusUpdates;
        }
        public boolean isEnableGroupMessaging() {
            return enableGroupMessaging;
        }
        public void setEnableGroupMessaging(boolean enableGroupMessaging) {
            this.enableGroupMessaging = enableGroupMessaging;
        }
        public boolean isEnableContactManagement() {
            return enableContactManagement;
        }
        public void setEnableContactManagement(boolean enableContactManagement) {
            this.enableContactManagement = enableContactManagement;
        }
        public boolean isEnableBusinessProfile() {
            return enableBusinessProfile;
        }
        public void setEnableBusinessProfile(boolean enableBusinessProfile) {
            this.enableBusinessProfile = enableBusinessProfile;
        }
        public boolean isEnableCatalogs() {
            return enableCatalogs;
        }
        public void setEnableCatalogs(boolean enableCatalogs) {
            this.enableCatalogs = enableCatalogs;
        }
        public boolean isEnableQRCodes() {
            return enableQRCodes;
        }
        public void setEnableQRCodes(boolean enableQRCodes) {
            this.enableQRCodes = enableQRCodes;
        }
        public boolean isEnableLabels() {
            return enableLabels;
        }
        public void setEnableLabels(boolean enableLabels) {
            this.enableLabels = enableLabels;
        }
        public boolean isEnableFlows() {
            return enableFlows;
        }
        public void setEnableFlows(boolean enableFlows) {
            this.enableFlows = enableFlows;
        }
    }

    public static class WhatsAppSettings {
        private int maxTextLength = 4096;
        private int maxCaptionLength = 1024;
        private int maxButtonsPerMessageDTO = 3;
        private int maxSectionsPerList = 10;
        private int maxItemsPerSection = 10;
        private int sessionTimeoutMinutes = 24 * 60; // 24 hours
        private boolean autoDownloadMedia = true;
        private boolean saveMediaLocally = false;
        private String mediaStoragePath = "/tmp/whatsapp-media";

        // Getters and setters
        public int getMaxTextLength() {
            return maxTextLength;
        }
        public void setMaxTextLength(int maxTextLength) {
            this.maxTextLength = maxTextLength;
        }
        public int getMaxCaptionLength() {
            return maxCaptionLength;
        }
        public void setMaxCaptionLength(int maxCaptionLength) {
            this.maxCaptionLength = maxCaptionLength;
        }
        public int getMaxButtonsPerMessageDTO() {
            return maxButtonsPerMessageDTO;
        }
        public void setMaxButtonsPerMessageDTO(int maxButtonsPerMessageDTO) {
            this.maxButtonsPerMessageDTO = maxButtonsPerMessageDTO;
        }
        public int getMaxSectionsPerList() {
            return maxSectionsPerList;
        }
        public void setMaxSectionsPerList(int maxSectionsPerList) {
            this.maxSectionsPerList = maxSectionsPerList;
        }
        public int getMaxItemsPerSection() {
            return maxItemsPerSection;
        }
        public void setMaxItemsPerSection(int maxItemsPerSection) {
            this.maxItemsPerSection = maxItemsPerSection;
        }
        public int getSessionTimeoutMinutes() {
            return sessionTimeoutMinutes;
        }
        public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
            this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        }
        public boolean isAutoDownloadMedia() {
            return autoDownloadMedia;
        }
        public void setAutoDownloadMedia(boolean autoDownloadMedia) {
            this.autoDownloadMedia = autoDownloadMedia;
        }
        public boolean isSaveMediaLocally() {
            return saveMediaLocally;
        }
        public void setSaveMediaLocally(boolean saveMediaLocally) {
            this.saveMediaLocally = saveMediaLocally;
        }
        public String getMediaStoragePath() {
            return mediaStoragePath;
        }
        public void setMediaStoragePath(String mediaStoragePath) {
            this.mediaStoragePath = mediaStoragePath;
        }
    }

    // MessageDTO types
    public enum MessageType {
        TEXT,
        IMAGE,
        AUDIO,
        VIDEO,
        DOCUMENT,
        LOCATION,
        CONTACTS,
        STICKER,
        TEMPLATE,
        INTERACTIVE,
        REACTION
    }

    // Interactive message types
    public enum InteractiveType {
        BUTTON,
        LIST,
        PRODUCT,
        PRODUCT_LIST,
        FLOW
    }

    // Template categories
    public enum TemplateCategory {
        MARKETING,
        UTILITY,
        AUTHENTICATION
    }

    // MessageDTO status
    public enum MessageDeliveryStatus {
        SENT,
        DELIVERED,
        READ,
        FAILED
    }

    // Business profile fields
    public enum BusinessProfileField {
        ABOUT,
        ADDRESS,
        DESCRIPTION,
        EMAIL,
        PROFILE_PICTURE_URL,
        VERTICAL,
        WEBSITES
    }
    // Getters and Setters
    public String getPhoneNumberId() {
        return phoneNumberId;
    }
    public void setPhoneNumberId(String phoneNumberId) {
        this.phoneNumberId = phoneNumberId;
    }
    public String getBusinessAccountId() {
        return businessAccountId;
    }
    public void setBusinessAccountId(String businessAccountId) {
        this.businessAccountId = businessAccountId;
    }
    public String getVerifyToken() {
        return verifyToken;
    }
    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }
    public String getSystemUserAccessToken() {
        return systemUserAccessToken;
    }
    public void setSystemUserAccessToken(String systemUserAccessToken) {
        this.systemUserAccessToken = systemUserAccessToken;
    }
    public WhatsAppFeatures getFeatures() {
        return features;
    }
    public void setFeatures(WhatsAppFeatures features) {
        this.features = features;
    }
    public WhatsAppSettings getSettings() {
        return settings;
    }
    public void setSettings(WhatsAppSettings settings) {
        this.settings = settings;
    }

    public Long getStatusPollingInterval() {
        return statusPollingInterval;
    }

    public void setStatusPollingInterval(Long statusPollingInterval) {
        this.statusPollingInterval = statusPollingInterval;
    }

    public WhatsAppLimits getLimits() {
        return new WhatsAppLimits();
    }

    // WhatsApp API limits
    public static class WhatsAppLimits {
        private int maxMediaSizeMb = 100;
        private int maxImageSizeMb = 5;
        private int maxVideoSizeMb = 16;
        private int maxAudioSizeMb = 16;
        private int maxDocumentSizeMb = 100;
        private int maxStickerSizeKb = 100;

        public int getMaxMediaSizeMb() {
            return maxMediaSizeMb;
        }

        public void setMaxMediaSizeMb(int maxMediaSizeMb) {
            this.maxMediaSizeMb = maxMediaSizeMb;
        }

        public int getMaxImageSizeMb() {
            return maxImageSizeMb;
        }

        public void setMaxImageSizeMb(int maxImageSizeMb) {
            this.maxImageSizeMb = maxImageSizeMb;
        }

        public int getMaxVideoSizeMb() {
            return maxVideoSizeMb;
        }

        public void setMaxVideoSizeMb(int maxVideoSizeMb) {
            this.maxVideoSizeMb = maxVideoSizeMb;
        }

        public int getMaxAudioSizeMb() {
            return maxAudioSizeMb;
        }

        public void setMaxAudioSizeMb(int maxAudioSizeMb) {
            this.maxAudioSizeMb = maxAudioSizeMb;
        }

        public int getMaxDocumentSizeMb() {
            return maxDocumentSizeMb;
        }

        public void setMaxDocumentSizeMb(int maxDocumentSizeMb) {
            this.maxDocumentSizeMb = maxDocumentSizeMb;
        }

        public int getMaxStickerSizeKb() {
            return maxStickerSizeKb;
        }

        public void setMaxStickerSizeKb(int maxStickerSizeKb) {
            this.maxStickerSizeKb = maxStickerSizeKb;
        }
    }

}
