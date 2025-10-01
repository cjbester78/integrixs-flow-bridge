package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.List;

public class ErrorPropagationPath {
    private List<ErrorEvent> path;
    private double propagationSpeed;
    private String rootCause;
    private String correlationId;
    private String originComponent;
    private List<ErrorEvent> errorSequence;
    private long propagationTime;

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

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getOriginComponent() {
        return originComponent;
    }

    public void setOriginComponent(String originComponent) {
        this.originComponent = originComponent;
    }

    public List<ErrorEvent> getErrorSequence() {
        return errorSequence;
    }

    public void setErrorSequence(List<ErrorEvent> errorSequence) {
        this.errorSequence = errorSequence;
    }

    public long getPropagationTime() {
        return propagationTime;
    }

    public void setPropagationTime(long propagationTime) {
        this.propagationTime = propagationTime;
    }
}
