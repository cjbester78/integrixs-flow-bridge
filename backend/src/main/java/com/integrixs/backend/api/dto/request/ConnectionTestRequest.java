package com.integrixs.backend.api.dto.request;

import com.integrixs.backend.model.enums.AdapterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestRequest {

    @NotNull(message = "Adapter type is required")
    private AdapterType adapterType;

    @NotBlank(message = "Adapter name is required")
    private String adapterName;

    @NotNull(message = "Configuration is required")
    @Builder.Default
    private Map<String, Object> configuration = new HashMap<>();

    // Optional test parameters
    private Integer timeout = 30000; // Default 30 seconds
    private boolean performExtendedTests = false;
    private boolean includeMetadata = true;
}
