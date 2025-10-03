package com.integrixs.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for dashboard statistics.
 *
 * <p>Provides key performance metrics and statistics for the
 * integration platform dashboard.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class DashboardStatsDTO {

    private int activeIntegrations;
    private long integrationFlowsToday;
    private double successRate;
    private long avgResponseTime;
    private Integer totalFlows;
    private Integer errorFlows;
    private Double uptimePercentage;

    // Default constructor
    public DashboardStatsDTO() {
    }

    // All args constructor
    public DashboardStatsDTO(int activeIntegrations, long integrationFlowsToday, double successRate, long avgResponseTime, Integer totalFlows, Integer errorFlows, Double uptimePercentage) {
        this.activeIntegrations = activeIntegrations;
        this.integrationFlowsToday = integrationFlowsToday;
        this.successRate = successRate;
        this.avgResponseTime = avgResponseTime;
        this.totalFlows = totalFlows;
        this.errorFlows = errorFlows;
        this.uptimePercentage = uptimePercentage;
    }

    // Getters
    public int getActiveIntegrations() { return activeIntegrations; }
    public long getIntegrationFlowsToday() { return integrationFlowsToday; }
    public double getSuccessRate() { return successRate; }
    public long getAvgResponseTime() { return avgResponseTime; }
    public Integer getTotalFlows() { return totalFlows; }
    public Integer getErrorFlows() { return errorFlows; }
    public Double getUptimePercentage() { return uptimePercentage; }

    // Setters
    public void setActiveIntegrations(int activeIntegrations) { this.activeIntegrations = activeIntegrations; }
    public void setIntegrationFlowsToday(long integrationFlowsToday) { this.integrationFlowsToday = integrationFlowsToday; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    public void setAvgResponseTime(long avgResponseTime) { this.avgResponseTime = avgResponseTime; }
    public void setTotalFlows(Integer totalFlows) { this.totalFlows = totalFlows; }
    public void setErrorFlows(Integer errorFlows) { this.errorFlows = errorFlows; }
    public void setUptimePercentage(Double uptimePercentage) { this.uptimePercentage = uptimePercentage; }

    // Builder
    public static DashboardStatsDTOBuilder builder() {
        return new DashboardStatsDTOBuilder();
    }

    public static class DashboardStatsDTOBuilder {
        private int activeIntegrations;
        private long integrationFlowsToday;
        private double successRate;
        private long avgResponseTime;
        private Integer totalFlows;
        private Integer errorFlows;
        private Double uptimePercentage;

        public DashboardStatsDTOBuilder activeIntegrations(int activeIntegrations) {
            this.activeIntegrations = activeIntegrations;
            return this;
        }

        public DashboardStatsDTOBuilder integrationFlowsToday(long integrationFlowsToday) {
            this.integrationFlowsToday = integrationFlowsToday;
            return this;
        }

        public DashboardStatsDTOBuilder successRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public DashboardStatsDTOBuilder avgResponseTime(long avgResponseTime) {
            this.avgResponseTime = avgResponseTime;
            return this;
        }

        public DashboardStatsDTOBuilder totalFlows(Integer totalFlows) {
            this.totalFlows = totalFlows;
            return this;
        }

        public DashboardStatsDTOBuilder errorFlows(Integer errorFlows) {
            this.errorFlows = errorFlows;
            return this;
        }

        public DashboardStatsDTOBuilder uptimePercentage(Double uptimePercentage) {
            this.uptimePercentage = uptimePercentage;
            return this;
        }

        public DashboardStatsDTO build() {
            return new DashboardStatsDTO(activeIntegrations, integrationFlowsToday, successRate, avgResponseTime, totalFlows, errorFlows, uptimePercentage);
        }
    }
}
