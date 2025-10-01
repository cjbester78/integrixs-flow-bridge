package com.integrixs.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.jobs.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for job progress updates
 */
@Component
public class JobProgressWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(JobProgressWebSocketHandler.class);

    @Autowired
    private ObjectMapper objectMapper;

    // Map of session ID to WebSocket session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Map of job ID to interested sessions
    private final Map<UUID, Set<String>> jobSubscriptions = new ConcurrentHashMap<>();

    // Map of user ID to sessions
    private final Map<UUID, Set<String>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);

        // Send welcome message
        sendMessage(session, new Message("connected", Map.of("sessionId", session.getId())));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) throws Exception {
        if(webSocketMessage instanceof TextMessage) {
            String payload = ((TextMessage) webSocketMessage).getPayload();
            Message message = objectMapper.readValue(payload, Message.class);

            switch(message.getType()) {
                case "subscribe":
                    handleSubscribe(session, message);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, message);
                    break;
                case "ping":
                    sendMessage(session, new Message("pong", Map.of("timestamp", System.currentTimeMillis())));
                    break;
                default:
                    logger.warn("Unknown message type: {}", message.getType());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}", session.getId(), exception);
    }

    /**
     * Convert from backend JobStatus to websocket JobStatus
     */
    private JobStatus convertJobStatus(com.integrixs.backend.jobs.JobStatus backendStatus) {
        if (backendStatus == null) {
            return JobStatus.PENDING;
        }

        switch (backendStatus) {
            case PENDING:
                return JobStatus.PENDING;
            case RUNNING:
                return JobStatus.RUNNING;
            case COMPLETED:
                return JobStatus.COMPLETED;
            case FAILED:
                return JobStatus.FAILED;
            case CANCELLED:
                return JobStatus.CANCELLED;
            case RETRYING:
                return JobStatus.RETRYING;
            default:
                return JobStatus.PENDING;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("WebSocket connection closed: {} with status {}", session.getId(), closeStatus);

        // Remove session
        sessions.remove(session.getId());

        // Remove from all subscriptions
        jobSubscriptions.values().forEach(sessions -> sessions.remove(session.getId()));
        userSessions.values().forEach(sessions -> sessions.remove(session.getId()));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Send job update to all subscribed sessions
     */
    public void sendJobUpdate(BackgroundJob job) {
        JobUpdateMessage update = new JobUpdateMessage();
        update.setJobId(job.getId());
        update.setStatus(convertJobStatus(job.getStatus()));
        update.setProgress(job.getProgress());
        update.setCurrentStep(job.getCurrentStep());
        update.setErrorMessage(job.getErrorMessage());
        update.setResults(job.getResults());

        Message message = new Message("jobUpdate", update);

        // Send to sessions subscribed to this specific job
        Set<String> subscribedSessions = jobSubscriptions.getOrDefault(job.getId(), Collections.emptySet());
        for(String sessionId : subscribedSessions) {
            WebSocketSession session = sessions.get(sessionId);
            if(session != null && session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch(Exception e) {
                    logger.error("Error sending job update to session {}", sessionId, e);
                }
            }
        }

        // Also send to sessions of the job creator
        if(job.getCreatedBy() != null) {
            Set<String> userSessionIds = userSessions.getOrDefault(job.getCreatedBy(), Collections.emptySet());
            for(String sessionId : userSessionIds) {
                if(!subscribedSessions.contains(sessionId)) { // Avoid duplicates
                    WebSocketSession session = sessions.get(sessionId);
                    if(session != null && session.isOpen()) {
                        try {
                            sendMessage(session, message);
                        } catch(Exception e) {
                            logger.error("Error sending job update to user session {}", sessionId, e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle subscription request
     */
    private void handleSubscribe(WebSocketSession session, Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();

        if(data.containsKey("jobId")) {
            UUID jobId = UUID.fromString(data.get("jobId").toString());
            jobSubscriptions.computeIfAbsent(jobId, k -> ConcurrentHashMap.newKeySet())
                .add(session.getId());
            logger.debug("Session {} subscribed to job {}", session.getId(), jobId);
        }

        if(data.containsKey("userId")) {
            UUID userId = UUID.fromString(data.get("userId").toString());
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(session.getId());
            logger.debug("Session {} subscribed to user {}", session.getId(), userId);
        }

        // Send acknowledgment
        sendMessage(session, new Message("subscribed", data));
    }

    /**
     * Handle unsubscription request
     */
    private void handleUnsubscribe(WebSocketSession session, Message message) {
        Map<String, Object> data = (Map<String, Object>) message.getData();

        if(data.containsKey("jobId")) {
            UUID jobId = UUID.fromString(data.get("jobId").toString());
            Set<String> sessions = jobSubscriptions.get(jobId);
            if(sessions != null) {
                sessions.remove(session.getId());
                if(sessions.isEmpty()) {
                    jobSubscriptions.remove(jobId);
                }
            }
        }

        if(data.containsKey("userId")) {
            UUID userId = UUID.fromString(data.get("userId").toString());
            Set<String> sessions = userSessions.get(userId);
            if(sessions != null) {
                sessions.remove(session.getId());
                if(sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }

        // Send acknowledgment
        sendMessage(session, new Message("unsubscribed", data));
    }

    /**
     * Send message to session
     */
    private void sendMessage(WebSocketSession session, Message message) {
        if(session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch(IOException e) {
                logger.error("Error sending message to session {}", session.getId(), e);
            }
        }
    }

    /**
     * WebSocket message class
     */
    public static class Message {
        private String type;
        private Object data;

        public Message() {}

        public Message(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    /**
     * Job update message
     */
    public static class JobUpdateMessage {
        private UUID jobId;
        private JobStatus status;
        private Integer progress;
        private String currentStep;
        private String errorMessage;
        private Map<String, String> results;

        // Getters and setters
        public UUID getJobId() { return jobId; }
        public void setJobId(UUID jobId) { this.jobId = jobId; }
        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Map<String, String> getResults() { return results; }
        public void setResults(Map<String, String> results) { this.results = results; }
    }
}
