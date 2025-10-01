package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.SystemLog;
import com.integrixs.shared.dto.log.LogSearchCriteria;
import com.integrixs.shared.dto.log.LogExportRequest;
import com.integrixs.shared.dto.log.CorrelatedLogGroup;
import com.integrixs.shared.dto.system.SystemLogDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced service for exporting logs in various formats.
 */
@Service
public class LogExportService {

    private static final Logger log = LoggerFactory.getLogger(LogExportService.class);


    private final LogSearchService logSearchService;
    private final LogCorrelationService logCorrelationService;
    private final ObjectMapper objectMapper;

    public LogExportService(LogSearchService logSearchService,
                           LogCorrelationService logCorrelationService,
                           ObjectMapper objectMapper) {
        this.logSearchService = logSearchService;
        this.logCorrelationService = logCorrelationService;
        this.objectMapper = objectMapper;
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy - MM - dd HH:mm:ss");

    /**
     * Export logs based on export request.
     */
    public byte[] exportLogs(LogExportRequest request) {
        try {
            List<SystemLogDTO> logs = getLogs(request);

            switch(request.getFormat().toUpperCase()) {
                case "CSV":
                    return exportToCsv(logs, request);
                case "JSON":
                    return exportToJson(logs, request);
                case "XML":
                    return exportToXml(logs, request);
                case "EXCEL":
                    return exportToExcel(logs, request);
                case "TEXT":
                    return exportToText(logs, request);
                case "HTML":
                    return exportToHtml(logs, request);
                case "ZIP":
                    return exportToZip(logs, request);
                default:
                    throw new IllegalArgumentException("Unsupported export format: " + request.getFormat());
            }
        } catch(Exception e) {
            log.error("Error exporting logs", e);
            throw new RuntimeException("Failed to export logs: " + e.getMessage(), e);
        }
    }

    /**
     * Get logs based on export request.
     */
    private List<SystemLogDTO> getLogs(LogExportRequest request) {
        if(request.getSearchCriteria() != null) {
            return logSearchService.searchLogs(request.getSearchCriteria()).getLogs();
        } else if(request.getCorrelationId() != null) {
            CorrelatedLogGroup group = logCorrelationService.getCorrelatedLogs(request.getCorrelationId());
            return group != null ? group.getLogs() : Collections.emptyList();
        } else if(request.getLogIds() != null && !request.getLogIds().isEmpty()) {
            // Export specific logs by IDs
            List<SystemLogDTO> logs = new ArrayList<>();
            for(String logId : request.getLogIds()) {
                // Note: Would need to add findById method to repository
                log.debug("Would fetch log by ID: {}", logId);
            }
            return logs;
        }

        return Collections.emptyList();
    }

    /**
     * Export to CSV format with enhanced options.
     */
    private byte[] exportToCsv(List<SystemLogDTO> logs, LogExportRequest request) {
        StringBuilder csv = new StringBuilder();

        // Headers
        List<String> headers = new ArrayList<>(Arrays.asList(
            "Timestamp", "Level", "Source", "Component", "Message"
       ));

        if(request.isIncludeUserInfo()) {
            headers.addAll(Arrays.asList("User ID", "Username", "IP Address"));
        }

        if(request.isIncludeCorrelationInfo()) {
            headers.addAll(Arrays.asList("Correlation ID", "Session ID"));
        }

        if(request.isIncludeDetails()) {
            headers.add("Details");
        }

        if(request.isIncludeStackTrace()) {
            headers.add("Stack Trace");
        }

        csv.append(String.join(",", headers)).append("\n");

        // Data rows
        for(SystemLogDTO log : logs) {
            List<String> values = new ArrayList<>();

            values.add(escapeCSV(log.getTimestamp().format(DATE_FORMATTER)));
            values.add(escapeCSV(log.getLevel()));
            values.add(escapeCSV(log.getSource()));
            values.add(escapeCSV(log.getComponent()));
            values.add(escapeCSV(log.getMessage()));

            if(request.isIncludeUserInfo()) {
                values.add(escapeCSV(log.getUserId() != null ? log.getUserId() : ""));
                values.add(escapeCSV(log.getUserId()));
                values.add(escapeCSV(log.getClientIp()));
            }

            if(request.isIncludeCorrelationInfo()) {
                values.add(escapeCSV(log.getCorrelationId()));
                values.add(escapeCSV(log.getCorrelationId()));
            }

            if(request.isIncludeDetails()) {
                values.add(escapeCSV(log.getDetails()));
            }

            if(request.isIncludeStackTrace()) {
                values.add(escapeCSV(log.getContext() != null && log.getContext().get("stackTrace") != null ? log.getContext().get("stackTrace").toString() : ""));
            }

            csv.append(String.join(",", values)).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Export to JSON format with formatting options.
     */
    private byte[] exportToJson(List<SystemLogDTO> logs, LogExportRequest request) throws Exception {
        List<Map<String, Object>> exportData = new ArrayList<>();

        for(SystemLogDTO log : logs) {
            Map<String, Object> logData = new HashMap<>();

            logData.put("timestamp", log.getTimestamp().toString());
            logData.put("level", log.getLevel());
            logData.put("source", log.getSource());
            logData.put("category", log.getComponent());
            logData.put("message", log.getMessage());

            if(request.isIncludeUserInfo()) {
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("userId", log.getUserId());
                userInfo.put("username", log.getUserId());
                userInfo.put("ipAddress", log.getClientIp());
                logData.put("userInfo", userInfo);
            }

            if(request.isIncludeCorrelationInfo()) {
                Map<String, String> correlationInfo = new HashMap<>();
                correlationInfo.put("correlationId", log.getCorrelationId());
                correlationInfo.put("sessionId", log.getCorrelationId());
                logData.put("correlationInfo", correlationInfo);
            }

            if(request.isIncludeDetails() && log.getDetails() != null) {
                try {
                    // Try to parse details as JSON
                    logData.put("details", objectMapper.readValue(log.getDetails(), Map.class));
                } catch(Exception e) {
                    logData.put("details", log.getDetails());
                }
            }

            if(request.isIncludeStackTrace() && log.getContext() != null && log.getContext().get("stackTrace") != null) {
                logData.put("stackTrace", log.getContext().get("stackTrace"));
            }

            exportData.add(logData);
        }

        if(request.isPrettyPrint()) {
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(exportData);
        } else {
            return objectMapper.writeValueAsBytes(exportData);
        }
    }

    /**
     * Export to XML format.
     */
    private byte[] exportToXml(List<SystemLogDTO> logs, LogExportRequest request) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Root element
        Element root = doc.createElement("logs");
        root.setAttribute("exportTime", LocalDateTime.now().toString());
        root.setAttribute("count", String.valueOf(logs.size()));
        doc.appendChild(root);

        // Log entries
        for(SystemLogDTO log : logs) {
            Element logElement = doc.createElement("log");

            addXmlElement(doc, logElement, "timestamp", log.getTimestamp().toString());
            addXmlElement(doc, logElement, "level", log.getLevel());
            addXmlElement(doc, logElement, "source", log.getSource());
            addXmlElement(doc, logElement, "category", log.getComponent());
            addXmlElement(doc, logElement, "message", log.getMessage());

            if(request.isIncludeUserInfo()) {
                Element userElement = doc.createElement("user");
                addXmlElement(doc, userElement, "id", log.getUserId());
                addXmlElement(doc, userElement, "username", log.getUserId());
                addXmlElement(doc, userElement, "ipAddress", log.getClientIp());
                logElement.appendChild(userElement);
            }

            if(request.isIncludeCorrelationInfo()) {
                Element correlationElement = doc.createElement("correlation");
                addXmlElement(doc, correlationElement, "correlationId", log.getCorrelationId());
                addXmlElement(doc, correlationElement, "sessionId", log.getCorrelationId());
                logElement.appendChild(correlationElement);
            }

            if(request.isIncludeDetails() && log.getDetails() != null) {
                addXmlElement(doc, logElement, "details", log.getDetails());
            }

            if(request.isIncludeStackTrace() && log.getContext() != null && log.getContext().get("stackTrace") != null) {
                addXmlElement(doc, logElement, "stackTrace", log.getContext().get("stackTrace").toString());
            }

            root.appendChild(logElement);
        }

        // Transform to string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("indent", request.isPrettyPrint() ? "yes" : "no");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(baos));

        return baos.toByteArray();
    }

    /**
     * Export to Excel format.
     */
    private byte[] exportToExcel(List<SystemLogDTO> logs, LogExportRequest request) throws Exception {
        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Logs");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;

            createCell(headerRow, colIndex++, "Timestamp", headerStyle);
            createCell(headerRow, colIndex++, "Level", headerStyle);
            createCell(headerRow, colIndex++, "Source", headerStyle);
            createCell(headerRow, colIndex++, "Category", headerStyle);
            createCell(headerRow, colIndex++, "Message", headerStyle);

            if(request.isIncludeUserInfo()) {
                createCell(headerRow, colIndex++, "User ID", headerStyle);
                createCell(headerRow, colIndex++, "Username", headerStyle);
                createCell(headerRow, colIndex++, "IP Address", headerStyle);
            }

            if(request.isIncludeCorrelationInfo()) {
                createCell(headerRow, colIndex++, "Correlation ID", headerStyle);
                createCell(headerRow, colIndex++, "Session ID", headerStyle);
            }

            if(request.isIncludeDetails()) {
                createCell(headerRow, colIndex++, "Details", headerStyle);
            }

            if(request.isIncludeStackTrace()) {
                createCell(headerRow, colIndex++, "Stack Trace", headerStyle);
            }

            // Create data rows
            int rowIndex = 1;
            for(SystemLogDTO log : logs) {
                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;

                createCell(row, colIndex++, log.getTimestamp().format(DATE_FORMATTER));
                createCell(row, colIndex++, log.getLevel());
                createCell(row, colIndex++, log.getSource());
                createCell(row, colIndex++, log.getComponent());
                createCell(row, colIndex++, log.getMessage());

                if(request.isIncludeUserInfo()) {
                    createCell(row, colIndex++, log.getUserId() != null ? log.getUserId().toString() : "");
                    createCell(row, colIndex++, log.getUserId());
                    createCell(row, colIndex++, log.getClientIp());
                }

                if(request.isIncludeCorrelationInfo()) {
                    createCell(row, colIndex++, log.getCorrelationId());
                    createCell(row, colIndex++, log.getCorrelationId());
                }

                if(request.isIncludeDetails()) {
                    createCell(row, colIndex++, log.getDetails());
                }

                if(request.isIncludeStackTrace()) {
                    createCell(row, colIndex++, log.getContext() != null && log.getContext().get("stackTrace") != null ? log.getContext().get("stackTrace").toString() : "");
                }

                // Apply conditional formatting for error rows
                if("ERROR".equals(log.getLevel())) {
                    CellStyle errorStyle = workbook.createCellStyle();
                    errorStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
                    errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    for(int i = 0; i < colIndex; i++) {
                        Cell cell = row.getCell(i);
                        if(cell != null) {
                            cell.setCellStyle(errorStyle);
                        }
                    }
                }
            }

            // Auto - size columns
            for(int i = 0; i < colIndex; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Export to formatted text.
     */
    private byte[] exportToText(List<SystemLogDTO> logs, LogExportRequest request) {
        StringBuilder text = new StringBuilder();

        text.append("=== Log Export ===\n");
        text.append("Export Time: ").append(LocalDateTime.now()).append("\n");
        text.append("Total Logs: ").append(logs.size()).append("\n");
        text.append("==================\n\n");

        for(SystemLogDTO log : logs) {
            text.append("[").append(log.getTimestamp().format(DATE_FORMATTER)).append("] ");
            text.append(log.getLevel()).append(" - ");
            text.append(log.getSource()).append(" - ");
            text.append(log.getMessage()).append("\n");

            if(request.isIncludeUserInfo() && log.getUserId() != null) {
                text.append(" User: ").append(log.getUserId());
                if(log.getClientIp() != null) {
                    text.append(" (").append(log.getClientIp()).append(")");
                }
                text.append("\n");
            }

            if(request.isIncludeCorrelationInfo() && log.getCorrelationId() != null) {
                text.append(" Correlation ID: ").append(log.getCorrelationId()).append("\n");
            }

            if(request.isIncludeDetails() && log.getDetails() != null) {
                text.append(" Details: ").append(log.getDetails()).append("\n");
            }

            if(request.isIncludeStackTrace() && log.getContext() != null && log.getContext().get("stackTrace") != null) {
                text.append(" Stack Trace:\n");
                String[] lines = log.getContext().get("stackTrace").toString().split("\n");
                for(String line : lines) {
                    text.append("    ").append(line).append("\n");
                }
            }

            text.append("\n");
        }

        return text.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Export to HTML format.
     */
    private byte[] exportToHtml(List<SystemLogDTO> logs, LogExportRequest request) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>Log Export</title>\n");
        html.append("<style>\n");
        html.append("body { font - family: Arial, sans - serif; }\n");
        html.append("table { border - collapse: collapse; width: 100%; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text - align: left; }\n");
        html.append("th { background - color: #f2f2f2; }\n");
        html.append("tr:nth - child(even) { background - color: #f9f9f9; }\n");
        html.append(".error { background - color: #ffcccc; }\n");
        html.append(".warn { background - color: #fff3cd; }\n");
        html.append(".debug { color: #666; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");

        html.append("<h1>Log Export</h1>\n");
        html.append("<p>Export Time: ").append(LocalDateTime.now()).append("</p>\n");
        html.append("<p>Total Logs: ").append(logs.size()).append("</p>\n");

        html.append("<table>\n<thead>\n<tr>\n");
        html.append("<th>Timestamp</th>\n");
        html.append("<th>Level</th>\n");
        html.append("<th>Source</th>\n");
        html.append("<th>Message</th>\n");

        if(request.isIncludeUserInfo()) {
            html.append("<th>User</th>\n");
        }

        html.append("</tr>\n</thead>\n<tbody>\n");

        for(SystemLogDTO log : logs) {
            String rowClass = "";
            if("ERROR".equals(log.getLevel())) {
                rowClass = " class = \"error\"";
            } else if("WARN".equals(log.getLevel())) {
                rowClass = " class = \"warn\"";
            } else if("DEBUG".equals(log.getLevel())) {
                rowClass = " class = \"debug\"";
            }

            html.append("<tr").append(rowClass).append(">\n");
            html.append("<td>").append(log.getTimestamp().format(DATE_FORMATTER)).append("</td>\n");
            html.append("<td>").append(log.getLevel()).append("</td>\n");
            html.append("<td>").append(escapeHtml(log.getSource())).append("</td>\n");
            html.append("<td>").append(escapeHtml(log.getMessage())).append("</td>\n");

            if(request.isIncludeUserInfo()) {
                String userInfo = log.getUserId() != null ? log.getUserId() : "";
                if(log.getClientIp() != null) {
                    userInfo += " (" + log.getClientIp() + ")";
                }
                html.append("<td>").append(escapeHtml(userInfo)).append("</td>\n");
            }

            html.append("</tr>\n");
        }

        html.append("</tbody>\n</table>\n");
        html.append("</body>\n</html>");

        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Export to ZIP with multiple formats.
     */
    private byte[] exportToZip(List<SystemLogDTO> logs, LogExportRequest request) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try(ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Add CSV file
            ZipEntry csvEntry = new ZipEntry("logs.csv");
            zos.putNextEntry(csvEntry);
            zos.write(exportToCsv(logs, request));
            zos.closeEntry();

            // Add JSON file
            ZipEntry jsonEntry = new ZipEntry("logs.json");
            zos.putNextEntry(jsonEntry);
            zos.write(exportToJson(logs, request));
            zos.closeEntry();

            // Add text file
            ZipEntry textEntry = new ZipEntry("logs.txt");
            zos.putNextEntry(textEntry);
            zos.write(exportToText(logs, request));
            zos.closeEntry();

            // Add metadata file
            ZipEntry metaEntry = new ZipEntry("export_metadata.json");
            zos.putNextEntry(metaEntry);
            zos.write(createMetadata(logs, request));
            zos.closeEntry();
        }

        return baos.toByteArray();
    }

    /**
     * Create export metadata.
     */
    private byte[] createMetadata(List<SystemLogDTO> logs, LogExportRequest request) throws Exception {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("exportTime", LocalDateTime.now().toString());
        metadata.put("totalLogs", logs.size());
        metadata.put("format", request.getFormat());
        metadata.put("exportOptions", request);

        // Calculate statistics
        Map<String, Long> levelCounts = new HashMap<>();
        for(SystemLogDTO log : logs) {
            levelCounts.merge(log.getLevel(), 1L, Long::sum);
        }
        metadata.put("levelCounts", levelCounts);

        if(!logs.isEmpty()) {
            metadata.put("startTime", logs.get(0).getTimestamp().toString());
            metadata.put("endTime", logs.get(logs.size() - 1).getTimestamp().toString());
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(metadata);
    }

    /**
     * Escape CSV value.
     */
    private String escapeCSV(String value) {
        if(value == null) {
            return "";
        }

        if(value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Escape HTML.
     */
    private String escapeHtml(String value) {
        if(value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    /**
     * Add XML element.
     */
    private void addXmlElement(Document doc, Element parent, String name, String value) {
        if(value != null) {
            Element element = doc.createElement(name);
            element.setTextContent(value);
            parent.appendChild(element);
        }
    }

    /**
     * Create Excel cell.
     */
    private void createCell(Row row, int column, String value) {
        createCell(row, column, value, null);
    }

    /**
     * Create Excel cell with style.
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        if(style != null) {
            cell.setCellStyle(style);
        }
    }
}
