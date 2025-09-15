package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
