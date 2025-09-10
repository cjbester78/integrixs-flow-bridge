package com.integrixs.adapters.social.youtube;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.youtube.analytics")
@EqualsAndHashCode(callSuper = true)
public class YouTubeAnalyticsApiConfig extends SocialMediaAdapterConfig {
    
    private String clientId;
    private String clientSecret;
    private String channelId;
    private YouTubeAnalyticsFeatures features = new YouTubeAnalyticsFeatures();
    private YouTubeAnalyticsLimits limits = new YouTubeAnalyticsLimits();
    
    @Data
    public static class YouTubeAnalyticsFeatures {
        private boolean enableChannelReports = true;
        private boolean enableVideoReports = true;
        private boolean enablePlaylistReports = true;
        private boolean enableRevenueReports = true;
        private boolean enableEngagementReports = true;
        private boolean enableAudienceReports = true;
        private boolean enableDeviceReports = true;
        private boolean enableGeographyReports = true;
        private boolean enableTrafficSourceReports = true;
        private boolean enableSearchTermReports = true;
        private boolean enableSharingServiceReports = true;
        private boolean enableAnnotationsReports = true;
        private boolean enableCardsReports = true;
        private boolean enableEndScreenReports = true;
        private boolean enableSubtitlesReports = true;
        private boolean enableRealtimeReports = true;
        private boolean enableContentOwnerReports = false;
        private boolean enableAdPerformanceReports = true;
        private boolean enableDemographicsReports = true;
        private boolean enablePlaybackLocationReports = true;
    }
    
    @Data
    public static class YouTubeAnalyticsLimits {
        private int maxReportDimensions = 3;
        private int maxReportMetrics = 10;
        private int maxReportRows = 10000;
        private int maxDateRange = 365; // days
        private int maxGroupItems = 500;
        private int maxFilters = 5;
        private int realtimeDelayMinutes = 5;
        private int dataAvailabilityDelayHours = 48;
    }
    
    // Report dimensions
    public enum Dimension {
        // Time dimensions
        DAY,
        MONTH,
        
        // Video dimensions
        VIDEO,
        PLAYLIST,
        CHANNEL,
        GROUP,
        
        // Audience dimensions
        AGE_GROUP,
        GENDER,
        
        // Geography dimensions
        COUNTRY,
        PROVINCE,
        CITY,
        
        // Playback dimensions
        LIVE_OR_ON_DEMAND,
        SUBSCRIBED_STATUS,
        YOUTUBE_PRODUCT,
        
        // Device dimensions
        DEVICE_TYPE,
        OPERATING_SYSTEM,
        
        // Traffic dimensions
        TRAFFIC_SOURCE_TYPE,
        TRAFFIC_SOURCE_DETAIL,
        
        // Content dimensions
        CLAIMED_STATUS,
        UPLOADER_TYPE,
        
        // Sharing dimensions
        SHARING_SERVICE,
        
        // Search dimensions
        SEARCH_TERM,
        
        // Ad dimensions
        AD_TYPE,
        
        // Playback location
        PLAYBACK_LOCATION_TYPE,
        PLAYBACK_LOCATION_DETAIL
    }
    
    // Report metrics
    public enum Metric {
        // View metrics
        VIEWS,
        RED_VIEWS, // YouTube Premium views
        UNIQUES,
        
        // Watch time metrics
        ESTIMATED_MINUTES_WATCHED,
        ESTIMATED_RED_MINUTES_WATCHED,
        AVERAGE_VIEW_DURATION,
        AVERAGE_VIEW_PERCENTAGE,
        
        // Engagement metrics
        COMMENTS,
        LIKES,
        DISLIKES,
        SHARES,
        SUBSCRIBERS_GAINED,
        SUBSCRIBERS_LOST,
        
        // Revenue metrics
        ESTIMATED_REVENUE,
        ESTIMATED_AD_REVENUE,
        ESTIMATED_RED_PARTNER_REVENUE,
        GROSS_REVENUE,
        CPM,
        PLAYBACK_BASED_CPM,
        
        // Ad metrics
        AD_IMPRESSIONS,
        MONETIZED_PLAYBACKS,
        
        // Annotation metrics
        ANNOTATION_IMPRESSIONS,
        ANNOTATION_CLICKABLE_IMPRESSIONS,
        ANNOTATION_CLICKS,
        ANNOTATION_CLICK_THROUGH_RATE,
        ANNOTATION_CLOSABLE_IMPRESSIONS,
        ANNOTATION_CLOSES,
        ANNOTATION_CLOSE_RATE,
        
        // Card metrics
        CARD_IMPRESSIONS,
        CARD_CLICKS,
        CARD_CLICK_RATE,
        CARD_TEASER_IMPRESSIONS,
        CARD_TEASER_CLICKS,
        CARD_TEASER_CLICK_RATE,
        
        // End screen metrics
        END_SCREEN_ELEMENT_IMPRESSIONS,
        END_SCREEN_ELEMENT_CLICKS,
        END_SCREEN_ELEMENT_CLICK_RATE,
        
        // Playlist metrics
        PLAYLIST_STARTS,
        VIEWS_PER_PLAYLIST_START,
        AVERAGE_TIME_IN_PLAYLIST
    }
    
    // Sort options
    public enum SortOrder {
        ASCENDING,
        DESCENDING
    }
    
    // Filter operators
    public enum FilterOperator {
        EQUALS("=="),
        NOT_EQUALS("!="),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        IN("=@"),
        NOT_IN("!@"),
        CONTAINS("=~"),
        NOT_CONTAINS("!~");
        
        private final String symbol;
        
        FilterOperator(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() { return symbol; }
    }
    
    // Report types
    public enum ReportType {
        CHANNEL_BASIC,
        CHANNEL_DEMOGRAPHICS,
        CHANNEL_GEOGRAPHY,
        CHANNEL_PLAYBACK_LOCATION,
        CHANNEL_TRAFFIC_SOURCE,
        CHANNEL_DEVICE_OS,
        CHANNEL_SUBSCRIPTION_STATUS,
        CHANNEL_SHARING_SERVICE,
        VIDEO_BASIC,
        VIDEO_DEMOGRAPHICS,
        VIDEO_GEOGRAPHY,
        VIDEO_PLAYBACK_LOCATION,
        VIDEO_TRAFFIC_SOURCE,
        VIDEO_DEVICE_OS,
        VIDEO_SUBSCRIPTION_STATUS,
        VIDEO_SHARING_SERVICE,
        PLAYLIST_BASIC,
        PLAYLIST_GEOGRAPHY,
        PLAYLIST_PLAYBACK_LOCATION,
        PLAYLIST_TRAFFIC_SOURCE,
        PLAYLIST_DEVICE_OS,
        AD_PERFORMANCE,
        REVENUE_REPORTS,
        ENGAGEMENT_REPORTS
    }
    
    // Time periods for quick reports
    public enum TimePeriod {
        LAST_7_DAYS,
        LAST_28_DAYS,
        LAST_30_DAYS,
        LAST_90_DAYS,
        LAST_365_DAYS,
        MONTH_TO_DATE,
        QUARTER_TO_DATE,
        YEAR_TO_DATE,
        LIFETIME,
        CUSTOM
    }
    
    // Currency for revenue reports
    public enum Currency {
        USD,
        EUR,
        GBP,
        JPY,
        CAD,
        AUD,
        INR,
        BRL,
        MXN,
        RUB,
        CNY,
        KRW
    }
    
    // Age groups
    public enum AgeGroup {
        AGE_13_17("13-17"),
        AGE_18_24("18-24"),
        AGE_25_34("25-34"),
        AGE_35_44("35-44"),
        AGE_45_54("45-54"),
        AGE_55_64("55-64"),
        AGE_65_PLUS("65+");
        
        private final String range;
        
        AgeGroup(String range) {
            this.range = range;
        }
        
        public String getRange() { return range; }
    }
    
    // Gender values
    public enum Gender {
        FEMALE,
        MALE,
        USER_SPECIFIED_OTHER
    }
    
    // Device types
    public enum DeviceType {
        DESKTOP,
        MOBILE,
        TABLET,
        TV,
        GAME_CONSOLE,
        UNKNOWN
    }
    
    // Operating systems
    public enum OperatingSystem {
        ANDROID,
        IOS,
        WINDOWS,
        MACINTOSH,
        LINUX,
        CHROME_OS,
        PLAYSTATION,
        XBOX,
        OTHER
    }
    
    // Traffic source types
    public enum TrafficSourceType {
        ADVERTISING,
        ANNOTATION,
        CAMPAIGN_CARD,
        END_SCREEN,
        EXT_URL,
        HASHTAGS,
        INFO_CARD,
        NO_LINK_EMBEDDED,
        NO_LINK_OTHER,
        NOTIFICATION,
        PLAYLIST,
        PROMOTED,
        RELATED_VIDEO,
        SHORTS,
        SOUNDS,
        SUBSCRIBER,
        YT_CHANNEL,
        YT_OTHER_PAGE,
        YT_PLAYLIST_PAGE,
        YT_SEARCH,
        YT_SHORTS_FEED,
        YT_TOPIC
    }
    
    // Playback location types
    public enum PlaybackLocationType {
        WATCH,      // YouTube watch page
        EMBEDDED,   // Embedded player
        CHANNEL,    // Channel page
        MOBILE,     // Mobile apps
        YOUTUBE_TV, // YouTube TV app
        UNKNOWN
    }
    
    // Sharing services
    public enum SharingService {
        FACEBOOK,
        TWITTER,
        TUMBLR,
        PINTEREST,
        REDDIT,
        LINKEDIN,
        WHATSAPP,
        EMAIL,
        OTHER,
        UNKNOWN
    }
    
    // Ad types
    public enum AdType {
        AUCTION_DISPLAY,
        AUCTION_INSTREAM,
        AUCTION_TRUEVIEW_INSTREAM,
        AUCTION_TRUEVIEW_INDISPLAY,
        RESERVED_DISPLAY,
        RESERVED_INSTREAM,
        RESERVED_TRUEVIEW_INSTREAM,
        RESERVED_TRUEVIEW_INDISPLAY
    }
    
    // YouTube products
    public enum YouTubeProduct {
        CORE,         // Main YouTube
        GAMING,       // YouTube Gaming
        KIDS,         // YouTube Kids
        MUSIC,        // YouTube Music
        TV,           // YouTube TV
        UNKNOWN
    }
}