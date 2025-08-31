CREATE TABLE IF NOT EXISTS totp_users
(
    id UUID PRIMARY KEY,
    user_reference_id VARCHAR(255) NOT NULL UNIQUE,
    secret VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS totp_users_user_reference_id_uidx ON totp_users (user_reference_id);
