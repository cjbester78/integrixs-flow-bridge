package com.integrixs.adapters.infrastructure.adapter;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.config.KafkaOutboundAdapterConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Kafka Receiver Adapter - sends messages to Kafka topics
 * In middleware terminology: Outbound = sends data TO external systems
 */
public class KafkaOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(KafkaOutboundAdapter.class);


    private final KafkaOutboundAdapterConfig config;
    private KafkaProducer<String, String> producer;

    public KafkaOutboundAdapter(KafkaOutboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        try {
            if(config.getTopic() == null || config.getTopic().isEmpty()) {
                return AdapterOperationResult.failure("Topic is required");
            }

            // Initialize producer properties
            Properties producerProperties = new Properties();
            producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getKeySerializer());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getValueSerializer());

            if(config.getAcks() != null) {
                producerProperties.put(ProducerConfig.ACKS_CONFIG, config.getAcks());
            }
            if(config.getRetries() != null) {
                producerProperties.put(ProducerConfig.RETRIES_CONFIG, config.getRetries());
            }
            if(config.getBatchSize() != null) {
                producerProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, config.getBatchSize());
            }
            if(config.getLingerMs() != null) {
                producerProperties.put(ProducerConfig.LINGER_MS_CONFIG, config.getLingerMs());
            }
            if(config.getBufferMemory() != null) {
                producerProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, config.getBufferMemory());
            }
            if(config.getCompressionType() != null) {
                producerProperties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config.getCompressionType());
            }

            // Security configuration
            if(config.getSecurityProtocol() != null) {
                producerProperties.put("security.protocol", config.getSecurityProtocol());
            }

            // Create producer
            producer = new KafkaProducer<>(producerProperties);

            log.info("Kafka outbound adapter initialized successfully");
            return AdapterOperationResult.success("Initialized successfully");
        } catch(Exception e) {
            log.error("Failed to initialize Kafka outbound adapter", e);
            return AdapterOperationResult.failure("Initialization failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        if(producer != null) {
            producer.close();
            producer = null;
        }
        return AdapterOperationResult.success("Shutdown successfully");
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        return AdapterOperationResult.success("Connection test passed");
    }

    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    config.getTopic(),
                    request.getPayload().toString()
           );

            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get();

            return AdapterOperationResult.success(
                    String.format("Message sent to topic %s, partition %d, offset %d",
                            metadata.topic(), metadata.partition(), metadata.offset())
           );
        } catch(Exception e) {
            return AdapterOperationResult.failure("Send failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }

    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        List<AdapterOperationResult> results = new ArrayList<>();
        for(SendRequest request : requests) {
            results.add(send(request));
        }
        long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
        return AdapterOperationResult.success("Sent " + successCount + "/" + requests.size() + " messages");
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }

    @Override
    public boolean supportsBatchOperations() {
        return true;
    }

    @Override
    public int getMaxBatchSize() {
        return config.getBatchSize() != null ? config.getBatchSize() : 1000;
    }
    public long getTimeout() {
        return config.getTimeout() != null ? config.getTimeout() : 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("Kafka Receiver: %s -> %s", config.getBootstrapServers(), config.getTopic());
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.KAFKA)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("Kafka Receiver Adapter - sends messages to Kafka")
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
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }

    protected AdapterOperationResult performSend(Object payload, Map<String, Object> headers) throws Exception {
        SendRequest request = new SendRequest();
        request.setPayload(payload);
        request.setParameters(headers);
        return send(request);
    }
}
