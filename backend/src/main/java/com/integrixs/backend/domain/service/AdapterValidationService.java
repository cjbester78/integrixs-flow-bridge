package com.integrixs.backend.domain.service;

import com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.shared.enums.AdapterType;
import static com.integrixs.shared.enums.AdapterType.*;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Domain service for adapter validation logic
 */
@Service
public class AdapterValidationService {

    private final CommunicationAdapterSqlRepository adapterRepository;

    public AdapterValidationService(CommunicationAdapterSqlRepository adapterRepository) {
        this.adapterRepository = adapterRepository;
    }

    /**
     * Validates that adapter name is unique
     */
    public void validateAdapterNameUniqueness(String name, UUID excludeId) {
        if(excludeId == null) {
            if(adapterRepository.existsByName(name)) {
                throw new IllegalArgumentException("An adapter with the name '" + name + "' already exists");
            }
        } else {
            // Check if another adapter with same name exists (excluding the current one)
            boolean exists = adapterRepository.findAll().stream()
                .anyMatch(adapter -> adapter.getName().equals(name) && !adapter.getId().equals(excludeId));

            if(exists) {
                throw new IllegalArgumentException("An adapter with the name '" + name + "' already exists");
            }
        }
    }

    /**
     * Validates adapter configuration based on type and mode
     */
    public void validateAdapterConfiguration(AdapterType type, AdapterModeEnum mode, Map<String, Object> config) {
        if(config == null || config.isEmpty()) {
            throw new IllegalArgumentException("Adapter configuration cannot be empty");
        }

        // Validate based on adapter type
        switch(type) {
            case HTTP:
            case HTTPS:
                validateHttpConfiguration(config, mode);
                break;
            case JDBC:
                validateJdbcConfiguration(config, mode);
                break;
            case FTP:
            case SFTP:
                validateFtpConfiguration(config, mode);
                break;
            case SOAP:
                validateSoapConfiguration(config, mode);
                break;
            case FILE:
                validateFileConfiguration(config, mode);
                break;
            case EMAIL:
                validateEmailConfiguration(config, mode);
                break;
            case IBMMQ:
                validateJmsConfiguration(config, mode);
                break;
            case KAFKA:
                validateKafkaConfiguration(config, mode);
                break;
            case ODATA:
                validateOdataConfiguration(config, mode);
                break;
            default:
                throw new IllegalArgumentException("Unsupported adapter type: " + type);
        }
    }

    /**
     * Validates if adapter can be activated
     */
    public void validateAdapterActivation(CommunicationAdapter adapter) {
        if(adapter.getConfiguration() == null || adapter.getConfiguration().trim().isEmpty()) {
            throw new IllegalStateException("Adapter configuration must be complete before activation");
        }

        if(adapter.getBusinessComponent() == null) {
            throw new IllegalStateException("Adapter must be assigned to a business component before activation");
        }
    }

    private void validateHttpConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("url")) {
            throw new IllegalArgumentException("HTTP adapter requires 'url' configuration");
        }

        if(mode == AdapterModeEnum.OUTBOUND && !config.containsKey("method")) {
            throw new IllegalArgumentException("HTTP outbound adapter requires 'method' configuration");
        }
    }

    private void validateJdbcConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("driverClass") || !config.containsKey("jdbcUrl")) {
            throw new IllegalArgumentException("JDBC adapter requires 'driverClass' and 'jdbcUrl' configuration");
        }

        if(!config.containsKey("username") || !config.containsKey("password")) {
            throw new IllegalArgumentException("JDBC adapter requires database credentials");
        }

        if(mode == AdapterModeEnum.INBOUND && !config.containsKey("query")) {
            throw new IllegalArgumentException("JDBC inbound adapter requires 'query' configuration");
        }
    }

    private void validateFtpConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("host") || !config.containsKey("port")) {
            throw new IllegalArgumentException("FTP adapter requires 'host' and 'port' configuration");
        }

        if(!config.containsKey("username") || !config.containsKey("password")) {
            throw new IllegalArgumentException("FTP adapter requires credentials");
        }

        if(!config.containsKey("directory")) {
            throw new IllegalArgumentException("FTP adapter requires 'directory' configuration");
        }
    }

    private void validateSoapConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("wsdlUrl")) {
            throw new IllegalArgumentException("SOAP adapter requires 'wsdlUrl' configuration");
        }

        if(!config.containsKey("operation")) {
            throw new IllegalArgumentException("SOAP adapter requires 'operation' configuration");
        }
    }

    private void validateFileConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("directory")) {
            throw new IllegalArgumentException("File adapter requires 'directory' configuration");
        }

        if(mode == AdapterModeEnum.INBOUND && !config.containsKey("filePattern")) {
            throw new IllegalArgumentException("File inbound adapter requires 'filePattern' configuration");
        }
    }

    private void validateEmailConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(mode == AdapterModeEnum.INBOUND) {
            if(!config.containsKey("host") || !config.containsKey("port")) {
                throw new IllegalArgumentException("Email inbound adapter requires mail server configuration");
            }
        } else {
            if(!config.containsKey("smtpHost") || !config.containsKey("smtpPort")) {
                throw new IllegalArgumentException("Email outbound adapter requires SMTP configuration");
            }
        }
    }

    private void validateJmsConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("connectionFactory") || !config.containsKey("destination")) {
            throw new IllegalArgumentException("JMS adapter requires 'connectionFactory' and 'destination' configuration");
        }
    }

    private void validateKafkaConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("bootstrapServers") || !config.containsKey("topic")) {
            throw new IllegalArgumentException("Kafka adapter requires 'bootstrapServers' and 'topic' configuration");
        }
    }

    private void validateOdataConfiguration(Map<String, Object> config, AdapterModeEnum mode) {
        if(!config.containsKey("serviceUrl") || !config.containsKey("entitySet")) {
            throw new IllegalArgumentException("OData adapter requires 'serviceUrl' and 'entitySet' configuration");
        }
    }
}
