-- Add health monitoring columns to communication_adapters
ALTER TABLE communication_adapters 
ADD COLUMN IF NOT EXISTS is_healthy BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS last_health_check TIMESTAMP;

-- Create index for health monitoring queries
CREATE INDEX IF NOT EXISTS idx_adapters_health_status ON communication_adapters(is_healthy, last_health_check);

-- Add comments for documentation
COMMENT ON COLUMN communication_adapters.is_healthy IS 'Current health status of the adapter';
COMMENT ON COLUMN communication_adapters.last_health_check IS 'Timestamp of the last health check performed';