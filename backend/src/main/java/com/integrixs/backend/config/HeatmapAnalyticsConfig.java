package com.integrixs.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for heatmap analytics service
 */
@Configuration
@ConfigurationProperties(prefix = "heatmap.analytics")
public class HeatmapAnalyticsConfig {

    private ExecutionCluster executionCluster = new ExecutionCluster();
    private CriticalPath criticalPath = new CriticalPath();
    private ErrorHotspot errorHotspot = new ErrorHotspot();

    public ExecutionCluster getExecutionCluster() {
        return executionCluster;
    }

    public void setExecutionCluster(ExecutionCluster executionCluster) {
        this.executionCluster = executionCluster;
    }

    public CriticalPath getCriticalPath() {
        return criticalPath;
    }

    public void setCriticalPath(CriticalPath criticalPath) {
        this.criticalPath = criticalPath;
    }

    // Convenience methods
    public long getExecutionClusterThresholdMillis() {
        return executionCluster.getThresholdMillis();
    }

    public int getCriticalPathMinTrafficVolume() {
        return criticalPath.getMinTrafficVolume();
    }

    public double getCriticalPathMaxErrorRate() {
        return criticalPath.getMaxErrorRate();
    }

    public ErrorHotspot getErrorHotspot() {
        return errorHotspot;
    }

    public void setErrorHotspot(ErrorHotspot errorHotspot) {
        this.errorHotspot = errorHotspot;
    }

    public int getErrorHotspotThreshold() {
        return errorHotspot.getThreshold();
    }

    public static class ExecutionCluster {
        private long thresholdMillis = 300000; // 5 minutes default

        public long getThresholdMillis() {
            return thresholdMillis;
        }

        public void setThresholdMillis(long thresholdMillis) {
            this.thresholdMillis = thresholdMillis;
        }
    }

    public static class CriticalPath {
        private int minTrafficVolume = 100;
        private double maxErrorRate = 0.1;

        public int getMinTrafficVolume() {
            return minTrafficVolume;
        }

        public void setMinTrafficVolume(int minTrafficVolume) {
            this.minTrafficVolume = minTrafficVolume;
        }

        public double getMaxErrorRate() {
            return maxErrorRate;
        }

        public void setMaxErrorRate(double maxErrorRate) {
            this.maxErrorRate = maxErrorRate;
        }
    }

    public static class ErrorHotspot {
        private int threshold = 10;

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }
}