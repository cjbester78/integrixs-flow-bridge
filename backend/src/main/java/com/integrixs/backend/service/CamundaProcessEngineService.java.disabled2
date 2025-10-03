package com.integrixs.backend.service;

import com.integrixs.backend.application.service.OrchestrationTargetService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.OrchestrationTarget;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced Process Engine Service using Camunda BPM
 * This service provides real process engine capabilities for executing BPMN workflows
 */
@Service
@Primary
public class CamundaProcessEngineService extends ProcessEngineService {

    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessEngineService.class);

    @Autowired(required = false)
    private ProcessEngine processEngine;

    @Autowired(required = false)
    private RepositoryService repositoryService;

    @Autowired(required = false)
    private RuntimeService runtimeService;

    @Autowired(required = false)
    private TaskService taskService;

    @Autowired(required = false)
    private HistoryService historyService;

    @Autowired
    private BpmnConverterService bpmnConverterService;

    @Autowired
    private BackendAdapterExecutor adapterExecutionService;

    @Autowired
    private TransformationExecutionService transformationService;

    @Autowired
    private com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository adapterRepository;

    // Cache for process definitions
    private final Map<String, String> processDefinitionCache = new ConcurrentHashMap<>();

    /**
     * Deploy a process definition from an integration flow using Camunda
     */
    @Override
    public ProcessDeploymentResult deployProcess(IntegrationFlow flow) {
        // If Camunda is not available, fall back to parent implementation
        if(processEngine == null || repositoryService == null) {
            logger.warn("Camunda engine not available, using mock implementation");
            return super.deployProcess(flow);
        }

        try {
            logger.info("Deploying process for flow: {}", flow.getName());

            // Convert visual flow to BPMN
            BpmnConverterService.BpmnConversionResult conversionResult =
                bpmnConverterService.convertToBpmn(flow);

            if(!conversionResult.isSuccess()) {
                return ProcessDeploymentResult.error(conversionResult.getError());
            }

            // Enhance BPMN with Camunda - specific elements
            String enhancedBpmn = enhanceBpmnForCamunda(conversionResult.getBpmnXml(), flow);

            // Deploy to Camunda
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                .name(flow.getName())
                .source("IntegrixFlowBridge")
                .tenantId(flow.getTenantId() != null ? flow.getTenantId().toString() : "default");

            // Add BPMN resource
            deploymentBuilder.addInputStream(
                flow.getName() + ".bpmn",
                new ByteArrayInputStream(enhancedBpmn.getBytes())
           );

            // Deploy
            Deployment deployment = deploymentBuilder.deploy();

            // Get deployed process definition
            org.camunda.bpm.engine.repository.ProcessDefinition camundaProcessDef = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

            if(camundaProcessDef == null) {
                return ProcessDeploymentResult.error("Process deployment succeeded but definition not found");
            }

            // Create our process definition wrapper
            ProcessDefinition definition = new ProcessDefinition();
            definition.setId(camundaProcessDef.getId());
            definition.setName(camundaProcessDef.getName());
            definition.setVersion(camundaProcessDef.getVersion());
            definition.setBpmnXml(enhancedBpmn);
            definition.setFlowId(flow.getId());
            definition.setDeploymentTime(LocalDateTime.now());

            // Cache the mapping
            processDefinitionCache.put(flow.getId().toString(), camundaProcessDef.getId());

            logger.info("Successfully deployed process: {} with Camunda ID: {}",
                flow.getName(), camundaProcessDef.getId());

            return ProcessDeploymentResult.success(definition);

        } catch(Exception e) {
            logger.error("Failed to deploy process", e);
            return ProcessDeploymentResult.error("Process deployment failed: " + e.getMessage());
        }
    }

    /**
     * Start a new process instance using Camunda
     */
    @Override
    public ProcessInstanceResult startProcess(String processDefinitionId, Map<String, Object> variables) {
        // If Camunda is not available, fall back to parent implementation
        if(processEngine == null || runtimeService == null) {
            logger.warn("Camunda engine not available, using mock implementation");
            return super.startProcess(processDefinitionId, variables);
        }

        try {
            logger.info("Starting process instance for definition: {}", processDefinitionId);

            // Start process instance in Camunda
            org.camunda.bpm.engine.runtime.ProcessInstance camundaInstance = runtimeService.startProcessInstanceById(
                processDefinitionId,
                variables
           );

            // Create our process instance wrapper
            ProcessInstance instance = new ProcessInstance();
            instance.setId(camundaInstance.getId());
            instance.setProcessDefinitionId(processDefinitionId);
            instance.setStartTime(LocalDateTime.now());
            instance.setStatus(ProcessStatus.RUNNING);
            instance.setVariables(new HashMap<>(variables));

            // Register external task workers for service tasks
            registerExternalTaskWorkers(processDefinitionId);

            logger.info("Successfully started process instance: {}", camundaInstance.getId());

            return ProcessInstanceResult.success(instance);

        } catch(Exception e) {
            logger.error("Failed to start process", e);
            return ProcessInstanceResult.error("Failed to start process: " + e.getMessage());
        }
    }

    /**
     * Get process instance status from Camunda
     */
    @Override
    public Optional<ProcessInstance> getProcessInstance(String instanceId) {
        if(processEngine == null || runtimeService == null) {
            return super.getProcessInstance(instanceId);
        }

        try {
            // Check if process is still active
            org.camunda.bpm.engine.runtime.ProcessInstance camundaInstance =
                runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId)
                    .singleResult();

            if(camundaInstance != null) {
                // Active instance
                ProcessInstance instance = mapFromCamundaInstance(camundaInstance);
                return Optional.of(instance);
            } else {
                // Check history for completed instances
                HistoricProcessInstance historicInstance =
                    historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(instanceId)
                        .singleResult();

                if(historicInstance != null) {
                    ProcessInstance instance = mapFromHistoricInstance(historicInstance);
                    return Optional.of(instance);
                }
            }

            return Optional.empty();

        } catch(Exception e) {
            logger.error("Failed to get process instance", e);
            return Optional.empty();
        }
    }

    /**
     * List active process instances from Camunda
     */
    @Override
    public List<ProcessInstance> listActiveProcesses() {
        if(processEngine == null || runtimeService == null) {
            return super.listActiveProcesses();
        }

        try {
            List<org.camunda.bpm.engine.runtime.ProcessInstance> camundaInstances =
                runtimeService.createProcessInstanceQuery()
                    .active()
                    .list();

            return camundaInstances.stream()
                .map(this::mapFromCamundaInstance)
                .collect(Collectors.toList());

        } catch(Exception e) {
            logger.error("Failed to list active processes", e);
            return new ArrayList<>();
        }
    }

    /**
     * Suspend process instance in Camunda
     */
    @Override
    public boolean suspendProcess(String instanceId) {
        if(processEngine == null || runtimeService == null) {
            return super.suspendProcess(instanceId);
        }

        try {
            runtimeService.suspendProcessInstanceById(instanceId);
            logger.info("Suspended process instance: {}", instanceId);
            return true;
        } catch(Exception e) {
            logger.error("Failed to suspend process", e);
            return false;
        }
    }

    /**
     * Resume process instance in Camunda
     */
    @Override
    public boolean resumeProcess(String instanceId) {
        if(processEngine == null || runtimeService == null) {
            return super.resumeProcess(instanceId);
        }

        try {
            runtimeService.activateProcessInstanceById(instanceId);
            logger.info("Resumed process instance: {}", instanceId);
            return true;
        } catch(Exception e) {
            logger.error("Failed to resume process", e);
            return false;
        }
    }

    /**
     * Terminate process instance in Camunda
     */
    @Override
    public boolean terminateProcess(String instanceId) {
        if(processEngine == null || runtimeService == null) {
            return super.terminateProcess(instanceId);
        }

        try {
            runtimeService.deleteProcessInstance(instanceId, "Terminated by user");
            logger.info("Terminated process instance: {}", instanceId);
            return true;
        } catch(Exception e) {
            logger.error("Failed to terminate process", e);
            return false;
        }
    }

    /**
     * Complete user task in Camunda
     */
    @Override
    public boolean completeUserTask(String taskId, Map<String, Object> variables) {
        if(processEngine == null || taskService == null) {
            return super.completeUserTask(taskId, variables);
        }

        try {
            taskService.complete(taskId, variables);
            logger.info("Completed user task: {}", taskId);
            return true;
        } catch(Exception e) {
            logger.error("Failed to complete user task", e);
            return false;
        }
    }

    /**
     * Get user tasks for a process instance
     */
    public List<UserTask> getUserTasks(String processInstanceId) {
        if(taskService == null) {
            return new ArrayList<>();
        }

        try {
            List<Task> camundaTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();

            return camundaTasks.stream()
                .map(this::mapFromCamundaTask)
                .collect(Collectors.toList());

        } catch(Exception e) {
            logger.error("Failed to get user tasks", e);
            return new ArrayList<>();
        }
    }

    /**
     * Enhance BPMN with Camunda - specific elements
     */
    private String enhanceBpmnForCamunda(String bpmnXml, IntegrationFlow flow) {
        try {
            // Parse BPMN
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(
                new ByteArrayInputStream(bpmnXml.getBytes())
           );

            // Get process element
            org.camunda.bpm.model.bpmn.instance.Process process = modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No process found in BPMN"));

            // Set process as executable
            process.setExecutable(true);

            // Enhance service tasks with Camunda attributes
            Collection<ServiceTask> serviceTasks = modelInstance.getModelElementsByType(ServiceTask.class);
            for(ServiceTask task : serviceTasks) {
                // Set implementation as external task
                task.setCamundaType("external");
                task.setCamundaTopic("integrix - service-" + task.getId());

                // Add retry configuration
                task.setCamundaAsyncBefore(true);
                task.setCamundaAsyncAfter(false);
                task.setCamundaExclusive(true);
            }

            // Enhance user tasks
            Collection<org.camunda.bpm.model.bpmn.instance.UserTask> userTasks = modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.UserTask.class);
            for(org.camunda.bpm.model.bpmn.instance.UserTask task : userTasks) {
                // User tasks configuration can be added here
                // For now, we'll keep them simple
                task.setCamundaFormKey("embedded:app:forms/userTask.html");
                task.setCamundaCandidateGroups("administrators");
            }

            // Convert back to XML
            return Bpmn.convertToString(modelInstance);

        } catch(Exception e) {
            logger.warn("Failed to enhance BPMN for Camunda, using original", e);
            return bpmnXml;
        }
    }

    /**
     * Register external task workers for service tasks
     */
    private void registerExternalTaskWorkers(String processDefinitionId) {
        if(processEngine == null) {
            return;
        }

        try {
            // Get all service tasks from process definition
            BpmnModelInstance model = repositoryService.getBpmnModelInstance(processDefinitionId);
            Collection<ServiceTask> serviceTasks = model.getModelElementsByType(ServiceTask.class);

            for(ServiceTask task : serviceTasks) {
                String topic = task.getCamundaTopic();
                if(topic != null && topic.startsWith("integrix - service-")) {
                    // Register worker for this topic
                    registerServiceTaskWorker(topic, task);
                }
            }
        } catch(Exception e) {
            logger.error("Failed to register external task workers", e);
        }
    }

    /**
     * Register a worker for a specific service task
     */
    private void registerServiceTaskWorker(String topic, ServiceTask task) {
        // In a production implementation, this would use Camunda's External Task Client
        // For now, we'll use a simple async executor
        CompletableFuture.runAsync(() -> {
            while(true) {
                try {
                    // Poll for external tasks
                    List<org.camunda.bpm.engine.externaltask.LockedExternalTask> tasks =
                        processEngine.getExternalTaskService()
                            .fetchAndLock(1, "integrix - worker")
                            .topic(topic, 60000L)
                            .execute();

                    for(org.camunda.bpm.engine.externaltask.LockedExternalTask externalTask : tasks) {
                        executeExternalTask(externalTask);
                    }

                    // Wait before next poll
                    Thread.sleep(1000);

                } catch(Exception e) {
                    logger.error("Error in external task worker", e);
                    try {
                        Thread.sleep(5000);
                    } catch(InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    /**
     * Execute an external task
     */
    private void executeExternalTask(org.camunda.bpm.engine.externaltask.LockedExternalTask task) {
        try {
            Map<String, Object> variables = task.getVariables();

            // Execute based on task type
            if(task.getActivityId().contains("Transform")) {
                // Execute transformation
                var result = transformationService.executeTransformation(
                    task.getActivityId(),
                    variables.get("currentData")
               );

                if(result.isSuccess()) {
                    variables.put("currentData", result.getData());
                    processEngine.getExternalTaskService().complete(task.getId(), "integrix - worker", variables);
                } else {
                    processEngine.getExternalTaskService().handleFailure(
                        task.getId(),
                        "integrix - worker",
                        result.getMessage(),
                        3,
                        5000L
                   );
                }
            } else if(task.getActivityId().contains("Adapter")) {
                // Execute adapter call
                String adapterId = (String) variables.get("adapterId");
                if(adapterId != null) {
                    // Load adapter from repository
                    CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                        .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));

                    try {
                        String result = adapterExecutionService.executeAdapter(
                            adapter,
                            String.valueOf(variables.get("currentData")),
                            variables
                       );

                        variables.put("currentData", result);
                        processEngine.getExternalTaskService().complete(task.getId(), "integrix-worker", variables);
                    } catch (Exception e) {
                        processEngine.getExternalTaskService().handleFailure(
                            task.getId(),
                            "integrix-worker",
                            e.getMessage(),
                            3,
                            5000L
                       );
                    }
                }
            } else {
                // Generic service task execution
                processEngine.getExternalTaskService().complete(task.getId(), "integrix - worker", variables);
            }

        } catch(Exception e) {
            logger.error("Failed to execute external task", e);
            processEngine.getExternalTaskService().handleFailure(
                task.getId(),
                "integrix - worker",
                e.getMessage(),
                0,
                0L
           );
        }
    }

    /**
     * Map Camunda process instance to our model
     */
    private ProcessInstance mapFromCamundaInstance(org.camunda.bpm.engine.runtime.ProcessInstance camundaInstance) {
        ProcessInstance instance = new ProcessInstance();
        instance.setId(camundaInstance.getId());
        instance.setProcessDefinitionId(camundaInstance.getProcessDefinitionId());
        instance.setStartTime(LocalDateTime.now()); // Camunda doesn't expose start time on runtime instance
        instance.setStatus(mapCamundaStatus(camundaInstance));

        // Get variables
        if(runtimeService != null) {
            Map<String, Object> variables = runtimeService.getVariables(camundaInstance.getId());
            instance.setVariables(variables);
        }

        return instance;
    }

    /**
     * Map historic process instance to our model
     */
    private ProcessInstance mapFromHistoricInstance(HistoricProcessInstance historicInstance) {
        ProcessInstance instance = new ProcessInstance();
        instance.setId(historicInstance.getId());
        instance.setProcessDefinitionId(historicInstance.getProcessDefinitionId());
        instance.setStartTime(historicInstance.getStartTime().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime());

        if(historicInstance.getEndTime() != null) {
            instance.setEndTime(historicInstance.getEndTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        // Map status based on state
        if(historicInstance.getState() != null) {
            switch(historicInstance.getState()) {
                case "COMPLETED":
                    instance.setStatus(ProcessStatus.COMPLETED);
                    break;
                case "INTERNALLY_TERMINATED":
                case "EXTERNALLY_TERMINATED":
                    instance.setStatus(ProcessStatus.TERMINATED);
                    break;
                default:
                    instance.setStatus(ProcessStatus.FAILED);
            }
        }

        return instance;
    }

    /**
     * Map Camunda task to our model
     */
    private UserTask mapFromCamundaTask(Task camundaTask) {
        UserTask task = new UserTask();
        task.setId(camundaTask.getId());
        task.setName(camundaTask.getName());
        task.setProcessInstanceId(camundaTask.getProcessInstanceId());
        task.setCreatedTime(camundaTask.getCreateTime().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime());
        task.setStatus(TaskStatus.PENDING);

        return task;
    }

    /**
     * Map Camunda instance status to our model
     */
    private ProcessStatus mapCamundaStatus(org.camunda.bpm.engine.runtime.ProcessInstance instance) {
        if(instance.isEnded()) {
            return ProcessStatus.COMPLETED;
        } else if(instance.isSuspended()) {
            return ProcessStatus.SUSPENDED;
        } else {
            return ProcessStatus.RUNNING;
        }
    }
}