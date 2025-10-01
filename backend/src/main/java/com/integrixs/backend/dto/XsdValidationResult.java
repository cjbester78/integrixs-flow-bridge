package com.integrixs.backend.dto;

import java.util.List;

public class XsdValidationResult {
    private String fileName;
    private boolean valid;
    private List<String> errors;
    private List<String> dependencies;
    private List<String> resolvedDependencies;
    private List<String> missingDependencies;

    // Default constructor
    public XsdValidationResult() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getResolvedDependencies() {
        return resolvedDependencies;
    }

    public void setResolvedDependencies(List<String> resolvedDependencies) {
        this.resolvedDependencies = resolvedDependencies;
    }

    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    public void setMissingDependencies(List<String> missingDependencies) {
        this.missingDependencies = missingDependencies;
    }
}
