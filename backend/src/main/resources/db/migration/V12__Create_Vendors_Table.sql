-- V12__Create_Vendors_Table.sql
-- Create vendors table and update procurement_batches to use vendor_id

-- Create vendors table
CREATE TABLE vendors (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    company_name VARCHAR(200),
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    mobile_number VARCHAR(20),
    fax_number VARCHAR(20),
    website VARCHAR(255),

    -- Address information
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    province VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50) DEFAULT 'Canada',

    -- Business information
    customer_id_with_vendor VARCHAR(100),
    business_number VARCHAR(50),
    tax_id VARCHAR(50),
    payment_terms VARCHAR(100),
    discount_rate DECIMAL(5, 2) DEFAULT 0.00,
    credit_limit DECIMAL(12, 2) DEFAULT 0.00,

    -- Categories and notes
    product_categories TEXT,
    description TEXT,
    notes TEXT,

    -- Status and tracking
    active BOOLEAN NOT NULL DEFAULT TRUE,
    preferred BOOLEAN DEFAULT FALSE,
    rating INT DEFAULT 0,

    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes
    INDEX idx_vendor_name (name),
    INDEX idx_vendor_company (company_name),
    INDEX idx_vendor_email (email),
    INDEX idx_vendor_active (active),
    INDEX idx_vendor_preferred (preferred),
    UNIQUE INDEX idx_vendor_customer_id (customer_id_with_vendor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create vendor purchase history summary table
CREATE TABLE vendor_purchase_history (
    id BINARY(16) PRIMARY KEY,
    vendor_id BINARY(16) NOT NULL,
    batch_id BINARY(16) NOT NULL,
    purchase_date DATETIME NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,

    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES procurement_batches(id) ON DELETE CASCADE,

    INDEX idx_vph_vendor (vendor_id),
    INDEX idx_vph_batch (batch_id),
    INDEX idx_vph_date (purchase_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add vendor_id to procurement_batches (keeping supplier for backward compatibility)
ALTER TABLE procurement_batches
ADD COLUMN vendor_id BINARY(16),
ADD FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE SET NULL,
ADD INDEX idx_batch_vendor (vendor_id);
