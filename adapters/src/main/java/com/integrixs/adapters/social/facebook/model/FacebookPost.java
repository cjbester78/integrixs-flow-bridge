package com.integrixs.adapters.social.facebook.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Facebook post model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacebookPost {
    
    private String message;
    private String link;
    private List<String> photoUrls;
    private String videoUrl;
    private boolean published = true;
    private LocalDateTime scheduledPublishTime;
    private FacebookTargeting targeting;
    private List<String> tags;
    private String place;  // Location ID
    private Map<String, Object> callToAction;
    
    // Story and Reel specific
    private boolean isStory;
    private boolean isReel;
    private Integer storyDurationSeconds;
    
    // Live video specific
    private boolean isLiveVideo;
    private String liveVideoTitle;
    private String liveVideoDescription;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookTargeting {
        private List<String> countries;
        private List<String> cities;
        private List<String> regions;
        private List<String> locales;  // Languages
        private Integer ageMin;
        private Integer ageMax;
        private List<Integer> genders;  // 1=male, 2=female
        private List<String> interests;
        private List<String> behaviors;
        private Map<String, Object> customAudiences;
        private String audienceOptimization;  // "NONE", "INTEREST_BASED", "CUSTOM"
    }
}

/**
 * Facebook post response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class FacebookPostResponse {
    private String id;
    private boolean success;
    private String message;
    private String permalinkUrl;
    private Map<String, String> photoIds;  // For multi-photo posts
    private LocalDateTime createdTime;
}

/**
 * Facebook insights data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class FacebookInsights {
    private String name;
    private String period;
    private List<InsightValue> values;
    private String title;
    private String description;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsightValue {
        private Object value;
        private String endTime;
    }
}

/**
 * Facebook API exception
 */
class FacebookApiException extends RuntimeException {
    public FacebookApiException(String message) {
        super(message);
    }
    
    public FacebookApiException(String message, Throwable cause) {
        super(message, cause);
    }
}