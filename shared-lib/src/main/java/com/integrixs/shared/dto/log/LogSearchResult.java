package com.integrixs.shared.dto.log;

import com.integrixs.shared.dto.system.SystemLogDTO;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Result of a log search operation.
 */
public class LogSearchResult {

    private List<SystemLogDTO> logs;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private Map<String, Map<String, Long>> facets;
    private Map<String, List<String>> highlights;
    private long searchTimeMs;
    private String searchQuery;

    // Default constructor
    public LogSearchResult() {
        this.logs = new ArrayList<>();
        this.facets = new HashMap<>();
        this.highlights = new HashMap<>();
    }

    // All args constructor
    public LogSearchResult(List<SystemLogDTO> logs, long totalElements, int totalPages, int currentPage, int pageSize, Map<String, Map<String, Long>> facets, Map<String, List<String>> highlights, long searchTimeMs, String searchQuery) {
        this.logs = logs != null ? logs : new ArrayList<>();
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.facets = facets != null ? facets : new HashMap<>();
        this.highlights = highlights != null ? highlights : new HashMap<>();
        this.searchTimeMs = searchTimeMs;
        this.searchQuery = searchQuery;
    }

    // Getters
    public List<SystemLogDTO> getLogs() { return logs; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
    public Map<String, Map<String, Long>> getFacets() { return facets; }
    public Map<String, List<String>> getHighlights() { return highlights; }
    public long getSearchTimeMs() { return searchTimeMs; }
    public String getSearchQuery() { return searchQuery; }

    // Setters
    public void setLogs(List<SystemLogDTO> logs) { this.logs = logs; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public void setFacets(Map<String, Map<String, Long>> facets) { this.facets = facets; }
    public void setHighlights(Map<String, List<String>> highlights) { this.highlights = highlights; }
    public void setSearchTimeMs(long searchTimeMs) { this.searchTimeMs = searchTimeMs; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
}
