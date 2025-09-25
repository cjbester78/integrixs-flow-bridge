package com.integrixs.backend.controller;

import com.integrixs.backend.service.ProcessEngineService;
import com.integrixs.backend.service.ProcessEngineService.*;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/process - engine")
public class ProcessEngineController {

    @Autowired
    private ProcessEngineService processEngineService;

    @Autowired
    private IntegrationFlowSqlRepository integrationFlowRepository;

    /**
     * Deploy a process from an integration flow
     */
    @PostMapping("/deploy/ {flowId}")
    public ResponseEntity<?> deployProcess(@PathVariable String flowId) {
        try {
            Optional<IntegrationFlow> flowOpt = integrationFlowRepository.findById(UUID.fromString(flowId));
            if(!flowOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            IntegrationFlow flow = flowOpt.get();
            ProcessDeploymentResult result = processEngineService.deployProcess(flow);

            if(result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "processDefinition", Map.of(
                        "id", result.getProcessDefinition().getId(),
                        "name", result.getProcessDefinition().getName(),
                        "version", result.getProcessDefinition().getVersion(),
                        "deploymentTime", result.getProcessDefinition().getDeploymentTime().toString()
                   )
               ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getError()
               ));
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Start a new process instance
     */
    @PostMapping("/start/ {processDefinitionId}")
    public ResponseEntity<?> startProcess(
            @PathVariable String processDefinitionId,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            if(variables == null) {
                variables = Map.of();
            }

            ProcessInstanceResult result = processEngineService.startProcess(processDefinitionId, variables);

            if(result.isSuccess()) {
                ProcessInstance instance = result.getProcessInstance();
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "instance", Map.of(
                        "id", instance.getId(),
                        "processDefinitionId", instance.getProcessDefinitionId(),
                        "status", instance.getStatus().toString(),
                        "startTime", instance.getStartTime().toString()
                   )
               ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.getError()
               ));
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get process instance status
     */
    @GetMapping("/instance/ {instanceId}")
    public ResponseEntity<?> getProcessInstance(@PathVariable String instanceId) {
        try {
            Optional<ProcessInstance> instanceOpt = processEngineService.getProcessInstance(instanceId);

            if(instanceOpt.isPresent()) {
                ProcessInstance instance = instanceOpt.get();
                return ResponseEntity.ok(Map.of(
                    "id", instance.getId(),
                    "processDefinitionId", instance.getProcessDefinitionId(),
                    "status", instance.getStatus().toString(),
                    "startTime", instance.getStartTime().toString(),
                    "endTime", instance.getEndTime() != null ? instance.getEndTime().toString() : null,
                    "currentStep", instance.getCurrentStep(),
                    "error", instance.getError(),
                    "variables", instance.getVariables(),
                    "executionLog", instance.getExecutionLog(),
                    "userTaskIds", instance.getUserTaskIds()
               ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List active process instances
     */
    @GetMapping("/instances")
    public ResponseEntity<?> listActiveProcesses() {
        try {
            List<ProcessInstance> instances = processEngineService.listActiveProcesses();

            List<Map<String, Object>> response = instances.stream()
                .map(instance -> Map.<String, Object>of(
                    "id", instance.getId(),
                    "processDefinitionId", instance.getProcessDefinitionId(),
                    "status", instance.getStatus().toString(),
                    "startTime", instance.getStartTime().toString(),
                    "currentStep", instance.getCurrentStep() != null ? instance.getCurrentStep() : ""
               ))
                .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(response);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Suspend a process instance
     */
    @PostMapping("/instance/ {instanceId}/suspend")
    public ResponseEntity<?> suspendProcess(@PathVariable String instanceId) {
        try {
            boolean suspended = processEngineService.suspendProcess(instanceId);

            if(suspended) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Process suspended successfully"
               ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Process not found or not in running state"
               ));
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resume a process instance
     */
    @PostMapping("/instance/ {instanceId}/resume")
    public ResponseEntity<?> resumeProcess(@PathVariable String instanceId) {
        try {
            boolean resumed = processEngineService.resumeProcess(instanceId);

            if(resumed) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Process resumed successfully"
               ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Process not found or not in suspended state"
               ));
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Terminate a process instance
     */
    @PostMapping("/instance/ {instanceId}/terminate")
    public ResponseEntity<?> terminateProcess(@PathVariable String instanceId) {
        try {
            boolean terminated = processEngineService.terminateProcess(instanceId);

            if(terminated) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Process terminated successfully"
               ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Process not found"
               ));
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Complete a user task
     */
    @PostMapping("/task/ {taskId}/complete")
    public ResponseEntity<?> completeUserTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> variables) {
        try {
            boolean completed = processEngineService.completeUserTask(taskId, variables);

            if(completed) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Task completed successfully"
               ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Task not found or already completed"
               ));
            }
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get process statistics
     */
    @GetMapping("/stats/ {processDefinitionId}")
    public ResponseEntity<?> getProcessStats(@PathVariable String processDefinitionId) {
        try {
            List<ProcessInstance> allInstances = processEngineService.listActiveProcesses();

            // Calculate statistics
            long totalInstances = allInstances.stream()
                .filter(i -> i.getProcessDefinitionId().equals(processDefinitionId))
                .count();

            long runningInstances = allInstances.stream()
                .filter(i -> i.getProcessDefinitionId().equals(processDefinitionId))
                .filter(i -> i.getStatus() == ProcessStatus.RUNNING)
                .count();

            long completedInstances = allInstances.stream()
                .filter(i -> i.getProcessDefinitionId().equals(processDefinitionId))
                .filter(i -> i.getStatus() == ProcessStatus.COMPLETED)
                .count();

            long failedInstances = allInstances.stream()
                .filter(i -> i.getProcessDefinitionId().equals(processDefinitionId))
                .filter(i -> i.getStatus() == ProcessStatus.FAILED)
                .count();

            return ResponseEntity.ok(Map.of(
                "processDefinitionId", processDefinitionId,
                "totalInstances", totalInstances,
                "runningInstances", runningInstances,
                "completedInstances", completedInstances,
                "failedInstances", failedInstances,
                "successRate", totalInstances > 0 ?
                    (double) completedInstances / totalInstances * 100 : 0
           ));
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
