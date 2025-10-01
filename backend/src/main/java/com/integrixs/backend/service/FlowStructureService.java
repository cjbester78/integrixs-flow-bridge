package com.integrixs.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.FlowStructureMessage;
import com.integrixs.data.model.FlowStructureNamespace;
import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.model.MessageStructureNamespace;
import com.integrixs.data.model.User;
import com.integrixs.data.model.FlowStructure.ProcessingMode;
import com.integrixs.data.model.FlowStructureMessage.MessageType;
import com.integrixs.data.sql.repository.BusinessComponentSqlRepository;
import com.integrixs.data.sql.repository.FlowStructureMessageSqlRepository;
import com.integrixs.data.sql.repository.FlowStructureSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.MessageStructureSqlRepository;
import com.integrixs.shared.dto.structure.*;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import java.util.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FlowStructureService {

    private static final Logger log = LoggerFactory.getLogger(FlowStructureService.class);

    private final FlowStructureSqlRepository flowStructureRepository;
    private final MessageStructureSqlRepository messageStructureRepository;
    private final FlowStructureMessageSqlRepository flowStructureMessageRepository;
    private final BusinessComponentSqlRepository businessComponentRepository;
    private final EnvironmentPermissionService environmentPermissionService;
    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
        public FlowStructureService(FlowStructureSqlRepository flowStructureRepository,
                              MessageStructureSqlRepository messageStructureRepository,
                              FlowStructureMessageSqlRepository flowStructureMessageRepository,
                              BusinessComponentSqlRepository businessComponentRepository,
                              EnvironmentPermissionService environmentPermissionService,
                              IntegrationFlowSqlRepository integrationFlowRepository) {
        this.flowStructureRepository = flowStructureRepository;
        this.messageStructureRepository = messageStructureRepository;
        this.flowStructureMessageRepository = flowStructureMessageRepository;
        this.businessComponentRepository = businessComponentRepository;
        this.environmentPermissionService = environmentPermissionService;
        this.integrationFlowRepository = integrationFlowRepository;
            }

    public FlowStructureDTO create(FlowStructureCreateRequestDTO request, User currentUser) {
        log.info("Creating flow structure: {}", request.getName());
        log.info("Current environment: {}, canCreateFlows: {}",
            environmentPermissionService.getEnvironmentInfo().get("type"),
            environmentPermissionService.isActionAllowed("flow.create"));

        // Check environment permissions
        try {
            environmentPermissionService.checkPermission("flow.create");
        } catch(Exception e) {
            log.error("Environment permission check failed: ", e);
            throw e;
        }

        try {
            // Check if name already exists for business component
        if(flowStructureRepository.existsByNameAndBusinessComponentIdAndIsActiveTrue(
                request.getName(), UUID.fromString(request.getBusinessComponentId()))) {
            throw new RuntimeException("Flow structure with name '" + request.getName() +
                    "' already exists for this business component");
        }

        FlowStructure flowStructure = FlowStructure.builder()
                .name(request.getName())
                .description(request.getDescription())
                .processingMode(FlowStructure.ProcessingMode.valueOf(request.getProcessingMode().name()))
                .direction(FlowStructure.Direction.valueOf(request.getDirection().name()))
                // Namespace, metadata, and tags are now handled separately
                .businessComponent(businessComponentRepository.findById(UUID.fromString(request.getBusinessComponentId()))
                        .orElseThrow(() -> new RuntimeException("Business component not found")))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        flowStructure = flowStructureRepository.save(flowStructure);

        // Create flow structure messages
        if(request.getMessageStructureIds() != null) {
            createFlowStructureMessages(flowStructure, request.getMessageStructureIds());
            log.info("Created flow structure messages for flow structure: {}", flowStructure.getId());

            // Clear the persistence context to ensure fresh load
            log.info("Flushed and cleared entity manager");

            // Reload the flow structure with associations
            flowStructure = flowStructureRepository.findById(flowStructure.getId())
                    .orElseThrow(() -> new RuntimeException("Flow structure not found after save"));
            log.info("Reloaded flow structure: {}, associations count: {}",
                flowStructure.getId(),
                flowStructure.getFlowStructureMessages() != null ? flowStructure.getFlowStructureMessages().size() : 0);

            // Initialize associations
            if(flowStructure.getFlowStructureMessages() != null) {
                // Collections are already loaded by SQL repository
                log.info("Initialized flow structure messages, count: {}", flowStructure.getFlowStructureMessages().size());
                for(FlowStructureMessage fsm : flowStructure.getFlowStructureMessages()) {
                    // Message structure already loaded by SQL repository
                    log.info("Initialized message structure: {} for type: {}",
                        fsm.getMessageStructure() != null ? fsm.getMessageStructure().getName() : "null",
                        fsm.getMessageType());
                }
            }
        }

        // Set WSDL content if provided(imported WSDL), otherwise generate
        if(request.getWsdlContent() != null && !request.getWsdlContent().trim().isEmpty()) {
            flowStructure.setWsdlContent(request.getWsdlContent());
            flowStructure.setSourceType("EXTERNAL");
        } else {
            generateWsdl(flowStructure);
            flowStructure.setSourceType("INTERNAL");
        }

        // Save the flow structure with WSDL content
        flowStructure = flowStructureRepository.save(flowStructure);

        return convertToFlowStructureDTO(flowStructure);
        } catch(Exception e) {
            log.error("Error creating flow structure: ", e);
            throw e;
        }
    }

    public FlowStructureDTO update(String id, FlowStructureCreateRequestDTO request, User currentUser) {
        log.info("Updating flow structure: {}", id);

        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));

        // Check if name is being changed and already exists
        if(!flowStructure.getName().equals(request.getName()) &&
                flowStructureRepository.existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(
                        request.getName(), UUID.fromString(request.getBusinessComponentId()), UUID.fromString(id))) {
            throw new RuntimeException("Flow structure with name '" + request.getName() +
                    "' already exists for this business component");
        }

        flowStructure.setName(request.getName());
        flowStructure.setDescription(request.getDescription());
        flowStructure.setProcessingMode(FlowStructure.ProcessingMode.valueOf(request.getProcessingMode().name()));
        flowStructure.setDirection(FlowStructure.Direction.valueOf(request.getDirection().name()));
        // Namespace, metadata, and tags are now handled separately in related tables
        flowStructure.setBusinessComponent(businessComponentRepository.findById(UUID.fromString(request.getBusinessComponentId()))
                .orElseThrow(() -> new RuntimeException("Business component not found")));
        flowStructure.setUpdatedBy(currentUser);
        flowStructure.setVersion(flowStructure.getVersion() + 1);

        // Update flow structure messages
        flowStructureMessageRepository.deleteByFlowStructureId(flowStructure.getId());
        if(request.getMessageStructureIds() != null) {
            createFlowStructureMessages(flowStructure, request.getMessageStructureIds());
            // Clear the persistence context to ensure fresh load
            // Reload the flow structure with associations
            flowStructure = flowStructureRepository.findById(flowStructure.getId())
                    .orElseThrow(() -> new RuntimeException("Flow structure not found after update"));
            // Initialize associations
            if(flowStructure.getFlowStructureMessages() != null) {
                // Collections are already loaded by SQL repository
                for(FlowStructureMessage fsm : flowStructure.getFlowStructureMessages()) {
                    // Message structure already loaded by SQL repository
                }
            }
        }

        // Update WSDL content if provided, otherwise regenerate
        if(request.getWsdlContent() != null && !request.getWsdlContent().trim().isEmpty()) {
            flowStructure.setWsdlContent(request.getWsdlContent());
            flowStructure.setSourceType("EXTERNAL");
        } else {
            generateWsdl(flowStructure);
            // Keep existing source type if updating, otherwise set to INTERNAL
            if(flowStructure.getSourceType() == null) {
                flowStructure.setSourceType("INTERNAL");
            }
        }

        flowStructure = flowStructureRepository.save(flowStructure);
        return convertToFlowStructureDTO(flowStructure);
    }

    public FlowStructureDTO findById(String id) {
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));
        return convertToFlowStructureDTO(flowStructure);
    }

    public Page<FlowStructureDTO> findAll(String businessComponentId,
                                         FlowStructureDTO.ProcessingMode processingMode,
                                         FlowStructureDTO.Direction direction,
                                         String search,
                                         Pageable pageable) {
        String modeString = processingMode != null ? processingMode.name() : null;
        String dirString = direction != null ? direction.name() : null;

        UUID businessComponentUuid = businessComponentId != null ? UUID.fromString(businessComponentId) : null;
        Page<FlowStructure> page = flowStructureRepository.findAllWithFilters(
                businessComponentUuid, modeString, dirString, search, pageable);
        return page.map(this::convertToFlowStructureDTO);
    }

    public List<FlowStructureDTO> findByBusinessComponent(String businessComponentId) {
        return flowStructureRepository.findByBusinessComponentIdAndIsActiveTrueOrderByName(UUID.fromString(businessComponentId))
                .stream()
                .map(this::convertToFlowStructureDTO)
                .collect(Collectors.toList());
    }

    public List<FlowStructureDTO> findByMessageStructure(String messageStructureId) {
        log.info("Finding flow structures using message structure: {}", messageStructureId);
        List<FlowStructure> flowStructures = flowStructureMessageRepository.findFlowStructuresByMessageStructureId(UUID.fromString(messageStructureId));

        return flowStructures.stream()
                .filter(FlowStructure::getIsActive)
                .map(flowStructure -> {
                    FlowStructureDTO dto = convertToFlowStructureDTO(flowStructure);

                    // Add information about which message types use this message structure
                    List<String> messageTypes = flowStructureMessageRepository.findByFlowStructureId(flowStructure.getId())
                            .stream()
                            .filter(fsm -> fsm.getMessageStructure().getId().toString().equals(messageStructureId))
                            .map(fsm -> fsm.getMessageType().toString())
                            .collect(Collectors.toList());

                    // Store message types in metadata for the frontend
                    Map<String, Object> metadata = dto.getMetadata() != null ?
                            new HashMap<>(dto.getMetadata()) : new HashMap<>();
                    metadata.put("messageTypes", messageTypes);
                    dto.setMetadata(metadata);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        log.info("Deleting flow structure: {}", id);
        FlowStructure flowStructure = flowStructureRepository.findByIdAndIsActiveTrue(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));

        // Check if this flow structure is referenced by any integration flows
        UUID flowStructureUuid = UUID.fromString(id);
        long sourceCount = integrationFlowRepository.countBySourceFlowStructureIdAndIsActiveTrue(flowStructureUuid);
        long targetCount = integrationFlowRepository.countByTargetFlowStructureIdAndIsActiveTrue(flowStructureUuid);

        if(sourceCount > 0 || targetCount > 0) {
            String message = "Cannot delete flow structure. It is being used by ";
            if(sourceCount > 0 && targetCount > 0) {
                message += sourceCount + " integration flow(s) as source and " + targetCount + " integration flow(s) as target";
            } else if(sourceCount > 0) {
                message += sourceCount + " integration flow(s) as source";
            } else {
                message += targetCount + " integration flow(s) as target";
            }
            throw new IllegalStateException(message);
        }

        // Perform hard delete - this will cascade delete flow_structure_messages due to CASCADE constraint
        flowStructureRepository.delete(flowStructure);
        // SQL repositories don't have flush() - operations are immediate
    }

    private void createFlowStructureMessages(FlowStructure flowStructure,
                                           Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds) {
        for(Map.Entry<FlowStructureMessageDTO.MessageType, String> entry : messageStructureIds.entrySet()) {
            MessageStructure messageStructure = messageStructureRepository.findById(UUID.fromString(entry.getValue()))
                    .orElseThrow(() -> new RuntimeException("Message structure not found: " + entry.getValue()));

            FlowStructureMessage flowMessage = FlowStructureMessage.builder()
                    .flowStructure(flowStructure)
                    .messageType(FlowStructureMessage.MessageType.valueOf(entry.getKey().name()))
                    .messageStructure(messageStructure)
                    .build();

            flowStructureMessageRepository.save(flowMessage);
        }
    }

    private void generateWsdl(FlowStructure flowStructure) {
        log.info("=== GENERATING WSDL for flow structure: {} ===", flowStructure.getName());

        // Ensure flow structure messages are initialized
        if(flowStructure.getFlowStructureMessages() != null) {
            // Collections are already loaded by SQL repository
            for(FlowStructureMessage fsm : flowStructure.getFlowStructureMessages()) {
                // Message structure already loaded by SQL repository
            }
        }

        String serviceName = flowStructure.getName().replaceAll("[^a - zA - Z0-9]", "");
        String namespace = "http://integrixflowbridge.com/" + serviceName;

        StringBuilder wsdl = new StringBuilder();
        wsdl.append("<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n");
        wsdl.append("<definitions name = \"").append(serviceName).append("Service\"\n");
        wsdl.append("             targetNamespace = \"").append(namespace).append("\"\n");
        wsdl.append("             xmlns = \"http://schemas.xmlsoap.org/wsdl/\"\n");
        wsdl.append("             xmlns:soap = \"http://schemas.xmlsoap.org/wsdl/soap/\"\n");
        wsdl.append("             xmlns:tns = \"").append(namespace).append("\"\n");
        wsdl.append("             xmlns:xsd = \"http://www.w3.org/2001/XMLSchema\">\n");
        wsdl.append("\n");

        // Types section - define message structures based on linked message structures
        wsdl.append(" <types>\n");
        wsdl.append("    <xsd:schema targetNamespace = \"").append(namespace).append("\">\n");

        // Generate element definitions based on message structures
        Set<FlowStructureMessage> messages = flowStructure.getFlowStructureMessages();
        log.info("Flow structure has {} message structure associations", messages != null ? messages.size() : 0);

        if(messages != null && !messages.isEmpty()) {
            boolean hasInlineXsd = false;

            // First, check if all message structures have XSD content for inline inclusion
            for(FlowStructureMessage msg : messages) {
                MessageStructure msgStructure = msg.getMessageStructure();
                log.info("Processing message structure: {} (type: {})",
                    msgStructure != null ? msgStructure.getName() : "null",
                    msg.getMessageType());
                if(msgStructure != null) {
                    String xsdContent = msgStructure.getXsdContent();
                    log.info("Message structure {} - XSD content present: {}, length: {}",
                        msgStructure.getName(),
                        xsdContent != null && !xsdContent.trim().isEmpty(),
                        xsdContent != null ? xsdContent.length() : 0);

                    if(xsdContent != null && !xsdContent.trim().isEmpty()) {
                        hasInlineXsd = true;
                        // Extract and inline the XSD content
                        try {
                            log.info("Processing XSD content for message structure: {} (type: {})", msgStructure.getName(), msg.getMessageType());
                            log.info("XSD content preview: {}", xsdContent.substring(0, Math.min(200, xsdContent.length())));

                        // Parse the XSD to extract element definitions
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document xsdDoc = builder.parse(new ByteArrayInputStream(xsdContent.getBytes(StandardCharsets.UTF_8)));

                        // Get the schema element
                        Element schemaElement = xsdDoc.getDocumentElement();

                        // Get direct child elements of the schema(top - level elements only)
                        NodeList schemaChildren = schemaElement.getChildNodes();

                        wsdl.append("      <!-- ").append(msg.getMessageType()).append(" message from ").append(msgStructure.getName()).append(" -->\n");

                        for(int i = 0; i < schemaChildren.getLength(); i++) {
                            Node child = schemaChildren.item(i);

                            // Only process element nodes that are actual element definitions
                            if(child.getNodeType() == Node.ELEMENT_NODE &&
                                child.getLocalName() != null &&
                                (child.getLocalName().equals("element") ||
                                 child.getLocalName().equals("complexType") ||
                                 child.getLocalName().equals("simpleType"))) {

                                Element elem = (Element) child;

                                // For elements, check if it's a message element(not a type definition element)
                                if(elem.getLocalName().equals("element")) {
                                    String elementName = elem.getAttribute("name");
                                    // Skip schema definition elements
                                    if(elementName != null && !elementName.isEmpty() &&
                                        !elementName.equals("schema") && !elementName.equals("element")) {
                                        wsdl.append(serializeElement(elem, "      "));
                                        wsdl.append("\n");
                                    }
                                } else {
                                    // Include type definitions as - is
                                    wsdl.append(serializeElement(elem, "      "));
                                    wsdl.append("\n");
                                }
                            }
                        }
                    } catch(Exception e) {
                        log.error("Error processing XSD content for message structure: " + msgStructure.getName(), e);
                        // Fall back to anyType if parsing fails
                        String elementName = msgStructure.getName();
                        wsdl.append("      <!-- ").append(msg.getMessageType()).append(" message from ").append(msgStructure.getName()).append(" (parse error) -->\n");
                        wsdl.append("      <xsd:element name = \"").append(elementName).append("\" type = \"xsd:anyType\"/>\n");
                    }
                    } else {
                        // No XSD content, use anyType
                        String elementName = msgStructure.getName();
                        wsdl.append("      <!-- ").append(msg.getMessageType()).append(" message from ").append(msgStructure.getName()).append(" -->\n");
                        wsdl.append("      <xsd:element name = \"").append(elementName).append("\" type = \"xsd:anyType\"/>\n");
                    }
                } else {
                    // Fallback to generic name if no structure defined
                    String elementName = getElementNameForMessageType(msg.getMessageType());
                    wsdl.append("      <xsd:element name = \"").append(elementName).append("\" type = \"xsd:anyType\"/>\n");
                }
            }
        } else {
            // Default elements if no message structures defined
            if(flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
                wsdl.append("      <xsd:element name = \"Request\" type = \"xsd:anyType\"/>\n");
                wsdl.append("      <xsd:element name = \"Response\" type = \"xsd:anyType\"/>\n");
                wsdl.append("      <xsd:element name = \"Fault\" type = \"xsd:anyType\"/>\n");
            } else {
                wsdl.append("      <xsd:element name = \"Message\" type = \"xsd:anyType\"/>\n");
            }
        }

        wsdl.append("    </xsd:schema>\n");
        wsdl.append(" </types>\n");
        wsdl.append("\n");

        // Messages section
        if(messages != null && !messages.isEmpty()) {
            for(FlowStructureMessage msg : messages) {
                String messageName = getMessageNameForType(msg.getMessageType());
                MessageStructure msgStructure = msg.getMessageStructure();
                String elementName = msgStructure != null ? msgStructure.getName() : getElementNameForMessageType(msg.getMessageType());
                wsdl.append(" <message name = \"").append(messageName).append("\">\n");
                wsdl.append("    <part name = \"parameters\" element = \"tns:").append(elementName).append("\"/>\n");
                wsdl.append(" </message>\n");
                wsdl.append("\n");
            }
        } else {
            // Default messages
            if(flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
                wsdl.append(" <message name = \"RequestMessage\">\n");
                wsdl.append("    <part name = \"parameters\" element = \"tns:Request\"/>\n");
                wsdl.append(" </message>\n");
                wsdl.append("\n");
                wsdl.append(" <message name = \"ResponseMessage\">\n");
                wsdl.append("    <part name = \"parameters\" element = \"tns:Response\"/>\n");
                wsdl.append(" </message>\n");
                wsdl.append("\n");
                wsdl.append(" <message name = \"FaultMessage\">\n");
                wsdl.append("    <part name = \"parameters\" element = \"tns:Fault\"/>\n");
                wsdl.append(" </message>\n");
                wsdl.append("\n");
            } else {
                wsdl.append(" <message name = \"Message\">\n");
                wsdl.append("    <part name = \"parameters\" element = \"tns:Message\"/>\n");
                wsdl.append(" </message>\n");
                wsdl.append("\n");
            }
        }

        // PortType section
        wsdl.append(" <portType name = \"").append(serviceName).append("PortType\">\n");
        wsdl.append("    <operation name = \"process\">\n");

        if(flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
            wsdl.append("      <input message = \"tns:RequestMessage\"/>\n");
            wsdl.append("      <output message = \"tns:ResponseMessage\"/>\n");
            wsdl.append("      <fault name = \"fault\" message = \"tns:FaultMessage\"/>\n");
        } else {
            wsdl.append("      <input message = \"tns:Message\"/>\n");
        }

        wsdl.append("    </operation>\n");
        wsdl.append(" </portType>\n");
        wsdl.append("\n");

        // Binding section
        wsdl.append(" <binding name = \"").append(serviceName).append("Binding\" type = \"tns:").append(serviceName).append("PortType\">\n");
        wsdl.append("    <soap:binding style = \"document\" transport = \"http://schemas.xmlsoap.org/soap/http\"/>\n");
        wsdl.append("    <operation name = \"process\">\n");
        wsdl.append("      <soap:operation soapAction = \"process\"/>\n");
        wsdl.append("      <input>\n");
        wsdl.append("        <soap:body use = \"literal\"/>\n");
        wsdl.append("      </input>\n");

        if(flowStructure.getProcessingMode() == ProcessingMode.SYNC) {
            wsdl.append("      <output>\n");
            wsdl.append("        <soap:body use = \"literal\"/>\n");
            wsdl.append("      </output>\n");
            wsdl.append("      <fault name = \"fault\">\n");
            wsdl.append("        <soap:fault name = \"fault\" use = \"literal\"/>\n");
            wsdl.append("      </fault>\n");
        }

        wsdl.append("    </operation>\n");
        wsdl.append(" </binding>\n");
        wsdl.append("\n");

        // Service section
        wsdl.append(" <service name = \"").append(serviceName).append("Service\">\n");
        wsdl.append("    <port name = \"").append(serviceName).append("Port\" binding = \"tns:").append(serviceName).append("Binding\">\n");
        wsdl.append("      <soap:address location = \"\"/>\n"); // Location will be set during deployment
        wsdl.append("    </port>\n");
        wsdl.append(" </service>\n");
        wsdl.append("</definitions>");

        String generatedWsdl = wsdl.toString();
        log.info("=== GENERATED WSDL PREVIEW(first 500 chars) ===");
        log.info(generatedWsdl.substring(0, Math.min(500, generatedWsdl.length())));
        log.info("=== END WSDL GENERATION ===");

        flowStructure.setWsdlContent(generatedWsdl);

        // Note: Operation info is stored in the metadata field as 'operationInfo'
        // See generateOperationMetadata() method for details
    }

    private String getElementNameForMessageType(MessageType type) {
        switch(type) {
            case INPUT:
                return "Request";
            case OUTPUT:
                return "Response";
            case FAULT:
                return "Fault";
            default:
                return "Message";
        }
    }

    private String getMessageNameForType(MessageType type) {
        switch(type) {
            case INPUT:
                return "RequestMessage";
            case OUTPUT:
                return "ResponseMessage";
            case FAULT:
                return "FaultMessage";
            default:
                return "Message";
        }
    }

    public FlowStructureDTO regenerateWsdl(String id) {
        log.info("Regenerating WSDL for flow structure: {}", id);
        FlowStructure flowStructure = flowStructureRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Flow structure not found"));

        if(!"INTERNAL".equals(flowStructure.getSourceType())) {
            throw new RuntimeException("Cannot regenerate WSDL for external flow structures");
        }

        // Initialize associations
        if(flowStructure.getFlowStructureMessages() != null) {
            // Collections are already loaded by SQL repository
            log.info("Loaded {} message associations", flowStructure.getFlowStructureMessages().size());
            for(FlowStructureMessage fsm : flowStructure.getFlowStructureMessages()) {
                // Message structure already loaded by SQL repository
                log.info("Message structure: {} (type: {})",
                    fsm.getMessageStructure().getName(), fsm.getMessageType());
            }
        }

        generateWsdl(flowStructure);
        flowStructure = flowStructureRepository.save(flowStructure);
        log.info("WSDL regenerated successfully for flow structure: {}", flowStructure.getName());

        return convertToFlowStructureDTO(flowStructure);
    }

    public void regenerateWsdlForAll() {
        log.info("Regenerating WSDL for all flow structures");
        List<FlowStructure> flowStructures = flowStructureRepository.findAll();
        for(FlowStructure flowStructure : flowStructures) {
            if(flowStructure.getSourceType() != null && flowStructure.getSourceType().equals("INTERNAL")) {
                log.info("Regenerating WSDL for flow structure: {}", flowStructure.getName());

                // Initialize associations
                if(flowStructure.getFlowStructureMessages() != null) {
                    // Collections are already loaded by SQL repository
                    for(FlowStructureMessage fsm : flowStructure.getFlowStructureMessages()) {
                        // Message structure already loaded by SQL repository
                    }
                }

                generateWsdl(flowStructure);
                flowStructureRepository.save(flowStructure);
                log.info("Regenerated WSDL for flow structure: {}", flowStructure.getName());
            }
        }
    }

    private FlowStructureDTO convertToFlowStructureDTO(FlowStructure entity) {
        try {
            Set<FlowStructureMessageDTO> messages = entity.getFlowStructureMessages() != null ?
                    entity.getFlowStructureMessages().stream()
                            .map(this::convertToFlowStructureMessageDTO)
                            .collect(Collectors.toSet()) : new HashSet<>();

            return FlowStructureDTO.builder()
                    .id(entity.getId().toString())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .processingMode(FlowStructureDTO.ProcessingMode.valueOf(entity.getProcessingMode().name()))
                    .direction(FlowStructureDTO.Direction.valueOf(entity.getDirection().name()))
                    .wsdlContent(entity.getWsdlContent())
                    .sourceType(entity.getSourceType())
                    .namespace(extractNamespaceData(entity))
                    .metadata(generateOperationMetadata(entity))
                    .tags(new HashSet<>()) // Note: Tags table not yet implemented
                    .version(entity.getVersion())
                    .isActive(entity.getIsActive())
                    .businessComponent(convertToBusinessComponentDTO(entity.getBusinessComponent()))
                    .flowStructureMessages(messages)
                    .createdBy(convertToUserDTO(entity.getCreatedBy()))
                    .updatedBy(convertToUserDTO(entity.getUpdatedBy()))
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch(Exception e) {
            log.error("Error converting FlowStructure to DTO", e);
            throw new RuntimeException("Error converting FlowStructure to DTO", e);
        }
    }

    private FlowStructureMessageDTO convertToFlowStructureMessageDTO(FlowStructureMessage entity) {
        return FlowStructureMessageDTO.builder()
                .flowStructureId(entity.getFlowStructure().getId().toString())
                .messageType(FlowStructureMessageDTO.MessageType.valueOf(entity.getMessageType().name()))
                .messageStructure(convertToMessageStructureDTO(entity.getMessageStructure()))
                .build();
    }

    private MessageStructureDTO convertToMessageStructureDTO(MessageStructure entity) {
        try {
            return MessageStructureDTO.builder()
                    .id(entity.getId().toString())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .xsdContent(entity.getXsdContent())
                    .namespace(extractMessageNamespaceData(entity))
                    .metadata(new HashMap<>()) // Note: Metadata table not yet implemented
                    .tags(new HashSet<>()) // Note: Tags table not yet implemented
                    .version(entity.getVersion())
                    .isActive(entity.getIsActive())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch(Exception e) {
            log.error("Error converting MessageStructure to DTO", e);
            throw new RuntimeException("Error converting MessageStructure to DTO", e);
        }
    }

    private BusinessComponentDTO convertToBusinessComponentDTO(com.integrixs.data.model.BusinessComponent entity) {
        if(entity == null) return null;
        return BusinessComponentDTO.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    private UserDTO convertToUserDTO(User user) {
        if(user == null) return null;
        return UserDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private Map<String, Object> extractNamespaceData(FlowStructure entity) {
        Map<String, Object> namespaceData = new HashMap<>();

        // Extract namespace from namespaces relationship
        if(entity.getNamespaces() != null && !entity.getNamespaces().isEmpty()) {
            // Find the default namespace or the first one
            FlowStructureNamespace defaultNs = entity.getNamespaces().stream()
                    .filter(FlowStructureNamespace::isDefault)
                    .findFirst()
                    .orElse(entity.getNamespaces().iterator().next());

            namespaceData.put("prefix", defaultNs.getPrefix() != null ? defaultNs.getPrefix() : "tns");
            namespaceData.put("uri", defaultNs.getUri());

            // Add all namespaces to a list
            List<Map<String, String>> allNamespaces = entity.getNamespaces().stream()
                    .map(ns -> {
                        Map<String, String> nsMap = new HashMap<>();
                        nsMap.put("prefix", ns.getPrefix() != null ? ns.getPrefix() : "");
                        nsMap.put("uri", ns.getUri());
                        nsMap.put("isDefault", String.valueOf(ns.isDefault()));
                        return nsMap;
                    })
                    .collect(Collectors.toList());
            namespaceData.put("all", allNamespaces);
        } else {
            // Default namespace
            namespaceData.put("prefix", "tns");
            namespaceData.put("uri", "http://integrixflowbridge.com/" + entity.getName());
            namespaceData.put("all", Collections.emptyList());
        }

        return namespaceData;
    }

    private Map<String, Object> generateOperationMetadata(FlowStructure entity) {
        Map<String, Object> metadata = new HashMap<>();

        // Generate operation info based on flow structure properties
        Map<String, Object> operationInfo = new HashMap<>();
        operationInfo.put("hasInput", true);
        operationInfo.put("hasOutput", entity.getProcessingMode() == ProcessingMode.SYNC);
        operationInfo.put("hasFault", entity.getProcessingMode() == ProcessingMode.SYNC);
        operationInfo.put("isSynchronous", entity.getProcessingMode() == ProcessingMode.SYNC);

        List<String> messageTypes = new ArrayList<>();
        messageTypes.add("input");
        if(entity.getProcessingMode() == ProcessingMode.SYNC) {
            messageTypes.add("output");
            messageTypes.add("fault");
        }
        operationInfo.put("messageTypes", messageTypes);

        metadata.put("operationInfo", operationInfo);

        // Add any additional metadata
        if(entity.getFlowStructureMessages() != null) {
            metadata.put("messageCount", entity.getFlowStructureMessages().size());
        }

        return metadata;
    }

    private Map<String, Object> extractMessageNamespaceData(MessageStructure entity) {
        Map<String, Object> namespaceData = new HashMap<>();

        // Extract namespace from namespaces relationship
        if(entity.getNamespaces() != null && !entity.getNamespaces().isEmpty()) {
            // Find the default namespace or the first one
            MessageStructureNamespace defaultNs = entity.getNamespaces().stream()
                    .filter(MessageStructureNamespace::isDefault)
                    .findFirst()
                    .orElse(entity.getNamespaces().iterator().next());

            namespaceData.put("prefix", defaultNs.getPrefix() != null ? defaultNs.getPrefix() : "msg");
            namespaceData.put("uri", defaultNs.getUri());

            // Add all namespaces to a list
            List<Map<String, String>> allNamespaces = entity.getNamespaces().stream()
                    .map(ns -> {
                        Map<String, String> nsMap = new HashMap<>();
                        nsMap.put("prefix", ns.getPrefix() != null ? ns.getPrefix() : "");
                        nsMap.put("uri", ns.getUri());
                        nsMap.put("isDefault", String.valueOf(ns.isDefault()));
                        return nsMap;
                    })
                    .collect(Collectors.toList());
            namespaceData.put("all", allNamespaces);
        } else {
            // Default namespace
            namespaceData.put("prefix", "msg");
            namespaceData.put("uri", "http://integrixflowbridge.com/messages/" + entity.getName());
            namespaceData.put("all", Collections.emptyList());
        }

        return namespaceData;
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch(Exception e) {
            log.error("Error serializing to JSON", e);
            throw new RuntimeException("Error serializing to JSON", e);
        }
    }

    private String serializeElement(Element element, String indent) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(" {http://xml.apache.org/xslt}indent - amount", "2");

            // Clone the element to avoid modifying the original
            Element clonedElement = (Element) element.cloneNode(true);

            // Fix namespace prefixes to use xsd: instead of xs:
            fixNamespacePrefixes(clonedElement);

            DOMSource source = new DOMSource(clonedElement);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            // Add proper indentation
            String serialized = writer.toString();
            String[] lines = serialized.split("\n");
            StringBuilder indented = new StringBuilder();
            for(String line : lines) {
                if(!line.trim().isEmpty()) {
                    indented.append(indent).append(line.trim()).append("\n");
                }
            }

            return indented.toString().trim();
        } catch(Exception e) {
            log.error("Error serializing element", e);
            // Fallback to basic element
            return indent + "<xsd:element name = \"" + element.getAttribute("name") + "\" type = \"xsd:anyType\"/>";
        }
    }

    private void fixNamespacePrefixes(Node node) {
        if(node.getNodeType() == Node.ELEMENT_NODE) {
            Element elem = (Element) node;

            // Fix element name
            if(elem.getPrefix() != null && elem.getPrefix().equals("xs")) {
                elem.setPrefix("xsd");
            }

            // Fix attributes
            NamedNodeMap attributes = elem.getAttributes();
            for(int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String value = attr.getNodeValue();
                if(value != null && value.contains("xs:")) {
                    attr.setNodeValue(value.replace("xs:", "xsd:"));
                }
            }

            // Recursively fix child nodes
            NodeList children = node.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                fixNamespacePrefixes(children.item(i));
            }
        }
    }
}
