\c mosip_pms

-- Dropping unique index from pms.misp_license table if it exists
DROP INDEX IF EXISTS uq_policy_id_license_key_name;