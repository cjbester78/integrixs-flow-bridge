-- Add tenant_id column to communication_adapters table for multi-tenancy support
ALTER TABLE communication_adapters 
ADD COLUMN IF NOT EXISTS tenant_id UUID;

-- Add index for tenant_id for better query performance
CREATE INDEX IF NOT EXISTS idx_communication_adapters_tenant_id 
ON communication_adapters(tenant_id);