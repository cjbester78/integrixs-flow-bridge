package com.integrixs.shared.dto;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Request DTO for testing field mappings
 */
public class TestFieldMappingsRequestDTO {

    private String inputXml;
    private List<FieldMappingDTO> mappings;
    private String mappingType;
    private String sourceStructureXml;
    private String targetStructureXml;
    private TestMappingDTO testMapping;
    private VisualFlowData visualFlowData;

    // Default constructor
    public TestFieldMappingsRequestDTO() {
        this.mappings = new ArrayList<>();
    }

    // All args constructor
    public TestFieldMappingsRequestDTO(String inputXml, List<FieldMappingDTO> mappings, String mappingType, String sourceStructureXml, String targetStructureXml) {
        this.inputXml = inputXml;
        this.mappings = mappings != null ? mappings : new ArrayList<>();
        this.mappingType = mappingType;
        this.sourceStructureXml = sourceStructureXml;
        this.targetStructureXml = targetStructureXml;
    }

    // Getters
    public String getInputXml() { return inputXml; }
    public List<FieldMappingDTO> getMappings() { return mappings; }
    public String getMappingType() { return mappingType; }
    public String getSourceStructureXml() { return sourceStructureXml; }
    public String getTargetStructureXml() { return targetStructureXml; }
    public TestMappingDTO getTestMapping() { return testMapping; }
    public VisualFlowData getVisualFlowData() { return visualFlowData; }

    // Setters
    public void setInputXml(String inputXml) { this.inputXml = inputXml; }
    public void setMappings(List<FieldMappingDTO> mappings) { this.mappings = mappings; }
    public void setMappingType(String mappingType) { this.mappingType = mappingType; }
    public void setSourceStructureXml(String sourceStructureXml) { this.sourceStructureXml = sourceStructureXml; }
    public void setTargetStructureXml(String targetStructureXml) { this.targetStructureXml = targetStructureXml; }
    public void setTestMapping(TestMappingDTO testMapping) { this.testMapping = testMapping; }
    public void setVisualFlowData(VisualFlowData visualFlowData) { this.visualFlowData = visualFlowData; }

    // Builder
    public static TestFieldMappingsRequestDTOBuilder builder() {
        return new TestFieldMappingsRequestDTOBuilder();
    }

    public static class TestFieldMappingsRequestDTOBuilder {
        private String inputXml;
        private List<FieldMappingDTO> mappings = new ArrayList<>();
        private String mappingType;
        private String sourceStructureXml;
        private String targetStructureXml;

        public TestFieldMappingsRequestDTOBuilder inputXml(String inputXml) {
            this.inputXml = inputXml;
            return this;
        }

        public TestFieldMappingsRequestDTOBuilder mappings(List<FieldMappingDTO> mappings) {
            this.mappings = mappings;
            return this;
        }

        public TestFieldMappingsRequestDTOBuilder mappingType(String mappingType) {
            this.mappingType = mappingType;
            return this;
        }

        public TestFieldMappingsRequestDTOBuilder sourceStructureXml(String sourceStructureXml) {
            this.sourceStructureXml = sourceStructureXml;
            return this;
        }

        public TestFieldMappingsRequestDTOBuilder targetStructureXml(String targetStructureXml) {
            this.targetStructureXml = targetStructureXml;
            return this;
        }

        public TestFieldMappingsRequestDTO build() {
            return new TestFieldMappingsRequestDTO(inputXml, mappings, mappingType, sourceStructureXml, targetStructureXml);
        }
    }

    // Inner class for test mapping configuration
    public static class TestMappingDTO {
        private String id;
        private String name;
        private String description;
        private String configuration;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getConfiguration() { return configuration; }
        public void setConfiguration(String configuration) { this.configuration = configuration; }
    }

    // Inner class for visual flow data
    public static class VisualFlowData {
        private String flowId;
        private String flowName;
        private String flowData;
        private String nodeData;
        private List<VisualFlowNode> nodes;
        private List<VisualFlowEdge> edges;

        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }

        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }

        public String getFlowData() { return flowData; }
        public void setFlowData(String flowData) { this.flowData = flowData; }

        public String getNodeData() { return nodeData; }
        public void setNodeData(String nodeData) { this.nodeData = nodeData; }

        public List<VisualFlowNode> getNodes() { return nodes; }
        public void setNodes(List<VisualFlowNode> nodes) { this.nodes = nodes; }

        public List<VisualFlowEdge> getEdges() { return edges; }
        public void setEdges(List<VisualFlowEdge> edges) { this.edges = edges; }
    }

    // Inner class for visual flow node
    public static class VisualFlowNode {
        private String id;
        private String type;
        private String label;
        private Map<String, Object> data;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    // Inner class for visual flow edge
    public static class VisualFlowEdge {
        private String id;
        private String source;
        private String target;
        private String label;
        private String targetHandle;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getTargetHandle() { return targetHandle; }
        public void setTargetHandle(String targetHandle) { this.targetHandle = targetHandle; }
    }
}