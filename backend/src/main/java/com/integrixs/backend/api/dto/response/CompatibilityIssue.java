package com.integrixs.backend.api.dto.response;

public class CompatibilityIssue {

    public enum Severity {
        ERROR, WARNING, INFO
    }

    public enum Category {
        TYPE_MISMATCH,
        MISSING_FIELD,
        FORMAT_DIFFERENCE,
        CONSTRAINT_CONFLICT,
        NAMESPACE_ISSUE,
        OTHER
    }

    private Severity severity;
    private Category category;
    private String sourcePath;
    private String targetPath;
    private String message;
    private String suggestion;

    // Default constructor
    public CompatibilityIssue() {
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    // Builder
    public static CompatibilityIssueBuilder builder() {
        return new CompatibilityIssueBuilder();
    }

    public static class CompatibilityIssueBuilder {
        private Severity severity;
        private Category category;
        private String sourcePath;
        private String targetPath;
        private String message;
        private String suggestion;

        public CompatibilityIssueBuilder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public CompatibilityIssueBuilder category(Category category) {
            this.category = category;
            return this;
        }

        public CompatibilityIssueBuilder sourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public CompatibilityIssueBuilder targetPath(String targetPath) {
            this.targetPath = targetPath;
            return this;
        }

        public CompatibilityIssueBuilder message(String message) {
            this.message = message;
            return this;
        }

        public CompatibilityIssueBuilder suggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public CompatibilityIssue build() {
            CompatibilityIssue issue = new CompatibilityIssue();
            issue.setSeverity(this.severity);
            issue.setCategory(this.category);
            issue.setSourcePath(this.sourcePath);
            issue.setTargetPath(this.targetPath);
            issue.setMessage(this.message);
            issue.setSuggestion(this.suggestion);
            return issue;
        }
    }
}
