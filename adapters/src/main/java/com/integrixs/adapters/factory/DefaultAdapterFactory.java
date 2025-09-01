package com.integrixs.adapters.factory;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.domain.port.SenderAdapterPort;
import com.integrixs.adapters.domain.port.ReceiverAdapterPort;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of AdapterFactory that creates adapter instances
 * based on configuration objects.
 */
public class DefaultAdapterFactory implements AdapterFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultAdapterFactory.class);
    
    @Override
    public SenderAdapterPort createSender(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException {
        logger.debug("Creating sender adapter for type: {}", adapterType);
        
        validateConfiguration(adapterType, configuration);
        
        try {
            SenderAdapterPort adapter = switch (adapterType) {
                case HTTP -> createHttpSender(configuration);
                case JDBC -> createJdbcSender(configuration);
                case REST -> createRestSender(configuration);
                case SOAP -> createSoapSender(configuration);
                case FILE -> createFileSender(configuration);
                case MAIL -> createMailSender(configuration);
                case FTP -> createFtpSender(configuration);
                case SFTP -> createSftpSender(configuration);
                case RFC -> createRfcSender(configuration);
                case IDOC -> createIdocSender(configuration);
                case JMS -> createJmsSender(configuration);
                case ODATA -> createOdataSender(configuration);
                case KAFKA -> createKafkaSender(configuration);
                default -> throw new AdapterException.ConfigurationException(adapterType, 
                        "Unsupported sender adapter type: " + adapterType);
            };
            
            logger.debug("Successfully created sender adapter for type: {}", adapterType);
            return adapter;
            
        } catch (ClassCastException e) {
            throw new AdapterException.ConfigurationException(adapterType, 
                    "Invalid configuration type for " + adapterType + " sender", e);
        } catch (Exception e) {
            logger.error("Failed to create sender adapter for type: {}", adapterType, e);
            throw new AdapterException(adapterType, AdapterConfiguration.AdapterModeEnum.SENDER, 
                    "Failed to create sender adapter", e);
        }
    }
    
    @Override
    public ReceiverAdapterPort createReceiver(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException {
        logger.debug("Creating receiver adapter for type: {}", adapterType);
        
        validateConfiguration(adapterType, configuration);
        
        try {
            ReceiverAdapterPort adapter = switch (adapterType) {
                case HTTP -> createHttpReceiver(configuration);
                case JDBC -> createJdbcReceiver(configuration);
                case REST -> createRestReceiver(configuration);
                case SOAP -> createSoapReceiver(configuration);
                case FILE -> createFileReceiver(configuration);
                case MAIL -> createMailReceiver(configuration);
                case FTP -> createFtpReceiver(configuration);
                case SFTP -> createSftpReceiver(configuration);
                case RFC -> createRfcReceiver(configuration);
                case IDOC -> createIdocReceiver(configuration);
                case JMS -> createJmsReceiver(configuration);
                case ODATA -> createOdataReceiver(configuration);
                case KAFKA -> createKafkaReceiver(configuration);
                default -> throw new AdapterException.ConfigurationException(adapterType, 
                        "Unsupported receiver adapter type: " + adapterType);
            };
            
            logger.debug("Successfully created receiver adapter for type: {}", adapterType);
            return adapter;
            
        } catch (ClassCastException e) {
            throw new AdapterException.ConfigurationException(adapterType, 
                    "Invalid configuration type for " + adapterType + " receiver", e);
        } catch (Exception e) {
            logger.error("Failed to create receiver adapter for type: {}", adapterType, e);
            throw new AdapterException(adapterType, AdapterConfiguration.AdapterModeEnum.RECEIVER, 
                    "Failed to create receiver adapter", e);
        }
    }
    
    @Override
    public boolean supports(AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode) {
        // This factory supports all adapter types and modes
        return adapterType != null && adapterMode != null;
    }
    
    @Override
    public String getFactoryName() {
        return "DefaultAdapterFactory";
    }
    
    private void validateConfiguration(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException {
        if (adapterType == null) {
            throw new AdapterException.ConfigurationException(null, "Adapter type cannot be null");
        }
        if (configuration == null) {
            throw new AdapterException.ConfigurationException(adapterType, "Configuration cannot be null");
        }
    }
    
    // Factory methods for sender adapters - these will be implemented as we build each adapter
    
    private SenderAdapterPort createHttpSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.HttpSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.HTTP, 
                    "HTTP sender requires HttpSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.HttpSenderAdapter(
                (com.integrixs.adapters.config.HttpSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createJdbcSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.JdbcSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, 
                    "JDBC sender requires JdbcSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.JdbcSenderAdapter(
                (com.integrixs.adapters.config.JdbcSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createRestSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.RestSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.REST, 
                    "REST sender requires RestSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RestSenderAdapter(
                (com.integrixs.adapters.config.RestSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createSoapSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.SoapSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.SOAP, 
                    "SOAP sender requires SoapSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SoapSenderAdapter(
                (com.integrixs.adapters.config.SoapSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createFileSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.FileSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FILE, 
                    "File sender requires FileSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FileSenderAdapter(
                (com.integrixs.adapters.config.FileSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createMailSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.MailSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.MAIL, 
                    "Mail sender requires MailSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.MailSenderAdapter(
                (com.integrixs.adapters.config.MailSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createFtpSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.FtpSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                    "FTP sender requires FtpSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FtpSenderAdapter(
                (com.integrixs.adapters.config.FtpSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createSftpSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.SftpSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.SFTP, 
                    "SFTP sender requires SftpSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SftpSenderAdapter(
                (com.integrixs.adapters.config.SftpSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createRfcSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.RfcSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.RFC, 
                    "RFC sender requires RfcSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RfcSenderAdapter(
                (com.integrixs.adapters.config.RfcSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createIdocSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.IdocSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, 
                    "IDOC sender requires IdocSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.IdocSenderAdapter(
                (com.integrixs.adapters.config.IdocSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createJmsSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.JmsSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JMS, 
                    "JMS sender requires JmsSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.JmsSenderAdapter(
                (com.integrixs.adapters.config.JmsSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createOdataSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.OdataSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.ODATA, 
                    "ODATA sender requires OdataSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.OdataSenderAdapter(
                (com.integrixs.adapters.config.OdataSenderAdapterConfig) configuration);
    }
    
    private SenderAdapterPort createKafkaSender(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.KafkaSenderAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.KAFKA, 
                    "KAFKA sender requires KafkaSenderAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.KafkaSenderAdapter(
                (com.integrixs.adapters.config.KafkaSenderAdapterConfig) configuration);
    }
    
    // Factory methods for receiver adapters - these will be implemented as we build each adapter
    
    private ReceiverAdapterPort createHttpReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.HttpReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.HTTP, 
                    "HTTP receiver requires HttpReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.HttpReceiverAdapter(
                (com.integrixs.adapters.config.HttpReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createJdbcReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.JdbcReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, 
                    "JDBC receiver requires JdbcReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.JdbcReceiverAdapter(
                (com.integrixs.adapters.config.JdbcReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createRestReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.RestReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.REST, 
                    "REST receiver requires RestReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RestReceiverAdapter(
                (com.integrixs.adapters.config.RestReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createSoapReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.SoapReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.SOAP, 
                    "SOAP receiver requires SoapReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SoapReceiverAdapter(
                (com.integrixs.adapters.config.SoapReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createFileReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.FileReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FILE, 
                    "File receiver requires FileReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FileReceiverAdapter(
                (com.integrixs.adapters.config.FileReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createMailReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.MailReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.MAIL, 
                    "Mail receiver requires MailReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.MailReceiverAdapter(
                (com.integrixs.adapters.config.MailReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createFtpReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.FtpReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                    "FTP receiver requires FtpReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FtpReceiverAdapter(
                (com.integrixs.adapters.config.FtpReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createSftpReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.SftpReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.SFTP, 
                    "SFTP receiver requires SftpReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SftpReceiverAdapter(
                (com.integrixs.adapters.config.SftpReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createRfcReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.RfcReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.RFC, 
                    "RFC receiver requires RfcReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RfcReceiverAdapter(
                (com.integrixs.adapters.config.RfcReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createIdocReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.IdocReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, 
                    "IDOC receiver requires IdocReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.IdocReceiverAdapter(
                (com.integrixs.adapters.config.IdocReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createJmsReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.JmsReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JMS, 
                    "JMS receiver requires JmsReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.JmsReceiverAdapter(
                (com.integrixs.adapters.config.JmsReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createOdataReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.OdataReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.ODATA, 
                    "ODATA receiver requires OdataReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.OdataReceiverAdapter(
                (com.integrixs.adapters.config.OdataReceiverAdapterConfig) configuration);
    }
    
    private ReceiverAdapterPort createKafkaReceiver(Object configuration) throws AdapterException {
        if (!(configuration instanceof com.integrixs.adapters.config.KafkaReceiverAdapterConfig)) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.KAFKA, 
                    "KAFKA receiver requires KafkaReceiverAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.KafkaReceiverAdapter(
                (com.integrixs.adapters.config.KafkaReceiverAdapterConfig) configuration);
    }
}