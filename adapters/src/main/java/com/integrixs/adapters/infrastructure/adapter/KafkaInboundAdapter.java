package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.FetchRequest;
import com.integrixs.adapters.domain.model.AdapterMetadata;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.config.KafkaInboundAdapterConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Map;
/**
 * Kafka implementation of inbound adapter(consumes messages from Kafka topics)
 * Follows middleware convention: Inbound = receives data FROM external systems.
 */
public class KafkaInboundAdapter extends AbstractAdapter implements InboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(KafkaInboundAdapter.class);


    private final KafkaInboundAdapterConfig config;
    private KafkaConsumer<String, String> consumer;
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private Thread pollingThread;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public KafkaInboundAdapter(KafkaInboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        try {
            log.info("Initializing Kafka inbound adapter with bootstrap servers: {}", config.getBootstrapServers());

            validateConfiguration();

            log.info("Kafka inbound adapter initialized for topics: {}", config.getTopics());
            return AdapterOperationResult.success("Initialized successfully");
        } catch(Exception e) {
            log.error("Failed to initialize Kafka adapter: {}", e.getMessage(), e);
            return AdapterOperationResult.failure("Initialization failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterOperationResult performStart() {
        try {
            consumer = createConsumer();
            List<String> topicList = Arrays.asList(config.getTopics().split(","));
            consumer.subscribe(topicList);

            log.info("Kafka consumer started for topics: {}", topicList);
            return AdapterOperationResult.success("Started successfully");
        } catch(Exception e) {
            log.error("Failed to start Kafka adapter: {}", e.getMessage(), e);
            return AdapterOperationResult.failure("Start failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterOperationResult performStop() {
        stopPolling();

        if(consumer != null) {
            try {
                consumer.close(Duration.ofSeconds(10));
                log.info("Kafka consumer closed");
        } catch(Exception e) {
                log.warn("Error closing Kafka consumer: {}", e.getMessage());
            }
            consumer = null;
        }

        return AdapterOperationResult.success("Stopped successfully");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        performStop();
        return AdapterOperationResult.success("Shutdown successfully");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        try {
            Properties testProps = createConsumerProperties();
            testProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test - connection-" + System.currentTimeMillis());

                try(KafkaConsumer<String, String> testConsumer = new KafkaConsumer<>(testProps)) {
                testConsumer.listTopics(Duration.ofSeconds(5));
                return AdapterOperationResult.success("Kafka connection successful");
            }
        } catch(Exception e) {
            log.error("Kafka connection test failed: {}", e.getMessage(), e);
            return AdapterOperationResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        if(!isReady() || consumer == null) {
            return AdapterOperationResult.failure("Adapter not ready");
        }

        try {
            List<Map<String, Object>> messages = new ArrayList<>();

            // Poll for records
            long pollTimeout = request.getParameters() != null &&
                    request.getParameters().containsKey("pollTimeout") ?
                    Long.parseLong(String.valueOf(request.getParameters().get("pollTimeout"))) : 1000;

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeout));

            for(ConsumerRecord<String, String> record : records) {
                Map<String, Object> messageData = extractMessageData(record);
                messages.add(messageData);
            }

            if(!messages.isEmpty()) {
                log.debug("Fetched {} messages from Kafka", messages.size());
            }

            return AdapterOperationResult.builder()
                    .success(true)
                    .message(String.format("Fetched %d messages", messages.size()))
                    .data(messages)
                    .recordsProcessed(messages.size())
                    .build();
        } catch(Exception e) {
            log.error("Kafka fetch failed: {}", e.getMessage(), e);
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }

    public AdapterOperationResult fetchBatch(List<FetchRequest> requests) {
        // For Kafka, batch fetch is same as single fetch
        return fetch(requests.get(0));
    }

    public CompletableFuture<AdapterOperationResult> fetchBatchAsync(List<FetchRequest> requests) {
        return CompletableFuture.supplyAsync(() -> fetchBatch(requests));
    }

    public boolean supportsBatchOperations() {
        return true;
    }

    public int getMaxBatchSize() {
        return config.getMaxPollRecords();
    }

    public void startPolling(long intervalMillis) {
        if(polling.get()) {
            log.warn("Polling already active");
            return;
        }

        polling.set(true);
        pollingThread = new Thread(() -> {
            log.info("Starting Kafka polling");

            try {
                while(polling.get() && consumer != null) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(intervalMillis));

                    if(!records.isEmpty()) {
                        List<Map<String, Object>> messages = new ArrayList<>();
                        for(ConsumerRecord<String, String> record : records) {
                            messages.add(extractMessageData(record));
                        }
                        log.debug("Polled {} messages from Kafka", messages.size());

                        // Process messages as needed
                        // This could invoke callbacks or queue messages for processing
                    }
                }
            } catch(WakeupException e) {
                log.info("Kafka polling interrupted");
            } catch(Exception e) {
                log.error("Error during Kafka polling", e);
            } finally {
                shutdownLatch.countDown();
            }
        }, "kafka - sender - polling");

        pollingThread.setName("kafka - polling-" + config.getGroupId());
        pollingThread.start();
    }

    public void stopPolling() {
        if(polling.compareAndSet(true, false)) {
            if(consumer != null) {
                consumer.wakeup();
            }

            try {
                shutdownLatch.await();
            } catch(InterruptedException e) {
                log.warn("Interrupted while waiting for polling thread to stop");
                Thread.currentThread().interrupt();
            }

            log.info("Stopped Kafka polling");
        }
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = createConsumerProperties();
        return new KafkaConsumer<>(props);
    }

    private Properties createConsumerProperties() {
        Properties props = new Properties();

        // Basic configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getValueDeserializer());

        // Offset and commit configuration
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.isEnableAutoCommit());
        if(config.isEnableAutoCommit()) {
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, config.getAutoCommitIntervalMs());
        }
        // Session and poll configuration
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, config.getSessionTimeoutMs());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.getMaxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, config.getMaxPollIntervalMs());

        // Security configuration
        if(config.getSecurityProtocol() != null && !config.getSecurityProtocol().isEmpty()) {
            props.put("security.protocol", config.getSecurityProtocol());

            // SASL configuration
            if(config.getSaslMechanism() != null) {
                props.put("sasl.mechanism", config.getSaslMechanism());
                if(config.getSaslJaasConfig() != null) {
                    props.put("sasl.jaas.config", config.getSaslJaasConfig());
                }
            }

            // SSL configuration
            if(config.getSecurityProtocol().contains("SSL")) {
                configureSSL(props);
            }
        }

        // Additional properties
        if(config.getAdditionalProperties() != null) {
            props.putAll(config.getAdditionalProperties());
        }
        return props;
    }

    private void configureSSL(Properties props) {
        if(config.getSslTruststoreLocation() != null) {
            props.put("ssl.truststore.location", config.getSslTruststoreLocation());
            if(config.getSslTruststorePassword() != null) {
                props.put("ssl.truststore.password", config.getSslTruststorePassword());
            }
        }

        if(config.getSslKeystoreLocation() != null) {
            props.put("ssl.keystore.location", config.getSslKeystoreLocation());
            if(config.getSslKeystorePassword() != null) {
                props.put("ssl.keystore.password", config.getSslKeystorePassword());
            }
            if(config.getSslKeyPassword() != null) {
                props.put("ssl.key.password", config.getSslKeyPassword());
            }
        }
    }

    private Map<String, Object> extractMessageData(ConsumerRecord<String, String> record) {
        Map<String, Object> data = new HashMap<>();

        // Basic record metadata
        data.put("topic", record.topic());
        data.put("partition", record.partition());
        data.put("offset", record.offset());
        data.put("timestamp", record.timestamp());
        data.put("timestampType", record.timestampType().toString());

        // Key and value
        data.put("key", record.key());
        data.put("value", record.value());

        // Headers
        Map<String, String> headers = new HashMap<>();
        record.headers().forEach(header -> {
            headers.put(header.key(), new String(header.value()));
        });
        data.put("headers", headers);

        return data;
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getBootstrapServers() == null || config.getBootstrapServers().trim().isEmpty()) {
            throw new AdapterException("Bootstrap servers are required");
        }

        if(config.getTopics() == null || config.getTopics().trim().isEmpty()) {
            throw new AdapterException("Topics are required");
        }

        if(config.getGroupId() == null || config.getGroupId().trim().isEmpty()) {
            throw new AdapterException("Group ID is required");
        }
    }
    public long getPollingInterval() {
        return config.getPollingInterval();
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("Kafka Sender(Consumer): servers = %s, topics = %s, groupId = %s",
                config.getBootstrapServers(),
                config.getTopics(),
                config.getGroupId());
    }

    @Override
    public void startListening(DataReceivedCallback callback) {
        // Not implemented for polling - based consumer
        log.debug("Push-based listening not supported by this adapter type");
    }

    @Override
    public void stopListening() {
        stopPolling();
    }

    @Override
    public boolean isListening() {
        return polling.get();
    }
    public void setDataReceivedCallback(DataReceivedCallback callback) {
        // Not implemented
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.KAFKA)
                .adapterMode(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("Kafka Inbound adapter - consumes messages from Kafka topics")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.KAFKA;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.INBOUND;
    }
}
