\c mosip_pms

-- Add new column for license_key_name in misp_license table
ALTER TABLE pms.misp_license ADD COLUMN license_key_name character varying(128);