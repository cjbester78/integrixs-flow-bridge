package com.integrixs.tests.adapter;

import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.auth.LoginResponse;
import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.TestAdapterRequest;
import com.integrixs.backend.api.dto.request.UpdateAdapterRequest;
import com.integrixs.backend.api.dto.response.AdapterResponse;
import com.integrixs.backend.api.dto.response.AdapterTestResponse;
import com.integrixs.tests.config.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Communication Adapter functionality
 */
public class CommunicationAdapterIntegrationTest extends BaseIntegrationTest {

    private String businessComponentId;

    @BeforeEach
    void setUp() throws Exception {
        // Login as admin
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_USER);
        loginRequest.setPassword(ADMIN_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), 
                LoginResponse.class);
        adminToken = authResponse.getToken();

        // Create a business component for testing
        Map<String, Object> bcRequest = new HashMap<>();
        bcRequest.put("name", "Test Business Component");
        bcRequest.put("type", "DEPARTMENT");
        bcRequest.put("description", "Test component for adapter tests");

        MvcResult bcResult = mockMvc.perform(post("/api/business-components")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bcRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> bcResponse = objectMapper.readValue(
                bcResult.getResponse().getContentAsString(), 
                Map.class);
        businessComponentId = (String) bcResponse.get("id");
    }

    @Test
    void testCreateHttpAdapter() throws Exception {
        // Given
        CreateAdapterRequest request = new CreateAdapterRequest();
        request.setName("Test HTTP Adapter");
        request.setType("HTTP");
        request.setMode("OUTBOUND");
        request.setDescription("Test HTTP adapter for integration tests");
        request.setBusinessComponentId(businessComponentId);
        request.setActive(true);
        
        Map<String, Object> config = new HashMap<>();
        config.put("url", "https://jsonplaceholder.typicode.com/posts");
        config.put("method", "POST");
        config.put("timeout", 30000);
        config.put("headers", Map.of("Content-Type", "application/json"));
        request.setConfiguration(objectMapper.writeValueAsString(config));

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test HTTP Adapter"))
                .andExpect(jsonPath("$.type").value("HTTP"))
                .andExpect(jsonPath("$.mode").value("OUTBOUND"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        AdapterResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                AdapterResponse.class);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getConfiguration()).containsKey("url");
    }

    @Test
    void testCreateFileAdapter() throws Exception {
        // Given
        CreateAdapterRequest request = new CreateAdapterRequest();
        request.setName("Test File Adapter");
        request.setType("FILE");
        request.setMode("INBOUND");
        request.setDescription("Test file adapter for integration tests");
        request.setBusinessComponentId(businessComponentId);
        request.setActive(false);
        
        Map<String, Object> config = new HashMap<>();
        config.put("directory", "/tmp/integrix/test");
        config.put("filePattern", "*.xml");
        config.put("pollInterval", 60000);
        config.put("moveToDirectory", "/tmp/integrix/processed");
        request.setConfiguration(objectMapper.writeValueAsString(config));

        // When & Then
        mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test File Adapter"))
                .andExpect(jsonPath("$.type").value("FILE"))
                .andExpect(jsonPath("$.mode").value("INBOUND"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void testUpdateAdapter() throws Exception {
        // Given - Create adapter first
        CreateAdapterRequest createRequest = new CreateAdapterRequest();
        createRequest.setName("Adapter to Update");
        createRequest.setType("FTP");
        createRequest.setMode("OUTBOUND");
        createRequest.setBusinessComponentId(businessComponentId);
        createRequest.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "host", "ftp.example.com",
                "port", 21,
                "username", "user"
        )));

        MvcResult createResult = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AdapterResponse createdAdapter = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                AdapterResponse.class);
        String adapterId = createdAdapter.getId();

        // When - Update the adapter
        UpdateAdapterRequest updateRequest = new UpdateAdapterRequest();
        updateRequest.setName("Updated FTP Adapter");
        updateRequest.setType("FTP");
        updateRequest.setMode("OUTBOUND");
        updateRequest.setDescription("Updated description");
        updateRequest.setBusinessComponentId(businessComponentId);
        updateRequest.setActive(true);
        updateRequest.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "host", "newftp.example.com",
                "port", 22,
                "username", "newuser",
                "protocol", "SFTP"
        )));

        // Then
        mockMvc.perform(put("/api/communication-adapters/" + adapterId)
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated FTP Adapter"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.configuration.host").value("newftp.example.com"));
    }

    @Test
    void testDeleteAdapter() throws Exception {
        // Given - Create adapter first
        CreateAdapterRequest createRequest = new CreateAdapterRequest();
        createRequest.setName("Adapter to Delete");
        createRequest.setType("EMAIL");
        createRequest.setMode("OUTBOUND");
        createRequest.setBusinessComponentId(businessComponentId);
        createRequest.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "smtpHost", "smtp.example.com",
                "smtpPort", 587
        )));

        MvcResult createResult = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AdapterResponse createdAdapter = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                AdapterResponse.class);
        String adapterId = createdAdapter.getId();

        // When - Delete the adapter
        mockMvc.perform(delete("/api/communication-adapters/" + adapterId)
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isNoContent());

        // Then - Verify it's deleted
        mockMvc.perform(get("/api/communication-adapters/" + adapterId)
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActivateDeactivateAdapter() throws Exception {
        // Given - Create inactive adapter
        CreateAdapterRequest createRequest = new CreateAdapterRequest();
        createRequest.setName("Adapter to Activate");
        createRequest.setType("DATABASE");
        createRequest.setMode("OUTBOUND");
        createRequest.setBusinessComponentId(businessComponentId);
        createRequest.setActive(false);
        createRequest.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "jdbcUrl", "jdbc:postgresql://localhost:5432/testdb",
                "username", "test"
        )));

        MvcResult createResult = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AdapterResponse createdAdapter = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                AdapterResponse.class);
        String adapterId = createdAdapter.getId();
        assertThat(createdAdapter.isActive()).isFalse();

        // When - Activate the adapter
        mockMvc.perform(post("/api/communication-adapters/" + adapterId + "/activate")
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // And - Deactivate the adapter
        mockMvc.perform(post("/api/communication-adapters/" + adapterId + "/deactivate")
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void testGetAdaptersByMode() throws Exception {
        // Given - Create adapters with different modes
        CreateAdapterRequest inboundRequest = new CreateAdapterRequest();
        inboundRequest.setName("Inbound Test Adapter");
        inboundRequest.setType("FILE");
        inboundRequest.setMode("INBOUND");
        inboundRequest.setBusinessComponentId(businessComponentId);
        inboundRequest.setConfiguration(objectMapper.writeValueAsString(Map.of("directory", "/tmp")));

        CreateAdapterRequest outboundRequest = new CreateAdapterRequest();
        outboundRequest.setName("Outbound Test Adapter");
        outboundRequest.setType("HTTP");
        outboundRequest.setMode("OUTBOUND");
        outboundRequest.setBusinessComponentId(businessComponentId);
        outboundRequest.setConfiguration(objectMapper.writeValueAsString(Map.of("url", "http://example.com")));

        mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inboundRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outboundRequest)))
                .andExpect(status().isCreated());

        // When & Then - Get only INBOUND adapters
        mockMvc.perform(get("/api/communication-adapters?mode=INBOUND")
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.mode == 'INBOUND')]").exists())
                .andExpect(jsonPath("$[?(@.mode == 'OUTBOUND')]").doesNotExist());

        // When & Then - Get only OUTBOUND adapters
        mockMvc.perform(get("/api/communication-adapters?mode=OUTBOUND")
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.mode == 'OUTBOUND')]").exists())
                .andExpect(jsonPath("$[?(@.mode == 'INBOUND')]").doesNotExist());
    }

    @Test
    void testAdapterConnectionTest() throws Exception {
        // Given - Create adapter
        CreateAdapterRequest createRequest = new CreateAdapterRequest();
        createRequest.setName("Adapter to Test Connection");
        createRequest.setType("HTTP");
        createRequest.setMode("OUTBOUND");
        createRequest.setBusinessComponentId(businessComponentId);
        createRequest.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "url", "https://httpstat.us/200",
                "method", "GET",
                "timeout", 5000
        )));

        MvcResult createResult = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AdapterResponse createdAdapter = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                AdapterResponse.class);
        String adapterId = createdAdapter.getId();

        // When - Test connection
        TestAdapterRequest testRequest = new TestAdapterRequest();
        testRequest.setAdapterId(adapterId);
        testRequest.setTestData(objectMapper.writeValueAsString(Map.of("test", "data")));

        // Then
        mockMvc.perform(post("/api/communication-adapters/" + adapterId + "/test")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}