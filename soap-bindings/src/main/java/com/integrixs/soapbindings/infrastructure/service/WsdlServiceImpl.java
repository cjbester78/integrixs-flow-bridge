package com.integrixs.soapbindings.infrastructure.service;

import com.integrixs.soapbindings.domain.model.*;
import com.integrixs.soapbindings.domain.enums.*;
import com.integrixs.soapbindings.domain.repository.WsdlRepository;
import com.integrixs.soapbindings.domain.service.WsdlService;
import com.integrixs.soapbindings.infrastructure.wsdl.WsdlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * WSDL service implementation
 */
@Service
public class WsdlServiceImpl implements WsdlService {

    private static final Logger logger = LoggerFactory.getLogger(WsdlServiceImpl.class);

    private final WsdlParser wsdlParser;
    private final WsdlRepository wsdlRepository;

    public WsdlServiceImpl(WsdlParser wsdlParser, WsdlRepository wsdlRepository) {
        this.wsdlParser = wsdlParser;
        this.wsdlRepository = wsdlRepository;
    }

    @Override
    public WsdlDefinition parseWsdl(String wsdlContent, String location) {
        logger.debug("Parsing WSDL from location: {}", location);

        try {
            WsdlDefinition wsdl = wsdlParser.parse(wsdlContent, location);
            logger.info("Successfully parsed WSDL: {} with {} services", wsdl.getName(), wsdl.getServices().size());
            return wsdl;
        } catch(Exception e) {
            logger.error("Failed to parse WSDL from location {}: {}", location, e.getMessage());
            throw new RuntimeException("Failed to parse WSDL", e);
        }
    }

    @Override
    public WsdlDefinition loadWsdlFromUrl(String wsdlUrl) {
        logger.info("Loading WSDL from URL: {}", wsdlUrl);

        try {
            String wsdlContent = fetchWsdlContent(wsdlUrl);
            return parseWsdl(wsdlContent, wsdlUrl);
        } catch(Exception e) {
            logger.error("Failed to load WSDL from URL {}: {}", wsdlUrl, e.getMessage());
            throw new RuntimeException("Failed to load WSDL from URL", e);
        }
    }

    @Override
    public WsdlDefinition loadWsdlFromFile(String filePath) {
        logger.info("Loading WSDL from file: {}", filePath);

        try {
            Path path = Paths.get(filePath);
            String wsdlContent = Files.readString(path);
            return parseWsdl(wsdlContent, filePath);
        } catch(Exception e) {
            logger.error("Failed to load WSDL from file {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Failed to load WSDL from file", e);
        }
    }

    @Override
    public boolean validateWsdl(WsdlDefinition wsdl) {
        logger.debug("Validating WSDL: {}", wsdl.getName());

        try {
            // Basic validation checks
            if(wsdl.getWsdlId() == null || wsdl.getWsdlId().isEmpty()) {
                logger.warn("WSDL validation failed: Missing WSDL ID");
                return false;
            }

            if(wsdl.getContent() == null || wsdl.getContent().isEmpty()) {
                logger.warn("WSDL validation failed: Empty content");
                return false;
            }

            if(wsdl.getServices() == null || wsdl.getServices().isEmpty()) {
                logger.warn("WSDL validation failed: No services defined");
                return false;
            }

            // Try to parse the content to validate structure
            wsdlParser.parse(wsdl.getContent(), wsdl.getLocation());

            logger.info("WSDL validation successful for: {}", wsdl.getName());
            return true;
        } catch(Exception e) {
            logger.warn("WSDL validation failed for {}: {}", wsdl.getName(), e.getMessage());
            return false;
        }
    }

    @Override
    public WsdlDefinition saveWsdl(WsdlDefinition wsdl) {
        logger.debug("Saving WSDL: {}", wsdl.getName());

        // Set ID if not present
        if(wsdl.getWsdlId() == null) {
            wsdl.setWsdlId(UUID.randomUUID().toString());
        }

        WsdlDefinition saved = wsdlRepository.save(wsdl);
        logger.info("Successfully saved WSDL: {} with ID: {}", saved.getName(), saved.getWsdlId());
        return saved;
    }

    @Override
    public WsdlDefinition getWsdl(String wsdlId) {
        logger.debug("Getting WSDL by ID: {}", wsdlId);

        return wsdlRepository.findById(wsdlId)
                .orElseThrow(() -> {
                    logger.error("WSDL not found with ID: {}", wsdlId);
                    return new RuntimeException("WSDL not found: " + wsdlId);
                });
    }

    @Override
    public List<WsdlDefinition> getAllWsdls() {
        logger.debug("Getting all WSDLs");

        List<WsdlDefinition> wsdls = wsdlRepository.findAll();
        logger.info("Found {} WSDLs", wsdls.size());
        return wsdls;
    }

    @Override
    public WsdlDefinition updateWsdl(WsdlDefinition wsdl) {
        logger.debug("Updating WSDL: {}", wsdl.getWsdlId());

        // Verify WSDL exists
        if(!wsdlRepository.existsById(wsdl.getWsdlId())) {
            logger.error("WSDL not found for update: {}", wsdl.getWsdlId());
            throw new RuntimeException("WSDL not found: " + wsdl.getWsdlId());
        }

        WsdlDefinition updated = wsdlRepository.save(wsdl);
        logger.info("Successfully updated WSDL: {} with ID: {}", updated.getName(), updated.getWsdlId());
        return updated;
    }

    @Override
    public void deleteWsdl(String wsdlId) {
        logger.debug("Deleting WSDL: {}", wsdlId);

        if(!wsdlRepository.existsById(wsdlId)) {
            logger.error("WSDL not found with ID: {}", wsdlId);
            throw new RuntimeException("WSDL not found: " + wsdlId);
        }

        wsdlRepository.deleteById(wsdlId);
        logger.info("Successfully deleted WSDL: {}", wsdlId);
    }

    @Override
    public List<String> extractServiceNames(WsdlDefinition wsdl) {
        logger.debug("Extracting service names from WSDL: {}", wsdl.getName());

        List<String> serviceNames = new ArrayList<>(wsdl.getServiceNames());
        logger.info("Extracted {} service names from WSDL: {}", serviceNames.size(), wsdl.getName());
        return serviceNames;
    }

    @Override
    public List<String> extractOperations(WsdlDefinition wsdl, String serviceName) {
        logger.debug("Extracting operations from WSDL: {} for service: {}", wsdl.getName(), serviceName);

        ServiceDefinition service = wsdl.getService(serviceName);
        if(service == null) {
            logger.warn("Service not found in WSDL: {}", serviceName);
            return new ArrayList<>();
        }

        List<String> operations = service.getPorts().stream()
                .flatMap(port -> port.getOperations().stream().map(OperationDefinition::getName))
                .distinct()
                .collect(Collectors.toList());

        logger.info("Extracted {} operations from service: {}", operations.size(), serviceName);
        return operations;
    }

    private String fetchWsdlContent(String wsdlUrl) throws Exception {
        URL url = new URL(wsdlUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } finally {
            connection.disconnect();
        }
    }
}
