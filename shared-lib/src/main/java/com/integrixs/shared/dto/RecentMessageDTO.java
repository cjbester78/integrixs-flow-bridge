package com.integrixs.shared.dto;

public class RecentMessageDTO {

    private String id;
    private String source;
    private String target;
    private String status;
    private String time;
    private String businessComponentId;

    // Default constructor
    public RecentMessageDTO() {
    }

    // All args constructor
    public RecentMessageDTO(String id, String source, String target, String status, String time, String businessComponentId) {
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
    public static RecentMessageDTOBuilder builder() {
        return new RecentMessageDTOBuilder();
    }

    public static class RecentMessageDTOBuilder {
        private String id;
        private String source;
        private String target;
        private String status;
        private String time;
        private String businessComponentId;

        public RecentMessageDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RecentMessageDTOBuilder source(String source) {
            this.source = source;
            return this;
        }

        public RecentMessageDTOBuilder target(String target) {
            this.target = target;
            return this;
        }

        public RecentMessageDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public RecentMessageDTOBuilder time(String time) {
            this.time = time;
            return this;
        }

        public RecentMessageDTOBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public RecentMessageDTO build() {
            return new RecentMessageDTO(id, source, target, status, time, businessComponentId);
        }
    }
}
