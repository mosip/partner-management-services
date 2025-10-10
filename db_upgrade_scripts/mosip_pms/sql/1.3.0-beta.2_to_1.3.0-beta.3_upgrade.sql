\c mosip_pms

-- Add new column for license_key_name in misp_license table
ALTER TABLE pms.misp_license ADD COLUMN license_key_name character varying(128);

-- Add NOT NULL constraint to email_id
ALTER TABLE pms.partner
    ALTER COLUMN email_id SET NOT NULL;

-- Update all records in pms.reg_device_sub_type to set cr_by = 'superadmin'
UPDATE pms.reg_device_sub_type
SET cr_by = 'superadmin';

-- Update all records in pms.reg_device_type to set cr_by = 'superadmin'
UPDATE pms.reg_device_type
SET cr_by = 'superadmin';

-- Drop the otp_transaction
DROP TABLE IF EXISTS pms.otp_transaction CASCADE;