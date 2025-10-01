package com.integrixs.backend.service;

import com.integrixs.backend.config.TrendAnalysisConfig;
import com.integrixs.backend.dto.dashboard.*;
import com.integrixs.backend.dto.dashboard.trend.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for analyzing historical performance trends.
 */
@Service
public class HistoricalTrendService {

    private final MeterRegistry meterRegistry;
    private final PerformanceDashboardService dashboardService;
    private final TrendAnalysisConfig trendConfig;

    // Time series data storage(in production, use time - series DB)
    private final Map<String, List<TimeSeriesDataPoint>> timeSeriesData = new ConcurrentHashMap<>();

    public HistoricalTrendService(MeterRegistry meterRegistry,
                                  PerformanceDashboardService dashboardService,
                                  TrendAnalysisConfig trendConfig) {
        this.meterRegistry = meterRegistry;
        this.dashboardService = dashboardService;
        this.trendConfig = trendConfig;
    }

    /**
     * Get trend analysis for a specific metric.
     */
    public TrendAnalysis analyzeTrend(String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        TrendAnalysis analysis = new TrendAnalysis();
        analysis.setMetricName(metricName);
        analysis.setStartTime(startTime);
        analysis.setEndTime(endTime);

        // Get historical data points
        List<TimeSeriesDataPoint> dataPoints = getTimeSeriesData(metricName, startTime, endTime);
        analysis.setDataPoints(dataPoints);

        if(dataPoints.size() < 2) {
            analysis.setTrend("INSUFFICIENT_DATA");
            return analysis;
        }

        // Calculate statistics
        analysis.setStatistics(calculateStatistics(dataPoints));

        // Determine trend
        analysis.setTrend(determineTrend(dataPoints));

        // Find anomalies
        analysis.setAnomalies(detectAnomalies(dataPoints));

        // Make predictions
        analysis.setPredictions(makePredictions(dataPoints));

        // Calculate correlation with other metrics
        analysis.setCorrelations(findCorrelations(metricName, dataPoints));

        return analysis;
    }

    /**
     * Get comparative analysis between components.
     */
    public ComponentComparison compareComponents(List<String> componentIds,
                                               String metric,
                                               LocalDateTime startTime,
                                               LocalDateTime endTime) {
        ComponentComparison comparison = new ComponentComparison();
        comparison.setComponentIds(componentIds);
        comparison.setMetric(metric);
        comparison.setTimeRange(Duration.between(startTime, endTime));

        Map<String, List<TimeSeriesDataPoint>> componentData = new HashMap<>();

        // Collect data for each component
        for(String componentId : componentIds) {
            String metricKey = componentId + "." + metric;
            List<TimeSeriesDataPoint> data = getTimeSeriesData(metricKey, startTime, endTime);
            componentData.put(componentId, data);
        }

        comparison.setComponentData(componentData);

        // Calculate comparative statistics
        Map<String, ComponentStats> componentStats = new HashMap<>();
        for(Map.Entry<String, List<TimeSeriesDataPoint>> entry : componentData.entrySet()) {
            componentStats.put(entry.getKey(), calculateComponentStats(entry.getValue()));
        }
        comparison.setComponentStatistics(componentStats);

        // Rank components
        comparison.setRanking(rankComponents(componentStats, metric));

        // Identify outliers
        comparison.setOutliers(identifyOutliers(componentStats));

        return comparison;
    }

    /**
     * Get capacity planning insights.
     */
    public CapacityPlanningInsights getCapacityInsights(LocalDateTime startTime, LocalDateTime endTime) {
        CapacityPlanningInsights insights = new CapacityPlanningInsights();

        // Analyze resource utilization trends
        insights.setResourceTrends(analyzeResourceTrends(startTime, endTime));

        // Project future capacity needs
        insights.setCapacityProjections(projectCapacityNeeds());

        // Identify growth patterns
        insights.setGrowthPatterns(identifyGrowthPatterns(startTime, endTime));

        // Make recommendations
        insights.setRecommendations(generateCapacityRecommendations(insights));

        return insights;
    }

    /**
     * Get performance trends by time of day/week.
     */
    public TemporalPatterns analyzeTemporalPatterns(String metric, int daysToAnalyze) {
        TemporalPatterns patterns = new TemporalPatterns();
        patterns.setMetric(metric);
        patterns.setAnalysisPeriodDays(daysToAnalyze);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(daysToAnalyze);

        List<TimeSeriesDataPoint> data = getTimeSeriesData(metric, startTime, endTime);

        // Analyze by hour of day
        patterns.setHourlyPatterns(analyzeHourlyPatterns(data));

        // Analyze by day of week
        patterns.setDailyPatterns(analyzeDailyPatterns(data));

        // Identify peak periods
        patterns.setPeakPeriods(identifyPeakPeriods(data));

        // Calculate seasonality
        patterns.setSeasonalityScore(calculateSeasonality(data));

        return patterns;
    }

    /**
     * Get regression analysis for performance metrics.
     */
    public RegressionAnalysis performRegression(String dependentMetric,
                                              List<String> independentMetrics,
                                              LocalDateTime startTime,
                                              LocalDateTime endTime) {
        RegressionAnalysis analysis = new RegressionAnalysis();
        analysis.setDependentVariable(dependentMetric);
        analysis.setIndependentVariables(independentMetrics);

        // Collect data
        List<TimeSeriesDataPoint> dependentData = getTimeSeriesData(dependentMetric, startTime, endTime);
        Map<String, List<TimeSeriesDataPoint>> independentData = new HashMap<>();

        for(String metric : independentMetrics) {
            independentData.put(metric, getTimeSeriesData(metric, startTime, endTime));
        }

        // Perform simple linear regression(in production, use proper ML library)
        Map<String, Double> coefficients = calculateRegressionCoefficients(dependentData, independentData);
        analysis.setCoefficients(coefficients);

        // Calculate R - squared
        analysis.setRSquared(calculateRSquared(dependentData, independentData, coefficients));

        // Calculate p - values
        analysis.setPValues(calculatePValues(dependentData, independentData, coefficients));

        // Make predictions
        analysis.setPredictedValues(makePredictionsFromRegression(independentData, coefficients));

        return analysis;
    }

    /**
     * Get time series data for a metric.
     */
    private List<TimeSeriesDataPoint> getTimeSeriesData(String metricName,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {
        // In production, this would query a time - series database
        // For now, generate sample data based on current metrics
        List<TimeSeriesDataPoint> dataPoints = new ArrayList<>();

        // Try to get real data from Micrometer
        Meter meter = meterRegistry.find(metricName).meter();
        if(meter != null) {
            // Generate synthetic historical data based on current value
            double currentValue = getCurrentValue(meter);

            long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
            for(long i = 0; i <= minutes; i += 5) { // 5 - minute intervals
                LocalDateTime timestamp = startTime.plusMinutes(i);
                double value = currentValue * (0.8 + Math.random() * 0.4); // ±20% variation

                TimeSeriesDataPoint point = new TimeSeriesDataPoint();
                point.setTimestamp(timestamp);
                point.setValue(value);
                point.setMetricName(metricName);

                dataPoints.add(point);
            }
        }

        return dataPoints;
    }

    /**
     * Get current value from a meter.
     */
    private double getCurrentValue(Meter meter) {
        if(meter instanceof Gauge) {
            return((Gauge) meter).value();
        } else if(meter instanceof Counter) {
            return((Counter) meter).count();
        } else if(meter instanceof Timer) {
            return((Timer) meter).mean(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }

    /**
     * Calculate statistics for data points.
     */
    private Map<String, Double> calculateStatistics(List<TimeSeriesDataPoint> dataPoints) {
        Map<String, Double> stats = new HashMap<>();

        if(dataPoints.isEmpty()) {
            return stats;
        }

        double[] values = dataPoints.stream()
            .mapToDouble(TimeSeriesDataPoint::getValue)
            .toArray();

        // Basic statistics
        stats.put("mean", Arrays.stream(values).average().orElse(0));
        stats.put("min", Arrays.stream(values).min().orElse(0));
        stats.put("max", Arrays.stream(values).max().orElse(0));

        // Standard deviation
        double mean = stats.get("mean");
        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        stats.put("stdDev", Math.sqrt(variance));

        // Percentiles
        Arrays.sort(values);
        stats.put("p50", getPercentile(values, 0.5));
        stats.put("p90", getPercentile(values, 0.9));
        stats.put("p95", getPercentile(values, 0.95));
        stats.put("p99", getPercentile(values, 0.99));

        return stats;
    }

    /**
     * Get percentile value.
     */
    private double getPercentile(double[] sortedValues, double percentile) {
        if(sortedValues.length == 0) return 0;

        int index = (int) Math.ceil(percentile * sortedValues.length) - 1;
        return sortedValues[Math.min(index, sortedValues.length - 1)];
    }

    /**
     * Determine trend direction.
     */
    private String determineTrend(List<TimeSeriesDataPoint> dataPoints) {
        if(dataPoints.size() < 2) return "UNKNOWN";

        // Simple linear regression to determine trend
        double n = dataPoints.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for(int i = 0; i < dataPoints.size(); i++) {
            double x = i;
            double y = dataPoints.get(i).getValue();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

        // Determine trend based on slope
        if(Math.abs(slope) < 0.01) return "STABLE";
        if(slope > 0.1) return "INCREASING_RAPIDLY";
        if(slope > 0) return "INCREASING";
        if(slope < -0.1) return "DECREASING_RAPIDLY";
        return "DECREASING";
    }

    /**
     * Detect anomalies in data.
     */
    private List<Anomaly> detectAnomalies(List<TimeSeriesDataPoint> dataPoints) {
        List<Anomaly> anomalies = new ArrayList<>();

        if(dataPoints.size() < trendConfig.getTemporal().getMinDataPoints()) return anomalies;

        // Calculate moving average and standard deviation
        int windowSize = Math.min(trendConfig.getAnomaly().getDetectionWindowSize(), dataPoints.size() / 5);

        for(int i = windowSize; i < dataPoints.size(); i++) {
            // Calculate stats for window
            List<TimeSeriesDataPoint> window = dataPoints.subList(i - windowSize, i);
            double mean = window.stream()
                .mapToDouble(TimeSeriesDataPoint::getValue)
                .average()
                .orElse(0);

            double stdDev = Math.sqrt(window.stream()
                .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
                .average()
                .orElse(0));

            // Check if current point is anomaly(3 sigma rule)
            TimeSeriesDataPoint current = dataPoints.get(i);
            double deviation = Math.abs(current.getValue() - mean);

            if(deviation > trendConfig.getAnomaly().getSigmaThreshold() * stdDev) {
                Anomaly anomaly = new Anomaly();
                anomaly.setTimestamp(current.getTimestamp());
                anomaly.setValue(current.getValue());
                anomaly.setExpectedValue(mean);
                anomaly.setDeviation(deviation / stdDev);
                anomaly.setSeverity(deviation > trendConfig.getAnomaly().getHighSeveritySigma() * stdDev ? "HIGH" : "MEDIUM");

                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * Make predictions based on historical data.
     */
    private List<Prediction> makePredictions(List<TimeSeriesDataPoint> dataPoints) {
        List<Prediction> predictions = new ArrayList<>();

        if(dataPoints.size() < trendConfig.getTemporal().getMinDataPoints()) return predictions;

        // Simple moving average prediction
        int periods = trendConfig.getPrediction().getPeriodsAhead();
        int maWindow = Math.min(trendConfig.getPrediction().getMovingAverageWindow(), dataPoints.size() / 2);

        double movingAvg = dataPoints.subList(dataPoints.size() - maWindow, dataPoints.size())
            .stream()
            .mapToDouble(TimeSeriesDataPoint::getValue)
            .average()
            .orElse(0);

        // Calculate trend
        double trend = determineTrendValue(dataPoints);

        LocalDateTime lastTime = dataPoints.get(dataPoints.size() - 1).getTimestamp();

        for(int i = 1; i <= periods; i++) {
            Prediction pred = new Prediction();
            pred.setTimestamp(lastTime.plusMinutes(i * 5));
            pred.setPredictedValue(movingAvg + (trend * i));
            pred.setConfidenceLevel(Math.max(0.5, 1.0 - (i * 0.1))); // Confidence decreases with time

            predictions.add(pred);
        }

        return predictions;
    }

    /**
     * Calculate trend value for predictions.
     */
    private double determineTrendValue(List<TimeSeriesDataPoint> dataPoints) {
        if(dataPoints.size() < 2) return 0;

        // Use last 10 points to determine trend
        int trendWindow = Math.min(10, dataPoints.size());
        List<TimeSeriesDataPoint> recentPoints = dataPoints.subList(
            dataPoints.size() - trendWindow, dataPoints.size());

        double firstValue = recentPoints.get(0).getValue();
        double lastValue = recentPoints.get(recentPoints.size() - 1).getValue();

        return(lastValue - firstValue) / trendWindow;
    }

    /**
     * Find correlations with other metrics.
     */
    private Map<String, Double> findCorrelations(String baseMetric, List<TimeSeriesDataPoint> baseData) {
        Map<String, Double> correlations = new HashMap<>();

        // Get list of other metrics
        Set<String> otherMetrics = meterRegistry.getMeters().stream()
            .map(meter -> meter.getId().getName())
            .filter(name -> !name.equals(baseMetric))
            .collect(Collectors.toSet());

        // Calculate correlation with each metric
        for(String metric : otherMetrics) {
            List<TimeSeriesDataPoint> otherData = getTimeSeriesData(
                metric,
                baseData.get(0).getTimestamp(),
                baseData.get(baseData.size() - 1).getTimestamp()
           );

            if(otherData.size() == baseData.size()) {
                double correlation = calculateCorrelation(baseData, otherData);
                if(Math.abs(correlation) > trendConfig.getCorrelation().getMinThreshold()) {
                    correlations.put(metric, correlation);
                }
            }
        }

        return correlations;
    }

    /**
     * Calculate Pearson correlation coefficient.
     */
    private double calculateCorrelation(List<TimeSeriesDataPoint> data1, List<TimeSeriesDataPoint> data2) {
        if(data1.size() != data2.size() || data1.isEmpty()) return 0;

        double[] x = data1.stream().mapToDouble(TimeSeriesDataPoint::getValue).toArray();
        double[] y = data2.stream().mapToDouble(TimeSeriesDataPoint::getValue).toArray();

        double meanX = Arrays.stream(x).average().orElse(0);
        double meanY = Arrays.stream(y).average().orElse(0);

        double numerator = 0;
        double denomX = 0;
        double denomY = 0;

        for(int i = 0; i < x.length; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;

            numerator += dx * dy;
            denomX += dx * dx;
            denomY += dy * dy;
        }

        if(denomX == 0 || denomY == 0) return 0;

        return numerator / Math.sqrt(denomX * denomY);
    }

    /**
     * Calculate component statistics.
     */
    private ComponentStats calculateComponentStats(List<TimeSeriesDataPoint> data) {
        ComponentStats stats = new ComponentStats();

        if(!data.isEmpty()) {
            stats.setMean(data.stream().mapToDouble(TimeSeriesDataPoint::getValue).average().orElse(0));
            stats.setMax(data.stream().mapToDouble(TimeSeriesDataPoint::getValue).max().orElse(0));
            stats.setMin(data.stream().mapToDouble(TimeSeriesDataPoint::getValue).min().orElse(0));

            // Calculate variance
            double mean = stats.getMean();
            double variance = data.stream()
                .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
                .average()
                .orElse(0);
            stats.setStdDev(Math.sqrt(variance));

            // Calculate trend
            stats.setTrend(determineTrend(data));
        }

        return stats;
    }

    /**
     * Rank components by performance.
     */
    private List<ComponentRanking> rankComponents(Map<String, ComponentStats> componentStats, String metric) {
        return componentStats.entrySet().stream()
            .map(entry -> {
                ComponentRanking ranking = new ComponentRanking();
                ranking.setComponentId(entry.getKey());
                ranking.setScore(calculatePerformanceScore(entry.getValue(), metric));
                return ranking;
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    /**
     * Calculate performance score for ranking.
     */
    private double calculatePerformanceScore(ComponentStats stats, String metric) {
        // Simple scoring based on mean and stability
        double score = stats.getMean();

        // Penalize high variance
        if(stats.getStdDev() > 0) {
            score = score / (1 + stats.getStdDev() / stats.getMean());
        }

        // Adjust for trend
        if("INCREASING_RAPIDLY".equals(stats.getTrend()) && metric.contains("error")) {
            score *= 0.5; // Penalize rapidly increasing errors
        } else if("DECREASING_RAPIDLY".equals(stats.getTrend()) && metric.contains("performance")) {
            score *= 0.7; // Penalize rapidly decreasing performance
        }

        return score;
    }

    /**
     * Identify outlier components.
     */
    private List<String> identifyOutliers(Map<String, ComponentStats> componentStats) {
        List<String> outliers = new ArrayList<>();

        // Calculate overall statistics
        double[] means = componentStats.values().stream()
            .mapToDouble(ComponentStats::getMean)
            .toArray();

        if(means.length < 3) return outliers;

        double overallMean = Arrays.stream(means).average().orElse(0);
        double overallStdDev = Math.sqrt(Arrays.stream(means)
            .map(m -> Math.pow(m - overallMean, 2))
            .average()
            .orElse(0));

        // Identify outliers(2 sigma rule)
        for(Map.Entry<String, ComponentStats> entry : componentStats.entrySet()) {
            double deviation = Math.abs(entry.getValue().getMean() - overallMean);
            if(deviation > trendConfig.getVarianceOutlier().getSigmaThreshold() * overallStdDev) {
                outliers.add(entry.getKey());
            }
        }

        return outliers;
    }

    /**
     * Analyze resource trends.
     */
    private Map<String, ResourceTrend> analyzeResourceTrends(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, ResourceTrend> trends = new HashMap<>();

        // CPU trend
        trends.put("cpu", analyzeResourceMetric("process.cpu.usage", startTime, endTime));

        // Memory trend
        trends.put("memory", analyzeResourceMetric("jvm.memory.used", startTime, endTime));

        // Thread trend
        trends.put("threads", analyzeResourceMetric("jvm.threads.live", startTime, endTime));

        return trends;
    }

    /**
     * Analyze a specific resource metric.
     */
    private ResourceTrend analyzeResourceMetric(String metric, LocalDateTime startTime, LocalDateTime endTime) {
        ResourceTrend trend = new ResourceTrend();
        trend.setMetric(metric);

        List<TimeSeriesDataPoint> data = getTimeSeriesData(metric, startTime, endTime);

        if(!data.isEmpty()) {
            trend.setCurrentValue(data.get(data.size() - 1).getValue());
            trend.setAverageValue(data.stream().mapToDouble(TimeSeriesDataPoint::getValue).average().orElse(0));
            trend.setPeakValue(data.stream().mapToDouble(TimeSeriesDataPoint::getValue).max().orElse(0));
            trend.setTrend(determineTrend(data));
            trend.setGrowthRate(calculateGrowthRate(data));
        }

        return trend;
    }

    /**
     * Calculate growth rate.
     */
    private double calculateGrowthRate(List<TimeSeriesDataPoint> data) {
        if(data.size() < 2) return 0;

        double firstValue = data.get(0).getValue();
        double lastValue = data.get(data.size() - 1).getValue();

        if(firstValue == 0) return 0;

        return((lastValue - firstValue) / firstValue) * 100;
    }

    /**
     * Project capacity needs.
     */
    private Map<String, CapacityProjection> projectCapacityNeeds() {
        Map<String, CapacityProjection> projections = new HashMap<>();

        // Project for next 30 days
        LocalDateTime now = LocalDateTime.now();

        projections.put("cpu", projectResourceCapacity("process.cpu.usage", now, 30));
        projections.put("memory", projectResourceCapacity("jvm.memory.used", now, 30));
        projections.put("storage", projectStorageCapacity(now, 30));

        return projections;
    }

    /**
     * Project resource capacity.
     */
    private CapacityProjection projectResourceCapacity(String metric, LocalDateTime baseTime, int daysAhead) {
        CapacityProjection projection = new CapacityProjection();
        projection.setResource(metric);

        // Get historical data
        List<TimeSeriesDataPoint> historicalData = getTimeSeriesData(
            metric,
            baseTime.minusDays(30),
            baseTime
       );

        if(!historicalData.isEmpty()) {
            // Calculate growth trend
            double growthRate = calculateGrowthRate(historicalData) / 30; // Daily growth rate

            double currentValue = historicalData.get(historicalData.size() - 1).getValue();
            double projectedValue = currentValue * (1 + (growthRate * daysAhead / 100));

            projection.setCurrentUsage(currentValue);
            projection.setProjectedUsage(projectedValue);
            projection.setDaysUntilCapacity(estimateDaysUntilCapacity(currentValue, growthRate));
            projection.setConfidence(trendConfig.getCapacity().getProjectionConfidence());
        }

        return projection;
    }

    /**
     * Project storage capacity.
     */
    private CapacityProjection projectStorageCapacity(LocalDateTime baseTime, int daysAhead) {
        CapacityProjection projection = new CapacityProjection();
        projection.setResource("storage");

        // Estimate based on log growth
        // In production, would query actual storage metrics
        projection.setCurrentUsage(1024); // 1GB estimate
        projection.setProjectedUsage(1024 * (1 + daysAhead * trendConfig.getCapacity().getStorageDailyGrowth()));
        projection.setDaysUntilCapacity(365); // Rough estimate
        projection.setConfidence(trendConfig.getCapacity().getProjectionConfidence() * 0.7);

        return projection;
    }

    /**
     * Estimate days until capacity limit.
     */
    private int estimateDaysUntilCapacity(double currentUsage, double dailyGrowthRate) {
        if(dailyGrowthRate <= 0) return Integer.MAX_VALUE;

        double capacityLimit = trendConfig.getCapacity().getLimitPercentage();
        double currentUtilization = currentUsage; // Assume current is percentage

        if(currentUtilization >= capacityLimit) return 0;

        // Simple calculation: days = (limit - current) / daily_growth
        return(int) ((capacityLimit - currentUtilization) / (dailyGrowthRate / 100));
    }

    /**
     * Identify growth patterns.
     */
    private List<GrowthPattern> identifyGrowthPatterns(LocalDateTime startTime, LocalDateTime endTime) {
        List<GrowthPattern> patterns = new ArrayList<>();

        // Analyze different metrics for patterns
        String[] metricsToAnalyze = {
            "adapter.operations.total",
            "adapter.data.volume",
            "adapter.messages.processed"
        };

        for(String metric : metricsToAnalyze) {
            GrowthPattern pattern = analyzeGrowthPattern(metric, startTime, endTime);
            if(pattern != null) {
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    /**
     * Analyze growth pattern for a metric.
     */
    private GrowthPattern analyzeGrowthPattern(String metric, LocalDateTime startTime, LocalDateTime endTime) {
        List<TimeSeriesDataPoint> data = getTimeSeriesData(metric, startTime, endTime);

        if(data.size() < 10) return null;

        GrowthPattern pattern = new GrowthPattern();
        pattern.setMetric(metric);

        // Determine pattern type
        double growthRate = calculateGrowthRate(data);
        pattern.setGrowthRate(growthRate);

        if(Math.abs(growthRate) < trendConfig.getGrowthPattern().getStableThreshold()) {
            pattern.setType("STABLE");
        } else if(growthRate > trendConfig.getGrowthPattern().getExponentialThreshold()) {
            pattern.setType("EXPONENTIAL");
        } else if(growthRate > trendConfig.getGrowthPattern().getRapidLinearThreshold()) {
            pattern.setType("RAPID_LINEAR");
        } else if(growthRate > 0) {
            pattern.setType("GRADUAL_LINEAR");
        } else {
            pattern.setType("DECLINING");
        }

        // Calculate volatility
        double mean = data.stream().mapToDouble(TimeSeriesDataPoint::getValue).average().orElse(0);
        double variance = data.stream()
            .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
            .average()
            .orElse(0);
        pattern.setVolatility(Math.sqrt(variance) / mean);

        return pattern;
    }

    /**
     * Generate capacity recommendations.
     */
    private List<String> generateCapacityRecommendations(CapacityPlanningInsights insights) {
        List<String> recommendations = new ArrayList<>();

        // Check CPU projections
        CapacityProjection cpuProjection = insights.getCapacityProjections().get("cpu");
        if(cpuProjection != null && cpuProjection.getDaysUntilCapacity() < 30) {
            recommendations.add("CPU capacity will be reached in " + cpuProjection.getDaysUntilCapacity() +
                              " days. Consider scaling up compute resources.");
        }

        // Check memory projections
        CapacityProjection memoryProjection = insights.getCapacityProjections().get("memory");
        if(memoryProjection != null && memoryProjection.getProjectedUsage() > 80) {
            recommendations.add("Memory usage is projected to exceed 80%. Consider increasing heap size.");
        }

        // Check growth patterns
        for(GrowthPattern pattern : insights.getGrowthPatterns()) {
            if("EXPONENTIAL".equals(pattern.getType())) {
                recommendations.add("Exponential growth detected in " + pattern.getMetric() +
                                  ". Review scaling strategy.");
            }
        }

        return recommendations;
    }

    /**
     * Analyze hourly patterns.
     */
    private Map<Integer, HourlyStats> analyzeHourlyPatterns(List<TimeSeriesDataPoint> data) {
        Map<Integer, List<Double>> hourlyValues = new HashMap<>();

        // Group values by hour
        for(TimeSeriesDataPoint point : data) {
            int hour = point.getTimestamp().getHour();
            hourlyValues.computeIfAbsent(hour, k -> new ArrayList<>()).add(point.getValue());
        }

        // Calculate stats for each hour
        Map<Integer, HourlyStats> hourlyStats = new HashMap<>();
        for(Map.Entry<Integer, List<Double>> entry : hourlyValues.entrySet()) {
            HourlyStats stats = new HourlyStats();
            stats.setHour(entry.getKey());
            stats.setAverageValue(entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0));
            stats.setSampleCount(entry.getValue().size());

            hourlyStats.put(entry.getKey(), stats);
        }

        return hourlyStats;
    }

    /**
     * Analyze daily patterns.
     */
    private Map<String, DailyStats> analyzeDailyPatterns(List<TimeSeriesDataPoint> data) {
        Map<String, List<Double>> dailyValues = new HashMap<>();

        // Group values by day of week
        for(TimeSeriesDataPoint point : data) {
            String dayOfWeek = point.getTimestamp().getDayOfWeek().toString();
            dailyValues.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(point.getValue());
        }

        // Calculate stats for each day
        Map<String, DailyStats> dailyStats = new HashMap<>();
        for(Map.Entry<String, List<Double>> entry : dailyValues.entrySet()) {
            DailyStats stats = new DailyStats();
            stats.setDayOfWeek(entry.getKey());
            stats.setAverageValue(entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0));
            stats.setSampleCount(entry.getValue().size());

            dailyStats.put(entry.getKey(), stats);
        }

        return dailyStats;
    }

    /**
     * Identify peak periods.
     */
    private List<PeakPeriod> identifyPeakPeriods(List<TimeSeriesDataPoint> data) {
        List<PeakPeriod> peaks = new ArrayList<>();

        if(data.isEmpty()) return peaks;

        // Calculate threshold for peak(e.g., mean + 2 * stddev)
        double mean = data.stream().mapToDouble(TimeSeriesDataPoint::getValue).average().orElse(0);
        double stdDev = Math.sqrt(data.stream()
            .mapToDouble(p -> Math.pow(p.getValue() - mean, 2))
            .average()
            .orElse(0));

        double peakThreshold = mean + trendConfig.getPeakDetection().getSigmaMultiplier() * stdDev;

        // Find continuous periods above threshold
        boolean inPeak = false;
        PeakPeriod currentPeak = null;

        for(TimeSeriesDataPoint point : data) {
            if(point.getValue() > peakThreshold) {
                if(!inPeak) {
                    currentPeak = new PeakPeriod();
                    currentPeak.setStartTime(point.getTimestamp());
                    currentPeak.setPeakValue(point.getValue());
                    inPeak = true;
                } else {
                    currentPeak.setPeakValue(Math.max(currentPeak.getPeakValue(), point.getValue()));
                }
            } else {
                if(inPeak && currentPeak != null) {
                    currentPeak.setEndTime(point.getTimestamp());
                    currentPeak.setDuration(Duration.between(currentPeak.getStartTime(), currentPeak.getEndTime()));
                    peaks.add(currentPeak);
                    inPeak = false;
                }
            }
        }

        return peaks;
    }

    /**
     * Calculate seasonality score.
     */
    private double calculateSeasonality(List<TimeSeriesDataPoint> data) {
        if(data.size() < trendConfig.getTemporal().getSeasonalityMinHours()) return 0;

        // Simple seasonality detection using autocorrelation
        double[] values = data.stream().mapToDouble(TimeSeriesDataPoint::getValue).toArray();

        // Check daily seasonality(24 - hour period)
        double dailyCorrelation = calculateAutocorrelation(values, 24);

        // Check weekly seasonality(168 - hour period)
        double weeklyCorrelation = calculateAutocorrelation(values, 168);

        // Return the stronger seasonality
        return Math.max(dailyCorrelation, weeklyCorrelation);
    }

    /**
     * Calculate autocorrelation for a given lag.
     */
    private double calculateAutocorrelation(double[] values, int lag) {
        if(values.length <= lag) return 0;

        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);

        if(variance == 0) return 0;

        double covariance = 0;
        for(int i = lag; i < values.length; i++) {
            covariance += (values[i] - mean) * (values[i - lag] - mean);
        }
        covariance /= (values.length - lag);

        return covariance / variance;
    }

    /**
     * Calculate regression coefficients.
     */
    private Map<String, Double> calculateRegressionCoefficients(
            List<TimeSeriesDataPoint> dependent,
            Map<String, List<TimeSeriesDataPoint>> independent) {

        // Simple implementation - in production use proper ML library
        Map<String, Double> coefficients = new HashMap<>();

        // For simplicity, calculate simple correlation as coefficient
        for(Map.Entry<String, List<TimeSeriesDataPoint>> entry : independent.entrySet()) {
            double correlation = calculateCorrelation(dependent, entry.getValue());
            coefficients.put(entry.getKey(), correlation);
        }

        return coefficients;
    }

    /**
     * Calculate R - squared value.
     */
    private double calculateRSquared(List<TimeSeriesDataPoint> dependent,
                                   Map<String, List<TimeSeriesDataPoint>> independent,
                                   Map<String, Double> coefficients) {
        // Simplified R - squared calculation
        // In production, use proper statistical library

        double meanDependent = dependent.stream()
            .mapToDouble(TimeSeriesDataPoint::getValue)
            .average()
            .orElse(0);

        double totalSumSquares = dependent.stream()
            .mapToDouble(p -> Math.pow(p.getValue() - meanDependent, 2))
            .sum();

        // Very simplified - just use the strongest correlation
        double maxCorrelation = coefficients.values().stream()
            .mapToDouble(Math::abs)
            .max()
            .orElse(0);

        return maxCorrelation * maxCorrelation; // R² approximation
    }

    /**
     * Calculate p - values.
     */
    private Map<String, Double> calculatePValues(List<TimeSeriesDataPoint> dependent,
                                               Map<String, List<TimeSeriesDataPoint>> independent,
                                               Map<String, Double> coefficients) {
        Map<String, Double> pValues = new HashMap<>();

        // Simplified p - value calculation
        // In production, use proper statistical library
        for(String variable : coefficients.keySet()) {
            double correlation = coefficients.get(variable);
            int n = dependent.size();

            // t - statistic approximation
            double t = correlation * Math.sqrt((n - 2) / (1 - correlation * correlation));

            // Very rough p - value approximation
            double pValue = 2 * (1 - normalCDF(Math.abs(t)));
            pValues.put(variable, pValue);
        }

        return pValues;
    }

    /**
     * Simple normal CDF approximation.
     */
    private double normalCDF(double x) {
        // Approximation of the normal CDF
        return 0.5 * (1 + Math.signum(x) * Math.sqrt(1 - Math.exp(-2 * x * x / Math.PI)));
    }

    /**
     * Make predictions from regression.
     */
    private List<Double> makePredictionsFromRegression(Map<String, List<TimeSeriesDataPoint>> independent,
                                                     Map<String, Double> coefficients) {
        List<Double> predictions = new ArrayList<>();

        // Simple linear combination
        int dataSize = independent.values().stream()
            .findFirst()
            .map(List::size)
            .orElse(0);

        for(int i = 0; i < dataSize; i++) {
            double prediction = 0;

            for(Map.Entry<String, List<TimeSeriesDataPoint>> entry : independent.entrySet()) {
                String variable = entry.getKey();
                double coefficient = coefficients.getOrDefault(variable, 0.0);
                double value = entry.getValue().get(i).getValue();

                prediction += coefficient * value;
            }

            predictions.add(prediction);
        }

        return predictions;
    }
}
