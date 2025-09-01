# Integrix Flow Bridge Architecture Diagrams

## System Overview

```mermaid
graph TB
    subgraph "External Systems"
        EXT1[HTTP APIs]
        EXT2[Databases]
        EXT3[FTP Servers]
        EXT4[Message Queues]
        EXT5[SOAP Services]
    end
    
    subgraph "Integrix Flow Bridge"
        subgraph "API Gateway"
            API[REST API<br/>Port 8080]
        end
        
        subgraph "Core Modules"
            BE[Backend Module<br/>Flow Management]
            ENG[Engine Module<br/>Flow Execution]
            ADAPT[Adapters Module<br/>External Connectivity]
        end
        
        subgraph "Support Modules"
            MON[Monitoring Module<br/>Observability]
            WC[WebClient Module<br/>Inbound Messages]
            WS[WebServer Module<br/>Outbound Calls]
            SOAP[SOAP Bindings<br/>WSDL Management]
        end
        
        subgraph "Data Layer"
            DB[(PostgreSQL<br/>Primary Storage)]
            CACHE[(Redis<br/>Caching)]
            QUEUE[Kafka<br/>Message Queue]
        end
    end
    
    subgraph "Frontend"
        UI[React UI<br/>Port 3000]
    end
    
    %% External connections
    UI --> API
    API --> BE
    WC --> EXT1
    ADAPT --> EXT1
    ADAPT --> EXT2
    ADAPT --> EXT3
    ADAPT --> EXT4
    WS --> EXT1
    SOAP --> EXT5
    
    %% Internal connections
    BE --> ENG
    ENG --> ADAPT
    BE --> MON
    ENG --> MON
    ADAPT --> MON
    WC --> ENG
    BE --> DB
    MON --> DB
    BE --> CACHE
    ENG --> QUEUE
```

## Clean Architecture Layers

```mermaid
graph TB
    subgraph "Clean Architecture"
        subgraph "API Layer"
            REST[REST Controllers]
            DTO[DTOs]
            MAP[Mappers]
        end
        
        subgraph "Application Layer"
            AS[Application Services]
            UC[Use Cases]
            TX[Transaction Management]
        end
        
        subgraph "Domain Layer"
            DM[Domain Models]
            DS[Domain Services]
            RI[Repository Interfaces]
            DE[Domain Events]
        end
        
        subgraph "Infrastructure Layer"
            REPO[Repository Implementations]
            CLIENT[External Clients]
            CONFIG[Configurations]
            INFRA[Infrastructure Services]
        end
    end
    
    %% Dependencies (only inward)
    REST --> AS
    DTO --> MAP
    MAP --> AS
    AS --> DS
    AS --> DM
    AS --> RI
    REPO --> RI
    CLIENT --> DS
    INFRA --> DS
```

## Module Communication Flow

```mermaid
sequenceDiagram
    participant UI as Frontend UI
    participant BE as Backend API
    participant ENG as Engine
    participant ADAPT as Adapters
    participant MON as Monitoring
    participant EXT as External System
    
    UI->>BE: Create Integration Flow
    BE->>BE: Validate Flow
    BE->>MON: Log Activity
    BE-->>UI: Flow Created
    
    UI->>BE: Execute Flow
    BE->>ENG: Execute Flow Request
    ENG->>ADAPT: Get Source Data
    ADAPT->>EXT: Fetch Data
    EXT-->>ADAPT: Data Response
    ADAPT-->>ENG: Adapter Result
    ENG->>ENG: Transform Data
    ENG->>ADAPT: Send to Target
    ADAPT->>EXT: Push Data
    EXT-->>ADAPT: Confirmation
    ADAPT-->>ENG: Success
    ENG->>MON: Record Metrics
    ENG-->>BE: Execution Result
    BE-->>UI: Flow Executed
```

## Database Schema (Simplified)

```mermaid
erDiagram
    INTEGRATION_FLOWS {
        string id PK
        string name UK
        string description
        string status
        string source_adapter_id FK
        string target_adapter_id FK
        int retry_attempts
        timestamp created_at
    }
    
    COMMUNICATION_ADAPTERS {
        string id PK
        string name UK
        string adapter_type
        string direction
        json configuration
        boolean active
        timestamp created_at
    }
    
    FIELD_MAPPINGS {
        string id PK
        string flow_id FK
        string source_field
        string target_field
        string transformation_type
        text transformation_expression
        boolean active
    }
    
    FLOW_EXECUTIONS {
        string id PK
        string flow_id FK
        string status
        timestamp start_time
        timestamp end_time
        text error_message
        json input_data
        json output_data
    }
    
    SYSTEM_LOGS {
        bigint id PK
        timestamp timestamp
        string module
        string operation
        json context
        string level
        text message
    }
    
    MESSAGES {
        string id PK
        string flow_id FK
        text content
        string status
        timestamp created_at
        timestamp processed_at
    }
    
    INTEGRATION_FLOWS ||--o{ FIELD_MAPPINGS : has
    INTEGRATION_FLOWS ||--o{ FLOW_EXECUTIONS : generates
    INTEGRATION_FLOWS }o--|| COMMUNICATION_ADAPTERS : source
    INTEGRATION_FLOWS }o--|| COMMUNICATION_ADAPTERS : target
    INTEGRATION_FLOWS ||--o{ MESSAGES : processes
```

## Deployment Architecture

```mermaid
graph TB
    subgraph "Production Environment"
        subgraph "Load Balancer"
            LB[Nginx/HAProxy]
        end
        
        subgraph "Application Tier"
            APP1[App Instance 1<br/>8080]
            APP2[App Instance 2<br/>8081]
            APP3[App Instance 3<br/>8082]
        end
        
        subgraph "Data Tier"
            subgraph "Primary DB"
                DB1[(PostgreSQL Master)]
            end
            
            subgraph "Read Replicas"
                DB2[(PostgreSQL Replica 1)]
                DB3[(PostgreSQL Replica 2)]
            end
            
            subgraph "Cache Cluster"
                REDIS1[Redis Master]
                REDIS2[Redis Slave]
            end
            
            subgraph "Message Queue"
                KAFKA1[Kafka Broker 1]
                KAFKA2[Kafka Broker 2]
                KAFKA3[Kafka Broker 3]
            end
        end
        
        subgraph "Monitoring Stack"
            PROM[Prometheus]
            GRAF[Grafana]
            ELK[ELK Stack]
        end
    end
    
    LB --> APP1
    LB --> APP2
    LB --> APP3
    
    APP1 --> DB1
    APP2 --> DB1
    APP3 --> DB1
    
    APP1 --> DB2
    APP2 --> DB3
    
    APP1 --> REDIS1
    APP2 --> REDIS1
    APP3 --> REDIS1
    
    APP1 --> KAFKA1
    APP2 --> KAFKA2
    APP3 --> KAFKA3
    
    APP1 --> PROM
    APP2 --> PROM
    APP3 --> PROM
```

## Security Architecture

```mermaid
graph TB
    subgraph "Security Layers"
        subgraph "Network Security"
            FW[Firewall]
            WAF[Web Application Firewall]
        end
        
        subgraph "Application Security"
            AUTH[Authentication<br/>JWT Tokens]
            AUTHZ[Authorization<br/>Role-Based]
            AUDIT[Audit Logging]
        end
        
        subgraph "Data Security"
            ENC[Encryption at Rest]
            TLS[TLS in Transit]
            MASK[Data Masking]
        end
        
        subgraph "Integration Security"
            OAUTH[OAuth 2.0]
            APIKEY[API Keys]
            CERT[Certificates]
        end
    end
    
    FW --> WAF
    WAF --> AUTH
    AUTH --> AUTHZ
    AUTHZ --> AUDIT
    
    ENC --> TLS
    TLS --> MASK
    
    OAUTH --> APIKEY
    APIKEY --> CERT
```

## Module Dependency Graph

```mermaid
graph LR
    subgraph "Shared Dependencies"
        SHARED[shared-lib<br/>DTOs, Enums, Interfaces]
        DATA[data-access<br/>Entities, Repositories]
    end
    
    subgraph "Core Modules"
        BE[backend]
        ENG[engine]
        ADAPT[adapters]
    end
    
    subgraph "Support Modules"
        MON[monitoring]
        WC[webclient]
        WS[webserver]
        SOAP[soap-bindings]
    end
    
    %% Dependencies
    BE --> SHARED
    BE --> DATA
    ENG --> SHARED
    ADAPT --> SHARED
    MON --> SHARED
    WC --> SHARED
    WS --> SHARED
    SOAP --> SHARED
    
    %% Module interactions (via interfaces)
    BE -.->|FlowExecutionClient| ENG
    ENG -.->|AdapterExecutionService| ADAPT
    BE -.->|SystemMonitor| MON
    ENG -.->|SystemMonitor| MON
    ADAPT -.->|SystemMonitor| MON
    WC -.->|MessageRoutingService| ENG
```

## Performance Optimization Points

```mermaid
graph TB
    subgraph "Caching Strategy"
        L1[Application Cache<br/>Caffeine]
        L2[Distributed Cache<br/>Redis]
        L3[Database Cache<br/>Query Results]
    end
    
    subgraph "Connection Pooling"
        DB_POOL[Database Pool<br/>HikariCP]
        HTTP_POOL[HTTP Pool<br/>Apache HttpClient]
        MQ_POOL[Message Queue Pool<br/>Kafka Producer Pool]
    end
    
    subgraph "Async Processing"
        ASYNC[Async Executors<br/>ThreadPoolTaskExecutor]
        REACTIVE[Reactive Streams<br/>Project Reactor]
        EVENT[Event-Driven<br/>Spring Events]
    end
    
    L1 --> L2
    L2 --> L3
    
    DB_POOL --> HTTP_POOL
    HTTP_POOL --> MQ_POOL
    
    ASYNC --> REACTIVE
    REACTIVE --> EVENT
```

## Monitoring and Observability

```mermaid
graph TB
    subgraph "Metrics Collection"
        APP[Application Metrics]
        SYS[System Metrics]
        BIZ[Business Metrics]
    end
    
    subgraph "Logging Pipeline"
        LOG[Application Logs]
        AUDIT[Audit Logs]
        ERROR[Error Logs]
    end
    
    subgraph "Tracing"
        TRACE[Distributed Tracing<br/>OpenTelemetry]
    end
    
    subgraph "Visualization"
        DASH[Dashboards<br/>Grafana]
        ALERT[Alerting<br/>AlertManager]
        REPORT[Reports<br/>Custom]
    end
    
    APP --> DASH
    SYS --> DASH
    BIZ --> DASH
    
    LOG --> ELK[ELK Stack]
    AUDIT --> ELK
    ERROR --> ELK
    
    TRACE --> DASH
    
    DASH --> ALERT
    ALERT --> REPORT
```