package com.integrixs.backend.api.controllers;

import com.integrixs.backend.api.dto.request.ConnectionTestRequest;
import com.integrixs.backend.api.dto.response.ConnectionTestResponse;
import com.integrixs.backend.service.AdapterConnectionTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/adapters/connection")
@Validated
@Tag(name = "Adapter Connection Testing", description = "API for testing adapter connections")
public class AdapterConnectionTestController {

    private static final Logger log = LoggerFactory.getLogger(AdapterConnectionTestController.class);


    private final AdapterConnectionTestService connectionTestService;

    public AdapterConnectionTestController(AdapterConnectionTestService connectionTestService) {
        this.connectionTestService = connectionTestService;
    }

    @PostMapping("/test")
    @Operation(
            summary = "Test adapter connection",
            description = "Tests the connection for a specific adapter type with provided configuration"
   )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Connection test completed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConnectionTestResponse.class)
                   )
           ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(mediaType = "application/json")
           ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during connection test",
                    content = @Content(mediaType = "application/json")
           )
    })
    public ResponseEntity<ConnectionTestResponse> testConnection(
            @Valid @RequestBody ConnectionTestRequest request) {

        log.info("Testing connection for adapter: {} ( {})",
                request.getAdapterName(), request.getAdapterType());

        try {
            ConnectionTestResponse response = connectionTestService.testConnection(request);

            log.info("Connection test completed for {}: success = {}, duration = {}ms",
                    request.getAdapterName(), response.isSuccess(), response.getDuration());

            // Return 200 OK regardless of test success - the success field indicates the result
            return ResponseEntity.ok(response);

        } catch(UnsupportedOperationException e) {
            log.error("Unsupported adapter type: {}", request.getAdapterType());
            return ResponseEntity.badRequest().body(
                    ConnectionTestResponse.builder()
                            .success(false)
                            .message("Unsupported adapter type: " + request.getAdapterType())
                            .duration(0)
                            .build()
           );
        } catch(Exception e) {
            log.error("Error during connection test", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ConnectionTestResponse.builder()
                            .success(false)
                            .message("Internal error during connection test: " + e.getMessage())
                            .duration(0)
                            .build()
           );
        }
    }

    @GetMapping("/test/supported - types")
    @Operation(
            summary = "Get supported adapter types",
            description = "Returns a list of adapter types that support connection testing"
   )
    public ResponseEntity<String[]> getSupportedTypes() {
        // Return adapter types that have connection test implementation
        String[] supportedTypes = {
                "REST", "SOAP", "DATABASE", "JMS", "RABBITMQ",
                "FILE", "KAFKA", "SFTP", "EMAIL"
        };
        return ResponseEntity.ok(supportedTypes);
    }
}
