-- Add receipt images support to procurement_batches table
CREATE TABLE IF NOT EXISTS procurement_receipt_images (
    id BINARY(16) PRIMARY KEY,
    batch_id BINARY(16) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    image_order INT NOT NULL DEFAULT 0,
    FOREIGN KEY (batch_id) REFERENCES procurement_batches(id) ON DELETE CASCADE,
    INDEX idx_batch_id (batch_id)
);
