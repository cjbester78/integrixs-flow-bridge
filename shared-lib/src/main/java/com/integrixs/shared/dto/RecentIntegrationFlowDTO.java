package com.integrixs.shared.dto;

public class RecentIntegrationFlowDTO {

    private String id;
    private String source;
    private String target;
    private String status;
    private String time;
    private String businessComponentId;

    // Default constructor
    public RecentIntegrationFlowDTO() {
    }

    // All args constructor
    public RecentIntegrationFlowDTO(String id, String source, String target, String status, String time, String businessComponentId) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.status = status;
        this.time = time;
        this.businessComponentId = businessComponentId;
    }

    // Getters
    public String getId() { return id; }
    public String getSource() { return source; }
    public String getTarget() { return target; }
    public String getStatus() { return status; }
    public String getTime() { return time; }
    public String getBusinessComponentId() { return businessComponentId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSource(String source) { this.source = source; }
    public void setTarget(String target) { this.target = target; }
    public void setStatus(String status) { this.status = status; }
    public void setTime(String time) { this.time = time; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }

    // Builder
    public static RecentIntegrationFlowDTOBuilder builder() {
        return new RecentIntegrationFlowDTOBuilder();
    }

    public static class RecentIntegrationFlowDTOBuilder {
        private String id;
        private String source;
        private String target;
        private String status;
        private String time;
        private String businessComponentId;

        public RecentIntegrationFlowDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RecentIntegrationFlowDTOBuilder source(String source) {
            this.source = source;
            return this;
        }

        public RecentIntegrationFlowDTOBuilder target(String target) {
            this.target = target;
            return this;
        }

        public RecentIntegrationFlowDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public RecentIntegrationFlowDTOBuilder time(String time) {
            this.time = time;
            return this;
        }

        public RecentIntegrationFlowDTOBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public RecentIntegrationFlowDTO build() {
            return new RecentIntegrationFlowDTO(id, source, target, status, time, businessComponentId);
        }
    }
}
