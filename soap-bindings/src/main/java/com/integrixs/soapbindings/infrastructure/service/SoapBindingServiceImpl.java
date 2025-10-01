package com.integrixs.soapbindings.infrastructure.service;

import com.integrixs.soapbindings.domain.model.GeneratedBinding;
import com.integrixs.soapbindings.domain.model.SoapBinding;
import com.integrixs.soapbindings.domain.model.SecurityConfiguration;
import com.integrixs.soapbindings.domain.model.WsdlDefinition;
import com.integrixs.soapbindings.domain.repository.SoapBindingRepository;
import com.integrixs.soapbindings.domain.service.SoapBindingService;
import com.integrixs.soapbindings.infrastructure.binding.BindingGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SOAP binding service implementation
 */
@Service
public class SoapBindingServiceImpl implements SoapBindingService {

    private static final Logger logger = LoggerFactory.getLogger(SoapBindingServiceImpl.class);

    private final SoapBindingRepository bindingRepository;
    private final BindingGenerator bindingGenerator;

    public SoapBindingServiceImpl(SoapBindingRepository bindingRepository, BindingGenerator bindingGenerator) {
        this.bindingRepository = bindingRepository;
        this.bindingGenerator = bindingGenerator;
    }

    @Override
    public GeneratedBinding generateBinding(WsdlDefinition wsdl, String packageName) {
        logger.debug("Generating binding for WSDL: {} with package: {}", wsdl.getName(), packageName);

        try {
            String outputDirectory = "target/generated - sources/soap";
            GeneratedBinding generated = bindingGenerator.generateFromWsdl(wsdl, packageName, outputDirectory);
            logger.info("Successfully generated binding for WSDL: {} with {} classes", wsdl.getName(), generated.getGeneratedClasses().size());
            return generated;
        } catch(Exception e) {
            logger.error("Failed to generate binding for WSDL {}: {}", wsdl.getName(), e.getMessage());
            throw new RuntimeException("Failed to generate binding", e);
        }
    }

    @Override
    public SoapBinding createBinding(SoapBinding binding) {
        logger.debug("Creating SOAP binding: {}", binding.getBindingName());

        // Check if binding name already exists
        if(bindingRepository.findByName(binding.getBindingName()).isPresent()) {
            logger.error("Binding already exists with name: {}", binding.getBindingName());
            throw new RuntimeException("Binding already exists: " + binding.getBindingName());
        }

        // Set default values
        if(binding.getBindingId() == null) {
            binding.setBindingId(UUID.randomUUID().toString());
        }
        if(binding.getCreatedAt() == null) {
            binding.setCreatedAt(LocalDateTime.now());
        }

        SoapBinding saved = bindingRepository.save(binding);
        logger.info("Successfully created SOAP binding: {} with ID: {}", saved.getBindingName(), saved.getBindingId());
        return saved;
    }

    @Override
    public SoapBinding updateBinding(SoapBinding updates) {
        logger.debug("Updating SOAP binding: {}", updates.getBindingId());

        SoapBinding existing = getBinding(updates.getBindingId());

        // Update fields
        if(updates.getBindingName() != null && !updates.getBindingName().equals(existing.getBindingName())) {
            // Check if new name is unique
            if(bindingRepository.findByName(updates.getBindingName()).isPresent()) {
                logger.error("Another binding exists with name: {}", updates.getBindingName());
                throw new RuntimeException("Another binding exists with name: " + updates.getBindingName());
            }
            existing.setBindingName(updates.getBindingName());
        }

        if(updates.getSecurity() != null) {
            existing.setSecurity(updates.getSecurity());
        }

        if(updates.getBindingStyle() != null) {
            existing.setBindingStyle(updates.getBindingStyle());
        }

        if(updates.getEncoding() != null) {
            existing.setEncoding(updates.getEncoding());
        }

        if(updates.getSoapVersion() != null) {
            existing.setSoapVersion(updates.getSoapVersion());
        }

        if(updates.isEnabled() != existing.isEnabled()) {
            existing.setEnabled(updates.isEnabled());
        }

        existing.setLastModified(LocalDateTime.now());

        SoapBinding saved = bindingRepository.save(existing);
        logger.info("Successfully updated SOAP binding: {} with ID: {}", saved.getBindingName(), saved.getBindingId());
        return saved;
    }

    @Override
    public SoapBinding getBinding(String bindingId) {
        logger.debug("Getting SOAP binding by ID: {}", bindingId);

        return bindingRepository.findById(bindingId)
                .orElseThrow(() -> {
                    logger.error("SOAP binding not found with ID: {}", bindingId);
                    return new RuntimeException("SOAP binding not found: " + bindingId);
                });
    }

    @Override
    public List<SoapBinding> getBindingsByWsdl(String wsdlId) {
        logger.debug("Getting SOAP bindings for WSDL: {}", wsdlId);

        List<SoapBinding> bindings = bindingRepository.findByWsdlId(wsdlId);
        logger.info("Found {} SOAP bindings for WSDL: {}", bindings.size(), wsdlId);
        return bindings;
    }

    @Override
    public List<SoapBinding> getActiveBindings() {
        logger.debug("Getting all active SOAP bindings");

        List<SoapBinding> bindings = bindingRepository.findAll().stream()
                .filter(SoapBinding::isActive)
                .collect(Collectors.toList());
        logger.info("Found {} active SOAP bindings", bindings.size());
        return bindings;
    }

    @Override
    public void activateBinding(String bindingId) {
        logger.debug("Activating SOAP binding: {}", bindingId);

        SoapBinding binding = getBinding(bindingId);
        binding.setActive(true);
        binding.setEnabled(true);
        binding.setLastModified(LocalDateTime.now());

        bindingRepository.save(binding);
        logger.info("Successfully activated SOAP binding: {}", bindingId);
    }

    @Override
    public void deactivateBinding(String bindingId) {
        logger.debug("Deactivating SOAP binding: {}", bindingId);

        SoapBinding binding = getBinding(bindingId);
        binding.setActive(false);
        binding.setEnabled(false);
        binding.setLastModified(LocalDateTime.now());

        bindingRepository.save(binding);
        logger.info("Successfully deactivated SOAP binding: {}", bindingId);
    }

    @Override
    public void deleteBinding(String bindingId) {
        logger.debug("Deleting SOAP binding: {}", bindingId);

        if(!bindingRepository.existsById(bindingId)) {
            logger.error("SOAP binding not found with ID: {}", bindingId);
            throw new RuntimeException("SOAP binding not found: " + bindingId);
        }

        bindingRepository.deleteById(bindingId);
        logger.info("Successfully deleted SOAP binding: {}", bindingId);
    }

    @Override
    public boolean testBindingConnectivity(String bindingId) {
        logger.debug("Testing connectivity for binding: {}", bindingId);

        try {
            SoapBinding binding = getBinding(bindingId);
            String testUrl = binding.getEndpointUrl();

            if(testUrl == null || testUrl.isEmpty()) {
                logger.warn("No endpoint URL configured for binding: {}", bindingId);
                return false;
            }

            URL url = new URL(testUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            boolean isReachable = responseCode >= 200 && responseCode < 300;
            logger.info("Connection test for binding {} resulted in: {} (HTTP {})",
                    bindingId, isReachable ? "success" : "failure", responseCode);

            return isReachable;

        } catch(Exception e) {
            logger.error("Connection test failed for binding {}: {}", bindingId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean compileBinding(GeneratedBinding generatedBinding) {
        logger.debug("Compiling generated binding: {}", generatedBinding.getBindingId());

        // In a real implementation, this would compile the generated Java classes
        // For now, we'll simulate success
        logger.warn("Binding compilation is simulated - actual compilation implementation required");
        return true;
    }

    @Override
    public ClassLoader loadBindingClasses(GeneratedBinding generatedBinding) {
        logger.debug("Loading binding classes for: {}", generatedBinding.getBindingId());

        // In a real implementation, this would create a custom class loader
        // to load the compiled binding classes
        logger.warn("Binding class loading is simulated - actual class loader implementation required");
        return Thread.currentThread().getContextClassLoader();
    }

    // Helper method to create all bindings
    public List<SoapBinding> getAllBindings() {
        logger.debug("Getting all SOAP bindings");

        List<SoapBinding> bindings = bindingRepository.findAll();
        logger.info("Found {} SOAP bindings", bindings.size());
        return bindings;
    }
}
