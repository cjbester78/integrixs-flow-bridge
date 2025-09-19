# TODO: Remove Lombok from shared-lib module

## Task: Fix all Lombok-using files by removing annotations and adding explicit implementations

### Files to fix (55 total):

#### Critical DTOs (likely causing compilation failures):
- [x] AdapterContext.java ✅
- [ ] AdapterResult.java
- [ ] ExecutionResult.java
- [ ] ValidationResult.java
- [ ] AdapterMetadata.java
- [ ] HealthCheckResult.java
- [ ] AdapterConfigDTO.java
- [ ] IntegrationFlowDTO.java
- [ ] MessageStructureDTO.java
- [ ] FlowStructureDTO.java

#### Events:
- [ ] AbstractDomainEvent.java
- [ ] FlowStatusChangedEvent.java
- [ ] FlowCreatedEvent.java
- [ ] FlowExecutedEvent.java

#### Flow DTOs:
- [ ] FlowTransformationDTO.java
- [ ] FlowCreateRequestDTO.java
- [ ] IntegrationFlowDTO.java (in flow package)

#### Structure DTOs:
- [ ] FlowStructureMessageDTO.java
- [ ] FlowStructureCreateRequestDTO.java
- [ ] MessageStructureCreateRequestDTO.java

#### Export/Import DTOs:
- [ ] FlowExportDTO.java
- [ ] FlowImportValidationDTO.java
- [ ] FlowExportRequestDTO.java
- [ ] FlowImportResultDTO.java
- [ ] FlowImportRequestDTO.java

#### Log DTOs:
- [ ] LogExportRequest.java
- [ ] FrontendLogBatchRequest.java
- [ ] FrontendLogEntry.java
- [ ] LogSearchResult.java
- [ ] LogSearchCriteria.java
- [ ] FlowExecutionTimeline.java
- [ ] CorrelatedLogGroup.java

#### System DTOs:
- [ ] SystemLogDTO.java
- [ ] DashboardStatsDTO.java
- [ ] DeploymentInfoDTO.java

#### Adapter DTOs:
- [ ] AdapterTestRequestDTO.java
- [ ] JsonXmlWrapperConfig.java
- [ ] AdapterTestResultDTO.java
- [ ] XmlMappingConfig.java

#### Other DTOs:
- [ ] BaseIntegrationException.java
- [ ] ChannelStatusDTO.java
- [ ] JarFileDTO.java
- [ ] TestFieldMappingsRequestDTO.java
- [ ] AdapterStatusDTO.java
- [ ] RoleDTO.java
- [ ] MessageStatsDTO.java
- [ ] ExternalAuthenticationDTO.java
- [ ] FunctionParameterDTO.java
- [ ] FieldMappingDTO.java
- [ ] TestFieldMappingsResponseDTO.java
- [ ] BusinessComponentDTO.java
- [ ] CertificateDTO.java
- [ ] RecentMessageDTO.java
- [ ] GlobalRetrySettingsDTO.java

## Implementation Plan:

1. Start with critical integration DTOs that adapters depend on
2. For each file:
   - Remove Lombok imports
   - Replace @Data with getters, setters, toString, equals, hashCode
   - Replace @Builder with static Builder inner class
   - Replace @NoArgsConstructor with default constructor
   - Replace @AllArgsConstructor with parameterized constructor
   - Replace @Getter/@Setter with individual methods
   - Replace @ToString with toString method
   - Replace @EqualsAndHashCode with equals and hashCode methods
3. Test compilation after each group of files

## Progress:
- Files completed: 0/55
- Current file: Starting with AdapterContext.java