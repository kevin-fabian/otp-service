CREATE TABLE IF NOT EXISTS otps (
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

CREATE INDEX IF NOT EXISTS otps_otp_code_idx ON otps (otp_code);
CREATE INDEX IF NOT EXISTS otps_recipient_idx ON otps (recipient);