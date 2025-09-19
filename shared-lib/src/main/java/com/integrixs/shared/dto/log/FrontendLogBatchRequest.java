package com.integrixs.shared.dto.log;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO for submitting a batch of frontend log entries.
 * Used for efficient bulk logging from the frontend.
 */
public class FrontendLogBatchRequest {

    private List<FrontendLogEntry> logs;

    // Default constructor
    public FrontendLogBatchRequest() {
        this.logs = new ArrayList<>();
    }

    // All args constructor
    public FrontendLogBatchRequest(List<FrontendLogEntry> logs) {
        this.logs = logs != null ? logs : new ArrayList<>();
    }

    // Getters
    public List<FrontendLogEntry> getLogs() { return logs; }

    // Setters
    public void setLogs(List<FrontendLogEntry> logs) { this.logs = logs; }

    // Builder
    public static FrontendLogBatchRequestBuilder builder() {
        return new FrontendLogBatchRequestBuilder();
    }

    public static class FrontendLogBatchRequestBuilder {
        private List<FrontendLogEntry> logs = new ArrayList<>();

        public FrontendLogBatchRequestBuilder logs(List<FrontendLogEntry> logs) {
            this.logs = logs;
            return this;
        }

        public FrontendLogBatchRequest build() {
            return new FrontendLogBatchRequest(logs);
        }
    }
}
