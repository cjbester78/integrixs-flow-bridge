# Integrix Flow Bridge Documentation

## Overview
Integrix Flow Bridge is a comprehensive integration middleware platform built with Spring Boot backend and React/TypeScript frontend. It provides visual flow composition, adapter management, field mapping, and orchestration capabilities for enterprise integration scenarios.

## Documentation Structure

### Architecture Documentation
- [Application Architecture](APPLICATION_ARCHITECTURE.md) - High-level application architecture
- [Integrix Flow Bridge Architecture](INTEGRIX_FLOW_BRIDGE_ARCHITECTURE.md) - Detailed system architecture
- [Architectural Analysis](ARCHITECTURAL_ANALYSIS.md) - Analysis of current architecture and issues
- [Architecture Refactoring Plan](ARCHITECTURE_REFACTORING_PLAN.md) - Clean architecture migration plan
- [Refactoring Example - Auth](REFACTORING_EXAMPLE_AUTH.md) - Example of clean architecture implementation

### Design Documentation
- [XML Native Design](XML_NATIVE_DESIGN.md) - Native XML processing design without JavaScript
- [Role Permissions Matrix](ROLE_PERMISSIONS_MATRIX.md) - User roles and permissions

### Adapter Documentation
- [File Adapter Operations](adapters/FILE_ADAPTER_OPERATIONS.md) - File/FTP/SFTP adapter guide
- [HTTP Adapter Operations](adapters/HTTP_ADAPTER_OPERATIONS.md) - HTTP/REST adapter guide

### Database Documentation
- [Database Schema](database/DATABASE_SCHEMA_REPORT_COMPLETE.md) - Complete database schema
- [Flyway Configuration](database/FLYWAY_CONFIGURATION.md) - Database migration setup
- [PostgreSQL Migration](database/POSTGRESQL_MIGRATION.md) - PostgreSQL migration guide
- [PostgreSQL Setup](database/POSTGRESQL_SETUP_COMPLETE.md) - PostgreSQL setup completion
- [XML Native Migration](database/XML_NATIVE_MIGRATION_COMPLETED.md) - XML storage migration

### Deployment
- [Production Deployment](PRODUCTION_DEPLOYMENT.md) - Production deployment guide

## Quick Links
- [CLAUDE.md](../CLAUDE.md) - AI assistant instructions (project root)
- [EULA.md](../EULA.md) - End User License Agreement (project root)
- [todo.md](../todo.md) - Current development tasks (project root)

## Project Status
The project has undergone a major architectural refactoring to implement clean architecture principles. The following modules have been successfully refactored:
- Authentication system
- Integration Flow management
- Communication Adapter management  
- Message Queue management
- Flow Execution monitoring
- User management

The refactoring follows a layered architecture with clear separation between API, Application, Domain, and Infrastructure layers.