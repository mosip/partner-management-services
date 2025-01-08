-- -------------------------------------------------------------------------------------------------
-- Database Name    : mosip_pms
-- Release Version 	: 1.4.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Swetha K
-- Created Date		: Aug-2024
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------
\c :mosipdbname
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
