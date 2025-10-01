package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;

public class TimeSlot {
    private LocalDateTime start;
    private LocalDateTime end;
    private String label;
    private int dayOfWeek;
    private int hour;

    // Default constructor
    public TimeSlot() {
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    // Alias methods for compatibility with services expecting different names
    public LocalDateTime getStartTime() {
        return start;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.start = startTime;
    }

    public LocalDateTime getEndTime() {
        return end;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.end = endTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }
}