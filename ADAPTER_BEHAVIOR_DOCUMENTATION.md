# Current Adapter Behavior Documentation

## Overview
This document captures the current behavior of all adapters before refactoring the naming convention. This serves as a reference to ensure functionality is preserved during the renaming process.

## Adapter Naming Convention (Current - Reversed)
- **Sender Adapter** = Receives data FROM external systems (industry calls this Inbound/Receiver)
- **Receiver Adapter** = Sends data TO external systems (industry calls this Outbound/Sender)

## Adapter Types and Their Current Behavior

### 1. File-based Adapters

#### FileSenderAdapter (Inbound)
- **Purpose**: Polls local file system directories for files
- **Key Methods**:
  - `pollForFiles()`: Scans directory for new files
  - `startPolling()`: Begins scheduled polling
  - `processFile()`: Reads file content and metadata
- **Configuration**: Directory path, file patterns, polling interval
- **Post-processing**: Delete, archive, or move files

#### FileReceiverAdapter (Outbound)
- **Purpose**: Writes files to local file system
- **Key Methods**:
  - `send()`: Writes data to file
  - `ensureDirectoryExists()`: Creates directories if needed
- **Configuration**: Target directory, file naming pattern

### 2. FTP/SFTP Adapters

#### FtpSenderAdapter (Inbound)
- **Purpose**: Polls FTP servers for files
- **Key Methods**:
  - `pollForFiles()`: Lists and downloads files from FTP
  - `startPolling()`: Scheduled FTP polling
- **Configuration**: Server, credentials, directory, patterns
- **Features**: Passive/active mode, SSL/TLS support

#### FtpReceiverAdapter (Outbound)
- **Purpose**: Uploads files to FTP servers
- **Key Methods**:
  - `send()`: Uploads data to FTP server
- **Configuration**: Server, credentials, target directory

#### SftpSenderAdapter (Inbound)
- **Purpose**: Polls SFTP servers for files
- **Key Methods**:
  - `pollForFiles()`: Lists and downloads files via SSH
  - `startPolling()`: Scheduled SFTP polling
- **Configuration**: Server, SSH auth, directory, patterns
- **Features**: Public key auth, known hosts verification

#### SftpReceiverAdapter (Outbound)
- **Purpose**: Uploads files to SFTP servers
- **Key Methods**:
  - `send()`: Uploads data via SFTP
- **Configuration**: Server, SSH auth, target directory

### 3. Database Adapters

#### JdbcSenderAdapter (Inbound)
- **Purpose**: Polls databases with SELECT queries
- **Key Methods**:
  - `pollForData()`: Executes SELECT query
  - `startPolling()`: Scheduled database polling
  - `buildIncrementalQuery()`: Adds WHERE clause for incremental polling
- **Configuration**: JDBC URL, query, polling interval
- **Features**: Incremental polling, connection pooling (HikariCP)

#### JdbcReceiverAdapter (Outbound)
- **Purpose**: Executes INSERT/UPDATE/DELETE operations
- **Key Methods**:
  - `send()`: Executes DML operations
- **Configuration**: JDBC URL, operation type, table

### 4. Messaging Adapters

#### JmsSenderAdapter (Inbound)
- **Purpose**: Consumes messages from JMS queues/topics
- **Key Methods**:
  - `startListening()`: Sets up JMS message listener
  - `onMessage()`: Processes received JMS messages
- **Configuration**: Connection factory, destination, selector
- **Features**: Durable subscriptions, message selectors

#### JmsReceiverAdapter (Outbound)
- **Purpose**: Sends messages to JMS queues/topics
- **Key Methods**:
  - `send()`: Publishes message to JMS
- **Configuration**: Connection factory, destination

#### KafkaSenderAdapter (Inbound)
- **Purpose**: Consumes messages from Kafka topics
- **Key Methods**:
  - `startListening()`: Starts Kafka consumer
- **Configuration**: Bootstrap servers, topics, consumer group
- **Features**: Offset management, partition assignment

#### KafkaReceiverAdapter (Outbound)
- **Purpose**: Produces messages to Kafka topics
- **Key Methods**:
  - `send()`: Publishes to Kafka topic
- **Configuration**: Bootstrap servers, topic

### 5. Web Service Adapters

#### HttpSenderAdapter (Inbound)
- **Purpose**: Exposes HTTP endpoints to receive data
- **Key Methods**:
  - `startListening()`: Starts HTTP server
- **Configuration**: Port, endpoint path, authentication
- **Note**: Acts as HTTP server

#### HttpReceiverAdapter (Outbound)
- **Purpose**: Makes HTTP requests to external APIs
- **Key Methods**:
  - `send()`: Executes HTTP request
- **Configuration**: URL, method, headers, auth

#### RestSenderAdapter (Inbound)
- **Purpose**: Exposes REST API endpoints
- **Key Methods**:
  - `startListening()`: Starts REST server
- **Configuration**: Port, resource paths
- **Features**: OpenAPI support

#### RestReceiverAdapter (Outbound)
- **Purpose**: Calls external REST APIs
- **Key Methods**:
  - `send()`: Makes REST API call
- **Configuration**: Base URL, resource, method

#### SoapSenderAdapter (Inbound)
- **Purpose**: Exposes SOAP web service endpoints
- **Key Methods**:
  - `startListening()`: Starts SOAP server
- **Configuration**: WSDL, endpoint URL
- **Features**: WS-Security support

#### SoapReceiverAdapter (Outbound)
- **Purpose**: Calls external SOAP services
- **Key Methods**:
  - `send()`: Invokes SOAP operation
- **Configuration**: WSDL URL, operation

### 6. Email Adapters

#### MailSenderAdapter (Inbound)
- **Purpose**: Polls email accounts for messages
- **Key Methods**:
  - `pollForEmails()`: Checks for new emails
  - `startPolling()`: Scheduled email polling
- **Configuration**: IMAP/POP3 server, credentials
- **Features**: Attachment handling, folder selection

#### MailReceiverAdapter (Outbound)
- **Purpose**: Sends emails
- **Key Methods**:
  - `send()`: Sends email via SMTP
- **Configuration**: SMTP server, credentials

### 7. SAP Adapters

#### RfcSenderAdapter (Inbound)
- **Purpose**: Receives RFC calls from SAP
- **Key Methods**:
  - `startListening()`: Registers RFC server
- **Configuration**: SAP gateway, program ID
- **Note**: SAP pushes RFC calls

#### RfcReceiverAdapter (Outbound)
- **Purpose**: Calls SAP RFCs
- **Key Methods**:
  - `send()`: Invokes RFC function
- **Configuration**: SAP system, RFC name

#### IdocSenderAdapter (Inbound)
- **Purpose**: Receives IDocs from SAP
- **Key Methods**:
  - `startListening()`: Registers IDoc server
- **Configuration**: SAP gateway, program ID
- **Note**: SAP pushes IDocs via tRFC/qRFC

#### IdocReceiverAdapter (Outbound)
- **Purpose**: Sends IDocs to SAP
- **Key Methods**:
  - `send()`: Sends IDoc to SAP
- **Configuration**: SAP system, IDoc type

### 8. OData Adapters

#### OdataSenderAdapter (Inbound)
- **Purpose**: Exposes OData service endpoints
- **Key Methods**:
  - `startListening()`: Starts OData server
- **Configuration**: Service metadata, entity sets
- **Note**: Request-response pattern

#### OdataReceiverAdapter (Outbound)
- **Purpose**: Consumes external OData services
- **Key Methods**:
  - `send()`: Executes OData query/operation
- **Configuration**: Service URL, entity/operation

## Adapter Communication Patterns

### Polling Adapters (Scheduled Pull)
- FileSenderAdapter
- FtpSenderAdapter
- SftpSenderAdapter
- JdbcSenderAdapter
- MailSenderAdapter

### Push-based Adapters (Event-driven)
- JmsSenderAdapter (message listener)
- KafkaSenderAdapter (consumer)
- HttpSenderAdapter (HTTP server)
- RestSenderAdapter (REST server)
- SoapSenderAdapter (SOAP server)
- RfcSenderAdapter (RFC server)
- IdocSenderAdapter (IDoc server)
- OdataSenderAdapter (OData server)

## Key Interfaces and Abstract Classes

### SenderAdapterPort
```java
public interface SenderAdapterPort {
    AdapterOperationResult fetch(FetchRequest request);
    CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request);
    void startListening(DataReceivedCallback callback);
    void stopListening();
    boolean isListening();
}
```

### ReceiverAdapterPort
```java
public interface ReceiverAdapterPort {
    AdapterOperationResult send(SendRequest request);
    CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request);
}
```

### AbstractSenderAdapter
- Base class for all Sender (Inbound) adapters
- Implements common lifecycle methods
- Provides polling infrastructure

### AbstractReceiverAdapter
- Base class for all Receiver (Outbound) adapters
- Implements common send patterns
- Handles retry logic

## Configuration Patterns

### Common Configuration Fields
- All adapters have mode (SENDER/RECEIVER)
- Connection parameters specific to protocol
- Authentication credentials
- Timeout and retry settings

### Polling Configuration (Sender Adapters)
- pollingInterval: Milliseconds between polls
- maxFilesPerPoll: Batch size limits
- processingMode: delete/archive/move after processing

## Database Schema References

### communication_adapters table
- mode column: SENDER or RECEIVER enum
- configuration column: JSON with adapter-specific config

### flow_structures table
- direction column: SOURCE (uses Sender) or TARGET (uses Receiver)

### integration_flows table
- source_adapter_id: References Sender adapter
- target_adapter_id: References Receiver adapter

## Testing Considerations

1. **Polling Behavior**: Test scheduled execution and file/data discovery
2. **Connection Management**: Test connect/disconnect cycles
3. **Error Handling**: Test failure scenarios and recovery
4. **Data Transformation**: Test data format conversions
5. **Authentication**: Test various auth mechanisms
6. **Performance**: Test with high volumes

## Migration Notes

When refactoring:
1. Preserve all functional behavior
2. Maintain configuration compatibility
3. Keep same error handling patterns
4. Preserve logging patterns
5. Maintain thread safety
6. Keep performance characteristics

---

This documentation serves as the baseline for validating that the refactoring maintains all existing functionality.