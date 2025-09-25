package com.integrixs.backend.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for messaging
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:integrix-consumer-group}")
    private String groupId;

    @Value("${integrix.messaging.topics.inbound:integrix-inbound}")
    private String inboundTopic;

    @Value("${integrix.messaging.topics.outbound:integrix-outbound}")
    private String outboundTopic;

    @Value("${integrix.messaging.topics.error:integrix-error}")
    private String errorTopic;

    @Value("${integrix.messaging.topics.audit:integrix-audit}")
    private String auditTopic;

    @Value("${integrix.messaging.topics.metrics:integrix-metrics}")
    private String metricsTopic;

    /**
     * Kafka admin configuration
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(AdminClientConfig.CLIENT_ID_CONFIG, "integrix-admin");
        return new KafkaAdmin(configs);
    }

    /**
     * Create topics
     */
    @Bean
    public NewTopic inboundTopic() {
        return TopicBuilder.name(inboundTopic)
            .partitions(3)
            .replicas(1)
            .compact()
            .build();
    }

    @Bean
    public NewTopic outboundTopic() {
        return TopicBuilder.name(outboundTopic)
            .partitions(3)
            .replicas(1)
            .compact()
            .build();
    }

    @Bean
    public NewTopic errorTopic() {
        return TopicBuilder.name(errorTopic)
            .partitions(1)
            .replicas(1)
            .config("retention.ms", "604800000") // 7 days
            .build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name(auditTopic)
            .partitions(1)
            .replicas(1)
            .config("retention.ms", "2592000000") // 30 days
            .compact()
            .build();
    }

    @Bean
    public NewTopic metricsTopic() {
        return TopicBuilder.name(metricsTopic)
            .partitions(3)
            .replicas(1)
            .config("retention.ms", "86400000") // 1 day
            .build();
    }

    /**
     * Producer configuration
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "integrix-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // Configure JSON serializer
        props.put(JsonSerializer.TYPE_MAPPINGS,
            "integrixMessage:com.integrixs.backend.messaging.IntegrixMessage");

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Kafka template
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
        template.setDefaultTopic(outboundTopic);
        return template;
    }

    /**
     * Consumer configuration
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "integrix-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

        // Configure JSON deserializer
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.integrixs.*");
        props.put(JsonDeserializer.TYPE_MAPPINGS,
            "integrixMessage:com.integrixs.backend.messaging.IntegrixMessage");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Map.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setPollTimeout(3000);

        // Configure error handling
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 3));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    /**
     * Kafka operations for advanced operations
     */
    @Bean
    public KafkaOperations<String, Object> kafkaOperations(KafkaTemplate<String, Object> kafkaTemplate) {
        return kafkaTemplate;
    }
}
