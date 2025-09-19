package com.integrixs.monitoring.api.dto;


/**
 * DTO for acknowledge alert request
 */
public class AcknowledgeAlertRequestDTO {
    private String userId;
    private String comment;


    // Getters
    public String getUserId() {
        return userId;
    }

    public String getComment() {
        return comment;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
