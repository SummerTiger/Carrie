-- V1__Initial_Schema.sql
-- Vending Inventory System Database Schema

-- Users table
CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    last_login DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User roles table
CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Vending machines table
CREATE TABLE vending_machines (
    id BINARY(16) PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    has_cash_bill_reader BOOLEAN DEFAULT FALSE,
    has_cashless_pos BOOLEAN DEFAULT FALSE,
    pos_serial_number VARCHAR(100),
    has_coin_changer BOOLEAN DEFAULT FALSE,
    coin_changer_serial_number VARCHAR(100),
    location_address VARCHAR(255) NOT NULL,
    location_city VARCHAR(100),
    location_province VARCHAR(50),
    location_postal_code VARCHAR(20),
    location_latitude DECIMAL(10, 8),
    location_longitude DECIMAL(11, 8),
    location_building_name VARCHAR(200),
    location_floor VARCHAR(50),
    location_contact_name VARCHAR(100),
    location_contact_phone VARCHAR(20),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_machine_location (location_address),
    INDEX idx_machine_brand (brand),
    UNIQUE INDEX idx_pos_serial (pos_serial_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Machine allowed categories
CREATE TABLE machine_allowed_categories (
    machine_id BINARY(16) NOT NULL,
    category VARCHAR(100) NOT NULL,
    PRIMARY KEY (machine_id, category),
    FOREIGN KEY (machine_id) REFERENCES vending_machines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Machine forbidden categories
CREATE TABLE machine_forbidden_categories (
    machine_id BINARY(16) NOT NULL,
    category VARCHAR(100) NOT NULL,
    PRIMARY KEY (machine_id, category),
    FOREIGN KEY (machine_id) REFERENCES vending_machines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Products table
CREATE TABLE products (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    unit_size VARCHAR(50),
    current_stock INT NOT NULL DEFAULT 0,
    minimum_stock INT DEFAULT 10,
    hst_exempt BOOLEAN NOT NULL DEFAULT FALSE,
    base_price DECIMAL(10, 2),
    description VARCHAR(1000),
    barcode VARCHAR(100),
    sku VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_category (category),
    INDEX idx_product_name (name),
    INDEX idx_product_stock (current_stock),
    UNIQUE INDEX idx_barcode (barcode),
    UNIQUE INDEX idx_sku (sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Machine product prices
CREATE TABLE machine_product_prices (
    id BINARY(16) PRIMARY KEY,
    machine_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (machine_id) REFERENCES vending_machines(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_machine_product (machine_id, product_id),
    INDEX idx_machine_price (machine_id),
    INDEX idx_product_price (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Procurement batches
CREATE TABLE procurement_batches (
    id BINARY(16) PRIMARY KEY,
    purchase_date DATETIME NOT NULL,
    supplier VARCHAR(200) NOT NULL,
    supplier_contact VARCHAR(200),
    total_hst DECIMAL(10, 2) DEFAULT 0.00,
    subtotal DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    invoice_number VARCHAR(100) UNIQUE,
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch_purchase_date (purchase_date),
    INDEX idx_batch_supplier (supplier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Procurement items
CREATE TABLE procurement_items (
    id BINARY(16) PRIMARY KEY,
    batch_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(10, 2) NOT NULL,
    hst_amount DECIMAL(10, 2) DEFAULT 0.00,
    hst_exempt BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (batch_id) REFERENCES procurement_batches(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_procurement_batch (batch_id),
    INDEX idx_procurement_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Batch products (many-to-many)
CREATE TABLE batch_products (
    batch_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    PRIMARY KEY (batch_id, product_id),
    FOREIGN KEY (batch_id) REFERENCES procurement_batches(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Restocking logs
CREATE TABLE restocking_logs (
    id BINARY(16) PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    machine_id BINARY(16) NOT NULL,
    total_slots INT,
    occupied_slots INT,
    empty_slots INT,
    total_products INT,
    low_stock_products INT DEFAULT 0,
    out_of_stock_products INT DEFAULT 0,
    notes TEXT,
    performed_by VARCHAR(100),
    cash_collected DECIMAL(10, 2),
    maintenance_performed BOOLEAN DEFAULT FALSE,
    maintenance_notes VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (machine_id) REFERENCES vending_machines(id) ON DELETE CASCADE,
    INDEX idx_restock_machine (machine_id),
    INDEX idx_restock_timestamp (timestamp),
    INDEX idx_restock_user (performed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Restock items
CREATE TABLE restock_items (
    id BINARY(16) PRIMARY KEY,
    restocking_log_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    stock_before INT,
    stock_after INT,
    expired_items_removed INT DEFAULT 0,
    FOREIGN KEY (restocking_log_id) REFERENCES restocking_logs(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_restock_log (restocking_log_id),
    INDEX idx_restock_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
