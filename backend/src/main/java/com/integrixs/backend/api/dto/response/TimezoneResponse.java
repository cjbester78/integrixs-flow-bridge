package com.integrixs.backend.api.dto.response;

/**
 * Response DTO for timezone information
 */
public class TimezoneResponse {

    private String id;
    private String displayName;

    // Default constructor
    public TimezoneResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    // Builder
    public static TimezoneResponseBuilder builder() {
        return new TimezoneResponseBuilder();
    }

    public static class TimezoneResponseBuilder {
        private String id;
        private String displayName;

        public TimezoneResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public TimezoneResponseBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public TimezoneResponse build() {
            TimezoneResponse result = new TimezoneResponse();
            result.setId(this.id);
            result.setDisplayName(this.displayName);
            return result;
        }
    }
}
