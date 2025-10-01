package com.integrixs.backend.api.dto.request;

import com.integrixs.shared.enums.AdapterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.HashMap;

public class ConnectionTestRequest {

    @NotNull(message = "Adapter type is required")
    private AdapterType adapterType;

    @NotBlank(message = "Adapter name is required")
    private String adapterName;

    @NotNull(message = "Configuration is required")
    private Map<String, Object> configuration = new HashMap<>();

    // Optional test parameters
    private Integer timeout = 30000; // Default 30 seconds
    private boolean performExtendedTests = false;
    private boolean includeMetadata = true;

    // Default constructor
    public ConnectionTestRequest() {
    }

    public AdapterType getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(AdapterType adapterType) {
        this.adapterType = adapterType;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public boolean isPerformExtendedTests() {
        return performExtendedTests;
    }

    public void setPerformExtendedTests(boolean performExtendedTests) {
        this.performExtendedTests = performExtendedTests;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }
}
