package com.integrixs.adapters.factory;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

/**
 * Default implementation of AdapterFactory that creates adapter instances
 * based on configuration objects.
 */
public class DefaultAdapterFactory implements AdapterFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAdapterFactory.class);

    @Override
    public InboundAdapterPort createInboundAdapter(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException {
        logger.debug("Creating inbound adapter for type: {}", adapterType);

        validateConfiguration(adapterType, configuration);

        try {
            InboundAdapterPort adapter = switch(adapterType) {
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
                case IBMMQ -> createIbmmqSender(configuration);
                case ODATA -> createOdataSender(configuration);
                case KAFKA -> createKafkaSender(configuration);
                default -> throw new AdapterException("Unsupported adapter type for inbound: " + adapterType);
            };

            logger.debug("Created inbound adapter of type: {}", adapterType);
            return adapter;

        } catch(Exception e) {
            throw new AdapterException("Failed to create inbound adapter: " + e.getMessage(), e);
        }
    }

    @Override
    public OutboundAdapterPort createOutboundAdapter(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException {
        logger.debug("Creating outbound adapter for type: {}", adapterType);

        validateConfiguration(adapterType, configuration);

        try {
            OutboundAdapterPort adapter = switch(adapterType) {
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
                case IBMMQ -> createIbmmqReceiver(configuration);
                case ODATA -> createOdataReceiver(configuration);
                case KAFKA -> createKafkaReceiver(configuration);
                default -> throw new AdapterException("Unsupported adapter type for outbound: " + adapterType);
            };

            logger.debug("Created outbound adapter of type: {}", adapterType);
            return adapter;

        } catch(Exception e) {
            throw new AdapterException("Failed to create outbound adapter: " + e.getMessage(), e);
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
        if(adapterType == null) {
            throw new AdapterException("Adapter type cannot be null");
        }
        if(configuration == null) {
            throw new AdapterException("Configuration cannot be null");
        }
    }

    // Factory methods for inbound adapters - these will be implemented as we build each adapter

    private InboundAdapterPort createHttpSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.HttpInboundAdapterConfig)) {
            throw new AdapterException("HTTP sender requires HttpInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.HttpInboundAdapter(
                (com.integrixs.adapters.config.HttpInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createJdbcSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.JdbcInboundAdapterConfig)) {
            throw new AdapterException("JDBC sender requires JdbcInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.JdbcInboundAdapter(
                (com.integrixs.adapters.config.JdbcInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createRestSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.RestInboundAdapterConfig)) {
            throw new AdapterException("REST sender requires RestInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RestInboundAdapter(
                (com.integrixs.adapters.config.RestInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createSoapSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.SoapInboundAdapterConfig)) {
            throw new AdapterException("SOAP sender requires SoapInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SoapInboundAdapter(
                (com.integrixs.adapters.config.SoapInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createFileSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.FileInboundAdapterConfig)) {
            throw new AdapterException("File sender requires FileInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FileInboundAdapter(
                (com.integrixs.adapters.config.FileInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createMailSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.MailInboundAdapterConfig)) {
            throw new AdapterException("Mail sender requires MailInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.MailInboundAdapter(
                (com.integrixs.adapters.config.MailInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createFtpSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.FtpInboundAdapterConfig)) {
            throw new AdapterException("FTP sender requires FtpInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FtpInboundAdapter(
                (com.integrixs.adapters.config.FtpInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createSftpSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.SftpInboundAdapterConfig)) {
            throw new AdapterException("SFTP sender requires SftpInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SftpInboundAdapter(
                (com.integrixs.adapters.config.SftpInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createRfcSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.RfcInboundAdapterConfig)) {
            throw new AdapterException("RFC sender requires RfcInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RfcInboundAdapter(
                (com.integrixs.adapters.config.RfcInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createIdocSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.IdocInboundAdapterConfig)) {
            throw new AdapterException("IDOC sender requires IdocInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.IdocInboundAdapter(
                (com.integrixs.adapters.config.IdocInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createIbmmqSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.IbmmqInboundAdapterConfig)) {
            throw new AdapterException("IBM MQ sender requires IbmmqInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.IbmmqInboundAdapter(
                (com.integrixs.adapters.config.IbmmqInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createOdataSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.OdataInboundAdapterConfig)) {
            throw new AdapterException("ODATA sender requires OdataInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.OdataInboundAdapter(
                (com.integrixs.adapters.config.OdataInboundAdapterConfig) configuration);
    }

    private InboundAdapterPort createKafkaSender(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.KafkaInboundAdapterConfig)) {
            throw new AdapterException("KAFKA sender requires KafkaInboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.KafkaInboundAdapter(
                (com.integrixs.adapters.config.KafkaInboundAdapterConfig) configuration);
    }

    // Factory methods for outbound adapters - these will be implemented as we build each adapter

    private OutboundAdapterPort createHttpReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.HttpOutboundAdapterConfig)) {
            throw new AdapterException("HTTP receiver requires HttpOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.HttpOutboundAdapter(
                (com.integrixs.adapters.config.HttpOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createJdbcReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.JdbcOutboundAdapterConfig)) {
            throw new AdapterException("JDBC receiver requires JdbcOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.JdbcOutboundAdapter(
                (com.integrixs.adapters.config.JdbcOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createRestReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.RestOutboundAdapterConfig)) {
            throw new AdapterException("REST receiver requires RestOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RestOutboundAdapter(
                (com.integrixs.adapters.config.RestOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createSoapReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.SoapOutboundAdapterConfig)) {
            throw new AdapterException("SOAP receiver requires SoapOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SoapOutboundAdapter(
                (com.integrixs.adapters.config.SoapOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createFileReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.FileOutboundAdapterConfig)) {
            throw new AdapterException("File receiver requires FileOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FileOutboundAdapter(
                (com.integrixs.adapters.config.FileOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createMailReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.MailOutboundAdapterConfig)) {
            throw new AdapterException("Mail receiver requires MailOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.MailOutboundAdapter(
                (com.integrixs.adapters.config.MailOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createFtpReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.FtpOutboundAdapterConfig)) {
            throw new AdapterException("FTP receiver requires FtpOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.FtpOutboundAdapter(
                (com.integrixs.adapters.config.FtpOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createSftpReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.SftpOutboundAdapterConfig)) {
            throw new AdapterException("SFTP receiver requires SftpOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.SftpOutboundAdapter(
                (com.integrixs.adapters.config.SftpOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createRfcReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.RfcOutboundAdapterConfig)) {
            throw new AdapterException("RFC receiver requires RfcOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.RfcOutboundAdapter(
                (com.integrixs.adapters.config.RfcOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createIdocReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.IdocOutboundAdapterConfig)) {
            throw new AdapterException("IDOC receiver requires IdocOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.IdocOutboundAdapter(
                (com.integrixs.adapters.config.IdocOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createIbmmqReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.IbmmqOutboundAdapterConfig)) {
            throw new AdapterException("IBM MQ receiver requires IbmmqOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.IbmmqOutboundAdapter(
                (com.integrixs.adapters.config.IbmmqOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createOdataReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.OdataOutboundAdapterConfig)) {
            throw new AdapterException("ODATA receiver requires OdataOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.OdataOutboundAdapter(
                (com.integrixs.adapters.config.OdataOutboundAdapterConfig) configuration);
    }

    private OutboundAdapterPort createKafkaReceiver(Object configuration) throws AdapterException {
        if(!(configuration instanceof com.integrixs.adapters.config.KafkaOutboundAdapterConfig)) {
            throw new AdapterException("KAFKA receiver requires KafkaOutboundAdapterConfig, got: " + configuration.getClass().getSimpleName());
        }
        return new com.integrixs.adapters.infrastructure.adapter.KafkaOutboundAdapter(
                (com.integrixs.adapters.config.KafkaOutboundAdapterConfig) configuration);
    }
}
