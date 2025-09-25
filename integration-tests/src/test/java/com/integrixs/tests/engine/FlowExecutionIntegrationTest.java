package com.integrixs.tests.engine;

import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.CreateFlowRequest;
import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.auth.LoginResponse;
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
 * Integration tests for Engine Flow Execution
 */
public class FlowExecutionIntegrationTest extends BaseIntegrationTest {

    private String flowId;
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

        // Create a business component
        Map<String, Object> bcRequest = new HashMap<>();
        bcRequest.put("name", "Engine Test Business Component");
        bcRequest.put("type", "DEPARTMENT");

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

        // Create adapters and flow for execution testing
        CreateAdapterRequest sourceAdapter = new CreateAdapterRequest();
        sourceAdapter.setName("Execution Source Adapter");
        sourceAdapter.setType("FILE");
        sourceAdapter.setMode("INBOUND");
        sourceAdapter.setBusinessComponentId(businessComponentId);
        sourceAdapter.setConfiguration(objectMapper.writeValueAsString(Map.of("directory", "/tmp/execution")));

        MvcResult sourceResult = mockMvc.perform(post("/api/communication-adapters")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sourceAdapter)))
                .andExpect(status().isCreated())
                .andReturn();

        AdapterResponse sourceResponse = objectMapper.readValue(
                sourceResult.getResponse().getContentAsString(), 
                AdapterResponse.class);

        CreateAdapterRequest targetAdapter = new CreateAdapterRequest();
        targetAdapter.setName("Execution Target Adapter");
        targetAdapter.setType("HTTP");
        targetAdapter.setMode("OUTBOUND");
        targetAdapter.setBusinessComponentId(businessComponentId);
        targetAdapter.setConfiguration(objectMapper.writeValueAsString(Map.of(
                "url", "http://localhost:8080/test",
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

        // Create and activate flow
        CreateFlowRequest flowRequest = new CreateFlowRequest();
        flowRequest.setName("Execution Test Flow");
        flowRequest.setInboundAdapterId(sourceResponse.getId());
        flowRequest.setOutboundAdapterId(targetResponse.getId());

        MvcResult flowResult = mockMvc.perform(post("/api/flows")
                .header("Authorization", getAuthHeader(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(flowRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        FlowResponse flowResponse = objectMapper.readValue(
                flowResult.getResponse().getContentAsString(), 
                FlowResponse.class);
        flowId = flowResponse.getId();

        // Flow is active by default
    }

    @Test
    void testFlowCreation() throws Exception {
        // Verify the flow was created successfully in the setup
        assertThat(flowId).isNotNull();
        assertThat(businessComponentId).isNotNull();
    }
}