package com.integrixs.webserver.application.service;

import com.integrixs.webserver.api.dto.*;
import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;
import com.integrixs.webserver.domain.model.ServiceEndpoint;
import com.integrixs.webserver.domain.model.ServiceEndpoint.ServiceType;
import com.integrixs.webserver.domain.repository.RequestHistoryRepository;
import com.integrixs.webserver.domain.repository.ServiceEndpointRepository;
import com.integrixs.webserver.domain.service.HttpClientService;
import com.integrixs.webserver.domain.service.OutboundRequestService;
import com.integrixs.webserver.domain.service.ServiceEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for orchestrating web server operations
 */
@Service
public class WebServerApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(WebServerApplicationService.class);

    private final OutboundRequestService requestService;
    private final ServiceEndpointService endpointService;
    private final HttpClientService httpClientService;
    private final ServiceEndpointRepository endpointRepository;
    private final RequestHistoryRepository historyRepository;

    public WebServerApplicationService(
            OutboundRequestService requestService,
            ServiceEndpointService endpointService,
            HttpClientService httpClientService,
            ServiceEndpointRepository endpointRepository,
            RequestHistoryRepository historyRepository) {
        this.requestService = requestService;
        this.endpointService = endpointService;
        this.httpClientService = httpClientService;
        this.endpointRepository = endpointRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Execute an outbound request
     * @param request Request DTO
     * @return Response DTO
     */
    public OutboundResponseDTO executeRequest(OutboundRequestDTO request) {
        logger.info("Executing outbound request to: {}", request.getTargetUrl());

        // Convert DTO to domain model
        OutboundRequest domainRequest = convertToDomainRequest(request);

        // Save request to history
        historyRepository.saveRequest(domainRequest);

        try {
            // Execute request
            OutboundResponse response = requestService.executeRequest(domainRequest);

            // Save response to history
            historyRepository.saveResponse(response);

            // Convert response to DTO
            return convertToResponseDTO(response);

        } catch(Exception e) {
            logger.error("Error executing request {}: {}", domainRequest.getRequestId(), e.getMessage(), e);

            // Create error response
            OutboundResponse errorResponse = OutboundResponse.failure(
                domainRequest.getRequestId(),
                500,
                e.getMessage()
           );
            historyRepository.saveResponse(errorResponse);

            return convertToResponseDTO(errorResponse);
        }
    }

    /**
     * Execute request with endpoint ID
     * @param endpointId Endpoint ID
     * @param request Request DTO
     * @return Response DTO
     */
    public OutboundResponseDTO executeRequestWithEndpoint(String endpointId, EndpointRequestDTO request) {
        logger.info("Executing request with endpoint: {}", endpointId);

        // Get endpoint configuration
        ServiceEndpoint endpoint = endpointService.getEndpoint(endpointId);
        if(endpoint == null || !endpoint.isActive()) {
            throw new RuntimeException("Endpoint not found or inactive: " + endpointId);
        }

        // Build full URL
        String fullUrl = endpoint.buildUrl(request.getPath());

        // Create outbound request with endpoint configuration
        OutboundRequest domainRequest = OutboundRequest.builder()
                .requestType(mapEndpointTypeToRequestType(endpoint.getType()))
                .targetUrl(fullUrl)
                .httpMethod(OutboundRequest.HttpMethod.valueOf(request.getMethod()))
                .payload(request.getPayload())
                .contentType(request.getContentType())
                .headers(endpoint.getMergedHeaders(request.getHeaders()))
                .queryParams(request.getQueryParams())
                .authentication(convertAuthConfig(endpoint.getDefaultAuth()))
                .timeoutSeconds(endpoint.getConnectionConfig().getReadTimeoutSeconds())
                .flowId(request.getFlowId())
                .adapterId(request.getAdapterId())
                .build();

        // Save request to history
        historyRepository.saveRequest(domainRequest);

        try {
            // Execute request based on type
            OutboundResponse response;
            switch(endpoint.getType()) {
                case SOAP_SERVICE:
                    response = httpClientService.executeSoapCall(domainRequest);
                    break;
                case GRAPHQL_API:
                    response = httpClientService.executeGraphQLQuery(domainRequest);
                    break;
                default:
                    response = httpClientService.executeRestCall(domainRequest);
                    break;
            }

            // Save response to history
            historyRepository.saveResponse(response);

            return convertToResponseDTO(response);

        } catch(Exception e) {
            logger.error("Error executing request with endpoint {}: {}", endpointId, e.getMessage(), e);

            OutboundResponse errorResponse = OutboundResponse.failure(
                domainRequest.getRequestId(),
                500,
                e.getMessage()
           );
            historyRepository.saveResponse(errorResponse);

            return convertToResponseDTO(errorResponse);
        }
    }

    /**
     * Register a new service endpoint
     * @param request Endpoint registration request
     * @return Endpoint details
     */
    public ServiceEndpointDTO registerEndpoint(RegisterEndpointDTO request) {
        logger.info("Registering new endpoint: {}", request.getName());

        // Check if endpoint name already exists
        if(endpointRepository.existsByName(request.getName())) {
            throw new RuntimeException("Endpoint with name already exists: " + request.getName());
        }

        // Create domain model
        ServiceEndpoint endpoint = convertToDomainEndpoint(request);

        // Validate endpoint configuration
        if(!endpointService.validateEndpointConfiguration(endpoint)) {
            throw new RuntimeException("Invalid endpoint configuration");
        }

        // Register endpoint
        ServiceEndpoint registered = endpointService.registerEndpoint(endpoint);

        return convertToEndpointDTO(registered);
    }

    /**
     * Update service endpoint
     * @param endpointId Endpoint ID
     * @param request Update request
     * @return Updated endpoint
     */
    public ServiceEndpointDTO updateEndpoint(String endpointId, UpdateEndpointDTO request) {
        logger.info("Updating endpoint: {}", endpointId);

        // Get existing endpoint
        ServiceEndpoint existing = endpointService.getEndpoint(endpointId);
        if(existing == null) {
            throw new RuntimeException("Endpoint not found: " + endpointId);
        }

        // Update fields
        updateEndpointFields(existing, request);

        // Update endpoint
        ServiceEndpoint updated = endpointService.updateEndpoint(existing);

        return convertToEndpointDTO(updated);
    }

    /**
     * Get all endpoints
     * @return List of endpoints
     */
    public List<ServiceEndpointDTO> getAllEndpoints() {
        return endpointRepository.findAll().stream()
                .map(this::convertToEndpointDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get endpoint by ID
     * @param endpointId Endpoint ID
     * @return Endpoint details
     */
    public ServiceEndpointDTO getEndpoint(String endpointId) {
        ServiceEndpoint endpoint = endpointService.getEndpoint(endpointId);
        if(endpoint == null) {
            throw new RuntimeException("Endpoint not found: " + endpointId);
        }
        return convertToEndpointDTO(endpoint);
    }

    /**
     * Test endpoint connectivity
     * @param endpointId Endpoint ID
     * @return Test result
     */
    public EndpointTestResultDTO testEndpoint(String endpointId) {
        logger.info("Testing endpoint connectivity: {}", endpointId);

        boolean isReachable = endpointService.testEndpointConnectivity(endpointId);

        return EndpointTestResultDTO.builder()
                .endpointId(endpointId)
                .reachable(isReachable)
                .timestamp(LocalDateTime.now())
                .message(isReachable ? "Endpoint is reachable" : "Endpoint is not reachable")
                .build();
    }

    /**
     * Get request history
     * @param criteria Search criteria
     * @return List of request history
     */
    public List<RequestHistoryDTO> getRequestHistory(RequestHistoryCriteriaDTO criteria) {
        List<OutboundRequest> requests;

        if(criteria.getFlowId() != null) {
            requests = historyRepository.findRequestsByFlowId(criteria.getFlowId());
        } else if(criteria.getAdapterId() != null) {
            requests = historyRepository.findRequestsByAdapterId(criteria.getAdapterId());
        } else if(criteria.getStartDate() != null && criteria.getEndDate() != null) {
            requests = historyRepository.findRequestsByDateRange(criteria.getStartDate(), criteria.getEndDate());
        } else {
            // Default to last 24 hours
            requests = historyRepository.findRequestsByDateRange(
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now()
           );
        }

        return requests.stream()
                .map(this::convertToHistoryDTO)
                .collect(Collectors.toList());
    }

    // Helper methods for conversion

    private OutboundRequest convertToDomainRequest(OutboundRequestDTO dto) {
        return OutboundRequest.builder()
                .requestType(OutboundRequest.RequestType.valueOf(dto.getRequestType()))
                .targetUrl(dto.getTargetUrl())
                .httpMethod(OutboundRequest.HttpMethod.valueOf(dto.getHttpMethod()))
                .payload(dto.getPayload())
                .contentType(dto.getContentType())
                .headers(dto.getHeaders())
                .queryParams(dto.getQueryParams())
                .authentication(convertAuthConfig(dto.getAuthentication()))
                .timeoutSeconds(dto.getTimeoutSeconds())
                .retryConfig(convertRetryConfig(dto.getRetryConfig()))
                .flowId(dto.getFlowId())
                .adapterId(dto.getAdapterId())
                .build();
    }

    private OutboundRequest.AuthenticationConfig convertAuthConfig(AuthenticationConfigDTO dto) {
        if(dto == null) return null;

        return OutboundRequest.AuthenticationConfig.builder()
                .authType(OutboundRequest.AuthenticationConfig.AuthType.valueOf(dto.getAuthType()))
                .credentials(dto.getCredentials())
                .build();
    }

    private OutboundRequest.AuthenticationConfig convertAuthConfig(ServiceEndpoint.AuthenticationConfig config) {
        if(config == null) return null;

        return OutboundRequest.AuthenticationConfig.builder()
                .authType(OutboundRequest.AuthenticationConfig.AuthType.valueOf(config.getAuthType().name()))
                .credentials(config.getCredentials())
                .build();
    }

    private OutboundRequest.RetryConfig convertRetryConfig(RetryConfigDTO dto) {
        if(dto == null) return null;

        return OutboundRequest.RetryConfig.builder()
                .maxRetries(dto.getMaxRetries())
                .retryDelayMillis(dto.getRetryDelayMillis())
                .exponentialBackoff(dto.isExponentialBackoff())
                .backoffMultiplier(dto.getBackoffMultiplier())
                .build();
    }

    private OutboundResponseDTO convertToResponseDTO(OutboundResponse response) {
        return OutboundResponseDTO.builder()
                .requestId(response.getRequestId())
                .success(response.isSuccess())
                .statusCode(response.getStatusCode())
                .statusMessage(response.getStatusMessage())
                .responseBody(response.getResponseBody())
                .contentType(response.getContentType())
                .headers(response.getHeaders())
                .timestamp(response.getTimestamp())
                .responseTimeMillis(response.getResponseTimeMillis())
                .errorMessage(response.getErrorMessage())
                .errorCode(response.getErrorCode())
                .retryInfo(convertRetryInfo(response.getRetryInfo()))
                .build();
    }

    private RetryInfoDTO convertRetryInfo(OutboundResponse.RetryInfo info) {
        if(info == null) return null;

        return RetryInfoDTO.builder()
                .attemptCount(info.getAttemptCount())
                .wasRetried(info.isWasRetried())
                .lastRetryReason(info.getLastRetryReason())
                .lastRetryTime(info.getLastRetryTime())
                .build();
    }

    private ServiceEndpoint convertToDomainEndpoint(RegisterEndpointDTO dto) {
        return ServiceEndpoint.builder()
                .name(dto.getName())
                .baseUrl(dto.getBaseUrl())
                .type(ServiceType.valueOf(dto.getType()))
                .description(dto.getDescription())
                .defaultAuth(convertEndpointAuth(dto.getDefaultAuth()))
                .defaultHeaders(dto.getDefaultHeaders())
                .connectionConfig(convertConnectionConfig(dto.getConnectionConfig()))
                .active(true)
                .version(dto.getVersion())
                .metadata(dto.getMetadata())
                .build();
    }

    private ServiceEndpoint.AuthenticationConfig convertEndpointAuth(AuthenticationConfigDTO dto) {
        if(dto == null) return null;

        return ServiceEndpoint.AuthenticationConfig.builder()
                .authType(ServiceEndpoint.AuthenticationConfig.AuthType.valueOf(dto.getAuthType()))
                .credentials(dto.getCredentials())
                .build();
    }

    private ServiceEndpoint.ConnectionConfig convertConnectionConfig(ConnectionConfigDTO dto) {
        if(dto == null) {
            return ServiceEndpoint.ConnectionConfig.builder().build();
        }

        return ServiceEndpoint.ConnectionConfig.builder()
                .connectionTimeoutSeconds(dto.getConnectionTimeoutSeconds())
                .readTimeoutSeconds(dto.getReadTimeoutSeconds())
                .maxConnections(dto.getMaxConnections())
                .maxConnectionsPerRoute(dto.getMaxConnectionsPerRoute())
                .keepAlive(dto.isKeepAlive())
                .followRedirects(dto.isFollowRedirects())
                .build();
    }

    private ServiceEndpointDTO convertToEndpointDTO(ServiceEndpoint endpoint) {
        return ServiceEndpointDTO.builder()
                .endpointId(endpoint.getEndpointId())
                .name(endpoint.getName())
                .baseUrl(endpoint.getBaseUrl())
                .type(endpoint.getType().name())
                .description(endpoint.getDescription())
                .active(endpoint.isActive())
                .version(endpoint.getVersion())
                .metadata(endpoint.getMetadata())
                .requiresAuth(endpoint.requiresAuth())
                .build();
    }

    private void updateEndpointFields(ServiceEndpoint endpoint, UpdateEndpointDTO dto) {
        if(dto.getBaseUrl() != null) {
            endpoint.setBaseUrl(dto.getBaseUrl());
        }
        if(dto.getDescription() != null) {
            endpoint.setDescription(dto.getDescription());
        }
        if(dto.getDefaultHeaders() != null) {
            endpoint.setDefaultHeaders(dto.getDefaultHeaders());
        }
        if(dto.getActive() != null) {
            endpoint.setActive(dto.getActive());
        }
        if(dto.getVersion() != null) {
            endpoint.setVersion(dto.getVersion());
        }
    }

    private OutboundRequest.RequestType mapEndpointTypeToRequestType(ServiceType type) {
        switch(type) {
            case SOAP_SERVICE:
                return OutboundRequest.RequestType.SOAP_SERVICE;
            case GRAPHQL_API:
                return OutboundRequest.RequestType.GRAPHQL;
            case WEBHOOK:
                return OutboundRequest.RequestType.WEBHOOK;
            default:
                return OutboundRequest.RequestType.REST_API;
        }
    }

    private RequestHistoryDTO convertToHistoryDTO(OutboundRequest request) {
        // Get response if available
        OutboundResponse response = historyRepository.findResponseByRequestId(request.getRequestId()).orElse(null);

        return RequestHistoryDTO.builder()
                .requestId(request.getRequestId())
                .requestType(request.getRequestType().name())
                .targetUrl(request.getTargetUrl())
                .httpMethod(request.getHttpMethod().name())
                .flowId(request.getFlowId())
                .adapterId(request.getAdapterId())
                .statusCode(response != null ? response.getStatusCode() : null)
                .success(response != null ? response.isSuccess() : null)
                .responseTime(response != null ? response.getResponseTimeMillis() : null)
                .errorMessage(response != null ? response.getErrorMessage() : null)
                .build();
    }
}
