package com.integrixs.backend.service;

import com.integrixs.data.model.SystemLog;
import com.integrixs.data.repository.SystemLogRepository;
import com.integrixs.shared.dto.log.LogSearchCriteria;
import com.integrixs.shared.dto.log.LogSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Enhanced log search service with advanced search capabilities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogSearchService {
    
    private final SystemLogRepository systemLogRepository;
    
    /**
     * Search logs with advanced criteria.
     */
    public LogSearchResult searchLogs(LogSearchCriteria criteria) {
        log.debug("Searching logs with criteria: {}", criteria);
        
        Specification<SystemLog> spec = buildSpecification(criteria);
        Pageable pageable = buildPageable(criteria);
        
        Page<SystemLog> logsPage = systemLogRepository.findAll(spec, pageable);
        
        // Build search result
        LogSearchResult result = new LogSearchResult();
        result.setLogs(logsPage.getContent());
        result.setTotalElements(logsPage.getTotalElements());
        result.setTotalPages(logsPage.getTotalPages());
        result.setCurrentPage(logsPage.getNumber());
        result.setPageSize(logsPage.getSize());
        
        // Add facets if requested
        if (criteria.isIncludeFacets()) {
            result.setFacets(calculateFacets(spec));
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
        Specification<SystemLog> spec = Specification.where(
            (root, query, cb) -> cb.and(
                cb.like(root.get("message"), "%flow: " + flowId + "%"),
                cb.between(root.get("timestamp"), startTime, endTime)
            )
        );
        
        return systemLogRepository.findAll(spec, Sort.by("timestamp"));
    }
    
    /**
     * Build JPA Specification from search criteria.
     */
    private Specification<SystemLog> buildSpecification(LogSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Time range filter
            if (criteria.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), criteria.getStartTime()));
            }
            if (criteria.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), criteria.getEndTime()));
            }
            
            // Level filter
            if (criteria.getLevels() != null && !criteria.getLevels().isEmpty()) {
                predicates.add(root.get("level").in(criteria.getLevels()));
            }
            
            // Source filter
            if (criteria.getSources() != null && !criteria.getSources().isEmpty()) {
                predicates.add(root.get("source").in(criteria.getSources()));
            }
            
            // Category filter
            if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
                predicates.add(root.get("category").in(criteria.getCategories()));
            }
            
            // Component filter
            if (criteria.getComponentIds() != null && !criteria.getComponentIds().isEmpty()) {
                predicates.add(root.get("componentId").in(criteria.getComponentIds()));
            }
            
            // User filter
            if (criteria.getUserIds() != null && !criteria.getUserIds().isEmpty()) {
                predicates.add(root.get("userId").in(criteria.getUserIds()));
            }
            
            // Correlation ID filter
            if (criteria.getCorrelationId() != null && !criteria.getCorrelationId().isEmpty()) {
                predicates.add(cb.equal(root.get("correlationId"), criteria.getCorrelationId()));
            }
            
            // Text search
            if (criteria.getSearchText() != null && !criteria.getSearchText().isEmpty()) {
                String searchPattern = "%" + criteria.getSearchText().toLowerCase() + "%";
                Predicate textPredicate = cb.or(
                    cb.like(cb.lower(root.get("message")), searchPattern),
                    cb.like(cb.lower(root.get("details")), searchPattern),
                    cb.like(cb.lower(root.get("stackTrace")), searchPattern)
                );
                predicates.add(textPredicate);
            }
            
            // Regex search (if supported by database)
            if (criteria.getRegexPattern() != null && !criteria.getRegexPattern().isEmpty()) {
                // Note: This is PostgreSQL specific, adjust for other databases
                predicates.add(cb.isTrue(
                    cb.function("regexp_like", Boolean.class, 
                        root.get("message"), cb.literal(criteria.getRegexPattern()))
                ));
            }
            
            // IP address filter
            if (criteria.getIpAddress() != null && !criteria.getIpAddress().isEmpty()) {
                predicates.add(cb.equal(root.get("ipAddress"), criteria.getIpAddress()));
            }
            
            // Exclude patterns
            if (criteria.getExcludePatterns() != null && !criteria.getExcludePatterns().isEmpty()) {
                for (String pattern : criteria.getExcludePatterns()) {
                    predicates.add(cb.notLike(root.get("message"), "%" + pattern + "%"));
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Build pageable from search criteria.
     */
    private Pageable buildPageable(LogSearchCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp"); // Default sort
        
        if (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty()) {
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
    private Map<String, Map<String, Long>> calculateFacets(Specification<SystemLog> baseSpec) {
        Map<String, Map<String, Long>> facets = new HashMap<>();
        
        // Level facets
        Map<String, Long> levelFacets = new HashMap<>();
        for (SystemLog.LogLevel level : SystemLog.LogLevel.values()) {
            Specification<SystemLog> levelSpec = baseSpec.and(
                (root, query, cb) -> cb.equal(root.get("level"), level)
            );
            long count = systemLogRepository.count(levelSpec);
            if (count > 0) {
                levelFacets.put(level.name(), count);
            }
        }
        facets.put("levels", levelFacets);
        
        // Source facets (top 10)
        List<Object[]> sourceCounts = systemLogRepository.findAll(baseSpec).stream()
            .collect(Collectors.groupingBy(SystemLog::getSource, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .collect(Collectors.toList());
        
        Map<String, Long> sourceFacets = new HashMap<>();
        for (Object[] sourceCount : sourceCounts) {
            sourceFacets.put((String) sourceCount[0], (Long) sourceCount[1]);
        }
        facets.put("sources", sourceFacets);
        
        // Category facets (top 10)
        List<Object[]> categoryCounts = systemLogRepository.findAll(baseSpec).stream()
            .filter(log -> log.getCategory() != null)
            .collect(Collectors.groupingBy(SystemLog::getCategory, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .collect(Collectors.toList());
        
        Map<String, Long> categoryFacets = new HashMap<>();
        for (Object[] categoryCount : categoryCounts) {
            categoryFacets.put((String) categoryCount[0], (Long) categoryCount[1]);
        }
        facets.put("categories", categoryFacets);
        
        return facets;
    }
    
    /**
     * Generate search highlights.
     */
    private Map<String, List<String>> generateHighlights(List<SystemLog> logs, String searchText) {
        Map<String, List<String>> highlights = new HashMap<>();
        Pattern pattern = Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE);
        
        for (SystemLog log : logs) {
            List<String> logHighlights = new ArrayList<>();
            
            // Check message
            if (log.getMessage() != null && pattern.matcher(log.getMessage()).find()) {
                logHighlights.add(highlightText(log.getMessage(), searchText));
            }
            
            // Check details
            if (log.getDetails() != null && pattern.matcher(log.getDetails()).find()) {
                logHighlights.add(highlightText(log.getDetails(), searchText));
            }
            
            if (!logHighlights.isEmpty()) {
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
        
        switch (format.toLowerCase()) {
            case "csv":
                return exportToCsv(searchResult.getLogs());
            case "json":
                return exportToJson(searchResult.getLogs());
            case "text":
                return exportToText(searchResult.getLogs());
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
        
        for (SystemLog log : logs) {
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
        } catch (Exception e) {
            log.error("Failed to export logs to JSON", e);
            return "[]".getBytes();
        }
    }
    
    /**
     * Export logs to plain text format.
     */
    private byte[] exportToText(List<SystemLog> logs) {
        StringBuilder text = new StringBuilder();
        
        for (SystemLog log : logs) {
            text.append(String.format("[%s] %s - %s - %s\\n",
                log.getTimestamp(),
                log.getLevel(),
                log.getSource(),
                log.getMessage()
            ));
            
            if (log.getStackTrace() != null && !log.getStackTrace().isEmpty()) {
                text.append("Stack trace:\\n").append(log.getStackTrace()).append("\\n");
            }
            
            text.append("\\n");
        }
        
        return text.toString().getBytes();
    }
}