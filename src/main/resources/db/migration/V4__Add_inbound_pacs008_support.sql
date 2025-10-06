-- Migration: Add inbound PACS.008 support
-- Version: V4
-- Description: Add support for inbound PACS.008 messages with SQS queue integration

-- Add new columns to payment_transactions_live table
ALTER TABLE payment_transactions_live 
ADD COLUMN direction VARCHAR(20) DEFAULT 'OUTBOUND',
ADD COLUMN queue_status VARCHAR(50),
ADD COLUMN queued_at TIMESTAMP,
ADD COLUMN processed_at TIMESTAMP,
ADD COLUMN response_sent_at TIMESTAMP,
ADD COLUMN response_message_id VARCHAR(100),
ADD COLUMN received_at TIMESTAMP,
ADD COLUMN sender_bank_code VARCHAR(50),
ADD COLUMN receiver_bank_code VARCHAR(50),
ADD COLUMN sender_account_number VARCHAR(50),
ADD COLUMN receiver_account_number VARCHAR(50);

-- Create indexes for performance
CREATE INDEX idx_payment_live_direction ON payment_transactions_live(direction);
CREATE INDEX idx_payment_live_queue_status ON payment_transactions_live(queue_status);
CREATE INDEX idx_payment_live_queued_at ON payment_transactions_live(queued_at);
CREATE INDEX idx_payment_live_response_sent_at ON payment_transactions_live(response_sent_at);
CREATE INDEX idx_payment_live_received_at ON payment_transactions_live(received_at);
CREATE INDEX idx_payment_live_sender_bank ON payment_transactions_live(sender_bank_code);
CREATE INDEX idx_payment_live_receiver_bank ON payment_transactions_live(receiver_bank_code);

-- Create inbound_client_subscriptions table
CREATE TABLE inbound_client_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    notes VARCHAR(500)
);

-- Create indexes for inbound_client_subscriptions
CREATE INDEX idx_inbound_sub_client_id ON inbound_client_subscriptions(client_id);
CREATE INDEX idx_inbound_sub_message_type ON inbound_client_subscriptions(message_type);
CREATE INDEX idx_inbound_sub_active ON inbound_client_subscriptions(active);
CREATE INDEX idx_inbound_sub_created_at ON inbound_client_subscriptions(created_at);
CREATE UNIQUE INDEX idx_inbound_sub_active_message_type ON inbound_client_subscriptions(message_type, active) WHERE active = true;

-- Add comments for documentation
COMMENT ON TABLE inbound_client_subscriptions IS 'Manages client subscriptions for inbound message notifications';
COMMENT ON COLUMN inbound_client_subscriptions.client_id IS 'Client ID that will receive notifications';
COMMENT ON COLUMN inbound_client_subscriptions.message_type IS 'Type of message (INBOUND_PACS008, INBOUND_ACMT023, etc.)';
COMMENT ON COLUMN inbound_client_subscriptions.active IS 'Whether the subscription is currently active';
COMMENT ON COLUMN inbound_client_subscriptions.notes IS 'Optional notes about the subscription';

COMMENT ON COLUMN payment_transactions_live.direction IS 'Transaction direction: INBOUND or OUTBOUND';
COMMENT ON COLUMN payment_transactions_live.queue_status IS 'Status in the processing queue: QUEUED, PROCESSING, COMPLETED, ERROR';
COMMENT ON COLUMN payment_transactions_live.queued_at IS 'When the transaction was queued for processing';
COMMENT ON COLUMN payment_transactions_live.processed_at IS 'When the transaction was processed';
COMMENT ON COLUMN payment_transactions_live.response_sent_at IS 'When the PACS.002 response was sent to NIBSS';
COMMENT ON COLUMN payment_transactions_live.response_message_id IS 'Message ID of the PACS.002 response sent to NIBSS';
COMMENT ON COLUMN payment_transactions_live.received_at IS 'When the inbound message was received from NIBSS';
COMMENT ON COLUMN payment_transactions_live.sender_bank_code IS 'Bank code of the sender (for inbound transactions)';
COMMENT ON COLUMN payment_transactions_live.receiver_bank_code IS 'Bank code of the receiver (for inbound transactions)';
COMMENT ON COLUMN payment_transactions_live.sender_account_number IS 'Account number of the sender (for inbound transactions)';
COMMENT ON COLUMN payment_transactions_live.receiver_account_number IS 'Account number of the receiver (for inbound transactions)';
