package com.integrixs.webserver.infrastructure.repository;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;
import com.integrixs.webserver.domain.repository.RequestHistoryRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of request history repository
 */
@Repository
public class InMemoryRequestHistoryRepository implements RequestHistoryRepository {

    private final Map<String, OutboundRequest> requests = new ConcurrentHashMap<>();
    private final Map<String, OutboundResponse> responses = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    public void saveRequest(OutboundRequest request) {
        requests.put(request.getRequestId(), request);
        requestTimestamps.put(request.getRequestId(), LocalDateTime.now());
    }

    @Override
    public void saveResponse(OutboundResponse response) {
        responses.put(response.getRequestId(), response);
    }

    @Override
    public Optional<OutboundRequest> findRequestById(String requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    @Override
    public Optional<OutboundResponse> findResponseByRequestId(String requestId) {
        return Optional.ofNullable(responses.get(requestId));
    }

    @Override
    public List<OutboundRequest> findRequestsByFlowId(String flowId) {
        return requests.values().stream()
                .filter(request -> flowId.equals(request.getFlowId()))
                .sorted(Comparator.comparing((OutboundRequest r) -> requestTimestamps.getOrDefault(r.getRequestId(), LocalDateTime.MIN)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<OutboundRequest> findRequestsByAdapterId(String adapterId) {
        return requests.values().stream()
                .filter(request -> adapterId.equals(request.getAdapterId()))
                .sorted(Comparator.comparing((OutboundRequest r) -> requestTimestamps.getOrDefault(r.getRequestId(), LocalDateTime.MIN)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<OutboundRequest> findRequestsByDateRange(LocalDateTime start, LocalDateTime end) {
        return requests.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime timestamp = requestTimestamps.get(entry.getKey());
                    return timestamp != null &&
                           !timestamp.isBefore(start) &&
                           !timestamp.isAfter(end);
                })
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing((OutboundRequest r) -> requestTimestamps.getOrDefault(r.getRequestId(), LocalDateTime.MIN)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<OutboundRequest> findFailedRequests(LocalDateTime start, LocalDateTime end) {
        return requests.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime timestamp = requestTimestamps.get(entry.getKey());
                    if(timestamp == null || timestamp.isBefore(start) || timestamp.isAfter(end)) {
                        return false;
                    }

                    OutboundResponse response = responses.get(entry.getKey());
                    return response != null && !response.isSuccess();
                })
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing((OutboundRequest r) -> requestTimestamps.getOrDefault(r.getRequestId(), LocalDateTime.MIN)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public RequestStatistics getRequestStatistics(String endpointId, LocalDateTime start, LocalDateTime end) {
        RequestStatistics stats = new RequestStatistics();

        List<String> relevantRequestIds = requests.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime timestamp = requestTimestamps.get(entry.getKey());
                    return timestamp != null &&
                           !timestamp.isBefore(start) &&
                           !timestamp.isAfter(end);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        stats.totalRequests = relevantRequestIds.size();

        long totalResponseTime = 0;
        long maxResponseTime = 0;
        long minResponseTime = Long.MAX_VALUE;

        for(String requestId : relevantRequestIds) {
            OutboundResponse response = responses.get(requestId);
            if(response != null) {
                if(response.isSuccess()) {
                    stats.successfulRequests++;
                } else {
                    stats.failedRequests++;
                }

                if(response.getResponseTimeMillis() != 0) {
                    long responseTime = response.getResponseTimeMillis();
                    totalResponseTime += responseTime;
                    maxResponseTime = Math.max(maxResponseTime, responseTime);
                    minResponseTime = Math.min(minResponseTime, responseTime);
                }
            }
        }

        if(stats.totalRequests > 0) {
            stats.averageResponseTime = (double) totalResponseTime / stats.totalRequests;
            stats.maxResponseTime = maxResponseTime;
            stats.minResponseTime = (minResponseTime == Long.MAX_VALUE) ? 0 : minResponseTime;
        }

        return stats;
    }

    @Override
    public int cleanupOldHistory(LocalDateTime before) {
        List<String> toRemove = requestTimestamps.entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(before))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        toRemove.forEach(requestId -> {
            requests.remove(requestId);
            responses.remove(requestId);
            requestTimestamps.remove(requestId);
        });

        return toRemove.size();
    }
}
