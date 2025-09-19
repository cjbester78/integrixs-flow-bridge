package com.integrixs.shared.dto.adapter;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * DTO for adapter test requests.
 *
 * <p>Used to test adapter connectivity and functionality before
 * deploying to production flows.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class AdapterTestRequestDTO {

    /**
     * Type of adapter to test(HTTP, JDBC, SOAP, etc.)
     */
    @NotBlank(message = "Adapter type is required")
    private String adapterType;

    /**
     * Test payload to send through the adapter
     */
    @NotBlank(message = "Test payload is required")
    private String payload;

    // Default constructor
    public AdapterTestRequestDTO() {
    }

    // All args constructor
    public AdapterTestRequestDTO(String adapterType, String payload) {
        this.adapterType = adapterType;
        this.payload = payload;
    }

    // Getters
    public String getAdapterType() {
        return adapterType;
    }

    public String getPayload() {
        return payload;
    }

    // Setters
    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterType;
        private String payload;

        public Builder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public AdapterTestRequestDTO build() {
            return new AdapterTestRequestDTO(adapterType, payload);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterTestRequestDTO that = (AdapterTestRequestDTO) o;
        return Objects.equals(adapterType, that.adapterType) &&
               Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adapterType, payload);
    }

    @Override
    public String toString() {
        return "AdapterTestRequestDTO{" +
                "adapterType='" + adapterType + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
