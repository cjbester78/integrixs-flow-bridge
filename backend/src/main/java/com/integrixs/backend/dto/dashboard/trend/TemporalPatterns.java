package com.integrixs.backend.dto.dashboard.trend;

import java.util.List;
import java.util.Map;

/**
 * Temporal patterns analysis(hourly, daily, weekly).
 */
public class TemporalPatterns {
    private String metric;
    private int analysisPeriodDays;
    private Map<Integer, HourlyStats> hourlyPatterns;
    private Map<String, DailyStats> dailyPatterns;
    private List<PeakPeriod> peakPeriods;
    private double seasonalityScore; // 0.0 to 1.0

    // Default constructor
    public TemporalPatterns() {
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public int getAnalysisPeriodDays() {
        return analysisPeriodDays;
    }

    public void setAnalysisPeriodDays(int analysisPeriodDays) {
        this.analysisPeriodDays = analysisPeriodDays;
    }

    public List<PeakPeriod> getPeakPeriods() {
        return peakPeriods;
    }

    public void setPeakPeriods(List<PeakPeriod> peakPeriods) {
        this.peakPeriods = peakPeriods;
    }

    public double getSeasonalityScore() {
        return seasonalityScore;
    }

    public void setSeasonalityScore(double seasonalityScore) {
        this.seasonalityScore = seasonalityScore;
    }

    public Map<Integer, HourlyStats> getHourlyPatterns() {
        return hourlyPatterns;
    }

    public void setHourlyPatterns(Map<Integer, HourlyStats> hourlyPatterns) {
        this.hourlyPatterns = hourlyPatterns;
    }

    public Map<String, DailyStats> getDailyPatterns() {
        return dailyPatterns;
    }

    public void setDailyPatterns(Map<String, DailyStats> dailyPatterns) {
        this.dailyPatterns = dailyPatterns;
    }
}
