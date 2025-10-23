-- V13: Create Category, Brand, and Model Management Tables
-- This migration creates proper lookup tables for categories, brands, and models
-- and migrates existing string data to the new structure

-- ============================================================
-- 1. CREATE PRODUCT CATEGORIES TABLE
-- ============================================================
CREATE TABLE product_categories (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_name (name),
    INDEX idx_category_active (active)
);

-- ============================================================
-- 2. CREATE PRODUCT BRANDS TABLE
-- ============================================================
CREATE TABLE product_brands (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    website VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_brand_name (name),
    INDEX idx_brand_active (active)
);

-- ============================================================
-- 3. CREATE MACHINE BRANDS TABLE
-- ============================================================
CREATE TABLE machine_brands (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    website VARCHAR(255),
    support_phone VARCHAR(20),
    support_email VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_machine_brand_name (name),
    INDEX idx_machine_brand_active (active)
);

-- ============================================================
-- 4. CREATE MACHINE MODELS TABLE
-- ============================================================
CREATE TABLE machine_models (
    id BINARY(16) PRIMARY KEY,
    brand_id BINARY(16),
    name VARCHAR(100) NOT NULL,
    model_number VARCHAR(100),
    description TEXT,
    capacity INT,
    dimensions VARCHAR(100),
    weight_kg DECIMAL(10,2),
    power_requirements VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_model_brand FOREIGN KEY (brand_id) REFERENCES machine_brands(id) ON DELETE SET NULL,
    INDEX idx_model_name (name),
    INDEX idx_model_brand (brand_id),
    INDEX idx_model_active (active),
    UNIQUE INDEX idx_brand_model (brand_id, name)
);

-- ============================================================
-- 5. MIGRATE EXISTING PRODUCT CATEGORIES
-- ============================================================
-- Extract unique categories from products table and insert them
INSERT INTO product_categories (id, name, description, icon, active, display_order)
SELECT
    UNHEX(REPLACE(UUID(), '-', '')) as id,
    category as name,
    CONCAT('Category for ', category) as description,
    CASE
        WHEN LOWER(category) IN ('beverages', 'drinks') THEN '🥤'
        WHEN LOWER(category) = 'snacks' THEN '🍿'
        WHEN LOWER(category) IN ('candy', 'chocolate') THEN '🍫'
        WHEN LOWER(category) = 'chips' THEN '🥔'
        WHEN LOWER(category) = 'water' THEN '💧'
        WHEN LOWER(category) IN ('soda', 'soft drinks') THEN '🥤'
        WHEN LOWER(category) = 'energy drinks' THEN '⚡'
        WHEN LOWER(category) = 'juice' THEN '🧃'
        WHEN LOWER(category) = 'coffee' THEN '☕'
        WHEN LOWER(category) = 'tea' THEN '🍵'
        WHEN LOWER(category) = 'food' THEN '🍱'
        WHEN LOWER(category) IN ('healthy', 'organic', 'healthy snacks') THEN '🥗'
        ELSE '📦'
    END as icon,
    TRUE as active,
    0 as display_order
FROM products
WHERE category IS NOT NULL AND category != ''
GROUP BY category
ORDER BY category;

-- ============================================================
-- 6. ADD NEW COLUMNS TO PRODUCTS TABLE
-- ============================================================
-- Add foreign key columns for category and brand
ALTER TABLE products
ADD COLUMN category_id BINARY(16),
ADD COLUMN brand_id BINARY(16),
ADD INDEX idx_product_category (category_id),
ADD INDEX idx_product_brand (brand_id),
ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_categories(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES product_brands(id) ON DELETE SET NULL;

-- ============================================================
-- 7. UPDATE PRODUCTS TO USE CATEGORY_ID
-- ============================================================
-- Link existing products to their categories
UPDATE products p
INNER JOIN product_categories pc ON p.category = pc.name
SET p.category_id = pc.id
WHERE p.category IS NOT NULL AND p.category != '';

-- ============================================================
-- 8. ADD NEW COLUMNS TO VENDING_MACHINES TABLE
-- ============================================================
-- Add foreign key columns for brand and model
ALTER TABLE vending_machines
ADD COLUMN brand_id BINARY(16),
ADD COLUMN model_id BINARY(16),
ADD INDEX idx_machine_brand (brand_id),
ADD INDEX idx_machine_model (model_id),
ADD CONSTRAINT fk_machine_brand FOREIGN KEY (brand_id) REFERENCES machine_brands(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_machine_model FOREIGN KEY (model_id) REFERENCES machine_models(id) ON DELETE SET NULL;

-- ============================================================
-- 9. NOTE: Old VARCHAR columns kept for backward compatibility
-- ============================================================
-- The old 'category' column in products table is kept for now
-- It can be removed in a future migration after verifying all data is migrated
-- Same for any brand/model VARCHAR columns if they exist
