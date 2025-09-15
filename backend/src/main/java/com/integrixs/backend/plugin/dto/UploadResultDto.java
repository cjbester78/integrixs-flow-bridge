package com.integrixs.backend.plugin.dto;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * DTO for plugin upload results
 */
@Data
@Builder
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
}
