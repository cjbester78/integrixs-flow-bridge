package com.integrixs.tests.config;

import com.integrixs.backend.api.dto.request.CreateFlowRequest;
import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.UpdateFlowRequest;
import com.integrixs.shared.dto.*;
import com.integrixs.shared.integration.AdapterContext;
import com.integrixs.shared.integration.AdapterMetadata;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Builder for creating test data
 */
public class TestDataBuilder {

    /**
     * Create a test integration flow request
     */
    public static CreateFlowRequest createTestFlowRequest() {
        CreateFlowRequest request = new CreateFlowRequest();
        request.setName("Test HTTP to Database Flow");
        request.setDescription("Test flow for integration testing");
        request.setInboundAdapterId(UUID.randomUUID().toString());
        request.setOutboundAdapterId(UUID.randomUUID().toString());
        request.setSourceFlowStructureId(UUID.randomUUID().toString());
        request.setTargetFlowStructureId(UUID.randomUUID().toString());
        request.setActive(true);
        return request;
    }

    /**
     * Create a test HTTP inbound adapter request
     */
    public static CreateAdapterRequest createHttpInboundAdapterRequest() {
        CreateAdapterRequest request = new CreateAdapterRequest();
        request.setName("Test HTTP Sender");
        request.setType("REST");
        request.setMode("INBOUND");
        request.setDirection("OUTBOUND");
        request.setDescription("HTTP adapter for receiving data");

        Map<String, Object> config = new HashMap<>();
        config.put("url", "http://localhost:8081/test - endpoint");
        config.put("method", "GET");
        config.put("headers", Map.of("Content - Type", "application/json"));

        // Convert configuration to JSON string
        try {
            request.setConfiguration(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(config));
        } catch(Exception e) {
            request.setConfiguration(" {}");
        }

        request.setActive(true);
        request.setBusinessComponentId(UUID.randomUUID().toString());
        return request;
    }

    /**
     * Create a test JDBC outbound adapter request
     */
    public static CreateAdapterRequest createJdbcOutboundAdapterRequest() {
        CreateAdapterRequest request = new CreateAdapterRequest();
        request.setName("Test JDBC Receiver");
        request.setType("JDBC");
        request.setMode("OUTBOUND");
        request.setDirection("INBOUND");
        request.setDescription("JDBC adapter for sending data");

        Map<String, Object> config = new HashMap<>();
        config.put("driverClassName", "org.postgresql.Driver");
        config.put("url", "jdbc:postgresql://localhost:5432/testdb");
        config.put("username", "testuser");
        config.put("password", "testpass");
        config.put("query", "INSERT INTO test_table(data) VALUES(:data)");

        // Convert configuration to JSON string
        try {
            request.setConfiguration(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(config));
        } catch(Exception e) {
            request.setConfiguration(" {}");
        }

        request.setActive(true);
        request.setBusinessComponentId(UUID.randomUUID().toString());
        return request;
    }

    /**
     * Create test field mappings
     */
    public static List<Map<String, Object>> createTestFieldMappings() {
        List<Map<String, Object>> mappings = new ArrayList<>();

        Map<String, Object> mapping1 = new HashMap<>();
        mapping1.put("sourceFields", Arrays.asList("input.id"));
        mapping1.put("targetField", "output.recordId");
        mapping1.put("mappingType", "DIRECT");
        mapping1.put("mappingOrder", 1);
        mapping1.put("isActive", true);
        mappings.add(mapping1);

        Map<String, Object> mapping2 = new HashMap<>();
        mapping2.put("sourceFields", Arrays.asList("input.name"));
        mapping2.put("targetField", "output.fullName");
        mapping2.put("mappingType", "EXPRESSION");
        mapping2.put("expression", "input.name.toUpperCase()");
        mapping2.put("mappingOrder", 2);
        mapping2.put("isActive", true);
        mappings.add(mapping2);

        return mappings;
    }

    /**
     * Create test adapter context
     */
    public static AdapterContext createTestAdapterContext() {
        return AdapterContext.builder()
                .executionId(UUID.randomUUID().toString())
                .flowId(UUID.randomUUID().toString())
                .inputData(Map.of("test", "data"))
                .headers(Map.of("Content - Type", "application/json"))
                .properties(new HashMap<>())
                .correlationId(UUID.randomUUID().toString())
                .timeout(30000L)
                .build();
    }

    /**
     * Create test adapter metadata
     */
    public static AdapterMetadata createTestAdapterMetadata(String type) {
        return AdapterMetadata.builder()
                .adapterId(UUID.randomUUID().toString())
                .name(type + " Adapter")
                .type(type)
                .version("1.0.0")
                .description("Test " + type + " adapter")
                .supportedOperations(Arrays.asList("send", "receive"))
                .requiredConfig(Map.of("url", "string"))
                .optionalConfig(Map.of("timeout", "number"))
                .active(true)
                .iconUrl("/icons/" + type.toLowerCase() + ".png")
                .build();
    }

    /**
     * Create test payload
     */
    public static Map<String, Object> createTestPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", UUID.randomUUID().toString());
        payload.put("name", "Test Record");
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("data", Map.of(
                "field1", "value1",
                "field2", 123,
                "field3", true
       ));
        return payload;
    }

    /**
     * Create complex test payload
     */
    public static Map<String, Object> createComplexTestPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order", Map.of(
                "orderId", "ORD-" + UUID.randomUUID().toString(),
                "customerName", "John Doe",
                "orderDate", LocalDateTime.now().toString(),
                "items", Arrays.asList(
                        Map.of("sku", "ITEM-001", "quantity", 2, "price", 29.99),
                        Map.of("sku", "ITEM-002", "quantity", 1, "price", 49.99)
               ),
                "shipping", Map.of(
                        "address", "123 Main St",
                        "city", "New York",
                        "state", "NY",
                        "zip", "10001"
               )
       ));
        return payload;
    }

    /**
     * Create test user
     */
    public static Map<String, Object> createTestUser(String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", UUID.randomUUID().toString());
        user.put("username", "test-" + role.toLowerCase() + "@example.com");
        user.put("email", "test-" + role.toLowerCase() + "@example.com");
        user.put("role", role);
        user.put("active", true);
        user.put("createdAt", LocalDateTime.now());
        return user;
    }

    /**
     * Create test system configuration
     */
    public static Map<String, String> createTestSystemConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("environment.type", "DEVELOPMENT");
        config.put("jwt.secret", "test - secret - key");
        config.put("jwt.expiration", "3600000");
        config.put("monitoring.enabled", "true");
        config.put("monitoring.retention.days", "7");
        return config;
    }
}
