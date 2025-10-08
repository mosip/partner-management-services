\c mosip_pms

-- Rollback script for the license_key_name column in misp_license table
ALTER TABLE pms.misp_license DROP COLUMN license_key_name;

-- Remove NOT NULL constraint from email_id
ALTER TABLE pms.partner
    ALTER COLUMN email_id DROP NOT NULL;