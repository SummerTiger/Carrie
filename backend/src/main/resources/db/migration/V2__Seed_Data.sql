-- V2__Seed_Data.sql
-- Initial seed data for development

-- Insert default admin user (password: admin123)
-- Note: In production, use bcrypt hashed password
INSERT INTO users (id, username, email, password, first_name, last_name, enabled)
VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'admin',
    'admin@vending.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- admin123
    'System',
    'Administrator',
    TRUE
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin';

-- Insert sample vending machines
INSERT INTO vending_machines (
    id, brand, model, has_cash_bill_reader, has_cashless_pos,
    location_address, location_city, location_province,
    location_postal_code, active
) VALUES
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Coca-Cola',
    'VendMax 3000',
    TRUE,
    TRUE,
    '123 Main Street',
    'Toronto',
    'ON',
    'M5H 2N2',
    TRUE
),
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Pepsi',
    'DrinkStation Pro',
    TRUE,
    FALSE,
    '456 University Ave',
    'Toronto',
    'ON',
    'M5G 1V7',
    TRUE
);

-- Insert sample product categories
INSERT INTO products (
    id, name, category, unit_size, current_stock, minimum_stock,
    hst_exempt, base_price, description, active
) VALUES
-- Beverages (HST Exempt)
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Coca-Cola 355ml',
    'Beverages',
    '355ml',
    100,
    20,
    TRUE,
    2.50,
    'Classic Coca-Cola in 355ml can',
    TRUE
),
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Pepsi 355ml',
    'Beverages',
    '355ml',
    95,
    20,
    TRUE,
    2.50,
    'Pepsi cola in 355ml can',
    TRUE
),
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Water 500ml',
    'Beverages',
    '500ml',
    150,
    30,
    TRUE,
    1.50,
    'Pure spring water',
    TRUE
),
-- Snacks (HST Applicable)
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Lays Chips Original 50g',
    'Snacks',
    '50g',
    80,
    15,
    FALSE,
    2.00,
    'Original flavor potato chips',
    TRUE
),
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Kit Kat Chocolate Bar',
    'Candy',
    '45g',
    120,
    25,
    FALSE,
    1.75,
    'Chocolate covered wafer bar',
    TRUE
),
(
    UNHEX(REPLACE(UUID(), '-', '')),
    'Trail Mix 100g',
    'Healthy Snacks',
    '100g',
    60,
    15,
    FALSE,
    3.50,
    'Mixed nuts and dried fruit',
    TRUE
);

-- Insert sample procurement batch
SET @batch_id = UNHEX(REPLACE(UUID(), '-', ''));
INSERT INTO procurement_batches (
    id, purchase_date, supplier, supplier_contact,
    invoice_number, subtotal, total_hst, total_amount
) VALUES (
    @batch_id,
    NOW(),
    'ABC Wholesale Distributors',
    'John Smith - 416-555-0100',
    'INV-2025-001',
    1000.00,
    130.00,
    1130.00
);

-- Link products to batch
INSERT INTO batch_products (batch_id, product_id)
SELECT @batch_id, id FROM products LIMIT 5;
