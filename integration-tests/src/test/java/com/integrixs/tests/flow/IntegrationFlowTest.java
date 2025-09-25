package com.integrixs.tests.flow;

import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.CreateFlowRequest;
import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.auth.LoginResponse;
import com.integrixs.backend.api.dto.request.UpdateFlowRequest;
import com.integrixs.backend.api.dto.response.AdapterResponse;
import com.integrixs.backend.api.dto.response.FlowResponse;
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
 * Integration tests for Integration Flow management
 */
public class IntegrationFlowTest extends BaseIntegrationTest {

    private String businessComponentId;
    private String sourceAdapterId;
    private String targetAdapterId;

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

        // Create a business component
        Map<String, Object> bcRequest = new HashMap<>();
        bcRequest.put("name", "Flow Test Business Component");
        bcRequest.put("type", "DEPARTMENT");
        bcRequest.put("description", "Test component for flow tests");

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

        // Create source adapter (INBOUND)
        CreateAdapterRequest sourceAdapter = new CreateAdapterRequest();
        sourceAdapter.setName("Source File Adapter");
        sourceAdapter.setType("FILE");
        sourceAdapter.setMode("INBOUND");
        sourceAdapter.setBusinessComponentId(businessComponentId);
        sourceAdapter.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "directory", "/tmp/source",
                "filePattern", "*.xml"
        )));

        MvcResult sourceResult = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sourceAdapter)))
                .andExpect(status().isCreated())
                .andReturn();

        AdapterResponse sourceResponse = objectMapper.readValue(
                sourceResult.getResponse().getContentAsString(), 
                AdapterResponse.class);
        sourceAdapterId = sourceResponse.getId();

        // Create target adapter (OUTBOUND)
        CreateAdapterRequest targetAdapter = new CreateAdapterRequest();
        targetAdapter.setName("Target HTTP Adapter");
        targetAdapter.setType("HTTP");
        targetAdapter.setMode("OUTBOUND");
        targetAdapter.setBusinessComponentId(businessComponentId);
        targetAdapter.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "url", "http://example.com/api",
                "method", "POST"
        )));

        MvcResult targetResult = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(targetAdapter)))
                .andExpect(status().isCreated())
                .andReturn();

        AdapterResponse targetResponse = objectMapper.readValue(
                targetResult.getResponse().getContentAsString(), 
                AdapterResponse.class);
        targetAdapterId = targetResponse.getId();
    }

    @Test
    void testCreateBasicFlow() throws Exception {
        // Given
        CreateFlowRequest request = new CreateFlowRequest();
        request.setName("Test Basic Flow");
        request.setDescription("Integration test basic flow");
        request.setInboundAdapterId(sourceAdapterId);
        request.setOutboundAdapterId(targetAdapterId);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Basic Flow"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        FlowResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                FlowResponse.class);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getInboundAdapterId()).isEqualTo(sourceAdapterId);
        assertThat(response.getOutboundAdapterId()).isEqualTo(targetAdapterId);
    }

    @Test
    void testCreateTransformationFlow() throws Exception {
        // Given
        CreateFlowRequest request = new CreateFlowRequest();
        request.setName("Test Transformation Flow");
        request.setDescription("Integration test transformation flow");
        request.setInboundAdapterId(sourceAdapterId);
        request.setOutboundAdapterId(targetAdapterId);

        // When & Then
        mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Transformation Flow"));
    }

    @Test
    void testUpdateFlow() throws Exception {
        // Given - Create flow first
        CreateFlowRequest createRequest = new CreateFlowRequest();
        createRequest.setName("Flow to Update");
        createRequest.setInboundAdapterId(sourceAdapterId);
        createRequest.setOutboundAdapterId(targetAdapterId);

        MvcResult createResult = mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        FlowResponse createdFlow = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                FlowResponse.class);
        String flowId = createdFlow.getId();

        // When - Update the flow
        UpdateFlowRequest updateRequest = new UpdateFlowRequest();
        updateRequest.setName("Updated Flow Name");
        updateRequest.setDescription("Updated description");
        updateRequest.setInboundAdapterId(sourceAdapterId);
        updateRequest.setOutboundAdapterId(targetAdapterId);

        // Then
        mockMvc.perform(put("/api/flows/" + flowId)
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Flow Name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void testFlowActivation() throws Exception {
        // Given - Create a flow
        CreateFlowRequest request = new CreateFlowRequest();
        request.setName("Lifecycle Test Flow");
        request.setDescription("Testing flow activation");
        request.setInboundAdapterId(sourceAdapterId);
        request.setOutboundAdapterId(targetAdapterId);
        request.setActive(false);
        
        MvcResult createResult = mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        FlowResponse createdFlow = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                FlowResponse.class);
        String flowId = createdFlow.getId();
        
        // Verify initial state
        assertThat(createdFlow.isActive()).isFalse();
        
        // When - Update to activate the flow
        UpdateFlowRequest updateRequest = new UpdateFlowRequest();
        updateRequest.setName(createdFlow.getName());
        updateRequest.setActive(true);
        
        mockMvc.perform(put("/api/flows/" + flowId)
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void testDeleteFlow() throws Exception {
        // Given - Create flow first
        CreateFlowRequest createRequest = new CreateFlowRequest();
        createRequest.setName("Flow to Delete");
        createRequest.setInboundAdapterId(sourceAdapterId);
        createRequest.setOutboundAdapterId(targetAdapterId);

        MvcResult createResult = mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        FlowResponse createdFlow = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                FlowResponse.class);
        String flowId = createdFlow.getId();

        // When - Delete the flow
        mockMvc.perform(delete("/api/flows/" + flowId)
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isNoContent());

        // Then - Verify it's deleted
        mockMvc.perform(get("/api/flows/" + flowId)
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFlowsByBusinessComponent() throws Exception {
        // Given - Create multiple flows
        CreateFlowRequest flow1 = new CreateFlowRequest();
        flow1.setName("Flow 1");
        flow1.setInboundAdapterId(sourceAdapterId);
        flow1.setOutboundAdapterId(targetAdapterId);

        CreateFlowRequest flow2 = new CreateFlowRequest();
        flow2.setName("Flow 2");
        flow2.setInboundAdapterId(sourceAdapterId);
        flow2.setOutboundAdapterId(targetAdapterId);

        mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(flow1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(flow2)))
                .andExpect(status().isCreated());

        // When & Then - Get flows by business component
        mockMvc.perform(get("/api/flows?businessComponentId=" + businessComponentId)
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name == 'Flow 1')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Flow 2')]").exists());
    }

    @Test
    void testFlowExportImport() throws Exception {
        // Given - Create a flow
        CreateFlowRequest request = new CreateFlowRequest();
        request.setName("Export Test Flow");
        request.setDescription("Testing export functionality");
        request.setInboundAdapterId(sourceAdapterId);
        request.setOutboundAdapterId(targetAdapterId);
        
        MvcResult createResult = mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        FlowResponse createdFlow = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                FlowResponse.class);
        String flowId = createdFlow.getId();

        // When - Export the flow
        MvcResult exportResult = mockMvc.perform(get("/api/flows/export-import/export/" + flowId)
                .header("Authorization", getAuthHeader(adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Verify export contains expected data
        String exportContent = exportResult.getResponse().getContentAsString();
        assertThat(exportContent).isNotEmpty();
        assertThat(exportContent).contains("Export Test Flow");
    }
}