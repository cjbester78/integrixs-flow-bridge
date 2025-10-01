package com.integrixs.shared.dto;

public class ChannelStatusDTO {

    private String name;
    private String status;
    private int load;
    private String businessComponentId;

    // Default constructor
    public ChannelStatusDTO() {
    }

    // All args constructor
    public ChannelStatusDTO(String name, String status, int load, String businessComponentId) {
        this.name = name;
        this.status = status;
        this.load = load;
        this.businessComponentId = businessComponentId;
    }

    // Getters
    public String getName() { return name; }
    public String getStatus() { return status; }
    public int getLoad() { return load; }
    public String getBusinessComponentId() { return businessComponentId; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setLoad(int load) { this.load = load; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }

    // Builder
    public static ChannelStatusDTOBuilder builder() {
        return new ChannelStatusDTOBuilder();
    }

    public static class ChannelStatusDTOBuilder {
        private String name;
        private String status;
        private int load;
        private String businessComponentId;

        public ChannelStatusDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChannelStatusDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ChannelStatusDTOBuilder load(int load) {
            this.load = load;
            return this;
        }

        public ChannelStatusDTOBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public ChannelStatusDTO build() {
            return new ChannelStatusDTO(name, status, load, businessComponentId);
        }
    }
}
