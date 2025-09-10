-- Migration to rename JMS adapter type to IBMMQ
-- This migration updates all references to JMS adapters to use IBMMQ instead
-- IBM MQ (formerly WebSphere MQ) is the actual product being used

-- Update adapter_type table if it exists
UPDATE adapter_type 
SET code = 'IBMMQ', 
    name = 'IBM MQ',
    description = 'IBM MQ (formerly WebSphere MQ) messaging adapter'
WHERE code = 'JMS';

-- Update communication_adapter table
UPDATE communication_adapter 
SET type = 'IBMMQ' 
WHERE type = 'JMS';

-- Update adapter_plugin table if it has adapter type references
UPDATE adapter_plugin 
SET adapter_type = 'IBMMQ' 
WHERE adapter_type = 'JMS';

-- Update any configuration that might reference JMS
UPDATE communication_adapter 
SET configuration = REPLACE(configuration, '"adapterType":"JMS"', '"adapterType":"IBMMQ"')
WHERE configuration LIKE '%"adapterType":"JMS"%';

-- Update plugin metadata that might reference JMS
UPDATE adapter_plugin 
SET metadata = REPLACE(metadata::text, '"adapterType":"JMS"', '"adapterType":"IBMMQ"')::jsonb
WHERE metadata::text LIKE '%"adapterType":"JMS"%';

-- Update system logs that reference JMS adapter type
UPDATE system_log 
SET message = REPLACE(message, 'JMS adapter', 'IBM MQ adapter'),
    details = REPLACE(details, 'JMS adapter', 'IBM MQ adapter')
WHERE message LIKE '%JMS adapter%' OR details LIKE '%JMS adapter%';

-- Add comment for documentation
COMMENT ON COLUMN communication_adapter.type IS 'Adapter type - supports HTTP, REST, SOAP, JDBC, FILE, FTP, SFTP, IBMMQ (formerly JMS), KAFKA, etc.';