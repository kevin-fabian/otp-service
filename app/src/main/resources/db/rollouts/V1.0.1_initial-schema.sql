CREATE TABLE IF NOT EXISTS otp_transactions (
    id UUID PRIMARY KEY,
    otp_code VARCHAR NOT NULL,
    recipient VARCHAR NOT NULL,
    purpose VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    delivery_method VARCHAR NOT NULL,
    attempt_count INTEGER NOT NULL,
    metadata TEXT
);

CREATE INDEX IF NOT EXISTS otp_transactions_otp_code_idx ON otp_transactions (otp_code);
CREATE INDEX IF NOT EXISTS otp_transactions_recipient_idx ON otp_transactions (recipient);