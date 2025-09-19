package com.integrixs.backend.marketplace.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public class RateTemplateRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    
    private String comment;

    // Default constructor
    public RateTemplateRequest() {
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}