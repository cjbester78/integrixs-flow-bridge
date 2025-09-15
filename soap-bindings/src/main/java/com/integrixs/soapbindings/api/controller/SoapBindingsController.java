package com.integrixs.soapbindings.api.controller;

import com.integrixs.soapbindings.api.dto.*;
import com.integrixs.soapbindings.application.service.SoapBindingsApplicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST controller for SOAP bindings operations
 */
@RestController
@RequestMapping("/api/soap - bindings")
public class SoapBindingsController {

    private static final Logger logger = LoggerFactory.getLogger(SoapBindingsController.class);

    private final SoapBindingsApplicationService applicationService;

    public SoapBindingsController(SoapBindingsApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // WSDL Management Endpoints

    /**
     * Upload WSDL file
     * @param file WSDL file
     * @param name WSDL name
     * @param description Optional description
     * @return WSDL details
     */
    @PostMapping(value = "/wsdl/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<WsdlDetailsDTO> uploadWsdl(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description) {

        logger.info("Uploading WSDL file: {}", file.getOriginalFilename());

        try {
            String wsdlContent = new String(file.getBytes(), StandardCharsets.UTF_8);

            UploadWsdlRequestDTO request = UploadWsdlRequestDTO.builder()
                    .name(name)
                    .wsdlContent(wsdlContent)
                    .location(file.getOriginalFilename())
                    .description(description)
                    .build();

            WsdlDetailsDTO wsdl = applicationService.uploadWsdl(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(wsdl);

        } catch(IOException e) {
            logger.error("Error reading WSDL file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch(Exception e) {
            logger.error("Error uploading WSDL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Import WSDL from URL
     * @param request Import request
     * @return WSDL details
     */
    @PostMapping("/wsdl/import")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<WsdlDetailsDTO> importWsdl(@Valid @RequestBody ImportWsdlRequestDTO request) {
        logger.info("Importing WSDL from URL: {}", request.getWsdlUrl());

        try {
            WsdlDetailsDTO wsdl = applicationService.importWsdlFromUrl(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(wsdl);
        } catch(Exception e) {
            logger.error("Error importing WSDL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get all WSDLs
     * @return List of WSDLs
     */
    @GetMapping("/wsdl")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<WsdlDetailsDTO>> getAllWsdls() {
        logger.info("Getting all WSDLs");

        try {
            List<WsdlDetailsDTO> wsdls = applicationService.getAllWsdls();
            return ResponseEntity.ok(wsdls);
        } catch(Exception e) {
            logger.error("Error getting WSDLs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get WSDL by ID
     * @param wsdlId WSDL ID
     * @return WSDL details
     */
    @GetMapping("/wsdl/ {wsdlId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<WsdlDetailsDTO> getWsdl(@PathVariable String wsdlId) {
        logger.info("Getting WSDL: {}", wsdlId);

        try {
            WsdlDetailsDTO wsdl = applicationService.getWsdl(wsdlId);
            return ResponseEntity.ok(wsdl);
        } catch(Exception e) {
            logger.error("Error getting WSDL {}: {}", wsdlId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete WSDL
     * @param wsdlId WSDL ID
     * @return No content
     */
    @DeleteMapping("/wsdl/ {wsdlId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<Void> deleteWsdl(@PathVariable String wsdlId) {
        logger.info("Deleting WSDL: {}", wsdlId);

        try {
            applicationService.deleteWsdl(wsdlId);
            return ResponseEntity.noContent().build();
        } catch(Exception e) {
            logger.error("Error deleting WSDL {}: {}", wsdlId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Binding Generation Endpoints

    /**
     * Generate SOAP binding from WSDL
     * @param wsdlId WSDL ID
     * @param request Generation request
     * @return Generated binding details
     */
    @PostMapping("/wsdl/ {wsdlId}/generate")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<GeneratedBindingDTO> generateBinding(
            @PathVariable String wsdlId,
            @Valid @RequestBody GenerateBindingRequestDTO request) {

        logger.info("Generating binding for WSDL: {}", wsdlId);

        try {
            GeneratedBindingDTO generated = applicationService.generateBinding(wsdlId, request);
            return ResponseEntity.ok(generated);
        } catch(Exception e) {
            logger.error("Error generating binding for WSDL {}: {}", wsdlId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Binding Configuration Endpoints

    /**
     * Create SOAP binding configuration
     * @param request Create binding request
     * @return Created binding
     */
    @PostMapping("/bindings")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<SoapBindingDTO> createBinding(@Valid @RequestBody CreateBindingRequestDTO request) {
        logger.info("Creating SOAP binding: {}", request.getBindingName());

        try {
            SoapBindingDTO binding = applicationService.createBinding(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(binding);
        } catch(Exception e) {
            logger.error("Error creating binding: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update SOAP binding
     * @param bindingId Binding ID
     * @param request Update request
     * @return Updated binding
     */
    @PutMapping("/bindings/ {bindingId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<SoapBindingDTO> updateBinding(
            @PathVariable String bindingId,
            @RequestBody UpdateBindingRequestDTO request) {

        logger.info("Updating SOAP binding: {}", bindingId);

        try {
            SoapBindingDTO binding = applicationService.updateBinding(bindingId, request);
            return ResponseEntity.ok(binding);
        } catch(Exception e) {
            logger.error("Error updating binding {}: {}", bindingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get all SOAP bindings
     * @return List of bindings
     */
    @GetMapping("/bindings")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<SoapBindingDTO>> getAllBindings() {
        logger.info("Getting all SOAP bindings");

        try {
            List<SoapBindingDTO> bindings = applicationService.getAllBindings();
            return ResponseEntity.ok(bindings);
        } catch(Exception e) {
            logger.error("Error getting bindings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get SOAP binding by ID
     * @param bindingId Binding ID
     * @return Binding details
     */
    @GetMapping("/bindings/ {bindingId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<SoapBindingDTO> getBinding(@PathVariable String bindingId) {
        logger.info("Getting SOAP binding: {}", bindingId);

        try {
            SoapBindingDTO binding = applicationService.getBinding(bindingId);
            return ResponseEntity.ok(binding);
        } catch(Exception e) {
            logger.error("Error getting binding {}: {}", bindingId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Test SOAP binding connectivity
     * @param bindingId Binding ID
     * @return Test result
     */
    @PostMapping("/bindings/ {bindingId}/test")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<BindingTestResultDTO> testBinding(@PathVariable String bindingId) {
        logger.info("Testing SOAP binding connectivity: {}", bindingId);

        try {
            BindingTestResultDTO result = applicationService.testBinding(bindingId);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            logger.error("Error testing binding {}: {}", bindingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // SOAP Operation Endpoints

    /**
     * Invoke SOAP operation
     * @param bindingId Binding ID
     * @param request Operation request
     * @return Operation response
     */
    @PostMapping("/bindings/ {bindingId}/invoke")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<SoapOperationResponseDTO> invokeOperation(
            @PathVariable String bindingId,
            @Valid @RequestBody SoapOperationRequestDTO request) {

        logger.info("Invoking SOAP operation {} on binding {}", request.getOperationName(), bindingId);

        try {
            SoapOperationResponseDTO response = applicationService.invokeOperation(bindingId, request);

            if(response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch(Exception e) {
            logger.error("Error invoking operation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("SOAP Bindings service is healthy");
    }
}
