package com.integrixs.adapters.social.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Generic social media content model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaContent {
    
    // Content Identifiers
    private String id;
    private String platform;
    private String contentType; // post, story, reel, video, comment, message
    private String parentId; // For comments or replies
    
    // Content Data
    private String text;
    private List<MediaAttachment> media;
    private List<String> hashtags;
    private List<String> mentions;
    private String link;
    private Map<String, Object> metadata;
    
    // Publishing Options
    private boolean published;
    private LocalDateTime scheduledTime;
    private ContentVisibility visibility;
    private TargetAudience targetAudience;
    
    // Engagement Metrics
    private EngagementMetrics engagement;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaAttachment {
        private String type; // image, video, gif, document
        private String url;
        private String thumbnailUrl;
        private String mimeType;
        private Long sizeBytes;
        private Integer width;
        private Integer height;
        private Integer durationSeconds; // For videos
        private String caption;
        private Map<String, String> metadata;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EngagementMetrics {
        private Long likes;
        private Long comments;
        private Long shares;
        private Long views;
        private Long saves;
        private Long clicks;
        private Double engagementRate;
        private Map<String, Long> customMetrics;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetAudience {
        private List<String> countries;
        private List<String> cities;
        private List<String> languages;
        private Integer ageMin;
        private Integer ageMax;
        private List<String> genders;
        private List<String> interests;
        private Map<String, Object> customTargeting;
    }
    
    public enum ContentVisibility {
        PUBLIC,
        FRIENDS,
        FOLLOWERS,
        PRIVATE,
        CUSTOM
    }
}