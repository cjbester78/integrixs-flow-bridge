package com.integrixs.adapters.social.facebook.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Facebook post model
 */
public class FacebookPost {
    public FacebookPost() {
    }



    private String message;
    private String link;
    private List<String> photoUrls;
    private String videoUrl;
    private boolean published = true;
    private LocalDateTime scheduledPublishTime;
    private FacebookTargeting targeting;
    private List<String> tags;
    private String place; // Location ID
    private Map<String, Object> callToAction;

    // Story and Reel specific
    private boolean isStory;
    private boolean isReel;
    private Integer storyDurationSeconds;

    // Live video specific
    private boolean isLiveVideo;
    private String liveVideoTitle;
    private String liveVideoDescription;

                    public static class FacebookTargeting {
        private List<String> countries;
        private List<String> cities;
        private List<String> regions;
        private List<String> locales; // Languages
        private Integer ageMin;
        private Integer ageMax;
        private List<Integer> genders; // 1 = male, 2 = female
        private List<String> interests;
        private List<String> behaviors;
        private Map<String, Object> customAudiences;
        private String audienceOptimization; // "NONE", "INTEREST_BASED", "CUSTOM"

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private List<String> countries;
            private Integer ageMin;
            private Integer ageMax;
            private List<String> interests;

            public Builder countries(List<String> countries) {
                this.countries = countries;
                return this;
            }

            public Builder ageMin(Integer ageMin) {
                this.ageMin = ageMin;
                return this;
            }

            public Builder ageMax(Integer ageMax) {
                this.ageMax = ageMax;
                return this;
            }

            public Builder interests(List<String> interests) {
                this.interests = interests;
                return this;
            }

            public FacebookTargeting build() {
                FacebookTargeting targeting = new FacebookTargeting();
                targeting.countries = this.countries;
                targeting.ageMin = this.ageMin;
                targeting.ageMax = this.ageMax;
                targeting.interests = this.interests;
                return targeting;
            }
        }

        // Getters and Setters for FacebookTargeting
        public List<String> getCountries() {
            return countries;
        }
        public void setCountries(List<String> countries) {
            this.countries = countries;
        }
        public List<String> getCities() {
            return cities;
        }
        public void setCities(List<String> cities) {
            this.cities = cities;
        }
        public List<String> getRegions() {
            return regions;
        }
        public void setRegions(List<String> regions) {
            this.regions = regions;
        }
        public List<String> getLocales() {
            return locales;
        }
        public void setLocales(List<String> locales) {
            this.locales = locales;
        }
        public Integer getAgeMin() {
            return ageMin;
        }
        public void setAgeMin(Integer ageMin) {
            this.ageMin = ageMin;
        }
        public Integer getAgeMax() {
            return ageMax;
        }
        public void setAgeMax(Integer ageMax) {
            this.ageMax = ageMax;
        }
        public List<Integer> getGenders() {
            return genders;
        }
        public void setGenders(List<Integer> genders) {
            this.genders = genders;
        }
        public List<String> getInterests() {
            return interests;
        }
        public void setInterests(List<String> interests) {
            this.interests = interests;
        }
        public List<String> getBehaviors() {
            return behaviors;
        }
        public void setBehaviors(List<String> behaviors) {
            this.behaviors = behaviors;
        }
        public Map<String, Object> getCustomAudiences() {
            return customAudiences;
        }
        public void setCustomAudiences(Map<String, Object> customAudiences) {
            this.customAudiences = customAudiences;
        }
        public String getAudienceOptimization() {
            return audienceOptimization;
        }
        public void setAudienceOptimization(String audienceOptimization) {
            this.audienceOptimization = audienceOptimization;
        }
    }
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public List<String> getPhotoUrls() {
        return photoUrls;
    }
    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }
    public String getVideoUrl() {
        return videoUrl;
    }
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    public boolean isPublished() {
        return published;
    }
    public void setPublished(boolean published) {
        this.published = published;
    }
    public LocalDateTime getScheduledPublishTime() {
        return scheduledPublishTime;
    }
    public void setScheduledPublishTime(LocalDateTime scheduledPublishTime) {
        this.scheduledPublishTime = scheduledPublishTime;
    }
    public FacebookTargeting getTargeting() {
        return targeting;
    }
    public void setTargeting(FacebookTargeting targeting) {
        this.targeting = targeting;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public String getPlace() {
        return place;
    }
    public void setPlace(String place) {
        this.place = place;
    }
    public Map<String, Object> getCallToAction() {
        return callToAction;
    }
    public void setCallToAction(Map<String, Object> callToAction) {
        this.callToAction = callToAction;
    }
    public boolean isIsStory() {
        return isStory;
    }
    public void setIsStory(boolean isStory) {
        this.isStory = isStory;
    }
    public boolean isIsReel() {
        return isReel;
    }
    public void setIsReel(boolean isReel) {
        this.isReel = isReel;
    }
    public Integer getStoryDurationSeconds() {
        return storyDurationSeconds;
    }
    public void setStoryDurationSeconds(Integer storyDurationSeconds) {
        this.storyDurationSeconds = storyDurationSeconds;
    }
    public boolean isIsLiveVideo() {
        return isLiveVideo;
    }
    public void setIsLiveVideo(boolean isLiveVideo) {
        this.isLiveVideo = isLiveVideo;
    }
    public String getLiveVideoTitle() {
        return liveVideoTitle;
    }
    public void setLiveVideoTitle(String liveVideoTitle) {
        this.liveVideoTitle = liveVideoTitle;
    }
    public String getLiveVideoDescription() {
        return liveVideoDescription;
    }
    public void setLiveVideoDescription(String liveVideoDescription) {
        this.liveVideoDescription = liveVideoDescription;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private String link;
        private List<String> photoUrls;
        private String videoUrl;
        private boolean published;
        private LocalDateTime scheduledPublishTime;
        private FacebookTargeting targeting;
        private List<String> tags;
        private String place;
        private Map<String, Object> callToAction;
        private boolean isStory;
        private boolean isReel;
        private Integer storyDurationSeconds;
        private boolean isLiveVideo;
        private String liveVideoTitle;
        private String liveVideoDescription;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder link(String link) {
            this.link = link;
            return this;
        }

        public Builder photoUrls(List<String> photoUrls) {
            this.photoUrls = photoUrls;
            return this;
        }

        public Builder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public Builder published(boolean published) {
            this.published = published;
            return this;
        }

        public Builder scheduledPublishTime(LocalDateTime scheduledPublishTime) {
            this.scheduledPublishTime = scheduledPublishTime;
            return this;
        }

        public Builder targeting(FacebookTargeting targeting) {
            this.targeting = targeting;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder place(String place) {
            this.place = place;
            return this;
        }

        public Builder callToAction(Map<String, Object> callToAction) {
            this.callToAction = callToAction;
            return this;
        }

        public Builder isStory(boolean isStory) {
            this.isStory = isStory;
            return this;
        }

        public Builder isReel(boolean isReel) {
            this.isReel = isReel;
            return this;
        }

        public Builder storyDurationSeconds(Integer storyDurationSeconds) {
            this.storyDurationSeconds = storyDurationSeconds;
            return this;
        }

        public Builder isLiveVideo(boolean isLiveVideo) {
            this.isLiveVideo = isLiveVideo;
            return this;
        }

        public Builder liveVideoTitle(String liveVideoTitle) {
            this.liveVideoTitle = liveVideoTitle;
            return this;
        }

        public Builder liveVideoDescription(String liveVideoDescription) {
            this.liveVideoDescription = liveVideoDescription;
            return this;
        }

        public FacebookPost build() {
            FacebookPost obj = new FacebookPost();
            obj.message = this.message;
            obj.link = this.link;
            obj.photoUrls = this.photoUrls;
            obj.videoUrl = this.videoUrl;
            obj.published = this.published;
            obj.scheduledPublishTime = this.scheduledPublishTime;
            obj.targeting = this.targeting;
            obj.tags = this.tags;
            obj.place = this.place;
            obj.callToAction = this.callToAction;
            obj.isStory = this.isStory;
            obj.isReel = this.isReel;
            obj.storyDurationSeconds = this.storyDurationSeconds;
            obj.isLiveVideo = this.isLiveVideo;
            obj.liveVideoTitle = this.liveVideoTitle;
            obj.liveVideoDescription = this.liveVideoDescription;
            return obj;
        }
    }
}
