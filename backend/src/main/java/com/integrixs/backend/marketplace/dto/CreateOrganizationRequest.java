package com.integrixs.backend.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
public class CreateOrganizationRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String website;
    private String logoUrl;

    // Default constructor
    public CreateOrganizationRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}