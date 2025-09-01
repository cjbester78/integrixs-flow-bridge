# Adapter Test Plan for Naming Refactoring

## Overview
This document outlines the comprehensive test plan to ensure adapter functionality is preserved during the naming refactoring from Sender/Receiver to Inbound/Outbound.

## Test Categories

### 1. Unit Tests for Each Adapter Type

#### File Adapters
```java
// FileSenderAdapterTest.java → FileInboundAdapterTest.java
- testPollForFiles()
- testFilePatternMatching()
- testPostProcessingDelete()
- testPostProcessingArchive()
- testPostProcessingMove()
- testEmptyFileHandling()
- testDuplicateDetection()
- testPollingSchedule()

// FileReceiverAdapterTest.java → FileOutboundAdapterTest.java
- testWriteFile()
- testDirectoryCreation()
- testFileNaming()
- testOverwriteHandling()
- testAtomicWrite()
```

#### FTP/SFTP Adapters
```java
// FtpSenderAdapterTest.java → FtpInboundAdapterTest.java
- testFtpConnection()
- testFtpPolling()
- testPassiveMode()
- testActiveMode()
- testSslConnection()
- testFileDownload()
- testRemoteDelete()

// SftpSenderAdapterTest.java → SftpInboundAdapterTest.java
- testSshConnection()
- testPublicKeyAuth()
- testPasswordAuth()
- testKnownHosts()
- testSftpPolling()
- testPermissionHandling()
```

#### Database Adapters
```java
// JdbcSenderAdapterTest.java → JdbcInboundAdapterTest.java
- testDatabaseConnection()
- testSelectQuery()
- testIncrementalPolling()
- testConnectionPooling()
- testQueryTimeout()
- testBatchFetch()
- testLastProcessedTracking()

// JdbcReceiverAdapterTest.java → JdbcOutboundAdapterTest.java
- testInsertOperation()
- testUpdateOperation()
- testDeleteOperation()
- testBatchOperations()
- testTransactionHandling()
```

#### Messaging Adapters
```java
// JmsSenderAdapterTest.java → JmsInboundAdapterTest.java
- testQueueConsumer()
- testTopicSubscriber()
- testDurableSubscription()
- testMessageSelector()
- testTransactedSession()
- testAcknowledgmentModes()

// KafkaSenderAdapterTest.java → KafkaInboundAdapterTest.java
- testConsumerGroup()
- testPartitionAssignment()
- testOffsetManagement()
- testRebalancing()
- testErrorHandling()
```

#### Web Service Adapters
```java
// HttpSenderAdapterTest.java → HttpInboundAdapterTest.java
- testHttpServerStart()
- testEndpointRegistration()
- testBasicAuth()
- testOAuth2()
- testRequestHandling()
- testSslSupport()

// RestSenderAdapterTest.java → RestInboundAdapterTest.java
- testRestEndpoints()
- testContentNegotiation()
- testOpenApiGeneration()
- testCorsHandling()

// SoapSenderAdapterTest.java → SoapInboundAdapterTest.java
- testWsdlExposure()
- testSoapOperations()
- testWsSecurity()
- testSoapFaults()
```

### 2. Integration Tests

#### Flow Integration Tests
```java
public class AdapterFlowIntegrationTest {
    @Test
    public void testFileToFtpFlow() {
        // Test FileSender → FtpReceiver flow
    }
    
    @Test
    public void testJdbcToKafkaFlow() {
        // Test JdbcSender → KafkaReceiver flow
    }
    
    @Test
    public void testHttpToJmsFlow() {
        // Test HttpSender → JmsReceiver flow
    }
    
    @Test
    public void testSftpToJdbcFlow() {
        // Test SftpSender → JdbcReceiver flow
    }
}
```

#### Database Migration Tests
```java
public class DatabaseMigrationTest {
    @Test
    public void testAdapterModeEnumMigration() {
        // Test SENDER → INBOUND, RECEIVER → OUTBOUND
    }
    
    @Test
    public void testFlowStructureDirectionMigration() {
        // Test SOURCE → INBOUND, TARGET → OUTBOUND
    }
    
    @Test
    public void testConfigurationJsonMigration() {
        // Test JSON config updates
    }
    
    @Test
    public void testColumnRenames() {
        // Test source_adapter_id → inbound_adapter_id
        // Test target_adapter_id → outbound_adapter_id
    }
}
```

### 3. API Compatibility Tests

#### REST API Tests
```java
public class AdapterApiCompatibilityTest {
    @Test
    public void testV1ApiWithOldTerminology() {
        // POST /api/v1/adapters with mode=SENDER
        // Should still work with deprecation warning
    }
    
    @Test
    public void testV2ApiWithNewTerminology() {
        // POST /api/v2/adapters with mode=INBOUND
        // Should work without warnings
    }
    
    @Test
    public void testApiVersionNegotiation() {
        // Test Accept headers for version selection
    }
}
```

### 4. Performance Tests

#### Adapter Performance Benchmarks
```java
public class AdapterPerformanceBenchmark {
    @Test
    public void benchmarkFilePolling() {
        // Measure polling performance before/after
    }
    
    @Test
    public void benchmarkDatabasePolling() {
        // Measure query execution time
    }
    
    @Test
    public void benchmarkMessageProcessing() {
        // Measure message throughput
    }
}
```

### 5. Frontend Integration Tests

#### React Component Tests
```typescript
describe('AdapterConfiguration', () => {
  it('should display INBOUND instead of SENDER', () => {
    // Test UI label changes
  });
  
  it('should handle old API responses', () => {
    // Test backward compatibility
  });
  
  it('should send new terminology to API', () => {
    // Test outgoing requests
  });
});
```

### 6. End-to-End Tests

#### Selenium/Cypress Tests
```javascript
describe('Adapter Management E2E', () => {
  it('should create inbound file adapter', () => {
    // Create adapter through UI
    // Verify in database
    // Test in flow
  });
  
  it('should migrate existing adapters', () => {
    // Load old adapter
    // Save with new UI
    // Verify functionality preserved
  });
});
```

## Test Data Sets

### 1. Adapter Configurations
Create test configurations for each adapter type with:
- Minimal required config
- Full feature config
- Edge case configs

### 2. Test Files/Messages
- Various file formats (CSV, XML, JSON, Binary)
- Different sizes (empty, small, large)
- Special characters in names
- Different encodings

### 3. Database Test Data
- Sample tables with various data types
- Large datasets for performance testing
- Incremental polling scenarios

## Test Execution Plan

### Phase 1: Pre-Refactoring Baseline
1. Run all existing tests
2. Record performance metrics
3. Document any failing tests
4. Create snapshot of test results

### Phase 2: During Refactoring
1. Run tests after each component change
2. Ensure no regression
3. Update test names/packages as needed
4. Maintain test coverage

### Phase 3: Post-Refactoring Validation
1. Run full test suite
2. Compare with baseline metrics
3. Verify backward compatibility
4. Performance regression testing

## Automated Test Pipeline

### CI/CD Integration
```yaml
# .github/workflows/adapter-tests.yml
name: Adapter Test Suite

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run Unit Tests
        run: mvn test -Dtest=*AdapterTest
  
  integration-tests:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
      kafka:
        image: confluentinc/cp-kafka:latest
    steps:
      - name: Run Integration Tests
        run: mvn test -Dtest=*IntegrationTest
  
  performance-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Run Performance Tests
        run: mvn test -Dtest=*Benchmark -DforkCount=1
```

## Test Coverage Requirements

### Minimum Coverage Targets
- Unit Tests: 80% line coverage
- Integration Tests: All adapter combinations
- API Tests: All endpoints
- Frontend Tests: All user flows

### Critical Path Coverage
Must have 100% coverage for:
- Adapter lifecycle methods
- Data transformation logic
- Error handling paths
- Configuration validation

## Rollback Testing

### Rollback Scenarios
1. Database rollback script testing
2. Code rollback procedure
3. Configuration rollback
4. API version rollback

### Rollback Validation
- Ensure old terminology works after rollback
- Verify no data loss
- Test flow continuity

## Test Documentation

### Test Reports
- Generate test execution reports
- Document any issues found
- Track resolution status
- Performance comparison reports

### Test Maintenance
- Update tests with refactoring
- Keep test documentation current
- Review and update test data
- Archive baseline metrics

---

This comprehensive test plan ensures that the adapter naming refactoring can be performed safely with confidence that no functionality is lost.