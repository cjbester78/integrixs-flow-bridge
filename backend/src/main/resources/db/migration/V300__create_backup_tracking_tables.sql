-- Create backup tracking tables
CREATE TABLE IF NOT EXISTS backup_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    backup_type VARCHAR(50) NOT NULL, -- database, files, config
    backup_name VARCHAR(255) NOT NULL,
    backup_path VARCHAR(500) NOT NULL,
    backup_size BIGINT NOT NULL,
    duration_ms BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- SUCCESS, FAILED, IN_PROGRESS
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    metadata JSONB
);

CREATE INDEX idx_backup_records_type ON backup_records(backup_type);
CREATE INDEX idx_backup_records_created_at ON backup_records(created_at);
CREATE INDEX idx_backup_records_status ON backup_records(status);

-- Create disaster recovery test results table
CREATE TABLE IF NOT EXISTS dr_test_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_type VARCHAR(100) NOT NULL, -- backup_restore, failover, full_recovery
    test_status VARCHAR(50) NOT NULL, -- PASSED, FAILED, PARTIAL
    duration_seconds INTEGER NOT NULL,
    test_details JSONB NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

CREATE INDEX idx_dr_test_results_type ON dr_test_results(test_type);
CREATE INDEX idx_dr_test_results_performed_at ON dr_test_results(performed_at);

-- Create emergency contacts table
CREATE TABLE IF NOT EXISTS emergency_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    availability VARCHAR(100), -- 24/7, Business hours, On-call
    priority INTEGER NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE INDEX idx_emergency_contacts_role ON emergency_contacts(role);
CREATE INDEX idx_emergency_contacts_priority ON emergency_contacts(priority);

-- Create recovery procedures table
CREATE TABLE IF NOT EXISTS recovery_procedures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    procedure_name VARCHAR(255) NOT NULL,
    procedure_type VARCHAR(100) NOT NULL, -- database, application, network, security
    severity_level VARCHAR(50) NOT NULL, -- CRITICAL, HIGH, MEDIUM, LOW
    procedure_steps JSONB NOT NULL,
    estimated_duration_minutes INTEGER,
    dependencies JSONB,
    version INTEGER NOT NULL DEFAULT 1,
    is_current BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE INDEX idx_recovery_procedures_type ON recovery_procedures(procedure_type);
CREATE INDEX idx_recovery_procedures_severity ON recovery_procedures(severity_level);
CREATE INDEX idx_recovery_procedures_current ON recovery_procedures(is_current);

-- Create system health metrics table for DR monitoring
CREATE TABLE IF NOT EXISTS dr_health_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_type VARCHAR(100) NOT NULL, -- database_lag, backup_age, service_health
    metric_name VARCHAR(255) NOT NULL,
    metric_value NUMERIC NOT NULL,
    metric_unit VARCHAR(50), -- seconds, bytes, percentage
    threshold_warning NUMERIC,
    threshold_critical NUMERIC,
    is_healthy BOOLEAN NOT NULL,
    measured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

CREATE INDEX idx_dr_health_metrics_type ON dr_health_metrics(metric_type);
CREATE INDEX idx_dr_health_metrics_measured_at ON dr_health_metrics(measured_at);
CREATE INDEX idx_dr_health_metrics_healthy ON dr_health_metrics(is_healthy);

-- Create DR incident log table
CREATE TABLE IF NOT EXISTS dr_incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_type VARCHAR(100) NOT NULL,
    severity VARCHAR(50) NOT NULL, -- CRITICAL, HIGH, MEDIUM, LOW
    description TEXT NOT NULL,
    impact_assessment TEXT,
    root_cause TEXT,
    resolution_steps TEXT,
    data_loss BOOLEAN NOT NULL DEFAULT false,
    downtime_minutes INTEGER,
    affected_systems JSONB,
    started_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    reported_by VARCHAR(100) NOT NULL,
    resolved_by VARCHAR(100),
    post_mortem_scheduled BOOLEAN NOT NULL DEFAULT false,
    lessons_learned TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dr_incidents_type ON dr_incidents(incident_type);
CREATE INDEX idx_dr_incidents_severity ON dr_incidents(severity);
CREATE INDEX idx_dr_incidents_started_at ON dr_incidents(started_at);
CREATE INDEX idx_dr_incidents_resolved ON dr_incidents(resolved_at);

-- Insert default emergency contacts
INSERT INTO emergency_contacts (name, role, phone, email, availability, priority) VALUES
('John Smith', 'DR Coordinator', '+1-555-0100', 'john.smith@integrixs.com', '24/7', 1),
('Jane Doe', 'Database Admin', '+1-555-0101', 'jane.doe@integrixs.com', '24/7', 2),
('Bob Wilson', 'System Admin', '+1-555-0102', 'bob.wilson@integrixs.com', 'Business hours', 3),
('Alice Brown', 'Security Lead', '+1-555-0103', 'alice.brown@integrixs.com', '24/7', 4);

-- Insert default recovery procedures
INSERT INTO recovery_procedures (procedure_name, procedure_type, severity_level, procedure_steps, estimated_duration_minutes, created_by) VALUES
('Database Failover', 'database', 'CRITICAL', 
'[{"step": 1, "action": "Verify primary database is down", "command": "pg_isready -h primary-db"},
  {"step": 2, "action": "Check replication lag on standby", "command": "psql -h standby-db -c \"SELECT pg_last_wal_replay_lsn();\""},
  {"step": 3, "action": "Promote standby to primary", "command": "pg_ctl promote -D /data/postgresql"},
  {"step": 4, "action": "Update application configuration", "command": "ansible all -m lineinfile -a \"path=/etc/integrixs/database.conf regexp=^host= line=host=standby-db\""},
  {"step": 5, "action": "Restart application services", "command": "systemctl restart integrixs-backend"},
  {"step": 6, "action": "Verify application connectivity", "command": "curl -f http://localhost:8080/health"}]'::jsonb,
15, 'system'),

('Application Service Recovery', 'application', 'HIGH',
'[{"step": 1, "action": "Check service status", "command": "systemctl status integrixs-backend"},
  {"step": 2, "action": "Review error logs", "command": "tail -n 100 /var/log/integrixs/backend.log"},
  {"step": 3, "action": "Restart service", "command": "systemctl restart integrixs-backend"},
  {"step": 4, "action": "Verify health endpoint", "command": "curl -f http://localhost:8080/health"},
  {"step": 5, "action": "Check dependent services", "command": "systemctl status redis rabbitmq-server postgresql"}]'::jsonb,
10, 'system'),

('Security Breach Response', 'security', 'CRITICAL',
'[{"step": 1, "action": "Isolate affected systems", "command": "iptables -A INPUT -j DROP"},
  {"step": 2, "action": "Preserve evidence", "command": "tar -czf /forensics/$(date +%Y%m%d_%H%M%S).tar.gz /var/log /tmp"},
  {"step": 3, "action": "Reset all credentials", "command": "/opt/integrixs/scripts/rotate-all-credentials.sh"},
  {"step": 4, "action": "Apply security patches", "command": "apt-get update && apt-get upgrade -y"},
  {"step": 5, "action": "Restore from clean backup", "command": "/opt/integrixs/scripts/restore-full-system.sh"},
  {"step": 6, "action": "Re-enable network with monitoring", "command": "systemctl restart fail2ban integrixs-ids"}]'::jsonb,
60, 'system');

-- Grant permissions
GRANT SELECT, INSERT, UPDATE ON backup_records TO integrixs_app;
GRANT SELECT, INSERT, UPDATE ON dr_test_results TO integrixs_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON emergency_contacts TO integrixs_app;
GRANT SELECT, INSERT, UPDATE ON recovery_procedures TO integrixs_app;
GRANT SELECT, INSERT ON dr_health_metrics TO integrixs_app;
GRANT SELECT, INSERT, UPDATE ON dr_incidents TO integrixs_app;

-- Create function to clean old health metrics
CREATE OR REPLACE FUNCTION cleanup_old_dr_health_metrics()
RETURNS void AS $$
BEGIN
    DELETE FROM dr_health_metrics WHERE measured_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

-- Create function to update contact modification timestamp
CREATE OR REPLACE FUNCTION update_contact_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER emergency_contacts_update_timestamp
    BEFORE UPDATE ON emergency_contacts
    FOR EACH ROW
    EXECUTE FUNCTION update_contact_timestamp();