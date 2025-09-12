\c mosip_pms

-- Creating unique index for policy_id and license_key_name
CREATE UNIQUE INDEX IF NOT EXISTS uq_policy_id_license_key_name
ON pms.misp_license (COALESCE(policy_id, 'N/A'), license_key_name);