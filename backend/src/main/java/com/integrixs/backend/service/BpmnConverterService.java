package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.application.service.OrchestrationTargetService;
import com.integrixs.backend.api.dto.response.OrchestrationTargetResponse;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.OrchestrationTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BpmnConverterService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrchestrationTargetService orchestrationTargetService;

    /**
     * Convert visual orchestration flow to BPMN 2.0 XML
     */
    public BpmnConversionResult convertToBpmn(IntegrationFlow flow) {
        try {
            // Parse visual flow JSON
            JsonNode visualFlow = objectMapper.readTree(flow.getDeploymentMetadata());

            // Create BPMN document
            Document bpmnDoc = createBpmnDocument();
            Element definitions = bpmnDoc.getDocumentElement();

            // Create process element
            Element process = createProcessElement(bpmnDoc, flow);
            definitions.appendChild(process);

            // Convert nodes and edges
            Map<String, String> nodeMapping = new HashMap<>();
            List<JsonNode> nodes = new ArrayList<>();
            List<JsonNode> edges = new ArrayList<>();

            if(visualFlow.has("nodes")) {
                visualFlow.get("nodes").forEach(nodes::add);
            }
            if(visualFlow.has("edges")) {
                visualFlow.get("edges").forEach(edges::add);
            }

            // Process nodes
            for(JsonNode node : nodes) {
                Element bpmnElement = convertNodeToBpmn(bpmnDoc, node, flow);
                if(bpmnElement != null) {
                    process.appendChild(bpmnElement);
                    nodeMapping.put(node.get("id").asText(), bpmnElement.getAttribute("id"));
                }
            }

            // Process edges(sequence flows)
            for(JsonNode edge : edges) {
                Element sequenceFlow = createSequenceFlow(bpmnDoc, edge, nodeMapping);
                if(sequenceFlow != null) {
                    process.appendChild(sequenceFlow);
                }
            }

            // Add orchestration targets as service tasks if not already in visual flow
            addOrchestrationTargets(bpmnDoc, process, flow, nodeMapping);

            // Convert to XML string
            String bpmnXml = documentToString(bpmnDoc);

            return BpmnConversionResult.success(bpmnXml, nodeMapping);

        } catch(Exception e) {
            return BpmnConversionResult.error("BPMN conversion failed: " + e.getMessage());
        }
    }

    /**
     * Validate BPMN XML
     */
    public BpmnValidationResult validateBpmn(String bpmnXml) {
        BpmnValidationResult result = new BpmnValidationResult();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(bpmnXml.getBytes()));

            // Validate structure
            Element root = doc.getDocumentElement();
            if(!root.getLocalName().equals("definitions")) {
                result.addError("Root element must be 'definitions'");
            }

            // Check for process element
            var processes = root.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "process");
            if(processes.getLength() == 0) {
                result.addError("No process element found");
            }

            // Validate each process
            for(int i = 0; i < processes.getLength(); i++) {
                Element process = (Element) processes.item(i);
                validateProcess(process, result);
            }

        } catch(Exception e) {
            result.addError("BPMN validation failed: " + e.getMessage());
        }

        return result;
    }

    private Document createBpmnDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Create root definitions element
        Element definitions = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:definitions");
        definitions.setAttribute("xmlns:bpmn", "http://www.omg.org/spec/BPMN/20100524/MODEL");
        definitions.setAttribute("xmlns:bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
        definitions.setAttribute("xmlns:dc", "http://www.omg.org/spec/DD/20100524/DC");
        definitions.setAttribute("xmlns:di", "http://www.omg.org/spec/DD/20100524/DI");
        definitions.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema - instance");
        definitions.setAttribute("targetNamespace", "http://integrixs.com/bpmn");

        doc.appendChild(definitions);
        return doc;
    }

    private Element createProcessElement(Document doc, IntegrationFlow flow) {
        Element process = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:process");
        process.setAttribute("id", "Process_" + flow.getId());
        process.setAttribute("name", flow.getName());
        process.setAttribute("isExecutable", "true");
        return process;
    }

    private Element convertNodeToBpmn(Document doc, JsonNode node, IntegrationFlow flow) {
        String type = node.get("type").asText();
        String id = node.get("id").asText();
        JsonNode data = node.get("data");

        Element element = null;

        switch(type) {
            case "startEvent":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:startEvent");
                element.setAttribute("id", "StartEvent_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Start");
                break;

            case "endEvent":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:endEvent");
                element.setAttribute("id", "EndEvent_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "End");
                break;

            case "serviceTask":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:serviceTask");
                element.setAttribute("id", "ServiceTask_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Service Task");

                // Add implementation details
                if(data.has("implementation")) {
                    element.setAttribute("implementation", data.get("implementation").asText());
                }
                break;

            case "userTask":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:userTask");
                element.setAttribute("id", "UserTask_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "User Task");
                break;

            case "scriptTask":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:scriptTask");
                element.setAttribute("id", "ScriptTask_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Script Task");

                if(data.has("scriptLanguage")) {
                    element.setAttribute("scriptFormat", data.get("scriptLanguage").asText());
                }

                if(data.has("script")) {
                    Element script = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:script");
                    script.setTextContent(data.get("script").asText());
                    element.appendChild(script);
                }
                break;

            case "exclusiveGateway":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:exclusiveGateway");
                element.setAttribute("id", "ExclusiveGateway_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Exclusive Gateway");
                break;

            case "parallelGateway":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:parallelGateway");
                element.setAttribute("id", "ParallelGateway_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Parallel Gateway");
                break;

            case "inclusiveGateway":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:inclusiveGateway");
                element.setAttribute("id", "InclusiveGateway_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Inclusive Gateway");
                break;

            case "timerEvent":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:intermediateCatchEvent");
                element.setAttribute("id", "TimerEvent_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Timer");

                Element timerDef = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:timerEventDefinition");
                element.appendChild(timerDef);
                break;

            case "messageEvent":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:intermediateCatchEvent");
                element.setAttribute("id", "MessageEvent_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Message");

                Element messageDef = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:messageEventDefinition");
                element.appendChild(messageDef);
                break;

            case "errorEvent":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:boundaryEvent");
                element.setAttribute("id", "ErrorEvent_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : "Error");

                Element errorDef = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:errorEventDefinition");
                element.appendChild(errorDef);
                break;

            // Transformation nodes
            case "transformation":
            case "csvToXml":
            case "xmlToCsv":
            case "jsonToXml":
            case "xmlToJson":
            case "encrypt":
            case "decrypt":
            case "validate":
            case "enrich":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:serviceTask");
                element.setAttribute("id", "Transform_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : type);
                element.setAttribute("implementation", "##WebService");

                // Add extension elements for transformation details
                Element extensionElements = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:extensionElements");
                Element transformConfig = doc.createElement("integrixs:transformConfig");
                transformConfig.setAttribute("type", type);
                if(data.has("config")) {
                    transformConfig.setTextContent(data.get("config").toString());
                }
                extensionElements.appendChild(transformConfig);
                element.appendChild(extensionElements);
                break;

            // Routing nodes
            case "contentRouter":
            case "recipientList":
            case "splitter":
            case "aggregator":
            case "resequencer":
                element = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:serviceTask");
                element.setAttribute("id", "Router_" + id);
                element.setAttribute("name", data.has("label") ? data.get("label").asText() : type);
                element.setAttribute("implementation", "##WebService");

                // Add routing configuration
                Element routingExt = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:extensionElements");
                Element routingConfig = doc.createElement("integrixs:routingConfig");
                routingConfig.setAttribute("type", type);
                routingExt.appendChild(routingConfig);
                element.appendChild(routingExt);
                break;
        }

        return element;
    }

    private Element createSequenceFlow(Document doc, JsonNode edge, Map<String, String> nodeMapping) {
        String sourceId = edge.get("source").asText();
        String targetId = edge.get("target").asText();

        String bpmnSourceId = nodeMapping.get(sourceId);
        String bpmnTargetId = nodeMapping.get(targetId);

        if(bpmnSourceId == null || bpmnTargetId == null) {
            return null;
        }

        Element sequenceFlow = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:sequenceFlow");
        sequenceFlow.setAttribute("id", "Flow_" + edge.get("id").asText());
        sequenceFlow.setAttribute("sourceRef", bpmnSourceId);
        sequenceFlow.setAttribute("targetRef", bpmnTargetId);

        // Add condition expression if present
        if(edge.has("data") && edge.get("data").has("condition")) {
            Element conditionExpression = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:conditionExpression");
            conditionExpression.setAttribute("xsi:type", "bpmn:tFormalExpression");
            conditionExpression.setTextContent(edge.get("data").get("condition").asText());
            sequenceFlow.appendChild(conditionExpression);
        }

        return sequenceFlow;
    }

    private void addOrchestrationTargets(Document doc, Element process, IntegrationFlow flow, Map<String, String> nodeMapping) {
        List<OrchestrationTargetResponse> targets = orchestrationTargetService.getFlowTargets(flow.getId().toString());

        for(OrchestrationTargetResponse target : targets) {
            String targetId = "Target_" + target.getId();

            // Skip if already in visual flow
            if(nodeMapping.containsValue(targetId)) {
                continue;
            }

            // Create service task for target
            Element serviceTask = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:serviceTask");
            serviceTask.setAttribute("id", targetId);
            serviceTask.setAttribute("name", target.getTargetAdapter() != null ? target.getTargetAdapter().getName() : "Target");
            serviceTask.setAttribute("implementation", "##WebService");

            // Add extension elements with target details
            Element extensionElements = doc.createElementNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:extensionElements");
            Element targetConfig = doc.createElement("integrixs:targetConfig");
            targetConfig.setAttribute("adapterId", target.getTargetAdapter() != null ? target.getTargetAdapter().getId() : "");
            targetConfig.setAttribute("executionOrder", String.valueOf(target.getExecutionOrder()));
            targetConfig.setAttribute("parallel", String.valueOf(target.isParallel()));

            if(target.getRoutingCondition() != null) {
                Element condition = doc.createElement("integrixs:routingCondition");
                condition.setTextContent(target.getRoutingCondition());
                targetConfig.appendChild(condition);
            }

            extensionElements.appendChild(targetConfig);
            serviceTask.appendChild(extensionElements);
            process.appendChild(serviceTask);
        }
    }

    private void validateProcess(Element process, BpmnValidationResult result) {
        String processId = process.getAttribute("id");

        // Check for start events
        var startEvents = process.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "startEvent");
        if(startEvents.getLength() == 0) {
            result.addWarning("Process '" + processId + "' has no start event");
        }

        // Check for end events
        var endEvents = process.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "endEvent");
        if(endEvents.getLength() == 0) {
            result.addWarning("Process '" + processId + "' has no end event");
        }

        // Validate sequence flows
        var sequenceFlows = process.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "sequenceFlow");
        Set<String> sourceRefs = new HashSet<>();
        Set<String> targetRefs = new HashSet<>();

        for(int i = 0; i < sequenceFlows.getLength(); i++) {
            Element flow = (Element) sequenceFlows.item(i);
            sourceRefs.add(flow.getAttribute("sourceRef"));
            targetRefs.add(flow.getAttribute("targetRef"));
        }

        // Check for disconnected elements
        var allElements = process.getChildNodes();
        for(int i = 0; i < allElements.getLength(); i++) {
            if(allElements.item(i) instanceof Element) {
                Element elem = (Element) allElements.item(i);
                String elemId = elem.getAttribute("id");

                if(!elemId.isEmpty() && !elem.getLocalName().equals("sequenceFlow")) {
                    boolean isStartEvent = elem.getLocalName().equals("startEvent");
                    boolean isEndEvent = elem.getLocalName().equals("endEvent");

                    if(!isStartEvent && !targetRefs.contains(elemId)) {
                        result.addWarning("Element '" + elemId + "' has no incoming flow");
                    }

                    if(!isEndEvent && !sourceRefs.contains(elemId)) {
                        result.addWarning("Element '" + elemId + "' has no outgoing flow");
                    }
                }
            }
        }
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(" {http://xml.apache.org/xslt}indent - amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    // Result classes
    public static class BpmnConversionResult {
        private boolean success;
        private String bpmnXml;
        private String error;
        private Map<String, String> nodeMapping;

        public static BpmnConversionResult success(String bpmnXml, Map<String, String> nodeMapping) {
            BpmnConversionResult result = new BpmnConversionResult();
            result.success = true;
            result.bpmnXml = bpmnXml;
            result.nodeMapping = nodeMapping;
            return result;
        }

        public static BpmnConversionResult error(String error) {
            BpmnConversionResult result = new BpmnConversionResult();
            result.success = false;
            result.error = error;
            return result;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getBpmnXml() { return bpmnXml; }
        public void setBpmnXml(String bpmnXml) { this.bpmnXml = bpmnXml; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public Map<String, String> getNodeMapping() { return nodeMapping; }
        public void setNodeMapping(Map<String, String> nodeMapping) { this.nodeMapping = nodeMapping; }
    }

    public static class BpmnValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public void addError(String error) {
            errors.add(error);
            valid = false;
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
}
