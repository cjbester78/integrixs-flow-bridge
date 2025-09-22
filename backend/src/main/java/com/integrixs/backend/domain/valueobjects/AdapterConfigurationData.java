package com.integrixs.backend.domain.valueobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.shared.exceptions.ValidationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Value object representing adapter configuration data.
 *
 * <p>Encapsulates and validates adapter configuration data.
 * This is distinct from the domain model in the adapters module.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class AdapterConfigurationData {

    private final Map<String, Object> properties;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new AdapterConfiguration.
     *
     * @param properties the configuration properties
     */
    private AdapterConfigurationData(Map<String, Object> properties) {
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
    }

    /**
     * Gets the properties map (immutable).
     *
     * @return unmodifiable map of properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Creates an AdapterConfiguration from a map.
     *
     * @param properties the properties map
     * @return new AdapterConfiguration instance
     */
    public static AdapterConfigurationData of(Map<String, Object> properties) {
        if(properties == null) {
            return new AdapterConfigurationData(Collections.emptyMap());
        }
        return new AdapterConfigurationData(properties);
    }

    /**
     * Creates an AdapterConfiguration from JSON string.
     *
     * @param json the JSON string
     * @return new AdapterConfiguration instance
     * @throws ValidationException if JSON is invalid
     */
    public static AdapterConfigurationData fromJson(String json) {
        if(json == null || json.trim().isEmpty()) {
            return new AdapterConfigurationData(Collections.emptyMap());
        }

        try {
            Map<String, Object> properties = objectMapper.readValue(json, Map.class);
            return new AdapterConfigurationData(properties);
        } catch(JsonProcessingException e) {
            throw new ValidationException("Invalid configuration JSON: " + e.getMessage());
        }
    }

    /**
     * Gets a property value.
     *
     * @param key the property key
     * @return the property value or null
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Gets a property value with type casting.
     *
     * @param key the property key
     * @param type the expected type
     * @param <T> the type parameter
     * @return the typed value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if(value == null) {
            return null;
        }

        if(!type.isInstance(value)) {
            throw new ValidationException(
                String.format("Property '%s' is not of type %s", key, type.getSimpleName()));
        }

        return(T) value;
    }

    /**
     * Gets a required property value.
     *
     * @param key the property key
     * @param type the expected type
     * @param <T> the type parameter
     * @return the typed value
     * @throws ValidationException if property is missing or wrong type
     */
    public <T> T getRequiredProperty(String key, Class<T> type) {
        T value = getProperty(key, type);
        if(value == null) {
            throw new ValidationException(String.format("Required property '%s' is missing", key));
        }
        return value;
    }

    /**
     * Checks if a property exists.
     *
     * @param key the property key
     * @return true if property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Converts to JSON string.
     *
     * @return JSON representation
     */
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(properties);
        } catch(JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize configuration", e);
        }
    }

    /**
     * Creates a builder for modifying configuration.
     *
     * @return new builder with current properties
     */
    public Builder toBuilder() {
        return new Builder(new HashMap<>(properties));
    }

    /**
     * Creates a new builder.
     *
     * @return new empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for AdapterConfiguration.
     */
    public static class Builder {
        private final Map<String, Object> properties;

        private Builder() {
            this.properties = new HashMap<>();
        }

        private Builder(Map<String, Object> properties) {
            this.properties = properties;
        }

        public Builder property(String key, Object value) {
            properties.put(key, value);
            return this;
        }

        public Builder properties(Map<String, Object> props) {
            properties.putAll(props);
            return this;
        }

        public Builder removeProperty(String key) {
            properties.remove(key);
            return this;
        }

        public AdapterConfigurationData build() {
            return new AdapterConfigurationData(properties);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterConfigurationData that = (AdapterConfigurationData) o;
        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public String toString() {
        return "AdapterConfigurationData{" +
                "properties=" + properties +
                '}';
    }
}
