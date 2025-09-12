\c mosip_pms

-- Dropping unique index from pms.misp_license table if it exists
DROP INDEX IF EXISTS uq_policy_id_license_key_name;

-- Rollback script for the license_key_name column in misp_license table
ALTER TABLE pms.misp_license DROP COLUMN license_key_name;