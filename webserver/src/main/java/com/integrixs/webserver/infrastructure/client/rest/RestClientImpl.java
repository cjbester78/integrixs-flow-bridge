package com.integrixs.webserver.infrastructure.client.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Base64;
import java.util.Map;

/**
 * REST client implementation for HTTP requests
 */
@Component
public class RestClientImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestClientImpl.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestClientImpl() {
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Execute REST API call
     * @param request Outbound request
     * @return Response from REST API
     */
    public OutboundResponse executeRestCall(OutboundRequest request) {
        logger.info("Executing REST call to: {}", request.getTargetUrl());
        long startTime = System.currentTimeMillis();

        try {
            // Build URI with query parameters
            URI uri = buildUri(request.getTargetUrl(), request.getQueryParams());

            // Prepare headers
            HttpHeaders headers = prepareHeaders(request);

            // Prepare request entity
            HttpEntity<?> requestEntity = prepareRequestEntity(request, headers);

            // Map HTTP method
            HttpMethod method = mapHttpMethod(request.getHttpMethod());

            // Configure timeout
            configureTimeout(request.getTimeoutSeconds());

            // Execute request
            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                method,
                requestEntity,
                String.class
           );

            // Build response
            return OutboundResponse.success(
                    request.getRequestId(),
                    response.getStatusCode().value(),
                    response.getBody()
               )
                .withResponseTime(startTime);

        } catch(HttpClientErrorException e) {
            logger.error("Client error calling {}: {}", request.getTargetUrl(), e.getMessage());
            return OutboundResponse.failure(
                    request.getRequestId(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
               )
                .withResponseTime(startTime);

        } catch(HttpServerErrorException e) {
            logger.error("Server error calling {}: {}", request.getTargetUrl(), e.getMessage());
            return OutboundResponse.failure(
                    request.getRequestId(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
               )
                .withResponseTime(startTime);

        } catch(ResourceAccessException e) {
            logger.error("Timeout or connection error calling {}: {}", request.getTargetUrl(), e.getMessage());
            return OutboundResponse.timeout(
                    request.getRequestId(),
                    "Connection timeout or refused: " + e.getMessage()
               )
                .withResponseTime(startTime);

        } catch(Exception e) {
            logger.error("Unexpected error calling {}: {}", request.getTargetUrl(), e.getMessage(), e);
            return OutboundResponse.failure(
                    request.getRequestId(),
                    500,
                    "Unexpected error: " + e.getMessage()
               )
                .withResponseTime(startTime);
        }
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        RestTemplate template = new RestTemplate(factory);
        return template;
    }

    private URI buildUri(String baseUrl, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

        if(queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(builder::queryParam);
        }

        return builder.build().encode().toUri();
    }

    private HttpHeaders prepareHeaders(OutboundRequest request) {
        HttpHeaders headers = new HttpHeaders();

        // Add custom headers
        if(request.getHeaders() != null) {
            request.getHeaders().forEach(headers::add);
        }

        // Set content type
        if(request.getContentType() != null) {
            headers.setContentType(MediaType.valueOf(request.getContentType()));
        }

        // Handle authentication
        addAuthentication(headers, request.getAuthentication());

        return headers;
    }

    private void addAuthentication(HttpHeaders headers, OutboundRequest.AuthenticationConfig authConfig) {
        if(authConfig == null) return;

        switch(authConfig.getAuthType()) {
            case BASIC:
                String username = authConfig.getCredentials().get("username");
                String password = authConfig.getCredentials().get("password");
                if(username != null && password != null) {
                    String auth = username + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.add("Authorization", "Basic " + encodedAuth);
                }
                break;

            case BEARER:
                String token = authConfig.getCredentials().get("token");
                if(token != null) {
                    headers.add("Authorization", "Bearer " + token);
                }
                break;

            case API_KEY:
                String keyName = authConfig.getCredentials().get("keyName");
                String keyValue = authConfig.getCredentials().get("keyValue");
                if(keyName != null && keyValue != null) {
                    headers.add(keyName, keyValue);
                }
                break;
        }
    }

    private HttpEntity<?> prepareRequestEntity(OutboundRequest request, HttpHeaders headers) throws Exception {
        if(request.getPayload() == null) {
            return new HttpEntity<>(headers);
        }

        // Convert payload to JSON if necessary
        if(request.getContentType() != null && request.getContentType().contains("json")) {
            String jsonPayload = (request.getPayload() instanceof String)
                ? (String) request.getPayload()
                : objectMapper.writeValueAsString(request.getPayload());
            return new HttpEntity<>(jsonPayload, headers);
        } else {
            return new HttpEntity<>(request.getPayload(), headers);
        }
    }

    private HttpMethod mapHttpMethod(OutboundRequest.HttpMethod method) {
        switch(method) {
            case GET: return HttpMethod.GET;
            case POST: return HttpMethod.POST;
            case PUT: return HttpMethod.PUT;
            case DELETE: return HttpMethod.DELETE;
            case PATCH: return HttpMethod.PATCH;
            case HEAD: return HttpMethod.HEAD;
            case OPTIONS: return HttpMethod.OPTIONS;
            default: return HttpMethod.GET;
        }
    }

    private void configureTimeout(int timeoutSeconds) {
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        if(factory instanceof SimpleClientHttpRequestFactory) {
            SimpleClientHttpRequestFactory simpleFactory = (SimpleClientHttpRequestFactory) factory;
            simpleFactory.setConnectTimeout(timeoutSeconds * 1000);
            simpleFactory.setReadTimeout(timeoutSeconds * 1000);
        }
    }
}
