package com.integrixs.backend.camunda;

import com.integrixs.backend.service.CamundaProcessEngineService;
import com.integrixs.backend.service.ProcessEngineService.ProcessDeploymentResult;
import com.integrixs.backend.service.ProcessEngineService.ProcessInstanceResult;
import com.integrixs.backend.service.BpmnConverterService;
import com.integrixs.data.model.IntegrationFlow;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Camunda process engine
 * This test is disabled by default as it requires Camunda to be configured
 */
@SpringBootTest
@ActiveProfiles( {"test", "camunda"})
@EnabledIfSystemProperty(named = "test.camunda.enabled", matches = "true")
public class CamundaIntegrationTest {

    @Autowired(required = false)
    private CamundaProcessEngineService processEngineService;

    @Autowired(required = false)
    private ProcessEngine processEngine;

    @Autowired(required = false)
    private RuntimeService runtimeService;

    @Autowired(required = false)
    private TaskService taskService;

    @Autowired
    private BpmnConverterService bpmnConverterService;

    private IntegrationFlow testFlow;

    @BeforeEach
    public void setUp() {
        // Skip if Camunda not available
        if(processEngine == null) {
            return;
        }

        // Create test flow
        testFlow = new IntegrationFlow();
        testFlow.setId(UUID.randomUUID());
        testFlow.setName("Test Integration Flow");
        testFlow.setTenantId(UUID.randomUUID());
        testFlow.setActive(true);

        // Add flow configuration
        // Configuration field was removed from IntegrationFlow model
        // Map<String, Object> config = new HashMap<>();
        // config.put("source", "test - source");
        // config.put("target", "test - target");
        // testFlow.setConfiguration(config);
    }

    @Test
    public void testProcessDeployment() {
        // Skip if Camunda not available
        if(processEngine == null) {
            System.out.println("Camunda not configured, skipping test");
            return;
        }

        // Deploy process
        ProcessDeploymentResult result = processEngineService.deployProcess(testFlow);

        // Verify deployment
        assertTrue(result.isSuccess(), "Process deployment should succeed");
        assertNotNull(result.getProcessDefinition(), "Process definition should be created");
        assertNotNull(result.getProcessDefinition().getId(), "Process definition should have ID");
        assertEquals(testFlow.getId(), result.getProcessDefinition().getFlowId(),
            "Process definition should reference flow");
    }

    @Test
    public void testProcessExecution() {
        // Skip if Camunda not available
        if(processEngine == null) {
            System.out.println("Camunda not configured, skipping test");
            return;
        }

        // Deploy process first
        ProcessDeploymentResult deployResult = processEngineService.deployProcess(testFlow);
        assertTrue(deployResult.isSuccess(), "Process deployment should succeed");

        // Start process instance
        Map<String, Object> variables = new HashMap<>();
        variables.put("testData", "Hello Camunda");
        variables.put("flowId", testFlow.getId().toString());

        ProcessInstanceResult startResult = processEngineService.startProcess(
            deployResult.getProcessDefinition().getId(),
            variables
       );

        // Verify process started
        assertTrue(startResult.isSuccess(), "Process should start successfully");
        assertNotNull(startResult.getProcessInstance(), "Process instance should be created");
        assertNotNull(startResult.getProcessInstance().getId(), "Process instance should have ID");

        // Verify process is running in Camunda
        ProcessInstance camundaInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(startResult.getProcessInstance().getId())
            .singleResult();

        assertNotNull(camundaInstance, "Process should be running in Camunda");
        assertFalse(camundaInstance.isEnded(), "Process should not be ended");
    }

    @Test
    public void testUserTaskCompletion() {
        // Skip if Camunda not available
        if(processEngine == null) {
            System.out.println("Camunda not configured, skipping test");
            return;
        }

        // Deploy a process with user task
        String bpmn = createBpmnWithUserTask();
        org.camunda.bpm.engine.repository.Deployment deployment =
            processEngine.getRepositoryService()
                .createDeployment()
                .name("Test Process with User Task")
                .addString("test - user - task.bpmn", bpmn)
                .deploy();

        // Start process
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("TestUserTaskProcess");

        // Find user task
        List<Task> tasks = taskService.createTaskQuery()
            .processInstanceId(instance.getId())
            .list();

        assertEquals(1, tasks.size(), "Should have one user task");
        Task userTask = tasks.get(0);

        // Complete task via service
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("approved", true);

        boolean completed = processEngineService.completeUserTask(userTask.getId(), taskVariables);
        assertTrue(completed, "Task should be completed");

        // Verify task is completed
        tasks = taskService.createTaskQuery()
            .processInstanceId(instance.getId())
            .list();
        assertEquals(0, tasks.size(), "No tasks should remain");

        // Clean up
        processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }

    @Test
    public void testProcessSuspendAndResume() {
        // Skip if Camunda not available
        if(processEngine == null) {
            System.out.println("Camunda not configured, skipping test");
            return;
        }

        // Deploy and start process
        ProcessDeploymentResult deployResult = processEngineService.deployProcess(testFlow);
        ProcessInstanceResult startResult = processEngineService.startProcess(
            deployResult.getProcessDefinition().getId(),
            new HashMap<>()
       );

        String instanceId = startResult.getProcessInstance().getId();

        // Suspend process
        boolean suspended = processEngineService.suspendProcess(instanceId);
        assertTrue(suspended, "Process should be suspended");

        // Verify in Camunda
        ProcessInstance camundaInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(instanceId)
            .singleResult();
        assertTrue(camundaInstance.isSuspended(), "Process should be suspended in Camunda");

        // Resume process
        boolean resumed = processEngineService.resumeProcess(instanceId);
        assertTrue(resumed, "Process should be resumed");

        // Verify in Camunda
        camundaInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(instanceId)
            .singleResult();
        assertFalse(camundaInstance.isSuspended(), "Process should not be suspended in Camunda");
    }

    @Test
    public void testProcessTermination() {
        // Skip if Camunda not available
        if(processEngine == null) {
            System.out.println("Camunda not configured, skipping test");
            return;
        }

        // Deploy and start process
        ProcessDeploymentResult deployResult = processEngineService.deployProcess(testFlow);
        ProcessInstanceResult startResult = processEngineService.startProcess(
            deployResult.getProcessDefinition().getId(),
            new HashMap<>()
       );

        String instanceId = startResult.getProcessInstance().getId();

        // Terminate process
        boolean terminated = processEngineService.terminateProcess(instanceId);
        assertTrue(terminated, "Process should be terminated");

        // Verify in Camunda
        ProcessInstance camundaInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(instanceId)
            .singleResult();
        assertNull(camundaInstance, "Process should not exist after termination");
    }

    /**
     * Create a simple BPMN with user task for testing
     */
    private String createBpmnWithUserTask() {
        return "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
            "<bpmn:definitions xmlns:bpmn = \"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n" +
            "                  xmlns:camunda = \"http://camunda.org/schema/1.0/bpmn\"\n" +
            "                  targetNamespace = \"http://integrix.com/test\">\n" +
            " <bpmn:process id = \"TestUserTaskProcess\" name = \"Test User Task Process\" isExecutable = \"true\">\n" +
            "    <bpmn:startEvent id = \"StartEvent_1\">\n" +
            "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
            "    </bpmn:startEvent>\n" +
            "    <bpmn:userTask id = \"UserTask_1\" name = \"Approve Request\" camunda:formKey = \"embedded:app:forms/approval.html\">\n" +
            "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
            "      <bpmn:outgoing>Flow_2</bpmn:outgoing>\n" +
            "    </bpmn:userTask>\n" +
            "    <bpmn:endEvent id = \"EndEvent_1\">\n" +
            "      <bpmn:incoming>Flow_2</bpmn:incoming>\n" +
            "    </bpmn:endEvent>\n" +
            "    <bpmn:sequenceFlow id = \"Flow_1\" sourceRef = \"StartEvent_1\" targetRef = \"UserTask_1\" />\n" +
            "    <bpmn:sequenceFlow id = \"Flow_2\" sourceRef = \"UserTask_1\" targetRef = \"EndEvent_1\" />\n" +
            " </bpmn:process>\n" +
            "</bpmn:definitions>";
    }
}
