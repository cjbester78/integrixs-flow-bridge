package com.integrixs.backend.api.dto.response;

import java.util.List;
import java.util.ArrayList;

public class StructureCompatibilityResponse {

    private int overallCompatibility; // 0-100 percentage
    private boolean isCompatible;

    private List<CompatibilityIssue> issues = new ArrayList<>();

    private List<FieldMapping> mappings = new ArrayList<>();

    private StructureMetadata sourceMetadata;
    private StructureMetadata targetMetadata;

    private List<String> recommendations = new ArrayList<>();

    // Default constructor
    public StructureCompatibilityResponse() {
    }

    public int getOverallCompatibility() {
        return overallCompatibility;
    }

    public void setOverallCompatibility(int overallCompatibility) {
        this.overallCompatibility = overallCompatibility;
    }

    public boolean isCompatible() {
        return isCompatible;
    }

    public void setCompatible(boolean isCompatible) {
        this.isCompatible = isCompatible;
    }

    public List<CompatibilityIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<CompatibilityIssue> issues) {
        this.issues = issues;
    }

    public List<FieldMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<FieldMapping> mappings) {
        this.mappings = mappings;
    }

    public StructureMetadata getSourceMetadata() {
        return sourceMetadata;
    }

    public void setSourceMetadata(StructureMetadata sourceMetadata) {
        this.sourceMetadata = sourceMetadata;
    }

    public StructureMetadata getTargetMetadata() {
        return targetMetadata;
    }

    public void setTargetMetadata(StructureMetadata targetMetadata) {
        this.targetMetadata = targetMetadata;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    // Builder
    public static StructureCompatibilityResponseBuilder builder() {
        return new StructureCompatibilityResponseBuilder();
    }

    public static class StructureCompatibilityResponseBuilder {
        private int overallCompatibility;
        private boolean isCompatible;
        private List<CompatibilityIssue> issues = new ArrayList<>();
        private List<FieldMapping> mappings = new ArrayList<>();
        private StructureMetadata sourceMetadata;
        private StructureMetadata targetMetadata;
        private List<String> recommendations = new ArrayList<>();

        public StructureCompatibilityResponseBuilder overallCompatibility(int overallCompatibility) {
            this.overallCompatibility = overallCompatibility;
            return this;
        }

        public StructureCompatibilityResponseBuilder isCompatible(boolean isCompatible) {
            this.isCompatible = isCompatible;
            return this;
        }

        public StructureCompatibilityResponseBuilder issues(List<CompatibilityIssue> issues) {
            this.issues = issues;
            return this;
        }

        public StructureCompatibilityResponseBuilder mappings(List<FieldMapping> mappings) {
            this.mappings = mappings;
            return this;
        }

        public StructureCompatibilityResponseBuilder sourceMetadata(StructureMetadata sourceMetadata) {
            this.sourceMetadata = sourceMetadata;
            return this;
        }

        public StructureCompatibilityResponseBuilder targetMetadata(StructureMetadata targetMetadata) {
            this.targetMetadata = targetMetadata;
            return this;
        }

        public StructureCompatibilityResponseBuilder recommendations(List<String> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public StructureCompatibilityResponse build() {
            StructureCompatibilityResponse response = new StructureCompatibilityResponse();
            response.setOverallCompatibility(this.overallCompatibility);
            response.setCompatible(this.isCompatible);
            response.setIssues(this.issues);
            response.setMappings(this.mappings);
            response.setSourceMetadata(this.sourceMetadata);
            response.setTargetMetadata(this.targetMetadata);
            response.setRecommendations(this.recommendations);
            return response;
        }
    }
}
