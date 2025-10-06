-- V3: Create Alert System Tables
-- This migration creates all tables needed for the real-time alerting system

-- Alert Rules Table
CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    condition_expression VARCHAR(1000) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('CRITICAL', 'WARNING', 'INFO')),
    escalation_policy VARCHAR(200),
    enabled BOOLEAN NOT NULL DEFAULT true,
    evaluation_interval_seconds INTEGER NOT NULL DEFAULT 60,
    suppression_window_seconds INTEGER NOT NULL DEFAULT 300,
    max_alerts_per_hour INTEGER DEFAULT 10,
    metric_type VARCHAR(100),
    metric_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- Alert Rule Notification Channels Table
CREATE TABLE alert_rule_channels (
    alert_rule_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    PRIMARY KEY (alert_rule_id, channel),
    FOREIGN KEY (alert_rule_id) REFERENCES alert_rules(id) ON DELETE CASCADE
);

-- Alerts Table
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_rule_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    message VARCHAR(1000),
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('CRITICAL', 'WARNING', 'INFO')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'SUPPRESSED', 'EXPIRED')),
    metric_value DECIMAL(20,4),
    threshold_value DECIMAL(20,4),
    metric_name VARCHAR(100),
    metric_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(100),
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    last_notification_sent TIMESTAMP,
    notification_count INTEGER NOT NULL DEFAULT 0,
    escalated_at TIMESTAMP,
    context VARCHAR(1000),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_rule_id) REFERENCES alert_rules(id)
);

-- Alert Notification Channels Table
CREATE TABLE alert_channels (
    alert_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    PRIMARY KEY (alert_id, channel),
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE
);

-- Alert History Table
CREATE TABLE alert_history (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATED', 'ACKNOWLEDGED', 'RESOLVED', 'SUPPRESSED', 'ESCALATED', 'NOTIFICATION_SENT', 'NOTIFICATION_FAILED', 'EXPIRED', 'REACTIVATED')),
    old_status VARCHAR(20) CHECK (old_status IN ('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'SUPPRESSED', 'EXPIRED')),
    new_status VARCHAR(20) CHECK (new_status IN ('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED', 'SUPPRESSED', 'EXPIRED')),
    user_id VARCHAR(100),
    details VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE
);

-- Indexes for Performance
CREATE INDEX idx_alert_rules_enabled ON alert_rules(enabled);
CREATE INDEX idx_alert_rules_severity ON alert_rules(severity);
CREATE INDEX idx_alert_rules_created ON alert_rules(created_at);
CREATE INDEX idx_alert_rules_metric_type ON alert_rules(metric_type);
CREATE INDEX idx_alert_rules_metric_name ON alert_rules(metric_name);

CREATE INDEX idx_alerts_rule_id ON alerts(alert_rule_id);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_created ON alerts(created_at);
CREATE INDEX idx_alerts_rule_status ON alerts(alert_rule_id, status);
CREATE INDEX idx_alerts_metric_name ON alerts(metric_name);
CREATE INDEX idx_alerts_metric_type ON alerts(metric_type);
CREATE INDEX idx_alerts_metric_name_type ON alerts(metric_name, metric_type);
CREATE INDEX idx_alerts_acknowledged ON alerts(acknowledged_at);
CREATE INDEX idx_alerts_resolved ON alerts(resolved_at);

CREATE INDEX idx_alert_history_alert_id ON alert_history(alert_id);
CREATE INDEX idx_alert_history_action ON alert_history(action);
CREATE INDEX idx_alert_history_created ON alert_history(created_at);
CREATE INDEX idx_alert_history_user_id ON alert_history(user_id);

-- Triggers for Updated Timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_alert_rules_updated_at BEFORE UPDATE ON alert_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alerts_updated_at BEFORE UPDATE ON alerts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert Default Alert Rules
INSERT INTO alert_rules (name, description, condition_expression, severity, escalation_policy, metric_type, metric_name, created_by) VALUES
('High Transaction Failure Rate', 'Alert when transaction success rate drops below 90%', 'transaction_metrics.success_rate < 90', 'CRITICAL', 'immediate', 'transaction', 'success_rate', 'SYSTEM'),
('High Processing Time', 'Alert when average processing time exceeds 10 seconds', 'transaction_metrics.avg_processing_time_seconds > 10', 'WARNING', '30m', 'transaction', 'avg_processing_time_seconds', 'SYSTEM'),
('High CPU Usage', 'Alert when CPU usage exceeds 80%', 'system_metrics.cpu_usage > 80', 'WARNING', '15m', 'system', 'cpu_usage', 'SYSTEM'),
('High Memory Usage', 'Alert when memory usage exceeds 85%', 'system_metrics.memory_usage > 85', 'WARNING', '15m', 'system', 'memory_usage', 'SYSTEM'),
('Low Transaction Volume', 'Alert when transaction volume drops below normal levels', 'transaction_metrics.total_transactions < 50', 'INFO', 'none', 'transaction', 'total_transactions', 'SYSTEM'),
('Bank Performance Degradation', 'Alert when overall bank success rate drops below 95%', 'bank_metrics.overall_success_rate < 95', 'WARNING', '15m', 'bank', 'overall_success_rate', 'SYSTEM');

-- Set notification channels for default rules
INSERT INTO alert_rule_channels (alert_rule_id, channel) VALUES
(1, 'email'), (1, 'slack'), (1, 'sms'),
(2, 'email'), (2, 'slack'),
(3, 'email'), (3, 'slack'),
(4, 'email'), (4, 'slack'),
(5, 'email'),
(6, 'slack');

-- Comments for Documentation
COMMENT ON TABLE alert_rules IS 'Configuration for alert rules and conditions';
COMMENT ON TABLE alerts IS 'Individual alert instances triggered by rules';
COMMENT ON TABLE alert_history IS 'Audit trail of all alert actions and status changes';
COMMENT ON TABLE alert_rule_channels IS 'Notification channels configured for each alert rule';
COMMENT ON TABLE alert_channels IS 'Notification channels for individual alerts';

COMMENT ON COLUMN alert_rules.condition_expression IS 'Expression to evaluate (e.g., transaction_metrics.success_rate < 90)';
COMMENT ON COLUMN alert_rules.evaluation_interval_seconds IS 'How often to check this rule';
COMMENT ON COLUMN alert_rules.suppression_window_seconds IS 'How long to suppress duplicate alerts';
COMMENT ON COLUMN alerts.metric_value IS 'Actual metric value that triggered the alert';
COMMENT ON COLUMN alerts.threshold_value IS 'Threshold value that was exceeded';
COMMENT ON COLUMN alerts.context IS 'JSON context with additional alert information';
