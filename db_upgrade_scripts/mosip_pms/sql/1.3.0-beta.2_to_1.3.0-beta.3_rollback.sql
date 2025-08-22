\c mosip_pms

-- Rollback script for the additional_config column in oidc_client table
ALTER TABLE pms.oidc_client DROP COLUMN additional_config;