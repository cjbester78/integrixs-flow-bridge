package com.integrixs.adapters.social.model;

import java.time.LocalDateTime;

public class SocialMediaResponse {
    private String id;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private String url;
    private Long views;
    private Long likes;
    private Long shares;
    private Long comments;
    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Long getViews() {
        return views;
    }
    public void setViews(Long views) {
        this.views = views;
    }
    public Long getLikes() {
        return likes;
    }
    public void setLikes(Long likes) {
        this.likes = likes;
    }
    public Long getShares() {
        return shares;
    }
    public void setShares(Long shares) {
        this.shares = shares;
    }
    public Long getComments() {
        return comments;
    }
    public void setComments(Long comments) {
        this.comments = comments;
    }
}
