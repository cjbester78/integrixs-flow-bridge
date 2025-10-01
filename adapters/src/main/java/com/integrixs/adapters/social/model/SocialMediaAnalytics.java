package com.integrixs.adapters.social.model;

import java.time.LocalDateTime;
import java.util.Map;

public class SocialMediaAnalytics {
    private String contentId;
    private LocalDateTime timestamp;
    private Long impressions;
    private Long reach;
    private Long engagement;
    private Long clicks;
    private Double engagementRate;
    private Map<String, Object> demographicData;
    // Getters and Setters
    public String getContentId() {
        return contentId;
    }
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public Long getImpressions() {
        return impressions;
    }
    public void setImpressions(Long impressions) {
        this.impressions = impressions;
    }
    public Long getReach() {
        return reach;
    }
    public void setReach(Long reach) {
        this.reach = reach;
    }
    public Long getEngagement() {
        return engagement;
    }
    public void setEngagement(Long engagement) {
        this.engagement = engagement;
    }
    public Long getClicks() {
        return clicks;
    }
    public void setClicks(Long clicks) {
        this.clicks = clicks;
    }
    public Double getEngagementRate() {
        return engagementRate;
    }
    public void setEngagementRate(Double engagementRate) {
        this.engagementRate = engagementRate;
    }
    public Map<String, Object> getDemographicData() {
        return demographicData;
    }
    public void setDemographicData(Map<String, Object> demographicData) {
        this.demographicData = demographicData;
    }
}
