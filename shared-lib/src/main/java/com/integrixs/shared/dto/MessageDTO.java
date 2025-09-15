package com.integrixs.shared.dto;

import com.integrixs.shared.enums.MessageStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFormat;

public class MessageDTO {
    private String id;
    private String correlationId;
    @JsonFormat(pattern = "yyyy - MM - dd HH:mm:ss")
    private LocalDateTime timestamp;
    private Instant messageTimestamp;
    private String source;
    private String target;
    private String type;
    private MessageStatus status;
    private String statusString; // for backward compatibility
    private Map<String, Object> headers;
    private String payload;
    private String processingTime; // in milliseconds, e.g. "250ms"
    private String size; // in bytes, e.g. "1024 bytes"
    private String businessComponentId;
    private List<MessageLogDTO> logs;

    public static class MessageLogDTO {
        @JsonFormat(pattern = "yyyy - MM - dd HH:mm:ss")
        private LocalDateTime timestamp;
        private String level;
        private String message;
        // Constructors
        public MessageLogDTO() {}

        public MessageLogDTO(LocalDateTime timestamp, String level, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
        }

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // Constructors
    public MessageDTO() {}

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Instant getMessageTimestamp() { return messageTimestamp; }
    public void setMessageTimestamp(Instant messageTimestamp) { this.messageTimestamp = messageTimestamp; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public String getStatusString() { return statusString; }
    public void setStatusString(String statusString) { this.statusString = statusString; }

    public Map<String, Object> getHeaders() { return headers; }
    public void setHeaders(Map<String, Object> headers) { this.headers = headers; }

    public String getPayload() { return payload; }

    // Alias for getPayload() for backward compatibility
    public String getContent() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getProcessingTime() { return processingTime; }
    public void setProcessingTime(String processingTime) { this.processingTime = processingTime; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }

    public List<MessageLogDTO> getLogs() { return logs; }
    public void setLogs(List<MessageLogDTO> logs) { this.logs = logs; }
}
