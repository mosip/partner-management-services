\c mosip_pms

-- Add new column for additional configuration
ALTER TABLE pms.oidc_client ADD COLUMN additional_config jsonb;