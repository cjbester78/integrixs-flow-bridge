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
    private List<String> adapterIds;
    private java.time.LocalDateTime timestamp;
    private Map<String, AdapterHealthScore> healthScores;
    private Map<String, PerformanceComparison> performanceComparisons;
    private List<String> ranking;
    private String bestPerformer;
    private String worstPerformer;

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

    public Map<String, Double> getAverageHealthScores() {
        return averageHealthScores;
    }

    public void setAverageHealthScores(Map<String, Double> averageHealthScores) {
        this.averageHealthScores = averageHealthScores;
    }

    public Map<String, Long> getTotalMessagesProcessed() {
        return totalMessagesProcessed;
    }

    public void setTotalMessagesProcessed(Map<String, Long> totalMessagesProcessed) {
        this.totalMessagesProcessed = totalMessagesProcessed;
    }

    public Map<String, Long> getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(Map<String, Long> totalErrors) {
        this.totalErrors = totalErrors;
    }

    public Map<String, Double> getErrorRates() {
        return errorRates;
    }

    public void setErrorRates(Map<String, Double> errorRates) {
        this.errorRates = errorRates;
    }

    public Map<String, Long> getAverageResponseTimes() {
        return averageResponseTimes;
    }

    public void setAverageResponseTimes(Map<String, Long> averageResponseTimes) {
        this.averageResponseTimes = averageResponseTimes;
    }

    public Map<String, List<String>> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(Map<String, List<String>> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getAdapterIds() {
        return adapterIds;
    }

    public void setAdapterIds(List<String> adapterIds) {
        this.adapterIds = adapterIds;
    }

    public java.time.LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(java.time.LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, AdapterHealthScore> getHealthScores() {
        return healthScores;
    }

    public void setHealthScores(Map<String, AdapterHealthScore> healthScores) {
        this.healthScores = healthScores;
    }

    public Map<String, PerformanceComparison> getPerformanceComparisons() {
        return performanceComparisons;
    }

    public void setPerformanceComparisons(Map<String, PerformanceComparison> performanceComparisons) {
        this.performanceComparisons = performanceComparisons;
    }

    public List<String> getRanking() {
        return ranking;
    }

    public void setRanking(List<String> ranking) {
        this.ranking = ranking;
    }

    public String getBestPerformer() {
        return bestPerformer;
    }

    public void setBestPerformer(String bestPerformer) {
        this.bestPerformer = bestPerformer;
    }

    public String getWorstPerformer() {
        return worstPerformer;
    }

    public void setWorstPerformer(String worstPerformer) {
        this.worstPerformer = worstPerformer;
    }
}