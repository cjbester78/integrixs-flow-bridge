package com.integrixs.backend.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.service.BackendAdapterExecutor;
import com.integrixs.data.model.CommunicationAdapter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Kafka message handler implementation for sending and receiving messages.
 * This is not an adapter implementation but a handler for the AdapterExecutionService.
 */
@Component("kafkaMessageHandler")
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaMessageHandler implements BackendAdapterExecutor.AdapterHandler {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageHandler.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean canHandle(CommunicationAdapter adapter) {
        return adapter != null &&
               adapter.getType() != null &&
               adapter.getType().name().toLowerCase().contains("kafka");
    }

    @Override
    public BackendAdapterExecutor.ExecutionResult execute(
            CommunicationAdapter adapter,
            Map<String, Object> context) {

        try {
            logger.debug("Executing Kafka adapter: {}", adapter.getName());

            // Get adapter configuration
            Map<String, Object> config = new HashMap<>();
            try {
                String configJson = adapter.getConfiguration();
                if(configJson != null && !configJson.isEmpty()) {
                    config = objectMapper.readValue(configJson, Map.class);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse adapter configuration: {}", e.getMessage());
            }

            // Extract Kafka settings
            String topic = (String) config.get("topic");
            String key = (String) config.get("key");
            Integer partition = (Integer) config.get("partition");
            Long timeout = (Long) config.getOrDefault("timeoutMs", 30000L);

            // Get message data from context
            Object messageData = context.get("messageData");

            // Handle inbound vs outbound
            if("INBOUND".equals(adapter.getDirection())) {
                return handleInbound(adapter, topic, context);
            } else {
                return handleOutbound(adapter, topic, key, partition, messageData, context, timeout);
            }

        } catch(Exception e) {
            logger.error("Error executing Kafka adapter", e);
            return new BackendAdapterExecutor.ExecutionResult(false, null,
                "Kafka adapter execution failed: " + e.getMessage());
        }
    }

    /**
     * Handle outbound message sending
     */
    private BackendAdapterExecutor.ExecutionResult handleOutbound(
            CommunicationAdapter adapter,
            String topic,
            String key,
            Integer partition,
            Object messageData,
            Map<String, Object> context,
            Long timeout) {

        try {
            if(topic == null || topic.isEmpty()) {
                return new BackendAdapterExecutor.ExecutionResult(false, null,
                    "No topic specified for Kafka adapter");
            }

            // Create headers
            Headers headers = new RecordHeaders();

            // Add standard headers
            headers.add("integrix - adapter - id", adapter.getId().toString().getBytes(StandardCharsets.UTF_8));
            headers.add("integrix - adapter - name", adapter.getName().getBytes(StandardCharsets.UTF_8));
            headers.add("integrix - timestamp", String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

            // Add correlation ID if present
            String correlationId = (String) context.get("correlationId");
            if(correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            headers.add("correlation - id", correlationId.getBytes(StandardCharsets.UTF_8));

            // Add custom headers
            Map<String, Object> customHeaders = (Map<String, Object>) context.get("headers");
            if(customHeaders != null) {
                for(Map.Entry<String, Object> entry : customHeaders.entrySet()) {
                    headers.add(entry.getKey(),
                        String.valueOf(entry.getValue()).getBytes(StandardCharsets.UTF_8));
                }
            }

            // Create producer record
            ProducerRecord<String, Object> record;
            if(partition != null) {
                record = new ProducerRecord<String, Object>(topic, partition, key, messageData, headers);
            } else if(key != null) {
                record = new ProducerRecord<String, Object>(topic, null, key, messageData, headers);
            } else {
                record = new ProducerRecord<String, Object>(topic, null, null, messageData, headers);
            }

            // Send message
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

            // Wait for result with timeout
            SendResult<String, Object> result = future.get(timeout, TimeUnit.MILLISECONDS);

            // Prepare success result
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("topic", topic);
            resultData.put("partition", result.getRecordMetadata().partition());
            resultData.put("offset", result.getRecordMetadata().offset());
            resultData.put("timestamp", result.getRecordMetadata().timestamp());
            resultData.put("key", key);
            resultData.put("correlationId", correlationId);
            resultData.put("success", true);

            logger.info("Message sent to Kafka topic: {} partition: {} offset: {}",
                topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());

            return new BackendAdapterExecutor.ExecutionResult(true, resultData);

        } catch(Exception e) {
            logger.error("Failed to send message to Kafka", e);
            return new BackendAdapterExecutor.ExecutionResult(false, null,
                "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Handle inbound message receiving(typically done via listeners)
     */
    private BackendAdapterExecutor.ExecutionResult handleInbound(
            CommunicationAdapter adapter,
            String topic,
            Map<String, Object> context) {

        try {
            // For inbound, we typically use listeners
            // This method would be called to set up or check the listener

            if(topic == null || topic.isEmpty()) {
                return new BackendAdapterExecutor.ExecutionResult(false, null,
                    "No topic specified for inbound Kafka adapter");
            }

            // Check if topic exists
            try {
                boolean topicExists = kafkaTemplate.execute(producer -> {
                    return producer.partitionsFor(topic) != null && !producer.partitionsFor(topic).isEmpty();
                });

                if(!topicExists) {
                    return new BackendAdapterExecutor.ExecutionResult(false, null,
                        "Topic does not exist: " + topic);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("topic", topic);
                result.put("status", "listener_configured");
                result.put("message", "Inbound messages will be received via @KafkaListener");

                return new BackendAdapterExecutor.ExecutionResult(true, result);

            } catch(Exception e) {
                return new BackendAdapterExecutor.ExecutionResult(false, null,
                    "Failed to check topic: " + topic + " - " + e.getMessage());
            }

        } catch(Exception e) {
            logger.error("Failed to configure inbound Kafka adapter", e);
            return new BackendAdapterExecutor.ExecutionResult(false, null,
                "Failed to configure inbound: " + e.getMessage());
        }
    }

    /**
     * Process inbound Kafka record(called from listener)
     */
    public Map<String, Object> processInboundRecord(ConsumerRecord<String, Object> record) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Extract headers
            Map<String, String> headers = new HashMap<>();
            record.headers().forEach(header -> {
                headers.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
            });

            result.put("topic", record.topic());
            result.put("partition", record.partition());
            result.put("offset", record.offset());
            result.put("timestamp", record.timestamp());
            result.put("key", record.key());
            result.put("value", record.value());
            result.put("headers", headers);
            result.put("correlationId", headers.get("correlation - id"));

            logger.debug("Processed inbound Kafka message from topic: {} partition: {} offset: {}",
                record.topic(), record.partition(), record.offset());

        } catch(Exception e) {
            logger.error("Error processing inbound Kafka record", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for(StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if(sb.length() > 4000) { // Limit size
                sb.append("\t... truncated");
                break;
            }
        }
        return sb.toString();
    }
}
