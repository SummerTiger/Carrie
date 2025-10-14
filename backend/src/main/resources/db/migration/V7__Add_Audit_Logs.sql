-- Create audit_logs table
CREATE TABLE audit_logs (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NULL,
    username VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NULL,
    resource_id VARCHAR(100) NULL,
    details VARCHAR(2000) NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message VARCHAR(1000) NULL,
    timestamp DATETIME NOT NULL,
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_username ON audit_logs(username);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_ip ON audit_logs(ip_address);
CREATE INDEX idx_audit_status ON audit_logs(status);
