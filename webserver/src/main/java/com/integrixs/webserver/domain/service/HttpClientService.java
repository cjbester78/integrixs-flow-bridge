package com.integrixs.webserver.domain.service;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;

/**
 * Domain service interface for HTTP client operations
 */
public interface HttpClientService {

    /**
     * Execute REST API call
     * @param request Outbound request
     * @return Response from REST API
     */
    OutboundResponse executeRestCall(OutboundRequest request);

    /**
     * Execute SOAP service call
     * @param request Outbound request
     * @return Response from SOAP service
     */
    OutboundResponse executeSoapCall(OutboundRequest request);

    /**
     * Execute GraphQL query
     * @param request Outbound request with GraphQL query
     * @return Response from GraphQL endpoint
     */
    OutboundResponse executeGraphQLQuery(OutboundRequest request);

    /**
     * Download file from URL
     * @param url File URL
     * @param headers Optional headers
     * @return File content as byte array
     */
    byte[] downloadFile(String url, java.util.Map<String, String> headers);

    /**
     * Upload file to endpoint
     * @param url Upload URL
     * @param fileContent File content
     * @param fileName File name
     * @param headers Optional headers
     * @return Upload response
     */
    OutboundResponse uploadFile(String url, byte[] fileContent, String fileName, java.util.Map<String, String> headers);

    /**
     * Stream data from endpoint
     * @param url Stream URL
     * @param callback Stream callback
     */
    void streamData(String url, StreamCallback callback);

    /**
     * Callback interface for streaming data
     */
    interface StreamCallback {
        void onData(byte[] chunk);
        void onComplete();
        void onError(String errorMessage);
    }
}
