-- Email verification for users
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verificado BOOLEAN DEFAULT false;

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token     VARCHAR(255) NOT NULL UNIQUE,
    expira_em TIMESTAMPTZ NOT NULL,
    usado     BOOLEAN DEFAULT false,
    criado_em TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_email_ver_token   ON email_verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_email_ver_user_id ON email_verification_tokens(user_id);
