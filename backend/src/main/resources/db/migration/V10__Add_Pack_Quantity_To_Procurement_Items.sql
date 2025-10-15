-- Add pack_quantity column to procurement_items table
-- This field stores how many units are in a pack/case (e.g., 32 cans in a case of Diet Pepsi)

ALTER TABLE procurement_items
ADD COLUMN pack_quantity INT NOT NULL DEFAULT 1
COMMENT 'Number of units in the pack/case purchased';

-- Update existing records to have pack_quantity = 1 (single unit)
UPDATE procurement_items SET pack_quantity = 1 WHERE pack_quantity IS NULL;
