package com.integrixs.data.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an organization in the marketplace
 */
public class Organization extends BaseEntity {

    private String name;
    private String description;
    private String website;
    private String email;
    private String contactPerson;
    private String logoUrl;
    private boolean verified;
    private LocalDateTime verifiedAt;
    private String verificationDetails;
    private int templateCount;
    private int downloadCount;
    private double averageRating;

    // Getters and setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerificationDetails() {
        return verificationDetails;
    }

    public void setVerificationDetails(String verificationDetails) {
        this.verificationDetails = verificationDetails;
    }

    public int getTemplateCount() {
        return templateCount;
    }

    public void setTemplateCount(int templateCount) {
        this.templateCount = templateCount;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}