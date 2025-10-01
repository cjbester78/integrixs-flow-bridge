package com.integrixs.adapters.social.base;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents analytics data from social media platforms
 */
public class SocialMediaAnalytics {
    public SocialMediaAnalytics() {
    }



    private String postId;
    private String platform;
    private LocalDateTime fetchedAt;
    private EngagementMetrics engagement;
    private ReachMetrics reach;
    private AudienceMetrics audience;
    private Map<String, Object> customMetrics;

    // Direct fields for backward compatibility
    private long likes;
    private long comments;
    private long shares;
    private long saves;
    private long clicks;
    private double engagementRate;
    private Map<String, Long> reactions;
    private long impressions;
    private long uniqueViews;
    private long videoViews;
    private long reachCount;
    private long videoCompletionRate;
    private Map<String, Long> reachBySource;
    private Map<String, Long> ageGroups;
    private Map<String, Long> genders;
    private Map<String, Long> locations;
    private Map<String, Long> devices;
    private Map<String, Long> interests;

                    public static class EngagementMetrics {
        private long likes;
        private long comments;
        private long shares;
        private long saves;
        private long clicks;
        private double engagementRate;
        private Map<String, Long> reactions; // For platforms with multiple reaction types
    }

                    public static class ReachMetrics {
        private long impressions;
        private long reachCount;
        private long uniqueViews;
        private long videoViews;
        private long videoCompletionRate;
        private Map<String, Long> reachBySource;
    }

                    public static class AudienceMetrics {
        private Map<String, Long> ageGroups;
        private Map<String, Long> genders;
        private Map<String, Long> locations;
        private Map<String, Long> devices;
        private Map<String, Long> interests;
    }
    // Getters and Setters
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }
    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
    public EngagementMetrics getEngagement() {
        return engagement;
    }
    public void setEngagement(EngagementMetrics engagement) {
        this.engagement = engagement;
    }
    public ReachMetrics getReach() {
        return reach;
    }
    public void setReach(ReachMetrics reach) {
        this.reach = reach;
    }
    public AudienceMetrics getAudience() {
        return audience;
    }
    public void setAudience(AudienceMetrics audience) {
        this.audience = audience;
    }
    public Map<String, Object> getCustomMetrics() {
        return customMetrics;
    }
    public void setCustomMetrics(Map<String, Object> customMetrics) {
        this.customMetrics = customMetrics;
    }
    public long getLikes() {
        return likes;
    }
    public void setLikes(long likes) {
        this.likes = likes;
    }
    public long getComments() {
        return comments;
    }
    public void setComments(long comments) {
        this.comments = comments;
    }
    public long getShares() {
        return shares;
    }
    public void setShares(long shares) {
        this.shares = shares;
    }
    public long getSaves() {
        return saves;
    }
    public void setSaves(long saves) {
        this.saves = saves;
    }
    public long getClicks() {
        return clicks;
    }
    public void setClicks(long clicks) {
        this.clicks = clicks;
    }
    public double getEngagementRate() {
        return engagementRate;
    }
    public void setEngagementRate(double engagementRate) {
        this.engagementRate = engagementRate;
    }
    public Map<String, Long> getReactions() {
        return reactions;
    }
    public void setReactions(Map<String, Long> reactions) {
        this.reactions = reactions;
    }
    public long getImpressions() {
        return impressions;
    }
    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }
    public long getReachCount() {
        return reachCount;
    }
    public void setReachCount(long reachCount) {
        this.reachCount = reachCount;
    }
    public long getUniqueViews() {
        return uniqueViews;
    }
    public void setUniqueViews(long uniqueViews) {
        this.uniqueViews = uniqueViews;
    }
    public long getVideoViews() {
        return videoViews;
    }
    public void setVideoViews(long videoViews) {
        this.videoViews = videoViews;
    }
    public long getVideoCompletionRate() {
        return videoCompletionRate;
    }
    public void setVideoCompletionRate(long videoCompletionRate) {
        this.videoCompletionRate = videoCompletionRate;
    }
    public Map<String, Long> getReachBySource() {
        return reachBySource;
    }
    public void setReachBySource(Map<String, Long> reachBySource) {
        this.reachBySource = reachBySource;
    }
    public Map<String, Long> getAgeGroups() {
        return ageGroups;
    }
    public void setAgeGroups(Map<String, Long> ageGroups) {
        this.ageGroups = ageGroups;
    }
    public Map<String, Long> getGenders() {
        return genders;
    }
    public void setGenders(Map<String, Long> genders) {
        this.genders = genders;
    }
    public Map<String, Long> getLocations() {
        return locations;
    }
    public void setLocations(Map<String, Long> locations) {
        this.locations = locations;
    }
    public Map<String, Long> getDevices() {
        return devices;
    }
    public void setDevices(Map<String, Long> devices) {
        this.devices = devices;
    }
    public Map<String, Long> getInterests() {
        return interests;
    }
    public void setInterests(Map<String, Long> interests) {
        this.interests = interests;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String postId;
        private String platform;
        private LocalDateTime fetchedAt;
        private EngagementMetrics engagement;
        private ReachMetrics reach;
        private AudienceMetrics audience;
        private Map<String, Object> customMetrics;
        private long likes;
        private long comments;
        private long shares;
        private long saves;
        private long clicks;
        private double engagementRate;
        private Map<String, Long> reactions;
        private long impressions;
        private long reachCount;
        private long uniqueViews;
        private long videoViews;
        private long videoCompletionRate;
        private Map<String, Long> reachBySource;
        private Map<String, Long> ageGroups;
        private Map<String, Long> genders;
        private Map<String, Long> locations;
        private Map<String, Long> devices;
        private Map<String, Long> interests;

        public Builder postId(String postId) {
            this.postId = postId;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder fetchedAt(LocalDateTime fetchedAt) {
            this.fetchedAt = fetchedAt;
            return this;
        }

        public Builder engagement(EngagementMetrics engagement) {
            this.engagement = engagement;
            return this;
        }

        public Builder reach(ReachMetrics reach) {
            this.reach = reach;
            return this;
        }

        public Builder audience(AudienceMetrics audience) {
            this.audience = audience;
            return this;
        }

        public Builder customMetrics(Map<String, Object> customMetrics) {
            this.customMetrics = customMetrics;
            return this;
        }

        public Builder likes(long likes) {
            this.likes = likes;
            return this;
        }

        public Builder comments(long comments) {
            this.comments = comments;
            return this;
        }

        public Builder shares(long shares) {
            this.shares = shares;
            return this;
        }

        public Builder saves(long saves) {
            this.saves = saves;
            return this;
        }

        public Builder clicks(long clicks) {
            this.clicks = clicks;
            return this;
        }

        public Builder engagementRate(double engagementRate) {
            this.engagementRate = engagementRate;
            return this;
        }

        public Builder reactions(Map<String, Long> reactions) {
            this.reactions = reactions;
            return this;
        }

        public Builder impressions(long impressions) {
            this.impressions = impressions;
            return this;
        }


        public Builder uniqueViews(long uniqueViews) {
            this.uniqueViews = uniqueViews;
            return this;
        }

        public Builder videoViews(long videoViews) {
            this.videoViews = videoViews;
            return this;
        }

        public Builder videoCompletionRate(long videoCompletionRate) {
            this.videoCompletionRate = videoCompletionRate;
            return this;
        }

        public Builder reachBySource(Map<String, Long> reachBySource) {
            this.reachBySource = reachBySource;
            return this;
        }

        public Builder ageGroups(Map<String, Long> ageGroups) {
            this.ageGroups = ageGroups;
            return this;
        }

        public Builder genders(Map<String, Long> genders) {
            this.genders = genders;
            return this;
        }

        public Builder locations(Map<String, Long> locations) {
            this.locations = locations;
            return this;
        }

        public Builder devices(Map<String, Long> devices) {
            this.devices = devices;
            return this;
        }

        public Builder interests(Map<String, Long> interests) {
            this.interests = interests;
            return this;
        }

        public SocialMediaAnalytics build() {
            SocialMediaAnalytics obj = new SocialMediaAnalytics();
            obj.postId = this.postId;
            obj.platform = this.platform;
            obj.fetchedAt = this.fetchedAt;
            obj.engagement = this.engagement;
            obj.reach = this.reach;
            obj.audience = this.audience;
            obj.customMetrics = this.customMetrics;
            obj.likes = this.likes;
            obj.comments = this.comments;
            obj.shares = this.shares;
            obj.saves = this.saves;
            obj.clicks = this.clicks;
            obj.engagementRate = this.engagementRate;
            obj.reactions = this.reactions;
            obj.impressions = this.impressions;
            obj.reachCount = this.reachCount;
            obj.uniqueViews = this.uniqueViews;
            obj.videoViews = this.videoViews;
            obj.videoCompletionRate = this.videoCompletionRate;
            obj.reachBySource = this.reachBySource;
            obj.ageGroups = this.ageGroups;
            obj.genders = this.genders;
            obj.locations = this.locations;
            obj.devices = this.devices;
            obj.interests = this.interests;
            return obj;
        }
    }
}
