package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.MessageQueryRequest;
import com.integrixs.backend.api.dto.request.QueueMessageRequest;
import com.integrixs.backend.api.dto.response.MessageResponse;
import com.integrixs.backend.api.dto.response.MessageStatsResponse;
import com.integrixs.backend.api.dto.response.PagedMessageResponse;
import com.integrixs.backend.application.service.MessageQueryService;
import com.integrixs.backend.application.service.MessageQueueManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for message operations
 */
@RestController
@RequestMapping("/api/messages")
@Validated
@Tag(name = "Messages", description = "Message queue and processing operations")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);


    private final MessageQueryService queryService;
    private final MessageQueueManagementService queueService;

    public MessageController(MessageQueryService queryService,
                           MessageQueueManagementService queueService) {
        this.queryService = queryService;
        this.queueService = queueService;
    }

    @GetMapping
    @Operation(summary = "Query messages", description = "Query messages with filtering, sorting and pagination")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<PagedMessageResponse> getMessages(@Valid MessageQueryRequest request) {
        log.debug("Querying messages with request: {}", request);
        PagedMessageResponse response = queryService.getMessages(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ {id}")
    @Operation(summary = "Get message by ID", description = "Retrieve a specific message by its ID")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<MessageResponse> getMessageById(
            @Parameter(description = "Message ID") @PathVariable String id) {
        log.debug("Getting message by ID: {}", id);
        try {
            MessageResponse response = queryService.getMessageById(id);
            return ResponseEntity.ok(response);
        } catch(RuntimeException e) {
            log.error("Message not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent messages", description = "Retrieve recent messages optionally filtered by component")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<List<MessageResponse>> getRecentMessages(
            @Parameter(description = "Business component ID") @RequestParam(required = false) String componentId,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "20") int limit) {
        log.debug("Getting recent messages for component: {}, limit: {}", componentId, limit);
        List<MessageResponse> messages = queryService.getRecentMessages(componentId, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get message statistics", description = "Calculate message statistics based on filters")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<MessageStatsResponse> getMessageStats(@Valid MessageQueryRequest request) {
        log.debug("Calculating message statistics with request: {}", request);
        MessageStatsResponse stats = queryService.getMessageStats(request);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/queue")
    @Operation(summary = "Queue a message", description = "Add a new message to the processing queue")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<MessageResponse> queueMessage(@Valid @RequestBody QueueMessageRequest request) {
        log.debug("Queueing message for flow: {}", request.getFlowId());
        try {
            MessageResponse response = queueService.queueMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch(IllegalArgumentException e) {
            log.error("Invalid queue request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/ {id}/process")
    @Operation(summary = "Process a message", description = "Manually trigger processing of a specific message")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    public ResponseEntity<MessageResponse> processMessage(
            @Parameter(description = "Message ID") @PathVariable String id) {
        log.debug("Processing message: {}", id);
        try {
            MessageResponse response = queueService.processMessage(id);
            return ResponseEntity.ok(response);
        } catch(IllegalArgumentException e) {
            log.error("Message not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch(IllegalStateException e) {
            log.error("Message cannot be processed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/ {id}/retry")
    @Operation(summary = "Retry a failed message", description = "Queue a failed message for retry")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<MessageResponse> retryMessage(
            @Parameter(description = "Message ID") @PathVariable String id) {
        log.debug("Retrying message: {}", id);
        try {
            MessageResponse response = queueService.retryMessage(id);
            return ResponseEntity.ok(response);
        } catch(IllegalArgumentException e) {
            log.error("Message not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch(IllegalStateException e) {
            log.error("Message cannot be retried: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/ {id}")
    @Operation(summary = "Cancel a message", description = "Cancel a pending or processing message")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    public ResponseEntity<Void> cancelMessage(
            @Parameter(description = "Message ID") @PathVariable String id) {
        log.debug("Cancelling message: {}", id);
        try {
            queueService.cancelMessage(id);
            return ResponseEntity.noContent().build();
        } catch(IllegalArgumentException e) {
            log.error("Message not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch(IllegalStateException e) {
            log.error("Message cannot be cancelled: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/queue/status")
    @Operation(summary = "Get queue status", description = "Get current message queue status and counts")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<Map<String, Object>> getQueueStatus() {
        log.debug("Getting queue status");

        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", queueService.getQueueSize());
        status.put("processingCount", queueService.getProcessingCount());
        status.put("failedCount", queueService.getFailedCount());

        List<MessageResponse> pendingMessages = queueService.getPendingMessages(10);
        status.put("nextInQueue", pendingMessages);

        return ResponseEntity.ok(status);
    }

    @PostMapping("/queue/process - next")
    @Operation(summary = "Process next message", description = "Process the next message in the queue")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Void> processNextMessage() {
        log.debug("Processing next message in queue");
        queueService.processNextInQueue();
        return ResponseEntity.accepted().build();
    }
}
