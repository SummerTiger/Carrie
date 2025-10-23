-- Add delivery option fields to vendors table
ALTER TABLE vendors
    ADD COLUMN order_deliver BOOLEAN DEFAULT FALSE,
    ADD COLUMN curbside_pickup BOOLEAN DEFAULT FALSE,
    ADD COLUMN in_person_only BOOLEAN DEFAULT FALSE;
