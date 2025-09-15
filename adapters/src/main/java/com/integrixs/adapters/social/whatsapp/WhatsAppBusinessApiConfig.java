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

    @Override
    public String getPlatformName() {
        return "whatsapp";
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
        private String mediaStoragePath = "/tmp/whatsapp - media";
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

    public boolean isAutoDownloadMedia() {
        return autoDownloadMedia;
    }

    public boolean isSaveMediaLocally() {
        return saveMediaLocally;
    }

}
