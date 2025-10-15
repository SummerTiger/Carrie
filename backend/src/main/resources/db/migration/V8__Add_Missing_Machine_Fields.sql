-- Add missing boolean fields for hardware features
ALTER TABLE vending_machines
ADD COLUMN has_cash_bill_reader TINYINT(1) NOT NULL DEFAULT 0,
ADD COLUMN has_cashless_pos TINYINT(1) NOT NULL DEFAULT 0,
ADD COLUMN has_coin_changer TINYINT(1) NOT NULL DEFAULT 0;
