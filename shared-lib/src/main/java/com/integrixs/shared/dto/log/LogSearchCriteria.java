package com.integrixs.shared.dto.log;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Search criteria for log queries.
 */
public class LogSearchCriteria {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> levels;
    private List<String> sources;
    private List<String> categories;
    private List<String> componentIds;
    private List<String> userIds;
    private String correlationId;
    private String ipAddress;
    private String searchText;
    private String regexPattern;
    private List<String> excludePatterns;
    private Integer page;
    private Integer pageSize;
    private String sortBy;
    private boolean ascending;
    private boolean includeFacets;
    private boolean includeStackTraces;

    // Default constructor
    public LogSearchCriteria() {
        this.levels = new ArrayList<>();
        this.sources = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.componentIds = new ArrayList<>();
        this.userIds = new ArrayList<>();
        this.excludePatterns = new ArrayList<>();
    }

    // All args constructor
    public LogSearchCriteria(LocalDateTime startTime, LocalDateTime endTime, List<String> levels, List<String> sources, List<String> categories, List<String> componentIds, List<String> userIds, String correlationId, String ipAddress, String searchText, String regexPattern, List<String> excludePatterns, Integer page, Integer pageSize, String sortBy, boolean ascending, boolean includeFacets, boolean includeStackTraces) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.levels = levels != null ? levels : new ArrayList<>();
        this.sources = sources != null ? sources : new ArrayList<>();
        this.categories = categories != null ? categories : new ArrayList<>();
        this.componentIds = componentIds != null ? componentIds : new ArrayList<>();
        this.userIds = userIds != null ? userIds : new ArrayList<>();
        this.correlationId = correlationId;
        this.ipAddress = ipAddress;
        this.searchText = searchText;
        this.regexPattern = regexPattern;
        this.excludePatterns = excludePatterns != null ? excludePatterns : new ArrayList<>();
        this.page = page;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.ascending = ascending;
        this.includeFacets = includeFacets;
        this.includeStackTraces = includeStackTraces;
    }

    // Getters
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<String> getLevels() { return levels; }
    public List<String> getSources() { return sources; }
    public List<String> getCategories() { return categories; }
    public List<String> getComponentIds() { return componentIds; }
    public List<String> getUserIds() { return userIds; }
    public String getCorrelationId() { return correlationId; }
    public String getIpAddress() { return ipAddress; }
    public String getSearchText() { return searchText; }
    public String getRegexPattern() { return regexPattern; }
    public List<String> getExcludePatterns() { return excludePatterns; }
    public Integer getPage() { return page; }
    public Integer getPageSize() { return pageSize; }
    public String getSortBy() { return sortBy; }
    public boolean isAscending() { return ascending; }
    public boolean isIncludeFacets() { return includeFacets; }
    public boolean isIncludeStackTraces() { return includeStackTraces; }

    // Setters
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setLevels(List<String> levels) { this.levels = levels; }
    public void setSources(List<String> sources) { this.sources = sources; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public void setComponentIds(List<String> componentIds) { this.componentIds = componentIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setSearchText(String searchText) { this.searchText = searchText; }
    public void setRegexPattern(String regexPattern) { this.regexPattern = regexPattern; }
    public void setExcludePatterns(List<String> excludePatterns) { this.excludePatterns = excludePatterns; }
    public void setPage(Integer page) { this.page = page; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public void setAscending(boolean ascending) { this.ascending = ascending; }
    public void setIncludeFacets(boolean includeFacets) { this.includeFacets = includeFacets; }
    public void setIncludeStackTraces(boolean includeStackTraces) { this.includeStackTraces = includeStackTraces; }
}
