\c mosip_pms

-- Add new column for additional_config in oidc_client table
ALTER TABLE pms.oidc_client ADD COLUMN additional_config character varying;