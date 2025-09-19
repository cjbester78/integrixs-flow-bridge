package com.integrixs.backend.domain.valueobjects;

import com.integrixs.shared.exceptions.ValidationException;
import java.util.regex.Pattern;

/**
 * Value object representing a flow name.
 *
 * <p>Ensures flow names follow business rules and conventions.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowName {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_\\-\\s]*$");

    private final String value;

    /**
     * Creates a new FlowName.
     *
     * @param value the flow name
     * @throws ValidationException if the name is invalid
     */
    public FlowName(String value) {
        if(value == null || value.trim().isEmpty()) {
            throw new ValidationException("Flow name cannot be empty");
        }

        String trimmed = value.trim();

        if(trimmed.length() < MIN_LENGTH) {
            throw new ValidationException(
                String.format("Flow name must be at least %d characters", MIN_LENGTH));
        }

        if(trimmed.length() > MAX_LENGTH) {
            throw new ValidationException(
                String.format("Flow name cannot exceed %d characters", MAX_LENGTH));
        }

        if(!VALID_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException(
                "Flow name must start with alphanumeric and contain only letters, numbers, spaces, hyphens, and underscores");
        }

        this.value = trimmed;
    }

    /**
     * Creates a FlowName from a string value.
     *
     * @param value the string value
     * @return new FlowName instance
     */
    public static FlowName of(String value) {
        return new FlowName(value);
    }

    /**
     * Gets the flow name value.
     *
     * @return the flow name
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowName flowName = (FlowName) o;
        return value.equals(flowName.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
