package com.integrixs.backend.controller;

import com.integrixs.backend.service.TestMappingService;
import com.integrixs.shared.dto.TestFieldMappingsRequestDTO;
import com.integrixs.shared.dto.TestFieldMappingsResponseDTO;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for testing field mappings without deploying flows
 */
@RestController
@RequestMapping("/api/test")
public class TestMappingController {

    private static final Logger log = LoggerFactory.getLogger(TestMappingController.class);


    private final TestMappingService testMappingService;

    public TestMappingController(TestMappingService testMappingService) {
        this.testMappingService = testMappingService;
    }

    /**
     * Test field mappings with sample XML
     */
    @PostMapping("/field - mappings")
    public ResponseEntity<TestFieldMappingsResponseDTO> testFieldMappings(
            @RequestBody TestFieldMappingsRequestDTO request) {
        log.info("Testing field mappings for type: {}", request.getMappingType());

        try {
            TestFieldMappingsResponseDTO response = testMappingService.testFieldMappings(request);
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            log.error("Error testing field mappings", e);
            return ResponseEntity.ok(TestFieldMappingsResponseDTO.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }
}
