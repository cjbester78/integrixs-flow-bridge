-- Add AMQP adapter type to the enum
ALTER TYPE adapter_type ADD VALUE IF NOT EXISTS 'AMQP' AFTER 'RABBITMQ';