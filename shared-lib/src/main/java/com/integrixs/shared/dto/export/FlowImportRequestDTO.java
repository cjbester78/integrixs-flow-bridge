package com.integrixs.shared.dto.export;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO for importing an integration flow with configuration options.
 */
public class FlowImportRequestDTO {

    private FlowExportDTO flowExport;
    private ImportOptions options;
    private Map<String, String> idMappings;
    private ConflictStrategy conflictStrategy = ConflictStrategy.FAIL;
    private boolean importBusinessComponent = true;
    private boolean importAdapters = true;
    private boolean importCertificateReferences = true;
    private boolean validateReferences = true;
    private boolean activateAfterImport = false;
    private String namePrefix;
    private String nameSuffix;
    private String targetBusinessComponentId;
    private Map<String, Object> configOverrides;

    // Default constructor
    public FlowImportRequestDTO() {
        this.idMappings = new HashMap<>();
        this.configOverrides = new HashMap<>();
    }

    // All args constructor
    public FlowImportRequestDTO(FlowExportDTO flowExport, ImportOptions options, Map<String, String> idMappings, ConflictStrategy conflictStrategy, boolean importBusinessComponent, boolean importAdapters, boolean importCertificateReferences, boolean validateReferences, boolean activateAfterImport, String namePrefix, String nameSuffix, String targetBusinessComponentId, Map<String, Object> configOverrides) {
        this.flowExport = flowExport;
        this.options = options;
        this.idMappings = idMappings != null ? idMappings : new HashMap<>();
        this.conflictStrategy = conflictStrategy;
        this.importBusinessComponent = importBusinessComponent;
        this.importAdapters = importAdapters;
        this.importCertificateReferences = importCertificateReferences;
        this.validateReferences = validateReferences;
        this.activateAfterImport = activateAfterImport;
        this.namePrefix = namePrefix;
        this.nameSuffix = nameSuffix;
        this.targetBusinessComponentId = targetBusinessComponentId;
        this.configOverrides = configOverrides != null ? configOverrides : new HashMap<>();
    }

    // Getters
    public FlowExportDTO getFlowExport() { return flowExport; }
    public ImportOptions getOptions() { return options; }
    public Map<String, String> getIdMappings() { return idMappings; }
    public ConflictStrategy getConflictStrategy() { return conflictStrategy; }
    public boolean isImportBusinessComponent() { return importBusinessComponent; }
    public boolean isImportAdapters() { return importAdapters; }
    public boolean isImportCertificateReferences() { return importCertificateReferences; }
    public boolean isValidateReferences() { return validateReferences; }
    public boolean isActivateAfterImport() { return activateAfterImport; }
    public String getNamePrefix() { return namePrefix; }
    public String getNameSuffix() { return nameSuffix; }
    public String getTargetBusinessComponentId() { return targetBusinessComponentId; }
    public Map<String, Object> getConfigOverrides() { return configOverrides; }

    // Setters
    public void setFlowExport(FlowExportDTO flowExport) { this.flowExport = flowExport; }
    public void setOptions(ImportOptions options) { this.options = options; }
    public void setIdMappings(Map<String, String> idMappings) { this.idMappings = idMappings; }
    public void setConflictStrategy(ConflictStrategy conflictStrategy) { this.conflictStrategy = conflictStrategy; }
    public void setImportBusinessComponent(boolean importBusinessComponent) { this.importBusinessComponent = importBusinessComponent; }
    public void setImportAdapters(boolean importAdapters) { this.importAdapters = importAdapters; }
    public void setImportCertificateReferences(boolean importCertificateReferences) { this.importCertificateReferences = importCertificateReferences; }
    public void setValidateReferences(boolean validateReferences) { this.validateReferences = validateReferences; }
    public void setActivateAfterImport(boolean activateAfterImport) { this.activateAfterImport = activateAfterImport; }
    public void setNamePrefix(String namePrefix) { this.namePrefix = namePrefix; }
    public void setNameSuffix(String nameSuffix) { this.nameSuffix = nameSuffix; }
    public void setTargetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; }
    public void setConfigOverrides(Map<String, Object> configOverrides) { this.configOverrides = configOverrides; }

    // Builder
    public static FlowImportRequestDTOBuilder builder() {
        return new FlowImportRequestDTOBuilder();
    }

    public static class FlowImportRequestDTOBuilder {
        private FlowExportDTO flowExport;
        private ImportOptions options;
        private Map<String, String> idMappings = new HashMap<>();
        private ConflictStrategy conflictStrategy = ConflictStrategy.FAIL;
        private boolean importBusinessComponent = true;
        private boolean importAdapters = true;
        private boolean importCertificateReferences = true;
        private boolean validateReferences = true;
        private boolean activateAfterImport = false;
        private String namePrefix;
        private String nameSuffix;
        private String targetBusinessComponentId;
        private Map<String, Object> configOverrides = new HashMap<>();

        public FlowImportRequestDTOBuilder flowExport(FlowExportDTO flowExport) {
            this.flowExport = flowExport;
            return this;
        }

        public FlowImportRequestDTOBuilder options(ImportOptions options) {
            this.options = options;
            return this;
        }

        public FlowImportRequestDTOBuilder idMappings(Map<String, String> idMappings) {
            this.idMappings = idMappings;
            return this;
        }

        public FlowImportRequestDTOBuilder conflictStrategy(ConflictStrategy conflictStrategy) {
            this.conflictStrategy = conflictStrategy;
            return this;
        }

        public FlowImportRequestDTOBuilder importBusinessComponent(boolean importBusinessComponent) {
            this.importBusinessComponent = importBusinessComponent;
            return this;
        }

        public FlowImportRequestDTOBuilder importAdapters(boolean importAdapters) {
            this.importAdapters = importAdapters;
            return this;
        }

        public FlowImportRequestDTOBuilder importCertificateReferences(boolean importCertificateReferences) {
            this.importCertificateReferences = importCertificateReferences;
            return this;
        }

        public FlowImportRequestDTOBuilder validateReferences(boolean validateReferences) {
            this.validateReferences = validateReferences;
            return this;
        }

        public FlowImportRequestDTOBuilder activateAfterImport(boolean activateAfterImport) {
            this.activateAfterImport = activateAfterImport;
            return this;
        }

        public FlowImportRequestDTOBuilder namePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
            return this;
        }

        public FlowImportRequestDTOBuilder nameSuffix(String nameSuffix) {
            this.nameSuffix = nameSuffix;
            return this;
        }

        public FlowImportRequestDTOBuilder targetBusinessComponentId(String targetBusinessComponentId) {
            this.targetBusinessComponentId = targetBusinessComponentId;
            return this;
        }

        public FlowImportRequestDTOBuilder configOverrides(Map<String, Object> configOverrides) {
            this.configOverrides = configOverrides;
            return this;
        }

        public FlowImportRequestDTO build() {
            return new FlowImportRequestDTO(flowExport, options, idMappings, conflictStrategy, importBusinessComponent, importAdapters, importCertificateReferences, validateReferences, activateAfterImport, namePrefix, nameSuffix, targetBusinessComponentId, configOverrides);
        }
    }

    /**
     * Import options
     */
    public static class ImportOptions {
        private boolean importBusinessComponent = true;
        private boolean importAdapters = true;
        private boolean importCertificateReferences = true;
        private boolean validateReferences = true;
        private boolean activateAfterImport = false;
        private ConflictStrategy conflictStrategy = ConflictStrategy.FAIL;
        private String targetBusinessComponentId;
        private String namePrefix;
        private String nameSuffix;

        // Default constructor
        public ImportOptions() {
        }

        // Getters
        public boolean isImportBusinessComponent() { return importBusinessComponent; }
        public boolean isImportAdapters() { return importAdapters; }
        public boolean isImportCertificateReferences() { return importCertificateReferences; }
        public boolean isValidateReferences() { return validateReferences; }
        public boolean isActivateAfterImport() { return activateAfterImport; }
        public ConflictStrategy getConflictStrategy() { return conflictStrategy; }
        public String getTargetBusinessComponentId() { return targetBusinessComponentId; }
        public String getNamePrefix() { return namePrefix; }
        public String getNameSuffix() { return nameSuffix; }

        // Setters
        public void setImportBusinessComponent(boolean importBusinessComponent) { this.importBusinessComponent = importBusinessComponent; }
        public void setImportAdapters(boolean importAdapters) { this.importAdapters = importAdapters; }
        public void setImportCertificateReferences(boolean importCertificateReferences) { this.importCertificateReferences = importCertificateReferences; }
        public void setValidateReferences(boolean validateReferences) { this.validateReferences = validateReferences; }
        public void setActivateAfterImport(boolean activateAfterImport) { this.activateAfterImport = activateAfterImport; }
        public void setConflictStrategy(ConflictStrategy conflictStrategy) { this.conflictStrategy = conflictStrategy; }
        public void setTargetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; }
        public void setNamePrefix(String namePrefix) { this.namePrefix = namePrefix; }
        public void setNameSuffix(String nameSuffix) { this.nameSuffix = nameSuffix; }

        // Builder
        public static ImportOptionsBuilder builder() {
            return new ImportOptionsBuilder();
        }

        public static class ImportOptionsBuilder {
            private boolean importBusinessComponent = true;
            private boolean importAdapters = true;
            private boolean importCertificateReferences = true;
            private boolean validateReferences = true;
            private boolean activateAfterImport = false;
            private ConflictStrategy conflictStrategy = ConflictStrategy.FAIL;
            private String targetBusinessComponentId;
            private String namePrefix;
            private String nameSuffix;

            public ImportOptionsBuilder importBusinessComponent(boolean importBusinessComponent) {
                this.importBusinessComponent = importBusinessComponent;
                return this;
            }

            public ImportOptionsBuilder importAdapters(boolean importAdapters) {
                this.importAdapters = importAdapters;
                return this;
            }

            public ImportOptionsBuilder importCertificateReferences(boolean importCertificateReferences) {
                this.importCertificateReferences = importCertificateReferences;
                return this;
            }

            public ImportOptionsBuilder validateReferences(boolean validateReferences) {
                this.validateReferences = validateReferences;
                return this;
            }

            public ImportOptionsBuilder activateAfterImport(boolean activateAfterImport) {
                this.activateAfterImport = activateAfterImport;
                return this;
            }

            public ImportOptionsBuilder conflictStrategy(ConflictStrategy conflictStrategy) {
                this.conflictStrategy = conflictStrategy;
                return this;
            }

            public ImportOptionsBuilder targetBusinessComponentId(String targetBusinessComponentId) {
                this.targetBusinessComponentId = targetBusinessComponentId;
                return this;
            }

            public ImportOptionsBuilder namePrefix(String namePrefix) {
                this.namePrefix = namePrefix;
                return this;
            }

            public ImportOptionsBuilder nameSuffix(String nameSuffix) {
                this.nameSuffix = nameSuffix;
                return this;
            }

            public ImportOptions build() {
                ImportOptions options = new ImportOptions();
                options.importBusinessComponent = this.importBusinessComponent;
                options.importAdapters = this.importAdapters;
                options.importCertificateReferences = this.importCertificateReferences;
                options.validateReferences = this.validateReferences;
                options.activateAfterImport = this.activateAfterImport;
                options.conflictStrategy = this.conflictStrategy;
                options.targetBusinessComponentId = this.targetBusinessComponentId;
                options.namePrefix = this.namePrefix;
                options.nameSuffix = this.nameSuffix;
                return options;
            }
        }
    }

    /**
     * Conflict resolution strategy
     */
    public enum ConflictStrategy {
        FAIL,        // Fail on any conflict
        SKIP,        // Skip conflicting items
        OVERWRITE,   // Overwrite existing items
        RENAME,      // Rename conflicting items
        CREATE_NEW   // Create new item with new ID
    }
}
