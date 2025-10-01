package com.integrixs.backend.service;

import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.shared.dto.log.LogSearchCriteria;
import com.integrixs.shared.dto.log.LogSearchResult;
import com.integrixs.shared.dto.system.SystemLogDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced log search service with advanced search capabilities.
 */
@Service
public class LogSearchService {

    private static final Logger log = LoggerFactory.getLogger(LogSearchService.class);

    private final SystemLogSqlRepository systemLogRepository;

    public LogSearchService(SystemLogSqlRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Search logs with advanced criteria.
     */
    public LogSearchResult searchLogs(LogSearchCriteria criteria) {
        log.debug("Searching logs with criteria: {}", criteria);

        // Build pageable
        Pageable pageable = buildPageable(criteria);

        // Execute search using the enhanced repository method
        Page<SystemLog> logsPage = systemLogRepository.searchLogs(criteria, pageable);

        // Build search result
        LogSearchResult result = new LogSearchResult();
        result.setLogs(convertToSystemLogDTOs(logsPage.getContent()));
        result.setTotalElements(logsPage.getTotalElements());
        result.setTotalPages(logsPage.getTotalPages());
        result.setCurrentPage(logsPage.getNumber());
        result.setPageSize(logsPage.getSize());

        // Add facets if requested
        if (criteria.isIncludeFacets()) {
            result.setFacets(calculateFacets(criteria));
        }

        // Add highlights if text search was used
        if (criteria.getSearchText() != null && !criteria.getSearchText().isEmpty()) {
            result.setHighlights(generateHighlights(logsPage.getContent(), criteria.getSearchText()));
        }

        return result;
    }

    /**
     * Search logs by correlation ID with timeline view.
     */
    public List<SystemLog> getCorrelatedLogs(String correlationId) {
        return systemLogRepository.findByCorrelationId(correlationId);
    }

    /**
     * Get logs for a specific flow execution.
     */
    public List<SystemLog> getFlowExecutionLogs(String flowId, LocalDateTime startTime, LocalDateTime endTime) {
        return systemLogRepository.findByFlowIdAndTimestampBetween(flowId, startTime, endTime);
    }

    /**
     * Build SQL query from search criteria.
     */


    /**
     * Build pageable from search criteria.
     */
    private Pageable buildPageable(LogSearchCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp"); // Default sort

        if(criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
            Sort.Direction direction = criteria.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, criteria.getSortBy());
        }

        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getPageSize() != null ? criteria.getPageSize() : 50;

        return PageRequest.of(page, size, sort);
    }

    /**
     * Calculate facets for search results.
     */
    private Map<String, Map<String, Long>> calculateFacets(LogSearchCriteria criteria) {
        Map<String, Map<String, Long>> facets = new HashMap<>();

        // Level facets
        Map<String, Long> levelFacets = systemLogRepository.calculateLevelFacets(criteria);
        facets.put("levels", levelFacets);

        // Source facets (top 10)
        Map<String, Long> sourceFacets = systemLogRepository.calculateSourceFacets(criteria);
        facets.put("sources", sourceFacets);

        // Category facets (top 10)
        Map<String, Long> categoryFacets = systemLogRepository.calculateCategoryFacets(criteria);
        facets.put("categories", categoryFacets);

        return facets;
    }

    /**
     * Generate search highlights.
     */
    private Map<String, List<String>> generateHighlights(List<SystemLog> logs, String searchText) {
        Map<String, List<String>> highlights = new HashMap<>();
        Pattern pattern = Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE);

        for(SystemLog log : logs) {
            List<String> logHighlights = new ArrayList<>();

            // Check message
            if(log.getMessage() != null && pattern.matcher(log.getMessage()).find()) {
                logHighlights.add(highlightText(log.getMessage(), searchText));
            }

            // Check details
            if(log.getDetails() != null && pattern.matcher(log.getDetails()).find()) {
                logHighlights.add(highlightText(log.getDetails(), searchText));
            }

            if(!logHighlights.isEmpty()) {
                highlights.put(log.getId().toString(), logHighlights);
            }
        }

        return highlights;
    }

    /**
     * Highlight search text in content.
     */
    private String highlightText(String content, String searchText) {
        // Simple highlight with <mark> tags
        return content.replaceAll("(?i)" + Pattern.quote(searchText),
            "<mark>$0</mark>");
    }

    /**
     * Export logs to various formats.
     */
    public byte[] exportLogs(LogSearchCriteria criteria, String format) {
        LogSearchResult searchResult = searchLogs(criteria);

        switch(format.toLowerCase()) {
            case "csv":
                return exportToCsv(convertToSystemLogs(searchResult.getLogs()));
            case "json":
                return exportToJson(convertToSystemLogs(searchResult.getLogs()));
            case "text":
                return exportToText(convertToSystemLogs(searchResult.getLogs()));
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    /**
     * Export logs to CSV format.
     */
    private byte[] exportToCsv(List<SystemLog> logs) {
        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,Level,Source,Category,Message,User,Correlation ID\\n");

        for(SystemLog log : logs) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\\n",
                log.getTimestamp(),
                log.getLevel(),
                log.getSource() != null ? log.getSource() : "",
                log.getCategory() != null ? log.getCategory() : "",
                log.getMessage().replace("\"", "\"\""),
                log.getUsername() != null ? log.getUsername() : "",
                log.getCorrelationId() != null ? log.getCorrelationId() : ""
           ));
        }

        return csv.toString().getBytes();
    }

    /**
     * Export logs to JSON format.
     */
    private byte[] exportToJson(List<SystemLog> logs) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsBytes(logs);
        } catch(Exception e) {
            log.error("Failed to export logs to JSON", e);
            return "[]".getBytes();
        }
    }

    /**
     * Export logs to plain text format.
     */
    private byte[] exportToText(List<SystemLog> logs) {
        StringBuilder text = new StringBuilder();

        for(SystemLog log : logs) {
            text.append(String.format("[%s] %s - %s - %s\\n",
                log.getTimestamp(),
                log.getLevel(),
                log.getSource(),
                log.getMessage()
           ));

            if(log.getStackTrace() != null && !log.getStackTrace().isEmpty()) {
                text.append("Stack trace:\\n").append(log.getStackTrace()).append("\\n");
            }

            text.append("\\n");
        }

        return text.toString().getBytes();
    }

    /**
     * Convert SystemLog entities to SystemLogDTOs
     */
    private List<SystemLogDTO> convertToSystemLogDTOs(List<SystemLog> logs) {
        List<SystemLogDTO> dtos = new ArrayList<>();
        for (SystemLog log : logs) {
            SystemLogDTO dto = new SystemLogDTO();
            dto.setId(log.getId() != null ? log.getId().toString() : null);
            dto.setTimestamp(log.getCreatedAt());
            dto.setLevel(log.getLevel() != null ? log.getLevel().name() : null);
            dto.setMessage(log.getMessage());
            dto.setDetails(log.getDetails());
            dto.setSource(log.getSource());
            dto.setSourceId(log.getSourceId());
            dto.setSourceName(log.getSourceName());
            dto.setComponent(log.getComponent());
            dto.setComponentId(log.getComponentId());
            dto.setDomainType(log.getDomainType());
            dto.setDomainReferenceId(log.getDomainReferenceId());
            dto.setUserId(log.getUserId() != null ? log.getUserId().toString() : null);
            dto.setCreatedAt(log.getCreatedAt());
            dto.setCorrelationId(log.getCorrelationId());
            dto.setClientIp(log.getIpAddress());

            // Note: SystemLog doesn't have context data field, leaving context empty

            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Convert SystemLogDTOs to SystemLog entities
     */
    private List<SystemLog> convertToSystemLogs(List<SystemLogDTO> dtos) {
        List<SystemLog> logs = new ArrayList<>();
        for (SystemLogDTO dto : dtos) {
            SystemLog log = new SystemLog();
            if (dto.getId() != null) {
                log.setId(UUID.fromString(dto.getId()));
            }
            log.setCreatedAt(dto.getTimestamp());
            if (dto.getLevel() != null) {
                log.setLevel(SystemLog.LogLevel.valueOf(dto.getLevel()));
            }
            log.setMessage(dto.getMessage());
            log.setDetails(dto.getDetails());
            log.setSource(dto.getSource());
            log.setSourceId(dto.getSourceId());
            log.setSourceName(dto.getSourceName());
            log.setComponent(dto.getComponent());
            log.setComponentId(dto.getComponentId());
            log.setDomainType(dto.getDomainType());
            log.setDomainReferenceId(dto.getDomainReferenceId());
            if (dto.getUserId() != null) {
                log.setUserId(UUID.fromString(dto.getUserId()));
            }
            log.setCorrelationId(dto.getCorrelationId());
            log.setIpAddress(dto.getClientIp());

            // Note: SystemLog doesn't have context data field

            logs.add(log);
        }
        return logs;
    }
}
