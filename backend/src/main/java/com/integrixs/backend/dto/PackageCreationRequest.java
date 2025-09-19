package com.integrixs.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request for creating a complete integration package
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageCreationRequest {

    @NotBlank(message = "Flow name is required")
    private String flowName;

    private String description;

    @NotNull(message = "Flow type is required")
    private String flowType;

    private UUID tenantId;

    private UUID userId;

    private Map<String, Object> flowConfiguration;

    private AdapterRequest sourceAdapter;

    private List<AdapterRequest> targetAdapters = new ArrayList<>();

    private StructureRequest sourceStructure;

    private List<StructureRequest> targetStructures = new ArrayList<>();

    private List<TransformationRequest> transformations = new ArrayList<>();

    private List<OrchestrationTargetRequest> orchestrationTargets = new ArrayList<>();

    private boolean deployToEngine = false;

    private boolean activateImmediately = false;

    private Map<String, Object> metadata;

    /**
     * Adapter configuration request
     */
    public static class AdapterRequest {
        @NotBlank(message = "Adapter name is required")
        private String name;

        @NotBlank(message = "Adapter type is required")
        private String type;

        private Map<String, Object> configuration;

        private Map<String, Object> credentials;

        private Map<String, Object> connectionSettings;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
        public Map<String, Object> getCredentials() { return credentials; }
        public void setCredentials(Map<String, Object> credentials) { this.credentials = credentials; }
        public Map<String, Object> getConnectionSettings() { return connectionSettings; }
        public void setConnectionSettings(Map<String, Object> connectionSettings) { this.connectionSettings = connectionSettings; }
    }

    /**
     * Structure configuration request
     */
    public static class StructureRequest {
        @NotBlank(message = "Structure name is required")
        private String name;

        @NotBlank(message = "Structure type is required")
        private String type;

        @NotBlank(message = "Structure format is required")
        private String format;

        private String content;

        private String schemaLocation;

        private Map<String, Object> metadata;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSchemaLocation() { return schemaLocation; }
        public void setSchemaLocation(String schemaLocation) { this.schemaLocation = schemaLocation; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    /**
     * Transformation configuration request
     */
    public static class TransformationRequest {
        @NotBlank(message = "Transformation name is required")
        private String name;

        @NotBlank(message = "Transformation type is required")
        private String type;

        private Map<String, Object> configuration;

        private List<FieldMappingRequest> fieldMappings = new ArrayList<>();

        private String script;

        private String template;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Map<String, Object> getConfiguration() { return configuration; }
        public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
        public List<FieldMappingRequest> getFieldMappings() { return fieldMappings; }
        public void setFieldMappings(List<FieldMappingRequest> fieldMappings) { this.fieldMappings = fieldMappings; }
        public String getScript() { return script; }
        public void setScript(String script) { this.script = script; }
        public String getTemplate() { return template; }
        public void setTemplate(String template) { this.template = template; }
    }

    /**
     * Field mapping request
     */
    public static class FieldMappingRequest {
        @NotBlank(message = "Source path is required")
        private String sourcePath;

        @NotBlank(message = "Target path is required")
        private String targetPath;

        private String expression;

        private String type = "DIRECT";

        private boolean required = false;

        private int order = 0;

        private Map<String, Object> transformationOptions;

        // Getters and setters
        public String getSourcePath() { return sourcePath; }
        public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
        public String getTargetPath() { return targetPath; }
        public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        public Map<String, Object> getTransformationOptions() { return transformationOptions; }
        public void setTransformationOptions(Map<String, Object> transformationOptions) { this.transformationOptions = transformationOptions; }
    }

    /**
     * Orchestration target request
     */
    public static class OrchestrationTargetRequest {
        @NotBlank(message = "Target name is required")
        private String name;

        @NotBlank(message = "Adapter name is required")
        private String adapterName;

        private String routingCondition;

        private Map<String, Object> transformationConfig;

        private int order = 0;

        private boolean parallel = false;

        private String errorStrategy = "FAIL_FAST";

        private Map<String, Object> retryConfig;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAdapterName() { return adapterName; }
        public void setAdapterName(String adapterName) { this.adapterName = adapterName; }
        public String getRoutingCondition() { return routingCondition; }
        public void setRoutingCondition(String routingCondition) { this.routingCondition = routingCondition; }
        public Map<String, Object> getTransformationConfig() { return transformationConfig; }
        public void setTransformationConfig(Map<String, Object> transformationConfig) { this.transformationConfig = transformationConfig; }
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        public boolean isParallel() { return parallel; }
        public void setParallel(boolean parallel) { this.parallel = parallel; }
        public String getErrorStrategy() { return errorStrategy; }
        public void setErrorStrategy(String errorStrategy) { this.errorStrategy = errorStrategy; }
        public Map<String, Object> getRetryConfig() { return retryConfig; }
        public void setRetryConfig(Map<String, Object> retryConfig) { this.retryConfig = retryConfig; }
    }

    // Main class getters and setters
    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFlowType() { return flowType; }
    public void setFlowType(String flowType) { this.flowType = flowType; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Map<String, Object> getFlowConfiguration() { return flowConfiguration; }
    public void setFlowConfiguration(Map<String, Object> flowConfiguration) { this.flowConfiguration = flowConfiguration; }
    public AdapterRequest getSourceAdapter() { return sourceAdapter; }
    public void setSourceAdapter(AdapterRequest sourceAdapter) { this.sourceAdapter = sourceAdapter; }
    public List<AdapterRequest> getTargetAdapters() { return targetAdapters; }
    public void setTargetAdapters(List<AdapterRequest> targetAdapters) { this.targetAdapters = targetAdapters; }
    public StructureRequest getSourceStructure() { return sourceStructure; }
    public void setSourceStructure(StructureRequest sourceStructure) { this.sourceStructure = sourceStructure; }
    public List<StructureRequest> getTargetStructures() { return targetStructures; }
    public void setTargetStructures(List<StructureRequest> targetStructures) { this.targetStructures = targetStructures; }
    public List<TransformationRequest> getTransformations() { return transformations; }
    public void setTransformations(List<TransformationRequest> transformations) { this.transformations = transformations; }
    public List<OrchestrationTargetRequest> getOrchestrationTargets() { return orchestrationTargets; }
    public void setOrchestrationTargets(List<OrchestrationTargetRequest> orchestrationTargets) { this.orchestrationTargets = orchestrationTargets; }
    public boolean isDeployToEngine() { return deployToEngine; }
    public void setDeployToEngine(boolean deployToEngine) { this.deployToEngine = deployToEngine; }
    public boolean isActivateImmediately() { return activateImmediately; }
    public void setActivateImmediately(boolean activateImmediately) { this.activateImmediately = activateImmediately; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
