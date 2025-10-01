package com.integrixs.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.dto.XsdImportResult;
import com.integrixs.backend.dto.XsdValidationResult;
import com.integrixs.data.model.BusinessComponent;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.BusinessComponentSqlRepository;
import com.integrixs.data.sql.repository.FlowStructureMessageSqlRepository;
import com.integrixs.data.sql.repository.MessageStructureSqlRepository;
import com.integrixs.shared.dto.structure.MessageStructureCreateRequestDTO;
import com.integrixs.shared.dto.structure.MessageStructureDTO;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageStructureService {

    private static final Logger log = LoggerFactory.getLogger(MessageStructureService.class);


    private final MessageStructureSqlRepository messageStructureRepository;
    private final BusinessComponentSqlRepository businessComponentRepository;
    private final FlowStructureMessageSqlRepository flowStructureMessageRepository;
    private final ObjectMapper objectMapper;

    public MessageStructureService(MessageStructureSqlRepository messageStructureRepository,
                                   BusinessComponentSqlRepository businessComponentRepository,
                                   FlowStructureMessageSqlRepository flowStructureMessageRepository,
                                   ObjectMapper objectMapper) {
        this.messageStructureRepository = messageStructureRepository;
        this.businessComponentRepository = businessComponentRepository;
        this.flowStructureMessageRepository = flowStructureMessageRepository;
        this.objectMapper = objectMapper;
    }

    public MessageStructureDTO create(MessageStructureCreateRequestDTO request, User currentUser) {
        log.info("Creating message structure: {}", request.getName());

        // Check if name already exists for business component
        if(messageStructureRepository.existsByNameAndBusinessComponentIdAndIsActiveTrue(
                request.getName(), UUID.fromString(request.getBusinessComponentId()))) {
            throw new RuntimeException("Message structure with name '" + request.getName() +
                    "' already exists for this business component");
        }

        MessageStructure messageStructure = MessageStructure.builder()
                .name(request.getName())
                .description(request.getDescription())
                .xsdContent(request.getXsdContent())
                // Namespace, metadata, and tags are now handled separately in related tables
                .sourceType("INTERNAL")
                .isEditable(true)
                .businessComponent(businessComponentRepository.findById(UUID.fromString(request.getBusinessComponentId()))
                        .orElseThrow(() -> new RuntimeException("Business component not found")))
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        messageStructure = messageStructureRepository.save(messageStructure);
        return convertToMessageStructureDTO(messageStructure);
    }

    public MessageStructureDTO update(String id, MessageStructureCreateRequestDTO request, User currentUser) {
        log.info("Updating message structure: {}", id);

        MessageStructure messageStructure = messageStructureRepository.findByIdAndIsActiveTrue(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Message structure not found"));

        // Check if structure is editable
        if(!messageStructure.getIsEditable()) {
            throw new RuntimeException("Cannot edit external message structure. This structure is read - only.");
        }

        // Check if name is being changed and already exists
        if(!messageStructure.getName().equals(request.getName()) &&
                messageStructureRepository.existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(
                        request.getName(), UUID.fromString(request.getBusinessComponentId()), UUID.fromString(id))) {
            throw new RuntimeException("Message structure with name '" + request.getName() +
                    "' already exists for this business component");
        }

        messageStructure.setName(request.getName());
        messageStructure.setDescription(request.getDescription());
        messageStructure.setXsdContent(request.getXsdContent());
        // Namespace, metadata, and tags are now handled separately in related tables
        messageStructure.setBusinessComponent(businessComponentRepository.findById(UUID.fromString(request.getBusinessComponentId()))
                .orElseThrow(() -> new RuntimeException("Business component not found")));
        messageStructure.setUpdatedBy(currentUser);
        messageStructure.setVersion(messageStructure.getVersion() + 1);

        messageStructure = messageStructureRepository.save(messageStructure);
        return convertToMessageStructureDTO(messageStructure);
    }

    public MessageStructureDTO findById(String id) {
        MessageStructure messageStructure = messageStructureRepository.findByIdAndIsActiveTrue(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Message structure not found"));
        return convertToMessageStructureDTO(messageStructure);
    }

    public Page<MessageStructureDTO> findAll(String businessComponentId, String search, Pageable pageable) {
        UUID businessComponentUuid = businessComponentId != null && !businessComponentId.isEmpty()
                ? UUID.fromString(businessComponentId) : null;
        Page<MessageStructure> page = messageStructureRepository.searchMessageStructures(
                businessComponentUuid, search, pageable);
        return page.map(this::convertToMessageStructureDTO);
    }

    public List<MessageStructureDTO> findByBusinessComponent(String businessComponentId) {
        return messageStructureRepository.findByBusinessComponentIdAndIsActiveTrueOrderByName(UUID.fromString(businessComponentId))
                .stream()
                .map(this::convertToMessageStructureDTO)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        log.info("Deleting message structure: {}", id);
        MessageStructure messageStructure = messageStructureRepository.findByIdAndIsActiveTrue(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Message structure not found"));

        // First check if there are any flow structures using this message structure
        List<FlowStructure> flowStructures = flowStructureMessageRepository.findFlowStructuresByMessageStructureId(UUID.fromString(id));

        // Filter out inactive flow structures(soft - deleted ones)
        List<FlowStructure> activeFlowStructures = flowStructures.stream()
                .filter(FlowStructure::getIsActive)
                .collect(Collectors.toList());

        if(!activeFlowStructures.isEmpty()) {
            // There are still active flow structures using this message structure
            String flowNames = activeFlowStructures.stream()
                    .map(FlowStructure::getName)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Cannot delete message structure. It is being used by the following flow structures: " + flowNames);
        }

        // Clean up any orphaned flow_structure_messages records from inactive flow structures
        if(!flowStructures.isEmpty()) {
            log.info("Found {} total flow structures(active and inactive) using this message structure", flowStructures.size());
            // Since we already checked there are no active flow structures, we can proceed with deletion
            // The cascade delete will handle the flow_structure_messages cleanup
        }

        // Perform hard delete - permanently remove from database
        messageStructureRepository.delete(messageStructure);
        log.info("Message structure {} permanently deleted", id);
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
                    .sourceType(entity.getSourceType())
                    .isEditable(entity.getIsEditable())
                    .isActive(entity.getIsActive())
                    .importMetadata(new HashMap<>()) // Note: Import metadata table not yet implemented
                    .businessComponent(toBusinessComponentDTO(entity.getBusinessComponent()))
                    .createdBy(convertToUserDTO(entity.getCreatedBy()))
                    .updatedBy(convertToUserDTO(entity.getUpdatedBy()))
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch(Exception e) {
            log.error("Error converting MessageStructure to DTO", e);
            throw new RuntimeException("Error converting MessageStructure to DTO", e);
        }
    }

    private BusinessComponentDTO toBusinessComponentDTO(com.integrixs.data.model.BusinessComponent entity) {
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

    private Map<String, Object> extractMessageNamespaceData(MessageStructure entity) {
        Map<String, Object> namespaceData = new HashMap<>();

        // Extract namespace from namespaces relationship or use default
        if(entity.getNamespaces() != null && !entity.getNamespaces().isEmpty()) {
            // Note: MessageStructureNamespace entities not yet implemented - using default namespace
            namespaceData.put("prefix", "msg");
            namespaceData.put("uri", "http://integrixflowbridge.com/messages/" + entity.getName());
        } else {
            // Default namespace - try to extract from XSD
            Map<String, Object> extractedNamespace = extractNamespaceInfo(entity.getXsdContent());
            if(extractedNamespace != null) {
                namespaceData.putAll(extractedNamespace);
            } else {
                namespaceData.put("prefix", "msg");
                namespaceData.put("uri", "http://integrixflowbridge.com/messages/" + entity.getName());
            }
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

    public List<XsdValidationResult> validateXsdFiles(List<MultipartFile> files) {
        return validateXsdFiles(files, null);
    }

    public List<XsdValidationResult> validateXsdFiles(List<MultipartFile> files, Set<String> allFileNamesInBatch) {
        log.info("=== Starting XSD validation for {} files ===", files.size());
        if(allFileNamesInBatch != null) {
            log.info("Additional file names in batch: {}", allFileNamesInBatch);
        }
        List<XsdValidationResult> results = new ArrayList<>();
        Map<String, String> fileContents = new HashMap<>();

        // First, read all files
        log.info("Step 1: Reading all files into memory");
        for(MultipartFile file : files) {
            log.info(" Reading file: {}", file.getOriginalFilename());
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                fileContents.put(file.getOriginalFilename(), content);
                log.info(" ✓ Successfully read file: {} ( {} bytes)", file.getOriginalFilename(), content.length());
            } catch(Exception e) {
                log.error(" ✗ Failed to read file: {}", file.getOriginalFilename(), e);
                results.add(XsdValidationResult.builder()
                        .fileName(file.getOriginalFilename())
                        .valid(false)
                        .errors(Arrays.asList("Failed to read file: " + e.getMessage()))
                        .build());
            }
        }

        log.info("Successfully read {} files into memory", fileContents.size());

        // Then validate each file and check dependencies
        log.info("Step 2: Validating each file and checking dependencies");
        for(MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            log.info(" Validating file: {}", fileName);

            if(!fileContents.containsKey(fileName)) {
                log.info(" → Skipping(already reported as error)");
                continue; // Already reported as error
            }

            XsdValidationResult result = XsdValidationResult.builder()
                    .fileName(fileName)
                    .valid(true)
                    .errors(new ArrayList<>())
                    .dependencies(new ArrayList<>())
                    .resolvedDependencies(new ArrayList<>())
                    .missingDependencies(new ArrayList<>())
                    .build();

            try {
                String content = fileContents.get(fileName);
                log.info(" → Parsing XSD content( {} chars)", content.length());

                // Parse XSD to check validity and extract dependencies
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false); // Don't validate against DTD
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load - external - dtd", false);
                DocumentBuilder builder = factory.newDocumentBuilder();

                // Set custom error handler to capture parsing errors
                builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                    @Override
                    public void warning(org.xml.sax.SAXParseException e) {
                        log.warn(" → XML Warning: {}", e.getMessage());
                    }

                    @Override
                    public void error(org.xml.sax.SAXParseException e) {
                        log.error(" → XML Error: {}", e.getMessage());
                        result.setValid(false);
                        result.getErrors().add("XML Error: " + e.getMessage());
                    }

                    @Override
                    public void fatalError(org.xml.sax.SAXParseException e) {
                        log.error(" → XML Fatal Error: {}", e.getMessage());
                        result.setValid(false);
                        result.getErrors().add("XML Fatal Error: " + e.getMessage());
                    }
                });

                Document doc = builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
                log.info(" → Successfully parsed XML document");

                // Extract imports and includes
                NodeList imports = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
                NodeList includes = doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");

                log.info(" → Found {} imports and {} includes", imports.getLength(), includes.getLength());

                List<String> dependencies = new ArrayList<>();

                for(int i = 0; i < imports.getLength(); i++) {
                    var schemaLocationAttr = imports.item(i).getAttributes().getNamedItem("schemaLocation");
                    if(schemaLocationAttr != null) {
                        String schemaLocation = schemaLocationAttr.getNodeValue();
                        log.info("    → Import schemaLocation: {}", schemaLocation);
                        if(schemaLocation != null && !schemaLocation.startsWith("http")) {
                            dependencies.add(schemaLocation);
                            log.info("    → Added as dependency: {}", schemaLocation);
                        }
                    }
                }

                for(int i = 0; i < includes.getLength(); i++) {
                    var schemaLocationAttr = includes.item(i).getAttributes().getNamedItem("schemaLocation");
                    if(schemaLocationAttr != null) {
                        String schemaLocation = schemaLocationAttr.getNodeValue();
                        log.info("    → Include schemaLocation: {}", schemaLocation);
                        if(schemaLocation != null && !schemaLocation.startsWith("http")) {
                            dependencies.add(schemaLocation);
                            log.info("    → Added as dependency: {}", schemaLocation);
                        }
                    }
                }

                result.setDependencies(dependencies);
                log.info(" → Total dependencies found: {}", dependencies.size());

                // Check which dependencies are resolved in the current batch or already exist
                log.info(" → Checking dependency resolution...");
                for(String dep : dependencies) {
                    String depFileName = dep.substring(dep.lastIndexOf('/') + 1);
                    String depStructureName = depFileName.replace(".xsd", "");
                    log.info("    → Checking dependency: {} (file: {}, structure: {})", dep, depFileName, depStructureName);

                    boolean resolved = false;

                    // Check if in current chunk
                    if(fileContents.containsKey(depFileName)) {
                        result.getResolvedDependencies().add(dep);
                        log.info("      ✓ Found in current chunk");
                        resolved = true;
                    }
                    // Check if in the full batch(all file names provided by frontend)
                    else if(allFileNamesInBatch != null && allFileNamesInBatch.contains(depFileName)) {
                        result.getResolvedDependencies().add(dep);
                        log.info("      ✓ Found in full batch(different chunk)");
                        resolved = true;
                    }
                    // Check if already exists in database
                    else if(messageStructureRepository.existsByNameAndIsActiveTrue(depStructureName)) {
                        result.getResolvedDependencies().add(dep);
                        log.info("      ✓ Found in database");
                        resolved = true;
                    }

                    if(!resolved) {
                        // Dependency is truly missing
                        result.getMissingDependencies().add(dep);
                        log.info("      ✗ NOT FOUND - marked as missing");
                    }
                }

                // Only mark as invalid if there are truly missing dependencies
                if(!result.getMissingDependencies().isEmpty()) {
                    result.setValid(false);
                    String errorMsg = "Missing dependencies not found in batch or database: " + String.join(", ", result.getMissingDependencies());
                    result.getErrors().add(errorMsg);
                    log.error(" ✗ Validation FAILED: {}", errorMsg);
                } else {
                    log.info(" ✓ Validation PASSED");
                }

            } catch(Exception e) {
                result.setValid(false);
                result.getErrors().add("XML parsing error: " + e.getMessage());
                log.error(" ✗ Error validating XSD: {}", e.getMessage(), e);
            }

            results.add(result);
            log.info(" → Validation result: valid = {}, errors = {}, dependencies = {}, resolved = {}, missing = {}",
                     result.isValid(),
                     result.getErrors().size(),
                     result.getDependencies().size(),
                     result.getResolvedDependencies().size(),
                     result.getMissingDependencies().size());
        }

        log.info("=== XSD validation completed. Total results: {} ===", results.size());
        return results;
    }

    public List<XsdImportResult> importXsdFiles(List<MultipartFile> files, String businessComponentId, User currentUser) {
        log.info("=== Starting XSD import for {} files with business component: {} ===", files.size(), businessComponentId);
        List<XsdImportResult> results = new ArrayList<>();

        // Get business component
        BusinessComponent businessComponent = businessComponentRepository.findById(UUID.fromString(businessComponentId))
                .orElseThrow(() -> new RuntimeException("Business component not found: " + businessComponentId));

        // First validate all files
        List<XsdValidationResult> validationResults = validateXsdFiles(files);

        // Group files by validation status
        Map<String, XsdValidationResult> validationMap = validationResults.stream()
                .collect(Collectors.toMap(XsdValidationResult::getFileName, v -> v));

        // Log validation summary
        long validCount = validationResults.stream().filter(XsdValidationResult::isValid).count();
        long invalidCount = validationResults.size() - validCount;
        log.info("Validation summary: {} valid, {} invalid", validCount, invalidCount);

        // Import valid files in dependency order
        Set<String> imported = new HashSet<>();
        boolean progress = true;
        int iteration = 0;

        log.info("Starting dependency - ordered import...");
        while(progress) {
            progress = false;
            iteration++;
            log.info("Import iteration # {}", iteration);

            for(MultipartFile file : files) {
                String fileName = file.getOriginalFilename();

                if(imported.contains(fileName)) {
                    log.debug(" → {} already imported, skipping", fileName);
                    continue;
                }

                XsdValidationResult validation = validationMap.get(fileName);
                if(validation == null || !validation.isValid()) {
                    log.debug(" → {} is invalid, skipping", fileName);
                    continue;
                }

                // Check if all dependencies are imported or already exist
                boolean allDepsAvailable = true;
                for(String dep : validation.getDependencies()) {
                    String depFileName = dep.substring(dep.lastIndexOf('/') + 1);
                    String depStructureName = depFileName.replace(".xsd", "");

                    // Check if dependency is already imported in this batch or exists in DB
                    if(!imported.contains(depFileName) &&
                        !messageStructureRepository.existsByNameAndIsActiveTrue(depStructureName)) {
                        allDepsAvailable = false;
                        break;
                    }
                }

                if(!allDepsAvailable) {
                    continue;
                }

                // Import this file
                try {
                    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                    String structureName = fileName.replace(".xsd", "");

                    // Check if already exists
                    if(messageStructureRepository.existsByNameAndIsActiveTrue(structureName)) {
                        results.add(XsdImportResult.builder()
                                .fileName(fileName)
                                .structureName(structureName)
                                .success(false)
                                .message("Message structure with this name already exists")
                                .build());
                    } else {
                        // Extract namespace information from XSD
                        Map<String, Object> namespaceInfo = extractNamespaceInfo(content);

                        // Create message structure
                        Map<String, Object> importMetadata = new HashMap<>();
                        importMetadata.put("originalFileName", fileName);
                        importMetadata.put("importedAt", new Date());
                        importMetadata.put("importedBy", currentUser.getUsername());

                        // Build metadata with resolved dependencies
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("importedFrom", fileName);
                        metadata.put("importedAt", new Date());

                        // Add resolved dependencies if any
                        if(validation.getDependencies() != null && !validation.getDependencies().isEmpty()) {
                            List<Map<String, String>> resolvedDeps = new ArrayList<>();
                            for(String dep : validation.getDependencies()) {
                                String depFileName = dep.substring(dep.lastIndexOf('/') + 1);
                                String depStructureName = depFileName.replace(".xsd", "");

                                // Find the message structure ID for this dependency
                                messageStructureRepository.findByNameAndIsActiveTrue(depStructureName)
                                    .ifPresent(depStructure -> {
                                        Map<String, String> depInfo = new HashMap<>();
                                        depInfo.put("name", depFileName);
                                        depInfo.put("structureId", depStructure.getId().toString());
                                        resolvedDeps.add(depInfo);
                                    });
                            }
                            if(!resolvedDeps.isEmpty()) {
                                metadata.put("resolvedDependencies", resolvedDeps);
                            }
                        }

                        MessageStructure messageStructure = MessageStructure.builder()
                                .name(structureName)
                                .description("Imported from " + fileName)
                                .xsdContent(content)
                                // Namespace info is stored separately in MessageStructureNamespace table
                                .sourceType("EXTERNAL")
                                .isEditable(false)
                                .isActive(true)
                                .businessComponent(businessComponent)
                                // Metadata and import metadata are stored separately
                                .createdBy(currentUser)
                                .updatedBy(currentUser)
                                .build();

                        messageStructureRepository.save(messageStructure);

                        results.add(XsdImportResult.builder()
                                .fileName(fileName)
                                .structureName(structureName)
                                .success(true)
                                .build());

                        imported.add(fileName);
                        progress = true;
                    }
                } catch(Exception e) {
                    results.add(XsdImportResult.builder()
                            .fileName(fileName)
                            .success(false)
                            .message("Import failed: " + e.getMessage())
                            .build());
                }
            }
        }

        // Report files that couldn't be imported
        for(MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if(!imported.contains(fileName) && !results.stream().anyMatch(r -> r.getFileName().equals(fileName))) {
                XsdValidationResult validation = validationMap.get(fileName);
                String message = validation != null && !validation.isValid()
                        ? "Validation failed: " + String.join(", ", validation.getErrors())
                        : "Could not import due to unresolved dependencies";

                results.add(XsdImportResult.builder()
                        .fileName(fileName)
                        .success(false)
                        .message(message)
                        .build());
            }
        }

        return results;
    }

    public static class XsdValidationResult {
        private String fileName;
        private boolean valid;
        private List<String> errors;
        private List<String> dependencies;
        private List<String> resolvedDependencies;
        private List<String> missingDependencies;

        public XsdValidationResult() {
        }

        public XsdValidationResult(String fileName, boolean valid, List<String> errors,
                                  List<String> dependencies, List<String> resolvedDependencies,
                                  List<String> missingDependencies) {
            this.fileName = fileName;
            this.valid = valid;
            this.errors = errors;
            this.dependencies = dependencies;
            this.resolvedDependencies = resolvedDependencies;
            this.missingDependencies = missingDependencies;
        }

        // Getters and setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }

        public List<String> getResolvedDependencies() {
            return resolvedDependencies;
        }

        public void setResolvedDependencies(List<String> resolvedDependencies) {
            this.resolvedDependencies = resolvedDependencies;
        }

        public List<String> getMissingDependencies() {
            return missingDependencies;
        }

        public void setMissingDependencies(List<String> missingDependencies) {
            this.missingDependencies = missingDependencies;
        }

        // Builder
        public static XsdValidationResultBuilder builder() {
            return new XsdValidationResultBuilder();
        }

        public static class XsdValidationResultBuilder {
            private String fileName;
            private boolean valid;
            private List<String> errors;
            private List<String> dependencies;
            private List<String> resolvedDependencies;
            private List<String> missingDependencies;

            public XsdValidationResultBuilder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public XsdValidationResultBuilder valid(boolean valid) {
                this.valid = valid;
                return this;
            }

            public XsdValidationResultBuilder errors(List<String> errors) {
                this.errors = errors;
                return this;
            }

            public XsdValidationResultBuilder dependencies(List<String> dependencies) {
                this.dependencies = dependencies;
                return this;
            }

            public XsdValidationResultBuilder resolvedDependencies(List<String> resolvedDependencies) {
                this.resolvedDependencies = resolvedDependencies;
                return this;
            }

            public XsdValidationResultBuilder missingDependencies(List<String> missingDependencies) {
                this.missingDependencies = missingDependencies;
                return this;
            }

            public XsdValidationResult build() {
                return new XsdValidationResult(fileName, valid, errors, dependencies,
                                             resolvedDependencies, missingDependencies);
            }
        }
    }

    public static class XsdImportResult {
        private String fileName;
        private String structureName;
        private boolean success;
        private String message;

        public XsdImportResult() {
        }

        public XsdImportResult(String fileName, String structureName, boolean success, String message) {
            this.fileName = fileName;
            this.structureName = structureName;
            this.success = success;
            this.message = message;
        }

        // Getters and setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getStructureName() {
            return structureName;
        }

        public void setStructureName(String structureName) {
            this.structureName = structureName;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        // Builder
        public static XsdImportResultBuilder builder() {
            return new XsdImportResultBuilder();
        }

        public static class XsdImportResultBuilder {
            private String fileName;
            private String structureName;
            private boolean success;
            private String message;

            public XsdImportResultBuilder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public XsdImportResultBuilder structureName(String structureName) {
                this.structureName = structureName;
                return this;
            }

            public XsdImportResultBuilder success(boolean success) {
                this.success = success;
                return this;
            }

            public XsdImportResultBuilder message(String message) {
                this.message = message;
                return this;
            }

            public XsdImportResult build() {
                return new XsdImportResult(fileName, structureName, success, message);
            }
        }
    }

    private Map<String, Object> extractNamespaceInfo(String xsdContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xsdContent.getBytes(StandardCharsets.UTF_8)));

            Map<String, Object> namespaceInfo = new HashMap<>();

            // Get the root schema element
            var schemaElement = doc.getDocumentElement();

            // Extract targetNamespace
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if(targetNamespace != null && !targetNamespace.isEmpty()) {
                namespaceInfo.put("targetNamespace", targetNamespace);
                namespaceInfo.put("uri", targetNamespace);
            }

            // Extract default namespace
            String xmlns = schemaElement.getAttribute("xmlns");
            if(xmlns != null && !xmlns.isEmpty()) {
                namespaceInfo.put("xmlns", xmlns);
            }

            // Extract prefix for XS namespace
            var attributes = schemaElement.getAttributes();
            for(int i = 0; i < attributes.getLength(); i++) {
                var attr = attributes.item(i);
                if(attr.getNodeName().startsWith("xmlns:") &&
                    "http://www.w3.org/2001/XMLSchema".equals(attr.getNodeValue())) {
                    String prefix = attr.getNodeName().substring(6);
                    namespaceInfo.put("prefix", prefix);
                }
            }

            return namespaceInfo.isEmpty() ? null : namespaceInfo;
        } catch(Exception e) {
            log.error("Error extracting namespace info from XSD", e);
            return null;
        }
    }

    /**
     * Infer XML structure from streaming elements
     */
    public Map<String, Object> inferXmlStructure(List<Map<String, Object>> elements) {
        Map<String, Object> structure = new HashMap<>();
        structure.put("type", "xml");
        structure.put("elements", new ArrayList<>());

        // Analyze elements to infer structure
        Map<String, Map<String, Object>> elementTypes = new HashMap<>();

        for(Map<String, Object> element : elements) {
            String elementName = (String) element.get("_name");
            if(elementName != null) {
                Map<String, Object> elementType = elementTypes.computeIfAbsent(
                    elementName, k -> new HashMap<>()
               );

                // Update element type info
                elementType.put("name", elementName);
                elementType.put("path", element.get("_path"));

                // Collect attributes
                Map<String, String> attributes = (Map<String, String>) element.get("_attributes");
                if(attributes != null && !attributes.isEmpty()) {
                    Map<String, String> existingAttrs = (Map<String, String>) elementType.get("attributes");
                    if(existingAttrs == null) {
                        elementType.put("attributes", new HashMap<>(attributes));
                    } else {
                        existingAttrs.putAll(attributes);
                    }
                }

                // Check for text content
                if(element.containsKey("_text")) {
                    elementType.put("hasTextContent", true);
                    String textSample = (String) element.get("_text");
                    if(textSample != null && textSample.length() > 50) {
                        textSample = textSample.substring(0, 50) + "...";
                    }
                    elementType.put("textSample", textSample);
                }

                // Track child elements
                element.forEach((key, value) -> {
                    if(!key.startsWith("_") && value instanceof Map) {
                        Set<String> children = (Set<String>) elementType.computeIfAbsent(
                            "children", k -> new HashSet<>()
                       );
                        children.add(key);
                    }
                });
            }
        }

        // Convert to list
        List<Map<String, Object>> elementList = new ArrayList<>(elementTypes.values());
        structure.put("elements", elementList);
        structure.put("sampleSize", elements.size());
        structure.put("uniqueElements", elementTypes.size());

        return structure;
    }

    /**
     * Infer JSON structure from streaming elements
     */
    public Map<String, Object> inferJsonStructure(List<JsonNode> elements) {
        Map<String, Object> structure = new HashMap<>();
        structure.put("type", "json");

        if(elements.isEmpty()) {
            structure.put("fields", Collections.emptyList());
            return structure;
        }

        // Analyze first element for structure(assume homogeneous)
        JsonNode sample = elements.get(0);
        Map<String, Object> schema = inferJsonSchema(sample);
        structure.put("schema", schema);

        // Collect field statistics
        Map<String, Map<String, Object>> fieldStats = new HashMap<>();
        for(JsonNode element : elements) {
            analyzeJsonFields(element, "", fieldStats);
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        fieldStats.forEach((path, stats) -> {
            Map<String, Object> field = new HashMap<>();
            field.put("path", path);
            field.put("type", stats.get("type"));
            field.put("nullable", stats.get("nullCount") != null && (int) stats.get("nullCount") > 0);
            field.put("samples", stats.get("samples"));
            fields.add(field);
        });

        structure.put("fields", fields);
        structure.put("sampleSize", elements.size());
        structure.put("fieldCount", fields.size());

        return structure;
    }

    private Map<String, Object> inferJsonSchema(JsonNode node) {
        Map<String, Object> schema = new HashMap<>();

        if(node.isObject()) {
            schema.put("type", "object");
            Map<String, Object> properties = new HashMap<>();

            node.fields().forEachRemaining(entry -> {
                properties.put(entry.getKey(), inferJsonSchema(entry.getValue()));
            });

            schema.put("properties", properties);
        } else if(node.isArray()) {
            schema.put("type", "array");
            if(node.size() > 0) {
                schema.put("items", inferJsonSchema(node.get(0)));
            }
        } else if(node.isTextual()) {
            schema.put("type", "string");
            String sample = node.asText();
            if(sample.length() > 50) {
                sample = sample.substring(0, 50) + "...";
            }
            schema.put("sample", sample);
        } else if(node.isNumber()) {
            if(node.isIntegralNumber()) {
                schema.put("type", "integer");
            } else {
                schema.put("type", "number");
            }
            schema.put("sample", node.numberValue());
        } else if(node.isBoolean()) {
            schema.put("type", "boolean");
            schema.put("sample", node.booleanValue());
        } else if(node.isNull()) {
            schema.put("type", "null");
        }

        return schema;
    }

    private void analyzeJsonFields(JsonNode node, String path, Map<String, Map<String, Object>> fieldStats) {
        if(node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
                updateFieldStats(fieldPath, entry.getValue(), fieldStats);
                analyzeJsonFields(entry.getValue(), fieldPath, fieldStats);
            });
        } else if(node.isArray() && node.size() > 0) {
            updateFieldStats(path + "[]", node.get(0), fieldStats);
            analyzeJsonFields(node.get(0), path + "[]", fieldStats);
        }
    }

    private void updateFieldStats(String path, JsonNode value, Map<String, Map<String, Object>> fieldStats) {
        Map<String, Object> stats = fieldStats.computeIfAbsent(path, k -> new HashMap<>());

        // Determine type
        String type;
        if(value.isTextual()) type = "string";
        else if(value.isNumber()) type = value.isIntegralNumber() ? "integer" : "number";
        else if(value.isBoolean()) type = "boolean";
        else if(value.isArray()) type = "array";
        else if(value.isObject()) type = "object";
        else if(value.isNull()) type = "null";
        else type = "unknown";

        stats.put("type", type);

        // Count nulls
        if(value.isNull()) {
            stats.put("nullCount", (int) stats.getOrDefault("nullCount", 0) + 1);
        }

        // Collect samples
        if(!value.isNull() && !value.isObject() && !value.isArray()) {
            List<Object> samples = (List<Object>) stats.computeIfAbsent("samples", k -> new ArrayList<>());
            if(samples.size() < 3) {
                String sampleValue = value.asText();
                if(sampleValue.length() > 50) {
                    sampleValue = sampleValue.substring(0, 50) + "...";
                }
                if(!samples.contains(sampleValue)) {
                    samples.add(sampleValue);
                }
            }
        }
    }
}
