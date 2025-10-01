package com.integrixs.adapters.social.facebook.model;

import java.util.Map;

public class FacebookPostResponse {
    private String id;
    private String createdTime;
    private boolean success;
    private String message;
    private String permalinkUrl;
    private Map<String, String> photoIds;
    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getPermalinkUrl() {
        return permalinkUrl;
    }
    public void setPermalinkUrl(String permalinkUrl) {
        this.permalinkUrl = permalinkUrl;
    }
    public Map<String, String> getPhotoIds() {
        return photoIds;
    }
    public void setPhotoIds(Map<String, String> photoIds) {
        this.photoIds = photoIds;
    }
}
