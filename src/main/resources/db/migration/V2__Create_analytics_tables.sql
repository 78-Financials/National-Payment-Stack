-- Create payment transaction live table
CREATE TABLE IF NOT EXISTS payment_transactions_live (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    original_message_id VARCHAR(100) NOT NULL,
    client_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    debtor_bank VARCHAR(50),
    creditor_bank VARCHAR(50),
    debtor_account VARCHAR(50),
    creditor_account VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    response_code VARCHAR(10),
    response_message TEXT,
    processing_time_ms BIGINT,
    request_created_at TIMESTAMP NOT NULL,
    response_received_at TIMESTAMP,
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_category VARCHAR(50),
    error_details VARCHAR(1000),
    
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_retry_count_non_negative CHECK (retry_count >= 0)
);

-- Create payment transaction history table
CREATE TABLE IF NOT EXISTS payment_transactions_history (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    original_message_id VARCHAR(100) NOT NULL,
    client_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    debtor_bank VARCHAR(50),
    creditor_bank VARCHAR(50),
    debtor_account VARCHAR(50),
    creditor_account VARCHAR(50),
    final_status VARCHAR(20) NOT NULL,
    response_code VARCHAR(10),
    response_message TEXT,
    processing_time_ms BIGINT,
    request_created_at TIMESTAMP NOT NULL,
    response_received_at TIMESTAMP,
    completed_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_category VARCHAR(50),
    error_details VARCHAR(1000),
    
    CONSTRAINT chk_history_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_history_retry_count_non_negative CHECK (retry_count >= 0)
);

-- Create bank performance daily table
CREATE TABLE IF NOT EXISTS bank_performance_daily (
    id BIGSERIAL PRIMARY KEY,
    bank_code VARCHAR(10) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    total_requests INTEGER NOT NULL DEFAULT 0,
    successful_requests INTEGER NOT NULL DEFAULT 0,
    failed_requests INTEGER NOT NULL DEFAULT 0,
    timeout_requests INTEGER NOT NULL DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    successful_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    avg_processing_time_ms BIGINT NOT NULL DEFAULT 0,
    min_processing_time_ms BIGINT,
    max_processing_time_ms BIGINT,
    success_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    avg_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_bank_date UNIQUE (bank_code, date),
    CONSTRAINT chk_performance_amounts_non_negative CHECK (total_amount >= 0 AND successful_amount >= 0),
    CONSTRAINT chk_performance_counts_non_negative CHECK (
        total_requests >= 0 AND successful_requests >= 0 AND 
        failed_requests >= 0 AND timeout_requests >= 0
    ),
    CONSTRAINT chk_performance_rates_valid CHECK (
        success_rate >= 0 AND success_rate <= 100
    )
);

-- Create indexes for payment_transactions_live
CREATE INDEX IF NOT EXISTS idx_live_original_message_id ON payment_transactions_live(original_message_id);
CREATE INDEX IF NOT EXISTS idx_live_transaction_id ON payment_transactions_live(transaction_id);
CREATE INDEX IF NOT EXISTS idx_live_client_id ON payment_transactions_live(client_id);
CREATE INDEX IF NOT EXISTS idx_live_status ON payment_transactions_live(status);
CREATE INDEX IF NOT EXISTS idx_live_created_at ON payment_transactions_live(request_created_at);
CREATE INDEX IF NOT EXISTS idx_live_client_status ON payment_transactions_live(client_id, status);
CREATE INDEX IF NOT EXISTS idx_live_bank_status ON payment_transactions_live(creditor_bank, status);

-- Create indexes for payment_transactions_history
CREATE INDEX IF NOT EXISTS idx_history_original_message_id ON payment_transactions_history(original_message_id);
CREATE INDEX IF NOT EXISTS idx_history_transaction_id ON payment_transactions_history(transaction_id);
CREATE INDEX IF NOT EXISTS idx_history_client_id ON payment_transactions_history(client_id);
CREATE INDEX IF NOT EXISTS idx_history_final_status ON payment_transactions_history(final_status);
CREATE INDEX IF NOT EXISTS idx_history_created_at ON payment_transactions_history(request_created_at);
CREATE INDEX IF NOT EXISTS idx_history_archived_at ON payment_transactions_history(archived_at);
CREATE INDEX IF NOT EXISTS idx_history_bank_date ON payment_transactions_history(creditor_bank, request_created_at);
CREATE INDEX IF NOT EXISTS idx_history_client_date ON payment_transactions_history(client_id, request_created_at);

-- Create indexes for bank_performance_daily
CREATE INDEX IF NOT EXISTS idx_bank_perf_date ON bank_performance_daily(date);
CREATE INDEX IF NOT EXISTS idx_bank_perf_bank ON bank_performance_daily(bank_code);
CREATE INDEX IF NOT EXISTS idx_bank_perf_success_rate ON bank_performance_daily(success_rate);
CREATE INDEX IF NOT EXISTS idx_bank_perf_avg_time ON bank_performance_daily(avg_processing_time_ms);

-- Add comments for documentation
COMMENT ON TABLE payment_transactions_live IS 'Live payment transactions for real-time monitoring (current day only)';
COMMENT ON TABLE payment_transactions_history IS 'Historical payment transactions for analytics and reporting';
COMMENT ON TABLE bank_performance_daily IS 'Daily aggregated bank performance metrics';
COMMENT ON TABLE distributed_locks IS 'Distributed locks for Kubernetes pod coordination';
