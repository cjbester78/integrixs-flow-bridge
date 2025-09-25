package com.integrixs.backend.api.controllers;

import com.integrixs.backend.api.dto.request.StructureCompatibilityRequest;
import com.integrixs.backend.api.dto.response.StructureCompatibilityResponse;
import com.integrixs.backend.service.StructureCompatibilityService;
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
@RequestMapping("/api/v1/structures/compatibility")
@Validated
@Tag(name = "Structure Compatibility", description = "API for analyzing compatibility between data structures")
public class StructureCompatibilityController {

    private static final Logger log = LoggerFactory.getLogger(StructureCompatibilityController.class);


    private final StructureCompatibilityService compatibilityService;

    public StructureCompatibilityController(StructureCompatibilityService compatibilityService) {
        this.compatibilityService = compatibilityService;
    }

    @PostMapping("/analyze")
    @Operation(
            summary = "Analyze structure compatibility",
            description = "Analyzes compatibility between source and target data structures(WSDL, JSON Schema, XSD)"
   )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compatibility analysis completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StructureCompatibilityResponse.class)
                   )
           ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(mediaType = "application/json")
           ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
           )
    })
    public ResponseEntity<StructureCompatibilityResponse> analyzeCompatibility(
            @Valid @RequestBody StructureCompatibilityRequest request) {

        log.info("Analyzing compatibility between {} and {} structures",
                request.getSourceType(), request.getTargetType());

        try {
            StructureCompatibilityResponse response = compatibilityService.analyzeCompatibility(request);

            log.info("Compatibility analysis complete: {}% compatible, {} issues found",
                    response.getOverallCompatibility(), response.getIssues().size());

            return ResponseEntity.ok(response);

        } catch(IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    StructureCompatibilityResponse.builder()
                            .overallCompatibility(0)
                            .isCompatible(false)
                            .build()
           );
        } catch(Exception e) {
            log.error("Error analyzing structure compatibility", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    StructureCompatibilityResponse.builder()
                            .overallCompatibility(0)
                            .isCompatible(false)
                            .build()
           );
        }
    }
}
