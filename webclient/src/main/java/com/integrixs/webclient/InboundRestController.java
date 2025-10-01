package com.integrixs.webclient;

import com.integrixs.webclient.api.dto.InboundMessageRequestDTO;
import com.integrixs.webclient.api.dto.InboundMessageResponseDTO;
import com.integrixs.webclient.application.service.WebClientApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Legacy Inbound REST Controller for backward compatibility.
 * New implementations should use WebClientController at /api/webclient
 *
 * @deprecated Use {@link com.integrixs.webclient.api.controller.WebClientController} instead
 */
@RestController
@RequestMapping("/api/inbound")
@Deprecated
public class InboundRestController {

    private static final Logger logger = LoggerFactory.getLogger(InboundRestController.class);
    private final WebClientApplicationService webClientService;

    public InboundRestController(WebClientApplicationService webClientService) {
        this.webClientService = webClientService;
    }

    /**
     * Generic webhook endpoint for receiving inbound HTTP POST requests
     */
    @PostMapping("/webhook/ {adapterId}")
    public ResponseEntity<String> receiveWebhook(
            @PathVariable String adapterId,
            @RequestBody String payload,
            HttpServletRequest request) {

        logger.info("Received inbound webhook for adapter: {}", adapterId);

        try {
            // Convert to new DTO format
            InboundMessageRequestDTO messageRequest = InboundMessageRequestDTO.builder()
                    .messageType("WEBHOOK")
                    .source(request.getRemoteAddr())
                    .adapterId(adapterId)
                    .payload(payload)
                    .contentType(request.getContentType())
                    .headers(extractHeaders(request))
                    .build();

            // Process through new service
            InboundMessageResponseDTO response = webClientService.receiveMessage(messageRequest);

            if(response.isSuccess()) {
                logger.info("Successfully processed inbound message for adapter: {}", adapterId);
                return ResponseEntity.ok("Message processed successfully");
            } else {
                logger.error("Failed to process inbound message for adapter {}: {}",
                        adapterId, response.getError());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to process message: " + response.getError());
            }

        } catch(Exception e) {
            logger.error("Error processing inbound webhook for adapter: {}", adapterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Generic GET endpoint for inbound requests
     */
    @GetMapping("/data/ {adapterId}")
    public ResponseEntity<String> receiveGetRequest(
            @PathVariable String adapterId,
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {

        logger.info("Received inbound GET request for adapter: {}", adapterId);

        try {
            // Convert to new DTO format
            InboundMessageRequestDTO messageRequest = InboundMessageRequestDTO.builder()
                    .messageType("API_CALL")
                    .source(request.getRemoteAddr())
                    .adapterId(adapterId)
                    .payload(params)
                    .contentType("application/x-www-form-urlencoded")
                    .headers(extractHeaders(request))
                    .build();

            // Process through new service
            InboundMessageResponseDTO response = webClientService.receiveMessage(messageRequest);

            if(response.isSuccess()) {
                logger.info("Successfully processed inbound GET request for adapter: {}", adapterId);
                return ResponseEntity.ok(response.getResponseData() != null ?
                        response.getResponseData().toString() : "Success");
            } else {
                logger.error("Failed to process inbound GET request for adapter {}: {}",
                        adapterId, response.getError());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to process request: " + response.getError());
            }

        } catch(Exception e) {
            logger.error("Error processing inbound GET request for adapter: {}", adapterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Inbound webclient is healthy");
    }

    /**
     * Extract headers from HTTP request
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }
}
