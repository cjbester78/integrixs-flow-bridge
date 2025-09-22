package com.integrixs.backend.plugin.dto;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import java.util.List;

/**
 * DTO for plugin upload results
 */
public class UploadResultDto {
    private boolean successful;
    private String pluginId;
    private AdapterMetadata metadata;
    private List<String> warnings;
    private String error;

    public static UploadResultDto success(String pluginId, AdapterMetadata metadata, List<String> warnings) {
        return UploadResultDto.builder()
                .successful(true)
                .pluginId(pluginId)
                .metadata(metadata)
                .warnings(warnings)
                .build();
    }

    public static UploadResultDto failure(String error) {
        return UploadResultDto.builder()
                .successful(false)
                .error(error)
                .build();
    }

    // Default constructor
    public UploadResultDto() {
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public AdapterMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AdapterMetadata metadata) {
        this.metadata = metadata;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Builder pattern
    public static UploadResultDtoBuilder builder() {
        return new UploadResultDtoBuilder();
    }

    public static class UploadResultDtoBuilder {
        private boolean successful;
        private String pluginId;
        private AdapterMetadata metadata;
        private List<String> warnings;
        private String error;

        public UploadResultDtoBuilder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public UploadResultDtoBuilder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public UploadResultDtoBuilder metadata(AdapterMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public UploadResultDtoBuilder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public UploadResultDtoBuilder error(String error) {
            this.error = error;
            return this;
        }

        public UploadResultDto build() {
            UploadResultDto dto = new UploadResultDto();
            dto.successful = this.successful;
            dto.pluginId = this.pluginId;
            dto.metadata = this.metadata;
            dto.warnings = this.warnings;
            dto.error = this.error;
            return dto;
        }
    }
}
