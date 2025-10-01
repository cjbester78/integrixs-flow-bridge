package com.integrixs.adapters.social.base;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents content to be posted on social media platforms
 */
public class SocialMediaContent {
    public SocialMediaContent() {
    }



    private String id;
    private String text;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private String link;
    private List<String> hashtags;
    private List<String> mentions;
    private LocalDateTime scheduledTime;
    private boolean published;
    private Map<String, Object> platformSpecificData;
    private ContentType contentType;
    private String author;
    private String authorId;

    public enum ContentType {
        POST,
        STORY,
        REEL,
        VIDEO,
        IMAGE,
        CAROUSEL,
        LIVE,
        POLL,
        EVENT
    }
    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public List<String> getImageUrls() {
        return imageUrls;
    }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    public List<String> getVideoUrls() {
        return videoUrls;
    }
    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public List<String> getHashtags() {
        return hashtags;
    }
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }
    public List<String> getMentions() {
        return mentions;
    }
    public void setMentions(List<String> mentions) {
        this.mentions = mentions;
    }
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    public boolean isPublished() {
        return published;
    }
    public void setPublished(boolean published) {
        this.published = published;
    }
    public Map<String, Object> getPlatformSpecificData() {
        return platformSpecificData;
    }
    public void setPlatformSpecificData(Map<String, Object> platformSpecificData) {
        this.platformSpecificData = platformSpecificData;
    }
    public ContentType getContentType() {
        return contentType;
    }
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String text;
        private List<String> imageUrls;
        private List<String> videoUrls;
        private String link;
        private List<String> hashtags;
        private List<String> mentions;
        private LocalDateTime scheduledTime;
        private boolean published;
        private Map<String, Object> platformSpecificData;
        private ContentType contentType;
        private String author;
        private String authorId;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder imageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
            return this;
        }

        public Builder videoUrls(List<String> videoUrls) {
            this.videoUrls = videoUrls;
            return this;
        }

        public Builder link(String link) {
            this.link = link;
            return this;
        }

        public Builder hashtags(List<String> hashtags) {
            this.hashtags = hashtags;
            return this;
        }

        public Builder mentions(List<String> mentions) {
            this.mentions = mentions;
            return this;
        }

        public Builder scheduledTime(LocalDateTime scheduledTime) {
            this.scheduledTime = scheduledTime;
            return this;
        }

        public Builder published(boolean published) {
            this.published = published;
            return this;
        }

        public Builder platformSpecificData(Map<String, Object> platformSpecificData) {
            this.platformSpecificData = platformSpecificData;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder authorId(String authorId) {
            this.authorId = authorId;
            return this;
        }

        public SocialMediaContent build() {
            SocialMediaContent obj = new SocialMediaContent();
            obj.id = this.id;
            obj.text = this.text;
            obj.imageUrls = this.imageUrls;
            obj.videoUrls = this.videoUrls;
            obj.link = this.link;
            obj.hashtags = this.hashtags;
            obj.mentions = this.mentions;
            obj.scheduledTime = this.scheduledTime;
            obj.published = this.published;
            obj.platformSpecificData = this.platformSpecificData;
            obj.contentType = this.contentType;
            obj.author = this.author;
            obj.authorId = this.authorId;
            return obj;
        }
    }
}
