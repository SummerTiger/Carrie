-- Add security fields to users table
ALTER TABLE users
ADD COLUMN failed_login_attempts INT DEFAULT 0,
ADD COLUMN account_locked_until DATETIME NULL,
ADD COLUMN password_changed_at DATETIME NULL;

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BINARY(16) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BINARY(16) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens(expires_at);
