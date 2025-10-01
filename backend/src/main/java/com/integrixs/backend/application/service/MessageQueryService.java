package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.request.MessageQueryRequest;
import com.integrixs.backend.api.dto.response.MessageResponse;
import com.integrixs.backend.api.dto.response.MessageStatsResponse;
import com.integrixs.backend.api.dto.response.PagedMessageResponse;
import com.integrixs.backend.domain.service.MessageStatisticsService;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.AdapterPayloadSqlRepository;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.shared.dto.log.LogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for querying and retrieving messages
 */
@Service
public class MessageQueryService {

    private static final Logger log = LoggerFactory.getLogger(MessageQueryService.class);

    private final SystemLogSqlRepository systemLogRepository;
    private final AdapterPayloadSqlRepository payloadRepository;
    private final IntegrationFlowSqlRepository flowRepository;
    private final MessageStatisticsService statisticsService;

    public MessageQueryService(SystemLogSqlRepository systemLogRepository,
                             AdapterPayloadSqlRepository payloadRepository,
                             IntegrationFlowSqlRepository flowRepository,
                             MessageStatisticsService statisticsService) {
        this.systemLogRepository = systemLogRepository;
        this.payloadRepository = payloadRepository;
        this.flowRepository = flowRepository;
        this.statisticsService = statisticsService;
    }

    public PagedMessageResponse getMessages(MessageQueryRequest request) {
        log.debug("Querying messages with filters: {}", request);

        // Build page request
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(
            request.getPage(),
            request.getSize(),
            Sort.by(direction, request.getSortBy())
       );

        // Build search criteria from request
        LogSearchCriteria criteria = buildSearchCriteria(request);

        // Execute query
        Page<SystemLog> logPage = systemLogRepository.searchLogs(criteria, pageRequest);

        // Convert to DTOs
        List<MessageResponse> messages = logPage.getContent().stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());

        // Build paged response
        return PagedMessageResponse.builder()
            .content(messages)
            .pageNumber(logPage.getNumber())
            .pageSize(logPage.getSize())
            .totalElements(logPage.getTotalElements())
            .totalPages(logPage.getTotalPages())
            .first(logPage.isFirst())
            .last(logPage.isLast())
            .empty(logPage.isEmpty())
            .build();
    }

    public MessageResponse getMessageById(String id) {
        log.debug("Getting message by ID: {}", id);

        SystemLog log = systemLogRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("Message not found: " + id));

        return convertToMessageResponse(log);
    }

    public List<MessageResponse> getRecentMessages(String businessComponentId, int limit) {
        log.debug("Getting recent messages for component: {}, limit: {}", businessComponentId, limit);

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));

        List<SystemLog> logs;
        if(businessComponentId != null) {
            logs = systemLogRepository.findByComponentId(businessComponentId, pageRequest).getContent();
        } else {
            logs = systemLogRepository.findAll(pageRequest).getContent();
        }

        return logs.stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());
    }

    public MessageStatsResponse getMessageStats(MessageQueryRequest request) {
        log.debug("Calculating message statistics with filters: {}", request);

        // Build search criteria from request
        LogSearchCriteria criteria = buildSearchCriteria(request);

        // Get all logs matching the criteria - using large page size to get all results
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        Page<SystemLog> logPage = systemLogRepository.searchLogs(criteria, pageRequest);
        List<SystemLog> logs = logPage.getContent();

        // Calculate statistics
        Map<String, Long> byStatus = statisticsService.calculateMessagesByStatus(logs);
        Map<String, Long> byType = statisticsService.calculateMessagesByType(logs);
        Map<String, Long> bySource = statisticsService.calculateMessagesBySource(logs);
        Map<String, Long> byTarget = statisticsService.calculateMessagesByTarget(logs);

        Double avgExecutionTime = statisticsService.calculateAverageExecutionTime(logs);
        Map<String, Long> executionStats = statisticsService.calculateExecutionTimeStats(logs);

        LocalDateTime periodStart = request.getDateFrom() != null ? request.getDateFrom() :
            logs.stream().map(SystemLog::getTimestamp).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        LocalDateTime periodEnd = request.getDateTo() != null ? request.getDateTo() :
            logs.stream().map(SystemLog::getTimestamp).max(LocalDateTime::compareTo).orElse(LocalDateTime.now());

        Long messagesPerHour = statisticsService.calculateMessagesPerHour(logs, periodStart, periodEnd);
        Long messagesPerDay = statisticsService.calculateMessagesPerDay(logs, periodStart, periodEnd);

        // Build response
        return MessageStatsResponse.builder()
            .totalMessages(logs.size())
            .successfulMessages(byStatus.getOrDefault("COMPLETED", 0L))
            .failedMessages(byStatus.getOrDefault("FAILED", 0L))
            .pendingMessages(byStatus.getOrDefault("PENDING", 0L))
            .processingMessages(byStatus.getOrDefault("PROCESSING", 0L))
            .avgExecutionTimeMs(avgExecutionTime)
            .minExecutionTimeMs(executionStats.get("min"))
            .maxExecutionTimeMs(executionStats.get("max"))
            .messagesByStatus(byStatus)
            .messagesByType(byType)
            .messagesBySource(bySource)
            .messagesByTarget(byTarget)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .messagesPerHour(messagesPerHour)
            .messagesPerDay(messagesPerDay)
            .build();
    }

    private LogSearchCriteria buildSearchCriteria(MessageQueryRequest request) {
        LogSearchCriteria criteria = new LogSearchCriteria();

        // Date range filter
        if (request.getDateFrom() != null) {
            criteria.setStartTime(request.getDateFrom());
        }
        if (request.getDateTo() != null) {
            criteria.setEndTime(request.getDateTo());
        }

        // Business component filter
        if (request.getBusinessComponentId() != null) {
            criteria.setComponentIds(Arrays.asList(request.getBusinessComponentId()));
        }

        // Correlation ID filter
        if (request.getCorrelationId() != null) {
            criteria.setCorrelationId(request.getCorrelationId());
        }

        // Search filter
        if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
            criteria.setSearchText(request.getSearch());
        }

        // Category filter for flow execution logs
        criteria.setCategories(Arrays.asList("FLOW_EXECUTION", "MESSAGE", "ADAPTER"));

        // Page settings
        criteria.setPage(request.getPage());
        criteria.setPageSize(request.getSize());

        // Sort settings
        if (request.getSortBy() != null) {
            criteria.setSortBy(request.getSortBy());
        }
        if (request.getSortDirection() != null) {
            criteria.setAscending("ASC".equalsIgnoreCase(request.getSortDirection()));
        }

        return criteria;
    }

    private MessageResponse convertToMessageResponse(SystemLog log) {
        MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
            .id(log.getId().toString())
            .correlationId(log.getCorrelationId())
            .createdAt(log.getTimestamp())
            .type(log.getDomainType());

        // Extract details from log message and details
        if(log.getDetails() != null) {
            // Parse details to extract fields
            builder.status(extractField(log.getDetails(), "status"));
            builder.source(extractField(log.getDetails(), "source"));
            builder.target(extractField(log.getDetails(), "target"));
            builder.errorMessage(extractField(log.getDetails(), "error"));
        }

        // Get flow information
        if(log.getDomainReferenceId() != null && "IntegrationFlow".equals(log.getDomainType())) {
            try {
                flowRepository.findById(UUID.fromString(log.getDomainReferenceId())).ifPresent(flow -> {
                    builder.flowId(flow.getId().toString());
                    builder.flowName(flow.getName());
                });
            } catch(Exception e) {
                // Ignore if not a valid flow ID
            }
        }

        // Get payload if available
        if(log.getCorrelationId() != null) {
            payloadRepository.findByCorrelationId(log.getCorrelationId()).stream()
                .findFirst()
                .ifPresent(payload -> {
                    builder.payload(payload.getPayload());
                });
        }

        return builder.build();
    }

    private String extractField(String details, String fieldName) {
        // Simple extraction - in production would use proper JSON parsing
        int index = details.indexOf(fieldName + ":");
        if(index == -1) return null;

        int start = index + fieldName.length() + 1;
        int end = details.indexOf(",", start);
        if(end == -1) end = details.indexOf("}", start);
        if(end == -1) end = details.length();

        return details.substring(start, end).trim().replace("\"", "");
    }
}
