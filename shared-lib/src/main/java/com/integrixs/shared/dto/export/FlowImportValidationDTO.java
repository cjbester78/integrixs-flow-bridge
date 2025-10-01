package com.integrixs.shared.dto.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO for flow import validation results.
 * Used to preview what will happen during import.
 */
public class FlowImportValidationDTO {

    private boolean canImport;
    private boolean isValid;
    private ImportPreview preview;
    private List<ValidationIssue> errors;
    private List<ValidationIssue> warnings;
    private List<Conflict> conflicts;
    private List<String> requiredPermissions;
    private VersionCompatibility versionCompatibility;

    // Default constructor
    public FlowImportValidationDTO() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.conflicts = new ArrayList<>();
        this.requiredPermissions = new ArrayList<>();
    }

    // All args constructor
    public FlowImportValidationDTO(boolean canImport, boolean isValid, ImportPreview preview, List<ValidationIssue> errors, List<ValidationIssue> warnings, List<Conflict> conflicts, List<String> requiredPermissions, VersionCompatibility versionCompatibility) {
        this.canImport = canImport;
        this.isValid = isValid;
        this.preview = preview;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.conflicts = conflicts != null ? conflicts : new ArrayList<>();
        this.requiredPermissions = requiredPermissions != null ? requiredPermissions : new ArrayList<>();
        this.versionCompatibility = versionCompatibility;
    }

    // Getters
    public boolean isCanImport() { return canImport; }
    public boolean isValid() { return isValid; }
    public ImportPreview getPreview() { return preview; }
    public List<ValidationIssue> getErrors() { return errors; }
    public List<ValidationIssue> getWarnings() { return warnings; }
    public List<Conflict> getConflicts() { return conflicts; }
    public List<String> getRequiredPermissions() { return requiredPermissions; }
    public VersionCompatibility getVersionCompatibility() { return versionCompatibility; }

    // Setters
    public void setCanImport(boolean canImport) { this.canImport = canImport; }
    public void setValid(boolean isValid) { this.isValid = isValid; }
    public void setPreview(ImportPreview preview) { this.preview = preview; }
    public void setErrors(List<ValidationIssue> errors) { this.errors = errors; }
    public void setWarnings(List<ValidationIssue> warnings) { this.warnings = warnings; }
    public void setConflicts(List<Conflict> conflicts) { this.conflicts = conflicts; }
    public void setRequiredPermissions(List<String> requiredPermissions) { this.requiredPermissions = requiredPermissions; }
    public void setVersionCompatibility(VersionCompatibility versionCompatibility) { this.versionCompatibility = versionCompatibility; }

    // Builder
    public static FlowImportValidationDTOBuilder builder() {
        return new FlowImportValidationDTOBuilder();
    }

    public static class FlowImportValidationDTOBuilder {
        private boolean canImport;
        private boolean isValid;
        private ImportPreview preview;
        private List<ValidationIssue> errors = new ArrayList<>();
        private List<ValidationIssue> warnings = new ArrayList<>();
        private List<Conflict> conflicts = new ArrayList<>();
        private List<String> requiredPermissions = new ArrayList<>();
        private VersionCompatibility versionCompatibility;

        public FlowImportValidationDTOBuilder canImport(boolean canImport) {
            this.canImport = canImport;
            return this;
        }

        public FlowImportValidationDTOBuilder isValid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public FlowImportValidationDTOBuilder preview(ImportPreview preview) {
            this.preview = preview;
            return this;
        }

        public FlowImportValidationDTOBuilder errors(List<ValidationIssue> errors) {
            this.errors = errors;
            return this;
        }

        public FlowImportValidationDTOBuilder warnings(List<ValidationIssue> warnings) {
            this.warnings = warnings;
            return this;
        }

        public FlowImportValidationDTOBuilder conflicts(List<Conflict> conflicts) {
            this.conflicts = conflicts;
            return this;
        }

        public FlowImportValidationDTOBuilder requiredPermissions(List<String> requiredPermissions) {
            this.requiredPermissions = requiredPermissions;
            return this;
        }

        public FlowImportValidationDTOBuilder versionCompatibility(VersionCompatibility versionCompatibility) {
            this.versionCompatibility = versionCompatibility;
            return this;
        }

        public FlowImportValidationDTO build() {
            return new FlowImportValidationDTO(canImport, isValid, preview, errors, warnings, conflicts, requiredPermissions, versionCompatibility);
        }
    }

    /**
     * Inner class for import preview information
     */
    public static class ImportPreview {
        private String flowName;
        private String flowDescription;
        private String businessComponentName;
        private String inboundAdapterName;
        private String outboundAdapterName;
        private int transformationCount;
        private int fieldMappingCount;
        private int certificateReferenceCount;
        private Map<String, Integer> objectCounts;

        public ImportPreview() {
            this.objectCounts = new HashMap<>();
        }

        // Getters and setters
        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public String getFlowDescription() { return flowDescription; }
        public void setFlowDescription(String flowDescription) { this.flowDescription = flowDescription; }
        public String getBusinessComponentName() { return businessComponentName; }
        public void setBusinessComponentName(String businessComponentName) { this.businessComponentName = businessComponentName; }
        public String getInboundAdapterName() { return inboundAdapterName; }
        public void setInboundAdapterName(String inboundAdapterName) { this.inboundAdapterName = inboundAdapterName; }
        public String getOutboundAdapterName() { return outboundAdapterName; }
        public void setOutboundAdapterName(String outboundAdapterName) { this.outboundAdapterName = outboundAdapterName; }
        public int getTransformationCount() { return transformationCount; }
        public void setTransformationCount(int transformationCount) { this.transformationCount = transformationCount; }
        public int getFieldMappingCount() { return fieldMappingCount; }
        public void setFieldMappingCount(int fieldMappingCount) { this.fieldMappingCount = fieldMappingCount; }
        public int getCertificateReferenceCount() { return certificateReferenceCount; }
        public void setCertificateReferenceCount(int certificateReferenceCount) { this.certificateReferenceCount = certificateReferenceCount; }
        public Map<String, Integer> getObjectCounts() { return objectCounts; }
        public void setObjectCounts(Map<String, Integer> objectCounts) { this.objectCounts = objectCounts; }

        // Builder
        public static ImportPreviewBuilder builder() {
            return new ImportPreviewBuilder();
        }

        public static class ImportPreviewBuilder {
            private String flowName;
            private String flowDescription;
            private String businessComponentName;
            private String inboundAdapterName;
            private String outboundAdapterName;
            private int transformationCount;
            private int fieldMappingCount;
            private int certificateReferenceCount;
            private Map<String, Integer> objectCounts = new HashMap<>();

            public ImportPreviewBuilder flowName(String flowName) {
                this.flowName = flowName;
                return this;
            }

            public ImportPreviewBuilder flowDescription(String flowDescription) {
                this.flowDescription = flowDescription;
                return this;
            }

            public ImportPreviewBuilder businessComponentName(String businessComponentName) {
                this.businessComponentName = businessComponentName;
                return this;
            }

            public ImportPreviewBuilder inboundAdapterName(String inboundAdapterName) {
                this.inboundAdapterName = inboundAdapterName;
                return this;
            }

            public ImportPreviewBuilder outboundAdapterName(String outboundAdapterName) {
                this.outboundAdapterName = outboundAdapterName;
                return this;
            }

            public ImportPreviewBuilder transformationCount(int transformationCount) {
                this.transformationCount = transformationCount;
                return this;
            }

            public ImportPreviewBuilder fieldMappingCount(int fieldMappingCount) {
                this.fieldMappingCount = fieldMappingCount;
                return this;
            }

            public ImportPreviewBuilder certificateReferenceCount(int certificateReferenceCount) {
                this.certificateReferenceCount = certificateReferenceCount;
                return this;
            }

            public ImportPreviewBuilder objectCounts(Map<String, Integer> objectCounts) {
                this.objectCounts = objectCounts;
                return this;
            }

            public ImportPreview build() {
                ImportPreview preview = new ImportPreview();
                preview.flowName = this.flowName;
                preview.flowDescription = this.flowDescription;
                preview.businessComponentName = this.businessComponentName;
                preview.inboundAdapterName = this.inboundAdapterName;
                preview.outboundAdapterName = this.outboundAdapterName;
                preview.transformationCount = this.transformationCount;
                preview.fieldMappingCount = this.fieldMappingCount;
                preview.certificateReferenceCount = this.certificateReferenceCount;
                preview.objectCounts = this.objectCounts;
                return preview;
            }
        }
    }

    /**
     * Inner class for validation issues
     */
    public static class ValidationIssue {
        private String code;
        private String message;
        private String field;
        private String objectType;
        private String objectId;
        private Severity severity;
        private Map<String, Object> context;

        public ValidationIssue() {
            this.context = new HashMap<>();
        }

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getObjectType() { return objectType; }
        public void setObjectType(String objectType) { this.objectType = objectType; }
        public String getObjectId() { return objectId; }
        public void setObjectId(String objectId) { this.objectId = objectId; }
        public Severity getSeverity() { return severity; }
        public void setSeverity(Severity severity) { this.severity = severity; }
        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }

        // Builder
        public static ValidationIssueBuilder builder() {
            return new ValidationIssueBuilder();
        }

        public static class ValidationIssueBuilder {
            private String code;
            private String message;
            private String field;
            private String objectType;
            private String objectId;
            private Severity severity;
            private Map<String, Object> context = new HashMap<>();

            public ValidationIssueBuilder code(String code) {
                this.code = code;
                return this;
            }

            public ValidationIssueBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ValidationIssueBuilder field(String field) {
                this.field = field;
                return this;
            }

            public ValidationIssueBuilder objectType(String objectType) {
                this.objectType = objectType;
                return this;
            }

            public ValidationIssueBuilder objectId(String objectId) {
                this.objectId = objectId;
                return this;
            }

            public ValidationIssueBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }

            public ValidationIssueBuilder context(Map<String, Object> context) {
                this.context = context;
                return this;
            }

            public ValidationIssue build() {
                ValidationIssue issue = new ValidationIssue();
                issue.code = this.code;
                issue.message = this.message;
                issue.field = this.field;
                issue.objectType = this.objectType;
                issue.objectId = this.objectId;
                issue.severity = this.severity;
                issue.context = this.context;
                return issue;
            }
        }
    }

    /**
     * Inner class for conflicts
     */
    public static class Conflict {
        private String objectType;
        private String importId;
        private String importName;
        private String existingId;
        private String existingName;
        private ConflictType type;
        private List<String> resolutionOptions;

        public Conflict() {
            this.resolutionOptions = new ArrayList<>();
        }

        // Getters and setters
        public String getObjectType() { return objectType; }
        public void setObjectType(String objectType) { this.objectType = objectType; }
        public String getImportId() { return importId; }
        public void setImportId(String importId) { this.importId = importId; }
        public String getImportName() { return importName; }
        public void setImportName(String importName) { this.importName = importName; }
        public String getExistingId() { return existingId; }
        public void setExistingId(String existingId) { this.existingId = existingId; }
        public String getExistingName() { return existingName; }
        public void setExistingName(String existingName) { this.existingName = existingName; }
        public ConflictType getType() { return type; }
        public void setType(ConflictType type) { this.type = type; }
        public List<String> getResolutionOptions() { return resolutionOptions; }
        public void setResolutionOptions(List<String> resolutionOptions) { this.resolutionOptions = resolutionOptions; }

        // Builder
        public static ConflictBuilder builder() {
            return new ConflictBuilder();
        }

        public static class ConflictBuilder {
            private String objectType;
            private String importId;
            private String importName;
            private String existingId;
            private String existingName;
            private ConflictType type;
            private List<String> resolutionOptions = new ArrayList<>();

            public ConflictBuilder objectType(String objectType) {
                this.objectType = objectType;
                return this;
            }

            public ConflictBuilder importId(String importId) {
                this.importId = importId;
                return this;
            }

            public ConflictBuilder importName(String importName) {
                this.importName = importName;
                return this;
            }

            public ConflictBuilder existingId(String existingId) {
                this.existingId = existingId;
                return this;
            }

            public ConflictBuilder existingName(String existingName) {
                this.existingName = existingName;
                return this;
            }

            public ConflictBuilder type(ConflictType type) {
                this.type = type;
                return this;
            }

            public ConflictBuilder resolutionOptions(List<String> resolutionOptions) {
                this.resolutionOptions = resolutionOptions;
                return this;
            }

            public Conflict build() {
                Conflict conflict = new Conflict();
                conflict.objectType = this.objectType;
                conflict.importId = this.importId;
                conflict.importName = this.importName;
                conflict.existingId = this.existingId;
                conflict.existingName = this.existingName;
                conflict.type = this.type;
                conflict.resolutionOptions = this.resolutionOptions;
                return conflict;
            }
        }
    }

    /**
     * Inner class for version compatibility
     */
    public static class VersionCompatibility {
        private String exportVersion;
        private String currentVersion;
        private boolean isCompatible;
        private boolean requiresMigration;
        private List<String> migrationSteps;
        private List<String> breakingChanges;

        public VersionCompatibility() {
            this.migrationSteps = new ArrayList<>();
            this.breakingChanges = new ArrayList<>();
        }

        // Getters and setters
        public String getExportVersion() { return exportVersion; }
        public void setExportVersion(String exportVersion) { this.exportVersion = exportVersion; }
        public String getCurrentVersion() { return currentVersion; }
        public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }
        public boolean isCompatible() { return isCompatible; }
        public void setCompatible(boolean isCompatible) { this.isCompatible = isCompatible; }
        public boolean isRequiresMigration() { return requiresMigration; }
        public void setRequiresMigration(boolean requiresMigration) { this.requiresMigration = requiresMigration; }
        public List<String> getMigrationSteps() { return migrationSteps; }
        public void setMigrationSteps(List<String> migrationSteps) { this.migrationSteps = migrationSteps; }
        public List<String> getBreakingChanges() { return breakingChanges; }
        public void setBreakingChanges(List<String> breakingChanges) { this.breakingChanges = breakingChanges; }

        // Builder
        public static VersionCompatibilityBuilder builder() {
            return new VersionCompatibilityBuilder();
        }

        public static class VersionCompatibilityBuilder {
            private String exportVersion;
            private String currentVersion;
            private boolean isCompatible;
            private boolean requiresMigration;
            private List<String> migrationSteps = new ArrayList<>();
            private List<String> breakingChanges = new ArrayList<>();

            public VersionCompatibilityBuilder exportVersion(String exportVersion) {
                this.exportVersion = exportVersion;
                return this;
            }

            public VersionCompatibilityBuilder currentVersion(String currentVersion) {
                this.currentVersion = currentVersion;
                return this;
            }

            public VersionCompatibilityBuilder isCompatible(boolean isCompatible) {
                this.isCompatible = isCompatible;
                return this;
            }

            public VersionCompatibilityBuilder requiresMigration(boolean requiresMigration) {
                this.requiresMigration = requiresMigration;
                return this;
            }

            public VersionCompatibilityBuilder migrationSteps(List<String> migrationSteps) {
                this.migrationSteps = migrationSteps;
                return this;
            }

            public VersionCompatibilityBuilder breakingChanges(List<String> breakingChanges) {
                this.breakingChanges = breakingChanges;
                return this;
            }

            public VersionCompatibility build() {
                VersionCompatibility compatibility = new VersionCompatibility();
                compatibility.exportVersion = this.exportVersion;
                compatibility.currentVersion = this.currentVersion;
                compatibility.isCompatible = this.isCompatible;
                compatibility.requiresMigration = this.requiresMigration;
                compatibility.migrationSteps = this.migrationSteps;
                compatibility.breakingChanges = this.breakingChanges;
                return compatibility;
            }
        }
    }

    /**
     * Enum for severity levels
     */
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    /**
     * Enum for conflict types
     */
    public enum ConflictType {
        NAME_CONFLICT,
        ID_CONFLICT,
        VERSION_CONFLICT,
        DEPENDENCY_CONFLICT
    }
}