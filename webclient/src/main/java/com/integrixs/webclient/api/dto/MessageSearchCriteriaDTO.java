package com.integrixs.webclient.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for message search criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchCriteriaDTO {

    private String status;
    private String flowId;
    private String correlationId;
    private String source;
    private String adapterId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer limit;
}
