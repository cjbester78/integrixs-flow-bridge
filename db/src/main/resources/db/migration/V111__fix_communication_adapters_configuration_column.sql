-- Rename connection_config to configuration and change type to JSON
ALTER TABLE communication_adapters 
RENAME COLUMN connection_config TO configuration;

ALTER TABLE communication_adapters 
ALTER COLUMN configuration TYPE JSON 
USING configuration::JSON;

-- Also need to fix some other column mismatches
-- The entity has 'type' but database has 'adapter_type'
ALTER TABLE communication_adapters 
RENAME COLUMN adapter_type TO type;

-- The entity has 'is_active' but database has 'status'
ALTER TABLE communication_adapters 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- Update is_active based on status if status exists
UPDATE communication_adapters 
SET is_active = CASE 
    WHEN status = 'ACTIVE' THEN TRUE 
    ELSE FALSE 
END
WHERE status IS NOT NULL;

-- Drop the old status column
ALTER TABLE communication_adapters 
DROP COLUMN IF EXISTS status;

-- Drop is_deployed and deployment_info as they're not in the entity
ALTER TABLE communication_adapters 
DROP COLUMN IF EXISTS is_deployed;

ALTER TABLE communication_adapters 
DROP COLUMN IF EXISTS deployment_info;

-- Add mode column if it doesn't exist
ALTER TABLE communication_adapters 
ADD COLUMN IF NOT EXISTS mode VARCHAR(10) NOT NULL DEFAULT 'INBOUND' 
CHECK (mode IN ('INBOUND', 'OUTBOUND'));

-- Update mode based on direction
UPDATE communication_adapters 
SET mode = direction 
WHERE direction IN ('INBOUND', 'OUTBOUND');

-- For adapters with INBOUND direction, set mode to OUTBOUND
UPDATE communication_adapters 
SET mode = 'OUTBOUND' 
WHERE direction = 'INBOUND';

-- For adapters with OUTBOUND direction, set mode to INBOUND
UPDATE communication_adapters 
SET mode = 'INBOUND' 
WHERE direction = 'OUTBOUND';

-- Add description column if it doesn't exist
ALTER TABLE communication_adapters 
ADD COLUMN IF NOT EXISTS description TEXT;