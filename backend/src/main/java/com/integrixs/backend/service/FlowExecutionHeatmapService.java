package com.integrixs.backend.service;

import com.integrixs.backend.dto.dashboard.heatmap.*;
import com.integrixs.backend.dto.FlowExecutionPatterns;
import com.integrixs.backend.dto.ErrorPropagationHeatmap;
import com.integrixs.backend.dto.ExecutionStats;
import com.integrixs.backend.config.HeatmapAnalyticsConfig;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for generating flow execution heatmaps and analytics.
 */
@Service
public class FlowExecutionHeatmapService {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionHeatmapService.class);

    private final SystemLogSqlRepository systemLogRepository;
    private final HeatmapAnalyticsConfig heatmapConfig;

    @Autowired
    public FlowExecutionHeatmapService(SystemLogSqlRepository systemLogRepository,
                                     HeatmapAnalyticsConfig heatmapConfig) {
        this.systemLogRepository = systemLogRepository;
        this.heatmapConfig = heatmapConfig;
    }

    /**
     * Generate execution heatmap for flows.
     */
    public FlowExecutionHeatmap generateHeatmap(HeatmapRequest request) {
        FlowExecutionHeatmap heatmap = new FlowExecutionHeatmap();
        heatmap.setStartTime(request.getStartTime());
        heatmap.setEndTime(request.getEndTime());
        heatmap.setGranularity(request.getGranularity());

        // Get flow execution data
        // Convert List<UUID> to List<String>
        List<String> flowIdStrings = request.getFlowIds() != null ?
            request.getFlowIds().stream()
                .map(UUID::toString)
                .collect(Collectors.toList()) :
            Collections.emptyList();

        List<FlowExecutionData> executionData = collectFlowExecutionData(
            request.getStartTime(),
            request.getEndTime(),
            flowIdStrings
       );

        // Generate time - based grid
        heatmap.setTimeGrid(generateTimeGrid(request));

        // Calculate execution intensity
        heatmap.setExecutionGrid(calculateExecutionGrid(executionData, heatmap.getTimeGrid(), request));

        // Calculate success rate grid
        heatmap.setSuccessRateGrid(calculateSuccessRateGrid(executionData, heatmap.getTimeGrid(), request));

        // Calculate performance grid
        heatmap.setPerformanceGrid(calculatePerformanceGrid(executionData, heatmap.getTimeGrid(), request));

        // Identify hotspots
        heatmap.setHotspots(identifyHotspots(heatmap));

        // Calculate statistics
        heatmap.setStatistics(calculateHeatmapStatistics(executionData));

        return heatmap;
    }

    /**
     * Get flow execution patterns.
     */
    public FlowExecutionPatterns analyzeExecutionPatterns(String flowId, int days) {
        FlowExecutionPatterns patterns = new FlowExecutionPatterns();
        patterns.setFlowId(flowId);
        patterns.setAnalysisPeriodDays(days);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        List<FlowExecutionData> executions = collectFlowExecutionData(
            startTime, endTime, Collections.singletonList(flowId.toString()));

        // Analyze temporal patterns
        Map<Integer, ExecutionStats> hourlyStats = analyzeHourlyPattern(executions);
        Map<Integer, Long> hourlyPattern = new HashMap<>();
        hourlyStats.forEach((hour, stats) -> hourlyPattern.put(hour, stats.getCount()));
        patterns.setHourlyPattern(hourlyPattern);

        Map<String, ExecutionStats> dailyStats = analyzeDailyPattern(executions);
        Map<LocalDateTime, Long> dailyPattern = new HashMap<>();
        dailyStats.forEach((dateStr, stats) -> {
            try {
                LocalDateTime date = LocalDateTime.parse(dateStr);
                dailyPattern.put(date, stats.getCount());
            } catch (Exception e) {
                // Skip invalid dates
            }
        });
        patterns.setDailyPattern(dailyPattern);

        Map<Integer, ExecutionStats> weeklyStats = analyzeWeeklyPattern(executions);
        Map<String, Long> weeklyPattern = new HashMap<>();
        weeklyStats.forEach((dayOfWeek, stats) ->
            weeklyPattern.put(getDayOfWeekName(dayOfWeek), stats.getCount()));
        patterns.setWeeklyPattern(weeklyPattern);

        // Identify execution clusters - convert to expected format
        List<ExecutionCluster> clusters = identifyExecutionClusters(executions);
        List<Map<String, Object>> clusterMaps = new ArrayList<>();
        for (ExecutionCluster cluster : clusters) {
            Map<String, Object> clusterMap = new HashMap<>();
            clusterMap.put("startTime", cluster.getStartTime());
            clusterMap.put("endTime", cluster.getEndTime());
            clusterMap.put("count", cluster.getExecutionCount());
            clusterMap.put("intensity", cluster.getIntensity());
            clusterMaps.add(clusterMap);
        }
        patterns.setExecutionClusters(clusterMaps);

        // Calculate execution velocity - convert to expected format
        ExecutionVelocity velocity = calculateExecutionVelocity(executions);
        Map<String, Double> velocityMap = new HashMap<>();
        velocityMap.put("current", velocity.getCurrentRate());
        velocityMap.put("average", velocity.getAverageRate());
        velocityMap.put("trend", velocity.getTrendValue());
        patterns.setExecutionVelocity(velocityMap);

        // Find anomalous patterns - convert to expected format
        List<AnomalousPattern> anomalous = findAnomalousPatterns(executions);
        List<Map<String, Object>> anomalousMaps = new ArrayList<>();
        for (AnomalousPattern pattern : anomalous) {
            Map<String, Object> patternMap = new HashMap<>();
            patternMap.put("type", pattern.getType());
            patternMap.put("startTime", pattern.getStartTime());
            patternMap.put("endTime", pattern.getEndTime());
            patternMap.put("severity", pattern.getSeverity());
            patternMap.put("description", pattern.getDescription());
            anomalousMaps.add(patternMap);
        }
        patterns.setAnomalousPatterns(anomalousMaps);

        return patterns;
    }

    /**
     * Get component interaction heatmap.
     */
    public ComponentInteractionHeatmap getComponentInteractionHeatmap(
            LocalDateTime startTime, LocalDateTime endTime) {

        ComponentInteractionHeatmap heatmap = new ComponentInteractionHeatmap();
        heatmap.setStartTime(startTime);
        heatmap.setEndTime(endTime);

        // Collect component interaction data
        Map<String, Map<String, InteractionMetrics>> interactions =
            collectComponentInteractions(startTime, endTime);

        // Build interaction matrix
        heatmap.setInteractionMatrix(buildInteractionMatrix(interactions));

        // Calculate component centrality
        heatmap.setComponentCentrality(calculateComponentCentrality(interactions));

        // Identify critical paths
        List<CriticalPath> criticalPaths = identifyCriticalPaths(interactions);
        List<List<String>> criticalPathStrings = criticalPaths.stream()
            .map(path -> {
                List<String> pathStrings = new ArrayList<>();
                if (path.getFlowIds() != null) {
                    path.getFlowIds().forEach(id -> pathStrings.add(id.toString()));
                }
                return pathStrings;
            })
            .collect(Collectors.toList());
        heatmap.setCriticalPaths(criticalPathStrings);

        // Calculate interaction statistics
        heatmap.setStatistics(calculateInteractionStatistics(interactions));

        return heatmap;
    }

    /**
     * Get error propagation heatmap.
     */
    public ErrorPropagationHeatmap getErrorPropagationHeatmap(
            LocalDateTime startTime, LocalDateTime endTime) {

        ErrorPropagationHeatmap heatmap = new ErrorPropagationHeatmap();
        heatmap.setStartTime(startTime);
        heatmap.setEndTime(endTime);

        // Collect error data
        List<ErrorEvent> errorEvents = collectErrorEvents(startTime, endTime);

        // Build error propagation paths
        List<ErrorPropagationPath> propagationPaths = buildErrorPropagationPaths(errorEvents);
        List<List<String>> propagationPathStrings = propagationPaths.stream()
            .map(path -> {
                List<String> pathStrings = new ArrayList<>();
                if (path.getPath() != null) {
                    path.getPath().forEach(event -> {
                        if (event.getComponentId() != null) {
                            pathStrings.add(event.getComponentId());
                        }
                    });
                }
                return pathStrings;
            })
            .collect(Collectors.toList());
        heatmap.setPropagationPaths(propagationPathStrings);

        // Calculate error density grid
        heatmap.setErrorDensityGrid(calculateErrorDensityGrid(errorEvents, startTime, endTime));

        // Identify error hotspots
        List<ErrorHotspot> errorHotspots = identifyErrorHotspots(errorEvents);
        List<Map<String, Object>> errorHotspotMaps = errorHotspots.stream()
            .map(hotspot -> {
                Map<String, Object> hotspotMap = new HashMap<>();
                hotspotMap.put("location", hotspot.getLocation());
                hotspotMap.put("errorCount", hotspot.getErrorCount());
                hotspotMap.put("errorTypes", hotspot.getErrorTypes());
                hotspotMap.put("errorRate", hotspot.getErrorRate());
                return hotspotMap;
            })
            .collect(Collectors.toList());
        heatmap.setErrorHotspots(errorHotspotMaps);

        // Calculate error impact scores
        heatmap.setComponentErrorImpact(calculateErrorImpactScores(errorEvents));

        return heatmap;
    }

    /**
     * Collect flow execution data from logs.
     */
    private List<FlowExecutionData> collectFlowExecutionData(
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<String> flowIds) {

        List<FlowExecutionData> executionData = new ArrayList<>();

        // Query logs for flow executions
        List<SystemLog> logs = systemLogRepository.findFlowExecutionLogs(startTime, endTime, flowIds);

        // Group logs by correlation ID to reconstruct executions
        Map<String, List<SystemLog>> executionGroups = logs.stream()
            .filter(log -> log.getCorrelationId() != null)
            .collect(Collectors.groupingBy(SystemLog::getCorrelationId));

        // Convert to FlowExecutionData
        for(Map.Entry<String, List<SystemLog>> entry : executionGroups.entrySet()) {
            FlowExecutionData execution = buildFlowExecutionData(entry.getValue());
            if(execution != null) {
                executionData.add(execution);
            }
        }

        return executionData;
    }

    /**
     * Build flow execution data from logs.
     */
    private FlowExecutionData buildFlowExecutionData(List<SystemLog> logs) {
        if(logs.isEmpty()) return null;

        // Sort by timestamp
        logs.sort(Comparator.comparing(SystemLog::getTimestamp));

        FlowExecutionData data = new FlowExecutionData();
        data.setCorrelationId(logs.get(0).getCorrelationId());
        data.setStartTime(logs.get(0).getTimestamp());
        data.setEndTime(logs.get(logs.size() - 1).getTimestamp());
        data.setDuration(ChronoUnit.MILLIS.between(data.getStartTime(), data.getEndTime()));

        // Extract flow ID
        for(SystemLog log : logs) {
            String flowIdStr = extractFlowId(log.getMessage());
            if(flowIdStr != null) {
                try {
                    data.setFlowId(UUID.fromString(flowIdStr));
                } catch (IllegalArgumentException e) {
                    // Invalid UUID format, skip
                }
                break;
            }
        }

        // Determine success
        boolean hasError = logs.stream()
            .anyMatch(log -> log.getLevel() == SystemLog.LogLevel.ERROR);
        data.setSuccess(!hasError);

        // Count components involved
        long componentCount = logs.stream()
            .map(SystemLog::getSource)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        data.setComponentCount((int) componentCount);

        return data;
    }

    /**
     * Extract flow ID from log message.
     */
    private String extractFlowId(String message) {
        int index = message.indexOf("flow: ");
        if(index >= 0) {
            int endIndex = message.indexOf(" ", index + 6);
            if(endIndex < 0) endIndex = message.length();
            return message.substring(index + 6, endIndex);
        }
        return null;
    }

    /**
     * Generate time grid based on granularity.
     */
    private List<TimeSlot> generateTimeGrid(HeatmapRequest request) {
        List<TimeSlot> timeGrid = new ArrayList<>();

        LocalDateTime current = request.getStartTime();
        while(current.isBefore(request.getEndTime())) {
            TimeSlot slot = new TimeSlot();
            slot.setStartTime(current);

            switch(request.getGranularity() != null ? request.getGranularity().toUpperCase() : "HOUR") {
                case "MINUTE":
                    slot.setEndTime(current.plusMinutes(1));
                    break;
                case "HOUR":
                    slot.setEndTime(current.plusHours(1));
                    break;
                case "DAY":
                    slot.setEndTime(current.plusDays(1));
                    break;
                default:
                    slot.setEndTime(current.plusHours(1));
            }

            timeGrid.add(slot);
            current = slot.getEndTime();
        }

        return timeGrid;
    }

    /**
     * Calculate execution intensity grid.
     */
    private double[][] calculateExecutionGrid(List<FlowExecutionData> executions,
                                            List<TimeSlot> timeGrid,
                                            HeatmapRequest request) {

        // Determine grid dimensions
        int timeSlots = timeGrid.size();
        int flowCount = request.getFlowIds() != null ? request.getFlowIds().size() :
                       getUniqueFlowCount(executions);

        double[][] grid = new double[flowCount][timeSlots];

        // Map flow IDs to indices
        List<String> flowIds = request.getFlowIds() != null ?
                              request.getFlowIds().stream().map(UUID::toString).collect(Collectors.toList()) :
                              getUniqueFlowIds(executions);

        // Fill grid with execution counts
        for(int i = 0; i < flowIds.size(); i++) {
            String flowId = flowIds.get(i);

            for(int j = 0; j < timeSlots; j++) {
                TimeSlot slot = timeGrid.get(j);

                long count = executions.stream()
                    .filter(exec -> flowId.equals(exec.getFlowId()))
                    .filter(exec -> isInTimeSlot(exec, slot))
                    .count();

                grid[i][j] = count;
            }
        }

        return normalizeGrid(grid);
    }

    /**
     * Calculate success rate grid.
     */
    private double[][] calculateSuccessRateGrid(List<FlowExecutionData> executions,
                                              List<TimeSlot> timeGrid,
                                              HeatmapRequest request) {

        int timeSlots = timeGrid.size();
        int flowCount = request.getFlowIds() != null ? request.getFlowIds().size() :
                       getUniqueFlowCount(executions);

        double[][] grid = new double[flowCount][timeSlots];

        List<String> flowIds = request.getFlowIds() != null ?
                              request.getFlowIds().stream().map(UUID::toString).collect(Collectors.toList()) :
                              getUniqueFlowIds(executions);

        for(int i = 0; i < flowIds.size(); i++) {
            String flowId = flowIds.get(i);

            for(int j = 0; j < timeSlots; j++) {
                TimeSlot slot = timeGrid.get(j);

                List<FlowExecutionData> slotExecutions = executions.stream()
                    .filter(exec -> flowId.equals(exec.getFlowId()))
                    .filter(exec -> isInTimeSlot(exec, slot))
                    .collect(Collectors.toList());

                if(!slotExecutions.isEmpty()) {
                    long successCount = slotExecutions.stream()
                        .filter(FlowExecutionData::isSuccess)
                        .count();

                    grid[i][j] = (double) successCount / slotExecutions.size();
                } else {
                    grid[i][j] = -1; // No data
                }
            }
        }

        return grid;
    }

    /**
     * Calculate performance grid(average duration).
     */
    private double[][] calculatePerformanceGrid(List<FlowExecutionData> executions,
                                              List<TimeSlot> timeGrid,
                                              HeatmapRequest request) {

        int timeSlots = timeGrid.size();
        int flowCount = request.getFlowIds() != null ? request.getFlowIds().size() :
                       getUniqueFlowCount(executions);

        double[][] grid = new double[flowCount][timeSlots];

        List<String> flowIds = request.getFlowIds() != null ?
                              request.getFlowIds().stream().map(UUID::toString).collect(Collectors.toList()) :
                              getUniqueFlowIds(executions);

        for(int i = 0; i < flowIds.size(); i++) {
            String flowId = flowIds.get(i);

            for(int j = 0; j < timeSlots; j++) {
                TimeSlot slot = timeGrid.get(j);

                double avgDuration = executions.stream()
                    .filter(exec -> flowId.equals(exec.getFlowId()))
                    .filter(exec -> isInTimeSlot(exec, slot))
                    .mapToLong(FlowExecutionData::getDuration)
                    .average()
                    .orElse(-1);

                grid[i][j] = avgDuration;
            }
        }

        return normalizeGrid(grid);
    }

    /**
     * Check if execution is in time slot.
     */
    private boolean isInTimeSlot(FlowExecutionData execution, TimeSlot slot) {
        return !execution.getStartTime().isBefore(slot.getStartTime()) &&
               execution.getStartTime().isBefore(slot.getEndTime());
    }

    /**
     * Get unique flow count.
     */
    private int getUniqueFlowCount(List<FlowExecutionData> executions) {
        return(int) executions.stream()
            .map(FlowExecutionData::getFlowId)
            .filter(Objects::nonNull)
            .distinct()
            .count();
    }

    /**
     * Get unique flow IDs.
     */
    private List<String> getUniqueFlowIds(List<FlowExecutionData> executions) {
        return executions.stream()
            .map(FlowExecutionData::getFlowId)
            .filter(Objects::nonNull)
            .map(UUID::toString)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Normalize grid values to 0-1 range.
     */
    private double[][] normalizeGrid(double[][] grid) {
        double max = Arrays.stream(grid)
            .flatMapToDouble(Arrays::stream)
            .filter(v -> v >= 0)
            .max()
            .orElse(1.0);

        if(max == 0) return grid;

        double[][] normalized = new double[grid.length][grid[0].length];

        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] >= 0) {
                    normalized[i][j] = grid[i][j] / max;
                } else {
                    normalized[i][j] = -1; // Preserve no - data indicator
                }
            }
        }

        return normalized;
    }

    /**
     * Identify hotspots in the heatmap.
     */
    private List<ExecutionHotspot> identifyHotspots(FlowExecutionHeatmap heatmap) {
        List<ExecutionHotspot> hotspots = new ArrayList<>();

        double[][] grid = heatmap.getExecutionGrid();
        double threshold = 0.8; // Top 20% are considered hotspots

        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[i].length; j++) {
                if(grid[i][j] >= threshold) {
                    ExecutionHotspot hotspot = new ExecutionHotspot();
                    hotspot.setFlowIndex(i);
                    hotspot.setTimeSlotIndex(j);
                    hotspot.setIntensity(grid[i][j]);
                    hotspot.setTimeSlot(heatmap.getTimeGrid().get(j));

                    hotspots.add(hotspot);
                }
            }
        }

        return hotspots;
    }

    /**
     * Calculate heatmap statistics.
     */
    private HeatmapStatistics calculateHeatmapStatistics(List<FlowExecutionData> executions) {
        HeatmapStatistics stats = new HeatmapStatistics();

        stats.setTotalExecutions(executions.size());

        long successCount = executions.stream()
            .filter(FlowExecutionData::isSuccess)
            .count();
        stats.setOverallSuccessRate((double) successCount / executions.size());

        stats.setAverageDuration(executions.stream()
            .mapToLong(FlowExecutionData::getDuration)
            .average()
            .orElse(0));

        // Peak execution time
        Map<Integer, Long> hourlyCount = executions.stream()
            .collect(Collectors.groupingBy(
                exec -> exec.getStartTime().getHour(),
                Collectors.counting()
           ));

        Map.Entry<Integer, Long> peakHour = hourlyCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);

        if(peakHour != null) {
            stats.setPeakExecutionHour(peakHour.getKey());
        }

        return stats;
    }

    /**
     * Analyze hourly execution pattern.
     */
    private Map<Integer, ExecutionStats> analyzeHourlyPattern(List<FlowExecutionData> executions) {
        Map<Integer, List<FlowExecutionData>> hourlyGroups = executions.stream()
            .collect(Collectors.groupingBy(exec -> exec.getStartTime().getHour()));

        Map<Integer, ExecutionStats> pattern = new HashMap<>();

        for(Map.Entry<Integer, List<FlowExecutionData>> entry : hourlyGroups.entrySet()) {
            ExecutionStats stats = calculateExecutionStats(entry.getValue());
            pattern.put(entry.getKey(), stats);
        }

        return pattern;
    }

    /**
     * Analyze daily execution pattern.
     */
    private Map<String, ExecutionStats> analyzeDailyPattern(List<FlowExecutionData> executions) {
        Map<String, List<FlowExecutionData>> dailyGroups = executions.stream()
            .collect(Collectors.groupingBy(
                exec -> exec.getStartTime().getDayOfWeek().toString()
           ));

        Map<String, ExecutionStats> pattern = new HashMap<>();

        for(Map.Entry<String, List<FlowExecutionData>> entry : dailyGroups.entrySet()) {
            ExecutionStats stats = calculateExecutionStats(entry.getValue());
            pattern.put(entry.getKey(), stats);
        }

        return pattern;
    }

    /**
     * Analyze weekly execution pattern.
     */
    private Map<Integer, ExecutionStats> analyzeWeeklyPattern(List<FlowExecutionData> executions) {
        Map<Integer, List<FlowExecutionData>> weeklyGroups = executions.stream()
            .collect(Collectors.groupingBy(
                exec ->(int) ChronoUnit.WEEKS.between(
                    executions.get(0).getStartTime(),
                    exec.getStartTime()
               )
           ));

        Map<Integer, ExecutionStats> pattern = new HashMap<>();

        for(Map.Entry<Integer, List<FlowExecutionData>> entry : weeklyGroups.entrySet()) {
            ExecutionStats stats = calculateExecutionStats(entry.getValue());
            pattern.put(entry.getKey(), stats);
        }

        return pattern;
    }

    /**
     * Calculate execution statistics.
     */
    private ExecutionStats calculateExecutionStats(List<FlowExecutionData> executions) {
        ExecutionStats stats = new ExecutionStats();

        stats.setCount(executions.size());

        long successCount = executions.stream()
            .filter(FlowExecutionData::isSuccess)
            .count();
        stats.setSuccessRate((double) successCount / executions.size());

        stats.setAverageDuration(executions.stream()
            .mapToLong(FlowExecutionData::getDuration)
            .average()
            .orElse(0));

        return stats;
    }

    /**
     * Identify execution clusters.
     */
    private List<ExecutionCluster> identifyExecutionClusters(List<FlowExecutionData> executions) {
        List<ExecutionCluster> clusters = new ArrayList<>();

        // Simple time - based clustering
        List<FlowExecutionData> sorted = new ArrayList<>(executions);
        sorted.sort(Comparator.comparing(FlowExecutionData::getStartTime));

        ExecutionCluster currentCluster = null;
        long clusterThreshold = heatmapConfig.getExecutionClusterThresholdMillis();

        for(FlowExecutionData execution : sorted) {
            if(currentCluster == null ||
                ChronoUnit.MILLIS.between(currentCluster.getEndTime(), execution.getStartTime()) > clusterThreshold) {

                // Start new cluster
                currentCluster = new ExecutionCluster();
                currentCluster.setStartTime(execution.getStartTime());
                currentCluster.setExecutions(new ArrayList<>());
                clusters.add(currentCluster);
            }

            currentCluster.getExecutions().add(execution);
            currentCluster.setEndTime(execution.getEndTime());
        }

        // Calculate cluster statistics
        for(ExecutionCluster cluster : clusters) {
            cluster.setSize(cluster.getExecutions().size());
            cluster.setDuration(ChronoUnit.MILLIS.between(
                cluster.getStartTime(), cluster.getEndTime()));
        }

        return clusters;
    }

    /**
     * Calculate execution velocity.
     */
    private ExecutionVelocity calculateExecutionVelocity(List<FlowExecutionData> executions) {
        ExecutionVelocity velocity = new ExecutionVelocity();

        if(executions.size() < 2) {
            velocity.setCurrentRate(0);
            velocity.setTrend("INSUFFICIENT_DATA");
            return velocity;
        }

        // Group by day
        Map<LocalDateTime, Long> dailyCounts = executions.stream()
            .collect(Collectors.groupingBy(
                exec -> exec.getStartTime().toLocalDate().atStartOfDay(),
                Collectors.counting()
           ));

        // Calculate current rate(last 7 days)
        LocalDateTime now = LocalDateTime.now();
        double currentRate = dailyCounts.entrySet().stream()
            .filter(entry -> entry.getKey().isAfter(now.minusDays(7)))
            .mapToLong(Map.Entry::getValue)
            .average()
            .orElse(0);

        velocity.setCurrentRate(currentRate);

        // Calculate previous rate(previous 7 days)
        double previousRate = dailyCounts.entrySet().stream()
            .filter(entry -> entry.getKey().isAfter(now.minusDays(14)) &&
                           entry.getKey().isBefore(now.minusDays(7)))
            .mapToLong(Map.Entry::getValue)
            .average()
            .orElse(0);

        // Determine trend
        if(currentRate > previousRate * 1.1) {
            velocity.setTrend("INCREASING");
        } else if(currentRate < previousRate * 0.9) {
            velocity.setTrend("DECREASING");
        } else {
            velocity.setTrend("STABLE");
        }

        velocity.setChangePercentage((currentRate - previousRate) / previousRate * 100);

        return velocity;
    }

    /**
     * Find anomalous execution patterns.
     */
    private List<AnomalousPattern> findAnomalousPatterns(List<FlowExecutionData> executions) {
        List<AnomalousPattern> anomalies = new ArrayList<>();

        // Check for unusual execution times
        double avgDuration = executions.stream()
            .mapToLong(FlowExecutionData::getDuration)
            .average()
            .orElse(0);

        double stdDev = Math.sqrt(executions.stream()
            .mapToDouble(exec -> Math.pow(exec.getDuration() - avgDuration, 2))
            .average()
            .orElse(0));

        // Find executions with unusual duration
        for(FlowExecutionData execution : executions) {
            if(Math.abs(execution.getDuration() - avgDuration) > 3 * stdDev) {
                AnomalousPattern anomaly = new AnomalousPattern();
                anomaly.setType("UNUSUAL_DURATION");
                anomaly.setTimestamp(execution.getStartTime());
                anomaly.setValue(execution.getDuration());
                anomaly.setExpectedValue(avgDuration);
                anomaly.setDescription("Execution duration " +
                    (execution.getDuration() > avgDuration ? "above" : "below") +
                    " normal");

                anomalies.add(anomaly);
            }
        }

        // Check for unusual execution frequency
        Map<LocalDateTime, Long> hourlyCounts = executions.stream()
            .collect(Collectors.groupingBy(
                exec -> exec.getStartTime().truncatedTo(ChronoUnit.HOURS),
                Collectors.counting()
           ));

        double avgHourlyCount = hourlyCounts.values().stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);

        for(Map.Entry<LocalDateTime, Long> entry : hourlyCounts.entrySet()) {
            if(entry.getValue() > avgHourlyCount * 3) {
                AnomalousPattern anomaly = new AnomalousPattern();
                anomaly.setType("UNUSUAL_FREQUENCY");
                anomaly.setTimestamp(entry.getKey());
                anomaly.setValue(entry.getValue());
                anomaly.setExpectedValue(avgHourlyCount);
                anomaly.setDescription("Unusually high execution frequency");

                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * Collect component interactions from logs.
     */
    private Map<String, Map<String, InteractionMetrics>> collectComponentInteractions(
            LocalDateTime startTime, LocalDateTime endTime) {

        Map<String, Map<String, InteractionMetrics>> interactions = new ConcurrentHashMap<>();

        // Query logs with correlation IDs
        List<SystemLog> logs = systemLogRepository.findByTimestampBetweenAndCorrelationIdNotNull(
            startTime, endTime
        );

        // Group by correlation ID
        Map<String, List<SystemLog>> correlatedGroups = logs.stream()
            .collect(Collectors.groupingBy(SystemLog::getCorrelationId));

        // Analyze each correlated group for component interactions
        for(List<SystemLog> group : correlatedGroups.values()) {
            analyzeComponentInteractions(group, interactions);
        }

        return interactions;
    }

    /**
     * Analyze component interactions in a correlated log group.
     */
    private void analyzeComponentInteractions(List<SystemLog> logs,
                                            Map<String, Map<String, InteractionMetrics>> interactions) {

        // Sort by timestamp
        logs.sort(Comparator.comparing(SystemLog::getTimestamp));

        // Track component sequence
        for(int i = 0; i < logs.size() - 1; i++) {
            String fromComponent = logs.get(i).getSource();
            String toComponent = logs.get(i + 1).getSource();

            if(fromComponent != null && toComponent != null && !fromComponent.equals(toComponent)) {
                InteractionMetrics metrics = interactions
                    .computeIfAbsent(fromComponent, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(toComponent, k -> new InteractionMetrics());

                metrics.incrementCount();

                long latency = ChronoUnit.MILLIS.between(
                    logs.get(i).getTimestamp(),
                    logs.get(i + 1).getTimestamp()
               );
                metrics.addLatency(latency);

                // Check for errors
                if(logs.get(i + 1).getLevel() == SystemLog.LogLevel.ERROR) {
                    metrics.incrementErrorCount();
                }
            }
        }
    }

    /**
     * Build interaction matrix.
     */
    private double[][] buildInteractionMatrix(Map<String, Map<String, InteractionMetrics>> interactions) {
        List<String> components = new ArrayList<>(interactions.keySet());
        components.sort(String::compareTo);

        int size = components.size();
        double[][] matrix = new double[size][size];

        for(int i = 0; i < size; i++) {
            Map<String, InteractionMetrics> fromInteractions = interactions.get(components.get(i));
            if(fromInteractions != null) {
                for(int j = 0; j < size; j++) {
                    InteractionMetrics metrics = fromInteractions.get(components.get(j));
                    if(metrics != null) {
                        matrix[i][j] = metrics.getCount();
                    }
                }
            }
        }

        return normalizeGrid(matrix);
    }

    /**
     * Calculate component centrality scores.
     */
    private Map<String, Double> calculateComponentCentrality(
            Map<String, Map<String, InteractionMetrics>> interactions) {

        Map<String, Double> centrality = new HashMap<>();

        // Calculate degree centrality(incoming + outgoing connections)
        for(String component : interactions.keySet()) {
            double outDegree = interactions.get(component).size();
            double inDegree = interactions.values().stream()
                .filter(map -> map.containsKey(component))
                .count();

            centrality.put(component, (outDegree + inDegree) / (interactions.size() - 1));
        }

        return centrality;
    }

    /**
     * Identify critical paths between components.
     */
    private List<CriticalPath> identifyCriticalPaths(
            Map<String, Map<String, InteractionMetrics>> interactions) {

        List<CriticalPath> criticalPaths = new ArrayList<>();

        // Find paths with high traffic or high error rates
        for(Map.Entry<String, Map<String, InteractionMetrics>> fromEntry : interactions.entrySet()) {
            for(Map.Entry<String, InteractionMetrics> toEntry : fromEntry.getValue().entrySet()) {
                InteractionMetrics metrics = toEntry.getValue();

                // Check if this is a critical path
                if(metrics.getCount() > heatmapConfig.getCriticalPathMinTrafficVolume() ||
                   metrics.getErrorRate() > heatmapConfig.getCriticalPathMaxErrorRate()) {
                    CriticalPath path = new CriticalPath();
                    path.setFromComponent(fromEntry.getKey());
                    path.setToComponent(toEntry.getKey());
                    path.setTrafficVolume(metrics.getCount());
                    path.setAverageLatency(metrics.getAverageLatency());
                    path.setErrorRate(metrics.getErrorRate());

                    criticalPaths.add(path);
                }
            }
        }

        // Sort by criticality
        criticalPaths.sort((a, b) ->
            Double.compare(b.getTrafficVolume() * (1 + b.getErrorRate()),
                          a.getTrafficVolume() * (1 + a.getErrorRate())));

        return criticalPaths;
    }

    /**
     * Calculate interaction statistics.
     */
    private Map<String, Object> calculateInteractionStatistics(
            Map<String, Map<String, InteractionMetrics>> interactions) {

        Map<String, Object> stats = new HashMap<>();

        // Total interactions
        long totalInteractions = interactions.values().stream()
            .flatMap(map -> map.values().stream())
            .mapToLong(InteractionMetrics::getCount)
            .sum();
        stats.put("totalInteractions", totalInteractions);

        // Average latency
        double avgLatency = interactions.values().stream()
            .flatMap(map -> map.values().stream())
            .mapToDouble(InteractionMetrics::getAverageLatency)
            .average()
            .orElse(0);
        stats.put("averageLatency", avgLatency);

        // Component count
        stats.put("componentCount", interactions.size());

        return stats;
    }

    /**
     * Collect error events.
     */
    private List<ErrorEvent> collectErrorEvents(LocalDateTime startTime, LocalDateTime endTime) {
        List<SystemLog> errorLogs = systemLogRepository.findByLevelAndTimestampBetween(
            SystemLog.LogLevel.ERROR, startTime, endTime
        );

        return errorLogs.stream()
            .map(this::convertToErrorEvent)
            .collect(Collectors.toList());
    }

    /**
     * Convert log to error event.
     */
    private ErrorEvent convertToErrorEvent(SystemLog log) {
        ErrorEvent event = new ErrorEvent();
        event.setTimestamp(log.getTimestamp());
        event.setComponentId(log.getSource());
        event.setErrorType(classifyErrorType(log.getMessage()));
        event.setErrorMessage(log.getMessage());
        event.setCorrelationId(log.getCorrelationId());
        return event;
    }

    /**
     * Classify error type from message.
     */
    private String classifyErrorType(String message) {
        if(message.contains("timeout")) return "TIMEOUT";
        if(message.contains("connection")) return "CONNECTION";
        if(message.contains("authentication")) return "AUTH";
        if(message.contains("validation")) return "VALIDATION";
        if(message.contains("null")) return "NULL_POINTER";
        return "GENERAL";
    }

    /**
     * Build error propagation paths.
     */
    private List<ErrorPropagationPath> buildErrorPropagationPaths(List<ErrorEvent> errorEvents) {
        List<ErrorPropagationPath> paths = new ArrayList<>();

        // Group by correlation ID
        Map<String, List<ErrorEvent>> correlatedErrors = errorEvents.stream()
            .filter(event -> event.getCorrelationId() != null)
            .collect(Collectors.groupingBy(ErrorEvent::getCorrelationId));

        // Build propagation paths
        for(List<ErrorEvent> correlatedGroup : correlatedErrors.values()) {
            if(correlatedGroup.size() > 1) {
                ErrorPropagationPath path = new ErrorPropagationPath();
                path.setCorrelationId(correlatedGroup.get(0).getCorrelationId());

                // Sort by timestamp
                correlatedGroup.sort(Comparator.comparing(ErrorEvent::getTimestamp));

                path.setOriginComponent(correlatedGroup.get(0).getComponentId());
                path.setErrorSequence(correlatedGroup);
                path.setPropagationTime(ChronoUnit.MILLIS.between(
                    correlatedGroup.get(0).getTimestamp(),
                    correlatedGroup.get(correlatedGroup.size() - 1).getTimestamp()
               ));

                paths.add(path);
            }
        }

        return paths;
    }

    /**
     * Calculate error density grid.
     */
    private double[][] calculateErrorDensityGrid(List<ErrorEvent> errorEvents,
                                               LocalDateTime startTime,
                                               LocalDateTime endTime) {

        // Create hourly grid for the time range
        long hours = ChronoUnit.HOURS.between(startTime, endTime);
        List<String> components = errorEvents.stream()
            .map(ErrorEvent::getComponentId)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        double[][] grid = new double[components.size()][(int) hours];

        // Fill grid with error counts
        for(int i = 0; i < components.size(); i++) {
            String component = components.get(i);

            for(ErrorEvent event : errorEvents) {
                if(component.equals(event.getComponentId())) {
                    int hourIndex = (int) ChronoUnit.HOURS.between(startTime, event.getTimestamp());
                    if(hourIndex >= 0 && hourIndex < hours) {
                        grid[i][hourIndex]++;
                    }
                }
            }
        }

        return normalizeGrid(grid);
    }

    /**
     * Identify error hotspots.
     */
    private List<ErrorHotspot> identifyErrorHotspots(List<ErrorEvent> errorEvents) {
        List<ErrorHotspot> hotspots = new ArrayList<>();

        // Group errors by component and hour
        Map<String, Map<LocalDateTime, Long>> componentHourlyErrors = new HashMap<>();

        for(ErrorEvent event : errorEvents) {
            if(event.getComponentId() != null) {
                LocalDateTime hour = event.getTimestamp().truncatedTo(ChronoUnit.HOURS);

                componentHourlyErrors
                    .computeIfAbsent(event.getComponentId(), k -> new HashMap<>())
                    .merge(hour, 1L, Long::sum);
            }
        }

        // Find hotspots(high error concentration)
        for(Map.Entry<String, Map<LocalDateTime, Long>> componentEntry : componentHourlyErrors.entrySet()) {
            for(Map.Entry<LocalDateTime, Long> hourEntry : componentEntry.getValue().entrySet()) {
                if(hourEntry.getValue() > heatmapConfig.getErrorHotspotThreshold()) {
                    ErrorHotspot hotspot = new ErrorHotspot();
                    hotspot.setComponent(componentEntry.getKey());
                    hotspot.setTimestamp(hourEntry.getKey());
                    hotspot.setErrorCount(hourEntry.getValue());

                    // Calculate error types
                    Map<String, Long> errorTypes = errorEvents.stream()
                        .filter(e -> componentEntry.getKey().equals(e.getComponentId()))
                        .filter(e -> e.getTimestamp().truncatedTo(ChronoUnit.HOURS).equals(hourEntry.getKey()))
                        .collect(Collectors.groupingBy(ErrorEvent::getErrorType, Collectors.counting()));

                    hotspot.setErrorTypes(errorTypes);
                    hotspots.add(hotspot);
                }
            }
        }

        return hotspots;
    }

    /**
     * Calculate error impact scores.
     */
    private Map<String, Double> calculateErrorImpactScores(List<ErrorEvent> errorEvents) {
        Map<String, Double> impactScores = new HashMap<>();

        // Count errors by component
        Map<String, Long> errorCounts = errorEvents.stream()
            .filter(e -> e.getComponentId() != null)
            .collect(Collectors.groupingBy(ErrorEvent::getComponentId, Collectors.counting()));

        // Count propagated errors(errors that appear in correlation chains)
        Map<String, Long> propagatedCounts = new HashMap<>();

        Map<String, List<ErrorEvent>> correlatedErrors = errorEvents.stream()
            .filter(e -> e.getCorrelationId() != null)
            .collect(Collectors.groupingBy(ErrorEvent::getCorrelationId));

        for(List<ErrorEvent> chain : correlatedErrors.values()) {
            if(chain.size() > 1) {
                for(ErrorEvent event : chain) {
                    propagatedCounts.merge(event.getComponentId(), 1L, Long::sum);
                }
            }
        }

        // Calculate impact scores
        for(String component : errorCounts.keySet()) {
            long errors = errorCounts.get(component);
            long propagated = propagatedCounts.getOrDefault(component, 0L);

            // Impact score = errors + (propagated errors * 2)
            double impactScore = errors + (propagated * 2.0);
            impactScores.put(component, impactScore);
        }

        return impactScores;
    }

    /**
     * Convert day of week number to name.
     */
    private String getDayOfWeekName(int dayOfWeek) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return (dayOfWeek >= 1 && dayOfWeek <= 7) ? days[dayOfWeek - 1] : "Unknown";
    }
}
