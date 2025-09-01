-- Add Alerting System Tables

-- Create alert_rules table
CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    
    -- Condition configuration
    condition_type VARCHAR(50) NOT NULL,
    condition_expression TEXT,
    threshold_value DOUBLE PRECISION,
    threshold_operator VARCHAR(30),
    time_window_minutes INTEGER,
    occurrence_count INTEGER,
    
    -- Target configuration
    target_type VARCHAR(50),
    target_id VARCHAR(255),
    
    -- Suppression
    suppression_duration_minutes INTEGER,
    last_triggered_at TIMESTAMP,
    trigger_count INTEGER DEFAULT 0,
    
    -- Escalation
    escalation_enabled BOOLEAN DEFAULT false,
    escalation_after_minutes INTEGER,
    
    -- Metadata
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (alert_type IN ('FLOW_FAILURE', 'FLOW_SLA_BREACH', 'ADAPTER_CONNECTION_FAILURE', 
                         'ADAPTER_HEALTH_DEGRADED', 'PERFORMANCE_THRESHOLD', 'ERROR_RATE_THRESHOLD', 
                         'SYSTEM_RESOURCE', 'CUSTOM')),
    CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO')),
    CHECK (condition_type IN ('SIMPLE_THRESHOLD', 'RATE_THRESHOLD', 'PATTERN_MATCH', 
                             'CUSTOM_EXPRESSION', 'ABSENCE_DETECTION')),
    CHECK (threshold_operator IN ('GREATER_THAN', 'GREATER_THAN_OR_EQUAL', 'LESS_THAN', 
                                  'LESS_THAN_OR_EQUAL', 'EQUAL', 'NOT_EQUAL')),
    CHECK (target_type IN ('ALL', 'FLOW', 'ADAPTER', 'SYSTEM', 'BUSINESS_COMPONENT'))
);

-- Create notification_channels table
CREATE TABLE notification_channels (
    id BIGSERIAL PRIMARY KEY,
    channel_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    channel_type VARCHAR(50) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    
    -- Rate limiting
    rate_limit_per_hour INTEGER,
    last_notification_at TIMESTAMP,
    notification_count_current_hour INTEGER DEFAULT 0,
    
    -- Testing
    last_test_at TIMESTAMP,
    last_test_success BOOLEAN,
    last_test_message TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (channel_type IN ('EMAIL', 'SMS', 'WEBHOOK', 'SLACK', 'TEAMS', 'PAGERDUTY', 'CUSTOM'))
);

-- Create alerts table
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_rule_id BIGINT NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    alert_id VARCHAR(255) NOT NULL UNIQUE,
    alert_title VARCHAR(255) NOT NULL,
    alert_message TEXT,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'TRIGGERED',
    
    -- Timing
    triggered_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(255),
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255),
    resolution_notes TEXT,
    
    -- Source
    source_type VARCHAR(50),
    source_id VARCHAR(255),
    source_name VARCHAR(255),
    
    -- Notification tracking
    notification_count INTEGER DEFAULT 0,
    last_notification_at TIMESTAMP,
    
    -- Escalation
    is_escalated BOOLEAN DEFAULT false,
    escalated_at TIMESTAMP,
    
    -- Suppression
    is_suppressed BOOLEAN DEFAULT false,
    suppressed_until TIMESTAMP,
    suppression_reason TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO')),
    CHECK (status IN ('TRIGGERED', 'NOTIFIED', 'ACKNOWLEDGED', 'ESCALATED', 'RESOLVED', 'SUPPRESSED', 'EXPIRED')),
    CHECK (source_type IN ('FLOW', 'ADAPTER', 'SYSTEM', 'TRANSFORMATION', 'MONITORING'))
);

-- Create junction tables

-- Alert rule notification channels
CREATE TABLE alert_rule_channels (
    alert_rule_id BIGINT NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    channel_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (alert_rule_id, channel_id)
);

-- Alert rule escalation channels
CREATE TABLE alert_rule_escalation_channels (
    alert_rule_id BIGINT NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    channel_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (alert_rule_id, channel_id)
);

-- Alert rule tags
CREATE TABLE alert_rule_tags (
    alert_rule_id BIGINT NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (alert_rule_id, tag)
);

-- Notification channel configuration
CREATE TABLE notification_channel_config (
    channel_id BIGINT NOT NULL REFERENCES notification_channels(id) ON DELETE CASCADE,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    PRIMARY KEY (channel_id, config_key)
);

-- Alert details
CREATE TABLE alert_details (
    alert_id BIGINT NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    detail_key VARCHAR(100) NOT NULL,
    detail_value TEXT,
    PRIMARY KEY (alert_id, detail_key)
);

-- Alert notifications
CREATE TABLE alert_notifications (
    alert_id BIGINT NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    notification_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (alert_id, notification_id)
);

-- Alert escalation notifications
CREATE TABLE alert_escalation_notifications (
    alert_id BIGINT NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    notification_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (alert_id, notification_id)
);

-- Create indexes
CREATE INDEX idx_alert_rules_enabled ON alert_rules(is_enabled);
CREATE INDEX idx_alert_rules_type ON alert_rules(alert_type);
CREATE INDEX idx_alert_rules_severity ON alert_rules(severity);
CREATE INDEX idx_alert_rules_target ON alert_rules(target_type, target_id);

CREATE INDEX idx_notification_channels_enabled ON notification_channels(is_enabled);
CREATE INDEX idx_notification_channels_type ON notification_channels(channel_type);

CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_triggered_at ON alerts(triggered_at);
CREATE INDEX idx_alerts_source ON alerts(source_type, source_id);
CREATE INDEX idx_alerts_alert_id ON alerts(alert_id);

-- Insert default notification channels
INSERT INTO notification_channels (channel_name, description, channel_type, is_enabled) VALUES
('Default Email Channel', 'Default email notification channel', 'EMAIL', false),
('Default Webhook Channel', 'Default webhook notification channel', 'WEBHOOK', false);

-- Insert default alert rules
INSERT INTO alert_rules (rule_name, description, alert_type, severity, condition_type, target_type) VALUES
('Flow Failure Alert', 'Alert when any flow fails', 'FLOW_FAILURE', 'HIGH', 'SIMPLE_THRESHOLD', 'ALL'),
('Adapter Connection Failure', 'Alert when adapter connection fails', 'ADAPTER_CONNECTION_FAILURE', 'CRITICAL', 'SIMPLE_THRESHOLD', 'ALL'),
('High Error Rate', 'Alert when error rate exceeds 10%', 'ERROR_RATE_THRESHOLD', 'HIGH', 'RATE_THRESHOLD', 'ALL');

-- Update the threshold for error rate rule
UPDATE alert_rules 
SET threshold_value = 10.0, 
    threshold_operator = 'GREATER_THAN',
    time_window_minutes = 5
WHERE rule_name = 'High Error Rate';