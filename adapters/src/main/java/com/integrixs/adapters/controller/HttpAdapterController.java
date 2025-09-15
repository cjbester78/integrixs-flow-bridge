package com.integrixs.adapters.controller;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.FetchRequest;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.infrastructure.adapter.AbstractAdapter;
import com.integrixs.adapters.config.HttpInboundAdapterConfig;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import com.integrixs.shared.exceptions.AdapterException;

/**
 * REST Controller to handle inbound HTTP requests routed to the HTTP Sender Adapter.
 * In middleware terminology, inbound adapters receive data FROM external systems.
 */
@RestController
@RequestMapping("/api/http - adapter")
public class HttpAdapterController {

    private static final Logger logger = LoggerFactory.getLogger(HttpAdapterController.class);

    private final InboundAdapterPort httpInboundAdapter;
    private final AdapterFactoryManager factoryManager;

    /**
     * Constructor injecting HttpInboundAdapterConfig and initializing HTTP Sender Adapter.
     */
    public HttpAdapterController(HttpInboundAdapterConfig config) {
        this.factoryManager = AdapterFactoryManager.getInstance();
        try {
            this.httpInboundAdapter = (InboundAdapterPort) factoryManager.createAndInitialize(
                    AdapterConfiguration.AdapterTypeEnum.HTTP, AdapterConfiguration.AdapterModeEnum.INBOUND, config);
            logger.info("HTTP Inbound Adapter initialized for controller");
        } catch(AdapterException e) {
            logger.error("Failed to initialize HTTP Inbound Adapter", e);
            throw new RuntimeException("Failed to initialize HTTP Inbound Adapter", e);
        }
    }

    /**
     * Inbound POST endpoint to receive JSON payloads.
     * This endpoint acts as a webhook/receiver for external systems to send data to the middleware.
     *
     * @param payload Raw JSON payload as String
     * @return ResponseEntity with response JSON or error message
     */
    @PostMapping("/receive")
    public ResponseEntity<String> receivePayload(@RequestBody String payload) {
        try {
            logger.debug("Received inbound HTTP payload of length: {}", payload != null ? payload.length() : 0);

            // Use the inbound adapter to process the inbound request
            // In middleware terminology, inbound adapters receive data FROM external systems
            Map<String, Object> headers = new HashMap<>();
            headers.put("requestMethod", "POST");
            headers.put("contentType", "application/json");

            // Create a FetchRequest with the payload and headers
            Map<String, String> stringHeaders = new HashMap<>();
            headers.forEach((k, v) -> stringHeaders.put(k, v != null ? v.toString() : ""));

            FetchRequest fetchRequest = new FetchRequest();
        fetchRequest.setAdapterId("http-controller");
        fetchRequest.setHeaders(stringHeaders);

            // Add the payload as a parameter
            fetchRequest.addParameter("payload", payload);

            // Use the inbound adapter to process the inbound request
            AdapterOperationResult result = httpInboundAdapter.fetch(fetchRequest);

            if(result.isSuccess()) {
                String responsePayload = result.getData() != null ? result.getData().toString() : "OK";
                logger.debug("Successfully processed inbound payload");
                return ResponseEntity.ok(responsePayload);
            } else {
                logger.warn("Failed to process inbound payload: {}", result.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to process payload: " + result.getMessage());
            }

        } catch(Exception e) {
            logger.error("Error processing inbound HTTP payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process inbound payload: " + e.getMessage());
        }
    }

    /**
     * Connection test endpoint for health checks.
     *
     * @return ResponseEntity with connection status
     */
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        try {
            // Use the AbstractAdapter to access testConnection method
            AdapterOperationResult result;
            if(httpInboundAdapter instanceof AbstractAdapter) {
                AbstractAdapter abstractAdapter = (AbstractAdapter) httpInboundAdapter;
                result = abstractAdapter.testConnection(abstractAdapter.getConfiguration());
            } else {
                // Fallback: create a simple test by checking if adapter is ready
                result = httpInboundAdapter.isReady()
                    ? AdapterOperationResult.success("Adapter is ready")
                    : AdapterOperationResult.failure("Adapter is not ready");
            }

            if(result.isSuccess()) {
                return ResponseEntity.ok("Connection test successful: " + result.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Connection test failed: " + result.getMessage());
            }

        } catch(Exception e) {
            logger.error("Error testing HTTP adapter connection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Connection test error: " + e.getMessage());
        }
    }

    // Add additional endpoints(GET, PUT, DELETE) here if needed
}
