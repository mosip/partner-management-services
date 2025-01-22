-- -------------------------------------------------------------------------------------------------
-- Database Name    : mosip_pms
-- Release Version 	: 1.4.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Swetha K
-- Created Date		: Aug-2024
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

--Dropping unique index from pms.device_detail table
DROP INDEX IF EXISTS pms.uk_devdtl_make_model_approval_status;

--Creating unique constraint for dprovider_id, dtype_code, dstype_code, make and model
ALTER TABLE pms.device_detail
ADD CONSTRAINT uk_devdtl_id 
UNIQUE (dprovider_id,dtype_code,dstype_code,make,model);

--Dropping unique index from pms.ftp_chip_detail table
DROP INDEX IF EXISTS pms.uk_fcdtl_make_model_approval_status;

--Creating unique constraint for foundational_trust_provider_id, make and model
ALTER TABLE pms.ftp_chip_detail
ADD CONSTRAINT uk_fcdtl_id 
UNIQUE (foundational_trust_provider_id, make, model);

