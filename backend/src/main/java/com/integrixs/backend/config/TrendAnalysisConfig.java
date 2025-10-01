package com.integrixs.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for trend analysis service
 */
@Configuration
@ConfigurationProperties(prefix = "trend.analysis")
public class TrendAnalysisConfig {

    private Anomaly anomaly = new Anomaly();
    private Prediction prediction = new Prediction();
    private Capacity capacity = new Capacity();
    private Correlation correlation = new Correlation();
    private PeakDetection peakDetection = new PeakDetection();
    private ErrorHotspotHourly errorHotspotHourly = new ErrorHotspotHourly();
    private VarianceOutlier varianceOutlier = new VarianceOutlier();
    private GrowthPattern growthPattern = new GrowthPattern();
    private Temporal temporal = new Temporal();

    public Anomaly getAnomaly() {
        return anomaly;
    }

    public void setAnomaly(Anomaly anomaly) {
        this.anomaly = anomaly;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public Capacity getCapacity() {
        return capacity;
    }

    public void setCapacity(Capacity capacity) {
        this.capacity = capacity;
    }

    public Correlation getCorrelation() {
        return correlation;
    }

    public void setCorrelation(Correlation correlation) {
        this.correlation = correlation;
    }

    public PeakDetection getPeakDetection() {
        return peakDetection;
    }

    public void setPeakDetection(PeakDetection peakDetection) {
        this.peakDetection = peakDetection;
    }

    public ErrorHotspotHourly getErrorHotspotHourly() {
        return errorHotspotHourly;
    }

    public void setErrorHotspotHourly(ErrorHotspotHourly errorHotspotHourly) {
        this.errorHotspotHourly = errorHotspotHourly;
    }

    public VarianceOutlier getVarianceOutlier() {
        return varianceOutlier;
    }

    public void setVarianceOutlier(VarianceOutlier varianceOutlier) {
        this.varianceOutlier = varianceOutlier;
    }

    public GrowthPattern getGrowthPattern() {
        return growthPattern;
    }

    public void setGrowthPattern(GrowthPattern growthPattern) {
        this.growthPattern = growthPattern;
    }

    public Temporal getTemporal() {
        return temporal;
    }

    public void setTemporal(Temporal temporal) {
        this.temporal = temporal;
    }

    public static class Anomaly {
        private int detectionWindowSize = 20;
        private double sigmaThreshold = 3.0;
        private double highSeveritySigma = 4.0;

        public int getDetectionWindowSize() {
            return detectionWindowSize;
        }

        public void setDetectionWindowSize(int detectionWindowSize) {
            this.detectionWindowSize = detectionWindowSize;
        }

        public double getSigmaThreshold() {
            return sigmaThreshold;
        }

        public void setSigmaThreshold(double sigmaThreshold) {
            this.sigmaThreshold = sigmaThreshold;
        }

        public double getHighSeveritySigma() {
            return highSeveritySigma;
        }

        public void setHighSeveritySigma(double highSeveritySigma) {
            this.highSeveritySigma = highSeveritySigma;
        }
    }

    public static class Prediction {
        private int periodsAhead = 5;
        private int movingAverageWindow = 10;

        public int getPeriodsAhead() {
            return periodsAhead;
        }

        public void setPeriodsAhead(int periodsAhead) {
            this.periodsAhead = periodsAhead;
        }

        public int getMovingAverageWindow() {
            return movingAverageWindow;
        }

        public void setMovingAverageWindow(int movingAverageWindow) {
            this.movingAverageWindow = movingAverageWindow;
        }
    }

    public static class Capacity {
        private double limitPercentage = 0.9;
        private double storageDailyGrowth = 0.01;
        private double projectionConfidence = 0.7;

        public double getLimitPercentage() {
            return limitPercentage;
        }

        public void setLimitPercentage(double limitPercentage) {
            this.limitPercentage = limitPercentage;
        }

        public double getStorageDailyGrowth() {
            return storageDailyGrowth;
        }

        public void setStorageDailyGrowth(double storageDailyGrowth) {
            this.storageDailyGrowth = storageDailyGrowth;
        }

        public double getProjectionConfidence() {
            return projectionConfidence;
        }

        public void setProjectionConfidence(double projectionConfidence) {
            this.projectionConfidence = projectionConfidence;
        }
    }

    public static class Correlation {
        private double minThreshold = 0.5;

        public double getMinThreshold() {
            return minThreshold;
        }

        public void setMinThreshold(double minThreshold) {
            this.minThreshold = minThreshold;
        }
    }

    public static class PeakDetection {
        private double sigmaMultiplier = 2.0;

        public double getSigmaMultiplier() {
            return sigmaMultiplier;
        }

        public void setSigmaMultiplier(double sigmaMultiplier) {
            this.sigmaMultiplier = sigmaMultiplier;
        }
    }

    public static class ErrorHotspotHourly {
        private int threshold = 10;

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }

    public static class VarianceOutlier {
        private double sigmaThreshold = 2.0;

        public double getSigmaThreshold() {
            return sigmaThreshold;
        }

        public void setSigmaThreshold(double sigmaThreshold) {
            this.sigmaThreshold = sigmaThreshold;
        }
    }

    public static class GrowthPattern {
        private int stableThreshold = 5;
        private int exponentialThreshold = 50;
        private int rapidLinearThreshold = 20;

        public int getStableThreshold() {
            return stableThreshold;
        }

        public void setStableThreshold(int stableThreshold) {
            this.stableThreshold = stableThreshold;
        }

        public int getExponentialThreshold() {
            return exponentialThreshold;
        }

        public void setExponentialThreshold(int exponentialThreshold) {
            this.exponentialThreshold = exponentialThreshold;
        }

        public int getRapidLinearThreshold() {
            return rapidLinearThreshold;
        }

        public void setRapidLinearThreshold(int rapidLinearThreshold) {
            this.rapidLinearThreshold = rapidLinearThreshold;
        }
    }

    public static class Temporal {
        private int minDataPoints = 10;
        private int seasonalityMinHours = 168;

        public int getMinDataPoints() {
            return minDataPoints;
        }

        public void setMinDataPoints(int minDataPoints) {
            this.minDataPoints = minDataPoints;
        }

        public int getSeasonalityMinHours() {
            return seasonalityMinHours;
        }

        public void setSeasonalityMinHours(int seasonalityMinHours) {
            this.seasonalityMinHours = seasonalityMinHours;
        }
    }
}