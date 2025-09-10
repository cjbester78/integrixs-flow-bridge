# JAR File Management System

## Overview

The JAR File Management System provides comprehensive functionality for uploading, storing, retrieving, and managing JAR files used by the plugin system. The system stores JAR file content directly in the database for better consistency and backup capabilities.

## Architecture

### Database Schema

The `jar_files` table stores JAR files with the following structure:

```sql
CREATE TABLE jar_files (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(50),
    file_size BIGINT,
    checksum VARCHAR(64),
    file_content BYTEA NOT NULL,  -- Stores actual JAR content
    adapter_types TEXT[],          -- Array of supported adapter types
    uploaded_by VARCHAR(100) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSON
);
```

### Components

1. **JarFile Entity** (`data-access/src/main/java/com/integrixs/data/model/JarFile.java`)
   - JPA entity mapping to the database table
   - Stores file content as byte array
   - Supports JSON metadata and array types

2. **JarFileRepository** (`data-access/src/main/java/com/integrixs/data/repository/JarFileRepository.java`)
   - Spring Data JPA repository
   - Custom queries for searching and filtering
   - Checksum-based duplicate detection

3. **JarFileService** (`backend/src/main/java/com/integrixs/backend/service/JarFileService.java`)
   - Business logic for JAR file operations
   - File validation and checksum calculation
   - Version extraction from filenames

4. **JarFileController** (`backend/src/main/java/com/integrixs/backend/controller/JarFileController.java`)
   - REST API endpoints
   - File upload/download handling
   - Search and statistics endpoints

## Features

### 1. File Upload

- **Validation**: Checks file type, size limits, and JAR file magic bytes
- **Duplicate Detection**: SHA-256 checksum prevents duplicate uploads
- **Version Extraction**: Automatically extracts version from filename patterns
- **Metadata Storage**: Stores additional metadata as JSON

```java
// Example upload
POST /api/jar-files
Content-Type: multipart/form-data

file: example-plugin-1.2.3.jar
description: "Example plugin for data transformation"
```

### 2. File Retrieval

- **Get by ID**: Retrieve JAR file metadata
- **Download**: Download actual JAR file content
- **Search**: Search by name, display name, or description
- **List Active**: Get all active JAR files

```java
// Download JAR file
GET /api/jar-files/{id}/download

// Search JAR files
GET /api/jar-files/search?q=transformation
```

### 3. File Management

- **Soft Delete**: Mark files as inactive without removing data
- **Permanent Delete**: Complete removal from database
- **Storage Statistics**: Track total storage usage

### 4. Security

- **File Type Validation**: Verifies ZIP/JAR magic bytes
- **Size Limits**: Configurable maximum file size (default 50MB)
- **Checksum Validation**: SHA-256 integrity checking
- **User Tracking**: Records who uploaded each file

## Configuration

```yaml
# Maximum file size in bytes (default: 50MB)
jar.max.size: 52428800
```

## API Endpoints

### Upload JAR File
```
POST /api/jar-files
Content-Type: multipart/form-data

Parameters:
- file: JAR file to upload
- description: Optional description
```

### Get All JAR Files
```
GET /api/jar-files
Returns: List of JarFileDTO
```

### Get JAR File by ID
```
GET /api/jar-files/{id}
Returns: JarFileDTO
```

### Download JAR File
```
GET /api/jar-files/{id}/download
Returns: Binary file content
```

### Delete JAR File (Soft)
```
DELETE /api/jar-files/{id}
Effect: Marks file as inactive
```

### Permanently Delete JAR File
```
DELETE /api/jar-files/{id}/permanent
Effect: Removes from database
```

### Search JAR Files
```
GET /api/jar-files/search?q={query}
Returns: List of matching JarFileDTO
```

### Get Storage Statistics
```
GET /api/jar-files/stats
Returns: {
  "totalFiles": 25,
  "totalSizeBytes": 104857600,
  "totalSizeMB": 100.0
}
```

## Integration with Plugin System

The JAR File Management System integrates with the plugin system through the `adapter_plugins` table:

```sql
-- adapter_plugins table references jar_files
jar_file_id UUID REFERENCES jar_files(id)
```

When uploading plugins:
1. JAR file is first stored via JAR File Management
2. Plugin metadata is extracted and validated
3. Plugin is registered with reference to JAR file

## Best Practices

1. **Version Management**: Use semantic versioning in filenames (e.g., `plugin-1.2.3.jar`)
2. **Description**: Provide clear descriptions for easier searching
3. **Cleanup**: Periodically remove inactive JAR files to save space
4. **Monitoring**: Monitor storage statistics to prevent database bloat

## Error Handling

Common errors and their meanings:

- **"File is empty"**: Uploaded file has no content
- **"File must be a JAR file"**: File extension is not .jar
- **"File is not a valid JAR file"**: Failed magic byte validation
- **"JAR file with same content already exists"**: Duplicate checksum detected
- **"File size exceeds maximum allowed size"**: File too large

## Future Enhancements

1. **Virus Scanning**: Integration with antivirus for uploaded files
2. **Compression**: Compress stored JAR content to save space
3. **Versioning**: Track multiple versions of the same plugin
4. **Dependencies**: Track JAR file dependencies
5. **Signing**: Support for signed JAR verification