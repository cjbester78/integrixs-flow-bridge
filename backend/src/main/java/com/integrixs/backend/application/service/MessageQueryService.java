package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.request.MessageQueryRequest;
import com.integrixs.backend.api.dto.response.MessageResponse;
import com.integrixs.backend.api.dto.response.MessageStatsResponse;
import com.integrixs.backend.api.dto.response.PagedMessageResponse;
import com.integrixs.backend.domain.service.MessageStatisticsService;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.repository.AdapterPayloadRepository;
import com.integrixs.data.repository.SystemLogRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for querying and retrieving messages
 */
@Service
public class MessageQueryService {

    private static final Logger log = LoggerFactory.getLogger(MessageQueryService.class);


    private final SystemLogRepository systemLogRepository;
    private final AdapterPayloadRepository payloadRepository;
    private final IntegrationFlowRepository flowRepository;
    private final MessageStatisticsService statisticsService;

    @Transactional(readOnly = true)
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

        // Build specification from filters
        Specification<SystemLog> spec = buildSpecification(request);

        // Execute query
        Page<SystemLog> logPage = systemLogRepository.findAll(spec, pageRequest);

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

    @Transactional(readOnly = true)
    public MessageResponse getMessageById(String id) {
        log.debug("Getting message by ID: {}", id);

        SystemLog log = systemLogRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("Message not found: " + id));

        return convertToMessageResponse(log);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getRecentMessages(String businessComponentId, int limit) {
        log.debug("Getting recent messages for component: {}, limit: {}", businessComponentId, limit);

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));

        List<SystemLog> logs;
        if(businessComponentId != null) {
            logs = systemLogRepository.findByComponentId(businessComponentId, pageRequest);
        } else {
            logs = systemLogRepository.findAll(pageRequest).getContent();
        }

        return logs.stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MessageStatsResponse getMessageStats(MessageQueryRequest request) {
        log.debug("Calculating message statistics with filters: {}", request);

        // Get all logs matching the criteria
        Specification<SystemLog> spec = buildSpecification(request);
        List<SystemLog> logs = systemLogRepository.findAll(spec);

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

    private Specification<SystemLog> buildSpecification(MessageQueryRequest request) {
        return(root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Status filter
            if(request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(root.get("details").in(
                    request.getStatus().stream()
                        .map(s -> "%" + s + "%")
                        .collect(Collectors.toList())
               ));
            }

            // Date range filter
            if(request.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), request.getDateFrom()));
            }
            if(request.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), request.getDateTo()));
            }

            // Business component filter
            if(request.getBusinessComponentId() != null) {
                predicates.add(cb.equal(root.get("componentId"), request.getBusinessComponentId()));
            }

            // Correlation ID filter
            if(request.getCorrelationId() != null) {
                predicates.add(cb.equal(root.get("correlationId"), request.getCorrelationId()));
            }

            // Search filter
            if(request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("message")), searchPattern),
                    cb.like(cb.lower(root.get("details")), searchPattern),
                    cb.like(cb.lower(root.get("correlationId")), searchPattern)
               ));
            }

            // Category filter for flow execution logs
            predicates.add(cb.or(
                cb.equal(root.get("category"), "FLOW_EXECUTION"),
                cb.equal(root.get("domainType"), "IntegrationFlow"),
                cb.equal(root.get("domainType"), "Message")
           ));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
        if(log.getDomainId() != null) {
            try {
                flowRepository.findById(UUID.fromString(log.getDomainId())).ifPresent(flow -> {
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
