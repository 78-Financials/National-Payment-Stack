-- Create audit_logs table for system audit logging
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(200) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(50),
    client_id VARCHAR(50),
    ip_address VARCHAR(100),
    user_agent VARCHAR(500),
    request_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    message VARCHAR(1000),
    details TEXT,
    error_code VARCHAR(100),
    error_message VARCHAR(1000),
    execution_time_ms BIGINT,
    session_id VARCHAR(100)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_log_client_id ON audit_logs(client_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_status ON audit_logs(status);
CREATE INDEX IF NOT EXISTS idx_audit_log_action_type ON audit_logs(action_type);
