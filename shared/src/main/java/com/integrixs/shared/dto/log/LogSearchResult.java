package com.integrixs.shared.dto.log;

import com.integrixs.data.model.SystemLog;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Result of a log search operation.
 */
@Data
public class LogSearchResult {
    
    // Search results
    private List<SystemLog> logs;
    
    // Pagination info
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    // Facets (aggregations)
    private Map<String, Map<String, Long>> facets;
    
    // Search highlights
    private Map<String, List<String>> highlights;
    
    // Search metadata
    private long searchTimeMs;
    private String searchQuery;
}