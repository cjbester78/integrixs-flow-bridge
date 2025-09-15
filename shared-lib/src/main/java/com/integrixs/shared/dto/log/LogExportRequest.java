package com.integrixs.shared.dto.log;

import lombok.Data;

import java.util.List;

/**
 * Request for exporting logs.
 */
@Data
public class LogExportRequest {

    // Export format
    private String format; // CSV, JSON, XML, EXCEL, TEXT, HTML, ZIP

    // Source selection
    private LogSearchCriteria searchCriteria;
    private String correlationId;
    private List<String> logIds;

    // Export options
    private boolean includeUserInfo = true;
    private boolean includeCorrelationInfo = true;
    private boolean includeDetails = false;
    private boolean includeStackTrace = false;
    private boolean prettyPrint = true;

    // File options
    private String filename;
    private String compression; // none, gzip, zip
}
