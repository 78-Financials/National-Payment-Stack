-- Create distributed_locks table for Kubernetes pod coordination
CREATE TABLE IF NOT EXISTS distributed_locks (
    id BIGSERIAL PRIMARY KEY,
    lock_name VARCHAR(100) NOT NULL UNIQUE,
    pod_id VARCHAR(200) NOT NULL,
    application_name VARCHAR(100) NOT NULL,
    acquired_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    lock_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_expires_after_acquired CHECK (expires_at > acquired_at)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_distributed_locks_name ON distributed_locks(lock_name);
CREATE INDEX IF NOT EXISTS idx_distributed_locks_expires ON distributed_locks(expires_at);
CREATE INDEX IF NOT EXISTS idx_distributed_locks_pod ON distributed_locks(pod_id);
CREATE INDEX IF NOT EXISTS idx_distributed_locks_app ON distributed_locks(application_name);

-- Create composite index for cleanup operations
CREATE INDEX IF NOT EXISTS idx_distributed_locks_cleanup ON distributed_locks(lock_name, expires_at);
