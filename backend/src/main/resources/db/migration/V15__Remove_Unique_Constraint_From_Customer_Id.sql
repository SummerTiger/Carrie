-- Remove unique constraint on customer_id_with_vendor
-- Multiple vendors can assign the same customer ID independently
ALTER TABLE vendors DROP INDEX idx_vendor_customer_id;
