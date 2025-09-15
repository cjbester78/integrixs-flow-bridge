package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResponse {

    private boolean success;
    private String message;

    @Builder.Default
    private List<ConnectionDiagnostic> diagnostics = new ArrayList<>();

    private long duration; // in milliseconds
    private LocalDateTime timestamp;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    // Connection health score(0-100)
    private Integer healthScore;

    // Recommendations for connection improvement
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();
}
