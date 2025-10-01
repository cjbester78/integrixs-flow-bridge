package com.integrixs.monitoring.api.dto;


import java.util.Map;

/**
 * DTO for alert action
 */
public class AlertActionDTO {
    private String type;
    private Map<String, String> parameters;


    // Getters
    public String getType() {
        return type;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    // Setters
    public void setType(String type) {
        this.type = type;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

}
