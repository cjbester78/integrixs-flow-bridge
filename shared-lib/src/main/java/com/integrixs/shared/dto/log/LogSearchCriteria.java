package com.integrixs.shared.dto.log;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Search criteria for log queries.
 */
@Data
public class LogSearchCriteria {

    // Time range
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Filters
    private List<String> levels;
    private List<String> sources;
    private List<String> categories;
    private List<String> componentIds;
    private List<String> userIds;
    private String correlationId;
    private String ipAddress;

    // Text search
    private String searchText;
    private String regexPattern;

    // Exclusions
    private List<String> excludePatterns;

    // Pagination
    private Integer page;
    private Integer pageSize;

    // Sorting
    private String sortBy;
    private boolean ascending;

    // Options
    private boolean includeFacets;
    private boolean includeStackTraces;
}
