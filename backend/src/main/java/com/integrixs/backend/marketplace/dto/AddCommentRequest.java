package com.integrixs.backend.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
public class AddCommentRequest {
    @NotBlank
    private String content;

    private String parentCommentId;

    // Default constructor
    public AddCommentRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}