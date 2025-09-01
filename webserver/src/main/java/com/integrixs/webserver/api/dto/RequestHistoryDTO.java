package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for request history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistoryDTO {
    
    private String requestId;
    private String requestType;
    private String targetUrl;
    private String httpMethod;
    private String flowId;
    private String adapterId;
    private Integer statusCode;
    private Boolean success;
    private Long responseTime;
    private String errorMessage;
}