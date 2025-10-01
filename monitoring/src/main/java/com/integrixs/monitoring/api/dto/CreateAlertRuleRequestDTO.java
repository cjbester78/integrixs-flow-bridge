package com.integrixs.monitoring.api.dto;


/**
 * DTO for create alert rule request
 */
public class CreateAlertRuleRequestDTO {
    private String ruleName;
    private String condition;
    private String alertType;
    private String severity;
    private boolean enabled;
    private int evaluationInterval;
    private String targetMetric;
    private double threshold;
    private String comparison;
    private AlertActionDTO action;


    // Getters
    public String getRuleName() {
        return ruleName;
    }

    public String getCondition() {
        return condition;
    }

    public String getAlertType() {
        return alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getEvaluationInterval() {
        return evaluationInterval;
    }

    public String getTargetMetric() {
        return targetMetric;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getComparison() {
        return comparison;
    }

    public AlertActionDTO getAction() {
        return action;
    }

    // Setters
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEvaluationInterval(int evaluationInterval) {
        this.evaluationInterval = evaluationInterval;
    }

    public void setTargetMetric(String targetMetric) {
        this.targetMetric = targetMetric;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setComparison(String comparison) {
        this.comparison = comparison;
    }

    public void setAction(AlertActionDTO action) {
        this.action = action;
    }

}
