package com.integrixs.tests.unit;

import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.CreateFlowRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple unit tests that don't require Spring context or Docker
 */
public class SimpleUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testLoginRequestSerialization() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        // When
        String json = objectMapper.writeValueAsString(request);

        // Then
        assertThat(json).contains("\"username\":\"testuser\"");
        assertThat(json).contains("\"password\":\"testpass\"");
    }

    @Test
    void testCreateAdapterRequestValidation() {
        // Given
        CreateAdapterRequest request = new CreateAdapterRequest();

        // When
        request.setName("Test Adapter");
        request.setType("HTTP");
        request.setMode("OUTBOUND");
        request.setBusinessComponentId("test-id");
        request.setConfiguration("{\"url\":\"http://example.com\"}");

        // Then
        assertThat(request.getName()).isEqualTo("Test Adapter");
        assertThat(request.getType()).isEqualTo("HTTP");
        assertThat(request.getMode()).isEqualTo("OUTBOUND");
        assertThat(request.isActive()).isTrue();
    }

    @Test
    void testCreateFlowRequestDefaults() {
        // Given
        CreateFlowRequest request = new CreateFlowRequest();

        // When
        request.setName("Test Flow");
        request.setInboundAdapterId("inbound-123");
        request.setOutboundAdapterId("outbound-456");

        // Then
        assertThat(request.getName()).isEqualTo("Test Flow");
        assertThat(request.isActive()).isTrue(); // Default value
        assertThat(request.getDescription()).isNull(); // Optional field
    }
}