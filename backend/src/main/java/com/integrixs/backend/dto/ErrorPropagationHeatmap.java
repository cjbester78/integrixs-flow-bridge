package com.integrixs.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO representing error propagation heatmap data
 */
public class ErrorPropagationHeatmap {
    private LocalDateTime analysisStart;
    private LocalDateTime analysisEnd;
    private List<ErrorNode> errorNodes;
    private List<ErrorLink> errorLinks;
    private Map<String, ErrorHotspot> hotspots;
    private Long totalErrors;
    private Map<String, Long> errorsByType;
    private Map<String, Double> propagationRates;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<List<String>> propagationPaths;
    private double[][] errorDensityGrid;
    private List<Map<String, Object>> errorHotspots;
    private Map<String, Double> componentErrorImpact;

    // Inner classes
    public static class ErrorNode {
        private String nodeId;
        private String nodeType; // ADAPTER, FLOW, TRANSFORMATION, etc.
        private String nodeName;
        private Long errorCount;
        private Double errorRate;
        private List<String> commonErrors;
        private Integer severity; // 1-5 scale
        private Map<String, Object> metadata;

        // Getters and setters
        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public Long getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Long errorCount) {
            this.errorCount = errorCount;
        }

        public Double getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(Double errorRate) {
            this.errorRate = errorRate;
        }

        public List<String> getCommonErrors() {
            return commonErrors;
        }

        public void setCommonErrors(List<String> commonErrors) {
            this.commonErrors = commonErrors;
        }

        public Integer getSeverity() {
            return severity;
        }

        public void setSeverity(Integer severity) {
            this.severity = severity;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    public static class ErrorLink {
        private String sourceId;
        private String targetId;
        private Long propagatedErrors;
        private Double propagationRate;
        private String linkType; // DIRECT, INDIRECT, CASCADE
        private List<String> errorTypes;

        // Getters and setters
        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public Long getPropagatedErrors() {
            return propagatedErrors;
        }

        public void setPropagatedErrors(Long propagatedErrors) {
            this.propagatedErrors = propagatedErrors;
        }

        public Double getPropagationRate() {
            return propagationRate;
        }

        public void setPropagationRate(Double propagationRate) {
            this.propagationRate = propagationRate;
        }

        public String getLinkType() {
            return linkType;
        }

        public void setLinkType(String linkType) {
            this.linkType = linkType;
        }

        public List<String> getErrorTypes() {
            return errorTypes;
        }

        public void setErrorTypes(List<String> errorTypes) {
            this.errorTypes = errorTypes;
        }
    }

    public static class ErrorHotspot {
        private String componentId;
        private String componentName;
        private String componentType;
        private Long errorCount;
        private Double errorRate;
        private List<String> affectedDownstream;
        private String primaryCause;
        private String recommendedAction;
        private Integer priority; // 1-5 scale

        // Getters and setters
        public String getComponentId() {
            return componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public String getComponentName() {
            return componentName;
        }

        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }

        public String getComponentType() {
            return componentType;
        }

        public void setComponentType(String componentType) {
            this.componentType = componentType;
        }

        public Long getErrorCount() {
            return errorCount;
        }

        public void setErrorCount(Long errorCount) {
            this.errorCount = errorCount;
        }

        public Double getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(Double errorRate) {
            this.errorRate = errorRate;
        }

        public List<String> getAffectedDownstream() {
            return affectedDownstream;
        }

        public void setAffectedDownstream(List<String> affectedDownstream) {
            this.affectedDownstream = affectedDownstream;
        }

        public String getPrimaryCause() {
            return primaryCause;
        }

        public void setPrimaryCause(String primaryCause) {
            this.primaryCause = primaryCause;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public void setRecommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }

    // Default constructor
    public ErrorPropagationHeatmap() {
    }

    // Main class getters and setters
    public LocalDateTime getAnalysisStart() {
        return analysisStart;
    }

    public void setAnalysisStart(LocalDateTime analysisStart) {
        this.analysisStart = analysisStart;
    }

    public LocalDateTime getAnalysisEnd() {
        return analysisEnd;
    }

    public void setAnalysisEnd(LocalDateTime analysisEnd) {
        this.analysisEnd = analysisEnd;
    }

    public List<ErrorNode> getErrorNodes() {
        return errorNodes;
    }

    public void setErrorNodes(List<ErrorNode> errorNodes) {
        this.errorNodes = errorNodes;
    }

    public List<ErrorLink> getErrorLinks() {
        return errorLinks;
    }

    public void setErrorLinks(List<ErrorLink> errorLinks) {
        this.errorLinks = errorLinks;
    }

    public Map<String, ErrorHotspot> getHotspots() {
        return hotspots;
    }

    public void setHotspots(Map<String, ErrorHotspot> hotspots) {
        this.hotspots = hotspots;
    }

    public Long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(Long totalErrors) {
        this.totalErrors = totalErrors;
    }

    public Map<String, Long> getErrorsByType() {
        return errorsByType;
    }

    public void setErrorsByType(Map<String, Long> errorsByType) {
        this.errorsByType = errorsByType;
    }

    public Map<String, Double> getPropagationRates() {
        return propagationRates;
    }

    public void setPropagationRates(Map<String, Double> propagationRates) {
        this.propagationRates = propagationRates;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<List<String>> getPropagationPaths() {
        return propagationPaths;
    }

    public void setPropagationPaths(List<List<String>> propagationPaths) {
        this.propagationPaths = propagationPaths;
    }

    public double[][] getErrorDensityGrid() {
        return errorDensityGrid;
    }

    public void setErrorDensityGrid(double[][] errorDensityGrid) {
        this.errorDensityGrid = errorDensityGrid;
    }

    public List<Map<String, Object>> getErrorHotspots() {
        return errorHotspots;
    }

    public void setErrorHotspots(List<Map<String, Object>> errorHotspots) {
        this.errorHotspots = errorHotspots;
    }

    public Map<String, Double> getComponentErrorImpact() {
        return componentErrorImpact;
    }

    public void setComponentErrorImpact(Map<String, Double> componentErrorImpact) {
        this.componentErrorImpact = componentErrorImpact;
    }
}