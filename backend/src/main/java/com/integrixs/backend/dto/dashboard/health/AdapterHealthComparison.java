package com.integrixs.backend.dto.dashboard.health;

import java.util.List;
import java.util.Map;

/**
 * DTO for comparing health metrics across multiple adapters
 */
public class AdapterHealthComparison {
    private String comparisonPeriod;
    private List<AdapterHealthSummary> adapters;
    private Map<String, Double> averageHealthScores;
    private Map<String, Long> totalMessagesProcessed;
    private Map<String, Long> totalErrors;
    private Map<String, Double> errorRates;
    private Map<String, Long> averageResponseTimes;
    private String bestPerformingAdapter;
    private String worstPerformingAdapter;
    private List<String> criticalAdapters;
    private Map<String, List<String>> recommendations;

    // Default constructor
    public AdapterHealthComparison() {
    }

    public String getComparisonPeriod() {
        return comparisonPeriod;
    }

    public void setComparisonPeriod(String comparisonPeriod) {
        this.comparisonPeriod = comparisonPeriod;
    }

    public List<AdapterHealthSummary> getAdapters() {
        return adapters;
    }

    public void setAdapters(List<AdapterHealthSummary> adapters) {
        this.adapters = adapters;
    }

    public String getBestPerformingAdapter() {
        return bestPerformingAdapter;
    }

    public void setBestPerformingAdapter(String bestPerformingAdapter) {
        this.bestPerformingAdapter = bestPerformingAdapter;
    }

    public String getWorstPerformingAdapter() {
        return worstPerformingAdapter;
    }

    public void setWorstPerformingAdapter(String worstPerformingAdapter) {
        this.worstPerformingAdapter = worstPerformingAdapter;
    }

    public List<String> getCriticalAdapters() {
        return criticalAdapters;
    }

    public void setCriticalAdapters(List<String> criticalAdapters) {
        this.criticalAdapters = criticalAdapters;
    }
}