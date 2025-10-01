package com.integrixs.shared.dto.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO representing the result of a flow import operation.
 */
public class FlowImportResultDTO {

    private boolean success;
    private String importedFlowId;
    private String importedFlowName;
    private ImportSummary summary;
    private Map<String, String> idMappings;
    private List<ImportMessage> warnings;
    private List<ImportMessage> errors;
    private List<ConflictResolution> conflictResolutions;

    // Default constructor
    public FlowImportResultDTO() {
        this.summary = new ImportSummary();
        this.idMappings = new HashMap<>();
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.conflictResolutions = new ArrayList<>();
    }

    // All args constructor
    public FlowImportResultDTO(boolean success, String importedFlowId, String importedFlowName, ImportSummary summary, Map<String, String> idMappings, List<ImportMessage> warnings, List<ImportMessage> errors, List<ConflictResolution> conflictResolutions) {
        this.success = success;
        this.importedFlowId = importedFlowId;
        this.importedFlowName = importedFlowName;
        this.summary = summary != null ? summary : new ImportSummary();
        this.idMappings = idMappings != null ? idMappings : new HashMap<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.errors = errors != null ? errors : new ArrayList<>();
        this.conflictResolutions = conflictResolutions != null ? conflictResolutions : new ArrayList<>();
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getImportedFlowId() { return importedFlowId; }
    public String getImportedFlowName() { return importedFlowName; }
    public ImportSummary getSummary() { return summary; }
    public Map<String, String> getIdMappings() { return idMappings; }
    public List<ImportMessage> getWarnings() { return warnings; }
    public List<ImportMessage> getErrors() { return errors; }
    public List<ConflictResolution> getConflictResolutions() { return conflictResolutions; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setImportedFlowId(String importedFlowId) { this.importedFlowId = importedFlowId; }
    public void setImportedFlowName(String importedFlowName) { this.importedFlowName = importedFlowName; }
    public void setSummary(ImportSummary summary) { this.summary = summary; }
    public void setIdMappings(Map<String, String> idMappings) { this.idMappings = idMappings; }
    public void setWarnings(List<ImportMessage> warnings) { this.warnings = warnings; }
    public void setErrors(List<ImportMessage> errors) { this.errors = errors; }
    public void setConflictResolutions(List<ConflictResolution> conflictResolutions) { this.conflictResolutions = conflictResolutions; }

    // Builder
    public static FlowImportResultDTOBuilder builder() {
        return new FlowImportResultDTOBuilder();
    }

    public static class FlowImportResultDTOBuilder {
        private boolean success;
        private String importedFlowId;
        private String importedFlowName;
        private ImportSummary summary = new ImportSummary();
        private Map<String, String> idMappings = new HashMap<>();
        private List<ImportMessage> warnings = new ArrayList<>();
        private List<ImportMessage> errors = new ArrayList<>();
        private List<ConflictResolution> conflictResolutions = new ArrayList<>();

        public FlowImportResultDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public FlowImportResultDTOBuilder importedFlowId(String importedFlowId) {
            this.importedFlowId = importedFlowId;
            return this;
        }

        public FlowImportResultDTOBuilder importedFlowName(String importedFlowName) {
            this.importedFlowName = importedFlowName;
            return this;
        }

        public FlowImportResultDTOBuilder summary(ImportSummary summary) {
            this.summary = summary;
            return this;
        }

        public FlowImportResultDTOBuilder idMappings(Map<String, String> idMappings) {
            this.idMappings = idMappings;
            return this;
        }

        public FlowImportResultDTOBuilder warnings(List<ImportMessage> warnings) {
            this.warnings = warnings;
            return this;
        }

        public FlowImportResultDTOBuilder errors(List<ImportMessage> errors) {
            this.errors = errors;
            return this;
        }

        public FlowImportResultDTOBuilder conflictResolutions(List<ConflictResolution> conflictResolutions) {
            this.conflictResolutions = conflictResolutions;
            return this;
        }

        public FlowImportResultDTO build() {
            return new FlowImportResultDTO(success, importedFlowId, importedFlowName, summary, idMappings, warnings, errors, conflictResolutions);
        }
    }

    /**
     * Inner class representing the import summary
     */
    public static class ImportSummary {
        private boolean flowImported;
        private boolean businessComponentImported;
        private int adaptersImported;
        private int transformationsImported;
        private int fieldMappingsImported;
        private int certificateReferencesCreated;
        private int totalObjectsImported;
        private long importDurationMs;

        // Default constructor
        public ImportSummary() {
        }

        // All args constructor
        public ImportSummary(boolean flowImported, boolean businessComponentImported, int adaptersImported, int transformationsImported, int fieldMappingsImported, int certificateReferencesCreated, int totalObjectsImported, long importDurationMs) {
            this.flowImported = flowImported;
            this.businessComponentImported = businessComponentImported;
            this.adaptersImported = adaptersImported;
            this.transformationsImported = transformationsImported;
            this.fieldMappingsImported = fieldMappingsImported;
            this.certificateReferencesCreated = certificateReferencesCreated;
            this.totalObjectsImported = totalObjectsImported;
            this.importDurationMs = importDurationMs;
        }

        // Getters
        public boolean isFlowImported() { return flowImported; }
        public boolean isBusinessComponentImported() { return businessComponentImported; }
        public int getAdaptersImported() { return adaptersImported; }
        public int getTransformationsImported() { return transformationsImported; }
        public int getFieldMappingsImported() { return fieldMappingsImported; }
        public int getCertificateReferencesCreated() { return certificateReferencesCreated; }
        public int getTotalObjectsImported() { return totalObjectsImported; }
        public long getImportDurationMs() { return importDurationMs; }

        // Setters
        public void setFlowImported(boolean flowImported) { this.flowImported = flowImported; }
        public void setBusinessComponentImported(boolean businessComponentImported) { this.businessComponentImported = businessComponentImported; }
        public void setAdaptersImported(int adaptersImported) { this.adaptersImported = adaptersImported; }
        public void setTransformationsImported(int transformationsImported) { this.transformationsImported = transformationsImported; }
        public void setFieldMappingsImported(int fieldMappingsImported) { this.fieldMappingsImported = fieldMappingsImported; }
        public void setCertificateReferencesCreated(int certificateReferencesCreated) { this.certificateReferencesCreated = certificateReferencesCreated; }
        public void setTotalObjectsImported(int totalObjectsImported) { this.totalObjectsImported = totalObjectsImported; }
        public void setImportDurationMs(long importDurationMs) { this.importDurationMs = importDurationMs; }
    }

    /**
     * Inner class representing import messages
     */
    public static class ImportMessage {
        private String code;
        private String message;
        private String objectType;
        private String objectId;
        private String objectName;
        private Map<String, Object> details;

        // Default constructor
        public ImportMessage() {
            this.details = new HashMap<>();
        }

        // All args constructor
        public ImportMessage(String code, String message, String objectType, String objectId, String objectName, Map<String, Object> details) {
            this.code = code;
            this.message = message;
            this.objectType = objectType;
            this.objectId = objectId;
            this.objectName = objectName;
            this.details = details != null ? details : new HashMap<>();
        }

        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getObjectType() { return objectType; }
        public String getObjectId() { return objectId; }
        public String getObjectName() { return objectName; }
        public Map<String, Object> getDetails() { return details; }

        // Setters
        public void setCode(String code) { this.code = code; }
        public void setMessage(String message) { this.message = message; }
        public void setObjectType(String objectType) { this.objectType = objectType; }
        public void setObjectId(String objectId) { this.objectId = objectId; }
        public void setObjectName(String objectName) { this.objectName = objectName; }
        public void setDetails(Map<String, Object> details) { this.details = details; }

        // Builder
        public static ImportMessageBuilder builder() {
            return new ImportMessageBuilder();
        }

        public static class ImportMessageBuilder {
            private String code;
            private String message;
            private String objectType;
            private String objectId;
            private String objectName;
            private Map<String, Object> details = new HashMap<>();

            public ImportMessageBuilder code(String code) {
                this.code = code;
                return this;
            }

            public ImportMessageBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ImportMessageBuilder objectType(String objectType) {
                this.objectType = objectType;
                return this;
            }

            public ImportMessageBuilder objectId(String objectId) {
                this.objectId = objectId;
                return this;
            }

            public ImportMessageBuilder objectName(String objectName) {
                this.objectName = objectName;
                return this;
            }

            public ImportMessageBuilder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public ImportMessage build() {
                return new ImportMessage(code, message, objectType, objectId, objectName, details);
            }
        }
    }

    /**
     * Inner class representing conflict resolutions
     */
    public static class ConflictResolution {
        private String objectType;
        private String originalId;
        private String originalName;
        private String newId;
        private String newName;
        private String resolution;
        private String reason;

        // Default constructor
        public ConflictResolution() {
        }

        // All args constructor
        public ConflictResolution(String objectType, String originalId, String originalName, String newId, String newName, String resolution, String reason) {
            this.objectType = objectType;
            this.originalId = originalId;
            this.originalName = originalName;
            this.newId = newId;
            this.newName = newName;
            this.resolution = resolution;
            this.reason = reason;
        }

        // Getters
        public String getObjectType() { return objectType; }
        public String getOriginalId() { return originalId; }
        public String getOriginalName() { return originalName; }
        public String getNewId() { return newId; }
        public String getNewName() { return newName; }
        public String getResolution() { return resolution; }
        public String getReason() { return reason; }

        // Setters
        public void setObjectType(String objectType) { this.objectType = objectType; }
        public void setOriginalId(String originalId) { this.originalId = originalId; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        public void setNewId(String newId) { this.newId = newId; }
        public void setNewName(String newName) { this.newName = newName; }
        public void setResolution(String resolution) { this.resolution = resolution; }
        public void setReason(String reason) { this.reason = reason; }

        // Builder
        public static ConflictResolutionBuilder builder() {
            return new ConflictResolutionBuilder();
        }

        public static class ConflictResolutionBuilder {
            private String objectType;
            private String originalId;
            private String originalName;
            private String newId;
            private String newName;
            private String resolution;
            private String reason;

            public ConflictResolutionBuilder objectType(String objectType) {
                this.objectType = objectType;
                return this;
            }

            public ConflictResolutionBuilder originalId(String originalId) {
                this.originalId = originalId;
                return this;
            }

            public ConflictResolutionBuilder originalName(String originalName) {
                this.originalName = originalName;
                return this;
            }

            public ConflictResolutionBuilder newId(String newId) {
                this.newId = newId;
                return this;
            }

            public ConflictResolutionBuilder newName(String newName) {
                this.newName = newName;
                return this;
            }

            public ConflictResolutionBuilder resolution(String resolution) {
                this.resolution = resolution;
                return this;
            }

            public ConflictResolutionBuilder reason(String reason) {
                this.reason = reason;
                return this;
            }

            public ConflictResolution build() {
                return new ConflictResolution(objectType, originalId, originalName, newId, newName, resolution, reason);
            }
        }
    }
}