package com.integrixs.soapbindings.application.service;

import com.integrixs.soapbindings.api.dto.*;
import com.integrixs.soapbindings.domain.model.*;
import com.integrixs.soapbindings.domain.enums.*;
import com.integrixs.soapbindings.domain.repository.GeneratedBindingRepository;
import com.integrixs.soapbindings.domain.repository.SoapBindingRepository;
import com.integrixs.soapbindings.domain.repository.WsdlRepository;
import com.integrixs.soapbindings.domain.service.SoapBindingService;
import com.integrixs.soapbindings.domain.service.SoapClientService;
import com.integrixs.soapbindings.domain.service.WsdlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service for orchestrating SOAP bindings operations
 */
@Service
public class SoapBindingsApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(SoapBindingsApplicationService.class);

    private final WsdlService wsdlService;
    private final SoapBindingService bindingService;
    private final SoapClientService clientService;
    private final WsdlRepository wsdlRepository;
    private final SoapBindingRepository bindingRepository;
    private final GeneratedBindingRepository generatedBindingRepository;

    public SoapBindingsApplicationService(
            WsdlService wsdlService,
            SoapBindingService bindingService,
            SoapClientService clientService,
            WsdlRepository wsdlRepository,
            SoapBindingRepository bindingRepository,
            GeneratedBindingRepository generatedBindingRepository) {
        this.wsdlService = wsdlService;
        this.bindingService = bindingService;
        this.clientService = clientService;
        this.wsdlRepository = wsdlRepository;
        this.bindingRepository = bindingRepository;
        this.generatedBindingRepository = generatedBindingRepository;
    }

    /**
     * Upload and parse WSDL
     * @param request Upload request
     * @return WSDL details
     */
    public WsdlDetailsDTO uploadWsdl(UploadWsdlRequestDTO request) {
        logger.info("Uploading WSDL: {}", request.getName());

        // Parse WSDL
        WsdlDefinition wsdl = wsdlService.parseWsdl(request.getWsdlContent(), request.getLocation());
        wsdl.setName(request.getName());

        // Validate WSDL
        if(!wsdlService.validateWsdl(wsdl)) {
            throw new RuntimeException("Invalid WSDL content");
        }

        // Save WSDL
        WsdlDefinition saved = wsdlService.saveWsdl(wsdl);

        return convertToWsdlDetailsDTO(saved);
    }

    /**
     * Import WSDL from URL
     * @param request Import request
     * @return WSDL details
     */
    public WsdlDetailsDTO importWsdlFromUrl(ImportWsdlRequestDTO request) {
        logger.info("Importing WSDL from URL: {}", request.getWsdlUrl());

        // Load WSDL from URL
        WsdlDefinition wsdl = wsdlService.loadWsdlFromUrl(request.getWsdlUrl());
        wsdl.setName(request.getName());

        // Save WSDL
        WsdlDefinition saved = wsdlService.saveWsdl(wsdl);

        return convertToWsdlDetailsDTO(saved);
    }

    /**
     * Generate SOAP binding from WSDL
     * @param wsdlId WSDL ID
     * @param request Generation request
     * @return Generated binding details
     */
    public GeneratedBindingDTO generateBinding(String wsdlId, GenerateBindingRequestDTO request) {
        logger.info("Generating binding for WSDL: {}", wsdlId);

        // Get WSDL
        WsdlDefinition wsdl = wsdlService.getWsdl(wsdlId);
        if(wsdl == null) {
            throw new RuntimeException("WSDL not found: " + wsdlId);
        }

        // Generate binding
        GeneratedBinding generated = bindingService.generateBinding(wsdl, request.getPackageName());

        // Save generated binding info
        generatedBindingRepository.save(generated);

        // Compile if requested
        if(request.isAutoCompile()) {
            boolean compiled = bindingService.compileBinding(generated);
            if(!compiled) {
                logger.warn("Failed to compile generated binding");
            }
        }

        return convertToGeneratedBindingDTO(generated);
    }

    /**
     * Create SOAP binding configuration
     * @param request Create binding request
     * @return Created binding
     */
    public SoapBindingDTO createBinding(CreateBindingRequestDTO request) {
        logger.info("Creating SOAP binding: {}", request.getBindingName());

        // Create domain model
        SoapBinding binding = SoapBinding.builder()
                .bindingName(request.getBindingName())
                .wsdlId(request.getWsdlId())
                .serviceName(request.getServiceName())
                .portName(request.getPortName())
                .endpointUrl(request.getEndpointUrl())
                .bindingStyle(SoapBinding.BindingStyle.valueOf(request.getBindingStyle()))
                .transport(SoapBinding.TransportProtocol.valueOf(request.getTransport()))
                .soapHeaders(request.getSoapHeaders())
                .security(convertSecurityConfig(request.getSecurity()))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        // Create binding
        SoapBinding created = bindingService.createBinding(binding);

        return convertToSoapBindingDTO(created);
    }

    /**
     * Update SOAP binding
     * @param bindingId Binding ID
     * @param request Update request
     * @return Updated binding
     */
    public SoapBindingDTO updateBinding(String bindingId, UpdateBindingRequestDTO request) {
        logger.info("Updating SOAP binding: {}", bindingId);

        // Get existing binding
        SoapBinding existing = bindingService.getBinding(bindingId);
        if(existing == null) {
            throw new RuntimeException("Binding not found: " + bindingId);
        }

        // Update fields
        updateBindingFields(existing, request);
        existing.setUpdatedAt(LocalDateTime.now());

        // Update binding
        SoapBinding updated = bindingService.updateBinding(existing);

        return convertToSoapBindingDTO(updated);
    }

    /**
     * Get all SOAP bindings
     * @return List of bindings
     */
    public List<SoapBindingDTO> getAllBindings() {
        return bindingRepository.findAll().stream()
                .map(this::convertToSoapBindingDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get SOAP binding by ID
     * @param bindingId Binding ID
     * @return Binding details
     */
    public SoapBindingDTO getBinding(String bindingId) {
        SoapBinding binding = bindingService.getBinding(bindingId);
        if(binding == null) {
            throw new RuntimeException("Binding not found: " + bindingId);
        }
        return convertToSoapBindingDTO(binding);
    }

    /**
     * Test SOAP binding connectivity
     * @param bindingId Binding ID
     * @return Test result
     */
    public BindingTestResultDTO testBinding(String bindingId) {
        logger.info("Testing SOAP binding connectivity: {}", bindingId);

        boolean isReachable = bindingService.testBindingConnectivity(bindingId);

        return BindingTestResultDTO.builder()
                .bindingId(bindingId)
                .reachable(isReachable)
                .timestamp(LocalDateTime.now())
                .message(isReachable ? "Service is reachable" : "Service is not reachable")
                .build();
    }

    /**
     * Invoke SOAP operation
     * @param bindingId Binding ID
     * @param request Invocation request
     * @return Operation response
     */
    public SoapOperationResponseDTO invokeOperation(String bindingId, SoapOperationRequestDTO request) {
        logger.info("Invoking SOAP operation {} on binding {}", request.getOperationName(), bindingId);

        // Get binding
        SoapBinding binding = bindingService.getBinding(bindingId);
        if(binding == null) {
            throw new RuntimeException("Binding not found: " + bindingId);
        }

        // Get generated binding for service class
        GeneratedBinding generatedBinding = generatedBindingRepository
                .findLatestByWsdlId(binding.getWsdlId())
                .orElseThrow(() -> new RuntimeException("No generated binding found for WSDL"));

        // Load service class
        ClassLoader classLoader = bindingService.loadBindingClasses(generatedBinding);
        Class<?> serviceClass = loadServiceClass(generatedBinding, classLoader);

        // Create client
        Object client = clientService.createClient(binding, serviceClass);

        // Configure headers if provided
        if(request.getSoapHeaders() != null && !request.getSoapHeaders().isEmpty()) {
            clientService.setSoapHeaders(client, request.getSoapHeaders());
        }

        try {
            // Invoke operation
            Object response = clientService.invokeOperation(client, request.getOperationName(), request.getPayload());

            return SoapOperationResponseDTO.builder()
                    .operationId(UUID.randomUUID().toString())
                    .success(true)
                    .response(response)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch(Exception e) {
            logger.error("Error invoking SOAP operation: {}", e.getMessage(), e);

            return SoapOperationResponseDTO.builder()
                    .operationId(UUID.randomUUID().toString())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Get all WSDLs
     * @return List of WSDLs
     */
    public List<WsdlDetailsDTO> getAllWsdls() {
        return wsdlRepository.findAll().stream()
                .map(this::convertToWsdlDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get WSDL by ID
     * @param wsdlId WSDL ID
     * @return WSDL details
     */
    public WsdlDetailsDTO getWsdl(String wsdlId) {
        WsdlDefinition wsdl = wsdlService.getWsdl(wsdlId);
        if(wsdl == null) {
            throw new RuntimeException("WSDL not found: " + wsdlId);
        }
        return convertToWsdlDetailsDTO(wsdl);
    }

    /**
     * Delete WSDL
     * @param wsdlId WSDL ID
     */
    public void deleteWsdl(String wsdlId) {
        logger.info("Deleting WSDL: {}", wsdlId);

        // Check if any active bindings exist
        List<SoapBinding> bindings = bindingRepository.findByWsdlId(wsdlId);
        if(!bindings.isEmpty()) {
            throw new RuntimeException("Cannot delete WSDL with active bindings");
        }

        // Delete WSDL
        wsdlService.deleteWsdl(wsdlId);
    }

    // Helper methods

    private WsdlDetailsDTO convertToWsdlDetailsDTO(WsdlDefinition wsdl) {
        return WsdlDetailsDTO.builder()
                .wsdlId(wsdl.getWsdlId())
                .name(wsdl.getName())
                .namespace(wsdl.getNamespace())
                .location(wsdl.getLocation())
                .type(wsdl.getType() != null ? wsdl.getType().name() : null)
                .services(wsdl.getServiceNames())
                .version(wsdl.getVersion())
                .validated(wsdl.isValidated())
                .build();
    }

    private SoapBindingDTO convertToSoapBindingDTO(SoapBinding binding) {
        return SoapBindingDTO.builder()
                .bindingId(binding.getBindingId())
                .bindingName(binding.getBindingName())
                .wsdlId(binding.getWsdlId())
                .serviceName(binding.getServiceName())
                .portName(binding.getPortName())
                .endpointUrl(binding.getEndpointUrl())
                .bindingStyle(binding.getBindingStyle().name())
                .transport(binding.getTransport().name())
                .active(binding.isActive())
                .requiresAuth(binding.requiresAuthentication())
                .secureTransport(binding.isSecureTransport())
                .createdAt(binding.getCreatedAt())
                .updatedAt(binding.getUpdatedAt())
                .build();
    }

    private GeneratedBindingDTO convertToGeneratedBindingDTO(GeneratedBinding generated) {
        return GeneratedBindingDTO.builder()
                .generationId(generated.getGenerationId())
                .wsdlId(generated.getWsdlId())
                .packageName(generated.getPackageName())
                .serviceName(generated.getServiceName())
                .status(generated.getStatus() != null ? generated.getStatus().name() : null)
                .generatedClasses(generated.getGeneratedClassesContent() != null ?
                        generated.getGeneratedClassesContent().keySet() : new HashSet<>())
                .generatedAt(generated.getGeneratedAt())
                .successful(generated.isSuccessful())
                .errorMessage(generated.getErrorMessage())
                .build();
    }

    private SoapBinding.SecurityConfiguration convertSecurityConfig(SecurityConfigurationDTO dto) {
        if(dto == null) return null;

        SoapBinding.SecurityConfiguration.Builder builder = SoapBinding.SecurityConfiguration.builder()
                .securityType(SecurityType.valueOf(dto.getSecurityType()))
                .credentials(dto.getCredentials());

        if(dto.getWsSecurityConfig() != null) {
            builder.wsSecurityConfig(WsSecurityConfig.builder()
                    .enableTimestamp(dto.getWsSecurityConfig().isEnableTimestamp())
                    .enableSignature(dto.getWsSecurityConfig().isEnableSignature())
                    .enableEncryption(dto.getWsSecurityConfig().isEnableEncryption())
                    .usernameToken(dto.getWsSecurityConfig().getUsernameToken())
                    .passwordType(dto.getWsSecurityConfig().getPasswordType())
                    .timestampTTL(dto.getWsSecurityConfig().getTimestampTTL())
                    .build());
        }

        return builder.build();
    }

    private void updateBindingFields(SoapBinding binding, UpdateBindingRequestDTO dto) {
        if(dto.getEndpointUrl() != null) {
            binding.setEndpointUrl(dto.getEndpointUrl());
        }
        if(dto.getSoapHeaders() != null) {
            binding.setSoapHeaders(dto.getSoapHeaders());
        }
        if(dto.getSecurity() != null) {
            binding.setSecurity(convertSecurityConfig(dto.getSecurity()));
        }
        if(dto.getActive() != null) {
            binding.setActive(dto.getActive());
        }
    }

    private Class<?> loadServiceClass(GeneratedBinding generatedBinding, ClassLoader classLoader) {
        ClassInfo serviceInfo = generatedBinding.getServiceInterface();
        if(serviceInfo == null) {
            throw new RuntimeException("No service interface found in generated binding");
        }

        try {
            return classLoader.loadClass(serviceInfo.getFullQualifiedName());
        } catch(ClassNotFoundException e) {
            throw new RuntimeException("Failed to load service class: " + serviceInfo.getFullQualifiedName(), e);
        }
    }
}
