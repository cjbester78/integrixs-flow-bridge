package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for request history search criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistoryCriteriaDTO {
    
    private String flowId;
    private String adapterId;
    private String endpointId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean onlyFailed;
    private Integer limit;
}