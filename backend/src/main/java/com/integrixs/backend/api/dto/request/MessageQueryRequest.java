package com.integrixs.backend.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request object for querying messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageQueryRequest {

    private List<String> status;
    private String source;
    private String target;
    private String type;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String search;
    private String businessComponentId;
    private String correlationId;

    @Min(0)
    @Builder.Default
    private int page = 0;

    @Min(1)
    @Max(100)
    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "timestamp";

    @Builder.Default
    private String sortDirection = "DESC";
}
