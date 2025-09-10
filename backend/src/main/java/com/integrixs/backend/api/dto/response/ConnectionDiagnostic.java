package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionDiagnostic {
    
    public enum Status {
        SUCCESS, FAILED, WARNING, SKIPPED
    }
    
    private String step;
    private Status status;
    private String message;
    private long duration; // in milliseconds
    
    @Builder.Default
    private Map<String, Object> details = new HashMap<>();
    
    // Optional error details
    private String errorCode;
    private String stackTrace;
}