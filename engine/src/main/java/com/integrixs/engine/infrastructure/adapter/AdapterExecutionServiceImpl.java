package com.integrixs.engine.infrastructure.adapter;

import com.integrixs.engine.AdapterExecutor;
import com.integrixs.engine.domain.model.AdapterExecutionContext;
import com.integrixs.engine.domain.model.AdapterExecutionResult;
import com.integrixs.engine.domain.service.FlowAdapterExecutor;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure implementation of AdapterExecutionService
 * Bridges clean architecture with existing AdapterExecutor
 */
@Service
public class AdapterExecutionServiceImpl implements FlowAdapterExecutor {

    private static final Logger log = LoggerFactory.getLogger(AdapterExecutionServiceImpl.class);


    private final AdapterExecutor adapterExecutor;
    private final CommunicationAdapterSqlRepository communicationAdapterRepository;

    // Cache adapter capabilities
    private final Map<String, Map<String, Object>> capabilitiesCache = new ConcurrentHashMap<>();

    public AdapterExecutionServiceImpl(AdapterExecutor adapterExecutor, CommunicationAdapterSqlRepository communicationAdapterRepository) {
        this.adapterExecutor = adapterExecutor;
        this.communicationAdapterRepository = communicationAdapterRepository;
    }

    public AdapterExecutionResult fetchData(String adapterId, AdapterExecutionContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Fetching data from adapter {} with context {}", adapterId, context);

        try {
            // Add context to thread local if needed
            Map<String, Object> executionContext = new HashMap<>();
            executionContext.putAll(context.getParameters());
            executionContext.putAll(context.getMetadata());
            executionContext.put("executionId", context.getExecutionId());
            executionContext.put("flowId", context.getFlowId());
            executionContext.put("stepId", context.getStepId());
            executionContext.put("correlationId", context.getCorrelationId());

            // Fetch data using existing adapter executor
            Object data = adapterExecutor.fetchDataAsObject(adapterId);

            // Calculate execution time
            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();

            // Build success result
            AdapterExecutionResult result = AdapterExecutionResult.builder()
                    .executionId(context.getExecutionId())
                    .success(true)
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .adapterId(adapterId)
                    .adapterType(getAdapterType(adapterId))
                    .build();

            // Add execution metadata
            result.addMetadata("flowId", context.getFlowId());
            result.addMetadata("stepId", context.getStepId());
            result.addMetadata("correlationId", context.getCorrelationId());

            return result;

        } catch(Exception e) {
            log.error("Error fetching data from adapter {}: {}", adapterId, e.getMessage(), e);

            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();

            return AdapterExecutionResult.builder()
                    .executionId(context.getExecutionId())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorCode("ADAPTER_FETCH_ERROR")
                    .timestamp(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .adapterId(adapterId)
                    .adapterType(getAdapterType(adapterId))
                    .build();
        }
    }

    @Override
    public AdapterExecutionResult sendData(String adapterId, Object data, AdapterExecutionContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Sending data to adapter {} with context {}", adapterId, context);

        try {
            // Build execution context
            Map<String, Object> executionContext = new HashMap<>();
            executionContext.putAll(context.getParameters());
            executionContext.putAll(context.getMetadata());
            executionContext.put("executionId", context.getExecutionId());
            executionContext.put("flowId", context.getFlowId());
            executionContext.put("stepId", context.getStepId());
            executionContext.put("correlationId", context.getCorrelationId());
            executionContext.put("headers", context.getHeaders());

            // Send data using existing adapter executor
            if(data instanceof String) {
                adapterExecutor.sendData(adapterId, (String) data, executionContext);
            } else if(data instanceof byte[]) {
                adapterExecutor.sendData(adapterId, (byte[]) data);
            } else {
                adapterExecutor.sendData(adapterId, data);
            }

            // Calculate execution time
            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();

            // Build success result
            AdapterExecutionResult result = AdapterExecutionResult.builder()
                    .executionId(context.getExecutionId())
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .adapterId(adapterId)
                    .adapterType(getAdapterType(adapterId))
                    .build();

            // Add execution metadata
            result.addMetadata("flowId", context.getFlowId());
            result.addMetadata("stepId", context.getStepId());
            result.addMetadata("correlationId", context.getCorrelationId());
            result.addMetadata("dataSize", getDataSize(data));

            return result;

        } catch(Exception e) {
            log.error("Error sending data to adapter {}: {}", adapterId, e.getMessage(), e);

            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();

            return AdapterExecutionResult.builder()
                    .executionId(context.getExecutionId())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorCode("ADAPTER_SEND_ERROR")
                    .timestamp(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .adapterId(adapterId)
                    .adapterType(getAdapterType(adapterId))
                    .build();
        }
    }

    @Override
    public void validateAdapterConfig(String adapterId, Map<String, Object> config) {
        log.debug("Validating adapter config for adapter {}", adapterId);

        try {
            CommunicationAdapter adapter = communicationAdapterRepository
                    .findById(UUID.fromString(adapterId))
                    .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));

            // Validate based on adapter type
            switch(adapter.getType()) {
                case HTTP, HTTPS, REST:
                    validateHttpConfig(config);
                    break;
                case JDBC:
                    validateJdbcConfig(config);
                    break;
                case FTP, SFTP:
                    validateFtpConfig(config);
                    break;
                case IBMMQ, RABBITMQ, AMQP:
                    validateJmsConfig(config);
                    break;
                case SOAP:
                    validateSoapConfig(config);
                    break;
                case FILE:
                    validateFileConfig(config);
                    break;
                case MAIL, EMAIL:
                    validateMailConfig(config);
                    break;
                default:
                    log.warn("No specific validation for adapter type: {}", adapter.getType());
            }
        } catch(Exception e) {
            throw new IllegalArgumentException("Invalid adapter configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAdapterReady(String adapterId) {
        try {
            CommunicationAdapter adapter = communicationAdapterRepository
                    .findById(UUID.fromString(adapterId))
                    .orElse(null);

            if(adapter == null) {
                return false;
            }

            // Check if adapter is active
            if(!adapter.isActive()) {
                return false;
            }

            // Additional readiness checks based on adapter type
            return checkAdapterReadiness(adapter);

        } catch(Exception e) {
            log.error("Error checking adapter readiness for {}: {}", adapterId, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getAdapterCapabilities(String adapterId) {
        // Check cache first
        if(capabilitiesCache.containsKey(adapterId)) {
            return capabilitiesCache.get(adapterId);
        }

        try {
            CommunicationAdapter adapter = communicationAdapterRepository
                    .findById(UUID.fromString(adapterId))
                    .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));

            Map<String, Object> capabilities = new HashMap<>();

            // Common capabilities
            capabilities.put("type", adapter.getType().name());
            capabilities.put("direction", adapter.getDirection());
            capabilities.put("supportsAsync", supportsAsync(adapter.getType()));
            capabilities.put("supportsBatch", supportsBatch(adapter.getType()));
            capabilities.put("supportsStreaming", supportsStreaming(adapter.getType()));

            // Type - specific capabilities
            switch(adapter.getType()) {
                case HTTP, HTTPS, REST:
                    capabilities.put("supportedMethods", new String[] {"GET", "POST", "PUT", "DELETE", "PATCH"});
                    capabilities.put("supportsCustomHeaders", true);
                    capabilities.put("supportsAuthentication", true);
                    break;
                case JDBC:
                    capabilities.put("supportsTransactions", true);
                    capabilities.put("supportsBulkOperations", true);
                    capabilities.put("supportsStoredProcedures", true);
                    break;
                case FTP, SFTP:
                    capabilities.put("supportsDirectoryListing", true);
                    capabilities.put("supportsRecursive", true);
                    capabilities.put("supportsBinaryTransfer", true);
                    break;
                case IBMMQ, RABBITMQ, AMQP:
                    capabilities.put("supportsTopics", true);
                    capabilities.put("supportsQueues", true);
                    capabilities.put("supportsDurableSubscriptions", true);
                    break;
                case FILE:
                    capabilities.put("supportsPolling", true);
                    capabilities.put("supportsArchiving", true);
                    capabilities.put("supportsFileLocking", true);
                    break;
            }

            // Cache the capabilities
            capabilitiesCache.put(adapterId, capabilities);

            return capabilities;

        } catch(Exception e) {
            log.error("Error getting adapter capabilities for {}: {}", adapterId, e.getMessage());
            return new HashMap<>();
        }
    }

    private String getAdapterType(String adapterId) {
        try {
            return communicationAdapterRepository
                    .findById(UUID.fromString(adapterId))
                    .map(adapter -> adapter.getType().name())
                    .orElse("UNKNOWN");
        } catch(Exception e) {
            return "UNKNOWN";
        }
    }

    private long getDataSize(Object data) {
        if(data == null) return 0;
        if(data instanceof String) return((String) data).length();
        if(data instanceof byte[]) return((byte[]) data).length;
        return data.toString().length();
    }

    private boolean checkAdapterReadiness(CommunicationAdapter adapter) {
        // Basic readiness checks - configuration is stored as JSON
        // For now, just check that configuration exists
        return adapter.getConfiguration() != null && !adapter.getConfiguration().isEmpty();
    }

    private boolean supportsAsync(com.integrixs.shared.enums.AdapterType type) {
        return switch(type) {
            case IBMMQ, RABBITMQ, AMQP, KAFKA -> true;
            default -> false;
        };
    }

    private boolean supportsBatch(com.integrixs.shared.enums.AdapterType type) {
        return switch(type) {
            case JDBC, FILE -> true;
            default -> false;
        };
    }

    private boolean supportsStreaming(com.integrixs.shared.enums.AdapterType type) {
        return switch(type) {
            case FILE, FTP, SFTP, KAFKA -> true;
            default -> false;
        };
    }

    private void validateHttpConfig(Map<String, Object> config) {
        // Basic HTTP validation
        if(config.get("url") == null) {
            throw new IllegalArgumentException("HTTP adapter requires 'url' configuration");
        }
    }

    private void validateJdbcConfig(Map<String, Object> config) {
        // Basic JDBC validation
        if(config.get("databaseUrl") == null) {
            throw new IllegalArgumentException("JDBC adapter requires 'databaseUrl' configuration");
        }
        if(config.get("databaseDriver") == null) {
            throw new IllegalArgumentException("JDBC adapter requires 'databaseDriver' configuration");
        }
    }

    private void validateFtpConfig(Map<String, Object> config) {
        // Basic FTP validation
        if(config.get("host") == null) {
            throw new IllegalArgumentException("FTP adapter requires 'host' configuration");
        }
        if(config.get("port") == null) {
            throw new IllegalArgumentException("FTP adapter requires 'port' configuration");
        }
    }

    private void validateJmsConfig(Map<String, Object> config) {
        // Basic JMS validation
        if(config.get("connectionFactory") == null && config.get("brokerUrl") == null) {
            throw new IllegalArgumentException("JMS adapter requires 'connectionFactory' or 'brokerUrl' configuration");
        }
    }

    private void validateSoapConfig(Map<String, Object> config) {
        // Basic SOAP validation
        if(config.get("wsdlUrl") == null && config.get("endpoint") == null) {
            throw new IllegalArgumentException("SOAP adapter requires 'wsdlUrl' or 'endpoint' configuration");
        }
    }

    private void validateFileConfig(Map<String, Object> config) {
        // Basic File validation
        if(config.get("filePath") == null && config.get("directory") == null) {
            throw new IllegalArgumentException("File adapter requires 'filePath' or 'directory' configuration");
        }
    }

    private void validateMailConfig(Map<String, Object> config) {
        // Basic Mail validation
        if(config.get("host") == null) {
            throw new IllegalArgumentException("Mail adapter requires 'host' configuration");
        }
        if(config.get("port") == null) {
            throw new IllegalArgumentException("Mail adapter requires 'port' configuration");
        }
    }

    // Builder
    public static AdapterExecutionServiceImplBuilder builder() {
        return new AdapterExecutionServiceImplBuilder();
    }

    public static class AdapterExecutionServiceImplBuilder {
        private AdapterExecutor adapterExecutor;
        private CommunicationAdapterSqlRepository communicationAdapterRepository;

        public AdapterExecutionServiceImplBuilder adapterExecutor(AdapterExecutor adapterExecutor) {
            this.adapterExecutor = adapterExecutor;
            return this;
        }

        public AdapterExecutionServiceImplBuilder communicationAdapterRepository(CommunicationAdapterSqlRepository communicationAdapterRepository) {
            this.communicationAdapterRepository = communicationAdapterRepository;
            return this;
        }

        public AdapterExecutionServiceImpl build() {
            return new AdapterExecutionServiceImpl(this.adapterExecutor, this.communicationAdapterRepository);
        }
    }
}
