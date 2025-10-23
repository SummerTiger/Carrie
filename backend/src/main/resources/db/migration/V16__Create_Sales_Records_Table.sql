-- Create sales_records table
CREATE TABLE sales_records (
    id BINARY(16) PRIMARY KEY,
    settlement_date DATE NOT NULL,
    source VARCHAR(50) NOT NULL COMMENT 'CSV or EXCEL',
    number_of_batches INT,
    number_completed INT,
    number_sur INT,
    number_incomplete INT,
    approved_amount DECIMAL(12, 2) DEFAULT 0.00,
    fee_amount DECIMAL(12, 2) DEFAULT 0.00,
    file_name VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_settlement_date (settlement_date),
    INDEX idx_source (source),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
