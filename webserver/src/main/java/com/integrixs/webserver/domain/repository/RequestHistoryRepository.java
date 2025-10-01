package com.integrixs.webserver.domain.repository;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for request history
 */
public interface RequestHistoryRepository {

    /**
     * Save request
     * @param request Request to save
     */
    void saveRequest(OutboundRequest request);

    /**
     * Save response
     * @param response Response to save
     */
    void saveResponse(OutboundResponse response);

    /**
     * Find request by ID
     * @param requestId Request ID
     * @return Request if found
     */
    Optional<OutboundRequest> findRequestById(String requestId);

    /**
     * Find response by request ID
     * @param requestId Request ID
     * @return Response if found
     */
    Optional<OutboundResponse> findResponseByRequestId(String requestId);

    /**
     * Find requests by flow ID
     * @param flowId Flow ID
     * @return List of requests
     */
    List<OutboundRequest> findRequestsByFlowId(String flowId);

    /**
     * Find requests by adapter ID
     * @param adapterId Adapter ID
     * @return List of requests
     */
    List<OutboundRequest> findRequestsByAdapterId(String adapterId);

    /**
     * Find requests by date range
     * @param start Start date
     * @param end End date
     * @return List of requests
     */
    List<OutboundRequest> findRequestsByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Find failed requests
     * @param start Start date
     * @param end End date
     * @return List of failed requests
     */
    List<OutboundRequest> findFailedRequests(LocalDateTime start, LocalDateTime end);

    /**
     * Get request statistics
     * @param endpointId Endpoint ID
     * @param start Start date
     * @param end End date
     * @return Request statistics
     */
    RequestStatistics getRequestStatistics(String endpointId, LocalDateTime start, LocalDateTime end);

    /**
     * Clean up old history
     * @param before Delete records before this date
     * @return Number of deleted records
     */
    int cleanupOldHistory(LocalDateTime before);

    /**
     * Request statistics
     */
    class RequestStatistics {
        public int totalRequests;
        public int successfulRequests;
        public int failedRequests;
        public double averageResponseTime;
        public long maxResponseTime;
        public long minResponseTime;
    }
}
