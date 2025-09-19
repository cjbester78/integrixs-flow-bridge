package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.List;

public class ErrorPropagationPath {
    private List<ErrorEvent> path;
    private double propagationSpeed;
    private String rootCause;

    // Default constructor
    public ErrorPropagationPath() {
    }

    public List<ErrorEvent> getPath() {
        return path;
    }

    public void setPath(List<ErrorEvent> path) {
        this.path = path;
    }

    public double getPropagationSpeed() {
        return propagationSpeed;
    }

    public void setPropagationSpeed(double propagationSpeed) {
        this.propagationSpeed = propagationSpeed;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }
}
