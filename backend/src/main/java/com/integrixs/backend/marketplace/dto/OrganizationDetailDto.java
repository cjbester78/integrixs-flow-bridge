package com.integrixs.backend.marketplace.dto;

import java.util.List;

public class OrganizationDetailDto extends OrganizationDto {
    private List<TemplateDto> templates;
    private int memberCount;
    private int totalDownloads;
    private double averageRating;

    // Default constructor
    public OrganizationDetailDto() {
    }

    public List<TemplateDto> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateDto> templates) {
        this.templates = templates;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(int totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}