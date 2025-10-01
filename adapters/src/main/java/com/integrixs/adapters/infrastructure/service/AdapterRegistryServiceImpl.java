package com.integrixs.adapters.infrastructure.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterMetadata;
import com.integrixs.adapters.domain.port.AdapterPort;
import com.integrixs.adapters.domain.service.AdapterRegistryService;
import com.integrixs.adapters.infrastructure.adapter.*;
import com.integrixs.adapters.config.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of adapter registry service
 */
@Service
public class AdapterRegistryServiceImpl implements AdapterRegistryService {
    private static final Logger log = LoggerFactory.getLogger(AdapterRegistryServiceImpl.class);


    private final Map<String, AdapterPort> adapterRegistry = new ConcurrentHashMap<>();
    private final Map<String, AdapterMetadata> metadataCache = new ConcurrentHashMap<>();

    public AdapterRegistryServiceImpl() {
        // Initialize metadata for supported adapters
        initializeMetadata();
    }

    @Override
    public void registerAdapter(String adapterId, AdapterPort adapter) {
        log.info("Registering adapter: {}", adapterId);
        adapterRegistry.put(adapterId, adapter);
    }

    @Override
    public void unregisterAdapter(String adapterId) {
        log.info("Unregistering adapter: {}", adapterId);
        AdapterPort adapter = adapterRegistry.remove(adapterId);
        if(adapter != null) {
            try {
                adapter.shutdown();
            } catch(Exception e) {
                log.error("Error shutting down adapter: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public Optional<AdapterPort> getAdapter(String adapterId) {
        return Optional.ofNullable(adapterRegistry.get(adapterId));
    }

    @Override
    public AdapterMetadata getAdapterMetadata(
            AdapterConfiguration.AdapterTypeEnum adapterType,
            AdapterConfiguration.AdapterModeEnum adapterMode) {

        String key = adapterType.name() + "_" + adapterMode.name();
        return metadataCache.getOrDefault(key, createDefaultMetadata(adapterType, adapterMode));
    }

    @Override
    public List<String> listRegisteredAdapters() {
        return new ArrayList<>(adapterRegistry.keySet());
    }

    @Override
    public List<String> listAdaptersByType(AdapterConfiguration.AdapterTypeEnum adapterType) {
        return adapterRegistry.entrySet().stream()
                .filter(entry -> {
                    // For now, we can't filter by type without getConfiguration
                    // This would need to be tracked separately
                    return true;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAdapterRegistered(String adapterId) {
        return adapterRegistry.containsKey(adapterId);
    }

    @Override
    public AdapterPort createAdapter(AdapterConfiguration configuration) {
        log.info("Creating adapter instance for type: {} mode: {}",
                configuration.getAdapterType(), configuration.getAdapterMode());

        switch(configuration.getAdapterType()) {
            case HTTP:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    HttpInboundAdapterConfig httpSenderConfig = createHttpSenderConfig(configuration);
                    return new HttpInboundAdapter(httpSenderConfig);
                } else {
                    HttpOutboundAdapterConfig httpReceiverConfig = createHttpReceiverConfig(configuration);
                    return new HttpOutboundAdapter(httpReceiverConfig);
                }

            case JDBC:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    JdbcInboundAdapterConfig jdbcSenderConfig = createJdbcSenderConfig(configuration);
                    return new JdbcInboundAdapter(jdbcSenderConfig);
                } else {
                    JdbcOutboundAdapterConfig jdbcReceiverConfig = createJdbcReceiverConfig(configuration);
                    return new JdbcOutboundAdapter(jdbcReceiverConfig);
                }

            case FTP:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    FtpInboundAdapterConfig ftpSenderConfig = createFtpSenderConfig(configuration);
                    return new FtpInboundAdapter(ftpSenderConfig);
                } else {
                    FtpOutboundAdapterConfig ftpReceiverConfig = createFtpReceiverConfig(configuration);
                    return new FtpOutboundAdapter(ftpReceiverConfig);
                }

            case SOAP:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    SoapInboundAdapterConfig soapSenderConfig = createSoapSenderConfig(configuration);
                    return new SoapInboundAdapter(soapSenderConfig);
                } else {
                    SoapOutboundAdapterConfig soapReceiverConfig = createSoapReceiverConfig(configuration);
                    return new SoapOutboundAdapter(soapReceiverConfig);
                }

            case FILE:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    FileInboundAdapterConfig fileSenderConfig = createFileSenderConfig(configuration);
                    return new FileInboundAdapter(fileSenderConfig);
                } else {
                    FileOutboundAdapterConfig fileReceiverConfig = createFileReceiverConfig(configuration);
                    return new FileOutboundAdapter(fileReceiverConfig);
                }

            case IBMMQ:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    IbmmqInboundAdapterConfig ibmmqSenderConfig = createIbmmqSenderConfig(configuration);
                    return new IbmmqInboundAdapter(ibmmqSenderConfig);
                } else {
                    IbmmqOutboundAdapterConfig ibmmqReceiverConfig = createIbmmqReceiverConfig(configuration);
                    return new IbmmqOutboundAdapter(ibmmqReceiverConfig);
                }

            case KAFKA:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    KafkaInboundAdapterConfig kafkaSenderConfig = createKafkaSenderConfig(configuration);
                    return new KafkaInboundAdapter(kafkaSenderConfig);
                } else {
                    KafkaOutboundAdapterConfig kafkaReceiverConfig = createKafkaReceiverConfig(configuration);
                    return new KafkaOutboundAdapter(kafkaReceiverConfig);
                }

            case SFTP:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    SftpInboundAdapterConfig sftpSenderConfig = createSftpSenderConfig(configuration);
                    return new SftpInboundAdapter(sftpSenderConfig);
                } else {
                    SftpOutboundAdapterConfig sftpReceiverConfig = createSftpReceiverConfig(configuration);
                    return new SftpOutboundAdapter(sftpReceiverConfig);
                }

            case MAIL:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    MailInboundAdapterConfig mailSenderConfig = createMailSenderConfig(configuration);
                    return new MailInboundAdapter(mailSenderConfig);
                } else {
                    MailOutboundAdapterConfig mailReceiverConfig = createMailReceiverConfig(configuration);
                    return new MailOutboundAdapter(mailReceiverConfig);
                }

            case RFC:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    RfcInboundAdapterConfig rfcSenderConfig = createRfcSenderConfig(configuration);
                    return new RfcInboundAdapter(rfcSenderConfig);
                } else {
                    RfcOutboundAdapterConfig rfcReceiverConfig = createRfcReceiverConfig(configuration);
                    return new RfcOutboundAdapter(rfcReceiverConfig);
                }

            case IDOC:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    IdocInboundAdapterConfig idocSenderConfig = createIdocSenderConfig(configuration);
                    return new IdocInboundAdapter(idocSenderConfig);
                } else {
                    IdocOutboundAdapterConfig idocReceiverConfig = createIdocReceiverConfig(configuration);
                    return new IdocOutboundAdapter(idocReceiverConfig);
                }

            case ODATA:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    OdataInboundAdapterConfig odataSenderConfig = createOdataSenderConfig(configuration);
                    return new OdataInboundAdapter(odataSenderConfig);
                } else {
                    OdataOutboundAdapterConfig odataReceiverConfig = createOdataReceiverConfig(configuration);
                    return new OdataOutboundAdapter(odataReceiverConfig);
                }

            case REST:
                if(configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.INBOUND) {
                    RestInboundAdapterConfig restSenderConfig = createRestSenderConfig(configuration);
                    return new RestInboundAdapter(restSenderConfig);
                } else {
                    RestOutboundAdapterConfig restReceiverConfig = createRestReceiverConfig(configuration);
                    return new RestOutboundAdapter(restReceiverConfig);
                }

            default:
                log.warn("Adapter type not supported: {}. Returning null.", configuration.getAdapterType());
                return null;
        }
    }

    @Override
    public List<AdapterConfiguration.AdapterTypeEnum> getSupportedAdapterTypes() {
        return Arrays.asList(
                AdapterConfiguration.AdapterTypeEnum.HTTP,
                AdapterConfiguration.AdapterTypeEnum.JDBC,
                AdapterConfiguration.AdapterTypeEnum.FTP,
                AdapterConfiguration.AdapterTypeEnum.SFTP,
                AdapterConfiguration.AdapterTypeEnum.SOAP,
                AdapterConfiguration.AdapterTypeEnum.FILE,
                AdapterConfiguration.AdapterTypeEnum.IBMMQ,
                AdapterConfiguration.AdapterTypeEnum.KAFKA,
                AdapterConfiguration.AdapterTypeEnum.MAIL,
                AdapterConfiguration.AdapterTypeEnum.RFC,
                AdapterConfiguration.AdapterTypeEnum.IDOC,
                AdapterConfiguration.AdapterTypeEnum.ODATA,
                AdapterConfiguration.AdapterTypeEnum.REST
       );
    }

    @Override
    public void shutdownAll() {
        log.info("Shutting down all adapters");

        adapterRegistry.forEach((id, adapter) -> {
            try {
                adapter.shutdown();
            } catch(Exception e) {
                log.error("Error shutting down adapter {}: {}", id, e.getMessage(), e);
            }
        });

        adapterRegistry.clear();
    }

    /**
     * Initialize metadata for all supported adapter types
     */
    private void initializeMetadata() {
        // HTTP Source(Sender)
        metadataCache.put("HTTP_INBOUND", AdapterMetadata.builder()
                .adapterName("HTTP Source Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.HTTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .version("1.0.0")
                .description("Fetches data from HTTP/REST endpoints")
                .supportedOperations(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"))
                .requiredProperties(Arrays.asList("url"))
                .optionalProperties(Arrays.asList("method", "headers", "queryParams", "body"))
                .capabilities(Map.of(
                        "authentication", "Basic, Bearer, API Key",
                        "contentTypes", "JSON, XML, Text, Binary"
               ))
                .supportsAsync(true)
                .supportsBatch(false)
                .supportsStreaming(false)
                .build());

        // HTTP Target(Receiver)
        metadataCache.put("HTTP_TARGET", AdapterMetadata.builder()
                .adapterName("HTTP Target Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.HTTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .version("1.0.0")
                .description("Sends data to HTTP/REST endpoints")
                .supportedOperations(Arrays.asList("POST", "PUT", "PATCH", "DELETE"))
                .requiredProperties(Arrays.asList("url"))
                .optionalProperties(Arrays.asList("method", "headers", "contentType"))
                .capabilities(Map.of(
                        "authentication", "Basic, Bearer, API Key",
                        "contentTypes", "JSON, XML, Text, Binary"
               ))
                .supportsAsync(true)
                .supportsBatch(true)
                .supportsStreaming(false)
                .build());

        // JDBC Source
        metadataCache.put("JDBC_INBOUND", AdapterMetadata.builder()
                .adapterName("JDBC Source Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.JDBC)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .version("1.0.0")
                .description("Fetches data from relational databases")
                .supportedOperations(Arrays.asList("SELECT", "STORED_PROCEDURE"))
                .requiredProperties(Arrays.asList("jdbcUrl", "driverClassName"))
                .optionalProperties(Arrays.asList("username", "password", "query", "pollingInterval"))
                .capabilities(Map.of(
                        "databases", "MySQL, PostgreSQL, Oracle, SQL Server",
                        "features", "Polling, Delta Fetch, Batch Processing"
               ))
                .supportsAsync(false)
                .supportsBatch(true)
                .supportsStreaming(true)
                .build());

        // JDBC Target
        metadataCache.put("JDBC_TARGET", AdapterMetadata.builder()
                .adapterName("JDBC Target Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.JDBC)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .version("1.0.0")
                .description("Writes data to relational databases")
                .supportedOperations(Arrays.asList("INSERT", "UPDATE", "DELETE", "UPSERT", "STORED_PROCEDURE"))
                .requiredProperties(Arrays.asList("jdbcUrl", "driverClassName"))
                .optionalProperties(Arrays.asList("username", "password", "table", "operation"))
                .capabilities(Map.of(
                        "databases", "MySQL, PostgreSQL, Oracle, SQL Server",
                        "features", "Batch Insert, Transactions, Upsert"
               ))
                .supportsAsync(false)
                .supportsBatch(true)
                .supportsStreaming(false)
                .build());

        // FTP Source
        metadataCache.put("FTP_INBOUND", AdapterMetadata.builder()
                .adapterName("FTP Source Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.FTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .version("1.0.0")
                .description("Downloads files from FTP/SFTP servers")
                .supportedOperations(Arrays.asList("LIST", "GET", "POLL"))
                .requiredProperties(Arrays.asList("host", "port"))
                .optionalProperties(Arrays.asList("username", "password", "directory", "filePattern"))
                .capabilities(Map.of(
                        "protocols", "FTP, SFTP",
                        "features", "File Polling, Pattern Matching, Archive"
               ))
                .supportsAsync(true)
                .supportsBatch(true)
                .supportsStreaming(false)
                .build());

        // FTP Target
        metadataCache.put("FTP_TARGET", AdapterMetadata.builder()
                .adapterName("FTP Target Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.FTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .version("1.0.0")
                .description("Uploads files to FTP/SFTP servers")
                .supportedOperations(Arrays.asList("PUT", "APPEND", "DELETE"))
                .requiredProperties(Arrays.asList("host", "port"))
                .optionalProperties(Arrays.asList("username", "password", "directory", "filename"))
                .capabilities(Map.of(
                        "protocols", "FTP, SFTP",
                        "features", "Overwrite, Append, Temporary Upload"
               ))
                .supportsAsync(true)
                .supportsBatch(true)
                .supportsStreaming(false)
                .build());
    }

    /**
     * Create default metadata for unsupported adapter types
     */
    private AdapterMetadata createDefaultMetadata(
            AdapterConfiguration.AdapterTypeEnum adapterType,
            AdapterConfiguration.AdapterModeEnum adapterMode) {

        return AdapterMetadata.builder()
                .adapterName(adapterType.name() + " " + adapterMode.name() + " Adapter")
                .adapterType(adapterType)
                .adapterMode(adapterMode)
                .version("1.0.0")
                .description("Generic adapter implementation")
                .supportedOperations(Collections.emptyList())
                .requiredProperties(Collections.emptyList())
                .optionalProperties(Collections.emptyList())
                .capabilities(Collections.emptyMap())
                .supportsAsync(false)
                .supportsBatch(false)
                .supportsStreaming(false)
                .build();
    }

    /**
     * Configuration creation helper methods
     */
    private HttpInboundAdapterConfig createHttpSenderConfig(AdapterConfiguration configuration) {
        HttpInboundAdapterConfig config = new HttpInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "GET");
        if(method != null) {
            config.setHttpMethod(HttpMethod.valueOf(method.toUpperCase()));
        }
        config.setConnectionTimeout((Integer) props.getOrDefault("connectionTimeout", 30000));
        config.setReadTimeout((Integer) props.getOrDefault("readTimeout", 30000));
        config.setContentType((String) props.getOrDefault("contentType", "application/json"));
        config.setAccept((String) props.getOrDefault("accept", "application/json"));
        String authType = (String) props.getOrDefault("authentication", "none");
        if(authType != null && !authType.equals("none")) {
            config.setAuthenticationType(AuthenticationType.valueOf(authType.toUpperCase()));
        }
        return config;
    }

    private HttpOutboundAdapterConfig createHttpReceiverConfig(AdapterConfiguration configuration) {
        HttpOutboundAdapterConfig config = new HttpOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setTargetEndpointUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "POST");
        if(method != null) {
            config.setHttpMethod(HttpMethod.valueOf(method.toUpperCase()));
        }
        config.setConnectionTimeout((Integer) props.getOrDefault("connectionTimeout", 30000));
        config.setReadTimeout((Integer) props.getOrDefault("readTimeout", 30000));
        config.setContentType((String) props.getOrDefault("contentType", "application/json"));

        return config;
    }

    private JdbcInboundAdapterConfig createJdbcSenderConfig(AdapterConfiguration configuration) {
        JdbcInboundAdapterConfig config = new JdbcInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setJdbcUrl((String) props.get("jdbcUrl"));
        config.setDriverClass((String) props.get("driverClassName"));
        config.setUsername((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setSelectQuery((String) props.get("query"));
        config.setPollingInterval((Long) props.getOrDefault("pollingInterval", 60000L));
        config.setFetchSize((Integer) props.getOrDefault("fetchSize", 1000));

        return config;
    }

    private JdbcOutboundAdapterConfig createJdbcReceiverConfig(AdapterConfiguration configuration) {
        JdbcOutboundAdapterConfig config = new JdbcOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setJdbcUrl((String) props.get("jdbcUrl"));
        config.setDriverClass((String) props.get("driverClassName"));
        config.setUsername((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setTargetTable((String) props.get("tableName"));
        config.setOperationType((String) props.getOrDefault("operation", "INSERT"));
        config.setBatchSize((Integer) props.getOrDefault("batchSize", 1000));

        return config;
    }

    private FtpInboundAdapterConfig createFtpSenderConfig(AdapterConfiguration configuration) {
        FtpInboundAdapterConfig config = new FtpInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setPort(String.valueOf(props.getOrDefault("port", 21)));
        config.setUserName((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setSourceDirectory((String) props.getOrDefault("directory", "/"));

        return config;
    }

    private FtpOutboundAdapterConfig createFtpReceiverConfig(AdapterConfiguration configuration) {
        FtpOutboundAdapterConfig config = new FtpOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setPort(String.valueOf(props.getOrDefault("port", 21)));
        config.setUserName((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setTargetDirectory((String) props.getOrDefault("directory", "/"));

        return config;
    }

    private SoapInboundAdapterConfig createSoapSenderConfig(AdapterConfiguration configuration) {
        SoapInboundAdapterConfig config = new SoapInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setWsdlUrl((String) props.get("wsdlUrl"));
        config.setEndpointUrl((String) props.get("endpointUrl"));
        config.setServiceName((String) props.get("serviceName"));
        config.setPortName((String) props.get("portName"));

        return config;
    }

    private SoapOutboundAdapterConfig createSoapReceiverConfig(AdapterConfiguration configuration) {
        SoapOutboundAdapterConfig config = new SoapOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setWsdlUrl((String) props.get("wsdlUrl"));
        config.setServiceName((String) props.get("serviceName"));
        config.setPortName((String) props.get("portName"));

        return config;
    }

    private FileInboundAdapterConfig createFileSenderConfig(AdapterConfiguration configuration) {
        FileInboundAdapterConfig config = new FileInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setSourceDirectory((String) props.getOrDefault("directory", "/"));
        config.setFileName((String) props.getOrDefault("filePattern", "*"));

        return config;
    }

    private FileOutboundAdapterConfig createFileReceiverConfig(AdapterConfiguration configuration) {
        FileOutboundAdapterConfig config = new FileOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setTargetDirectory((String) props.getOrDefault("directory", "/"));
        config.setFileNamePattern((String) props.getOrDefault("filePattern", "${filename}"));

        return config;
    }

    private IbmmqInboundAdapterConfig createIbmmqSenderConfig(AdapterConfiguration configuration) {
        IbmmqInboundAdapterConfig config = new IbmmqInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setHost((String) props.getOrDefault("brokerUrl", "tcp://localhost:61616"));
        config.setSourceQueueName((String) props.get("queueName"));
        config.setUsername((String) props.get("username"));
        config.setPassword((String) props.get("password"));

        return config;
    }

    private IbmmqOutboundAdapterConfig createIbmmqReceiverConfig(AdapterConfiguration configuration) {
        IbmmqOutboundAdapterConfig config = new IbmmqOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setProviderUrl((String) props.getOrDefault("brokerUrl", "tcp://localhost:61616"));
        config.setTargetQueueName((String) props.get("queueName"));
        config.setTargetUsername((String) props.get("username"));
        config.setTargetPassword((String) props.get("password"));

        return config;
    }

    private KafkaInboundAdapterConfig createKafkaSenderConfig(AdapterConfiguration configuration) {
        KafkaInboundAdapterConfig config = new KafkaInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setBootstrapServers((String) props.getOrDefault("bootstrapServers", "localhost:9092"));
        config.setTopics((String) props.get("topics"));
        config.setGroupId((String) props.getOrDefault("groupId", "integrix - consumer - group"));
        config.setKeyDeserializer((String) props.getOrDefault("keyDeserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        config.setValueDeserializer((String) props.getOrDefault("valueDeserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        config.setAutoOffsetReset((String) props.getOrDefault("autoOffsetReset", "earliest"));
        config.setEnableAutoCommit((Boolean) props.getOrDefault("enableAutoCommit", true));
        config.setAutoCommitIntervalMs((Integer) props.getOrDefault("autoCommitIntervalMs", 5000));
        config.setSessionTimeoutMs((Integer) props.getOrDefault("sessionTimeoutMs", 30000));
        config.setMaxPollRecords((Integer) props.getOrDefault("maxPollRecords", 500));

        return config;
    }

    private KafkaOutboundAdapterConfig createKafkaReceiverConfig(AdapterConfiguration configuration) {
        KafkaOutboundAdapterConfig config = new KafkaOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setBootstrapServers((String) props.getOrDefault("bootstrapServers", "localhost:9092"));
        config.setTopic((String) props.get("topic"));
        config.setKeySerializer((String) props.getOrDefault("keySerializer", "org.apache.kafka.common.serialization.StringSerializer"));
        config.setValueSerializer((String) props.getOrDefault("valueSerializer", "org.apache.kafka.common.serialization.StringSerializer"));
        config.setAcks((String) props.getOrDefault("acks", "all"));
        config.setRetries((Integer) props.getOrDefault("retries", 3));
        config.setBatchSize((Integer) props.getOrDefault("batchSize", 16384));
        config.setLingerMs(Long.valueOf(props.getOrDefault("lingerMs", 1).toString()));
        config.setBufferMemory((Long) props.getOrDefault("bufferMemory", 33554432L));

        return config;
    }

    private SftpInboundAdapterConfig createSftpSenderConfig(AdapterConfiguration configuration) {
        SftpInboundAdapterConfig config = new SftpInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setPort(String.valueOf(props.getOrDefault("port", 22)));
        config.setUserName((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setSourceDirectory((String) props.getOrDefault("directory", "/"));
        config.setAuthenticationType((String) props.getOrDefault("authenticationType", "password"));

        return config;
    }

    private SftpOutboundAdapterConfig createSftpReceiverConfig(AdapterConfiguration configuration) {
        SftpOutboundAdapterConfig config = new SftpOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setTargetServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setTargetPort(String.valueOf(props.getOrDefault("port", 22)));
        config.setTargetUserName((String) props.get("username"));
        config.setTargetPassword((String) props.get("password"));
        config.setTargetDirectory((String) props.getOrDefault("directory", "/"));
        config.setAuthenticationType((String) props.getOrDefault("authenticationType", "password"));

        return config;
    }

    private MailInboundAdapterConfig createMailSenderConfig(AdapterConfiguration configuration) {
        MailInboundAdapterConfig config = new MailInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setMailProtocol((String) props.getOrDefault("protocol", "imap"));
        config.setMailServerHost((String) props.getOrDefault("host", "localhost"));
        config.setMailServerPort(String.valueOf(props.getOrDefault("port", 993)));
        config.setMailUsername((String) props.get("username"));
        config.setMailPassword((String) props.get("password"));
        config.setUseSSLTLS((Boolean) props.getOrDefault("useSsl", true));
        config.setFolderName((String) props.getOrDefault("folder", "INBOX"));

        return config;
    }

    private MailOutboundAdapterConfig createMailReceiverConfig(AdapterConfiguration configuration) {
        MailOutboundAdapterConfig config = new MailOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setSmtpServerHost((String) props.getOrDefault("host", "localhost"));
        config.setSmtpServerPort(String.valueOf(props.getOrDefault("port", 587)));
        config.setSmtpUsername((String) props.get("username"));
        config.setSmtpPassword((String) props.get("password"));
        config.setSmtpUseSSLTLS((Boolean) props.getOrDefault("useTls", true));
        config.setFromAddress((String) props.getOrDefault("fromAddress", "noreply@example.com"));

        return config;
    }

    private RfcInboundAdapterConfig createRfcSenderConfig(AdapterConfiguration configuration) {
        RfcInboundAdapterConfig config = new RfcInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setSapApplicationServerHost((String) props.get("sapHost"));
        config.setSapSystemNumber((String) props.get("systemNumber"));
        config.setSapClientNumber((String) props.get("client"));
        config.setUsername((String) props.get("user"));
        config.setPassword((String) props.get("password"));
        config.setLanguage((String) props.getOrDefault("language", "EN"));
        config.setProgramId((String) props.get("programId"));
        config.setGatewayHost((String) props.get("gatewayHost"));
        config.setGatewayService((String) props.get("gatewayService"));

        return config;
    }

    private RfcOutboundAdapterConfig createRfcReceiverConfig(AdapterConfiguration configuration) {
        RfcOutboundAdapterConfig config = new RfcOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setTargetSapApplicationServerHost((String) props.get("sapHost"));
        config.setTargetSapSystemNumber((String) props.get("systemNumber"));
        config.setTargetSapClientNumber((String) props.get("client"));
        config.setTargetUsername((String) props.get("user"));
        config.setTargetPassword((String) props.get("password"));
        config.setTargetLanguage((String) props.getOrDefault("language", "EN"));
        config.setTargetRfcFunctionName((String) props.get("rfcName"));

        return config;
    }

    private IdocInboundAdapterConfig createIdocSenderConfig(AdapterConfiguration configuration) {
        IdocInboundAdapterConfig config = new IdocInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setSourceSapApplicationServerHost((String) props.get("sapHost"));
        config.setSourceSapSystemNumber((String) props.get("systemNumber"));
        config.setSourceSapClientNumber((String) props.get("client"));
        config.setSourceUsername((String) props.get("user"));
        config.setSourcePassword((String) props.get("password"));
        config.setSourceIdocType((String) props.get("idocType"));

        return config;
    }

    private IdocOutboundAdapterConfig createIdocReceiverConfig(AdapterConfiguration configuration) {
        IdocOutboundAdapterConfig config = new IdocOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setTargetSapApplicationServerHost((String) props.get("sapHost"));
        config.setTargetSapSystemNumber((String) props.get("systemNumber"));
        config.setTargetSapClientNumber((String) props.get("client"));
        config.setTargetUsername((String) props.get("user"));
        config.setTargetPassword((String) props.get("password"));
        config.setTargetIdocType((String) props.get("idocType"));

        return config;
    }

    private OdataInboundAdapterConfig createOdataSenderConfig(AdapterConfiguration configuration) {
        OdataInboundAdapterConfig config = new OdataInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setSourceServiceEndpointUrl((String) props.get("serviceUrl"));
        config.setSourceUsername((String) props.get("username"));
        config.setSourcePassword((String) props.get("password"));
        config.setSourceEntitySet((String) props.get("entitySet"));

        return config;
    }

    private OdataOutboundAdapterConfig createOdataReceiverConfig(AdapterConfiguration configuration) {
        OdataOutboundAdapterConfig config = new OdataOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setTargetServiceEndpointUrl((String) props.get("serviceUrl"));
        config.setTargetUsername((String) props.get("username"));
        config.setTargetPassword((String) props.get("password"));
        config.setTargetEntitySet((String) props.get("entitySet"));

        return config;
    }

    private RestInboundAdapterConfig createRestSenderConfig(AdapterConfiguration configuration) {
        RestInboundAdapterConfig config = new RestInboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setBaseEndpointUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "GET");
        config.setResourcePath((String) props.get("endpoint"));
        config.setAcceptHeader((String) props.getOrDefault("accept", "application/json"));
        String authType = (String) props.getOrDefault("authentication", "none");
        if(authType != null && !authType.equals("none")) {
            config.setAuthenticationType(AuthenticationType.valueOf(authType.toUpperCase()));
        }

        return config;
    }

    private RestOutboundAdapterConfig createRestReceiverConfig(AdapterConfiguration configuration) {
        RestOutboundAdapterConfig config = new RestOutboundAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();

        config.setEndpointUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "POST");
        if(method != null) {
            config.setHttpMethod(HttpMethod.valueOf(method.toUpperCase()));
        }
        config.setContentType((String) props.getOrDefault("contentType", "application/json"));
        config.setAcceptHeader((String) props.getOrDefault("accept", "application/json"));
        String authType = (String) props.getOrDefault("authentication", "none");
        if(authType != null && !authType.equals("none")) {
            config.setAuthenticationType(AuthenticationType.valueOf(authType.toUpperCase()));
        }

        return config;
    }
}
