package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request object for testing an adapter connection
 */
public class TestAdapterRequest {

    @NotBlank(message = "Adapter ID is required")
    private String adapterId;

    private String testData;

    private Map<String, String> testParameters;

    private boolean validateOnly = false;

    // Default constructor
    public TestAdapterRequest() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public String getTestData() {
        return testData;
    }

    public void setTestData(String testData) {
        this.testData = testData;
    }

    public boolean isValidateOnly() {
        return validateOnly;
    }

    public void setValidateOnly(boolean validateOnly) {
        this.validateOnly = validateOnly;
    }
}
