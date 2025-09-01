package com.integrixs.adapters.infrastructure.service;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterMetadata;
import com.integrixs.adapters.domain.port.AdapterPort;
import com.integrixs.adapters.domain.service.AdapterRegistryService;
import com.integrixs.adapters.infrastructure.adapter.*;
import com.integrixs.adapters.config.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Infrastructure implementation of adapter registry service
 */
@Slf4j
@Service
public class AdapterRegistryServiceImpl implements AdapterRegistryService {
    
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
        if (adapter != null) {
            try {
                adapter.shutdown();
            } catch (Exception e) {
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
        
        switch (configuration.getAdapterType()) {
            case HTTP:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    HttpSenderAdapterConfig httpSenderConfig = createHttpSenderConfig(configuration);
                    return new HttpSenderAdapter(httpSenderConfig);
                } else {
                    HttpReceiverAdapterConfig httpReceiverConfig = createHttpReceiverConfig(configuration);
                    return new HttpReceiverAdapter(httpReceiverConfig);
                }
                
            case JDBC:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    JdbcSenderAdapterConfig jdbcSenderConfig = createJdbcSenderConfig(configuration);
                    return new JdbcSenderAdapter(jdbcSenderConfig);
                } else {
                    JdbcReceiverAdapterConfig jdbcReceiverConfig = createJdbcReceiverConfig(configuration);
                    return new JdbcReceiverAdapter(jdbcReceiverConfig);
                }
                
            case FTP:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    FtpSenderAdapterConfig ftpSenderConfig = createFtpSenderConfig(configuration);
                    return new FtpSenderAdapter(ftpSenderConfig);
                } else {
                    FtpReceiverAdapterConfig ftpReceiverConfig = createFtpReceiverConfig(configuration);
                    return new FtpReceiverAdapter(ftpReceiverConfig);
                }
                
            case SOAP:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    SoapSenderAdapterConfig soapSenderConfig = createSoapSenderConfig(configuration);
                    return new SoapSenderAdapter(soapSenderConfig);
                } else {
                    SoapReceiverAdapterConfig soapReceiverConfig = createSoapReceiverConfig(configuration);
                    return new SoapReceiverAdapter(soapReceiverConfig);
                }
                
            case FILE:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    FileSenderAdapterConfig fileSenderConfig = createFileSenderConfig(configuration);
                    return new FileSenderAdapter(fileSenderConfig);
                } else {
                    FileReceiverAdapterConfig fileReceiverConfig = createFileReceiverConfig(configuration);
                    return new FileReceiverAdapter(fileReceiverConfig);
                }
                
            case JMS:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    JmsSenderAdapterConfig jmsSenderConfig = createJmsSenderConfig(configuration);
                    return new JmsSenderAdapter(jmsSenderConfig);
                } else {
                    JmsReceiverAdapterConfig jmsReceiverConfig = createJmsReceiverConfig(configuration);
                    return new JmsReceiverAdapter(jmsReceiverConfig);
                }
                
            case KAFKA:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    KafkaSenderAdapterConfig kafkaSenderConfig = createKafkaSenderConfig(configuration);
                    return new KafkaSenderAdapter(kafkaSenderConfig);
                } else {
                    KafkaReceiverAdapterConfig kafkaReceiverConfig = createKafkaReceiverConfig(configuration);
                    return new KafkaReceiverAdapter(kafkaReceiverConfig);
                }
                
            case SFTP:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    SftpSenderAdapterConfig sftpSenderConfig = createSftpSenderConfig(configuration);
                    return new SftpSenderAdapter(sftpSenderConfig);
                } else {
                    SftpReceiverAdapterConfig sftpReceiverConfig = createSftpReceiverConfig(configuration);
                    return new SftpReceiverAdapter(sftpReceiverConfig);
                }
                
            case MAIL:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    MailSenderAdapterConfig mailSenderConfig = createMailSenderConfig(configuration);
                    return new MailSenderAdapter(mailSenderConfig);
                } else {
                    MailReceiverAdapterConfig mailReceiverConfig = createMailReceiverConfig(configuration);
                    return new MailReceiverAdapter(mailReceiverConfig);
                }
                
            case RFC:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    RfcSenderAdapterConfig rfcSenderConfig = createRfcSenderConfig(configuration);
                    return new RfcSenderAdapter(rfcSenderConfig);
                } else {
                    RfcReceiverAdapterConfig rfcReceiverConfig = createRfcReceiverConfig(configuration);
                    return new RfcReceiverAdapter(rfcReceiverConfig);
                }
                
            case IDOC:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    IdocSenderAdapterConfig idocSenderConfig = createIdocSenderConfig(configuration);
                    return new IdocSenderAdapter(idocSenderConfig);
                } else {
                    IdocReceiverAdapterConfig idocReceiverConfig = createIdocReceiverConfig(configuration);
                    return new IdocReceiverAdapter(idocReceiverConfig);
                }
                
            case ODATA:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    OdataSenderAdapterConfig odataSenderConfig = createOdataSenderConfig(configuration);
                    return new OdataSenderAdapter(odataSenderConfig);
                } else {
                    OdataReceiverAdapterConfig odataReceiverConfig = createOdataReceiverConfig(configuration);
                    return new OdataReceiverAdapter(odataReceiverConfig);
                }
                
            case REST:
                if (configuration.getAdapterMode() == AdapterConfiguration.AdapterModeEnum.SENDER) {
                    RestSenderAdapterConfig restSenderConfig = createRestSenderConfig(configuration);
                    return new RestSenderAdapter(restSenderConfig);
                } else {
                    RestReceiverAdapterConfig restReceiverConfig = createRestReceiverConfig(configuration);
                    return new RestReceiverAdapter(restReceiverConfig);
                }
                
            default:
                throw new UnsupportedOperationException(
                        "Adapter type not supported: " + configuration.getAdapterType());
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
                AdapterConfiguration.AdapterTypeEnum.JMS,
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
            } catch (Exception e) {
                log.error("Error shutting down adapter {}: {}", id, e.getMessage(), e);
            }
        });
        
        adapterRegistry.clear();
    }
    
    /**
     * Initialize metadata for all supported adapter types
     */
    private void initializeMetadata() {
        // HTTP Source (Sender)
        metadataCache.put("HTTP_SENDER", AdapterMetadata.builder()
                .adapterName("HTTP Source Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.HTTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.SENDER)
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
        
        // HTTP Target (Receiver)
        metadataCache.put("HTTP_TARGET", AdapterMetadata.builder()
                .adapterName("HTTP Target Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.HTTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.RECEIVER)
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
        metadataCache.put("JDBC_SENDER", AdapterMetadata.builder()
                .adapterName("JDBC Source Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.JDBC)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.SENDER)
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
                .adapterMode(AdapterConfiguration.AdapterModeEnum.RECEIVER)
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
        metadataCache.put("FTP_SENDER", AdapterMetadata.builder()
                .adapterName("FTP Source Adapter")
                .adapterType(AdapterConfiguration.AdapterTypeEnum.FTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.SENDER)
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
                .adapterMode(AdapterConfiguration.AdapterModeEnum.RECEIVER)
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
    private HttpSenderAdapterConfig createHttpSenderConfig(AdapterConfiguration configuration) {
        HttpSenderAdapterConfig config = new HttpSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "GET");
        if (method != null) {
            config.setHttpMethod(HttpMethod.valueOf(method.toUpperCase()));
        }
        config.setConnectionTimeout((Integer) props.getOrDefault("connectionTimeout", 30000));
        config.setReadTimeout((Integer) props.getOrDefault("readTimeout", 30000));
        config.setContentType((String) props.getOrDefault("contentType", "application/json"));
        config.setAccept((String) props.getOrDefault("accept", "application/json"));
        String authType = (String) props.getOrDefault("authentication", "none");
        if (authType != null && !authType.equals("none")) {
            config.setAuthenticationType(AuthenticationType.valueOf(authType.toUpperCase()));
        }        
        return config;
    }
    
    private HttpReceiverAdapterConfig createHttpReceiverConfig(AdapterConfiguration configuration) {
        HttpReceiverAdapterConfig config = new HttpReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setTargetEndpointUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "POST");
        if (method != null) {
            config.setHttpMethod(HttpMethod.valueOf(method.toUpperCase()));
        }
        config.setConnectionTimeout((Integer) props.getOrDefault("connectionTimeout", 30000));
        config.setReadTimeout((Integer) props.getOrDefault("readTimeout", 30000));
        config.setContentType((String) props.getOrDefault("contentType", "application/json"));
        
        return config;
    }
    
    private JdbcSenderAdapterConfig createJdbcSenderConfig(AdapterConfiguration configuration) {
        JdbcSenderAdapterConfig config = new JdbcSenderAdapterConfig();
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
    
    private JdbcReceiverAdapterConfig createJdbcReceiverConfig(AdapterConfiguration configuration) {
        JdbcReceiverAdapterConfig config = new JdbcReceiverAdapterConfig();
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
    
    private FtpSenderAdapterConfig createFtpSenderConfig(AdapterConfiguration configuration) {
        FtpSenderAdapterConfig config = new FtpSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setPort(String.valueOf(props.getOrDefault("port", 21)));
        config.setUserName((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setSourceDirectory((String) props.getOrDefault("directory", "/"));
        
        return config;
    }
    
    private FtpReceiverAdapterConfig createFtpReceiverConfig(AdapterConfiguration configuration) {
        FtpReceiverAdapterConfig config = new FtpReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setPort(String.valueOf(props.getOrDefault("port", 21)));
        config.setUserName((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setTargetDirectory((String) props.getOrDefault("directory", "/"));
        
        return config;
    }
    
    private SoapSenderAdapterConfig createSoapSenderConfig(AdapterConfiguration configuration) {
        SoapSenderAdapterConfig config = new SoapSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setWsdlUrl((String) props.get("wsdlUrl"));
        config.setEndpointUrl((String) props.get("endpointUrl"));
        config.setServiceName((String) props.get("serviceName"));
        config.setPortName((String) props.get("portName"));
        
        return config;
    }
    
    private SoapReceiverAdapterConfig createSoapReceiverConfig(AdapterConfiguration configuration) {
        SoapReceiverAdapterConfig config = new SoapReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setWsdlUrl((String) props.get("wsdlUrl"));
        config.setServiceName((String) props.get("serviceName"));
        config.setPortName((String) props.get("portName"));
        
        return config;
    }
    
    private FileSenderAdapterConfig createFileSenderConfig(AdapterConfiguration configuration) {
        FileSenderAdapterConfig config = new FileSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setSourceDirectory((String) props.getOrDefault("directory", "/"));
        config.setFileName((String) props.getOrDefault("filePattern", "*"));
        
        return config;
    }
    
    private FileReceiverAdapterConfig createFileReceiverConfig(AdapterConfiguration configuration) {
        FileReceiverAdapterConfig config = new FileReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setTargetDirectory((String) props.getOrDefault("directory", "/"));
        config.setFileNamePattern((String) props.getOrDefault("filePattern", "${filename}"));
        
        return config;
    }
    
    private JmsSenderAdapterConfig createJmsSenderConfig(AdapterConfiguration configuration) {
        JmsSenderAdapterConfig config = new JmsSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setHost((String) props.getOrDefault("brokerUrl", "tcp://localhost:61616"));
        config.setSourceQueueName((String) props.get("queueName"));
        config.setUsername((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        
        return config;
    }
    
    private JmsReceiverAdapterConfig createJmsReceiverConfig(AdapterConfiguration configuration) {
        JmsReceiverAdapterConfig config = new JmsReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setProviderUrl((String) props.getOrDefault("brokerUrl", "tcp://localhost:61616"));
        config.setTargetQueueName((String) props.get("queueName"));
        config.setTargetUsername((String) props.get("username"));
        config.setTargetPassword((String) props.get("password"));
        
        return config;
    }
    
    private KafkaSenderAdapterConfig createKafkaSenderConfig(AdapterConfiguration configuration) {
        KafkaSenderAdapterConfig config = new KafkaSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setBootstrapServers((String) props.getOrDefault("bootstrapServers", "localhost:9092"));
        config.setTopics((String) props.get("topics"));
        config.setGroupId((String) props.getOrDefault("groupId", "integrix-consumer-group"));
        config.setKeyDeserializer((String) props.getOrDefault("keyDeserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        config.setValueDeserializer((String) props.getOrDefault("valueDeserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        config.setAutoOffsetReset((String) props.getOrDefault("autoOffsetReset", "earliest"));
        config.setEnableAutoCommit((Boolean) props.getOrDefault("enableAutoCommit", true));
        config.setAutoCommitIntervalMs((Integer) props.getOrDefault("autoCommitIntervalMs", 5000));
        config.setSessionTimeoutMs((Integer) props.getOrDefault("sessionTimeoutMs", 30000));
        config.setMaxPollRecords((Integer) props.getOrDefault("maxPollRecords", 500));
        
        return config;
    }
    
    private KafkaReceiverAdapterConfig createKafkaReceiverConfig(AdapterConfiguration configuration) {
        KafkaReceiverAdapterConfig config = new KafkaReceiverAdapterConfig();
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
    
    private SftpSenderAdapterConfig createSftpSenderConfig(AdapterConfiguration configuration) {
        SftpSenderAdapterConfig config = new SftpSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setPort(String.valueOf(props.getOrDefault("port", 22)));
        config.setUserName((String) props.get("username"));
        config.setPassword((String) props.get("password"));
        config.setSourceDirectory((String) props.getOrDefault("directory", "/"));
        config.setAuthenticationType((String) props.getOrDefault("authenticationType", "password"));
        
        return config;
    }
    
    private SftpReceiverAdapterConfig createSftpReceiverConfig(AdapterConfiguration configuration) {
        SftpReceiverAdapterConfig config = new SftpReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setTargetServerAddress((String) props.getOrDefault("host", "localhost"));
        config.setTargetPort(String.valueOf(props.getOrDefault("port", 22)));
        config.setTargetUserName((String) props.get("username"));
        config.setTargetPassword((String) props.get("password"));
        config.setTargetDirectory((String) props.getOrDefault("directory", "/"));
        config.setAuthenticationType((String) props.getOrDefault("authenticationType", "password"));
        
        return config;
    }
    
    private MailSenderAdapterConfig createMailSenderConfig(AdapterConfiguration configuration) {
        MailSenderAdapterConfig config = new MailSenderAdapterConfig();
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
    
    private MailReceiverAdapterConfig createMailReceiverConfig(AdapterConfiguration configuration) {
        MailReceiverAdapterConfig config = new MailReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setSmtpServerHost((String) props.getOrDefault("host", "localhost"));
        config.setSmtpServerPort(String.valueOf(props.getOrDefault("port", 587)));
        config.setSmtpUsername((String) props.get("username"));
        config.setSmtpPassword((String) props.get("password"));
        config.setSmtpUseSSLTLS((Boolean) props.getOrDefault("useTls", true));
        config.setFromAddress((String) props.getOrDefault("fromAddress", "noreply@example.com"));
        
        return config;
    }
    
    private RfcSenderAdapterConfig createRfcSenderConfig(AdapterConfiguration configuration) {
        RfcSenderAdapterConfig config = new RfcSenderAdapterConfig();
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
    
    private RfcReceiverAdapterConfig createRfcReceiverConfig(AdapterConfiguration configuration) {
        RfcReceiverAdapterConfig config = new RfcReceiverAdapterConfig();
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
    
    private IdocSenderAdapterConfig createIdocSenderConfig(AdapterConfiguration configuration) {
        IdocSenderAdapterConfig config = new IdocSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setSourceSapApplicationServerHost((String) props.get("sapHost"));
        config.setSourceSapSystemNumber((String) props.get("systemNumber"));
        config.setSourceSapClientNumber((String) props.get("client"));
        config.setSourceUsername((String) props.get("user"));
        config.setSourcePassword((String) props.get("password"));
        config.setSourceIdocType((String) props.get("idocType"));
        
        return config;
    }
    
    private IdocReceiverAdapterConfig createIdocReceiverConfig(AdapterConfiguration configuration) {
        IdocReceiverAdapterConfig config = new IdocReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setTargetSapApplicationServerHost((String) props.get("sapHost"));
        config.setTargetSapSystemNumber((String) props.get("systemNumber"));
        config.setTargetSapClientNumber((String) props.get("client"));
        config.setTargetUsername((String) props.get("user"));
        config.setTargetPassword((String) props.get("password"));
        config.setTargetIdocType((String) props.get("idocType"));
        
        return config;
    }
    
    private OdataSenderAdapterConfig createOdataSenderConfig(AdapterConfiguration configuration) {
        OdataSenderAdapterConfig config = new OdataSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setSourceServiceEndpointUrl((String) props.get("serviceUrl"));
        config.setSourceUsername((String) props.get("username"));
        config.setSourcePassword((String) props.get("password"));
        config.setSourceEntitySet((String) props.get("entitySet"));
        
        return config;
    }
    
    private OdataReceiverAdapterConfig createOdataReceiverConfig(AdapterConfiguration configuration) {
        OdataReceiverAdapterConfig config = new OdataReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setTargetServiceEndpointUrl((String) props.get("serviceUrl"));
        config.setTargetUsername((String) props.get("username"));
        config.setTargetPassword((String) props.get("password"));
        config.setTargetEntitySet((String) props.get("entitySet"));
        
        return config;
    }
    
    private RestSenderAdapterConfig createRestSenderConfig(AdapterConfiguration configuration) {
        RestSenderAdapterConfig config = new RestSenderAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setBaseEndpointUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "GET");
        config.setResourcePath((String) props.get("endpoint"));
        config.setAcceptHeader((String) props.getOrDefault("accept", "application/json"));
        String authType = (String) props.getOrDefault("authentication", "none");
        if (authType != null && !authType.equals("none")) {
            config.setAuthenticationType(AuthenticationType.valueOf(authType.toUpperCase()));
        }
        
        return config;
    }
    
    private RestReceiverAdapterConfig createRestReceiverConfig(AdapterConfiguration configuration) {
        RestReceiverAdapterConfig config = new RestReceiverAdapterConfig();
        Map<String, Object> props = configuration.getConnectionProperties();
        
        config.setEndpointUrl((String) props.get("url"));
        String method = (String) props.getOrDefault("method", "POST");
        if (method != null) {
            config.setHttpMethod(HttpMethod.valueOf(method.toUpperCase()));
        }
        config.setContentType((String) props.getOrDefault("contentType", "application/json"));
        config.setAcceptHeader((String) props.getOrDefault("accept", "application/json"));
        String authType = (String) props.getOrDefault("authentication", "none");
        if (authType != null && !authType.equals("none")) {
            config.setAuthenticationType(AuthenticationType.valueOf(authType.toUpperCase()));
        }
        
        return config;
    }
}