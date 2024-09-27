-- -------------------------------------------------------------------------------------------------
-- Database Name    : mosip_pms
-- Release Version 	: 1.4.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Swetha K
-- Created Date		: Aug-2024
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

-- Delete rows from pms.device_detail_sbi where the device_detail_id does not exist in the pms.device_detail table.
-- This ensures that only existing device_detail_id references are retained in the pms.device_detail_sbi table.
delete FROM pms.device_detail_sbi where device_detail_id not in (SELECT distinct id FROM pms.device_detail);

-- Add a foreign key constraint fk_mapping_device_detail_id to enforce a relationship between device_detail_id in pms.device_detail_sbi and id in pms.device_detail.
-- The constraint prevents deletion or updating of referenced ids in pms.device_detail if they are used in pms.device_detail_sbi.
ALTER TABLE pms.device_detail_sbi ADD CONSTRAINT fk_mapping_device_detail_id FOREIGN KEY (device_detail_id)
REFERENCES pms.device_detail(id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Delete rows from pms.device_detail_sbi where the sbi_id does not exist in the pms.secure_biometric_interface table.
-- This ensures that only existing sbi_id references are retained in the pms.device_detail_sbi table.
delete FROM pms.device_detail_sbi where sbi_id not in (SELECT distinct id FROM pms.secure_biometric_interface);

-- Add a foreign key constraint fk_mapping_sbi_id to enforce a relationship between sbi_id in pms.device_detail_sbi and id in pms.secure_biometric_interface.
-- The constraint prevents deletion or updating of referenced ids in pms.secure_biometric_interface if they are used in pms.device_detail_sbi.
ALTER TABLE pms.device_detail_sbi ADD CONSTRAINT fk_mapping_sbi_id FOREIGN KEY (sbi_id)
REFERENCES pms.secure_biometric_interface(id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Dropping unique constraint from pms.device_detail table
ALTER TABLE pms.device_detail DROP CONSTRAINT uk_devdtl_id;

-- Creating unique index for make, model, and approval status
CREATE UNIQUE INDEX uk_devdtl_make_model_approval_status
ON pms.device_detail (dprovider_id,dtype_code,dstype_code,make,model)
WHERE approval_status != 'rejected' AND NOT (approval_status = 'approved' AND is_active = false);

-- Dropping unique constraint from pms.ftp_chip_detail table
ALTER TABLE pms.ftp_chip_detail DROP CONSTRAINT uk_fcdtl_id;

-- Creating unique index for make, model, and approval status
CREATE UNIQUE INDEX uk_fcdtl_make_model_approval_status
ON pms.ftp_chip_detail (foundational_trust_provider_id, make, model)
WHERE approval_status != 'rejected' AND NOT (approval_status = 'approved' AND is_active = false);