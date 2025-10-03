package com.integrixs.shared.dto;

public class IntegrationFlowStatsDTO {

    private long total;
    private long successful;
    private long processing;
    private long failed;
    private double successRate;
    private double avgProcessingTime;

    // Default constructor
    public IntegrationFlowStatsDTO() {
    }

    // All args constructor
    public IntegrationFlowStatsDTO(long total, long successful, long processing, long failed, double successRate, double avgProcessingTime) {
        this.total = total;
        this.successful = successful;
        this.processing = processing;
        this.failed = failed;
        this.successRate = successRate;
        this.avgProcessingTime = avgProcessingTime;
    }

    // Getters
    public long getTotal() { return total; }
    public long getSuccessful() { return successful; }
    public long getProcessing() { return processing; }
    public long getFailed() { return failed; }
    public double getSuccessRate() { return successRate; }
    public double getAvgProcessingTime() { return avgProcessingTime; }

    // Setters
    public void setTotal(long total) { this.total = total; }
    public void setSuccessful(long successful) { this.successful = successful; }
    public void setProcessing(long processing) { this.processing = processing; }
    public void setFailed(long failed) { this.failed = failed; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    public void setAvgProcessingTime(double avgProcessingTime) { this.avgProcessingTime = avgProcessingTime; }

    // Builder
    public static IntegrationFlowStatsDTOBuilder builder() {
        return new IntegrationFlowStatsDTOBuilder();
    }

    public static class IntegrationFlowStatsDTOBuilder {
        private long total;
        private long successful;
        private long processing;
        private long failed;
        private double successRate;
        private double avgProcessingTime;

        public IntegrationFlowStatsDTOBuilder total(long total) {
            this.total = total;
            return this;
        }

        public IntegrationFlowStatsDTOBuilder successful(long successful) {
            this.successful = successful;
            return this;
        }

        public IntegrationFlowStatsDTOBuilder processing(long processing) {
            this.processing = processing;
            return this;
        }

        public IntegrationFlowStatsDTOBuilder failed(long failed) {
            this.failed = failed;
            return this;
        }

        public IntegrationFlowStatsDTOBuilder successRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public IntegrationFlowStatsDTOBuilder avgProcessingTime(double avgProcessingTime) {
            this.avgProcessingTime = avgProcessingTime;
            return this;
        }

        public IntegrationFlowStatsDTO build() {
            return new IntegrationFlowStatsDTO(total, successful, processing, failed, successRate, avgProcessingTime);
        }
    }
}
