-- Create audit events table
CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    event_timestamp TIMESTAMP NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_category VARCHAR(50) NOT NULL,
    username VARCHAR(100),
    user_id UUID,
    tenant_id VARCHAR(50),
    session_id VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    request_id VARCHAR(100),
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    entity_name VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000),
    duration_ms BIGINT,
    old_value TEXT,
    new_value TEXT,
    correlation_id VARCHAR(100),
    service_name VARCHAR(50),
    environment VARCHAR(20),
    api_endpoint VARCHAR(255),
    http_method VARCHAR(10),
    http_status INTEGER
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_timestamp ON audit_events(event_timestamp DESC);
CREATE INDEX idx_audit_user ON audit_events(username);
CREATE INDEX idx_audit_type ON audit_events(event_type);
CREATE INDEX idx_audit_entity ON audit_events(entity_type, entity_id);
CREATE INDEX idx_audit_tenant ON audit_events(tenant_id);
CREATE INDEX idx_audit_correlation ON audit_events(correlation_id);
CREATE INDEX idx_audit_outcome ON audit_events(outcome);
CREATE INDEX idx_audit_category ON audit_events(event_category);

-- Create audit event details table
CREATE TABLE audit_event_details (
    audit_event_id UUID NOT NULL,
    detail_key VARCHAR(100) NOT NULL,
    detail_value VARCHAR(1000),
    PRIMARY KEY (audit_event_id, detail_key),
    FOREIGN KEY (audit_event_id) REFERENCES audit_events(id) ON DELETE CASCADE
);

-- Create index for details
CREATE INDEX idx_audit_details_event ON audit_event_details(audit_event_id);

-- Create audit retention policy table
CREATE TABLE audit_retention_policies (
    id UUID PRIMARY KEY,
    category VARCHAR(50) NOT NULL UNIQUE,
    retention_days INTEGER NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default retention policies
INSERT INTO audit_retention_policies (id, category, retention_days) VALUES
    (gen_random_uuid(), 'AUTHENTICATION', 365),
    (gen_random_uuid(), 'AUTHORIZATION', 365),
    (gen_random_uuid(), 'DATA_ACCESS', 180),
    (gen_random_uuid(), 'CONFIGURATION', 365),
    (gen_random_uuid(), 'SYSTEM', 90),
    (gen_random_uuid(), 'SECURITY', 730),
    (gen_random_uuid(), 'INTEGRATION', 90),
    (gen_random_uuid(), 'ADMINISTRATION', 365),
    (gen_random_uuid(), 'JOB_EXECUTION', 30);

-- Create function to automatically clean old audit events
CREATE OR REPLACE FUNCTION cleanup_old_audit_events() RETURNS void AS $$
DECLARE
    policy RECORD;
BEGIN
    FOR policy IN SELECT * FROM audit_retention_policies WHERE enabled = true
    LOOP
        DELETE FROM audit_events
        WHERE event_category = policy.category
        AND event_timestamp < CURRENT_TIMESTAMP - INTERVAL '1 day' * policy.retention_days;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Create view for security events
CREATE VIEW v_security_audit_events AS
SELECT *
FROM audit_events
WHERE category = 'SECURITY'
   OR event_type IN ('LOGIN_FAILURE', 'ACCESS_DENIED', 'RATE_LIMIT_EXCEEDED',
                     'SUSPICIOUS_ACTIVITY', 'SECURITY_ALERT')
ORDER BY event_timestamp DESC;

-- Create view for recent failed logins
CREATE VIEW v_recent_failed_logins AS
SELECT 
    ip_address,
    username,
    COUNT(*) as failure_count,
    MAX(event_timestamp) as last_attempt
FROM audit_events
WHERE event_type = 'LOGIN_FAILURE'
  AND event_timestamp >= CURRENT_TIMESTAMP - INTERVAL '1 hour'
GROUP BY ip_address, username
HAVING COUNT(*) > 3
ORDER BY failure_count DESC;

-- Create materialized view for daily statistics
CREATE MATERIALIZED VIEW mv_daily_audit_statistics AS
SELECT 
    DATE_TRUNC('day', event_timestamp) as audit_date,
    event_category,
    event_type,
    outcome,
    COUNT(*) as event_count,
    AVG(duration_ms) as avg_duration_ms
FROM audit_events
GROUP BY DATE_TRUNC('day', event_timestamp), event_category, event_type, outcome;

-- Create index on materialized view
CREATE INDEX idx_mv_audit_stats_date ON mv_daily_audit_statistics(audit_date);

-- Create function to refresh materialized view
CREATE OR REPLACE FUNCTION refresh_audit_statistics() RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_audit_statistics;
END;
$$ LANGUAGE plpgsql;