-- Add new fields to vending_machines table
ALTER TABLE vending_machines
ADD COLUMN machine_id VARCHAR(50) UNIQUE NOT NULL AFTER id,
ADD COLUMN machine_name VARCHAR(200) AFTER machine_id,
ADD COLUMN model_number VARCHAR(100) AFTER model,
ADD COLUMN serial_number VARCHAR(100) AFTER model_number,
ADD COLUMN date_purchased DATE AFTER serial_number,
ADD COLUMN purchased_price DECIMAL(10,2) AFTER date_purchased,
ADD COLUMN `condition` ENUM('NEW', 'USED', 'REFURBISHED') AFTER purchased_price,
ADD COLUMN deployed TINYINT(1) NOT NULL DEFAULT 0 AFTER `condition`,
ADD COLUMN status ENUM('ACTIVE', 'BROKEN', 'INACTIVE', 'SOLD') NOT NULL DEFAULT 'ACTIVE' AFTER deployed;

-- Add index on status for faster queries
CREATE INDEX idx_machine_status ON vending_machines(status);
