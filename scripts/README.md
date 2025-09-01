# Scripts Directory

This directory contains various utility scripts for the Integrix Flow Bridge project.

## Main Deployment Script
The main deployment script `deploy.sh` is kept at the project root for easy access:
```bash
./deploy.sh  # Build and deploy the application
```

## Script Categories

### database/
Database-related SQL scripts:
- `create_test_users.sql` - Creates test users for development
- `fix_language_enum.sql` - Fixes language enum values
- `populate_builtin_functions.sql` - Populates built-in transformation functions
- `update_functions_to_java.sql` - Updates functions from JavaScript to Java
- `update_roles.sql` - Updates user roles
- `clear_logs_and_errors.sql` - Clears logs and error tables

### testing/
Testing and verification scripts:
- `test_roles.sh` - Tests role-based access control
- `test_deletion_constraints.sh` - Tests database deletion constraints

### maintenance/
Maintenance and cleanup scripts:
- `clear_logs_and_errors.sh` - Shell script to clear logs and errors
- `run_clear_logs.sh` - Wrapper script for log clearing
- `cleanup_unused_imports.sh` - Removes unused Java imports
- `find_unused_imports.sh` - Finds unused Java imports

### migration/
Migration and update scripts (mostly historical, migrations are complete):
- `update_preauthorize_annotations.sh` - Updates Spring Security annotations
- `apply_critical_migrations.sh` - Applies critical database migrations
- `BATCH_UUID_UPDATE.sh` - Batch updates IDs to UUIDs
- `UPDATE_TO_UUID.sh` - Updates database to use UUID primary keys
- `ARCHIVE_MYSQL_MIGRATIONS.sh` - Archives old MySQL migrations
- `migrate_mysql_to_postgresql.sh` - MySQL to PostgreSQL migration script

## Usage Examples

### Clear logs and errors:
```bash
./scripts/maintenance/clear_logs_and_errors.sh
```

### Create test users:
```bash
psql -U integrix -d integrixflowbridge -f scripts/database/create_test_users.sql
```

### Test role permissions:
```bash
./scripts/testing/test_roles.sh
```

## Note
Many scripts in the migration/ folder are historical and were used during the project's migration from MySQL to PostgreSQL and the conversion to UUID primary keys. They are kept for reference but are no longer needed for normal operation.