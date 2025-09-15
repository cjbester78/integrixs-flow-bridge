package com.integrixs.webserver.domain.service;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;

/**
 * Domain service interface for handling outbound requests
 */
public interface OutboundRequestService {

    /**
     * Execute an outbound request
     * @param request The outbound request
     * @return The response from external service
     */
    OutboundResponse executeRequest(OutboundRequest request);

    /**
     * Execute an outbound request asynchronously
     * @param request The outbound request
     * @param callback Callback to handle response
     */
    void executeRequestAsync(OutboundRequest request, ResponseCallback callback);

    /**
     * Validate outbound request
     * @param request The request to validate
     * @return true if valid
     */
    boolean validateRequest(OutboundRequest request);

    /**
     * Transform request payload to target format
     * @param request The request
     * @param targetFormat Target format(JSON, XML, etc.)
     * @return Transformed payload
     */
    Object transformRequestPayload(OutboundRequest request, String targetFormat);

    /**
     * Transform response payload to desired format
     * @param response The response
     * @param targetFormat Target format
     * @return Transformed payload
     */
    Object transformResponsePayload(OutboundResponse response, String targetFormat);

    /**
     * Cancel an in - progress request
     * @param requestId Request ID
     * @return true if cancelled successfully
     */
    boolean cancelRequest(String requestId);

    /**
     * Get request status
     * @param requestId Request ID
     * @return Request status
     */
    String getRequestStatus(String requestId);

    /**
     * Callback interface for async responses
     */
    interface ResponseCallback {
        void onSuccess(OutboundResponse response);
        void onError(String requestId, String errorMessage);
    }
}
