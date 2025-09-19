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