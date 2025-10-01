package com.integrixs.shared.dto.log;

import java.util.List;
import java.util.ArrayList;

/**
 * Request for exporting logs.
 */
public class LogExportRequest {

    private String format;
    private LogSearchCriteria searchCriteria;
    private String correlationId;
    private List<String> logIds;
    private boolean includeUserInfo = true;
    private boolean includeCorrelationInfo = true;
    private boolean includeDetails = false;
    private boolean includeStackTrace = false;
    private boolean prettyPrint = true;
    private String filename;
    private String compression;

    // Default constructor
    public LogExportRequest() {
        this.logIds = new ArrayList<>();
    }

    // All args constructor
    public LogExportRequest(String format, LogSearchCriteria searchCriteria, String correlationId, List<String> logIds, boolean includeUserInfo, boolean includeCorrelationInfo, boolean includeDetails, boolean includeStackTrace, boolean prettyPrint, String filename, String compression) {
        this.format = format;
        this.searchCriteria = searchCriteria;
        this.correlationId = correlationId;
        this.logIds = logIds != null ? logIds : new ArrayList<>();
        this.includeUserInfo = includeUserInfo;
        this.includeCorrelationInfo = includeCorrelationInfo;
        this.includeDetails = includeDetails;
        this.includeStackTrace = includeStackTrace;
        this.prettyPrint = prettyPrint;
        this.filename = filename;
        this.compression = compression;
    }

    // Getters
    public String getFormat() { return format; }
    public LogSearchCriteria getSearchCriteria() { return searchCriteria; }
    public String getCorrelationId() { return correlationId; }
    public List<String> getLogIds() { return logIds; }
    public boolean isIncludeUserInfo() { return includeUserInfo; }
    public boolean isIncludeCorrelationInfo() { return includeCorrelationInfo; }
    public boolean isIncludeDetails() { return includeDetails; }
    public boolean isIncludeStackTrace() { return includeStackTrace; }
    public boolean isPrettyPrint() { return prettyPrint; }
    public String getFilename() { return filename; }
    public String getCompression() { return compression; }

    // Setters
    public void setFormat(String format) { this.format = format; }
    public void setSearchCriteria(LogSearchCriteria searchCriteria) { this.searchCriteria = searchCriteria; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setLogIds(List<String> logIds) { this.logIds = logIds; }
    public void setIncludeUserInfo(boolean includeUserInfo) { this.includeUserInfo = includeUserInfo; }
    public void setIncludeCorrelationInfo(boolean includeCorrelationInfo) { this.includeCorrelationInfo = includeCorrelationInfo; }
    public void setIncludeDetails(boolean includeDetails) { this.includeDetails = includeDetails; }
    public void setIncludeStackTrace(boolean includeStackTrace) { this.includeStackTrace = includeStackTrace; }
    public void setPrettyPrint(boolean prettyPrint) { this.prettyPrint = prettyPrint; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setCompression(String compression) { this.compression = compression; }
}
